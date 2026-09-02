package com.sohrab.baghbakri.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sohrab.baghbakri.game.BaghBakriAi
import com.sohrab.baghbakri.game.BaghBakriGame
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var aiJob: Job? = null
    private var tapFeedbackJob: Job? = null

    fun configure(session: GameSession) {
        aiJob?.cancel()
        _uiState.value = GameUiState(
            gameState = BaghBakriGame.newGame(),
            session = session
        )
        scheduleAiTurnIfNeeded()
    }

    fun startNewGame() {
        val session = _uiState.value.session
        aiJob?.cancel()
        _uiState.value = GameUiState(
            gameState = BaghBakriGame.newGame(),
            session = session
        )
        scheduleAiTurnIfNeeded()
    }

    fun onIntersectionTap(index: Int) {
        showTapFeedback(index)

        val current = _uiState.value
        if (!current.canInteract) return

        val state = current.gameState
        val legalMoves = BaghBakriGame.getLegalMoves(state)

        if (state.phase == GamePhase.PLACEMENT && state.currentTurn == PlayerSide.GOAT) {
            val placeMove = legalMoves.filterIsInstance<Move.PlaceGoat>().find { it.to == index }
            if (placeMove != null) {
                beginAnimatedMove(placeMove)
            }
            return
        }

        val selected = current.selectedPosition
        if (selected == null) {
            if (state.pieceAt(index) == state.currentTurn) {
                selectPiece(index, legalMoves)
            }
            return
        }

        if (index == selected) {
            clearSelection()
            return
        }

        val relocate = legalMoves.filterIsInstance<Move.Relocate>().find {
            it.from == selected && it.to == index
        }
        if (relocate != null) {
            beginAnimatedMove(relocate)
            return
        }

        if (state.pieceAt(index) == state.currentTurn) {
            selectPiece(index, legalMoves)
        } else {
            clearSelection()
        }
    }

    fun commitAnimatedMove() {
        val current = _uiState.value
        val move = current.animatingMove ?: return
        val baseState = current.boardDisplayState ?: current.gameState
        val newState = BaghBakriGame.applyMove(baseState, move)
        _uiState.value = current.copy(
            gameState = newState,
            boardDisplayState = null,
            animatingMove = null,
            selectedPosition = null,
            highlightedDestinations = emptySet(),
            lastMove = move,
            isAiThinking = false
        )
        scheduleAiTurnIfNeeded()
    }

    private fun selectPiece(index: Int, legalMoves: List<Move>) {
        val destinations = legalMoves.mapNotNull { move ->
            when (move) {
                is Move.PlaceGoat -> null
                is Move.Relocate -> move.to.takeIf { move.from == index }
            }
        }.toSet()

        _uiState.update {
            it.copy(selectedPosition = index, highlightedDestinations = destinations)
        }
    }

    private fun beginAnimatedMove(move: Move) {
        val current = _uiState.value
        _uiState.value = current.copy(
            boardDisplayState = current.gameState,
            animatingMove = move,
            selectedPosition = null,
            highlightedDestinations = emptySet(),
            isAiThinking = false
        )
    }

    private fun scheduleAiTurnIfNeeded() {
        val current = _uiState.value
        if (current.session.mode != GameMode.VS_AI) return
        if (current.isGameOver || current.isAnimating) return
        if (current.gameState.currentTurn == current.session.humanSide) return

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isAiThinking = true, selectedPosition = null, highlightedDestinations = emptySet())
            }
            delay(AI_MOVE_DELAY_MS)

            val snapshot = _uiState.value
            if (snapshot.isGameOver || snapshot.isAnimating ||
                snapshot.gameState.currentTurn == snapshot.session.humanSide
            ) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }

            val aiMove = BaghBakriAi.findBestMove(
                state = snapshot.gameState,
                aiSide = snapshot.gameState.currentTurn,
                difficulty = snapshot.session.difficulty
            )

            if (aiMove != null) {
                _uiState.update { it.copy(isAiThinking = false) }
                beginAnimatedMove(aiMove)
            } else {
                _uiState.update { it.copy(isAiThinking = false) }
            }
        }
    }

    private fun clearSelection() {
        _uiState.update { it.copy(selectedPosition = null, highlightedDestinations = emptySet()) }
    }

    private fun showTapFeedback(index: Int) {
        tapFeedbackJob?.cancel()
        _uiState.update {
            it.copy(
                tapFeedbackIndex = index,
                tapFeedbackTick = it.tapFeedbackTick + 1
            )
        }
        tapFeedbackJob = viewModelScope.launch {
            delay(TAP_FEEDBACK_MS)
            _uiState.update { it.copy(tapFeedbackIndex = null) }
        }
    }

    companion object {
        private const val AI_MOVE_DELAY_MS = 450L
        private const val TAP_FEEDBACK_MS = 350L
    }
}
