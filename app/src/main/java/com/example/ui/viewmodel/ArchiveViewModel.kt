package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SynthesizedSoundEngine
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.firebase.ArchiveRepository
import com.example.data.local.PreferencesManager
import com.example.data.model.BattleVote
import com.example.data.model.CommunitySubmission
import com.example.data.model.FavoriteItemType
import com.example.data.model.MemeArtifact
import com.example.data.model.MuseumTab
import com.example.data.model.SoundArtifact
import com.example.data.model.TimelineMilestone
import com.example.data.model.TrendBattle
import com.example.data.model.UiState
import com.example.data.model.UserFavorite
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModel(
    private val repository: ArchiveRepository,
    private val preferencesManager: PreferencesManager,
    val soundEngine: SynthesizedSoundEngine,
    val currentUserId: String,
    val currentUserDisplayName: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MuseumTab.EXPLORE)
    val selectedTab: StateFlow<MuseumTab> = _selectedTab.asStateFlow()

    private val _replayIntroTrigger = MutableStateFlow(false)
    val replayIntroTrigger: StateFlow<Boolean> = _replayIntroTrigger.asStateFlow()

    val skipIntroOnLaunch: StateFlow<Boolean?> = preferencesManager.skipIntroOnLaunch
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val reducedMotion: StateFlow<Boolean> = preferencesManager.reducedMotion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    // --- MEME SEARCH & FILTERING ---
    private val _memeSearchQuery = MutableStateFlow("")
    val memeSearchQuery: StateFlow<String> = _memeSearchQuery.asStateFlow()

    private val _selectedMemeCategory = MutableStateFlow("All")
    val selectedMemeCategory: StateFlow<String> = _selectedMemeCategory.asStateFlow()

    val filteredMemes: StateFlow<List<MemeArtifact>> = combine(
        _memeSearchQuery,
        _selectedMemeCategory
    ) { query, category ->
        val q = query.trim().lowercase()
        CuratedArchiveCatalog.memes.filter { meme ->
            val matchesCategory = category == "All" || meme.category.equals(category, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                meme.title.lowercase().contains(q) ||
                meme.summary.lowercase().contains(q) ||
                meme.tags.any { it.lowercase().contains(q) } ||
                meme.year.toString().contains(q)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CuratedArchiveCatalog.memes)

    // --- SOUND LAB FILTERING ---
    private val _selectedSoundCategory = MutableStateFlow("All")
    val selectedSoundCategory: StateFlow<String> = _selectedSoundCategory.asStateFlow()

    private val _soundFavoritesOnly = MutableStateFlow(false)
    val soundFavoritesOnly: StateFlow<Boolean> = _soundFavoritesOnly.asStateFlow()

    // --- TIMELINE FILTERING ---
    private val _selectedTimelineYear = MutableStateFlow<Int?>(null)
    val selectedTimelineYear: StateFlow<Int?> = _selectedTimelineYear.asStateFlow()

    private val _selectedTimelineMonth = MutableStateFlow("ALL")
    val selectedTimelineMonth: StateFlow<String> = _selectedTimelineMonth.asStateFlow()

    val filteredMilestones: StateFlow<List<TimelineMilestone>> = combine(
        _selectedTimelineYear,
        _selectedTimelineMonth
    ) { year, month ->
        CuratedArchiveCatalog.timelineMilestones.filter { item ->
            (year == null || item.year == year) &&
                (month == "ALL" || item.month.equals(month, ignoreCase = true))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        CuratedArchiveCatalog.timelineMilestones
    )

    // --- FIRESTORE FAVORITES ---
    val favoritesState: StateFlow<UiState<List<UserFavorite>>> = repository.observeFavorites(currentUserId)
        .map<List<UserFavorite>, UiState<List<UserFavorite>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Failed observing favorites", root)
            emit(UiState.Error(root.message ?: "Unable to sync favorites."))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val favoriteIds: StateFlow<Set<String>> = favoritesState
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.map { it.itemId }.toSet()
                else -> emptySet()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

    val filteredSounds: StateFlow<List<SoundArtifact>> = combine(
        _selectedSoundCategory,
        _soundFavoritesOnly,
        favoriteIds
    ) { category, favOnly, favs ->
        CuratedArchiveCatalog.sounds.filter { sound ->
            val catOk = category == "All" || sound.category.equals(category, ignoreCase = true)
            val favOk = !favOnly || favs.contains(sound.id)
            catOk && favOk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CuratedArchiveCatalog.sounds)

    // --- FIRESTORE TREND BATTLES ---
    val battlesState: StateFlow<UiState<List<TrendBattle>>> = repository.observeActiveBattles()
        .map<List<TrendBattle>, UiState<List<TrendBattle>>> { list ->
            UiState.Success(list.sortedByDescending { it.votesA + it.votesB })
        }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Failed observing battles", root)
            emit(UiState.Error(root.message ?: "Unable to load Trend Battles."))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val userVotesByBattleId: StateFlow<Map<String, BattleVote?>> = battlesState
        .flatMapLatest { state ->
            val battles = (state as? UiState.Success)?.data.orEmpty()
            if (battles.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    battles.map { battle ->
                        repository.observeUserVote(battle.id, currentUserId)
                            .map { vote -> battle.id to vote }
                            .catch { emit(battle.id to null) }
                    }
                ) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    // --- COMMUNITY SUBMISSIONS & CURATOR MODERATION ---
    private val _isCuratorAccount = MutableStateFlow(false)
    val isCuratorAccount: StateFlow<Boolean> = _isCuratorAccount.asStateFlow()

    private val _curatorModeEnabled = MutableStateFlow(false)
    val curatorModeEnabled: StateFlow<Boolean> = _curatorModeEnabled.asStateFlow()

    val approvedSubmissionsState: StateFlow<UiState<List<CommunitySubmission>>> =
        repository.observeApprovedSubmissions()
            .map<List<CommunitySubmission>, UiState<List<CommunitySubmission>>> { list ->
                UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Failed observing approved submissions", root)
                emit(UiState.Error(root.message ?: "Failed to load community exhibits."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val mySubmissionsState: StateFlow<UiState<List<CommunitySubmission>>> =
        repository.observeMySubmissions(currentUserId)
            .map<List<CommunitySubmission>, UiState<List<CommunitySubmission>>> { list ->
                UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Failed observing personal submissions", root)
                emit(UiState.Error(root.message ?: "Failed to load your submissions."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val pendingModerationState: StateFlow<UiState<List<CommunitySubmission>>> =
        combine(_isCuratorAccount, _curatorModeEnabled) { isCurator, enabled ->
            isCurator && enabled
        }.flatMapLatest { active ->
            if (!active) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observePendingSubmissions()
                    .map<List<CommunitySubmission>, UiState<List<CommunitySubmission>>> { list ->
                        UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
                    }
                    .catch { error ->
                        val firestoreError = unwrapFirestoreError(error)
                        if (firestoreError == null && error is CancellationException) throw error
                        val root = firestoreError ?: error
                        Log.w(TAG, "Failed observing moderation queue", root)
                        emit(UiState.Error(root.message ?: "Curator permissions required."))
                    }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Success(emptyList()))

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private val _isSubmittingArtifact = MutableStateFlow(false)
    val isSubmittingArtifact: StateFlow<Boolean> = _isSubmittingArtifact.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureBattlesSeeded()
            _isCuratorAccount.value = repository.checkIsCurator()
        }
    }

    fun selectTab(tab: MuseumTab) {
        _selectedTab.value = tab
    }

    fun setSkipIntroPreference(skip: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSkipIntroOnLaunch(skip)
        }
    }

    fun requestReplayIntro() {
        _replayIntroTrigger.value = true
    }

    fun completeIntroReplay() {
        _replayIntroTrigger.value = false
    }

    fun toggleReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReducedMotion(enabled)
        }
    }

    fun updateMemeSearch(query: String) {
        _memeSearchQuery.value = query
    }

    fun selectMemeCategory(category: String) {
        _selectedMemeCategory.value = category
    }

    fun selectSoundCategory(category: String) {
        _selectedSoundCategory.value = category
    }

    fun toggleSoundFavoritesOnly() {
        _soundFavoritesOnly.value = !_soundFavoritesOnly.value
    }

    fun selectTimelineYear(year: Int?) {
        _selectedTimelineYear.value = year
    }

    fun selectTimelineMonth(month: String) {
        _selectedTimelineMonth.value = month
    }

    fun toggleFavorite(itemId: String, itemType: FavoriteItemType) {
        val currentlyFav = favoriteIds.value.contains(itemId)
        viewModelScope.launch {
            val result = repository.toggleFavorite(
                userId = currentUserId,
                itemId = itemId,
                itemType = itemType,
                currentlyFavorite = currentlyFav
            )
            result.onSuccess {
                _toastMessages.tryEmit(
                    if (currentlyFav) "Removed from your Archive Vault." else "Saved to your Archive Vault."
                )
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Could not update favorite.")
            }
        }
    }

    fun castVote(battleId: String, choice: String) {
        if (userVotesByBattleId.value[battleId] != null) {
            _toastMessages.tryEmit("You have already cast your vote in this battle.")
            return
        }
        viewModelScope.launch {
            val result = repository.castBattleVote(battleId, choice)
            result.onSuccess {
                _toastMessages.tryEmit("Vote locked in the permanent ledger.")
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Vote rejected.")
            }
        }
    }

    fun submitCommunityArtifact(
        title: String,
        eraYear: Int,
        category: String,
        story: String,
        quote: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingArtifact.value = true
            val result = repository.submitArtifact(
                authorName = currentUserDisplayName,
                title = title,
                eraYear = eraYear,
                category = category,
                story = story,
                quote = quote
            )
            _isSubmittingArtifact.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Artifact submitted for Curator Moderation.")
                onSuccess()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Submission failed validation.")
            }
        }
    }

    fun setCuratorMode(enabled: Boolean) {
        if (enabled && !_isCuratorAccount.value) {
            _toastMessages.tryEmit("Curator mode requires authorized curator credentials.")
            return
        }
        _curatorModeEnabled.value = enabled
    }

    fun moderateSubmission(submissionId: String, approve: Boolean, curatorNote: String) {
        viewModelScope.launch {
            val result = repository.moderateSubmission(submissionId, approve, curatorNote)
            result.onSuccess {
                _toastMessages.tryEmit(
                    if (approve) "Artifact approved and published to museum." else "Artifact marked as rejected."
                )
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Moderation action failed.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    companion object {
        private const val TAG = "ArchiveViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
    }
}
