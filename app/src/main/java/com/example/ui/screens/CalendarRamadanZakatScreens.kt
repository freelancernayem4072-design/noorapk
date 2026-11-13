package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.NoorRespectfulBannerAd
import com.example.data.content.IslamicKnowledgeData
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.localization.BengaliFormatter
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorBrandGradientBanner
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.theme.DeepRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel
import java.util.Calendar

@Composable
fun IslamicCalendarScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val liveStatus by viewModel.livePrayerStatus.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ইসলামিক ও বাংলা ক্যালেন্ডার",
                subtitle = "হিজরি, বাংলা (বঙ্গাব্দ) ও ইংরেজি তারিখ",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NoorBrandGradientBanner {
                    Text(
                        text = "${BengaliFormatter.formatNumber(liveStatus.hijriDate.day)} ${liveStatus.hijriDate.monthNameBn} ${BengaliFormatter.formatNumber(liveStatus.hijriDate.year)} হিজরি",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "বাংলা: ${BengaliFormatter.formatNumber(liveStatus.banglaDate.day)} ${liveStatus.banglaDate.monthNameBn} ${BengaliFormatter.formatNumber(liveStatus.banglaDate.year)} বঙ্গাব্দ (${liveStatus.banglaDate.seasonBn})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Text(
                        text = "ইংরেজি: ${liveStatus.gregorianDateBn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            // Moon-sighting offset control
            item {
                NoorElevatedCard {
                    Text(
                        text = "স্থানীয় চাঁদ দেখা সমন্বয় (Moon Sighting Offset)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "বাংলাদেশে চাঁদ দেখার ওপর ভিত্তি করে হিজরি তারিখ -২ থেকে +২ দিন পর্যন্ত সমন্বয় করতে পারেন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setHijriMoonOffset(prefs.hijriMoonOffset - 1) },
                            enabled = prefs.hijriMoonOffset > -2
                        ) { Icon(Icons.Filled.Remove, "কমান") }

                        Text(
                            text = "${if (prefs.hijriMoonOffset >= 0) "+" else ""}${BengaliFormatter.formatNumber(prefs.hijriMoonOffset)} দিন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )

                        OutlinedButton(
                            onClick = { viewModel.setHijriMoonOffset(prefs.hijriMoonOffset + 1) },
                            enabled = prefs.hijriMoonOffset < 2
                        ) { Icon(Icons.Filled.Add, "বাড়ান") }
                    }
                }
            }

            item {
                Text(
                    text = "গুরুত্বপূর্ণ ইসলামিক দিবসসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(IslamicKnowledgeData.islamicEvents) { event ->
                val monthName = BengaliFormatter.hijriMonthsBn[(event.hijriMonth - 1).coerceIn(0, 11)]
                NoorElevatedCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${BengaliFormatter.formatNumber(event.hijriDay)} $monthName",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = DeepRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = event.moonSightingNoteBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(event.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(event.descriptionBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun RamadanModuleScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val liveStatus by viewModel.livePrayerStatus.collectAsStateWithLifecycle()
    val fastingDays by viewModel.fastingDays.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    val cal = remember { Calendar.getInstance() }
    val monthSchedule = remember(prefs.city, prefs.calculationMethod, prefs.asrMadhhab) {
        viewModel.getMonthlySchedule(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }
    val fastedMap = remember(fastingDays) { fastingDays.associateBy { it.dateKey } }
    val totalFasted = remember(fastingDays) { fastingDays.count { it.isFasted } }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "রমজান ও সিয়াম",
                subtitle = "সাহরি-ইফতার, রোজা ট্র্যাকার ও মাসয়ালা",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            NoorBrandGradientBanner(modifier = Modifier.padding(16.dp)) {
                val sched = liveStatus.todaySchedule
                Text(
                    text = "আজকের সাহরি ও ইফতার (${prefs.city.bnName})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("সাহরির শেষ সময়", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Text(
                            BengaliFormatter.formatTime12Hour(sched.sehriHour24, sched.sehriMinute, prefs.language),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ইফতারের সময়", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Text(
                            BengaliFormatter.formatTime12Hour(sched.maghrib.hour24, sched.maghrib.minute, prefs.language),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "মোট সংরক্ষিত রোজা: ${BengaliFormatter.formatNumber(totalFasted)} দিন",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("৩০ দিনের সূচি") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("রোজা ট্র্যাকার") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("দোয়া ও মাসয়ালা") })
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (tab) {
                    0 -> {
                        items(monthSchedule.indices.toList()) { idx ->
                            val daySched = monthSchedule[idx]
                            val dayNum = idx + 1
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "দিন ${BengaliFormatter.formatNumber(dayNum)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "সাহরি: ${BengaliFormatter.formatCompactTime(daySched.sehriHour24, daySched.sehriMinute)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "ইফতার: ${BengaliFormatter.formatCompactTime(daySched.maghrib.hour24, daySched.maghrib.minute)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        items((1..30).toList()) { day ->
                            val key = "ramadan_day_${day.toString().padStart(2, '0')}"
                            val fasted = fastedMap[key]?.isFasted == true
                            Surface(
                                onClick = { viewModel.toggleFastingDay(key, !fasted, "RAMADAN") },
                                shape = RoundedCornerShape(14.dp),
                                color = if (fasted) EmeraldSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${BengaliFormatter.formatNumber(day)} রমজান",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (fasted) "রোজা রাখা হয়েছে" else "চিহ্নিত করুন",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (fasted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Checkbox(
                                            checked = fasted,
                                            onCheckedChange = { viewModel.toggleFastingDay(key, it, "RAMADAN") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            NoorElevatedCard {
                                Text("ইফতারের দোয়া", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                Spacer(Modifier.height(6.dp))
                                ArabicRtlText(text = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", fontSizeSp = 21f)
                                Spacer(Modifier.height(6.dp))
                                Text("উচ্চারণ: যাহাবাজ জামাউ ওয়াবতাল্লাতিল উরূকু ওয়া ছাবাতাল আজরু ইনশাআল্লাহ।", style = MaterialTheme.typography.bodyMedium, color = RoyalBlue)
                                Text("অর্থ: পিপাসা দূর হয়েছে, শিরা-উপশিরা সিক্ত হয়েছে এবং আল্লাহ চাইলে সওয়াব নির্ধারিত হয়েছে। (সুনানে আবু দাউদ: ২৩৫৭)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        items(IslamicKnowledgeData.ramadanGuideTopics, key = { it.id }) { topic ->
                            NoorElevatedCard {
                                Text(topic.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(topic.bodyBn, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZakatCalculatorScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var cashInput by remember { mutableStateOf("") }
    var bankInput by remember { mutableStateOf("") }
    var goldGramsInput by remember { mutableStateOf("") }
    var silverGramsInput by remember { mutableStateOf("") }
    var businessInput by remember { mutableStateOf("") }
    var otherAssetsInput by remember { mutableStateOf("") }
    var liabilitiesInput by remember { mutableStateOf("") }

    var goldRateInput by remember(prefs.goldPricePerGramBdt) {
        mutableStateOf(prefs.goldPricePerGramBdt.toLong().toString())
    }
    var silverRateInput by remember(prefs.silverPricePerGramBdt) {
        mutableStateOf(prefs.silverPricePerGramBdt.toLong().toString())
    }
    var useSilverNisab by remember { mutableStateOf(true) }
    var savedBanner by remember { mutableStateOf(false) }

    val cash = cashInput.toDoubleOrNull() ?: 0.0
    val bank = bankInput.toDoubleOrNull() ?: 0.0
    val goldGrams = goldGramsInput.toDoubleOrNull() ?: 0.0
    val silverGrams = silverGramsInput.toDoubleOrNull() ?: 0.0
    val business = businessInput.toDoubleOrNull() ?: 0.0
    val other = otherAssetsInput.toDoubleOrNull() ?: 0.0
    val liabilities = liabilitiesInput.toDoubleOrNull() ?: 0.0

    val goldRate = goldRateInput.toDoubleOrNull() ?: prefs.goldPricePerGramBdt
    val silverRate = silverRateInput.toDoubleOrNull() ?: prefs.silverPricePerGramBdt

    val goldValue = goldGrams * goldRate
    val silverValue = silverGrams * silverRate
    val grossAssets = cash + bank + goldValue + silverValue + business + other
    val netZakatable = (grossAssets - liabilities).coerceAtLeast(0.0)

    // Standard Nisab: 595 grams Silver (52.5 tola/vori) or 85 grams Gold (7.5 tola/vori)
    val nisabThreshold = if (useSilverNisab) 595.0 * silverRate else 85.0 * goldRate
    val isZakatDue = netZakatable >= nisabThreshold && netZakatable > 0.0
    val zakatAmount = if (isZakatDue) netZakatable * 0.025 else 0.0

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "যাকাত ক্যালকুলেটর",
                subtitle = "নিসাব ও ২.৫% যাকাতের নিখুঁত হিসাব",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Result Banner
            item {
                NoorBrandGradientBanner {
                    Text(
                        text = if (isZakatDue) "আপনার প্রদেয় যাকাত (২.৫%)" else "নিসাব সীমা পূর্ণ হয়নি",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "৳ ${BengaliFormatter.formatDecimal(zakatAmount, 2)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "নিট যাকাতযোগ্য সম্পদ: ৳ ${BengaliFormatter.formatDecimal(netZakatable, 0)} | নিসাব: ৳ ${BengaliFormatter.formatDecimal(nisabThreshold, 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            // Nisab & Metal Market Rate
            item {
                NoorElevatedCard {
                    Text("নিসাব ও স্বর্ণ-রূপার বাজারদর (প্রতি গ্রাম ৳)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = useSilverNisab,
                            onClick = { useSilverNisab = true },
                            label = { Text("রূপার নিসাব (৫৯৫ গ্রাম)") }
                        )
                        FilterChip(
                            selected = !useSilverNisab,
                            onClick = { useSilverNisab = false },
                            label = { Text("স্বর্ণের নিসাব (৮৫ গ্রাম)") }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goldRateInput,
                            onValueChange = { goldRateInput = it },
                            label = { Text("স্বর্ণ (প্রতি গ্রাম ৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = silverRateInput,
                            onValueChange = { silverRateInput = it },
                            label = { Text("রূপা (প্রতি গ্রাম ৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.setZakatMetalRates(goldRate, silverRate)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("বাজারদর সংরক্ষণ করুন")
                    }
                }
            }

            // Asset Inputs
            item {
                NoorElevatedCard {
                    Text("সম্পদ ও দায়ের হিসাব", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    ZakatNumberField("নগদ টাকা (৳)", cashInput) { cashInput = it }
                    ZakatNumberField("ব্যাংক সঞ্চয় ও এফডিআর (৳)", bankInput) { bankInput = it }
                    ZakatNumberField("স্বর্ণের পরিমাণ (গ্রাম — ১ ভরি ≈ ১১.৬৬ গ্রাম)", goldGramsInput) { goldGramsInput = it }
                    ZakatNumberField("রূপার পরিমাণ (গ্রাম)", silverGramsInput) { silverGramsInput = it }
                    ZakatNumberField("ব্যবসায়িক পণ্য ও শেয়ার (৳)", businessInput) { businessInput = it }
                    ZakatNumberField("অন্যান্য যাকাতযোগ্য সম্পদ / পাওনা (৳)", otherAssetsInput) { otherAssetsInput = it }
                    ZakatNumberField("বিয়োগযোগ্য ঋণ ও বকেয়া দায় (৳)", liabilitiesInput) { liabilitiesInput = it }
                }
            }

            // Breakdown & Save
            item {
                NoorElevatedCard {
                    Text("বিস্তারিত হিসাব", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• মোট স্থূল সম্পদ: ৳ ${BengaliFormatter.formatDecimal(grossAssets, 0)}")
                    Text("• বিয়োগযোগ্য দায়: ৳ ${BengaliFormatter.formatDecimal(liabilities, 0)}")
                    Text("• নিট যাকাতযোগ্য সম্পদ: ৳ ${BengaliFormatter.formatDecimal(netZakatable, 0)}")
                    Text("• যাকাতের পরিমাণ (২.৫%): ৳ ${BengaliFormatter.formatDecimal(zakatAmount, 2)}", fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val summary = "নগদ: ৳${cash.toLong()}, ব্যাংক: ৳${bank.toLong()}, স্বর্ণ: ${goldGrams}g, রূপা: ${silverGrams}g, ব্যবসা: ৳${business.toLong()}, ঋণ: ৳${liabilities.toLong()}\nনিট সম্পদ: ৳${netZakatable.toLong()}\nপ্রদেয় যাকাত: ৳${zakatAmount.toLong()}"
                            viewModel.saveNote(title = "যাকাতের হিসাব", content = summary, category = "যাকাত")
                            savedBanner = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_zakat_note")
                    ) {
                        Icon(Icons.Filled.BookmarkAdded, null)
                        Spacer(Modifier.width(6.dp))
                        Text("ব্যক্তিগত নোটে হিসাব সংরক্ষণ করুন")
                    }
                    if (savedBanner) {
                        Spacer(Modifier.height(6.dp))
                        Text("হিসাবটি ব্যক্তিগত নোটে সংরক্ষিত হয়েছে।", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                    }
                }
            }

            // Scholar Disclaimer
            item {
                Surface(
                    color = DeepRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, null, tint = DeepRed)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "সতর্কতা: এটি সাধারণ যাকাত হিসাবের সহায়ক ক্যালকুলেটর। যৌথ ব্যবসা, কৃষি ফসল (উশর), প্রভিডেন্ট ফান্ড বা জটিল মাসয়ালার ক্ষেত্রে অবশ্যই অভিজ্ঞ আলেম বা মুফতির পরামর্শ নিন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepRed
                        )
                    }
                }
            }

            // Respectful Banner Ad (utility screen only)
            item {
                NoorRespectfulBannerAd(isOnline = isOnline)
            }
        }
    }
}

@Composable
private fun ZakatNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}
