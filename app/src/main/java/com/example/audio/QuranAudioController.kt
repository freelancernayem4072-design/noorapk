package com.example.audio

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.content.QuranCatalogMetadata
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AudioRepeatMode(val bnLabel: String, val enLabel: String) {
    OFF("পুনরাবৃত্তি বন্ধ", "Repeat Off"),
    REPEAT_ONE("সূরা/আয়াত পুনরাবৃত্তি", "Repeat Current")
}

data class QuranPlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSurahNumber: Int? = null,
    val currentSurahTitle: String = "",
    val currentAyahNumber: Int? = null,
    val reciterId: String = "ar.alafasy",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: AudioRepeatMode = AudioRepeatMode.OFF,
    val isOfflinePlayback: Boolean = false,
    val errorMessage: String? = null
)

class QuranAudioController(
    private val context: Context,
    private val quranRepository: QuranRepository,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(QuranPlaybackState())
    val state: StateFlow<QuranPlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private fun ensureController(onReady: (MediaController) -> Unit) {
        controller?.let {
            onReady(it)
            return
        }
        val sessionToken = SessionToken(context, ComponentName(context, QuranAudioService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            runCatching {
                val mc = future.get()
                controller = mc
                attachListener(mc)
                onReady(mc)
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isBuffering = false,
                        errorMessage = "অডিও প্লেয়ার চালু করা যায়নি: ${err.localizedMessage ?: ""}"
                    )
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun attachListener(mc: MediaController) {
        mc.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressLoop() else progressJob?.cancel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = mc.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "তিলাওয়াত লোড করা যায়নি। অনুগ্রহ করে ইন্টারনেট সংযোগ পরীক্ষা করুন অথবা সূরাটি অফলাইনে ডাউনলোড করুন।"
                    )
                }
            }
        })
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (true) {
                val mc = controller ?: break
                _state.update {
                    it.copy(
                        positionMs = mc.currentPosition.coerceAtLeast(0L),
                        durationMs = mc.duration.coerceAtLeast(0L)
                    )
                }
                delay(500L)
            }
        }
    }

    fun playSurah(surahNumber: Int, surahNameBn: String, reciterId: String) {
        scope.launch {
            val localPath = quranRepository.getLocalAudioPathIfDownloaded(reciterId, surahNumber)
            val uri = if (localPath != null) {
                Uri.fromFile(java.io.File(localPath))
            } else {
                Uri.parse(QuranCatalogMetadata.audioSurahUrl(reciterId, surahNumber))
            }

            _state.update {
                it.copy(
                    currentSurahNumber = surahNumber,
                    currentSurahTitle = surahNameBn,
                    currentAyahNumber = null,
                    reciterId = reciterId,
                    isOfflinePlayback = localPath != null,
                    isBuffering = localPath == null,
                    errorMessage = null
                )
            }

            ensureController { mc ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(surahNameBn)
                    .setArtist(QuranCatalogMetadata.reciters.firstOrNull { r -> r.id == reciterId }?.nameBn ?: "নূর লাইফ")
                    .build()
                val item = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(metadata)
                    .build()
                mc.setMediaItem(item)
                mc.repeatMode = if (_state.value.repeatMode == AudioRepeatMode.REPEAT_ONE) {
                    Player.REPEAT_MODE_ONE
                } else {
                    Player.REPEAT_MODE_OFF
                }
                mc.prepare()
                mc.play()
            }
        }
    }

    fun playAyah(surahNumber: Int, surahNameBn: String, ayahNumberInSurah: Int, globalAyahNumber: Int, reciterId: String) {
        val uri = Uri.parse(QuranCatalogMetadata.audioAyahUrl(reciterId, globalAyahNumber))
        _state.update {
            it.copy(
                currentSurahNumber = surahNumber,
                currentSurahTitle = "$surahNameBn — আয়াত $ayahNumberInSurah",
                currentAyahNumber = ayahNumberInSurah,
                reciterId = reciterId,
                isOfflinePlayback = false,
                isBuffering = true,
                errorMessage = null
            )
        }
        ensureController { mc ->
            val metadata = MediaMetadata.Builder()
                .setTitle("$surahNameBn • আয়াত $ayahNumberInSurah")
                .setArtist(QuranCatalogMetadata.reciters.firstOrNull { r -> r.id == reciterId }?.nameBn ?: "নূর লাইফ")
                .build()
            val item = MediaItem.Builder().setUri(uri).setMediaMetadata(metadata).build()
            mc.setMediaItem(item)
            mc.prepare()
            mc.play()
        }
    }

    fun togglePlayPause() {
        val mc = controller ?: return
        if (mc.isPlaying) mc.pause() else mc.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun toggleRepeat() {
        val next = if (_state.value.repeatMode == AudioRepeatMode.OFF) {
            AudioRepeatMode.REPEAT_ONE
        } else {
            AudioRepeatMode.OFF
        }
        _state.update { it.copy(repeatMode = next) }
        controller?.repeatMode = if (next == AudioRepeatMode.REPEAT_ONE) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    fun playNextSurah(reciterId: String) {
        val current = _state.value.currentSurahNumber ?: return
        val next = if (current >= 114) 1 else current + 1
        val name = QuranCatalogMetadata.surahBnMap[next]?.first ?: "সূরা $next"
        playSurah(next, name, reciterId)
    }

    fun playPreviousSurah(reciterId: String) {
        val current = _state.value.currentSurahNumber ?: return
        val prev = if (current <= 1) 114 else current - 1
        val name = QuranCatalogMetadata.surahBnMap[prev]?.first ?: "সূরা $prev"
        playSurah(prev, name, reciterId)
    }

    fun stop() {
        controller?.stop()
        progressJob?.cancel()
        _state.update { QuranPlaybackState(reciterId = it.reciterId, repeatMode = it.repeatMode) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
