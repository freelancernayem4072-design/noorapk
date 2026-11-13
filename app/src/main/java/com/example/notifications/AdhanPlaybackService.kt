package com.example.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.NotificationSoundType
import com.example.domain.prayer.PrayerWaqt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Plays the real vocal Muazzin Adhan ("Allahu Akbar, Allahu Akbar...") cleanly
 * via Android's Alarm audio stream when any of the 5 daily prayer times arrives
 * or when the user tests Adhan playback, without triggering a notification chime sound.
 */
class AdhanPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var playbackJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var remainingFallbackCycles: Int = 2

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ADHAN -> {
                stopPlaybackAndSelf()
                return START_NOT_STICKY
            }
            else -> {
                val waqtId = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_WAQT_ID) ?: PrayerWaqt.FAJR.id
                val title = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_TITLE) ?: "আজান চলছে • নূর লাইফ"
                val body = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_BODY) ?: "নামাজের ওয়াক্ত শুরু হয়েছে।"
                val soundType = NotificationSoundType.fromId(
                    intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_TYPE)
                )

                PrayerNotificationScheduler.ensureChannels(this)
                val notif = buildAdhanNotification(title, body, waqtId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        FOREGROUND_NOTIF_ID,
                        notif,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(FOREGROUND_NOTIF_ID, notif)
                }

                startVocalAdhan(waqtId, soundType)
                return START_NOT_STICKY
            }
        }
    }

    private fun buildAdhanNotification(title: String, body: String, waqtId: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            this,
            4100 + waqtId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPi = PendingIntent.getService(
            this,
            4200,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PrayerNotificationScheduler.CHANNEL_ADHAN_LIVE)
            .setSmallIcon(R.drawable.ic_noor_emblem)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(longArrayOf(0L))
            .setContentIntent(contentPi)
            .addAction(0, "আজান বন্ধ করুন", stopPi)
            .build()
    }

    private fun startVocalAdhan(waqtId: String, soundType: NotificationSoundType) {
        playbackJob?.cancel()
        releasePlayerOnly()
        _isPlaying.value = true
        _activeWaqtId.value = waqtId

        playbackJob = serviceScope.launch {
            val cached = AdhanAudioRepository.getCachedAdhanFile(this@AdhanPlaybackService)
                ?: AdhanAudioRepository.ensureAdhanCached(this@AdhanPlaybackService)

            if (!_isPlaying.value) return@launch

            if (cached != null && playMp3File(cached)) {
                return@launch
            }

            // If network stream works without file cache, try streaming selected vocal Adhan URL
            val voiceUrl = AdhanAudioRepository.getSelectedVoice(this@AdhanPlaybackService).url
            if (playRemoteUrl(voiceUrl)) {
                return@launch
            }

            // Pure offline emergency fallback if user never connected to internet since install
            playRawFallback(waqtId, soundType)
        }
    }

    private fun alarmAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private fun playMp3File(file: File): Boolean {
        return runCatching {
            requestAudioFocus()
            val mp = MediaPlayer().apply {
                setAudioAttributes(alarmAudioAttributes())
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    stopPlaybackAndSelf()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlaybackAndSelf()
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = mp
            true
        }.getOrElse {
            releasePlayerOnly()
            false
        }
    }

    private fun playRemoteUrl(url: String): Boolean {
        return runCatching {
            requestAudioFocus()
            val mp = MediaPlayer().apply {
                setAudioAttributes(alarmAudioAttributes())
                setDataSource(url)
                setOnPreparedListener { player ->
                    if (_isPlaying.value) {
                        player.start()
                    } else {
                        stopPlaybackAndSelf()
                    }
                }
                setOnCompletionListener {
                    stopPlaybackAndSelf()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlaybackAndSelf()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
            true
        }.getOrElse {
            releasePlayerOnly()
            false
        }
    }

    private fun playRawFallback(waqtId: String, soundType: NotificationSoundType) {
        val rawRes = if (waqtId == PrayerWaqt.FAJR.id || waqtId == "sehri" || soundType == NotificationSoundType.FAJR_CHIME) {
            R.raw.fajr_chime
        } else {
            R.raw.adhan_chime
        }
        requestAudioFocus()
        remainingFallbackCycles = 2
        runCatching {
            val mp = MediaPlayer.create(
                this,
                rawRes,
                alarmAudioAttributes(),
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ) ?: error("MediaPlayer creation returned null")
            mp.setOnCompletionListener { player ->
                remainingFallbackCycles--
                if (remainingFallbackCycles > 0 && _isPlaying.value) {
                    runCatching {
                        player.seekTo(0)
                        player.start()
                    }.onFailure { stopPlaybackAndSelf() }
                } else {
                    stopPlaybackAndSelf()
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                stopPlaybackAndSelf()
                true
            }
            mp.start()
            mediaPlayer = mp
        }.onFailure {
            stopPlaybackAndSelf()
        }
    }

    private fun requestAudioFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(alarmAudioAttributes())
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun releasePlayerOnly() {
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        }
        audioFocusRequest = null
    }

    private fun stopPlaybackAndSelf() {
        playbackJob?.cancel()
        releasePlayerOnly()
        _isPlaying.value = false
        _activeWaqtId.value = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(FOREGROUND_NOTIF_ID)
        stopSelf()
    }

    override fun onDestroy() {
        playbackJob?.cancel()
        serviceScope.cancel()
        releasePlayerOnly()
        _isPlaying.value = false
        _activeWaqtId.value = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY_ADHAN = "com.example.notifications.ACTION_PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.notifications.ACTION_STOP_ADHAN"
        const val FOREGROUND_NOTIF_ID = 4401

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

        private val _activeWaqtId = MutableStateFlow<String?>(null)
        val activeWaqtId: StateFlow<String?> = _activeWaqtId.asStateFlow()

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            runCatching { context.startService(intent) }
        }
    }
}
