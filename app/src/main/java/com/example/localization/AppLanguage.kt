package com.example.localization

enum class AppLanguage(val code: String, val nativeName: String) {
    BENGALI("bn", "বাংলা"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BENGALI
    }
}
