/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The one primitive the whole skin is built from: a flat panel with a drawn
 * outline and a hard, zero-blur shadow offset down and to the right. Pressing
 * it slides the panel onto its own shadow, which is nuclear's
 * `hover:translate-x-shadow-x hover:shadow-none` translated to touch.
 *
 * The shadow is laid out *inside* this composable's bounds rather than painted
 * outside them, so it can never bleed into a neighbour or be clipped away by
 * an ancestor. That means the visible panel is [NuclearMetrics.shadowOffset]
 * narrower and shorter than the space this occupies - size it the way you
 * would size a card, and it will look right.
 */
@Composable
fun NuclearSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = NuclearTheme.colors.backgroundSecondary,
    contentColor: Color = NuclearTheme.colors.contentColorOn(color),
    borderColor: Color = NuclearTheme.colors.border,
    borderWidth: Dp = NuclearTheme.metrics.borderWidth,
    shadow: Boolean = true,
    shadowColor: Color = NuclearTheme.colors.shadow,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    faceFill: NuclearFaceFill = NuclearFaceFill.None,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val offset = if (shadow) NuclearTheme.metrics.shadowOffset else 0.dp

    // Springy rather than linear: the panel should feel like it snaps down.
    val press by animateDpAsState(
        targetValue = if (pressed && enabled) offset else 0.dp,
        animationSpec = spring<Dp>(),
        label = "nuclearPress",
    )

    Box(modifier) {
        if (shadow) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding(start = offset, top = offset)
                    .background(shadowColor, shape),
            )
        }

        Box(
            modifier = Modifier
                .padding(end = offset, bottom = offset)
                .then(
                    when (faceFill) {
                        NuclearFaceFill.None -> Modifier
                        NuclearFaceFill.Height -> Modifier.fillMaxHeight()
                        NuclearFaceFill.Both -> Modifier.fillMaxSize()
                    },
                )
                .offset(press)
                .clip(shape)
                .background(color)
                .border(borderWidth, borderColor, shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            enabled = enabled,
                            interactionSource = source,
                            // nuclear has no ripple; the press *is* the feedback.
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(contentPadding),
            contentAlignment = contentAlignment,
        ) {
            val boxScope = this
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                with(boxScope) { content() }
            }
        }
    }
}

/**
 * How much of the space reserved by [NuclearSurface] the visible face should
 * take up.
 *
 * A Box sizes its children to their content, so a surface given a fixed size
 * would paint a full-size shadow behind a face only as big as its icon. Any
 * caller that imposes a size - an icon button, a weighted transport control -
 * has to say so here.
 */
enum class NuclearFaceFill { None, Height, Both }

/** Equal x/y translation that does not disturb the parent's measurement. */
private fun Modifier.offset(amount: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    // roundToPx needs a Density, and the placement block is not one.
    val px = amount.roundToPx()
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(px, px)
    }
}

/** Just the outline, for things that are already sized and clipped - artwork, chips. */
fun Modifier.nuclearBorder(
    shape: Shape,
    color: Color,
    width: Dp,
) = border(width, color, shape)

/**
 * Paints a hard offset shadow *behind* the element, outside its own bounds.
 *
 * Compose does not clip children by default, so this works anywhere there is
 * no `clipToBounds` ancestor - but the shadow is invisible to layout, so
 * reserve room for it yourself (usually end/bottom padding of [offset]).
 * Prefer [NuclearSurface] where you can wrap; use this where you cannot.
 */
fun Modifier.nuclearHardShadow(
    shape: Shape,
    color: Color,
    offset: Dp,
) = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    val px = offset.toPx()
    translate(left = px, top = px) {
        drawOutline(outline, color = color)
    }
}

/**
 * Strokes [shape] *over* the element's own drawing, inset so the whole stroke
 * stays inside the bounds.
 *
 * `Modifier.border` on a composable that paints its own background - a
 * Material `Surface`, or `ModalBottomSheet` - is drawn first and then covered
 * by that background. This runs after, so the outline survives.
 */
fun Modifier.nuclearOutlineOverlay(
    shape: Shape,
    color: Color,
    width: Dp,
) = drawWithContent {
    drawContent()
    val stroke = width.toPx()
    val inset = stroke / 2f
    val outline = shape.createOutline(
        Size(
            (size.width - stroke).coerceAtLeast(0f),
            (size.height - stroke).coerceAtLeast(0f),
        ),
        layoutDirection,
        this,
    )
    translate(inset, inset) {
        drawOutline(outline, color = color, style = Stroke(stroke))
    }
}
