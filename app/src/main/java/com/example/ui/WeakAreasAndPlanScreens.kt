package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeakAreasInsightsScreen(
    attempts: List<TestAttempt>,
    insights: List<TopicMasteryInsight>,
    aiConceptBoosterMap: Map<String, String>,
    loadingConceptTopic: String?,
    onRequestAiBooster: (String, String, Int) -> Unit,
    onStartTopicDrill: (String, String) -> Unit,
    onGenerateWeakAreaMock: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var showOnlyWeakAreas by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf("All") }
    var expandedTopics by remember {
        mutableStateOf(insights.filter { it.isWeakArea }.take(2).map { it.topic }.toSet())
    }

    val weakTopicsList = insights.filter { it.isWeakArea }
    val filteredInsights = insights.filter { item ->
        val passWeak = !showOnlyWeakAreas || item.isWeakArea
        val passSubj = selectedSubject == "All" || item.subject.equals(selectedSubject, ignoreCase = true)
        passWeak && passSubj
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weak_areas_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Summary Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        CrimsonAlert.copy(alpha = 0.4f),
                        RoundedCornerShape(22.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weak-Area Diagnostic & Concept Studio",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${weakTopicsList.size} Priority Weak Areas detected (< 68% accuracy). Review their concept summaries, key formulas, and video guides below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Subject Mastery Bars
                    val phyAcc = insights.filter { it.subject == "Physics" }.map { it.accuracyPercent }.average().toInt()
                    val chemAcc = insights.filter { it.subject == "Chemistry" }.map { it.accuracyPercent }.average().toInt()
                    val mathAcc = insights.filter { it.subject == "Mathematics" }.map { it.accuracyPercent }.average().toInt()

                    SubjectAccuracyBar("Physics Mastery", phyAcc, PhysicsBlue)
                    SubjectAccuracyBar("Chemistry Mastery", chemAcc, ChemistryAmber)
                    SubjectAccuracyBar("Mathematics Mastery", mathAcc, MathsViolet)

                    Button(
                        onClick = {
                            onGenerateWeakAreaMock(weakTopicsList.map { it.topic }.take(4))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_weak_area_ai_mock_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletAi,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Weak Area Mock",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate AI Mock on My Weak Areas",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Filter Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = showOnlyWeakAreas,
                    onClick = { showOnlyWeakAreas = !showOnlyWeakAreas },
                    label = { Text("Weak Areas Only (${weakTopicsList.size})") },
                    modifier = Modifier.testTag("filter_weak_only_chip")
                )
                listOf("All", "Physics", "Chemistry", "Mathematics").forEach { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj) }
                    )
                }
            }
        }

        // Topic Concept Explainer Cards
        items(filteredInsights, key = { it.topic }) { item ->
            val isExpanded = item.topic in expandedTopics
            val subjectColor = when (item.subject) {
                "Physics" -> PhysicsBlue
                "Chemistry" -> ChemistryAmber
                else -> MathsViolet
            }
            val statusColor = if (item.isWeakArea) CrimsonAlert else EmeraldSuccess
            val aiNote = aiConceptBoosterMap[item.topic]
            val isLoadingAi = loadingConceptTopic == item.topic

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (item.isWeakArea) CrimsonAlert.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedTopics = if (isExpanded) expandedTopics - item.topic
                                else expandedTopics + item.topic
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetadataPill(text = item.subject, color = subjectColor)
                                MetadataPill(
                                    text = if (item.isWeakArea) "WEAK AREA • ${item.accuracyPercent}%"
                                    else "STRONG • ${item.accuracyPercent}%",
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.topic,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.conceptGuide.highYieldWeightage} • Avg Speed: ${item.avgTimeSeconds}s/Q",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse concept" else "Expand concept",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            // 1. Short, Clear Concept Summary
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Concept Explanation",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Underlying Concept Summary",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ElectricCyan
                                )
                            }
                            Text(
                                text = item.conceptGuide.conceptSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // 2. Set of Key Formulas
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Functions,
                                            contentDescription = "Key Formulas",
                                            tint = AmberGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "MUST-KNOW FORMULA SHEET",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AmberGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    item.conceptGuide.keyFormulas.forEach { formula ->
                                        Text(
                                            text = "• $formula",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // 3. Exam Trap Alert
                            Surface(
                                color = CrimsonAlert.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "Exam Trap",
                                        tint = CrimsonAlert,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "JEE Trap Tip: ${item.conceptGuide.examTrapTip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // 4. AI Personalized Concept Booster Output (if generated)
                            if (aiNote != null) {
                                Surface(
                                    color = VioletAi.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, VioletAi.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "AI PERSONALIZED CONCEPT TUTOR",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = VioletAi
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = aiNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // 5. Action Buttons: Video Link, AI Deep Dive, Practice PYQs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(item.conceptGuide.videoUrl)
                                        )
                                        try {
                                            context.startActivity(intent)
                                        } catch (ignored: Exception) {
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = "Video Lecture",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Video Guide", style = MaterialTheme.typography.labelMedium)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        onRequestAiBooster(item.subject, item.topic, item.accuracyPercent)
                                    },
                                    enabled = !isLoadingAi,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoadingAi) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Tutor",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Explain", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            Button(
                                onClick = { onStartTopicDrill(item.subject, item.topic) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("practice_topic_${item.topic}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Solve PYQs",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Practice ${item.topic} PYQs Now")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectAccuracyBar(label: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalizedStudyPlanScreen(
    tasks: List<StudyPlanTask>,
    weakInsights: List<TopicMasteryInsight>,
    isGeneratingPlan: Boolean,
    onToggleTask: (StudyPlanTask) -> Unit,
    onStartTaskPyqDrill: (StudyPlanTask) -> Unit,
    onRegenerateAiPlan: (Int, String) -> Unit,
    onAddCustomTask: (String, String, String, String, Int, Int, String) -> Unit
) {
    var selectedHours by remember { mutableIntStateOf(6) }
    var selectedExam by remember { mutableStateOf("JEE Main & Adv 2026") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.completed }
    val totalCount = tasks.size.coerceAtLeast(1)
    val completionRatio = completedCount.toFloat() / totalCount.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_plan_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Study Plan Overview & AI Generator Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        ElectricCyan.copy(alpha = 0.4f),
                        RoundedCornerShape(22.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Personalized JEE Study & PYQ Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prioritizes your detected weak areas (${weakInsights.take(3).joinToString { it.topic }}) with daily PYQ drills and formula revisions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Weekly Schedule Progress ($completedCount / ${tasks.size} completed)",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "+15 XP / task",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { completionRatio.coerceIn(0f, 1f) },
                        color = EmeraldSuccess,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Target Exam & Daily Hours Selector
                    Text(
                        text = "Customize Daily Study Capacity & Target Exam:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(4, 6, 8, 10).forEach { hrs ->
                            FilterChip(
                                selected = selectedHours == hrs,
                                onClick = { selectedHours = hrs },
                                label = { Text("${hrs}h / day") }
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("JEE Main 2026", "JEE Advanced 2026", "JEE Main & Adv 2026").forEach { exam ->
                            FilterChip(
                                selected = selectedExam == exam,
                                onClick = { selectedExam = exam },
                                label = { Text(exam) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onRegenerateAiPlan(selectedHours, selectedExam) },
                            enabled = !isGeneratingPlan,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("regenerate_study_plan_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (isGeneratingPlan) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Plan",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Rebuild Plan", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_study_task_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Block")
                        }
                    }
                }
            }
        }

        // Scheduled Task Items
        items(tasks, key = { it.id.ifBlank { it.topic + it.dayLabel } }) { task ->
            val prioColor = when (task.priorityLevel) {
                "HIGH_WEAK_AREA" -> CrimsonAlert
                "MEDIUM_PRACTICE" -> AmberGold
                else -> ElectricCyan
            }
            val prioLabel = when (task.priorityLevel) {
                "HIGH_WEAK_AREA" -> "HIGH PRIORITY • WEAK AREA"
                "MEDIUM_PRACTICE" -> "CORE PRACTICE"
                else -> "REVISION CYCLE"
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.completed)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (task.completed) EmeraldSuccess.copy(alpha = 0.4f)
                        else prioColor.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggleTask(task) }
                        ) {
                            Icon(
                                imageVector = if (task.completed) Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Complete Task",
                                tint = if (task.completed) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = task.dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = prioColor
                                )
                                Text(
                                    text = "${task.subject}: ${task.topic}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        MetadataPill(text = prioLabel, color = prioColor)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${task.durationMinutes} mins • Solve ${task.targetPyqCount} PYQs (${task.activityType.replace("_", " ")})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { onStartTaskPyqDrill(task) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start PYQ Drill",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start PYQs", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var dayInput by remember { mutableStateOf("Day 6 • Custom Drill") }
        var selectedSubj by remember { mutableStateOf("Physics") }
        val availableTopics = PyqQuestionBank.getTopicsForSubject(selectedSubj)
        var selectedTopic by remember(selectedSubj) {
            mutableStateOf(availableTopics.firstOrNull() ?: "Rotational Motion")
        }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Scheduled PYQ Block") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dayInput,
                        onValueChange = { dayInput = it },
                        label = { Text("Day / Slot Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Subject:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Physics", "Chemistry", "Mathematics").forEach { s ->
                            FilterChip(
                                selected = selectedSubj == s,
                                onClick = { selectedSubj = s },
                                label = { Text(s) }
                            )
                        }
                    }
                    Text("Topic:", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        availableTopics.forEach { t ->
                            FilterChip(
                                selected = selectedTopic == t,
                                onClick = { selectedTopic = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCustomTask(
                            dayInput.ifBlank { "Custom Slot" },
                            selectedSubj,
                            selectedTopic,
                            "PYQ_DRILL",
                            15,
                            45,
                            "HIGH_WEAK_AREA"
                        )
                        showAddTaskDialog = false
                    }
                ) {
                    Text("Save Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
