/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.metrolist.music.R
import com.metrolist.music.ui.screens.settings.DarkMode
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.ui.theme.nuclear.NuclearButton
import com.metrolist.music.ui.theme.nuclear.NuclearButtonVariant
import com.metrolist.music.utils.rememberEnumPreference
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * Where a chosen playlist cover is kept.
 *
 * Deliberately `filesDir` and not `cacheDir`: a cover is something the user
 * picked and expects to stay. Android is free to empty the cache whenever it
 * wants space, and a playlist whose artwork silently reverted to a grey note
 * would look like data loss.
 */
object PlaylistCover {
    private const val DIRECTORY = "playlist_covers"

    private fun directory(context: Context): File =
        File(context.filesDir, DIRECTORY).apply { mkdirs() }

    private fun authority(context: Context) = "${context.packageName}.FileProvider"

    /**
     * Whether this artwork is one the user chose, rather than a song's cover.
     *
     * The authority is read from the running package. Hardcoding it - as this
     * check used to - silently returns false for any fork, which leaves the
     * user unable to change or remove a cover they had just set.
     */
    fun isCustom(
        context: Context,
        url: String?,
    ): Boolean {
        val value = url ?: return false
        return value.contains("studio_square_thumbnail") ||
            value.contains(authority(context)) ||
            value.contains("/$DIRECTORY/")
    }

    /** Copies a cropped image into durable storage and returns a shareable uri for it. */
    fun persist(
        context: Context,
        source: Uri,
    ): Uri? =
        runCatching {
            val destination = File(directory(context), "cover_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            FileProvider.getUriForFile(context, authority(context), destination)
        }.getOrNull()

    /** Removes a cover this app owns. Anything else - a song's artwork - is left alone. */
    fun delete(
        context: Context,
        url: String?,
    ) {
        val value = url ?: return
        if (!value.contains("/$DIRECTORY/") && !value.contains(authority(context))) return
        runCatching {
            val name = value.substringAfterLast('/').substringBefore('?')
            File(directory(context), name).takeIf { it.exists() }?.delete()
        }
    }
}

/** Launches the pickers and hands back a square, durable cover. */
@Stable
class PlaylistCoverPicker internal constructor(
    private val onGallery: () -> Unit,
    private val onCamera: () -> Unit,
) {
    fun pickFromGallery() = onGallery()

    fun takePhoto() = onCamera()
}

/**
 * Gallery or camera, then a square crop, then a copy into durable storage.
 *
 * Both sources land in the same crop step, so a photo and a picked image are
 * indistinguishable by the time [onPicked] sees them - which is what lets the
 * create dialog and the playlist screen share one implementation instead of
 * growing two that drift apart.
 */
@Composable
fun rememberPlaylistCoverPicker(onPicked: (Uri) -> Unit): PlaylistCoverPicker {
    val context = LocalContext.current
    val currentOnPicked by rememberUpdatedState(onPicked)

    val (darkMode, _) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val darkTheme = darkMode == DarkMode.ON || (darkMode == DarkMode.AUTO && isSystemInDarkTheme())
    val scheme = MaterialTheme.colorScheme
    val cropTitle = stringResource(R.string.edit_playlist_cover)

    var pendingCropDestination by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraOutput by remember { mutableStateOf<Uri?>(null) }

    val cropLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val cropped =
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    result.data?.let { UCrop.getOutput(it) } ?: pendingCropDestination
                } else {
                    null
                }
            // The camera shot and the crop output are both scratch files; only
            // the persisted copy outlives this call.
            pendingCameraOutput?.let { runCatching { context.contentResolver.delete(it, null, null) } }
            pendingCameraOutput = null
            if (cropped != null) {
                PlaylistCover.persist(context, cropped)?.let { currentOnPicked(it) }
            }
        }

    val startCrop: (Uri) -> Unit = { source ->
        val destinationFile = File(context.cacheDir, "playlist_cover_crop_${System.currentTimeMillis()}.jpg")
        val destination = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", destinationFile)
        pendingCropDestination = destination

        val options =
            UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(90)
                setHideBottomControls(true)
                setToolbarTitle(cropTitle)
                setStatusBarLight(!darkTheme)
                setToolbarColor(scheme.surface.toArgb())
                setToolbarWidgetColor(scheme.inverseSurface.toArgb())
                setRootViewBackgroundColor(scheme.surface.toArgb())
                setLogoColor(scheme.surface.toArgb())
            }

        val intent =
            UCrop
                .of(source, destination)
                .withAspectRatio(1f, 1f)
                .withOptions(options)
                .getIntent(context)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        cropLauncher.launch(intent)
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let(startCrop)
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val output = pendingCameraOutput
            if (saved && output != null) {
                startCrop(output)
            } else {
                pendingCameraOutput = null
            }
        }

    return remember(context) {
        PlaylistCoverPicker(
            onGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onCamera = {
                val photoFile = File(context.cacheDir, "playlist_cover_shot_${System.currentTimeMillis()}.jpg")
                val photoUri =
                    FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", photoFile)
                pendingCameraOutput = photoUri
                cameraLauncher.launch(photoUri)
            },
        )
    }
}

/**
 * Where a cover comes from, asked as a dialog rather than a bottom sheet.
 *
 * The sheet it replaces was a stack of bare Material list rows - no outline, no
 * shadow, nothing pressable - which is the one shape the rest of the app does
 * not have. Each choice is a push button here, and removing is the destructive
 * variant, so the row that throws something away looks like it.
 */
@Composable
fun PlaylistCoverDialog(
    onDismiss: () -> Unit,
    onChooseFromLibrary: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    DefaultDialog(
        onDismiss = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.insert_photo),
                contentDescription = null,
            )
        },
        title = { Text(stringResource(R.string.playlist_cover)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NuclearButton(
                onClick = {
                    onDismiss()
                    onChooseFromLibrary()
                },
                variant = NuclearButtonVariant.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.insert_photo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(stringResource(R.string.choose_from_library))
            }

            NuclearButton(
                onClick = {
                    onDismiss()
                    onTakePhoto()
                },
                variant = NuclearButtonVariant.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.photo_camera),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(stringResource(R.string.take_a_photo))
            }

            if (onRemove != null) {
                NuclearButton(
                    onClick = {
                        onDismiss()
                        onRemove()
                    },
                    variant = NuclearButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(stringResource(R.string.remove_custom_image))
                }
            }
        }
    }
}
