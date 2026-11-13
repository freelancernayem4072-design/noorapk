package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NoorRoute {
    // 5 Primary Bottom Tabs
    @Serializable data object Home : NoorRoute
    @Serializable data object QuranHome : NoorRoute
    @Serializable data object PrayerHub : NoorRoute
    @Serializable data object ToolsHub : NoorRoute
    @Serializable data object ProfileHub : NoorRoute

    // Secondary / Feature Screens
    @Serializable data class SurahReader(val surahNumber: Int, val targetAyah: Int = 1) : NoorRoute
    @Serializable data object QiblaCompass : NoorRoute
    @Serializable data object DigitalTasbih : NoorRoute
    @Serializable data object NamesOfAllah : NoorRoute
    @Serializable data object DuaLibrary : NoorRoute
    @Serializable data object FiveKalimas : NoorRoute
    @Serializable data object NamazEducation : NoorRoute
    @Serializable data object WuduGhusl : NoorRoute
    @Serializable data object IslamicCalendar : NoorRoute
    @Serializable data object RamadanModule : NoorRoute
    @Serializable data object ZakatCalculator : NoorRoute
    @Serializable data object HadithKnowledge : NoorRoute
    @Serializable data object IslamicQuiz : NoorRoute
    @Serializable data object GreetingCards : NoorRoute
    @Serializable data object Wallpapers : NoorRoute
    @Serializable data object PersonalNotes : NoorRoute
    @Serializable data object GlobalSearch : NoorRoute
    @Serializable data object Bookmarks : NoorRoute
    @Serializable data object Settings : NoorRoute
    @Serializable data object PrivacyAbout : NoorRoute
}
