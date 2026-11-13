package com.example.domain.prayer

import com.example.localization.AppLanguage
import com.example.localization.AppStrings

enum class PrayerWaqt(
    val id: String,
    val bnName: String,
    val enName: String,
    val isObligatory: Boolean = true
) {
    FAJR("fajr", "ফজর", "Fajr", true),
    SUNRISE("sunrise", "সূর্যোদয়", "Sunrise", false),
    DHUHR("dhuhr", "যোহর", "Dhuhr", true),
    ASR("asr", "আসর", "Asr", true),
    MAGHRIB("maghrib", "মাগরিব", "Maghrib", true),
    ISHA("isha", "এশা", "Isha", true);

    fun displayName(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) bnName else enName
    fun localizedName(strings: AppStrings): String = when (this) {
        FAJR -> strings.fajr
        SUNRISE -> strings.sunrise
        DHUHR -> strings.dhuhr
        ASR -> strings.asr
        MAGHRIB -> strings.maghrib
        ISHA -> strings.isha
    }
}

enum class CalculationMethod(
    val id: String,
    val bnName: String,
    val enName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    ISLAMIC_FOUNDATION_BD(
        "ifb",
        "ইসলামিক ফাউন্ডেশন বাংলাদেশ (১৮°/১৮°)",
        "Islamic Foundation Bangladesh (18°/18°)",
        18.0,
        18.0
    ),
    KARACHI(
        "karachi",
        "করাচি ইসলামিক ইউনিভার্সিটি (১৮°/১৮°)",
        "Univ. of Islamic Sciences, Karachi (18°/18°)",
        18.0,
        18.0
    ),
    MWL(
        "mwl",
        "মুসলিম ওয়ার্ল্ড লিগ (১৮°/১৭°)",
        "Muslim World League (18°/17°)",
        18.0,
        17.0
    ),
    UMM_AL_QURA(
        "makkah",
        "উম্মুল কুরা, মক্কা (১৮.৫°/৯০ মি.)",
        "Umm Al-Qura, Makkah (18.5°/90m)",
        18.5,
        0.0,
        90
    ),
    EGYPTIAN(
        "egypt",
        "মিশরীয় জেনারেল অথরিটি (১৯.৫°/১৭.৫°)",
        "Egyptian General Authority (19.5°/17.5°)",
        19.5,
        17.5
    ),
    ISNA(
        "isna",
        "উত্তর আমেরিকা - ISNA (১৫°/১৫°)",
        "ISNA North America (15°/15°)",
        15.0,
        15.0
    );

    companion object {
        fun fromId(id: String?): CalculationMethod =
            entries.firstOrNull { it.id == id } ?: ISLAMIC_FOUNDATION_BD
    }
}

enum class AsrMadhhab(val id: String, val bnName: String, val enName: String, val shadowFactor: Int) {
    HANAFI("hanafi", "হানাফি (২ গুণ ছায়া)", "Hanafi (2x Shadow)", 2),
    SHAFI("shafi", "শাফেয়ী / মালিকি / হাম্বলি (১ গুণ ছায়া)", "Shafi'i / Maliki / Hanbali (1x Shadow)", 1);

    companion object {
        fun fromId(id: String?): AsrMadhhab = entries.firstOrNull { it.id == id } ?: HANAFI
    }
}

data class CityLocation(
    val id: String,
    val bnName: String,
    val enName: String,
    val countryBn: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String = "Asia/Dhaka"
) {
    fun displayName(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) bnName else enName
    fun displayFull(lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) "$bnName, $countryBn" else "$enName, $countryEn"
}

data class PrayerTimeSlot(
    val waqt: PrayerWaqt,
    val epochMillis: Long,
    val hour24: Int,
    val minute: Int
)

data class DailyPrayerSchedule(
    val dateMillis: Long,
    val city: CityLocation,
    val fajr: PrayerTimeSlot,
    val sunrise: PrayerTimeSlot,
    val dhuhr: PrayerTimeSlot,
    val asr: PrayerTimeSlot,
    val maghrib: PrayerTimeSlot,
    val isha: PrayerTimeSlot,
    val sehriEndMillis: Long,
    val sehriHour24: Int,
    val sehriMinute: Int,
    val tahajjudStartMillis: Long,
    val tahajjudHour24: Int,
    val tahajjudMinute: Int
) {
    val allSlots: List<PrayerTimeSlot>
        get() = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)

    val fiveWaqtSlots: List<PrayerTimeSlot>
        get() = listOf(fajr, dhuhr, asr, maghrib, isha)
}

data class HijriDateInfo(
    val day: Int,
    val month: Int, // 1..12
    val year: Int,
    val monthNameBn: String,
    val monthNameEn: String
)

data class BanglaDateInfo(
    val day: Int,
    val month: Int, // 1..12
    val year: Int,
    val monthNameBn: String,
    val seasonBn: String
)

object BangladeshCitiesCatalog {
    val cities: List<CityLocation> = listOf(
        // Major Bangladesh Divisions & All Key Districts
        CityLocation("dhaka", "ঢাকা", "Dhaka", "বাংলাদেশ", "Bangladesh", 23.8103, 90.4125),
        CityLocation("chattogram", "চট্টগ্রাম", "Chattogram", "বাংলাদেশ", "Bangladesh", 22.3569, 91.7832),
        CityLocation("sylhet", "সিলেট", "Sylhet", "বাংলাদেশ", "Bangladesh", 24.8949, 91.8687),
        CityLocation("rajshahi", "রাজশাহী", "Rajshahi", "বাংলাদেশ", "Bangladesh", 24.3745, 88.6042),
        CityLocation("khulna", "খুলনা", "Khulna", "বাংলাদেশ", "Bangladesh", 22.8456, 89.5403),
        CityLocation("barishal", "বরিশাল", "Barishal", "বাংলাদেশ", "Bangladesh", 22.7010, 90.3535),
        CityLocation("rangpur", "রংপুর", "Rangpur", "বাংলাদেশ", "Bangladesh", 25.7439, 89.2752),
        CityLocation("mymensingh", "ময়মনসিংহ", "Mymensingh", "বাংলাদেশ", "Bangladesh", 24.7471, 90.4203),
        CityLocation("cumilla", "কুমিল্লা", "Cumilla", "বাংলাদেশ", "Bangladesh", 23.4607, 91.1809),
        CityLocation("gazipur", "গাজীপুর", "Gazipur", "বাংলাদেশ", "Bangladesh", 23.9999, 90.4203),
        CityLocation("narayanganj", "নারায়ণগঞ্জ", "Narayanganj", "বাংলাদেশ", "Bangladesh", 23.6238, 90.5000),
        CityLocation("coxsbazar", "কক্সবাজার", "Cox's Bazar", "বাংলাদেশ", "Bangladesh", 21.4272, 92.0058),
        CityLocation("bogura", "বগুড়া", "Bogura", "বাংলাদেশ", "Bangladesh", 24.8481, 89.3730),
        CityLocation("jashore", "যশোর", "Jashore", "বাংলাদেশ", "Bangladesh", 23.1634, 89.2182),
        CityLocation("dinajpur", "দিনাজপুর", "Dinajpur", "বাংলাদেশ", "Bangladesh", 25.6279, 88.6332),
        CityLocation("tangail", "টাঙ্গাইল", "Tangail", "বাংলাদেশ", "Bangladesh", 24.2513, 89.9167),
        CityLocation("faridpur", "ফরিদপুর", "Faridpur", "বাংলাদেশ", "Bangladesh", 23.6071, 89.8429),
        CityLocation("noakhali", "নোয়াখালী", "Noakhali", "বাংলাদেশ", "Bangladesh", 22.8696, 91.0994),
        CityLocation("feni", "ফেনী", "Feni", "বাংলাদেশ", "Bangladesh", 23.0159, 91.3976),
        CityLocation("brahmanbaria", "ব্রাহ্মণবাড়িয়া", "Brahmanbaria", "বাংলাদেশ", "Bangladesh", 23.9608, 91.1115),
        CityLocation("chandpur", "চাঁদপুর", "Chandpur", "বাংলাদেশ", "Bangladesh", 23.2333, 90.6713),
        CityLocation("pabna", "পাবনা", "Pabna", "বাংলাদেশ", "Bangladesh", 24.0064, 89.2372),
        CityLocation("sirajganj", "সিরাজগঞ্জ", "Sirajganj", "বাংলাদেশ", "Bangladesh", 24.4534, 89.7007),
        CityLocation("kushtia", "কুষ্টিয়া", "Kushtia", "বাংলাদেশ", "Bangladesh", 23.9013, 89.1205),
        CityLocation("satkhira", "সাতক্ষীরা", "Satkhira", "বাংলাদেশ", "Bangladesh", 22.7185, 89.0705),
        CityLocation("bagerhat", "বাগেরহাট", "Bagerhat", "বাংলাদেশ", "Bangladesh", 22.6516, 89.7859),
        CityLocation("patuakhali", "পটুয়াখালী", "Patuakhali", "বাংলাদেশ", "Bangladesh", 22.3596, 90.3299),
        CityLocation("bhola", "ভোলা", "Bhola", "বাংলাদেশ", "Bangladesh", 22.6859, 90.6482),
        CityLocation("jamalpur", "জামালপুর", "Jamalpur", "বাংলাদেশ", "Bangladesh", 24.9375, 89.9378),
        CityLocation("kishoreganj", "কিশোরগঞ্জ", "Kishoreganj", "বাংলাদেশ", "Bangladesh", 24.4449, 90.7766),
        CityLocation("narsingdi", "নরসিংদী", "Narsingdi", "বাংলাদেশ", "Bangladesh", 23.9229, 90.7177),
        CityLocation("manikganj", "মানিকগঞ্জ", "Manikganj", "বাংলাদেশ", "Bangladesh", 23.8644, 90.0047),
        CityLocation("munshiganj", "মুন্সিগঞ্জ", "Munshiganj", "বাংলাদেশ", "Bangladesh", 23.5422, 90.5305),
        CityLocation("netrokona", "নেত্রকোনা", "Netrokona", "বাংলাদেশ", "Bangladesh", 24.8709, 90.7279),
        CityLocation("sherpur", "শেরপুর", "Sherpur", "বাংলাদেশ", "Bangladesh", 25.0205, 90.0153),
        CityLocation("habiganj", "হবিগঞ্জ", "Habiganj", "বাংলাদেশ", "Bangladesh", 24.3749, 91.4155),
        CityLocation("moulvibazar", "মৌলভীবাজার", "Moulvibazar", "বাংলাদেশ", "Bangladesh", 24.4829, 91.7774),
        CityLocation("sunamganj", "সুনামগঞ্জ", "Sunamganj", "বাংলাদেশ", "Bangladesh", 25.0658, 91.3950),
        CityLocation("rangamati", "রাঙ্গামাটি", "Rangamati", "বাংলাদেশ", "Bangladesh", 22.7324, 92.2985),
        CityLocation("bandarban", "বান্দরবান", "Bandarban", "বাংলাদেশ", "Bangladesh", 22.1953, 92.2184),
        CityLocation("khagrachhari", "খাগড়াছড়ি", "Khagrachhari", "বাংলাদেশ", "Bangladesh", 23.1193, 91.9847),
        CityLocation("lakshmipur", "লক্ষ্মীপুর", "Lakshmipur", "বাংলাদেশ", "Bangladesh", 22.9425, 90.8412),
        CityLocation("natore", "নাটোর", "Natore", "বাংলাদেশ", "Bangladesh", 24.4206, 89.0003),
        CityLocation("naogaon", "নওগাঁ", "Naogaon", "বাংলাদেশ", "Bangladesh", 24.7936, 88.9318),
        CityLocation("chapainawabganj", "চাঁপাইনবাবগঞ্জ", "Chapainawabganj", "বাংলাদেশ", "Bangladesh", 24.5965, 88.2775),
        CityLocation("joypurhat", "জয়পুরহাট", "Joypurhat", "বাংলাদেশ", "Bangladesh", 25.0968, 89.0227),
        CityLocation("kurigram", "কুড়িগ্রাম", "Kurigram", "বাংলাদেশ", "Bangladesh", 25.8072, 89.6295),
        CityLocation("gaibandha", "গাইবান্ধা", "Gaibandha", "বাংলাদেশ", "Bangladesh", 25.3288, 89.5281),
        CityLocation("nilphamari", "নীলফামারী", "Nilphamari", "বাংলাদেশ", "Bangladesh", 25.9318, 88.8560),
        CityLocation("lalmonirhat", "লালমনিরহাট", "Lalmonirhat", "বাংলাদেশ", "Bangladesh", 25.9923, 89.2847),
        CityLocation("thakurgaon", "ঠাকুরগাঁও", "Thakurgaon", "বাংলাদেশ", "Bangladesh", 26.0337, 88.4617),
        CityLocation("panchagarh", "পঞ্চগড়", "Panchagarh", "বাংলাদেশ", "Bangladesh", 26.3411, 88.5542),
        CityLocation("jhenaidah", "ঝিনাইদহ", "Jhenaidah", "বাংলাদেশ", "Bangladesh", 23.5450, 89.1726),
        CityLocation("magura", "মাগুরা", "Magura", "বাংলাদেশ", "Bangladesh", 23.4873, 89.4199),
        CityLocation("narail", "নড়াইল", "Narail", "বাংলাদেশ", "Bangladesh", 23.1725, 89.5127),
        CityLocation("chuadanga", "চুয়াডাঙ্গা", "Chuadanga", "বাংলাদেশ", "Bangladesh", 23.6402, 88.8418),
        CityLocation("meherpur", "মেহেরপুর", "Meherpur", "বাংলাদেশ", "Bangladesh", 23.7622, 88.6318),
        CityLocation("gopalganj", "গোপালগঞ্জ", "Gopalganj", "বাংলাদেশ", "Bangladesh", 23.0051, 89.8266),
        CityLocation("madaripur", "মাদারীপুর", "Madaripur", "বাংলাদেশ", "Bangladesh", 23.1641, 90.1897),
        CityLocation("shariatpur", "শরীয়তপুর", "Shariatpur", "বাংলাদেশ", "Bangladesh", 23.2423, 90.4348),
        CityLocation("rajbari", "রাজবাড়ী", "Rajbari", "বাংলাদেশ", "Bangladesh", 23.7574, 89.6445),
        CityLocation("pirojpur", "পিরোজপুর", "Pirojpur", "বাংলাদেশ", "Bangladesh", 22.5841, 89.9720),
        CityLocation("jhalokathi", "ঝালকাঠি", "Jhalokathi", "বাংলাদেশ", "Bangladesh", 22.6406, 90.1987),
        CityLocation("barguna", "বরগুনা", "Barguna", "বাংলাদেশ", "Bangladesh", 22.1508, 90.1264),
        // Major International Cities
        CityLocation("makkah", "মক্কা মুকাররামা", "Makkah", "সৌদি আরব", "Saudi Arabia", 21.3891, 39.8579, "Asia/Riyadh"),
        CityLocation("madinah", "মদিনা মুনাওয়ারা", "Madinah", "সৌদি আরব", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        CityLocation("riyadh", "রিয়াদ", "Riyadh", "সৌদি আরব", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        CityLocation("dubai", "দুবাই", "Dubai", "সংযুক্ত আরব আমিরাত", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
        CityLocation("doha", "দোহা", "Doha", "কাতার", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        CityLocation("kualalumpur", "কুয়ালালামপুর", "Kuala Lumpur", "মালয়েশিয়া", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityLocation("london", "লন্ডন", "London", "যুক্তরাজ্য", "UK", 51.5074, -0.1278, "Europe/London"),
        CityLocation("newyork", "নিউ ইয়র্ক", "New York", "যুক্তরাষ্ট্র", "USA", 40.7128, -74.0060, "America/New_York"),
        CityLocation("kolkata", "কলকাতা", "Kolkata", "ভারত", "India", 22.5726, 88.3639, "Asia/Kolkata")
    )

    val defaultDhaka: CityLocation get() = cities.first()

    fun findById(id: String?): CityLocation = cities.firstOrNull { it.id == id } ?: defaultDhaka
}
