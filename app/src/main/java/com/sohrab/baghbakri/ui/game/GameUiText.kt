package com.sohrab.baghbakri.ui.game

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.game.AiDifficulty
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.PlayerSide

@Composable
fun GameUiState.localizedTurnHeadline(): String {
    val res = LocalContext.current.resources
    return turnHeadline(res)
}

@Composable
fun GameUiState.localizedModeText(): String {
    val res = LocalContext.current.resources
    return modeText(res)
}

fun GameUiState.turnHeadline(res: Resources): String {
    if (isGameOver) {
        return when (gameState.winner) {
            PlayerSide.TIGER -> res.getString(R.string.tigers_win)
            PlayerSide.GOAT -> res.getString(R.string.goats_win)
            null -> res.getString(R.string.game_over)
        }
    }
    if (isAiThinking) return res.getString(R.string.ai_thinking)
    if (isAnimating) {
        return when (currentTurn) {
            PlayerSide.TIGER -> res.getString(R.string.tiger_moving)
            PlayerSide.GOAT -> res.getString(R.string.goat_moving)
        }
    }
    return when (currentTurn) {
        PlayerSide.TIGER -> res.getString(R.string.tigers_turn)
        PlayerSide.GOAT -> res.getString(R.string.goats_turn)
    }
}

fun GameUiState.turnSubline(res: Resources): String {
    if (isGameOver) return res.getString(R.string.tap_new_game_again)
    if (isAiThinking) return res.getString(R.string.please_wait)
    if (isAnimating) return res.getString(R.string.watch_piece_slide)
    val action = when (gameState.phase) {
        GamePhase.PLACEMENT -> if (currentTurn == PlayerSide.GOAT) {
            res.getString(R.string.place_a_goat)
        } else {
            res.getString(R.string.move_or_capture)
        }
        GamePhase.MOVEMENT -> res.getString(R.string.move_a_piece)
    }
    return when {
        session.mode == GameMode.VS_AI && isHumanTurn ->
            res.getString(R.string.your_turn_action, action)
        session.mode == GameMode.VS_AI ->
            res.getString(R.string.ai_action, action)
        else -> action
    }
}

fun GameUiState.modeText(res: Resources): String = when (session.mode) {
    GameMode.PASS_AND_PLAY -> res.getString(R.string.mode_pass_and_play)
    GameMode.VS_AI -> res.getString(
        R.string.mode_vs_ai,
        session.difficulty.label(res),
        session.humanSide.sideLabel(res)
    )
}

fun AiDifficulty.label(res: Resources): String = when (this) {
    AiDifficulty.EASY -> res.getString(R.string.difficulty_easy)
    AiDifficulty.MEDIUM -> res.getString(R.string.difficulty_medium)
    AiDifficulty.HARD -> res.getString(R.string.difficulty_hard)
}

@Composable
fun AiDifficulty.localizedLabel(): String = when (this) {
    AiDifficulty.EASY -> stringResource(R.string.difficulty_easy)
    AiDifficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
    AiDifficulty.HARD -> stringResource(R.string.difficulty_hard)
}

fun PlayerSide.sideLabel(res: Resources): String = when (this) {
    PlayerSide.TIGER -> res.getString(R.string.tigers)
    PlayerSide.GOAT -> res.getString(R.string.goats)
}

@Composable
fun boardHintText(uiState: GameUiState): String {
    if (uiState.isGameOver) return stringResource(R.string.tap_new_game_again)
    if (uiState.isAnimating) return stringResource(R.string.watch_piece_move)
    if (uiState.isAiThinking) return stringResource(R.string.ai_thinking)
    if (uiState.session.mode == GameMode.VS_AI && !uiState.isHumanTurn) {
        return stringResource(R.string.ai_playing)
    }

    val hints = uiState.settings.showMoveHints
    return if (uiState.gameState.phase == GamePhase.PLACEMENT &&
        uiState.gameState.currentTurn == PlayerSide.GOAT
    ) {
        if (hints) stringResource(R.string.hint_place_glow) else stringResource(R.string.hint_place_plain)
    } else if (uiState.selectedPosition != null) {
        if (hints) stringResource(R.string.hint_move_glow) else stringResource(R.string.hint_move_plain)
    } else {
        if (hints) stringResource(R.string.hint_select_glow) else stringResource(R.string.hint_select_plain)
    }
}
