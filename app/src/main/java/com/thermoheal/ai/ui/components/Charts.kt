package com.thermoheal.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import java.util.Locale

/**
 * Responsive, interactive line chart.
 * Automatically adapts height according to WindowSizeClass (190dp phone, 240dp medium, 280dp tablet).
 * Supports tap & drag inspection with live tooltip, touch isolation against page swiping,
 * and TalkBack textual accessibility summaries (Sections 32, 33, 92).
 */
@Composable
fun ThermoLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillChart: Boolean = true,
    heightDp: Int? = null,
    yMin: Float? = null,
    yMax: Float? = null,
    comfortBandMin: Float? = null,
    comfortBandMax: Float? = null,
    unitLabel: String = ""
) {
    val windowSize = LocalWindowSizeInfo.current
    val effectiveHeight = (heightDp ?: windowSize.chartHeightDp).dp

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val fillColor = lineColor.copy(alpha = 0.15f)
    val bandColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)

    // Accessibility summary for screen readers
    val summaryText = if (values.isEmpty()) {
        "Chart is empty"
    } else {
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val latest = values.lastOrNull() ?: 0f
        String.format(Locale.US, "Chart with %d data points. Latest value is %.1f %s, ranging from %.1f to %.1f.",
            values.size, latest, unitLabel, min, max)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = summaryText }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(effectiveHeight)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(values) {
                        // Capture tap and drag gestures so chart interaction takes priority
                        // over parent page swipes (Section 33, 96)
                        detectTapGestures(
                            onPress = { offset ->
                                if (values.isNotEmpty()) {
                                    val stepX = if (values.size > 1) size.width.toFloat() / (values.size - 1).toFloat() else size.width.toFloat()
                                    val idx = (offset.x / stepX).toInt().coerceIn(0, values.lastIndex)
                                    selectedIndex = idx
                                }
                            }
                        )
                    }
                    .pointerInput(values) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (values.isNotEmpty()) {
                                    val stepX = if (values.size > 1) size.width.toFloat() / (values.size - 1).toFloat() else size.width.toFloat()
                                    selectedIndex = (offset.x / stepX).toInt().coerceIn(0, values.lastIndex)
                                }
                            },
                            onDragEnd = { selectedIndex = null },
                            onDragCancel = { selectedIndex = null },
                            onDrag = { change, _ ->
                                change.consume()
                                if (values.isNotEmpty()) {
                                    val stepX = if (values.size > 1) size.width.toFloat() / (values.size - 1).toFloat() else size.width.toFloat()
                                    selectedIndex = (change.position.x / stepX).toInt().coerceIn(0, values.lastIndex)
                                }
                            }
                        )
                    }
            ) {
                if (values.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val minV = yMin ?: (values.minOrNull() ?: 0f)
                val maxV = yMax ?: (values.maxOrNull() ?: 1f)
                val range = (maxV - minV).let { if (it == 0f) 1f else it }

                // Grid lines (4 horizontal bands)
                for (i in 0..3) {
                    val y = h * i / 3f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                }

                // Comfort / target band
                if (comfortBandMin != null && comfortBandMax != null) {
                    val yTop = h - ((comfortBandMax - minV) / range) * h
                    val yBottom = h - ((comfortBandMin - minV) / range) * h
                    drawRect(
                        color = bandColor,
                        topLeft = Offset(0f, yTop.coerceIn(0f, h)),
                        size = Size(w, (yBottom - yTop).coerceIn(0f, h))
                    )
                }

                val stepX = if (values.size > 1) w / (values.size - 1) else w
                val points = values.mapIndexed { index, v ->
                    Offset(index * stepX, h - ((v - minV) / range) * h)
                }

                if (fillChart && points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, h)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, h)
                        close()
                    }
                    drawPath(
                        fillPath,
                        brush = Brush.verticalGradient(
                            listOf(fillColor, Color.Transparent),
                            startY = points.minOfOrNull { it.y } ?: 0f,
                            endY = h
                        )
                    )
                }

                if (points.size > 1) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }
                    drawPath(linePath, color = lineColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
                } else if (points.size == 1) {
                    drawCircle(lineColor, radius = 5.dp.toPx(), center = points.first())
                }

                // Selected point indicator overlay
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        // Vertical guideline
                        drawLine(
                            color = lineColor.copy(alpha = 0.6f),
                            start = Offset(pt.x, 0f),
                            end = Offset(pt.x, h),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        // Outer pulse ring
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 8.dp.toPx(),
                            center = pt
                        )
                        // Inner solid marker
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Interactive Tooltip Badge
            selectedIndex?.let { idx ->
                if (idx in values.indices) {
                    val currentVal = values[idx]
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = String.format(Locale.US, "Point %d: %.2f %s", idx + 1, currentVal, unitLabel),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
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
        val path = Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = h - ((v - minV) / range) * h
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    }
}

/** Simple horizontal bar comparison (Left vs Right pressure balance, section 16). */
@Composable
fun LeftRightBalanceBar(leftPercent: Double, rightPercent: Double, modifier: Modifier = Modifier) {
    val leftColor = MaterialTheme.colorScheme.primary
    val rightColor = MaterialTheme.colorScheme.tertiary
    Column(modifier) {
        Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
            Box(modifier = Modifier.weight(leftPercent.toFloat().coerceAtLeast(0.01f)).fillMaxHeight().background(leftColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)))
            Box(modifier = Modifier.weight(rightPercent.toFloat().coerceAtLeast(0.01f)).fillMaxHeight().background(rightColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Left: ${leftPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, color = leftColor)
            Text("Right: ${rightPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, color = rightColor)
        }
    }
}
