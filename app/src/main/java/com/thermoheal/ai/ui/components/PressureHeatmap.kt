package com.thermoheal.ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.domain.model.FootSide
import com.thermoheal.ai.domain.model.FootZone
import com.thermoheal.ai.ui.theme.SensorScale

/**
 * A stylized single-foot pressure map. Values are 0-100 per zone.
 * Tapping a zone reports back via [onZoneTap] (section 14).
 */
@Composable
fun FootPressureMap(
    heel: Double,
    arch: Double,
    midfoot: Double,
    forefoot: Double,
    bigToe: Double,
    lesserToes: Double,
    side: FootSide,
    modifier: Modifier = Modifier,
    onZoneTap: (FootZone) -> Unit = {}
) {
    // Normalized (0..1) vertical zone bands approximating a footprint silhouette, mirrored for the right foot.
    val zones = listOf(
        Triple(FootZone.BIG_TOE, 0.03f to 0.16f, bigToe),
        Triple(FootZone.LESSER_TOES, 0.03f to 0.16f, lesserToes),
        Triple(FootZone.FOREFOOT, 0.18f to 0.40f, forefoot),
        Triple(FootZone.MIDFOOT, 0.40f to 0.55f, midfoot),
        Triple(FootZone.ARCH, 0.55f to 0.72f, arch),
        Triple(FootZone.HEEL, 0.74f to 0.98f, heel)
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .pointerInput(side) {
                    detectTapGestures { offset ->
                        val h = size.height.toFloat()
                        val yFrac = offset.y / h
                        val zone = when {
                            yFrac < 0.18f -> if (offset.x < size.width / 2f) FootZone.BIG_TOE else FootZone.LESSER_TOES
                            yFrac < 0.40f -> FootZone.FOREFOOT
                            yFrac < 0.55f -> FootZone.MIDFOOT
                            yFrac < 0.72f -> FootZone.ARCH
                            else -> FootZone.HEEL
                        }
                        onZoneTap(zone)
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val mirror = side == FootSide.RIGHT

            // Footprint silhouette (soft rounded blob approximation of heel->toes).
            zones.forEach { (_, band, value) ->
                val (yStart, yEnd) = band
                val color = pressureColor(value)
                val topY = h * yStart
                val bottomY = h * yEnd
                val widthFactor = when {
                    yStart < 0.18f -> 0.55f  // toes narrower
                    yStart < 0.40f -> 0.85f  // forefoot widest
                    yStart < 0.55f -> 0.6f   // midfoot
                    yStart < 0.72f -> 0.5f   // arch narrowest
                    else -> 0.7f             // heel
                }
                val left = w * (1f - widthFactor) / 2f
                val right = w - left
                val actualLeft = if (mirror) w - right else left
                val actualRight = if (mirror) w - left else right

                drawRoundRect(
                    color = color,
                    topLeft = Offset(actualLeft, topY),
                    size = androidx.compose.ui.geometry.Size(actualRight - actualLeft, bottomY - topY),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )
            }
        }
    }
}

private fun pressureColor(value: Double): Color {
    val v = (value / 100.0).coerceIn(0.0, 1.0)
    return when {
        v < 0.35 -> lerp(SensorScale.pressureLow, SensorScale.pressureMedium, (v / 0.35).toFloat())
        v < 0.7 -> lerp(SensorScale.pressureMedium, SensorScale.pressureMediumHigh, ((v - 0.35) / 0.35).toFloat())
        else -> lerp(SensorScale.pressureMediumHigh, SensorScale.pressureHigh, ((v - 0.7) / 0.3).toFloat())
    }
}

@Composable
fun PressureLegend(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        LegendDot(SensorScale.pressureLow, "Low")
        LegendDot(SensorScale.pressureMedium, "Medium")
        LegendDot(SensorScale.pressureMediumHigh, "Med-High")
        LegendDot(SensorScale.pressureHigh, "High")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color = color, shape = CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
