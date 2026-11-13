package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.content.IslamicKnowledgeData
import com.example.domain.prayer.PrayerWaqt
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.BengaliFormatter
import com.example.navigation.NoorRoute
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.ContentActionRow
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.sharePlainText
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.DeepRed
import com.example.ui.theme.RedAccent
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
import com.example.viewmodel.NoorViewModel
import java.util.Calendar

private data class QuickShortcut(
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val accent: Color,
    val route: NoorRoute
)

@Composable
fun HomeScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val liveStatus by viewModel.livePrayerStatus.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val dailyAyah by viewModel.dailyAyah.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val recents by viewModel.recentItems.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val dailyHadith = IslamicKnowledgeData.curatedHadith[dayIndex % IslamicKnowledgeData.curatedHadith.size]
    val dailyReminder = IslamicKnowledgeData.dailyRemindersBn[dayIndex % IslamicKnowledgeData.dailyRemindersBn.size]

    val shortcuts = listOf(
        QuickShortcut("কুরআন", "Quran", Icons.Filled.MenuBook, RoyalBlue, NoorRoute.QuranHome),
        QuickShortcut("কিবলা", "Qibla", Icons.Filled.Explore, DeepRed, NoorRoute.QiblaCompass),
        QuickShortcut("তাসবিহ", "Tasbih", Icons.Filled.RadioButtonChecked, RoyalBlue, NoorRoute.DigitalTasbih),
        QuickShortcut("দোয়া", "Duas", Icons.Filled.VolunteerActivism, DeepRed, NoorRoute.DuaLibrary),
        QuickShortcut("৯৯ নাম", "99 Names", Icons.Filled.AutoAwesome, RoyalBlue, NoorRoute.NamesOfAllah),
        QuickShortcut("নামাজ শিক্ষা", "Salah", Icons.Filled.Mosque, DeepRed, NoorRoute.NamazEducation),
        QuickShortcut("রমজান", "Ramadan", Icons.Filled.NightsStay, RoyalBlue, NoorRoute.RamadanModule),
        QuickShortcut("যাকাত", "Zakat", Icons.Filled.Calculate, DeepRed, NoorRoute.ZakatCalculator)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Brand Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_noor_emblem),
                            contentDescription = "নূর লাইফ প্রতীক",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOOR ",
                                style = MaterialTheme.typography.titleLarge,
                                color = RoyalBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "LIFE",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• নূর লাইফ",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { onNavigate(NoorRoute.GlobalSearch) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = strings.globalSearch)
                    }
                    IconButton(
                        onClick = { onNavigate(NoorRoute.Bookmarks) },
                        modifier = Modifier.testTag("home_bookmarks_button")
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = strings.bookmarksAndFavorites)
                    }
                }
            }
        }

        item {
            OfflineStatusBanner(isOnline = isOnline, message = strings.offlineBanner)
        }

        // Hero Prayer Dashboard Card
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(RoyalBlueDark, RoyalBlue, DeepRed)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    // Date & Location Chip Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prefs.language == AppLanguage.BENGALI) {
                                    liveStatus.gregorianDateBn
                                } else {
                                    liveStatus.gregorianDateEn
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            val hijriText = if (prefs.language == AppLanguage.BENGALI) {
                                "${BengaliFormatter.formatNumber(liveStatus.hijriDate.day)} ${liveStatus.hijriDate.monthNameBn} ${BengaliFormatter.formatNumber(liveStatus.hijriDate.year)} হিজরি • ${BengaliFormatter.formatNumber(liveStatus.banglaDate.day)} ${liveStatus.banglaDate.monthNameBn}"
                            } else {
                                "${liveStatus.hijriDate.day} ${liveStatus.hijriDate.monthNameEn} ${liveStatus.hijriDate.year} AH"
                            }
                            Text(
                                text = hijriText,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }

                        Surface(
                            onClick = { onNavigate(NoorRoute.PrayerHub) },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "অবস্থান",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = prefs.city.displayName(prefs.language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Prayer & Next Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${strings.currentPrayer}: ${liveStatus.currentWaqt.localizedName(strings)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${strings.nextPrayer}: ${liveStatus.nextSlot.waqt.localizedName(strings)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = BengaliFormatter.formatTime12Hour(
                                    liveStatus.nextSlot.hour24,
                                    liveStatus.nextSlot.minute,
                                    prefs.language
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = strings.timeRemaining,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = BengaliFormatter.formatCountdown(
                                        liveStatus.countdownSeconds,
                                        prefs.language
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sunrise, Sunset, Sehri Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val sched = liveStatus.todaySchedule
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbSunny, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${strings.sunrise}: ${BengaliFormatter.formatCompactTime(sched.sunrise.hour24, sched.sunrise.minute, prefs.language)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbTwilight, null, tint = Color(0xFFFFAB91), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${strings.iftarTime}: ${BengaliFormatter.formatCompactTime(sched.maghrib.hour24, sched.maghrib.minute, prefs.language)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Five Daily Prayer Times Row
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(
                    title = if (prefs.language == AppLanguage.BENGALI) "আজকের নামাজের সময়সূচি" else "Today's Prayer Times",
                    icon = Icons.Filled.AccessTime,
                    actionText = if (prefs.language == AppLanguage.BENGALI) "সম্পূর্ণ সূচি" else "Full Schedule",
                    onActionClick = { onNavigate(NoorRoute.PrayerHub) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    liveStatus.todaySchedule.fiveWaqtSlots.forEach { slot ->
                        val isNext = slot.waqt == liveStatus.nextSlot.waqt
                        val isCurrent = slot.waqt == liveStatus.currentWaqt
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(NoorRoute.PrayerHub) },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isNext -> RoyalBlue
                                isCurrent -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = slot.waqt.localizedName(strings),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = BengaliFormatter.toBengaliDigits(
                                        "%d:%02d".format(
                                            if (slot.hour24 % 12 == 0) 12 else slot.hour24 % 12,
                                            slot.minute
                                        )
                                    ).let {
                                        if (prefs.language == AppLanguage.BENGALI) it else "%d:%02d".format(
                                            if (slot.hour24 % 12 == 0) 12 else slot.hour24 % 12,
                                            slot.minute
                                        )
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) Color.White else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isNext) {
                                        if (prefs.language == AppLanguage.BENGALI) "পরবর্তী" else "Next"
                                    } else if (isCurrent) {
                                        if (prefs.language == AppLanguage.BENGALI) "চলমান" else "Now"
                                    } else {
                                        if (slot.hour24 < 12) "পূর্বাহ্ণ" else "অপরাহ্ণ"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isNext) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Access Grid (2 rows of 4)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = strings.quickAccess, icon = Icons.Filled.AutoAwesome)
                Spacer(modifier = Modifier.height(8.dp))
                shortcuts.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { sc ->
                            Card(
                                onClick = { onNavigate(sc.route) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shortcut_${sc.titleEn.lowercase()}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(sc.accent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = sc.icon,
                                            contentDescription = sc.titleBn,
                                            tint = sc.accent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (prefs.language == AppLanguage.BENGALI) sc.titleBn else sc.titleEn,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Ayah Card
        dailyAyah?.let { (surah, ayah) ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(title = strings.dailyAyah, icon = Icons.Filled.MenuBook)
                    Spacer(modifier = Modifier.height(6.dp))
                    val bmId = "ayah:${surah.number}:${ayah.numberInSurah}"
                    val isBookmarked = bookmarks.any { it.id == bmId }
                    NoorElevatedCard {
                        Text(
                            text = "${surah.nameBengali} • আয়াত ${BengaliFormatter.formatNumber(ayah.numberInSurah)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ArabicRtlText(text = ayah.arabicText, fontSizeSp = 22f)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ayah.bengaliTranslation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        ContentActionRow(
                            isBookmarked = isBookmarked,
                            onToggleBookmark = {
                                viewModel.toggleBookmark(
                                    id = bmId,
                                    type = "AYAH",
                                    title = "${surah.nameBengali} : ${BengaliFormatter.formatNumber(ayah.numberInSurah)}",
                                    subtitle = ayah.bengaliTranslation,
                                    arabicText = ayah.arabicText,
                                    routeHint = "surah:${surah.number}:${ayah.numberInSurah}"
                                )
                            },
                            onCopy = {
                                copyTextToClipboard(
                                    context,
                                    "আয়াত",
                                    "${ayah.arabicText}\n${ayah.bengaliTranslation}\n(${surah.nameBengali}: ${ayah.numberInSurah})"
                                )
                            },
                            onShare = {
                                sharePlainText(
                                    context,
                                    "আজকের আয়াত — ${surah.nameBengali}",
                                    "${ayah.arabicText}\n\n${ayah.bengaliTranslation}\n(${surah.nameBengali}, আয়াত ${ayah.numberInSurah})"
                                )
                            }
                        )
                    }
                }
            }
        }

        // Daily Hadith Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(
                    title = strings.dailyHadith,
                    icon = Icons.Filled.Favorite,
                    actionText = if (prefs.language == AppLanguage.BENGALI) "আরও হাদিস" else "More",
                    onActionClick = { onNavigate(NoorRoute.HadithKnowledge) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                NoorElevatedCard(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    if (dailyHadith.arabic.isNotBlank()) {
                        ArabicRtlText(text = dailyHadith.arabic, fontSizeSp = 20f)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = dailyHadith.bengaliText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "সূত্র: ${dailyHadith.referenceBn}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Daily Islamic Reminder
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = strings.dailyReminder, icon = Icons.Filled.CalendarMonth)
                Spacer(modifier = Modifier.height(6.dp))
                NoorElevatedCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = dailyReminder,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Recently Viewed
        if (recents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = if (prefs.language == AppLanguage.BENGALI) "সম্প্রতি দেখা হয়েছে" else "Recently Viewed"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recents) { item ->
                        Surface(
                            onClick = {
                                if (item.routeHint.startsWith("surah:")) {
                                    val num = item.routeHint.removePrefix("surah:").toIntOrNull() ?: 1
                                    onNavigate(NoorRoute.SurahReader(num))
                                } else {
                                    onNavigate(NoorRoute.ToolsHub)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(180.dp)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
