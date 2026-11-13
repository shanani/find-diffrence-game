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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Reflex Hunter: the odd one jumps to a brand new cell on every hop, and you have to
 * find and tap it before the window closes. Simple visual reaction time alone is
 * ~200-250ms, but that's for a difference you already know the location of; here the
 * player also has to actively SEARCH a 3x3 grid for a subtle visual difference first,
 * which realistically costs several hundred ms more on top. ~1600ms is a fair
 * starting pace for that full search-then-tap cycle, ramping down toward a 700ms
 * floor as the streak grows (below that it stops testing skill and starts testing
 * luck) while the grid stays fixed at 3x3 so the hunt stays legible at speed.
 */
class ReflexHunterViewModel(private val audio: GameAudio, private val pauseController: GamePauseController) : ViewModel() {

    companion object {
        const val GRID_ROWS = 3
        const val GRID_COLS = 3
        private const val START_INTERVAL_MS = 1600L
        private const val MIN_INTERVAL_MS = 700L
        private const val SPEEDUP_STEP_MS = 40L
        private const val HITS_PER_SPEEDUP = 6
        private const val HITS_PER_DIFFICULTY_BUMP = 8

        fun factoryFor(audio: GameAudio, pauseController: GamePauseController): ViewModelProvider.Factory =
            viewModelFactory { initializer { ReflexHunterViewModel(audio, pauseController) } }
    }

    private var difficultyTier = 1
    private val cache = ChallengeCache(GRID_ROWS, GRID_COLS, { difficultyTier }, viewModelScope)
    private var hopJob: Job? = null

    var challenge by mutableStateOf<Challenge?>(null); private set
    var streak by mutableStateOf(0); private set
    var intervalMs by mutableStateOf(START_INTERVAL_MS); private set
    var hopToken by mutableStateOf(0); private set
    var gameOver by mutableStateOf(false); private set
    var wrongPulse by mutableStateOf(0); private set

    init {
        viewModelScope.launch {
            challenge = cache.take()
            audio.play(Sfx.ROUND_START)
            startHopWindow()
        }
    }

    private fun startHopWindow() {
        hopToken += 1
        hopJob?.cancel()
        hopJob = viewModelScope.launch {
            pauseController.pausableDelay(intervalMs)
            if (!gameOver) endGame()
        }
    }

    fun onCellTapped(index: Int) {
        if (gameOver) return
        val current = challenge ?: return
        if (index == current.oddIndex) {
            hopJob?.cancel()
            streak += 1
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)
            if (streak % HITS_PER_SPEEDUP == 0) {
                intervalMs = (intervalMs - SPEEDUP_STEP_MS).coerceAtLeast(MIN_INTERVAL_MS)
            }
            if (streak % HITS_PER_DIFFICULTY_BUMP == 0) {
                difficultyTier = (difficultyTier + 1).coerceAtMost(5)
            }
            viewModelScope.launch {
                challenge = cache.take()
                startHopWindow()
            }
        } else {
            audio.play(Sfx.WRONG)
            wrongPulse += 1
            endGame()
        }
    }

    private fun endGame() {
        if (gameOver) return
        gameOver = true
        hopJob?.cancel()
        audio.play(if (streak >= 10) Sfx.VICTORY else Sfx.TIMEOUT)
    }
}
