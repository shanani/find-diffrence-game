package com.oddduel.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// FontFamily.Cursive resolves to Android's built-in "casual" system font alias - a
// rounded, playful display face that ships on every device, so every button gets a
// game-arcade feel with no font file to bundle and no network fetch required.
val GameButtonFont = FontFamily.Cursive

val OddDuelTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 40.sp, letterSpacing = 0.5.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    // Button/OutlinedButton default their text to labelLarge, so this one change
    // gives every button app-wide the playful font without touching each screen.
    labelLarge = TextStyle(
        fontFamily = GameButtonFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.6.sp
    )
)
