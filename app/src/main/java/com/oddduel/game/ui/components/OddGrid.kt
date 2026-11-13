package com.oddduel.game.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oddduel.game.engine.Challenge

private val GridSpacing = 6.dp
private val GridContentPadding = 8.dp

/**
 * Sizes every card from BOTH the available width and height, not just the width.
 * The previous version gave each card a fixed `aspectRatio(1f)` derived only from
 * column width, which works for a square grid but, with scrolling off (there's never
 * anything to scroll to - every cell is always meant to be visible at once), silently
 * cropped the bottom rows off-screen the moment row count didn't match column count
 * (e.g. a wide 4x8 board, or a short/landscape viewport) instead of shrinking the
 * cards to fit. Measuring the real available box with BoxWithConstraints and taking
 * the smaller of width-per-column and height-per-row keeps the whole board visible,
 * uncropped, on any grid size or screen shape.
 */
@Composable
fun OddGrid(challenge: Challenge, enabled: Boolean, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val cols = challenge.cols.coerceAtLeast(1)
        val rows = challenge.rows.coerceAtLeast(1)
        val availableWidth = maxWidth - GridContentPadding * 2 - GridSpacing * (cols - 1)
        val availableHeight = maxHeight - GridContentPadding * 2 - GridSpacing * (rows - 1)
        val cellSize = minOf(availableWidth / cols, availableHeight / rows).coerceAtLeast(1.dp)

        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(GridSpacing),
            verticalArrangement = Arrangement.spacedBy(GridSpacing),
            contentPadding = PaddingValues(GridContentPadding)
        ) {
            items(challenge.cells.size) { index ->
                OddGridCell(
                    style = challenge.cells[index],
                    highlight = false,
                    enabled = enabled,
                    onTap = { onTap(index) },
                    modifier = Modifier.size(cellSize)
                )
            }
        }
    }
}
