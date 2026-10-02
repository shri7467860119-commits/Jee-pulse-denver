package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class JeePulseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun recordAndObserveTestAttempt_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = JeePulseRepository(firestore)

        val attempt = TestAttempt(
            testTitle = "JEE Main 2025 Physics Rotational Motion",
            testType = "PYQ_PRACTICE",
            subjectFilter = "Physics",
            totalQuestions = 10,
            attemptedCount = 9,
            correctCount = 7,
            incorrectCount = 2,
            scoreObtained = 26,
            maxScore = 40,
            accuracyPercent = 78,
            timeSpentSeconds = 480,
            xpEarned = 185,
            weakTopics = listOf("Rotational Motion"),
            strongTopics = listOf("Electrostatics")
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordTestAttempt(attempt)
        }
        assertTrue("Expected recordTestAttempt to succeed", createResult.isSuccess)
        val createdId = createResult.getOrThrow()

        val observedAttempts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserAttempts(aliceUid).first { list -> list.any { it.id == createdId } }
        }
        assertTrue(observedAttempts.any { it.id == createdId })
    }

    @Test
    fun getAttemptById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = JeePulseRepository(firestore)
        val createdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.recordTestAttempt(
                TestAttempt(
                    testTitle = "Alice Private Attempt",
                    testType = "FULL_MOCK",
                    subjectFilter = "All Subjects",
                    totalQuestions = 15,
                    attemptedCount = 12,
                    correctCount = 10,
                    incorrectCount = 2,
                    scoreObtained = 38,
                    maxScore = 60,
                    accuracyPercent = 83,
                    timeSpentSeconds = 900,
                    xpEarned = 250,
                    weakTopics = listOf("Matrices & Determinants"),
                    strongTopics = listOf("Electrostatics")
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = JeePulseRepository(firestore)
        val bobGetResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getAttemptById(createdId)
        }
        assertTrue("Cross-user access must fail", bobGetResult.isFailure)
        val ex = bobGetResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeUserAttempts_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = JeePulseRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserAttempts("unauth_user_123").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: Throwable) {
            val firestoreEx = generateSequence(e) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun upsertLeaderboardAndStudyTasks_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = JeePulseRepository(firestore)

        val leaderboardResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertLeaderboardProfile(
                LeaderboardProfile(
                    userId = aliceUid,
                    displayName = "Aarav IITB",
                    xpPoints = 540,
                    currentStreak = 4,
                    bestStreak = 7,
                    questionsSolved = 32,
                    correctAnswers = 26,
                    testsCompleted = 4,
                    highScorePercent = 88,
                    badgesUnlocked = listOf("first_step", "sniper_accuracy"),
                    targetExam = "JEE Main & Adv 2026"
                )
            )
        }
        assertTrue("Expected upsertLeaderboardProfile to succeed", leaderboardResult.isSuccess)

        val taskResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createStudyTask(
                StudyPlanTask(
                    dayLabel = "Day 1 • Today",
                    subject = "Physics",
                    topic = "Rotational Motion",
                    activityType = "PYQ_DRILL",
                    targetPyqCount = 12,
                    durationMinutes = 45,
                    priorityLevel = "HIGH_WEAK_AREA",
                    completed = false
                )
            )
        }
        assertTrue("Expected createStudyTask to succeed", taskResult.isSuccess)
        val taskId = taskResult.getOrThrow()

        val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleStudyTaskCompleted(taskId, true)
        }
        assertTrue("Expected toggleStudyTaskCompleted to succeed", toggleResult.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_jee@test.com"
        const val BOB_EMAIL = "bob_jee@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
