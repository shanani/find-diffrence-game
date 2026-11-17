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
import com.oddduel.game.engine.GamePauseController
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
    private val audio: GameAudio,
    private val pauseController: GamePauseController
) : ViewModel() {

    companion object {
        fun factoryFor(
            settings: GameSettings,
            left: PlayerProfile,
            right: PlayerProfile,
            audio: GameAudio,
            pauseController: GamePauseController
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DuelViewModel(settings, left, right, audio, pauseController) }
            }
    }

    var leftScore by mutableStateOf(0); private set
    var rightScore by mutableStateOf(0); private set
    var leftChallenge by mutableStateOf<Challenge?>(null); private set
    var rightChallenge by mutableStateOf<Challenge?>(null); private set
    var timeLeft by mutableStateOf(settings.roundTimeoutSeconds); private set
    val totalTime = settings.roundTimeoutSeconds
    var finished by mutableStateOf(false); private set
    var winner by mutableStateOf<PlayerProfile?>(null); private set
    // True when the match ended with the score tied and the winner was decided by who
    // answered correctly faster overall, not by points - the UI shows this distinctly
    // rather than presenting it as an ordinary score win.
    var wonByResponseTime by mutableStateOf(false); private set
    var leftWrongPulse by mutableStateOf(0); private set
    var rightWrongPulse by mutableStateOf(0); private set

    private var leftCorrect = 0
    private var rightCorrect = 0

    // Sum of "time from when a challenge appeared to the correct tap" across every
    // correctly-solved challenge this match - the tie-break signal when scores are equal.
    private var leftTotalResponseMs = 0L
    private var rightTotalResponseMs = 0L
    private var leftChallengeStartMs = 0L
    private var rightChallengeStartMs = 0L

    private val leftCache = ChallengeCache(settings.gridRows, settings.gridCols, { 1 + leftCorrect / 5 }, viewModelScope)
    private val rightCache = ChallengeCache(settings.gridRows, settings.gridCols, { 1 + rightCorrect / 5 }, viewModelScope)

    init {
        viewModelScope.launch {
            leftChallenge = leftCache.take()
            rightChallenge = rightCache.take()
            val now = System.currentTimeMillis()
            leftChallengeStartMs = now
            rightChallengeStartMs = now
            audio.play(Sfx.ROUND_START)
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (!finished) {
            pauseController.pausableDelay(1000)
            if (finished) return
            timeLeft -= 1
            if (timeLeft in 1..3) audio.play(Sfx.TICK)
            if (timeLeft <= 0) endRound()
        }
    }

    fun onCellTapped(side: PlayerSide, index: Int) {
        if (finished) return
        val challenge = (if (side == PlayerSide.LEFT) leftChallenge else rightChallenge) ?: return
        if (index == challenge.oddIndex) {
            audio.play(Sfx.CORRECT)
            audio.play(Sfx.COIN)
            val now = System.currentTimeMillis()
            if (side == PlayerSide.LEFT) {
                leftScore += 10
                leftCorrect += 1
                leftTotalResponseMs += now - leftChallengeStartMs
            } else {
                rightScore += 10
                rightCorrect += 1
                rightTotalResponseMs += now - rightChallengeStartMs
            }
            viewModelScope.launch {
                val cache = if (side == PlayerSide.LEFT) leftCache else rightCache
                val next = cache.take()
                val startedAt = System.currentTimeMillis()
                if (side == PlayerSide.LEFT) {
                    leftChallenge = next
                    leftChallengeStartMs = startedAt
                } else {
                    rightChallenge = next
                    rightChallengeStartMs = startedAt
                }
            }
        } else {
            audio.play(Sfx.WRONG)
            if (side == PlayerSide.LEFT) leftWrongPulse += 1 else rightWrongPulse += 1
        }
    }

    private fun endRound() {
        finished = true
        if (leftScore != rightScore) {
            winner = if (leftScore > rightScore) leftProfile else rightProfile
        } else {
            wonByResponseTime = true
            winner = when {
                leftTotalResponseMs < rightTotalResponseMs -> leftProfile
                rightTotalResponseMs < leftTotalResponseMs -> rightProfile
                // Neither side ever solved one correctly (or, vanishingly rarely, their
                // cumulative times matched exactly too) - nothing left to break the tie on.
                else -> if (kotlin.random.Random.nextBoolean()) leftProfile else rightProfile
            }
        }
        audio.play(Sfx.VICTORY)
    }
}
