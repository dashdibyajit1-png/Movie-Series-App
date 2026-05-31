package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NetflixRed,
    secondary = PrimeBlue,
    tertiary = CinemaAccent,
    background = CinemaBlack,
    surface = CinemaDarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF26303C),
    onSurfaceVariant = Color(0xFFCFD8DC)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightOnSurface, // Off-black as the main branding color in Editorial
    secondary = PrimeBlue,
    tertiary = CinemaAccent,
    background = LightSurface,
    surface = LightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceVariant = EditorialLightGray,
    onSurfaceVariant = EditorialGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to gorgeous editorial light mode (Paper Cream/Off-black)
  dynamicColor: Boolean = false, // Disable dynamic colors so our intentional platform branding shines!
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
