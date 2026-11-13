package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.IslamicKnowledgeData
import com.example.localization.BengaliFormatter
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorBrandGradientBanner
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.components.sharePlainText
import com.example.ui.theme.BgDark
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.DeepRed
import com.example.ui.theme.DeepRedDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
import com.example.viewmodel.NoorViewModel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HadithKnowledgeScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val hadithList by viewModel.hadithList.collectAsStateWithLifecycle()
    val syncMsg by viewModel.hadithSyncMessage.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "হাদিস ও ইসলামি জ্ঞান",
                subtitle = "সহীহ হাদিস, মৌলিক শিক্ষা, ইতিহাস ও প্রশ্নোত্তর",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("হাদিস") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("ইসলামি শিক্ষা") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("প্রশ্ন ও উক্তি") })
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> {
                        item {
                            NoorElevatedCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                                Text("সহীহ বুখারী থেকে অধ্যায় ডাউনলোড (অনলাইন)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = false,
                                        enabled = isOnline,
                                        onClick = { viewModel.fetchBukhariSectionOnline(8, "সালাত") },
                                        leadingIcon = { Icon(Icons.Filled.CloudDownload, null) },
                                        label = { Text("সালাত অধ্যায়") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        enabled = isOnline,
                                        onClick = { viewModel.fetchBukhariSectionOnline(24, "যাকাত") },
                                        leadingIcon = { Icon(Icons.Filled.CloudDownload, null) },
                                        label = { Text("যাকাত অধ্যায়") }
                                    )
                                }
                                if (syncMsg != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(syncMsg ?: "", style = MaterialTheme.typography.labelSmall, color = RoyalBlue)
                                }
                            }
                        }

                        items(hadithList, key = { it.id }) { h ->
                            val bmId = "hadith:${h.id}"
                            val isFav = bookmarks.any { it.id == bmId }
                            NoorElevatedCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${h.bookBn} • ${h.categoryBn}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DeepRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            viewModel.toggleBookmark(
                                                id = bmId,
                                                type = "HADITH",
                                                title = h.referenceBn,
                                                subtitle = h.bengaliText,
                                                arabicText = h.arabic
                                            )
                                        }) {
                                            Icon(
                                                if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = "বুকমার্ক",
                                                tint = if (isFav) DeepRed else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = {
                                            sharePlainText(
                                                context,
                                                h.referenceBn,
                                                "${if (h.arabic.isNotBlank()) "${h.arabic}\n\n" else ""}${h.bengaliText}\n— ${h.referenceBn}"
                                            )
                                        }) { Icon(Icons.Filled.Share, "শেয়ার") }
                                    }
                                }
                                if (h.arabic.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    ArabicRtlText(text = h.arabic, fontSizeSp = 21f)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(h.bengaliText, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(6.dp))
                                Text("সূত্র: ${h.referenceBn}", style = MaterialTheme.typography.labelMedium, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    1 -> {
                        items(IslamicKnowledgeData.knowledgeArticles, key = { it.id }) { art ->
                            NoorElevatedCard {
                                Text(art.categoryBn, style = MaterialTheme.typography.labelSmall, color = DeepRed, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(art.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(art.summaryBn, style = MaterialTheme.typography.bodySmall, color = RoyalBlue)
                                Spacer(Modifier.height(8.dp))
                                Text(art.bodyBn, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    else -> {
                        item {
                            Text("দৈনন্দিন প্রশ্ন ও উত্তর", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(IslamicKnowledgeData.dailyQnaList) { qna ->
                            NoorElevatedCard {
                                Text("প্রশ্ন: ${qna.questionBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                Spacer(Modifier.height(6.dp))
                                Text("উত্তর: ${qna.answerBn}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("সূত্র: ${qna.referenceBn}", style = MaterialTheme.typography.labelSmall, color = DeepRed)
                            }
                        }
                        item {
                            Text("ইসলামিক বাণী ও স্মরণ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(IslamicKnowledgeData.islamicQuotesBn) { quote ->
                            NoorElevatedCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                                Text(quote, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicQuizScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val questions = IslamicKnowledgeData.quizQuestions
    val attempts by viewModel.quizAttempts.collectAsStateWithLifecycle()

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ইসলামিক কুইজ",
                subtitle = "জ্ঞান যাচাই ও ব্যাখ্যাসহ উত্তর",
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
            if (!finished) {
                val q = questions[currentIndex]
                item {
                    NoorElevatedCard {
                        Text(
                            text = "প্রশ্ন ${BengaliFormatter.formatNumber(currentIndex + 1)} / ${BengaliFormatter.formatNumber(questions.size)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(q.questionBn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        q.optionsBn.forEachIndexed { idx, opt ->
                            val isSelected = selectedOption == idx
                            val isCorrect = idx == q.correctIndex
                            val bgColor = when {
                                selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                isCorrect -> EmeraldSuccess.copy(alpha = 0.18f)
                                isSelected -> DeepRed.copy(alpha = 0.18f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                            Surface(
                                onClick = {
                                    if (selectedOption == null) {
                                        selectedOption = idx
                                        if (idx == q.correctIndex) score++
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = bgColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected || (selectedOption != null && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        if (selectedOption != null) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "ব্যাখ্যা: ${q.explanationBn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < questions.size) {
                                        currentIndex++
                                        selectedOption = null
                                    } else {
                                        finished = true
                                        viewModel.recordQuizAttempt(score, questions.size, "সাধারণ ইসলামিক জ্ঞান")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (currentIndex + 1 < questions.size) "পরবর্তী প্রশ্ন" else "ফলাফল দেখুন")
                            }
                        }
                    }
                }
            } else {
                item {
                    NoorBrandGradientBanner {
                        Text("কুইজ সম্পন্ন হয়েছে!", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "আপনার স্কোর: ${BengaliFormatter.formatNumber(score)} / ${BengaliFormatter.formatNumber(questions.size)}",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                currentIndex = 0
                                selectedOption = null
                                score = 0
                                finished = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RoyalBlue)
                        ) {
                            Text("আবার খেলুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (attempts.isNotEmpty()) {
                item {
                    NoorElevatedCard {
                        Text("পূর্ববর্তী স্কোরসমূহ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        attempts.take(5).forEach { a ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(a.categoryBn, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${BengaliFormatter.formatNumber(a.score)}/${BengaliFormatter.formatNumber(a.totalQuestions)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class GreetingTemplate(
    val titleBn: String,
    val arabicGreeting: String,
    val messageBn: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun GreetingCardsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cards = remember {
        listOf(
            GreetingTemplate(
                "জুম্মা মুবারক",
                "جُمُعَة مُبَارَكَة",
                "পবিত্র জুমার দিনের অফুরন্ত রহমত ও বরকত আপনার জীবনে বর্ষিত হোক।",
                RoyalBlueDark,
                DeepRed
            ),
            GreetingTemplate(
                "ঈদ মোবারক",
                "عِيد مُبَارَك",
                "তাকাব্বালাল্লাহু মিন্না ওয়া মিনকুম — আল্লাহ আমাদের ও আপনাদের নেক আমল কবুল করুন।",
                DeepRedDark,
                RoyalBlue
            ),
            GreetingTemplate(
                "রমজান কারীম",
                "رَمَضَان كَرِيم",
                "পবিত্র রমজানের প্রতিটি মুহূর্ত তাকওয়া, ক্ষমা ও কল্যাণে ভরে উঠুক।",
                RoyalBlue,
                Color(0xFF0E2954)
            ),
            GreetingTemplate(
                "দোয়া ও ভালোবাসা",
                "بَارَكَ اللَّهُ فِيكُمْ",
                "আল্লাহ তাআলা আপনাকে সুস্থতা, ঈমান ও দুনিয়া-আখিরাতের কল্যাণ দান করুন।",
                DeepRed,
                RoyalBlueDark
            )
        )
    }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ইসলামিক শুভেচ্ছা ও দোয়া কার্ড",
                subtitle = "শেয়ারযোগ্য ইসলামিক গ্রিটিং কার্ড",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(cards) { card ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(card.primaryColor, card.secondaryColor)))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(card.arabicGreeting, style = MaterialTheme.typography.headlineMedium, color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(card.titleBn, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = card.messageBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { shareGreetingCardBitmap(context, card) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RoyalBlue)
                        ) {
                            Icon(Icons.Filled.Share, null)
                            Spacer(Modifier.width(6.dp))
                            Text("ছবি শেয়ার")
                        }
                        OutlinedButton(
                            onClick = {
                                sharePlainText(context, card.titleBn, "${card.arabicGreeting}\n${card.titleBn}\n\n${card.messageBn}")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("টেক্সট শেয়ার")
                        }
                    }
                }
            }
        }
    }
}

private fun shareGreetingCardBitmap(context: Context, card: GreetingTemplate) {
    runCatching {
        val w = 1080
        val h = 720
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                card.primaryColor.toArgb(), card.secondaryColor.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val borderPaint = Paint().apply {
            color = GoldAccent.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        c.drawRoundRect(36f, 36f, w - 36f, h - 36f, 40f, 40f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 68f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val arabicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GoldAccent.toArgb()
            textSize = 72f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        c.drawText(card.arabicGreeting, w / 2f, 220f, arabicPaint)
        c.drawText(card.titleBn, w / 2f, 325f, titlePaint)
        c.drawText(card.messageBn.take(54), w / 2f, 430f, bodyPaint)
        if (card.messageBn.length > 54) {
            c.drawText(card.messageBn.drop(54), w / 2f, 485f, bodyPaint)
        }
        c.drawText("NOOR LIFE • নূর লাইফ", w / 2f, 625f, bodyPaint)

        val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        val file = File(dir, "noor_card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 95, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, card.titleBn))
    }.onFailure {
        sharePlainText(context, card.titleBn, "${card.arabicGreeting}\n${card.titleBn}\n${card.messageBn}")
    }
}

private data class IslamicWallpaperPreset(
    val id: String,
    val titleBn: String,
    val c1: Color,
    val c2: Color,
    val accent: Color
)

@Composable
fun WallpapersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val presets = remember {
        listOf(
            IslamicWallpaperPreset("royal_noor", "রয়্যাল নূর জ্যামিতি", RoyalBlueDark, RoyalBlue, GoldAccent),
            IslamicWallpaperPreset("crimson_crescent", "সন্ধ্যা মদিনা", DeepRedDark, DeepRed, Color.White),
            IslamicWallpaperPreset("midnight_star", "তাহাজ্জুদ নক্ষত্র", BgDark, RoyalBlueDark, BlueAccent),
            IslamicWallpaperPreset("noor_duotone", "নূর লাইফ ডুওটোন", RoyalBlue, DeepRed, GoldAccent)
        )
    }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ইসলামিক ওয়ালপেপার",
                subtitle = "উচ্চ-রেজল্যুশন জ্যামিতিক ইসলামিক ওয়ালপেপার",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(presets, key = { it.id }) { wp ->
                NoorElevatedCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(Brush.linearGradient(listOf(wp.c1, wp.c2)))
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.minDimension * 0.35f
                            for (i in 0 until 8) {
                                val a = Math.toRadians(i * 45.0)
                                val px = cx + (r * cos(a)).toFloat()
                                val py = cy + (r * sin(a)).toFloat()
                                drawCircle(wp.accent.copy(alpha = 0.28f), radius = r * 0.55f, center = Offset(px, py), style = Stroke(3f))
                            }
                            drawCircle(wp.accent, radius = r * 0.38f, center = Offset(cx, cy), style = Stroke(4f))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(wp.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = { saveWallpaperToGallery(context, wp) }) {
                            Icon(Icons.Filled.Download, null)
                            Spacer(Modifier.width(6.dp))
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}

private fun saveWallpaperToGallery(context: Context, wp: IslamicWallpaperPreset) {
    runCatching {
        val w = 1080
        val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val bg = Paint().apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), wp.c1.toArgb(), wp.c2.toArgb(), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = wp.accent.copy(alpha = 0.35f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val cx = w / 2f
        val cy = h / 2f
        val r = 340f
        for (i in 0 until 12) {
            val a = Math.toRadians(i * 30.0)
            c.drawCircle(cx + (r * cos(a)).toFloat(), cy + (r * sin(a)).toFloat(), r * 0.6f, stroke)
        }
        c.drawCircle(cx, cy, r * 0.42f, stroke)

        val filename = "NoorLife_${wp.id}_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NoorLife")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore URI failed")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            Toast.makeText(context, "ওয়ালপেপার Pictures/NoorLife ফোল্ডারে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
        } else {
            val dir = File(context.filesDir, "wallpapers").apply { mkdirs() }
            val file = File(dir, filename)
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 95, it) }
            Toast.makeText(context, "ওয়ালপেপার সংরক্ষিত হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }.onFailure {
        Toast.makeText(context, "ওয়ালপেপার সংরক্ষণ করা যায়নি: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
