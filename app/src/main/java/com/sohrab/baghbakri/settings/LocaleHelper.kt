package com.sohrab.baghbakri.settings

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        return context.createConfigurationContext(config)
    }

    fun applyStoredLanguage(context: Context): Context {
        val prefs = AppPreferences(context)
        return if (prefs.isLanguageChosen()) {
            wrap(context, prefs.getLanguage())
        } else {
            context
        }
    }
}
