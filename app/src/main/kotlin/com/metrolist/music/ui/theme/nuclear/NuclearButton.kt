/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
}

@Immutable
data class NuclearButtonColors(
    val fill: Color,
    val outline: Color,
    val content: Color,
    val hasShadow: Boolean,
)

@Composable
fun nuclearButtonColors(variant: NuclearButtonVariant): NuclearButtonColors {
    val palette = NuclearTheme.colors
    val fill = when (variant) {
        NuclearButtonVariant.Primary, NuclearButtonVariant.Flat -> palette.primary
        NuclearButtonVariant.Secondary -> palette.background
        NuclearButtonVariant.Tertiary -> palette.backgroundSecondary
        NuclearButtonVariant.Danger -> palette.accents.red
        NuclearButtonVariant.Text, NuclearButtonVariant.Ghost -> Color.Transparent
    }
    return NuclearButtonColors(
        fill = fill,
        outline = when (variant) {
            NuclearButtonVariant.Text -> Color.Transparent
            NuclearButtonVariant.Ghost -> palette.foreground
            else -> palette.border
        },
        content = when (variant) {
            NuclearButtonVariant.Text, NuclearButtonVariant.Ghost -> palette.foreground
            else -> palette.contentColorOn(fill)
        },
        hasShadow = variant in ShadowedVariants,
    )
}

@Composable
fun NuclearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NuclearButtonVariant = NuclearButtonVariant.Primary,
    size: NuclearButtonSize = NuclearButtonSize.Default,
    shape: Shape = MaterialTheme.shapes.medium,
    enabled: Boolean = true,
    /** Overrides the variant's fill, for call sites that derive their own. */
    color: Color? = null,
    contentColor: Color? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = nuclearButtonColors(variant)
    val metrics = NuclearTheme.metrics
    val fill = color ?: colors.fill
    val ink = contentColor ?: color?.let { NuclearTheme.colors.contentColorOn(it) } ?: colors.content

    // The face should be the height nuclear specifies; the shadow lives outside
    // it, so the box we ask for has to be that much taller.
    val boxHeight = if (colors.hasShadow) size.height + metrics.shadowOffset else size.height

    NuclearSurface(
        modifier = modifier
            .defaultMinSize(minHeight = boxHeight),
        shape = shape,
        color = fill,
        contentColor = ink,
        borderColor = colors.outline,
        borderWidth = if (variant == NuclearButtonVariant.Text) 0.dp else metrics.borderWidth,
        shadow = colors.hasShadow,
        enabled = enabled,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        contentAlignment = Alignment.Center,
        interactionSource = interactionSource,
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

/**
 * A square button for a single icon.
 *
 * [size] is the size of the visible face; the shadow is added outside it, so
 * the composable occupies slightly more room than that. Use this anywhere a
 * control is pressed often - transport, like, share, overflow - so it gets
 * nuclear's weight and its press-into-the-shadow feedback rather than reading
 * as a flat outlined box.
 */
@Composable
fun NuclearIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NuclearButtonVariant = NuclearButtonVariant.Primary,
    size: Dp = 40.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    /** Overrides the variant's fill, for call sites that derive their own. */
    color: Color? = null,
    contentColor: Color? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = nuclearButtonColors(variant)
    val metrics = NuclearTheme.metrics
    val box = if (colors.hasShadow) size + metrics.shadowOffset else size
    val fill = color ?: colors.fill
    val ink = contentColor ?: color?.let { NuclearTheme.colors.contentColorOn(it) } ?: colors.content

    NuclearSurface(
        modifier = modifier
            .size(box),
        shape = shape,
        color = fill,
        contentColor = ink,
        borderColor = colors.outline,
        borderWidth = if (variant == NuclearButtonVariant.Text) 0.dp else metrics.borderWidth,
        shadow = colors.hasShadow,
        enabled = enabled,
        onClick = onClick,
        onLongClick = onLongClick,
        contentAlignment = Alignment.Center,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

private val ShadowedVariants = setOf(
    NuclearButtonVariant.Primary,
    NuclearButtonVariant.Secondary,
    NuclearButtonVariant.Tertiary,
    NuclearButtonVariant.Danger,
)
