package com.thermoheal.ai.presentation.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.thermoheal.ai.data.bluetooth.SimulationSpeed
import com.thermoheal.ai.domain.model.ConnectionState
import com.thermoheal.ai.domain.model.DeviceInfo
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val demoDataSimulator: DemoDataSimulator
) : ViewModel() {

    val deviceState: StateFlow<DeviceInfo?> = deviceRepository.observeDeviceState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSimRunning = demoDataSimulator.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val scenario = demoDataSimulator.scenario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoScenario.NORMAL)
    val speed = demoDataSimulator.speed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimulationSpeed.NORMAL)

    private val _sensorTestResult = MutableStateFlow<Map<String, Boolean>?>(null)
    val sensorTestResult: StateFlow<Map<String, Boolean>?> = _sensorTestResult.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _calibrationDone = MutableStateFlow(false)
    val calibrationDone: StateFlow<Boolean> = _calibrationDone.asStateFlow()

    fun connect() = viewModelScope.launch {
        _isBusy.value = true
        deviceRepository.connect(deviceState.value?.id ?: "DEMO-INSOLE-0001")
        _isBusy.value = false
    }

    fun disconnect() = viewModelScope.launch { deviceRepository.disconnect() }

    fun scan() = viewModelScope.launch {
        _isBusy.value = true
        deviceRepository.scanForDevices()
        _isBusy.value = false
    }

    fun testSensors() = viewModelScope.launch {
        _isBusy.value = true
        _sensorTestResult.value = deviceRepository.testSensors()
        _isBusy.value = false
    }

    fun calibrate() = viewModelScope.launch {
        _isBusy.value = true
        deviceRepository.calibrate()
        _calibrationDone.value = true
        _isBusy.value = false
    }

    fun startSimulation() = demoDataSimulator.startSimulation()
    fun stopSimulation() = demoDataSimulator.stopSimulation()
    fun resetSimulation() = demoDataSimulator.resetData()
    fun setScenario(s: DemoScenario) = demoDataSimulator.setScenario(s)
    fun setSpeed(s: SimulationSpeed) = demoDataSimulator.setSpeed(s)
}

@Composable
fun DeviceScreen(onBack: () -> Unit, viewModel: DeviceViewModel = hiltViewModel()) {
    val device by viewModel.deviceState.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val isSimRunning by viewModel.isSimRunning.collectAsState()
    val scenario by viewModel.scenario.collectAsState()
    val sensorTest by viewModel.sensorTestResult.collectAsState()
    val calibrationDone by viewModel.calibrationDone.collectAsState()

    Scaffold(topBar = { ThermoHealTopBar("Smart Insole", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(device?.name ?: "ThermoHeal-AI Insole", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        ConnectionBadge(state = device?.connectionState ?: ConnectionState.DISCONNECTED)
                    }
                }
                Spacer(Modifier.height(14.dp))
                InfoRow("Battery", "${device?.batteryPercent ?: "—"}%")
                InfoRow("Signal", device?.signalStrength?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—")
                InfoRow("Firmware", device?.firmwareVersion ?: "—")
                InfoRow("Packets Received", "${device?.packetsReceived ?: 0}")
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Sensors")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SensorStatusRow("Pressure", device?.hasPressureSensor ?: false)
                SensorStatusRow("Temperature", device?.hasTemperatureSensor ?: false)
                SensorStatusRow("Moisture", device?.hasMoistureSensor ?: false)
                SensorStatusRow("IMU / Gait", device?.hasImuSensor ?: false)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { viewModel.connect() }, enabled = !isBusy, modifier = Modifier.weight(1f)) { Text("Connect") }
                OutlinedButton(onClick = { viewModel.disconnect() }, enabled = !isBusy, modifier = Modifier.weight(1f)) { Text("Disconnect") }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { viewModel.scan() }, enabled = !isBusy, modifier = Modifier.weight(1f)) { Text("Scan Devices") }
                OutlinedButton(onClick = { viewModel.testSensors() }, enabled = !isBusy, modifier = Modifier.weight(1f)) { Text("Test Sensors") }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { viewModel.calibrate() }, enabled = !isBusy, modifier = Modifier.fillMaxWidth()) { Text("Calibrate") }

            if (sensorTest != null) {
                Spacer(Modifier.height(12.dp))
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Sensor Test Results", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    sensorTest!!.forEach { (name, ok) -> SensorStatusRow(name, ok) }
                }
            }
            if (calibrationDone) {
                Spacer(Modifier.height(12.dp))
                StatusBadge("Calibration complete", BadgeTone.SUCCESS)
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Research Demo Mode")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                if (isSimRunning) DemoModeBanner()
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.startSimulation() }, enabled = !isSimRunning, modifier = Modifier.weight(1f)) { Text("Start Simulation") }
                    OutlinedButton(onClick = { viewModel.stopSimulation() }, enabled = isSimRunning, modifier = Modifier.weight(1f)) { Text("Pause Simulation") }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.resetSimulation() }, modifier = Modifier.fillMaxWidth()) { Text("Reset Simulation") }

                Spacer(Modifier.height(16.dp))
                Text("Scenario", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DemoScenario.entries.toList()) { s ->
                        FilterChip(
                            selected = scenario == s, onClick = { viewModel.setScenario(s) },
                            label = { Text(s.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Hardware integration pending / prototype interface. This screen currently drives the built-in simulator; a physical ThermoHeal-AI insole can be wired in via the same BleRepository abstraction.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SensorStatusRow(name: String, ok: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        StatusBadge(if (ok) "Available" else "Unavailable", if (ok) BadgeTone.SUCCESS else BadgeTone.WARNING)
    }
}
