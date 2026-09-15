package com.thermoheal.ai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.domain.model.Trend
import com.thermoheal.ai.ui.theme.MetricNumberStyle
import com.thermoheal.ai.ui.theme.thermoColors

/** One of the four 2x2 dashboard metric cards (section 13). */
@Composable
fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    status: String,
    trend: Trend,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ThermoCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            TrendIcon(trend)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TrendIcon(trend: Trend, modifier: Modifier = Modifier) {
    val (icon, tint) = when (trend) {
        Trend.INCREASING -> Icons.Filled.TrendingUp to MaterialTheme.thermoColors.warning
        Trend.DECREASING -> Icons.Filled.TrendingDown to MaterialTheme.thermoColors.info
        Trend.STABLE -> Icons.Filled.TrendingFlat to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Icon(icon, contentDescription = trend.name, tint = tint, modifier = modifier.size(16.dp))
}

/** "Foot Wellness Index" hero circular gauge (section 12, 62) — never labeled as a medical score. */
@Composable
fun FootHealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    ringSizeDp: Int = 140
) {
    val progress by animateFloatAsState(targetValue = score / 100f, animationSpec = tween(900), label = "wellnessRing")

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringColor = when {
        score >= 90 -> MaterialTheme.thermoColors.success
        score >= 75 -> MaterialTheme.thermoColors.ecoGreen
        score >= 60 -> MaterialTheme.thermoColors.warning
        else -> MaterialTheme.thermoColors.danger
    }

    Box(modifier = modifier.size(ringSizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f, (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = MetricNumberStyle)
            Text("/ 100", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Droplet-style moisture gauge (section 18, 65). */
@Composable
fun MoistureDropletGauge(percent: Double, modifier: Modifier = Modifier, sizeDp: Int = 72) {
    val fillColor = when {
        percent < 20 -> MaterialTheme.thermoColors.info.copy(alpha = 0.85f)
        percent < 35 -> MaterialTheme.thermoColors.info
        percent < 50 -> MaterialTheme.thermoColors.warning
        else -> MaterialTheme.thermoColors.danger
    }
    Box(modifier = modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w / 2f, 0f)
                cubicTo(w * 0.95f, h * 0.55f, w * 0.85f, h, w / 2f, h)
                cubicTo(w * 0.15f, h, w * 0.05f, h * 0.55f, w / 2f, 0f)
                close()
            }
            drawPath(path, color = fillColor.copy(alpha = 0.18f))
            clipPath(path) {
                val fillHeight = h * (percent / 100f).coerceIn(0.05, 1.0).toFloat()
                drawRect(
                    color = fillColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, h - fillHeight),
                    size = Size(w, fillHeight)
                )
            }
        }
        Text("${percent.toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
