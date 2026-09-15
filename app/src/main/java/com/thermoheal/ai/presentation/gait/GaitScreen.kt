package com.thermoheal.ai.presentation.gait

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.GaitReading
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class GaitViewModel @Inject constructor(
    sensorRepository: SensorRepository
) : ViewModel() {
    val readings: StateFlow<List<GaitReading>> = sensorRepository
        .observeGaitReadings(System.currentTimeMillis() - 30 * 60_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun GaitScreen(onBack: () -> Unit, viewModel: GaitViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsStateWithLifecycle()
    val avgCadence = readings.filter { it.cadence > 0 }.map { it.cadence }.let { if (it.isEmpty()) 0.0 else it.average() }
    val activeCount = readings.count { it.cadence > 0 }
    val standingCount = readings.size - activeCount
    val avgDeviation = readings.map { abs(it.leftRightBalance) }.let { if (it.isEmpty()) 0.0 else it.average() }
    val patternLabel = when {
        avgDeviation > 25 -> "Needs Attention"
        avgDeviation > 12 -> "Changing"
        else -> "Stable"
    }

    Scaffold(topBar = { ThermoHealTopBar("Gait & Activity", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Gait Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                StatusBadge(
                    patternLabel,
                    tone = when (patternLabel) {
                        "Needs Attention" -> BadgeTone.WARNING
                        "Changing" -> BadgeTone.INFO
                        else -> BadgeTone.SUCCESS
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Cadence", "${avgCadence.toInt()} spm", Modifier.weight(1f))
                StatCard("Active Samples", "$activeCount", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Standing Samples", "$standingCount", Modifier.weight(1f))
                StatCard("Baseline Deviation", "${avgDeviation.toInt()}%", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Left/Right Balance Over Time")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                ThermoLineChart(values = readings.map { it.leftRightBalance.toFloat() }, lineColor = MaterialTheme.colorScheme.primary, yMin = -100f, yMax = 100f)
            }

            if (patternLabel != "Stable") {
                Spacer(Modifier.height(16.dp))
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Your recent gait pattern differs from your personal baseline.", style = MaterialTheme.typography.bodyMedium)
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
