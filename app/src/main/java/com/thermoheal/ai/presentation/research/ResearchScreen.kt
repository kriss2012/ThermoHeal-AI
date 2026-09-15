package com.thermoheal.ai.presentation.research

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.StatusBadge
import com.thermoheal.ai.ui.components.BadgeTone
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar

private val workflowStages = listOf(
    "Material Characterization", "Thermal Testing", "Sensor Validation",
    "Mechanical Testing", "System Integration", "AI Validation", "User Study"
)

private data class RoadmapPhase(val name: String, val items: List<String>, val status: String)

private val roadmap = listOf(
    RoadmapPhase("Phase 1: Material Characterization", listOf("Tensile testing", "Moisture behavior", "Stability"), "Planned"),
    RoadmapPhase("Phase 2: Thermal Testing", listOf("Thermal cycling", "Heat absorption"), "Planned"),
    RoadmapPhase("Phase 3: Sensor Validation", listOf("Accuracy", "Repeatability", "Latency"), "In Progress"),
    RoadmapPhase("Phase 4: Mechanical Testing", listOf("Compression", "Fatigue", "Wear"), "Planned"),
    RoadmapPhase("Phase 5: System Integration", listOf("Power performance", "Connectivity"), "In Progress"),
    RoadmapPhase("Phase 6: AI Validation", listOf("Dataset training", "Sensitivity analysis"), "Planned"),
    RoadmapPhase("Phase 7: User Study", listOf("Post-ethical-approval testing"), "Future"),
)

@Composable
fun ResearchScreen(
    onBack: () -> Unit,
    onOpenBiomaterial: () -> Unit,
    onOpenThermoregulation: () -> Unit,
    onOpenPresentationMode: () -> Unit
) {
    Scaffold(topBar = { ThermoHealTopBar("Research", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("ThermoHeal-AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Smart Thermoregulatory Insole — Research Prototype", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("Research Domains", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                listOf("Biomaterials", "Passive thermoregulation", "Wearable sensing", "AI analytics", "Sustainable healthcare").forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Modules")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleCard(Icons.Filled.Science, "Biomaterial", Modifier.weight(1f), onOpenBiomaterial)
                ModuleCard(Icons.Filled.DeviceThermostat, "Thermoregulation", Modifier.weight(1f), onOpenThermoregulation)
            }
            Spacer(Modifier.height(12.dp))
            ThermoCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenPresentationMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Presentation Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Optimized for projector / judging panel demonstration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Research Workflow")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                workflowStages.forEachIndexed { index, stage ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(24.dp))
                        Text(stage, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Validation Roadmap")
            roadmap.forEach { phase ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(phase.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StatusBadge(phase.status, tone = when (phase.status) {
                            "In Progress" -> BadgeTone.INFO
                            "Future" -> BadgeTone.NEUTRAL
                            else -> BadgeTone.WARNING
                        })
                    }
                    Spacer(Modifier.height(4.dp))
                    phase.items.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Future phases are shown for roadmap clarity only and are not represented as completed. No clinical trial results are claimed.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ModuleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    ThermoCard(modifier = modifier, onClick = onClick) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
