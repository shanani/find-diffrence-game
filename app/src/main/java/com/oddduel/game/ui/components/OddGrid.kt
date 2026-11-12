package com.oddduel.game.ui.components

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oddduel.game.engine.Challenge

@Composable
fun OddGrid(challenge: Challenge, enabled: Boolean, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(challenge.cols),
        modifier = modifier,
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
    ) {
        items(challenge.cells.size) { index ->
            OddGridCell(
                style = challenge.cells[index],
                highlight = false,
                enabled = enabled,
                onTap = { onTap(index) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
            )
        }
    }
}
