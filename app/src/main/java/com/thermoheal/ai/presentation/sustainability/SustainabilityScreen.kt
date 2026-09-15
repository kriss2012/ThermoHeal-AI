package com.thermoheal.ai.presentation.sustainability

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import com.thermoheal.ai.ui.theme.thermoColors

private val circularFlow = listOf(
    "Agricultural Waste" to Icons.Filled.Grass,
    "Bioresource" to Icons.Filled.Eco,
    "Biomaterial" to Icons.Filled.Science,
    "Healthcare Product" to Icons.Filled.HealthAndSafety,
    "Circular Bioeconomy" to Icons.Filled.Loop,
)

private data class ImpactPillar(val title: String, val points: List<String>, val icon: ImageVector)

private val pillars = listOf(
    ImpactPillar("Environmental", listOf("Agricultural waste valorization", "Circular economy reduction", "Biomaterial sustainability"), Icons.Filled.Eco),
    ImpactPillar("Healthcare", listOf("Continuous wellness monitoring", "Preventive foot ulcer awareness", "Multimodal sensing"), Icons.Filled.HealthAndSafety),
    ImpactPillar("Economic", listOf("Agricultural farmer income addition", "Biomanufacturing supply chain", "Scalable IP innovation"), Icons.AutoMirrored.Filled.TrendingUp),
)

private data class Application(val title: String, val note: String)
private val applications = listOf(
    Application("Healthcare Workers", "Prolonged static standing during surgical shifts"),
    Application("Diabetic Individuals", "Plantar pressure awareness & ulcer prevention"),
    Application("Athletes & Runners", "Kinematic symmetry and impact monitoring"),
    Application("Industrial Personnel", "Heavy duty occupational foot comfort"),
)

@Composable
fun SustainabilityScreen(onBack: () -> Unit) {
    val windowSize = LocalWindowSizeInfo.current
    val isMultiColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val circularFlowCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Circular Bioeconomy Lifecycle")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                circularFlow.forEachIndexed { index, (label, icon) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.thermoColors.ecoGreen.copy(alpha = 0.15f)) {
                            Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.thermoColors.ecoGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Stage ${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Target User Populations")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                applications.forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(app.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(app.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    val pillarsCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Sustainability Pillars")
            pillars.forEach { pillar ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(pillar.icon, contentDescription = null, tint = MaterialTheme.thermoColors.ecoGreen, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(pillar.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    pillar.points.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }

    Scaffold(topBar = { ThermoHealTopBar("Sustainability & Impact", onBack) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .readableContentWidth(maxDp = 1100.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(windowSize.contentPadding)
            ) {
                Text(
                    "From Banana Agricultural Waste to Healthcare Value",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                if (isMultiColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.05f)) {
                            circularFlowCard()
                        }
                        Column(modifier = Modifier.weight(1.15f)) {
                            pillarsCard()
                        }
                    }
                } else {
                    circularFlowCard()
                    Spacer(Modifier.height(16.dp))
                    pillarsCard()
                }

                Spacer(Modifier.height(84.dp))
            }
        }
    }
}
