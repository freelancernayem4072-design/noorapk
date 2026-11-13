package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.NoorRespectfulBannerAd
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.BengaliFormatter
import com.example.navigation.NoorRoute
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.theme.DeepRed
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel

private data class ToolTile(
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val color: Color,
    val route: NoorRoute
)

@Composable
fun ToolsHubScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val tools = remember {
        listOf(
            ToolTile("ডিজিটাল তাসবিহ", "Digital Tasbih", "জিকির গণনা ও দৈনিক রেকর্ড", Icons.Filled.RadioButtonChecked, RoyalBlue, NoorRoute.DigitalTasbih),
            ToolTile("কিবলা কম্পাস", "Qibla Compass", "কাবা শরীফের দিক ও দূরত্ব", Icons.Filled.Explore, DeepRed, NoorRoute.QiblaCompass),
            ToolTile("আল্লাহর ৯৯ নাম", "99 Names", "অর্থ ও তাৎপর্যসহ আসমাউল হুসনা", Icons.Filled.AutoAwesome, RoyalBlue, NoorRoute.NamesOfAllah),
            ToolTile("দোয়া ও জিকির", "Dua Library", "১৫টি ক্যাটাগরির প্রামাণিক দোয়া", Icons.Filled.VolunteerActivism, DeepRed, NoorRoute.DuaLibrary),
            ToolTile("পাঁচ কালিমা", "Five Kalimas", "আরবি, উচ্চারণ ও বাংলা অর্থ", Icons.Filled.Book, RoyalBlue, NoorRoute.FiveKalimas),
            ToolTile("পূর্ণাঙ্গ নামাজ শিক্ষা", "Salah Guide", "রাকাত, তাসবিহ ও ধাপে ধাপে নিয়ম", Icons.Filled.Mosque, DeepRed, NoorRoute.NamazEducation),
            ToolTile("অজু ও গোসল", "Wudu & Ghusl", "ফরজ, সুন্নত ও পবিত্রতার নিয়ম", Icons.Filled.WaterDrop, RoyalBlue, NoorRoute.WuduGhusl),
            ToolTile("ইসলামিক ক্যালেন্ডার", "Calendar", "হিজরি, বাংলা ও ইংরেজি তারিখ", Icons.Filled.CalendarMonth, DeepRed, NoorRoute.IslamicCalendar),
            ToolTile("রমজান ও রোজা", "Ramadan", "সাহরি-ইফতার ও রোজা ট্র্যাকার", Icons.Filled.NightsStay, RoyalBlue, NoorRoute.RamadanModule),
            ToolTile("যাকাত ক্যালকুলেটর", "Zakat Calculator", "নিসাব ও ২.৫% যাকাত হিসাব", Icons.Filled.Calculate, DeepRed, NoorRoute.ZakatCalculator),
            ToolTile("হাদিস ও ইসলামি জ্ঞান", "Hadith & Knowledge", "সহীহ হাদিস, নবী কাহিনী ও প্রশ্নোত্তর", Icons.Filled.MenuBook, RoyalBlue, NoorRoute.HadithKnowledge),
            ToolTile("ইসলামিক কুইজ", "Islamic Quiz", "জ্ঞান যাচাই ও ব্যাখ্যাসহ উত্তর", Icons.Filled.Quiz, DeepRed, NoorRoute.IslamicQuiz),
            ToolTile("শুভেচ্ছা ও দোয়া কার্ড", "Greeting Cards", "জুম্মা, ঈদ ও দোয়ার শেয়ারযোগ্য কার্ড", Icons.Filled.CardGiftcard, RoyalBlue, NoorRoute.GreetingCards),
            ToolTile("ইসলামিক ওয়ালপেপার", "Wallpapers", "জ্যামিতিক ইসলামিক ওয়ালপেপার", Icons.Filled.Wallpaper, DeepRed, NoorRoute.Wallpapers),
            ToolTile("ব্যক্তিগত নোট", "Personal Notes", "ইসলামিক নোট ও যাকাত হিসাব", Icons.Filled.EditNote, RoyalBlue, NoorRoute.PersonalNotes),
            ToolTile("বুকমার্ক ও প্রিয়", "Bookmarks", "সংরক্ষিত আয়াত, দোয়া ও হাদিস", Icons.Filled.Bookmark, DeepRed, NoorRoute.Bookmarks)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (prefs.language == AppLanguage.BENGALI) "ইসলামিক টুলস ও শিক্ষা" else "Islamic Tools & Guides",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "দৈনন্দিন ইবাদত ও জ্ঞানার্জনের সকল মাধ্যম",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onNavigate(NoorRoute.GlobalSearch) }) {
                    Icon(Icons.Filled.Search, contentDescription = strings.globalSearch)
                }
            }
        }

        items(tools.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { tile ->
                    Surface(
                        onClick = { onNavigate(tile.route) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tool_${tile.titleEn.lowercase().replace(" ", "_")}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(tile.color.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(tile.icon, contentDescription = tile.titleBn, tint = tile.color)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (prefs.language == AppLanguage.BENGALI) tile.titleBn else tile.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tile.subtitleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            NoorRespectfulBannerAd(isOnline = isOnline)
        }
    }
}

@Composable
fun PersonalNotesScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.personalNotes.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf(0L) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ব্যক্তিগত নোট",
                subtitle = "ইসলামিক শিক্ষা, তাফসির ও ব্যক্তিগত নোট",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingId = 0L
                    titleInput = ""
                    contentInput = ""
                    showEditor = true
                },
                containerColor = RoyalBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন নোট")
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "এখনো কোনো নোট সংরক্ষিত নেই। নিচের '+' বাটনে ট্যাপ করে আপনার প্রথম নোট লিখুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoorElevatedCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.category, style = MaterialTheme.typography.labelSmall, color = DeepRed, fontWeight = FontWeight.Bold)
                                Text(
                                    note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        editingId = note.id
                                        titleInput = note.title
                                        contentInput = note.content
                                        showEditor = true
                                    }
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "নোট মুছুন", tint = DeepRed)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(note.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingId == 0L) "নতুন নোট লিখুন" else "নোট সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("শিরোনাম") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("বিস্তারিত লিখুন") },
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveNote(editingId, titleInput, contentInput)
                    showEditor = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun GlobalSearchScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit,
    onBack: () -> Unit
) {
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = strings.globalSearch,
                subtitle = "কুরআন, আয়াত, দোয়া, হাদিস, ৯৯ নাম ও শিক্ষা",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.performGlobalSearch(it)
                },
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (results.surahs.isNotEmpty()) {
                    item { Text("সূরাসমূহ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue) }
                    items(results.surahs) { s ->
                        Surface(
                            onClick = { onNavigate(NoorRoute.SurahReader(s.number)) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${BengaliFormatter.formatNumber(s.number)}. ${s.nameBengali} (${s.meaningBengali})", fontWeight = FontWeight.Bold)
                                Text(s.nameArabic)
                            }
                        }
                    }
                }
                if (results.ayahs.isNotEmpty()) {
                    item { Text("কুরআনের আয়াত", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue) }
                    items(results.ayahs) { (s, a) ->
                        NoorElevatedCard(modifier = Modifier.clickable { onNavigate(NoorRoute.SurahReader(s.number, a.numberInSurah)) }) {
                            Text("${s.nameBengali} : ${BengaliFormatter.formatNumber(a.numberInSurah)}", style = MaterialTheme.typography.labelLarge, color = RoyalBlue)
                            ArabicRtlText(text = a.arabicText, fontSizeSp = 20f)
                            Text(a.bengaliTranslation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (results.duas.isNotEmpty()) {
                    item { Text("দোয়া", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepRed) }
                    items(results.duas) { d ->
                        NoorElevatedCard(modifier = Modifier.clickable { onNavigate(NoorRoute.DuaLibrary) }) {
                            Text(d.titleBn, fontWeight = FontWeight.Bold)
                            Text(d.meaningBn, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (results.hadiths.isNotEmpty()) {
                    item { Text("হাদিস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepRed) }
                    items(results.hadiths) { h ->
                        NoorElevatedCard(modifier = Modifier.clickable { onNavigate(NoorRoute.HadithKnowledge) }) {
                            Text(h.referenceBn, style = MaterialTheme.typography.labelMedium, color = RoyalBlue)
                            Text(h.bengaliText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (results.allahNames.isNotEmpty()) {
                    item { Text("আল্লাহর ৯৯ নাম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue) }
                    items(results.allahNames) { n ->
                        NoorElevatedCard(modifier = Modifier.clickable { onNavigate(NoorRoute.NamesOfAllah) }) {
                            Text("${n.pronunciationBn} (${n.arabic}) — ${n.meaningBn}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (results.guides.isNotEmpty()) {
                    item { Text("নামাজ ও ইসলামি শিক্ষা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue) }
                    items(results.guides) { g ->
                        NoorElevatedCard(modifier = Modifier.clickable { onNavigate(NoorRoute.NamazEducation) }) {
                            Text(g.titleBn, fontWeight = FontWeight.Bold)
                            Text(g.summaryBn, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (query.isNotBlank() && !isSearching &&
                    results.surahs.isEmpty() && results.ayahs.isEmpty() &&
                    results.duas.isEmpty() && results.hadiths.isEmpty() &&
                    results.allahNames.isEmpty() && results.guides.isEmpty()
                ) {
                    item {
                        Text(strings.noResults, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksScreen(
    viewModel: NoorViewModel,
    onNavigate: (NoorRoute) -> Unit,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "বুকমার্ক ও প্রিয় তালিকা",
                subtitle = "সংরক্ষিত আয়াত, দোয়া, হাদিস ও নাম",
                onBack = onBack
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("এখনো কোনো আয়াত, দোয়া বা হাদিস বুকমার্ক করা হয়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(bookmarks, key = { it.id }) { bm ->
                    NoorElevatedCard(
                        modifier = Modifier.clickable {
                            if (bm.routeHint.startsWith("surah:")) {
                                val parts = bm.routeHint.split(":")
                                val s = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                val a = parts.getOrNull(2)?.toIntOrNull() ?: 1
                                onNavigate(NoorRoute.SurahReader(s, a))
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bm.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.toggleBookmark(bm.id, bm.type, bm.title, bm.subtitle) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "সরান", tint = DeepRed)
                            }
                        }
                        if (bm.arabicText.isNotBlank()) {
                            ArabicRtlText(text = bm.arabicText, fontSizeSp = 20f)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(bm.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
