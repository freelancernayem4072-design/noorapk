package com.example.notifications

import android.content.Context
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PrayerRescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val prefs = UserPreferencesRepository(applicationContext).preferencesFlow.first()
            PrayerNotificationScheduler.scheduleUpcomingPrayers(applicationContext, prefs)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val WORK_NAME = "noor_prayer_reschedule_daily"

        fun enqueuePeriodic(context: Context) {
            runCatching {
                val appContext = context.applicationContext
                if (!WorkManager.isInitialized()) {
                    WorkManager.initialize(
                        appContext,
                        Configuration.Builder().build()
                    )
                }
                val request = PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(12, TimeUnit.HOURS)
                    .build()
                WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            }
        }
    }
}
