package com.oddduel.game.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// A blue-leaning dusk/aurora cycle: deep navy -> indigo -> violet -> plum -> a teal-blue
// hint, then back to navy. Every stop stays dark enough to keep white text readable.
private val AuroraPalette = listOf(
    Color(0xFF0B1442),
    Color(0xFF1B2A6B),
    Color(0xFF3A2A8F),
    Color(0xFF5C2E86),
    Color(0xFF1A3A5E)
)

private fun auroraColorAt(tRaw: Float): Color {
    val t = ((tRaw % 1f) + 1f) % 1f
    val n = AuroraPalette.size
    val scaled = t * n
    val i = scaled.toInt() % n
    val frac = scaled - scaled.toInt()
    val next = (i + 1) % n
    return lerp(AuroraPalette[i], AuroraPalette[next], frac)
}

/**
 * A slowly shifting diagonal gradient brush - an animated "aurora"/dusk-sky backdrop
 * for menu-ish, fixed-content screens (Home, result screens, mode select). Three
 * gradient stops sample the same color cycle a third of a turn apart, so the whole
 * thing reads as continuously flowing hues rather than a static gradient.
 */
@Composable
fun rememberAuroraBrush(periodMillis: Int = 16000): Brush {
    val infinite = rememberInfiniteTransition(label = "aurora")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraPhase"
    )
    val c1 = auroraColorAt(phase)
    val c2 = auroraColorAt(phase + 1f / 3f)
    val c3 = auroraColorAt(phase + 2f / 3f)
    return Brush.linearGradient(listOf(c1, c2, c3))
}

/** The three festive accent colors to rotate through for buttons/cards - pink, turquoise, purple. */
val FestiveAccents = listOf(NeonPink, NeonCyan, NeonPurple)
