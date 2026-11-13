package com.oddduel.game.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.analytics.DifficultyAnalytics
import com.oddduel.game.ui.theme.DangerRed
import com.oddduel.game.ui.theme.GameButtonFont
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.rememberAuroraBrush
import kotlinx.coroutines.launch

/**
 * Settings is a plain scrolling list, not a fixed-layout screen scaled to fit (like
 * Home or a result screen) - it has a Slider's native, non-shrinkable touch-target
 * height mixed in, and a scrolling settings list is itself the normal, expected
 * pattern (every settings screen anywhere works this way), not a bug to design around.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), onBack: () -> Unit, onAbout: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(rememberAuroraBrush())
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Settings", fontFamily = GameButtonFont, fontSize = 22.sp)
        }
        Spacer(Modifier.height(16.dp))

        SectionTitle("Round timeout: ${settings.roundTimeoutSeconds}s")
        Slider(
            value = settings.roundTimeoutSeconds.toFloat(),
            onValueChange = { viewModel.setTimeout(it.toInt()) },
            valueRange = 15f..120f,
            steps = 20
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle("Grid columns: ${settings.gridCols}")
        Slider(
            value = settings.gridCols.toFloat(),
            onValueChange = { viewModel.setGridSize(it.toInt(), settings.gridRows) },
            valueRange = 2f..8f,
            steps = 5
        )

        SectionTitle("Grid rows: ${settings.gridRows}")
        Slider(
            value = settings.gridRows.toFloat(),
            onValueChange = { viewModel.setGridSize(settings.gridCols, it.toInt()) },
            valueRange = 2f..8f,
            steps = 5
        )
        Text("${settings.gridCols * settings.gridRows} tiles per side", fontSize = 12.sp, color = NeonCyan)

        Spacer(Modifier.height(20.dp))
        ToggleRow(
            label = "Music",
            checked = settings.musicEnabled,
            onCheckedChange = { viewModel.setMusicEnabled(it) }
        )
        if (settings.musicEnabled) {
            Slider(value = settings.musicVolume, onValueChange = { viewModel.setMusicVolume(it) })
        }

        ToggleRow(
            label = "Sound effects",
            checked = settings.sfxEnabled,
            onCheckedChange = { viewModel.setSfxEnabled(it) }
        )
        if (settings.sfxEnabled) {
            Slider(value = settings.sfxVolume, onValueChange = { viewModel.setSfxVolume(it) })
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) { Text("Reset All Player Scores") }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onAbout) { Text("About") }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    val json = DifficultyAnalytics.readStatsJson(context)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Odd One Out Duel - gameplay stats")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share gameplay stats"))
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text("Share Gameplay Stats") }
        Text(
            "A local-only record of how long serious solo sessions take, used only to " +
                "help tune difficulty in future updates - never read by the game itself, " +
                "and never sent anywhere unless you tap this and choose where.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "Music credit: W. A. Mozart - \"Eine kleine Nachtmusik\" (I. Allegro), " +
                "performed by the Advent Chamber Orchestra. Licensed CC BY-SA 2.0 " +
                "via Wikimedia Commons.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            "Display font: \"Bungee\" by David Jonathan Ross, licensed under the SIL " +
                "Open Font License 1.1.",
            fontSize = 11.sp,
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
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
