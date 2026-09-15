package com.thermoheal.ai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thermoheal.ai.presentation.about.AboutScreen
import com.thermoheal.ai.presentation.auth.LoginScreen
import com.thermoheal.ai.presentation.auth.SignupScreen
import com.thermoheal.ai.presentation.dashboard.HomeScreen
import com.thermoheal.ai.presentation.device.DeviceScreen
import com.thermoheal.ai.presentation.gait.GaitScreen
import com.thermoheal.ai.presentation.history.HistoryScreen
import com.thermoheal.ai.presentation.history.WeeklyReportScreen
import com.thermoheal.ai.presentation.insights.InsightsScreen
import com.thermoheal.ai.presentation.live_monitor.LiveMonitorScreen
import com.thermoheal.ai.presentation.moisture.MoistureScreen
import com.thermoheal.ai.presentation.notifications.NotificationsScreen
import com.thermoheal.ai.presentation.onboarding.OnboardingScreen
import com.thermoheal.ai.presentation.pressure.PressureScreen
import com.thermoheal.ai.presentation.privacy.PrivacyScreen
import com.thermoheal.ai.presentation.profile.ProfileScreen
import com.thermoheal.ai.presentation.research.BiomaterialScreen
import com.thermoheal.ai.presentation.research.PresentationModeScreen
import com.thermoheal.ai.presentation.research.ResearchScreen
import com.thermoheal.ai.presentation.research.ThermoregulationScreen
import com.thermoheal.ai.presentation.settings.SettingsScreen
import com.thermoheal.ai.presentation.setup.CalibrationScreen
import com.thermoheal.ai.presentation.setup.DeviceSetupScreen
import com.thermoheal.ai.presentation.setup.ProfileSetupScreen
import com.thermoheal.ai.presentation.splash.SplashScreen
import com.thermoheal.ai.presentation.sustainability.SustainabilityScreen
import com.thermoheal.ai.presentation.temperature.TemperatureScreen
import com.thermoheal.ai.ui.components.ThermoHealBottomBar
import kotlinx.coroutines.launch

@Composable
fun ThermoHealNavGraph(rootViewModel: RootViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    val showBottomBar = Screen.bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ThermoHealBottomBar(currentRoute = currentRoute) { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = {
                    scope.launch {
                        val onboardingDone = rootViewModel.onboardingComplete.value
                        val loggedIn = rootViewModel.isLoggedIn()
                        val destination = when {
                            !onboardingDone -> Screen.Onboarding.route
                            !loggedIn -> Screen.Login.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                })
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinished = {
                    rootViewModel.markOnboardingComplete()
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onContinueAsDemo = {
                        navController.navigate(Screen.DeviceSetup.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = { navController.navigate(Screen.ProfileSetup.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(onContinue = { navController.navigate(Screen.DeviceSetup.route) { popUpTo(Screen.ProfileSetup.route) { inclusive = true } } })
            }

            composable(Screen.DeviceSetup.route) {
                DeviceSetupScreen(
                    onChooseDemo = { navController.navigate(Screen.Calibration.route) },
                    onChooseRealDevice = { navController.navigate(Screen.Calibration.route) }
                )
            }

            composable(Screen.Calibration.route) {
                CalibrationScreen(onFinished = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.DeviceSetup.route) { inclusive = true } }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenPressure = { navController.navigate(Screen.Pressure.route) },
                    onOpenTemperature = { navController.navigate(Screen.Temperature.route) },
                    onOpenMoisture = { navController.navigate(Screen.Moisture.route) },
                    onOpenGait = { navController.navigate(Screen.Gait.route) },
                    onOpenDevice = { navController.navigate(Screen.Device.route) },
                    onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                    onOpenInsights = { navController.navigate(Screen.Insights.route) },
                    onOpenSustainability = { navController.navigate(Screen.Sustainability.route) },
                    onOpenResearch = { navController.navigate(Screen.Research.route) }
                )
            }

            composable(Screen.Live.route) { LiveMonitorScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenPrivacy = { navController.navigate(Screen.Privacy.route) },
                    onOpenDevice = { navController.navigate(Screen.Device.route) },
                    onLoggedOut = { navController.navigate(Screen.Login.route) { popUpTo(0) } }
                )
            }

            composable(Screen.Pressure.route) { PressureScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Temperature.route) { TemperatureScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Moisture.route) { MoistureScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Gait.route) { GaitScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Device.route) { DeviceScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Sustainability.route) { SustainabilityScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Research.route) {
                ResearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenBiomaterial = { navController.navigate(Screen.Biomaterial.route) },
                    onOpenThermoregulation = { navController.navigate(Screen.Thermoregulation.route) },
                    onOpenPresentationMode = { navController.navigate(Screen.PresentationMode.route) }
                )
            }
            composable(Screen.Biomaterial.route) { BiomaterialScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Thermoregulation.route) { ThermoregulationScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.PresentationMode.route) { PresentationModeScreen(onExit = { navController.popBackStack() }) }
            composable(Screen.Settings.route) { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Privacy.route) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Notifications.route) { NotificationsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.WeeklyReport.route) { WeeklyReportScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
