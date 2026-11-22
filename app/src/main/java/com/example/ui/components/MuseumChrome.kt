package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MuseumTab
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
import kotlinx.coroutines.delay

@Composable
fun FilmGrainOverlay(
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    if (reducedMotion) return
    val transition = rememberInfiniteTransition(label = "grain")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grain_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val stepX = 48f
        val stepY = 48f
        var y = (phase * 0.7f) % stepY
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.018f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += stepY
        }
        var x = (phase * 1.3f) % stepX
        var idx = 0
        while (x < size.width) {
            val py = ((idx * 97 + phase * 11f) % size.height)
            drawCircle(
                color = ArchivalOffWhite.copy(alpha = 0.028f),
                radius = 1.4f,
                center = Offset(x, py)
            )
            x += stepX
            idx++
        }
    }
}

@Composable
fun CinematicIntroScreen(
    onFinished: (rememberSkip: Boolean) -> Unit
) {
    var progress by remember { mutableIntStateOf(0) }
    var glitchActive by remember { mutableStateOf(false) }
    var showHeadline by remember { mutableStateOf(false) }
    var dontShowAgain by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        for (pct in 0..100 step 4) {
            progress = pct
            delay(36L)
        }
        progress = 100
        glitchActive = true
        delay(220L)
        glitchActive = false
        showHeadline = true
        delay(1750L)
        onFinished(dontShowAgain)
    }

    val animatedPct by animateIntAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80),
        label = "intro_pct"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        if (glitchActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = AcidLime.copy(alpha = 0.14f),
                    topLeft = Offset(0f, size.height * 0.25f),
                    size = androidx.compose.ui.geometry.Size(size.width, 18f)
                )
                drawRect(
                    color = GlitchCrimson.copy(alpha = 0.22f),
                    topLeft = Offset(0f, size.height * 0.56f),
                    size = androidx.compose.ui.geometry.Size(size.width, 28f)
                )
                drawRect(
                    color = ElectricPurple.copy(alpha = 0.18f),
                    topLeft = Offset(0f, size.height * 0.72f),
                    size = androidx.compose.ui.geometry.Size(size.width, 12f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NODE // MUSEUM-2026",
                style = MaterialTheme.typography.labelMedium,
                color = MutedArchiveGrey
            )
            Surface(
                color = GlassSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, AcidLime.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onFinished(dontShowAgain) }
                    .testTag("skip_intro_button")
            ) {
                Text(
                    text = stringResource(R.string.intro_skip),
                    style = MaterialTheme.typography.labelMedium,
                    color = AcidLime,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.intro_connecting),
                style = MaterialTheme.typography.labelLarge,
                color = AcidLime
            )

            Text(
                text = "${animatedPct}%",
                style = MaterialTheme.typography.displayLarge,
                color = if (glitchActive) GlitchCrimson else ArchivalOffWhite
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(GlassBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedPct / 100f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(ElectricPurple, AcidLime))
                        )
                )
            }

            AnimatedVisibility(
                visible = showHeadline,
                enter = fadeIn(tween(380, easing = FastOutSlowInEasing)),
                exit = fadeOut()
            ) {
                Box {
                    Text(
                        text = stringResource(R.string.intro_headline),
                        style = MaterialTheme.typography.displayLarge,
                        color = ElectricPurple.copy(alpha = 0.45f),
                        modifier = Modifier.offset(x = 3.dp, y = 2.dp)
                    )
                    Text(
                        text = stringResource(R.string.intro_headline),
                        style = MaterialTheme.typography.displayLarge,
                        color = ArchivalOffWhite
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clickable { dontShowAgain = !dontShowAgain }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = dontShowAgain,
                onCheckedChange = { dontShowAgain = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = AcidLime,
                    checkmarkColor = VoidBlack,
                    uncheckedColor = MutedArchiveGrey
                )
            )
            Text(
                text = "Skip intro automatically on return visits",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedArchiveGrey
            )
        }
    }
}

private fun MuseumTab.icons(): Pair<ImageVector, ImageVector> = when (this) {
    MuseumTab.EXPLORE -> Icons.Filled.Explore to Icons.Outlined.Explore
    MuseumTab.LEGENDS -> Icons.Filled.Star to Icons.Outlined.AutoAwesome
    MuseumTab.MEMES -> Icons.Filled.Collections to Icons.Outlined.Collections
    MuseumTab.SOUND_LAB -> Icons.Filled.GraphicEq to Icons.Outlined.GraphicEq
    MuseumTab.TIMELINE -> Icons.Filled.History to Icons.Outlined.History
    MuseumTab.COMMUNITY -> Icons.Filled.Forum to Icons.Outlined.Forum
}

@Composable
fun FloatingBottomNavBar(
    selectedTab: MuseumTab,
    onSelectTab: (MuseumTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, GlassBorder),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MuseumTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val (filledIcon, outlinedIcon) = tab.icons()
                Surface(
                    color = if (selected) AcidLime else Color.Transparent,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.route}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) filledIcon else outlinedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) VoidBlack else ArchivalOffWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = tab.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selected) VoidBlack else ArchivalOffWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuseumNavigationRail(
    selectedTab: MuseumTab,
    onSelectTab: (MuseumTab) -> Unit
) {
    NavigationRail(
        containerColor = ObsidianSurface,
        contentColor = ArchivalOffWhite,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AcidLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IA",
                        style = MaterialTheme.typography.labelLarge,
                        color = VoidBlack
                    )
                }
            }
        }
    ) {
        MuseumTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val (filledIcon, outlinedIcon) = tab.icons()
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = VoidBlack,
                    selectedTextColor = AcidLime,
                    indicatorColor = AcidLime,
                    unselectedIconColor = MutedArchiveGrey,
                    unselectedTextColor = MutedArchiveGrey
                ),
                modifier = Modifier.testTag("rail_tab_${tab.route}")
            )
        }
    }
}

@Composable
fun CinematicFooter(
    reducedMotion: Boolean,
    onToggleReducedMotion: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    onSignOut: () -> Unit,
    userEmail: String?
) {
    val context = LocalContext.current
    var showAttributionDialog by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "footer_pulse")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "footer_glow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(color = GlassBorder)

        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ARCHIVE TRANSMISSION // END OF HALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime.copy(alpha = if (reducedMotion) 1f else glowAlpha)
                )

                Text(
                    text = stringResource(R.string.footer_headline),
                    style = MaterialTheme.typography.displayMedium,
                    color = ArchivalOffWhite
                )

                Text(
                    text = stringResource(R.string.footer_sub),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedArchiveGrey
                )

                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AcidLime.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.footer_credit_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedArchiveGrey
                        )
                        Text(
                            text = stringResource(R.string.footer_creator),
                            style = MaterialTheme.typography.headlineMedium,
                            color = AcidLime
                        )
                        Text(
                            text = "Digital Culture Museum • Experimental UI & Motion Architecture",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArchivalOffWhite
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REDUCED MOTION MODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = ArchivalOffWhite
                        )
                        Text(
                            text = "Disables grain & subtle motion overlays",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedArchiveGrey
                        )
                    }
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = onToggleReducedMotion,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidBlack,
                            checkedTrackColor = AcidLime,
                            uncheckedThumbColor = ArchivalOffWhite,
                            uncheckedTrackColor = GlassBorder
                        ),
                        modifier = Modifier.testTag("reduced_motion_switch")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplayIntro,
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArchivalOffWhite),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("REPLAY INTRO", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showAttributionDialog = true },
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ATTRIBUTIONS", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                                )
                            }
                        },
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricPurple),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CULTURE LINKS", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userEmail?.let { "SIGNED IN: $it" } ?: "ARCHIVE VISITOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedArchiveGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = GlitchCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SIGN OUT",
                            style = MaterialTheme.typography.labelMedium,
                            color = GlitchCrimson
                        )
                    }
                }

                Text(
                    text = "© 2026 THE INTERNET ARCHIVE • DEVELOPED BY SALMAN PATHAAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedArchiveGrey
                )
            }
        }
    }

    if (showAttributionDialog) {
        AlertDialog(
            onDismissRequest = { showAttributionDialog = false },
            containerColor = ObsidianSurface,
            titleContentColor = AcidLime,
            textContentColor = ArchivalOffWhite,
            title = {
                Text("ASSET ATTRIBUTION & LICENSING", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    text = "• Typography: Space Grotesk, Archivo Black, Inter, Space Mono via Google Fonts (SIL OFL).\n" +
                        "• Exhibit Artwork: Original bespoke museum illustrations bundled locally (zero third-party hotlinking).\n" +
                        "• Sound Lab Audio: Original real-time PCM synthesis via Android AudioTrack.\n" +
                        "• Cultural References: Wikipedia & public historical links for educational citation.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAttributionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AcidLime, contentColor = VoidBlack)
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}
