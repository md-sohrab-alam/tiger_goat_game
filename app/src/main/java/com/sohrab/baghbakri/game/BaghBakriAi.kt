package com.sohrab.baghbakri.game

import kotlin.random.Random

/**
 * Minimax AI with alpha-beta pruning for Bagh Bakri.
 */
object BaghBakriAi {

    private const val WIN_SCORE = 10_000
    private const val LOSE_SCORE = -10_000

    fun findBestMove(
        state: GameState,
        aiSide: PlayerSide,
        difficulty: AiDifficulty,
        random: Random = Random.Default
    ): Move? {
        val moves = BaghBakriGame.getLegalMoves(state)
        if (moves.isEmpty()) return null

        if (difficulty == AiDifficulty.EASY && random.nextFloat() < 0.25f) {
            return moves.random(random)
        }

        val depth = effectiveDepth(state, difficulty)
        val ordered = orderMoves(moves)

        var bestMove = ordered.first()
        var bestScore = Int.MIN_VALUE

        for (move in ordered) {
            val child = BaghBakriGame.applyMove(state, move)
            val score = minimax(
                state = child,
                depth = depth - 1,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE,
                aiSide = aiSide
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        alpha: Int,
        beta: Int,
        aiSide: PlayerSide
    ): Int {
        val evaluated = BaghBakriGame.evaluate(state)
        if (depth <= 0 || evaluated.isOver()) {
            return evaluate(evaluated, aiSide)
        }

        val moves = BaghBakriGame.getLegalMoves(evaluated)
        if (moves.isEmpty()) {
            return evaluate(BaghBakriGame.evaluate(evaluated), aiSide)
        }

        val maximizing = evaluated.currentTurn == aiSide
        var currentAlpha = alpha
        var currentBeta = beta

        if (maximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in orderMoves(moves)) {
                val child = BaghBakriGame.applyMove(evaluated, move)
                val score = minimax(child, depth - 1, currentAlpha, currentBeta, aiSide)
                maxEval = maxOf(maxEval, score)
                currentAlpha = maxOf(currentAlpha, score)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        }

        var minEval = Int.MAX_VALUE
        for (move in orderMoves(moves)) {
            val child = BaghBakriGame.applyMove(evaluated, move)
            val score = minimax(child, depth - 1, currentAlpha, currentBeta, aiSide)
            minEval = minOf(minEval, score)
            currentBeta = minOf(currentBeta, score)
            if (currentBeta <= currentAlpha) break
        }
        return minEval
    }

    internal fun evaluate(state: GameState, forSide: PlayerSide): Int {
        state.winner?.let { winner ->
            return if (winner == forSide) WIN_SCORE else LOSE_SCORE
        }

        val tigerMobility = mobilityCount(state, PlayerSide.TIGER)
        val goatMobility = mobilityCount(state, PlayerSide.GOAT)

        return when (forSide) {
            PlayerSide.TIGER -> {
                state.capturedGoats * 800 +
                    tigerMobility * 12 -
                    goatMobility * 8 +
                    state.goatsOnBoard * 3
            }
            PlayerSide.GOAT -> {
                -state.capturedGoats * 800 -
                    tigerMobility * 12 +
                    goatMobility * 10 +
                    (Board.GOAT_COUNT - state.goatsRemainingToPlace) * 4
            }
        }
    }

    private fun mobilityCount(state: GameState, side: PlayerSide): Int {
        if (state.currentTurn != side) return 0
        return BaghBakriGame.getLegalMoves(state).size
    }

    private fun effectiveDepth(state: GameState, difficulty: AiDifficulty): Int {
        val base = difficulty.searchDepth
        if (state.phase == GamePhase.PLACEMENT && state.goatsRemainingToPlace > 12) {
            return minOf(base, 2)
        }
        if (state.phase == GamePhase.PLACEMENT && state.goatsRemainingToPlace > 6) {
            return minOf(base, 3)
        }
        return base
    }

    private fun orderMoves(moves: List<Move>): List<Move> =
        moves.sortedWith(
            compareByDescending<Move> { it is Move.Relocate && it.capturedGoat != null }
                .thenByDescending { it is Move.PlaceGoat }
        )
}
