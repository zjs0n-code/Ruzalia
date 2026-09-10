/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * nuclear's push button wearing Material's `IconButton` signature.
 *
 * Call sites import this instead of `androidx.compose.material3.IconButton` and
 * keep their arguments, the same trick the [Switch] shim uses. Material's
 * version is a bare glyph on an invisible circle, which is how app bars, sort
 * rows and menu headers kept ending up with unframed icons floating beside
 * framed everything-else.
 *
 * The 48dp matches the home page's own app bar buttons. A call site that sizes
 * itself smaller still wins, because its `size` constrains this one - which is
 * what keeps the compact sort-row icons compact.
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    NuclearIconButton(
        onClick = onClick,
        modifier = modifier,
        variant = NuclearButtonVariant.Tertiary,
        size = 48.dp,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        content()
    }
}
