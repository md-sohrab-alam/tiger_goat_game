package com.sohrab.baghbakri.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.GoatTurnColor
import com.sohrab.baghbakri.ui.theme.GoatTurnContainer
import com.sohrab.baghbakri.ui.theme.TigerPiece
import com.sohrab.baghbakri.ui.theme.TigerTurnColor
import com.sohrab.baghbakri.ui.theme.TigerTurnContainer

@Composable
fun TurnIndicatorBar(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val turn = uiState.currentTurn
    val isTiger = turn == PlayerSide.TIGER
    val containerColor = when {
        uiState.isGameOver -> MaterialTheme.colorScheme.surfaceVariant
        isTiger -> TigerTurnContainer
        else -> GoatTurnContainer
    }
    val accentColor = when {
        uiState.isGameOver -> MaterialTheme.colorScheme.primary
        isTiger -> TigerTurnColor
        else -> GoatTurnColor
    }

    val pulse by rememberInfiniteTransition(label = "turnPulse").animateFloat(
        initialValue = if (uiState.isGameOver || uiState.isAiThinking) 1f else 0.92f,
        targetValue = if (uiState.isGameOver || uiState.isAiThinking) 1f else 1.08f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "turnPulseScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .scale(if (uiState.isGameOver) 1f else pulse)
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            PieceIcon(
                side = if (uiState.isGameOver) uiState.gameState.winner ?: turn else turn,
                modifier = Modifier.size(30.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.turnHeadline,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = uiState.turnSubline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
        if (uiState.canInteract) {
            Text(
                text = "YOUR\nTURN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun PieceIcon(side: PlayerSide, modifier: Modifier = Modifier) {
    val color = when (side) {
        PlayerSide.TIGER -> TigerPiece
        PlayerSide.GOAT -> GoatPiece
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}
