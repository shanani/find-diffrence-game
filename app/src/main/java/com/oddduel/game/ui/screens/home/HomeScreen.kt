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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.ui.theme.BgDeep
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonPink
import com.oddduel.game.ui.theme.NeonPurple

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

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, NeonPurple.copy(alpha = 0.25f), BgDeep))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ODD ONE OUT",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 46.sp
            )
            Text(
                "D U E L",
                color = NeonPink,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
            Spacer(Modifier.height(40.dp))

            MenuButton("▶  Quick Play (Guest)", Icons.Filled.PlayArrow, onQuickPlay)
            Spacer(Modifier.height(16.dp))
            MenuButton("Tournament", Icons.Filled.EmojiEvents, onTournament)
            Spacer(Modifier.height(16.dp))
            MenuButton("Solo Challenges", Icons.Filled.Bolt, onSolo)
            Spacer(Modifier.height(16.dp))
            MenuButton("Players", Icons.Filled.Group, onAccounts)
            Spacer(Modifier.height(16.dp))
            MenuButton("Settings", Icons.Filled.Settings, onSettings)
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
private fun MenuButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
        modifier = Modifier.width(320.dp).height(60.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
