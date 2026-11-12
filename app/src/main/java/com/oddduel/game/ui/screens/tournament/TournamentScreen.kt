package com.oddduel.game.ui.screens.tournament

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.screens.game.GameScreen

private enum class Phase { SETUP, PLAYING, RESULT }

/**
 * Owns the whole tournament lifecycle in one place: build the roster, play each ready
 * bracket match as a normal timed duel, show the round winner, and repeat until a
 * single champion remains - split screen can only ever host 2 players at once, so this
 * is what lets any number of players compete overall.
 */
@Composable
fun TournamentScreen(
    settings: GameSettings,
    audio: GameAudio,
    tournamentViewModel: TournamentViewModel = viewModel(),
    onExit: () -> Unit
) {
    var phase by remember { mutableStateOf(Phase.SETUP) }
    var lastResult by remember { mutableStateOf<Triple<PlayerProfile, Int, Int>?>(null) }
    val version = tournamentViewModel.version // read to subscribe to bracket mutations

    when (phase) {
        Phase.SETUP -> TournamentSetupScreen(
            onBack = onExit,
            onStart = { players ->
                tournamentViewModel.start(players)
                phase = Phase.PLAYING
            }
        )

        Phase.PLAYING -> {
            val tournament = tournamentViewModel.tournament
            val match = tournament?.nextReadyMatch()
            if (tournament == null || match == null) {
                LaunchedEffect(Unit) { onExit() }
            } else {
                val p1 = match.p1!!
                val p2 = match.p2!!
                GameScreen(
                    settings = settings,
                    leftProfile = p1,
                    rightProfile = p2,
                    audio = audio,
                    onRoundFinished = { winner, leftScore, rightScore ->
                        tournamentViewModel.recordResult(match.round, match.index, winner, leftScore, rightScore)
                        lastResult = Triple(winner, leftScore, rightScore)
                        phase = Phase.RESULT
                    }
                )
            }
        }

        Phase.RESULT -> {
            val tournament = tournamentViewModel.tournament
            val result = lastResult
            if (tournament == null || result == null) {
                LaunchedEffect(Unit) { onExit() }
            } else {
                val champion = tournament.champion
                RoundResultScreen(
                    winner = result.first,
                    leftScore = result.second,
                    rightScore = result.third,
                    isFinalChampion = champion != null,
                    onContinue = {
                        if (champion != null) {
                            tournamentViewModel.reset()
                            onExit()
                        } else {
                            phase = Phase.PLAYING
                        }
                    }
                )
            }
        }
    }
}
