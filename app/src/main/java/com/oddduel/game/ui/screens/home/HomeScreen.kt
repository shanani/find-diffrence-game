package com.oddduel.game.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.components.rememberFitScale
import com.oddduel.game.ui.components.scaledBy
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.FestiveAccents
import com.oddduel.game.ui.theme.GameButtonFont
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonPink
import com.oddduel.game.ui.theme.rememberAuroraBrush

@Composable
fun HomeScreen(
    onQuickPlay: () -> Unit,
    onTournament: () -> Unit,
    onSolo: () -> Unit,
    onAccounts: () -> Unit,
    onSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    var showExitConfirm by remember { mutableStateOf(false) }
    BackHandler { showExitConfirm = true }

    // Scaling (instead of scrolling or letting it clip) is what actually keeps every
    // menu button on screen at once on a short display, while a roomy TV/tablet still
    // sees the full, unscaled design.
    val scale = rememberFitScale()

    Box(
        Modifier
            .fillMaxSize()
            .background(rememberAuroraBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp.scaledBy(scale)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ODD ONE OUT",
                color = NeonCyan,
                fontFamily = GameButtonFont,
                fontSize = 40.sp.scaledBy(scale)
            )
            Text(
                "D U E L",
                color = NeonPink,
                fontFamily = GameButtonFont,
                fontSize = 24.sp.scaledBy(scale)
            )
            Spacer(Modifier.height(40.dp.scaledBy(scale)))

            MenuButton("▶  Quick Play (Guest)", Icons.Filled.PlayArrow, onQuickPlay, scale, FestiveAccents[0])
            Spacer(Modifier.height(16.dp.scaledBy(scale)))
            MenuButton("Tournament", Icons.Filled.EmojiEvents, onTournament, scale, FestiveAccents[1])
            Spacer(Modifier.height(16.dp.scaledBy(scale)))
            MenuButton("Solo Challenges", Icons.Filled.Bolt, onSolo, scale, FestiveAccents[2])
            Spacer(Modifier.height(16.dp.scaledBy(scale)))
            MenuButton("Players", Icons.Filled.Group, onAccounts, scale, FestiveAccents[0])
            Spacer(Modifier.height(16.dp.scaledBy(scale)))
            MenuButton("Settings", Icons.Filled.Settings, onSettings, scale, FestiveAccents[1])
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Exit Odd One Out Duel?") },
            text = { Text("The app will close completely.") },
            confirmButton = {
                Button(
                    onClick = onExitApp,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Exit") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MenuButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    scale: Float,
    accent: Color
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent),
        modifier = Modifier.width(320.dp.scaledBy(scale)).height(60.dp.scaledBy(scale))
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp.scaledBy(scale)))
        Text(label, fontSize = 18.sp.scaledBy(scale))
    }
}
