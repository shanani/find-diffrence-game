package com.oddduel.game.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun UpdateAvailableDialog(info: UpdateInfo, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update available") },
        text = {
            Text(
                "A new version (${info.versionName}) is ready to install." +
                    if (info.releaseNotes.isNotBlank()) "\n\n${info.releaseNotes}" else ""
            )
        },
        confirmButton = {
            Button(onClick = onUpdate) { Text("Update Now") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Later") }
        }
    )
}
