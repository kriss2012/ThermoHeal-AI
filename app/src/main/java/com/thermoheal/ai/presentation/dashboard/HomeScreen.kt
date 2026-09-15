package com.thermoheal.ai.presentation.dashboard

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
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.theme.thermoColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onOpenPressure: () -> Unit,
    onOpenTemperature: () -> Unit,
    onOpenMoisture: () -> Unit,
    onOpenGait: () -> Unit,
    onOpenDevice: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenSustainability: () -> Unit,
    onOpenResearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val user by viewModel.userProfile.collectAsState()
    val device by viewModel.deviceState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val insight by viewModel.latestInsight.collectAsState()
    val wellness by viewModel.wellnessIndex.collectAsState()
    val isDemoRunning by viewModel.isDemoRunning.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {

        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Good ${greeting()}, ${user?.name?.substringBefore(" ") ?: "there"}",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                ConnectionBadge(state = device?.connectionState ?: ConnectionState.DISCONNECTED)
            }
            IconButton(onClick = onOpenNotifications) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = "Notifications")
            }
        }

        if (isDemoRunning) {
            Spacer(Modifier.height(12.dp))
            DemoModeBanner()
        }

        Spacer(Modifier.height(16.dp))

        // Hero wellness card
        ThermoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surface) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FootHealthScoreRing(score = wellness?.score ?: 0, ringSizeDp = 108)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("Foot Wellness Index", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        wellness?.label ?: "Awaiting data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Prototype wellness index — not clinically validated.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (device?.connectionState != ConnectionState.CONNECTED) {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Connect your ThermoHeal-AI insole", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Start Research Demo Mode to explore the full app with realistic simulated data, or connect a physical prototype.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.startDemoMonitoring() }) { Text("Start Demo") }
                    OutlinedButton(onClick = onOpenDevice) { Text("Connect Insole") }
                }
            }
            Spacer(Modifier.height(20.dp))
        } else if (!isDemoRunning && reading?.sessionType == SessionType.DEMO) {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Simulation paused", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.startDemoMonitoring() }) { Text("Start Monitoring") }
            }
            Spacer(Modifier.height(20.dp))
        }

        // 2x2 metric grid
        SectionHeader("Today's Metrics")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Filled.Speed, label = "Pressure",
                value = pressureStatus(reading), status = pressureDescriptor(reading),
                trend = Trend.STABLE, accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f), onClick = onOpenPressure
            )
            MetricCard(
                icon = Icons.Filled.Thermostat, label = "Temperature",
                value = reading?.let { "%.1f°C".format(it.temperature) } ?: "—",
                status = temperatureDescriptor(reading), trend = Trend.STABLE,
                accentColor = MaterialTheme.thermoColors.info, modifier = Modifier.weight(1f), onClick = onOpenTemperature
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Filled.WaterDrop, label = "Moisture",
                value = reading?.let { "${it.moisture.roundToInt()}%" } ?: "—",
                status = moistureDescriptor(reading), trend = Trend.STABLE,
                accentColor = MaterialTheme.thermoColors.info, modifier = Modifier.weight(1f), onClick = onOpenMoisture
            )
            MetricCard(
                icon = Icons.Filled.DirectionsWalk, label = "Activity",
                value = reading?.let { "${it.steps} steps" } ?: "0 steps",
                status = reading?.activityState?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Idle",
                trend = Trend.STABLE, accentColor = MaterialTheme.thermoColors.ecoGreen,
                modifier = Modifier.weight(1f), onClick = onOpenGait
            )
        }

        Spacer(Modifier.height(20.dp))

        // Foot pressure preview
        SectionHeader("Foot Pressure Map", actionLabel = "View Details", onActionClick = onOpenPressure)
        ThermoCard(modifier = Modifier.fillMaxWidth()) {
            FootPressureMap(
                heel = (reading?.leftPressure ?: 40.0) * 0.9,
                arch = (reading?.leftPressure ?: 40.0) * 0.55,
                midfoot = (reading?.leftPressure ?: 40.0) * 0.7,
                forefoot = (reading?.leftPressure ?: 40.0) * 1.05,
                bigToe = (reading?.leftPressure ?: 40.0) * 0.8,
                lesserToes = (reading?.leftPressure ?: 40.0) * 0.6,
                side = FootSide.LEFT
            )
            Spacer(Modifier.height(8.dp))
            PressureLegend()
        }

        Spacer(Modifier.height(20.dp))

        // AI Insight
        SectionHeader("AI Insight", actionLabel = "View All", onActionClick = onOpenInsights)
        if (insight != null) {
            AIInsightCard(insight!!)
        } else {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                EmptyState(
                    title = "No wellness insight yet",
                    subtitle = "Continue monitoring to generate personalized wellness insights."
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Today's summary
        SectionHeader("Today's Summary")
        ThermoCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                dailySummary?.summarySentence ?: "Your wellness timeline will appear here after monitoring begins.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SummaryStat("Steps", "${dailySummary?.steps ?: 0}")
                SummaryStat("Active", "${dailySummary?.activeMinutes ?: 0} min")
                SummaryStat("Insights", "${dailySummary?.insightCount ?: 0}")
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Explore")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickLinkCard(Icons.Filled.Eco, "Sustainability", Modifier.weight(1f), onClick = onOpenSustainability)
            QuickLinkCard(Icons.Filled.Science, "Research", Modifier.weight(1f), onClick = onOpenResearch)
        }

        Spacer(Modifier.height(16.dp))
        WellnessDisclaimer()
        Spacer(Modifier.height(80.dp)) // room above bottom nav
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickLinkCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    ThermoCard(modifier = modifier, onClick = onClick) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.thermoColors.ecoGreen)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}

private fun pressureStatus(reading: SensorReading?): String {
    val peak = maxOf(reading?.leftPressure ?: 0.0, reading?.rightPressure ?: 0.0)
    return when {
        reading == null -> "—"
        peak > 65 -> "Elevated"
        peak > 50 -> "Moderate"
        else -> "Normal"
    }
}

private fun pressureDescriptor(reading: SensorReading?): String =
    if (reading == null) "Awaiting data" else "L ${reading.leftPressure.roundToInt()} / R ${reading.rightPressure.roundToInt()} kPa"

private fun temperatureDescriptor(reading: SensorReading?): String =
    if (reading == null) "Awaiting data" else if (reading.temperature > 33.0) "Warm Trend" else "Stable"

private fun moistureDescriptor(reading: SensorReading?): String =
    if (reading == null) "Awaiting data" else if (reading.moisture > 30.0) "Accumulating" else "Stable"
