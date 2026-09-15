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
import com.thermoheal.ai.ui.components.BadgeTone
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.StatusBadge
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth

private val workflowStages = listOf(
    "Material Characterization", "Thermal Testing", "Sensor Validation",
    "Mechanical Testing", "System Integration", "AI Validation", "User Study"
)

private data class RoadmapPhase(val name: String, val items: List<String>, val status: String)

private val roadmap = listOf(
    RoadmapPhase("Phase 1: Material Characterization", listOf("Tensile testing", "Moisture behavior", "Structural stability"), "Planned"),
    RoadmapPhase("Phase 2: Thermal Testing", listOf("Thermal cycling", "PCM heat absorption", "Buffering efficiency"), "Planned"),
    RoadmapPhase("Phase 3: Sensor Validation", listOf("Piezo-resistive linearity", "NTC drift calibration", "BLE latency"), "In Progress"),
    RoadmapPhase("Phase 4: Mechanical Testing", listOf("Plantar impact compression", "Fatigue life", "Shear resistance"), "Planned"),
    RoadmapPhase("Phase 5: System Integration", listOf("Ultra-low power telemetry", "WorkManager sync engine"), "In Progress"),
    RoadmapPhase("Phase 6: AI Validation", listOf("On-device feature pipeline", "Heuristic sensitivity mapping"), "Planned"),
    RoadmapPhase("Phase 7: Clinical User Study", listOf("Post-ethical-approval human clinical trials"), "Future"),
)

@Composable
fun ResearchScreen(
    onBack: () -> Unit,
    onOpenBiomaterial: () -> Unit,
    onOpenThermoregulation: () -> Unit,
    onOpenPresentationMode: () -> Unit
) {
    val windowSize = LocalWindowSizeInfo.current
    val isMultiColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val overviewAndModules: @Composable () -> Unit = {
        Column {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("ThermoHeal-AI Research Platform", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Sustainable Smart Thermoregulatory Insole Prototype", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("Core Scientific Domains", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                listOf("Banana pseudostem nanocellulose biomaterials", "Bio-based PCM passive thermoregulation", "Multimodal IoT sensor integration", "Explainable clinical AI analytics", "Circular healthcare bioeconomy").forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Research Modules")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ModuleCard(Icons.Filled.Science, "Biomaterial", Modifier.weight(1f), onOpenBiomaterial)
                ModuleCard(Icons.Filled.DeviceThermostat, "Thermoregulation", Modifier.weight(1f), onOpenThermoregulation)
            }
            Spacer(Modifier.height(12.dp))
            ThermoCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenPresentationMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Presentation Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("16:9 widescreen immersive demonstration for project judging panels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    val workflowAndRoadmap: @Composable () -> Unit = {
        Column {
            SectionHeader("Research Workflow")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                workflowStages.forEachIndexed { index, stage ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(26.dp), fontWeight = FontWeight.Bold)
                        Text(stage, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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
        }
    }

    Scaffold(topBar = { ThermoHealTopBar("Research & Validation", onBack) }) { padding ->
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
                if (isMultiColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.05f)) {
                            overviewAndModules()
                        }
                        Column(modifier = Modifier.weight(1.15f)) {
                            workflowAndRoadmap()
                        }
                    }
                } else {
                    overviewAndModules()
                    Spacer(Modifier.height(20.dp))
                    workflowAndRoadmap()
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Future roadmap phases are shown for conceptual design clarity and are not represented as completed clinical trials.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun ModuleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    ThermoCard(modifier = modifier, onClick = onClick) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
