package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMob Configuration & Religious UX Guarded Manager.
 *
 * Religious UX Rules enforced:
 * - NEVER show ads inside Quran reading, Tafsir, Audio recitation, Salah guide, Wudu/Ghusl, Dua reading, or Tasbih dhikr focus.
 * - Only show respectful banner/optional ads on non-worship utility or dashboard footer areas.
 * - Uses official Google Test Ad Unit IDs in DEBUG builds; configure production IDs before release.
 */
object AdConfig {
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Replace with your live AdMob IDs in release builds before Play Console submission
    private const val PROD_BANNER_ID = TEST_BANNER_ID
    private const val PROD_INTERSTITIAL_ID = TEST_INTERSTITIAL_ID
    private const val PROD_REWARDED_ID = TEST_REWARDED_ID

    val isTestMode: Boolean get() = BuildConfig.DEBUG || PROD_BANNER_ID == TEST_BANNER_ID
    val bannerUnitId: String get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID
    val interstitialUnitId: String get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
    val rewardedUnitId: String get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID
}

object NoorAdManager {
    private const val TAG = "NoorAdManager"

    @Volatile
    private var initialized = false
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialShownMs: Long = 0L

    fun initialize(context: Context) {
        if (initialized) return
        runCatching {
            MobileAds.initialize(context.applicationContext) {
                initialized = true
                preloadInterstitial(context.applicationContext)
                preloadRewarded(context.applicationContext)
            }
        }.onFailure {
            Log.w(TAG, "AdMob init skipped or offline: ${it.message}")
        }
    }

    fun preloadInterstitial(context: Context) {
        runCatching {
            val request = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AdConfig.interstitialUnitId,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        Log.d(TAG, "Interstitial failed: ${error.message}")
                    }
                }
            )
        }
    }

    fun preloadRewarded(context: Context) {
        runCatching {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                AdConfig.rewardedUnitId,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        Log.d(TAG, "Rewarded failed: ${error.message}")
                    }
                }
            )
        }
    }

    /**
     * Respectful frequency-capped interstitial (at least 3 minutes apart, never in worship screens).
     */
    fun showInterstitialIfReady(activity: Activity?, onComplete: () -> Unit) {
        val now = System.currentTimeMillis()
        val ad = interstitialAd
        if (activity == null || ad == null || now - lastInterstitialShownMs < 180_000L) {
            onComplete()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShownMs = System.currentTimeMillis()
                preloadInterstitial(activity.applicationContext)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onComplete()
            }
        }
        ad.show(activity)
    }

    fun showRewardedSupportAd(activity: Activity?, onRewarded: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (activity == null || ad == null) {
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                preloadRewarded(activity.applicationContext)
            }
        }
        ad.show(activity) {
            onRewarded()
        }
    }
}

/**
 * Respectful AdMob Banner strictly for non-worship utility screens (e.g., Zakat/Tools/Profile).
 * Never placed inside Quran reading, Tafsir, Salah, Wudu, Dua, or Tasbih focus screens.
 */
@Composable
fun NoorRespectfulBannerAd(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOnline) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.small
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConfig.bannerUnitId
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.d("NoorAdBanner", "Banner load skipped: ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
