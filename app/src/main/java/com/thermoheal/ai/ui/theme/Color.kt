package com.thermoheal.ai.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ThermoHeal-AI Color System
 * "Green biotechnology meets intelligent healthcare."
 */

// --- Brand ---
val BioTealDeep = Color(0xFF0B4F4F)
val BioTealSecondary = Color(0xFF167C78)
val TechTeal = Color(0xFF20A6A0)
val AccentCyan = Color(0xFF4FD8D1)
val AIGlow = Color(0xFF63E6D8)

// --- Sustainability ---
val SustainableGreen = Color(0xFF39A96B)
val EcoGreen = Color(0xFF65C98A)
val DeepForest = Color(0xFF123B32)
val ScientificDark = Color(0xFF102A2E)

// --- Light Surfaces ---
val BgPrimaryLight = Color(0xFFF4FAF8)
val BgSecondaryLight = Color(0xFFEAF5F2)
val CardBgLight = Color(0xFFFFFFFF)
val SoftMint = Color(0xFFDDF3EC)
val BorderLight = Color(0xFFC8E2DD)

// --- Light Text ---
val TextPrimaryLight = Color(0xFF102426)
val TextSecondaryLight = Color(0xFF58706F)
val TextMutedLight = Color(0xFF809392)

// --- Status ---
val SuccessColor = Color(0xFF36A269)
val WarningColor = Color(0xFFE6A23C)
val DangerColor = Color(0xFFD9534F)
val InfoColor = Color(0xFF3296B8)

// --- Dark Mode (true scientific control-room palette, not simple inversion) ---
val BgPrimaryDark = Color(0xFF07191A)
val SurfaceDark = Color(0xFF0D2627)
val SurfaceElevatedDark = Color(0xFF123334)
val PrimaryDark = Color(0xFF42D1C8)
val SecondaryDark = Color(0xFF67E3A2)
val TextPrimaryDark = Color(0xFFEAF8F5)
val TextMutedDark = Color(0xFF9AB5B1)

/**
 * Scientific / semantic scales used by heatmaps and gauges throughout the app.
 * These are intentionally *not* generic rainbow scales — each maps to a
 * physically-intuitive low -> high progression per sensor modality.
 */
object SensorScale {
    // Pressure: cool teal (low) -> green/yellow (medium) -> orange/red (high)
    val pressureLow = AccentCyan
    val pressureMedium = EcoGreen
    val pressureMediumHigh = WarningColor
    val pressureHigh = DangerColor

    // Temperature: cyan/blue (cool) -> teal/green (normal) -> amber (warm) -> red (high)
    val tempCool = Color(0xFF3296B8)
    val tempNormal = TechTeal
    val tempWarm = WarningColor
    val tempHigh = DangerColor

    // Moisture: teal (low) -> blue (moderate) -> amber/red (high)
    val moistureLow = TechTeal
    val moistureModerate = InfoColor
    val moistureHigh = WarningColor
    val moistureVeryHigh = DangerColor

    // AI confidence: green (high) -> amber (moderate) -> gray (low)
    val confidenceHigh = SuccessColor
    val confidenceModerate = WarningColor
    val confidenceLow = Color(0xFF9AA5A4)
}
