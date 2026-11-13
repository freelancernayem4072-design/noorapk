package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BookmarkEntity
import com.example.data.local.FastingDayEntity
import com.example.data.local.NoorDatabase
import com.example.data.local.PersonalNoteEntity
import com.example.data.local.TasbihSessionEntity
import com.example.data.repository.QuranRepository
import com.example.ui.screens.NoorSplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: NoorDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, NoorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app identity uses bengali app name and com_noorlife_islamicapp package`() {
        assertEquals("নূর লাইফ", context.getString(R.string.app_name))
        assertEquals("NOOR LIFE", context.getString(R.string.brand_english))
        assertEquals("com.noorlife.islamicapp", BuildConfig.APPLICATION_ID)
    }

    @Test
    fun `complete offline quran asset loads all 114 surahs and 6236 ayahs with bengali content`() = runBlocking {
        val repo = QuranRepository(
            context = context,
            tafsirCacheDao = db.tafsirCacheDao(),
            audioDownloadDao = db.audioDownloadDao()
        )
        val surahs = repo.getAllSurahSummaries()
        assertEquals(114, surahs.size)
        assertEquals(6236, surahs.sumOf { it.ayahCount })
        assertEquals("আল-ফাতিহা", surahs.first().nameBengali)
        assertEquals("আন-নাস", surahs.last().nameBengali)

        val fatihah = repo.getSurahDetail(1)
        assertNotNull(fatihah)
        assertEquals(7, fatihah!!.ayahs.size)
        assertTrue(fatihah.ayahs.first().arabicText.isNotBlank())
        assertTrue(fatihah.ayahs.first().bengaliTranslation.isNotBlank())
        assertTrue(fatihah.ayahs.first().bengaliPronunciation.isNotBlank())
        assertTrue(fatihah.ayahs.first().englishTranslation.isNotBlank())

        val searchResults = repo.searchQuran("রহমান", limit = 10)
        assertTrue(searchResults.isNotEmpty())

        val tafsirResult = repo.loadOrFetchTafsir(1)
        assertTrue(tafsirResult.isSuccess)
        val cachedTafsir = db.tafsirCacheDao().observeBySurah(1).first()
        assertTrue(cachedTafsir.isNotEmpty())

        val hadiths = com.example.data.content.IslamicKnowledgeData.loadBundledBukhariHadith(context)
        assertTrue("Expected bundled + curated hadiths > 15, got ${hadiths.size}", hadiths.size > 15)
        assertTrue(hadiths.any { it.bookBn.contains("বুখারী") && it.bengaliText.isNotBlank() })
    }

    @Test
    fun `room database persists bookmarks tasbih notes and fasting records`() = runBlocking {
        // Bookmarks
        val bookmark = BookmarkEntity(
            id = "ayah:2:255",
            type = "AYAH",
            title = "সূরা আল-বাকারা : ২৫৫",
            subtitle = "আয়াতুল কুরসি",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ"
        )
        db.bookmarkDao().insert(bookmark)
        assertTrue(db.bookmarkDao().exists("ayah:2:255"))
        db.bookmarkDao().deleteById("ayah:2:255")
        assertFalse(db.bookmarkDao().exists("ayah:2:255"))

        // Tasbih
        val sessionId = db.tasbihDao().insertSession(
            TasbihSessionEntity(
                titleBn = "সুবহানাল্লাহ",
                titleEn = "SubhanAllah",
                arabicText = "سُبْحَانَ اللَّهِ",
                pronunciationBn = "সুবহানাল্লাহ",
                meaningBn = "আল্লাহ পবিত্র",
                currentCount = 12,
                targetCount = 33,
                totalLifetimeCount = 12,
                isDefault = true
            )
        )
        assertTrue(sessionId > 0L)
        assertEquals(1, db.tasbihDao().sessionCount())

        // Notes
        db.personalNoteDao().upsert(
            PersonalNoteEntity(title = "জুমার খুতবা নোট", content = "দরুদ ও সূরা কাহফ পাঠ")
        )
        assertEquals(1, db.personalNoteDao().observeAll().first().size)

        // Fasting
        db.fastingDao().upsert(FastingDayEntity(dateKey = "ramadan-day-01", isFasted = true))
        val fastingDays = db.fastingDao().observeAll().first()
        assertEquals(1, fastingDays.size)
        assertTrue(fastingDays.first().isFasted)
    }

    @Test
    fun `splash screen renders bengali branding and completes cleanly`() {
        var finished = false
        composeRule.setContent {
            NoorSplashScreen(onFinished = { finished = true })
        }
        composeRule.onNodeWithText("নূর লাইফ").assertIsDisplayed()
        composeRule.onNodeWithText("আপনার সম্পূর্ণ ইসলামিক জীবনসঙ্গী").assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(2500L)
        composeRule.waitForIdle()
        assertTrue(finished)
    }

    @Test
    fun `clicking prayer in rakat list reveals detailed step by step rakats`() {
        val fajr = com.example.data.content.NamazAndWuduData.rakatDetailsList.first()
        composeRule.setContent {
            com.example.ui.screens.InteractivePrayerRakatCard(detail = fajr)
        }
        composeRule.onNodeWithText("১. ফজরের নামাজ").assertIsDisplayed()
        composeRule.onNodeWithText("মোট ৪ রাকাত").assertIsDisplayed()
        composeRule.onNodeWithTag("rakat_card_fajr").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ধারাবাহিক রাকাত বিন্যাস:").assertIsDisplayed()
        composeRule.onNodeWithText("সুন্নতে মুয়াক্কাদা").assertIsDisplayed()
        composeRule.onNodeWithText("ফরজ").assertIsDisplayed()
    }

    @Test
    fun `adhan notification channels and real time prayer alarms schedule cleanly`() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        com.example.notifications.PrayerNotificationScheduler.ensureChannels(context)
        val liveChannel = nm.getNotificationChannel(
            com.example.notifications.PrayerNotificationScheduler.CHANNEL_ADHAN_LIVE
        )
        assertNotNull(liveChannel)
        assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, liveChannel.importance)

        com.example.notifications.PrayerNotificationScheduler.scheduleUpcomingPrayers(
            context,
            com.example.data.local.UserPreferences()
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val shadowAm = org.robolectric.Shadows.shadowOf(am)
        assertTrue(shadowAm.scheduledAlarms.isNotEmpty() || shadowAm.nextScheduledAlarm != null)
    }
}
