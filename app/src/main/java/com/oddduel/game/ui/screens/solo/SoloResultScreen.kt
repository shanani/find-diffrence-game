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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonYellow

@Composable
fun SoloResultScreen(
    modeName: String,
    scoreLabel: String,
    score: Int,
    subtitle: String? = null,
    onPlayAgain: () -> Unit,
    onBackToModes: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(BgDeep), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(modeName, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.size(8.dp))
            Text("GAME OVER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Spacer(Modifier.size(20.dp))
            Text(scoreLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            Text(score.toString(), color = NeonYellow, fontWeight = FontWeight.Black, fontSize = 56.sp)
            if (subtitle != null) {
                Spacer(Modifier.size(6.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Spacer(Modifier.size(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlayAgain) { Text("Play Again") }
                OutlinedButton(
                    onClick = onBackToModes,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Back to Modes") }
            }
        }
    }
}
