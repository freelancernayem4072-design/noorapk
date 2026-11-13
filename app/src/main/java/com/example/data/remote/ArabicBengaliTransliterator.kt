package com.example.data.remote

/**
 * Deterministic phonetic transliterator from standard Latin Quranic transliteration
 * (ISO / AlQuran.cloud `en.transliteration`) to readable Bengali pronunciation (বাংলা উচ্চারণ).
 */
object ArabicBengaliTransliterator {

    private val replacements = listOf(
        // Common Quranic phrases first
        "bismillahi" to "বিসমিল্লাহি",
        "bismillah" to "বিসমিল্লাহ",
        "ar-rahmani" to "আর-রাহমানি",
        "ar-rahman" to "আর-রাহমান",
        "ar-rahimi" to "আর-রাহীমি",
        "ar-rahim" to "আর-রাহীম",
        "al-hamdu" to "আল-হামদু",
        "alhamdu" to "আলহামদু",
        "lillahi" to "লিল্লাহি",
        "allahu" to "আল্লাহু",
        "allaha" to "আল্লাহা",
        "allahi" to "আল্লাহি",
        "allah" to "আল্লাহ",
        "rabbil" to "রব্বিল",
        "alamin" to "আলামীন",
        "al-alamin" to "আল-আলামীন",
        "iyyaka" to "ইয়্যাকা",
        "na'budu" to "না'বুদু",
        "nasta'in" to "নাসতাঈন",
        "ihdinas" to "ইহদিনাস",
        "ihdina" to "ইহদিনা",
        "siratal" to "সিরাতাল",
        "sirat" to "সিরাত",
        "mustaqim" to "মুস্তাকীম",
        "an'amta" to "আন'আমতা",
        "alayhim" to "আলাইহিম",
        "ghayril" to "গাইরিল",
        "maghdubi" to "মাগদুবি",
        "wa la" to "ওয়া লা",
        "wala" to "ওয়ালা",
        "qul" to "কুল",
        "huwa" to "হুয়া",
        "ahad" to "আহাদ",
        "samad" to "সামাদ"
    )

    fun toBengaliPronunciation(latinTranslit: String): String {
        if (latinTranslit.isBlank()) return ""
        val normalized = normalizeDiacritics(latinTranslit.lowercase().trim())
        val words = normalized.split(Regex("\\s+"))
        return words.joinToString(" ") { word ->
            transliterateWord(word)
        }
    }

    private fun normalizeDiacritics(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            when (ch) {
                'ā', 'â', 'á', 'à' -> sb.append("aa")
                'ī', 'î', 'í', 'ì' -> sb.append("ee")
                'ū', 'û', 'ú', 'ù' -> sb.append("oo")
                'ḥ' -> sb.append('h')
                'ṣ' -> sb.append('s')
                'ḍ' -> sb.append('d')
                'ṭ' -> sb.append('t')
                'ẓ' -> sb.append('z')
                'ā' -> sb.append("aa")
                'ʻ', '‘', '’', '\'' -> sb.append('\'')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun transliterateWord(raw: String): String {
        for ((k, v) in replacements) {
            if (raw == k) return v
        }
        val sb = StringBuilder()
        var i = 0
        val n = raw.length
        while (i < n) {
            val c = raw[i]
            val next = if (i + 1 < n) raw[i + 1] else '\u0000'
            val next2 = if (i + 2 < n) raw[i + 2] else '\u0000'

            if (c !in 'a'..'z' && c != '\'') {
                sb.append(c)
                i++
                continue
            }

            // Two-character digraphs
            if (c == 's' && next == 'h') {
                sb.append('শ')
                i += 2
                continue
            }
            if (c == 'k' && next == 'h') {
                sb.append('খ')
                i += 2
                continue
            }
            if (c == 'g' && next == 'h') {
                sb.append('গ')
                i += 2
                continue
            }
            if (c == 't' && next == 'h') {
                sb.append('ছ')
                i += 2
                continue
            }
            if (c == 'd' && next == 'h') {
                sb.append('য')
                i += 2
                continue
            }
            if (c == 'z' && next == 'h') {
                sb.append('জ')
                i += 2
                continue
            }
            if (c == 'a' && next == 'a') {
                appendVowel(sb, "আ", "া")
                i += 2
                continue
            }
            if (c == 'e' && next == 'e') {
                appendVowel(sb, "ঈ", "ী")
                i += 2
                continue
            }
            if (c == 'o' && next == 'o') {
                appendVowel(sb, "ঊ", "ূ")
                i += 2
                continue
            }
            if (c == 'a' && next == 'y') {
                appendVowel(sb, "আই", "াই")
                i += 2
                continue
            }
            if (c == 'a' && next == 'w') {
                appendVowel(sb, "আও", "াও")
                i += 2
                continue
            }

            when (c) {
                '\'' -> { /* Ayn/Hamza pause */ }
                'a' -> {
                    if (sb.isEmpty()) sb.append('আ') else if (isConsonantEnd(sb)) sb.append('া') else sb.append('আ')
                }
                'i' -> {
                    if (sb.isEmpty()) sb.append('ই') else if (isConsonantEnd(sb)) sb.append('ি') else sb.append('ই')
                }
                'u' -> {
                    if (sb.isEmpty()) sb.append('উ') else if (isConsonantEnd(sb)) sb.append('ু') else sb.append('উ')
                }
                'e' -> {
                    if (sb.isEmpty()) sb.append('এ') else if (isConsonantEnd(sb)) sb.append('ে') else sb.append('এ')
                }
                'o' -> {
                    if (sb.isEmpty()) sb.append('ও') else if (isConsonantEnd(sb)) sb.append('ো') else sb.append('ও')
                }
                'b' -> sb.append('ব')
                't' -> sb.append('ত')
                'j' -> sb.append('জ')
                'h' -> sb.append('হ')
                'd' -> sb.append('দ')
                'r' -> sb.append('র')
                'z' -> sb.append('য')
                's' -> sb.append('স')
                'f' -> sb.append('ফ')
                'q' -> sb.append('ক')
                'k' -> sb.append('ক')
                'l' -> sb.append('ল')
                'm' -> sb.append('ম')
                'n' -> sb.append('ন')
                'w' -> sb.append("ওয়")
                'y' -> sb.append("য়")
                else -> {}
            }
            i++
        }
        return sb.toString()
    }

    private fun appendVowel(sb: StringBuilder, standalone: String, kar: String) {
        if (sb.isNotEmpty() && isConsonantEnd(sb)) {
            sb.append(kar)
        } else {
            sb.append(standalone)
        }
    }

    private fun isConsonantEnd(sb: StringBuilder): Boolean {
        if (sb.isEmpty()) return false
        val last = sb[sb.length - 1]
        return last in '\u0995'..'\u09B9' || last in '\u09DC'..'\u09DF'
    }
}
