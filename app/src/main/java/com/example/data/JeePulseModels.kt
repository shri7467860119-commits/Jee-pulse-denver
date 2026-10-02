package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class LeaderboardProfile(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "JEE Aspirant",
    val xpPoints: Int = 0,
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val questionsSolved: Int = 0,
    val correctAnswers: Int = 0,
    val testsCompleted: Int = 0,
    val highScorePercent: Int = 0,
    val badgesUnlocked: List<String> = emptyList(),
    val targetExam: String = "JEE Main & Adv 2026",
    val visibility: String = "public",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val accuracyPercent: Int
        get() = if (questionsSolved > 0) ((correctAnswers * 100) / questionsSolved).coerceIn(0, 100) else 0

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): LeaderboardProfile {
            val rawBadges = doc.get("badgesUnlocked") as? List<*> ?: emptyList<Any>()
            return LeaderboardProfile(
                id = doc.id,
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "JEE Aspirant",
                xpPoints = (doc.getLong("xpPoints") ?: 0L).toInt(),
                currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt(),
                bestStreak = (doc.getLong("bestStreak") ?: 0L).toInt(),
                questionsSolved = (doc.getLong("questionsSolved") ?: 0L).toInt(),
                correctAnswers = (doc.getLong("correctAnswers") ?: 0L).toInt(),
                testsCompleted = (doc.getLong("testsCompleted") ?: 0L).toInt(),
                highScorePercent = (doc.getLong("highScorePercent") ?: 0L).toInt(),
                badgesUnlocked = rawBadges.mapNotNull { it as? String },
                targetExam = doc.getString("targetExam") ?: "JEE Main & Adv 2026",
                visibility = doc.getString("visibility") ?: "public",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

data class TestAttempt(
    val id: String = "",
    val userId: String = "",
    val testTitle: String = "",
    val testType: String = "PYQ_PRACTICE",
    val subjectFilter: String = "All Subjects",
    val totalQuestions: Int = 1,
    val attemptedCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val scoreObtained: Int = 0,
    val maxScore: Int = 4,
    val accuracyPercent: Int = 0,
    val timeSpentSeconds: Int = 0,
    val xpEarned: Int = 0,
    val weakTopics: List<String> = emptyList(),
    val strongTopics: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): TestAttempt {
            val rawWeak = doc.get("weakTopics") as? List<*> ?: emptyList<Any>()
            val rawStrong = doc.get("strongTopics") as? List<*> ?: emptyList<Any>()
            return TestAttempt(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                testTitle = doc.getString("testTitle") ?: "PYQ Session",
                testType = doc.getString("testType") ?: "PYQ_PRACTICE",
                subjectFilter = doc.getString("subjectFilter") ?: "All Subjects",
                totalQuestions = (doc.getLong("totalQuestions") ?: 1L).toInt(),
                attemptedCount = (doc.getLong("attemptedCount") ?: 0L).toInt(),
                correctCount = (doc.getLong("correctCount") ?: 0L).toInt(),
                incorrectCount = (doc.getLong("incorrectCount") ?: 0L).toInt(),
                scoreObtained = (doc.getLong("scoreObtained") ?: 0L).toInt(),
                maxScore = (doc.getLong("maxScore") ?: 4L).toInt(),
                accuracyPercent = (doc.getLong("accuracyPercent") ?: 0L).toInt(),
                timeSpentSeconds = (doc.getLong("timeSpentSeconds") ?: 0L).toInt(),
                xpEarned = (doc.getLong("xpEarned") ?: 0L).toInt(),
                weakTopics = rawWeak.mapNotNull { it as? String },
                strongTopics = rawStrong.mapNotNull { it as? String },
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

data class StudyPlanTask(
    val id: String = "",
    val userId: String = "",
    val dayLabel: String = "Day 1 • Today",
    val subject: String = "Physics",
    val topic: String = "Rotational Motion",
    val activityType: String = "PYQ_DRILL",
    val targetPyqCount: Int = 10,
    val durationMinutes: Int = 45,
    val priorityLevel: String = "HIGH_WEAK_AREA",
    val completed: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): StudyPlanTask {
            return StudyPlanTask(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                dayLabel = doc.getString("dayLabel") ?: "Day 1 • Today",
                subject = doc.getString("subject") ?: "Physics",
                topic = doc.getString("topic") ?: "Rotational Motion",
                activityType = doc.getString("activityType") ?: "PYQ_DRILL",
                targetPyqCount = (doc.getLong("targetPyqCount") ?: 10L).toInt(),
                durationMinutes = (doc.getLong("durationMinutes") ?: 45L).toInt(),
                priorityLevel = doc.getString("priorityLevel") ?: "HIGH_WEAK_AREA",
                completed = doc.getBoolean("completed") ?: false,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

data class BookmarkedQuestion(
    val id: String = "",
    val userId: String = "",
    val questionId: String = "",
    val subject: String = "Physics",
    val topic: String = "",
    val personalNote: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): BookmarkedQuestion {
            return BookmarkedQuestion(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                questionId = doc.getString("questionId") ?: "",
                subject = doc.getString("subject") ?: "Physics",
                topic = doc.getString("topic") ?: "",
                personalNote = doc.getString("personalNote") ?: "",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}
