package com.sohrab.baghbakri.settings

import android.content.Context
import android.content.SharedPreferences

data class AppSettings(
    val showMoveHints: Boolean = true,
    val showStatusTips: Boolean = true,
    val hapticFeedback: Boolean = true,
    val showLastMove: Boolean = true
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        showMoveHints = prefs.getBoolean(KEY_SHOW_MOVE_HINTS, true),
        showStatusTips = prefs.getBoolean(KEY_SHOW_STATUS_TIPS, true),
        hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
        showLastMove = prefs.getBoolean(KEY_SHOW_LAST_MOVE, true)
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean(KEY_SHOW_MOVE_HINTS, settings.showMoveHints)
            .putBoolean(KEY_SHOW_STATUS_TIPS, settings.showStatusTips)
            .putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            .putBoolean(KEY_SHOW_LAST_MOVE, settings.showLastMove)
            .apply()
    }

    fun isTutorialDone(): Boolean = prefs.getBoolean(KEY_TUTORIAL_DONE, false)

    fun setTutorialDone(done: Boolean = true) {
        prefs.edit().putBoolean(KEY_TUTORIAL_DONE, done).apply()
    }

    fun isLanguageChosen(): Boolean = prefs.getBoolean(KEY_LANGUAGE_CHOSEN, false)

    fun getLanguage(): AppLanguage =
        AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.tag))

    /** Persist language only. Caller should recreate the Activity to apply it. */
    fun setLanguage(language: AppLanguage) {
        prefs.edit()
            .putString(KEY_LANGUAGE, language.tag)
            .putBoolean(KEY_LANGUAGE_CHOSEN, true)
            .commit()
    }

    fun setPendingRoute(route: String) {
        prefs.edit().putString(KEY_PENDING_ROUTE, route).commit()
    }

    fun consumePendingRoute(): String? {
        val route = prefs.getString(KEY_PENDING_ROUTE, null) ?: return null
        prefs.edit().remove(KEY_PENDING_ROUTE).commit()
        return route
    }

    companion object {
        const val PREFS_NAME = "bagh_bakri_prefs"
        private const val KEY_TUTORIAL_DONE = "tutorial_done"
        private const val KEY_LANGUAGE_CHOSEN = "language_chosen"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_PENDING_ROUTE = "pending_route"
        private const val KEY_SHOW_MOVE_HINTS = "show_move_hints"
        private const val KEY_SHOW_STATUS_TIPS = "show_status_tips"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SHOW_LAST_MOVE = "show_last_move"
    }
}
