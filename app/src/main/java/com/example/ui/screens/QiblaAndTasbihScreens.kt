package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.BengaliFormatter
import com.example.ui.components.ArabicRtlText
import com.example.ui.components.NoorElevatedCard
import com.example.ui.components.NoorSubScreenTopBar
import com.example.ui.theme.DeepRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoyalBlue
import com.example.viewmodel.NoorViewModel
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun QiblaCompassScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val liveStatus by viewModel.livePrayerStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var magneticFieldMicroTesla by remember { mutableFloatStateOf(45f) }
    var sensorAccuracyLow by remember { mutableStateOf(false) }
    var sensorsAvailable by remember { mutableStateOf(true) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magSensor == null)) {
            sensorsAvailable = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasGeo = false
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            azimuthDeg = azimuthDeg + 0.25f * (((deg - azimuthDeg + 540f) % 360f) - 180f)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, 3)
                            hasGravity = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                            hasGeo = true
                            val x = event.values[0]
                            val y = event.values[1]
                            val z = event.values[2]
                            magneticFieldMicroTesla = sqrt(x * x + y * y + z * z)
                        }
                    }
                    if (rotationSensor == null && hasGravity && hasGeo) {
                        if (SensorManager.getRotationMatrix(rotMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            azimuthDeg = azimuthDeg + 0.25f * (((deg - azimuthDeg + 540f) % 360f) - 180f)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    sensorAccuracyLow = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                        accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
                }
            }

            if (rotationSensor != null) {
                sm.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            }
            if (magSensor != null && rotationSensor != null) {
                sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose { sm.unregisterListener(listener) }
        }
    }

    val qiblaBearing = liveStatus.qiblaBearingDeg.toFloat()
    val relativeAngle = ((qiblaBearing - azimuthDeg + 360f) % 360f)
    val isAligned = relativeAngle < 6f || relativeAngle > 354f
    val hasMagneticInterference = magneticFieldMicroTesla < 25f || magneticFieldMicroTesla > 68f

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "কিবলা কম্পাস",
                subtitle = "${prefs.city.bnName} • কাবা শরীফ পর্যন্ত ${BengaliFormatter.formatDecimal(liveStatus.kaabaDistanceKm, 0)} কি.মি.",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "ক্যালিব্রেশন নির্দেশনা")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!sensorsAvailable) {
                // Honest Fallback Bearing Screen when hardware sensors are missing
                item {
                    NoorElevatedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = DeepRed)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "কম্পাস সেন্সর পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepRed
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "আপনার এই ডিভাইসে হার্ডওয়্যার ম্যাগনেটোমিটার বা রোটেশন সেন্সর অনুপস্থিত। তাই ভুয়া কম্পাস ঘোরানোর পরিবর্তে প্রকৃত গাণিতিক কিবলার দিক নিচে দেওয়া হলো:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "সঠিক উত্তর দিক থেকে ${BengaliFormatter.formatDecimal(liveStatus.qiblaBearingDeg, 1)}° কোণে (পশ্চিম ও সামান্য উত্তর-পশ্চিম দিকে)",
                            style = MaterialTheme.typography.titleLarge,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "অক্ষাংশ: ${BengaliFormatter.formatDecimal(prefs.city.latitude, 4)}° • দ্রাঘিমাংশ: ${BengaliFormatter.formatDecimal(prefs.city.longitude, 4)}°",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                if (hasMagneticInterference || sensorAccuracyLow) {
                    item {
                        Surface(
                            color = DeepRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = DeepRed)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "চৌম্বকীয় হস্তক্ষেপ বা সেন্সর নির্ভুলতা কম (${magneticFieldMicroTesla.toInt()} µT)। ধাতব বস্তু থেকে দূরে সরে ফোনটি '৮' আকৃতিতে ঘোরান।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepRed
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isAligned) EmeraldSuccess.copy(alpha = 0.14f) else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (isAligned) "সঠিক কিবলার দিক নির্ধারিত হয়েছে!" else "ফোনটি সমতলে রেখে কাবা চিহ্নের দিকে ঘোরান",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) EmeraldSuccess else RoyalBlue,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier.size(290.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f - 16f

                            drawCircle(
                                color = if (isAligned) EmeraldSuccess.copy(alpha = 0.16f) else RoyalBlue.copy(alpha = 0.08f),
                                radius = radius,
                                center = center
                            )
                            drawCircle(
                                color = if (isAligned) EmeraldSuccess else RoyalBlue,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 6f)
                            )

                            // Rotate compass dial by -azimuthDeg
                            rotate(degrees = -azimuthDeg, pivot = center) {
                                // North marker
                                drawLine(
                                    color = DeepRed,
                                    start = Offset(center.x, center.y - radius + 6f),
                                    end = Offset(center.x, center.y - radius + 30f),
                                    strokeWidth = 8f,
                                    cap = StrokeCap.Round
                                )
                                // Cardinal ticks
                                for (angle in 0 until 360 step 30) {
                                    rotate(degrees = angle.toFloat(), pivot = center) {
                                        drawLine(
                                            color = Color.Gray.copy(alpha = 0.5f),
                                            start = Offset(center.x, center.y - radius + 6f),
                                            end = Offset(center.x, center.y - radius + 16f),
                                            strokeWidth = 3f
                                        )
                                    }
                                }
                            }

                            // Qibla Needle pointing at relativeAngle
                            rotate(degrees = relativeAngle, pivot = center) {
                                val arrowPath = Path().apply {
                                    moveTo(center.x, center.y - radius + 24f)
                                    lineTo(center.x - 18f, center.y + 10f)
                                    lineTo(center.x, center.y - 6f)
                                    lineTo(center.x + 18f, center.y + 10f)
                                    close()
                                }
                                drawPath(arrowPath, color = if (isAligned) EmeraldSuccess else DeepRed)
                                drawCircle(color = GoldAccent, radius = 14f, center = Offset(center.x, center.y - radius + 28f))
                            }

                            drawCircle(color = RoyalBlue, radius = 12f, center = center)
                        }
                    }
                }
            }

            item {
                NoorElevatedCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("কিবলার কোণ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${BengaliFormatter.formatDecimal(liveStatus.qiblaBearingDeg, 1)}°",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("কাবা শরীফের দূরত্ব", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${BengaliFormatter.formatDecimal(liveStatus.kaabaDistanceKm, 0)} কি.মি.",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepRed
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showCalibrationDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Explore, null)
                        Spacer(Modifier.width(6.dp))
                        Text("কম্পাস ক্যালিব্রেট করার নিয়ম দেখুন")
                    }
                }
            }
        }
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = { Text("কিবলা কম্পাস ক্যালিব্রেশন") },
            text = {
                Text(
                    "১. ফোনের ম্যাগনেটিক কভার বা ধাতব বস্তু থেকে দূরে থাকুন।\n" +
                        "২. ফোনটি হাতে ধরে বাতাসে ইংরেজি '8' (আট) আকৃতিতে ৩-৪ বার ঘোরান।\n" +
                        "৩. সঠিক দিক দেখার জন্য ফোনটি মাটির সমান্তরালে রাখুন।"
                )
            },
            confirmButton = {
                Button(onClick = { showCalibrationDialog = false }) { Text("বুঝেছি") }
            }
        )
    }
}

@Composable
fun DigitalTasbihScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.tasbihSessions.collectAsStateWithLifecycle()
    val history by viewModel.tasbihHistory.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedSessionId by remember { mutableStateOf<Long?>(null) }
    val activeSession = sessions.firstOrNull { it.id == selectedSessionId } ?: sessions.firstOrNull()

    var showResetConfirm by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customArabic by remember { mutableStateOf("") }
    var customTargetInput by remember { mutableStateOf("100") }

    val toneGen = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 45) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { toneGen?.release() } }
    }

    fun triggerFeedback() {
        if (prefs.tasbihSoundEnabled) {
            runCatching { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 35) }
        }
        if (prefs.tasbihVibrationEnabled) {
            runCatching {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vm.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            NoorSubScreenTopBar(
                title = "ডিজিটাল তাসবিহ",
                subtitle = "জিকির গণনা ও দৈনিক রেকর্ড",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setTasbihAudioHaptics(!prefs.tasbihSoundEnabled, prefs.tasbihVibrationEnabled)
                        }
                    ) {
                        Icon(
                            if (prefs.tasbihSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = "শব্দ টগল"
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.setTasbihAudioHaptics(prefs.tasbihSoundEnabled, !prefs.tasbihVibrationEnabled)
                        }
                    ) {
                        Icon(
                            Icons.Filled.Vibration,
                            contentDescription = "ভাইব্রেশন টগল",
                            tint = if (prefs.tasbihVibrationEnabled) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dhikr Session Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sessions, key = { it.id }) { s ->
                        FilterChip(
                            selected = activeSession?.id == s.id,
                            onClick = { selectedSessionId = s.id },
                            label = { Text("${s.titleBn} (${BengaliFormatter.formatNumber(s.currentCount)})") },
                            trailingIcon = if (!s.isDefault) {
                                {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "মুছুন",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.deleteCustomTasbih(s.id) }
                                    )
                                }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showAddDialog = true },
                            leadingIcon = { Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp)) },
                            label = { Text("নতুন জিকির") }
                        )
                    }
                }
            }

            if (activeSession != null) {
                item {
                    NoorElevatedCard {
                        if (activeSession.arabicText.isNotBlank()) {
                            ArabicRtlText(
                                text = activeSession.arabicText,
                                fontSizeSp = 28f,
                                color = RoyalBlue
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = activeSession.titleBn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeSession.meaningBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Large Tap Counter Ring
                item {
                    val progress = (activeSession.currentCount.toFloat() / activeSession.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .clickable {
                                triggerFeedback()
                                viewModel.incrementTasbih(activeSession)
                            }
                            .testTag("tasbih_tap_pad"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 16.dp.toPx()
                            drawCircle(
                                color = RoyalBlue.copy(alpha = 0.14f),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = if (activeSession.currentCount >= activeSession.targetCount) EmeraldSuccess else DeepRed,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = BengaliFormatter.formatNumber(activeSession.currentCount),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlue
                            )
                            Text(
                                text = "লক্ষ্য: ${BengaliFormatter.formatNumber(activeSession.targetCount)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "গণনা করতে ট্যাপ করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Target Preset Chips + Reset Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(33, 99, 100, 1000).forEach { target ->
                            FilterChip(
                                selected = activeSession.targetCount == target,
                                onClick = { viewModel.updateTasbihTarget(activeSession, target) },
                                label = { Text(BengaliFormatter.formatNumber(target)) }
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepRed)
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("রিসেট")
                        }
                    }
                }

                // Daily History Summary
                item {
                    NoorElevatedCard {
                        Text("দৈনিক তাসবিহ ইতিহাস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        if (history.isEmpty()) {
                            Text("আজকের গণনা শুরু করুন—প্রতিদিনের হিসাব এখানে সংরক্ষিত হবে।", style = MaterialTheme.typography.bodySmall)
                        } else {
                            history.take(7).forEach { h ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(BengaliFormatter.toBengaliDigits(h.dateKey), style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${BengaliFormatter.formatNumber(h.totalCount)} বার",
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

    if (showResetConfirm && activeSession != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("গণনা রিসেট করবেন?") },
            text = { Text("আপনি কি '${activeSession.titleBn}' তাসবিহের বর্তমান গণনা শূন্যে নামিয়ে আনতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTasbihSession(activeSession)
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
                ) { Text("হ্যাঁ, রিসেট করুন") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("বাতিল") }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন জিকির যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("জিকিরের নাম (বাংলায়)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customArabic,
                        onValueChange = { customArabic = it },
                        label = { Text("আরবি লেখা (ঐচ্ছিক)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { customTargetInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("দৈনিক লক্ষ্য সংখ্যা") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val t = customTargetInput.toIntOrNull() ?: 100
                    viewModel.addCustomTasbih(customTitle, customArabic, t)
                    customTitle = ""
                    customArabic = ""
                    showAddDialog = false
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("বাতিল") }
            }
        )
    }
}
