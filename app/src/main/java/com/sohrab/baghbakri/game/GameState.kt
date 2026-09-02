package com.sohrab.baghbakri.game

data class GameState(
    val tigers: Set<Int>,
    val goats: Set<Int>,
    val goatsRemainingToPlace: Int,
    val capturedGoats: Int,
    val phase: GamePhase,
    val currentTurn: PlayerSide,
    val winner: PlayerSide? = null,
    /** Board position hashes seen in movement phase — prevents infinite loops. */
    val positionHistory: Set<Long> = emptySet()
) {
    val goatsOnBoard: Int get() = goats.size
    val goatsLeft: Int get() = goatsRemainingToPlace

    fun isOccupied(index: Int): Boolean = index in tigers || index in goats

    fun pieceAt(index: Int): PlayerSide? = when {
        index in tigers -> PlayerSide.TIGER
        index in goats -> PlayerSide.GOAT
        else -> null
    }

    fun isOver(): Boolean = winner != null

    companion object {
        fun newGame(): GameState = GameState(
            tigers = Board.TIGER_START_POSITIONS,
            goats = emptySet(),
            goatsRemainingToPlace = Board.GOAT_COUNT,
            capturedGoats = 0,
            phase = GamePhase.PLACEMENT,
            currentTurn = PlayerSide.GOAT
        )
    }
}
