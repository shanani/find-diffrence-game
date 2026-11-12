package com.oddduel.game.ui.screens.tournament

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.components.AvatarPicker
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.screens.accounts.AccountsViewModel
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.NeonPink

@Composable
fun TournamentSetupScreen(
    accountsViewModel: AccountsViewModel = viewModel(),
    onBack: () -> Unit,
    onStart: (List<PlayerProfile>) -> Unit
) {
    val roster = remember { mutableStateListOf<PlayerProfile>() }
    var guestName by remember { mutableStateOf("") }
    var avatarIndex by remember { mutableIntStateOf(0) }
    val users by accountsViewModel.users.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Tournament Setup", fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("Any number of players can join - they'll be bracketed into head-to-head rounds until one champion remains.", fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Add a guest player", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = guestName, onValueChange = { guestName = it }, label = { Text("Guest name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AvatarPicker(selectedIndex = avatarIndex, onSelect = { avatarIndex = it }, modifier = Modifier.height(140.dp))
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    val label = guestName.trim().ifEmpty { "Guest ${roster.size + 1}" }
                    roster.add(PlayerProfile.Guest(label, avatarIndex))
                    guestName = ""
                    avatarIndex = (avatarIndex + 1) % AvatarCatalog.all.size
                }) { Text("Add Guest") }

                Spacer(Modifier.height(16.dp))
                Text("Add a saved player", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.height(140.dp)) {
                    items(users, key = { it.id }) { user ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatar = AvatarCatalog.get(user.avatarIndex)
                            Box(Modifier.size(28.dp).clip(CircleShape).background(avatar.colorHex.toColor()))
                            Spacer(Modifier.width(8.dp))
                            Text(user.name, modifier = Modifier.weight(1f))
                            Button(onClick = { roster.add(PlayerProfile.Registered(user)) }) { Text("Add") }
                        }
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Text("Roster (${roster.size})", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roster.size) { index ->
                        val player = roster[index]
                        val avatar = AvatarCatalog.get(player.avatarIndex)
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BgPanel).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(28.dp).clip(CircleShape).background(avatar.colorHex.toColor()))
                            Spacer(Modifier.width(8.dp))
                            Text(player.displayName, modifier = Modifier.weight(1f))
                            IconButton(onClick = { roster.removeAt(index) }) { Icon(Icons.Filled.Close, contentDescription = "Remove") }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onStart(roster.toList()) },
                    enabled = roster.size >= 2
                ) { Text(if (roster.size < 2) "Add at least 2 players" else "Start Tournament (${roster.size})") }
                if (roster.size >= 2) {
                    Spacer(Modifier.height(4.dp))
                    Text("Single elimination · byes auto-advance", fontSize = 11.sp, color = NeonPink)
                }
            }
        }
    }
}
