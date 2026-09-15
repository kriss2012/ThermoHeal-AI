package com.thermoheal.ai.presentation.moisture

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.MoistureReading
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.theme.thermoColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MoistureViewModel @Inject constructor(
    sensorRepository: SensorRepository
) : ViewModel() {
    val readings: StateFlow<List<MoistureReading>> = sensorRepository
        .observeMoistureReadings(System.currentTimeMillis() - 30 * 60_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun MoistureScreen(onBack: () -> Unit, viewModel: MoistureViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsStateWithLifecycle()
    val current = readings.lastOrNull()?.moisture ?: 0.0
    val avg = readings.map { it.moisture }.let { if (it.isEmpty()) 0.0 else it.average() }
    val accumulatedMinutes = readings.count { it.moisture > 30.0 }
    val trend = if (readings.size >= 2 && readings.last().moisture - readings.first().moisture > 5) "Rising" else "Stable"

    Scaffold(topBar = { ThermoHealTopBar("Moisture Monitoring", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoistureDropletGauge(percent = current, sizeDp = 90)
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Current Moisture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(trend, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Average", "${avg.toInt()}%", Modifier.weight(1f))
                StatCard("Accumulation", "$accumulatedMinutes samples", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Moisture Trend")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                ThermoLineChart(values = readings.map { it.moisture.toFloat() }, lineColor = MaterialTheme.thermoColors.info)
            }

            if (current > 30.0) {
                Spacer(Modifier.height(16.dp))
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Moisture accumulation detected.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Consider ventilation or a short break if comfortable.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
