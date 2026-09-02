package com.sohrab.baghbakri.game

enum class PlayerSide {
    TIGER,
    GOAT;

    fun opponent(): PlayerSide = when (this) {
        TIGER -> GOAT
        GOAT -> TIGER
    }
}
