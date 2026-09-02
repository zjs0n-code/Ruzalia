/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * Builds a Material 3 [ColorScheme] wearing a nuclear palette.
 *
 * Material has roughly thirty-five colour roles; nuclear has eight tokens. So
 * rather than invent the missing twenty-seven by hand and risk a role that is
 * unreadable on some screen we never look at, we seed a full scheme from the
 * palette's primary and then overwrite every role that actually carries
 * nuclear's identity. Anything left over is a harmonised tone of the right
 * hue, which is a safe place to land.
 *
 * @param accentOverride when the user has "album art accent" on, the colour
 *   pulled from the artwork. It replaces the primary only - background,
 *   borders, type and shadows stay nuclear.
 */
@Composable
fun nuclearColorScheme(
    palette: NuclearPalette,
    accentOverride: Color? = null,
): ColorScheme {
    val fill = accentOverride ?: palette.primary

    // Metrolist uses `primary` as a *foreground* 79 times - `color =` on Text
    // and `tint =` on Icon. nuclear's primary is a mid-tone fill sitting on a
    // background tinted with the same hue, which as text gives 1.37:1 in the
    // light theme: invisible. So the Material `primary` role becomes an ink
    // derived from that fill and guaranteed readable on the page, while the
    // literal fill stays available as `primaryContainer` and through
    // NuclearTheme.colors for anything that paints with it.
    val ink = remember(fill, palette) { fill.readableOn(palette.background, palette.isDark) }
    val muted = remember(palette) { palette.mutedText() }

    val seeded = rememberDynamicColorScheme(
        seedColor = fill,
        isDark = palette.isDark,
        style = PaletteStyle.Fidelity,
    )

    val onFill = palette.contentColorOn(fill)
    val accents = palette.accents

    return seeded.copy(
        primary = ink,
        onPrimary = palette.contentColorOn(ink),
        primaryContainer = fill,
        onPrimaryContainer = onFill,

        // Metrolist uses `secondary` almost exclusively as a muted *text and
        // icon* colour - 36 of its 55 references are `color =` on a Text - so
        // it maps to nuclear's foreground-secondary, not to an accent.
        // `secondaryContainer` is the one that behaves like a container
        // (navigation pill, playing-row highlight, FAB), and that is primary.
        secondary = muted,
        onSecondary = palette.contentColorOn(muted),
        secondaryContainer = fill,
        onSecondaryContainer = onFill,

        tertiary = accents.purple,
        onTertiary = palette.contentColorOn(accents.purple),
        tertiaryContainer = accents.purple,
        onTertiaryContainer = palette.contentColorOn(accents.purple),

        background = palette.background,
        onBackground = palette.foreground,

        surface = palette.background,
        onSurface = palette.foreground,
        surfaceVariant = palette.backgroundSecondary,
        onSurfaceVariant = muted,

        surfaceContainerLowest = palette.backgroundInput,
        surfaceContainerLow = palette.background,
        surfaceContainer = palette.backgroundSecondary,
        surfaceContainerHigh = palette.backgroundSecondary,
        surfaceContainerHighest = palette.backgroundSecondary,
        surfaceBright = palette.backgroundSecondary,
        surfaceDim = palette.background,

        // nuclear is flat: cards are separated by an outline, never by an
        // elevation tint. Tinting toward the surface itself makes Material's
        // elevation overlay a no-op instead of muddying every raised surface.
        surfaceTint = palette.background,

        outline = palette.border,
        outlineVariant = palette.border,

        error = accents.red,
        onError = palette.contentColorOn(accents.red),
        errorContainer = accents.red,
        onErrorContainer = palette.contentColorOn(accents.red),

        inverseSurface = palette.foreground,
        inverseOnSurface = palette.background,
        scrim = Color.Black,
    )
}

/** WCAG relative contrast between two opaque colours. */
private fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

/**
 * Walks [this] toward white (on a dark page) or black (on a light one) until
 * it clears 4.5:1 against [background], so a fill colour can safely be used as
 * text. Hue is preserved, so the result still reads as the theme's accent.
 */
internal fun Color.readableOn(background: Color, isDark: Boolean, minimum: Float = 4.5f): Color {
    val target = if (isDark) Color.White else Color.Black
    var step = 0f
    var candidate = this
    while (step < 1f && contrastRatio(candidate, background) < minimum) {
        step += 0.05f
        candidate = lerp(this, target, step)
    }
    return candidate
}

/**
 * Secondary text, quietened so it sits under the primary text instead of
 * beside it.
 *
 * On the dark themes nuclear's foreground-secondary is a saturated rose that
 * measures 8.3:1 against the page - barely below the 12.9:1 of the body text.
 * nuclear can afford that because it uses the colour on a handful of labels;
 * Metrolist puts it on every subtitle and every settings value, and the screen
 * turns uniformly pink. Capping it near 42% of the body text's contrast keeps
 * the rose but restores the hierarchy. The light themes already sit under that
 * ceiling, so they come through unchanged.
 */
internal fun NuclearPalette.mutedText(): Color {
    val ceiling = maxOf(4.5f, contrastRatio(foreground, background) * 0.42f)
    var step = 0f
    var candidate = foregroundSecondary
    while (step < 1f && contrastRatio(candidate, background) > ceiling) {
        step += 0.03f
        candidate = lerp(foregroundSecondary, background, step)
    }
    return candidate
}

/** Black in a light palette, the palette's own off-white in a dark one. */
private val NuclearPalette.paper: Color
    get() = if (isDark) foreground else Color.White

/**
 * Picks whichever of ink/paper reads better on [background], by WCAG contrast
 * ratio rather than a luminance threshold. This lands on nuclear's own choices
 * (black on the pink primary, off-white on the dark rose one) while staying
 * correct for arbitrary album-art accents.
 */
internal fun NuclearPalette.contentColorOn(background: Color): Color {
    val bg = background.luminance() + 0.05f
    val inkContrast = bg / 0.05f
    val paperContrast = (paper.luminance() + 0.05f) / bg
    return if (inkContrast >= paperContrast) Color.Black else paper
}
