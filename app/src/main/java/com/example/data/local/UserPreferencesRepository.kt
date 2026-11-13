package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.prayer.AsrMadhhab
import com.example.domain.prayer.BangladeshCitiesCatalog
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.PrayerWaqt
import com.example.localization.AppLanguage
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.noorDataStore: DataStore<Preferences> by preferencesDataStore(name = "noor_life_prefs")

enum class NotificationSoundType(val id: String, val bnName: String, val enName: String) {
    ADHAN_CHIME("adhan_chime", "আজান সুরেলা ধ্বনি", "Adhan Melodic Chime"),
    FAJR_CHIME("fajr_chime", "ফজর আজান ধ্বনি", "Fajr Melodic Chime"),
    SYSTEM_DEFAULT("system_default", "ফোনের ডিফল্ট সাউন্ড", "Device Default Sound"),
    SILENT("silent", "নীরব (শুধু বার্তা)", "Silent (Visual Only)");

    companion object {
        fun fromId(id: String?): NotificationSoundType =
            entries.firstOrNull { it.id == id } ?: ADHAN_CHIME
    }
}

data class UserPreferences(
    val userName: String = "মুসলিম ব্যবহারকারী",
    val language: AppLanguage = AppLanguage.BENGALI,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val city: CityLocation = BangladeshCitiesCatalog.defaultDhaka,
    val useGpsLocation: Boolean = false,
    val calculationMethod: CalculationMethod = CalculationMethod.ISLAMIC_FOUNDATION_BD,
    val asrMadhhab: AsrMadhhab = AsrMadhhab.HANAFI,
    val hijriMoonOffset: Int = 0,
    val notifyFajr: Boolean = true,
    val notifySunrise: Boolean = false,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,
    val notifySehriIftar: Boolean = true,
    val notificationSound: NotificationSoundType = NotificationSoundType.ADHAN_CHIME,
    val vibrationEnabled: Boolean = true,
    val tasbihSoundEnabled: Boolean = true,
    val tasbihVibrationEnabled: Boolean = true,
    val arabicFontSize: Float = 26f,
    val bengaliFontSize: Float = 16f,
    val showBengaliPronunciation: Boolean = true,
    val showEnglishTranslation: Boolean = false,
    val selectedReciterId: String = "ar.alafasy",
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val goldPricePerGramBdt: Double = 11500.0,
    val silverPricePerGramBdt: Double = 165.0
) {
    fun isNotificationEnabled(waqt: PrayerWaqt): Boolean = when (waqt) {
        PrayerWaqt.FAJR -> notifyFajr
        PrayerWaqt.SUNRISE -> notifySunrise
        PrayerWaqt.DHUHR -> notifyDhuhr
        PrayerWaqt.ASR -> notifyAsr
        PrayerWaqt.MAGHRIB -> notifyMaghrib
        PrayerWaqt.ISHA -> notifyIsha
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CITY_ID = stringPreferencesKey("city_id")
        val CITY_NAME_BN = stringPreferencesKey("city_name_bn")
        val CITY_NAME_EN = stringPreferencesKey("city_name_en")
        val CITY_LAT = doublePreferencesKey("city_lat")
        val CITY_LON = doublePreferencesKey("city_lon")
        val CITY_TZ = stringPreferencesKey("city_tz")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ASR_MADHHAB = stringPreferencesKey("asr_madhhab")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_SUNRISE = booleanPreferencesKey("notify_sunrise")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")
        val NOTIFY_SEHRI_IFTAR = booleanPreferencesKey("notify_sehri_iftar")
        val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val TASBIH_SOUND = booleanPreferencesKey("tasbih_sound")
        val TASBIH_VIBRATION = booleanPreferencesKey("tasbih_vibration")
        val ARABIC_FONT_SIZE = floatPreferencesKey("arabic_font_size")
        val BENGALI_FONT_SIZE = floatPreferencesKey("bengali_font_size")
        val SHOW_PRONUNCIATION = booleanPreferencesKey("show_pronunciation")
        val SHOW_ENGLISH = booleanPreferencesKey("show_english")
        val RECITER_ID = stringPreferencesKey("reciter_id")
        val LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val LAST_READ_AYAH = intPreferencesKey("last_read_ayah")
        val GOLD_RATE = doublePreferencesKey("gold_rate")
        val SILVER_RATE = doublePreferencesKey("silver_rate")
    }

    val preferencesFlow: Flow<UserPreferences> = context.noorDataStore.data.map { prefs ->
        val defaultCity = BangladeshCitiesCatalog.defaultDhaka
        val cityId = prefs[Keys.CITY_ID] ?: defaultCity.id
        val preset = BangladeshCitiesCatalog.findById(cityId)
        val city = if (cityId == "gps") {
            CityLocation(
                id = "gps",
                bnName = prefs[Keys.CITY_NAME_BN] ?: "জিপিএস অবস্থান",
                enName = prefs[Keys.CITY_NAME_EN] ?: "GPS Location",
                countryBn = "বাংলাদেশ / বর্তমান অবস্থান",
                countryEn = "Current GPS Location",
                latitude = prefs[Keys.CITY_LAT] ?: defaultCity.latitude,
                longitude = prefs[Keys.CITY_LON] ?: defaultCity.longitude,
                timeZoneId = prefs[Keys.CITY_TZ] ?: java.util.TimeZone.getDefault().id
            )
        } else {
            preset
        }
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = ThemeMode.entries.firstOrNull { it.name == themeStr } ?: ThemeMode.SYSTEM

        UserPreferences(
            userName = prefs[Keys.USER_NAME] ?: "মুসলিম ব্যবহারকারী",
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            themeMode = themeMode,
            city = city,
            useGpsLocation = prefs[Keys.USE_GPS] ?: false,
            calculationMethod = CalculationMethod.fromId(prefs[Keys.CALC_METHOD]),
            asrMadhhab = AsrMadhhab.fromId(prefs[Keys.ASR_MADHHAB]),
            hijriMoonOffset = (prefs[Keys.HIJRI_OFFSET] ?: 0).coerceIn(-2, 2),
            notifyFajr = prefs[Keys.NOTIFY_FAJR] ?: true,
            notifySunrise = prefs[Keys.NOTIFY_SUNRISE] ?: false,
            notifyDhuhr = prefs[Keys.NOTIFY_DHUHR] ?: true,
            notifyAsr = prefs[Keys.NOTIFY_ASR] ?: true,
            notifyMaghrib = prefs[Keys.NOTIFY_MAGHRIB] ?: true,
            notifyIsha = prefs[Keys.NOTIFY_ISHA] ?: true,
            notifySehriIftar = prefs[Keys.NOTIFY_SEHRI_IFTAR] ?: true,
            notificationSound = NotificationSoundType.fromId(prefs[Keys.NOTIFICATION_SOUND]),
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            tasbihSoundEnabled = prefs[Keys.TASBIH_SOUND] ?: true,
            tasbihVibrationEnabled = prefs[Keys.TASBIH_VIBRATION] ?: true,
            arabicFontSize = prefs[Keys.ARABIC_FONT_SIZE] ?: 26f,
            bengaliFontSize = prefs[Keys.BENGALI_FONT_SIZE] ?: 16f,
            showBengaliPronunciation = prefs[Keys.SHOW_PRONUNCIATION] ?: true,
            showEnglishTranslation = prefs[Keys.SHOW_ENGLISH] ?: false,
            selectedReciterId = prefs[Keys.RECITER_ID] ?: "ar.alafasy",
            lastReadSurah = prefs[Keys.LAST_READ_SURAH] ?: 1,
            lastReadAyah = prefs[Keys.LAST_READ_AYAH] ?: 1,
            goldPricePerGramBdt = prefs[Keys.GOLD_RATE] ?: 11500.0,
            silverPricePerGramBdt = prefs[Keys.SILVER_RATE] ?: 165.0
        )
    }

    suspend fun setUserName(name: String) {
        context.noorDataStore.edit { it[Keys.USER_NAME] = name.ifBlank { "মুসলিম ব্যবহারকারী" } }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.noorDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.noorDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCity(city: CityLocation, isGps: Boolean = false) {
        context.noorDataStore.edit {
            it[Keys.CITY_ID] = if (isGps) "gps" else city.id
            it[Keys.CITY_NAME_BN] = city.bnName
            it[Keys.CITY_NAME_EN] = city.enName
            it[Keys.CITY_LAT] = city.latitude
            it[Keys.CITY_LON] = city.longitude
            it[Keys.CITY_TZ] = city.timeZoneId
            it[Keys.USE_GPS] = isGps
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.noorDataStore.edit { it[Keys.CALC_METHOD] = method.id }
    }

    suspend fun setAsrMadhhab(madhhab: AsrMadhhab) {
        context.noorDataStore.edit { it[Keys.ASR_MADHHAB] = madhhab.id }
    }

    suspend fun setHijriMoonOffset(offset: Int) {
        context.noorDataStore.edit { it[Keys.HIJRI_OFFSET] = offset.coerceIn(-2, 2) }
    }

    suspend fun setPrayerNotification(waqt: PrayerWaqt, enabled: Boolean) {
        context.noorDataStore.edit {
            when (waqt) {
                PrayerWaqt.FAJR -> it[Keys.NOTIFY_FAJR] = enabled
                PrayerWaqt.SUNRISE -> it[Keys.NOTIFY_SUNRISE] = enabled
                PrayerWaqt.DHUHR -> it[Keys.NOTIFY_DHUHR] = enabled
                PrayerWaqt.ASR -> it[Keys.NOTIFY_ASR] = enabled
                PrayerWaqt.MAGHRIB -> it[Keys.NOTIFY_MAGHRIB] = enabled
                PrayerWaqt.ISHA -> it[Keys.NOTIFY_ISHA] = enabled
            }
        }
    }

    suspend fun setSehriIftarNotification(enabled: Boolean) {
        context.noorDataStore.edit { it[Keys.NOTIFY_SEHRI_IFTAR] = enabled }
    }

    suspend fun setNotificationSound(sound: NotificationSoundType) {
        context.noorDataStore.edit { it[Keys.NOTIFICATION_SOUND] = sound.id }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.noorDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setTasbihAudioHaptics(sound: Boolean, vibration: Boolean) {
        context.noorDataStore.edit {
            it[Keys.TASBIH_SOUND] = sound
            it[Keys.TASBIH_VIBRATION] = vibration
        }
    }

    suspend fun setQuranReadingSettings(
        arabicSize: Float,
        bengaliSize: Float,
        showPronunciation: Boolean,
        showEnglish: Boolean
    ) {
        context.noorDataStore.edit {
            it[Keys.ARABIC_FONT_SIZE] = arabicSize.coerceIn(18f, 40f)
            it[Keys.BENGALI_FONT_SIZE] = bengaliSize.coerceIn(13f, 24f)
            it[Keys.SHOW_PRONUNCIATION] = showPronunciation
            it[Keys.SHOW_ENGLISH] = showEnglish
        }
    }

    suspend fun setSelectedReciter(reciterId: String) {
        context.noorDataStore.edit { it[Keys.RECITER_ID] = reciterId }
    }

    suspend fun setLastReadPosition(surah: Int, ayah: Int) {
        context.noorDataStore.edit {
            it[Keys.LAST_READ_SURAH] = surah
            it[Keys.LAST_READ_AYAH] = ayah
        }
    }

    suspend fun setZakatMetalRates(goldPerGram: Double, silverPerGram: Double) {
        context.noorDataStore.edit {
            it[Keys.GOLD_RATE] = goldPerGram
            it[Keys.SILVER_RATE] = silverPerGram
        }
    }
}
