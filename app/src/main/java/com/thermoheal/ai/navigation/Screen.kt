package com.thermoheal.ai.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ProfileSetup : Screen("profile_setup")
    object DeviceSetup : Screen("device_setup")
    object Calibration : Screen("calibration")

    // Bottom nav destinations
    object Home : Screen("home")
    object Live : Screen("live")
    object Insights : Screen("insights")
    object History : Screen("history")
    object Profile : Screen("profile")

    // Secondary destinations
    object Pressure : Screen("pressure")
    object Temperature : Screen("temperature")
    object Moisture : Screen("moisture")
    object Gait : Screen("gait")
    object Device : Screen("device")
    object Sustainability : Screen("sustainability")
    object Research : Screen("research")
    object Biomaterial : Screen("biomaterial")
    object Thermoregulation : Screen("thermoregulation")
    object PresentationMode : Screen("presentation_mode")
    object Settings : Screen("settings")
    object Privacy : Screen("privacy")
    object About : Screen("about")
    object Notifications : Screen("notifications")
    object WeeklyReport : Screen("weekly_report")

    companion object {
        val bottomNavScreens = listOf(Home, Live, Insights, History, Profile)
    }
}
