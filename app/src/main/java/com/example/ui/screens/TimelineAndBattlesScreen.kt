package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.BattleVote
import com.example.data.model.TimelineMilestone
import com.example.data.model.TrendBattle
import com.example.data.model.UiState
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ArchivalOffWhite
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.MutedArchiveGrey
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.VoidBlack

private enum class TimelineSubMode { TIMELINE, BATTLES }

@Composable
fun TimelineAndBattlesScreen(
    milestones: List<TimelineMilestone>,
    selectedYear: Int?,
    selectedMonth: String,
    battlesState: UiState<List<TrendBattle>>,
    userVotesByBattleId: Map<String, BattleVote?>,
    onSelectYear: (Int?) -> Unit,
    onSelectMonth: (String) -> Unit,
    onCastVote: (battleId: String, choice: String) -> Unit
) {
    var mode by remember { mutableStateOf(TimelineSubMode.TIMELINE) }

    val availableYears = remember {
        CuratedArchiveCatalog.timelineMilestones.map { it.year }.distinct().sorted()
    }
    val availableMonths = remember {
        listOf("ALL", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "AUG", "NOV", "DEC")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (mode == TimelineSubMode.TIMELINE) "WING D // TREND TIMELINE" else "WING F // TREND BATTLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = AcidLime
                )
                Text(
                    text = if (mode == TimelineSubMode.TIMELINE) "CHRONOLOGY OF\nTHE CULTURE" else "HEAD-TO-HEAD\nTREND BATTLES",
                    style = MaterialTheme.typography.displayMedium,
                    color = ArchivalOffWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeSwitchPill(
                        label = "TIMELINE",
                        selected = mode == TimelineSubMode.TIMELINE,
                        onClick = { mode = TimelineSubMode.TIMELINE },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("switch_timeline_mode")
                    )
                    ModeSwitchPill(
                        label = "TREND BATTLE",
                        selected = mode == TimelineSubMode.BATTLES,
                        onClick = { mode = TimelineSubMode.BATTLES },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("switch_battle_mode")
                    )
                }
            }
        }

        if (mode == TimelineSubMode.TIMELINE) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FILTER BY YEAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedArchiveGrey
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedYear == null,
                                onClick = { onSelectYear(null) },
                                label = { Text("ALL ERAS", style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AcidLime,
                                    selectedLabelColor = VoidBlack,
                                    containerColor = ObsidianSurface,
                                    labelColor = ArchivalOffWhite
                                ),
                                modifier = Modifier.height(48.dp)
                            )
                        }
                        items(availableYears) { yr ->
                            val selected = selectedYear == yr
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectYear(if (selected) null else yr) },
                                label = { Text("$yr", style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AcidLime,
                                    selectedLabelColor = VoidBlack,
                                    containerColor = ObsidianSurface,
                                    labelColor = ArchivalOffWhite
                                ),
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }

                    Text(
                        text = "FILTER BY MONTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedArchiveGrey
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableMonths) { month ->
                            val selected = selectedMonth == month
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectMonth(month) },
                                label = { Text(month, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple,
                                    selectedLabelColor = ArchivalOffWhite,
                                    containerColor = ObsidianSurface,
                                    labelColor = ArchivalOffWhite
                                ),
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            }

            if (milestones.isEmpty()) {
                item {
                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "NO RECORDS FOR SELECTED YEAR/MONTH",
                                style = MaterialTheme.typography.titleMedium,
                                color = AcidLime
                            )
                            Button(
                                onClick = {
                                    onSelectYear(null)
                                    onSelectMonth("ALL")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AcidLime, contentColor = VoidBlack)
                            ) {
                                Text("SHOW FULL TIMELINE", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else {
                items(milestones, key = { it.id }) { milestone ->
                    TimelineMilestoneRow(milestone = milestone)
                }
            }
        } else {
            // --- TREND BATTLE & LIVE LEADERBOARD ---
            when (battlesState) {
                is UiState.Loading -> {
                    items(3) {
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "LOADING LIVE BATTLES...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MutedArchiveGrey
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Surface(
                            color = GlitchCrimson.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GlitchCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = battlesState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArchivalOffWhite,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val battles = battlesState.data
                    items(battles, key = { it.id }) { battle ->
                        TrendBattleCard(
                            battle = battle,
                            userVote = userVotesByBattleId[battle.id],
                            onVote = { choice -> onCastVote(battle.id, choice) }
                        )
                    }

                    // Live Leaderboard
                    item {
                        LiveBattleLeaderboard(battles = battles)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSwitchPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) AcidLime else ObsidianSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) AcidLime else GlassBorder),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) VoidBlack else ArchivalOffWhite
            )
        }
    }
}

@Composable
private fun TimelineMilestoneRow(milestone: TimelineMilestone) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(64.dp)
        ) {
            Surface(
                color = GlassSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AcidLime.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = milestone.month,
                        style = MaterialTheme.typography.labelSmall,
                        color = AcidLime
                    )
                    Text(
                        text = "${milestone.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchivalOffWhite
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(42.dp)
                    .background(GlassBorder)
            )
        }

        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${milestone.archiveCode} • ${milestone.category.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Text(
                        text = milestone.impactTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricPurple
                    )
                }
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ArchivalOffWhite
                )
                Text(
                    text = milestone.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey
                )
            }
        }
    }
}

@Composable
private fun TrendBattleCard(
    battle: TrendBattle,
    userVote: BattleVote?,
    onVote: (String) -> Unit
) {
    val totalVotes = (battle.votesA + battle.votesB).coerceAtLeast(1L)
    val pctA = if (battle.votesA + battle.votesB == 0L) 0.5f else battle.votesA.toFloat() / totalVotes
    val animatedPctA by animateFloatAsState(
        targetValue = pctA,
        animationSpec = tween(durationMillis = 500),
        label = "battle_pct"
    )
    val hasVoted = userVote != null

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (hasVoted) AcidLime.copy(alpha = 0.7f) else GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("battle_card_${battle.id}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = AcidLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = battle.eraTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = AcidLime
                    )
                }
                Text(
                    text = "${battle.votesA + battle.votesB} TOTAL VOTES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedArchiveGrey
                )
            }

            Text(
                text = battle.title,
                style = MaterialTheme.typography.headlineMedium,
                color = ArchivalOffWhite
            )

            // Animated split bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElectricPurple)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPctA)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(AcidLime, CyberCyan))
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContenderVoteButton(
                    name = battle.contenderA,
                    votes = battle.votesA,
                    percent = (pctA * 100).toInt(),
                    isUserChoice = userVote?.choice == "A",
                    locked = hasVoted,
                    accent = AcidLime,
                    onClick = { onVote("A") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vote_a_${battle.id}")
                )
                ContenderVoteButton(
                    name = battle.contenderB,
                    votes = battle.votesB,
                    percent = 100 - (pctA * 100).toInt(),
                    isUserChoice = userVote?.choice == "B",
                    locked = hasVoted,
                    accent = ElectricPurple,
                    onClick = { onVote("B") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vote_b_${battle.id}")
                )
            }
        }
    }
}

@Composable
private fun ContenderVoteButton(
    name: String,
    votes: Long,
    percent: Int,
    isUserChoice: Boolean,
    locked: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isUserChoice) accent.copy(alpha = 0.2f) else GlassSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isUserChoice) accent else GlassBorder),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !locked, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleLarge,
                    color = accent
                )
                if (isUserChoice) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Your vote",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = ArchivalOffWhite
            )
            Text(
                text = if (locked) "$votes votes" else "TAP TO VOTE",
                style = MaterialTheme.typography.labelSmall,
                color = MutedArchiveGrey
            )
        }
    }
}

@Composable
private fun LiveBattleLeaderboard(battles: List<TrendBattle>) {
    val rankedContenders = remember(battles) {
        battles.flatMap { battle ->
            listOf(
                battle.contenderA to battle.votesA,
                battle.contenderB to battle.votesB
            )
        }.sortedByDescending { it.second }.take(6)
    }

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = AcidLime,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "LIVE CULTURE LEADERBOARD",
                    style = MaterialTheme.typography.labelLarge,
                    color = AcidLime
                )
            }
            rankedContenders.forEachIndexed { idx, (name, votes) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (idx == 0) AcidLime else GlassSurface,
                            shape = CircleShape,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (idx == 0) VoidBlack else ArchivalOffWhite
                                )
                            }
                        }
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ArchivalOffWhite
                        )
                    }
                    Text(
                        text = "$votes VOTES",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan
                    )
                }
            }
        }
    }
}
