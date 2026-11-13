package com.oddduel.game.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.oddduel.game.engine.GamePauseController

/**
 * Marks [pauseController] as "a game is actually in progress" for as long as [active]
 * is true, and always clears it when the call site leaves composition (e.g. the
 * player backs out mid-round) so it can never get stuck true on a screen the player
 * has already left. Drives whether the floating pause button shows at all, and
 * whether backgrounding the app freezes a round or just mutes the music.
 */
@Composable
fun TrackGameActive(pauseController: GamePauseController, active: Boolean) {
    LaunchedEffect(active) {
        pauseController.setGameActive(active)
    }
    DisposableEffect(Unit) {
        onDispose { pauseController.setGameActive(false) }
    }
}
