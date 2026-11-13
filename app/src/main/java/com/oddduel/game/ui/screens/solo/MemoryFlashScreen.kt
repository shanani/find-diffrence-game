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
import androidx.compose.ui.text.font.FontWeight
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
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonYellow

@Composable
fun MemoryFlashScreen(audio: GameAudio, pauseController: GamePauseController, onBack: () -> Unit) {
    var playCount by remember { mutableIntStateOf(0) }
    val vm: MemoryFlashViewModel = viewModel(
        key = "memory_flash_$playCount",
        factory = MemoryFlashViewModel.factoryFor(audio, pauseController)
    )
    TrackGameActive(pauseController, active = !vm.gameOver)

    val context = LocalContext.current
    LaunchedEffect(vm.gameOver) {
        if (vm.gameOver) {
            DifficultyAnalytics.reportSession(context, "memory_flash", vm.elapsedMs, vm.streak + 1, vm.streak)
        }
    }

    if (vm.gameOver) {
        SoloResultScreen(
            modeName = "Memory Flash",
            scoreLabel = "Correct recalls",
            score = vm.streak,
            onPlayAgain = { playCount += 1 },
            onBackToModes = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().background(rememberAuroraBrush())) {
        SoloTopBar(title = "Memory Flash", score = vm.streak, onBack = onBack)
        Text(
            if (vm.phase == MemoryFlashViewModel.Phase.FLASH) "MEMORIZE IT..." else "WHERE WAS IT?",
            color = if (vm.phase == MemoryFlashViewModel.Phase.FLASH) NeonYellow else NeonCyan,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        ShakeBox(pulse = vm.wrongPulse, modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            vm.displayedChallenge?.let { challenge ->
                OddGrid(
                    challenge = challenge,
                    enabled = vm.phase == MemoryFlashViewModel.Phase.RECALL,
                    onTap = { vm.onCellTapped(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
