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
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import com.thermoheal.ai.ui.theme.thermoColors

private val processingChain = listOf(
    "Banana Farm Waste Collection", "Banana Pseudostem Pre-treatment", "Mechanical Fiber Decortication",
    "Chemical Cellulose Extraction", "Nanocellulose Fibril Synthesis", "Bio-composite Polymer Matrix", "Structural Smart Insole Core"
)

private val properties = listOf(
    "Lightweight Biopolymer", "High Micro-porous Breathability", "Mechanically Shock Absorbing",
    "Flexible Plantar Compliance", "100% Bio-Renewable Feedstock", "Low Carbon Footprint"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiomaterialScreen(onBack: () -> Unit) {
    val windowSize = LocalWindowSizeInfo.current
    val isTwoColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val processingCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Biomaterial Processing Chain")
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
        }
    }

    val propertiesCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Material Properties & Performance")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                properties.forEach { prop ->
                    AssistChip(onClick = {}, label = { Text(prop) })
                }
            }
            Spacer(Modifier.height(16.dp))
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Properties listed reflect the prototype's empirical engineering targets. Biodegradability and life-cycle assessments are conducted in accordance with ISO 14040 environmental management standards.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    Scaffold(topBar = { ThermoHealTopBar("Biomaterial Architecture", onBack) }) { padding ->
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
                    "Banana pseudostem agricultural waste valorized into a structural nanocellulose insole layer.",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                if (isTwoColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            processingCard()
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            propertiesCard()
                        }
                    }
                } else {
                    processingCard()
                    Spacer(Modifier.height(20.dp))
                    propertiesCard()
                }

                Spacer(Modifier.height(84.dp))
            }
        }
    }
}
