package com.thermoheal.ai.presentation.temperature

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
import com.thermoheal.ai.domain.model.TemperatureReading
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TemperatureViewModel @Inject constructor(
    sensorRepository: SensorRepository
) : ViewModel() {
    val readings: StateFlow<List<TemperatureReading>> = sensorRepository
        .observeTemperatureReadings(System.currentTimeMillis() - 30 * 60_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun TemperatureScreen(onBack: () -> Unit, viewModel: TemperatureViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsState()
    val current = readings.lastOrNull()?.temperature
    val avg = readings.map { it.temperature }.let { if (it.isEmpty()) null else it.average() }
    val max = readings.maxOfOrNull { it.temperature }
    val min = readings.minOfOrNull { it.temperature }
    val slope = if (readings.size >= 2) readings.last().temperature - readings.first().temperature else 0.0

    Scaffold(topBar = { ThermoHealTopBar("Thermal Environment", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Current", current?.let { "%.1f°C".format(it) } ?: "—", Modifier.weight(1f))
                StatCard("Average", avg?.let { "%.1f°C".format(it) } ?: "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Maximum", max?.let { "%.1f°C".format(it) } ?: "—", Modifier.weight(1f))
                StatCard("Minimum", min?.let { "%.1f°C".format(it) } ?: "—", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Thermal Comfort Zone")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                ThermoLineChart(
                    values = readings.map { it.temperature.toFloat() },
                    lineColor = MaterialTheme.colorScheme.primary,
                    comfortBandMin = 29f, comfortBandMax = 32f, heightDp = 160
                )
                Spacer(Modifier.height(8.dp))
                StatusBadge(
                    text = if (slope > 1.0) "Warm Trend" else "Thermal conditions appear stable",
                    tone = if (slope > 1.0) BadgeTone.WARNING else BadgeTone.SUCCESS
                )
            }

            Spacer(Modifier.height(16.dp))
            if (slope > 1.0) {
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Foot temperature has increased compared with your recent baseline.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "The passive PCM (phase-change material) layer is designed to buffer thermal fluctuations like this.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Thermal thresholds shown here are prototype targets, not scientifically validated medical limits.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
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
