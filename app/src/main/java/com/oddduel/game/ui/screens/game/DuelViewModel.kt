package com.oddduel.game.ui.screens.game

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
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.engine.Challenge
import com.oddduel.game.engine.ChallengeCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PlayerSide { LEFT, RIGHT }

/**
 * Drives exactly one timed 2-player duel - the atomic "round" the whole game is built
 * from, whether it's a no-setup quick match or one bracket round of a tournament.
 */
class DuelViewModel(
    private val settings: GameSettings,
    val leftProfile: PlayerProfile,
    val rightProfile: PlayerProfile,
    private val audio: GameAudio
) : ViewModel() {

    companion object {
        fun factoryFor(settings: GameSettings, left: PlayerProfile, right: PlayerProfile, audio: GameAudio): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DuelViewModel(settings, left, right, audio) }
            }
    }

    var leftScore by mutableStateOf(0); private set
    var rightScore by mutableStateOf(0); private set
    var leftChallenge by mutableStateOf<Challenge?>(null); private set
    var rightChallenge by mutableStateOf<Challenge?>(null); private set
    var timeLeft by mutableStateOf(settings.roundTimeoutSeconds); private set
    var totalTime by mutableStateOf(settings.roundTimeoutSeconds); private set
    var isOvertime by mutableStateOf(false); private set
    var finished by mutableStateOf(false); private set
    var winner by mutableStateOf<PlayerProfile?>(null); private set
    var leftWrongPulse by mutableStateOf(0); private set
    var rightWrongPulse by mutableStateOf(0); private set

    private var leftCorrect = 0
    private var rightCorrect = 0

    private val leftCache = ChallengeCache(settings.gridRows, settings.gridCols, { 1 + leftCorrect / 5 }, viewModelScope)
    private val rightCache = ChallengeCache(settings.gridRows, settings.gridCols, { 1 + rightCorrect / 5 }, viewModelScope)

    init {
        viewModelScope.launch {
            leftChallenge = leftCache.take()
            rightChallenge = rightCache.take()
            audio.play(Sfx.ROUND_START)
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (!finished) {
            delay(1000)
            if (finished) return
            timeLeft -= 1
            if (timeLeft in 1..3) audio.play(Sfx.TICK)
            if (timeLeft <= 0) {
                if (leftScore == rightScore) {
                    isOvertime = true
                    totalTime = 12
                    timeLeft = 12
                } else {
                    endRound()
                }
            }
        }
    }

    fun onCellTapped(side: PlayerSide, index: Int) {
        if (finished) return
        val challenge = (if (side == PlayerSide.LEFT) leftChallenge else rightChallenge) ?: return
        if (index == challenge.oddIndex) {
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)
            if (side == PlayerSide.LEFT) {
                leftScore += 10
                leftCorrect += 1
            } else {
                rightScore += 10
                rightCorrect += 1
            }
            viewModelScope.launch {
                val cache = if (side == PlayerSide.LEFT) leftCache else rightCache
                val next = cache.take()
                if (side == PlayerSide.LEFT) leftChallenge = next else rightChallenge = next
            }
        } else {
            audio.play(Sfx.WRONG)
            if (side == PlayerSide.LEFT) leftWrongPulse += 1 else rightWrongPulse += 1
        }
    }

    private fun endRound() {
        finished = true
        val w = when {
            leftScore > rightScore -> leftProfile
            rightScore > leftScore -> rightProfile
            else -> leftProfile // extremely rare: overtime exhausted in a dead tie, pick arbitrarily
        }
        winner = w
        audio.play(Sfx.VICTORY)
    }
}
