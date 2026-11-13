package com.oddduel.game.ui.screens.tournament

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.data.Tournament

class TournamentViewModel : ViewModel() {
    var tournament by mutableStateOf<Tournament?>(null)
        private set

    /** Bumped on every mutation so composables watching it know the bracket changed. */
    var version by mutableIntStateOf(0)
        private set

    fun start(players: List<PlayerProfile>) {
        tournament = Tournament(players)
        version++
    }

    fun recordResult(round: Int, index: Int, winner: PlayerProfile, leftScore: Int, rightScore: Int) {
        tournament?.recordMatchResult(round, index, winner, leftScore, rightScore)
        version++
    }
}
