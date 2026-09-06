package com.sohrab.baghbakri.ui.common

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.sohrab.baghbakri.R
import com.sohrab.baghbakri.ui.theme.GoatPiece
import com.sohrab.baghbakri.ui.theme.TigerPiece
import kotlin.math.roundToInt

/** Logo-matched Bagh / Bakri token bitmaps for Canvas drawing. */
data class PieceSprites(
    val bagh: ImageBitmap,
    val bakri: ImageBitmap
)

@Composable
fun rememberPieceSprites(): PieceSprites {
    val context = LocalContext.current
    return remember(context) {
        val resources = context.resources
        PieceSprites(
            bagh = BitmapFactory.decodeResource(resources, R.drawable.piece_bagh).asImageBitmap(),
            bakri = BitmapFactory.decodeResource(resources, R.drawable.piece_bakri).asImageBitmap()
        )
    }
}

fun DrawScope.drawTigerPiece(
    sprites: PieceSprites,
    center: Offset,
    step: Float,
    scale: Float = 1f,
    alpha: Float = 1f,
    glow: Boolean = false
) {
    drawPieceToken(
        image = sprites.bagh,
        center = center,
        step = step,
        scale = scale,
        alpha = alpha,
        glow = glow,
        glowColor = TigerPiece
    )
}

fun DrawScope.drawGoatPiece(
    sprites: PieceSprites,
    center: Offset,
    step: Float,
    scale: Float = 1f,
    alpha: Float = 1f,
    glow: Boolean = false
) {
    drawPieceToken(
        image = sprites.bakri,
        center = center,
        step = step,
        scale = scale,
        alpha = alpha,
        glow = glow,
        glowColor = GoatPiece
    )
}

private fun DrawScope.drawPieceToken(
    image: ImageBitmap,
    center: Offset,
    step: Float,
    scale: Float,
    alpha: Float,
    glow: Boolean,
    glowColor: Color
) {
    val diameter = step * 0.56f * scale
    if (glow) {
        drawCircle(
            color = glowColor.copy(alpha = 0.32f * alpha),
            radius = diameter * 0.62f,
            center = center
        )
    }

    val dstSize = IntSize(diameter.roundToInt().coerceAtLeast(1), diameter.roundToInt().coerceAtLeast(1))
    val dstOffset = IntOffset(
        (center.x - dstSize.width / 2f).roundToInt(),
        (center.y - dstSize.height / 2f).roundToInt()
    )
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = dstOffset,
        dstSize = dstSize,
        alpha = alpha
    )
}

@Composable
fun PieceIcon(
    isTiger: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Image(
        painter = painterResource(
            if (isTiger) R.drawable.piece_bagh else R.drawable.piece_bakri
        ),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}
