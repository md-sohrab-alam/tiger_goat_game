package com.sohrab.baghbakri.game

/**
 * A point on the 5×5 Bagh Bakri board (Alquerque-style grid).
 * Pieces sit on intersections, not inside squares.
 */
data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0 until Board.SIZE) { "Row $row out of bounds" }
        require(col in 0 until Board.SIZE) { "Col $col out of bounds" }
    }

    val index: Int get() = row * Board.SIZE + col

    companion object {
        fun fromIndex(index: Int): Position {
            require(index in 0 until Board.POINT_COUNT) { "Index $index out of bounds" }
            return Position(index / Board.SIZE, index % Board.SIZE)
        }
    }
}
