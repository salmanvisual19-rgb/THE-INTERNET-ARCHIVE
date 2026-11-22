package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CommunitySubmission
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

private enum class CommunityTabMode { EXHIBITS, SUBMIT, CURATOR }

@Composable
fun CommunityScreen(
    approvedState: UiState<List<CommunitySubmission>>,
    mySubmissionsState: UiState<List<CommunitySubmission>>,
    pendingModerationState: UiState<List<CommunitySubmission>>,
    isCuratorAccount: Boolean,
    curatorModeEnabled: Boolean,
    isSubmitting: Boolean,
    onSubmitArtifact: (title: String, eraYear: Int, category: String, story: String, quote: String, onSuccess: () -> Unit) -> Unit,
    onSetCuratorMode: (Boolean) -> Unit,
    onModerateSubmission: (submissionId: String, approve: Boolean, curatorNote: String) -> Unit
) {
    var activeMode by remember { mutableStateOf(CommunityTabMode.EXHIBITS) }
    var showPinDialog by remember { mutableStateOf(false) }

    // Submission form state
    var title by remember { mutableStateOf("") }
    var eraYearSlider by remember { mutableFloatStateOf(2012f) }
    var category by remember { mutableStateOf("Meme") }
    var story by remember { mutableStateOf("") }
    var quote by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var localPreviewUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        localPreviewUri = uri
    }

    val categories = remember { listOf("Meme", "Legend", "Sound", "Lost Internet", "Trend") }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = ObsidianSurface,
            title = {
                Text("CURATOR CONSOLE ACCESS", style = MaterialTheme.typography.titleLarge, color = AcidLime)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isCuratorAccount) {
                            "Enter Curator verification PIN (default: 2026) to unlock pending moderation queue."
                        } else {
                            "Curator moderation is restricted to authorized archive curator accounts."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchivalOffWhite
                    )
                    if (isCuratorAccount) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = null
                            },
                            label = { Text("Curator PIN") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AcidLime,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = ArchivalOffWhite,
                                unfocusedTextColor = ArchivalOffWhite
                            )
                        )
                        if (pinError != null) {
                            Text(pinError!!, color = GlitchCrimson, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                if (isCuratorAccount) {
                    Button(
                        onClick = {
                            if (pinInput.trim() == "2026") {
                                onSetCuratorMode(true)
                                activeMode = CommunityTabMode.CURATOR
                                showPinDialog = false
                            } else {
                                pinError = "Invalid Curator PIN. Use 2026."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcidLime, contentColor = VoidBlack)
                    ) {
                        Text("UNLOCK", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("CLOSE", style = MaterialTheme.typography.labelMedium, color = MutedArchiveGrey)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WING G // COMMUNITY HALL",
                        style = MaterialTheme.typography.labelLarge,
                        color = AcidLime
                    )
                    Surface(
                        color = if (curatorModeEnabled) ElectricPurple else GlassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (curatorModeEnabled) ElectricPurple else GlassBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (curatorModeEnabled) {
                                    onSetCuratorMode(false)
                                    if (activeMode == CommunityTabMode.CURATOR) {
                                        activeMode = CommunityTabMode.EXHIBITS
                                    }
                                } else {
                                    showPinDialog = true
                                }
                            }
                            .testTag("curator_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Curator Mode",
                                tint = ArchivalOffWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (curatorModeEnabled) "CURATOR ON" else "CURATOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArchivalOffWhite
                            )
                        }
                    }
                }

                Text(
                    text = "PUBLIC ARCHIVE\nSUBMISSIONS",
                    style = MaterialTheme.typography.displayMedium,
                    color = ArchivalOffWhite
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CommunityTabPill(
                            label = "EXHIBITS",
                            selected = activeMode == CommunityTabMode.EXHIBITS,
                            onClick = { activeMode = CommunityTabMode.EXHIBITS },
                            modifier = Modifier.testTag("community_tab_exhibits")
                        )
                    }
                    item {
                        CommunityTabPill(
                            label = "SUBMIT DISCOVERY",
                            selected = activeMode == CommunityTabMode.SUBMIT,
                            onClick = { activeMode = CommunityTabMode.SUBMIT },
                            modifier = Modifier.testTag("community_tab_submit")
                        )
                    }
                    if (curatorModeEnabled) {
                        item {
                            CommunityTabPill(
                                label = "MODERATION QUEUE",
                                selected = activeMode == CommunityTabMode.CURATOR,
                                onClick = { activeMode = CommunityTabMode.CURATOR },
                                modifier = Modifier.testTag("community_tab_moderation")
                            )
                        }
                    }
                }
            }
        }

        when (activeMode) {
            CommunityTabMode.EXHIBITS -> {
                when (approvedState) {
                    is UiState.Loading -> {
                        item {
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AcidLime)
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            ErrorBanner(message = approvedState.message)
                        }
                    }
                    is UiState.Success -> {
                        if (approvedState.data.isEmpty()) {
                            item {
                                Surface(
                                    color = ObsidianSurface,
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "NO PUBLISHED COMMUNITY EXHIBITS YET",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AcidLime
                                        )
                                        Text(
                                            text = "Submit an internet memory or viral moment—once reviewed by a curator, it appears permanently in this gallery.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedArchiveGrey
                                        )
                                        Button(
                                            onClick = { activeMode = CommunityTabMode.SUBMIT },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AcidLime,
                                                contentColor = VoidBlack
                                            )
                                        ) {
                                            Text("SUBMIT AN ARTIFACT", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(approvedState.data, key = { it.id }) { sub ->
                                SubmissionCard(submission = sub, showStatus = false)
                            }
                        }
                    }
                }

                // Personal submissions status section
                item {
                    Text(
                        text = "YOUR SUBMISSION STATUS",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberCyan,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                when (mySubmissionsState) {
                    is UiState.Loading -> {
                        item {
                            Text(
                                text = "Syncing your submissions...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedArchiveGrey
                            )
                        }
                    }
                    is UiState.Error -> {
                        item { ErrorBanner(message = mySubmissionsState.message) }
                    }
                    is UiState.Success -> {
                        if (mySubmissionsState.data.isEmpty()) {
                            item {
                                Text(
                                    text = "You haven't submitted any discoveries yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedArchiveGrey
                                )
                            }
                        } else {
                            items(mySubmissionsState.data, key = { "mine_${it.id}" }) { sub ->
                                SubmissionCard(submission = sub, showStatus = true)
                            }
                        }
                    }
                }
            }

            CommunityTabMode.SUBMIT -> {
                item {
                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "ARCHIVE SUBMISSION FORM",
                                style = MaterialTheme.typography.labelLarge,
                                color = AcidLime
                            )
                            Text(
                                text = "All entries enter the Curator Moderation Queue before publication.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedArchiveGrey
                            )

                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it.take(80)
                                    validationError = null
                                },
                                label = { Text("Artifact Title (3–80 chars)") },
                                singleLine = true,
                                colors = fieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submission_title_input")
                            )

                            Column {
                                Text(
                                    text = "ERA YEAR: ${eraYearSlider.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AcidLime
                                )
                                Slider(
                                    value = eraYearSlider,
                                    onValueChange = { eraYearSlider = it },
                                    valueRange = 1990f..2026f,
                                    steps = 35,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AcidLime,
                                        activeTrackColor = AcidLime,
                                        inactiveTrackColor = GlassBorder
                                    )
                                )
                            }

                            Text(
                                text = "CATEGORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedArchiveGrey
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    val selected = category == cat
                                    FilterChip(
                                        selected = selected,
                                        onClick = { category = cat },
                                        label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AcidLime,
                                            selectedLabelColor = VoidBlack,
                                            containerColor = GlassSurface,
                                            labelColor = ArchivalOffWhite
                                        ),
                                        modifier = Modifier.height(48.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = story,
                                onValueChange = {
                                    story = it.take(500)
                                    validationError = null
                                },
                                label = { Text("Origin & Cultural Story (10–500 chars)") },
                                minLines = 3,
                                maxLines = 6,
                                colors = fieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submission_story_input")
                            )

                            OutlinedTextField(
                                value = quote,
                                onValueChange = { quote = it.take(200) },
                                label = { Text("Iconic Dialogue / Tagline (Optional)") },
                                singleLine = true,
                                colors = fieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submission_quote_input")
                            )

                            // Zero-permission Photo Picker optional local preview
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                border = BorderStroke(1.dp, GlassBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (localPreviewUri != null) "CHANGE REFERENCE IMAGE" else "ATTACH REFERENCE PREVIEW (OPTIONAL)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (localPreviewUri != null) {
                                AsyncImage(
                                    model = localPreviewUri,
                                    contentDescription = "Selected submission preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }

                            if (validationError != null) {
                                Text(
                                    text = validationError!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlitchCrimson
                                )
                            }

                            Button(
                                onClick = {
                                    val trimmedTitle = title.trim()
                                    val trimmedStory = story.trim()
                                    when {
                                        trimmedTitle.length < 3 ->
                                            validationError = "Title must be at least 3 characters."
                                        trimmedStory.length < 10 ->
                                            validationError = "Story must be at least 10 characters."
                                        else -> {
                                            onSubmitArtifact(
                                                trimmedTitle,
                                                eraYearSlider.toInt(),
                                                category,
                                                trimmedStory,
                                                quote.trim()
                                            ) {
                                                title = ""
                                                story = ""
                                                quote = ""
                                                localPreviewUri = null
                                                activeMode = CommunityTabMode.EXHIBITS
                                            }
                                        }
                                    }
                                },
                                enabled = !isSubmitting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AcidLime,
                                    contentColor = VoidBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("submit_artifact_button")
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = VoidBlack,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("SUBMIT FOR MODERATION", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }

            CommunityTabMode.CURATOR -> {
                when (pendingModerationState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ElectricPurple)
                            }
                        }
                    }
                    is UiState.Error -> {
                        item { ErrorBanner(message = pendingModerationState.message) }
                    }
                    is UiState.Success -> {
                        if (pendingModerationState.data.isEmpty()) {
                            item {
                                Surface(
                                    color = ObsidianSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Moderation queue is clear. All community submissions have been reviewed.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MutedArchiveGrey,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        } else {
                            items(pendingModerationState.data, key = { "mod_${it.id}" }) { sub ->
                                CuratorModerationCard(
                                    submission = sub,
                                    onModerate = { approve, note ->
                                        onModerateSubmission(sub.id, approve, note)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) AcidLime else ObsidianSurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selected) AcidLime else GlassBorder),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) VoidBlack else ArchivalOffWhite
            )
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: CommunitySubmission,
    showStatus: Boolean
) {
    val statusColor = when (submission.status) {
        "approved" -> AcidLime
        "rejected" -> GlitchCrimson
        else -> CyberCyan
    }

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
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
                    text = "${submission.eraYear} • ${submission.category.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime
                )
                if (showStatus) {
                    Surface(
                        color = statusColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = submission.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = submission.title,
                style = MaterialTheme.typography.titleLarge,
                color = ArchivalOffWhite
            )
            Text(
                text = submission.story,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedArchiveGrey
            )
            if (submission.quote.isNotBlank()) {
                Surface(
                    color = GlassSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${submission.quote}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchivalOffWhite,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            if (submission.curatorNote.isNotBlank()) {
                Text(
                    text = "CURATOR NOTE: ${submission.curatorNote}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricPurple
                )
            }
            Text(
                text = "SUBMITTED BY ${submission.authorName.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MutedArchiveGrey
            )
        }
    }
}

@Composable
private fun CuratorModerationCard(
    submission: CommunitySubmission,
    onModerate: (approve: Boolean, note: String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ElectricPurple),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PENDING REVIEW • ${submission.eraYear} • ${submission.category.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricPurple
            )
            Text(
                text = submission.title,
                style = MaterialTheme.typography.titleLarge,
                color = ArchivalOffWhite
            )
            Text(
                text = submission.story,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedArchiveGrey
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(240) },
                label = { Text("Curator Note (Optional)") },
                singleLine = true,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onModerate(true, note) },
                    colors = ButtonDefaults.buttonColors(containerColor = AcidLime, contentColor = VoidBlack),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("APPROVE", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { onModerate(false, note) },
                    border = BorderStroke(1.dp, GlitchCrimson),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GlitchCrimson),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("REJECT", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = GlitchCrimson.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GlitchCrimson),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArchivalOffWhite,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AcidLime,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = AcidLime,
    unfocusedLabelColor = MutedArchiveGrey,
    focusedTextColor = ArchivalOffWhite,
    unfocusedTextColor = ArchivalOffWhite,
    focusedContainerColor = GlassSurface,
    unfocusedContainerColor = GlassSurface
)
