package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JeePulseRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. Test Attempts ---

    fun observeUserAttempts(userId: String): Flow<List<TestAttempt>> {
        val path = "test_attempts"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { TestAttempt.fromSnapshot(it) }
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun recordTestAttempt(attempt: TestAttempt): Result<String> {
        val uid = requireUserId()
        val docId = if (attempt.id.isNotBlank()) attempt.id else "att_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val path = "test_attempts/$docId"
        val payload = mapOf(
            "userId" to uid,
            "testTitle" to attempt.testTitle.take(120).ifBlank { "PYQ Practice" },
            "testType" to attempt.testType,
            "subjectFilter" to attempt.subjectFilter.take(60).ifBlank { "All Subjects" },
            "totalQuestions" to attempt.totalQuestions.coerceIn(1, 200),
            "attemptedCount" to attempt.attemptedCount.coerceIn(0, 200),
            "correctCount" to attempt.correctCount.coerceIn(0, 200),
            "incorrectCount" to attempt.incorrectCount.coerceIn(0, 200),
            "scoreObtained" to attempt.scoreObtained.coerceIn(-200, 800),
            "maxScore" to attempt.maxScore.coerceIn(1, 800),
            "accuracyPercent" to attempt.accuracyPercent.coerceIn(0, 100),
            "timeSpentSeconds" to attempt.timeSpentSeconds.coerceIn(0, 36000),
            "xpEarned" to attempt.xpEarned.coerceIn(0, 50000),
            "weakTopics" to attempt.weakTopics.take(20).map { it.take(80) },
            "strongTopics" to attempt.strongTopics.take(20).map { it.take(80) },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            db.collection("test_attempts").document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun getAttemptById(attemptId: String): Result<TestAttempt> {
        val path = "test_attempts/$attemptId"
        return try {
            val snapshot = db.collection("test_attempts").document(attemptId).get().await()
            if (!snapshot.exists()) {
                Result.failure(NoSuchElementException("Attempt $attemptId not found"))
            } else {
                Result.success(TestAttempt.fromSnapshot(snapshot))
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    // --- 2. Gamification Leaderboard ---

    fun observeLeaderboard(): Flow<List<LeaderboardProfile>> {
        val path = "leaderboard"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { LeaderboardProfile.fromSnapshot(it) }
                    .sortedByDescending { it.xpPoints }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun upsertLeaderboardProfile(profile: LeaderboardProfile): Result<Unit> {
        val uid = requireUserId()
        val path = "leaderboard/$uid"
        return try {
            val docRef = db.collection("leaderboard").document(uid)
            val existingSnap = try {
                docRef.get().await()
            } catch (e: Exception) {
                null
            }
            if (existingSnap != null && existingSnap.exists()) {
                val updatePayload = mapOf(
                    "displayName" to profile.displayName.take(60).ifBlank { "JEE Aspirant" },
                    "xpPoints" to profile.xpPoints.coerceIn(0, 10000000),
                    "currentStreak" to profile.currentStreak.coerceIn(0, 3650),
                    "bestStreak" to profile.bestStreak.coerceIn(0, 3650),
                    "questionsSolved" to profile.questionsSolved.coerceIn(0, 1000000),
                    "correctAnswers" to profile.correctAnswers.coerceIn(0, 1000000),
                    "testsCompleted" to profile.testsCompleted.coerceIn(0, 100000),
                    "highScorePercent" to profile.highScorePercent.coerceIn(0, 100),
                    "badgesUnlocked" to profile.badgesUnlocked.take(30).map { it.take(60) },
                    "targetExam" to profile.targetExam,
                    "visibility" to "public",
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName.take(60).ifBlank { "JEE Aspirant" },
                    "xpPoints" to profile.xpPoints.coerceIn(0, 10000000),
                    "currentStreak" to profile.currentStreak.coerceIn(0, 3650),
                    "bestStreak" to profile.bestStreak.coerceIn(0, 3650),
                    "questionsSolved" to profile.questionsSolved.coerceIn(0, 1000000),
                    "correctAnswers" to profile.correctAnswers.coerceIn(0, 1000000),
                    "testsCompleted" to profile.testsCompleted.coerceIn(0, 100000),
                    "highScorePercent" to profile.highScorePercent.coerceIn(0, 100),
                    "badgesUnlocked" to profile.badgesUnlocked.take(30).map { it.take(60) },
                    "targetExam" to profile.targetExam,
                    "visibility" to "public",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- 3. Personalized Study Plan Tasks ---

    fun observeStudyTasks(userId: String): Flow<List<StudyPlanTask>> {
        val path = "study_tasks"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { StudyPlanTask.fromSnapshot(it) }
                    .sortedBy { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createStudyTask(task: StudyPlanTask): Result<String> {
        val uid = requireUserId()
        val docId = if (task.id.isNotBlank()) task.id else "task_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val path = "study_tasks/$docId"
        val payload = mapOf(
            "userId" to uid,
            "dayLabel" to task.dayLabel.take(40).ifBlank { "Day 1 • Today" },
            "subject" to task.subject,
            "topic" to task.topic.take(80).ifBlank { "Rotational Motion" },
            "activityType" to task.activityType,
            "targetPyqCount" to task.targetPyqCount.coerceIn(1, 100),
            "durationMinutes" to task.durationMinutes.coerceIn(5, 360),
            "priorityLevel" to task.priorityLevel,
            "completed" to task.completed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            db.collection("study_tasks").document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleStudyTaskCompleted(taskId: String, completed: Boolean): Result<Unit> {
        requireUserId()
        val path = "study_tasks/$taskId"
        val payload = mapOf(
            "completed" to completed,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            db.collection("study_tasks").document(taskId).update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudyTask(taskId: String): Result<Unit> {
        requireUserId()
        val path = "study_tasks/$taskId"
        return try {
            db.collection("study_tasks").document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- 4. Bookmarked PYQ Questions ---

    fun observeBookmarks(userId: String): Flow<List<BookmarkedQuestion>> {
        val path = "bookmarks"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { BookmarkedQuestion.fromSnapshot(it) }
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun saveBookmark(
        questionId: String,
        subject: String,
        topic: String,
        personalNote: String
    ): Result<String> {
        val uid = requireUserId()
        val safeQId = questionId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val docId = "bm_${uid.take(20)}_${safeQId.take(40)}"
        val path = "bookmarks/$docId"
        val payload = mapOf(
            "userId" to uid,
            "questionId" to questionId.take(80),
            "subject" to subject,
            "topic" to topic.take(80),
            "personalNote" to personalNote.take(500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            val docRef = db.collection("bookmarks").document(docId)
            val existing = try {
                docRef.get().await()
            } catch (e: Exception) {
                null
            }
            if (existing != null && existing.exists()) {
                docRef.update(
                    mapOf(
                        "personalNote" to personalNote.take(500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(payload).await()
            }
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(bookmarkId: String): Result<Unit> {
        requireUserId()
        val path = "bookmarks/$bookmarkId"
        return try {
            db.collection("bookmarks").document(bookmarkId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
