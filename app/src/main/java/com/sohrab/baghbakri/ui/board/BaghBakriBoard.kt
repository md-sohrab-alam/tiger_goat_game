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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.game.Board
import com.sohrab.baghbakri.game.GameState
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide
import com.sohrab.baghbakri.ui.common.PieceSprites
import com.sohrab.baghbakri.ui.common.drawGoatPiece
import com.sohrab.baghbakri.ui.common.drawTigerPiece
import com.sohrab.baghbakri.ui.common.rememberPieceSprites
import com.sohrab.baghbakri.ui.theme.BoardFrame
import com.sohrab.baghbakri.ui.theme.BoardFrameLight
import com.sohrab.baghbakri.ui.theme.BoardHighlight
import com.sohrab.baghbakri.ui.theme.BoardHighlightSoft
import com.sohrab.baghbakri.ui.theme.BoardLine
import com.sohrab.baghbakri.ui.theme.BoardLineShadow
import com.sohrab.baghbakri.ui.theme.BoardSelected
import com.sohrab.baghbakri.ui.theme.BoardSelectedGlow
import com.sohrab.baghbakri.ui.theme.BoardSurface
import com.sohrab.baghbakri.ui.theme.BoardSurfaceDark
import com.sohrab.baghbakri.ui.theme.BoardSurfaceLight
import com.sohrab.baghbakri.ui.theme.IntersectionDot
import com.sohrab.baghbakri.ui.theme.LastMoveHighlight
import com.sohrab.baghbakri.ui.theme.TapPulseColor
import com.sohrab.baghbakri.ui.theme.TapTargetFill
import com.sohrab.baghbakri.ui.theme.TapTargetRing
import kotlin.math.hypot
import kotlin.math.min

private const val SLIDE_DURATION_MS = 520
private const val CAPTURE_DURATION_MS = 780
private const val PLACE_DURATION_MS = 560

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
    modifier: Modifier = Modifier,
    boardSize: Dp? = null,
    hapticEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val pieceSprites = rememberPieceSprites()
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

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = if (boardSize != null) {
                Modifier.size(boardSize)
            } else {
                Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            color = BoardFrame
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .pointerInput(inputEnabled, hapticEnabled, gameState, selectedPosition, interactiveTargets, animatingMove) {
                        detectTapGestures { offset ->
                            val index = nearestIntersection(
                                offset.x,
                                offset.y,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                            if (index != null) {
                                if (hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onIntersectionTap(index)
                            }
                        }
                    }
            ) {
                val boardPadding = size.minDimension * 0.08f
                val span = size.minDimension - boardPadding * 2f
                val step = span / (Board.SIZE - 1)
                val originX = (size.width - span) / 2f
                val originY = (size.height - span) / 2f
                val layout = BoardLayout(originX, originY, step)
                val nodeRadius = step * 0.08f
                val tapRingRadius = step * 0.30f
                val progress = moveProgress.value

                // Wood frame inset
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(BoardFrameLight, BoardFrame, BoardFrameLight),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    size = size,
                    cornerRadius = CornerRadius(18f, 18f)
                )

                // Playable wood surface
                val inset = size.minDimension * 0.035f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(BoardSurfaceLight, BoardSurface, BoardSurfaceDark)
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Grid shadow + lines
                for (index in 0 until Board.POINT_COUNT) {
                    for (neighbor in Board.neighborsOf(index)) {
                        if (neighbor > index) {
                            drawLine(
                                color = BoardLineShadow,
                                start = layout.centerOf(index) + Offset(1.5f, 1.5f),
                                end = layout.centerOf(neighbor) + Offset(1.5f, 1.5f),
                                strokeWidth = 4f
                            )
                            drawLine(
                                color = BoardLine,
                                start = layout.centerOf(index),
                                end = layout.centerOf(neighbor),
                                strokeWidth = 3.5f
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
                        PlayerSide.TIGER -> drawTigerPiece(pieceSprites, center, step, alpha = alpha)
                        PlayerSide.GOAT -> drawGoatPiece(pieceSprites, center, step, alpha = alpha)
                        null -> Unit
                    }
                }

                animatingMove?.let { move ->
                    drawAnimatedPiece(move, layout, step, progress, size, pieceSprites)
                }
            }
        }
    }
}

private fun shouldHideStaticPiece(index: Int, move: Move?, progress: Float): Boolean {
    if (move == null) return false
    return when (move) {
        is Move.PlaceGoat -> index == move.to
        is Move.Relocate -> {
            if (index == move.from) return true
            // Hide captured goat on board once it starts flying to the tiger tray.
            move.capturedGoat == index && progress >= 0.35f
        }
    }
}

private fun capturedGoatAlpha(index: Int, move: Move?, progress: Float): Float {
    if (move !is Move.Relocate) return 1f
    val captured = move.capturedGoat ?: return 1f
    if (index != captured) return 1f
    // Fade while still on the path, then hidden (flying piece takes over).
    return (1f - ((progress - 0.2f) / 0.2f).coerceIn(0f, 1f))
}

private fun DrawScope.drawAnimatedPiece(
    move: Move,
    layout: BoardLayout,
    step: Float,
    progress: Float,
    canvasSize: Size,
    sprites: PieceSprites
) {
    when (move) {
        is Move.PlaceGoat -> {
            // Jump from near the goat tray (bottom-left of board) onto the point.
            val destination = layout.centerOf(move.to)
            val origin = Offset(
                x = canvasSize.width * 0.22f,
                y = canvasSize.height + step * 0.15f
            )
            val t = progress
            val mid = Offset(
                x = (origin.x + destination.x) / 2f,
                y = min(origin.y, destination.y) - step * 0.85f
            )
            val center = quadraticBezier(origin, mid, destination, t)
            val scale = 0.55f + 0.45f * t
            drawGoatPiece(sprites, center, step, scale = scale, alpha = 1f, glow = true)
        }
        is Move.Relocate -> {
            drawMoveTrail(move, layout, progress, step)
            val tigerCenter = layout.lerp(move.from, move.to, progress)
            drawTigerPiece(sprites, tigerCenter, step, scale = 1.08f, alpha = 1f, glow = true)

            // Captured goat flies toward tiger capture tray (bottom-right).
            val captured = move.capturedGoat
            if (captured != null && progress >= 0.35f) {
                val flyT = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                val start = layout.centerOf(captured)
                val end = Offset(
                    x = canvasSize.width * 0.78f,
                    y = canvasSize.height + step * 0.2f
                )
                val mid = Offset(
                    x = (start.x + end.x) / 2f,
                    y = min(start.y, end.y) - step * 0.7f
                )
                val goatPos = quadraticBezier(start, mid, end, flyT)
                val goatScale = 1f - 0.35f * flyT
                val goatAlpha = 1f - 0.25f * flyT
                drawGoatPiece(sprites, goatPos, step, scale = goatScale, alpha = goatAlpha, glow = true)
            }
        }
    }
}

private fun quadraticBezier(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        x = u * u * p0.x + 2f * u * t * p1.x + t * t * p2.x,
        y = u * u * p0.y + 2f * u * t * p1.y + t * t * p2.y
    )
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

private fun nearestIntersection(
    x: Float,
    y: Float,
    canvasWidth: Float,
    canvasHeight: Float
): Int? {
    val minDim = min(canvasWidth, canvasHeight)
    val boardPadding = minDim * 0.08f
    val span = minDim - boardPadding * 2f
    val step = span / (Board.SIZE - 1)
    val originX = (canvasWidth - span) / 2f
    val originY = (canvasHeight - span) / 2f
    val hitRadius = step * 0.55f
    val layout = BoardLayout(originX, originY, step)

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
