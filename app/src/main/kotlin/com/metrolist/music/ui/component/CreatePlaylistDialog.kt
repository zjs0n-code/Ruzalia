/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.metrolist.music.ui.theme.nuclear.NuclearArtwork
import com.metrolist.music.ui.theme.nuclear.NuclearButton
import com.metrolist.music.ui.theme.nuclear.NuclearButtonSize
import com.metrolist.music.ui.theme.nuclear.NuclearButtonVariant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.theme.nuclear.Switch
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalSyncUtils
import com.metrolist.music.R
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.extensions.isSyncEnabled
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    initialTextFieldValue: String? = null,
    allowSyncing: Boolean = true,
    onPlaylistCreated: ((String) -> Unit)? = null,
) {
    val syncUtils = LocalSyncUtils.current
    val coroutineScope = rememberCoroutineScope()
    var syncedPlaylist by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isSignedIn = innerTubeCookie.isNotEmpty()

    // Chosen before the playlist exists, so it is carried on the entity rather
    // than written afterwards. A cover picked and then abandoned is deleted on
    // dismiss so it does not sit in storage forever.
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    val coverPicker = rememberPlaylistCoverPicker { uri ->
        coverUri?.let { PlaylistCover.delete(context, it.toString()) }
        coverUri = uri
    }
    val discardUnusedCover = {
        coverUri?.let { PlaylistCover.delete(context, it.toString()) }
        coverUri = null
    }

    val notLoggedInYoutubeStr = stringResource(R.string.not_logged_in_youtube)
    val syncDisabledStr = stringResource(R.string.sync_disabled)
    val playlistCreatedLocallyStr = stringResource(R.string.playlist_created_locally)

    TextFieldDialog(
        icon = { Icon(painter = painterResource(R.drawable.add), contentDescription = null) },
        title = { Text(text = stringResource(R.string.create_playlist)) },
        initialTextFieldValue = TextFieldValue(initialTextFieldValue ?: ""),
        onDismiss = {
            discardUnusedCover()
            onDismiss()
        },
        onDone = { playlistName ->
            syncUtils.createPlaylist(
                playlist = PlaylistEntity(
                    name = playlistName,
                    bookmarkedAt = LocalDateTime.now(),
                    isEditable = true,
                    thumbnailUrl = coverUri?.toString(),
                ),
                syncWithYouTube = syncedPlaylist,
            ) { playlistId, remoteCreated ->
                if (syncedPlaylist && !remoteCreated) {
                    Toast.makeText(context, playlistCreatedLocallyStr, Toast.LENGTH_LONG).show()
                }
                onPlaylistCreated?.invoke(playlistId)
            }
        },
        extraContent = {
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 40.dp, end = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NuclearArtwork(size = 88.dp) {
                    val picked = coverUri
                    if (picked != null) {
                        AsyncImage(
                            model = picked,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.insert_photo),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.playlist_cover),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NuclearButton(
                            onClick = coverPicker::pickFromGallery,
                            variant = NuclearButtonVariant.Tertiary,
                            size = NuclearButtonSize.Small,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.insert_photo),
                                contentDescription = stringResource(R.string.choose_from_library),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        NuclearButton(
                            onClick = coverPicker::takePhoto,
                            variant = NuclearButtonVariant.Tertiary,
                            size = NuclearButtonSize.Small,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.photo_camera),
                                contentDescription = stringResource(R.string.take_a_photo),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            if (allowSyncing) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 40.dp),
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.sync_playlist),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.allows_for_sync_witch_youtube),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(0.7f),
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Switch(
                            checked = syncedPlaylist,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    val isYtmSyncEnabled = withContext(Dispatchers.IO) { context.isSyncEnabled() }
                                    if (!isSignedIn && !syncedPlaylist) {
                                        Toast
                                            .makeText(
                                                context,
                                                notLoggedInYoutubeStr,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    } else if (!isYtmSyncEnabled) {
                                        Toast
                                            .makeText(
                                                context,
                                                syncDisabledStr,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    } else {
                                        syncedPlaylist = !syncedPlaylist
                                    }
                                }
                            },
                        )
                    }
                }
            }
        },
    )
}
