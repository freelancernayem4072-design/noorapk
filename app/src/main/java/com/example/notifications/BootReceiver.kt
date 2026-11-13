package com.example.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val isExactAlarmPermChanged = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            isExactAlarmPermChanged
        ) {
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
}
