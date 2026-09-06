package com.sohrab.baghbakri.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.game.Board
import com.sohrab.baghbakri.game.GamePhase
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.ui.common.PieceIcon
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.GoatPieceBorder
import com.sohrab.baghbakri.ui.theme.GoatTurnColor
import com.sohrab.baghbakri.ui.theme.GoatTurnContainer
import com.sohrab.baghbakri.ui.theme.TigerTurnColor
import com.sohrab.baghbakri.ui.theme.TigerTurnContainer
import kotlin.math.min

/**
 * Bottom turn strip with piece trays:
 * - Goats: remaining goats to place (stack + count)
 * - Tigers: 5 capture slots that fill as goats are caught
 */
@Composable
fun TurnSideButtons(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val activeSide = when {
        uiState.isGameOver -> uiState.gameState.winner
        else -> uiState.currentTurn
    }

    val goatsInTray = goatsRemainingInTray(uiState)
    val capturedShown = capturedGoatsShown(uiState)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TurnSideButton(
            side = PlayerSide.GOAT,
            title = stringResource(R.string.goats),
            isActive = activeSide == PlayerSide.GOAT && !uiState.isGameOver,
            isWinner = uiState.isGameOver && uiState.gameState.winner == PlayerSide.GOAT,
            modifier = Modifier.weight(1f)
        ) {
            GoatTray(
                remainingToPlace = goatsInTray,
                onBoard = uiState.visibleBoardState.goatsOnBoard,
                phase = uiState.visibleBoardState.phase,
                placingNow = uiState.animatingMove is Move.PlaceGoat
            )
        }
        TurnSideButton(
            side = PlayerSide.TIGER,
            title = stringResource(R.string.tigers),
            isActive = activeSide == PlayerSide.TIGER && !uiState.isGameOver,
            isWinner = uiState.isGameOver && uiState.gameState.winner == PlayerSide.TIGER,
            modifier = Modifier.weight(1f)
        ) {
            CaptureSlots(
                filled = capturedShown,
                fillingNow = (uiState.animatingMove as? Move.Relocate)?.capturedGoat != null
            )
        }
    }
}

/** Tray count drops when a place animation starts (goat left the queue). */
private fun goatsRemainingInTray(uiState: GameUiState): Int {
    val base = uiState.visibleBoardState.goatsRemainingToPlace
    return if (uiState.animatingMove is Move.PlaceGoat) {
        (base - 1).coerceAtLeast(0)
    } else {
        base
    }
}

/** Capture slots fill as soon as a capture animation starts. */
private fun capturedGoatsShown(uiState: GameUiState): Int {
    val base = uiState.visibleBoardState.capturedGoats
    val capturing = (uiState.animatingMove as? Move.Relocate)?.capturedGoat != null
    return if (capturing) {
        (base + 1).coerceAtMost(Board.GOATS_TO_CAPTURE_FOR_TIGER_WIN)
    } else {
        base
    }
}

@Composable
private fun TurnSideButton(
    side: PlayerSide,
    title: String,
    isActive: Boolean,
    isWinner: Boolean,
    modifier: Modifier = Modifier,
    trayContent: @Composable () -> Unit
) {
    val accent = when (side) {
        PlayerSide.TIGER -> TigerTurnColor
        PlayerSide.GOAT -> GoatTurnColor
    }
    val container = when (side) {
        PlayerSide.TIGER -> TigerTurnContainer
        PlayerSide.GOAT -> GoatTurnContainer
    }

    val blink by rememberInfiniteTransition(label = "turnBlink_$title").animateFloat(
        initialValue = if (isActive) 0.55f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 550 else 1),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha_$title"
    )
    val scale by rememberInfiniteTransition(label = "turnScale_$title").animateFloat(
        initialValue = if (isActive) 0.97f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 550 else 1),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkScale_$title"
    )

    Column(
        modifier = modifier
            .scale(if (isActive) scale else 1f)
            .alpha(if (isActive) blink.coerceAtLeast(0.78f) else if (isWinner) 1f else 0.78f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(container, container.copy(alpha = 0.88f))
                )
            )
            .border(
                width = if (isActive || isWinner) 3.dp else 1.dp,
                color = if (isActive || isWinner) accent else accent.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Text(
            text = when {
                isWinner -> stringResource(R.string.winner)
                isActive -> stringResource(R.string.your_turn)
                else -> stringResource(R.string.waiting)
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        trayContent()
    }
}

@Composable
private fun GoatTray(
    remainingToPlace: Int,
    onBoard: Int,
    phase: GamePhase,
    placingNow: Boolean
) {
    val showPlacementTray = phase == GamePhase.PLACEMENT || remainingToPlace > 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showPlacementTray) {
            Text(
                text = stringResource(R.string.ready_to_place),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                GoatStack(
                    count = remainingToPlace,
                    highlightTop = placingNow || remainingToPlace > 0
                )
            }
            Text(
                text = "$remainingToPlace",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoatTurnColor
            )
            Text(
                text = stringResource(
                    if (remainingToPlace == 1) R.string.goat_left else R.string.goats_left
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Text(
                text = stringResource(R.string.on_the_board),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            PieceIcon(isTiger = false, size = 28.dp)
            Text(
                text = "$onBoard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoatTurnColor
            )
            Text(
                text = stringResource(R.string.goats_alive),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GoatStack(count: Int, highlightTop: Boolean) {
    val visible = min(count, 6)
    if (visible <= 0) {
        Text(
            text = "—",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
        return
    }
    Box(
        modifier = Modifier.height(32.dp).width((22 + (visible - 1) * 10).dp),
        contentAlignment = Alignment.CenterStart
    ) {
        for (i in 0 until visible) {
            PieceIcon(
                isTiger = false,
                size = if (i == visible - 1 && highlightTop) 26.dp else 22.dp,
                modifier = Modifier.offset(x = (i * 10).dp)
            )
        }
    }
}

@Composable
private fun CaptureSlots(
    filled: Int,
    fillingNow: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.caught_goats),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(Board.GOATS_TO_CAPTURE_FOR_TIGER_WIN) { index ->
                val isFilled = index < filled
                val isNewest = fillingNow && index == filled - 1
                CaptureSlot(filled = isFilled, newest = isNewest)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$filled / ${Board.GOATS_TO_CAPTURE_FOR_TIGER_WIN}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TigerTurnColor
        )
        Text(
            text = stringResource(
                if (filled >= Board.GOATS_TO_CAPTURE_FOR_TIGER_WIN) {
                    R.string.enough_to_win
                } else {
                    R.string.need_5_to_win
                }
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CaptureSlot(filled: Boolean, newest: Boolean) {
    val pulse by rememberInfiniteTransition(label = "slotPulse").animateFloat(
        initialValue = if (newest) 0.9f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (newest) 400 else 1),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slotPulseAnim"
    )
    Box(
        modifier = Modifier
            .size(22.dp)
            .scale(if (newest) pulse else 1f)
            .clip(CircleShape)
            .border(
                width = 1.5.dp,
                color = if (filled) TigerTurnColor else TigerTurnColor.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .background(
                if (filled) GoatPiece else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        if (filled) {
            PieceIcon(isTiger = false, size = 16.dp)
        }
    }
}
