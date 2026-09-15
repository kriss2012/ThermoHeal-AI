package com.thermoheal.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = BioTealDeep,
    onPrimary = Color.White,
    secondary = TechTeal,
    onSecondary = Color.White,
    tertiary = SustainableGreen,
    background = BgPrimaryLight,
    onBackground = TextPrimaryLight,
    surface = CardBgLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SoftMint,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = DangerColor,
    onError = Color.White,
)

private val DarkScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = ScientificDark,
    secondary = SecondaryDark,
    onSecondary = ScientificDark,
    tertiary = AIGlow,
    background = BgPrimaryDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextMutedDark,
    outline = SurfaceElevatedDark,
    error = DangerColor,
    onError = Color.White,
)

/** Extra semantic colors not modeled by Material3's ColorScheme, exposed via CompositionLocal. */
data class ThermoHealExtendedColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val aiGlow: Color,
    val ecoGreen: Color,
    val textMuted: Color,
)

val LocalThermoHealColors = staticCompositionLocalOf {
    ThermoHealExtendedColors(
        success = SuccessColor, warning = WarningColor, danger = DangerColor,
        info = InfoColor, aiGlow = AIGlow, ecoGreen = EcoGreen, textMuted = TextMutedLight
    )
}

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun ThermoHealTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) DarkScheme else LightScheme
    val extended = if (useDark) {
        ThermoHealExtendedColors(SuccessColor, WarningColor, DangerColor, InfoColor, AIGlow, EcoGreen, TextMutedDark)
    } else {
        ThermoHealExtendedColors(SuccessColor, WarningColor, DangerColor, InfoColor, AIGlow, EcoGreen, TextMutedLight)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalThermoHealColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ThermoHealTypography,
            shapes = ThermoHealShapes,
            content = content
        )
    }
}

/** Convenience accessor: MaterialTheme.thermoColors.success etc. */
val MaterialTheme.thermoColors: ThermoHealExtendedColors
    @Composable get() = LocalThermoHealColors.current
