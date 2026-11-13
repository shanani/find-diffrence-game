package com.oddduel.game.ui.screens.solo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.engine.GamePauseController
import com.oddduel.game.ui.components.OddGrid
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.NeonYellow

@Composable
fun ReflexHunterScreen(audio: GameAudio, pauseController: GamePauseController, onBack: () -> Unit) {
    var playCount by remember { mutableIntStateOf(0) }
    val vm: ReflexHunterViewModel = viewModel(
        key = "reflex_hunter_$playCount",
        factory = ReflexHunterViewModel.factoryFor(audio, pauseController)
    )

    if (vm.gameOver) {
        SoloResultScreen(
            modeName = "Reflex Hunter",
            scoreLabel = "Hops survived",
            score = vm.streak,
            onPlayAgain = { playCount += 1 },
            onBackToModes = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().background(BgDeep)) {
        SoloTopBar(title = "Reflex Hunter", score = vm.streak, onBack = onBack)
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ReflexTimerBar(intervalMs = vm.intervalMs, hopToken = vm.hopToken)
        }
        ShakeBox(pulse = vm.wrongPulse, modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            vm.challenge?.let { challenge ->
                OddGrid(challenge = challenge, enabled = true, onTap = { vm.onCellTapped(it) }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ReflexTimerBar(intervalMs: Long, hopToken: Int) {
    val progress = remember { Animatable(1f) }
    LaunchedEffect(hopToken) {
        progress.snapTo(1f)
        progress.animateTo(0f, animationSpec = tween(intervalMs.toInt(), easing = LinearEasing))
    }
    Text("Find it fast - it's about to jump!", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(5.dp))
                .background(if (progress.value < 0.25f) DangerRed else NeonYellow)
        )
    }
}
