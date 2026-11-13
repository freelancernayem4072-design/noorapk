package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.NotificationSoundType
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val waqtId = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_WAQT_ID) ?: "fajr"
        val title = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_TITLE) ?: "নামাজের সময় • নূর লাইফ"
        val body = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_BODY) ?: "নামাজের ওয়াক্ত শুরু হয়েছে।"
        val soundType = NotificationSoundType.fromId(
            intent.getStringExtra(PrayerNotificationScheduler.EXTRA_SOUND_TYPE)
        )
        val vibrate = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_VIBRATE, true)

        PrayerNotificationScheduler.ensureChannels(context)
        PrayerNotificationScheduler.showNotification(
            context = context,
            notificationId = 3000 + waqtId.hashCode(),
            waqtId = waqtId,
            title = title,
            body = body,
            soundType = soundType,
            vibrate = vibrate
        )

        // Reschedule next day's alarm asynchronously
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val prefs = UserPreferencesRepository(context).preferencesFlow.first()
                PrayerNotificationScheduler.scheduleUpcomingPrayers(context, prefs)
            }
            pendingResult.finish()
        }
    }
}
