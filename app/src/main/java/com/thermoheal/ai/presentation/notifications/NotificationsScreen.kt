package com.thermoheal.ai.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.components.EmptyState
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar

private enum class NotificationCategory { DEVICE, SENSOR, AI, SYSTEM }

private data class AppNotification(
    val id: Int, val category: NotificationCategory, val title: String, val body: String,
    val timeLabel: String, var read: Boolean, val icon: ImageVector
)

private fun sampleNotifications() = mutableStateListOf(
    AppNotification(1, NotificationCategory.AI, "New wellness insight available", "Pressure pattern detected in your recent session.", "2m ago", false, Icons.Filled.AutoAwesome),
    AppNotification(2, NotificationCategory.SENSOR, "Moisture accumulation detected", "Consider ventilation or a short break.", "18m ago", false, Icons.Filled.WaterDrop),
    AppNotification(3, NotificationCategory.DEVICE, "Insole battery is low", "18% remaining — consider charging soon.", "1h ago", true, Icons.Filled.BatteryAlert),
    AppNotification(4, NotificationCategory.SYSTEM, "Weekly report ready", "Your Week of Sep 8 trends are ready to view.", "1d ago", true, Icons.Filled.Description),
)

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = remember { sampleNotifications() }

    Scaffold(topBar = {
        ThermoHealTopBar("Notifications", onBack) {
            TextButton(onClick = { notifications.forEach { it.read = true } }) { Text("Mark all read") }
        }
    }) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(title = "No notifications", subtitle = "You're all caught up.")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(notifications, key = { it.id }) { n ->
                    ThermoCard(modifier = Modifier.fillMaxWidth(), onClick = { n.read = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(n.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(n.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold)
                                Text(n.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(n.timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                IconButton(onClick = { notifications.remove(n) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
