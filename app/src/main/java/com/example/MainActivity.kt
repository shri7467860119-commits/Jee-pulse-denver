package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.JeePulseRepository
import com.example.data.LeaderboardProfile
import com.example.data.PyqQuestionBank
import com.example.ui.AuthScreen
import com.example.ui.DashboardHomeScreen
import com.example.ui.ExamSessionScreen
import com.example.ui.GamificationLeaderboardScreen
import com.example.ui.JeePulseViewModel
import com.example.ui.PersonalizedStudyPlanScreen
import com.example.ui.PyqArenaAndAiMockScreen
import com.example.ui.UiState
import com.example.ui.WeakAreasInsightsScreen
import com.example.ui.signOutUser
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                JeePulseAppRoot()
            }
        }
    }
}

@Composable
fun JeePulseAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        AuthenticatedJeePulseShell(
            currentUserId = user.uid,
            userDisplayName = user.displayName ?: "JEE Aspirant"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedJeePulseShell(
    currentUserId: String,
    userDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // ViewModel instantiated via viewModelFactory keyed by currentUserId
    val viewModel: JeePulseViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                JeePulseViewModel(
                    repository = JeePulseRepository(db),
                    currentUserId = currentUserId,
                    userDisplayName = userDisplayName
                )
            }
        }
    )

    val attemptsUiState by viewModel.attemptsState.collectAsStateWithLifecycle()
    val leaderboardUiState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val studyTasksUiState by viewModel.studyTasksState.collectAsStateWithLifecycle()
    val bookmarksUiState by viewModel.bookmarksState.collectAsStateWithLifecycle()

    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val selectedTopic by viewModel.selectedTopicFilter.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficultyFilter.collectAsStateWithLifecycle()
    val isGeneratingAiMock by viewModel.isGeneratingAiMock.collectAsStateWithLifecycle()
    val isGeneratingStudyPlan by viewModel.isGeneratingStudyPlan.collectAsStateWithLifecycle()
    val aiConceptBoosterMap by viewModel.aiConceptBoosterMap.collectAsStateWithLifecycle()
    val loadingConceptTopic by viewModel.loadingConceptTopic.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val attempts = (attemptsUiState as? UiState.Success)?.data ?: emptyList()
    val leaderboard = (leaderboardUiState as? UiState.Success)?.data ?: emptyList()
    val studyTasks = (studyTasksUiState as? UiState.Success)?.data ?: emptyList()
    val bookmarks = (bookmarksUiState as? UiState.Success)?.data ?: emptyList()

    val myLeaderboardProfile = leaderboard.firstOrNull { it.userId == currentUserId }
        ?: LeaderboardProfile(
            userId = currentUserId,
            displayName = userDisplayName,
            xpPoints = 120,
            currentStreak = 1,
            bestStreak = 1
        )

    val topicInsights = remember(attempts) {
        viewModel.computeTopicMasteryInsights(attempts)
    }

    val filteredQuestions = remember(selectedSubject, selectedTopic, selectedDifficulty) {
        viewModel.getFilteredQuestions()
    }

    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = activeSession == null && currentTab != 0) {
        currentTab = 0
    }

    // If a live timed PYQ or Mock session is active, render the full-screen Exam Arena
    val session = activeSession
    if (session != null) {
        ExamSessionScreen(
            session = session,
            bookmarkedQuestionIds = bookmarks.map { it.questionId }.toSet(),
            onSelectOption = viewModel::selectMcqOption,
            onUpdateNumerical = viewModel::updateNumericalInput,
            onToggleReview = viewModel::toggleMarkForReview,
            onToggleRevealSolution = viewModel::toggleRevealSolution,
            onTogglePause = viewModel::toggleSessionPause,
            onNavigateIndex = viewModel::navigateToQuestionIndex,
            onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) },
            onSubmitSession = viewModel::submitActiveSession,
            onCloseSession = viewModel::closeActiveSession
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${myLeaderboardProfile.displayName} • ${myLeaderboardProfile.xpPoints} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            signOutUser(
                                credentialManager = credentialManager,
                                onSignOutComplete = {},
                                scope = scope
                            )
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(stringResource(id = R.string.nav_home)) },
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "PYQ Arena & AI Mock") },
                    label = { Text(stringResource(id = R.string.nav_pyq_Arena)) },
                    modifier = Modifier.testTag("nav_tab_pyq")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Weak Areas") },
                    label = { Text(stringResource(id = R.string.nav_insights)) },
                    modifier = Modifier.testTag("nav_tab_insights")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Study Plan") },
                    label = { Text(stringResource(id = R.string.nav_study_plan)) },
                    modifier = Modifier.testTag("nav_tab_plan")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Rank & XP") },
                    label = { Text(stringResource(id = R.string.nav_leaderboard)) },
                    modifier = Modifier.testTag("nav_tab_leaderboard")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = statusBannerMessage != null) {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusBannerMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::dismissStatusBanner) {
                            Text("OK")
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    0 -> DashboardHomeScreen(
                        userProfile = myLeaderboardProfile,
                        attempts = attempts,
                        studyTasks = studyTasks,
                        weakInsights = topicInsights,
                        onStartFullMock = viewModel::startFullMockExam,
                        onNavigateToTab = { currentTab = it },
                        onStartSubjectDrill = { subject ->
                            val qList = PyqQuestionBank.allQuestions.filter {
                                it.subject.equals(subject, ignoreCase = true)
                            }
                            viewModel.startPyqSession(
                                title = "$subject • High-Yield PYQ Drill",
                                testType = "PYQ_PRACTICE",
                                subjectFilter = subject,
                                questions = qList,
                                isPracticeMode = true
                            )
                        },
                        onStartTopicDrill = { subj, topic ->
                            viewModel.startTopicDrill(subj, topic, isPracticeMode = true)
                        },
                        onToggleStudyTask = viewModel::toggleStudyTask
                    )

                    1 -> PyqArenaAndAiMockScreen(
                        selectedSubject = selectedSubject,
                        selectedTopic = selectedTopic,
                        selectedDifficulty = selectedDifficulty,
                        filteredQuestions = filteredQuestions,
                        bookmarks = bookmarks,
                        weakInsights = topicInsights,
                        isGeneratingAiMock = isGeneratingAiMock,
                        onSelectSubject = viewModel::setSubjectFilter,
                        onSelectTopic = viewModel::setTopicFilter,
                        onSelectDifficulty = viewModel::setDifficultyFilter,
                        onStartPracticeSession = { title, qList, isPractice ->
                            viewModel.startPyqSession(
                                title = title,
                                testType = if (isPractice) "PYQ_PRACTICE" else "FULL_MOCK",
                                subjectFilter = selectedSubject,
                                questions = qList,
                                isPracticeMode = isPractice
                            )
                        },
                        onGenerateAiMock = viewModel::generateAndLaunchAiMockTest,
                        onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) }
                    )

                    2 -> WeakAreasInsightsScreen(
                        attempts = attempts,
                        insights = topicInsights,
                        aiConceptBoosterMap = aiConceptBoosterMap,
                        loadingConceptTopic = loadingConceptTopic,
                        onRequestAiBooster = viewModel::requestAiConceptBooster,
                        onStartTopicDrill = { subj, topic ->
                            viewModel.startTopicDrill(subj, topic, isPracticeMode = true)
                        },
                        onGenerateWeakAreaMock = { weakTopicNames ->
                            viewModel.generateAndLaunchAiMockTest(
                                subjectFilter = "All Subjects",
                                focusTopics = weakTopicNames,
                                difficulty = "Moderate",
                                questionCount = 6
                            )
                        }
                    )

                    3 -> PersonalizedStudyPlanScreen(
                        tasks = studyTasks,
                        weakInsights = topicInsights.filter { it.isWeakArea },
                        isGeneratingPlan = isGeneratingStudyPlan,
                        onToggleTask = viewModel::toggleStudyTask,
                        onStartTaskPyqDrill = { task ->
                            viewModel.startTopicDrill(
                                subject = task.subject,
                                topic = task.topic,
                                isPracticeMode = false
                            )
                        },
                        onRegenerateAiPlan = { dailyHours, targetExam ->
                            viewModel.regeneratePersonalizedStudyPlan(
                                attempts = attempts,
                                dailyHours = dailyHours,
                                targetExam = targetExam
                            )
                        },
                        onAddCustomTask = viewModel::addCustomStudyTask
                    )

                    4 -> GamificationLeaderboardScreen(
                        currentUserId = currentUserId,
                        userDisplayName = userDisplayName,
                        liveLeaderboard = leaderboard
                    )
                }
            }
        }
    }
}
