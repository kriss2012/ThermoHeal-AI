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
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.ui.theme.thermoColors

private val processingChain = listOf(
    "Banana Farm", "Banana Pseudostem Waste", "Fiber Processing", "Cellulose Extraction",
    "Purification", "Nanocellulose Development", "Composite Formation", "Insole Layer"
)

private val properties = listOf(
    "Lightweight", "Breathable", "Mechanically stable", "Flexible", "Renewable", "Potentially biodegradable"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiomaterialScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ThermoHealTopBar("Biomaterial", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            Text(
                "Banana pseudostem waste, valorized into a structural biomaterial for the ThermoHeal-AI insole.",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            SectionHeader("Processing Chain")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                processingChain.forEachIndexed { index, stage ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Icon(Icons.Filled.Circle, contentDescription = null, tint = MaterialTheme.thermoColors.ecoGreen, modifier = Modifier.size(8.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stage, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (index != processingChain.lastIndex) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 3.dp).size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Material Properties")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                properties.forEach { prop ->
                    AssistChip(onClick = {}, label = { Text(prop) })
                }
            }

            Spacer(Modifier.height(20.dp))
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Properties listed reflect the prototype's design targets. \"Potentially biodegradable\" is used pending experimental validation — not yet independently confirmed.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}
