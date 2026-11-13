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
    // Kept intentionally wide (not just a handful of families) so the EMOJI category
    // doesn't start repeating itself after a few rounds.
    val emojiFamilies: List<List<String>> = listOf(
        listOf("😀", "😃", "😄", "😁"), // grins
        listOf("🙂", "🙃", "☹️", "😐"), // slight/upside-down/frown/neutral
        listOf("😂", "🤣", "😅", "😆"), // laughing family
        listOf("😢", "😭", "😿", "🥺"), // crying family
        listOf("⬆️", "⬇️", "⬅️", "➡️"), // arrows
        listOf("❤️", "🧡", "💛", "💚", "💙", "💜"), // hearts
        listOf("🐱", "🐶", "🦊", "🐼"), // cat/dog/fox/panda faces
        listOf("🐸", "🐵", "🐰", "🐻", "🐨"), // frog/monkey/rabbit/bear/koala faces
        listOf("🐠", "🐡", "🦈", "🐙", "🦑"), // sea creatures
        listOf("🐝", "🐞", "🦋", "🐛", "🐌"), // bugs
        listOf("⭐", "🌟", "💫", "✨"), // star/sparkle family
        listOf("👍", "👎", "✌️", "🤞"), // hand gestures
        listOf("🤚", "✋", "🖐️", "👋"), // hand variants
        listOf("🍎", "🍊", "🍋", "🍇", "🍓"), // fruits
        listOf("🍌", "🍍", "🥝", "🍑", "🍒"), // more fruits
        listOf("☀️", "⛅", "☁️", "🌧️", "⛈️", "❄️"), // weather
        listOf("🌑", "🌓", "🌕", "🌗"), // moon phases
        listOf("🚗", "🚕", "🚙", "🚓", "🚐"), // road vehicles
        listOf("✈️", "🚀", "🚁", "🚂", "⛵"), // other transport
        listOf("⚽", "🏀", "🏈", "⚾", "🎾"), // sports balls
        listOf("🍕", "🍔", "🍟", "🌮", "🍩"), // fast food
        listOf("☕", "🍵", "🧃", "🥤", "🍺"), // drinks
        listOf("🎸", "🎹", "🎺", "🥁", "🎷"), // instruments
        listOf("♠️", "♥️", "♦️", "♣️"), // card suits
        listOf("🕐", "🕑", "🕒", "🕓", "🕔") // clock faces - a very subtle family on purpose
    )

    val shapeSymmetryOrder: Map<ShapeKind, Int> = mapOf(
        ShapeKind.CIRCLE to 0, // infinite symmetry -> rotation is never visible
        ShapeKind.SQUARE to 4,
        ShapeKind.TRIANGLE to 3,
        ShapeKind.PENTAGON to 5,
        ShapeKind.HEXAGON to 6,
        ShapeKind.DIAMOND to 4,
        ShapeKind.STAR to 5,
        ShapeKind.HEPTAGON to 7,
        ShapeKind.OCTAGON to 8,
        ShapeKind.STAR6 to 6
    )

    val palette: List<Long> = listOf(
        0xFFFF5252L, 0xFFFF9800L, 0xFFFFD600L, 0xFF4CAF50L, 0xFF00BCD4L,
        0xFF2196F3L, 0xFF7C4DFFL, 0xFFE040FBL, 0xFFFF4081L, 0xFF8D6E63L,
        0xFF26C6DAL, 0xFF9CCC65L, 0xFFFF6E40L, 0xFF64DD17L, 0xFF00B8D4L,
        0xFF6200EAL, 0xFFC51162L, 0xFFAEEA00L
    )
}
