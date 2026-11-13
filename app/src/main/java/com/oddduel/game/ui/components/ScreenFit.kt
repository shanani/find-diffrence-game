package com.oddduel.game.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single number that shrinks fixed-content screens (menus, result screens, static
 * info) to genuinely fit any screen height, instead of either scrolling or clipping.
 *
 * Every such screen in this app was laid out and tuned against a roomy ~800dp-tall
 * landscape canvas (a TV or tablet). On a shorter screen - a phone in landscape, or a
 * split-screen/multi-window window - that same content would either get clipped or
 * need to scroll. Scaling every font size and spacing by this one factor keeps all of
 * it visible and proportionally laid out with nothing cut off, which only works
 * because these screens have fixed, enumerable content (not a list that can grow
 * without bound - those screens use LazyColumn instead, which is the correct, normal
 * place for scrolling to happen).
 *
 * Clamped to [1, down to 0.5]: a tall display never stretches content past its
 * designed size, and nothing shrinks to the point of being unreadable.
 */
@Composable
fun rememberFitScale(referenceHeightDp: Float = 820f, minScale: Float = 0.5f): Float {
    val heightDp = LocalConfiguration.current.screenHeightDp.toFloat()
    return remember(heightDp, referenceHeightDp, minScale) {
        (heightDp / referenceHeightDp).coerceIn(minScale, 1f)
    }
}

fun Dp.scaledBy(scale: Float): Dp = (value * scale).dp

fun TextUnit.scaledBy(scale: Float): TextUnit {
    if (type != TextUnitType.Sp) return this
    return (value * scale).sp
}
