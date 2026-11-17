package com.oddduel.game.engine

import com.oddduel.game.R

/**
 * Ten real kid photos, each captured in two different expressions/poses (state A and
 * state B). Backs the FACE challenge category: identity swaps, geometric transforms and
 * expression swaps are all built from these 20 drawables - see [ChallengeGenerator].
 */
object FaceCatalog {

    data class Identity(val stateA: Int, val stateB: Int)

    val identities: List<Identity> = listOf(
        Identity(R.drawable.face01_a, R.drawable.face01_b),
        Identity(R.drawable.face02_a, R.drawable.face02_b),
        Identity(R.drawable.face03_a, R.drawable.face03_b),
        Identity(R.drawable.face04_a, R.drawable.face04_b),
        Identity(R.drawable.face05_a, R.drawable.face05_b),
        Identity(R.drawable.face06_a, R.drawable.face06_b),
        Identity(R.drawable.face07_a, R.drawable.face07_b),
        Identity(R.drawable.face08_a, R.drawable.face08_b),
        Identity(R.drawable.face09_a, R.drawable.face09_b),
        Identity(R.drawable.face10_a, R.drawable.face10_b)
    )

    fun drawableFor(identityIndex: Int, state: Int): Int {
        val identity = identities[identityIndex]
        return if (state == 0) identity.stateA else identity.stateB
    }
}
