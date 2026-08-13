package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AdhkarItem
import com.example.data.models.HisnCategory
import com.example.data.models.QuranSurah
import com.example.ui.theme.*
import com.example.ui.viewmodels.AlUfuqViewModel
import com.example.utils.QiblaCalculator
import com.example.utils.QiblaStatus

import kotlin.math.cos
import kotlin.math.sin

enum class WorshipTab(val titleArabic: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PRAYERS("المواقيت", Icons.Default.Mosque),
    QURAN("القرآن", Icons.Default.MenuBook),
    ADHKAR("الأذكار", Icons.Default.AutoAwesome),
    TASBEEH("المسبحة", Icons.Default.Fingerprint),
    QIBLA("القبلة", Icons.Default.Explore),
    HISN_ALMUSLIM("حصن المسلم", Icons.Default.MenuBook)
}

@Composable
fun WorshipScreen(
    viewModel: AlUfuqViewModel,
    initialTab: WorshipTab = WorshipTab.PRAYERS,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .testTag("worship_screen")
    ) {
        // Segmented Tabs Header
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MidnightNavy,
            contentColor = SacredGold,
            edgePadding = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            WorshipTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .testTag("worship_tab_${tab.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SacredGold.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.titleArabic,
                            tint = if (isSelected) SacredGold else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.titleArabic,
                            color = if (isSelected) SacredGold else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                WorshipTab.PRAYERS -> PrayerTimesTab(viewModel)
                WorshipTab.QURAN -> QuranTab(viewModel)
                WorshipTab.ADHKAR -> AdhkarTab(viewModel)
                WorshipTab.TASBEEH -> TasbeehTab(viewModel)
                WorshipTab.QIBLA -> QiblaTab(viewModel)
                WorshipTab.HISN_ALMUSLIM -> HisnAlMuslimTab(viewModel)
            }
        }
    }
}

// --- 1. Prayer Times Tab ---
@Composable
private fun PrayerTimesTab(viewModel: AlUfuqViewModel) {
    val prayerState by viewModel.prayerState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedMuezzin by viewModel.selectedMuezzin.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val locationStatusText by viewModel.locationStatusText.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header Info Card with Hijri Date, City & Weather
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = prayerState.hijriDateString,
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmIvory.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = locationStatusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmIvory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Weather Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardNavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${weatherState.iconEmoji} ${weatherState.temperatureCelcius}°م • ${weatherState.conditionArabic}",
                                color = WarmIvory,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💨 ${weatherState.windSpeedKm} كم/س",
                                color = SacredGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            // Digital Countdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "متبقٍ على صلاة ${prayerState.nextPrayer.nameArabic}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SacredGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prayerState.remainingTimeString,
                        style = MaterialTheme.typography.displayLarge,
                        color = WarmIvory,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        items(prayerState.prayers) { prayer ->
            val isNext = prayer.isNext
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isNext) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SacredGold.copy(alpha = 0.18f),
                                        TerracottaSunset.copy(alpha = 0.08f)
                                    )
                                )
                            )
                        } else {
                            Modifier.background(CardNavySurface.copy(alpha = 0.6f))
                        }
                    )
                    .border(
                        1.dp,
                        if (isNext) SacredGold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNext) SacredGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (prayer.nameArabic) {
                                    "الفجر" -> "🌅"
                                    "الشروق" -> "☀️"
                                    "الظهر" -> "☀️"
                                    "العصر" -> "🌤️"
                                    "المغرب" -> "🌇"
                                    else -> "🌙"
                                },
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = prayer.nameArabic,
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmIvory,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = prayer.nameEnglish,
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmIvory.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.timeFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isNext) SacredGold else WarmIvory.copy(alpha = if (prayer.isCompleted) 0.4f else 0.85f),
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = {
                                if (audioState.isPlaying) {
                                    viewModel.stopAudioPreview()
                                } else {
                                    viewModel.playAdhanPreview(selectedMuezzin.audioUrl)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "استماع للأذان",
                                tint = SacredGold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- 2. Quran Tab ---
@Composable
private fun QuranTab(viewModel: AlUfuqViewModel) {
    val surahsList by viewModel.surahsList.collectAsState()
    val isQuranLoading by viewModel.isQuranLoading.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val readingVerses by viewModel.readingVerses.collectAsState()
    val selectedSurahNum by viewModel.selectedSurahNumber.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedJuzFilter by remember { mutableStateOf("الكل") }
    var isReadingMode by remember { mutableStateOf(false) }
    var isParchmentTheme by remember { mutableStateOf(true) }

    val currentSurah = surahsList.find { it.number == selectedSurahNum }

    if (isReadingMode) {
        // Full Surah Quran Reader View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isParchmentTheme) ParchmentBg else ObsidianNavy)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isReadingMode = false }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = if (isParchmentTheme) ParchmentDarkText else WarmIvory
                    )
                }

                Text(
                    text = currentSurah?.nameArabic ?: "القرآن الكريم",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isParchmentTheme) ParchmentDarkText else SacredGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Row {
                    IconButton(onClick = { isParchmentTheme = !isParchmentTheme }) {
                        Icon(
                            imageVector = if (isParchmentTheme) Icons.Default.NightsStay else Icons.Default.WbSunny,
                            contentDescription = "تغيير المظهر",
                            tint = if (isParchmentTheme) ParchmentDarkText else SacredGold
                        )
                    }

                    IconButton(
                        onClick = {
                            if (audioState.isPlaying) {
                                viewModel.stopAudioPreview()
                            } else {
                                viewModel.playSurahAudio(selectedSurahNum)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "استماع للسورة",
                            tint = SacredGold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Bismillah Header — hidden for At-Tawbah (no Bismillah at all) and
                    // Al-Fatiha (its verse 1 IS the Bismillah, shown in the list below;
                    // a separate header there would duplicate it).
                    if (selectedSurahNum != 9 && selectedSurahNum != 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineLarge,
                                color = if (isParchmentTheme) TerracottaSunset else SacredGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(readingVerses) { verse ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isParchmentTheme) Color.White.copy(alpha = 0.5f) else CardNavySurface
                            )
                            .border(
                                1.dp,
                                if (isParchmentTheme) ParchmentBorder else Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(TerracottaSunset.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${verse.verseNumber}",
                                            color = TerracottaSunset,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val isBookmarked = bookmarks.any { b ->
                                        b.surahNumber == verse.surahNumber && b.ayahNumber == verse.verseNumber
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                verse.surahNumber,
                                                currentSurah?.nameArabic ?: "",
                                                verse.verseNumber,
                                                verse.textArabic
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "حفظ الآية",
                                            tint = if (isBookmarked) SacredGold else (if (isParchmentTheme) ParchmentDarkText.copy(alpha = 0.5f) else WarmIvory.copy(alpha = 0.5f))
                                        )
                                    }
                                }

                                Text(
                                    text = "﴿${verse.verseNumber}﴾",
                                    color = if (isParchmentTheme) TerracottaSunset else SacredGold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = verse.textArabic,
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (isParchmentTheme) ParchmentDarkText else WarmIvory,
                                lineHeight = 38.sp,
                                textAlign = TextAlign.Right,
                                fontSize = 21.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    } else {
        // Surah Index View
        val filteredSurahs = surahsList.filter {
            it.nameArabic.contains(searchQuery) || it.nameEnglish.contains(searchQuery, ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Quran Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("🔍 ابحث عن سورة...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SacredGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = CardNavySurface,
                    unfocusedContainerColor = CardNavySurface,
                    focusedTextColor = WarmIvory,
                    unfocusedTextColor = WarmIvory
                )
            )

            // Juz Scroll Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                val juzOptions = listOf("الكل", "جزء ١", "جزء ٢", "جزء ٣", "جزء ٤", "جزء ٥")
                items(juzOptions) { juz ->
                    val isSelected = selectedJuzFilter == juz
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SacredGold else CardNavySurface)
                            .clickable { selectedJuzFilter = juz }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = juz,
                            color = if (isSelected) MidnightNavy else WarmIvory,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSurahs) { surah ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardNavySurface.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.openSurah(surah.number)
                                isReadingMode = true
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rotated Diamond Number Frame
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerracottaSunset.copy(alpha = 0.15f))
                                    .border(1.dp, TerracottaSunset.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${surah.number}",
                                    color = TerracottaSunset,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = surah.nameArabic,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WarmIvory,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${surah.revelationType} • ${surah.totalVerses} آية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmIvory.copy(alpha = 0.45f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "﴿",
                            color = SacredGold.copy(alpha = 0.5f),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// --- 3. Adhkar Tab ---
@Composable
private fun AdhkarTab(viewModel: AlUfuqViewModel) {
    val categories = viewModel.adhkarCategories
    val activeCategory by viewModel.activeAdhkarCategory.collectAsState()
    val activeItems by viewModel.activeAdhkarItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = cat.titleArabic == activeCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SacredGold else CardNavySurface)
                        .clickable { viewModel.selectAdhkarCategory(cat.titleArabic) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat.titleArabic,
                        color = if (isSelected) MidnightNavy else WarmIvory,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeItems) { item ->
                val isDone = item.currentCount >= item.targetCount
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDone) EmeraldGreen.copy(alpha = 0.12f) else CardNavySurface.copy(alpha = 0.7f))
                        .border(
                            1.dp,
                            if (isDone) LightEmerald.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = item.textArabic,
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmIvory,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Right,
                            fontSize = 17.sp
                        )

                        if (item.benefit.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "الفضل: ${item.benefit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SacredGold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المرجع: ${item.reference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmIvory.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )

                            Button(
                                onClick = { viewModel.incrementAdhkarItem(item.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDone) EmeraldGreen else SacredGold,
                                    contentColor = if (isDone) WarmIvory else MidnightNavy
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isDone) "تمت التلاوة ✓" else "${item.currentCount} / ${item.targetCount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// --- 4. Tasbeeh Tab ---
@Composable
private fun TasbeehTab(viewModel: AlUfuqViewModel) {
    val count by viewModel.tasbeehCount.collectAsState()
    val target by viewModel.tasbeehTarget.collectAsState()
    val title by viewModel.selectedDhikrTitle.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))

            // Presets Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ" to 33,
                    "الْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِينَ" to 33,
                    "اللّٰهُ أَكْبَرُ كَبِيرًا" to 33,
                    "أَسْتَغْفِرُ اللّٰهَ الْعَظِيمَ" to 100
                )
                items(presets) { preset ->
                    val isSelected = title == preset.first
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SacredGold.copy(alpha = 0.25f) else CardNavySurface)
                            .border(
                                1.dp,
                                if (isSelected) SacredGold else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.setTasbeehPreset(preset.first, preset.second) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.first,
                            color = WarmIvory,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = WarmIvory,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        }

        // Circular 33-Bead Ring Canvas
        val haptics = LocalHapticFeedback.current
        Box(
            modifier = Modifier
                .size(240.dp)
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.incrementTasbeeh()
                }
                .testTag("tasbeeh_bead_ring"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 20f
                val totalBeads = 33

                // Subtle radial glow so the ring doesn't sit on bare background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SacredGold.copy(alpha = 0.07f), Color.Transparent),
                        center = center,
                        radius = radius * 1.25f
                    ),
                    radius = radius * 1.25f,
                    center = center
                )

                // Marker ticks at 0/11/22 — a real tasbih string has a distinct
                // separator bead every 11, dividing the 33 into three sets.
                for (marker in listOf(0, 11, 22)) {
                    val angle = (marker.toFloat() / totalBeads.toFloat()) * 2f * Math.PI - Math.PI / 2f
                    val innerX = center.x + (radius - 14f) * cos(angle).toFloat()
                    val innerY = center.y + (radius - 14f) * sin(angle).toFloat()
                    val outerX = center.x + (radius + 10f) * cos(angle).toFloat()
                    val outerY = center.y + (radius + 10f) * sin(angle).toFloat()
                    drawLine(
                        color = WarmIvory.copy(alpha = 0.25f),
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = 1.5f
                    )
                }

                for (i in 0 until totalBeads) {
                    val angle = (i.toFloat() / totalBeads.toFloat()) * 2f * Math.PI - Math.PI / 2f
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()

                    val isFilled = i < count
                    drawCircle(
                        color = if (isFilled) SacredGold else WarmIvory.copy(alpha = 0.15f),
                        radius = if (isFilled) 7f else 5f,
                        center = Offset(x, y)
                    )
                    if (isFilled) {
                        drawCircle(
                            color = SacredGold.copy(alpha = 0.35f),
                            radius = 12f,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.displayLarge,
                    color = WarmIvory,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "من $target",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmIvory.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        // Reset Button — outlined utility action (not a "time transition", so no Terracotta)
        OutlinedButton(
            onClick = { viewModel.resetTasbeeh() },
            border = BorderStroke(1.dp, SacredGold.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SacredGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("إعادة العداد", fontWeight = FontWeight.Bold)
        }
    }
}

// --- 5. Qibla Tab ---
@Composable
private fun QiblaTab(viewModel: AlUfuqViewModel) {
    val qiblaState by viewModel.qiblaState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Trigger haptic feedback when aligned
    LaunchedEffect(qiblaState.isAligned) {
        if (qiblaState.isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -qiblaState.deviceHeading,
        animationSpec = tween(durationMillis = 300),
        label = "dial_rotation"
    )

    val animatedNeedleRotation by animateFloatAsState(
        targetValue = qiblaState.relativeQiblaAngle,
        animationSpec = tween(durationMillis = 300),
        label = "needle_rotation"
    )

    val distanceKm = remember(qiblaState.latitude, qiblaState.longitude) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            qiblaState.latitude,
            qiblaState.longitude,
            QiblaCalculator.KAABA_LATITUDE,
            QiblaCalculator.KAABA_LONGITUDE,
            results
        )
        (results[0] / 1000f).toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "اتجاه القبلة",
                style = MaterialTheme.typography.headlineLarge,
                color = SacredGold,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val subtitleText = when (qiblaState.status) {
                QiblaStatus.CALIBRATING -> "جاري معايرة الحساس... قم بتدوير الهاتف على شكل 8 ♾️"
                QiblaStatus.LOCATING -> "جاري تحديد الموقع وحساب زاوية القبلة..."
                QiblaStatus.SENSOR_UNAVAILABLE -> "مستشعر البوصلة غير متاح (استخدام الزاوية المحسوبة)"
                QiblaStatus.PERMISSION_MISSING -> "يرجى السماح بالموقع لحساب القبلة بدقة"
                QiblaStatus.LOCATION_UNAVAILABLE -> "الموقع غير متاح حالياً"
                QiblaStatus.READY -> if (qiblaState.isAligned) "أنت باتجاه القبلة مباشرة ✨" else "وجّه أعلى الهاتف نحو الرمز الذهبي ليتطابق مع القبلة"
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = if (qiblaState.isAligned) EmeraldGreen else WarmIvory.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Live Sensor-Driven Compass Ring & Qibla Pointer
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 10f

                // Subtle radial glow behind the ring so it doesn't read as bare/empty
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SacredGold.copy(alpha = 0.08f), Color.Transparent),
                        center = center,
                        radius = radius * 1.3f
                    ),
                    radius = radius * 1.3f,
                    center = center
                )

                // Outer Ring with glow on alignment
                drawCircle(
                    color = if (qiblaState.isAligned) SacredGold.copy(alpha = 0.3f) else WarmIvory.copy(alpha = 0.12f),
                    radius = radius,
                    style = Stroke(width = if (qiblaState.isAligned) 4f else 2f)
                )

                // 72 Compass Ticks rotated by device heading
                rotate(animatedDialRotation, center) {
                    for (i in 0 until 72) {
                        val isMajor = i % 9 == 0
                        val angle = i * 5f
                        rotate(angle, center) {
                            drawLine(
                                color = WarmIvory.copy(alpha = if (isMajor) 0.6f else 0.2f),
                                start = Offset(center.x, center.y - radius),
                                end = Offset(center.x, center.y - radius + if (isMajor) 12f else 6f),
                                strokeWidth = if (isMajor) 2f else 1f
                            )
                        }
                    }
                }

                // Cardinal direction letters (N/E/S/W), rotated with the dial like the ticks
                rotate(animatedDialRotation, center) {
                    val cardinalRadius = radius - 26f
                    val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                    cardinals.forEach { (label, angle) ->
                        rotate(angle, center) {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = WarmIvory.copy(alpha = 0.5f).toArgb()
                                    textSize = 22f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                                save()
                                rotate(-angle - animatedDialRotation, center.x, center.y - cardinalRadius)
                                drawText(label, center.x, center.y - cardinalRadius + 8f, paint)
                                restore()
                            }
                        }
                    }
                }

                // Kaaba Icon Marker Position (Rotated relative to device heading)
                rotate(animatedNeedleRotation, center) {
                    drawCircle(
                        color = SacredGold,
                        radius = 10f,
                        center = Offset(center.x, center.y - radius + 18f)
                    )
                }

                // Qibla Needle Pointer (Rotated relative to device heading)
                rotate(animatedNeedleRotation, center) {
                    val needlePath = Path().apply {
                        moveTo(center.x, center.y - radius + 30f)
                        lineTo(center.x - 7f, center.y)
                        lineTo(center.x + 7f, center.y)
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        brush = Brush.verticalGradient(
                            listOf(
                                if (qiblaState.isAligned) EmeraldGreen else SacredGold,
                                TerracottaSunset
                            )
                        )
                    )
                }
            }

            // Compass Center Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ObsidianNavy)
                    .border(
                        1.dp,
                        if (qiblaState.isAligned) EmeraldGreen else SacredGold.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (qiblaState.isAligned) "🕋" else "🧭", fontSize = 20.sp)
            }
        }

        // Readout & Stats
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${qiblaState.qiblaBearing.toInt()}°",
                style = MaterialTheme.typography.displayMedium,
                color = WarmIvory,
                fontWeight = FontWeight.Light,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$distanceKm كم", color = SacredGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("المسافة للكعبة", color = WarmIvory.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(qiblaState.locationName.ifEmpty { selectedCity }, color = SacredGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("موقعك الحالي", color = WarmIvory.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// --- 6. Hisn Al-Muslim Tab (full reference: 132 categories from the book) ---
@Composable
private fun HisnAlMuslimTab(viewModel: AlUfuqViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var categories by remember { mutableStateOf<List<HisnCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategoryId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        categories = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.data.local.HisnAlMuslimLoader.load(context)
        }
        isLoading = false
    }

    val filtered = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { cat ->
            cat.category.contains(searchQuery) || cat.items.any { it.text.contains(searchQuery) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في حصن المسلم (١٣٢ بابًا)...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WarmIvory.copy(alpha = 0.5f)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SacredGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = WarmIvory,
                unfocusedTextColor = WarmIvory,
                cursorColor = SacredGold
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SacredGold, modifier = Modifier.size(28.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { cat ->
                    val isExpanded = expandedCategoryId == cat.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardNavySurface)
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                            .clickable { expandedCategoryId = if (isExpanded) null else cat.id }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.category,
                                color = WarmIvory,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${cat.items.size}",
                                color = SacredGold,
                                fontSize = 11.sp
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = WarmIvory.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            cat.items.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = item.text,
                                        color = WarmIvory.copy(alpha = 0.9f),
                                        fontSize = 14.sp,
                                        lineHeight = 24.sp
                                    )
                                    if (item.count > 1) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "يُقال ${item.count} مرات",
                                            color = TerracottaSunset,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                if (item != cat.items.last()) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}


