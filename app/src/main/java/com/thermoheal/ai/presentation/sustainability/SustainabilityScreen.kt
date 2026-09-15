package com.thermoheal.ai.presentation.sustainability

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    ImpactPillar("Environmental", listOf("Agricultural waste valorization", "Circular economy", "Biomaterial innovation"), Icons.Filled.Eco),
    ImpactPillar("Healthcare", listOf("Continuous wellness monitoring", "Preventive wellness support", "Multimodal sensing"), Icons.Filled.HealthAndSafety),
    ImpactPillar("Economic", listOf("Agricultural value addition", "Biomanufacturing opportunity", "Scalable innovation"), Icons.Filled.TrendingUp),
)

private data class Application(val title: String, val note: String)
private val applications = listOf(
    Application("Healthcare Professionals", "Prolonged standing during shifts"),
    Application("Elderly Populations", "Mobility and wellness tracking"),
    Application("Athletes", "Gait and load-distribution awareness"),
    Application("Industrial Personnel", "Prolonged or extreme environments"),
    Application("Military Personnel", "Extreme environments"),
)

@Composable
fun SustainabilityScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ThermoHealTopBar("Sustainability", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            Text(
                "From agricultural waste to healthcare value.",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                circularFlow.forEachIndexed { index, (label, icon) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.thermoColors.ecoGreen.copy(alpha = 0.15f)) {
                            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.thermoColors.ecoGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    if (index != circularFlow.lastIndex) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 16.dp).size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Impact")
            pillars.forEach { pillar ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(pillar.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pillar.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(6.dp))
                    pillar.points.forEach { point ->
                        Text("• $point", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Potential Applications")
            Text(
                "Potential applications only — not yet clinically validated for these populations.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(10.dp))
            applications.forEach { app ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text(app.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(app.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(90.dp))
        }
    }
}
