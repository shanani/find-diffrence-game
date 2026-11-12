package com.oddduel.game.ui.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.screens.tournament.RoundResultScreen

private enum class Phase { SETUP, PLAYING, RESULT }

/**
 * No-tournament duel: pick who's playing (a saved player or a guest on each side,
 * "guest mode" needing no setup at all), play one round, see the result, done.
 */
@Composable
fun QuickPlayScreen(settings: GameSettings, audio: GameAudio, onExit: () -> Unit) {
    var phase by remember { mutableStateOf(Phase.SETUP) }
    var leftProfile by remember { mutableStateOf<PlayerProfile?>(null) }
    var rightProfile by remember { mutableStateOf<PlayerProfile?>(null) }
    var result by remember { mutableStateOf<Triple<PlayerProfile, Int, Int>?>(null) }

    when (phase) {
        Phase.SETUP -> QuickPlaySetupScreen(
            onBack = onExit,
            onStart = { left, right ->
                leftProfile = left
                rightProfile = right
                phase = Phase.PLAYING
            }
        )

        Phase.PLAYING -> {
            val left = leftProfile
            val right = rightProfile
            if (left == null || right == null) {
                LaunchedEffect(Unit) { onExit() }
            } else {
                GameScreen(
                    settings = settings,
                    leftProfile = left,
                    rightProfile = right,
                    audio = audio,
                    matchKey = "quick_play",
                    onExitRequested = onExit,
                    onRoundFinished = { winner, leftScore, rightScore ->
                        result = Triple(winner, leftScore, rightScore)
                        phase = Phase.RESULT
                    }
                )
            }
        }

        Phase.RESULT -> {
            val finalResult = result
            if (finalResult == null) {
                LaunchedEffect(Unit) { onExit() }
            } else {
                RoundResultScreen(
                    winner = finalResult.first,
                    leftScore = finalResult.second,
                    rightScore = finalResult.third,
                    isFinalChampion = true,
                    onContinue = onExit
                )
            }
        }
    }
}
