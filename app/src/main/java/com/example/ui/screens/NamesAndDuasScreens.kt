package com.example.ui.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.DuasAndKalimasData
import com.example.data.content.NamesOfAllahData
import com.example.localization.BengaliFormatter
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.ContentActionRow
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.sharePlainText
import com.example.ui.theme.DeepRed
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel
import java.util.Locale

@Composable
fun NamesOfAllahScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var onlyFavorites by remember { mutableStateOf(false) }

    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = instance?.setLanguage(Locale("ar"))
                ttsReady = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        instance
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                tts.stop()
                tts.shutdown()
            }
        }
    }

    val filtered = remember(query, onlyFavorites, bookmarks) {
        NamesOfAllahData.names.filter { entry ->
            val bmId = "name:${entry.number}"
            val matchesFav = !onlyFavorites || bookmarks.any { it.id == bmId }
            val q = query.trim().lowercase()
            val matchesQ = q.isBlank() ||
                entry.pronunciationBn.lowercase().contains(q) ||
                entry.meaningBn.lowercase().contains(q) ||
                entry.arabic.contains(q) ||
                entry.number.toString() == q
            matchesFav && matchesQ
        }
    }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "আল্লাহর ৯৯টি গুণবাচক নাম",
                subtitle = "আসমাউল হুসনা — অর্থ ও তাৎপর্যসহ",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("নামের উচ্চারণ বা অর্থ লিখুন...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !onlyFavorites,
                        onClick = { onlyFavorites = false },
                        label = { Text("সকল নাম (৯৯)") }
                    )
                    FilterChip(
                        selected = onlyFavorites,
                        onClick = { onlyFavorites = true },
                        label = { Text("প্রিয় তালিকা") }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.number }) { entry ->
                    val bmId = "name:${entry.number}"
                    val isFav = bookmarks.any { it.id == bmId }
                    NoorElevatedCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = BengaliFormatter.formatNumber(entry.number),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(entry.pronunciationBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(entry.meaningBn, style = MaterialTheme.typography.labelLarge, color = DeepRed)
                                }
                            }
                            Row {
                                if (ttsReady) {
                                    IconButton(onClick = {
                                        tts.speak(entry.arabic, TextToSpeech.QUEUE_FLUSH, null, "name_${entry.number}")
                                    }) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = "উচ্চারণ শুনুন", tint = RoyalBlue)
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.toggleBookmark(
                                        id = bmId,
                                        type = "NAME_OF_ALLAH",
                                        title = "${entry.pronunciationBn} — ${entry.meaningBn}",
                                        subtitle = entry.explanationBn,
                                        arabicText = entry.arabic
                                    )
                                }) {
                                    Icon(
                                        if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "প্রিয়",
                                        tint = if (isFav) DeepRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        ArabicRtlText(text = entry.arabic, fontSizeSp = 28f, color = RoyalBlue)
                        Spacer(Modifier.height(6.dp))
                        Text(entry.explanationBn, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun DuaLibraryScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("সকল") }
    var searchQuery by remember { mutableStateOf("") }

    val allCategories = remember { listOf("সকল") + DuasAndKalimasData.categoriesBn }
    val filteredDuas = remember(selectedCategory, searchQuery) {
        DuasAndKalimasData.duas.filter { dua ->
            val catOk = selectedCategory == "সকল" || dua.categoryBn == selectedCategory
            val q = searchQuery.trim().lowercase()
            val qOk = q.isBlank() ||
                dua.titleBn.lowercase().contains(q) ||
                dua.meaningBn.lowercase().contains(q) ||
                dua.pronunciationBn.lowercase().contains(q)
            catOk && qOk
        }
    }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "দোয়া ও জিকির ভাণ্ডার",
                subtitle = "প্রামাণিক হাদিস ও কুরআনের দোয়া",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("দোয়ার বিষয় বা অর্থ খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allCategories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDuas, key = { it.id }) { dua ->
                    val bmId = "dua:${dua.id}"
                    val isBookmarked = bookmarks.any { it.id == bmId }
                    NoorElevatedCard {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = dua.categoryBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(dua.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        ArabicRtlText(text = dua.arabic, fontSizeSp = 23f)
                        Spacer(Modifier.height(8.dp))
                        Text("উচ্চারণ: ${dua.pronunciationBn}", style = MaterialTheme.typography.bodyMedium, color = RoyalBlue)
                        Spacer(Modifier.height(4.dp))
                        Text("অর্থ: ${dua.meaningBn}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("সূত্র: ${dua.referenceBn}", style = MaterialTheme.typography.labelMedium, color = DeepRed, fontWeight = FontWeight.SemiBold)

                        ContentActionRow(
                            isBookmarked = isBookmarked,
                            onToggleBookmark = {
                                viewModel.toggleBookmark(
                                    id = bmId,
                                    type = "DUA",
                                    title = dua.titleBn,
                                    subtitle = "${dua.meaningBn} (${dua.referenceBn})",
                                    arabicText = dua.arabic
                                )
                            },
                            onCopy = {
                                copyTextToClipboard(
                                    context,
                                    dua.titleBn,
                                    "${dua.titleBn}\n${dua.arabic}\nউচ্চারণ: ${dua.pronunciationBn}\nঅর্থ: ${dua.meaningBn}\nসূত্র: ${dua.referenceBn}"
                                )
                            },
                            onShare = {
                                sharePlainText(
                                    context,
                                    dua.titleBn,
                                    "${dua.titleBn}\n\n${dua.arabic}\n\nউচ্চারণ: ${dua.pronunciationBn}\nঅর্থ: ${dua.meaningBn}\nসূত্র: ${dua.referenceBn}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
