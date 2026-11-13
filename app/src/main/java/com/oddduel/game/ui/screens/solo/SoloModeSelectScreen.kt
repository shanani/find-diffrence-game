package com.oddduel.game.ui.screens.solo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.components.rememberFitScale
import com.oddduel.game.ui.components.scaledBy
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonGreen
import com.oddduel.game.ui.theme.NeonPink
import com.oddduel.game.ui.theme.NeonYellow

@Composable
fun SoloModeSelectScreen(
    onBack: () -> Unit,
    onTimeAttack: () -> Unit,
    onReflexHunter: () -> Unit,
    onSurvivalStreak: () -> Unit,
    onMemoryFlash: () -> Unit
) {
    val scale = rememberFitScale()
    Column(
        Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(20.dp.scaledBy(scale)),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text("Solo Challenges", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp.scaledBy(scale))
        }
        Spacer(Modifier.height(16.dp.scaledBy(scale)))

        ModeCard("⏱️ Time Attack", "20 cards, 5 seconds each - how many can you solve?", NeonYellow, onTimeAttack, scale)
        Spacer(Modifier.height(12.dp.scaledBy(scale)))
        ModeCard("⚡ Reflex Hunter", "The odd one keeps jumping - tap it before it moves again", NeonPink, onReflexHunter, scale)
        Spacer(Modifier.height(12.dp.scaledBy(scale)))
        ModeCard("🔥 Survival Streak", "No clock, just don't miss - it gets harder every few rounds", NeonCyan, onSurvivalStreak, scale)
        Spacer(Modifier.height(12.dp.scaledBy(scale)))
        ModeCard("🧠 Memory Flash", "The grid flashes then goes blank - tap where the odd one was", NeonGreen, onMemoryFlash, scale)
    }
}

@Composable
private fun ModeCard(title: String, description: String, accent: Color, onClick: () -> Unit, scale: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgPanel)
            .clickable(onClick = onClick)
            .padding(16.dp.scaledBy(scale))
    ) {
        Text(title, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp.scaledBy(scale))
        Spacer(Modifier.height(4.dp.scaledBy(scale)))
        Text(description, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp.scaledBy(scale))
    }
}
