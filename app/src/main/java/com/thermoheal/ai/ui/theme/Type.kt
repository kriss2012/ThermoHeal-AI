package com.thermoheal.ai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ThermoHeal-AI Typography
 * Primary: Inter (falls back to platform default until font files are added
 * under res/font — see README "Fonts" section).
 */
val ThermoHealFontFamily = FontFamily.Default // swap for Inter/Manrope FontFamily once .ttf assets are added

val ThermoHealTypography = Typography(
    // Display — 32-40sp Bold
    displayLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp),
    displayMedium = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
    displaySmall = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),

    // Screen title — 26-30sp Bold
    headlineLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),

    // Section title — 20-24sp SemiBold
    titleLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    // Card title — 16-18sp SemiBold
    titleSmall = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),

    // Body — 14-16sp
    bodyLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    // Supporting text — 12-14sp
    bodySmall = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),

    labelLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

/** Metric numbers — 28-42sp Bold, used by MetricCard / FootHealthScore */
val MetricNumberStyle = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 40.sp)
val MetricNumberStyleLarge = TextStyle(fontFamily = ThermoHealFontFamily, fontWeight = FontWeight.Bold, fontSize = 42.sp, lineHeight = 46.sp)
