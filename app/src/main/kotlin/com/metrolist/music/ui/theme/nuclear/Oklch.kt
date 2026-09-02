/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * OKLCH, the colour space nuclear authors its themes in.
 *
 * Its presets are one structure at five hues: every slot sits at a fixed
 * lightness and chroma and only the hue moves. Working in the same space is
 * what lets a user-picked colour generate a palette that looks like it belongs
 * with the built-in five, rather than a set of arbitrary tints.
 *
 * @param lightness perceptual lightness, 0..1
 * @param chroma roughly 0..0.4 for displayable colours
 * @param hue degrees, 0..360
 */
@Immutable
data class Oklch(
    val lightness: Float,
    val chroma: Float,
    val hue: Float,
) {
    fun toColor(): Color {
        val radians = hue * (Math.PI.toFloat() / 180f)
        val a = chroma * cos(radians)
        val b = chroma * sin(radians)

        val l = (lightness + 0.3963377774f * a + 0.2158037573f * b).let { it * it * it }
        val m = (lightness - 0.1055613458f * a - 0.0638541728f * b).let { it * it * it }
        val s = (lightness - 0.0894841775f * a - 1.2914855480f * b).let { it * it * it }

        return Color(
            red = gamma(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s),
            green = gamma(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s),
            blue = gamma(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s),
        )
    }

    private fun gamma(linear: Float): Float {
        val v = if (linear <= 0.0031308f) {
            12.92f * linear
        } else {
            1.055f * linear.coerceAtLeast(0f).pow(1f / 2.4f) - 0.055f
        }
        return v.coerceIn(0f, 1f)
    }
}

fun Color.toOklch(): Oklch {
    val r = inverseGamma(red)
    val g = inverseGamma(green)
    val b = inverseGamma(blue)

    val l = cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b)
    val m = cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b)
    val s = cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b)

    val lightness = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s
    val a = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s
    val bb = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s

    val hue = atan2(bb, a) * (180f / Math.PI.toFloat())
    return Oklch(
        lightness = lightness,
        chroma = sqrt(a * a + bb * bb),
        hue = if (hue < 0f) hue + 360f else hue,
    )
}

private fun inverseGamma(channel: Float): Float =
    if (channel <= 0.04045f) channel / 12.92f else ((channel + 0.055f) / 1.055f).pow(2.4f)

/** `#RRGGBB`, the form nuclear's theme files and every colour picker use. */
fun Color.toHex(): String {
    fun channel(value: Float) = (value * 255f).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${channel(red)}${channel(green)}${channel(blue)}".uppercase()
}

/** Parses `#RRGGBB` or `#AARRGGBB`, returning null rather than throwing on junk. */
fun String.toColorOrNull(): Color? {
    val hex = trim().removePrefix("#")
    if (hex.length != 6 && hex.length != 8) return null
    val value = hex.toLongOrNull(16) ?: return null
    return if (hex.length == 6) Color(value or 0xFF000000L) else Color(value)
}
