package com.oddduel.game.ui.screens.solo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.ui.components.OddGrid
import com.oddduel.game.ui.theme.BgDeep

@Composable
fun SurvivalStreakScreen(audio: GameAudio, onBack: () -> Unit) {
    var playCount by remember { mutableIntStateOf(0) }
    val vm: SurvivalStreakViewModel = viewModel(
        key = "survival_streak_$playCount",
        factory = SurvivalStreakViewModel.factoryFor(audio)
    )

    if (vm.gameOver) {
        SoloResultScreen(
            modeName = "Survival Streak",
            scoreLabel = "Streak",
            score = vm.streak,
            subtitle = "Grid reached ${vm.gridCols}x${vm.gridRows}",
            onPlayAgain = { playCount += 1 },
            onBackToModes = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().background(BgDeep)) {
        SoloTopBar(title = "Survival Streak", score = vm.streak, onBack = onBack)
        Text(
            "No clock - one mistake ends it. Gets harder as you go.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        ShakeBox(pulse = vm.wrongPulse, modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            vm.challenge?.let { challenge ->
                OddGrid(challenge = challenge, enabled = true, onTap = { vm.onCellTapped(it) }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
