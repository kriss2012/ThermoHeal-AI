package com.thermoheal.ai.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.navigation.Screen

data class BottomNavItem(val screen: Screen, val label: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Live, "Live", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    BottomNavItem(Screen.Insights, "Insights", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem(Screen.History, "History", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun ThermoHealBottomBar(currentRoute: String?, onNavigate: (Screen) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(if (selected) item.filledIcon else item.outlinedIcon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}
