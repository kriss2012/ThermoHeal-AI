package com.thermoheal.ai.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.domain.model.UserProfile
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.ui.components.ThermoCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val demoDataSimulator: DemoDataSimulator
) : ViewModel() {
    val user: StateFlow<UserProfile?> = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isCalibrating by mutableStateOf(false)
        private set
    var calibrationComplete by mutableStateOf(false)
        private set

    fun chooseDemoMode() = viewModelScope.launch {
        deviceRepository.connect("DEMO-INSOLE-0001")
    }

    fun chooseRealDevice() = viewModelScope.launch {
        deviceRepository.scanForDevices()
    }

    fun runCalibration() = viewModelScope.launch {
        isCalibrating = true
        deviceRepository.calibrate()
        isCalibrating = false
        calibrationComplete = true
    }
}

@Composable
fun ProfileSetupScreen(onContinue: () -> Unit, viewModel: FirstRunViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("You're all set, ${user?.name ?: "there"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your profile has been created. Next, connect your insole or start Research Demo Mode.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Continue") }
    }
}

@Composable
fun DeviceSetupScreen(
    onChooseDemo: () -> Unit,
    onChooseRealDevice: () -> Unit,
    viewModel: FirstRunViewModel = hiltViewModel()
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Connect Your Insole", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose how you'd like to get started. You can switch at any time from Settings.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        ThermoCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.chooseRealDevice(); onChooseRealDevice() }
        ) {
            Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Connect Physical Insole", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Scan for a nearby ThermoHeal-AI prototype device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(16.dp))
        ThermoCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.chooseDemoMode(); onChooseDemo() }
        ) {
            Icon(Icons.Filled.Science, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(8.dp))
            Text("Research Demo Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Explore the full app with realistic simulated sensor data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CalibrationScreen(onFinished: () -> Unit, viewModel: FirstRunViewModel = hiltViewModel()) {
    val steps = listOf(
        "Remove footwear / prepare the insole according to prototype protocol.",
        "Place foot normally.",
        "Remain still.",
        "Collect baseline.",
        "Confirm calibration."
    )

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Sensor Calibration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        steps.forEachIndexed { i, step ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${i + 1}.", modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(step, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(24.dp))

        if (viewModel.calibrationComplete) {
            Text("Pressure baseline: captured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("Temperature baseline: captured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("Moisture baseline: captured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onFinished, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Go to Dashboard") }
        } else {
            Button(
                onClick = { viewModel.runCalibration() },
                enabled = !viewModel.isCalibrating,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (viewModel.isCalibrating) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Start Calibration")
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onFinished) { Text("Skip for now") }
        }
    }
}
