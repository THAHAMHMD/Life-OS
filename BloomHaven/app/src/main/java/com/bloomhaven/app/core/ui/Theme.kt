package com.bloomhaven.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BloomBlushPink,
    onPrimary = BloomPaperWhite,
    primaryContainer = BloomBlushPinkContainer,
    onPrimaryContainer = BloomCharcoalBrown,
    secondary = BloomSageGreen,
    onSecondary = BloomPaperWhite,
    secondaryContainer = BloomSageGreenContainer,
    onSecondaryContainer = BloomCharcoalBrown,
    tertiary = BloomTerracotta,
    onTertiary = BloomPaperWhite,
    background = BloomCream,
    onBackground = BloomCharcoalBrown,
    surface = BloomPaperWhite,
    onSurface = BloomCharcoalBrown,
    surfaceVariant = BloomBlushPinkContainer,
    onSurfaceVariant = BloomCharcoalBrown,
    error = BloomSoftRose,
    onError = BloomPaperWhite,
    outline = BloomOutline,
)

private val NightColors = darkColorScheme(
    primary = NightBlush,
    onPrimary = NightIndigo,
    primaryContainer = NightBlushContainer,
    onPrimaryContainer = NightOnSurface,
    secondary = NightSage,
    onSecondary = NightIndigo,
    secondaryContainer = NightSageContainer,
    onSecondaryContainer = NightOnSurface,
    tertiary = NightTerracotta,
    onTertiary = NightIndigo,
    background = NightIndigo,
    onBackground = NightOnSurface,
    surface = NightPlumSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightPlumSurfaceHigh,
    onSurfaceVariant = NightOnSurface,
    error = NightRose,
    onError = NightIndigo,
    outline = NightOutline,
)

/**
 * @param nightHaven true when Night Haven (Bloom Haven's dark mode) should be active —
 * driven by user setting (auto by time / always / never), resolved by the caller.
 */
@Composable
fun BloomHavenTheme(
    nightHaven: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (nightHaven) NightColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = BloomTypography,
        shapes = BloomShapes,
        content = content,
    )
}
