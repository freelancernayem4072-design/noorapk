package com.example.notifications

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class AdhanVoiceOption(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val url: String
)

object AdhanAudioRepository {

    val voices = listOf(
        AdhanVoiceOption(
            id = "a9",
            nameBn = "মিশারি রশিদ আল-আফাসি (আজান)",
            nameEn = "Mishary Rashid Alafasy",
            url = "https://cdn.aladhan.com/audio/adhans/a9.mp3"
        ),
        AdhanVoiceOption(
            id = "a1",
            nameBn = "আহমদ আল-নাফীস (আজান)",
            nameEn = "Ahmad Al-Nafees",
            url = "https://cdn.aladhan.com/audio/adhans/a1.mp3"
        ),
        AdhanVoiceOption(
            id = "a2",
            nameBn = "হাফেজ মুস্তাফা ওজকান (আজান)",
            nameEn = "Hafiz Mustafa Ozcan",
            url = "https://cdn.aladhan.com/audio/adhans/a2.mp3"
        ),
        AdhanVoiceOption(
            id = "a11",
            nameBn = "মানসুর আল-জাহরানি (আজান)",
            nameEn = "Mansour Al-Zahrani",
            url = "https://cdn.aladhan.com/audio/adhans/a11-mansour-al-zahrani.mp3"
        ),
        AdhanVoiceOption(
            id = "a4",
            nameBn = "ঐতিহ্যবাহী আজান",
            nameEn = "Traditional Adhan",
            url = "https://cdn.aladhan.com/audio/adhans/a4.mp3"
        )
    )

    private const val PREF_FILE = "adhan_voice_prefs"
    private const val KEY_SELECTED_VOICE = "selected_voice_id"
    private const val MIN_VALID_MP3_BYTES = 16_384L

    private val downloadMutex = Mutex()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _isVocalAdhanCached = MutableStateFlow(false)
    val isVocalAdhanCached: StateFlow<Boolean> = _isVocalAdhanCached.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun getSelectedVoice(context: Context): AdhanVoiceOption {
        val id = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_VOICE, voices.first().id)
        return voices.firstOrNull { it.id == id } ?: voices.first()
    }

    fun setSelectedVoiceId(context: Context, voiceId: String) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_VOICE, voiceId)
            .apply()
        refreshCacheState(context)
    }

    private fun cacheDir(context: Context): File =
        File(context.filesDir, "adhan_cache").apply { mkdirs() }

    private fun fileForVoice(context: Context, voiceId: String): File =
        File(cacheDir(context), "adhan_$voiceId.mp3")

    fun getCachedAdhanFile(context: Context): File? {
        val selected = fileForVoice(context, getSelectedVoice(context).id)
        if (selected.exists() && selected.length() >= MIN_VALID_MP3_BYTES) {
            return selected
        }
        // Fallback to any already cached vocal Adhan MP3
        return cacheDir(context).listFiles()
            ?.firstOrNull { it.name.endsWith(".mp3") && it.length() >= MIN_VALID_MP3_BYTES }
    }

    fun refreshCacheState(context: Context): Boolean {
        val ready = getCachedAdhanFile(context) != null
        _isVocalAdhanCached.value = ready
        return ready
    }

    suspend fun ensureAdhanCached(context: Context, forceSelectedVoice: Boolean = false): File? =
        withContext(Dispatchers.IO) {
            downloadMutex.withLock {
                val selected = getSelectedVoice(context)
                val targetFile = fileForVoice(context, selected.id)
                if (targetFile.exists() && targetFile.length() >= MIN_VALID_MP3_BYTES) {
                    _isVocalAdhanCached.value = true
                    return@withLock targetFile
                }
                if (!forceSelectedVoice) {
                    getCachedAdhanFile(context)?.let { existing ->
                        _isVocalAdhanCached.value = true
                        return@withLock existing
                    }
                }

                _isDownloading.value = true
                try {
                    val candidates = listOf(selected) + voices.filter { it.id != selected.id }
                    for (candidate in candidates) {
                        val outFile = fileForVoice(context, candidate.id)
                        val tmpFile = File(cacheDir(context), "adhan_${candidate.id}.tmp")
                        val downloaded = runCatching {
                            val req = Request.Builder()
                                .url(candidate.url)
                                .header("User-Agent", "NoorLifeAndroid/1.0")
                                .build()
                            httpClient.newCall(req).execute().use { resp ->
                                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                                val contentType = resp.header("Content-Type")?.lowercase().orEmpty()
                                if (contentType.contains("text/html")) error("Unexpected HTML response")
                                val body = resp.body ?: error("Empty body")
                                body.byteStream().use { input ->
                                    FileOutputStream(tmpFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                            if (tmpFile.length() < MIN_VALID_MP3_BYTES) {
                                error("File too small (${tmpFile.length()} bytes)")
                            }
                            if (outFile.exists()) outFile.delete()
                            if (!tmpFile.renameTo(outFile)) {
                                tmpFile.copyTo(outFile, overwrite = true)
                                tmpFile.delete()
                            }
                            outFile
                        }.getOrNull()

                        if (downloaded != null && downloaded.exists() && downloaded.length() >= MIN_VALID_MP3_BYTES) {
                            _isVocalAdhanCached.value = true
                            return@withLock downloaded
                        } else {
                            runCatching { tmpFile.delete() }
                        }
                    }
                    refreshCacheState(context)
                    getCachedAdhanFile(context)
                } finally {
                    _isDownloading.value = false
                }
            }
        }
}
