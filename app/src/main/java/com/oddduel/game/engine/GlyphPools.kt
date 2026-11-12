package com.oddduel.game.engine

/**
 * Curated content pools plus the symmetry metadata the generator needs to guarantee
 * every produced "odd one" is actually visible to the eye, never an invisible no-op
 * mutation (e.g. flipping a letter that is mirror-symmetric).
 */
object GlyphPools {

    // Letters/digits whose glyph does NOT change when mirrored left-right -
    // flipping them horizontally would be an invisible, unfair difference.
    val flipHSymmetric = setOf('A', 'H', 'I', 'M', 'O', 'T', 'U', 'V', 'W', 'X', 'Y', '0', '1', '8')

    // Letters/digits whose glyph does NOT change when mirrored top-bottom.
    val flipVSymmetric = setOf('B', 'C', 'D', 'E', 'H', 'I', 'K', 'O', 'X', '0', '1', '3', '8')

    val letterPool: List<Char> = ('A'..'Z').toList() + ('0'..'9').toList()

    // Small families of visually related emoji. A GLYPH_SWAP odd-one picks a
    // different member of the same family so the swap always reads as "close but off".
    val emojiFamilies: List<List<String>> = listOf(
        listOf("😀", "😃", "😄", "😁"), // grins
        listOf("🙂", "🙃", "☹️", "😐"), // slight/upside-down/frown/neutral
        listOf("⬆️", "⬇️", "⬅️", "➡️"), // arrows
        listOf("❤️", "🧡", "💛", "💚", "💙", "💜"), // hearts
        listOf("🐱", "🐶", "🦊", "🐼"), // cat/dog/fox/panda faces
        listOf("⭐", "🌟", "💫", "✨"), // star/sparkle family
        listOf("👍", "👎", "✌️", "🤞") // hands
    )

    val shapeSymmetryOrder: Map<ShapeKind, Int> = mapOf(
        ShapeKind.CIRCLE to 0, // infinite symmetry -> rotation is never visible
        ShapeKind.SQUARE to 4,
        ShapeKind.TRIANGLE to 3,
        ShapeKind.PENTAGON to 5,
        ShapeKind.HEXAGON to 6,
        ShapeKind.DIAMOND to 4,
        ShapeKind.STAR to 5
    )

    val palette: List<Long> = listOf(
        0xFFFF5252L, 0xFFFF9800L, 0xFFFFD600L, 0xFF4CAF50L, 0xFF00BCD4L,
        0xFF2196F3L, 0xFF7C4DFFL, 0xFFE040FBL, 0xFFFF4081L, 0xFF8D6E63L,
        0xFF26C6DAL, 0xFF9CCC65L
    )
}
