package com.thermoheal.ai.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermoheal.ai.R
import com.thermoheal.ai.ui.theme.AIGlow
import com.thermoheal.ai.ui.theme.SustainableGreen
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity animated splash screen highlighting the official ThermoHeal-AI logo,
 * bio-nanocellulose molecular lattice awakening, animated ECG heartbeat pulse,
 * and thermal wave radiance.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var stage by remember { mutableStateOf(0) }

    // Ambient pulsing glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Staged entrance animations
    val latticeAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "lattice_alpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = when {
            stage >= 2 -> 1f
            stage == 1 -> 0.75f
            else -> 0.4f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )

    val ecgProgress by animateFloatAsState(
        targetValue = if (stage >= 3) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "ecg_progress"
    )

    val thermalPulseRadius by animateFloatAsState(
        targetValue = if (stage >= 3) 70f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "thermal_pulse"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (stage >= 4) 1f else 0f,
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "text_alpha"
    )

    val systemStatusAlpha by animateFloatAsState(
        targetValue = if (stage >= 5) 1f else 0f,
        animationSpec = tween(400, easing = LinearOutSlowInEasing),
        label = "status_alpha"
    )

    LaunchedEffect(Unit) {
        delay(250)
        stage = 1 // Awakening molecular lattice
        delay(450)
        stage = 2 // Official Emblem pop & halo
        delay(550)
        stage = 3 // ECG & thermal wave sweep
        delay(500)
        stage = 4 // Typography & tagline
        delay(400)
        stage = 5 // Neural core status
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF073832),
                        Color(0xFF03211E),
                        Color(0xFF011412)
                    ),
                    center = Offset.Unspecified,
                    radius = 1600f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background decorative elements: Nanocellulose hexagonal lattice & ambient particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            if (latticeAlpha > 0f) {
                // Outer orbital ambient ring
                drawCircle(
                    color = AIGlow.copy(alpha = 0.08f * latticeAlpha * pulseGlow),
                    radius = 210.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = SustainableGreen.copy(alpha = 0.12f * latticeAlpha),
                    radius = 160.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Cellulose hexagonal nodes
                val nodeCount = 6
                for (i in 0 until nodeCount) {
                    val angle = (i * 60f) * (Math.PI / 180f)
                    val radius = 170.dp.toPx()
                    val nodePos = Offset(
                        center.x + (radius * cos(angle)).toFloat(),
                        center.y + (radius * sin(angle)).toFloat()
                    )
                    // Connecting fiber lines
                    drawLine(
                        color = AIGlow.copy(alpha = 0.15f * latticeAlpha),
                        start = center,
                        end = nodePos,
                        strokeWidth = 1.dp.toPx()
                    )
                    // Node circle
                    drawCircle(
                        color = SustainableGreen.copy(alpha = 0.35f * latticeAlpha),
                        radius = 4.dp.toPx(),
                        center = nodePos
                    )
                }
            }

            // 2. Animated ECG waveform line sweep across the bottom of the logo
            if (ecgProgress > 0f) {
                val ecgStartX = center.x - 130.dp.toPx()
                val ecgEndX = center.x + 130.dp.toPx()
                val currentX = ecgStartX + ((ecgEndX - ecgStartX) * ecgProgress)
                val ecgY = center.y + 115.dp.toPx()

                val path = Path().apply {
                    moveTo(ecgStartX, ecgY)
                    // Normal baseline
                    val p1 = center.x - 50.dp.toPx()
                    val p2 = center.x - 30.dp.toPx()
                    val p3 = center.x - 10.dp.toPx()
                    val p4 = center.x + 10.dp.toPx()
                    val p5 = center.x + 30.dp.toPx()

                    if (currentX > p1) lineTo(p1, ecgY)
                    if (currentX > p2) lineTo(p2, ecgY - 6.dp.toPx()) // P wave
                    if (currentX > p3) lineTo(p3, ecgY + 12.dp.toPx()) // Q dip
                    if (currentX > p4) lineTo(p4, ecgY - 26.dp.toPx()) // R spike
                    if (currentX > p5) lineTo(p5, ecgY + 10.dp.toPx()) // S dip
                    if (currentX > p5 + 20.dp.toPx()) lineTo(p5 + 20.dp.toPx(), ecgY - 8.dp.toPx()) // T wave
                    lineTo(currentX, ecgY)
                }

                drawPath(
                    path = path,
                    color = AIGlow.copy(alpha = 0.85f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Glowing leading spark on ECG wave
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(currentX, ecgY)
                )
            }

            // 3. Thermal hotspot expansion wave from heel center
            if (thermalPulseRadius > 0f) {
                val heelCenter = Offset(center.x, center.y + 40.dp.toPx())
                drawCircle(
                    color = Color(0xFFFF7043).copy(alpha = (1f - (thermalPulseRadius / 70f)) * 0.45f),
                    radius = thermalPulseRadius.dp.toPx(),
                    center = heelCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center Hero: Official Logo with Glassmorphic Halo Glow
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Soft background radial aura
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(1.05f + (pulseGlow * 0.08f))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AIGlow.copy(alpha = 0.35f * pulseGlow),
                                    SustainableGreen.copy(alpha = 0.15f * pulseGlow),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Official Logo Emblem
                Image(
                    painter = painterResource(id = R.drawable.ic_thermoheal_logo),
                    contentDescription = "ThermoHeal-AI Official Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(44.dp))
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(44.dp),
                            spotColor = AIGlow.copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    AIGlow.copy(alpha = 0.6f),
                                    SustainableGreen.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(44.dp)
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(36.dp))

            // Official Typography & Tagline
            Column(
                modifier = Modifier.alpha(textAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ThermoHeal-AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFFEAF8F5)
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(SustainableGreen.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "  HEALTHY STEPS AHEAD  ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF63E6D8)
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(SustainableGreen.copy(alpha = 0.7f))
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Banana-Nanocellulose Biomaterial • Smart Insole System",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8FAFA9),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom: Status and System Readiness
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .alpha(systemStatusAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AIGlow, shape = CircleShape)
                )
                Text(
                    text = "Intelligent Diagnostic Platform Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7FA8A2)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Subtle medical disclaimer footer
            Text(
                text = "Research & Wellness Prototype • Not a Medical Diagnosis",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color(0xFF557771)
            )
        }
    }
}
