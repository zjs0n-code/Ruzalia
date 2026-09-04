/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Matches Material's 52x32dp footprint once the shadow is counted. */
private val TrackWidth = 54.dp
private val TrackHeight = 32.dp
private val TrackInset = 3.dp
private val ThumbSize = 24.dp

/**
 * nuclear's toggle: a bordered track with a hard shadow and a square key that
 * slides between the two ends.
 *
 * This deliberately shadows `androidx.compose.material3.Switch` - the call
 * sites import this instead, and keep Material's parameter names - because a
 * pill with a floating circular thumb is the one Material shape that nothing
 * else in the skin has, and there are a hundred of them across the settings.
 *
 * The fill is the palette's own primary rather than the Material accent role,
 * so a settings screen reads in the chosen theme even while album-art colouring
 * is reseeding the accents used on content.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val palette = NuclearTheme.colors
    // The face is the track minus the shadow gap, and the key travels the width
    // that is left over once both insets are taken off it.
    val travel = TrackWidth - NuclearTheme.metrics.shadowOffset - TrackInset * 2 - ThumbSize
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) travel else 0.dp,
        animationSpec = tween(durationMillis = 140),
        label = "nuclearSwitchThumb",
    )

    NuclearSurface(
        modifier = modifier
            .size(TrackWidth, TrackHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Switch,
                        interactionSource = interactionSource,
                        // nuclear has no ripple; the track sliding onto its
                        // shadow is the feedback.
                        indication = null,
                        onValueChange = onCheckedChange,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (checked) palette.primary else palette.backgroundInput,
        contentPadding = PaddingValues(TrackInset),
        contentAlignment = Alignment.CenterStart,
    ) {
        NuclearSurface(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(ThumbSize),
            shape = RoundedCornerShape(4.dp),
            color = palette.backgroundSecondary,
            shadow = false,
            contentAlignment = Alignment.Center,
        ) {
            thumbContent?.invoke()
        }
    }
}
