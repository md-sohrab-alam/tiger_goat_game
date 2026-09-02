package com.sohrab.baghbakri.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.game.highlightedPositions
import com.sohrab.baghbakri.ui.board.BaghBakriBoard

@Composable
fun GameScreen(
    session: GameSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(session) {
        viewModel.configure(session)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TurnIndicatorBar(uiState = uiState)

        GameStatsBar(uiState = uiState)

        if (uiState.isAiThinking || uiState.isAnimating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        BaghBakriBoard(
            gameState = uiState.visibleBoardState,
            selectedPosition = uiState.selectedPosition,
            highlightedDestinations = uiState.highlightedDestinations,
            interactiveTargets = uiState.interactiveTargets,
            lastMovePositions = uiState.lastMove?.highlightedPositions().orEmpty(),
            tapFeedbackIndex = uiState.tapFeedbackIndex,
            tapFeedbackTick = uiState.tapFeedbackTick,
            animatingMove = uiState.animatingMove,
            inputEnabled = uiState.canInteract,
            onIntersectionTap = viewModel::onIntersectionTap,
            onAnimationComplete = viewModel::commitAnimatedMove,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = hintText(uiState),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (uiState.canInteract) FontWeight.Medium else FontWeight.Normal,
            color = if (uiState.canInteract) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Button(
            onClick = viewModel::startNewGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New Game")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home")
        }
    }

    if (uiState.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text(uiState.turnHeadline) },
            confirmButton = {
                Button(onClick = viewModel::startNewGame) {
                    Text("Play Again")
                }
            }
        )
    }
}

private fun hintText(uiState: GameUiState): String {
    if (uiState.isGameOver) return "Tap New Game to play again"
    if (uiState.isAnimating) return "Watch the piece slide across the board"
    if (uiState.isAiThinking) return "Waiting for AI…"
    if (uiState.session.mode == GameMode.VS_AI && !uiState.isHumanTurn) return "AI is playing — sit tight"

    return if (uiState.gameState.phase == GamePhase.PLACEMENT &&
        uiState.gameState.currentTurn == PlayerSide.GOAT
    ) {
        "Tap a glowing spot to place a goat"
    } else if (uiState.selectedPosition != null) {
        "Tap a green ring to move"
    } else {
        "Tap a glowing piece to select it"
    }
}

@Composable
private fun GameStatsBar(uiState: GameUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = uiState.modeText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = uiState.captureText, style = MaterialTheme.typography.bodySmall)
            Text(text = uiState.goatsRemainingText, style = MaterialTheme.typography.bodySmall)
        }
    }
}
