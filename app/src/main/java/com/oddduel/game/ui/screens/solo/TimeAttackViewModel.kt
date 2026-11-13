package com.oddduel.game.ui.screens.solo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.audio.Sfx
import com.oddduel.game.engine.Challenge
import com.oddduel.game.engine.ChallengeCache
import com.oddduel.game.engine.GamePauseController
import kotlinx.coroutines.launch

/**
 * Time Attack: a fixed 20-card deck, 5 seconds per card. A 3x3 grid at the gentlest
 * difficulty tier is the ceiling for a fair 5s window - subtle-difference visual
 * search runs roughly 200-400ms per item scanned, so anything bigger or harder would
 * stop testing speed and start testing luck. A miss (wrong tap or the clock running
 * out) still consumes the card, keeping the pace brisk like a real speed round.
 */
class TimeAttackViewModel(private val audio: GameAudio, private val pauseController: GamePauseController) : ViewModel() {

    companion object {
        const val DECK_SIZE = 20
        const val PER_CARD_SECONDS = 5
        const val GRID_ROWS = 3
        const val GRID_COLS = 3
        private const val DIFFICULTY_TIER = 1

        fun factoryFor(audio: GameAudio, pauseController: GamePauseController): ViewModelProvider.Factory =
            viewModelFactory { initializer { TimeAttackViewModel(audio, pauseController) } }
    }

    private val cache = ChallengeCache(GRID_ROWS, GRID_COLS, { DIFFICULTY_TIER }, viewModelScope)

    var challenge by mutableStateOf<Challenge?>(null); private set
    var correctCount by mutableStateOf(0); private set
    var cardsPlayed by mutableStateOf(0); private set
    var timeLeft by mutableStateOf(PER_CARD_SECONDS); private set
    var gameOver by mutableStateOf(false); private set
    var wrongPulse by mutableStateOf(0); private set

    init {
        viewModelScope.launch {
            challenge = cache.take()
            audio.play(Sfx.ROUND_START)
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (!gameOver) {
            pauseController.pausableDelay(1000)
            if (gameOver) return
            timeLeft -= 1
            if (timeLeft in 1..2) audio.play(Sfx.TICK)
            if (timeLeft <= 0) advanceCard(hit = false)
        }
    }

    fun onCellTapped(index: Int) {
        if (gameOver) return
        val current = challenge ?: return
        if (index == current.oddIndex) {
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)
            correctCount += 1
            advanceCard(hit = true)
        } else {
            audio.play(Sfx.WRONG)
            wrongPulse += 1
            advanceCard(hit = false)
        }
    }

    private fun advanceCard(hit: Boolean) {
        cardsPlayed += 1
        if (cardsPlayed >= DECK_SIZE) {
            gameOver = true
            audio.play(if (correctCount >= DECK_SIZE / 2) Sfx.VICTORY else Sfx.TIMEOUT)
            return
        }
        timeLeft = PER_CARD_SECONDS
        viewModelScope.launch { challenge = cache.take() }
    }
}
