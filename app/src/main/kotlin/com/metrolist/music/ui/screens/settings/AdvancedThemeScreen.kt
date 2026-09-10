/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.ui.theme.nuclear.IconButton
import com.metrolist.music.R
import com.metrolist.music.constants.NuclearCustomThemeKey
import com.metrolist.music.constants.NuclearThemeModeKey
import com.metrolist.music.ui.component.DefaultDialog
import com.metrolist.music.ui.theme.nuclear.NuclearButton
import com.metrolist.music.ui.theme.nuclear.NuclearButtonVariant
import com.metrolist.music.ui.theme.nuclear.NuclearColorPicker
import com.metrolist.music.ui.theme.nuclear.NuclearColorRow
import com.metrolist.music.ui.theme.nuclear.NuclearPalette
import com.metrolist.music.ui.theme.nuclear.NuclearThemeFile
import com.metrolist.music.ui.theme.nuclear.NuclearThemeMode
import com.metrolist.music.ui.theme.nuclear.token
import com.metrolist.music.ui.theme.nuclear.withToken
import com.metrolist.music.ui.theme.rememberNuclearPalette
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedThemeScreen(navController: NavController) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val (_, onModeChange) = rememberEnumPreference(NuclearThemeModeKey, NuclearThemeMode.PRESET)
    val (storedTheme, onStoredThemeChange) = rememberPreference(NuclearCustomThemeKey, "")

    // Both modes, not just the one matching the system: otherwise opening the
    // editor seeds one tab from the applied theme and the other from a preset.
    val liveLight = rememberNuclearPalette(dark = false)
    val liveDark = rememberNuclearPalette(dark = true)

    // Seeded from whatever is in use, so opening the editor never starts from a
    // blank slate - you are always editing the theme you can see.
    var name by rememberSaveable { mutableStateOf(NuclearThemeFile.decodeOrNull(storedTheme)?.name ?: "My theme") }
    var editingDark by rememberSaveable { mutableStateOf(systemDark) }
    // Preferences arrive from DataStore a frame or two after first composition,
    // so capturing the palette in a plain remember{} seeds the editor from the
    // built-in default rather than from the theme actually in use. Track the
    // live palette until the first edit, then leave the user's work alone.
    var edited by rememberSaveable { mutableStateOf(false) }
    var light by remember { mutableStateOf(liveLight) }
    var dark by remember { mutableStateOf(liveDark) }

    LaunchedEffect(liveLight, liveDark, storedTheme, edited) {
        if (edited) return@LaunchedEffect
        val stored = NuclearThemeFile.decodeOrNull(storedTheme)
        light = stored?.palette(false) ?: liveLight
        dark = stored?.palette(true) ?: liveDark
        stored?.name?.let { name = it }
    }

    var editingToken by remember { mutableStateOf<String?>(null) }
    var showImport by rememberSaveable { mutableStateOf(false) }

    val editing = if (editingDark) dark else light
    fun update(palette: NuclearPalette) {
        edited = true
        if (editingDark) dark = palette else light = palette
    }

    fun currentFile() = NuclearThemeFile.from(name, light, dark)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nuclear_advanced_theme)) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.cd_back),
                    )
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.nuclear_theme_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NuclearButton(
                        onClick = { editingDark = false },
                        variant = if (editingDark) {
                            NuclearButtonVariant.Secondary
                        } else {
                            NuclearButtonVariant.Primary
                        },
                    ) { Text(stringResource(R.string.nuclear_light_tokens)) }
                    NuclearButton(
                        onClick = { editingDark = true },
                        variant = if (editingDark) {
                            NuclearButtonVariant.Primary
                        } else {
                            NuclearButtonVariant.Secondary
                        },
                    ) { Text(stringResource(R.string.nuclear_dark_tokens)) }
                }
            }

            items(NuclearThemeFile.TOKENS) { tokenName ->
                NuclearColorRow(
                    label = stringResource(tokenLabel(tokenName)),
                    color = editing.token(tokenName),
                    onClick = { editingToken = tokenName },
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NuclearButton(
                        onClick = {
                            onStoredThemeChange(currentFile().encode())
                            onModeChange(NuclearThemeMode.CUSTOM)
                        },
                    ) { Text(stringResource(R.string.nuclear_apply_theme)) }

                    NuclearButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "$name.ruzalia.json")
                                putExtra(Intent.EXTRA_TEXT, currentFile().encode())
                            }
                            context.startActivity(Intent.createChooser(send, null))
                        },
                        variant = NuclearButtonVariant.Tertiary,
                    ) { Text(stringResource(R.string.nuclear_share_theme)) }

                    NuclearButton(
                        onClick = { showImport = true },
                        variant = NuclearButtonVariant.Tertiary,
                    ) { Text(stringResource(R.string.nuclear_import_theme)) }
                }
            }
        }
    }

    editingToken?.let { tokenName ->
        DefaultDialog(onDismiss = { editingToken = null }) {
            Text(
                text = stringResource(tokenLabel(tokenName)),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            NuclearColorPicker(
                color = editing.token(tokenName),
                onColorChange = { update(editing.withToken(tokenName, it)) },
            )
            Spacer(Modifier.height(12.dp))
            NuclearButton(onClick = { editingToken = null }) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }

    if (showImport) {
        var pasted by remember { mutableStateOf("") }
        var failed by remember { mutableStateOf(false) }
        DefaultDialog(onDismiss = { showImport = false }) {
            Text(
                text = stringResource(R.string.nuclear_import_theme),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pasted,
                onValueChange = {
                    pasted = it
                    failed = false
                },
                label = { Text(stringResource(R.string.nuclear_import_hint)) },
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            if (failed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.nuclear_import_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(12.dp))
            NuclearButton(
                onClick = {
                    val parsed = NuclearThemeFile.decodeOrNull(pasted)
                    if (parsed == null) {
                        failed = true
                    } else {
                        edited = true
                        name = parsed.name
                        light = parsed.palette(false)
                        dark = parsed.palette(true)
                        showImport = false
                    }
                },
            ) { Text(stringResource(R.string.nuclear_import_theme)) }
        }
    }
}

private fun tokenLabel(token: String): Int = when (token) {
    NuclearThemeFile.TOKEN_BACKGROUND -> R.string.token_background
    NuclearThemeFile.TOKEN_BACKGROUND_SECONDARY -> R.string.token_background_secondary
    NuclearThemeFile.TOKEN_BACKGROUND_INPUT -> R.string.token_background_input
    NuclearThemeFile.TOKEN_FOREGROUND -> R.string.token_foreground
    NuclearThemeFile.TOKEN_FOREGROUND_SECONDARY -> R.string.token_foreground_secondary
    NuclearThemeFile.TOKEN_PRIMARY -> R.string.token_primary
    NuclearThemeFile.TOKEN_BORDER -> R.string.token_border
    NuclearThemeFile.TOKEN_GREEN -> R.string.token_accent_green
    NuclearThemeFile.TOKEN_YELLOW -> R.string.token_accent_yellow
    NuclearThemeFile.TOKEN_PURPLE -> R.string.token_accent_purple
    NuclearThemeFile.TOKEN_BLUE -> R.string.token_accent_blue
    NuclearThemeFile.TOKEN_ORANGE -> R.string.token_accent_orange
    NuclearThemeFile.TOKEN_CYAN -> R.string.token_accent_cyan
    else -> R.string.token_accent_red
}
