package com.oddduel.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.theme.BgPanelLight
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.NeonGreen
import com.oddduel.game.ui.theme.NeonYellow

/**
 * Countdown display that shifts from calm green through amber to a pulsing red as the
 * round runs out, mirroring the urgency cues of top mobile/TV arcade games.
 */
@Composable
fun FancyCountdownTimer(secondsLeft: Int, totalSeconds: Int, modifier: Modifier = Modifier) {
    val fraction = (secondsLeft.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val color = when {
        fraction > 0.5f -> lerp(NeonYellow, NeonGreen, (fraction - 0.5f) * 2f)
        fraction > 0.2f -> lerp(DangerRed, NeonYellow, (fraction - 0.2f) / 0.3f)
        else -> DangerRed
    }
    val urgent = fraction <= 0.2f
    val infinite = rememberInfiniteTransition(label = "timerPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (urgent) 1.18f else 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier = modifier
            .scale(pulse)
            .clip(RoundedCornerShape(20.dp))
            .background(BgPanelLight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = secondsLeft.toString(),
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        )
    }
}
