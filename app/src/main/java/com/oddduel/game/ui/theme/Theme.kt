package com.oddduel.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonPink = Color(0xFFFF4FA3)
val NeonCyan = Color(0xFF3DE7E0)
val NeonYellow = Color(0xFFFFD23F)
val NeonGreen = Color(0xFF4CE07A)
val NeonPurple = Color(0xFF8A5BFF)
val BgDeep = Color(0xFF0E0A1F)
val BgPanel = Color(0xFF191433)
val BgPanelLight = Color(0xFF241D4A)
val PlayerLeftAccent = Color(0xFF3DE7E0)
val PlayerRightAccent = Color(0xFFFF4FA3)
val DangerRed = Color(0xFFFF4D4D)

private val OddDuelColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonPink,
    background = BgDeep,
    surface = BgPanel,
    error = DangerRed,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun OddDuelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OddDuelColorScheme,
        typography = OddDuelTypography,
        content = content
    )
}
