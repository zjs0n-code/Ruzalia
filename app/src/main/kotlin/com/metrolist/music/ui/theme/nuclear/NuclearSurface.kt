/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

/**
 * The one primitive the whole skin is built from: a flat panel with a drawn
 * outline and a hard, zero-blur shadow offset down and to the right. Pressing
 * it slides the panel onto its own shadow, which is nuclear's
 * `hover:translate-x-shadow-x hover:shadow-none` translated to touch.
 *
 * The shadow is laid out *inside* these bounds rather than painted outside
 * them, so it can never bleed into a neighbour or be clipped by an ancestor.
 *
 * The face is measured with the incoming constraints minus the shadow offset.
 * That is what makes a surface given a fixed size produce a face that fills it,
 * and one given loose constraints produce a face that wraps its content -
 * without the call site needing to know how any of this is put together.
 */
@OptIn(ExperimentalFoundationApi::class)
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
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val gap = if (shadow) NuclearTheme.metrics.shadowOffset else 0.dp
    val isClickable = onClick != null

    val source = if (isClickable) interactionSource ?: remember { MutableInteractionSource() } else null
    val ownPress = source?.collectIsPressedAsState()

    // A surface can be pressable without owning the click: GridItem is wrapped
    // in a clickable supplied by its caller, so the card carries the shadow
    // while something above it handles the tap. Watching the pointer without
    // consuming it lets those press too, and leaves the ancestor's click
    // handling exactly as it was.
    val observedPress = remember { mutableStateOf(false) }
    val isPressed = ownPress?.value ?: observedPress.value

    val press: State<Dp> =
        if (shadow) {
            animateDpAsState(
                targetValue = if (isPressed && enabled) gap else 0.dp,
                // Down is instant, release is animated. A tap is only ~60ms
                // from press to release, and a spring travelling both ways
                // never leaves zero before the target flips back - the button
                // looked dead on a real tap and only moved on a long press.
                animationSpec = if (isPressed) snap() else tween(durationMillis = 110),
                label = "nuclearPress",
            )
        } else {
            remember { mutableStateOf(0.dp) }
        }

    val pressObserver =
        if (!isClickable && shadow) {
            Modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        observedPress.value = true
                        waitForUpOrCancellation()
                        observedPress.value = false
                    }
                }
            }
        } else {
            Modifier
        }

    val faceModifier = Modifier
        .clip(shape)
        .background(color)
        .border(borderWidth, borderColor, shape)
        .then(
            if (onClick != null) {
                Modifier.combinedClickable(
                    interactionSource = source,
                    // nuclear has no ripple; the press *is* the feedback.
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onLongClick = onLongClick,
                    onClick = onClick,
                )
            } else {
                Modifier
            },
        )
        .padding(contentPadding)

    Layout(
        modifier = modifier
            .alpha(if (isClickable && !enabled) 0.5f else 1f)
            .then(pressObserver),
        content = {
            if (shadow) {
                Box(Modifier.background(shadowColor, shape))
            }
            Box(modifier = faceModifier, contentAlignment = contentAlignment) {
                val scope = this
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    with(scope) { content() }
                }
            }
        },
    ) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val faceIndex = if (shadow) 1 else 0

        val face = measurables[faceIndex].measure(constraints.offset(-gapPx, -gapPx))
        val shadowPlaceable =
            if (shadow) {
                measurables[0].measure(Constraints.fixed(face.width, face.height))
            } else {
                null
            }

        val pressPx = press.value.roundToPx()
        layout(face.width + gapPx, face.height + gapPx) {
            shadowPlaceable?.place(gapPx, gapPx)
            face.place(pressPx, pressPx)
        }
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

/**
 * A hard rule along the bottom edge, drawn *over* the element's own background.
 *
 * The app bar and the navigation bar sit at the page colour rather than on a
 * card, so the only thing separating them from the content is this line -
 * nuclear divides a header from its body with a rule, not with a fill.
 */
fun Modifier.bottomRule(
    color: Color,
    width: Dp,
) = drawWithContent {
    drawContent()
    val stroke = width.toPx()
    drawRect(
        color = color,
        topLeft = Offset(0f, size.height - stroke),
        size = Size(size.width, stroke),
    )
}
