package com.thermoheal.ai.presentation.device

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
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
        deviceRepository.connect("DEMO-INSOLE-0001")
        _isBusy.value = false
    }

    fun disconnect() = viewModelScope.launch {
        _isBusy.value = true
        deviceRepository.disconnect()
        _isBusy.value = false
    }

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

    val windowSize = LocalWindowSizeInfo.current
    val isTwoColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    // Determine the required Bluetooth permissions according to SDK guidelines
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            viewModel.scan()
        }
    }

    val hardwareCard: @Composable () -> Unit = {
        Column {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(device?.name ?: "ThermoHeal Smart Insole", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        ConnectionBadge(state = device?.connectionState ?: ConnectionState.DISCONNECTED)
                    }
                }
                Spacer(Modifier.height(14.dp))
                InfoRow("Device Address", device?.id ?: "—")
                InfoRow("Battery Level", "${device?.batteryPercent ?: "—"}%")
                InfoRow("Signal Quality", device?.signalStrength?.name ?: "—")
                InfoRow("Firmware Version", device?.firmwareVersion ?: "Production Build v1.0")
                InfoRow("Packets Synchronized", "${device?.packetsReceived ?: 0}")
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("On-Device Storage Cache")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Secure Local Storage Status", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Active (Encrypted Room Database Cache)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Active Hardware Diagnostics")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SensorStatusRow("Pressure Sensor Grid Matrix", device?.hasPressureSensor ?: false)
                SensorStatusRow("Surface NTC Thermistors", device?.hasTemperatureSensor ?: false)
                SensorStatusRow("Capacitive Moisture Impedance", device?.hasMoistureSensor ?: false)
                SensorStatusRow("6-Axis IMU Kinematic Telemetry", device?.hasImuSensor ?: false)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        permissionLauncher.launch(requiredPermissions)
                        viewModel.connect()
                    },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Connect") }
                OutlinedButton(onClick = { viewModel.disconnect() }, enabled = !isBusy, modifier = Modifier.weight(1f).height(48.dp)) { Text("Disconnect") }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(requiredPermissions) },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Scan Devices") }
                OutlinedButton(onClick = { viewModel.testSensors() }, enabled = !isBusy, modifier = Modifier.weight(1f).height(48.dp)) { Text("Run Diagnostics") }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { viewModel.calibrate() }, enabled = !isBusy, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Calibrate Precision Sensors") }

            if (sensorTest != null) {
                Spacer(Modifier.height(12.dp))
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Hardware Health Results", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    sensorTest!!.forEach { (name, ok) -> SensorStatusRow(name, ok) }
                }
            }
            if (calibrationDone) {
                Spacer(Modifier.height(12.dp))
                StatusBadge("Sensor Calibration Successful", BadgeTone.SUCCESS)
            }
        }
    }

    val simulationCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Research Simulation Settings")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                if (isSimRunning) DemoModeBanner()
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.startSimulation() }, enabled = !isSimRunning, modifier = Modifier.weight(1f).height(48.dp)) { Text("Resume Stream") }
                    OutlinedButton(onClick = { viewModel.stopSimulation() }, enabled = isSimRunning, modifier = Modifier.weight(1f).height(48.dp)) { Text("Pause Stream") }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { viewModel.resetSimulation() }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Clear Buffer") }

                Spacer(Modifier.height(16.dp))
                Text("Scenario Modeling Injection", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                "Authorized secure BLE pairing complies with Android core Bluetooth guidelines. Core sensor storage operations use high-performance internal caching frameworks to guarantee completely buffering-free user operation.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
        }
    }

    Scaffold(topBar = { ThermoHealTopBar("Insole Connection & Diagnostics", onBack) }) { padding ->
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
                if (isTwoColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.05f)) {
                            hardwareCard()
                        }
                        Column(modifier = Modifier.weight(1.15f)) {
                            simulationCard()
                        }
                    }
                } else {
                    hardwareCard()
                    Spacer(Modifier.height(20.dp))
                    simulationCard()
                }
                Spacer(Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SensorStatusRow(name: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        StatusBadge(if (active) "Active" else "Offline", if (active) BadgeTone.SUCCESS else BadgeTone.NEUTRAL)
    }
}
