package com.example.domain.prayer

import com.example.localization.BengaliFormatter
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Pure-Kotlin Astronomical Solar Prayer Time, Qibla, Hijri & Bangladesh Bangla Calendar Engine.
 * Based on Jean Meeus solar position algorithms (Astronomical Algorithms).
 */
object PrayerTimeCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateDailySchedule(
        year: Int,
        month: Int, // 1..12
        day: Int,
        city: CityLocation,
        method: CalculationMethod,
        madhhab: AsrMadhhab
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone(city.timeZoneId)
        val cal = Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val baseDayMillis = cal.timeInMillis
        val tzOffsetHours = tz.getOffset(baseDayMillis) / 3_600_000.0

        val jd = julianDay(year, month, day) - city.longitude / 360.0
        val sun = sunPosition(jd)
        val eqt = sun.equationOfTime
        val decl = sun.declination

        // Solar Noon (Dhuhr transit) in local fractional hours
        val transit = 12.0 + tzOffsetHours - city.longitude / 15.0 - eqt

        val sunriseAngle = 0.833333 // standard atmospheric refraction + solar radius
        val sunriseHour = transit - hourAngle(city.latitude, decl, -sunriseAngle)
        val sunsetHour = transit + hourAngle(city.latitude, decl, -sunriseAngle)

        val fajrHour = transit - hourAngle(city.latitude, decl, -method.fajrAngle)
        val ishaHour = if (method.ishaIntervalMinutes != null) {
            sunsetHour + method.ishaIntervalMinutes / 60.0
        } else {
            transit + hourAngle(city.latitude, decl, -method.ishaAngle)
        }

        // Asr altitude: arccot(shadowFactor + tan(|lat - decl|))
        val asrAltDeg = Math.toDegrees(
            atan(1.0 / (madhhab.shadowFactor + tan(Math.toRadians(kotlin.math.abs(city.latitude - decl)))))
        )
        val asrHour = transit + hourAngle(city.latitude, decl, asrAltDeg)

        // Slight standard safety precaution (+1 min Dhuhr, +1 min Maghrib, -1 min Sehri)
        val fajrSlot = makeSlot(PrayerWaqt.FAJR, baseDayMillis, fajrHour)
        val sunriseSlot = makeSlot(PrayerWaqt.SUNRISE, baseDayMillis, sunriseHour)
        val dhuhrSlot = makeSlot(PrayerWaqt.DHUHR, baseDayMillis, transit + 1.0 / 60.0)
        val asrSlot = makeSlot(PrayerWaqt.ASR, baseDayMillis, asrHour)
        val maghribSlot = makeSlot(PrayerWaqt.MAGHRIB, baseDayMillis, sunsetHour + 1.0 / 60.0)
        val ishaSlot = makeSlot(PrayerWaqt.ISHA, baseDayMillis, ishaHour)

        // Sehri ends ~2 minutes before Fajr
        val sehriTotalMin = ((fajrHour * 60.0).roundToInt() - 2).coerceAtLeast(0)
        val sehriH = (sehriTotalMin / 60) % 24
        val sehriM = sehriTotalMin % 60
        val sehriMillis = baseDayMillis + sehriTotalMin * 60_000L

        // Last third of the night (Tahajjud recommended start)
        val nightDurationHours = (fajrHour + 24.0) - sunsetHour
        val tahajjudHour = (fajrHour - nightDurationHours / 3.0).let { if (it < 0) it + 24.0 else it }
        val tahajjudTotalMin = (tahajjudHour * 60.0).roundToInt().coerceIn(0, 1439)
        val tahajjudH = tahajjudTotalMin / 60
        val tahajjudM = tahajjudTotalMin % 60
        val tahajjudMillis = baseDayMillis + tahajjudTotalMin * 60_000L

        return DailyPrayerSchedule(
            dateMillis = baseDayMillis,
            city = city,
            fajr = fajrSlot,
            sunrise = sunriseSlot,
            dhuhr = dhuhrSlot,
            asr = asrSlot,
            maghrib = maghribSlot,
            isha = ishaSlot,
            sehriEndMillis = sehriMillis,
            sehriHour24 = sehriH,
            sehriMinute = sehriM,
            tahajjudStartMillis = tahajjudMillis,
            tahajjudHour24 = tahajjudH,
            tahajjudMinute = tahajjudM
        )
    }

    fun calculateMonthlySchedule(
        year: Int,
        month: Int,
        city: CityLocation,
        method: CalculationMethod,
        madhhab: AsrMadhhab
    ): List<DailyPrayerSchedule> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..maxDay).map { day ->
            calculateDailySchedule(year, month, day, city, method, madhhab)
        }
    }

    private fun makeSlot(waqt: PrayerWaqt, baseDayMillis: Long, fractionalHour: Double): PrayerTimeSlot {
        val normalized = ((fractionalHour % 24.0) + 24.0) % 24.0
        val totalMinutes = (normalized * 60.0).roundToInt().coerceIn(0, 1439)
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return PrayerTimeSlot(
            waqt = waqt,
            epochMillis = baseDayMillis + totalMinutes * 60_000L,
            hour24 = h,
            minute = m
        )
    }

    private data class SunCoords(val declination: Double, val equationOfTime: Double)

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): SunCoords {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0
        val decl = Math.toDegrees(asin(sinDeg(e) * sinDeg(l)))
        val eqt = q / 15.0 - fixHour(ra)
        return SunCoords(declination = decl, equationOfTime = eqt)
    }

    private fun hourAngle(lat: Double, decl: Double, angleDeg: Double): Double {
        val cosH = (sinDeg(angleDeg) - sinDeg(lat) * sinDeg(decl)) / (cosDeg(lat) * cosDeg(decl))
        val clamped = cosH.coerceIn(-0.9999, 0.9999)
        return Math.toDegrees(acos(clamped)) / 15.0
    }

    private fun sinDeg(d: Double) = sin(Math.toRadians(d))
    private fun cosDeg(d: Double) = cos(Math.toRadians(d))
    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double): Double = a - 24.0 * floor(a / 24.0)

    /**
     * Great-circle bearing from user's location to Holy Kaaba (degrees clockwise from True North).
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)
        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val deg = Math.toDegrees(atan2(y, x))
        return (deg + 360.0) % 360.0
    }

    /**
     * Haversine distance in kilometers from user's location to Holy Kaaba.
     */
    fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val dLat = Math.toRadians(KAABA_LAT - latitude)
        val dLon = Math.toRadians(KAABA_LON - longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(KAABA_LAT)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Kuwaiti tabular Hijri algorithm with user-configurable moon-sighting offset (-2..+2 days).
     */
    fun gregorianToHijri(year: Int, month: Int, day: Int, moonOffsetDays: Int = 0): HijriDateInfo {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
            add(Calendar.DAY_OF_MONTH, moonOffsetDays)
        }
        val gY = cal.get(Calendar.YEAR)
        val gM = cal.get(Calendar.MONTH) + 1
        val gD = cal.get(Calendar.DAY_OF_MONTH)

        val jd = if (gY > 1582 || (gY == 1582 && gM > 10) || (gY == 1582 && gM == 10 && gD > 14)) {
            ((1461 * (gY + 4800 + (gM - 14) / 12)) / 4 +
                (367 * (gM - 2 - 12 * ((gM - 14) / 12))) / 12 -
                (3 * ((gY + 4900 + (gM - 14) / 12) / 100)) / 4 +
                gD - 32075)
        } else {
            367 * gY - (7 * (gY + 5001 + (gM - 9) / 7)) / 4 + (275 * gM) / 9 + gD + 1729777
        }

        val l0 = jd - 1948440 + 10632
        val n = (l0 - 1) / 10631
        val l1 = l0 - 10631 * n + 354
        val j = ((10985 - l1) / 5316) * ((50 * l1) / 17719) + (l1 / 5670) * ((43 * l1) / 15238)
        val l2 = l1 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hMonth = (24 * l2) / 709
        val hDay = l2 - (709 * hMonth) / 24
        val hYear = 30 * n + j - 30

        val safeMonth = hMonth.coerceIn(1, 12)
        return HijriDateInfo(
            day = hDay.coerceIn(1, 30),
            month = safeMonth,
            year = hYear,
            monthNameBn = BengaliFormatter.hijriMonthsBn[safeMonth - 1],
            monthNameEn = BengaliFormatter.hijriMonthsEn[safeMonth - 1]
        )
    }

    /**
     * Bangladesh Revised Bangla Calendar (2019 Bangla Academy revision):
     * Months 1..6 (Boishakh..Ashwin) = 31 days; Months 7..11 (Kartik..Falgun) = 30 days (Falgun 31 in leap year); Chaitra = 30 days.
     */
    fun gregorianToBanglaDate(year: Int, month: Int, day: Int): BanglaDateInfo {
        val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        // Pohela Boishakh is always April 14 in Bangladesh
        val pohelaBoishakh = Calendar.getInstance().apply { set(year, Calendar.APRIL, 14, 0, 0, 0) }
        val target = Calendar.getInstance().apply { set(year, month - 1, day, 0, 0, 0) }

        val banglaYear: Int
        val refStart: Calendar
        val leapForFalgun: Boolean
        if (target.before(pohelaBoishakh)) {
            banglaYear = year - 594
            refStart = Calendar.getInstance().apply { set(year - 1, Calendar.APRIL, 14, 0, 0, 0) }
            leapForFalgun = isLeap // Falgun falls in Feb/March of `year`
        } else {
            banglaYear = year - 593
            refStart = pohelaBoishakh
            val nextYear = year + 1
            leapForFalgun = (nextYear % 4 == 0 && nextYear % 100 != 0) || (nextYear % 400 == 0)
        }

        val diffDays = ((target.timeInMillis - refStart.timeInMillis) / 86_400_000L).toInt().coerceAtLeast(0)
        val monthLengths = intArrayOf(
            31, 31, 31, 31, 31, 31, // Boishakh .. Ashwin
            30, 30, 30, 30, if (leapForFalgun) 31 else 30, 30 // Kartik .. Chaitra
        )
        var remaining = diffDays
        var bMonth = 1
        for (len in monthLengths) {
            if (remaining < len) break
            remaining -= len
            bMonth++
        }
        val bDay = remaining + 1
        val safeMonth = bMonth.coerceIn(1, 12)
        val seasonBn = when (safeMonth) {
            1, 2 -> "গ্রীষ্মকাল"
            3, 4 -> "বর্ষাকাল"
            5, 6 -> "শরৎকাল"
            7, 8 -> "হেমন্তকাল"
            9, 10 -> "শীতকাল"
            else -> "বসন্তকাল"
        }
        return BanglaDateInfo(
            day = bDay,
            month = safeMonth,
            year = banglaYear,
            monthNameBn = BengaliFormatter.bengaliBanglaMonths[safeMonth - 1],
            seasonBn = seasonBn
        )
    }
}
