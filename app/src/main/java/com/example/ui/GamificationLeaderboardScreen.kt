package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LeaderboardProfile
import com.example.data.PyqQuestionBank
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletAi

@Composable
fun GamificationLeaderboardScreen(
    currentUserId: String,
    userDisplayName: String,
    liveLeaderboard: List<LeaderboardProfile>
) {
    val myProfile = liveLeaderboard.firstOrNull { it.userId == currentUserId }
        ?: LeaderboardProfile(
            userId = currentUserId,
            displayName = userDisplayName.ifBlank { "You (JEE Aspirant)" },
            xpPoints = 120,
            currentStreak = 1,
            bestStreak = 1
        )

    // Combine live cloud users with national JEE benchmark peers for a rich competitive table
    val benchmarkPeers = listOf(
        LeaderboardProfile(
            id = "peer_1",
            userId = "peer_air_1",
            displayName = "Vedant Kulkarni • IITB Target",
            xpPoints = 2480,
            currentStreak = 14,
            bestStreak = 19,
            questionsSolved = 142,
            correctAnswers = 128,
            testsCompleted = 16,
            highScorePercent = 96,
            badgesUnlocked = listOf("first_step", "pyq_warrior", "sniper_accuracy", "streak_7", "jee_titan")
        ),
        LeaderboardProfile(
            id = "peer_2",
            userId = "peer_air_2",
            displayName = "Ananya Verma • IITD Target",
            xpPoints = 1920,
            currentStreak = 9,
            bestStreak = 12,
            questionsSolved = 115,
            correctAnswers = 98,
            testsCompleted = 12,
            highScorePercent = 92,
            badgesUnlocked = listOf("first_step", "pyq_warrior", "sniper_accuracy", "streak_7", "jee_titan")
        ),
        LeaderboardProfile(
            id = "peer_3",
            userId = "peer_air_3",
            displayName = "Rohan Deshmukh • IITM Target",
            xpPoints = 1340,
            currentStreak = 6,
            bestStreak = 8,
            questionsSolved = 84,
            correctAnswers = 69,
            testsCompleted = 9,
            highScorePercent = 88,
            badgesUnlocked = listOf("first_step", "pyq_warrior", "sniper_accuracy", "streak_3")
        ),
        LeaderboardProfile(
            id = "peer_4",
            userId = "peer_air_4",
            displayName = "Kritika Nair • IITK Target",
            xpPoints = 780,
            currentStreak = 4,
            bestStreak = 5,
            questionsSolved = 48,
            correctAnswers = 38,
            testsCompleted = 5,
            highScorePercent = 82,
            badgesUnlocked = listOf("first_step", "streak_3", "sniper_accuracy")
        )
    )

    val combinedRankings = (liveLeaderboard + benchmarkPeers.filter { peer ->
        liveLeaderboard.none { it.userId == peer.userId }
    }).let { list ->
        if (list.none { it.userId == currentUserId }) list + myProfile else list
    }.sortedByDescending { it.xpPoints }

    val myRank = (combinedRankings.indexOfFirst { it.userId == currentUserId } + 1).coerceAtLeast(1)
    val currentLevel = (myProfile.xpPoints / 250) + 1
    val nextLevelXp = currentLevel * 250
    val levelProgress = ((myProfile.xpPoints % 250) / 250f).coerceIn(0.05f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User XP, Level, Streak & Rank Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, AmberGold.copy(alpha = 0.55f), RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    color = AmberGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "LEVEL $currentLevel • AIR CONTENDER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberGold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${myProfile.xpPoints} XP",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Current Standing: Rank #$myRank of ${combinedRankings.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricCyan
                                )
                            }

                            // Streak Flame Pill
                            Surface(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.border(
                                    1.dp,
                                    AmberGold.copy(alpha = 0.5f),
                                    RoundedCornerShape(18.dp)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Active Streak",
                                        tint = AmberGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "${myProfile.currentStreak} Day Streak",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AmberGold
                                    )
                                    Text(
                                        text = "Best: ${myProfile.bestStreak}d",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Level Progress Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progress to Level ${currentLevel + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${myProfile.xpPoints} / $nextLevelXp XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { levelProgress },
                                color = AmberGold,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }

                        // XP Earning Rules Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            XpRuleChip("+20 XP / Correct PYQ")
                            XpRuleChip("+40 XP Sniper (80%+)")
                            XpRuleChip("+50 XP Test Finish")
                        }
                    }
                }
            }
        }

        // 2. Milestone Badges Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milestone Badges (${myProfile.badgesUnlocked.size} / ${PyqQuestionBank.milestoneBadges.size} Unlocked)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                PyqQuestionBank.milestoneBadges.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            val isUnlocked = badge.id in myProfile.badgesUnlocked
                            val accent = if (isUnlocked) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                            Surface(
                                color = if (isUnlocked)
                                    AmberGold.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = if (isUnlocked) AmberGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(accent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                                                contentDescription = badge.title,
                                                tint = accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "+${badge.xpBonus} XP",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isUnlocked) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = badge.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Live Cloud Leaderboard Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Leaderboard",
                    tint = AmberGold
                )
                Text(
                    text = "Live Aspirant Leaderboard",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Leaderboard Rows
        itemsIndexed(combinedRankings, key = { _, p -> p.userId }) { index, entry ->
            val rank = index + 1
            val isMe = entry.userId == currentUserId
            val rankColor = when (rank) {
                1 -> AmberGold
                2 -> Color(0xFFE2E8F0)
                3 -> Color(0xFFF59E0B)
                else -> ElectricCyan
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isMe) 1.5.dp else 1.dp,
                        color = if (isMe) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(rankColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelLarge,
                            color = rankColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isMe) "${entry.displayName} (You)" else entry.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (rank <= 3) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Top Rank",
                                    tint = rankColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "${entry.questionsSolved} PYQs • ${entry.accuracyPercent}% Acc • ${entry.currentStreak}d Streak • ${entry.badgesUnlocked.size} Badges",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.xpPoints} XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Best: ${entry.highScorePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun XpRuleChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
