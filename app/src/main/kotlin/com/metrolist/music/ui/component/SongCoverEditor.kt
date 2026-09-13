/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.utils.CustomArtworkOriginals
import com.metrolist.music.utils.isCustomArtwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Changing a song's cover, shared by every menu that offers it - the song menu
 * in lists and the player's own menu - so the two cannot drift apart.
 *
 * Call it from inside the menu and invoke what it returns to open the chooser.
 * It hosts the dialog itself, and it has to live inside the menu rather than
 * beside it: the gallery and crop screens report back to launchers registered
 * here, and those only stay registered while the menu is still showing.
 */
@Composable
fun rememberSongCoverEditor(songId: String): () -> Unit {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current
    val scope = rememberCoroutineScope()

    val song by database.song(songId).collectAsStateWithLifecycle(initialValue = null)
    var open by rememberSaveable(songId) { mutableStateOf(false) }

    // Whatever changed, the player has to be told, or the artwork on screen
    // stays the old one until the song is queued again.
    val refreshPlayer: suspend () -> Unit = {
        database.song(songId).first()?.let { refreshed ->
            playerConnection?.refreshSongMetadata(refreshed)
        }
    }

    val cropTitle = stringResource(R.string.edit_song_cover)
    val picker = rememberPlaylistCoverPicker(title = cropTitle) { uri ->
        scope.launch {
            val current = database.song(songId).first() ?: return@launch
            val previous = current.song.thumbnailUrl
            withContext(Dispatchers.IO) {
                CustomArtworkOriginals.rememberSong(context, current.id, previous)
                database.update(current.song.copy(thumbnailUrl = uri.toString()))
                if (previous.isCustomArtwork()) PlaylistCover.delete(context, previous)
            }
            refreshPlayer()
        }
    }

    if (open) {
        PlaylistCoverDialog(
            title = stringResource(R.string.song_cover),
            onDismiss = { open = false },
            onChooseFromLibrary = picker::pickFromGallery,
            onTakePhoto = picker::takePhoto,
            onRemove =
                if (song?.song?.thumbnailUrl.isCustomArtwork()) {
                    {
                        scope.launch {
                            val current = database.song(songId).first() ?: return@launch
                            val previous = current.song.thumbnailUrl
                            withContext(Dispatchers.IO) {
                                // Songs are YouTube videos, so if the original was
                                // never recorded its standard thumbnail stands in.
                                val original =
                                    CustomArtworkOriginals.takeSong(context, current.id)
                                        ?: "https://i.ytimg.com/vi/${current.id}/hqdefault.jpg"
                                database.update(current.song.copy(thumbnailUrl = original))
                                PlaylistCover.delete(context, previous)
                            }
                            refreshPlayer()
                        }
                    }
                } else {
                    null
                },
        )
    }

    return remember(songId) { { open = true } }
}
