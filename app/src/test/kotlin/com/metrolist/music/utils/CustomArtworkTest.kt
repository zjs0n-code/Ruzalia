package com.metrolist.music.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomArtworkTest {
    private val custom = "content://com.ruzalia.music.FileProvider/playlist_covers/cover_1.jpg"
    private val youtube = "https://yt3.googleusercontent.com/abc=w1080-h1080-p-l90-rj"
    private val refreshed = "https://yt3.googleusercontent.com/def=w1080-h1080-p-l90-rj"

    @Test
    fun `a chosen cover survives a refresh from YouTube`() {
        assertEquals(custom, keepCustomArtwork(custom, refreshed))
    }

    @Test
    fun `a custom cover is kept even when YouTube returns nothing`() {
        assertEquals(custom, keepCustomArtwork(custom, null))
    }

    @Test
    fun `ordinary artwork still takes the refreshed value`() {
        assertEquals(refreshed, keepCustomArtwork(youtube, refreshed))
    }

    @Test
    fun `missing artwork is filled in by the refresh`() {
        assertEquals(refreshed, keepCustomArtwork(null, refreshed))
        assertNull(keepCustomArtwork(null, null))
    }

    @Test
    fun `only app-owned cover paths count as custom`() {
        assertTrue(custom.isCustomArtwork())
        assertFalse(youtube.isCustomArtwork())
        assertFalse("content://media/external/images/1".isCustomArtwork())
        assertFalse((null as String?).isCustomArtwork())
    }
}
