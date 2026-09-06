package com.sohrab.baghbakri

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.sohrab.baghbakri.game.GameMode
import com.sohrab.baghbakri.game.GameSession
import com.sohrab.baghbakri.settings.AppPreferences
import com.sohrab.baghbakri.settings.LocaleHelper
import com.sohrab.baghbakri.ui.game.GameScreen
import com.sohrab.baghbakri.ui.home.AiSetupScreen
import com.sohrab.baghbakri.ui.home.HomeScreen
import com.sohrab.baghbakri.ui.language.LanguagePickerScreen
import com.sohrab.baghbakri.ui.privacy.PrivacyPolicyScreen
import com.sohrab.baghbakri.ui.settings.SettingsScreen
import com.sohrab.baghbakri.ui.theme.BaghBakriTheme
import com.sohrab.baghbakri.ui.tutorial.TutorialScreen

private enum class AppScreen {
    LANGUAGE,
    HOME,
    AI_SETUP,
    GAME,
    TUTORIAL,
    SETTINGS,
    PRIVACY
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaghBakriTheme {
                val context = LocalContext.current
                val prefs = remember { AppPreferences(context) }

                fun defaultScreen(): AppScreen = when {
                    !prefs.isLanguageChosen() -> AppScreen.LANGUAGE
                    !prefs.isTutorialDone() -> AppScreen.TUTORIAL
                    else -> AppScreen.HOME
                }

                var screen by rememberSaveable { mutableStateOf(defaultScreen().name) }
                var gameSession by remember { mutableStateOf<GameSession?>(null) }
                var tutorialReturnScreen by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }
                var privacyReturnScreen by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }

                LaunchedEffect(Unit) {
                    prefs.consumePendingRoute()?.let { pending ->
                        screen = pending
                    }
                }

                fun go(target: AppScreen) {
                    screen = target.name
                }

                fun changeLanguage(language: com.sohrab.baghbakri.settings.AppLanguage, nextScreen: AppScreen) {
                    prefs.setPendingRoute(nextScreen.name)
                    prefs.setLanguage(language)
                    recreate()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (AppScreen.valueOf(screen)) {
                        AppScreen.LANGUAGE -> LanguagePickerScreen(
                            initial = prefs.getLanguage(),
                            onContinue = { language ->
                                val next = if (!prefs.isTutorialDone()) {
                                    AppScreen.TUTORIAL
                                } else {
                                    AppScreen.HOME
                                }
                                changeLanguage(language, next)
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.HOME -> HomeScreen(
                            onPassAndPlay = {
                                gameSession = GameSession(mode = GameMode.PASS_AND_PLAY)
                                go(AppScreen.GAME)
                            },
                            onVsAi = { go(AppScreen.AI_SETUP) },
                            onSettings = { go(AppScreen.SETTINGS) },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.AI_SETUP -> AiSetupScreen(
                            onStart = { session ->
                                gameSession = session
                                go(AppScreen.GAME)
                            },
                            onBack = { go(AppScreen.HOME) },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.GAME -> gameSession?.let { session ->
                            GameScreen(
                                session = session,
                                onBack = {
                                    go(AppScreen.HOME)
                                    gameSession = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.SETTINGS -> SettingsScreen(
                            onBack = { go(AppScreen.HOME) },
                            onOpenTutorial = {
                                tutorialReturnScreen = AppScreen.SETTINGS.name
                                go(AppScreen.TUTORIAL)
                            },
                            onOpenPrivacy = {
                                privacyReturnScreen = AppScreen.SETTINGS.name
                                go(AppScreen.PRIVACY)
                            },
                            onLanguageSelected = { language ->
                                if (language != prefs.getLanguage()) {
                                    changeLanguage(language, AppScreen.SETTINGS)
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.TUTORIAL -> TutorialScreen(
                            onFinished = {
                                prefs.setTutorialDone(true)
                                go(AppScreen.valueOf(tutorialReturnScreen))
                            },
                            onSkip = {
                                prefs.setTutorialDone(true)
                                go(AppScreen.valueOf(tutorialReturnScreen))
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppScreen.PRIVACY -> PrivacyPolicyScreen(
                            onBack = { go(AppScreen.valueOf(privacyReturnScreen)) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
