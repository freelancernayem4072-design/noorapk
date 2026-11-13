package com.example

import com.example.data.content.QuranCatalogMetadata
import com.example.data.remote.ArabicBengaliTransliterator
import com.example.domain.prayer.AsrMadhhab
import com.example.domain.prayer.BangladeshCitiesCatalog
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.localization.AppLanguage
import com.example.localization.BengaliFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `dhaka daily prayer schedule maintains strict chronological order and madhhab rules`() {
        val dhaka = BangladeshCitiesCatalog.defaultDhaka
        val hanafi = PrayerTimeCalculator.calculateDailySchedule(
            year = 2026,
            month = 4,
            day = 15,
            city = dhaka,
            method = CalculationMethod.ISLAMIC_FOUNDATION_BD,
            madhhab = AsrMadhhab.HANAFI
        )
        val shafii = PrayerTimeCalculator.calculateDailySchedule(
            year = 2026,
            month = 4,
            day = 15,
            city = dhaka,
            method = CalculationMethod.ISLAMIC_FOUNDATION_BD,
            madhhab = AsrMadhhab.SHAFI
        )

        assertTrue(hanafi.sehriEndMillis < hanafi.fajr.epochMillis)
        assertTrue(hanafi.fajr.epochMillis < hanafi.sunrise.epochMillis)
        assertTrue(hanafi.sunrise.epochMillis < hanafi.dhuhr.epochMillis)
        assertTrue(hanafi.dhuhr.epochMillis < hanafi.asr.epochMillis)
        assertTrue(hanafi.asr.epochMillis < hanafi.maghrib.epochMillis)
        assertTrue(hanafi.maghrib.epochMillis < hanafi.isha.epochMillis)

        // Hanafi Asr (2x shadow) must always be later than Shafi'i Asr (1x shadow)
        assertTrue(hanafi.asr.epochMillis > shafii.asr.epochMillis)

        // Fajr in Dhaka in mid-April should reasonably fall around 4 AM local time
        assertTrue(hanafi.fajr.hour24 in 3..5)
        // Maghrib in Dhaka in mid-April should fall around 6 PM local time
        assertTrue(hanafi.maghrib.hour24 in 17..19)
    }

    @Test
    fun `monthly prayer schedule returns exact calendar day counts including leap years`() {
        val dhaka = BangladeshCitiesCatalog.defaultDhaka
        val febLeap = PrayerTimeCalculator.calculateMonthlySchedule(
            2024, 2, dhaka, CalculationMethod.ISLAMIC_FOUNDATION_BD, AsrMadhhab.HANAFI
        )
        val febNonLeap = PrayerTimeCalculator.calculateMonthlySchedule(
            2026, 2, dhaka, CalculationMethod.ISLAMIC_FOUNDATION_BD, AsrMadhhab.HANAFI
        )
        assertEquals(29, febLeap.size)
        assertEquals(28, febNonLeap.size)
    }

    @Test
    fun `qibla bearing and kaaba distance from dhaka match spherical geometry`() {
        val dhaka = BangladeshCitiesCatalog.defaultDhaka
        val bearing = PrayerTimeCalculator.calculateQiblaBearing(dhaka.latitude, dhaka.longitude)
        val distanceKm = PrayerTimeCalculator.calculateDistanceToKaabaKm(dhaka.latitude, dhaka.longitude)

        // Dhaka to Makkah is roughly WNW (~277.6 degrees) and ~5,168 km
        assertTrue("Expected bearing ~277.6 deg, got $bearing", bearing in 275.0..280.0)
        assertTrue("Expected distance ~5168 km, got $distanceKm", distanceKm in 5100.0..5250.0)
    }

    @Test
    fun `hijri date moon sighting offset shifts date cleanly`() {
        val base = PrayerTimeCalculator.gregorianToHijri(2026, 3, 21, moonOffsetDays = 0)
        val plusOne = PrayerTimeCalculator.gregorianToHijri(2026, 3, 21, moonOffsetDays = 1)
        val minusOne = PrayerTimeCalculator.gregorianToHijri(2026, 3, 21, moonOffsetDays = -1)

        assertTrue(base.year >= 1447)
        assertTrue(base.month in 1..12)
        assertTrue(base.monthNameBn.isNotBlank())
        assertTrue(plusOne.day != base.day || plusOne.month != base.month)
        assertTrue(minusOne.day != base.day || minusOne.month != base.month)
    }

    @Test
    fun `bangla revised calendar maps pohela boishakh accurately`() {
        val pohela = PrayerTimeCalculator.gregorianToBanglaDate(2026, 4, 14)
        assertEquals(1, pohela.day)
        assertEquals(1, pohela.month)
        assertEquals("বৈশাখ", pohela.monthNameBn)
        assertEquals(1433, pohela.year)
    }

    @Test
    fun `bengali formatter converts numerals countdowns and 12h clock`() {
        assertEquals("১২৩৪৫৬৭৮৯০", BengaliFormatter.toBengaliDigits("1234567890"))
        assertEquals("০১:০৫:০৯", BengaliFormatter.formatCountdown(3909, AppLanguage.BENGALI))
        assertEquals("01:05:09", BengaliFormatter.formatCountdown(3909, AppLanguage.ENGLISH))
        assertEquals("৫:৩০ (ভোর)", BengaliFormatter.formatTime12Hour(5, 30, AppLanguage.BENGALI))
    }

    @Test
    fun `arabic to bengali transliterator produces readable bengali pronunciation`() {
        val bismillahBn = ArabicBengaliTransliterator.toBengaliPronunciation("Bismillahi ar-rahmani ar-rahim")
        assertTrue(bismillahBn.contains("বিসমিল্লাহি"))
        assertTrue(bismillahBn.contains("আর-রাহমানি"))
        assertTrue(bismillahBn.contains("আর-রাহীম"))
    }

    @Test
    fun `bangladesh districts and surah metadata catalogs are complete`() {
        assertTrue(BangladeshCitiesCatalog.cities.size >= 64)
        assertEquals(114, QuranCatalogMetadata.surahBnMap.size)
        assertEquals("আল-ফাতিহা", QuranCatalogMetadata.surahBnMap[1]?.first)
        assertEquals("আন-নাস", QuranCatalogMetadata.surahBnMap[114]?.first)
    }

    @Test
    fun `live clock formatter formats exact seconds accurately in bengali and english`() {
        assertEquals("৫:০৪:০৯ ভোর", BengaliFormatter.formatLiveClock(5, 4, 9, AppLanguage.BENGALI))
        assertEquals("১২:৩০:৪৫ দুপুর", BengaliFormatter.formatLiveClock(12, 30, 45, AppLanguage.BENGALI))
        assertEquals("5:04:09 AM", BengaliFormatter.formatLiveClock(5, 4, 9, AppLanguage.ENGLISH))
        assertEquals("12:30:45 PM", BengaliFormatter.formatLiveClock(12, 30, 45, AppLanguage.ENGLISH))
    }
}
