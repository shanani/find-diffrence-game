package com.oddduel.game.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oddduel.game.data.AvatarCatalog
import com.oddduel.game.data.PlayerProfile
import com.oddduel.game.ui.components.toColor
import com.oddduel.game.ui.screens.accounts.AccountsViewModel
import com.oddduel.game.ui.theme.BgPanel
import com.oddduel.game.ui.theme.GameButtonFont
import com.oddduel.game.ui.theme.NeonCyan
import com.oddduel.game.ui.theme.PlayerLeftAccent
import com.oddduel.game.ui.theme.PlayerRightAccent
import com.oddduel.game.ui.theme.rememberAuroraBrush

@Composable
fun QuickPlaySetupScreen(
    accountsViewModel: AccountsViewModel = viewModel(),
    onBack: () -> Unit,
    onStart: (PlayerProfile, PlayerProfile) -> Unit
) {
    val users by accountsViewModel.users.collectAsState()
    var left by remember { mutableStateOf<PlayerProfile>(PlayerProfile.Guest("Player 1", 0)) }
    var right by remember { mutableStateOf<PlayerProfile>(PlayerProfile.Guest("Player 2", 1)) }

    Column(Modifier.fillMaxSize().background(rememberAuroraBrush()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Quick Play", fontFamily = GameButtonFont, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text("Pick who's playing on each side - a saved player or a guest.", fontSize = 13.sp, color = NeonCyan)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PlayerSlotPicker(
                title = "Left Player",
                accent = PlayerLeftAccent,
                users = users,
                guestDefault = PlayerProfile.Guest("Player 1", 0),
                selection = left,
                onSelect = { left = it },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            PlayerSlotPicker(
                title = "Right Player",
                accent = PlayerRightAccent,
                users = users,
                guestDefault = PlayerProfile.Guest("Player 2", 1),
                selection = right,
                onSelect = { right = it },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onStart(left, right) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start Duel") }
    }
}

@Composable
private fun PlayerSlotPicker(
    title: String,
    accent: Color,
    users: List<com.oddduel.game.data.User>,
    guestDefault: PlayerProfile.Guest,
    selection: PlayerProfile,
    onSelect: (PlayerProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accent)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            item {
                SelectableRow(
                    avatarEmoji = AvatarCatalog.get(guestDefault.avatarIndex).emoji,
                    avatarColor = AvatarCatalog.get(guestDefault.avatarIndex).colorHex.toColor(),
                    label = "Guest",
                    selected = selection is PlayerProfile.Guest,
                    onClick = { onSelect(guestDefault) }
                )
            }
            items(users, key = { it.id }) { user ->
                val avatar = AvatarCatalog.get(user.avatarIndex)
                SelectableRow(
                    avatarEmoji = avatar.emoji,
                    avatarColor = avatar.colorHex.toColor(),
                    label = user.name,
                    selected = selection is PlayerProfile.Registered && selection.user.id == user.id,
                    onClick = { onSelect(PlayerProfile.Registered(user)) }
                )
            }
        }
    }
}

@Composable
private fun SelectableRow(avatarEmoji: String, avatarColor: Color, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(alpha = 0.16f) else BgPanel)
            .border(width = if (selected) 2.dp else 0.dp, color = Color.White, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(avatarColor), contentAlignment = Alignment.Center) {
            Text(avatarEmoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Medium)
    }
}
