package com.thermoheal.ai.presentation.research

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import com.thermoheal.ai.ui.theme.thermoColors

private val hotSequence = listOf(
    "Plantar foot temperature rises during ambulation",
    "Bio-based PCM layer absorbs excess thermal energy",
    "Latent heat phase transition occurs smoothly",
    "Plantar skin interface temperature is stabilized"
)

private val coldSequence = listOf(
    "Ambient or rest temperature drops",
    "Stored latent thermal energy is released",
    "Microclimate stabilizes preventing hypothermic vasoconstriction"
)

@Composable
fun ThermoregulationScreen(onBack: () -> Unit) {
    val windowSize = LocalWindowSizeInfo.current
    val isTwoColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val hotCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Hyperthermic Buffering (Heat Rise)")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SequenceList(hotSequence, MaterialTheme.thermoColors.warning)
            }
        }
    }

    val coldCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Hypothermic Stabilization (Heat Release)")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SequenceList(coldSequence, MaterialTheme.thermoColors.info)
            }
        }
    }

    Scaffold(topBar = { ThermoHealTopBar("Passive Thermoregulation", onBack) }) { padding ->
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
                Text("Bio-Based Phase Change Material (PCM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "A specialized bio-PCM layer absorbs and releases thermal energy passively — buffering plantar thermal runaway without active electronic cooling components.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                if (isTwoColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            hotCard()
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            coldCard()
                        }
                    }
                } else {
                    hotCard()
                    Spacer(Modifier.height(16.dp))
                    coldCard()
                }

                Spacer(Modifier.height(20.dp))
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Describes the passive buffering mechanism of the material stack. Actual performance is verified via thermal cycling tests documented in Research → Validation Roadmap.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun SequenceList(steps: List<String>, accentColor: Color) {
    steps.forEachIndexed { index, step ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
            Box(
                modifier = Modifier.size(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(10.dp)) {
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
