package com.sohrab.baghbakri.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BaghBakriGameTest {

    @Test
    fun newGame_startsWithTigersOnCornersAndGoatTurn() {
        val state = BaghBakriGame.newGame()

        assertEquals(Board.TIGER_START_POSITIONS, state.tigers)
        assertTrue(state.goats.isEmpty())
        assertEquals(20, state.goatsRemainingToPlace)
        assertEquals(0, state.capturedGoats)
        assertEquals(GamePhase.PLACEMENT, state.phase)
        assertEquals(PlayerSide.GOAT, state.currentTurn)
        assertNull(state.winner)
    }

    @Test
    fun placementPhase_goatCanPlaceOnEmptyIntersection() {
        val state = BaghBakriGame.newGame()
        val moves = BaghBakriGame.getLegalMoves(state)

        assertTrue(moves.all { it is Move.PlaceGoat })
        assertTrue(moves.any { (it as Move.PlaceGoat).to == 12 })

        val afterPlace = BaghBakriGame.applyMove(state, Move.PlaceGoat(12))
        assertTrue(12 in afterPlace.goats)
        assertEquals(19, afterPlace.goatsRemainingToPlace)
        assertEquals(PlayerSide.TIGER, afterPlace.currentTurn)
    }

    @Test
    fun placementPhase_goatsCannotMoveUntilAllPlaced() {
        val state = BaghBakriGame.newGame()
        val afterGoat = BaghBakriGame.applyMove(state, Move.PlaceGoat(12))

        assertTrue(BaghBakriGame.getLegalMoves(afterGoat).none { it.player == PlayerSide.GOAT })
    }

    @Test
    fun tigerCanMoveToAdjacentEmptyPoint() {
        val state = BaghBakriGame.newGame()
        val afterGoat = BaghBakriGame.applyMove(state, Move.PlaceGoat(12))

        val tigerMove = Move.Relocate(PlayerSide.TIGER, from = 0, to = 6)
        assertTrue(tigerMove in BaghBakriGame.getLegalMoves(afterGoat))

        val afterTiger = BaghBakriGame.applyMove(afterGoat, tigerMove)
        assertTrue(6 in afterTiger.tigers)
        assertFalse(0 in afterTiger.tigers)
        assertEquals(PlayerSide.GOAT, afterTiger.currentTurn)
    }

    @Test
    fun tigerCanCaptureAdjacentGoat() {
        val state = GameState(
            tigers = setOf(0),
            goats = setOf(6),
            goatsRemainingToPlace = 18,
            capturedGoats = 0,
            phase = GamePhase.PLACEMENT,
            currentTurn = PlayerSide.TIGER
        )

        val capture = Move.Relocate(PlayerSide.TIGER, from = 0, to = 12, capturedGoat = 6)
        assertTrue(capture in BaghBakriGame.getLegalMoves(state))

        val afterCapture = BaghBakriGame.applyMove(state, capture)
        assertEquals(1, afterCapture.capturedGoats)
        assertFalse(6 in afterCapture.goats)
        assertTrue(12 in afterCapture.tigers)
    }

    @Test
    fun tigersWinAfterFiveCaptures() {
        var state = GameState(
            tigers = setOf(0),
            goats = setOf(6, 7, 8, 9, 10),
            goatsRemainingToPlace = 0,
            capturedGoats = 4,
            phase = GamePhase.MOVEMENT,
            currentTurn = PlayerSide.TIGER
        )

        val capture = Move.Relocate(PlayerSide.TIGER, from = 0, to = 8, capturedGoat = 6)
        state = BaghBakriGame.applyMove(state, capture)

        assertEquals(PlayerSide.TIGER, state.winner)
        assertEquals(5, state.capturedGoats)
    }

    @Test
    fun goatsWinWhenTigersAreBlocked() {
        val state = GameState(
            tigers = setOf(12),
            goats = setOf(6, 7, 8, 11, 13, 16, 17, 18),
            goatsRemainingToPlace = 0,
            capturedGoats = 0,
            phase = GamePhase.MOVEMENT,
            currentTurn = PlayerSide.TIGER
        )

        assertTrue(BaghBakriGame.getLegalMoves(state).isEmpty())
        assertEquals(PlayerSide.GOAT, BaghBakriGame.evaluate(state).winner)
    }

    @Test
    fun phaseTransitionsToMovementWhenAllGoatsPlaced() {
        var state = BaghBakriGame.newGame()

        while (state.goatsRemainingToPlace > 0 && state.winner == null) {
            if (state.currentTurn == PlayerSide.GOAT) {
                val move = BaghBakriGame.getLegalMoves(state).first() as Move.PlaceGoat
                state = BaghBakriGame.applyMove(state, move)
            } else {
                val move = BaghBakriGame.getLegalMoves(state).first()
                state = BaghBakriGame.applyMove(state, move)
            }
        }

        assertEquals(GamePhase.MOVEMENT, state.phase)
        assertEquals(0, state.goatsRemainingToPlace)
        assertEquals(20, state.goatsOnBoard)
    }

    @Test
    fun movementPhase_repeatedBoardPositionIsIllegal() {
        var state = BaghBakriGame.newGame()

        while (state.goatsRemainingToPlace > 0 && state.winner == null) {
            state = if (state.currentTurn == PlayerSide.GOAT) {
                BaghBakriGame.applyMove(
                    state,
                    BaghBakriGame.getLegalMoves(state).first() as Move.PlaceGoat
                )
            } else {
                BaghBakriGame.applyMove(state, BaghBakriGame.getLegalMoves(state).first())
            }
        }

        require(state.phase == GamePhase.MOVEMENT)

        val firstTigerMove = BaghBakriGame.getLegalMoves(state).first() as Move.Relocate
        state = BaghBakriGame.applyMove(state, firstTigerMove)

        val undoMove = Move.Relocate(
            player = PlayerSide.TIGER,
            from = firstTigerMove.to,
            to = firstTigerMove.from
        )
        assertFalse(undoMove in BaghBakriGame.getLegalMoves(state))
    }

    @Test
    fun board_neighborsAreEightConnected() {
        val center = 12
        val neighbors = Board.neighborsOf(center)
        assertEquals(8, neighbors.size)
        assertTrue(6 in neighbors)
        assertTrue(7 in neighbors)
        assertTrue(8 in neighbors)
    }

    @Test
    fun captureLanding_requiresStraightLineJump() {
        assertNotNull(Board.captureLanding(tigerIndex = 0, goatIndex = 6))
        assertEquals(12, Board.captureLanding(tigerIndex = 0, goatIndex = 6))
        assertNull(Board.captureLanding(tigerIndex = 0, goatIndex = 1))
    }
}
