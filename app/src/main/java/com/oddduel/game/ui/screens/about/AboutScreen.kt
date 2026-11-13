package com.oddduel.game.ui.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.oddduel.game.BuildConfig
import com.oddduel.game.update.UpdateChecker
import com.oddduel.game.update.UpdateInfo
import com.oddduel.game.update.UpdateInstaller
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.NeonGreen
import kotlinx.coroutines.launch

private sealed class CheckState {
    data object Checking : CheckState()
    data object UpToDate : CheckState()
    data class Available(val info: UpdateInfo) : CheckState()
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<CheckState>(CheckState.Checking) }

    fun runCheck() {
        state = CheckState.Checking
        scope.launch {
            val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
            state = if (info != null) CheckState.Available(info) else CheckState.UpToDate
        }
    }

    LaunchedEffect(Unit) { runCheck() }

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("About", fontWeight = FontWeight.Black, fontSize = 26.sp)
        }
        Spacer(Modifier.height(24.dp))

        Text("Odd One Out Duel", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))

        when (val current = state) {
            is CheckState.Checking -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Checking for updates...", fontSize = 14.sp)
                }
            }
            is CheckState.UpToDate -> {
                Text("✓ You're on the latest version", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { runCheck() }) { Text("Check for Updates") }
            }
            is CheckState.Available -> {
                Text(
                    "A new version is available: ${current.info.versionName}",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (current.info.releaseNotes.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(current.info.releaseNotes, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { scope.launch { UpdateInstaller.downloadAndInstall(context, current.info) } }) {
                    Text("Update Now")
                }
            }
        }
    }
}
