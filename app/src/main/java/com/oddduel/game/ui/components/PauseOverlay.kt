package com.oddduel.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonCyan

/**
 * Full-screen modal shown whenever [com.oddduel.game.engine.GamePauseController] is
 * paused - from the floating [PauseButton] or automatically when the app is
 * backgrounded. It blocks every touch to whatever screen is underneath (a plain
 * scrim with no gesture handling would NOT do this - Compose only swallows a touch
 * if something actually consumes it) and is the only place gameplay resumes from,
 * so returning to the app after switching away never silently un-pauses a round.
 */
@Composable
fun PauseOverlay(audio: GameAudio, onResume: () -> Unit, onQuitToHome: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { /* absorb taps */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BgPanel)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PAUSED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Resume", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { audio.toggleMusicMute() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = if (audio.musicPlaying) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(if (audio.musicPlaying) "Mute Music" else "Unmute Music")
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onQuitToHome,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Quit to Home")
            }
        }
    }
}
