package com.sohrab.baghbakri.game

enum class GamePhase {
    /** Goats are placed one per turn; tigers may move and capture. */
    PLACEMENT,

    /** All goats are on the board; both sides move one piece per turn. */
    MOVEMENT
}
