package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.BattleVote
import com.example.data.model.CommunitySubmission
import com.example.data.model.FavoriteItemType
import com.example.data.model.TrendBattle
import com.example.data.model.UserFavorite
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ArchiveRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- FAVORITES ---
    fun observeFavorites(userId: String): Flow<List<UserFavorite>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank." }
        val path = "users/$userId/favorites"
        emitAll(
            db.collection("users").document(userId).collection("favorites")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UserFavorite::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun toggleFavorite(
        userId: String,
        itemId: String,
        itemType: FavoriteItemType,
        currentlyFavorite: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/favorites/$itemId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            require(itemId.matches(VALID_ID_REGEX)) { "Invalid item ID." }
            val docRef = db.collection("users").document(uid).collection("favorites").document(itemId)
            if (currentlyFavorite) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "itemId" to itemId,
                    "itemType" to itemType.wireValue,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- TREND BATTLES ---
    suspend fun ensureBattlesSeeded(): Result<Unit> {
        return try {
            requireUserId()
            val existing = db.collection("battles")
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .await()
            if (existing.isEmpty) {
                val batch = db.batch()
                CuratedArchiveCatalog.defaultBattlesSeed.forEach { (id, title, details) ->
                    val (contenderA, contenderB, eraTag) = details
                    val docRef = db.collection("battles").document(id)
                    batch.set(
                        docRef,
                        mapOf(
                            "title" to title,
                            "contenderA" to contenderA,
                            "contenderB" to contenderB,
                            "eraTag" to eraTag,
                            "status" to "active",
                            "votesA" to 0L,
                            "votesB" to 0L,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "battles")
            Result.failure(e)
        }
    }

    fun observeActiveBattles(): Flow<List<TrendBattle>> = flow {
        val uid = requireUserId()
        val path = "battles"
        emitAll(
            db.collection("battles")
                .whereEqualTo("status", "active")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TrendBattle::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeUserVote(battleId: String, userId: String): Flow<BattleVote?> = flow {
        require(battleId.isNotBlank() && userId.isNotBlank())
        val path = "battles/$battleId/votes/$userId"
        emitAll(
            db.collection("battles").document(battleId)
                .collection("votes").document(userId)
                .snapshots()
                .map { doc ->
                    if (doc.exists()) {
                        doc.toObject(
                            BattleVote::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun castBattleVote(battleId: String, choice: String): Result<Unit> {
        val path = "battles/$battleId"
        return try {
            val uid = requireUserId()
            require(battleId.matches(VALID_ID_REGEX)) { "Invalid battle ID." }
            require(choice == "A" || choice == "B") { "Choice must be A or B." }

            val battleRef = db.collection("battles").document(battleId)
            val voteRef = battleRef.collection("votes").document(uid)

            val batch = db.batch()
            batch.set(
                voteRef,
                mapOf(
                    "userId" to uid,
                    "battleId" to battleId,
                    "choice" to choice,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            val incrementField = if (choice == "A") "votesA" else "votesB"
            batch.update(
                battleRef,
                mapOf(
                    incrementField to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- COMMUNITY SUBMISSIONS & CURATOR MODERATION ---
    fun observeApprovedSubmissions(): Flow<List<CommunitySubmission>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("submissions")
                .whereEqualTo("status", "approved")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CommunitySubmission::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, "submissions")
                    }
                    throw error
                }
        )
    }

    fun observeMySubmissions(userId: String): Flow<List<CommunitySubmission>> = flow {
        require(userId.isNotBlank())
        emitAll(
            db.collection("submissions")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CommunitySubmission::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, "submissions")
                    }
                    throw error
                }
        )
    }

    fun observePendingSubmissions(): Flow<List<CommunitySubmission>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("submissions")
                .whereEqualTo("status", "pending")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CommunitySubmission::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, "submissions")
                    }
                    throw error
                }
        )
    }

    suspend fun checkIsCurator(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.email == BOOTSTRAPPED_ADMIN_EMAIL && user.isEmailVerified) {
            return true
        }
        return try {
            val doc = db.collection("admins").document(user.uid).get().await()
            doc.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitArtifact(
        authorName: String,
        title: String,
        eraYear: Int,
        category: String,
        story: String,
        quote: String?
    ): Result<Unit> {
        var docPath = "submissions"
        return try {
            val uid = requireUserId()
            val cleanAuthor = authorName.trim().ifBlank { "Archivist" }.take(60)
            val cleanTitle = title.trim()
            val cleanStory = story.trim()
            val cleanQuote = quote?.trim()?.takeIf { it.isNotEmpty() }?.take(200)

            require(cleanTitle.length in 3..80) { "Title must be between 3 and 80 characters." }
            require(eraYear in 1990..2026) { "Era year must be between 1990 and 2026." }
            require(category in ALLOWED_CATEGORIES) { "Invalid category selected." }
            require(cleanStory.length in 10..500) { "Story must be between 10 and 500 characters." }

            val docRef = db.collection("submissions").document()
            docPath = docRef.path
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "authorName" to cleanAuthor,
                "title" to cleanTitle,
                "eraYear" to eraYear.toLong(),
                "category" to category,
                "story" to cleanStory,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanQuote != null) {
                payload["quote"] = cleanQuote
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun moderateSubmission(
        submissionId: String,
        approved: Boolean,
        curatorNote: String
    ): Result<Unit> {
        val docPath = "submissions/$submissionId"
        return try {
            requireUserId()
            require(submissionId.matches(VALID_ID_REGEX)) { "Invalid submission ID." }
            val statusValue = if (approved) "approved" else "rejected"
            val cleanNote = curatorNote.trim().take(240)
            val updateMap = mutableMapOf<String, Any>(
                "status" to statusValue,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanNote.isNotEmpty()) {
                updateMap["curatorNote"] = cleanNote
            }
            db.collection("submissions").document(submissionId)
                .update(updateMap)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    companion object {
        private const val BOOTSTRAPPED_ADMIN_EMAIL = "salmankheri9@gmail.com"
        private val VALID_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
        private val ALLOWED_CATEGORIES = setOf("Meme", "Legend", "Sound", "Lost Internet", "Trend")
    }
}
