package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioRepeatMode
import com.example.data.content.QuranCatalogMetadata
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.BengaliFormatter
import com.example.navigation.NoorRoute
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorBrandGradientBanner
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.sharePlainText
import com.example.ui.theme.ArabicQuranFont
import com.example.ui.theme.DeepRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel

@Composable
fun QuranHomeScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit
) {
    val surahs by viewModel.surahSummaries.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val downloadedAudio by viewModel.downloadedAudio.collectAsStateWithLifecycle()
    val downloadingSurahs by viewModel.audioDownloadingSurahs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSurahs = remember(surahs, searchQuery) {
        if (searchQuery.isBlank()) surahs else {
            val q = searchQuery.trim().lowercase()
            surahs.filter {
                it.nameBengali.lowercase().contains(q) ||
                    it.meaningBengali.lowercase().contains(q) ||
                    it.nameEnglish.lowercase().contains(q) ||
                    it.number.toString() == q
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (prefs.language == AppLanguage.BENGALI) "পবিত্র আল-কুরআন" else "The Holy Quran",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (prefs.language == AppLanguage.BENGALI) {
                            "১১৪টি সূরা ও ৩০ পারা • সম্পূর্ণ অফলাইনে সংরক্ষিত"
                        } else {
                            "114 Surahs & 30 Juz • Available Offline"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSuccess.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("অফলাইন প্রস্তুত", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resume Last Read Banner
            val lastSurah = surahs.firstOrNull { it.number == prefs.lastReadSurah }
            if (lastSurah != null) {
                NoorBrandGradientBanner(
                    modifier = Modifier.clickable {
                        onNavigate(NoorRoute.SurahReader(lastSurah.number, prefs.lastReadAyah))
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সর্বশেষ পঠিত",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "${lastSurah.nameBengali} (${lastSurah.nameArabic})",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "আয়াত ${BengaliFormatter.formatNumber(prefs.lastReadAyah)} থেকে পুনরায় পড়ুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = "পড়ুন",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoyalBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("সূরার নাম বা নম্বর লিখুন (যেমন: ইয়াসিন, ১১২)...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "অনুসন্ধান") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input")
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("${strings.surahList} (১১৪)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("${strings.juzList} (৩০)") }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val isAudioDownloaded = downloadedAudio.any {
                        it.surahNumber == surah.number && it.reciterId == prefs.selectedReciterId
                    }
                    val isDownloading = downloadingSurahs.contains(surah.number)
                    Surface(
                        onClick = { onNavigate(NoorRoute.SurahReader(surah.number)) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.testTag("surah_item_${surah.number}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = BengaliFormatter.formatNumber(surah.number, prefs.language),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surah.nameBengali,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${surah.meaningBengali} • ${surah.revelationTypeBn} • ${BengaliFormatter.formatNumber(surah.ayahCount, prefs.language)} আয়াত",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = surah.nameArabic,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ArabicQuranFont),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else if (isAudioDownloaded) {
                                    Icon(
                                        Icons.Filled.CloudDone,
                                        contentDescription = "অডিও ডাউনলোডকৃত",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(QuranCatalogMetadata.juzList, key = { it.number }) { juz ->
                    Surface(
                        onClick = { onNavigate(NoorRoute.SurahReader(juz.startSurah, juz.startAyah)) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = BengaliFormatter.formatNumber(juz.number, prefs.language),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = juz.nameBengali,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val startName = QuranCatalogMetadata.surahBnMap[juz.startSurah]?.first ?: ""
                                val endName = QuranCatalogMetadata.surahBnMap[juz.endSurah]?.first ?: ""
                                Text(
                                    text = "$startName (${juz.startAyah}) — $endName (${juz.endAyah})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = juz.nameArabic,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ArabicQuranFont),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    surahNumber: Int,
    targetAyah: Int,
    viewModel: NoorViewModel,
    strings: AppStrings,
    onBack: () -> Unit
) {
    val detail by viewModel.currentSurahDetail.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val tafsirMap by viewModel.currentSurahTafsir.collectAsStateWithLifecycle()
    val tafsirLoading by viewModel.tafsirLoading.collectAsStateWithLifecycle()
    val tafsirError by viewModel.tafsirError.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val downloadedAudio by viewModel.downloadedAudio.collectAsStateWithLifecycle()
    val downloadingSurahs by viewModel.audioDownloadingSurahs.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showSettingsSheet by remember { mutableStateOf(false) }
    var expandedTafsirAyahs by remember { mutableStateOf(setOf<Int>()) }
    var tafsirSearchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(surahNumber) {
        viewModel.openSurah(surahNumber)
    }

    LaunchedEffect(detail, targetAyah) {
        if (detail != null && targetAyah > 1) {
            val index = (targetAyah).coerceAtMost(detail!!.ayahs.size)
            listState.scrollToItem(index)
        }
    }

    val isCurrentSurahAudioPlaying =
        audioState.currentSurahNumber == surahNumber && audioState.isPlaying
    val isCurrentSurahDownloaded = downloadedAudio.any {
        it.surahNumber == surahNumber && it.reciterId == prefs.selectedReciterId
    }
    val isDownloading = downloadingSurahs.contains(surahNumber)

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = detail?.summary?.nameBengali ?: "সূরা $surahNumber",
                subtitle = detail?.let {
                    "${it.summary.meaningBengali} • ${it.summary.revelationTypeBn} • ${BengaliFormatter.formatNumber(it.summary.ayahCount)} আয়াত"
                },
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.downloadSurahAudio(surahNumber) },
                        enabled = !isDownloading
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (isCurrentSurahDownloaded) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                                contentDescription = "অফলাইন অডিও ডাউনলোড",
                                tint = if (isCurrentSurahDownloaded) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "পাঠ সেটিংস")
                    }
                }
            )
        },
        bottomBar = {
            // Dedicated Audio Recitation Controller Bar (zero ads!)
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    if (audioState.errorMessage != null) {
                        Text(
                            text = audioState.errorMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (audioState.currentSurahNumber != null && audioState.durationMs > 0L) {
                        Slider(
                            value = audioState.positionMs.toFloat(),
                            onValueChange = { viewModel.audioController.seekTo(it.toLong()) },
                            valueRange = 0f..audioState.durationMs.toFloat().coerceAtLeast(1f),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val reciter = QuranCatalogMetadata.reciters.firstOrNull { it.id == prefs.selectedReciterId }
                            Text(
                                text = audioState.currentSurahTitle.ifBlank { detail?.summary?.nameBengali ?: "কুরআন তিলাওয়াত" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = (reciter?.nameBn ?: "ক্বারী") + if (audioState.isOfflinePlayback) " • অফলাইন" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.audioController.playPreviousSurah(prefs.selectedReciterId) }) {
                                Icon(Icons.Filled.SkipPrevious, contentDescription = "পূর্ববর্তী সূরা")
                            }
                            Surface(
                                onClick = {
                                    if (audioState.currentSurahNumber == surahNumber) {
                                        viewModel.audioController.togglePlayPause()
                                    } else {
                                        viewModel.audioController.playSurah(
                                            surahNumber,
                                            detail?.summary?.nameBengali ?: "সূরা $surahNumber",
                                            prefs.selectedReciterId
                                        )
                                    }
                                },
                                shape = CircleShape,
                                color = RoyalBlue,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (audioState.isBuffering && audioState.currentSurahNumber == surahNumber) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isCurrentSurahAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "চালু বা বিরতি",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.audioController.playNextSurah(prefs.selectedReciterId) }) {
                                Icon(Icons.Filled.SkipNext, contentDescription = "পরবর্তী সূরা")
                            }
                            IconButton(onClick = { viewModel.audioController.toggleRepeat() }) {
                                Icon(
                                    imageVector = if (audioState.repeatMode == AudioRepeatMode.REPEAT_ONE) {
                                        Icons.Filled.RepeatOne
                                    } else {
                                        Icons.Filled.Repeat
                                    },
                                    contentDescription = "পুনরাবৃত্তি",
                                    tint = if (audioState.repeatMode == AudioRepeatMode.REPEAT_ONE) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val current = detail
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Surah Bismillah & Tafsir Search Header
                item {
                    NoorBrandGradientBanner {
                        Text(
                            text = current.summary.nameArabic,
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = ArabicQuranFont),
                            color = Color.White
                        )
                        Text(
                            text = "${current.summary.nameBengali} — ${current.summary.meaningBengali}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (surahNumber != 1 && surahNumber != 9) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ArabicQuranFont),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tafsirSearchQuery,
                        onValueChange = { tafsirSearchQuery = it },
                        placeholder = { Text("এই সূরার আয়াত বা তাফসিরে খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (tafsirLoading) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "তাফসির ইবনে কাসীর (বাংলা) লোড হচ্ছে...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (tafsirError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tafsirError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val filteredAyahs = if (tafsirSearchQuery.isBlank()) {
                    current.ayahs
                } else {
                    val q = tafsirSearchQuery.trim().lowercase()
                    current.ayahs.filter { a ->
                        val tText = tafsirMap[a.numberInSurah]?.tafsirText?.lowercase() ?: ""
                        a.bengaliTranslation.lowercase().contains(q) ||
                            a.arabicText.contains(q) ||
                            tText.contains(q)
                    }
                }

                items(filteredAyahs, key = { it.globalNumber }) { ayah ->
                    val bmId = "ayah:${surahNumber}:${ayah.numberInSurah}"
                    val isBookmarked = bookmarks.any { it.id == bmId }
                    val tafsirItem = tafsirMap[ayah.numberInSurah]
                    val isTafsirExpanded = expandedTafsirAyahs.contains(ayah.numberInSurah)

                    NoorElevatedCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "আয়াত ${BengaliFormatter.formatNumber(ayah.numberInSurah)} • পারা ${BengaliFormatter.formatNumber(ayah.juz)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.updateLastReadAyah(surahNumber, ayah.numberInSurah)
                                        viewModel.audioController.playAyah(
                                            surahNumber,
                                            current.summary.nameBengali,
                                            ayah.numberInSurah,
                                            ayah.globalNumber,
                                            prefs.selectedReciterId
                                        )
                                    }
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "এই আয়াত শুনুন", tint = RoyalBlue)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.toggleBookmark(
                                            id = bmId,
                                            type = "AYAH",
                                            title = "${current.summary.nameBengali} : ${BengaliFormatter.formatNumber(ayah.numberInSurah)}",
                                            subtitle = ayah.bengaliTranslation,
                                            arabicText = ayah.arabicText,
                                            routeHint = "surah:$surahNumber:${ayah.numberInSurah}"
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "বুকমার্ক",
                                        tint = if (isBookmarked) DeepRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        copyTextToClipboard(
                                            context,
                                            "কুরআনের আয়াত",
                                            "${ayah.arabicText}\nউচ্চারণ: ${ayah.bengaliPronunciation}\nঅর্থ: ${ayah.bengaliTranslation}\n(${current.summary.nameBengali}: ${ayah.numberInSurah})"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "কপি")
                                }
                                IconButton(
                                    onClick = {
                                        sharePlainText(
                                            context,
                                            "${current.summary.nameBengali} - আয়াত ${ayah.numberInSurah}",
                                            "${ayah.arabicText}\n\n${ayah.bengaliTranslation}\n(${current.summary.nameBengali}, আয়াত ${ayah.numberInSurah})"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "শেয়ার")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        ArabicRtlText(
                            text = ayah.arabicText,
                            fontSizeSp = prefs.arabicFontSize
                        )

                        if (prefs.showBengaliPronunciation && ayah.bengaliPronunciation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "উচ্চারণ: ${ayah.bengaliPronunciation}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (prefs.bengaliFontSize - 1).sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ayah.bengaliTranslation,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = prefs.bengaliFontSize.sp)
                        )

                        if (prefs.showEnglishTranslation) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ayah.englishTranslation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (tafsirItem != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedTafsirAyahs = if (isTafsirExpanded) {
                                            expandedTafsirAyahs - ayah.numberInSurah
                                        } else {
                                            expandedTafsirAyahs + ayah.numberInSurah
                                        }
                                    }
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTafsirExpanded) "তাফসির লুকান" else "তাফসির পড়ুন (${tafsirItem.sourceName})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DeepRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = if (isTafsirExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "তাফসির টগল",
                                    tint = DeepRed
                                )
                            }
                            AnimatedVisibility(visible = isTafsirExpanded) {
                                Text(
                                    text = tafsirItem.tafsirText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("কুরআন পাঠ ও তিলাওয়াত সেটিংস", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text("আরবি ফন্ট সাইজ: ${BengaliFormatter.formatNumber(prefs.arabicFontSize.toInt())}")
                Slider(
                    value = prefs.arabicFontSize,
                    onValueChange = {
                        viewModel.setQuranReadingSettings(
                            it,
                            prefs.bengaliFontSize,
                            prefs.showBengaliPronunciation,
                            prefs.showEnglishTranslation
                        )
                    },
                    valueRange = 18f..38f
                )

                Text("বাংলা ফন্ট সাইজ: ${BengaliFormatter.formatNumber(prefs.bengaliFontSize.toInt())}")
                Slider(
                    value = prefs.bengaliFontSize,
                    onValueChange = {
                        viewModel.setQuranReadingSettings(
                            prefs.arabicFontSize,
                            it,
                            prefs.showBengaliPronunciation,
                            prefs.showEnglishTranslation
                        )
                    },
                    valueRange = 13f..24f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("বাংলা উচ্চারণ দেখান")
                    Switch(
                        checked = prefs.showBengaliPronunciation,
                        onCheckedChange = {
                            viewModel.setQuranReadingSettings(
                                prefs.arabicFontSize,
                                prefs.bengaliFontSize,
                                it,
                                prefs.showEnglishTranslation
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ইংরেজি অনুবাদ দেখান (ঐচ্ছিক)")
                    Switch(
                        checked = prefs.showEnglishTranslation,
                        onCheckedChange = {
                            viewModel.setQuranReadingSettings(
                                prefs.arabicFontSize,
                                prefs.bengaliFontSize,
                                prefs.showBengaliPronunciation,
                                it
                            )
                        }
                    )
                }

                Text("ক্বারী নির্বাচন করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                QuranCatalogMetadata.reciters.forEach { rec ->
                    FilterChip(
                        selected = prefs.selectedReciterId == rec.id,
                        onClick = { viewModel.setSelectedReciter(rec.id) },
                        label = { Text("${rec.nameBn} — ${rec.styleBn}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { showSettingsSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.close)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
