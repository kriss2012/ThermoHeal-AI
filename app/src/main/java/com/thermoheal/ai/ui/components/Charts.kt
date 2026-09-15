package com.thermoheal.ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Minimal, premium line chart: thin smooth-ish line, subtle grid, optional
 * fill gradient. Deliberately avoids 3D/rainbow/excess decoration (section 64).
 */
@Composable
fun ThermoLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillChart: Boolean = true,
    heightDp: Int = 140,
    yMin: Float? = null,
    yMax: Float? = null,
    comfortBandMin: Float? = null,
    comfortBandMax: Float? = null
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val fillColor = lineColor.copy(alpha = 0.12f)
    val bandColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(heightDp.dp)) {
            if (values.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val minV = yMin ?: (values.minOrNull() ?: 0f)
            val maxV = yMax ?: (values.maxOrNull() ?: 1f)
            val range = (maxV - minV).let { if (it == 0f) 1f else it }

            // Grid lines (4 horizontal bands)
            for (i in 0..3) {
                val y = h * i / 3f
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

            // Comfort / target band, if provided (Thermal Comfort Zone, section 17)
            if (comfortBandMin != null && comfortBandMax != null) {
                val yTop = h - ((comfortBandMax - minV) / range) * h
                val yBottom = h - ((comfortBandMin - minV) / range) * h
                drawRect(
                    color = bandColor,
                    topLeft = Offset(0f, yTop.coerceIn(0f, h)),
                    size = androidx.compose.ui.geometry.Size(w, (yBottom - yTop).coerceIn(0f, h))
                )
            }

            val stepX = if (values.size > 1) w / (values.size - 1) else w
            val points = values.mapIndexed { index, v ->
                Offset(index * stepX, h - ((v - minV) / range) * h)
            }

            if (fillChart && points.isNotEmpty()) {
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, h)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, h)
                    close()
                }
                drawPath(fillPath, color = fillColor)
            }

            if (points.size > 1) {
                val linePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }
                drawPath(linePath, color = lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
            } else if (points.size == 1) {
                drawCircle(lineColor, radius = 5f, center = points.first())
            }
        }
    }
}

/** Compact inline "mini trend" sparkline used inside MetricCard (section 13). */
@Composable
fun MiniSparkline(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(28.dp)) {
        if (values.size < 2) return@Canvas
        val w = size.width; val h = size.height
        val minV = values.min(); val maxV = values.max()
        val range = (maxV - minV).let { if (it == 0f) 1f else it }
        val stepX = w / (values.size - 1)
        val path = androidx.compose.ui.graphics.Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = h - ((v - minV) / range) * h
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    }
}

/** Simple horizontal bar comparison (e.g. Left vs Right pressure balance, section 16). */
@Composable
fun LeftRightBalanceBar(leftPercent: Double, rightPercent: Double, modifier: Modifier = Modifier) {
    val leftColor = MaterialTheme.colorScheme.primary
    val rightColor = MaterialTheme.colorScheme.tertiary
    Column(modifier) {
        Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
            Box(modifier = Modifier.weight(leftPercent.toFloat().coerceAtLeast(0.01f)).fillMaxHeight().background(leftColor))
            Box(modifier = Modifier.weight(rightPercent.toFloat().coerceAtLeast(0.01f)).fillMaxHeight().background(rightColor))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Left: ${leftPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, color = leftColor)
            Text("Right: ${rightPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, color = rightColor)
        }
    }
}
