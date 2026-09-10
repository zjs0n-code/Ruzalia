/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The big cover at the top of an album or playlist: framed by a real outline
 * and a hard offset shadow instead of the soft blurred drop shadow Material
 * gives it.
 *
 * This is not a [NuclearSurface] because the artwork is not a button - a
 * surface would slide onto its shadow when touched, and nothing would happen.
 * The shadow is drawn outside the inner box and into padding reserved for it,
 * so the whole thing still occupies exactly [size] and cannot bleed into the
 * title underneath.
 */
@Composable
fun NuclearArtwork(
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = NuclearTheme.colors.backgroundSecondary,
    content: @Composable BoxScope.() -> Unit,
) {
    val metrics = NuclearTheme.metrics
    Box(
        modifier = modifier
            .size(size)
            .padding(end = metrics.shadowOffset, bottom = metrics.shadowOffset)
            .nuclearHardShadow(shape, NuclearTheme.colors.shadow, metrics.shadowOffset)
            .clip(shape)
            .background(color)
            .nuclearBorder(shape, NuclearTheme.colors.border, metrics.borderWidth),
        content = content,
    )
}
