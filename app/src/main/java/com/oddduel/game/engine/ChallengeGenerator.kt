package com.oddduel.game.engine

import kotlin.math.sqrt
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
 *
 * FACE is the one category where [difficultyTier] also shifts *which kind* of
 * difference gets picked, not just its size - see [pickFaceDifference].
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
            ChallengeCategory.FACE -> {
                val identityIndex = FaceCatalog.identities.indices.random(random)
                val state = random.nextInt(2)
                val style = CellStyle(category = category, faceDrawableRes = FaceCatalog.drawableFor(identityIndex, state))
                // Stashes (identityIndex, state) through the same "family" slot EMOJI uses for
                // its sibling-glyph list, so mutate() below can recover which photo is the base
                // without widening buildBaseTemplate's return type just for this one category.
                style to listOf(identityIndex.toString(), state.toString())
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
        if (category == ChallengeCategory.FACE) {
            val identityIndex = family[0].toInt()
            val state = family[1].toInt()
            val diffType = pickFaceDifference(tier, random)
            return applyFaceDifference(base, identityIndex, state, diffType, random) to diffType
        }
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
            // Unreachable here: FACE returns early above, and validDifferenceTypes() never
            // hands a non-FACE category one of these. Kept only so this `when` is exhaustive.
            DifferenceType.FACE_IDENTITY, DifferenceType.FACE_TRANSFORM, DifferenceType.FACE_EXPRESSION -> base
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
            // FACE is handled entirely inside mutate() via pickFaceDifference/applyFaceDifference,
            // since its three difference types are tier-weighted rather than uniformly random -
            // this branch only exists so the `when` above stays exhaustive.
            ChallengeCategory.FACE -> listOf(DifferenceType.FACE_IDENTITY, DifferenceType.FACE_TRANSFORM, DifferenceType.FACE_EXPRESSION)
        }
    }

    /**
     * Weights FACE's three sub-difficulties by [tier]: low tiers favor FACE_IDENTITY (a
     * completely different kid - unmissable), high tiers favor FACE_EXPRESSION (the same
     * kid, same pose, only their expression changed - the hardest spot in the whole game).
     */
    private fun pickFaceDifference(tier: Int, random: Random): DifferenceType {
        val weights = when (tier) {
            1 -> listOf(DifferenceType.FACE_IDENTITY to 70, DifferenceType.FACE_TRANSFORM to 25, DifferenceType.FACE_EXPRESSION to 5)
            2 -> listOf(DifferenceType.FACE_IDENTITY to 55, DifferenceType.FACE_TRANSFORM to 30, DifferenceType.FACE_EXPRESSION to 15)
            3 -> listOf(DifferenceType.FACE_IDENTITY to 35, DifferenceType.FACE_TRANSFORM to 35, DifferenceType.FACE_EXPRESSION to 30)
            4 -> listOf(DifferenceType.FACE_IDENTITY to 20, DifferenceType.FACE_TRANSFORM to 30, DifferenceType.FACE_EXPRESSION to 50)
            else -> listOf(DifferenceType.FACE_IDENTITY to 10, DifferenceType.FACE_TRANSFORM to 20, DifferenceType.FACE_EXPRESSION to 70)
        }
        var r = random.nextInt(weights.sumOf { it.second })
        for ((type, weight) in weights) {
            if (r < weight) return type
            r -= weight
        }
        return weights.last().first
    }

    private fun applyFaceDifference(
        base: CellStyle,
        identityIndex: Int,
        state: Int,
        diffType: DifferenceType,
        random: Random
    ): CellStyle = when (diffType) {
        DifferenceType.FACE_IDENTITY -> {
            var otherIndex = FaceCatalog.identities.indices.random(random)
            var guard = 0
            while (otherIndex == identityIndex && guard < 10) {
                otherIndex = FaceCatalog.identities.indices.random(random)
                guard++
            }
            base.copy(faceDrawableRes = FaceCatalog.drawableFor(otherIndex, random.nextInt(2)))
        }
        DifferenceType.FACE_TRANSFORM -> when (random.nextInt(5)) {
            0 -> base.copy(rotationDeg = 90f)
            1 -> base.copy(rotationDeg = 180f)
            2 -> base.copy(rotationDeg = 270f)
            3 -> base.copy(flipX = true)
            else -> base.copy(flipY = true)
        }
        DifferenceType.FACE_EXPRESSION -> base.copy(faceDrawableRes = FaceCatalog.drawableFor(identityIndex, 1 - state))
        else -> base
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

    // Below this Euclidean RGB distance, two palette colors read as the same color on a
    // real screen (e.g. the palette's two near-duplicate cyans are only 4 apart) - picking
    // one as the "different" color would make the odd cell invisible. 80 is comfortably
    // under every palette entry's own hue-neighbor distance, so this can never starve.
    private const val MIN_COLOR_DISTANCE = 80.0

    private fun colorDistance(a: Long, b: Long): Double {
        val dr = ((a shr 16) and 0xFFL) - ((b shr 16) and 0xFFL)
        val dg = ((a shr 8) and 0xFFL) - ((b shr 8) and 0xFFL)
        val db = (a and 0xFFL) - (b and 0xFFL)
        return sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    private fun pickDifferentColor(current: Long, random: Random): Long {
        val distinct = GlyphPools.palette.filter { colorDistance(it, current) >= MIN_COLOR_DISTANCE }
        if (distinct.isNotEmpty()) return distinct.random(random)
        // Should be unreachable given the palette's measured distances, but never fall back
        // to a near-duplicate: pick whichever palette color is furthest from the current one.
        return GlyphPools.palette.maxByOrNull { colorDistance(it, current) }!!
    }
}
