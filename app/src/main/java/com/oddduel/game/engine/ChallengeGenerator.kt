package com.oddduel.game.engine

import kotlin.random.Random

/**
 * Procedural "odd one out" generator.
 *
 * Every challenge is built the same way: pick a random content category, build one
 * base [CellStyle] template, stamp it into every grid cell, then mutate exactly ONE
 * cell with a difference that is guaranteed - by construction, not by luck - to be
 * perceivable: rotations skip angles that coincide with a shape's own rotational
 * symmetry, flips are refused on glyphs/positions that are mirror-symmetric, and every
 * category always keeps at least one "universal fallback" difference type (color shift
 * for shapes/letters/patterns, a sibling-glyph swap for emoji) so generation can never
 * produce an invisible, unfair round.
 *
 * [difficultyTier] (1 = gentle, 5 = razor subtle) only scales *how large* a difference
 * is, never whether it is visible at all.
 */
object ChallengeGenerator {

    private val rotate90Map = mapOf(0 to 2, 1 to 5, 2 to 8, 3 to 1, 4 to 4, 5 to 7, 6 to 0, 7 to 3, 8 to 6)
    private val rotate180Map = mapOf(0 to 8, 1 to 7, 2 to 6, 3 to 5, 4 to 4, 5 to 3, 6 to 2, 7 to 1, 8 to 0)
    private val flipHMap = mapOf(0 to 2, 1 to 1, 2 to 0, 3 to 5, 4 to 4, 5 to 3, 6 to 8, 7 to 7, 8 to 6)
    private val flipVMap = mapOf(0 to 6, 1 to 7, 2 to 8, 3 to 3, 4 to 4, 5 to 5, 6 to 0, 7 to 1, 8 to 2)
    private val nonCenterPositions = (0..8).filter { it != 4 }

    fun generate(rows: Int, cols: Int, difficultyTier: Int = 1, random: Random = Random.Default): Challenge {
        val tier = difficultyTier.coerceIn(1, 5)
        val category = ChallengeCategory.entries.random(random)
        val cellCount = (rows * cols).coerceAtLeast(2)
        val oddIndex = random.nextInt(cellCount)

        val (base, family) = buildBaseTemplate(category, random)
        val (oddStyle, diffType) = mutate(base, category, family, tier, random)

        val cells = MutableList(cellCount) { base }
        cells[oddIndex] = oddStyle

        return Challenge(
            id = random.nextLong().toString(36) + System.nanoTime().toString(36),
            rows = rows,
            cols = cols,
            cells = cells,
            oddIndex = oddIndex,
            category = category,
            differenceType = diffType
        )
    }

    private fun buildBaseTemplate(category: ChallengeCategory, random: Random): Pair<CellStyle, List<String>> {
        return when (category) {
            ChallengeCategory.SHAPE -> {
                val shape = ShapeKind.entries.random(random)
                val color = GlyphPools.palette.random(random)
                CellStyle(category = category, shape = shape, colorArgb = color) to emptyList()
            }
            ChallengeCategory.LETTER -> {
                val glyph = GlyphPools.letterPool.random(random).toString()
                val color = GlyphPools.palette.random(random)
                CellStyle(category = category, glyph = glyph, colorArgb = color) to emptyList()
            }
            ChallengeCategory.EMOJI -> {
                val family = GlyphPools.emojiFamilies.random(random)
                val glyph = family.random(random)
                CellStyle(category = category, glyph = glyph, colorArgb = 0xFFFFFFFFL) to family
            }
            ChallengeCategory.PATTERN -> {
                val pos = nonCenterPositions.random(random)
                val color = GlyphPools.palette.random(random)
                CellStyle(category = category, patternHighlights = setOf(pos), colorArgb = color) to emptyList()
            }
        }
    }

    private fun mutate(
        base: CellStyle,
        category: ChallengeCategory,
        family: List<String>,
        tier: Int,
        random: Random
    ): Pair<CellStyle, DifferenceType> {
        val candidates = validDifferenceTypes(base, category, family)
        val chosen = candidates.random(random)
        val mutated = when (chosen) {
            DifferenceType.ROTATE -> base.copy(rotationDeg = pickRotation(base.shape, tier, random))
            DifferenceType.FLIP_H -> base.copy(flipX = true)
            DifferenceType.FLIP_V -> base.copy(flipY = true)
            DifferenceType.COLOR_SHIFT -> base.copy(colorArgb = pickDifferentColor(base.colorArgb, random))
            DifferenceType.SCALE -> base.copy(scale = pickScale(tier, random))
            DifferenceType.STYLE -> if (category == ChallengeCategory.LETTER) {
                base.copy(fontWeightBold = !base.fontWeightBold)
            } else {
                base.copy(style = if (base.style == RenderStyle.FILLED) RenderStyle.OUTLINED else RenderStyle.FILLED)
            }
            DifferenceType.GLYPH_SWAP -> {
                val alt = family.filter { it != base.glyph }.random(random)
                base.copy(glyph = alt)
            }
            DifferenceType.PATTERN_MOVE -> {
                val pos = base.patternHighlights.first()
                val maps = buildList {
                    add(rotate90Map); add(rotate180Map)
                    if (pos != 1 && pos != 7) add(flipHMap)
                    if (pos != 3 && pos != 5) add(flipVMap)
                }
                val map = maps.random(random)
                base.copy(patternHighlights = setOf(map.getValue(pos)))
            }
            DifferenceType.PATTERN_REMOVE -> base.copy(patternHighlights = emptySet())
            DifferenceType.PATTERN_ADD -> {
                val extra = nonCenterPositions.filter { it !in base.patternHighlights }.random(random)
                base.copy(patternHighlights = base.patternHighlights + extra)
            }
        }
        return mutated to chosen
    }

    private fun validDifferenceTypes(base: CellStyle, category: ChallengeCategory, family: List<String>): List<DifferenceType> {
        return when (category) {
            ChallengeCategory.SHAPE -> buildList {
                if ((base.shape?.let { GlyphPools.shapeSymmetryOrder[it] } ?: 0) > 0) add(DifferenceType.ROTATE)
                add(DifferenceType.COLOR_SHIFT)
                add(DifferenceType.SCALE)
                add(DifferenceType.STYLE)
            }
            ChallengeCategory.LETTER -> buildList {
                add(DifferenceType.ROTATE)
                val c = base.glyph?.firstOrNull() ?: ' '
                if (c !in GlyphPools.flipHSymmetric) add(DifferenceType.FLIP_H)
                if (c !in GlyphPools.flipVSymmetric) add(DifferenceType.FLIP_V)
                add(DifferenceType.COLOR_SHIFT)
                add(DifferenceType.SCALE)
                add(DifferenceType.STYLE)
            }
            ChallengeCategory.EMOJI -> buildList {
                if (family.size > 1) add(DifferenceType.GLYPH_SWAP)
                add(DifferenceType.SCALE)
                add(DifferenceType.ROTATE)
            }
            ChallengeCategory.PATTERN -> buildList {
                add(DifferenceType.PATTERN_MOVE)
                add(DifferenceType.COLOR_SHIFT)
                add(DifferenceType.PATTERN_REMOVE)
                add(DifferenceType.PATTERN_ADD)
            }
        }
    }

    private fun pickRotation(shape: ShapeKind?, tier: Int, random: Random): Float {
        val symmetryOrder = shape?.let { GlyphPools.shapeSymmetryOrder[it] } ?: 0
        val (minDelta, maxDelta) = rotationRangeForTier(tier)
        if (symmetryOrder <= 0) {
            val delta = minDelta + random.nextFloat() * (maxDelta - minDelta)
            return if (random.nextBoolean()) delta else -delta
        }
        val symmetryAngle = 360f / symmetryOrder
        repeat(12) {
            val delta = minDelta + random.nextFloat() * (maxDelta - minDelta)
            val signed = if (random.nextBoolean()) delta else -delta
            val mod = ((signed % symmetryAngle) + symmetryAngle) % symmetryAngle
            if (mod > 10f && mod < symmetryAngle - 10f) return signed
        }
        // Fallback: half of the symmetry angle is always maximally visible.
        return symmetryAngle / 2f
    }

    private fun rotationRangeForTier(tier: Int): Pair<Float, Float> = when (tier) {
        1 -> 45f to 80f
        2 -> 35f to 65f
        3 -> 28f to 55f
        4 -> 22f to 45f
        else -> 18f to 35f
    }

    private fun pickScale(tier: Int, random: Random): Float {
        val delta = when (tier) {
            1 -> 0.30f + random.nextFloat() * 0.15f
            2 -> 0.24f + random.nextFloat() * 0.12f
            3 -> 0.20f + random.nextFloat() * 0.10f
            4 -> 0.16f + random.nextFloat() * 0.08f
            else -> 0.13f + random.nextFloat() * 0.06f
        }
        return if (random.nextBoolean()) 1f + delta else 1f - delta
    }

    private fun pickDifferentColor(current: Long, random: Random): Long {
        var next = GlyphPools.palette.random(random)
        var guard = 0
        while (next == current && guard < 10) {
            next = GlyphPools.palette.random(random)
            guard++
        }
        return next
    }
}
