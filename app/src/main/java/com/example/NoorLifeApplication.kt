package com.example

import android.app.Application
import com.example.audio.QuranAudioController
import com.example.data.local.NoorDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.NetworkConnectivityObserver
import com.example.data.repository.QuranRepository
import com.example.notifications.AdhanAudioRepository
import com.example.notifications.PrayerNotificationScheduler
import com.example.notifications.PrayerRescheduleWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NoorLifeApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: NoorDatabase by lazy { NoorDatabase.getInstance(this) }
    val preferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val connectivityObserver: NetworkConnectivityObserver by lazy { NetworkConnectivityObserver(this) }
    val quranRepository: QuranRepository by lazy {
        QuranRepository(
            context = this,
            tafsirCacheDao = database.tafsirCacheDao(),
            audioDownloadDao = database.audioDownloadDao()
        )
    }
    val quranAudioController: QuranAudioController by lazy {
        QuranAudioController(
            context = this,
            quranRepository = quranRepository,
            scope = applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        PrayerNotificationScheduler.ensureChannels(this)
        PrayerRescheduleWorker.enqueuePeriodic(this)
        AdhanAudioRepository.refreshCacheState(this)
        applicationScope.launch(Dispatchers.IO) {
            runCatching { AdhanAudioRepository.ensureAdhanCached(this@NoorLifeApplication) }
        }
    }
}
