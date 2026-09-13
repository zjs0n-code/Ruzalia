/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context

/** Where every user-chosen cover is written - playlists, songs and albums alike. */
internal const val CUSTOM_ARTWORK_DIRECTORY = "playlist_covers"

/**
 * Whether a thumbnail is one the user chose.
 *
 * Deliberately context-free so the database layer can ask it. Refreshing from
 * YouTube rewrites song and album thumbnails wholesale - on every download, and
 * every time an album screen opens - so without this check a cover someone set
 * would be silently replaced the next time the song played or the album opened.
 */
fun String?.isCustomArtwork(): Boolean = this?.contains("/$CUSTOM_ARTWORK_DIRECTORY/") == true

/**
 * The thumbnail to keep after a refresh from YouTube: a cover the user chose
 * beats whatever came back. Kept as a pure function so the rule is tested
 * directly rather than inferred from a network call that may not have happened.
 */
fun keepCustomArtwork(current: String?, incoming: String?): String? =
    if (current.isCustomArtwork()) current else incoming

/**
 * The artwork a custom cover replaced, so removing the cover can put it back.
 *
 * Kept in preferences rather than the database on purpose: it is only ever read
 * when undoing, and a schema migration for that would be the heavier change.
 * Only the first replacement is recorded - swapping one custom cover for another
 * must still restore the real artwork, not the previous custom one.
 */
object CustomArtworkOriginals {
    private const val PREFERENCES = "custom_artwork_originals"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun rememberSong(context: Context, songId: String, original: String?) =
        remember(context, "song:$songId", original)

    fun takeSong(context: Context, songId: String): String? = take(context, "song:$songId")

    fun rememberAlbum(context: Context, albumId: String, original: String?) =
        remember(context, "album:$albumId", original)

    fun takeAlbum(context: Context, albumId: String): String? = take(context, "album:$albumId")

    private fun remember(context: Context, key: String, original: String?) {
        if (original == null || original.isCustomArtwork()) return
        val preferences = preferences(context)
        if (preferences.contains(key)) return
        preferences.edit().putString(key, original).apply()
    }

    private fun take(context: Context, key: String): String? {
        val preferences = preferences(context)
        val original = preferences.getString(key, null)
        preferences.edit().remove(key).apply()
        return original
    }
}
