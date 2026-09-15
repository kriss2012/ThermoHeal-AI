package com.thermoheal.ai.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.ui.theme.AIGlow
import com.thermoheal.ai.ui.theme.ScientificDark
import com.thermoheal.ai.ui.theme.SustainableGreen
import kotlinx.coroutines.delay

/**
 * Section 7: dark teal background -> glowing particle -> molecular pattern
 * -> leaf -> footprint -> circuit traces -> thermal wave -> logo -> tagline.
 * Total duration ~2.6s, implemented with native Compose animation (no Lottie
 * asset required) so it stays lightweight.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var stage by remember { mutableStateOf(0) }

    val particleAlpha by animateFloatAsState(if (stage >= 1) 1f else 0f, tween(300), label = "particle")
    val moleculeAlpha by animateFloatAsState(if (stage >= 2) 1f else 0f, tween(400), label = "molecule")
    val leafAlpha by animateFloatAsState(if (stage >= 3) 1f else 0f, tween(400), label = "leaf")
    val footprintAlpha by animateFloatAsState(if (stage >= 4) 1f else 0f, tween(400), label = "footprint")
    val circuitAlpha by animateFloatAsState(if (stage >= 5) 1f else 0f, tween(350), label = "circuit")
    val thermalWave by animateFloatAsState(if (stage >= 6) 1f else 0f, tween(500), label = "wave")
    val logoAlpha by animateFloatAsState(if (stage >= 7) 1f else 0f, tween(450), label = "logo")
    val taglineAlpha by animateFloatAsState(if (stage >= 8) 1f else 0f, tween(450), label = "tagline")

    LaunchedEffect(Unit) {
        val stepDelay = 260L
        for (i in 1..8) {
            delay(stepDelay)
            stage = i
        }
        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(ScientificDark),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Particle
            if (particleAlpha > 0f) {
                drawCircle(AIGlow.copy(alpha = particleAlpha * 0.9f), radius = 6f, center = center)
            }
            // 2. Molecular / cellulose lattice
            if (moleculeAlpha > 0f) {
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (Math.PI / 180f)
                    val p = Offset(center.x + 26 * kotlin.math.cos(angle).toFloat(), center.y + 26 * kotlin.math.sin(angle).toFloat())
                    drawLine(AIGlow.copy(alpha = moleculeAlpha * 0.7f), center, p, strokeWidth = 2f)
                    drawCircle(AIGlow.copy(alpha = moleculeAlpha), radius = 4f, center = p)
                }
            }
            // 3. Leaf silhouette
            if (leafAlpha > 0f) {
                drawOval(
                    color = SustainableGreen.copy(alpha = leafAlpha * 0.8f),
                    topLeft = Offset(center.x - 30f, center.y - 55f),
                    size = androidx.compose.ui.geometry.Size(60f, 90f)
                )
            }
            // 4. Footprint
            if (footprintAlpha > 0f) {
                drawOval(
                    color = AIGlow.copy(alpha = footprintAlpha),
                    topLeft = Offset(center.x - 22f, center.y - 10f),
                    size = androidx.compose.ui.geometry.Size(44f, 70f)
                )
                drawCircle(AIGlow.copy(alpha = footprintAlpha), radius = 14f, center = Offset(center.x, center.y - 60f))
            }
            // 5. Circuit traces
            if (circuitAlpha > 0f) {
                drawLine(SustainableGreen.copy(alpha = circuitAlpha), Offset(center.x - 40f, center.y), Offset(center.x - 20f, center.y), strokeWidth = 1.6f)
                drawLine(SustainableGreen.copy(alpha = circuitAlpha), Offset(center.x + 20f, center.y - 30f), Offset(center.x + 40f, center.y - 30f), strokeWidth = 1.6f)
            }
            // 6. Thermal wave sweep
            if (thermalWave > 0f) {
                val waveY = center.y + (thermalWave * 90f) - 45f
                drawLine(
                    Color(0xFFE6A23C).copy(alpha = (1f - thermalWave) * 0.8f),
                    Offset(center.x - 40f, waveY), Offset(center.x + 40f, waveY), strokeWidth = 2f
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ThermoHeal-AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEAF8F5),
                modifier = Modifier.alpha(logoAlpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Smart Foot Health.\nSustainable Materials.\nIntelligent Insights.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9AB5B1),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
