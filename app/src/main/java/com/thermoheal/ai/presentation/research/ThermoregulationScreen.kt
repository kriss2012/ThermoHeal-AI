package com.thermoheal.ai.presentation.research

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.ui.theme.thermoColors

private val hotSequence = listOf("Foot temperature rises", "PCM absorbs thermal energy", "Phase transition occurs", "Excess heat is buffered")
private val coldSequence = listOf("Temperature falls", "Stored thermal energy is released", "Microclimate may stabilize")

@Composable
fun ThermoregulationScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ThermoHealTopBar("Passive Thermoregulation", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            Text("Passive thermal buffering", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "A bio-based phase-change material (PCM) layer absorbs and releases thermal energy passively — no active cooling components.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            SectionHeader("Hot Condition")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SequenceList(hotSequence, MaterialTheme.thermoColors.warning)
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Cold Condition")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SequenceList(coldSequence, MaterialTheme.thermoColors.info)
            }

            Spacer(Modifier.height(20.dp))
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "This describes the intended passive-buffering mechanism of the material design. Guaranteed thermal control is not claimed — actual performance requires thermal cycling and validation testing (see Research → Validation Roadmap).",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SequenceList(steps: List<String>, accentColor: androidx.compose.ui.graphics.Color) {
    steps.forEachIndexed { index, step ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
            Box(
                modifier = Modifier.size(8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = accentColor)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(step, style = MaterialTheme.typography.bodyMedium)
        }
        if (index != steps.lastIndex) {
            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 2.dp).size(14.dp))
        }
    }
}
