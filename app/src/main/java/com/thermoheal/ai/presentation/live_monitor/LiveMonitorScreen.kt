package com.thermoheal.ai.presentation.live_monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.data.bluetooth.DemoScenario
import com.thermoheal.ai.domain.model.ConnectionState
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.theme.thermoColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class LiveMonitorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val deviceRepository: DeviceRepository,
    private val demoDataSimulator: DemoDataSimulator
) : ViewModel() {

    val deviceState = deviceRepository.observeDeviceState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isRunning = demoDataSimulator.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // NOTE: for a long-running production build this should page from Room with a
    // bounded time window (see HistoryScreen) rather than trimming client-side;
    // kept simple here since a single demo/research session stays well within
    // a few thousand rows.
    val recentReadings: StateFlow<List<SensorReading>> = sensorRepository
        .observeReadingsSince(0)
        .map { list -> list.takeLast(120) } // ~2 min at 1s cadence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun start() = viewModelScope.launch {
        if (deviceState.value?.connectionState != ConnectionState.CONNECTED) {
            deviceRepository.connect("DEMO-INSOLE-0001")
        }
        demoDataSimulator.startSimulation()
    }

    fun stop() = demoDataSimulator.stopSimulation()
}

@Composable
fun LiveMonitorScreen(viewModel: LiveMonitorViewModel = hiltViewModel()) {
    val device by viewModel.deviceState.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val readings by viewModel.recentReadings.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Live Monitoring", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            ConnectionBadge(state = device?.connectionState ?: ConnectionState.DISCONNECTED)
        }
        Spacer(Modifier.height(12.dp))

        if (readings.isNotEmpty() && readings.lastOrNull()?.sessionType?.name == "DEMO") {
            DemoModeBanner()
            Spacer(Modifier.height(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { viewModel.start() }, enabled = !isRunning) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp)); Text("Start Monitoring")
            }
            OutlinedButton(onClick = { viewModel.stop() }, enabled = isRunning) {
                Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp)); Text("Stop Monitoring")
            }
        }

        Spacer(Modifier.height(20.dp))

        if (readings.isEmpty()) {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                EmptyState(
                    title = "No live data yet",
                    subtitle = "Start monitoring to see real-time pressure, temperature, moisture and activity.",
                    actionLabel = "Start Monitoring",
                    onAction = { viewModel.start() }
                )
            }
        } else {
            LiveChartCard("Pressure (kPa)", readings.map { it.leftPressure.toFloat() }, readings.map { it.rightPressure.toFloat() }, MaterialTheme.colorScheme.primary, MaterialTheme.thermoColors.ecoGreen)
            Spacer(Modifier.height(16.dp))
            LiveChartCard("Temperature (°C)", readings.map { it.temperature.toFloat() }, null, MaterialTheme.thermoColors.info, null, comfortMin = 29f, comfortMax = 32f)
            Spacer(Modifier.height(16.dp))
            LiveChartCard("Moisture (%)", readings.map { it.moisture.toFloat() }, null, MaterialTheme.thermoColors.info, null)
            Spacer(Modifier.height(16.dp))
            LiveChartCard("Activity (cumulative steps)", readings.map { it.steps.toFloat() }, null, MaterialTheme.thermoColors.ecoGreen, null)
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun LiveChartCard(
    title: String, primary: List<Float>, secondary: List<Float>?,
    primaryColor: androidx.compose.ui.graphics.Color, secondaryColor: androidx.compose.ui.graphics.Color?,
    comfortMin: Float? = null, comfortMax: Float? = null
) {
    ThermoCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ThermoLineChart(values = primary, lineColor = primaryColor, comfortBandMin = comfortMin, comfortBandMax = comfortMax)
        if (secondary != null && secondaryColor != null) {
            Spacer(Modifier.height(4.dp))
            ThermoLineChart(values = secondary, lineColor = secondaryColor, fillChart = false)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Last value: ${primary.lastOrNull()?.let { "%.1f".format(it) } ?: "—"}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
