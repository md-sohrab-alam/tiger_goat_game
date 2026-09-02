package com.sohrab.baghbakri.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.game.Board
import com.sohrab.baghbakri.game.GameState
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.ui.theme.BoardBackgroundInner
import com.sohrab.baghbakri.ui.theme.BoardHighlight
import com.sohrab.baghbakri.ui.theme.BoardHighlightSoft
import com.sohrab.baghbakri.ui.theme.BoardLine
import com.sohrab.baghbakri.ui.theme.BoardSelected
import com.sohrab.baghbakri.ui.theme.BoardSelectedGlow
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.GoatPieceBorder
import com.sohrab.baghbakri.ui.theme.IntersectionDot
import com.sohrab.baghbakri.ui.theme.LastMoveHighlight
import com.sohrab.baghbakri.ui.theme.TapPulseColor
import com.sohrab.baghbakri.ui.theme.TapTargetFill
import com.sohrab.baghbakri.ui.theme.TapTargetRing
import com.sohrab.baghbakri.ui.theme.TigerPiece
import com.sohrab.baghbakri.ui.theme.TigerPieceDark
import kotlin.math.hypot

private const val SLIDE_DURATION_MS = 520
private const val CAPTURE_DURATION_MS = 620
private const val PLACE_DURATION_MS = 420

@Composable
fun BaghBakriBoard(
    gameState: GameState,
    selectedPosition: Int?,
    highlightedDestinations: Set<Int>,
    interactiveTargets: Set<Int>,
    lastMovePositions: Set<Int>,
    tapFeedbackIndex: Int?,
    tapFeedbackTick: Long,
    animatingMove: Move?,
    inputEnabled: Boolean,
    onIntersectionTap: (Int) -> Unit,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val tapPulse = remember { Animatable(0f) }
    val moveProgress = remember { Animatable(0f) }

    LaunchedEffect(tapFeedbackTick) {
        if (tapFeedbackIndex != null) {
            tapPulse.snapTo(0.4f)
            tapPulse.animateTo(1.15f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            tapPulse.animateTo(0f, tween(220))
        }
    }

    LaunchedEffect(animatingMove) {
        val move = animatingMove ?: return@LaunchedEffect
        moveProgress.snapTo(0f)
        val duration = when (move) {
            is Move.PlaceGoat -> PLACE_DURATION_MS
            is Move.Relocate -> if (move.capturedGoat != null) CAPTURE_DURATION_MS else SLIDE_DURATION_MS
        }
        moveProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
        onAnimationComplete()
    }

    val targetPulse by rememberInfiniteTransition(label = "targetPulse").animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "targetPulseScale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp)
                .pointerInput(inputEnabled, gameState, selectedPosition, interactiveTargets, animatingMove) {
                    detectTapGestures { offset ->
                        val index = nearestIntersection(offset.x, offset.y, size.width.toFloat())
                        if (index != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIntersectionTap(index)
                        }
                    }
                }
        ) {
            val padding = size.minDimension * 0.07f
            val span = size.minDimension - padding * 2f
            val step = span / (Board.SIZE - 1)
            val layout = BoardLayout(padding, step)
            val nodeRadius = step * 0.07f
            val tapRingRadius = step * 0.30f
            val progress = moveProgress.value

            drawRect(BoardBackgroundInner, size = size)

            for (index in 0 until Board.POINT_COUNT) {
                for (neighbor in Board.neighborsOf(index)) {
                    if (neighbor > index) {
                        drawLine(
                            color = BoardLine,
                            start = layout.centerOf(index),
                            end = layout.centerOf(neighbor),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            for (index in 0 until Board.POINT_COUNT) {
                drawCircle(
                    color = IntersectionDot,
                    radius = nodeRadius,
                    center = layout.centerOf(index)
                )
            }

            if (inputEnabled) {
                for (target in interactiveTargets) {
                    val center = layout.centerOf(target)
                    val scale = if (target in highlightedDestinations || selectedPosition == null) {
                        targetPulse
                    } else {
                        1f
                    }
                    val ringRadius = tapRingRadius * scale
                    drawCircle(color = TapTargetFill, radius = ringRadius, center = center)
                    drawCircle(
                        color = TapTargetRing,
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            for (destination in highlightedDestinations) {
                val center = layout.centerOf(destination)
                drawCircle(
                    color = BoardHighlightSoft,
                    radius = step * 0.26f * targetPulse,
                    center = center
                )
                drawCircle(
                    color = BoardHighlight,
                    radius = step * 0.20f,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }

            for (position in lastMovePositions) {
                drawCircle(
                    color = LastMoveHighlight.copy(alpha = 0.4f),
                    radius = step * 0.28f,
                    center = layout.centerOf(position),
                    style = Stroke(width = 3f)
                )
            }

            tapFeedbackIndex?.let { index ->
                val center = layout.centerOf(index)
                val pulseRadius = tapRingRadius * tapPulse.value
                drawCircle(
                    color = TapPulseColor.copy(alpha = 0.5f * (1f - tapPulse.value * 0.5f)),
                    radius = pulseRadius,
                    center = center
                )
                drawCircle(
                    color = TapPulseColor,
                    radius = nodeRadius * 2f,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }

            for (index in 0 until Board.POINT_COUNT) {
                if (shouldHideStaticPiece(index, animatingMove, progress)) continue

                val center = layout.centerOf(index)
                if (index == selectedPosition) {
                    drawCircle(color = BoardSelectedGlow, radius = step * 0.30f, center = center)
                    drawCircle(
                        color = BoardSelected,
                        radius = step * 0.28f,
                        center = center,
                        style = Stroke(width = 5f)
                    )
                }

                val alpha = capturedGoatAlpha(index, animatingMove, progress)
                when (gameState.pieceAt(index)) {
                    PlayerSide.TIGER -> drawTiger(center, step, alpha = alpha)
                    PlayerSide.GOAT -> drawGoat(center, step, alpha = alpha)
                    null -> Unit
                }
            }

            animatingMove?.let { move ->
                drawAnimatedPiece(move, layout, step, progress)
            }
        }
    }
}

private fun shouldHideStaticPiece(index: Int, move: Move?, progress: Float): Boolean {
    if (move == null) return false
    return when (move) {
        is Move.PlaceGoat -> index == move.to
        is Move.Relocate -> index == move.from
    }
}

private fun capturedGoatAlpha(index: Int, move: Move?, progress: Float): Float {
    if (move !is Move.Relocate) return 1f
    val captured = move.capturedGoat ?: return 1f
    if (index != captured) return 1f
    return (1f - ((progress - 0.35f) / 0.45f).coerceIn(0f, 1f))
}

private fun DrawScope.drawAnimatedPiece(
    move: Move,
    layout: BoardLayout,
    step: Float,
    progress: Float
) {
    val side = move.movingSide()
    val (center, scale) = when (move) {
        is Move.PlaceGoat -> {
            val destination = layout.centerOf(move.to)
            val origin = Offset(destination.x, destination.y - step * 0.55f)
            Offset(
                x = origin.x + (destination.x - origin.x) * progress,
                y = origin.y + (destination.y - origin.y) * progress
            ) to (0.45f + 0.55f * progress)
        }
        is Move.Relocate -> {
            layout.lerp(move.from, move.to, progress) to 1.08f
        }
    }

    drawMoveTrail(move, layout, progress, step)

    when (side) {
        PlayerSide.TIGER -> drawTiger(center, step, scale = scale, alpha = 1f, glow = true)
        PlayerSide.GOAT -> drawGoat(center, step, scale = scale, alpha = 1f, glow = true)
    }
}

private fun DrawScope.drawMoveTrail(
    move: Move,
    layout: BoardLayout,
    progress: Float,
    step: Float
) {
    if (move !is Move.Relocate || progress <= 0.05f) return
    val start = layout.centerOf(move.from)
    val end = layout.centerOf(move.to)
    val current = layout.lerp(move.from, move.to, progress)
    drawLine(
        color = LastMoveHighlight.copy(alpha = 0.35f),
        start = start,
        end = current,
        strokeWidth = step * 0.08f
    )
    drawLine(
        color = LastMoveHighlight.copy(alpha = 0.18f),
        start = start,
        end = end,
        strokeWidth = step * 0.04f
    )
}

private fun DrawScope.drawTiger(
    center: Offset,
    step: Float,
    scale: Float = 1f,
    alpha: Float = 1f,
    glow: Boolean = false
) {
    if (glow) {
        drawCircle(
            color = TigerPiece.copy(alpha = 0.35f * alpha),
            radius = step * 0.30f * scale,
            center = center
        )
    }
    drawCircle(
        color = TigerPieceDark.copy(alpha = alpha),
        radius = step * 0.26f * scale,
        center = center
    )
    drawCircle(
        color = TigerPiece.copy(alpha = alpha),
        radius = step * 0.22f * scale,
        center = center
    )
}

private fun DrawScope.drawGoat(
    center: Offset,
    step: Float,
    scale: Float = 1f,
    alpha: Float = 1f,
    glow: Boolean = false
) {
    if (glow) {
        drawCircle(
            color = GoatPiece.copy(alpha = 0.45f * alpha),
            radius = step * 0.24f * scale,
            center = center
        )
    }
    drawCircle(
        color = GoatPieceBorder.copy(alpha = alpha),
        radius = step * 0.20f * scale,
        center = center
    )
    drawCircle(
        color = GoatPiece.copy(alpha = alpha),
        radius = step * 0.17f * scale,
        center = center
    )
}

private fun nearestIntersection(x: Float, y: Float, canvasSize: Float): Int? {
    val padding = canvasSize * 0.07f
    val span = canvasSize - padding * 2f
    val step = span / (Board.SIZE - 1)
    val hitRadius = step * 0.55f
    val layout = BoardLayout(padding, step)

    var closest: Int? = null
    var closestDistance = Float.MAX_VALUE

    for (index in 0 until Board.POINT_COUNT) {
        val center = layout.centerOf(index)
        val distance = hypot(x - center.x, y - center.y)
        if (distance < closestDistance) {
            closest = index
            closestDistance = distance
        }
    }
    return closest?.takeIf { closestDistance <= hitRadius }
}
