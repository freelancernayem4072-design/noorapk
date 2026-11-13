package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.NotificationSoundType
import com.example.data.local.UserPreferences
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerWaqt
import com.example.localization.BengaliFormatter
import java.util.Calendar
import java.util.TimeZone

object PrayerNotificationScheduler {

    const val EXTRA_WAQT_ID = "extra_waqt_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_SOUND_TYPE = "extra_sound_type"
    const val EXTRA_VIBRATE = "extra_vibrate"

    const val CHANNEL_ADHAN_LIVE = "noor_prayer_adhan_live_v1"
    private const val CHANNEL_ADHAN = "noor_prayer_adhan_v1"
    private const val CHANNEL_FAJR = "noor_prayer_fajr_v1"
    private const val CHANNEL_DEFAULT = "noor_prayer_default_v1"
    private const val CHANNEL_SILENT = "noor_prayer_silent_v1"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val adhanUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.adhan_chime}")
        val fajrUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.fajr_chime}")

        val chAdhanLive = NotificationChannel(
            CHANNEL_ADHAN_LIVE,
            "আজান প্লেয়ার অ্যালার্ট",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "আজানের সময় সরাসরি আজান ধ্বনি ও নিয়ন্ত্রণ দেখায়"
            setSound(null, null)
            enableVibration(false)
        }

        val chAdhan = NotificationChannel(
            CHANNEL_ADHAN,
            "নামাজের সময় (আজান ধ্বনি)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_prayer_desc)
            setSound(adhanUri, audioAttrs)
            enableVibration(true)
        }

        val chFajr = NotificationChannel(
            CHANNEL_FAJR,
            "ফজরের আজান ও সাহরি",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "ফজর ও সাহরির সময় অনুস্মারক"
            setSound(fajrUri, audioAttrs)
            enableVibration(true)
        }

        val chDefault = NotificationChannel(
            CHANNEL_DEFAULT,
            "নামাজের সময় (ডিফল্ট সাউন্ড)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_prayer_desc)
            enableVibration(true)
        }

        val chSilent = NotificationChannel(
            CHANNEL_SILENT,
            "নামাজের সময় (নীরব)",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "শব্দহীন নামাজের সময় নোটিফিকেশন"
            setSound(null, null)
            enableVibration(false)
        }

        nm.createNotificationChannels(listOf(chAdhanLive, chAdhan, chFajr, chDefault, chSilent))
    }

    fun channelFor(soundType: NotificationSoundType, waqtId: String): String = when (soundType) {
        NotificationSoundType.ADHAN_CHIME -> if (waqtId == PrayerWaqt.FAJR.id || waqtId == "sehri") CHANNEL_FAJR else CHANNEL_ADHAN
        NotificationSoundType.FAJR_CHIME -> CHANNEL_FAJR
        NotificationSoundType.SYSTEM_DEFAULT -> CHANNEL_DEFAULT
        NotificationSoundType.SILENT -> CHANNEL_SILENT
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun scheduleUpcomingPrayers(context: Context, prefs: UserPreferences) {
        ensureChannels(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        val cityTz = TimeZone.getTimeZone(prefs.city.timeZoneId)

        val cal = Calendar.getInstance(cityTz)
        val todaySchedule = PrayerTimeCalculator.calculateDailySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            prefs.city,
            prefs.calculationMethod,
            prefs.asrMadhhab
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrowSchedule = PrayerTimeCalculator.calculateDailySchedule(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            prefs.city,
            prefs.calculationMethod,
            prefs.asrMadhhab
        )

        // Schedule next slot for every enabled waqt independently (today if future, else tomorrow)
        cancelAllPrayerAlarms(context, am)
        for (waqt in PrayerWaqt.entries) {
            if (!prefs.isNotificationEnabled(waqt)) continue
            val todaySlot = todaySchedule.allSlots.first { it.waqt == waqt }
            val tomorrowSlot = tomorrowSchedule.allSlots.first { it.waqt == waqt }
            val chosen = if (todaySlot.epochMillis > now + 2_000L) todaySlot else tomorrowSlot
            val timeLabel = BengaliFormatter.formatTime12Hour(chosen.hour24, chosen.minute, prefs.language)
            val body = if (waqt == PrayerWaqt.SUNRISE) {
                "${todaySchedule.city.bnName}: সূর্যোদয় হয়েছে ($timeLabel)।"
            } else {
                "${todaySchedule.city.bnName}: ${waqt.bnName} নামাজের সময় হয়েছে ($timeLabel)।"
            }
            scheduleSingleAlarm(
                context = context,
                am = am,
                requestCode = 2000 + waqt.ordinal,
                triggerAtMillis = chosen.epochMillis,
                waqtId = waqt.id,
                title = "${waqt.bnName} ওয়াক্ত • নূর লাইফ",
                body = body,
                prefs = prefs
            )
        }

        if (prefs.notifySehriIftar) {
            val sehriTarget = if (todaySchedule.sehriEndMillis > now + 2_000L) {
                todaySchedule.sehriEndMillis to BengaliFormatter.formatTime12Hour(todaySchedule.sehriHour24, todaySchedule.sehriMinute, prefs.language)
            } else {
                tomorrowSchedule.sehriEndMillis to BengaliFormatter.formatTime12Hour(tomorrowSchedule.sehriHour24, tomorrowSchedule.sehriMinute, prefs.language)
            }
            scheduleSingleAlarm(
                context = context,
                am = am,
                requestCode = 2090,
                triggerAtMillis = sehriTarget.first,
                waqtId = "sehri",
                title = "সাহরির শেষ সময় • নূর লাইফ",
                body = "${todaySchedule.city.bnName}: সাহরির শেষ সময় (${sehriTarget.second})।",
                prefs = prefs
            )
        }
    }

    private fun cancelAllPrayerAlarms(context: Context, am: AlarmManager) {
        for (waqt in PrayerWaqt.entries) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                2000 + waqt.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
        val sehriIntent = Intent(context, PrayerAlarmReceiver::class.java)
        val sehriPi = PendingIntent.getBroadcast(
            context,
            2090,
            sehriIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(sehriPi)
    }

    private fun scheduleSingleAlarm(
        context: Context,
        am: AlarmManager,
        requestCode: Int,
        triggerAtMillis: Long,
        waqtId: String,
        title: String,
        body: String,
        prefs: UserPreferences
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_WAQT_ID, waqtId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_SOUND_TYPE, prefs.notificationSound.id)
            putExtra(EXTRA_VIBRATE, prefs.vibrationEnabled)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = PendingIntent.getActivity(
            context,
            requestCode + 500,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            if (canScheduleExactAlarms(context)) {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        }
    }

    fun showImmediateTestNotification(context: Context, prefs: UserPreferences) {
        ensureChannels(context)
        triggerAdhanOrNotification(
            context = context,
            notificationId = 9999,
            waqtId = PrayerWaqt.FAJR.id,
            title = "নূর লাইফ • আজান ও নামাজ অ্যালার্ট",
            body = "${prefs.city.bnName}: আজানের সময় হলে ঠিক এভাবেই নোটিফিকেশন ও আজান ধ্বনি বেজে উঠবে।",
            soundType = prefs.notificationSound,
            vibrate = prefs.vibrationEnabled
        )
    }

    fun triggerAdhanOrNotification(
        context: Context,
        notificationId: Int,
        waqtId: String,
        title: String,
        body: String,
        soundType: NotificationSoundType,
        vibrate: Boolean
    ) {
        ensureChannels(context)
        val shouldPlayAdhanService = (soundType == NotificationSoundType.ADHAN_CHIME || soundType == NotificationSoundType.FAJR_CHIME) &&
            waqtId != PrayerWaqt.SUNRISE.id

        if (shouldPlayAdhanService) {
            val svcIntent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = AdhanPlaybackService.ACTION_PLAY_ADHAN
                putExtra(EXTRA_WAQT_ID, waqtId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_SOUND_TYPE, soundType.id)
                putExtra(EXTRA_VIBRATE, vibrate)
            }
            val started = runCatching {
                ContextCompat.startForegroundService(context, svcIntent)
            }.isSuccess
            if (started) return
        }

        showNotification(
            context = context,
            notificationId = notificationId,
            waqtId = waqtId,
            title = title,
            body = body,
            soundType = soundType,
            vibrate = vibrate
        )
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        waqtId: String,
        title: String,
        body: String,
        soundType: NotificationSoundType,
        vibrate: Boolean
    ) {
        val channelId = channelFor(soundType, waqtId)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_noor_emblem)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)

        if (soundType == NotificationSoundType.SILENT) {
            builder.setSilent(true)
        }
        if (!vibrate) {
            builder.setVibrate(longArrayOf(0L))
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, builder.build())
    }
}
