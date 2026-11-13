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
 * Memory Flash: the grid shows normally for a moment, then blanks to identical cells -
 * you tap where the odd one WAS. Spatial recall for one position among nine is well
 * within normal working-memory capacity, so a 2.5s flash (shrinking to a 1.2s floor
 * as the streak grows) is enough time to locate and encode it without turning the
 * mode into a guessing game. Kept at the gentlest difficulty tier throughout, since
 * subtlety isn't the point here - the flash itself must be clearly readable, because
 * it is memory, not perception, being tested once the grid goes blank. No clock
 * during recall itself - the challenge is remembering, not reacting fast.
 */
class MemoryFlashViewModel(private val audio: GameAudio, private val pauseController: GamePauseController) : ViewModel() {

    enum class Phase { FLASH, RECALL }

    companion object {
        const val GRID_ROWS = 3
        const val GRID_COLS = 3
        private const val START_FLASH_MS = 2500L
        private const val MIN_FLASH_MS = 1200L
        private const val SHRINK_STEP_MS = 100L
        private const val STREAK_PER_SHRINK = 3
        private const val DIFFICULTY_TIER = 1

        fun factoryFor(audio: GameAudio, pauseController: GamePauseController): ViewModelProvider.Factory =
            viewModelFactory { initializer { MemoryFlashViewModel(audio, pauseController) } }
    }

    private val cache = ChallengeCache(GRID_ROWS, GRID_COLS, { DIFFICULTY_TIER }, viewModelScope)
    private var realChallenge: Challenge? = null
    private var flashMs = START_FLASH_MS

    var displayedChallenge by mutableStateOf<Challenge?>(null); private set
    var phase by mutableStateOf(Phase.FLASH); private set
    var streak by mutableStateOf(0); private set
    var gameOver by mutableStateOf(false); private set
    var wrongPulse by mutableStateOf(0); private set

    init {
        viewModelScope.launch {
            audio.play(Sfx.ROUND_START)
            nextRound()
        }
    }

    private suspend fun nextRound() {
        val fresh = cache.take()
        realChallenge = fresh
        displayedChallenge = fresh
        phase = Phase.FLASH
        pauseController.pausableDelay(flashMs)
        if (gameOver) return
        displayedChallenge = blank(fresh)
        phase = Phase.RECALL
    }

    private fun blank(challenge: Challenge): Challenge {
        val baseStyle = challenge.cells[(challenge.oddIndex + 1) % challenge.cells.size]
        return challenge.copy(cells = List(challenge.cells.size) { baseStyle })
    }

    fun onCellTapped(index: Int) {
        if (gameOver || phase != Phase.RECALL) return
        val current = realChallenge ?: return
        if (index == current.oddIndex) {
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)
            streak += 1
            if (streak % STREAK_PER_SHRINK == 0) {
                flashMs = (flashMs - SHRINK_STEP_MS).coerceAtLeast(MIN_FLASH_MS)
            }
            viewModelScope.launch { nextRound() }
        } else {
            audio.play(Sfx.WRONG)
            wrongPulse += 1
            gameOver = true
            audio.play(if (streak >= 8) Sfx.VICTORY else Sfx.TIMEOUT)
        }
    }
}
