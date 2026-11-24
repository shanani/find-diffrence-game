package com.oddduel.game.ui.screens.tournament

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.data.AvatarCatalog
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.components.rememberFitScale
import com.oddduel.game.ui.components.scaledBy
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.theme.GameButtonFont
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonYellow
import com.oddduel.game.ui.theme.rememberAuroraBrush
import kotlinx.coroutines.delay

// A tap that was really meant for the match that just ended (the last correct cell, or
// a remote's OK button held a beat too long) can otherwise land on this screen's primary
// button, which sits in roughly the same spot - triggering an unintended instant Rematch
// or Next Round. Keeping the action buttons disabled for this long after the result
// first appears gives that leftover tap nowhere to land.
private const val ActionButtonDebounceMs = 1000L

/**
 * Shown right after a single duel ends. The caller decides what the two action
 * buttons actually do - rematch-and-stay (quick play) and next-round/view-bracket
 * (tournament) both reuse this same winner reveal, just with different actions.
 */
@Composable
fun RoundResultScreen(
    winner: PlayerProfile,
    leftScore: Int,
    rightScore: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    headline: String = "ROUND WINNER",
    wonByResponseTime: Boolean = false
) {
    val avatar = AvatarCatalog.get(winner.avatarIndex)
    val infinite = rememberInfiniteTransition(label = "trophyBounce")
    val bounce by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "bounce"
    )
    val scale = rememberFitScale()
    var actionsEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(ActionButtonDebounceMs)
        actionsEnabled = true
    }
    Box(Modifier.fillMaxSize().background(rememberAuroraBrush()), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp.scaledBy(scale)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(headline, color = NeonYellow, fontFamily = GameButtonFont, fontSize = 24.sp.scaledBy(scale))
            Box(
                Modifier
                    .padding(24.dp.scaledBy(scale))
                    .size(110.dp.scaledBy(scale))
                    .scale(bounce)
                    .clip(CircleShape)
                    .background(avatar.colorHex.toColor()),
                contentAlignment = Alignment.Center
            ) { Text(avatar.emoji, fontSize = 54.sp.scaledBy(scale)) }
            Text(winner.displayName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp.scaledBy(scale))
            Text("$leftScore  -  $rightScore", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp.scaledBy(scale))
            if (wonByResponseTime) {
                Spacer(Modifier.size(4.dp.scaledBy(scale)))
                Text(
                    "⚡ Tied on points - won on faster overall response time",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp.scaledBy(scale)
                )
            }
            Spacer(Modifier.size(24.dp.scaledBy(scale)))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp.scaledBy(scale))) {
                Button(onClick = onPrimary, enabled = actionsEnabled) { Text(primaryLabel) }
                if (secondaryLabel != null && onSecondary != null) {
                    OutlinedButton(
                        onClick = onSecondary,
                        enabled = actionsEnabled,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text(secondaryLabel) }
                }
            }
        }
    }
}
