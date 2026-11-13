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
import kotlinx.coroutines.launch

/**
 * Survival Streak: no clock at all - the pressure comes purely from "don't miss."
 * Difficulty and grid size both escalate on streak milestones instead of a timer,
 * so unlike the timed modes this tests sustained accuracy rather than speed. One
 * wrong tap ends the run.
 */
class SurvivalStreakViewModel(private val audio: GameAudio) : ViewModel() {

    companion object {
        private val GRID_SEQUENCE = listOf(3 to 3, 4 to 3, 4 to 4, 5 to 4, 5 to 5)
        private const val STREAK_PER_GRID_STEP = 10
        private const val STREAK_PER_DIFFICULTY_STEP = 5

        fun factoryFor(audio: GameAudio): ViewModelProvider.Factory =
            viewModelFactory { initializer { SurvivalStreakViewModel(audio) } }
    }

    private var difficultyTier = 1
    private var gridIndex = 0
    private var cache: ChallengeCache
    private val startTimeMs = System.currentTimeMillis()

    var challenge by mutableStateOf<Challenge?>(null); private set
    var streak by mutableStateOf(0); private set
    var gridRows by mutableStateOf(GRID_SEQUENCE[0].first); private set
    var gridCols by mutableStateOf(GRID_SEQUENCE[0].second); private set
    var gameOver by mutableStateOf(false); private set
    var wrongPulse by mutableStateOf(0); private set

    /** Wall-clock time this run took, set the moment [gameOver] flips - for DifficultyAnalytics. */
    var elapsedMs by mutableStateOf(0L); private set

    init {
        cache = ChallengeCache(gridRows, gridCols, { difficultyTier }, viewModelScope)
        viewModelScope.launch {
            challenge = cache.take()
            audio.play(Sfx.ROUND_START)
        }
    }

    fun onCellTapped(index: Int) {
        if (gameOver) return
        val current = challenge ?: return
        if (index == current.oddIndex) {
            streak += 1
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)

            if (streak % STREAK_PER_DIFFICULTY_STEP == 0) {
                difficultyTier = (difficultyTier + 1).coerceAtMost(5)
            }
            val newGridIndex = (streak / STREAK_PER_GRID_STEP).coerceAtMost(GRID_SEQUENCE.size - 1)
            if (newGridIndex != gridIndex) {
                gridIndex = newGridIndex
                val (rows, cols) = GRID_SEQUENCE[gridIndex]
                gridRows = rows
                gridCols = cols
                cache = ChallengeCache(rows, cols, { difficultyTier }, viewModelScope)
            }
            viewModelScope.launch { challenge = cache.take() }
        } else {
            audio.play(Sfx.WRONG)
            wrongPulse += 1
            elapsedMs = System.currentTimeMillis() - startTimeMs
            gameOver = true
            audio.play(if (streak >= 15) Sfx.VICTORY else Sfx.TIMEOUT)
        }
    }
}
