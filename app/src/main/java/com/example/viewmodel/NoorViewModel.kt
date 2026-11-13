package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NoorLifeApplication
import com.example.audio.QuranPlaybackState
import com.example.data.content.DuaEntry
import com.example.data.content.HadithEntry
import com.example.data.content.IslamicKnowledgeData
import com.example.data.content.KnowledgeArticle
import com.example.data.content.NamesOfAllahData
import com.example.data.content.QuranAyah
import com.example.data.content.SurahDetail
import com.example.data.content.SurahSummary
import com.example.data.content.DuasAndKalimasData
import com.example.data.content.NamazAndWuduData
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedHadithEntity
import com.example.data.local.CachedTafsirEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.FastingDayEntity
import com.example.data.local.NotificationSoundType
import com.example.data.local.PersonalNoteEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.RecentItemEntity
import com.example.data.local.TasbihHistoryEntity
import com.example.data.local.TasbihSessionEntity
import com.example.data.local.UserPreferences
import com.example.domain.prayer.AsrMadhhab
import com.example.domain.prayer.BanglaDateInfo
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.DailyPrayerSchedule
import com.example.domain.prayer.HijriDateInfo
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerTimeSlot
import com.example.domain.prayer.PrayerWaqt
import com.example.localization.AppLanguage
import com.example.localization.BengaliFormatter
import com.example.location.LocationHelper
import com.example.notifications.PrayerNotificationScheduler
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class LivePrayerStatus(
    val todaySchedule: DailyPrayerSchedule,
    val currentWaqt: PrayerWaqt,
    val nextSlot: PrayerTimeSlot,
    val countdownSeconds: Long,
    val hijriDate: HijriDateInfo,
    val banglaDate: BanglaDateInfo,
    val gregorianDateBn: String,
    val gregorianDateEn: String,
    val qiblaBearingDeg: Double,
    val kaabaDistanceKm: Double
)

data class GlobalSearchResults(
    val surahs: List<SurahSummary> = emptyList(),
    val ayahs: List<Pair<SurahSummary, QuranAyah>> = emptyList(),
    val duas: List<DuaEntry> = emptyList(),
    val hadiths: List<HadithEntry> = emptyList(),
    val allahNames: List<com.example.data.content.AllahNameEntry> = emptyList(),
    val guides: List<KnowledgeArticle> = emptyList()
)

class NoorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NoorLifeApplication
    private val db = app.database
    private val prefsRepo = app.preferencesRepository
    private val quranRepo = app.quranRepository
    val audioController = app.quranAudioController

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val prefs: StateFlow<UserPreferences> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserPreferences()
    )

    val isOnline: StateFlow<Boolean> = app.connectivityObserver.isOnlineFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = app.connectivityObserver.isCurrentlyOnline()
    )

    val audioState: StateFlow<QuranPlaybackState> = audioController.state

    val bookmarks: StateFlow<List<BookmarkEntity>> = db.bookmarkDao().observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasbihSessions: StateFlow<List<TasbihSessionEntity>> = db.tasbihDao().observeSessions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasbihHistory: StateFlow<List<TasbihHistoryEntity>> = db.tasbihDao().observeHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val fastingDays: StateFlow<List<FastingDayEntity>> = db.fastingDao().observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val personalNotes: StateFlow<List<PersonalNoteEntity>> = db.personalNoteDao().observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentItems: StateFlow<List<RecentItemEntity>> = db.recentItemDao().observeRecents().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloadedAudio: StateFlow<List<DownloadedAudioEntity>> = quranRepo.observeDownloadedAudio().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = db.quizAttemptDao().observeAttempts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _livePrayerStatus = MutableStateFlow(computeLivePrayerStatus(UserPreferences()))
    val livePrayerStatus: StateFlow<LivePrayerStatus> = _livePrayerStatus.asStateFlow()

    private val _surahSummaries = MutableStateFlow<List<SurahSummary>>(emptyList())
    val surahSummaries: StateFlow<List<SurahSummary>> = _surahSummaries.asStateFlow()

    private val _dailyAyah = MutableStateFlow<Pair<SurahSummary, QuranAyah>?>(null)
    val dailyAyah: StateFlow<Pair<SurahSummary, QuranAyah>?> = _dailyAyah.asStateFlow()

    private val _currentSurahDetail = MutableStateFlow<SurahDetail?>(null)
    val currentSurahDetail: StateFlow<SurahDetail?> = _currentSurahDetail.asStateFlow()

    private val _currentSurahTafsir = MutableStateFlow<Map<Int, CachedTafsirEntity>>(emptyMap())
    val currentSurahTafsir: StateFlow<Map<Int, CachedTafsirEntity>> = _currentSurahTafsir.asStateFlow()

    private val _tafsirLoading = MutableStateFlow(false)
    val tafsirLoading: StateFlow<Boolean> = _tafsirLoading.asStateFlow()

    private val _tafsirError = MutableStateFlow<String?>(null)
    val tafsirError: StateFlow<String?> = _tafsirError.asStateFlow()

    private val _audioDownloadingSurahs = MutableStateFlow<Set<Int>>(emptySet())
    val audioDownloadingSurahs: StateFlow<Set<Int>> = _audioDownloadingSurahs.asStateFlow()

    private val _hadithList = MutableStateFlow<List<HadithEntry>>(IslamicKnowledgeData.curatedHadith)
    val hadithList: StateFlow<List<HadithEntry>> = _hadithList.asStateFlow()

    private val _hadithSyncMessage = MutableStateFlow<String?>(null)
    val hadithSyncMessage: StateFlow<String?> = _hadithSyncMessage.asStateFlow()

    private val _locationDetecting = MutableStateFlow(false)
    val locationDetecting: StateFlow<Boolean> = _locationDetecting.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _searchResults = MutableStateFlow(GlobalSearchResults())
    val searchResults: StateFlow<GlobalSearchResults> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            seedDefaultTasbihSessions()
        }
        viewModelScope.launch {
            val summaries = quranRepo.getAllSurahSummaries()
            _surahSummaries.value = summaries
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            _dailyAyah.value = quranRepo.getAyahOfTheDay(dayOfYear)
        }
        viewModelScope.launch {
            val bundled = IslamicKnowledgeData.loadBundledBukhariHadith(app)
            val cached = db.hadithCacheDao().observeAll().first().map {
                HadithEntry(it.id, it.bookNameBn, it.hadithNumber.toString(), it.categoryBn, "", it.text, it.referenceBn)
            }
            _hadithList.value = (bundled + cached).distinctBy { it.id }
        }
        // Live 1-second prayer countdown ticker & auto-reschedule on pref changes
        viewModelScope.launch {
            prefs.collectLatest { currentPrefs ->
                _livePrayerStatus.value = computeLivePrayerStatus(currentPrefs)
                withContext(Dispatchers.IO) {
                    PrayerNotificationScheduler.scheduleUpcomingPrayers(app, currentPrefs)
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _livePrayerStatus.value = computeLivePrayerStatus(prefs.value)
            }
        }
    }

    private suspend fun seedDefaultTasbihSessions() {
        if (db.tasbihDao().sessionCount() > 0) return
        val defaults = listOf(
            TasbihSessionEntity(
                titleBn = "সুবহানাল্লাহ",
                titleEn = "SubhanAllah",
                arabicText = "سُبْحَانَ اللَّهِ",
                pronunciationBn = "সুবহানাল্লাহ",
                meaningBn = "আল্লাহ পবিত্র ও মহিমান্বিত",
                targetCount = 33,
                isDefault = true
            ),
            TasbihSessionEntity(
                titleBn = "আলহামদুলিল্লাহ",
                titleEn = "Alhamdulillah",
                arabicText = "الْحَمْدُ لِلَّهِ",
                pronunciationBn = "আলহামদুলিল্লাহ",
                meaningBn = "সমস্ত প্রশংসা আল্লাহর জন্য",
                targetCount = 33,
                isDefault = true
            ),
            TasbihSessionEntity(
                titleBn = "আল্লাহু আকবার",
                titleEn = "Allahu Akbar",
                arabicText = "اللَّهُ أَكْبَرُ",
                pronunciationBn = "আল্লাহু আকবার",
                meaningBn = "আল্লাহ সর্বশ্রেষ্ঠ",
                targetCount = 34,
                isDefault = true
            ),
            TasbihSessionEntity(
                titleBn = "আস্তাগফিরুল্লাহ",
                titleEn = "Astaghfirullah",
                arabicText = "أَسْتَغْفِرُ اللَّهَ",
                pronunciationBn = "আস্তাগফিরুল্লাহ",
                meaningBn = "আমি আল্লাহর নিকট ক্ষমা প্রার্থনা করছি",
                targetCount = 100,
                isDefault = true
            ),
            TasbihSessionEntity(
                titleBn = "দরুদ শরিফ",
                titleEn = "Durood Sharif",
                arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
                pronunciationBn = "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ ﷺ",
                meaningBn = "হে আল্লাহ! মুহাম্মদ ﷺ-এর ওপর রহমত বর্ষণ করুন",
                targetCount = 100,
                isDefault = true
            ),
            TasbihSessionEntity(
                titleBn = "লা ইলাহা ইল্লাল্লাহ",
                titleEn = "La ilaha illallah",
                arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ",
                pronunciationBn = "লা ইলাহা ইল্লাল্লাহ",
                meaningBn = "আল্লাহ ব্যতীত কোনো সত্য ইলাহ নেই",
                targetCount = 100,
                isDefault = true
            )
        )
        db.tasbihDao().insertSessions(defaults)
    }

    private fun computeLivePrayerStatus(userPrefs: UserPreferences): LivePrayerStatus {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)

        val today = PrayerTimeCalculator.calculateDailySchedule(
            y, m, d, userPrefs.city, userPrefs.calculationMethod, userPrefs.asrMadhhab
        )

        val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrow = PrayerTimeCalculator.calculateDailySchedule(
            tomorrowCal.get(Calendar.YEAR),
            tomorrowCal.get(Calendar.MONTH) + 1,
            tomorrowCal.get(Calendar.DAY_OF_MONTH),
            userPrefs.city,
            userPrefs.calculationMethod,
            userPrefs.asrMadhhab
        )

        val ordered = today.allSlots
        var currentWaqt = PrayerWaqt.ISHA
        var nextSlot: PrayerTimeSlot = tomorrow.fajr
        for (i in ordered.indices) {
            if (now < ordered[i].epochMillis) {
                nextSlot = ordered[i]
                currentWaqt = if (i == 0) PrayerWaqt.ISHA else ordered[i - 1].waqt
                break
            } else {
                currentWaqt = ordered[i].waqt
            }
        }
        val remainingSec = ((nextSlot.epochMillis - now) / 1000L).coerceAtLeast(0L)

        val hijri = PrayerTimeCalculator.gregorianToHijri(y, m, d, userPrefs.hijriMoonOffset)
        val bangla = PrayerTimeCalculator.gregorianToBanglaDate(y, m, d)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0..6
        val weekdayBn = BengaliFormatter.bengaliWeekdays[dayOfWeek]
        val monthBn = BengaliFormatter.bengaliGregorianMonths[m - 1]
        val gregorianBn = "$weekdayBn, ${BengaliFormatter.formatNumber(d)} $monthBn ${BengaliFormatter.formatNumber(y)}"
        val weekdayEn = BengaliFormatter.englishWeekdays[dayOfWeek]
        val monthEn = BengaliFormatter.englishGregorianMonths[m - 1]
        val gregorianEn = "$weekdayEn, $d $monthEn $y"

        val qiblaDeg = PrayerTimeCalculator.calculateQiblaBearing(
            userPrefs.city.latitude,
            userPrefs.city.longitude
        )
        val distKm = PrayerTimeCalculator.calculateDistanceToKaabaKm(
            userPrefs.city.latitude,
            userPrefs.city.longitude
        )

        return LivePrayerStatus(
            todaySchedule = today,
            currentWaqt = currentWaqt,
            nextSlot = nextSlot,
            countdownSeconds = remainingSec,
            hijriDate = hijri,
            banglaDate = bangla,
            gregorianDateBn = gregorianBn,
            gregorianDateEn = gregorianEn,
            qiblaBearingDeg = qiblaDeg,
            kaabaDistanceKm = distKm
        )
    }

    fun getMonthlySchedule(year: Int, month: Int): List<DailyPrayerSchedule> {
        val p = prefs.value
        return PrayerTimeCalculator.calculateMonthlySchedule(
            year, month, p.city, p.calculationMethod, p.asrMadhhab
        )
    }

    // --- Quran & Tafsir & Audio ---
    fun openSurah(surahNumber: Int) {
        viewModelScope.launch {
            val detail = quranRepo.getSurahDetail(surahNumber)
            _currentSurahDetail.value = detail
            if (detail != null) {
                prefsRepo.setLastReadPosition(surahNumber, 1)
                db.recentItemDao().upsert(
                    RecentItemEntity(
                        id = "surah:$surahNumber",
                        type = "SURAH",
                        title = "${detail.summary.nameBengali} (${detail.summary.nameArabic})",
                        subtitle = "সূরা ${BengaliFormatter.formatNumber(surahNumber)} • ${detail.summary.meaningBengali}",
                        routeHint = "surah:$surahNumber"
                    )
                )
            }
            _tafsirError.value = null
            _tafsirLoading.value = true
            launch {
                quranRepo.observeSurahTafsir(surahNumber).collect { list ->
                    _currentSurahTafsir.value = list.associateBy { it.ayahNumber }
                }
            }
            val res = quranRepo.loadOrFetchTafsir(surahNumber)
            _tafsirLoading.value = false
            res.onFailure { err ->
                if (_currentSurahTafsir.value.isEmpty()) {
                    _tafsirError.value = "তাফসির লোড করা যায়নি (${err.localizedMessage ?: "অফলাইন"})। ইন্টারনেট সংযোগ চালু থাকলে তাফসির ডাউনলোড হয়ে সংরক্ষিত থাকবে।"
                }
            }
        }
    }

    fun updateLastReadAyah(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            prefsRepo.setLastReadPosition(surahNumber, ayahNumber)
        }
    }

    fun downloadSurahAudio(surahNumber: Int) {
        val reciterId = prefs.value.selectedReciterId
        if (_audioDownloadingSurahs.value.contains(surahNumber)) return
        viewModelScope.launch {
            _audioDownloadingSurahs.update { it + surahNumber }
            val result = quranRepo.downloadSurahAudio(reciterId, surahNumber)
            _audioDownloadingSurahs.update { it - surahNumber }
            result.onSuccess {
                _statusBannerMessage.value = "সূরা অডিও অফলাইনের জন্য ডাউনলোড সম্পন্ন হয়েছে।"
            }.onFailure { err ->
                _statusBannerMessage.value = "ডাউনলোড ব্যর্থ: ${err.localizedMessage ?: "ইন্টারনেট পরীক্ষা করুন"}"
            }
        }
    }

    fun deleteDownloadedAudio(entity: DownloadedAudioEntity) {
        viewModelScope.launch {
            quranRepo.deleteDownloadedAudio(entity)
        }
    }

    fun clearCachedDownloads() {
        viewModelScope.launch {
            quranRepo.clearAudioAndTafsirCache()
            _statusBannerMessage.value = "ক্যাশ ও ডাউনলোডকৃত অডিও পরিষ্কার করা হয়েছে।"
        }
    }

    // --- Bookmarks & Recents ---
    fun toggleBookmark(
        id: String,
        type: String,
        title: String,
        subtitle: String,
        arabicText: String = "",
        routeHint: String = ""
    ) {
        viewModelScope.launch {
            if (db.bookmarkDao().exists(id)) {
                db.bookmarkDao().deleteById(id)
            } else {
                db.bookmarkDao().insert(
                    BookmarkEntity(
                        id = id,
                        type = type,
                        title = title,
                        subtitle = subtitle,
                        arabicText = arabicText,
                        routeHint = routeHint
                    )
                )
            }
        }
    }

    fun recordRecent(id: String, type: String, title: String, subtitle: String, routeHint: String) {
        viewModelScope.launch {
            db.recentItemDao().upsert(
                RecentItemEntity(id = id, type = type, title = title, subtitle = subtitle, routeHint = routeHint)
            )
        }
    }

    // --- Tasbih ---
    fun incrementTasbih(session: TasbihSessionEntity) {
        viewModelScope.launch {
            val nextCount = session.currentCount + 1
            val completedSession = nextCount == session.targetCount
            db.tasbihDao().updateSession(
                session.copy(
                    currentCount = nextCount,
                    totalLifetimeCount = session.totalLifetimeCount + 1,
                    updatedAt = System.currentTimeMillis()
                )
            )
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val existing = db.tasbihDao().getHistoryForDate(dateKey)
            db.tasbihDao().upsertHistory(
                TasbihHistoryEntity(
                    dateKey = dateKey,
                    totalCount = (existing?.totalCount ?: 0L) + 1L,
                    sessionsCompleted = (existing?.sessionsCompleted ?: 0) + if (completedSession) 1 else 0
                )
            )
        }
    }

    fun resetTasbihSession(session: TasbihSessionEntity) {
        viewModelScope.launch {
            db.tasbihDao().updateSession(
                session.copy(currentCount = 0, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun updateTasbihTarget(session: TasbihSessionEntity, target: Int) {
        viewModelScope.launch {
            db.tasbihDao().updateSession(
                session.copy(targetCount = target.coerceIn(1, 100000), updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun addCustomTasbih(titleBn: String, arabic: String, target: Int) {
        if (titleBn.isBlank()) return
        viewModelScope.launch {
            db.tasbihDao().insertSession(
                TasbihSessionEntity(
                    titleBn = titleBn.trim(),
                    titleEn = titleBn.trim(),
                    arabicText = arabic.trim(),
                    pronunciationBn = titleBn.trim(),
                    meaningBn = "কাস্টম জিকির",
                    targetCount = target.coerceIn(1, 100000),
                    isDefault = false
                )
            )
        }
    }

    fun deleteCustomTasbih(id: Long) {
        viewModelScope.launch {
            db.tasbihDao().deleteCustomSession(id)
        }
    }

    // --- Fasting Tracker ---
    fun toggleFastingDay(dateKey: String, fasted: Boolean, type: String = "RAMADAN") {
        viewModelScope.launch {
            db.fastingDao().upsert(
                FastingDayEntity(dateKey = dateKey, isFasted = fasted, fastingType = type)
            )
        }
    }

    // --- Personal Notes ---
    fun saveNote(id: Long = 0, title: String, content: String, category: String = "সাধারণ") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            db.personalNoteDao().upsert(
                PersonalNoteEntity(
                    id = id,
                    title = title.ifBlank { "শিরোনামহীন নোট" },
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            db.personalNoteDao().deleteById(id)
        }
    }

    // --- Hadith Online Chapter Loader ---
    fun fetchBukhariSectionOnline(sectionNumber: Int, sectionNameBn: String) {
        viewModelScope.launch {
            _hadithSyncMessage.value = "হাদিস সংগ্রহ করা হচ্ছে..."
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val url = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ben-bukhari/sections/$sectionNumber.json"
                    val req = Request.Builder().url(url).header("User-Agent", "NoorLifeAndroid/1.0").build()
                    val bodyStr = httpClient.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) error("HTTP ${resp.code}")
                        resp.body?.string() ?: error("Empty")
                    }
                    val root = JSONObject(bodyStr)
                    val arr = root.getJSONArray("hadiths")
                    val entities = mutableListOf<CachedHadithEntity>()
                    for (i in 0 until minOf(arr.length(), 40)) {
                        val obj = arr.getJSONObject(i)
                        val num = obj.optInt("hadithnumber", i + 1)
                        val text = obj.optString("text", "").trim()
                        if (text.isNotBlank()) {
                            entities.add(
                                CachedHadithEntity(
                                    id = "bukhari_sec_${sectionNumber}_$num",
                                    bookId = "bukhari",
                                    bookNameBn = "সহীহ বুখারী",
                                    hadithNumber = num,
                                    categoryBn = sectionNameBn,
                                    text = text,
                                    referenceBn = "সহীহ বুখারী ($sectionNameBn), হাদিস নং: $num"
                                )
                            )
                        }
                    }
                    db.hadithCacheDao().insertAll(entities)
                    entities
                }
            }
            result.onSuccess { added ->
                val mapped = added.map {
                    HadithEntry(it.id, it.bookNameBn, it.hadithNumber.toString(), it.categoryBn, "", it.text, it.referenceBn)
                }
                _hadithList.update { prev -> (prev + mapped).distinctBy { it.id } }
                _hadithSyncMessage.value = "$sectionNameBn অধ্যায় থেকে ${BengaliFormatter.formatNumber(added.size)}টি সহীহ হাদিস সংরক্ষিত হয়েছে।"
            }.onFailure {
                _hadithSyncMessage.value = "অনলাইন থেকে হাদিস আনা যায়নি। অফলাইনে সংরক্ষিত হাদিসসমূহ পড়তে পারেন।"
            }
        }
    }

    // --- Quiz ---
    fun recordQuizAttempt(score: Int, total: Int, categoryBn: String) {
        viewModelScope.launch {
            db.quizAttemptDao().insert(
                QuizAttemptEntity(score = score, totalQuestions = total, categoryBn = categoryBn)
            )
        }
    }

    // --- Global Search ---
    fun performGlobalSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = GlobalSearchResults()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val q = query.trim().lowercase()
            val surahMatches = _surahSummaries.value.filter {
                it.nameBengali.lowercase().contains(q) ||
                    it.meaningBengali.lowercase().contains(q) ||
                    it.nameEnglish.lowercase().contains(q) ||
                    it.nameArabic.contains(q) ||
                    it.number.toString() == q
            }
            val ayahMatches = quranRepo.searchQuran(query, limit = 25)
            val duaMatches = DuasAndKalimasData.duas.filter {
                it.titleBn.lowercase().contains(q) ||
                    it.meaningBn.lowercase().contains(q) ||
                    it.categoryBn.lowercase().contains(q) ||
                    it.arabic.contains(q)
            }
            val hadithMatches = _hadithList.value.filter {
                it.bengaliText.lowercase().contains(q) ||
                    it.categoryBn.lowercase().contains(q) ||
                    it.referenceBn.lowercase().contains(q)
            }.take(20)
            val nameMatches = NamesOfAllahData.names.filter {
                it.pronunciationBn.lowercase().contains(q) ||
                    it.meaningBn.lowercase().contains(q) ||
                    it.arabic.contains(q)
            }
            val guideMatches = (IslamicKnowledgeData.knowledgeArticles +
                NamazAndWuduData.salahTopics.map {
                    KnowledgeArticle(it.id, "নামাজ শিক্ষা", it.titleBn, it.summaryBn, it.detailsBn)
                }).filter {
                it.titleBn.lowercase().contains(q) || it.bodyBn.lowercase().contains(q)
            }
            _searchResults.value = GlobalSearchResults(
                surahs = surahMatches,
                ayahs = ayahMatches,
                duas = duaMatches,
                hadiths = hadithMatches,
                allahNames = nameMatches,
                guides = guideMatches
            )
            _isSearching.value = false
        }
    }

    // --- Location & Settings Actions ---
    fun detectGpsCity(context: Context) {
        viewModelScope.launch {
            _locationDetecting.value = true
            val res = LocationHelper.detectCurrentCity(context)
            _locationDetecting.value = false
            res.onSuccess { detectedCity ->
                prefsRepo.setCity(detectedCity, isGps = true)
                _statusBannerMessage.value = "জিপিএস অবস্থান নির্ধারিত হয়েছে: ${detectedCity.bnName}"
            }.onFailure { err ->
                _statusBannerMessage.value = err.localizedMessage ?: "জিপিএস অবস্থান পাওয়া যায়নি।"
            }
        }
    }

    fun selectManualCity(city: CityLocation) {
        viewModelScope.launch {
            prefsRepo.setCity(city, isGps = false)
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(lang) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { prefsRepo.setUserName(name) }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch { prefsRepo.setCalculationMethod(method) }
    }

    fun setAsrMadhhab(madhhab: AsrMadhhab) {
        viewModelScope.launch { prefsRepo.setAsrMadhhab(madhhab) }
    }

    fun setHijriMoonOffset(offset: Int) {
        viewModelScope.launch { prefsRepo.setHijriMoonOffset(offset) }
    }

    fun setPrayerNotification(waqt: PrayerWaqt, enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setPrayerNotification(waqt, enabled) }
    }

    fun setSehriIftarNotification(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setSehriIftarNotification(enabled) }
    }

    fun setNotificationSound(sound: NotificationSoundType) {
        viewModelScope.launch { prefsRepo.setNotificationSound(sound) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setVibrationEnabled(enabled) }
    }

    fun setTasbihAudioHaptics(sound: Boolean, vibration: Boolean) {
        viewModelScope.launch { prefsRepo.setTasbihAudioHaptics(sound, vibration) }
    }

    fun setQuranReadingSettings(
        arabicSize: Float,
        bengaliSize: Float,
        showPronunciation: Boolean,
        showEnglish: Boolean
    ) {
        viewModelScope.launch {
            prefsRepo.setQuranReadingSettings(arabicSize, bengaliSize, showPronunciation, showEnglish)
        }
    }

    fun setSelectedReciter(reciterId: String) {
        viewModelScope.launch { prefsRepo.setSelectedReciter(reciterId) }
    }

    fun setZakatMetalRates(goldPerGram: Double, silverPerGram: Double) {
        viewModelScope.launch { prefsRepo.setZakatMetalRates(goldPerGram, silverPerGram) }
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }
}
