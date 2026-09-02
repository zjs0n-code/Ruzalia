/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** How the palette in use is being produced. */
enum class NuclearThemeMode {
    /** One of the five ports of nuclear's own presets. */
    PRESET,

    /** Every token derived from a single colour the user picked. */
    SEED,

    /** Every token set by hand in the advanced editor. */
    CUSTOM,
}

/**
 * The on-disk and shareable form of a theme.
 *
 * Deliberately shaped like nuclear's own advanced theme files - a version, a
 * name, and `vars`/`dark` maps of token name to colour, using nuclear's
 * kebab-case token names - so a Ruzalia theme reads as the same kind of object
 * as a nuclear one and can be hand-edited in a text editor.
 */
@Serializable
data class NuclearThemeFile(
    val version: Int = 1,
    val name: String = "Custom",
    val author: String = "",
    val vars: Map<String, String> = emptyMap(),
    val dark: Map<String, String> = emptyMap(),
) {
    fun palette(isDark: Boolean): NuclearPalette {
        val map = if (isDark) dark else vars
        val fallback = if (isDark) {
            NuclearThemeId.Default.palette(dark = true)
        } else {
            NuclearThemeId.Default.palette(dark = false)
        }
        fun read(key: String, default: Color) = map[key]?.toColorOrNull() ?: default
        return fallback.copy(
            background = read(TOKEN_BACKGROUND, fallback.background),
            backgroundSecondary = read(TOKEN_BACKGROUND_SECONDARY, fallback.backgroundSecondary),
            backgroundInput = read(TOKEN_BACKGROUND_INPUT, fallback.backgroundInput),
            foreground = read(TOKEN_FOREGROUND, fallback.foreground),
            foregroundSecondary = read(TOKEN_FOREGROUND_SECONDARY, fallback.foregroundSecondary),
            primary = read(TOKEN_PRIMARY, fallback.primary),
            border = read(TOKEN_BORDER, fallback.border),
            accents = NuclearAccents(
                green = read(TOKEN_GREEN, fallback.accents.green),
                yellow = read(TOKEN_YELLOW, fallback.accents.yellow),
                purple = read(TOKEN_PURPLE, fallback.accents.purple),
                blue = read(TOKEN_BLUE, fallback.accents.blue),
                orange = read(TOKEN_ORANGE, fallback.accents.orange),
                cyan = read(TOKEN_CYAN, fallback.accents.cyan),
                red = read(TOKEN_RED, fallback.accents.red),
            ),
            isDark = isDark,
        )
    }

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        const val TOKEN_BACKGROUND = "background"
        const val TOKEN_BACKGROUND_SECONDARY = "background-secondary"
        const val TOKEN_BACKGROUND_INPUT = "background-input"
        const val TOKEN_FOREGROUND = "foreground"
        const val TOKEN_FOREGROUND_SECONDARY = "foreground-secondary"
        const val TOKEN_PRIMARY = "primary"
        const val TOKEN_BORDER = "border"
        const val TOKEN_GREEN = "accent-green"
        const val TOKEN_YELLOW = "accent-yellow"
        const val TOKEN_PURPLE = "accent-purple"
        const val TOKEN_BLUE = "accent-blue"
        const val TOKEN_ORANGE = "accent-orange"
        const val TOKEN_CYAN = "accent-cyan"
        const val TOKEN_RED = "accent-red"

        /** Editor order: page and text first, then the semantic accents. */
        val TOKENS = listOf(
            TOKEN_BACKGROUND,
            TOKEN_BACKGROUND_SECONDARY,
            TOKEN_BACKGROUND_INPUT,
            TOKEN_FOREGROUND,
            TOKEN_FOREGROUND_SECONDARY,
            TOKEN_PRIMARY,
            TOKEN_BORDER,
            TOKEN_GREEN,
            TOKEN_YELLOW,
            TOKEN_PURPLE,
            TOKEN_BLUE,
            TOKEN_ORANGE,
            TOKEN_CYAN,
            TOKEN_RED,
        )

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun from(name: String, light: NuclearPalette, dark: NuclearPalette) = NuclearThemeFile(
            name = name,
            vars = light.toMap(),
            dark = dark.toMap(),
        )

        /** Returns null rather than throwing: this parses text a user pasted in. */
        fun decodeOrNull(text: String): NuclearThemeFile? = runCatching {
            json.decodeFromString(serializer(), text)
        }.getOrNull()?.takeIf { it.vars.isNotEmpty() || it.dark.isNotEmpty() }
    }
}

fun NuclearPalette.toMap(): Map<String, String> = mapOf(
    NuclearThemeFile.TOKEN_BACKGROUND to background.toHex(),
    NuclearThemeFile.TOKEN_BACKGROUND_SECONDARY to backgroundSecondary.toHex(),
    NuclearThemeFile.TOKEN_BACKGROUND_INPUT to backgroundInput.toHex(),
    NuclearThemeFile.TOKEN_FOREGROUND to foreground.toHex(),
    NuclearThemeFile.TOKEN_FOREGROUND_SECONDARY to foregroundSecondary.toHex(),
    NuclearThemeFile.TOKEN_PRIMARY to primary.toHex(),
    NuclearThemeFile.TOKEN_BORDER to border.toHex(),
    NuclearThemeFile.TOKEN_GREEN to accents.green.toHex(),
    NuclearThemeFile.TOKEN_YELLOW to accents.yellow.toHex(),
    NuclearThemeFile.TOKEN_PURPLE to accents.purple.toHex(),
    NuclearThemeFile.TOKEN_BLUE to accents.blue.toHex(),
    NuclearThemeFile.TOKEN_ORANGE to accents.orange.toHex(),
    NuclearThemeFile.TOKEN_CYAN to accents.cyan.toHex(),
    NuclearThemeFile.TOKEN_RED to accents.red.toHex(),
)

/** Reads one token out of a palette, for the advanced editor's rows. */
fun NuclearPalette.token(name: String): Color = when (name) {
    NuclearThemeFile.TOKEN_BACKGROUND -> background
    NuclearThemeFile.TOKEN_BACKGROUND_SECONDARY -> backgroundSecondary
    NuclearThemeFile.TOKEN_BACKGROUND_INPUT -> backgroundInput
    NuclearThemeFile.TOKEN_FOREGROUND -> foreground
    NuclearThemeFile.TOKEN_FOREGROUND_SECONDARY -> foregroundSecondary
    NuclearThemeFile.TOKEN_PRIMARY -> primary
    NuclearThemeFile.TOKEN_BORDER -> border
    NuclearThemeFile.TOKEN_GREEN -> accents.green
    NuclearThemeFile.TOKEN_YELLOW -> accents.yellow
    NuclearThemeFile.TOKEN_PURPLE -> accents.purple
    NuclearThemeFile.TOKEN_BLUE -> accents.blue
    NuclearThemeFile.TOKEN_ORANGE -> accents.orange
    NuclearThemeFile.TOKEN_CYAN -> accents.cyan
    else -> accents.red
}

/** Returns a copy with one token replaced. */
fun NuclearPalette.withToken(name: String, color: Color): NuclearPalette = when (name) {
    NuclearThemeFile.TOKEN_BACKGROUND -> copy(background = color)
    NuclearThemeFile.TOKEN_BACKGROUND_SECONDARY -> copy(backgroundSecondary = color)
    NuclearThemeFile.TOKEN_BACKGROUND_INPUT -> copy(backgroundInput = color)
    NuclearThemeFile.TOKEN_FOREGROUND -> copy(foreground = color)
    NuclearThemeFile.TOKEN_FOREGROUND_SECONDARY -> copy(foregroundSecondary = color)
    NuclearThemeFile.TOKEN_PRIMARY -> copy(primary = color)
    NuclearThemeFile.TOKEN_BORDER -> copy(border = color)
    NuclearThemeFile.TOKEN_GREEN -> copy(accents = accents.copy(green = color))
    NuclearThemeFile.TOKEN_YELLOW -> copy(accents = accents.copy(yellow = color))
    NuclearThemeFile.TOKEN_PURPLE -> copy(accents = accents.copy(purple = color))
    NuclearThemeFile.TOKEN_BLUE -> copy(accents = accents.copy(blue = color))
    NuclearThemeFile.TOKEN_ORANGE -> copy(accents = accents.copy(orange = color))
    NuclearThemeFile.TOKEN_CYAN -> copy(accents = accents.copy(cyan = color))
    else -> copy(accents = accents.copy(red = color))
}
