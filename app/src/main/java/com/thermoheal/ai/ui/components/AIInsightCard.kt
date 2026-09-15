package com.thermoheal.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.domain.model.AIInsight
import com.thermoheal.ai.domain.model.InsightSeverity
import com.thermoheal.ai.ui.theme.thermoColors

@Composable
fun severityTone(severity: InsightSeverity): BadgeTone = when (severity) {
    InsightSeverity.NORMAL -> BadgeTone.SUCCESS
    InsightSeverity.OBSERVATION -> BadgeTone.INFO
    InsightSeverity.ATTENTION -> BadgeTone.WARNING
    InsightSeverity.HIGH_ATTENTION -> BadgeTone.DANGER
}

private fun iconForCategory(category: com.thermoheal.ai.domain.model.InsightCategory) = when (category) {
    com.thermoheal.ai.domain.model.InsightCategory.PRESSURE -> Icons.Filled.Speed
    com.thermoheal.ai.domain.model.InsightCategory.TEMPERATURE -> Icons.Filled.Thermostat
    com.thermoheal.ai.domain.model.InsightCategory.MOISTURE -> Icons.Filled.WaterDrop
    com.thermoheal.ai.domain.model.InsightCategory.GAIT -> Icons.Filled.DirectionsWalk
    com.thermoheal.ai.domain.model.InsightCategory.ACTIVITY -> Icons.Filled.Timeline
    com.thermoheal.ai.domain.model.InsightCategory.THERMAL -> Icons.Filled.DeviceThermostat
}

@Composable
fun AIInsightCard(insight: AIInsight, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    ThermoCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(iconForCategory(insight.category), contentDescription = null, tint = MaterialTheme.thermoColors.aiGlow, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            StatusBadge(insight.severity.label, severityTone(insight.severity))
        }
        Spacer(Modifier.height(8.dp))
        Text(insight.observation, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Recommendation: ${insight.recommendation}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Pattern confidence: ${insight.confidencePercent}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide details" else "Why am I seeing this?")
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 6.dp)) {
                insight.contributingFactors.forEach { factor ->
                    ContributingFactorBar(factor.label, factor.weightPercent)
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    "The insight is primarily influenced by ${insight.contributingFactors.maxByOrNull { it.weightPercent }?.label?.lowercase() ?: "recent sensor trends"}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(insight.disclaimer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ContributingFactorBar(label: String, weightPercent: Int) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$weightPercent%", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(weightPercent / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.thermoColors.aiGlow)
            )
        }
    }
}
