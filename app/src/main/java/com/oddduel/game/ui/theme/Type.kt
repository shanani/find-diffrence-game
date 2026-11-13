package com.oddduel.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.oddduel.game.R

// Bungee (SIL Open Font License, bundled as res/font/bungee_regular.ttf) - a chunky,
// blocky arcade-sign display face, a much stronger "game" identity than any built-in
// system font alias. Used for every headline/title/button label app-wide.
val GameButtonFont = FontFamily(Font(R.font.bungee_regular))

val OddDuelTypography = Typography(
    headlineLarge = TextStyle(fontFamily = GameButtonFont, fontSize = 36.sp, letterSpacing = 0.5.sp),
    headlineMedium = TextStyle(fontFamily = GameButtonFont, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = GameButtonFont, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    // Button/OutlinedButton default their text to labelLarge, so this one change
    // gives every button app-wide the arcade font without touching each screen.
    labelLarge = TextStyle(
        fontFamily = GameButtonFont,
        fontSize = 15.sp,
        letterSpacing = 0.6.sp
    )
)
