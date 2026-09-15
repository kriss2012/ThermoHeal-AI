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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.domain.model.FootSide
import com.thermoheal.ai.domain.model.FootZone
import com.thermoheal.ai.ui.theme.SensorScale

/**
 * Responsive anatomical foot pressure map.
 * Guaranteed never to distort horizontally on tablets or landscape displays.
 * Supports interactive zone tapping and TalkBack accessibility semantics.
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
    // Normalized (0..1) vertical zone bands approximating a footprint silhouette
    val zones = listOf(
        Triple(FootZone.BIG_TOE, 0.03f to 0.16f, bigToe),
        Triple(FootZone.LESSER_TOES, 0.03f to 0.16f, lesserToes),
        Triple(FootZone.FOREFOOT, 0.18f to 0.40f, forefoot),
        Triple(FootZone.MIDFOOT, 0.40f to 0.55f, midfoot),
        Triple(FootZone.ARCH, 0.55f to 0.72f, arch),
        Triple(FootZone.HEEL, 0.74f to 0.98f, heel)
    )

    val highestZone = zones.maxByOrNull { it.third }?.first?.name ?: "Forefoot"
    val maxPressureVal = zones.maxOfOrNull { it.third }?.toInt() ?: 0

    val accessibilityText = "${side.name.lowercase().replaceFirstChar { it.uppercase() }} foot pressure map. " +
            "Highest pressure is at $highestZone with $maxPressureVal percent load."

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { contentDescription = accessibilityText },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .pointerInput(side) {
                    detectTapGestures { offset ->
                        val h = size.height.toFloat()
                        val w = size.width.toFloat()
                        val footWidth = (h * 0.46f).coerceAtMost(w * 0.92f)
                        val leftOrigin = (w - footWidth) / 2f
                        val relX = (offset.x - leftOrigin) / footWidth

                        val yFrac = offset.y / h
                        val isLeftHalf = if (side == FootSide.RIGHT) relX > 0.5f else relX < 0.5f

                        val zone = when {
                            yFrac < 0.18f -> if (isLeftHalf) FootZone.BIG_TOE else FootZone.LESSER_TOES
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

            // Anatomically bounded footprint box to avoid stretching on wide screens
            val footWidth = (h * 0.46f).coerceAtMost(w * 0.92f)
            val footLeft = (w - footWidth) / 2f

            // Soft outline track for foot silhouette
            drawRoundRect(
                color = Color.White.copy(alpha = 0.04f),
                topLeft = Offset(footLeft, h * 0.02f),
                size = Size(footWidth, h * 0.96f),
                cornerRadius = CornerRadius(footWidth / 2f, footWidth / 2f)
            )

            // Render each anatomical zone
            zones.forEach { (zone, band, value) ->
                val (yStart, yEnd) = band
                val color = pressureColor(value)
                val topY = h * yStart
                val bottomY = h * yEnd
                val zoneHeight = bottomY - topY

                val widthFactor = when (zone) {
                    FootZone.BIG_TOE -> 0.42f
                    FootZone.LESSER_TOES -> 0.48f
                    FootZone.FOREFOOT -> 0.88f
                    FootZone.MIDFOOT -> 0.65f
                    FootZone.ARCH -> 0.52f
                    FootZone.HEEL -> 0.68f
                }

                val currentZoneWidth = footWidth * widthFactor

                val zoneLeft = when (zone) {
                    FootZone.BIG_TOE -> if (mirror) footLeft + footWidth - currentZoneWidth else footLeft
                    FootZone.LESSER_TOES -> if (mirror) footLeft else footLeft + footWidth - currentZoneWidth
                    FootZone.ARCH -> if (mirror) footLeft + (footWidth - currentZoneWidth) * 0.8f else footLeft + (footWidth - currentZoneWidth) * 0.2f
                    else -> footLeft + (footWidth - currentZoneWidth) / 2f
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(zoneLeft, topY),
                    size = Size(currentZoneWidth, zoneHeight),
                    cornerRadius = CornerRadius(20f, 20f)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(SensorScale.pressureLow, "Low (<35%)")
        LegendDot(SensorScale.pressureMedium, "Med (35-70%)")
        LegendDot(SensorScale.pressureMediumHigh, "High (70-85%)")
        LegendDot(SensorScale.pressureHigh, "Critical (>85%)")
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
