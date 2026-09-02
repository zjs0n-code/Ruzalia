/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Ruzalia: the scheme is no longer derived from a Material seed colour. It is
 * built from one of nuclear's theme presets, which also brings its typography,
 * corner radii and border/shadow metrics along. See ui/theme/nuclear/.
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score
import com.metrolist.music.ui.theme.nuclear.LocalNuclearMetrics
import com.metrolist.music.ui.theme.nuclear.LocalNuclearPalette
import com.metrolist.music.ui.theme.nuclear.NuclearMetrics
import com.metrolist.music.ui.theme.nuclear.NuclearShapes
import com.metrolist.music.ui.theme.nuclear.NuclearThemeId
import com.metrolist.music.ui.theme.nuclear.NuclearTypography
import com.metrolist.music.ui.theme.nuclear.nuclearColorScheme

/** nuclear's default primary: the candy pink from `--primary` in light mode. */
val DefaultThemeColor = Color(0xFFFF9CB4)

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    nuclearTheme: NuclearThemeId = NuclearThemeId.Default,
    accentOverride: Color? = null,
    content: @Composable () -> Unit,
) {
    val palette = remember(nuclearTheme, darkTheme) { nuclearTheme.palette(darkTheme) }
    val applyPureBlack = darkTheme && pureBlack

    val baseColorScheme = nuclearColorScheme(palette, accentOverride)
    val colorScheme = remember(baseColorScheme, applyPureBlack) {
        baseColorScheme.pureBlack(applyPureBlack)
    }

    // Keep the nuclear tokens in step with pure black, so borders are still
    // drawn against the surface they actually sit on.
    val effectivePalette = remember(palette, applyPureBlack) {
        if (applyPureBlack) {
            palette.copy(background = Color.Black, backgroundInput = Color.Black)
        } else {
            palette
        }
    }

    CompositionLocalProvider(
        LocalNuclearPalette provides effectivePalette,
        LocalNuclearMetrics provides NuclearMetrics(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NuclearTypography,
            shapes = NuclearShapes,
            content = content,
        )
    }
}

fun Bitmap.extractThemeColor(): Color = Color(
    Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .rankedColors(1, DefaultThemeColor.toArgb())
        .first()
)

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> = Score.score(
    swatches.associate { it.rgb to it.population },
    desiredColorCount,
    fallbackColor,
    true,
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black,
        surfaceContainerLow = Color.Black,
        surfaceDim = Color.Black,
        surfaceTint = Color.Black,
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
