package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.ads.AdConfig
import com.example.ads.NoorRespectfulBannerAd
import com.example.data.content.QuranCatalogMetadata
import com.example.data.local.NotificationSoundType
import com.example.domain.prayer.AsrMadhhab
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerWaqt
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.BengaliFormatter
import com.example.navigation.NoorRoute
import com.example.notifications.PrayerNotificationScheduler
import com.example.ui.components.NoorBrandGradientBanner
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.components.sharePlainText
import com.example.ui.theme.DeepRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.NoorViewModel

@Composable
fun ProfileHubScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notes by viewModel.personalNotes.collectAsStateWithLifecycle()
    val fastingDays by viewModel.fastingDays.collectAsStateWithLifecycle()
    val tasbihSessions by viewModel.tasbihSessions.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
    val totalTasbih = remember(tasbihSessions) { tasbihSessions.sumOf { it.totalLifetimeCount } }
    val totalFasted = remember(fastingDays) { fastingDays.count { it.isFasted } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NoorBrandGradientBanner {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = prefs.userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${prefs.city.displayName(prefs.language)} • ${prefs.language.nativeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    IconButton(onClick = { showNameDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "নাম পরিবর্তন", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem("তাসবিহ", BengaliFormatter.formatNumber(totalTasbih, prefs.language))
                    ProfileStatItem("বুকমার্ক", BengaliFormatter.formatNumber(bookmarks.size, prefs.language))
                    ProfileStatItem("রোজা", BengaliFormatter.formatNumber(totalFasted, prefs.language))
                    ProfileStatItem("নোট", BengaliFormatter.formatNumber(notes.size, prefs.language))
                }
            }
        }

        item {
            NoorElevatedCard {
                ProfileMenuRow(strings.settings, "ভাষা, থিম, আজান, ফন্ট ও অফলাইন অডিও", Icons.Filled.Settings) {
                    onNavigate(NoorRoute.Settings)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileMenuRow(strings.bookmarksAndFavorites, "সংরক্ষিত আয়াত, দোয়া, হাদিস ও ৯৯ নাম", Icons.Filled.Bookmark) {
                    onNavigate(NoorRoute.Bookmarks)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileMenuRow(strings.personalNotes, "ব্যক্তিগত ইসলামি নোট ও যাকাত হিসাব", Icons.Filled.EditNote) {
                    onNavigate(NoorRoute.PersonalNotes)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileMenuRow(strings.privacyPolicy, "ডেটা নিরাপত্তা, তৃতীয় পক্ষের SDK ও নীতিমালা", Icons.Filled.PrivacyTip) {
                    onNavigate(NoorRoute.PrivacyAbout)
                }
            }
        }

        item {
            NoorElevatedCard {
                ProfileMenuRow("অ্যাপ শেয়ার করুন", "আপনার বন্ধু ও পরিবারের সাথে নূর লাইফ শেয়ার করুন", Icons.Filled.Share) {
                    sharePlainText(
                        context,
                        "NOOR LIFE — নূর লাইফ",
                        "নূর লাইফ: আপনার সম্পূর্ণ বাংলা ইসলামিক জীবনসঙ্গী অ্যাপ (কুরআন, নামাজ, কিবলা, তাসবিহ, দোয়া ও যাকাত)।"
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileMenuRow("অ্যাপ রেটিং দিন", "গুগল প্লে স্টোরে আপনার মূল্যবান মতামত দিন", Icons.Filled.StarRate) {
                    val uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    runCatching { context.startActivity(intent) }.onFailure {
                        onNavigate(NoorRoute.PrivacyAbout)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileMenuRow("সহায়তা ও যোগাযোগ", "সাহায্য ও প্রতিক্রিয়ার জন্য যোগাযোগ করুন", Icons.Filled.SupportAgent) {
                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@noorlife.app")
                        putExtra(Intent.EXTRA_SUBJECT, "NOOR LIFE App Support")
                    }
                    runCatching { context.startActivity(mailIntent) }.onFailure {
                        onNavigate(NoorRoute.PrivacyAbout)
                    }
                }
            }
        }

        item {
            NoorRespectfulBannerAd(isOnline = isOnline)
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("ব্যবহারকারীর নাম") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("আপনার নাম") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setUserName(nameInput)
                    showNameDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onBack: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val downloadedAudio by viewModel.downloadedAudio.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val exactAlarmAllowed = PrayerNotificationScheduler.canScheduleExactAlarms(context)
    val batteryUnrestricted = PrayerNotificationScheduler.isIgnoringBatteryOptimizations(context)
    val notifPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = strings.settings,
                subtitle = "ভাষা, থিম, নামাজ ও নোটিফিকেশন নিয়ন্ত্রণ",
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
            // Language
            item {
                NoorElevatedCard {
                    Text(strings.languageSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = prefs.language == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(lang.nativeName) },
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }

            // Theme
            item {
                NoorElevatedCard {
                    Text(strings.themeSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ThemeMode.SYSTEM to "সিস্টেম",
                            ThemeMode.LIGHT to "লাইট মোড",
                            ThemeMode.DARK to "ডার্ক মোড"
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Notifications & Android Scheduling Transparency
            item {
                NoorElevatedCard {
                    Text(strings.notificationSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))

                    if (!notifPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = { notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.NotificationsActive, null)
                            Spacer(Modifier.width(6.dp))
                            Text("নোটিফিকেশন অনুমতি দিন")
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Honest Android OS scheduling disclosure
                    Surface(
                        color = if (exactAlarmAllowed && batteryUnrestricted) {
                            EmeraldSuccess.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (exactAlarmAllowed) {
                                    "সঠিক সময়ের অ্যালার্ম অনুমতি সক্রিয় আছে।"
                                } else {
                                    "সতর্কতা: Android-এর 'Exact Alarm' বা ব্যাটারি অপ্টিমাইজেশন সীমাবদ্ধতার কারণে নামাজের নোটিফিকেশন কয়েক মিনিট আগে-পরে হতে পারে।"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!exactAlarmAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                TextButton(onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        })
                                    }
                                }) { Text("Exact Alarm সেটিংস খুলুন") }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    PrayerWaqt.entries.forEach { w ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(w.localizedName(strings))
                            Switch(
                                checked = prefs.isNotificationEnabled(w),
                                onCheckedChange = { viewModel.setPrayerNotification(w, it) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("সাহরি ও ইফতারের অনুস্মারক")
                        Switch(
                            checked = prefs.notifySehriIftar,
                            onCheckedChange = { viewModel.setSehriIftarNotification(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("কম্পন (Vibration)")
                        Switch(
                            checked = prefs.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("নোটিফিকেশন সাউন্ড", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    NotificationSoundType.entries.forEach { st ->
                        FilterChip(
                            selected = prefs.notificationSound == st,
                            onClick = { viewModel.setNotificationSound(st) },
                            label = { Text(st.bnName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { PrayerNotificationScheduler.showImmediateTestNotification(context, prefs) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পরীক্ষামূলক নোটিফিকেশন পাঠান")
                    }
                }
            }

            // Quran Fonts & Reciter
            item {
                NoorElevatedCard {
                    Text(strings.fontSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("আরবি ফন্ট সাইজ: ${BengaliFormatter.formatNumber(prefs.arabicFontSize.toInt())}")
                    Slider(
                        value = prefs.arabicFontSize,
                        onValueChange = {
                            viewModel.setQuranReadingSettings(it, prefs.bengaliFontSize, prefs.showBengaliPronunciation, prefs.showEnglishTranslation)
                        },
                        valueRange = 18f..38f
                    )
                    Text("বাংলা ফন্ট সাইজ: ${BengaliFormatter.formatNumber(prefs.bengaliFontSize.toInt())}")
                    Slider(
                        value = prefs.bengaliFontSize,
                        onValueChange = {
                            viewModel.setQuranReadingSettings(prefs.arabicFontSize, it, prefs.showBengaliPronunciation, prefs.showEnglishTranslation)
                        },
                        valueRange = 13f..24f
                    )
                    Text("ক্বারী নির্বাচন", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    QuranCatalogMetadata.reciters.forEach { r ->
                        FilterChip(
                            selected = prefs.selectedReciterId == r.id,
                            onClick = { viewModel.setSelectedReciter(r.id) },
                            label = { Text(r.nameBn) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Offline Downloads & Cache Management
            item {
                NoorElevatedCard {
                    Text(strings.offlineDownloads, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "ডাউনলোডকৃত সূরা অডিও: ${BengaliFormatter.formatNumber(downloadedAudio.size)}টি",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    downloadedAudio.forEach { audio ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sName = QuranCatalogMetadata.surahBnMap[audio.surahNumber]?.first ?: "সূরা ${audio.surahNumber}"
                            Text("$sName (${audio.sizeBytes / 1024} KB)", style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.deleteDownloadedAudio(audio) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "মুছুন", tint = DeepRed)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearCachedDownloads() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ক্যাশ ও অডিও ফাইল পরিষ্কার করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyAboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "গোপনীয়তা নীতি ও নূর লাইফ পরিচিতি",
                subtitle = "NOOR LIFE • সংস্করণ ${BuildConfig.VERSION_NAME}",
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
                NoorElevatedCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_noor_emblem),
                            contentDescription = "নূর লাইফ লোগো",
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("NOOR LIFE (নূর লাইফ)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue)
                            Text("প্যাকেজ: ${BuildConfig.APPLICATION_ID}", style = MaterialTheme.typography.labelSmall)
                            Text("ভার্শন: ${BuildConfig.VERSION_NAME} (কোড ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "নূর লাইফ একটি সম্পূর্ণ অফলাইন-সমর্থিত বাংলা ইসলামিক জীবনসঙ্গী অ্যাপ। পবিত্র কুরআন (১১৪ সূরা), নির্ভুল নামাজের সময়সূচি, কিবলা কম্পাস, ডিজিটাল তাসবিহ, আসমাউল হুসনা, দোয়া ভাণ্ডার, নামাজ শিক্ষা, যাকাত এবং রমজান গাইড সব একত্রে নির্ভরযোগ্য ধর্মীয় উৎস থেকে উপস্থাপন করা হয়েছে।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                NoorElevatedCard {
                    Text("গোপনীয়তা নীতি ও ডেটা নিরাপত্তা (Data Safety)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• অবস্থান (Location): কেবল আপনার অনুমতিক্রমেই সঠিক নামাজের সময় ও কিবলার দিক নির্ণয়ের জন্য ব্যবহৃত হয়; কোনো ব্যক্তিগত অবস্থান সার্ভারে সংরক্ষণ বা বিক্রি করা হয় না।\n" +
                            "• স্থানীয় ডেটা: আপনার বুকমার্ক, তাসবিহ গণনা, রোজার রেকর্ড, ব্যক্তিগত নোট এবং সেটিংস সম্পূর্ণভাবে আপনার নিজস্ব ডিভাইসেই (Room & DataStore) সংরক্ষিত থাকে।\n" +
                            "• মিডিয়া ও স্টোরেজ: কোনো বিস্তৃত স্টোরেজ পারমিশন চাওয়া হয় না; ওয়ালপেপার সংরক্ষণে আধুনিক অ্যান্ড্রয়েড MediaStore API ব্যবহৃত হয়।\n" +
                            "• বিজ্ঞাপন (AdMob): Google Mobile Ads SDK কেবল নন-ইবাদত ইউটিলিটি স্ক্রিনে ব্যবহৃত হয়। কুরআন পাঠ, তাফসির, তিলাওয়াত, নামাজ শিক্ষা বা জিকিরের সময় কখনোই বিজ্ঞাপন দেখানো হয় না। (বর্তমান বিল্ডে টেস্ট মোড: ${if (AdConfig.isTestMode) "সক্রিয়" else "প্রোডাকশন"})।\n" +
                            "• তথ্যসূত্র: কুরআনের আরবি ও বাংলা পাঠ AlQuran.cloud (মুহিউদ্দীন খান / উসমানি পাঠ), তাফসির ইবনে কাসীর (তাওহীদ পাবলিকেশন) ও সহীহ বুখারী (ফওয়াজ আহমেদ হাদিস ভান্ডার) থেকে সংগৃহীত।",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                NoorElevatedCard {
                    Text("গুগল প্লে স্টোর রিলিজ তথ্য", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepRed)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• টার্গেট API: Android 16 (API 36), ন্যূনতম API 24।\n" +
                            "• AAB জেনারেশন কমান্ড: `gradle bundleRelease` (আপলোড কী-স্টোর ও প্রোডাকশন AdMob ID কনফিগার করার পর)।\n" +
                            "• দাবিত্যাগ: অ্যাপটি গুগল প্লে কনসোলে জমা দেওয়ার জন্য প্রস্তুত; প্রকৃত প্রকাশনা এবং অনুমোদন কেবল প্লে কনসোল পর্যালোচনা সম্পন্ন হওয়ার পরই কার্যকর হবে।",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
