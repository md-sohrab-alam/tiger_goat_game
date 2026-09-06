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
import com.sohrab.baghbakri.settings.AppSettings
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

    fun configure(session: GameSession, settings: AppSettings = AppSettings()) {
        aiJob?.cancel()
        _uiState.value = GameUiState(
            gameState = BaghBakriGame.newGame(),
            session = session,
            settings = settings
        )
        scheduleAiTurnIfNeeded()
    }

    fun applySettings(settings: AppSettings) {
        _uiState.update { it.copy(settings = settings) }
    }

    fun startNewGame() {
        val current = _uiState.value
        aiJob?.cancel()
        _uiState.value = GameUiState(
            gameState = BaghBakriGame.newGame(),
            session = current.session,
            settings = current.settings
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
            // Think time: random 1–5s so moves feel deliberate (faster early, slower later as difficulty grows).
            delay(randomAiThinkMs())

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

    private fun randomAiThinkMs(): Long {
        val difficulty = _uiState.value.session.difficulty
        // Easy leans faster; Hard leans slower — still within 1–5 seconds overall.
        val (minMs, maxMs) = when (difficulty) {
            com.sohrab.baghbakri.game.AiDifficulty.EASY -> 1000L to 2800L
            com.sohrab.baghbakri.game.AiDifficulty.MEDIUM -> 1500L to 4000L
            com.sohrab.baghbakri.game.AiDifficulty.HARD -> 2200L to 5000L
        }
        return kotlin.random.Random.nextLong(minMs, maxMs + 1)
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
        private const val TAP_FEEDBACK_MS = 350L
    }
}
