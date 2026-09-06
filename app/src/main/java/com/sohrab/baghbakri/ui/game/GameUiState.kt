package com.sohrab.baghbakri.ui.game

import com.sohrab.baghbakri.game.BaghBakriGame
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.GameState
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.settings.AppSettings

data class GameUiState(
    val gameState: GameState = GameState.newGame(),
    val session: GameSession = GameSession(mode = GameMode.PASS_AND_PLAY),
    val selectedPosition: Int? = null,
    val highlightedDestinations: Set<Int> = emptySet(),
    val lastMove: Move? = null,
    val isAiThinking: Boolean = false,
    val tapFeedbackIndex: Int? = null,
    val tapFeedbackTick: Long = 0L,
    /** Snapshot before a move — used while the slide animation plays. */
    val boardDisplayState: GameState? = null,
    val animatingMove: Move? = null,
    val settings: AppSettings = AppSettings()
) {
    val isGameOver: Boolean get() = gameState.isOver()

    val isAnimating: Boolean get() = animatingMove != null

    val visibleBoardState: GameState get() = boardDisplayState ?: gameState

    val currentTurn: PlayerSide
        get() = if (isAnimating) animatingMove?.let { move ->
            when (move) {
                is Move.PlaceGoat -> PlayerSide.GOAT
                is Move.Relocate -> move.player
            }
        } ?: visibleBoardState.currentTurn else gameState.currentTurn

    val isHumanTurn: Boolean
        get() = session.mode == GameMode.PASS_AND_PLAY ||
            gameState.currentTurn == session.humanSide

    val canInteract: Boolean
        get() = !isGameOver && !isAiThinking && !isAnimating && isHumanTurn

    /** Positions the user can tap right now — shown as visible targets on the board. */
    val interactiveTargets: Set<Int>
        get() {
            if (!canInteract || !settings.showMoveHints) return emptySet()
            val moves = BaghBakriGame.getLegalMoves(gameState)
            return when {
                gameState.phase == GamePhase.PLACEMENT && gameState.currentTurn == PlayerSide.GOAT -> {
                    moves.filterIsInstance<Move.PlaceGoat>().map { it.to }.toSet()
                }
                selectedPosition != null -> {
                    highlightedDestinations + setOf(selectedPosition)
                }
                else -> {
                    moves.filterIsInstance<Move.Relocate>().map { it.from }.toSet()
                }
            }
        }

    /** Destination rings after selecting a piece — only if move hints are on. */
    val visibleDestinations: Set<Int>
        get() = if (settings.showMoveHints) highlightedDestinations else emptySet()

    val visibleLastMovePositions: Set<Int>
        get() = if (settings.showLastMove) {
            lastMove?.let { move ->
                when (move) {
                    is Move.PlaceGoat -> setOf(move.to)
                    is Move.Relocate -> setOf(move.from, move.to)
                }
            }.orEmpty()
        } else {
            emptySet()
        }

    val captureCount: Int get() = gameState.capturedGoats

    val goatsToPlaceCount: Int get() = gameState.goatsRemainingToPlace

    val goatsOnBoardCount: Int get() = gameState.goatsOnBoard
}
