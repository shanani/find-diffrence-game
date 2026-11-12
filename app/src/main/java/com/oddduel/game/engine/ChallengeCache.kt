package com.oddduel.game.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps at least [minCacheSize] freshly-generated challenges ready per player so a
 * correct tap never has to wait on generation - the grid is built off the UI thread
 * ahead of time and topped back up in the background the moment it dips low.
 */
class ChallengeCache(
    private val rows: Int,
    private val cols: Int,
    private val difficultyTier: () -> Int,
    private val scope: CoroutineScope,
    private val minCacheSize: Int = 10
) {
    private val mutex = Mutex()
    private val buffer = ArrayDeque<Challenge>()
    private var refillJob: Job? = null

    init {
        triggerRefill()
    }

    private fun triggerRefill() {
        if (refillJob?.isActive == true) return
        refillJob = scope.launch(Dispatchers.Default) {
            mutex.withLock {
                while (buffer.size < minCacheSize) {
                    buffer.addLast(ChallengeGenerator.generate(rows, cols, difficultyTier()))
                }
            }
        }
    }

    /** Pops the next ready challenge, generating synchronously only on a cold start. */
    suspend fun take(): Challenge {
        val item = mutex.withLock {
            if (buffer.isEmpty()) {
                buffer.addLast(ChallengeGenerator.generate(rows, cols, difficultyTier()))
            }
            buffer.removeFirst()
        }
        triggerRefill()
        return item
    }

    fun cachedCount(): Int = buffer.size
}
