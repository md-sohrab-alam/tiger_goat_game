package com.sohrab.baghbakri.settings

enum class AppLanguage(val tag: String, val nativeLabel: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    NEPALI("ne", "नेपाली");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.find { it.tag == tag } ?: ENGLISH
    }
}
