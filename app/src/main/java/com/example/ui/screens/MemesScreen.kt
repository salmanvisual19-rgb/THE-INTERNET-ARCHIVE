package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.FavoriteItemType
import com.example.data.model.MemeArtifact
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ArchivalOffWhite
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.MutedArchiveGrey
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.VoidBlack

@Composable
fun MemesScreen(
    memes: List<MemeArtifact>,
    searchQuery: String,
    selectedCategory: String,
    favoriteIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleFavorite: (String, FavoriteItemType) -> Unit
) {
    val context = LocalContext.current
    var favoritesOnly by remember { mutableStateOf(false) }
    var inspectedMeme by remember { mutableStateOf<MemeArtifact?>(null) }

    val displayMemes = remember(memes, favoritesOnly, favoriteIds) {
        if (favoritesOnly) memes.filter { favoriteIds.contains(it.id) } else memes
    }

    if (inspectedMeme != null) {
        BackHandler { inspectedMeme = null }
        MemeInspectionDialog(
            meme = inspectedMeme!!,
            isFavorite = favoriteIds.contains(inspectedMeme!!.id),
            onDismiss = { inspectedMeme = null },
            onToggleFavorite = { onToggleFavorite(inspectedMeme!!.id, FavoriteItemType.MEME) },
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${inspectedMeme!!.title} (${inspectedMeme!!.year}): ${inspectedMeme!!.summary} — THE INTERNET ARCHIVE"
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Share Meme Exhibit"))
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        val columnCount = if (maxWidth >= 560.dp) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(columnCount) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WING B // MEME UNIVERSE",
                        style = MaterialTheme.typography.labelLarge,
                        color = AcidLime
                    )
                    Text(
                        text = "VISUAL FOLKLORE\nGALLERY",
                        style = MaterialTheme.typography.displayMedium,
                        color = ArchivalOffWhite
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                "Search memes, years, tags...",
                                color = MutedArchiveGrey,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = AcidLime)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = ArchivalOffWhite)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcidLime,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface,
                            focusedTextColor = ArchivalOffWhite,
                            unfocusedTextColor = ArchivalOffWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meme_search_input")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CuratedArchiveCatalog.memeCategories) { category ->
                            val selected = selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCategory(category) },
                                label = {
                                    Text(
                                        text = category.uppercase(),
                                        style = MaterialTheme.typography.labelMedium
                                    )
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
                                onClick = { favoritesOnly = !favoritesOnly },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (favoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text("SAVED", style = MaterialTheme.typography.labelMedium)
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
            }

            if (displayMemes.isEmpty()) {
                item(span = { GridItemSpan(columnCount) }) {
                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "NO ARTIFACTS MATCH FILTER",
                                style = MaterialTheme.typography.titleMedium,
                                color = AcidLime
                            )
                            Text(
                                text = "Try clearing your search query or switching back to All eras.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedArchiveGrey
                            )
                            Button(
                                onClick = {
                                    onSearchChange("")
                                    onSelectCategory("All")
                                    favoritesOnly = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AcidLime,
                                    contentColor = VoidBlack
                                )
                            ) {
                                Text("RESET FILTERS", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else {
                items(displayMemes, key = { it.id }) { meme ->
                    MemeCard(
                        meme = meme,
                        isFavorite = favoriteIds.contains(meme.id),
                        onClick = { inspectedMeme = meme },
                        onToggleFavorite = { onToggleFavorite(meme.id, FavoriteItemType.MEME) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemeCard(
    meme: MemeArtifact,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "meme_press"
    )

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .testTag("meme_card_${meme.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = meme.artworkRes),
                    contentDescription = meme.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, VoidBlack.copy(alpha = 0.8f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VoidBlack.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, meme.accentColor)
                    ) {
                        Text(
                            text = "${meme.year} • ${meme.category.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = meme.accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(VoidBlack.copy(alpha = 0.72f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite ${meme.title}",
                            tint = if (isFavorite) GlitchCrimson else ArchivalOffWhite
                        )
                    }
                }
                Text(
                    text = meme.archiveCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArchivalOffWhite.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = meme.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ArchivalOffWhite
                )
                Text(
                    text = meme.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemeInspectionDialog(
    meme: MemeArtifact,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        titleContentColor = ArchivalOffWhite,
        textContentColor = ArchivalOffWhite,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${meme.archiveCode} • ${meme.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = meme.accentColor
                )
                Text(text = meme.title, style = MaterialTheme.typography.headlineMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(id = meme.artworkRes),
                    contentDescription = meme.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Text(text = meme.summary, style = MaterialTheme.typography.bodyLarge)
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CULTURAL IMPACT",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = meme.impactNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedArchiveGrey
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    meme.tags.forEach { tag ->
                        Surface(
                            color = GlassSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArchivalOffWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) GlitchCrimson else ArchivalOffWhite
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = AcidLime)
                }
                TextButton(onClick = onDismiss) {
                    Text("CLOSE", style = MaterialTheme.typography.labelMedium, color = AcidLime)
                }
            }
        }
    )
}
