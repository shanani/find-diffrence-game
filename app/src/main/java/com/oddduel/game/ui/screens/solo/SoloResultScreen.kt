package com.oddduel.game.ui.screens.solo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.components.rememberFitScale
import com.oddduel.game.ui.components.scaledBy
import com.oddduel.game.ui.theme.GameButtonFont
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonYellow
import com.oddduel.game.ui.theme.rememberAuroraBrush

@Composable
fun SoloResultScreen(
    modeName: String,
    scoreLabel: String,
    score: Int,
    subtitle: String? = null,
    onPlayAgain: () -> Unit,
    onBackToModes: () -> Unit
) {
    val scale = rememberFitScale()
    Box(Modifier.fillMaxSize().background(rememberAuroraBrush()), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp.scaledBy(scale)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(modeName, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp.scaledBy(scale))
            Spacer(Modifier.size(8.dp.scaledBy(scale)))
            Text("GAME OVER", color = Color.White, fontFamily = GameButtonFont, fontSize = 24.sp.scaledBy(scale))
            Spacer(Modifier.size(20.dp.scaledBy(scale)))
            Text(scoreLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp.scaledBy(scale))
            Text(score.toString(), color = NeonYellow, fontFamily = GameButtonFont, fontSize = 48.sp.scaledBy(scale))
            if (subtitle != null) {
                Spacer(Modifier.size(6.dp.scaledBy(scale)))
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp.scaledBy(scale))
            }
            Spacer(Modifier.size(28.dp.scaledBy(scale)))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp.scaledBy(scale))) {
                Button(onClick = onPlayAgain) { Text("Play Again") }
                OutlinedButton(
                    onClick = onBackToModes,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Back to Modes") }
            }
        }
    }
}
