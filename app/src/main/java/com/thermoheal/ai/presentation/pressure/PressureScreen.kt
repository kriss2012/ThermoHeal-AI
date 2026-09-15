package com.thermoheal.ai.presentation.pressure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.FootSide
import com.thermoheal.ai.domain.model.PressureReading
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PressureViewModel @Inject constructor(
    sensorRepository: SensorRepository
) : ViewModel() {
    val readings: StateFlow<List<PressureReading>> = sensorRepository
        .observePressureReadings(System.currentTimeMillis() - 30 * 60_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun PressureScreen(onBack: () -> Unit, viewModel: PressureViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsState()
    val left = readings.filter { it.side == FootSide.LEFT }
    val right = readings.filter { it.side == FootSide.RIGHT }
    val leftAvg = left.map { it.average }.let { if (it.isEmpty()) 0.0 else it.average() }
    val rightAvg = right.map { it.average }.let { if (it.isEmpty()) 0.0 else it.average() }
    val total = (leftAvg + rightAvg).let { if (it == 0.0) 1.0 else it }
    val leftPercent = leftAvg / total * 100.0
    val rightPercent = rightAvg / total * 100.0
    val peak = readings.maxOfOrNull { it.peak } ?: 0.0

    Scaffold(topBar = { ThermoHealTopBar("Pressure Analysis", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Pressure Balance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                LeftRightBalanceBar(leftPercent, rightPercent)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Peak Pressure", "${peak.roundToInt()} kPa", Modifier.weight(1f))
                StatCard("Samples (30 min)", "${readings.size}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Left Foot")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                FootPressureMap(
                    heel = left.lastOrNull()?.heel ?: 0.0, arch = left.lastOrNull()?.arch ?: 0.0,
                    midfoot = left.lastOrNull()?.midfoot ?: 0.0, forefoot = left.lastOrNull()?.forefoot ?: 0.0,
                    bigToe = left.lastOrNull()?.bigToe ?: 0.0, lesserToes = left.lastOrNull()?.lesserToes ?: 0.0,
                    side = FootSide.LEFT
                )
                Spacer(Modifier.height(8.dp))
                PressureLegend()
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Right Foot")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                FootPressureMap(
                    heel = right.lastOrNull()?.heel ?: 0.0, arch = right.lastOrNull()?.arch ?: 0.0,
                    midfoot = right.lastOrNull()?.midfoot ?: 0.0, forefoot = right.lastOrNull()?.forefoot ?: 0.0,
                    bigToe = right.lastOrNull()?.bigToe ?: 0.0, lesserToes = right.lastOrNull()?.lesserToes ?: 0.0,
                    side = FootSide.RIGHT
                )
                Spacer(Modifier.height(8.dp))
                PressureLegend()
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Pressure Over Time")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                ThermoLineChart(values = readings.map { it.average.toFloat() }, lineColor = MaterialTheme.colorScheme.primary)
            }

            if (leftPercent > 60 || rightPercent > 60) {
                Spacer(Modifier.height(16.dp))
                SectionHeader("AI Insight")
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Your recent activity shows increased loading in the ${if (leftPercent > rightPercent) "left" else "right"} forefoot compared with your baseline.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Wellness insight only — not a medical diagnosis.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ThermoCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
