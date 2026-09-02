package com.sohrab.baghbakri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.ui.game.GameScreen
import com.sohrab.baghbakri.ui.home.AiSetupScreen
import com.sohrab.baghbakri.ui.home.HomeScreen
import com.sohrab.baghbakri.ui.theme.BaghBakriTheme

private enum class AppScreen {
    HOME,
    AI_SETUP,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaghBakriTheme {
                var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
                var gameSession by remember { mutableStateOf<GameSession?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        AppScreen.HOME -> HomeScreen(
                            onPassAndPlay = {
                                gameSession = GameSession(mode = GameMode.PASS_AND_PLAY)
                                screen = AppScreen.GAME
                            },
                            onVsAi = { screen = AppScreen.AI_SETUP },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.AI_SETUP -> AiSetupScreen(
                            onStart = { session ->
                                gameSession = session
                                screen = AppScreen.GAME
                            },
                            onBack = { screen = AppScreen.HOME },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.GAME -> gameSession?.let { session ->
                            GameScreen(
                                session = session,
                                onBack = {
                                    screen = AppScreen.HOME
                                    gameSession = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
