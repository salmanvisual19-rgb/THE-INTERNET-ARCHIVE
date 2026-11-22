package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.catalog.CuratedArchiveCatalog
import com.example.data.model.FavoriteItemType
import com.example.data.model.ViralLegend
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ArchivalOffWhite
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.MutedArchiveGrey
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.VoidBlack

@Composable
fun LegendsScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String, FavoriteItemType) -> Unit,
    onJumpToMeme: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedLegend by remember { mutableStateOf<ViralLegend?>(null) }

    if (selectedLegend != null) {
        BackHandler { selectedLegend = null }
        LegendDossierView(
            legend = selectedLegend!!,
            isFavorite = favoriteIds.contains(selectedLegend!!.id),
            onToggleFavorite = { onToggleFavorite(selectedLegend!!.id, FavoriteItemType.LEGEND) },
            onBack = { selectedLegend = null },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${selectedLegend!!.name} (${selectedLegend!!.era}) — ${selectedLegend!!.popularDialogue} | THE INTERNET ARCHIVE"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Viral Legend"))
            },
            onOpenSource = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(selectedLegend!!.sourceUrl))
                    )
                }
            },
            onOpenRelatedMeme = { memeTitle ->
                selectedLegend = null
                onJumpToMeme(memeTitle)
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "WING A // VIRAL LEGENDS",
                    style = MaterialTheme.typography.labelLarge,
                    color = AcidLime
                )
                Text(
                    text = "ICONS OF THE\nOPEN WEB",
                    style = MaterialTheme.typography.displayMedium,
                    color = ArchivalOffWhite
                )
                Text(
                    text = "Tap any dossier to inspect origin stories, iconic dialogue, source links, and related memes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey
                )
            }
        }

        items(CuratedArchiveCatalog.viralLegends, key = { it.id }) { legend ->
            val isFav = favoriteIds.contains(legend.id)
            LegendCard(
                legend = legend,
                isFavorite = isFav,
                onClick = { selectedLegend = legend },
                onToggleFavorite = { onToggleFavorite(legend.id, FavoriteItemType.LEGEND) },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${legend.name}: ${legend.popularDialogue} — Explored in THE INTERNET ARCHIVE"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Viral Legend"))
                }
            )
        }
    }
}

@Composable
private fun LegendCard(
    legend: ViralLegend,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("legend_card_${legend.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                Image(
                    painter = painterResource(id = legend.artworkRes),
                    contentDescription = legend.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, VoidBlack.copy(alpha = 0.88f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VoidBlack.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, legend.accentColor)
                    ) {
                        Text(
                            text = "${legend.archiveCode} • ${legend.era}",
                            style = MaterialTheme.typography.labelSmall,
                            color = legend.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VoidBlack.copy(alpha = 0.72f))
                                .testTag("fav_legend_${legend.id}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite ${legend.name}",
                                tint = if (isFavorite) GlitchCrimson else ArchivalOffWhite
                            )
                        }
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VoidBlack.copy(alpha = 0.72f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share ${legend.name}",
                                tint = ArchivalOffWhite
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = legend.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArchivalOffWhite
                    )
                    Text(
                        text = legend.alias,
                        style = MaterialTheme.typography.labelMedium,
                        color = legend.accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = legend.shortStory,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedArchiveGrey
                )
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = legend.popularDialogue,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ArchivalOffWhite,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text(
                    text = "TAP TO OPEN FULL DOSSIER →",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendDossierView(
    legend: ViralLegend,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenSource: () -> Unit,
    onOpenRelatedMeme: (String) -> Unit
) {
    val relatedMemes = remember(legend.id) {
        CuratedArchiveCatalog.memes.filter { legend.relatedMemeIds.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("legend_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Viral Legends",
                    tint = ArchivalOffWhite
                )
            }
            Text(
                text = "${legend.archiveCode} // DOSSIER",
                style = MaterialTheme.typography.labelLarge,
                color = legend.accentColor
            )
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (isFavorite) GlitchCrimson else ArchivalOffWhite
                )
            }
        }

        Image(
            painter = painterResource(id = legend.artworkRes),
            contentDescription = legend.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Text(
            text = legend.name,
            style = MaterialTheme.typography.displayMedium,
            color = ArchivalOffWhite
        )
        Text(
            text = "${legend.alias} • ${legend.era}",
            style = MaterialTheme.typography.labelLarge,
            color = legend.accentColor
        )

        DossierBlock(label = "VIRAL ORIGIN", body = legend.viralOrigin)
        DossierBlock(label = "ARCHIVE STORY", body = legend.shortStory)

        Surface(
            color = GlassSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, legend.accentColor.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "POPULAR DIALOGUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = legend.accentColor
                )
                Text(
                    text = legend.popularDialogue,
                    style = MaterialTheme.typography.titleLarge,
                    color = ArchivalOffWhite
                )
            }
        }

        if (relatedMemes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RELATED MEMES IN UNIVERSE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedArchiveGrey
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    relatedMemes.forEach { meme ->
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, AcidLime.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onOpenRelatedMeme(meme.title) }
                        ) {
                            Text(
                                text = "${meme.title} →",
                                style = MaterialTheme.typography.labelMedium,
                                color = AcidLime,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidLime,
                    contentColor = VoidBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ORIGINAL SOURCE", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onShare,
                border = BorderStroke(1.dp, GlassBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArchivalOffWhite),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SHARE", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DossierBlock(label: String, body: String) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AcidLime
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = ArchivalOffWhite
            )
        }
    }
}
