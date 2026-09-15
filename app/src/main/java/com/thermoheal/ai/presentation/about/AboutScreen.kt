package com.thermoheal.ai.presentation.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.R
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar

private data class AboutSection(val title: String, val body: String)

private val sections = listOf(
    AboutSection("Problem", "Prolonged standing and uneven loading affect foot comfort and wellness — largely invisible without continuous monitoring."),
    AboutSection("Innovation", "A sustainable smart insole combining biomaterial engineering, passive thermoregulation, multimodal sensing and AI analytics."),
    AboutSection("Technology", "Kotlin + Jetpack Compose app, BLE-abstracted sensor architecture, on-device rule-based AI, Room-backed offline-first storage."),
    AboutSection("Biomaterial", "Cellulose/nanocellulose derived from banana pseudostem agricultural waste, formed into a structural insole layer."),
    AboutSection("Thermoregulation", "A bio-based phase-change material (PCM) layer passively buffers thermal fluctuations."),
    AboutSection("Sensors", "Pressure, temperature, moisture, and (where available) IMU/gait sensing sampled continuously."),
    AboutSection("AI", "A modular, swappable AI engine — currently a transparent rule-based prototype — turns features into wellness insights."),
    AboutSection("Sustainability", "Agricultural waste valorization within a circular bioeconomy model."),
    AboutSection("Research Roadmap", "Material characterization through thermal, sensor, and mechanical testing, to AI and user-study validation."),
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ThermoHealTopBar("About ThermoHeal-AI", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            // Official Logo Branding Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_thermoheal_logo),
                    contentDescription = "ThermoHeal-AI Official Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text("ThermoHeal-AI™", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Healthy Steps Ahead • v1.0.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "From Agricultural Waste to Healthcare Value",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            sections.forEach { s ->
                ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(s.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(s.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "ThermoHeal-AI is a research and wellness-monitoring prototype. Its sensor measurements and AI-generated insights are intended for monitoring and decision support only and are not intended to diagnose, treat, cure, or prevent disease.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(90.dp))
        }
    }
}
