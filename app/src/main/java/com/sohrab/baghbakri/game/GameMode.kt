package com.sohrab.baghbakri.game

enum class GameMode {
    PASS_AND_PLAY,
    VS_AI
}

data class GameSession(
    val mode: GameMode,
    val humanSide: PlayerSide = PlayerSide.GOAT,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM
)
