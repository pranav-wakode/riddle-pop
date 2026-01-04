package com.example.neonpuzzle.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// "Sunny Pop" Palette - Optimized for Engagement
val PopBackgroundStart = Color(0xFFFFF0E6) // Soft Peach
val PopBackgroundEnd = Color(0xFFE0F7FA)   // Soft Sky

val PrimaryAction = Color(0xFFFF6F61) // Coral / Energetic Red
val SecondaryAction = Color(0xFF4DB6AC) // Teal / Calming Green
val AccentYellow = Color(0xFFFFD54F) // Sunny Yellow
val TextPrimary = Color(0xFF2D3436) // Soft Charcoal (Not harsh black)
val TextSecondary = Color(0xFF636E72)
val CardBackground = Color(0xFFFFFFFF)

val SuccessGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF5252)

// A warm, engaging gradient for the background
val MainBackgroundBrush = Brush.verticalGradient(
    colors = listOf(PopBackgroundStart, PopBackgroundEnd)
)