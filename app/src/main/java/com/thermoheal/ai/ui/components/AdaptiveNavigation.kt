package com.thermoheal.ai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thermoheal.ai.R
import com.thermoheal.ai.navigation.Screen
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass

/**
 * Navigation items for primary phone bottom navigation (5 items)
 */
val primaryNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Live, "Live", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    BottomNavItem(Screen.Insights, "Insights", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem(Screen.History, "History", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * Expanded navigation items for tablet/foldable NavigationRail (Section 22)
 */
val tabletRailNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Live, "Live", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    BottomNavItem(Screen.Insights, "Insights", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem(Screen.History, "History", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Device, "Device", Icons.Filled.Bluetooth, Icons.Outlined.Bluetooth),
    BottomNavItem(Screen.Research, "Research", Icons.Filled.Science, Icons.Outlined.Science),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * Tablet / Foldable / Landscape Navigation Rail (Section 22)
 */
@Composable
fun ThermoHealNavigationRail(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.widthIn(min = 72.dp, max = 84.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_thermoheal_logo),
                    contentDescription = "ThermoHeal-AI Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    ) {
        Spacer(Modifier.weight(1f))
        tabletRailNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                alwaysShowLabel = true
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

/**
 * Adaptive layout scaffolding: renders BottomBar on Compact screens,
 * and NavigationRail on Medium/Expanded/Landscape screens.
 */
@Composable
fun ThermoHealAdaptiveScaffold(
    currentRoute: String?,
    showNavigation: Boolean,
    onNavigate: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val windowSize = LocalWindowSizeInfo.current
    val isRail = showNavigation && windowSize.widthClass != WindowWidthClass.COMPACT

    if (isRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            ThermoHealNavigationRail(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                content(PaddingValues(0.dp))
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showNavigation) {
                    ThermoHealBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            }
        ) { padding ->
            content(padding)
        }
    }
}
