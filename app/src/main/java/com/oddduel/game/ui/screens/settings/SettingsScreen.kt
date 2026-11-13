package com.oddduel.game.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.ui.components.rememberFitScale
import com.oddduel.game.ui.components.scaledBy
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.NeonCyan

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), onBack: () -> Unit, onAbout: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    val scale = rememberFitScale()

    Column(Modifier.fillMaxSize().padding(20.dp.scaledBy(scale))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Settings", fontWeight = FontWeight.Black, fontSize = 26.sp.scaledBy(scale))
        }
        Spacer(Modifier.height(16.dp.scaledBy(scale)))

        SectionTitle("Round timeout: ${settings.roundTimeoutSeconds}s", scale)
        Slider(
            value = settings.roundTimeoutSeconds.toFloat(),
            onValueChange = { viewModel.setTimeout(it.toInt()) },
            valueRange = 15f..120f,
            steps = 20
        )

        Spacer(Modifier.height(16.dp.scaledBy(scale)))
        SectionTitle("Grid columns: ${settings.gridCols}", scale)
        Slider(
            value = settings.gridCols.toFloat(),
            onValueChange = { viewModel.setGridSize(it.toInt(), settings.gridRows) },
            valueRange = 2f..8f,
            steps = 5
        )

        SectionTitle("Grid rows: ${settings.gridRows}", scale)
        Slider(
            value = settings.gridRows.toFloat(),
            onValueChange = { viewModel.setGridSize(settings.gridCols, it.toInt()) },
            valueRange = 2f..8f,
            steps = 5
        )
        Text("${settings.gridCols * settings.gridRows} tiles per side", fontSize = 12.sp.scaledBy(scale), color = NeonCyan)

        Spacer(Modifier.height(20.dp.scaledBy(scale)))
        ToggleRow(
            label = "Music",
            checked = settings.musicEnabled,
            onCheckedChange = { viewModel.setMusicEnabled(it) },
            scale = scale
        )
        if (settings.musicEnabled) {
            Slider(value = settings.musicVolume, onValueChange = { viewModel.setMusicVolume(it) })
        }

        ToggleRow(
            label = "Sound effects",
            checked = settings.sfxEnabled,
            onCheckedChange = { viewModel.setSfxEnabled(it) },
            scale = scale
        )
        if (settings.sfxEnabled) {
            Slider(value = settings.sfxVolume, onValueChange = { viewModel.setSfxVolume(it) })
        }

        Spacer(Modifier.height(24.dp.scaledBy(scale)))
        Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) { Text("Reset All Player Scores") }

        Spacer(Modifier.height(16.dp.scaledBy(scale)))
        Button(onClick = onAbout) { Text("About") }

        Spacer(Modifier.height(24.dp.scaledBy(scale)))
        Text(
            "Music credit: W. A. Mozart - \"Eine kleine Nachtmusik\" (I. Allegro), " +
                "performed by the Advent Chamber Orchestra. Licensed CC BY-SA 2.0 " +
                "via Wikimedia Commons.",
            fontSize = 11.sp.scaledBy(scale),
            color = Color.White.copy(alpha = 0.5f)
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset all scores?") },
            text = { Text("Every saved player's best score, wins and match count will be set back to zero. This can't be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetAllScores()
                    showResetConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                Button(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String, scale: Float) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp.scaledBy(scale))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, scale: Float) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp.scaledBy(scale))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
