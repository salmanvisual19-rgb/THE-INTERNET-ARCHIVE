package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MuseumTab(val route: String, val label: String) {
    EXPLORE("explore", "Explore"),
    LEGENDS("legends", "Legends"),
    MEMES("memes", "Memes"),
    SOUND_LAB("sound_lab", "Sound Lab"),
    TIMELINE("timeline", "Timeline"),
    COMMUNITY("community", "Community")
}

enum class FavoriteItemType(val wireValue: String) {
    LEGEND("legend"),
    MEME("meme"),
    SOUND("sound");

    companion object {
        fun fromWire(raw: String?): FavoriteItemType =
            entries.firstOrNull { it.wireValue == raw } ?: MEME
    }
}

enum class SoundSynthPreset {
    DIALUP_HANDSHAKE,
    VINE_BOOM,
    EIGHT_BIT_LEVELUP,
    CRT_DEGAUSS,
    MODEM_GLITCH,
    AIRHORN_FANFARE,
    DRAMATIC_STING,
    SYNTHWAVE_CHORD
}

data class ViralLegend(
    val id: String,
    val archiveCode: String,
    val name: String,
    val alias: String,
    val era: String,
    val viralOrigin: String,
    val shortStory: String,
    val popularDialogue: String,
    val sourceUrl: String,
    val relatedMemeIds: List<String>,
    @DrawableRes val artworkRes: Int,
    val accentColor: Color
)

data class MemeArtifact(
    val id: String,
    val archiveCode: String,
    val title: String,
    val category: String,
    val year: Int,
    val summary: String,
    val impactNote: String,
    val tags: List<String>,
    @DrawableRes val artworkRes: Int,
    val accentColor: Color
)

data class SoundArtifact(
    val id: String,
    val archiveCode: String,
    val title: String,
    val category: String,
    val eraTag: String,
    val durationLabel: String,
    val durationMs: Long,
    val description: String,
    val preset: SoundSynthPreset,
    val accentColor: Color
)

data class TimelineMilestone(
    val id: String,
    val archiveCode: String,
    val year: Int,
    val month: String,
    val monthIndex: Int,
    val title: String,
    val category: String,
    val summary: String,
    val impactTag: String
)

data class LostInternetRelic(
    val id: String,
    val archiveCode: String,
    val title: String,
    val platformEra: String,
    val yearsActive: String,
    val epitaph: String,
    val nostalgiaNote: String,
    val badgeLabel: String
)

@Keep
@IgnoreExtraProperties
data class UserFavorite(
    @DocumentId val id: String = "",
    val userId: String = "",
    val itemId: String = "",
    val itemType: String = "meme",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TrendBattle(
    @DocumentId val id: String = "",
    val title: String = "",
    val contenderA: String = "",
    val contenderB: String = "",
    val eraTag: String = "",
    val status: String = "active",
    val votesA: Long = 0L,
    val votesB: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BattleVote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val battleId: String = "",
    val choice: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommunitySubmission(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val title: String = "",
    val eraYear: Long = 2010L,
    val category: String = "Meme",
    val story: String = "",
    val quote: String = "",
    val status: String = "pending",
    val curatorNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
