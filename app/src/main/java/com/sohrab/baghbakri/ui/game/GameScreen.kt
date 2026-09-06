package com.sohrab.baghbakri.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.settings.AppPreferences
import com.sohrab.baghbakri.ui.board.BaghBakriBoard
import com.sohrab.baghbakri.ui.common.ConfirmDialog
import com.sohrab.baghbakri.ui.theme.ScreenBackground
import com.sohrab.baghbakri.ui.theme.ScreenBackgroundDark
import kotlin.math.min

@Composable
fun GameScreen(
    session: GameSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingAction by remember { mutableStateOf<PendingLeaveAction?>(null) }
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    LaunchedEffect(session) {
        viewModel.configure(session, prefs.load())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ScreenBackground, ScreenBackgroundDark))
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight

            if (landscape) {
                LandscapeGameLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestHome = { pendingAction = PendingLeaveAction.HOME },
                    onRequestNewGame = { pendingAction = PendingLeaveAction.NEW_GAME }
                )
            } else {
                PortraitGameLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestHome = { pendingAction = PendingLeaveAction.HOME },
                    onRequestNewGame = { pendingAction = PendingLeaveAction.NEW_GAME }
                )
            }
        }
    }

    when (val action = pendingAction) {
        PendingLeaveAction.HOME -> ConfirmDialog(
            title = stringResource(R.string.leave_game_title),
            message = stringResource(R.string.leave_game_message),
            confirmLabel = stringResource(R.string.leave),
            onConfirm = {
                pendingAction = null
                onBack()
            },
            onDismiss = { pendingAction = null }
        )
        PendingLeaveAction.NEW_GAME -> ConfirmDialog(
            title = stringResource(R.string.new_game_title),
            message = stringResource(R.string.new_game_message),
            confirmLabel = stringResource(R.string.new_game),
            onConfirm = {
                pendingAction = null
                viewModel.startNewGame()
            },
            onDismiss = { pendingAction = null }
        )
        null -> Unit
    }

    if (uiState.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.game_over)) },
            text = { Text(uiState.localizedTurnHeadline()) },
            confirmButton = {
                Button(onClick = viewModel::startNewGame) {
                    Text(stringResource(R.string.play_again))
                }
            }
        )
    }
}

private enum class PendingLeaveAction {
    HOME,
    NEW_GAME
}

@Composable
private fun PortraitGameLayout(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onRequestHome: () -> Unit,
    onRequestNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompactHeader(uiState = uiState)
        Spacer(modifier = Modifier.height(8.dp))

        StatusHint(uiState = uiState)

        if (uiState.isAiThinking || uiState.isAnimating) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val side = min(maxWidth.value, maxHeight.value).dp
            BaghBakriBoard(
                gameState = uiState.visibleBoardState,
                selectedPosition = uiState.selectedPosition,
                highlightedDestinations = uiState.visibleDestinations,
                interactiveTargets = uiState.interactiveTargets,
                lastMovePositions = uiState.visibleLastMovePositions,
                tapFeedbackIndex = uiState.tapFeedbackIndex,
                tapFeedbackTick = uiState.tapFeedbackTick,
                animatingMove = uiState.animatingMove,
                inputEnabled = uiState.canInteract,
                hapticEnabled = uiState.settings.hapticFeedback,
                onIntersectionTap = viewModel::onIntersectionTap,
                onAnimationComplete = viewModel::commitAnimatedMove,
                boardSize = side
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TurnSideButtons(uiState = uiState)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRequestHome,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.home))
            }
            Button(
                onClick = onRequestNewGame,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(stringResource(R.string.new_game))
            }
        }
    }
}

@Composable
private fun LandscapeGameLayout(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onRequestHome: () -> Unit,
    onRequestNewGame: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val side = min(maxWidth.value, maxHeight.value).dp
            BaghBakriBoard(
                gameState = uiState.visibleBoardState,
                selectedPosition = uiState.selectedPosition,
                highlightedDestinations = uiState.visibleDestinations,
                interactiveTargets = uiState.interactiveTargets,
                lastMovePositions = uiState.visibleLastMovePositions,
                tapFeedbackIndex = uiState.tapFeedbackIndex,
                tapFeedbackTick = uiState.tapFeedbackTick,
                animatingMove = uiState.animatingMove,
                inputEnabled = uiState.canInteract,
                hapticEnabled = uiState.settings.hapticFeedback,
                onIntersectionTap = viewModel::onIntersectionTap,
                onAnimationComplete = viewModel::commitAnimatedMove,
                boardSize = side
            )
        }

        Column(
            modifier = Modifier
                .weight(0.85f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompactHeader(uiState = uiState)
            StatusHint(uiState = uiState)

            if (uiState.isAiThinking || uiState.isAnimating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            TurnSideButtons(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestHome,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.home))
                }
                Button(
                    onClick = onRequestNewGame,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.new_short))
                }
            }
        }
    }
}

@Composable
private fun CompactHeader(uiState: GameUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.app_brand),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = uiState.localizedModeText(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusHint(uiState: GameUiState) {
    if (!uiState.settings.showStatusTips) {
        Spacer(modifier = Modifier.height(4.dp))
        return
    }
    Text(
        text = boardHintText(uiState),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (uiState.canInteract) FontWeight.SemiBold else FontWeight.Normal,
        color = if (uiState.canInteract) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
