package com.oddduel.game.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.data.AvatarCatalog
import com.oddduel.game.data.User
import com.oddduel.game.ui.components.AvatarPicker
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonCyan

@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(),
    onBack: () -> Unit,
    onUserChosen: ((User) -> Unit)? = null
) {
    val users by viewModel.users.collectAsState()
    var name by remember { mutableStateOf("") }
    var avatarIndex by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Players", fontWeight = FontWeight.Black, fontSize = 26.sp)
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Text("New player", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                viewModel.errorMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, fontSize = 12.sp) }
                Spacer(Modifier.height(8.dp))
                AvatarPicker(selectedIndex = avatarIndex, onSelect = { avatarIndex = it }, modifier = Modifier.height(180.dp))
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.createUser(name, avatarIndex) { name = "" }
                }) { Text("Create Account") }
            }

            Column(Modifier.weight(1f).fillMaxHeight()) {
                Text("Existing players", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users, key = { it.id }) { user ->
                        UserRow(user = user, onDelete = { viewModel.deleteUser(user) }, onChoose = onUserChosen?.let { cb -> { cb(user) } })
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: User, onDelete: () -> Unit, onChoose: (() -> Unit)?) {
    val avatar = AvatarCatalog.get(user.avatarIndex)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgPanel)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(avatar.colorHex.toColor()),
            contentAlignment = Alignment.Center
        ) { Text(avatar.emoji, fontSize = 18.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.Bold)
            Text("Best ${user.bestScore} · Wins ${user.totalWins}/${user.totalMatches}", fontSize = 12.sp, color = NeonCyan)
        }
        if (onChoose != null) {
            Button(onClick = onChoose) { Text("Select") }
            Spacer(Modifier.width(6.dp))
        }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
    }
}
