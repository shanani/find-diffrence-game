package com.oddduel.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oddduel.game.ui.theme.NeonCyan

/**
 * Fixed, app-wide pause control for the whole game (not just the music). Placed once
 * in [com.oddduel.game.ui.OddDuelApp] as an overlay above the nav host so it keeps the
 * exact same spot on every single screen, rather than being wired into each screen's
 * own top bar individually. Tapping it opens [PauseOverlay], which is what actually
 * freezes gameplay timers and resumes them again.
 */
@Composable
fun PauseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause", tint = NeonCyan)
    }
}
