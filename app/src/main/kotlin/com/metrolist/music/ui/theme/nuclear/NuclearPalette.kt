/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Colour values are ports of nuclear's CSS custom properties
 * (packages/tailwind-config/global.css and the presets under
 * packages/themes/src/basic),
 * converted from OKLCH to sRGB. nuclear is (C) nukeop, AGPL-3.0; only the
 * visual design is reproduced here, none of its code.
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.metrolist.music.R

/**
 * nuclear's seven fixed accent hues. They are keyed to light/dark rather than
 * to the selected theme, exactly as in nuclear's `[data-theme='dark']` block.
 */
@Immutable
data class NuclearAccents(
    val green: Color,
    val yellow: Color,
    val purple: Color,
    val blue: Color,
    val orange: Color,
    val cyan: Color,
    val red: Color,
) {
    /** Stable order, used where a series needs distinct colours (stats, wrapped, EQ). */
    val cycle: List<Color> get() = listOf(purple, cyan, yellow, green, orange, blue, red)
}

private val LightAccents = NuclearAccents(
    green = Color(0xFFA0EEA2),
    yellow = Color(0xFFF9D956),
    purple = Color(0xFFCEB6FC),
    blue = Color(0xFFA2CFFF),
    orange = Color(0xFFFFCD9C),
    cyan = Color(0xFF8CEAEF),
    red = Color(0xFFFF4E6C),
)

private val DarkAccents = NuclearAccents(
    green = Color(0xFF67BB6B),
    yellow = Color(0xFFD3B63B),
    purple = Color(0xFFAB8BE3),
    blue = Color(0xFF67AAED),
    orange = Color(0xFFE29D56),
    cyan = Color(0xFF42C3C9),
    red = Color(0xFFE17177),
)

/**
 * One resolved nuclear theme, for one light/dark mode.
 *
 * These are nuclear's own token names rather than Material's, because they do
 * not map one-to-one: [background] is the page, [backgroundSecondary] is any
 * raised card, [backgroundInput] is a recessed field, and [border] is a real
 * drawn outline rather than Material's decorative `outline`.
 */
@Immutable
data class NuclearPalette(
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundInput: Color,
    val foreground: Color,
    val foregroundSecondary: Color,
    val primary: Color,
    val border: Color,
    val accents: NuclearAccents,
    val isDark: Boolean,
) {
    /**
     * Colour of the hard offset shadow.
     *
     * In the light themes the border is black - the darkest thing available -
     * and reads as a shadow. In the dark themes the border is a *lighter* rose
     * than the page, so an offset drawn in it reads as a highlight and the
     * chunk flattens out; there the recessed input colour is darkest.
     *
     * Picking whichever of the two separates further from the page keeps that
     * working when the page is not the palette's own. Under pure black the
     * input colour is also black, and only the border is still visible.
     */
    val shadow: Color
        get() {
            val page = background.luminance()
            val fromInput = kotlin.math.abs(backgroundInput.luminance() - page)
            val fromBorder = kotlin.math.abs(border.luminance() - page)
            return if (fromInput >= fromBorder) backgroundInput else border
        }
}

enum class NuclearThemeId(
    @param:StringRes val nameRes: Int,
) {
    DEFAULT(R.string.nuclear_theme_default),
    AURORA(R.string.nuclear_theme_aurora),
    EMBER(R.string.nuclear_theme_ember),
    LAGOON(R.string.nuclear_theme_lagoon),
    ARCTIC_MOSS(R.string.nuclear_theme_arctic_moss),
    ;

    fun palette(dark: Boolean): NuclearPalette = if (dark) darkPalette() else lightPalette()

    private fun lightPalette(): NuclearPalette = when (this) {
        DEFAULT -> light(0xFFFADADF, 0xFF783246, 0xFFFF9CB4)
        AURORA -> light(0xFFFDF6FB, 0xFF5A3D78, 0xFFC190F6)
        EMBER -> light(0xFFFEF3E7, 0xFF7A342B, 0xFFFD8C7B)
        LAGOON -> light(0xFFEDFEFF, 0xFF005B67, 0xFF00AFC4)
        ARCTIC_MOSS -> light(0xFFEFF7F7, 0xFF005E4A, 0xFF30B69A)
    }

    private fun darkPalette(): NuclearPalette = when (this) {
        DEFAULT -> dark(0xFF261519, 0xFF351F24, 0xFF12080A, 0xFFE3DCDD, 0xFFEE9CAF, 0xFF92495B, 0xFF72545A)
        AURORA -> dark(0xFF1E1726, 0xFF2A2234, 0xFF0D0912, 0xFFDFDDE2, 0xFFC6A7EB, 0xFF9674BB, 0xFF635870)
        EMBER -> dark(0xFF271515, 0xFF361F1F, 0xFF100606, 0xFFE3DCDB, 0xFFF19F91, 0xFFC17468, 0xFF735554)
        LAGOON -> dark(0xFF07191B, 0xFF0E2628, 0xFF02090A, 0xFFD8E0E0, 0xFF5ECAD6, 0xFF008493, 0xFF426468)
        ARCTIC_MOSS -> dark(0xFF0C1915, 0xFF142621, 0xFF040907, 0xFFDADFDE, 0xFF6BCCB4, 0xFF338C77, 0xFF49645C)
    }

    companion object {
        val Default = DEFAULT

        /**
         * In nuclear, light themes only override background, primary and
         * foreground-secondary; everything else falls through to `:root`.
         */
        private fun light(background: Long, foregroundSecondary: Long, primary: Long) = NuclearPalette(
            background = Color(background),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundInput = Color(0xFFFFFFFF),
            foreground = Color(0xFF000000),
            foregroundSecondary = Color(foregroundSecondary),
            primary = Color(primary),
            border = Color(0xFF000000),
            accents = LightAccents,
            isDark = false,
        )

        private fun dark(
            background: Long,
            backgroundSecondary: Long,
            backgroundInput: Long,
            foreground: Long,
            foregroundSecondary: Long,
            primary: Long,
            border: Long,
        ) = NuclearPalette(
            background = Color(background),
            backgroundSecondary = Color(backgroundSecondary),
            backgroundInput = Color(backgroundInput),
            foreground = Color(foreground),
            foregroundSecondary = Color(foregroundSecondary),
            primary = Color(primary),
            border = Color(border),
            accents = DarkAccents,
            isDark = true,
        )
    }
}
