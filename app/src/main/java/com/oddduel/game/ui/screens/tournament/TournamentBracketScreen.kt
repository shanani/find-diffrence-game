package com.oddduel.game.ui.screens.tournament

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.oddduel.game.data.Match
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.data.Tournament
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonGreen
import com.oddduel.game.ui.theme.NeonYellow

/**
 * The whole bracket at a glance: every round, every match, every winner so far -
 * shown as the champion screen when the tournament finishes, and reachable as a
 * "View Bracket" peek from any round-result screen in between.
 */
@Composable
fun TournamentBracketScreen(tournament: Tournament, isFinal: Boolean, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize().background(BgDeep).padding(20.dp)) {
        if (isFinal && tournament.champion != null) {
            ChampionBanner(tournament.champion!!)
            Spacer(Modifier.height(12.dp))
        } else {
            Text("Tournament Bracket", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            Spacer(Modifier.height(12.dp))
        }

        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            tournament.rounds.forEachIndexed { roundIndex, matches ->
                val roundLabel = when (roundIndex) {
                    tournament.totalRounds - 1 -> "Final"
                    tournament.totalRounds - 2 -> "Semifinals"
                    else -> "Round ${roundIndex + 1}"
                }
                Column(
                    Modifier
                        .width(220.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(roundLabel, color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    matches.forEach { match -> MatchCard(match) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onDone) { Text(if (isFinal) "Done" else "Continue") }
    }
}

@Composable
private fun ChampionBanner(champion: PlayerProfile) {
    val avatar = AvatarCatalog.get(champion.avatarIndex)
    val infinite = rememberInfiniteTransition(label = "championBounce")
    val bounce by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "bounce"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(56.dp).scale(bounce).clip(CircleShape).background(avatar.colorHex.toColor()),
            contentAlignment = Alignment.Center
        ) { Text(avatar.emoji, fontSize = 28.sp) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("🏆 CHAMPION", color = NeonYellow, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(champion.displayName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun MatchCard(match: Match) {
    val hasResult = match.winner != null
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgPanel)
            .padding(10.dp)
    ) {
        MatchPlayerRow(match.p1, match.score1, isWinner = hasResult && match.winner == match.p1, hasResult = hasResult)
        Spacer(Modifier.height(6.dp))
        MatchPlayerRow(match.p2, match.score2, isWinner = hasResult && match.winner == match.p2, hasResult = hasResult)
    }
}

@Composable
private fun MatchPlayerRow(player: PlayerProfile?, score: Int, isWinner: Boolean, hasResult: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (player == null) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
            Spacer(Modifier.width(8.dp))
            Text("—", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp, modifier = Modifier.weight(1f))
        } else {
            val avatar = AvatarCatalog.get(player.avatarIndex)
            Box(Modifier.size(22.dp).clip(CircleShape).background(avatar.colorHex.toColor()), contentAlignment = Alignment.Center) {
                Text(avatar.emoji, fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                player.displayName,
                color = if (isWinner) NeonGreen else Color.White.copy(alpha = 0.85f),
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            if (hasResult) {
                Text(score.toString(), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}
