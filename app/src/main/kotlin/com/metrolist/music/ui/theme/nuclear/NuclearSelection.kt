/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.metrolist.music.R

private val BoxSize = 26.dp

/**
 * The tick box used when selecting songs, wearing nuclear's frame.
 *
 * Material draws a rounded square that fills with the accent and has no
 * outline; this is the same push surface as every other control, so a row of
 * them reads as part of the same app. Selecting is the accent fill, unselected
 * is the recessed input colour.
 */
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = NuclearTheme.colors
    NuclearSurface(
        modifier = modifier
            .size(BoxSize)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Checkbox,
                        indication = null,
                        interactionSource = null,
                        onValueChange = onCheckedChange,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(4.dp),
        color = if (checked) palette.primary else palette.backgroundInput,
        enabled = enabled,
        contentPadding = PaddingValues(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * The single-choice equivalent, in a square rather than a circle - nuclear has
 * no round controls anywhere else.
 */
@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = NuclearTheme.colors
    NuclearSurface(
        modifier = modifier
            .size(BoxSize)
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        indication = null,
                        interactionSource = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(4.dp),
        color = palette.backgroundInput,
        enabled = enabled,
        contentPadding = PaddingValues(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.primary),
            )
        }
    }
}
