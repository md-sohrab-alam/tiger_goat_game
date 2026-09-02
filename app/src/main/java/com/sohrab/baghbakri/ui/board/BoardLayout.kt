package com.sohrab.baghbakri.ui.board

import androidx.compose.ui.geometry.Offset
import com.sohrab.baghbakri.game.Board
import com.sohrab.baghbakri.game.Move
import com.sohrab.baghbakri.game.PlayerSide

internal data class BoardLayout(
    val padding: Float,
    val step: Float
) {
    fun centerOf(index: Int): Offset {
        val row = index / Board.SIZE
        val col = index % Board.SIZE
        return Offset(padding + col * step, padding + row * step)
    }

    fun lerp(fromIndex: Int, toIndex: Int, fraction: Float): Offset {
        val start = centerOf(fromIndex)
        val end = centerOf(toIndex)
        return Offset(
            x = start.x + (end.x - start.x) * fraction,
            y = start.y + (end.y - start.y) * fraction
        )
    }
}

internal fun Move.movingSide(): PlayerSide = when (this) {
    is Move.PlaceGoat -> PlayerSide.GOAT
    is Move.Relocate -> player
}

internal fun Move.originIndex(): Int? = when (this) {
    is Move.PlaceGoat -> null
    is Move.Relocate -> from
}

internal fun Move.destinationIndex(): Int = when (this) {
    is Move.PlaceGoat -> to
    is Move.Relocate -> to
}
