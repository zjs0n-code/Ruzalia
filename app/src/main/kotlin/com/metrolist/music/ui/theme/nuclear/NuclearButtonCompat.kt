/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Material's four button shapes wearing nuclear's.
 *
 * Every dialog in the app confirms and cancels through Material `TextButton`s,
 * and the settings, login and Listen Together screens act through `Button`,
 * `FilledTonalButton` and `OutlinedButton`. All of them are pills with a tonal
 * wash and no outline, which is the one button shape nothing else in the skin
 * has. Call sites import these instead and keep their arguments, the same trick
 * the [Switch] and [IconButton] shims use.
 *
 * The emphasis Material encodes in the choice of composable is kept: a filled
 * `Button` is the call to action, the other three are quieter. A call site that
 * hands over its own `colors` still wins - that is how the destructive and the
 * on-artwork buttons keep the colour they were given.
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = NuclearButton(
    onClick = onClick,
    modifier = modifier,
    variant = NuclearButtonVariant.Tertiary,
    shape = shape,
    enabled = enabled,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = NuclearButton(
    onClick = onClick,
    modifier = modifier,
    variant = NuclearButtonVariant.Primary,
    shape = shape,
    enabled = enabled,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = NuclearButton(
    onClick = onClick,
    modifier = modifier,
    variant = NuclearButtonVariant.Tertiary,
    shape = shape,
    enabled = enabled,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    interactionSource = interactionSource,
    content = content,
)

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = NuclearButton(
    onClick = onClick,
    modifier = modifier,
    variant = NuclearButtonVariant.Secondary,
    shape = shape,
    enabled = enabled,
    color = colors?.containerColor,
    contentColor = colors?.contentColor,
    interactionSource = interactionSource,
    content = content,
)
