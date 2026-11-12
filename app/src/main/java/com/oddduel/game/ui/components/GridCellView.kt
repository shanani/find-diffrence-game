package com.oddduel.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oddduel.game.engine.CellStyle
import com.oddduel.game.engine.ChallengeCategory
import com.oddduel.game.engine.RenderStyle
import com.oddduel.game.engine.ShapeKind
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

fun Long.toColor(): Color = Color(this.toInt())

/**
 * Renders a single grid cell for any [ChallengeCategory]. Each cell owns its own
 * [clickable] gesture detector, which is what lets Jetpack Compose track the left
 * player's finger and the right player's finger as fully independent pointers at the
 * same time - nothing here waits on, consumes, or shares state with the sibling grid.
 */
@Composable
fun OddGridCell(style: CellStyle, highlight: Boolean, enabled: Boolean, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val bgAlpha = if (highlight) 0.22f else 0.08f
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = bgAlpha), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        val finalScaleX = style.scale * if (style.flipX) -1f else 1f
        val finalScaleY = style.scale * if (style.flipY) -1f else 1f
        when (style.category) {
            ChallengeCategory.SHAPE -> Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = style.rotationDeg, scaleX = finalScaleX, scaleY = finalScaleY)
            ) {
                ShapeCanvas(style.shape ?: ShapeKind.CIRCLE, style.colorArgb.toColor(), style.style)
            }
            ChallengeCategory.LETTER -> Text(
                text = style.glyph ?: "",
                color = style.colorArgb.toColor(),
                fontWeight = if (style.fontWeightBold) FontWeight.Black else FontWeight.Normal,
                fontSize = 40.sp,
                modifier = Modifier.graphicsLayer(rotationZ = style.rotationDeg, scaleX = finalScaleX, scaleY = finalScaleY)
            )
            ChallengeCategory.EMOJI -> Text(
                text = style.glyph ?: "",
                fontSize = 38.sp,
                modifier = Modifier.graphicsLayer(rotationZ = style.rotationDeg, scaleX = style.scale, scaleY = style.scale)
            )
            ChallengeCategory.PATTERN -> PatternCanvas(style.patternHighlights, style.colorArgb.toColor())
        }
    }
}

@Composable
private fun ShapeCanvas(shape: ShapeKind, color: Color, renderStyle: RenderStyle) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.minDimension * 0.62f
        val path = buildShapePath(shape, w)
        val offset = Offset((size.width - w) / 2f, (size.height - w) / 2f)
        translate(left = offset.x, top = offset.y) {
            if (renderStyle == RenderStyle.FILLED) {
                drawPath(path, color = color)
            } else {
                drawPath(path, color = color, style = Stroke(width = w * 0.09f))
            }
        }
    }
}

private fun buildShapePath(shape: ShapeKind, s: Float): Path = Path().apply {
    when (shape) {
        ShapeKind.CIRCLE -> addOval(androidx.compose.ui.geometry.Rect(0f, 0f, s, s))
        ShapeKind.SQUARE -> addRect(androidx.compose.ui.geometry.Rect(0f, 0f, s, s))
        ShapeKind.DIAMOND -> {
            moveTo(s / 2f, 0f); lineTo(s, s / 2f); lineTo(s / 2f, s); lineTo(0f, s / 2f); close()
        }
        ShapeKind.TRIANGLE -> polygon(s, 3, startAngleDeg = -90f)
        ShapeKind.PENTAGON -> polygon(s, 5, startAngleDeg = -90f)
        ShapeKind.HEXAGON -> polygon(s, 6, startAngleDeg = -90f)
        ShapeKind.STAR -> star(s, 5)
    }
}

private fun Path.polygon(s: Float, sides: Int, startAngleDeg: Float) {
    val r = s / 2f
    val cx = r
    val cy = r
    for (i in 0 until sides) {
        val angle = Math.toRadians((startAngleDeg + i * 360f / sides).toDouble())
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun Path.star(s: Float, points: Int) {
    val outerR = s / 2f
    val innerR = outerR * 0.42f
    val cx = outerR
    val cy = outerR
    val totalPoints = points * 2
    for (i in 0 until totalPoints) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = Math.toRadians((-90f + i * 360f / totalPoints).toDouble())
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
private fun PatternCanvas(highlights: Set<Int>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cell = size.minDimension / 4.2f
        val startX = (size.width - cell * 2) / 2f
        val startY = (size.height - cell * 2) / 2f
        for (index in 0..8) {
            val row = index / 3
            val col = index % 3
            val cx = startX + col * cell
            val cy = startY + row * cell
            val isHighlighted = index in highlights
            drawCircle(
                color = if (isHighlighted) color else Color.White.copy(alpha = 0.25f),
                radius = if (isHighlighted) cell * 0.34f else cell * 0.16f,
                center = Offset(cx, cy)
            )
        }
    }
}
