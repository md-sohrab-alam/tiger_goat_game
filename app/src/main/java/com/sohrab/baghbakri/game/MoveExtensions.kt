package com.sohrab.baghbakri.game

fun Move.highlightedPositions(): Set<Int> = when (this) {
    is Move.PlaceGoat -> setOf(to)
    is Move.Relocate -> setOf(from, to)
}
