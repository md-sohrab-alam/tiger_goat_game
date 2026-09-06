package com.sohrab.baghbakri.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.ui.theme.BoardLine
import com.sohrab.baghbakri.ui.theme.BoardSurface
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.GoatPieceBorder
import com.sohrab.baghbakri.ui.theme.GoatTurnColor
import com.sohrab.baghbakri.ui.theme.ScreenBackground
import com.sohrab.baghbakri.ui.theme.ScreenBackgroundDark
import com.sohrab.baghbakri.ui.theme.TigerPiece
import com.sohrab.baghbakri.ui.theme.TigerPieceDark
import com.sohrab.baghbakri.ui.theme.TigerTurnColor

private data class TutorialStepIds(val titleRes: Int, val bodyRes: Int)

private val stepIds = listOf(
    TutorialStepIds(R.string.tutorial_title_1, R.string.tutorial_body_1),
    TutorialStepIds(R.string.tutorial_title_2, R.string.tutorial_body_2),
    TutorialStepIds(R.string.tutorial_title_3, R.string.tutorial_body_3),
    TutorialStepIds(R.string.tutorial_title_4, R.string.tutorial_body_4),
    TutorialStepIds(R.string.tutorial_title_5, R.string.tutorial_body_5)
)

@Composable
fun TutorialScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val step = stepIds[stepIndex]
    val isLast = stepIndex == stepIds.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackground, ScreenBackgroundDark)))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.how_to_play),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.tutorial_step_of, stepIndex + 1, stepIds.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        TutorialVisual(stepIndex = stepIndex)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(step.titleRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(step.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepIds.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == stepIndex) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == stepIndex) GoatTurnColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (stepIndex > 0) {
                OutlinedButton(
                    onClick = { stepIndex-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.back))
                }
            } else {
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.skip))
                }
            }
            Button(
                onClick = {
                    if (isLast) onFinished() else stepIndex++
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(if (isLast) R.string.got_it else R.string.next))
            }
        }
    }
}

@Composable
private fun TutorialVisual(stepIndex: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BoardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val pad = size.minDimension * 0.12f
            val span = size.minDimension - pad * 2f
            val step = span / 4f
            fun pt(r: Int, c: Int) = Offset(pad + c * step, pad + r * step)

            for (r in 0..4) {
                for (c in 0..4) {
                    if (c < 4) {
                        drawLine(BoardLine, pt(r, c), pt(r, c + 1), strokeWidth = 3f)
                    }
                    if (r < 4) {
                        drawLine(BoardLine, pt(r, c), pt(r + 1, c), strokeWidth = 3f)
                    }
                }
            }

            when (stepIndex) {
                0 -> {
                    drawCircle(BoardLine, radius = 6f, center = pt(2, 2))
                }
                1 -> {
                    listOf(0 to 0, 0 to 4, 4 to 0, 4 to 4).forEach { (r, c) ->
                        drawCircle(TigerPieceDark, radius = step * 0.22f, center = pt(r, c))
                        drawCircle(TigerPiece, radius = step * 0.17f, center = pt(r, c))
                    }
                }
                2 -> {
                    listOf(0 to 0, 0 to 4, 4 to 0, 4 to 4).forEach { (r, c) ->
                        drawCircle(TigerPiece, radius = step * 0.16f, center = pt(r, c))
                    }
                    listOf(1 to 2, 2 to 1, 2 to 3).forEach { (r, c) ->
                        drawCircle(GoatPieceBorder, radius = step * 0.16f, center = pt(r, c))
                        drawCircle(GoatPiece, radius = step * 0.12f, center = pt(r, c))
                    }
                }
                3 -> {
                    drawCircle(TigerPiece, radius = step * 0.18f, center = pt(1, 1))
                    drawCircle(GoatPieceBorder, radius = step * 0.16f, center = pt(2, 2))
                    drawCircle(GoatPiece, radius = step * 0.12f, center = pt(2, 2))
                    drawCircle(TigerTurnColor.copy(alpha = 0.35f), radius = step * 0.2f, center = pt(3, 3))
                }
                else -> {
                    drawCircle(TigerPiece, radius = step * 0.16f, center = pt(2, 2))
                    listOf(
                        1 to 1, 1 to 2, 1 to 3,
                        2 to 1, 2 to 3,
                        3 to 1, 3 to 2, 3 to 3
                    ).forEach { (r, c) ->
                        drawCircle(GoatPieceBorder, radius = step * 0.14f, center = pt(r, c))
                        drawCircle(GoatPiece, radius = step * 0.1f, center = pt(r, c))
                    }
                }
            }
        }
    }
}
