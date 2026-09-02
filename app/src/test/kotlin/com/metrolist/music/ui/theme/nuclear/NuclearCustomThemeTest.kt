package com.metrolist.music.ui.theme.nuclear

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guarantees for the custom and advanced themes.
 *
 * A user can pick any colour at all, including black, white and fully grey, so
 * the generated palette has to stay readable across the whole spectrum rather
 * than only for the hues the built-in presets happen to use.
 */
class NuclearCustomThemeTest {

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance() + 0.05f
        val lb = b.luminance() + 0.05f
        return if (la > lb) la / lb else lb / la
    }

    private val spectrum: List<Color>
        get() = (0 until 360 step 15).map { Color.hsv(it.toFloat(), 0.8f, 0.75f) }

    @Test
    fun `a seeded palette keeps body text readable at every hue`() {
        spectrum.forEach { seed ->
            listOf(false, true).forEach { dark ->
                val p = seedPalette(seed, dark)
                val ratio = contrast(p.foreground, p.background)
                assertTrue(
                    "hue ${seed.toHex()} dark=$dark: body text is $ratio:1",
                    ratio >= 4.5f,
                )
            }
        }
    }

    @Test
    fun `a seeded palette keeps its derived accent and secondary text readable`() {
        spectrum.forEach { seed ->
            listOf(false, true).forEach { dark ->
                val p = seedPalette(seed, dark)
                val ink = contrast(p.primary.readableOn(p.background, dark), p.background)
                assertTrue("hue ${seed.toHex()} dark=$dark: ink is $ink:1", ink >= 4.4f)

                val muted = contrast(p.mutedText(), p.background)
                assertTrue("hue ${seed.toHex()} dark=$dark: muted is $muted:1", muted >= 4.4f)
            }
        }
    }

    @Test
    fun `extreme picks still produce a usable palette`() {
        // Black, white and a fully desaturated grey carry no hue and almost no
        // chroma; the palette must not collapse to a single flat colour.
        listOf(Color.Black, Color.White, Color(0xFF808080)).forEach { seed ->
            listOf(false, true).forEach { dark ->
                val p = seedPalette(seed, dark)
                assertTrue(
                    "seed ${seed.toHex()} dark=$dark: page and card are identical",
                    p.background != p.backgroundSecondary,
                )
                assertTrue(
                    "seed ${seed.toHex()} dark=$dark: text is unreadable",
                    contrast(p.foreground, p.background) >= 4.5f,
                )
            }
        }
    }

    @Test
    fun `a theme survives a round trip through its file format`() {
        val light = seedPalette(Color(0xFF3366CC), dark = false)
        val dark = seedPalette(Color(0xFF3366CC), dark = true)
        val encoded = NuclearThemeFile.from("Round trip", light, dark).encode()

        val decoded = NuclearThemeFile.decodeOrNull(encoded)
        assertNotNull("encoded theme failed to parse", decoded)
        assertEquals("Round trip", decoded!!.name)

        // Hex is 8 bits per channel, so compare in that form rather than on the
        // float values, which carry more precision than the file does.
        assertEquals(light.toMap(), decoded.palette(false).toMap())
        assertEquals(dark.toMap(), decoded.palette(true).toMap())
    }

    @Test
    fun `importing junk fails rather than throwing`() {
        listOf("", "not json", "{}", "[1,2,3]", "{\"version\":1}").forEach { text ->
            assertNull("accepted junk: $text", NuclearThemeFile.decodeOrNull(text))
        }
    }

    @Test
    fun `an imported theme falls back per token rather than failing wholesale`() {
        // Hand-edited files will be partial; missing tokens should come from the
        // default preset instead of rejecting the file.
        val partial = """{"version":1,"name":"Partial","vars":{"primary":"#123456"}}"""
        val decoded = NuclearThemeFile.decodeOrNull(partial)
        assertNotNull(decoded)
        val palette = decoded!!.palette(false)
        assertEquals("#123456", palette.primary.toHex())
        assertEquals(
            NuclearThemeId.Default.palette(false).background.toHex(),
            palette.background.toHex(),
        )
    }

    @Test
    fun `hex parsing round trips and rejects nonsense`() {
        assertEquals(Color(0xFFFF9CB4).toHex(), "#FF9CB4".toColorOrNull()?.toHex())
        assertNull("#GGGGGG".toColorOrNull())
        assertNull("#FFF".toColorOrNull())
        assertNull("".toColorOrNull())
    }

    @Test
    fun `oklch survives a conversion round trip`() {
        spectrum.forEach { color ->
            val back = color.toOklch().toColor()
            listOf(color.red to back.red, color.green to back.green, color.blue to back.blue)
                .forEach { (a, b) ->
                    assertTrue(
                        "round trip drifted for ${color.toHex()} -> ${back.toHex()}",
                        kotlin.math.abs(a - b) < 0.02f,
                    )
                }
        }
    }
}
