package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.audio.SynthesizedSoundEngine
import com.example.data.firebase.ArchiveRepository
import com.example.data.local.PreferencesManager
import com.example.data.model.MuseumTab
import com.example.data.model.UiState
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOut
import com.example.ui.components.CinematicIntroScreen
import com.example.ui.components.FilmGrainOverlay
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.MuseumNavigationRail
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.LegendsScreen
import com.example.ui.screens.MemesScreen
import com.example.ui.screens.SoundLabScreen
import com.example.ui.screens.TimelineAndBattlesScreen
import com.example.ui.theme.InternetArchiveTheme
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.ArchiveViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            InternetArchiveTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember(context) { PreferencesManager(context.applicationContext) }
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var introDone by remember { mutableStateOf(false) }
    var introPreferenceLoaded by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    LaunchedEffect(Unit) {
        val skip = preferencesManager.skipIntroOnLaunch.first()
        if (skip) {
            introDone = true
        }
        introPreferenceLoaded = true
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = {},
            scope = scope
        )
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    if (!introPreferenceLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        )
        return
    }

    if (!introDone) {
        CinematicIntroScreen(
            onFinished = { rememberSkip ->
                introDone = true
                if (rememberSkip) {
                    scope.launch {
                        preferencesManager.setSkipIntroOnLaunch(true)
                    }
                }
            }
        )
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
    } else {
        key(user.uid) {
            AuthenticatedMuseumShell(
                currentUserId = user.uid,
                currentUserDisplayName = user.displayName ?: user.email?.substringBefore("@") ?: "Archivist",
                currentUserEmail = user.email,
                preferencesManager = preferencesManager,
                onSignOut = {
                    signOut(credentialManager, scope) {
                        currentUser = null
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedMuseumShell(
    currentUserId: String,
    currentUserDisplayName: String,
    currentUserEmail: String?,
    preferencesManager: PreferencesManager,
    onSignOut: () -> Unit
) {
    val viewModel: ArchiveViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ArchiveViewModel(
                    repository = ArchiveRepository(db),
                    preferencesManager = preferencesManager,
                    soundEngine = SynthesizedSoundEngine(),
                    currentUserId = currentUserId,
                    currentUserDisplayName = currentUserDisplayName
                )
            }
        }
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val replayIntroTrigger by viewModel.replayIntroTrigger.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val filteredMemes by viewModel.filteredMemes.collectAsStateWithLifecycle()
    val memeSearchQuery by viewModel.memeSearchQuery.collectAsStateWithLifecycle()
    val selectedMemeCategory by viewModel.selectedMemeCategory.collectAsStateWithLifecycle()
    val filteredSounds by viewModel.filteredSounds.collectAsStateWithLifecycle()
    val selectedSoundCategory by viewModel.selectedSoundCategory.collectAsStateWithLifecycle()
    val soundFavoritesOnly by viewModel.soundFavoritesOnly.collectAsStateWithLifecycle()
    val playingSoundId by viewModel.soundEngine.playingSoundId.collectAsStateWithLifecycle()
    val soundProgress by viewModel.soundEngine.progress.collectAsStateWithLifecycle()
    val waveformBars by viewModel.soundEngine.waveformBars.collectAsStateWithLifecycle()
    val soundVolume by viewModel.soundEngine.volume.collectAsStateWithLifecycle()
    val filteredMilestones by viewModel.filteredMilestones.collectAsStateWithLifecycle()
    val selectedTimelineYear by viewModel.selectedTimelineYear.collectAsStateWithLifecycle()
    val selectedTimelineMonth by viewModel.selectedTimelineMonth.collectAsStateWithLifecycle()
    val battlesState by viewModel.battlesState.collectAsStateWithLifecycle()
    val userVotes by viewModel.userVotesByBattleId.collectAsStateWithLifecycle()
    val approvedState by viewModel.approvedSubmissionsState.collectAsStateWithLifecycle()
    val mySubmissionsState by viewModel.mySubmissionsState.collectAsStateWithLifecycle()
    val pendingModerationState by viewModel.pendingModerationState.collectAsStateWithLifecycle()
    val isCuratorAccount by viewModel.isCuratorAccount.collectAsStateWithLifecycle()
    val curatorModeEnabled by viewModel.curatorModeEnabled.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingArtifact.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.toastMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (replayIntroTrigger) {
        BackHandler { viewModel.completeIntroReplay() }
        CinematicIntroScreen(
            onFinished = { rememberSkip ->
                viewModel.setSkipIntroPreference(rememberSkip)
                viewModel.completeIntroReplay()
            }
        )
        return
    }

    if (selectedTab != MuseumTab.EXPLORE) {
        BackHandler { viewModel.selectTab(MuseumTab.EXPLORE) }
    }

    val approvedCount = (approvedState as? UiState.Success)?.data?.size ?: 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            containerColor = VoidBlack,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (useRail) 12.dp else 82.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useRail) {
                        MuseumNavigationRail(
                            selectedTab = selectedTab,
                            onSelectTab = viewModel::selectTab
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        Crossfade(targetState = selectedTab, label = "museum_tabs") { tab ->
                            when (tab) {
                                MuseumTab.EXPLORE -> ExploreScreen(
                                    favoriteCount = favoriteIds.size,
                                    approvedCommunityCount = approvedCount,
                                    reducedMotion = reducedMotion,
                                    userEmail = currentUserEmail,
                                    onNavigateTab = viewModel::selectTab,
                                    onToggleReducedMotion = viewModel::toggleReducedMotion,
                                    onReplayIntro = viewModel::requestReplayIntro,
                                    onSignOut = onSignOut
                                )
                                MuseumTab.LEGENDS -> LegendsScreen(
                                    favoriteIds = favoriteIds,
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onJumpToMeme = { memeTitle ->
                                        viewModel.updateMemeSearch(memeTitle)
                                        viewModel.selectTab(MuseumTab.MEMES)
                                    }
                                )
                                MuseumTab.MEMES -> MemesScreen(
                                    memes = filteredMemes,
                                    searchQuery = memeSearchQuery,
                                    selectedCategory = selectedMemeCategory,
                                    favoriteIds = favoriteIds,
                                    onSearchChange = viewModel::updateMemeSearch,
                                    onSelectCategory = viewModel::selectMemeCategory,
                                    onToggleFavorite = viewModel::toggleFavorite
                                )
                                MuseumTab.SOUND_LAB -> SoundLabScreen(
                                    sounds = filteredSounds,
                                    selectedCategory = selectedSoundCategory,
                                    favoritesOnly = soundFavoritesOnly,
                                    favoriteIds = favoriteIds,
                                    playingSoundId = playingSoundId,
                                    progress = soundProgress,
                                    waveformBars = waveformBars,
                                    volume = soundVolume,
                                    onSelectCategory = viewModel::selectSoundCategory,
                                    onToggleFavoritesOnly = viewModel::toggleSoundFavoritesOnly,
                                    onTogglePlay = { viewModel.soundEngine.togglePlay(it) },
                                    onVolumeChange = { viewModel.soundEngine.setVolume(it) },
                                    onToggleFavorite = viewModel::toggleFavorite
                                )
                                MuseumTab.TIMELINE -> TimelineAndBattlesScreen(
                                    milestones = filteredMilestones,
                                    selectedYear = selectedTimelineYear,
                                    selectedMonth = selectedTimelineMonth,
                                    battlesState = battlesState,
                                    userVotesByBattleId = userVotes,
                                    onSelectYear = viewModel::selectTimelineYear,
                                    onSelectMonth = viewModel::selectTimelineMonth,
                                    onCastVote = viewModel::castVote
                                )
                                MuseumTab.COMMUNITY -> CommunityScreen(
                                    approvedState = approvedState,
                                    mySubmissionsState = mySubmissionsState,
                                    pendingModerationState = pendingModerationState,
                                    isCuratorAccount = isCuratorAccount,
                                    curatorModeEnabled = curatorModeEnabled,
                                    isSubmitting = isSubmitting,
                                    onSubmitArtifact = viewModel::submitCommunityArtifact,
                                    onSetCuratorMode = viewModel::setCuratorMode,
                                    onModerateSubmission = viewModel::moderateSubmission
                                )
                            }
                        }

                        if (!useRail) {
                            FloatingBottomNavBar(
                                selectedTab = selectedTab,
                                onSelectTab = viewModel::selectTab,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }

                FilmGrainOverlay(reducedMotion = reducedMotion)
            }
        }
    }
}
