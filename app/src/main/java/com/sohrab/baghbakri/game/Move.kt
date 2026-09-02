package com.sohrab.baghbakri.game

sealed class Move {
    abstract val player: PlayerSide

    data class PlaceGoat(val to: Int) : Move() {
        override val player: PlayerSide = PlayerSide.GOAT
    }

    data class Relocate(
        override val player: PlayerSide,
        val from: Int,
        val to: Int,
        val capturedGoat: Int? = null
    ) : Move()
}
