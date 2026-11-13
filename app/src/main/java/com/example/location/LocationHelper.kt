package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.domain.prayer.CityLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentCity(context: Context): Result<CityLocation> = runCatching {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCancellableCoroutine<Location?> { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last -> cont.resume(last) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last -> cont.resume(last) }
                        .addOnFailureListener { cont.resume(null) }
                }
        } ?: error("জিপিএস অবস্থান নির্ণয় করা যায়নি। অনুগ্রহ করে ডিভাইসের লোকেশন চালু আছে কিনা দেখুন অথবা ম্যানুয়ালি শহর নির্বাচন করুন।")

        val cityName = reverseGeocodeCityName(context, location.latitude, location.longitude)
        CityLocation(
            id = "gps",
            bnName = cityName ?: "বর্তমান জিপিএস অবস্থান",
            enName = cityName ?: "Current GPS Location",
            countryBn = "জিপিএস অবস্থান",
            countryEn = "GPS Location",
            latitude = location.latitude,
            longitude = location.longitude,
            timeZoneId = TimeZone.getDefault().id
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocodeCityName(context: Context, lat: Double, lon: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            runCatching {
                val geocoder = Geocoder(context, Locale("bn", "BD"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine<String?> { cont ->
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            cont.resume(addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea)
                        }
                    }
                } else {
                    val addr = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                }
            }.getOrNull()
        }
}
