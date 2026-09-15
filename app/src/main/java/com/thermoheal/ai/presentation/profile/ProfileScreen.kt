package com.thermoheal.ai.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.UserProfile
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {
    val user: StateFlow<UserProfile?> = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() = viewModelScope.launch { userRepository.logout() }
    fun deleteLocalData() = viewModelScope.launch { sensorRepository.clearAllLocalData() }
}

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenDevice: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val windowSize = LocalWindowSizeInfo.current
    val isTwoColumn = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val userHero: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(user?.name ?: "Guest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (user?.isDemoUser == true) {
                    Spacer(Modifier.height(4.dp))
                    Text("Research Demo Account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }

    val personalizationCard: @Composable () -> Unit = {
        Column {
            SectionHeader("Biometric Profile")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                ProfileRow("Foot Size", user?.footSizeEu?.let { "EU $it" } ?: "—")
                ProfileRow("Dominant Foot", user?.dominantFoot?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—")
                ProfileRow("Activity Level", user?.activityLevel?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—")
                ProfileRow("Height", user?.heightCm?.let { "$it cm" } ?: "—")
                ProfileRow("Weight", user?.weightKg?.let { "$it kg" } ?: "—")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.logout(); onLoggedOut() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Logout") }
        }
    }

    val settingsAndDataCards: @Composable () -> Unit = {
        Column {
            SectionHeader("Device Management")
            ThermoCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenDevice) {
                Text("Connected Insole", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("View device connection, hardware telemetry, calibration, and demo injection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Security & Privacy")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("Application Settings & Preferences")
                    }
                }
                TextButton(onClick = onOpenPrivacy, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("Privacy & Safety Center")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Local Storage")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("Clear Local Sensor History", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .readableContentWidth(maxDp = 1100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(windowSize.contentPadding)
        ) {
            Text("User Profile & Preferences", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            userHero()
            Spacer(Modifier.height(24.dp))

            if (isTwoColumn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        personalizationCard()
                    }
                    Column(modifier = Modifier.weight(1.1f)) {
                        settingsAndDataCards()
                    }
                }
            } else {
                personalizationCard()
                Spacer(Modifier.height(20.dp))
                settingsAndDataCards()
            }

            Spacer(Modifier.height(84.dp))

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Local Data?") },
                    text = { Text("This will permanently remove all sensor readings and AI insights stored on this device. This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.deleteLocalData(); showDeleteConfirm = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
