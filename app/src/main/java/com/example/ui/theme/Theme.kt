package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ArchiveColorScheme = darkColorScheme(
    primary = AcidLime,
    onPrimary = VoidBlack,
    primaryContainer = AcidLimeDim,
    onPrimaryContainer = VoidBlack,
    secondary = ElectricPurple,
    onSecondary = ArchivalOffWhite,
    secondaryContainer = GlassSurface,
    onSecondaryContainer = ArchivalOffWhite,
    tertiary = GlitchCrimson,
    onTertiary = ArchivalOffWhite,
    background = VoidBlack,
    onBackground = ArchivalOffWhite,
    surface = VoidBlack,
    onSurface = ArchivalOffWhite,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = MutedArchiveGrey,
    surfaceContainerLowest = VoidBlack,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = GlassSurface,
    surfaceContainerHigh = GlassSurface,
    outline = GlassBorder,
    outlineVariant = GlassBorder,
    error = GlitchCrimson,
    onError = ArchivalOffWhite
)

private val MuseumShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun InternetArchiveTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArchiveColorScheme,
        typography = MuseumTypography,
        shapes = MuseumShapes,
        content = content
    )
}
