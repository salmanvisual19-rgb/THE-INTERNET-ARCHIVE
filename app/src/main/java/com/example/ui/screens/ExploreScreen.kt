package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.LostInternetRelic
import com.example.data.model.MuseumTab
import com.example.ui.components.CinematicFooter
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ExploreScreen(
    favoriteCount: Int,
    approvedCommunityCount: Int,
    reducedMotion: Boolean,
    userEmail: String?,
    onNavigateTab: (MuseumTab) -> Unit,
    onToggleReducedMotion: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    onSignOut: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var targetCount by remember { mutableIntStateOf(0) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var expandedRelicId by remember { mutableStateOf<String?>(null) }

    val totalArtifacts = CuratedArchiveCatalog.totalCuratedArtifacts + approvedCommunityCount
    LaunchedEffect(totalArtifacts) {
        targetCount = totalArtifacts
    }
    val liveCounter by animateIntAsState(
        targetValue = targetCount,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 1100),
        label = "archive_counter"
    )

    val floatTransition = rememberInfiniteTransition(label = "hero_float")
    val scrollCueY by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scroll_cue"
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HERO SECTION ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GlassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, AcidLime.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AcidLime)
                            )
                            Text(
                                text = "ARCHIVE COUNT: ${liveCounter.toString().padStart(3, '0')}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AcidLime
                            )
                        }
                    }

                    Surface(
                        color = GlassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = "VAULT: $favoriteCount SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArchivalOffWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Touch-responsive 3D parallax hero composition
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ObsidianSurface)
                        .pointerInput(reducedMotion) {
                            if (!reducedMotion) {
                                detectDragGestures(
                                    onDragEnd = {
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(-36f, 36f)
                                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-24f, 24f)
                                    }
                                )
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_museum_1795377163161),
                        contentDescription = "Digital Museum Central Installation",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (!reducedMotion) {
                                    translationX = dragOffsetX * 0.45f
                                    translationY = dragOffsetY * 0.45f
                                    rotationY = dragOffsetX * 0.12f
                                    rotationX = -dragOffsetY * 0.12f
                                    scaleX = 1.08f
                                    scaleY = 1.08f
                                }
                            }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        VoidBlack.copy(alpha = 0.55f),
                                        VoidBlack.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    // Floating layered archival thumbnail badges
                    Surface(
                        color = VoidBlack.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectricPurple),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .graphicsLayer {
                                if (!reducedMotion) {
                                    translationX = -dragOffsetX * 0.3f
                                    translationY = -dragOffsetY * 0.3f
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "HALL 01 // 1994–2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArchivalOffWhite
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "EXHIBITION BY SALMAN PATHAAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Text(
                            text = "Drag installation to tilt perspective",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArchivalOffWhite.copy(alpha = 0.85f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.hero_headline),
                    style = MaterialTheme.typography.displayLarge,
                    color = ArchivalOffWhite
                )

                Text(
                    text = stringResource(R.string.hero_subheadline),
                    style = MaterialTheme.typography.titleMedium,
                    color = MutedArchiveGrey
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcidLime,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("explore_archive_cta")
                    ) {
                        Text(
                            text = "[ ${stringResource(R.string.hero_cta)} ]",
                            style = MaterialTheme.typography.labelLarge,
                            color = VoidBlack
                        )
                    }

                    Surface(
                        color = GlassSurface,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .size(54.dp)
                            .offset { IntOffset(0, scrollCueY.roundToInt()) }
                            .clip(CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(1)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Scroll down to exhibits",
                                tint = AcidLime
                            )
                        }
                    }
                }
            }
        }

        // --- MUSEUM WINGS PORTAL DIRECTORY ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "01 // MUSEUM WINGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = AcidLime
                )

                val wings = listOf(
                    Triple(MuseumTab.LEGENDS, "Viral Legends", "Personalities & stories" to Icons.Default.Star),
                    Triple(MuseumTab.MEMES, "Meme Universe", "Visual history & search" to Icons.Default.Collections),
                    Triple(MuseumTab.SOUND_LAB, "Sound Lab", "Synthesized audio deck" to Icons.Default.GraphicEq),
                    Triple(MuseumTab.TIMELINE, "Timeline & Battle", "Year scrubber & voting" to Icons.Default.History),
                    Triple(MuseumTab.COMMUNITY, "Community Hall", "Submissions & moderation" to Icons.Default.Forum)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wings) { (tab, title, descAndIcon) ->
                        val (subtitle, icon) = descAndIcon
                        MuseumWingPortalCard(
                            title = title,
                            subtitle = subtitle,
                            icon = icon,
                            onClick = { onNavigateTab(tab) }
                        )
                    }
                }
            }
        }

        // --- WING E: LOST INTERNET ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "02 // WING E: LOST INTERNET",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                        Text(
                            text = "Forgotten relics, dead plugins & 1999 CRT memories",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedArchiveGrey
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lost_internet_1795377164160),
                        contentDescription = "Lost Internet CRT Exhibition",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(VoidBlack.copy(alpha = 0.85f), Color.Transparent)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = GlitchCrimson,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SIGNAL LOST",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArchivalOffWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "THE FORGOTTEN\nWEB ARCHIVE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArchivalOffWhite
                        )
                    }
                }
            }
        }

        items(CuratedArchiveCatalog.lostInternetRelics, key = { it.id }) { relic ->
            val isExpanded = expandedRelicId == relic.id
            LostRelicCard(
                relic = relic,
                expanded = isExpanded,
                onClick = {
                    expandedRelicId = if (isExpanded) null else relic.id
                }
            )
        }

        // --- CINEMATIC FOOTER ---
        item {
            CinematicFooter(
                reducedMotion = reducedMotion,
                onToggleReducedMotion = onToggleReducedMotion,
                onReplayIntro = onReplayIntro,
                onSignOut = onSignOut,
                userEmail = userEmail
            )
        }
    }
}

@Composable
private fun MuseumWingPortalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = GlassSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .width(196.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("portal_${title.lowercase().replace(" ", "_")}")
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
                Surface(
                    color = AcidLime.copy(alpha = 0.14f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AcidLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open $title",
                    tint = MutedArchiveGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ArchivalOffWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedArchiveGrey
            )
        }
    }
}

@Composable
private fun LostRelicCard(
    relic: LostInternetRelic,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (expanded) CyberCyan else GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("lost_relic_${relic.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${relic.archiveCode} • ${relic.yearsActive}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text(
                        text = relic.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = ArchivalOffWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = relic.title,
                style = MaterialTheme.typography.titleLarge,
                color = ArchivalOffWhite
            )
            Text(
                text = relic.epitaph,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedArchiveGrey
            )
            AnimatedVisibility(visible = expanded) {
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PLATFORM ERA: ${relic.platformEra.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Text(
                            text = relic.nostalgiaNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArchivalOffWhite
                        )
                    }
                }
            }
        }
    }
}
