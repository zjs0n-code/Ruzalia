/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The structural half of nuclear's design language: the parts Material 3 has
 * no colour role for. nuclear draws a real 2px outline on everything and casts
 * a hard, zero-blur shadow offset by 2px.
 *
 * nuclear itself drops the shadow and thins the border to 1px in dark mode.
 * Ruzalia keeps both in dark mode, because a phone music player is used in
 * dark mode almost exclusively and that is precisely where the chunk needs to
 * be visible.
 */
@Immutable
data class NuclearMetrics(
    val borderWidth: Dp = 2.dp,
    val shadowOffset: Dp = 2.dp,
    val radiusSmall: Dp = 4.dp,
    val radiusMedium: Dp = 8.dp,
    val radiusLarge: Dp = 12.dp,
)

val NuclearShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp),
)

val LocalNuclearPalette = staticCompositionLocalOf { NuclearThemeId.DEFAULT.palette(dark = true) }
val LocalNuclearMetrics = staticCompositionLocalOf { NuclearMetrics() }

/** Accessor mirroring `MaterialTheme`, for the tokens Material cannot carry. */
object NuclearTheme {
    val colors: NuclearPalette
        @Composable @ReadOnlyComposable get() = LocalNuclearPalette.current

    val metrics: NuclearMetrics
        @Composable @ReadOnlyComposable get() = LocalNuclearMetrics.current
}
