package com.oddduel.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.theme.NeonYellow
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * A score counter that rolls upward with a quick spring and throws a little "+N" coin
 * popup, the same tactile feedback used by top casual/arcade score HUDs.
 */
@Composable
fun AnimatedScoreCounter(score: Int, accentColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val animatable = remember { Animatable(score.toFloat()) }
    val popups = remember { mutableStateListOf<Pair<Int, Int>>() } // id, delta

    LaunchedEffect(score) {
        val previous = animatable.value.toInt()
        val delta = score - previous
        if (delta > 0) {
            val id = Random.nextInt()
            popups.add(id to delta)
        }
        animatable.animateTo(score.toFloat(), animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Box(modifier = modifier) {
        Row {
            Text(
                text = "🪙 ", // coin emoji
                fontSize = 22.sp
            )
            Text(
                text = animatable.value.toInt().toString(),
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
        }
        popups.forEach { (id, delta) ->
            key(id) {
                ScorePopup(delta = delta, onDone = { popups.remove(id to delta) })
            }
        }
    }
}

@Composable
private fun ScorePopup(delta: Int, onDone: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(650)
        visible = false
        delay(200)
        onDone()
    }
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it }
    ) {
        Text(
            text = "+$delta",
            color = NeonYellow,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            modifier = Modifier
                .offset(x = 54.dp, y = (-6).dp)
        )
    }
}
