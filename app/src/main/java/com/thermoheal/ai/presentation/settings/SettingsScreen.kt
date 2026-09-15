package com.thermoheal.ai.presentation.settings

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
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoScenario
import com.thermoheal.ai.data.bluetooth.SimulationSpeed
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {
    val themeMode: StateFlow<String> = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")
    val unitSystem: StateFlow<String> = prefs.unitSystem.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "METRIC")
    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val researchModeEnabled: StateFlow<Boolean> = prefs.researchModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val demoSpeed: StateFlow<String> = prefs.demoSpeed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NORMAL")

    fun setThemeMode(mode: String) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setUnitSystem(system: String) = viewModelScope.launch { prefs.setUnitSystem(system) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    fun setResearchModeEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setResearchModeEnabled(enabled) }
    fun setDemoSpeed(speed: String) = viewModelScope.launch { prefs.setDemoSpeed(speed) }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val researchModeEnabled by viewModel.researchModeEnabled.collectAsState()
    val demoSpeed by viewModel.demoSpeed.collectAsState()

    Scaffold(topBar = { ThermoHealTopBar("Settings", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            SectionHeader("Appearance")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LIGHT" to "Light", "DARK" to "Dark", "SYSTEM" to "System").forEach { (value, label) ->
                        FilterChip(selected = themeMode == value, onClick = { viewModel.setThemeMode(value) }, label = { Text(label) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Units")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("METRIC" to "Metric", "IMPERIAL" to "Imperial").forEach { (value, label) ->
                        FilterChip(selected = unitSystem == value, onClick = { viewModel.setUnitSystem(value) }, label = { Text(label) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Notifications")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchRow("Enable Notifications", notificationsEnabled) { viewModel.setNotificationsEnabled(it) }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Research Mode")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchRow("Enable Research Mode", researchModeEnabled) { viewModel.setResearchModeEnabled(it) }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Exposes session IDs, raw sensor streams and export tools for researcher use.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Research Demo Mode")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Simulation Speed", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SLOW" to "Slow", "NORMAL" to "Normal", "FAST" to "Fast").forEach { (value, label) ->
                        FilterChip(selected = demoSpeed == value, onClick = { viewModel.setDemoSpeed(value) }, label = { Text(label) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("AI")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Engine: Prototype AI / Rule-based demonstration", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Architected so a trained ML model can be swapped in without UI changes.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("About")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("ThermoHeal-AI", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Version 1.0.0-prototype", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
