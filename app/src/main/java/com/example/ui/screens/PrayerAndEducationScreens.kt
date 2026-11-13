package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.DuasAndKalimasData
import com.example.data.content.NamazAndWuduData
import com.example.domain.prayer.AsrMadhhab
import com.example.domain.prayer.BangladeshCitiesCatalog
import com.example.domain.prayer.CalculationMethod
import com.example.localization.AppStrings
import com.example.localization.BengaliFormatter
import com.example.navigation.NoorRoute
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorBrandGradientBanner
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.sharePlainText
import com.example.ui.theme.DeepRed
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerHubScreen(
    viewModel: NoorViewModel,
    strings: AppStrings,
    onNavigate: (NoorRoute) -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val liveStatus by viewModel.livePrayerStatus.collectAsStateWithLifecycle()
    val isDetecting by viewModel.locationDetecting.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showGpsRationale by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }

    val cal = remember { Calendar.getInstance() }
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1
    val monthlyList = remember(prefs.city, prefs.calculationMethod, prefs.asrMadhhab) {
        viewModel.getMonthlySchedule(currentYear, currentMonth)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectGpsCity(context)
        }
    }

    fun requestGpsOrDetect() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            viewModel.detectGpsCity(context)
        } else {
            showGpsRationale = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NoorBrandGradientBanner {
                Text(
                    text = "নামাজের সময়সূচি ও শিক্ষা",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${prefs.city.displayFull(prefs.language)} • ${prefs.calculationMethod.bnName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { requestGpsOrDetect() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDetecting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.GpsFixed, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("জিপিএস অবস্থান", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                    Surface(
                        onClick = { showCityPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.LocationCity, null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("শহর পরিবর্তন", style = MaterialTheme.typography.labelMedium, color = RoyalBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (statusMsg != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(statusMsg ?: "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.clearStatusBanner() }) { Text("ঠিক আছে") }
                    }
                }
            }
        }

        // Action Pills for Qibla, Namaz Education, Wudu/Ghusl, Kalimas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrayerFeatureChip("কিবলা কম্পাস", Icons.Filled.Explore, RoyalBlue, Modifier.weight(1f)) {
                    onNavigate(NoorRoute.QiblaCompass)
                }
                PrayerFeatureChip("নামাজ শিক্ষা", Icons.Filled.Mosque, DeepRed, Modifier.weight(1f)) {
                    onNavigate(NoorRoute.NamazEducation)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrayerFeatureChip("অজু ও গোসল", Icons.Filled.WaterDrop, RoyalBlue, Modifier.weight(1f)) {
                    onNavigate(NoorRoute.WuduGhusl)
                }
                PrayerFeatureChip("পাঁচ কালিমা", Icons.Filled.Book, DeepRed, Modifier.weight(1f)) {
                    onNavigate(NoorRoute.FiveKalimas)
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("দৈনিক সূচি ও নোটিফিকেশন") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("মাসিক ক্যালেন্ডার") })
            }
        }

        if (selectedTab == 0) {
            item {
                NoorElevatedCard {
                    val sched = liveStatus.todaySchedule
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "সাহরির শেষ: ${BengaliFormatter.formatTime12Hour(sched.sehriHour24, sched.sehriMinute, prefs.language)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "তাহাজ্জুদ: ${BengaliFormatter.formatTime12Hour(sched.tahajjudHour24, sched.tahajjudMinute, prefs.language)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    sched.allSlots.forEach { slot ->
                        val isNext = slot.waqt == liveStatus.nextSlot.waqt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = slot.waqt.localizedName(strings),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNext) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isNext) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = RoyalBlue.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "পরবর্তী",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = RoyalBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = BengaliFormatter.formatTime12Hour(slot.hour24, slot.minute, prefs.language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.NotificationsActive,
                                    contentDescription = "আজান নোটিফিকেশন",
                                    tint = if (prefs.isNotificationEnabled(slot.waqt)) DeepRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Switch(
                                    checked = prefs.isNotificationEnabled(slot.waqt),
                                    onCheckedChange = { viewModel.setPrayerNotification(slot.waqt, it) }
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            // Calculation Method & Madhhab quick settings
            item {
                NoorElevatedCard {
                    SectionHeader(title = "হিসাব পদ্ধতি ও মাযহাব")
                    Spacer(modifier = Modifier.height(6.dp))
                    AsrMadhhab.entries.forEach { m ->
                        FilterChip(
                            selected = prefs.asrMadhhab == m,
                            onClick = { viewModel.setAsrMadhhab(m) },
                            label = { Text(m.bnName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("হিসাব পদ্ধতি", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    CalculationMethod.entries.forEach { method ->
                        FilterChip(
                            selected = prefs.calculationMethod == method,
                            onClick = { viewModel.setCalculationMethod(method) },
                            label = { Text(method.bnName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "${BengaliFormatter.bengaliGregorianMonths[currentMonth - 1]} ${BengaliFormatter.formatNumber(currentYear)} — ${prefs.city.bnName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(monthlyList) { daySched ->
                val dCal = Calendar.getInstance().apply { timeInMillis = daySched.dateMillis }
                val dayNum = dCal.get(Calendar.DAY_OF_MONTH)
                val isToday = dayNum == cal.get(Calendar.DAY_OF_MONTH)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BengaliFormatter.formatNumber(dayNum),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp)
                        )
                        MiniPrayerCell("ফজর", daySched.fajr.hour24, daySched.fajr.minute)
                        MiniPrayerCell("যোহর", daySched.dhuhr.hour24, daySched.dhuhr.minute)
                        MiniPrayerCell("আসর", daySched.asr.hour24, daySched.asr.minute)
                        MiniPrayerCell("মাগরিব", daySched.maghrib.hour24, daySched.maghrib.minute)
                        MiniPrayerCell("এশা", daySched.isha.hour24, daySched.isha.minute)
                    }
                }
            }
        }
    }

    if (showGpsRationale) {
        AlertDialog(
            onDismissRequest = { showGpsRationale = false },
            title = { Text("লোকেশন অনুমতির প্রয়োজন") },
            text = {
                Text("আপনার বর্তমান শহরের সঠিক নামাজের সময়সূচি ও কিবলার দিক নির্ণয়ের জন্য নূর লাইফ অ্যাপ আপনার ডিভাইসের অবস্থান (Location) ব্যবহার করতে চায়। আমরা আপনার অবস্থান কোনো সার্ভারে সংরক্ষণ বা বিক্রি করি না।")
            },
            confirmButton = {
                Button(onClick = {
                    showGpsRationale = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }) { Text("অনুমতি দিন") }
            },
            dismissButton = {
                TextButton(onClick = { showGpsRationale = false }) { Text("পরে") }
            }
        )
    }

    if (showCityPicker) {
        ModalBottomSheet(onDismissRequest = { showCityPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("শহর / জেলা নির্বাচন করুন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = citySearchQuery,
                    onValueChange = { citySearchQuery = it },
                    placeholder = { Text("বাংলাদেশের ৬৪ জেলা বা শহরের নাম খুঁজুন...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                val filteredCities = remember(citySearchQuery) {
                    if (citySearchQuery.isBlank()) BangladeshCitiesCatalog.cities
                    else {
                        val q = citySearchQuery.trim().lowercase()
                        BangladeshCitiesCatalog.cities.filter {
                            it.bnName.lowercase().contains(q) || it.enName.lowercase().contains(q)
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCities, key = { it.id }) { city ->
                        val selected = prefs.city.id == city.id && !prefs.useGpsLocation
                        Surface(
                            onClick = {
                                viewModel.selectManualCity(city)
                                showCityPicker = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(city.bnName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${city.enName}, ${city.countryBn}", style = MaterialTheme.typography.labelSmall)
                                }
                                if (selected) {
                                    Icon(Icons.Filled.Check, contentDescription = "নির্বাচিত", tint = RoyalBlue)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PrayerFeatureChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MiniPrayerCell(label: String, h: Int, m: Int) {
    val h12 = if (h % 12 == 0) 12 else h % 12
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = BengaliFormatter.toBengaliDigits("%d:%02d".format(h12, m)),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NamazEducationScreen(onBack: () -> Unit) {
    var expandedIds by remember { mutableStateOf(setOf("importance")) }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "পূর্ণাঙ্গ নামাজ শিক্ষা",
                subtitle = "রাকাত, দোয়া-তাসবিহ, মাসয়ালা ও ধাপে ধাপে নিয়ম",
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
                NoorElevatedCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                    Text(
                        "পাঁচ ওয়াক্ত ও জুমার নামাজের রাকাত সংখ্যা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    NamazAndWuduData.rakatTable.forEach { row ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row.waqtBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("মোট: ${row.totalBn}", style = MaterialTheme.typography.labelMedium, color = DeepRed, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "পূর্বে: ${row.sunnatBeforeBn} | ফরজ: ${row.fardBn} | পরে: ${row.sunnatAfterBn} | বিতর/নফল: ${row.witrOrNaflBn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            items(NamazAndWuduData.salahTopics, key = { it.id }) { topic ->
                val expanded = expandedIds.contains(topic.id)
                NoorElevatedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedIds = if (expanded) expandedIds - topic.id else expandedIds + topic.id
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(topic.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(topic.summaryBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "বিস্তারিত"
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            if (topic.arabic.isNotBlank()) {
                                ArabicRtlText(text = topic.arabic, fontSizeSp = 22f)
                                Spacer(Modifier.height(6.dp))
                            }
                            if (topic.pronunciationBn.isNotBlank()) {
                                Text("উচ্চারণ: ${topic.pronunciationBn}", style = MaterialTheme.typography.bodyMedium, color = RoyalBlue)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (topic.meaningBn.isNotBlank()) {
                                Text("অর্থ: ${topic.meaningBn}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                            }
                            Text(topic.detailsBn, style = MaterialTheme.typography.bodyMedium)
                            if (topic.scholarlyNoteBn.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "মাজহাব ও ফিকহি ব্যাখ্যা: ${topic.scholarlyNoteBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WuduGhuslScreen(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "অজু ও গোসল শিক্ষা",
                subtitle = "পবিত্রতার ধাপে ধাপে নির্দেশিকা",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("অজুর নিয়ম") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("ফরজ গোসল") })
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tab == 0) {
                    item {
                        NoorElevatedCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                            Text("অজুর ৪টি ফরজ কাজ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue)
                            Spacer(Modifier.height(6.dp))
                            NamazAndWuduData.wuduFardList.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                    items(NamazAndWuduData.wuduSteps) { step ->
                        NoorElevatedCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(RoyalBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        BengaliFormatter.formatNumber(step.stepNumber),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(step.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(step.descriptionBn, style = MaterialTheme.typography.bodyMedium)
                            if (step.arabic.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                ArabicRtlText(text = step.arabic, fontSizeSp = 20f)
                                Text("উচ্চারণ: ${step.pronunciationBn}", style = MaterialTheme.typography.bodySmall, color = RoyalBlue)
                            }
                        }
                    }
                    item {
                        NoorElevatedCard {
                            Text("অজুর সুন্নতসমূহ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            NamazAndWuduData.wuduSunnatList.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            Spacer(Modifier.height(12.dp))
                            Text("অজু ভঙ্গের কারণসমূহ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepRed)
                            Spacer(Modifier.height(6.dp))
                            NamazAndWuduData.wuduBreakersList.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                } else {
                    item {
                        NoorElevatedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                            Text("গোসলের ৩টি ফরজ কাজ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepRed)
                            Spacer(Modifier.height(6.dp))
                            NamazAndWuduData.ghuslFardList.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                    items(NamazAndWuduData.ghuslSteps) { step ->
                        NoorElevatedCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DeepRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        BengaliFormatter.formatNumber(step.stepNumber),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(step.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(step.descriptionBn, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiveKalimasScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "পাঁচ কালিমা",
                subtitle = "আরবি, বাংলা উচ্চারণ ও অর্থসহ",
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
            items(DuasAndKalimasData.fiveKalimas, key = { it.number }) { kalima ->
                NoorElevatedCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kalima.titleBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = {
                                copyTextToClipboard(
                                    context,
                                    kalima.titleBn,
                                    "${kalima.titleBn}\n${kalima.arabic}\nউচ্চারণ: ${kalima.pronunciationBn}\nঅর্থ: ${kalima.meaningBn}"
                                )
                            }) { Icon(Icons.Filled.ContentCopy, "কপি") }
                            IconButton(onClick = {
                                sharePlainText(
                                    context,
                                    kalima.titleBn,
                                    "${kalima.titleBn}\n\n${kalima.arabic}\n\nউচ্চারণ: ${kalima.pronunciationBn}\nঅর্থ: ${kalima.meaningBn}"
                                )
                            }) { Icon(Icons.Filled.Share, "শেয়ার") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ArabicRtlText(text = kalima.arabic, fontSizeSp = 23f)
                    Spacer(Modifier.height(8.dp))
                    Text("উচ্চারণ: ${kalima.pronunciationBn}", style = MaterialTheme.typography.bodyMedium, color = RoyalBlue)
                    Spacer(Modifier.height(4.dp))
                    Text("অর্থ: ${kalima.meaningBn}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
