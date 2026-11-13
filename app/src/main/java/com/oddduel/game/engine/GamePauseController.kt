package com.oddduel.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Single app-wide pause switch, shared by every timer-driven ViewModel (duel round
 * countdown, the solo-mode clocks) and the full-screen Pause overlay in OddDuelApp.
 * It's one instance for the whole app rather than per-screen so that backgrounding
 * the app (ON_STOP) truly freezes whatever mode is active, not just the music.
 */
class GamePauseController {

    var isPaused by mutableStateOf(false)
        private set

    /**
     * True only while an actual round/run is in progress on screen (a duel, or a solo
     * mode before it's game-over) - not on any menu, setup or result screen. Drives
     * whether the floating pause button even shows, and which of the two background
     * behaviors ON_STOP uses: freeze the round, or just mute the music.
     */
    var isGameActive by mutableStateOf(false)
        private set

    fun setGameActive(active: Boolean) {
        isGameActive = active
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    /**
     * Delays for [ms] of *unpaused* time: any stretch spent paused doesn't count
     * toward it, so a round timer, hop window or memorize-it flash genuinely stops
     * advancing while paused instead of just being visually hidden behind the overlay.
     */
    suspend fun pausableDelay(ms: Long) {
        var remaining = ms
        while (remaining > 0) {
            if (isPaused) {
                delay(100)
                continue
            }
            val step = if (remaining < 100L) remaining else 100L
            delay(step)
            remaining -= step
        }
    }
}
