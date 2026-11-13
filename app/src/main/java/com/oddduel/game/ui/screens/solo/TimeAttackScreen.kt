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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.analytics.DifficultyAnalytics
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.engine.GamePauseController
import com.oddduel.game.ui.components.FancyCountdownTimer
import com.oddduel.game.ui.components.OddGrid
import com.oddduel.game.ui.components.TrackGameActive
import com.oddduel.game.ui.theme.BgDeep

@Composable
fun TimeAttackScreen(audio: GameAudio, pauseController: GamePauseController, onBack: () -> Unit) {
    var playCount by remember { mutableIntStateOf(0) }
    val vm: TimeAttackViewModel = viewModel(
        key = "time_attack_$playCount",
        factory = TimeAttackViewModel.factoryFor(audio, pauseController)
    )
    TrackGameActive(pauseController, active = !vm.gameOver)

    val context = LocalContext.current
    LaunchedEffect(vm.gameOver) {
        if (vm.gameOver) {
            DifficultyAnalytics.reportSession(context, "time_attack", vm.elapsedMs, TimeAttackViewModel.DECK_SIZE, vm.correctCount)
        }
    }

    if (vm.gameOver) {
        SoloResultScreen(
            modeName = "Time Attack",
            scoreLabel = "Cards solved",
            score = vm.correctCount,
            subtitle = "out of ${TimeAttackViewModel.DECK_SIZE}",
            onPlayAgain = { playCount += 1 },
            onBackToModes = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().background(BgDeep)) {
        SoloTopBar(title = "Time Attack", score = vm.correctCount, onBack = onBack)
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Card ${vm.cardsPlayed + 1} / ${TimeAttackViewModel.DECK_SIZE}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            FancyCountdownTimer(secondsLeft = vm.timeLeft, totalSeconds = TimeAttackViewModel.PER_CARD_SECONDS)
        }
        ShakeBox(pulse = vm.wrongPulse, modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            vm.challenge?.let { challenge ->
                OddGrid(challenge = challenge, enabled = true, onTap = { vm.onCellTapped(it) }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
