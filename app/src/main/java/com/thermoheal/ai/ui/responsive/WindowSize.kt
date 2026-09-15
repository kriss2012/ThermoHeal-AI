package com.thermoheal.ai.ui.responsive

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard Android Material 3 Window Width Size Classes
 */
enum class WindowWidthClass {
    COMPACT,   // < 600dp (phones)
    MEDIUM,    // 600dp - 839dp (small tablets, foldables, landscape phones)
    EXPANDED   // >= 840dp (large tablets, desktop-class Android)
}

/**
 * Standard Android Material 3 Window Height Size Classes
 */
enum class WindowHeightClass {
    COMPACT,   // < 480dp (landscape phones)
    MEDIUM,    // 480dp - 899dp (standard portrait phones & tablets)
    EXPANDED   // >= 900dp (tall tablets, desktop-class Android)
}

/**
 * Rich responsive device state encapsulation
 */
data class WindowSizeInfo(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val widthDp: Dp,
    val heightDp: Dp,
    val isLandscape: Boolean,
    val isSmallPhone: Boolean,      // width < 360dp (down to 320dp)
    val isTabletOrFoldable: Boolean // width >= 600dp
) {
    /**
     * Recommended screen-level content padding adhering to section 65
     */
    val contentPadding: PaddingValues
        get() = when (widthClass) {
            WindowWidthClass.COMPACT -> PaddingValues(if (isSmallPhone) 12.dp else 16.dp)
            WindowWidthClass.MEDIUM -> PaddingValues(horizontal = 20.dp, vertical = 18.dp)
            WindowWidthClass.EXPANDED -> PaddingValues(horizontal = 28.dp, vertical = 24.dp)
        }

    /**
     * Dynamic columns for metric grids adhering to section 28 & 112
     */
    val metricGridColumns: Int
        get() = when {
            widthClass == WindowWidthClass.EXPANDED -> 4
            widthClass == WindowWidthClass.MEDIUM -> 3
            isSmallPhone -> 1
            else -> 2
        }

    /**
     * Responsive chart height adhering to section 32
     */
    val chartHeightDp: Int
        get() = when (widthClass) {
            WindowWidthClass.COMPACT -> if (isLandscape) 160 else 190
            WindowWidthClass.MEDIUM -> 240
            WindowWidthClass.EXPANDED -> 280
        }
}

val LocalWindowSizeInfo = staticCompositionLocalOf<WindowSizeInfo> {
    WindowSizeInfo(
        widthClass = WindowWidthClass.COMPACT,
        heightClass = WindowHeightClass.MEDIUM,
        widthDp = 360.dp,
        heightDp = 800.dp,
        isLandscape = false,
        isSmallPhone = false,
        isTabletOrFoldable = false
    )
}

/**
 * Calculates WindowSizeInfo reactively from LocalConfiguration
 */
@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val widthClass = when {
        configuration.screenWidthDp < 600 -> WindowWidthClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }

    val heightClass = when {
        configuration.screenHeightDp < 480 -> WindowHeightClass.COMPACT
        configuration.screenHeightDp < 900 -> WindowHeightClass.MEDIUM
        else -> WindowHeightClass.EXPANDED
    }

    return remember(configuration.screenWidthDp, configuration.screenHeightDp, isLandscape) {
        WindowSizeInfo(
            widthClass = widthClass,
            heightClass = heightClass,
            widthDp = screenWidth,
            heightDp = screenHeight,
            isLandscape = isLandscape,
            isSmallPhone = configuration.screenWidthDp < 360,
            isTabletOrFoldable = configuration.screenWidthDp >= 600
        )
    }
}

/**
 * Modifier to clamp wide content to a maximum readable width (e.g. 840dp)
 * and center it horizontally on tablets and desktops (Section 66).
 */
fun Modifier.readableContentWidth(maxDp: Dp = 840.dp): Modifier = composed {
    val density = LocalDensity.current
    val maxPx = with(density) { maxDp.roundToPx() }

    this.layout { measurable, constraints ->
        val targetWidth = constraints.maxWidth.coerceAtMost(maxPx)
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = constraints.minWidth.coerceAtMost(targetWidth),
                maxWidth = targetWidth
            )
        )
        val horizontalOffset = (constraints.maxWidth - placeable.width) / 2

        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(horizontalOffset, 0)
        }
    }
}

/**
 * Helper to safely extract Activity from any Context
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
