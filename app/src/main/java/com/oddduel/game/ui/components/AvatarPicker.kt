package com.oddduel.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oddduel.game.data.AvatarCatalog

@Composable
fun AvatarPicker(selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        userScrollEnabled = false
    ) {
        items(AvatarCatalog.all.size) { index ->
            val avatar = AvatarCatalog.all[index]
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(avatar.colorHex.toColor())
                    .border(width = if (selected) 3.dp else 0.dp, color = Color.White, shape = CircleShape)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(avatar.emoji, fontSize = 24.sp)
            }
        }
    }
}
