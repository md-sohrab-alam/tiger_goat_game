package com.sohrab.baghbakri.game

/**
 * Pure game engine for Bagh Bakri / Bagh Chal.
 * No Android dependencies — fully unit-testable.
 */
object BaghBakriGame {

    fun newGame(): GameState = GameState.newGame()

    /** Evaluates win conditions on the current state without applying a move. */
    fun evaluate(state: GameState): GameState = resolveOutcome(state)

    fun getLegalMoves(state: GameState): List<Move> {
        if (state.isOver()) return emptyList()
        val evaluated = evaluate(state)
        if (evaluated.isOver()) return emptyList()
        if (state.isOver()) return emptyList()
        return when (state.currentTurn) {
            PlayerSide.GOAT -> getGoatMoves(state)
            PlayerSide.TIGER -> getTigerMoves(state)
        }
    }

    fun applyMove(state: GameState, move: Move): GameState {
        require(!state.isOver()) { "Cannot move after game is over" }
        require(move in getLegalMoves(state)) { "Illegal move: $move" }

        val afterMove = when (move) {
            is Move.PlaceGoat -> applyPlaceGoat(state, move)
            is Move.Relocate -> applyRelocate(state, move)
        }

        return resolveOutcome(afterMove)
    }

    private fun getGoatMoves(state: GameState): List<Move> {
        if (state.phase == GamePhase.PLACEMENT && state.goatsRemainingToPlace > 0) {
            return allEmptyPositions(state).map { Move.PlaceGoat(it) }
        }
        if (state.phase == GamePhase.MOVEMENT) {
            return state.goats.flatMap { from ->
                Board.neighborsOf(from)
                    .filter { !state.isOccupied(it) }
                    .map { to -> Move.Relocate(PlayerSide.GOAT, from, to) }
            }.filter { move -> !wouldRepeatPosition(state, move) }
        }
        return emptyList()
    }

    private fun getTigerMoves(state: GameState): List<Move> {
        val moves = mutableListOf<Move>()

        for (tiger in state.tigers) {
            for (neighbor in Board.neighborsOf(tiger)) {
                if (!state.isOccupied(neighbor)) {
                    moves.add(Move.Relocate(PlayerSide.TIGER, tiger, neighbor))
                } else if (neighbor in state.goats) {
                    val landing = Board.captureLanding(tiger, neighbor) ?: continue
                    if (!state.isOccupied(landing)) {
                        moves.add(
                            Move.Relocate(
                                player = PlayerSide.TIGER,
                                from = tiger,
                                to = landing,
                                capturedGoat = neighbor
                            )
                        )
                    }
                }
            }
        }

        return if (state.phase == GamePhase.MOVEMENT) {
            moves.filter { move -> !wouldRepeatPosition(state, move) }
        } else {
            moves
        }
    }

    private fun applyPlaceGoat(state: GameState, move: Move.PlaceGoat): GameState {
        val goatsRemaining = state.goatsRemainingToPlace - 1
        val newPhase = if (goatsRemaining == 0) GamePhase.MOVEMENT else GamePhase.PLACEMENT

        return state.copy(
            goats = state.goats + move.to,
            goatsRemainingToPlace = goatsRemaining,
            phase = newPhase,
            currentTurn = PlayerSide.TIGER
        )
    }

    private fun applyRelocate(state: GameState, move: Move.Relocate): GameState {
        val newTigers = if (move.player == PlayerSide.TIGER) {
            state.tigers - move.from + move.to
        } else {
            state.tigers
        }

        val captured = move.capturedGoat
        val newGoats = when {
            captured != null -> state.goats - captured
            move.player == PlayerSide.GOAT -> state.goats - move.from + move.to
            else -> state.goats
        }

        val newCapturedCount = if (captured != null) state.capturedGoats + 1 else state.capturedGoats

        val nextTurn = state.currentTurn.opponent()
        val boardHash = if (state.phase == GamePhase.MOVEMENT || state.goatsRemainingToPlace == 0) {
            boardHash(newTigers, newGoats, nextTurn)
        } else {
            null
        }

        val newHistory = if (boardHash != null) {
            state.positionHistory + boardHash
        } else {
            state.positionHistory
        }

        return state.copy(
            tigers = newTigers,
            goats = newGoats,
            capturedGoats = newCapturedCount,
            currentTurn = nextTurn,
            positionHistory = newHistory
        )
    }

    private fun resolveOutcome(state: GameState): GameState {
        if (state.capturedGoats >= Board.GOATS_TO_CAPTURE_FOR_TIGER_WIN) {
            return state.copy(winner = PlayerSide.TIGER)
        }

        val tigerMoves = getTigerMovesIgnoringRepetition(state)
        if (tigerMoves.isEmpty()) {
            return state.copy(winner = PlayerSide.GOAT)
        }

        val goatMoves = getGoatMoves(state)
        if (goatMoves.isEmpty() && state.phase == GamePhase.MOVEMENT) {
            return state.copy(winner = PlayerSide.TIGER)
        }

        return state
    }

    /** Tiger moves without anti-repetition filter — used for win detection. */
    private fun getTigerMovesIgnoringRepetition(state: GameState): List<Move> {
        val moves = mutableListOf<Move>()
        for (tiger in state.tigers) {
            for (neighbor in Board.neighborsOf(tiger)) {
                if (!state.isOccupied(neighbor)) {
                    moves.add(Move.Relocate(PlayerSide.TIGER, tiger, neighbor))
                } else if (neighbor in state.goats) {
                    val landing = Board.captureLanding(tiger, neighbor) ?: continue
                    if (!state.isOccupied(landing)) {
                        moves.add(
                            Move.Relocate(PlayerSide.TIGER, tiger, landing, neighbor)
                        )
                    }
                }
            }
        }
        return moves
    }

    private fun wouldRepeatPosition(state: GameState, move: Move): Boolean {
        if (state.phase != GamePhase.MOVEMENT) return false
        val preview = when (move) {
            is Move.PlaceGoat -> applyPlaceGoat(state, move)
            is Move.Relocate -> applyRelocate(state, move)
        }
        val hash = boardHash(preview.tigers, preview.goats, preview.currentTurn)
        return hash in state.positionHistory
    }

    private fun allEmptyPositions(state: GameState): List<Int> =
        (0 until Board.POINT_COUNT).filter { !state.isOccupied(it) }

    private fun boardHash(tigers: Set<Int>, goats: Set<Int>, turn: PlayerSide): Long {
        var hash = 0L
        for (index in tigers) {
            hash = hash xor (1L shl index)
        }
        for (index in goats) {
            hash = hash xor (1L shl (index + 25))
        }
        hash = hash xor if (turn == PlayerSide.TIGER) (1L shl 50) else 0L
        return hash
    }
}
