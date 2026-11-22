package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.FavoriteItemType
import com.example.data.model.SoundArtifact
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ArchivalOffWhite
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.MutedArchiveGrey
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.VoidBlack

@Composable
fun SoundLabScreen(
    sounds: List<SoundArtifact>,
    selectedCategory: String,
    favoritesOnly: Boolean,
    favoriteIds: Set<String>,
    playingSoundId: String?,
    progress: Float,
    waveformBars: List<Float>,
    volume: Float,
    onSelectCategory: (String) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onTogglePlay: (SoundArtifact) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: (String, FavoriteItemType) -> Unit
) {
    val activeSound = CuratedArchiveCatalog.sounds.firstOrNull { it.id == playingSoundId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WING C // INTERNET SOUND LAB",
                    style = MaterialTheme.typography.labelLarge,
                    color = AcidLime
                )
                Text(
                    text = "SYNTHESIZED\nAUDIO VAULT",
                    style = MaterialTheme.typography.displayMedium,
                    color = ArchivalOffWhite
                )
                Text(
                    text = "Original real-time PCM audio recreations of iconic web textures—zero broken streams.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey
                )
            }
        }

        // --- LIVE WAVEFORM DECK & VOLUME ---
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (activeSound != null) activeSound.accentColor else GlassBorder
                ),
                modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeSound != null) "NOW SYNTHESIZING" else "OSCILLOSCOPE IDLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeSound?.accentColor ?: MutedArchiveGrey
                            )
                            Text(
                                text = activeSound?.title ?: "Select any sound card below",
                                style = MaterialTheme.typography.titleLarge,
                                color = ArchivalOffWhite
                            )
                        }
                        Surface(
                            color = GlassSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text(
                                text = activeSound?.durationLabel ?: "00.0s",
                                style = MaterialTheme.typography.labelMedium,
                                color = AcidLime,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Live Canvas Waveform
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidBlack)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        val barColor = activeSound?.accentColor ?: AcidLime
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val count = waveformBars.size.coerceAtLeast(1)
                            val slotWidth = size.width / count
                            val barWidth = (slotWidth * 0.56f).coerceAtLeast(4f)
                            val centerY = size.height / 2f

                            waveformBars.forEachIndexed { idx, amp ->
                                val h = (amp * size.height * 0.9f).coerceAtLeast(6f)
                                val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                                drawRoundRect(
                                    color = if (activeSound != null) barColor else barColor.copy(alpha = 0.35f),
                                    topLeft = Offset(x, centerY - h / 2f),
                                    size = Size(barWidth, h),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 80),
                        label = "sound_progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        color = activeSound?.accentColor ?: AcidLime,
                        trackColor = GlassBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )

                    // Volume control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Master volume",
                            tint = AcidLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = volume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = AcidLime,
                                activeTrackColor = AcidLime,
                                inactiveTrackColor = GlassBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sound_volume_slider")
                        )
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = ArchivalOffWhite
                        )
                    }
                }
            }
        }

        // --- SOUND CATEGORIES & FAVORITES FILTER ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CuratedArchiveCatalog.soundCategories) { category ->
                    val selected = selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(category.uppercase(), style = MaterialTheme.typography.labelMedium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AcidLime,
                            selectedLabelColor = VoidBlack,
                            containerColor = ObsidianSurface,
                            labelColor = ArchivalOffWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = GlassBorder
                        ),
                        modifier = Modifier.height(48.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = onToggleFavoritesOnly,
                        leadingIcon = {
                            Icon(
                                imageVector = if (favoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text("FAVORITES", style = MaterialTheme.typography.labelMedium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GlitchCrimson,
                            selectedLabelColor = ArchivalOffWhite,
                            selectedLeadingIconColor = ArchivalOffWhite,
                            containerColor = ObsidianSurface,
                            labelColor = ArchivalOffWhite,
                            iconColor = GlitchCrimson
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = favoritesOnly,
                            borderColor = GlassBorder
                        ),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }
        }

        if (sounds.isEmpty()) {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NO FAVORITE SOUNDS YET",
                            style = MaterialTheme.typography.titleMedium,
                            color = AcidLime
                        )
                        Text(
                            text = "Tap the heart on any sound card to pin it to your Sound Lab favorites.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedArchiveGrey
                        )
                    }
                }
            }
        } else {
            items(sounds, key = { it.id }) { sound ->
                val isPlaying = playingSoundId == sound.id
                val isFavorite = favoriteIds.contains(sound.id)
                SoundCard(
                    sound = sound,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    onTogglePlay = { onTogglePlay(sound) },
                    onToggleFavorite = { onToggleFavorite(sound.id, FavoriteItemType.SOUND) }
                )
            }
        }
    }
}

@Composable
private fun SoundCard(
    sound: SoundArtifact,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = if (isPlaying) GlassSurface else ObsidianSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isPlaying) sound.accentColor else GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onTogglePlay)
            .testTag("sound_card_${sound.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = if (isPlaying) sound.accentColor else GlassSurface,
                shape = CircleShape,
                border = BorderStroke(1.dp, sound.accentColor),
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlay)
                    .testTag("sound_play_${sound.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause ${sound.title}" else "Play ${sound.title}",
                        tint = if (isPlaying) VoidBlack else sound.accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sound.archiveCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = sound.accentColor
                    )
                    Text(
                        text = "• ${sound.category.uppercase()} • ${sound.eraTag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedArchiveGrey
                    )
                }
                Text(
                    text = sound.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchivalOffWhite
                )
                Text(
                    text = sound.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite ${sound.title}",
                        tint = if (isFavorite) GlitchCrimson else MutedArchiveGrey
                    )
                }
                Text(
                    text = sound.durationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedArchiveGrey
                )
            }
        }
    }
}
