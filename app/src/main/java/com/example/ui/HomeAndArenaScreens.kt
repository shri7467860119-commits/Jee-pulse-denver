package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BookmarkedQuestion
import com.example.data.LeaderboardProfile
import com.example.data.PyqQuestion
import com.example.data.PyqQuestionBank
import com.example.data.StudyPlanTask
import com.example.data.TestAttempt
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ChemistryAmber
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MathsViolet
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.VioletAi

@Composable
fun DashboardHomeScreen(
    userProfile: LeaderboardProfile,
    attempts: List<TestAttempt>,
    studyTasks: List<StudyPlanTask>,
    weakInsights: List<TopicMasteryInsight>,
    onStartFullMock: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onStartSubjectDrill: (String) -> Unit,
    onStartTopicDrill: (String, String) -> Unit,
    onToggleStudyTask: (StudyPlanTask) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner with Live Gamification Bar & Primary Actions
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        ElectricCyan.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_jee_hero),
                        contentDescription = "JEE Pulse Hero Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Gamification Pill Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .border(1.dp, AmberGold.copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .clickable { onNavigateToTab(4) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "XP Points",
                                        tint = AmberGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${userProfile.xpPoints} XP • ${userProfile.badgesUnlocked.size} Badges",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberGold
                                    )
                                }
                            }

                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(
                                    1.dp,
                                    EmeraldSuccess.copy(alpha = 0.6f),
                                    RoundedCornerShape(50)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = AmberGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${userProfile.currentStreak}d Streak",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Crack JEE 2026 with Precision PYQs",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Timed exam simulation (+4/-1), step-by-step derivations, AI weak-area mocks & daily study schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onStartFullMock,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_full_mock_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Full Mock",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full PYQ Mock", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigateToTab(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_ai_mock_builder_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VioletAi,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Mock",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Custom Mock", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Subject-Wise Quick Practice Arena Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Subject-Wise PYQ Quick Drills",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubjectQuickCard(
                        subject = "Physics",
                        subtitle = "Mechanics, Electrodynamics & Modern",
                        color = PhysicsBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartSubjectDrill("Physics") }
                    )
                    SubjectQuickCard(
                        subject = "Chemistry",
                        subtitle = "Bonding, Kinetics, GOC & Coordination",
                        color = ChemistryAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartSubjectDrill("Chemistry") }
                    )
                    SubjectQuickCard(
                        subject = "Mathematics",
                        subtitle = "Calculus, Matrices, Vectors & Conics",
                        color = MathsViolet,
                        modifier = Modifier.weight(1f),
                        onClick = { onStartSubjectDrill("Mathematics") }
                    )
                }
            }
        }

        // 3. Priority Weak-Area Spotlight (with Concept Explainer + Practice CTA)
        item {
            val topWeak = weakInsights.filter { it.isWeakArea }.take(2)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        CrimsonAlert.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "Weak Areas",
                                tint = CrimsonAlert
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weak-Area Concept Spotlight",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "All Explainers →",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan,
                            modifier = Modifier.clickable { onNavigateToTab(2) }
                        )
                    }

                    topWeak.forEach { weak ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${weak.subject}: ${weak.topic}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    MetadataPill(
                                        text = "${weak.accuracyPercent}% Accuracy",
                                        color = CrimsonAlert
                                    )
                                }
                                Text(
                                    text = weak.conceptGuide.conceptSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onNavigateToTab(2) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Formulas & Video", style = MaterialTheme.typography.labelSmall)
                                    }
                                    FilledTonalButton(
                                        onClick = { onStartTopicDrill(weak.subject, weak.topic) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Fix with PYQs", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Today's Personalized Study Plan Preview
        item {
            val nextTasks = studyTasks.take(2)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Study Plan Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Full Schedule →",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan,
                            modifier = Modifier.clickable { onNavigateToTab(3) }
                        )
                    }

                    nextTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleStudyTask(task) }
                            ) {
                                Icon(
                                    imageVector = if (task.completed) Icons.Default.CheckCircle
                                    else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete Task",
                                    tint = if (task.completed) EmeraldSuccess else AmberGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${task.subject} • ${task.topic}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${task.dayLabel} • ${task.targetPyqCount} PYQs (${task.durationMinutes}m)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { onStartTopicDrill(task.subject, task.topic) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Solve", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Test Attempts History
        if (attempts.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recent Tests",
                        tint = ElectricCyan
                    )
                    Text(
                        text = "Recent PYQ & Mock Sessions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(attempts.take(5), key = { it.id }) { attempt ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attempt.testTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Score: ${attempt.scoreObtained}/${attempt.maxScore} • Accuracy: ${attempt.accuracyPercent}% • Time: ${formatSecondsMmSs(attempt.timeSpentSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = AmberGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "+${attempt.xpEarned} XP",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectQuickCard(
    subject: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("subject_card_${subject.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = subject,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = subject,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PyqArenaAndAiMockScreen(
    selectedSubject: String,
    selectedTopic: String,
    selectedDifficulty: String,
    filteredQuestions: List<PyqQuestion>,
    bookmarks: List<BookmarkedQuestion>,
    weakInsights: List<TopicMasteryInsight>,
    isGeneratingAiMock: Boolean,
    onSelectSubject: (String) -> Unit,
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (String) -> Unit,
    onStartPracticeSession: (String, List<PyqQuestion>, Boolean) -> Unit,
    onGenerateAiMock: (String, List<String>, String, Int) -> Unit,
    onToggleBookmark: (PyqQuestion) -> Unit
) {
    val bookmarkedIds = remember(bookmarks) { bookmarks.map { it.questionId }.toSet() }
    var showBookmarkedOnly by remember { mutableStateOf(false) }

    // AI Custom Mock Builder State
    var aiSubject by remember { mutableStateOf("All Subjects") }
    var aiDifficulty by remember { mutableStateOf("Moderate") }
    var aiQuestionCount by remember { mutableIntStateOf(5) }
    val defaultWeakTopics = remember(weakInsights) {
        weakInsights.filter { it.isWeakArea }.map { it.topic }.take(3).toSet()
    }
    var selectedAiTopics by remember(defaultWeakTopics) { mutableStateOf(defaultWeakTopics) }

    val displayedQuestions = remember(filteredQuestions, showBookmarkedOnly, bookmarkedIds) {
        if (showBookmarkedOnly) {
            PyqQuestionBank.allQuestions.filter { it.id in bookmarkedIds }
        } else {
            filteredQuestions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pyq_arena_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. AI Custom Mock Test Generator Studio Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = VioletAi.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VioletAi.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Custom Mock",
                                tint = VioletAi
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Custom Mock Test Generator",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Synthesizes fresh JEE-level problems targeting your selected subjects & weak areas (+150 XP Badge).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Subject Selection
                    Text(
                        text = "1. Target Subject:",
                        style = MaterialTheme.typography.labelLarge,
                        color = VioletAi
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All Subjects", "Physics", "Chemistry", "Mathematics").forEach { subj ->
                            FilterChip(
                                selected = aiSubject == subj,
                                onClick = { aiSubject = subj },
                                label = { Text(subj) }
                            )
                        }
                    }

                    // Focus Weak Topics Selection
                    Text(
                        text = "2. Focus Chapters (Weak Areas Pre-Selected):",
                        style = MaterialTheme.typography.labelLarge,
                        color = VioletAi
                    )
                    val availableAiTopics = PyqQuestionBank.getTopicsForSubject(aiSubject)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableAiTopics.forEach { topic ->
                            val isSelected = topic in selectedAiTopics
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedAiTopics = if (isSelected) selectedAiTopics - topic
                                    else selectedAiTopics + topic
                                },
                                label = { Text(topic) }
                            )
                        }
                    }

                    // Difficulty & Question Count Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Easy", "Moderate", "Hard").forEach { diff ->
                                FilterChip(
                                    selected = aiDifficulty == diff,
                                    onClick = { aiDifficulty = diff },
                                    label = { Text(diff) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(4, 6, 8).forEach { count ->
                                FilterChip(
                                    selected = aiQuestionCount == count,
                                    onClick = { aiQuestionCount = count },
                                    label = { Text("$count Qs") }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onGenerateAiMock(
                                aiSubject,
                                selectedAiTopics.toList(),
                                aiDifficulty,
                                aiQuestionCount
                            )
                        },
                        enabled = !isGeneratingAiMock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_ai_mock_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletAi,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isGeneratingAiMock) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synthesizing Custom JEE Mock...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Launch AI Mock",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate & Start Timed AI Mock ($aiQuestionCount Qs)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Chapter-Wise PYQ Bank Filter & Timed Session Launcher
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official JEE Main & Advanced PYQ Bank",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilterChip(
                            selected = showBookmarkedOnly,
                            onClick = { showBookmarkedOnly = !showBookmarkedOnly },
                            label = { Text("Saved (${bookmarkedIds.size})") }
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All Subjects", "Physics", "Chemistry", "Mathematics").forEach { subj ->
                            FilterChip(
                                selected = !showBookmarkedOnly && selectedSubject == subj,
                                onClick = {
                                    showBookmarkedOnly = false
                                    onSelectSubject(subj)
                                },
                                label = { Text(subj) }
                            )
                        }
                    }

                    val topicsForFilter = listOf("All Topics") + PyqQuestionBank.getTopicsForSubject(selectedSubject)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topicsForFilter.forEach { top ->
                            FilterChip(
                                selected = !showBookmarkedOnly && selectedTopic == top,
                                onClick = {
                                    showBookmarkedOnly = false
                                    onSelectTopic(top)
                                },
                                label = { Text(top) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onStartPracticeSession(
                                    "$selectedSubject • ${if (selectedTopic == "All Topics") "PYQ Practice" else selectedTopic}",
                                    displayedQuestions,
                                    true
                                )
                            },
                            enabled = displayedQuestions.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_filtered_pyq_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Practice",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Practice Mode (${displayedQuestions.size} Qs)")
                        }

                        OutlinedButton(
                            onClick = {
                                onStartPracticeSession(
                                    "$selectedSubject • Timed Exam",
                                    displayedQuestions,
                                    false
                                )
                            },
                            enabled = displayedQuestions.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Exam Mode",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strict Mock Mode")
                        }
                    }
                }
            }
        }

        // 3. Individual PYQ Question Cards
        items(displayedQuestions, key = { it.id }) { q ->
            val isBookmarked = q.id in bookmarkedIds
            val subjColor = when (q.subject) {
                "Physics" -> PhysicsBlue
                "Chemistry" -> ChemistryAmber
                else -> MathsViolet
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            MetadataPill(text = q.subject, color = subjColor)
                            MetadataPill(text = q.topic, color = ElectricCyan)
                            MetadataPill(text = q.difficulty, color = AmberGold)
                        }
                        IconButton(onClick = { onToggleBookmark(q) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark PYQ",
                                tint = if (isBookmarked) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = q.examTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )

                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${q.questionType.label} • Target ${q.targetTimeSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(
                            onClick = {
                                onStartPracticeSession(
                                    "${q.topic} • PYQ Solve",
                                    listOf(q) + displayedQuestions.filter { it.id != q.id },
                                    true
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Solve & View Solution", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
