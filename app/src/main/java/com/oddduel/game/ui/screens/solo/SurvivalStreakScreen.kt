package com.oddduel.game.ui.screens.solo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.analytics.DifficultyAnalytics
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.engine.GamePauseController
import com.oddduel.game.ui.components.OddGrid
import com.oddduel.game.ui.components.TrackGameActive
import com.oddduel.game.ui.theme.rememberAuroraBrush

@Composable
fun SurvivalStreakScreen(audio: GameAudio, pauseController: GamePauseController, onBack: () -> Unit) {
    var playCount by remember { mutableIntStateOf(0) }
    val vm: SurvivalStreakViewModel = viewModel(
        key = "survival_streak_$playCount",
        factory = SurvivalStreakViewModel.factoryFor(audio)
    )
    TrackGameActive(pauseController, active = !vm.gameOver)

    val context = LocalContext.current
    LaunchedEffect(vm.gameOver) {
        if (vm.gameOver) {
            DifficultyAnalytics.reportSession(context, "survival_streak", vm.elapsedMs, vm.streak + 1, vm.streak)
        }
    }

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

    Column(Modifier.fillMaxSize().background(rememberAuroraBrush())) {
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
