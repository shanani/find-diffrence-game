package com.oddduel.game.ui.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.screens.tournament.RoundResultScreen

/** No-setup "guest mode" duel: two anonymous players jump straight into a single round. */
@Composable
fun QuickPlayScreen(settings: GameSettings, audio: GameAudio, onExit: () -> Unit) {
    var result by remember { mutableStateOf<Triple<PlayerProfile, Int, Int>?>(null) }

    val result1 = result
    if (result1 == null) {
        GameScreen(
            settings = settings,
            leftProfile = PlayerProfile.Guest("Player 1", 0),
            rightProfile = PlayerProfile.Guest("Player 2", 1),
            audio = audio,
            onRoundFinished = { winner, leftScore, rightScore -> result = Triple(winner, leftScore, rightScore) }
        )
    } else {
        RoundResultScreen(
            winner = result1.first,
            leftScore = result1.second,
            rightScore = result1.third,
            isFinalChampion = true,
            onContinue = onExit
        )
    }
}
