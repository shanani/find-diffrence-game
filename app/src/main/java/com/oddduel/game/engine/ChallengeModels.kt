package com.oddduel.game.engine

enum class ChallengeCategory { SHAPE, LETTER, EMOJI, PATTERN, FACE }

enum class ShapeKind { CIRCLE, SQUARE, TRIANGLE, PENTAGON, HEXAGON, STAR, DIAMOND, HEPTAGON, OCTAGON, STAR6 }

enum class DifferenceType {
    ROTATE, FLIP_H, FLIP_V, COLOR_SHIFT, SCALE, STYLE, GLYPH_SWAP, PATTERN_MOVE, PATTERN_REMOVE, PATTERN_ADD,
    // FACE_IDENTITY: the odd cell is simply a different kid (easiest - no two faces look alike).
    // FACE_TRANSFORM: same photo everywhere, odd cell rotated/mirrored (medium - a real face
    // is never left-right symmetric, so the transform is always visible on close looking).
    // FACE_EXPRESSION: same kid, same pose, odd cell swapped to their other captured expression
    // (hardest - the only difference is what's on the face itself).
    FACE_IDENTITY, FACE_TRANSFORM, FACE_EXPRESSION
}

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
    val fontWeightBold: Boolean = false,
    val faceDrawableRes: Int = 0 // only for ChallengeCategory.FACE; an @drawable id, 0 = none
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
