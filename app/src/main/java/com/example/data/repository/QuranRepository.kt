package com.example.data.repository

import android.content.Context
import com.example.data.content.QuranAyah
import com.example.data.content.QuranCatalogMetadata
import com.example.data.content.RawSurahJson
import com.example.data.content.RawTafsirItemJson
import com.example.data.content.SurahDetail
import com.example.data.content.SurahSummary
import com.example.data.content.TafsirEntry
import com.example.data.local.AudioDownloadDao
import com.example.data.local.CachedTafsirEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.TafsirCacheDao
import com.example.data.remote.ArabicBengaliTransliterator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

class QuranRepository(
    private val context: Context,
    private val tafsirCacheDao: TafsirCacheDao,
    private val audioDownloadDao: AudioDownloadDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val loadMutex = Mutex()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var cachedRawSurahs: List<RawSurahJson>? = null

    @Volatile
    private var starterTafsirMap: Map<String, List<RawTafsirItemJson>>? = null

    suspend fun getAllSurahSummaries(): List<SurahSummary> {
        val raw = ensureQuranLoaded()
        return raw.map { s ->
            val bnPair = QuranCatalogMetadata.surahBnMap[s.num] ?: (s.nameEn to s.meaningEn)
            val revBn = if (s.rev.equals("Meccan", ignoreCase = true)) "মক্কি" else "মাদানি"
            SurahSummary(
                number = s.num,
                nameArabic = s.nameAr,
                nameBengali = bnPair.first,
                meaningBengali = bnPair.second,
                nameEnglish = s.nameEn,
                revelationType = s.rev,
                revelationTypeBn = revBn,
                ayahCount = s.ayahs.size,
                juzStart = s.ayahs.firstOrNull()?.j ?: 1
            )
        }
    }

    suspend fun getSurahDetail(surahNumber: Int): SurahDetail? {
        val raw = ensureQuranLoaded()
        val s = raw.firstOrNull { it.num == surahNumber } ?: return null
        val bnPair = QuranCatalogMetadata.surahBnMap[s.num] ?: (s.nameEn to s.meaningEn)
        val revBn = if (s.rev.equals("Meccan", ignoreCase = true)) "মক্কি" else "মাদানি"
        val summary = SurahSummary(
            number = s.num,
            nameArabic = s.nameAr,
            nameBengali = bnPair.first,
            meaningBengali = bnPair.second,
            nameEnglish = s.nameEn,
            revelationType = s.rev,
            revelationTypeBn = revBn,
            ayahCount = s.ayahs.size,
            juzStart = s.ayahs.firstOrNull()?.j ?: 1
        )
        val ayahs = s.ayahs.map { a ->
            QuranAyah(
                numberInSurah = a.n,
                globalNumber = a.g,
                juz = a.j,
                page = a.p,
                arabicText = a.ar,
                bengaliTranslation = a.bn,
                englishTranslation = a.en,
                latinTransliteration = a.tr,
                bengaliPronunciation = ArabicBengaliTransliterator.toBengaliPronunciation(a.tr)
            )
        }
        return SurahDetail(summary = summary, ayahs = ayahs)
    }

    suspend fun searchQuran(query: String, limit: Int = 50): List<Pair<SurahSummary, QuranAyah>> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        val raw = ensureQuranLoaded()
        val results = mutableListOf<Pair<SurahSummary, QuranAyah>>()
        for (s in raw) {
            val bnPair = QuranCatalogMetadata.surahBnMap[s.num] ?: (s.nameEn to s.meaningEn)
            val revBn = if (s.rev.equals("Meccan", ignoreCase = true)) "মক্কি" else "মাদানি"
            val summary = SurahSummary(
                number = s.num,
                nameArabic = s.nameAr,
                nameBengali = bnPair.first,
                meaningBengali = bnPair.second,
                nameEnglish = s.nameEn,
                revelationType = s.rev,
                revelationTypeBn = revBn,
                ayahCount = s.ayahs.size,
                juzStart = s.ayahs.firstOrNull()?.j ?: 1
            )
            for (a in s.ayahs) {
                if (a.bn.lowercase().contains(q) ||
                    a.en.lowercase().contains(q) ||
                    a.ar.contains(q) ||
                    a.tr.lowercase().contains(q)
                ) {
                    results.add(
                        summary to QuranAyah(
                            numberInSurah = a.n,
                            globalNumber = a.g,
                            juz = a.j,
                            page = a.p,
                            arabicText = a.ar,
                            bengaliTranslation = a.bn,
                            englishTranslation = a.en,
                            latinTransliteration = a.tr,
                            bengaliPronunciation = ArabicBengaliTransliterator.toBengaliPronunciation(a.tr)
                        )
                    )
                    if (results.size >= limit) return results
                }
            }
        }
        return results
    }

    suspend fun getAyahOfTheDay(dayOfYear: Int): Pair<SurahSummary, QuranAyah>? {
        val highlights = listOf(
            1 to 1, 2 to 152, 2 to 186, 2 to 255, 2 to 286,
            3 to 139, 3 to 159, 13 to 28, 14 to 7, 16 to 97,
            24 to 35, 29 to 69, 39 to 53, 55 to 13, 65 to 3,
            94 to 5, 94 to 6, 103 to 1, 112 to 1
        )
        val pick = highlights[dayOfYear % highlights.size]
        val detail = getSurahDetail(pick.first) ?: return null
        val ayah = detail.ayahs.firstOrNull { it.numberInSurah == pick.second } ?: detail.ayahs.firstOrNull() ?: return null
        return detail.summary to ayah
    }

    fun observeSurahTafsir(surahNumber: Int): Flow<List<CachedTafsirEntity>> =
        tafsirCacheDao.observeBySurah(surahNumber)

    suspend fun loadOrFetchTafsir(surahNumber: Int): Result<List<TafsirEntry>> = withContext(Dispatchers.IO) {
        // 1. Check Room cache first
        val cachedCount = tafsirCacheDao.countForSurah(surahNumber)
        if (cachedCount > 0) {
            return@withContext Result.success(emptyList()) // Observed via Flow
        }

        // 2. Check bundled starter Tafsir asset
        val starter = ensureStarterTafsirLoaded()[surahNumber.toString()]
        if (!starter.isNullOrEmpty()) {
            val entities = starter.map {
                CachedTafsirEntity(
                    id = "$surahNumber:${it.a}",
                    surahNumber = surahNumber,
                    ayahNumber = it.a,
                    sourceName = "তাফসির ইবনে কাসীর (তাওহীদ পাবলিকেশন)",
                    tafsirText = cleanHtmlTags(it.t)
                )
            }
            tafsirCacheDao.insertAll(entities)
            return@withContext Result.success(
                entities.map { TafsirEntry(it.surahNumber, it.ayahNumber, it.sourceName, it.tafsirText) }
            )
        }

        // 3. Fetch verified Bengali Tafsir from spa5k/tafsir_api CDN
        return@withContext runCatching {
            val url = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/bn-tafseer-ibn-e-kaseer/$surahNumber.json"
            val req = Request.Builder().url(url).header("User-Agent", "NoorLifeAndroid/1.0").build()
            val bodyStr = httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                resp.body?.string() ?: error("Empty response")
            }
            val parsed = parseTafsirJson(surahNumber, bodyStr, "তাফসির ইবনে কাসীর (তাওহীদ পাবলিকেশন)")
            if (parsed.isNotEmpty()) {
                tafsirCacheDao.insertAll(parsed)
            }
            parsed.map { TafsirEntry(it.surahNumber, it.ayahNumber, it.sourceName, it.tafsirText) }
        }
    }

    private fun parseTafsirJson(surahNumber: Int, rawJson: String, sourceName: String): List<CachedTafsirEntity> {
        val trimmed = rawJson.trim()
        val array: JSONArray = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("ayahs") ?: JSONArray()
        }
        val list = mutableListOf<CachedTafsirEntity>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val ayah = obj.optInt("ayah", i + 1)
            val text = cleanHtmlTags(obj.optString("text", ""))
            if (text.isNotBlank()) {
                list.add(
                    CachedTafsirEntity(
                        id = "$surahNumber:$ayah",
                        surahNumber = surahNumber,
                        ayahNumber = ayah,
                        sourceName = sourceName,
                        tafsirText = text
                    )
                )
            }
        }
        return list
    }

    private fun cleanHtmlTags(raw: String): String {
        return raw.replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }

    fun observeDownloadedAudio(): Flow<List<DownloadedAudioEntity>> = audioDownloadDao.observeAll()

    suspend fun getLocalAudioPathIfDownloaded(reciterId: String, surahNumber: Int): String? {
        val entity = audioDownloadDao.find(reciterId, surahNumber) ?: return null
        val file = File(entity.localPath)
        return if (file.exists() && file.length() > 1024L) file.absolutePath else null
    }

    suspend fun downloadSurahAudio(reciterId: String, surahNumber: Int): Result<DownloadedAudioEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "quran_audio/$reciterId").apply { mkdirs() }
                val targetFile = File(dir, "surah_$surahNumber.mp3")
                val url = QuranCatalogMetadata.audioSurahUrl(reciterId, surahNumber)
                val req = Request.Builder().url(url).header("User-Agent", "NoorLifeAndroid/1.0").build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("ডাউনলোড ব্যর্থ হয়েছে (HTTP ${resp.code})")
                    val body = resp.body ?: error("অডিও ফাইল পাওয়া যায়নি")
                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                val entity = DownloadedAudioEntity(
                    id = "$reciterId:$surahNumber",
                    reciterId = reciterId,
                    surahNumber = surahNumber,
                    localPath = targetFile.absolutePath,
                    sizeBytes = targetFile.length()
                )
                audioDownloadDao.upsert(entity)
                entity
            }
        }

    suspend fun deleteDownloadedAudio(entity: DownloadedAudioEntity) = withContext(Dispatchers.IO) {
        runCatching { File(entity.localPath).delete() }
        audioDownloadDao.deleteById(entity.id)
    }

    suspend fun clearAudioAndTafsirCache() = withContext(Dispatchers.IO) {
        val audioDir = File(context.filesDir, "quran_audio")
        if (audioDir.exists()) audioDir.deleteRecursively()
        audioDownloadDao.clearAll()
        tafsirCacheDao.clearAll()
    }

    private suspend fun ensureQuranLoaded(): List<RawSurahJson> {
        cachedRawSurahs?.let { return it }
        return loadMutex.withLock {
            cachedRawSurahs?.let { return it }
            withContext(Dispatchers.IO) {
                val text = context.assets.open("quran_complete.bin").use { stream ->
                    GZIPInputStream(stream).bufferedReader(Charsets.UTF_8).readText()
                }
                val parsed = json.decodeFromString<List<RawSurahJson>>(text)
                cachedRawSurahs = parsed
                parsed
            }
        }
    }

    private suspend fun ensureStarterTafsirLoaded(): Map<String, List<RawTafsirItemJson>> {
        starterTafsirMap?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val text = context.assets.open("tafsir_starter.bin").use { stream ->
                    GZIPInputStream(stream).bufferedReader(Charsets.UTF_8).readText()
                }
                json.decodeFromString<Map<String, List<RawTafsirItemJson>>>(text)
            }.getOrDefault(emptyMap()).also { starterTafsirMap = it }
        }
    }
}
