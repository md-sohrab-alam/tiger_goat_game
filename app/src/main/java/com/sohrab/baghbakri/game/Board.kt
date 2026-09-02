package com.sohrab.baghbakri.game

/**
 * 5×5 Alquerque-style board used in Bagh Chal / Bagh Bakri.
 * Points connect horizontally, vertically, and diagonally to adjacent intersections.
 */
object Board {
    const val SIZE = 5
    const val POINT_COUNT = SIZE * SIZE
    const val TIGER_COUNT = 4
    const val GOAT_COUNT = 20
    const val GOATS_TO_CAPTURE_FOR_TIGER_WIN = 5

    /** Corner positions where tigers start: (0,0), (0,4), (4,0), (4,4). */
    val TIGER_START_POSITIONS: Set<Int> = setOf(0, 4, 20, 24)

    private val neighbors: List<List<Int>> = buildAdjacency()

    fun neighborsOf(index: Int): List<Int> = neighbors[index]

    fun areAdjacent(from: Int, to: Int): Boolean = to in neighbors[from]

    fun isCorner(index: Int): Boolean = index in TIGER_START_POSITIONS

    /**
     * Returns landing index if [tigerIndex] can capture a goat at [goatIndex] by jumping.
     * Null when the jump is not legal.
     */
    fun captureLanding(tigerIndex: Int, goatIndex: Int): Int? {
        if (!areAdjacent(tigerIndex, goatIndex)) return null

        val tiger = Position.fromIndex(tigerIndex)
        val goat = Position.fromIndex(goatIndex)
        val dRow = goat.row - tiger.row
        val dCol = goat.col - tiger.col
        val landingRow = goat.row + dRow
        val landingCol = goat.col + dCol

        if (landingRow !in 0 until SIZE || landingCol !in 0 until SIZE) return null

        val landingIndex = Position(landingRow, landingCol).index
        return landingIndex.takeIf { areAdjacent(goatIndex, landingIndex) }
    }

    private fun buildAdjacency(): List<List<Int>> {
        val adjacency = List(POINT_COUNT) { mutableListOf<Int>() }
        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val index = row * SIZE + col
                for (dRow in -1..1) {
                    for (dCol in -1..1) {
                        if (dRow == 0 && dCol == 0) continue
                        val nRow = row + dRow
                        val nCol = col + dCol
                        if (nRow in 0 until SIZE && nCol in 0 until SIZE) {
                            adjacency[index].add(nRow * SIZE + nCol)
                        }
                    }
                }
            }
        }
        return adjacency
    }
}
