package com.thermoheal.ai.presentation.privacy

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.UserProfile
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.domain.usecase.ExportDataUseCase
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sensorRepository: SensorRepository,
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun exportData(context: Context, format: String = "CSV") {
        viewModelScope.launch {
            try {
                val readings = sensorRepository.getAllReadingsForExport()
                val user = userProfile.value
                val content = if (format == "JSON") {
                    exportDataUseCase.exportToJson(user, readings)
                } else {
                    exportDataUseCase.exportToCsv(user, readings)
                }

                val ext = if (format == "JSON") "json" else "csv"
                val file = File(context.cacheDir, "ThermoHeal_Export_${System.currentTimeMillis()}.$ext")
                file.writeText(content)

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = if (format == "JSON") "application/json" else "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "ThermoHeal-AI Data Export")
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export My Data"))
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteLocalSensorData(onComplete: () -> Unit) {
        viewModelScope.launch {
            sensorRepository.clearAllLocalData()
            onComplete()
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.deleteAccount()
            onComplete()
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            onComplete()
        }
    }
}

private data class SafetyItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val note: String)

private val safetyItems = listOf(
    SafetyItem(Icons.Filled.HealthAndSafety, "Skin-contact biocompatibility", "Design consideration — validation required before extended skin contact."),
    SafetyItem(Icons.Filled.WaterDrop, "Moisture-sealed electronics", "Target specification for the prototype enclosure."),
    SafetyItem(Icons.Filled.BatteryChargingFull, "Low-power operation", "Design goal to extend usable session time between charges."),
    SafetyItem(Icons.Filled.Memory, "On-device preprocessing", "Where applicable, raw signals are processed locally before storage."),
    SafetyItem(Icons.Filled.Lock, "Secure communication", "Local data is stored on-device; cloud sync (when enabled) uses HTTPS."),
)

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    viewModel: PrivacyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = { ThermoHealTopBar("Privacy Center", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "ThermoHeal-AI is a research and wellness-monitoring prototype. Its sensor measurements and AI-generated insights are intended for monitoring and decision support only and are not intended to diagnose, treat, cure, or prevent disease.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Clinical performance, safety, accuracy, and regulatory requirements require appropriate validation before clinical deployment.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Data Governance & Retention")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                PrivacyPoint("Data Collected", "Multimodal insole metrics: pressure (kPa), temperature (°C), moisture (%), step cadence, and user profile fields.")
                PrivacyPoint("Why It Is Collected", "To power continuous wellness monitoring, posture feedback, and algorithm baseline calibration.")
                PrivacyPoint("Where It Is Stored", "Stored locally on your device in an encrypted Room SQLite database. Cloud sync is optional and opt-in.")
                PrivacyPoint("AI Processing Model", "On-device rule-based analytics engine with full explainability. Telemetry is not sent to external LLMs.")
                PrivacyPoint("Data Minimization", "Only information necessary for biomechanical and thermal feedback is stored. Passwords are never stored in plain text.")
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Data Control & Export")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("You have complete ownership of your sensor and wellness data.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.exportData(context, "CSV") },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                    OutlinedButton(
                        onClick = { viewModel.exportData(context, "JSON") },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export JSON")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Data Deletion & Account")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showDeleteDataDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Local Sensor History")
                }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.PersonRemove, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account & All Data")
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Safety Considerations")
            safetyItems.forEach { item ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(90.dp))
        }

        if (showDeleteDataDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDataDialog = false },
                title = { Text("Delete Sensor History?") },
                text = { Text("This will permanently remove all pressure, temperature, moisture, and gait records from your device. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDataDialog = false
                            viewModel.deleteLocalSensorData {
                                Toast.makeText(context, "Local sensor data deleted.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete All") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDataDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("Delete Account?") },
                text = { Text("This will delete your user profile, all recorded sessions, and reset the application. Are you sure you want to proceed?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAccountDialog = false
                            viewModel.deleteAccount {
                                Toast.makeText(context, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete Account") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun PrivacyPoint(title: String, description: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

