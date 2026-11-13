package com.example.data.content

import kotlinx.serialization.Serializable

@Serializable
data class RawAyahJson(
    val n: Int,
    val g: Int,
    val j: Int,
    val p: Int,
    val ar: String,
    val bn: String,
    val en: String,
    val tr: String
)

@Serializable
data class RawSurahJson(
    val num: Int,
    val nameAr: String,
    val nameEn: String,
    val meaningEn: String,
    val rev: String,
    val ayahs: List<RawAyahJson>
)

@Serializable
data class RawTafsirItemJson(
    val a: Int,
    val t: String
)

data class QuranAyah(
    val numberInSurah: Int,
    val globalNumber: Int,
    val juz: Int,
    val page: Int,
    val arabicText: String,
    val bengaliTranslation: String,
    val englishTranslation: String,
    val latinTransliteration: String,
    val bengaliPronunciation: String
)

data class SurahSummary(
    val number: Int,
    val nameArabic: String,
    val nameBengali: String,
    val meaningBengali: String,
    val nameEnglish: String,
    val revelationType: String,
    val revelationTypeBn: String,
    val ayahCount: Int,
    val juzStart: Int
)

data class SurahDetail(
    val summary: SurahSummary,
    val ayahs: List<QuranAyah>
)

data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val nameBengali: String,
    val startSurah: Int,
    val startAyah: Int,
    val endSurah: Int,
    val endAyah: Int
)

data class QuranReciter(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val styleBn: String
)

data class TafsirEntry(
    val surahNumber: Int,
    val ayahNumber: Int,
    val sourceName: String,
    val text: String
)

object QuranCatalogMetadata {
    val reciters = listOf(
        QuranReciter("ar.alafasy", "মিশারি রশিদ আল-আফাসি", "Mishary Rashid Alafasy", "মুরাত্তাল (কুয়েত)"),
        QuranReciter("ar.abdulbasitmurattal", "আব্দুল বাসিত আব্দুস সামাদ", "Abdul Basit Abdus Samad", "মুরাত্তাল (মিশর)"),
        QuranReciter("ar.mahermuaiqly", "মাহের আল-মুয়াইকলি", "Maher Al Muaiqly", "মক্কা হারাম"),
        QuranReciter("ar.abdurrahmaansudais", "আবদুর রহমান আস-সুদাইস", "Abdur-Rahman As-Sudais", "মক্কা হারাম")
    )

    fun audioSurahUrl(reciterId: String, surahNumber: Int): String =
        "https://cdn.islamic.network/quran/audio-surah/128/$reciterId/$surahNumber.mp3"

    fun audioAyahUrl(reciterId: String, globalAyahNumber: Int): String =
        "https://cdn.islamic.network/quran/audio/128/$reciterId/$globalAyahNumber.mp3"

    // Bengali Surah Names & Meanings for all 114 Surahs
    val surahBnMap: Map<Int, Pair<String, String>> = mapOf(
        1 to ("আল-ফাতিহা" to "সূচনা"),
        2 to ("আল-বাকারা" to "গাভী"),
        3 to ("আলে ইমরান" to "ইমরানের পরিবার"),
        4 to ("আন-নিসা" to "নারী"),
        5 to ("আল-মায়িদাহ" to "খাদ্য পরিবেশিত পাত্র"),
        6 to ("আল-আনআম" to "গবাদি পশু"),
        7 to ("আল-আরাফ" to "উঁচু স্থান"),
        8 to ("আল-আনফাল" to "যুদ্ধলব্ধ সম্পদ"),
        9 to ("আত-তাওবাহ" to "অনুতাপ"),
        10 to ("ইউনুস" to "নবী ইউনুস (আ.)"),
        11 to ("হুদ" to "নবী হুদ (আ.)"),
        12 to ("ইউসুফ" to "নবী ইউসুফ (আ.)"),
        13 to ("আর-রাদ" to "বজ্রপাত"),
        14 to ("ইব্রাহিম" to "নবী ইব্রাহিম (আ.)"),
        15 to ("আল-হিজর" to "পাথুরে উপত্যকা"),
        16 to ("আন-নাহল" to "মৌমাছি"),
        17 to ("বনি ইসরাইল" to "ইসরাইলের বংশধর"),
        18 to ("আল-কাহফ" to "গুহা"),
        19 to ("মারইয়াম" to "মারইয়াম (আ.)"),
        20 to ("ত্বোয়াহা" to "ত্বোয়া-হা"),
        21 to ("আল-আম্বিয়া" to "নবীগণ"),
        22 to ("আল-হজ" to "হজ"),
        23 to ("আল-মুমিনুন" to "মুমিনগণ"),
        24 to ("আন-নূর" to "আলো"),
        25 to ("আল-ফুরকান" to "সত্য-মিথ্যার পার্থক্যকারী"),
        26 to ("আশ-শুআরা" to "কবিগণ"),
        27 to ("আন-নামল" to "পিপীলিকা"),
        28 to ("আল-কাসাস" to "কাহিনী"),
        29 to ("আল-আনকাবুত" to "মাকড়সা"),
        30 to ("আর-রূম" to "রোমক জাতি"),
        31 to ("লুকমান" to "হাকিম লুকমান"),
        32 to ("আস-সাজদাহ" to "সিজদা"),
        33 to ("আল-আহযাব" to "জোটসমূহ"),
        34 to ("সাবা" to "সাবা জাতি"),
        35 to ("ফাতির" to "সৃষ্টিকর্তা"),
        36 to ("ইয়াসিন" to "ইয়া-সিন"),
        37 to ("আস-সাফফাত" to "সারিবদ্ধভাবে দণ্ডায়মান"),
        38 to ("ছোয়াদ" to "ছোয়াদ"),
        39 to ("আয-যুমার" to "দলবদ্ধ জনতা"),
        40 to ("আল-মুমিন (গাফির)" to "ক্ষমাশীল"),
        41 to ("হা-মীম সাজদাহ" to "সুস্পষ্ট বিবরণ"),
        42 to ("আশ-শূরা" to "পরামর্শ"),
        43 to ("আয-যুখরুফ" to "স্বর্ণালংকার"),
        44 to ("আদ-দুখান" to "ধোঁয়া"),
        45 to ("আল-জাসিয়াহ" to "নতজানু"),
        46 to ("আল-আহকাফ" to "বালুর পাহাড়"),
        47 to ("মুহাম্মদ" to "নবী মুহাম্মদ ﷺ"),
        48 to ("আল-ফাতহ" to "বিজয়"),
        49 to ("আল-হুজুরাত" to "বাসগৃহসমূহ"),
        50 to ("ক্বাফ" to "ক্বাফ"),
        51 to ("আয-যারিয়াত" to "বিক্ষেপকারী বাতাস"),
        52 to ("আত-তূর" to "তূর পাহাড়"),
        53 to ("আন-নাজম" to "নক্ষত্র"),
        54 to ("আল-কামার" to "চন্দ্র"),
        55 to ("আর-রাহমান" to "পরম করুণাময়"),
        56 to ("আল-ওয়াকিয়াহ" to "মহাঘটনা"),
        57 to ("আল-হাদিদ" to "লৌহ"),
        58 to ("আল-মুজাদালাহ" to "অনুযোগকারিণী"),
        59 to ("আল-হাশর" to "সমাবেশ"),
        60 to ("আল-মুমতাহিনাহ" to "পরীক্ষিত নারী"),
        61 to ("আস-সাফ" to "সারিবদ্ধ সৈন্য"),
        62 to ("আল-জুমুআহ" to "শুক্রবার"),
        63 to ("আল-মুনাফিকুন" to "মুনাফিকগণ"),
        64 to ("আত-তাগাবুন" to "লাভ-ক্ষতি"),
        65 to ("আত-তালাক" to "বিবাহবিচ্ছেদ"),
        66 to ("আত-তাহরিম" to "নিষিদ্ধকরণ"),
        67 to ("আল-মুলক" to "সার্বভৌম কর্তৃত্ব"),
        68 to ("আল-কলম" to "কলম"),
        69 to ("আল-হাক্কাহ" to "অবশ্যম্ভাবী সত্য"),
        70 to ("আল-মাআরিজ" to "উন্নয়নের সোপান"),
        71 to ("নূহ" to "নবী নূহ (আ.)"),
        72 to ("আল-জিন" to "জিন জাতি"),
        73 to ("আল-মুযযাম্মিল" to "বস্ত্রাবৃত"),
        74 to ("আল-মুদ্দাসসির" to "পোশাক আবৃত"),
        75 to ("আল-কিয়ামাহ" to "পুনরুত্থান"),
        76 to ("আদ-দাহর (আল-ইনসান)" to "মানুষ"),
        77 to ("আল-মুরসালাত" to "প্রেরিত বায়ু"),
        78 to ("আন-নাবা" to "মহাসংবাদ"),
        79 to ("আন-নাযিয়াত" to "প্রচেষ্টাকারী"),
        80 to ("আবাসা" to "ভ্রুকুটি"),
        81 to ("আত-তাকভীর" to "অন্ধকারাচ্ছন্ন"),
        82 to ("আল-ইনফিতার" to "বিদীর্ণ হওয়া"),
        83 to ("আল-মুতাফফিফীন" to "ওজনে কমদানকারী"),
        84 to ("আল-ইনশিকাক" to "খণ্ড-বিখণ্ড হওয়া"),
        85 to ("আল-বুরূজ" to "নক্ষত্রপুঞ্জ"),
        86 to ("আত-তারিক" to "রাতের আগন্তুক"),
        87 to ("আল-আলা" to "সর্বোচ্চ"),
        88 to ("আল-গাশিয়াহ" to "আচ্ছন্নকারী"),
        89 to ("আল-ফাজর" to "ভোরবেলা"),
        90 to ("আল-বালাদ" to "নগরী"),
        91 to ("আশ-শামস" to "সূর্য"),
        92 to ("আল-লাইল" to "রাত্রি"),
        93 to ("আদ-দুহা" to "পূর্বাহ্ণ"),
        94 to ("আল-ইনশিরাহ" to "বক্ষ উন্মোচন"),
        95 to ("আত-তীন" to "ডুমুর ফল"),
        96 to ("আল-আলাক" to "রক্তপিণ্ড"),
        97 to ("আল-কদর" to "মহিমান্বিত রজনী"),
        98 to ("আল-বাইয়্যিনাহ" to "সুস্পষ্ট প্রমাণ"),
        99 to ("আয-যিলযাল" to "ভূমিকম্প"),
        100 to ("আল-আদিয়াত" to "অভিযানকারী অশ্ব"),
        101 to ("আল-কারিয়াহ" to "মহাভীতি"),
        102 to ("আত-তাকাসুর" to "প্রাচুর্যের প্রতিযোগিতা"),
        103 to ("আল-আসর" to "মহাকাল"),
        104 to ("আল-হুমাযাহ" to "পরনিন্দাকারী"),
        105 to ("আল-ফীল" to "হাতি"),
        106 to ("কুরাইশ" to "কুরাইশ গোত্র"),
        107 to ("আল-মাউন" to "নিত্যপ্রয়োজনীয় সাহায্য"),
        108 to ("আল-কাউসার" to "প্রাচুর্য / হাউজে কাউসার"),
        109 to ("আল-কাফিরুন" to "অবিশ্বাসীগণ"),
        110 to ("আন-নাসর" to "সাহায্য"),
        111 to ("আল-লাহাব" to "জ্বলন্ত অঙ্গার"),
        112 to ("আল-ইখলাস" to "একনিষ্ঠতা"),
        113 to ("আল-ফালাক" to "ঊষা / ভোর"),
        114 to ("আন-নাস" to "মানবজাতি")
    )

    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "آلم", "পারা ১: আলিফ-লাম-মীম", 1, 1, 2, 141),
        JuzInfo(2, "سَيَقُولُ", "পারা ২: সায়াকূলু", 2, 142, 2, 252),
        JuzInfo(3, "تِلْكَ الرُّسُلُ", "পারা ৩: তিলকার রুসুলু", 2, 253, 3, 92),
        JuzInfo(4, "لَنْ تَنَالُوا", "পারা ৪: লান তানালু", 3, 93, 4, 23),
        JuzInfo(5, "وَالْمُحْصَنَاتُ", "পারা ৫: ওয়াল মুহসানাতু", 4, 24, 4, 147),
        JuzInfo(6, "لَا يُحِبُّ اللَّهُ", "পারা ৬: লা ইউহিব্বুল্লাহু", 4, 148, 5, 81),
        JuzInfo(7, "وَإِذَا سَمِعُوا", "পারা ৭: ওয়া ইযা সামিউ", 5, 82, 6, 110),
        JuzInfo(8, "وَلَوْ أَنَّنَا", "পারা ৮: ওয়া লাউ আন্নানা", 6, 111, 7, 87),
        JuzInfo(9, "قَالَ الْمَلَأُ", "পারা ৯: ক্বালাল মালাউ", 7, 88, 8, 40),
        JuzInfo(10, "وَاعْلَمُوا", "পারা ১০: ওয়া'লামু", 8, 41, 9, 92),
        JuzInfo(11, "يَعْتَذِرُونَ", "পারা ১১: ইয়াতাযিরূনা", 9, 93, 11, 5),
        JuzInfo(12, "وَمَا مِنْ دَابَّةٍ", "পারা ১২: ওয়া মা মিন দাব্বাতিন", 11, 6, 12, 52),
        JuzInfo(13, "وَمَا أُبَرِّئُ", "পারা ১৩: ওয়া মা উবাররিউ", 12, 53, 14, 52),
        JuzInfo(14, "رُبَمَا", "পারা ১৪: রুবামা", 15, 1, 16, 128),
        JuzInfo(15, "سُبْحَانَ الَّذِي", "পারা ১৫: সুবহানাল্লাযী", 17, 1, 18, 74),
        JuzInfo(16, "قَالَ أَلَمْ", "পারা ১৬: ক্বালা আলাম", 18, 75, 20, 135),
        JuzInfo(17, "اقْتَرَبَ لِلنَّاسِ", "পারা ১৭: ইকতারাবা লিন্নাসি", 21, 1, 22, 78),
        JuzInfo(18, "قَدْ أَفْلَحَ", "পারা ১৮: ক্বাদ আফলাহা", 23, 1, 25, 20),
        JuzInfo(19, "وَقَالَ الَّذِينَ", "পারা ১৯: ওয়া ক্বালাল্লাযীনা", 25, 21, 27, 55),
        JuzInfo(20, "أَمَّنْ خَلَقَ", "পারা ২০: আম্মান খালাকা", 27, 56, 29, 45),
        JuzInfo(21, "اتْلُ مَا أُوحِيَ", "পারা ২১: উতলু মা উহিয়া", 29, 46, 33, 30),
        JuzInfo(22, "وَمَنْ يَقْنُتْ", "পারা ২২: ওয়া মাই ইয়াকনুত", 33, 31, 36, 27),
        JuzInfo(23, "وَمَا لِيَ", "পারা ২৩: ওয়া মা লিয়া", 36, 28, 39, 31),
        JuzInfo(24, "فَمَنْ أَظْلَمُ", "পারা ২৪: ফামান আজলামু", 39, 32, 41, 46),
        JuzInfo(25, "إِلَيْهِ يُرَدُّ", "পারা ২৫: ইলাইহি ইউরাদ্দু", 41, 47, 45, 37),
        JuzInfo(26, "حم", "পারা ২৬: হা-মীম", 46, 1, 51, 30),
        JuzInfo(27, "قَالَ فَمَا خَطْبُكُمْ", "পারা ২৭: ক্বালা ফামা খাতবুকুম", 51, 31, 57, 29),
        JuzInfo(28, "قَدْ سَمِعَ اللَّهُ", "পারা ২৮: ক্বাদ সামিআল্লাহু", 58, 1, 66, 12),
        JuzInfo(29, "تَبَارَكَ الَّذِي", "পারা ২৯: তাবারাকাল্লাযী", 67, 1, 77, 50),
        JuzInfo(30, "عَمَّ", "পারা ৩০: আম্মা ইয়াতাসায়ালুন", 78, 1, 114, 6)
    )
}
