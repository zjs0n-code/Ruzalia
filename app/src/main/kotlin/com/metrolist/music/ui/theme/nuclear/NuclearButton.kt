/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** nuclear's `buttonVariants` from packages/ui/src/components/Button/Button.tsx. */
enum class NuclearButtonVariant {
    /** Filled with the theme's primary. The default call to action. */
    Primary,

    /** Page-coloured fill: a quieter button that still has weight. */
    Secondary,

    /** Card-coloured fill, for buttons sitting directly on the page. */
    Tertiary,

    /** Primary fill with no shadow, for buttons inside an already-raised panel. */
    Flat,

    /** No fill, no outline. A link that happens to be a button. */
    Text,

    /** Outline only, inheriting the surrounding content colour. */
    Ghost,

    /** Destructive: filled with the accent red. */
    Danger,
}

enum class NuclearButtonSize(val height: Dp, val horizontalPadding: Dp) {
    Default(40.dp, 16.dp),
    Small(36.dp, 12.dp),
    ExtraSmall(32.dp, 8.dp),
    Large(44.dp, 32.dp),
    Icon(40.dp, 0.dp),
    IconSmall(32.dp, 0.dp),
}

@Composable
fun NuclearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NuclearButtonVariant = NuclearButtonVariant.Primary,
    size: NuclearButtonSize = NuclearButtonSize.Default,
    shape: Shape = MaterialTheme.shapes.medium,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = NuclearTheme.colors
    val metrics = NuclearTheme.metrics

    val hasShadow = variant in ShadowedVariants
    val fill = when (variant) {
        NuclearButtonVariant.Primary, NuclearButtonVariant.Flat -> palette.primary
        NuclearButtonVariant.Secondary -> palette.background
        NuclearButtonVariant.Tertiary -> palette.backgroundSecondary
        NuclearButtonVariant.Danger -> palette.accents.red
        NuclearButtonVariant.Text, NuclearButtonVariant.Ghost -> Color.Transparent
    }
    val outline = when (variant) {
        NuclearButtonVariant.Text -> Color.Transparent
        NuclearButtonVariant.Ghost -> palette.foreground
        else -> palette.border
    }
    val ink = when (variant) {
        NuclearButtonVariant.Text, NuclearButtonVariant.Ghost -> palette.foreground
        else -> palette.contentColorOn(fill)
    }

    // The face should be the size nuclear specifies; the shadow lives outside
    // it, so the box we ask for has to be that much taller.
    val boxHeight = if (hasShadow) size.height + metrics.shadowOffset else size.height
    val isIcon = size == NuclearButtonSize.Icon || size == NuclearButtonSize.IconSmall

    NuclearSurface(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .defaultMinSize(
                minWidth = if (isIcon) boxHeight else Dp.Unspecified,
                minHeight = boxHeight,
            ),
        shape = shape,
        color = fill,
        contentColor = ink,
        borderColor = outline,
        borderWidth = if (variant == NuclearButtonVariant.Text) 0.dp else metrics.borderWidth,
        shadow = hasShadow,
        enabled = enabled,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

private val ShadowedVariants = setOf(
    NuclearButtonVariant.Primary,
    NuclearButtonVariant.Secondary,
    NuclearButtonVariant.Tertiary,
    NuclearButtonVariant.Danger,
)
