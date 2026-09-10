/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Material's `Card` wearing nuclear's panel.
 *
 * A Material card is a tonal rectangle lifted by a soft blurred elevation
 * shadow. Nuclear's is a flat panel with a real outline and a hard offset one,
 * which is what the settings groups, the list rows and the grid cards already
 * are - these are the leftovers on the lyrics, Listen Together, integrations
 * and equalizer screens.
 *
 * `elevation` is accepted and dropped: the depth is the offset shadow now, and
 * there is nothing to raise a card above. A `border` is dropped too, since the
 * panel draws its own; a `colors` fill is kept.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) = NuclearCardImpl(
    onClick = null,
    modifier = modifier,
    shape = shape,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    content = content,
)

@Composable
fun Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    border: BorderStroke? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) = NuclearCardImpl(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    shape = shape,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    content = content,
)

/** Material's raised variant; nuclear has one depth, so it is the same panel. */
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    content: @Composable ColumnScope.() -> Unit,
) = NuclearCardImpl(
    onClick = null,
    modifier = modifier,
    shape = shape,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    content = content,
)

@Composable
private fun NuclearCardImpl(
    onClick: (() -> Unit)?,
    modifier: Modifier,
    shape: Shape,
    color: Color?,
    contentColor: Color?,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val fill = color?.takeIf { it != Color.Unspecified } ?: NuclearTheme.colors.backgroundSecondary
    NuclearSurface(
        modifier = modifier,
        shape = shape,
        color = fill,
        contentColor = contentColor?.takeIf { it != Color.Unspecified }
            ?: NuclearTheme.colors.contentColorOn(fill),
        enabled = enabled,
        onClick = onClick,
    ) {
        Column(content = content)
    }
}
