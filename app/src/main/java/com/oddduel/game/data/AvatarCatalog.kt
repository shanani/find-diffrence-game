package com.oddduel.game.data

/**
 * Fixed set of 15 built-in avatars. Rendered as emoji glyphs on a colored disc so the
 * game needs no network access or bundled images to work fully offline on a TV.
 */
data class Avatar(val emoji: String, val colorHex: Long)

object AvatarCatalog {
    val all: List<Avatar> = listOf(
        Avatar("🦁", 0xFFFF7043L), // lion
        Avatar("🦊", 0xFFFF8A65L), // fox
        Avatar("🐱", 0xFFFFCA28L), // cat
        Avatar("🐶", 0xFFA1887FL), // dog
        Avatar("🐼", 0xFF90A4AEL), // panda
        Avatar("🦉", 0xFF8D6E63L), // owl
        Avatar("🐧", 0xFF4FC3F7L), // penguin
        Avatar("🐸", 0xFF66BB6AL), // frog
        Avatar("🦄", 0xFFBA68C8L), // unicorn
        Avatar("🐙", 0xFFEC407AL), // octopus
        Avatar("🦖", 0xFF26A69AL), // dinosaur
        Avatar("🦋", 0xFFFFB74DL), // butterfly
        Avatar("🐯", 0xFFF57C00L), // tiger
        Avatar("🐨", 0xFF6D4C41L), // bear
        Avatar("🦚", 0xFF42A5F5L)  // unicorn alt / peacock
    )

    fun get(index: Int): Avatar = all[index.coerceIn(0, all.size - 1)]
}
