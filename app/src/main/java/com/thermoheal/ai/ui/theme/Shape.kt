package com.thermoheal.ai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ThermoHealShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),   // standard card radius
    large = RoundedCornerShape(20.dp),    // hero card radius
    extraLarge = RoundedCornerShape(28.dp)
)

// Named tokens matching spec section 50 (Cards: 16-22dp radius, 1dp border, 16-20dp padding)
object ThermoHealDimens {
    val CardRadius = 18.dp
    val HeroCardRadius = 22.dp
    val CardBorderWidth = 1.dp
    val CardPadding = 18.dp
    val ScreenPadding = 20.dp
    val MinTouchTarget = 48.dp
}
