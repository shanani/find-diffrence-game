package com.oddduel.game.ui.screens.tournament

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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

@Composable
fun RoundResultScreen(winner: PlayerProfile, leftScore: Int, rightScore: Int, isFinalChampion: Boolean, onContinue: () -> Unit) {
    val avatar = AvatarCatalog.get(winner.avatarIndex)
    val infinite = rememberInfiniteTransition(label = "trophyBounce")
    val bounce by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "bounce"
    )
    Box(Modifier.fillMaxSize().background(BgDeep), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isFinalChampion) "🏆 CHAMPION! 🏆" else "ROUND WINNER", color = NeonYellow, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Box(
                Modifier.padding(24.dp).size(110.dp).scale(bounce).clip(CircleShape).background(avatar.colorHex.toColor()),
                contentAlignment = Alignment.Center
            ) { Text(avatar.emoji, fontSize = 54.sp) }
            Text(winner.displayName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text("$leftScore  -  $rightScore", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
            Button(onClick = onContinue) { Text(if (isFinalChampion) "Back to Home" else "Next Round") }
        }
    }
}
