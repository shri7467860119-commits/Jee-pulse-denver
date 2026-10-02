package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkedQuestion
import com.example.data.JeePulseRepository
import com.example.data.LeaderboardProfile
import com.example.data.PyqQuestion
import com.example.data.PyqQuestionBank
import com.example.data.QuestionType
import com.example.data.StudyPlanTask
import com.example.data.TestAttempt
import com.example.data.TopicConceptGuide
import com.example.network.GeminiAiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class ActiveTestSession(
    val title: String,
    val testType: String, // "PYQ_PRACTICE", "FULL_MOCK", "AI_CUSTOM_MOCK", "STUDY_PLAN_DRILL"
    val subjectFilter: String,
    val isPracticeMode: Boolean, // Practice mode allows instant solution check; Mock mode evaluates at end
    val questions: List<PyqQuestion>,
    val currentIndex: Int = 0,
    val selectedOptions: Map<String, Int> = emptyMap(), // questionId -> optionIndex (0..3)
    val numericalInputs: Map<String, String> = emptyMap(), // questionId -> numeric string
    val markedForReview: Set<String> = emptySet(),
    val revealedSolutions: Set<String> = emptySet(),
    val perQuestionSeconds: Map<String, Int> = emptyMap(),
    val totalDurationSeconds: Int = 900,
    val remainingSeconds: Int = 900,
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isSubmitted: Boolean = false,
    val lastSubmissionSummary: SessionResultSummary? = null
)

data class SessionResultSummary(
    val testTitle: String,
    val testType: String,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val scoreObtained: Int,
    val maxScore: Int,
    val accuracyPercent: Int,
    val timeSpentSeconds: Int,
    val xpEarned: Int,
    val newlyUnlockedBadges: List<String>,
    val weakTopics: List<String>,
    val strongTopics: List<String>
)

data class TopicMasteryInsight(
    val subject: String,
    val topic: String,
    val questionsAttempted: Int,
    val correctCount: Int,
    val accuracyPercent: Int,
    val avgTimeSeconds: Int,
    val isWeakArea: Boolean,
    val conceptGuide: TopicConceptGuide
)

class JeePulseViewModel(
    private val repository: JeePulseRepository,
    private val currentUserId: String,
    private val userDisplayName: String
) : ViewModel() {

    // Two-Tier Kotlin Flow Error Handling for all 4 Firestore collections
    val attemptsState: StateFlow<UiState<List<TestAttempt>>> = repository.observeUserAttempts(currentUserId)
        .map<List<TestAttempt>, UiState<List<TestAttempt>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing user test attempts", e)
            emit(UiState.Error(e.message ?: "Unable to load test history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val leaderboardState: StateFlow<UiState<List<LeaderboardProfile>>> = repository.observeLeaderboard()
        .map<List<LeaderboardProfile>, UiState<List<LeaderboardProfile>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing leaderboard", e)
            emit(UiState.Error(e.message ?: "Unable to load leaderboard"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val studyTasksState: StateFlow<UiState<List<StudyPlanTask>>> = repository.observeStudyTasks(currentUserId)
        .map<List<StudyPlanTask>, UiState<List<StudyPlanTask>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing study plan tasks", e)
            emit(UiState.Error(e.message ?: "Unable to load study plan"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val bookmarksState: StateFlow<UiState<List<BookmarkedQuestion>>> = repository.observeBookmarks(currentUserId)
        .map<List<BookmarkedQuestion>, UiState<List<BookmarkedQuestion>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing bookmarks", e)
            emit(UiState.Error(e.message ?: "Unable to load bookmarks"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Filter state for PYQ Arena
    private val _selectedSubjectFilter = MutableStateFlow("All Subjects")
    val selectedSubjectFilter: StateFlow<String> = _selectedSubjectFilter.asStateFlow()

    private val _selectedTopicFilter = MutableStateFlow("All Topics")
    val selectedTopicFilter: StateFlow<String> = _selectedTopicFilter.asStateFlow()

    private val _selectedDifficultyFilter = MutableStateFlow("All")
    val selectedDifficultyFilter: StateFlow<String> = _selectedDifficultyFilter.asStateFlow()

    // Active Timed Exam / Practice Session
    private val _activeSession = MutableStateFlow<ActiveTestSession?>(null)
    val activeSession: StateFlow<ActiveTestSession?> = _activeSession.asStateFlow()

    private var timerJob: Job? = null

    // AI Generation states
    private val _isGeneratingAiMock = MutableStateFlow(false)
    val isGeneratingAiMock: StateFlow<Boolean> = _isGeneratingAiMock.asStateFlow()

    private val _aiConceptBoosterMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiConceptBoosterMap: StateFlow<Map<String, String>> = _aiConceptBoosterMap.asStateFlow()

    private val _loadingConceptTopic = MutableStateFlow<String?>(null)
    val loadingConceptTopic: StateFlow<String?> = _loadingConceptTopic.asStateFlow()

    private val _isGeneratingStudyPlan = MutableStateFlow(false)
    val isGeneratingStudyPlan: StateFlow<Boolean> = _isGeneratingStudyPlan.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private var hasSeededInitialUserData = false

    init {
        seedInitialProfileAndPlanIfNeeded()
    }

    private fun seedInitialProfileAndPlanIfNeeded() {
        if (hasSeededInitialUserData) return
        hasSeededInitialUserData = true

        viewModelScope.launch {
            // Ensure user has a public Leaderboard entry and starter Study Plan tasks if none exist yet
            delay(800)
            val currentLeaderboard = (leaderboardState.value as? UiState.Success)?.data ?: emptyList()
            val myProfile = currentLeaderboard.firstOrNull { it.userId == currentUserId }
            if (myProfile == null) {
                repository.upsertLeaderboardProfile(
                    LeaderboardProfile(
                        userId = currentUserId,
                        displayName = userDisplayName.ifBlank { "JEE Aspirant" }.take(60),
                        xpPoints = 120,
                        currentStreak = 1,
                        bestStreak = 1,
                        questionsSolved = 0,
                        correctAnswers = 0,
                        testsCompleted = 0,
                        highScorePercent = 0,
                        badgesUnlocked = emptyList(),
                        targetExam = "JEE Main & Adv 2026"
                    )
                )
            }

            val currentTasks = (studyTasksState.value as? UiState.Success)?.data
            if (currentTasks != null && currentTasks.isEmpty()) {
                val starterTasks = listOf(
                    StudyPlanTask(
                        dayLabel = "Day 1 • Today (Weak Area Priority)",
                        subject = "Physics",
                        topic = "Rotational Motion",
                        activityType = "PYQ_DRILL",
                        targetPyqCount = 12,
                        durationMinutes = 45,
                        priorityLevel = "HIGH_WEAK_AREA",
                        completed = false
                    ),
                    StudyPlanTask(
                        dayLabel = "Day 1 • Evening Concept Fix",
                        subject = "Chemistry",
                        topic = "Organic GOC & Carbonyls",
                        activityType = "CONCEPT_REVIEW",
                        targetPyqCount = 10,
                        durationMinutes = 40,
                        priorityLevel = "HIGH_WEAK_AREA",
                        completed = false
                    ),
                    StudyPlanTask(
                        dayLabel = "Day 2 • Morning Speed Drill",
                        subject = "Mathematics",
                        topic = "Definite Integration",
                        activityType = "PYQ_DRILL",
                        targetPyqCount = 15,
                        durationMinutes = 60,
                        priorityLevel = "HIGH_WEAK_AREA",
                        completed = false
                    ),
                    StudyPlanTask(
                        dayLabel = "Day 3 • Formula & Nernst Mastery",
                        subject = "Chemistry",
                        topic = "Electrochemistry & Kinetics",
                        activityType = "FORMULA_REVISION",
                        targetPyqCount = 12,
                        durationMinutes = 45,
                        priorityLevel = "MEDIUM_PRACTICE",
                        completed = false
                    ),
                    StudyPlanTask(
                        dayLabel = "Day 4 • Full AI Weak-Area Mock",
                        subject = "Physics",
                        topic = "Electrostatics",
                        activityType = "AI_MOCK_TEST",
                        targetPyqCount = 15,
                        durationMinutes = 45,
                        priorityLevel = "REVISION",
                        completed = false
                    )
                )
                starterTasks.forEach { repository.createStudyTask(it) }
            }
        }
    }

    fun dismissStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubjectFilter.value = subject
        _selectedTopicFilter.value = "All Topics"
    }

    fun setTopicFilter(topic: String) {
        _selectedTopicFilter.value = topic
    }

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficultyFilter.value = difficulty
    }

    fun getFilteredQuestions(): List<PyqQuestion> {
        val subj = _selectedSubjectFilter.value
        val top = _selectedTopicFilter.value
        val diff = _selectedDifficultyFilter.value
        return PyqQuestionBank.allQuestions.filter { q ->
            val matchSubj = subj == "All Subjects" || q.subject.equals(subj, ignoreCase = true)
            val matchTop = top == "All Topics" || q.topic.equals(top, ignoreCase = true)
            val matchDiff = diff == "All" || q.difficulty.equals(diff, ignoreCase = true)
            matchSubj && matchTop && matchDiff
        }
    }

    // --- Timed PYQ Practice & Mock Session Control ---

    fun startPyqSession(
        title: String,
        testType: String,
        subjectFilter: String,
        questions: List<PyqQuestion>,
        isPracticeMode: Boolean
    ) {
        if (questions.isEmpty()) return
        val totalTime = questions.sumOf { it.targetTimeSeconds }.coerceAtLeast(180)
        _activeSession.value = ActiveTestSession(
            title = title,
            testType = testType,
            subjectFilter = subjectFilter,
            isPracticeMode = isPracticeMode,
            questions = questions,
            currentIndex = 0,
            totalDurationSeconds = totalTime,
            remainingSeconds = totalTime,
            elapsedSeconds = 0,
            isPaused = false,
            isSubmitted = false
        )
        startTimerLoop()
    }

    fun startTopicDrill(subject: String, topic: String, isPracticeMode: Boolean = true) {
        val matching = PyqQuestionBank.allQuestions.filter {
            it.topic.contains(topic, ignoreCase = true) || topic.contains(it.topic, ignoreCase = true)
        }.ifEmpty {
            PyqQuestionBank.allQuestions.filter { it.subject.equals(subject, ignoreCase = true) }
        }.ifEmpty {
            PyqQuestionBank.allQuestions
        }
        startPyqSession(
            title = "$topic • PYQ Drill",
            testType = if (isPracticeMode) "PYQ_PRACTICE" else "STUDY_PLAN_DRILL",
            subjectFilter = subject,
            questions = matching,
            isPracticeMode = isPracticeMode
        )
    }

    fun startFullMockExam() {
        val questions = PyqQuestionBank.allQuestions.shuffled()
        startPyqSession(
            title = "JEE 2026 Full Multi-Subject Mock",
            testType = "FULL_MOCK",
            subjectFilter = "All Subjects",
            questions = questions,
            isPracticeMode = false
        )
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val session = _activeSession.value ?: break
                if (session.isSubmitted) break
                if (!session.isPaused) {
                    val currentQ = session.questions.getOrNull(session.currentIndex)
                    val updatedPerQuestion = if (currentQ != null) {
                        val prev = session.perQuestionSeconds[currentQ.id] ?: 0
                        session.perQuestionSeconds + (currentQ.id to (prev + 1))
                    } else {
                        session.perQuestionSeconds
                    }
                    val nextRemaining = (session.remainingSeconds - 1).coerceAtLeast(0)
                    _activeSession.update { cur ->
                        cur?.copy(
                            remainingSeconds = nextRemaining,
                            elapsedSeconds = cur.elapsedSeconds + 1,
                            perQuestionSeconds = updatedPerQuestion
                        )
                    }
                    if (nextRemaining == 0) {
                        submitActiveSession()
                        break
                    }
                }
            }
        }
    }

    fun toggleSessionPause() {
        _activeSession.update { it?.copy(isPaused = !it.isPaused) }
    }

    fun selectMcqOption(questionId: String, optionIndex: Int) {
        _activeSession.update { session ->
            if (session == null || session.isSubmitted) return@update session
            val currentSelection = session.selectedOptions[questionId]
            val updatedMap = if (currentSelection == optionIndex) {
                session.selectedOptions - questionId // allow clearing response like real JEE CBT
            } else {
                session.selectedOptions + (questionId to optionIndex)
            }
            session.copy(selectedOptions = updatedMap)
        }
    }

    fun updateNumericalInput(questionId: String, value: String) {
        _activeSession.update { session ->
            if (session == null || session.isSubmitted) return@update session
            session.copy(numericalInputs = session.numericalInputs + (questionId to value.trim()))
        }
    }

    fun toggleMarkForReview(questionId: String) {
        _activeSession.update { session ->
            if (session == null) return@update null
            val updated = if (questionId in session.markedForReview) {
                session.markedForReview - questionId
            } else {
                session.markedForReview + questionId
            }
            session.copy(markedForReview = updated)
        }
    }

    fun toggleRevealSolution(questionId: String) {
        _activeSession.update { session ->
            if (session == null) return@update null
            val updated = if (questionId in session.revealedSolutions) {
                session.revealedSolutions - questionId
            } else {
                session.revealedSolutions + questionId
            }
            session.copy(revealedSolutions = updated)
        }
    }

    fun navigateToQuestionIndex(index: Int) {
        _activeSession.update { session ->
            if (session == null) return@update null
            session.copy(currentIndex = index.coerceIn(0, session.questions.lastIndex))
        }
    }

    fun closeActiveSession() {
        timerJob?.cancel()
        _activeSession.value = null
    }

    fun submitActiveSession() {
        val session = _activeSession.value ?: return
        if (session.isSubmitted) return
        timerJob?.cancel()

        var attempted = 0
        var correct = 0
        var incorrect = 0
        var netScore = 0
        val maxScore = session.questions.size * 4
        val topicStats = mutableMapOf<String, Pair<Int, Int>>() // topic -> (correct, total)

        session.questions.forEach { q ->
            val prevStat = topicStats[q.topic] ?: (0 to 0)
            when (q.questionType) {
                QuestionType.MCQ -> {
                    val chosen = session.selectedOptions[q.id]
                    if (chosen != null) {
                        attempted++
                        if (chosen == q.correctOptionIndex) {
                            correct++
                            netScore += q.questionType.positiveMarks
                            topicStats[q.topic] = (prevStat.first + 1) to (prevStat.second + 1)
                        } else {
                            incorrect++
                            netScore -= q.questionType.negativeMarks
                            topicStats[q.topic] = prevStat.first to (prevStat.second + 1)
                        }
                    } else {
                        topicStats[q.topic] = prevStat.first to (prevStat.second + 1)
                    }
                }
                QuestionType.NUMERICAL -> {
                    val entered = session.numericalInputs[q.id]?.trim().orEmpty()
                    if (entered.isNotEmpty()) {
                        attempted++
                        val enteredNum = entered.toDoubleOrNull()
                        val targetNum = q.numericalAnswer.toDoubleOrNull()
                        val isRight = if (enteredNum != null && targetNum != null) {
                            kotlin.math.abs(enteredNum - targetNum) < 0.01
                        } else {
                            entered.equals(q.numericalAnswer.trim(), ignoreCase = true)
                        }
                        if (isRight) {
                            correct++
                            netScore += q.questionType.positiveMarks
                            topicStats[q.topic] = (prevStat.first + 1) to (prevStat.second + 1)
                        } else {
                            incorrect++
                            topicStats[q.topic] = prevStat.first to (prevStat.second + 1)
                        }
                    } else {
                        topicStats[q.topic] = prevStat.first to (prevStat.second + 1)
                    }
                }
            }
        }

        val accuracy = if (attempted > 0) ((correct * 100) / attempted).coerceIn(0, 100) else 0
        val weakTopics = topicStats.filter { (_, pair) ->
            val topicAcc = if (pair.second > 0) (pair.first * 100) / pair.second else 0
            topicAcc < 70
        }.keys.toList()
        val strongTopics = topicStats.filter { (_, pair) ->
            val topicAcc = if (pair.second > 0) (pair.first * 100) / pair.second else 0
            topicAcc >= 70
        }.keys.toList()

        // Gamification XP calculation
        var xpEarned = (correct * 20) + (attempted * 5) + 50
        if (accuracy >= 80 && attempted >= 2) xpEarned += 40
        if (session.testType == "AI_CUSTOM_MOCK") xpEarned += 35

        // Update Leaderboard & Check Newly Unlocked Badges
        val currentLeaderboard = (leaderboardState.value as? UiState.Success)?.data ?: emptyList()
        val existingProfile = currentLeaderboard.firstOrNull { it.userId == currentUserId }
            ?: LeaderboardProfile(
                userId = currentUserId,
                displayName = userDisplayName.ifBlank { "JEE Aspirant" }
            )

        val newQuestionsSolved = existingProfile.questionsSolved + attempted
        val newCorrectAnswers = existingProfile.correctAnswers + correct
        val newTestsCompleted = existingProfile.testsCompleted + 1
        val newStreak = (existingProfile.currentStreak + 1).coerceAtLeast(1)
        val newBestStreak = maxOf(existingProfile.bestStreak, newStreak)
        val scorePercent = if (maxScore > 0) ((netScore.coerceAtLeast(0) * 100) / maxScore).coerceIn(0, 100) else 0
        val newHighScorePercent = maxOf(existingProfile.highScorePercent, scorePercent)

        val badgesSet = existingProfile.badgesUnlocked.toMutableSet()
        val newlyUnlocked = mutableListOf<String>()

        fun unlockBadge(id: String) {
            if (badgesSet.add(id)) {
                newlyUnlocked.add(id)
                val bonus = PyqQuestionBank.milestoneBadges.firstOrNull { it.id == id }?.xpBonus ?: 50
                xpEarned += bonus
            }
        }

        if (newTestsCompleted >= 1) unlockBadge("first_step")
        if (newQuestionsSolved >= 20) unlockBadge("pyq_warrior")
        if (accuracy >= 80 && attempted >= 2) unlockBadge("sniper_accuracy")
        if (newStreak >= 3) unlockBadge("streak_3")
        if (newStreak >= 7) unlockBadge("streak_7")
        if (session.testType == "AI_CUSTOM_MOCK") unlockBadge("ai_pioneer")
        if (existingProfile.xpPoints + xpEarned >= 1000) unlockBadge("jee_titan")

        val summary = SessionResultSummary(
            testTitle = session.title,
            testType = session.testType,
            totalQuestions = session.questions.size,
            attemptedCount = attempted,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = session.questions.size - attempted,
            scoreObtained = netScore,
            maxScore = maxScore,
            accuracyPercent = accuracy,
            timeSpentSeconds = session.elapsedSeconds.coerceAtLeast(1),
            xpEarned = xpEarned,
            newlyUnlockedBadges = newlyUnlocked,
            weakTopics = weakTopics,
            strongTopics = strongTopics
        )

        _activeSession.update {
            it?.copy(
                isSubmitted = true,
                isPaused = true,
                revealedSolutions = session.questions.map { q -> q.id }.toSet(),
                lastSubmissionSummary = summary
            )
        }

        viewModelScope.launch {
            repository.recordTestAttempt(
                TestAttempt(
                    userId = currentUserId,
                    testTitle = session.title,
                    testType = session.testType,
                    subjectFilter = session.subjectFilter,
                    totalQuestions = session.questions.size,
                    attemptedCount = attempted,
                    correctCount = correct,
                    incorrectCount = incorrect,
                    scoreObtained = netScore,
                    maxScore = maxScore,
                    accuracyPercent = accuracy,
                    timeSpentSeconds = session.elapsedSeconds.coerceAtLeast(1),
                    xpEarned = xpEarned,
                    weakTopics = weakTopics,
                    strongTopics = strongTopics
                )
            )

            repository.upsertLeaderboardProfile(
                existingProfile.copy(
                    displayName = userDisplayName.ifBlank { existingProfile.displayName }.take(60),
                    xpPoints = existingProfile.xpPoints + xpEarned,
                    currentStreak = newStreak,
                    bestStreak = newBestStreak,
                    questionsSolved = newQuestionsSolved,
                    correctAnswers = newCorrectAnswers,
                    testsCompleted = newTestsCompleted,
                    highScorePercent = newHighScorePercent,
                    badgesUnlocked = badgesSet.toList()
                )
            )
        }
    }

    // --- AI Custom Mock Test Generator ---

    fun generateAndLaunchAiMockTest(
        subjectFilter: String,
        focusTopics: List<String>,
        difficulty: String,
        questionCount: Int
    ) {
        if (_isGeneratingAiMock.value) return
        _isGeneratingAiMock.value = true
        viewModelScope.launch {
            val (questions, usedLiveGemini) = GeminiAiService.generateCustomAiMockTest(
                subjectFilter = subjectFilter,
                focusTopics = focusTopics,
                difficulty = difficulty,
                questionCount = questionCount
            )
            _isGeneratingAiMock.value = false
            val tagNote = if (usedLiveGemini) "Gemini AI Custom Mock" else "Adaptive Weak-Area Mock"
            startPyqSession(
                title = "$tagNote • $subjectFilter ($difficulty)",
                testType = "AI_CUSTOM_MOCK",
                subjectFilter = subjectFilter,
                questions = questions,
                isPracticeMode = false
            )
        }
    }

    // --- Weak-Area Concept Explainer & AI Concept Booster ---

    fun computeTopicMasteryInsights(attempts: List<TestAttempt>): List<TopicMasteryInsight> {
        val weakCounts = mutableMapOf<String, Int>()
        val strongCounts = mutableMapOf<String, Int>()

        attempts.forEach { att ->
            att.weakTopics.forEach { t -> weakCounts[t] = (weakCounts[t] ?: 0) + 1 }
            att.strongTopics.forEach { t -> strongCounts[t] = (strongCounts[t] ?: 0) + 1 }
        }

        return PyqQuestionBank.conceptGuides.mapIndexed { idx, guide ->
            val w = weakCounts[guide.topic] ?: 0
            val s = strongCounts[guide.topic] ?: 0
            val totalSessions = w + s
            // Provide realistic baseline insights before/alongside user attempts so weak areas are immediately actionable
            val accuracy = when {
                totalSessions > 0 -> ((s * 85 + w * 42) / totalSessions).coerceIn(25, 96)
                idx % 3 == 0 -> 48 // Default weak priority topic
                idx % 3 == 1 -> 62 // Moderate weak topic
                else -> 82 // Strong topic
            }
            val avgTime = when {
                accuracy < 55 -> 142
                accuracy < 70 -> 118
                else -> 88
            }
            TopicMasteryInsight(
                subject = guide.subject,
                topic = guide.topic,
                questionsAttempted = (totalSessions * 4).coerceAtLeast(if (idx % 2 == 0) 6 else 8),
                correctCount = (((totalSessions * 4).coerceAtLeast(6) * accuracy) / 100),
                accuracyPercent = accuracy,
                avgTimeSeconds = avgTime,
                isWeakArea = accuracy < 68,
                conceptGuide = guide
            )
        }.sortedBy { it.accuracyPercent }
    }

    fun requestAiConceptBooster(subject: String, topic: String, accuracyPercent: Int) {
        if (_loadingConceptTopic.value == topic) return
        _loadingConceptTopic.value = topic
        viewModelScope.launch {
            val explanation = GeminiAiService.generateAiConceptBooster(
                subject = subject,
                topic = topic,
                userAccuracyPercent = accuracyPercent
            )
            _aiConceptBoosterMap.update { it + (topic to explanation) }
            _loadingConceptTopic.value = null
        }
    }

    // --- Personalized Study Plan Actions ---

    fun toggleStudyTask(task: StudyPlanTask) {
        val newCompleted = !task.completed
        viewModelScope.launch {
            repository.toggleStudyTaskCompleted(task.id, newCompleted)
            if (newCompleted) {
                val currentLeaderboard = (leaderboardState.value as? UiState.Success)?.data ?: emptyList()
                val myProfile = currentLeaderboard.firstOrNull { it.userId == currentUserId }
                if (myProfile != null) {
                    val allTasks = (studyTasksState.value as? UiState.Success)?.data ?: emptyList()
                    val completedCount = allTasks.count { it.completed || it.id == task.id }
                    val badgesSet = myProfile.badgesUnlocked.toMutableSet()
                    var bonusXp = 15
                    if (completedCount >= 3 && badgesSet.add("planner_pro")) {
                        bonusXp += 110
                        _statusBannerMessage.value = "Unlocked 'Disciplined Strategist' Badge (+110 XP)!"
                    } else {
                        _statusBannerMessage.value = "+15 XP awarded for completing '${task.topic}'!"
                    }
                    repository.upsertLeaderboardProfile(
                        myProfile.copy(
                            xpPoints = myProfile.xpPoints + bonusXp,
                            badgesUnlocked = badgesSet.toList()
                        )
                    )
                }
            }
        }
    }

    fun addCustomStudyTask(
        dayLabel: String,
        subject: String,
        topic: String,
        activityType: String,
        targetPyqCount: Int,
        durationMinutes: Int,
        priorityLevel: String
    ) {
        viewModelScope.launch {
            repository.createStudyTask(
                StudyPlanTask(
                    dayLabel = dayLabel,
                    subject = subject,
                    topic = topic,
                    activityType = activityType,
                    targetPyqCount = targetPyqCount,
                    durationMinutes = durationMinutes,
                    priorityLevel = priorityLevel,
                    completed = false
                )
            )
            _statusBannerMessage.value = "Added '$topic' to your Study Plan!"
        }
    }

    fun regeneratePersonalizedStudyPlan(
        attempts: List<TestAttempt>,
        dailyHours: Int,
        targetExam: String
    ) {
        if (_isGeneratingStudyPlan.value) return
        _isGeneratingStudyPlan.value = true
        viewModelScope.launch {
            val insights = computeTopicMasteryInsights(attempts)
            val weakPairs = insights.filter { it.isWeakArea }.map { it.subject to it.topic }
            val generatedTasks = GeminiAiService.generateAiStudySchedule(
                weakTopics = weakPairs,
                dailyHours = dailyHours,
                targetExam = targetExam
            )

            // Replace uncompleted tasks with newly prioritized schedule
            val existingTasks = (studyTasksState.value as? UiState.Success)?.data ?: emptyList()
            existingTasks.filter { !it.completed }.forEach {
                repository.deleteStudyTask(it.id)
            }
            generatedTasks.forEach { task ->
                repository.createStudyTask(task)
            }
            _isGeneratingStudyPlan.value = false
            _statusBannerMessage.value = "Personalized Study Plan updated for $targetExam ($dailyHours hrs/day)!"
        }
    }

    // --- Bookmarks ---

    fun toggleBookmarkQuestion(question: PyqQuestion, personalNote: String = "High-yield JEE formula & concept") {
        val currentBookmarks = (bookmarksState.value as? UiState.Success)?.data ?: emptyList()
        val existing = currentBookmarks.firstOrNull { it.questionId == question.id }
        viewModelScope.launch {
            if (existing != null) {
                repository.removeBookmark(existing.id)
                _statusBannerMessage.value = "Removed from Bookmarked PYQs"
            } else {
                repository.saveBookmark(
                    questionId = question.id,
                    subject = question.subject,
                    topic = question.topic,
                    personalNote = personalNote
                )
                _statusBannerMessage.value = "Bookmarked '${question.topic}' PYQ for revision!"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val TAG = "JeePulseVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
