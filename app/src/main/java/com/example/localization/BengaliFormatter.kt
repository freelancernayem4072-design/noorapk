package com.example.localization

import java.text.DecimalFormat
import java.util.Locale

object BengaliFormatter {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(value: Int, lang: AppLanguage = AppLanguage.BENGALI): String {
        val str = value.toString()
        return if (lang == AppLanguage.BENGALI) toBengaliDigits(str) else str
    }

    fun formatNumber(value: Long, lang: AppLanguage = AppLanguage.BENGALI): String {
        val str = value.toString()
        return if (lang == AppLanguage.BENGALI) toBengaliDigits(str) else str
    }

    fun formatDecimal(value: Double, decimals: Int = 2, lang: AppLanguage = AppLanguage.BENGALI): String {
        val pattern = if (decimals <= 0) "#,##0" else "#,##0." + "0".repeat(decimals)
        val df = DecimalFormat(pattern)
        val formatted = df.format(value)
        return if (lang == AppLanguage.BENGALI) toBengaliDigits(formatted) else formatted
    }

    fun formatTime12Hour(hour24: Int, minute: Int, lang: AppLanguage = AppLanguage.BENGALI): String {
        val isPm = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minStr = minute.toString().padStart(2, '0')
        return if (lang == AppLanguage.BENGALI) {
            val period = when {
                hour24 in 0..3 -> "রাত"
                hour24 in 4..5 -> "ভোর"
                hour24 in 6..11 -> "সকাল"
                hour24 in 12..15 -> "দুপুর"
                hour24 in 16..17 -> "বিকাল"
                hour24 in 18..19 -> "সন্ধ্যা"
                else -> "রাত"
            }
            "${toBengaliDigits(hour12.toString())}:${toBengaliDigits(minStr)} ($period)"
        } else {
            val amPm = if (isPm) "PM" else "AM"
            String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
        }
    }

    fun formatCompactTime(hour24: Int, minute: Int, lang: AppLanguage = AppLanguage.BENGALI): String {
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minStr = minute.toString().padStart(2, '0')
        return if (lang == AppLanguage.BENGALI) {
            val suffix = if (hour24 < 12) "পূর্বাহ্ণ" else "অপরাহ্ণ"
            "${toBengaliDigits(hour12.toString())}:${toBengaliDigits(minStr)} $suffix"
        } else {
            val amPm = if (hour24 >= 12) "PM" else "AM"
            String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
        }
    }

    fun formatLiveClock(hour24: Int, minute: Int, second: Int, lang: AppLanguage = AppLanguage.BENGALI): String {
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minStr = minute.toString().padStart(2, '0')
        val secStr = second.toString().padStart(2, '0')
        return if (lang == AppLanguage.BENGALI) {
            val period = when {
                hour24 in 0..3 -> "রাত"
                hour24 in 4..5 -> "ভোর"
                hour24 in 6..11 -> "সকাল"
                hour24 in 12..15 -> "দুপুর"
                hour24 in 16..17 -> "বিকাল"
                hour24 in 18..19 -> "সন্ধ্যা"
                else -> "রাত"
            }
            "${toBengaliDigits(hour12.toString())}:${toBengaliDigits(minStr)}:${toBengaliDigits(secStr)} $period"
        } else {
            val amPm = if (hour24 >= 12) "PM" else "AM"
            String.format(Locale.US, "%d:%02d:%02d %s", hour12, minute, second, amPm)
        }
    }

    fun formatCountdown(totalSeconds: Long, lang: AppLanguage = AppLanguage.BENGALI): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val seconds = safe % 60
        val raw = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        return if (lang == AppLanguage.BENGALI) toBengaliDigits(raw) else raw
    }

    val bengaliWeekdays = listOf(
        "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    )
    val englishWeekdays = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val bengaliGregorianMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val englishGregorianMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val bengaliBanglaMonths = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    val hijriMonthsBn = listOf(
        "মুহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
}
