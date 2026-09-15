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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
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
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val device by viewModel.deviceState.collectAsStateWithLifecycle()
    val reading by viewModel.latestReading.collectAsStateWithLifecycle()
    val insight by viewModel.latestInsight.collectAsStateWithLifecycle()
    val wellness by viewModel.wellnessIndex.collectAsStateWithLifecycle()
    val isDemoRunning by viewModel.isDemoRunning.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()

    val windowSize = LocalWindowSizeInfo.current
    val isExpanded = windowSize.widthClass == WindowWidthClass.EXPANDED
    val isMedium = windowSize.widthClass == WindowWidthClass.MEDIUM || (windowSize.isLandscape && !isExpanded)

    // Reusable UI components
    val headerSection: @Composable () -> Unit = {
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
    }

    val demoBannerSection: @Composable () -> Unit = {
        if (isDemoRunning) {
            Spacer(Modifier.height(12.dp))
            DemoModeBanner()
        }
    }

    val heroWellnessCard: @Composable () -> Unit = {
        ThermoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surface) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FootHealthScoreRing(score = wellness?.score ?: 0, ringSizeDp = if (windowSize.isSmallPhone) 88 else 108)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Foot Wellness Index", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        wellness?.label ?: "Awaiting data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Wellness insight only — not a medical diagnosis.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    val devicePromptCard: @Composable () -> Unit = {
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
        } else if (!isDemoRunning && reading?.sessionType == SessionType.DEMO) {
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text("Simulation paused", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.startDemoMonitoring() }) { Text("Start Monitoring") }
            }
        }
    }

    val metricGridSection: @Composable () -> Unit = {
        Column {
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
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk, label = "Activity",
                    value = reading?.let { "${it.steps} steps" } ?: "0 steps",
                    status = reading?.activityState?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Idle",
                    trend = Trend.STABLE, accentColor = MaterialTheme.thermoColors.ecoGreen,
                    modifier = Modifier.weight(1f), onClick = onOpenGait
                )
            }
        }
    }

    val footPressureSection: @Composable () -> Unit = {
        Column {
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
                Spacer(Modifier.height(12.dp))
                PressureLegend()
            }
        }
    }

    val aiInsightSection: @Composable () -> Unit = {
        Column {
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
        }
    }

    val dailySummarySection: @Composable () -> Unit = {
        Column {
            SectionHeader("Today's Summary")
            ThermoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    dailySummary?.summarySentence ?: "Your wellness timeline will appear here after monitoring begins.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    SummaryStat("Steps", "${dailySummary?.steps ?: 0}")
                    SummaryStat("Active", "${dailySummary?.activeMinutes ?: 0} min")
                    SummaryStat("Insights", "${dailySummary?.insightCount ?: 0}")
                }
            }
        }
    }

    val exploreSection: @Composable () -> Unit = {
        Column {
            SectionHeader("Explore")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickLinkCard(Icons.Filled.Eco, "Sustainability", Modifier.weight(1f), onClick = onOpenSustainability)
                QuickLinkCard(Icons.Filled.Science, "Research", Modifier.weight(1f), onClick = onOpenResearch)
            }
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
            demoBannerSection()
            Spacer(Modifier.height(16.dp))

            when {
                // EXPANDED: 3-column tablet dashboard (Section 5 & 27)
                isExpanded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Column 1: Wellness index, metrics, quick links
                        Column(modifier = Modifier.weight(1.05f)) {
                            heroWellnessCard()
                            Spacer(Modifier.height(16.dp))
                            metricGridSection()
                            Spacer(Modifier.height(16.dp))
                            exploreSection()
                        }
                        // Column 2: Foot Pressure Map & Device Prompt
                        Column(modifier = Modifier.weight(1.15f)) {
                            footPressureSection()
                            Spacer(Modifier.height(16.dp))
                            devicePromptCard()
                        }
                        // Column 3: AI Insights, Daily Summary & Disclaimer
                        Column(modifier = Modifier.weight(1.1f)) {
                            aiInsightSection()
                            Spacer(Modifier.height(16.dp))
                            dailySummarySection()
                            Spacer(Modifier.height(16.dp))
                            WellnessDisclaimer()
                        }
                    }
                }

                // MEDIUM / LANDSCAPE: 2-column layout (Section 5 & 27)
                isMedium -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column
                        Column(modifier = Modifier.weight(1f)) {
                            heroWellnessCard()
                            Spacer(Modifier.height(16.dp))
                            metricGridSection()
                            Spacer(Modifier.height(16.dp))
                            aiInsightSection()
                        }
                        // Right Column
                        Column(modifier = Modifier.weight(1f)) {
                            devicePromptCard()
                            if (device?.connectionState != ConnectionState.CONNECTED) Spacer(Modifier.height(16.dp))
                            footPressureSection()
                            Spacer(Modifier.height(16.dp))
                            dailySummarySection()
                            Spacer(Modifier.height(16.dp))
                            exploreSection()
                            Spacer(Modifier.height(16.dp))
                            WellnessDisclaimer()
                        }
                    }
                }

                // COMPACT: Single-column vertical scroll (Section 5 & 27)
                else -> {
                    heroWellnessCard()
                    Spacer(Modifier.height(16.dp))
                    devicePromptCard()
                    if (device?.connectionState != ConnectionState.CONNECTED) Spacer(Modifier.height(16.dp))
                    metricGridSection()
                    Spacer(Modifier.height(20.dp))
                    footPressureSection()
                    Spacer(Modifier.height(20.dp))
                    aiInsightSection()
                    Spacer(Modifier.height(20.dp))
                    dailySummarySection()
                    Spacer(Modifier.height(20.dp))
                    exploreSection()
                    Spacer(Modifier.height(16.dp))
                    WellnessDisclaimer()
                }
            }

            Spacer(Modifier.height(84.dp)) // padding above navigation
        }
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
