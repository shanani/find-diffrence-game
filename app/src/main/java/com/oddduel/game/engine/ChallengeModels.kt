package com.oddduel.game.engine

enum class ChallengeCategory { SHAPE, LETTER, EMOJI, PATTERN }

enum class ShapeKind { CIRCLE, SQUARE, TRIANGLE, PENTAGON, HEXAGON, STAR, DIAMOND, HEPTAGON, OCTAGON, STAR6 }

enum class DifferenceType { ROTATE, FLIP_H, FLIP_V, COLOR_SHIFT, SCALE, STYLE, GLYPH_SWAP, PATTERN_MOVE, PATTERN_REMOVE, PATTERN_ADD }

enum class RenderStyle { FILLED, OUTLINED }

/**
 * A fully-resolved visual description for one grid cell. The engine never touches
 * Compose types so it stays trivially unit-testable; the UI layer maps [colorArgb] etc.
 * to androidx.compose.ui.graphics.Color when drawing.
 */
data class CellStyle(
    val category: ChallengeCategory,
    val glyph: String? = null,
    val shape: ShapeKind? = null,
    val patternHighlights: Set<Int> = emptySet(), // indices 0..8 in a 3x3 minigrid
    val rotationDeg: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val scale: Float = 1f,
    val colorArgb: Long = 0xFFFFFFFFL,
    val style: RenderStyle = RenderStyle.FILLED,
    val fontWeightBold: Boolean = false
)

data class Challenge(
    val id: String,
    val rows: Int,
    val cols: Int,
    val cells: List<CellStyle>,
    val oddIndex: Int,
    val category: ChallengeCategory,
    val differenceType: DifferenceType
)
