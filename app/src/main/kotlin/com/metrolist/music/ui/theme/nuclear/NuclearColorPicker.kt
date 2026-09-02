/**
 * Ruzalia - Nuclear design language for Metrolist.
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme.nuclear

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Hue bar plus saturation/value square - the picker people expect when asked
 * to "choose any colour", rather than a fixed swatch list.
 *
 * Drawn in nuclear's own language: everything is a bordered panel and the
 * handles are outlined rings rather than shadows, so it reads as part of the
 * app instead of a system dialog dropped into it.
 */
@Composable
fun NuclearColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = NuclearTheme.colors
    val metrics = NuclearTheme.metrics

    // Hue is kept alongside the colour because it is unrecoverable from a
    // fully desaturated or black value - without this the handle jumps to red
    // as soon as you drag saturation to zero.
    var hue by remember { mutableFloatStateOf(color.hue()) }
    var hexText by remember(color) { mutableStateOf(color.toHex()) }

    val saturation = color.saturation()
    val value = color.value()

    fun emit(h: Float, s: Float, v: Float) {
        hue = h
        val next = Color.hsv(h.coerceIn(0f, 359.99f), s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
        hexText = next.toHex()
        onColorChange(next)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // saturation / value field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(MaterialTheme.shapes.medium)
                .border(metrics.borderWidth, palette.border, MaterialTheme.shapes.medium)
                .pointerInput(hue) {
                    fun pick(position: Offset) {
                        emit(hue, position.x / size.width, 1f - position.y / size.height)
                    }
                    detectTapGestures { pick(it) }
                }
                .pointerInput(hue) {
                    detectDragGestures { change, _ ->
                        emit(
                            hue,
                            change.position.x / size.width,
                            1f - change.position.y / size.height,
                        )
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1.6f)) {
                drawRect(
                    Brush.horizontalGradient(
                        listOf(Color.White, Color.hsv(hue.coerceIn(0f, 359.99f), 1f, 1f)),
                    ),
                )
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

                val handle = Offset(saturation * size.width, (1f - value) * size.height)
                drawCircle(Color.White, radius = 11.dp.toPx(), center = handle, style = ringStroke())
                drawCircle(Color.Black, radius = 8.dp.toPx(), center = handle, style = ringStroke())
            }
        }

        // hue
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(MaterialTheme.shapes.small)
                .border(metrics.borderWidth, palette.border, MaterialTheme.shapes.small)
                .pointerInput(Unit) {
                    fun pick(position: Offset) {
                        emit(position.x / size.width * 360f, saturation, value)
                    }
                    detectTapGestures { pick(it) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        emit(change.position.x / size.width * 360f, saturation, value)
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                drawRect(
                    Brush.horizontalGradient(
                        (0..360 step 30).map { Color.hsv(it.coerceAtMost(359).toFloat(), 1f, 1f) },
                    ),
                )
                val x = (hue / 360f) * size.width
                drawLine(
                    Color.White,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 6.dp.toPx(),
                )
                drawLine(
                    Color.Black,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color)
                    .border(metrics.borderWidth, palette.border, MaterialTheme.shapes.small),
            )
            OutlinedTextField(
                value = hexText,
                onValueChange = { typed ->
                    hexText = typed
                    typed.toColorOrNull()?.let { parsed ->
                        hue = parsed.hue()
                        onColorChange(parsed)
                    }
                },
                singleLine = true,
                label = { Text("Hex") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun ringStroke() = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)

private fun Color.hsv(): FloatArray {
    val out = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (red * 255f).toInt().coerceIn(0, 255),
        (green * 255f).toInt().coerceIn(0, 255),
        (blue * 255f).toInt().coerceIn(0, 255),
        out,
    )
    return out
}

private fun Color.hue(): Float = hsv()[0]

private fun Color.saturation(): Float = hsv()[1]

private fun Color.value(): Float = hsv()[2]

/** A labelled swatch that opens the picker; the advanced editor is a list of these. */
@Composable
fun NuclearColorRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = NuclearTheme.colors
    NuclearSurface(
        modifier = modifier.fillMaxWidth(),
        color = palette.backgroundSecondary,
        onClick = onClick,
        contentPadding = PaddingValues(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color)
                    .border(
                        NuclearTheme.metrics.borderWidth,
                        palette.border,
                        MaterialTheme.shapes.small,
                    ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = color.toHex(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = SpaceMono,
            )
        }
    }
}
