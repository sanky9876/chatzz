package com.chatzz.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WhatsAppGreen = Color(0xFF075E54)
val WhatsAppLightGreen = Color(0xFF25D366)
val WhatsAppBlue = Color(0xFF34B7F1)
val WhatsAppGray = Color(0xFFECE5DD)
val WhatsAppTeal = Color(0xFF128C7E)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppTeal,
    secondary = WhatsAppLightGreen,
    tertiary = WhatsAppBlue,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreen,
    secondary = WhatsAppTeal,
    tertiary = WhatsAppBlue,
    background = Color(0xFF121B22),
    surface = Color(0xFF1F2C34),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun ChatzzTheme(
    darkTheme: Boolean = false, // You can toggle this based on system settings
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
