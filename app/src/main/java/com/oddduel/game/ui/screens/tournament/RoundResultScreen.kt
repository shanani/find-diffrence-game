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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.NeonYellow

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
    headline: String = "ROUND WINNER"
) {
    val avatar = AvatarCatalog.get(winner.avatarIndex)
    val infinite = rememberInfiniteTransition(label = "trophyBounce")
    val bounce by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "bounce"
    )
    Box(Modifier.fillMaxSize().background(BgDeep), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(headline, color = NeonYellow, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Box(
                Modifier.padding(24.dp).size(110.dp).scale(bounce).clip(CircleShape).background(avatar.colorHex.toColor()),
                contentAlignment = Alignment.Center
            ) { Text(avatar.emoji, fontSize = 54.sp) }
            Text(winner.displayName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text("$leftScore  -  $rightScore", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            Spacer(Modifier.size(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPrimary) { Text(primaryLabel) }
                if (secondaryLabel != null && onSecondary != null) {
                    OutlinedButton(
                        onClick = onSecondary,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text(secondaryLabel) }
                }
            }
        }
    }
}
