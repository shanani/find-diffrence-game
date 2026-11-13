package com.oddduel.game.ui.screens.solo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Wraps content with a quick horizontal shake whenever [pulse] changes (and is > 0). */
@Composable
fun ShakeBox(pulse: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(pulse) {
        if (pulse > 0) {
            shakeX.animateTo(18f, tween(45))
            shakeX.animateTo(-18f, tween(70))
            shakeX.animateTo(0f, tween(60))
        }
    }
    Box(modifier.graphicsLayer(translationX = shakeX.value)) { content() }
}
