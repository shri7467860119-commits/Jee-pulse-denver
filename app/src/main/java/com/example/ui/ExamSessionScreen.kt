package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PyqQuestion
import com.example.data.PyqQuestionBank
import com.example.data.QuestionType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess

fun formatSecondsMmSs(totalSeconds: Int): String {
    val mins = (totalSeconds / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(mins, secs)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamSessionScreen(
    session: ActiveTestSession,
    bookmarkedQuestionIds: Set<String>,
    onSelectOption: (String, Int) -> Unit,
    onUpdateNumerical: (String, String) -> Unit,
    onToggleReview: (String) -> Unit,
    onToggleRevealSolution: (String) -> Unit,
    onTogglePause: () -> Unit,
    onNavigateIndex: (Int) -> Unit,
    onToggleBookmark: (PyqQuestion) -> Unit,
    onSubmitSession: () -> Unit,
    onCloseSession: () -> Unit
) {
    BackHandler {
        onCloseSession()
    }

    val context = LocalContext.current
    var showPalette by remember { mutableStateOf(false) }
    val currentQuestion = session.questions.getOrElse(session.currentIndex) { session.questions.first() }
    val questionSpentSec = session.perQuestionSeconds[currentQuestion.id] ?: 0
    val isBookmarked = currentQuestion.id in bookmarkedQuestionIds
    val isMarkedReview = currentQuestion.id in session.markedForReview
    val isSolutionVisible = session.isSubmitted || (currentQuestion.id in session.revealedSolutions)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top Live Timer & Control Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onCloseSession,
                                modifier = Modifier.testTag("close_session_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Session"
                                )
                            }
                            Column {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Q ${session.currentIndex + 1} of ${session.questions.size} • ${currentQuestion.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Countdown Timer Pill
                        val timerColor = when {
                            session.isSubmitted -> EmeraldSuccess
                            session.remainingSeconds < 120 -> CrimsonAlert
                            session.remainingSeconds < 300 -> AmberGold
                            else -> ElectricCyan
                        }
                        Surface(
                            color = timerColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = timerColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Countdown timer",
                                    tint = timerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (session.isSubmitted) "COMPLETED" else formatSecondsMmSs(session.remainingSeconds),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = timerColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!session.isSubmitted) {
                            IconButton(
                                onClick = onTogglePause,
                                modifier = Modifier.testTag("pause_timer_button")
                            ) {
                                Icon(
                                    imageVector = if (session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (session.isPaused) "Resume Timer" else "Pause Timer",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { showPalette = !showPalette },
                            modifier = Modifier.testTag("toggle_palette_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Question Palette",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Collapsible Question Palette Grid
                    AnimatedVisibility(visible = showPalette) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Question Palette (Tap to Jump)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                session.questions.forEachIndexed { idx, q ->
                                    val isAnswered = session.selectedOptions.containsKey(q.id) ||
                                        !session.numericalInputs[q.id].isNullOrBlank()
                                    val isReview = q.id in session.markedForReview
                                    val isCurrent = idx == session.currentIndex
                                    val badgeBg = when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isReview -> AmberGold
                                        isAnswered -> EmeraldSuccess
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val badgeFg = when {
                                        isCurrent -> MaterialTheme.colorScheme.onPrimary
                                        isReview || isAnswered -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(badgeBg)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onNavigateIndex(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = badgeFg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scrollable Question & Solution Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Post-Submission Result Summary Card
                session.lastSubmissionSummary?.let { summary ->
                    SessionScorecardBanner(summary = summary)
                }

                // Pause Overlay Banner
                if (session.isPaused && !session.isSubmitted) {
                    Surface(
                        color = AmberGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Timer is paused. Tap Resume to continue solving.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmberGold
                            )
                            FilledTonalButton(onClick = onTogglePause) {
                                Text("Resume")
                            }
                        }
                    }
                }

                // Question Metadata Chips & Per-Question Stopwatch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        MetadataPill(text = currentQuestion.examTag, color = MaterialTheme.colorScheme.primary)
                        MetadataPill(text = currentQuestion.topic, color = MaterialTheme.colorScheme.tertiary)
                        MetadataPill(text = currentQuestion.difficulty, color = AmberGold)
                        MetadataPill(text = currentQuestion.questionType.label, color = EmeraldSuccess)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleBookmark(currentQuestion) },
                            modifier = Modifier.testTag("bookmark_question_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Question",
                                tint = if (isBookmarked) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onToggleReview(currentQuestion.id) },
                            modifier = Modifier.testTag("mark_review_button")
                        ) {
                            Icon(
                                imageVector = if (isMarkedReview) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                contentDescription = "Mark for Review",
                                tint = if (isMarkedReview) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Per-Question Pace Indicator
                val paceOverTarget = questionSpentSec > currentQuestion.targetTimeSeconds
                Text(
                    text = "Time on this question: ${formatSecondsMmSs(questionSpentSec)}  •  Target JEE Pace: ${formatSecondsMmSs(currentQuestion.targetTimeSeconds)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (paceOverTarget) CrimsonAlert else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Question Stem Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Answer Input Area: MCQ vs NUMERICAL
                if (currentQuestion.questionType == QuestionType.MCQ) {
                    val selectedIndex = session.selectedOptions[currentQuestion.id]
                    val optionLabels = listOf("A", "B", "C", "D")

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.options.forEachIndexed { idx, optionText ->
                            val isSelected = selectedIndex == idx
                            val isCorrectOption = idx == currentQuestion.correctOptionIndex

                            val borderColor = when {
                                isSolutionVisible && isCorrectOption -> EmeraldSuccess
                                isSolutionVisible && isSelected && !isCorrectOption -> CrimsonAlert
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            }
                            val containerBg = when {
                                isSolutionVisible && isCorrectOption -> EmeraldSuccess.copy(alpha = 0.14f)
                                isSolutionVisible && isSelected && !isCorrectOption -> CrimsonAlert.copy(alpha = 0.14f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = containerBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                                    .clickable(enabled = !session.isSubmitted) {
                                        onSelectOption(currentQuestion.id, idx)
                                    }
                                    .testTag("mcq_option_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = optionLabels.getOrElse(idx) { "${idx + 1}" },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Numerical Value Answer Input
                    val currentVal = session.numericalInputs[currentQuestion.id].orEmpty()
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Enter Numerical Answer (No Negative Marking)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { onUpdateNumerical(currentQuestion.id, it) },
                                enabled = !session.isSubmitted,
                                label = { Text("Integer / Decimal Value") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("numerical_answer_input")
                            )
                            if (isSolutionVisible) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Official Correct Value: ${currentQuestion.numericalAnswer}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                    }
                }

                // Practice Mode Instant Solution Toggle
                if (!session.isSubmitted) {
                    OutlinedButton(
                        onClick = { onToggleRevealSolution(currentQuestion.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reveal_solution_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Solution",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSolutionVisible) "Hide Detailed Solution & Formulas"
                            else "Show Step-by-Step Solution & Key Formulas"
                        )
                    }
                }

                // Detailed Solution, Key Formulas, Concept Breakdown & Video Link
                AnimatedVisibility(visible = isSolutionVisible) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                EmeraldSuccess.copy(alpha = 0.45f),
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Solution Verified",
                                    tint = EmeraldSuccess
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step-by-Step PYQ Solution & Concept Guide",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EmeraldSuccess
                                )
                            }

                            // Key Formulas Box
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "KEY FORMULAS USED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    currentQuestion.keyFormulas.forEach { formula ->
                                        Text(
                                            text = "• $formula",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Derivation Steps
                            currentQuestion.stepByStepSolution.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            // Underlying Concept Summary
                            Text(
                                text = "Concept Insight: ${currentQuestion.conceptSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Common Exam Pitfall Warning
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
                                        contentDescription = "Common Pitfall",
                                        tint = CrimsonAlert,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Negative Marking Trap: ${currentQuestion.commonPitfall}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Concept Video Resource Button
                            FilledTonalButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(currentQuestion.videoResourceUrl)
                                    )
                                    try {
                                        context.startActivity(intent)
                                    } catch (ignored: Exception) {
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = "Watch Concept Video",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Watch Video: ${currentQuestion.videoResourceTitle}")
                            }
                        }
                    }
                }
            }

            // Bottom Navigation & Submit Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigateIndex(session.currentIndex - 1) },
                        enabled = session.currentIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    if (session.currentIndex < session.questions.lastIndex) {
                        Button(
                            onClick = { onNavigateIndex(session.currentIndex + 1) },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("next_question_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!session.isSubmitted) {
                        Button(
                            onClick = onSubmitSession,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("submit_exam_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Submit & Score",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onCloseSession,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("finish_review_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Done Reviewing")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionScorecardBanner(summary: SessionResultSummary) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, AmberGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "XP Earned",
                        tint = AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Test Evaluated! +${summary.xpEarned} XP Earned",
                            style = MaterialTheme.typography.titleMedium,
                            color = AmberGold
                        )
                        Text(
                            text = "Score: ${summary.scoreObtained} / ${summary.maxScore}  •  Accuracy: ${summary.accuracyPercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreStatMini("Correct (+4)", "${summary.correctCount}", EmeraldSuccess)
                ScoreStatMini("Wrong (-1)", "${summary.incorrectCount}", CrimsonAlert)
                ScoreStatMini("Skipped (0)", "${summary.unattemptedCount}", MaterialTheme.colorScheme.onSurfaceVariant)
                ScoreStatMini("Time", formatSecondsMmSs(summary.timeSpentSeconds), ElectricCyan)
            }

            if (summary.newlyUnlockedBadges.isNotEmpty()) {
                val badgeTitles = summary.newlyUnlockedBadges.mapNotNull { id ->
                    PyqQuestionBank.milestoneBadges.firstOrNull { it.id == id }?.title
                }
                Surface(
                    color = AmberGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "New Milestone Badge Unlocked: ${badgeTitles.joinToString(", ")}!",
                        style = MaterialTheme.typography.labelLarge,
                        color = AmberGold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (summary.weakTopics.isNotEmpty()) {
                Text(
                    text = "Detected Weak Topics added to Insights & Study Plan: ${summary.weakTopics.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonAlert
                )
            }
        }
    }
}

@Composable
private fun ScoreStatMini(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MetadataPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
