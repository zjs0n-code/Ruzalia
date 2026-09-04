/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.component.shimmer.ListItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost
import com.metrolist.music.ui.menu.YouTubeAlbumMenu
import com.metrolist.music.ui.menu.YouTubeArtistMenu
import com.metrolist.music.ui.menu.YouTubePlaylistMenu
import com.metrolist.music.ui.menu.YouTubeSongMenu
import com.metrolist.music.ui.theme.nuclear.NuclearButtonVariant
import com.metrolist.music.ui.theme.nuclear.NuclearIconButton
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.viewmodels.YouTubeBrowseViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun YouTubeBrowseScreen(
    navController: NavController,
    viewModel: YouTubeBrowseViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val browseResult by viewModel.result.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    // A mood or genre page is a list of songs and playlists, which is what
    // every other list in the app is - so it is laid out the way they are, in
    // rows, rather than in a grid of cards that nothing else here uses. The
    // page also arrives already divided into sections, which the old flat grid
    // threw away.
    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
        if (browseResult == null) {
            item(key = "browse_shimmer") {
                ShimmerHost {
                    repeat(8) {
                        ListItemPlaceHolder()
                    }
                }
            }
        }

        browseResult?.items?.forEachIndexed { sectionIndex, section ->
            section.title?.let { sectionTitle ->
                item(key = "browse_section_$sectionIndex") {
                    NavigationTitle(title = sectionTitle)
                }
            }

            items(
                items = section.items.distinctBy { it.id },
                // The same song can head more than one section on a genre page,
                // so the section has to be part of the key.
                key = { "yt_browse_" + sectionIndex + "_" + it.id },
            ) { item ->
                val openMenu: () -> Unit = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        when (item) {
                            is SongItem -> YouTubeSongMenu(
                                song = item,
                                onDismiss = menuState::dismiss,
                            )

                            is AlbumItem -> YouTubeAlbumMenu(
                                albumItem = item,
                                onDismiss = menuState::dismiss,
                            )

                            is ArtistItem -> YouTubeArtistMenu(
                                artist = item,
                                onDismiss = menuState::dismiss,
                            )

                            is PlaylistItem -> YouTubePlaylistMenu(
                                playlist = item,
                                coroutineScope = coroutineScope,
                                onDismiss = menuState::dismiss,
                            )

                            is PodcastItem -> YouTubePlaylistMenu(
                                playlist = item.asPlaylistItem(),
                                coroutineScope = coroutineScope,
                                onDismiss = menuState::dismiss,
                            )

                            is EpisodeItem -> YouTubeSongMenu(
                                song = item.asSongItem(),
                                onDismiss = menuState::dismiss,
                            )
                        }
                    }
                }

                YouTubeListItem(
                    item = item,
                    isActive =
                        when (item) {
                            is SongItem -> mediaMetadata?.id == item.id
                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                            is EpisodeItem -> mediaMetadata?.id == item.id
                            else -> false
                        },
                    isPlaying = isPlaying,
                    trailingContent = {
                        NuclearIconButton(
                            onClick = openMenu,
                            variant = NuclearButtonVariant.Tertiary,
                            size = 40.dp,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .combinedClickable(
                                onClick = {
                                    when (item) {
                                        is SongItem -> {
                                            if (item.id == mediaMetadata?.id) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue.radio(item.toMediaMetadata()),
                                                )
                                            }
                                        }

                                        is AlbumItem -> navController.navigate("album/" + item.id)

                                        is ArtistItem -> navController.navigate("artist/" + item.id)

                                        is PlaylistItem -> navController.navigate("online_playlist/" + item.id)

                                        is PodcastItem -> navController.navigate("online_podcast/" + item.id)

                                        is EpisodeItem -> {
                                            if (item.id == mediaMetadata?.id) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue.radio(item.toMediaMetadata()),
                                                )
                                            }
                                        }
                                    }
                                },
                                onLongClick = openMenu,
                            ).animateItem(),
                )
            }
        }
    }

    TopAppBar(
        title = { Text(browseResult?.title.orEmpty()) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}
