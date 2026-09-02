package com.sohrab.baghbakri.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BaghBakriAiTest {

    @Test
    fun aiTigerCapturesWhenAvailable() {
        val state = GameState(
            tigers = setOf(0),
            goats = setOf(6),
            goatsRemainingToPlace = 18,
            capturedGoats = 0,
            phase = GamePhase.PLACEMENT,
            currentTurn = PlayerSide.TIGER
        )

        val move = BaghBakriAi.findBestMove(
            state = state,
            aiSide = PlayerSide.TIGER,
            difficulty = AiDifficulty.HARD,
            random = kotlin.random.Random(0)
        )

        assertNotNull(move)
        assertTrue(move is Move.Relocate)
        assertEquals(6, (move as Move.Relocate).capturedGoat)
    }

    @Test
    fun aiReturnsLegalMove() {
        val state = BaghBakriGame.newGame()
        val move = BaghBakriAi.findBestMove(
            state = state,
            aiSide = PlayerSide.GOAT,
            difficulty = AiDifficulty.MEDIUM,
            random = kotlin.random.Random(1)
        )

        assertNotNull(move)
        assertTrue(move in BaghBakriGame.getLegalMoves(state))
    }
}
