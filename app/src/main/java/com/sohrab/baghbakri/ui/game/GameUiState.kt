package com.sohrab.baghbakri.ui.game

import com.sohrab.baghbakri.game.BaghBakriGame
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.GameState
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide

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
    val animatingMove: Move? = null
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
            if (!canInteract) return emptySet()
            val moves = BaghBakriGame.getLegalMoves(gameState)
            return when {
                gameState.phase == GamePhase.PLACEMENT && gameState.currentTurn == PlayerSide.GOAT -> {
                    moves.filterIsInstance<Move.PlaceGoat>().map { it.to }.toSet()
                }
                selectedPosition != null -> {
                    highlightedDestinations + setOfNotNull(selectedPosition)
                }
                else -> {
                    moves.filterIsInstance<Move.Relocate>().map { it.from }.toSet()
                }
            }
        }

    val turnHeadline: String
        get() {
            if (isGameOver) {
                return when (gameState.winner) {
                    PlayerSide.TIGER -> "Tigers Win!"
                    PlayerSide.GOAT -> "Goats Win!"
                    null -> "Game Over"
                }
            }
            if (isAiThinking) return "AI is thinking…"
            if (isAnimating) {
                return when (currentTurn) {
                    PlayerSide.TIGER -> "Tiger moving…"
                    PlayerSide.GOAT -> "Goat moving…"
                }
            }
            return when (currentTurn) {
                PlayerSide.TIGER -> "Tigers' Turn"
                PlayerSide.GOAT -> "Goats' Turn"
            }
        }

    val turnSubline: String
        get() {
            if (isGameOver) return "Tap New Game to play again"
            if (isAiThinking) return "Please wait"
            if (isAnimating) return "Watch the piece slide"
            val action = when (gameState.phase) {
                GamePhase.PLACEMENT -> if (currentTurn == PlayerSide.GOAT) "Place a goat" else "Move or capture"
                GamePhase.MOVEMENT -> "Move a piece"
            }
            return when {
                session.mode == GameMode.VS_AI && isHumanTurn -> "Your turn — $action"
                session.mode == GameMode.VS_AI -> "AI — $action"
                isHumanTurn || session.mode == GameMode.PASS_AND_PLAY -> action
                else -> action
            }
        }

    val captureText: String
        get() = "Captured: ${gameState.capturedGoats}/5"

    val goatsRemainingText: String
        get() = if (gameState.phase == GamePhase.PLACEMENT) {
            "Goats to place: ${gameState.goatsRemainingToPlace}"
        } else {
            "Goats on board: ${gameState.goatsOnBoard}"
        }

    val modeText: String
        get() = when (session.mode) {
            GameMode.PASS_AND_PLAY -> "Pass & Play"
            GameMode.VS_AI -> "vs AI (${session.difficulty.label}) — You: ${session.humanSide.label}"
        }

    private val PlayerSide.label: String
        get() = when (this) {
            PlayerSide.TIGER -> "Tigers"
            PlayerSide.GOAT -> "Goats"
        }
}
