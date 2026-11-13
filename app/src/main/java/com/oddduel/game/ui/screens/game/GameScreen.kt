package com.oddduel.game.ui.screens.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.data.AvatarCatalog
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.data.UserRepository
import com.oddduel.game.engine.Challenge
import com.oddduel.game.engine.GamePauseController
import com.oddduel.game.ui.components.AnimatedScoreCounter
import com.oddduel.game.ui.components.FancyCountdownTimer
import com.oddduel.game.ui.components.OddGrid
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonYellow
import com.oddduel.game.ui.theme.PlayerLeftAccent
import com.oddduel.game.ui.theme.PlayerRightAccent

@Composable
fun GameScreen(
    settings: GameSettings,
    leftProfile: PlayerProfile,
    rightProfile: PlayerProfile,
    audio: GameAudio,
    pauseController: GamePauseController,
    matchKey: String = "${leftProfile.key}_vs_${rightProfile.key}",
    onExitRequested: () -> Unit,
    onRoundFinished: (winner: PlayerProfile, leftScore: Int, rightScore: Int) -> Unit
) {
    // Keying by match identity is essential: without it Compose's viewModel()
    // would reuse the SAME DuelViewModel (and its already-"finished" state) for
    // every match played inside one screen instance - e.g. every round of a
    // tournament - instantly short-circuiting every match after the first.
    val viewModel: DuelViewModel = viewModel(
        key = matchKey,
        factory = DuelViewModel.factoryFor(settings, leftProfile, rightProfile, audio, pauseController)
    )
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.finished) {
        if (viewModel.finished) {
            val w = viewModel.winner ?: return@LaunchedEffect
            if (leftProfile is PlayerProfile.Registered) {
                userRepository.recordResult(leftProfile.user, viewModel.leftScore, w == leftProfile)
            }
            if (rightProfile is PlayerProfile.Registered) {
                userRepository.recordResult(rightProfile.user, viewModel.rightScore, w == rightProfile)
            }
            onRoundFinished(w, viewModel.leftScore, viewModel.rightScore)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxSize()) {
            PlayerPanel(
                profile = leftProfile,
                accent = PlayerLeftAccent,
                score = viewModel.leftScore,
                challenge = viewModel.leftChallenge,
                enabled = !viewModel.finished,
                wrongPulse = viewModel.leftWrongPulse,
                onTap = { index -> viewModel.onCellTapped(PlayerSide.LEFT, index) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            Column(
                modifier = Modifier.width(96.dp).fillMaxHeight().background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { showExitConfirm = true }) {
                    Icon(Icons.Filled.Home, contentDescription = "Exit match", tint = Color.White.copy(alpha = 0.8f))
                }
                if (viewModel.isOvertime) {
                    Text("OVERTIME", color = NeonYellow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(Modifier.size(6.dp))
                }
                FancyCountdownTimer(secondsLeft = viewModel.timeLeft, totalSeconds = viewModel.totalTime)
                Spacer(Modifier.size(8.dp))
                Text("VS", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            PlayerPanel(
                profile = rightProfile,
                accent = PlayerRightAccent,
                score = viewModel.rightScore,
                challenge = viewModel.rightChallenge,
                enabled = !viewModel.finished,
                wrongPulse = viewModel.rightWrongPulse,
                onTap = { index -> viewModel.onCellTapped(PlayerSide.RIGHT, index) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Leave this match?") },
            text = { Text("The current round's progress will be lost.") },
            confirmButton = {
                Button(onClick = { showExitConfirm = false; onExitRequested() }) { Text("Leave") }
            },
            dismissButton = {
                Button(onClick = { showExitConfirm = false }) { Text("Stay") }
            }
        )
    }
}

@Composable
private fun PlayerPanel(
    profile: PlayerProfile,
    accent: Color,
    score: Int,
    challenge: Challenge?,
    enabled: Boolean,
    wrongPulse: Int,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(wrongPulse) {
        if (wrongPulse > 0) {
            shakeX.animateTo(18f, tween(45))
            shakeX.animateTo(-18f, tween(70))
            shakeX.animateTo(0f, tween(60))
        }
    }

    Column(modifier.background(BgPanel)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val avatar = AvatarCatalog.get(profile.avatarIndex)
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatar.colorHex.toColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(avatar.emoji, fontSize = 20.sp)
            }
            Text(profile.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            AnimatedScoreCounter(score = score, accentColor = accent)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .graphicsLayer(translationX = shakeX.value)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center
        ) {
            challenge?.let {
                OddGrid(challenge = it, enabled = enabled, onTap = onTap, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
