package com.oddduel.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.theme.NeonCyan

/**
 * Visible, app-wide "an update is downloading right now" chip - shown automatically
 * whenever [com.oddduel.game.update.UpdateInstaller.isDownloading] is true, whether
 * that download was just started by the player or resumed on its own after they
 * granted the install-unknown-apps permission and came back. The point is that the
 * player never has to wonder "did tapping Update Now actually do anything?" - the
 * same worry a silent permission round trip would otherwise reintroduce.
 */
@Composable
fun UpdateProgressIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NeonCyan)
        Text(
            "Downloading update…",
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
