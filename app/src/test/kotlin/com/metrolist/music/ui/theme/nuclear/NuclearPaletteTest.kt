package com.metrolist.music.ui.theme.nuclear

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrast guarantees for the ported nuclear palettes.
 *
 * These exist because the reskin shipped two contrast bugs that only turned up
 * on a phone: `primary` used as text measured 1.51:1 on the light theme, and
 * the dark themes' secondary text sat at 8.3:1 against body text's 12.9:1, so
 * every screen went uniformly pink. Both are properties of pure functions and
 * are cheap to assert, so a regression should fail here rather than on a
 * screen.
 */
class NuclearPaletteTest {

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance() + 0.05f
        val lb = b.luminance() + 0.05f
        return if (la > lb) la / lb else lb / la
    }

    private val palettes: List<Pair<String, NuclearPalette>>
        get() = NuclearThemeId.entries.flatMap { theme ->
            listOf(
                "${theme.name} light" to theme.palette(dark = false),
                "${theme.name} dark" to theme.palette(dark = true),
            )
        }

    @Test
    fun `body text clears AA large on every preset`() {
        palettes.forEach { (name, p) ->
            val ratio = contrast(p.foreground, p.background)
            assertTrue("$name: body text is $ratio:1 on the page", ratio >= 4.5f)
        }
    }

    @Test
    fun `the accent derived from primary is readable as text`() {
        palettes.forEach { (name, p) ->
            val ink = p.primary.readableOn(p.background, p.isDark)
            val ratio = contrast(ink, p.background)
            assertTrue("$name: derived ink is $ratio:1 on the page", ratio >= 4.4f)
        }
    }

    @Test
    fun `secondary text stays readable but subordinate to body text`() {
        palettes.forEach { (name, p) ->
            val muted = p.mutedText()
            val mutedRatio = contrast(muted, p.background)
            val bodyRatio = contrast(p.foreground, p.background)
            assertTrue("$name: secondary text is $mutedRatio:1", mutedRatio >= 4.4f)
            assertTrue(
                "$name: secondary text ($mutedRatio:1) should sit under body text ($bodyRatio:1)",
                mutedRatio < bodyRatio,
            )
        }
    }

    @Test
    fun `content colour on a fill is readable on that fill`() {
        palettes.forEach { (name, p) ->
            listOf(
                "primary" to p.primary,
                "card" to p.backgroundSecondary,
                "accent red" to p.accents.red,
                "accent green" to p.accents.green,
                "accent purple" to p.accents.purple,
            ).forEach { (label, fill) ->
                val ratio = contrast(p.contentColorOn(fill), fill)
                assertTrue("$name: content on $label is $ratio:1", ratio >= 4.5f)
            }
        }
    }

    @Test
    fun `the hard shadow is visible against the page it sits on`() {
        palettes.forEach { (name, p) ->
            val ratio = contrast(p.shadow, p.background)
            assertTrue("$name: shadow is only $ratio:1 against the page", ratio >= 1.5f)
        }
    }

    @Test
    fun `pure black does not swallow the shadow`() {
        // Theme.kt rewrites background and backgroundInput to black for the
        // pure-black option; the shadow has to fall back to the border there.
        palettes.filter { it.second.isDark }.forEach { (name, p) ->
            val pureBlack = p.copy(background = Color.Black, backgroundInput = Color.Black)
            assertNotEquals("$name: shadow is black on black", Color.Black, pureBlack.shadow)
            val ratio = contrast(pureBlack.shadow, pureBlack.background)
            assertTrue("$name: pure-black shadow is only $ratio:1", ratio >= 1.5f)
        }
    }

    @Test
    fun `readableOn terminates even when no blend can reach the target`() {
        // The loop walks toward white or black in fixed steps; a colour already
        // equal to the background must still return rather than spin.
        val page = Color(0xFF808080)
        val result = page.readableOn(page, isDark = false)
        assertTrue("readableOn returned a colour", result.alpha > 0f)
    }
}
