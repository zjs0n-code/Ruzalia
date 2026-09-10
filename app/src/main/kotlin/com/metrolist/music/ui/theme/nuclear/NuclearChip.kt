/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Material's `FilterChip` as one of the small push buttons the mood row already
 * uses.
 *
 * The library filter row, the add-to-playlist dialog and the lyrics style
 * picker were the last places still showing Material's outlined pill. Selected
 * takes the theme's primary, unselected the card colour - the same pairing
 * `ChipsRow` settled on.
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: SelectableChipColors? = null,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    NuclearButton(
        onClick = onClick,
        modifier = modifier,
        variant = if (selected) NuclearButtonVariant.Primary else NuclearButtonVariant.Tertiary,
        size = NuclearButtonSize.Small,
        shape = shape,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        leadingIcon?.invoke()
        label()
        trailingIcon?.invoke()
    }
}
