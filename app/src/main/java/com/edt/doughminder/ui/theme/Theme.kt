package com.edt.doughminder.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Claude-inspired warm dark palette
val Ink = Color(0xFF262624)          // background
val InkRaised = Color(0xFF30302E)    // cards / surfaces
val InkBorder = Color(0xFF3E3E3A)
val Cream = Color(0xFFF0EEE6)        // primary text
val CreamDim = Color(0xFFA6A39A)     // secondary text
val Coral = Color(0xFFD97757)        // accent
val CoralDeep = Color(0xFFB85C3E)
val Sage = Color(0xFF7D9B76)         // "fed & happy"
val Amber = Color(0xFFD4A257)        // "getting hungry"

// Jar dough tints the user can pick per starter
val StarterPalette = listOf(
    Color(0xFFE8C9A0), // classic wheat
    Color(0xFFD9B08C), // rye
    Color(0xFFC9CBB8), // spelt
    Color(0xFFE3B7A0), // apricot
)

private val DoughColors = darkColorScheme(
    primary = Coral,
    onPrimary = Ink,
    primaryContainer = Coral,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF4A3A32),   // muted coral-brown for selection pills
    onSecondaryContainer = Cream,
    tertiary = Amber,
    onTertiary = Ink,
    background = Ink,
    onBackground = Cream,
    surface = InkRaised,
    onSurface = Cream,
    surfaceVariant = InkRaised,
    onSurfaceVariant = CreamDim,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = InkRaised,
    surfaceContainerHighest = Color(0xFF3A3A37),
    outline = InkBorder,
    outlineVariant = InkBorder,
    error = Color(0xFFE07A6B),
)

private val Serif = FontFamily.Serif

private val DoughType = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 32.sp, color = Cream),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

private val DoughShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun DoughminderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DoughColors,
        typography = DoughType,
        shapes = DoughShapes,
        content = content,
    )
}
