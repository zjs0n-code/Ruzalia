/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.theme.nuclear.NuclearButtonVariant
import com.metrolist.music.ui.theme.nuclear.NuclearIconButton

@Composable
fun ResizableIconButton(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    indication: Indication? = null,
    onClick: () -> Unit = {},
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
            .clickable(
                indication = indication ?: ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick,
            )
            .alpha(if (enabled) 1f else 0.5f),
    )
}

/**
 * The long-pressable icon button used in almost every app bar - the back arrow
 * top left, the search and overflow icons top right.
 *
 * It was a bare glyph on a transparent circle, which left every screen but the
 * home page opening with an unframed arrow floating next to a nuclear title.
 * Routing it through [NuclearIconButton] gives all fifty of those call sites the
 * outline, the hard shadow and the press without touching any of them, and
 * matches the size the home page's own app bar buttons already use.
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    NuclearIconButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        variant = NuclearButtonVariant.Tertiary,
        size = 48.dp,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        content()
    }
}
