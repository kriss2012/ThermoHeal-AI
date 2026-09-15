package com.thermoheal.ai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.domain.model.ConnectionState
import com.thermoheal.ai.ui.theme.ThermoHealDimens
import com.thermoheal.ai.ui.theme.thermoColors

/** Base card used everywhere: 16-22dp radius, 1dp border, subtle elevation (section 50). */
@Composable
fun ThermoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var cardModifier = modifier
        .clip(RoundedCornerShape(ThermoHealDimens.CardRadius))
        .background(backgroundColor)
        .border(BorderStroke(ThermoHealDimens.CardBorderWidth, MaterialTheme.colorScheme.outline), RoundedCornerShape(ThermoHealDimens.CardRadius))
    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }
    Column(modifier = cardModifier.padding(ThermoHealDimens.CardPadding), content = content)
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) { Text(actionLabel) }
        }
    }
}

enum class BadgeTone { SUCCESS, WARNING, DANGER, INFO, NEUTRAL }

@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        BadgeTone.SUCCESS -> MaterialTheme.thermoColors.success.copy(alpha = 0.15f) to MaterialTheme.thermoColors.success
        BadgeTone.WARNING -> MaterialTheme.thermoColors.warning.copy(alpha = 0.15f) to MaterialTheme.thermoColors.warning
        BadgeTone.DANGER -> MaterialTheme.thermoColors.danger.copy(alpha = 0.15f) to MaterialTheme.thermoColors.danger
        BadgeTone.INFO -> MaterialTheme.thermoColors.info.copy(alpha = 0.15f) to MaterialTheme.thermoColors.info
        BadgeTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ConnectionBadge(state: ConnectionState, modifier: Modifier = Modifier) {
    val (tone, label, icon) = when (state) {
        ConnectionState.CONNECTED -> Triple(BadgeTone.SUCCESS, "Insole Connected", Icons.Filled.BluetoothConnected)
        ConnectionState.CONNECTING -> Triple(BadgeTone.INFO, "Connecting…", Icons.Filled.Bluetooth)
        ConnectionState.SCANNING -> Triple(BadgeTone.INFO, "Scanning…", Icons.Filled.Bluetooth)
        ConnectionState.DISCONNECTED -> Triple(BadgeTone.NEUTRAL, "Insole Disconnected", Icons.Filled.BluetoothDisabled)
        ConnectionState.ERROR -> Triple(BadgeTone.DANGER, "Connection Error", Icons.Filled.BluetoothDisabled)
    }
    val tint = when (tone) {
        BadgeTone.SUCCESS -> MaterialTheme.thermoColors.success
        BadgeTone.INFO -> MaterialTheme.thermoColors.info
        BadgeTone.DANGER -> MaterialTheme.thermoColors.danger
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Elegant, unmissable "SIMULATED SENSOR DATA" banner (section 83). Never let this be confused with real data. */
@Composable
fun DemoModeBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.thermoColors.warning.copy(alpha = 0.14f))
            .border(BorderStroke(1.dp, MaterialTheme.thermoColors.warning.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Science, contentDescription = null, tint = MaterialTheme.thermoColors.warning, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text("DEMO MODE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.thermoColors.warning)
            Text("Simulated sensor data — not from a physical device", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, actionLabel: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun LoadingState(label: String = "Loading…", modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.thermoColors.danger, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry) { Text("Try Again") }
        }
    }
}

/** Persistent, unobtrusive wellness disclaimer footer (section 41). */
@Composable
fun WellnessDisclaimer(modifier: Modifier = Modifier) {
    Text(
        text = "ThermoHeal-AI provides wellness-monitoring information and decision-support insights. It is not intended to diagnose, treat, cure, or prevent disease.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}
