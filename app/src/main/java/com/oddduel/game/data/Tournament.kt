package com.oddduel.game.data

class Match(val round: Int, val index: Int) {
    var p1: PlayerProfile? = null
    var p2: PlayerProfile? = null
    var winner: PlayerProfile? = null
    var score1: Int = 0
    var score2: Int = 0

    val isBye: Boolean get() = (p1 == null) != (p2 == null)
    val isReady: Boolean get() = p1 != null && p2 != null && winner == null
}

/**
 * Single-elimination bracket for any number of players (any count >= 2 - odd counts
 * or counts that aren't a power of two get automatic byes in round one, same as a real
 * tournament seeding sheet). Call [nextReadyMatch] in a loop, play it on the duel
 * screen, report the result with [recordMatchResult], and repeat until it returns null
 * and [champion] is set.
 */
class Tournament(players: List<PlayerProfile>) {
    val rounds: List<MutableList<Match>>
    var champion: PlayerProfile? = null
        private set

    init {
        var size = 1
        while (size < players.size) size *= 2
        size = maxOf(size, 2)

        val shuffled = players.shuffled()
        val slots: List<PlayerProfile?> = List(size) { i -> shuffled.getOrNull(i) }

        val numRounds = Integer.numberOfTrailingZeros(size)
        rounds = List(numRounds) { r -> MutableList(size / (1 shl (r + 1))) { i -> Match(r, i) } }

        rounds[0].forEachIndexed { i, match ->
            match.p1 = slots.getOrNull(i * 2)
            match.p2 = slots.getOrNull(i * 2 + 1)
        }
        resolveByes()
    }

    private fun resolveByes() {
        for (match in rounds[0]) {
            if (match.isBye) {
                val winner = match.p1 ?: match.p2
                recordMatchResult(match.round, match.index, winner!!, if (match.p1 != null) 1 else 0, if (match.p2 != null) 1 else 0)
            }
        }
    }

    fun nextReadyMatch(): Match? = rounds.flatten().firstOrNull { it.isReady }

    fun recordMatchResult(round: Int, index: Int, winner: PlayerProfile, score1: Int, score2: Int) {
        val match = rounds[round][index]
        match.winner = winner
        match.score1 = score1
        match.score2 = score2
        val nextRoundIndex = round + 1
        if (nextRoundIndex >= rounds.size) {
            champion = winner
            return
        }
        val nextMatch = rounds[nextRoundIndex][index / 2]
        if (index % 2 == 0) nextMatch.p1 = winner else nextMatch.p2 = winner
    }

    val isFinished: Boolean get() = champion != null
    val totalRounds: Int get() = rounds.size
    fun currentRoundNumber(): Int = rounds.indexOfFirst { r -> r.any { it.winner == null } }.let { if (it == -1) rounds.size else it + 1 }
}
