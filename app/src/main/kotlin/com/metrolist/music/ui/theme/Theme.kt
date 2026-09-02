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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score
import com.metrolist.music.ui.theme.nuclear.LocalNuclearMetrics
import com.metrolist.music.ui.theme.nuclear.LocalNuclearPalette
import com.metrolist.music.ui.theme.nuclear.DefaultNuclearMetrics
import com.metrolist.music.ui.theme.nuclear.NuclearShapes
import com.metrolist.music.constants.NuclearCustomThemeKey
import com.metrolist.music.constants.NuclearSeedColorKey
import com.metrolist.music.constants.NuclearThemeKey
import com.metrolist.music.constants.NuclearThemeModeKey
import com.metrolist.music.ui.theme.nuclear.NuclearPalette
import com.metrolist.music.ui.theme.nuclear.NuclearThemeFile
import com.metrolist.music.ui.theme.nuclear.NuclearThemeId
import com.metrolist.music.ui.theme.nuclear.NuclearThemeMode
import com.metrolist.music.ui.theme.nuclear.seedPalette
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.ui.theme.nuclear.NuclearTypography
import com.metrolist.music.ui.theme.nuclear.nuclearColorScheme

/** nuclear's default primary: the candy pink from `--primary` in light mode. */
val DefaultThemeColor = Color(0xFFFF9CB4)

/**
 * Resolves the palette in use from the stored theme settings: one of the five
 * ports of nuclear's presets, a full palette derived from a colour the user
 * picked, or one edited token by token in the advanced editor.
 */
@Composable
fun rememberNuclearPalette(dark: Boolean): NuclearPalette {
    val mode by rememberEnumPreference(NuclearThemeModeKey, NuclearThemeMode.PRESET)
    val preset by rememberEnumPreference(NuclearThemeKey, NuclearThemeId.Default)
    val seedColor by rememberPreference(NuclearSeedColorKey, DefaultThemeColor.toArgb())
    val customTheme by rememberPreference(NuclearCustomThemeKey, "")

    return remember(mode, preset, seedColor, customTheme, dark) {
        when (mode) {
            NuclearThemeMode.PRESET -> preset.palette(dark)
            NuclearThemeMode.SEED -> seedPalette(Color(seedColor), dark)
            NuclearThemeMode.CUSTOM ->
                NuclearThemeFile.decodeOrNull(customTheme)?.palette(dark) ?: preset.palette(dark)
        }
    }
}

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    palette: NuclearPalette = NuclearThemeId.Default.palette(darkTheme),
    accentOverride: Color? = null,
    content: @Composable () -> Unit,
) {
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
        LocalNuclearMetrics provides DefaultNuclearMetrics,
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

/** As [ColorSaver], but able to round-trip "no colour extracted yet". */
val NullableColorSaver = object : Saver<Color?, Int> {
    override fun restore(value: Int): Color? = if (value == NO_COLOR) null else Color(value)
    override fun SaverScope.save(value: Color?): Int = value?.toArgb() ?: NO_COLOR
}

private const val NO_COLOR = 0
