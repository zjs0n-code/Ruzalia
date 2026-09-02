/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.ui.graphics.Color

/**
 * Builds a full palette from a single colour.
 *
 * nuclear's five presets are one structure at five hues: every slot sits at a
 * fixed lightness and chroma and only the hue moves. Default is hue 5, Aurora
 * 305, Ember 30, Lagoon 205, Arctic Moss 175 - and the lightness of, say, the
 * dark page colour is the same 0.20-0.22 in all of them. Generating a custom
 * theme the same way is what makes a picked colour produce something that sits
 * beside the built-in five rather than looking pasted in.
 *
 * The seven semantic accents are deliberately *not* rotated with the seed.
 * They carry meaning - red is error, green is downloaded - and nuclear keys
 * them to light/dark rather than to the selected theme. Spinning them with the
 * hue would turn "error" green.
 */
fun seedPalette(seed: Color, dark: Boolean): NuclearPalette {
    val picked = seed.toOklch()
    val hue = picked.hue

    // Keep the seed's own saturation, but inside the range nuclear's presets
    // occupy: a fully grey pick still needs a visible theme, and a neon one
    // should not blow past what the built-ins do.
    val chroma = picked.chroma.coerceIn(0.05f, 0.20f)

    return if (dark) {
        NuclearPalette(
            background = Oklch(0.21f, chroma * 0.15f, hue).toColor(),
            backgroundSecondary = Oklch(0.26f, chroma * 0.17f, hue).toColor(),
            backgroundInput = Oklch(0.14f, chroma * 0.10f, hue).toColor(),
            foreground = Oklch(0.90f, 0.008f, hue).toColor(),
            foregroundSecondary = Oklch(0.78f, chroma * 0.55f, hue).toColor(),
            primary = Oklch(0.60f, chroma * 0.72f, hue).toColor(),
            border = Oklch(0.48f, chroma * 0.22f, hue).toColor(),
            accents = darkAccents,
            isDark = true,
        )
    } else {
        NuclearPalette(
            background = Oklch(0.965f, chroma * 0.16f, hue).toColor(),
            backgroundSecondary = Color.White,
            backgroundInput = Color.White,
            foreground = Color.Black,
            foregroundSecondary = Oklch(0.42f, chroma * 0.60f, hue).toColor(),
            // The picked colour itself, nudged only if it is too dark or too
            // pale to work as a fill.
            primary = Oklch(picked.lightness.coerceIn(0.62f, 0.84f), chroma, hue).toColor(),
            border = Color.Black,
            accents = lightAccents,
            isDark = false,
        )
    }
}
