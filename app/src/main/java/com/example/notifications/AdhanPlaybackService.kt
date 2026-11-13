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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.NotificationSoundType
import com.example.domain.prayer.PrayerWaqt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Plays the Adhan alert cleanly in the foreground when a scheduled prayer alarm fires
 * (or when user tests Adhan), providing an immediate "Stop Adhan" action.
 */
class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var remainingCycles: Int = 2

    private var lastTitle: String = "নামাজের সময় • নূর লাইফ"
    private var lastBody: String = "নামাজের ওয়াক্ত শুরু হয়েছে।"
    private var lastWaqtId: String = PrayerWaqt.FAJR.id

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ADHAN -> {
                stopPlaybackAndSelf(keepVisualReminder = false)
                return START_NOT_STICKY
            }
            else -> {
                val waqtId = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_WAQT_ID) ?: PrayerWaqt.FAJR.id
                val title = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_TITLE) ?: "নামাজের সময় • নূর লাইফ"
                val body = intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_BODY) ?: "নামাজের ওয়াক্ত শুরু হয়েছে।"
                val soundType = NotificationSoundType.fromId(
                    intent?.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_TYPE)
                )
                val vibrate = intent?.getBooleanExtra(PrayerNotificationScheduler.EXTRA_VIBRATE, true) ?: true

                lastWaqtId = waqtId
                lastTitle = title
                lastBody = body

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

                startAdhanAudio(waqtId, soundType, vibrate)
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPi)
            .addAction(0, "আজান বন্ধ করুন", stopPi)
            .build()
    }

    private fun startAdhanAudio(waqtId: String, soundType: NotificationSoundType, vibrate: Boolean) {
        releasePlayerOnly()
        _isPlaying.value = true
        _activeWaqtId.value = waqtId

        if (vibrate) {
            triggerShortVibrate()
        }

        val rawRes = if (waqtId == PrayerWaqt.FAJR.id || waqtId == "sehri" || soundType == NotificationSoundType.FAJR_CHIME) {
            R.raw.fajr_chime
        } else {
            R.raw.adhan_chime
        }

        requestAudioFocus()
        remainingCycles = 2

        runCatching {
            val mp = MediaPlayer.create(
                this,
                rawRes,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ) ?: error("MediaPlayer creation returned null")

            mp.setOnCompletionListener { player ->
                remainingCycles--
                if (remainingCycles > 0) {
                    runCatching {
                        player.seekTo(0)
                        player.start()
                    }.onFailure {
                        stopPlaybackAndSelf(keepVisualReminder = true)
                    }
                } else {
                    stopPlaybackAndSelf(keepVisualReminder = true)
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                stopPlaybackAndSelf(keepVisualReminder = true)
                true
            }
            mp.start()
            mediaPlayer = mp
        }.onFailure {
            stopPlaybackAndSelf(keepVisualReminder = true)
        }
    }

    private fun triggerShortVibrate() {
        runCatching {
            val pattern = longArrayOf(0L, 280L, 180L, 280L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, -1)
                }
            }
        }
    }

    private fun requestAudioFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
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
            mediaPlayer?.release()
        }
        mediaPlayer = null
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        }
        audioFocusRequest = null
    }

    private fun stopPlaybackAndSelf(keepVisualReminder: Boolean = false) {
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
        if (keepVisualReminder) {
            PrayerNotificationScheduler.showNotification(
                context = this,
                notificationId = 3000 + lastWaqtId.hashCode(),
                waqtId = lastWaqtId,
                title = lastTitle,
                body = lastBody,
                soundType = NotificationSoundType.SILENT,
                vibrate = false
            )
        }
        stopSelf()
    }

    override fun onDestroy() {
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
