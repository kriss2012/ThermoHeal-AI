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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.domain.model.ConnectionState
import com.thermoheal.ai.domain.model.FootSide
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import com.thermoheal.ai.ui.theme.thermoColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // Bounded query (Phase 85): queries up to 120 latest points without full-table scan
    val recentReadings: StateFlow<List<SensorReading>> = sensorRepository
        .observeRecentReadings(120)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun start() = viewModelScope.launch {
        if (deviceState.value?.connectionState != ConnectionState.CONNECTED) {
            deviceRepository.connect("DEMO-INSOLE-0001")
        }
        demoDataSimulator.startSimulation()
    }

    fun stop() = demoDataSimulator.stopSimulation()

    fun reconnect() = viewModelScope.launch {
        deviceRepository.connect(deviceState.value?.id ?: "DEMO-INSOLE-0001")
    }
}

@Composable
fun LiveMonitorScreen(viewModel: LiveMonitorViewModel = hiltViewModel()) {
    val device by viewModel.deviceState.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val readings by viewModel.recentReadings.collectAsStateWithLifecycle()
    val isConnected = device?.connectionState == ConnectionState.CONNECTED

    var selectedFoot by remember { mutableStateOf(FootSide.LEFT) }

    val windowSize = LocalWindowSizeInfo.current
    val isMultiPane = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape
    val latestReading = readings.lastOrNull()

    val headerSection: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Live Telemetry", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            ConnectionBadge(state = device?.connectionState ?: ConnectionState.DISCONNECTED)
        }
    }

    val alertBannerSection: @Composable () -> Unit = {
        if (!isConnected && readings.isNotEmpty()) {
            val lastTime = readings.lastOrNull()?.timestamp?.let {
                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(it))
            } ?: "N/A"

            ThermoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.errorContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Connection lost", tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Connection Lost", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Last telemetry packet received at $lastTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Button(
                        onClick = { viewModel.reconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Reconnect")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (readings.isNotEmpty() && readings.lastOrNull()?.sessionType?.name == "DEMO") {
            DemoModeBanner()
            Spacer(Modifier.height(12.dp))
        }
    }

    val controlsSection: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.start() },
                enabled = !isRunning,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Start Monitoring")
            }
            OutlinedButton(
                onClick = { viewModel.stop() },
                enabled = isRunning,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Stop Monitoring")
            }
        }
    }

    val footVisualizationCard: @Composable () -> Unit = {
        ThermoCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real-Time Pressure Heatmap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedFoot == FootSide.LEFT,
                        onClick = { selectedFoot = FootSide.LEFT },
                        label = { Text("Left") }
                    )
                    FilterChip(
                        selected = selectedFoot == FootSide.RIGHT,
                        onClick = { selectedFoot = FootSide.RIGHT },
                        label = { Text("Right") }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            val currentP = if (selectedFoot == FootSide.LEFT) latestReading?.leftPressure ?: 40.0 else latestReading?.rightPressure ?: 40.0
            FootPressureMap(
                heel = currentP * 0.9,
                arch = currentP * 0.55,
                midfoot = currentP * 0.7,
                forefoot = currentP * 1.05,
                bigToe = currentP * 0.8,
                lesserToes = currentP * 0.6,
                side = selectedFoot
            )
            Spacer(Modifier.height(12.dp))
            PressureLegend()
        }
    }

    val liveChartsSection: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LiveChartCard(
                title = "Pressure Dynamics (kPa)",
                primary = readings.map { it.leftPressure.toFloat() },
                secondary = readings.map { it.rightPressure.toFloat() },
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.thermoColors.ecoGreen,
                unit = "kPa"
            )
            LiveChartCard(
                title = "Thermal Stability (°C)",
                primary = readings.map { it.temperature.toFloat() },
                secondary = null,
                primaryColor = MaterialTheme.thermoColors.info,
                secondaryColor = null,
                comfortMin = 29f,
                comfortMax = 32f,
                unit = "°C"
            )
            LiveChartCard(
                title = "Microclimate Moisture (%)",
                primary = readings.map { it.moisture.toFloat() },
                secondary = null,
                primaryColor = MaterialTheme.thermoColors.info,
                secondaryColor = null,
                unit = "%"
            )
            LiveChartCard(
                title = "Gait Kinematics (Steps)",
                primary = readings.map { it.steps.toFloat() },
                secondary = null,
                primaryColor = MaterialTheme.thermoColors.ecoGreen,
                secondaryColor = null,
                unit = "steps"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .readableContentWidth(maxDp = 1200.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(windowSize.contentPadding)
        ) {
            headerSection()
            Spacer(Modifier.height(12.dp))
            alertBannerSection()
            controlsSection()
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
            } else if (isMultiPane) {
                // Multi-pane for Tablets & Landscape: Foot visualization on Left, Dynamic Charts on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(modifier = Modifier.weight(1.05f)) {
                        footVisualizationCard()
                    }
                    Column(modifier = Modifier.weight(1.35f)) {
                        liveChartsSection()
                    }
                }
            } else {
                // Single column for Compact Phones
                footVisualizationCard()
                Spacer(Modifier.height(16.dp))
                liveChartsSection()
            }

            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
private fun LiveChartCard(
    title: String, primary: List<Float>, secondary: List<Float>?,
    primaryColor: Color, secondaryColor: Color?,
    comfortMin: Float? = null, comfortMax: Float? = null,
    unit: String = ""
) {
    ThermoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Latest: ${primary.lastOrNull()?.let { "%.1f %s".format(it, unit) } ?: "—"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        ThermoLineChart(values = primary, lineColor = primaryColor, comfortBandMin = comfortMin, comfortBandMax = comfortMax, unitLabel = unit)
        if (secondary != null && secondaryColor != null) {
            Spacer(Modifier.height(4.dp))
            ThermoLineChart(values = secondary, lineColor = secondaryColor, fillChart = false, unitLabel = unit)
        }
    }
}
