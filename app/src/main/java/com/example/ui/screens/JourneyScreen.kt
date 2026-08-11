package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.PermissionsCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.AlUfuqViewModel

@Composable
fun JourneyScreen(
    viewModel: AlUfuqViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val goals by viewModel.dailyGoals.collectAsState()
    val quranProgress by viewModel.quranProgress.collectAsState()

    val selectedCity by viewModel.selectedCity.collectAsState()
    val calculationMethodName by viewModel.calculationMethodName.collectAsState()
    val asrSchoolName by viewModel.asrSchoolName.collectAsState()
    val selectedMuezzin by viewModel.selectedMuezzin.collectAsState()
    val notificationMode by viewModel.notificationMode.collectAsState()
    val audioState by viewModel.audioState.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .padding(horizontal = 20.dp)
            .testTag("journey_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 8.dp)
            ) {
                Column {
                    Text(
                        text = "رحلتي والإعدادات",
                        style = MaterialTheme.typography.headlineLarge,
                        color = WarmIvory,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إعدادات الأذان والمؤذن والموقع وتتبع أهدافك اليومية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Section: Permissions & GPS Location Card
        item {
            PermissionsCard(
                onLocationGranted = {
                    viewModel.detectGPSLocationAndRefresh(context)
                }
            )
        }

        // Stats Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "سلسلة القرآن",
                    value = "${quranProgress?.streakDays ?: 4} أيام",
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الأهداف اليومية",
                    value = "${goals.count { it.isCompleted }} / ${goals.size}",
                    icon = Icons.Default.TaskAlt,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الاستمرارية",
                    value = "96%",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Daily Goals Checklist
        item {
            Text(
                text = "أهدافي اليومية المباركة",
                style = MaterialTheme.typography.titleMedium,
                color = SacredGold,
                fontWeight = FontWeight.Bold
            )
        }

        items(goals) { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardNavySurface)
                    .clickable { viewModel.toggleGoal(goal.id) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = goal.isCompleted,
                        onCheckedChange = { viewModel.toggleGoal(goal.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldGreen,
                            uncheckedColor = SacredGold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.titleArabic,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmIvory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (goal.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تم ✓", color = LightEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Detailed App Settings
        item {
            Text(
                text = "إعدادات الأذان والموقع والمصادر",
                style = MaterialTheme.typography.titleMedium,
                color = SacredGold,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. City / Location
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCityDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("مدينة الإقامة والموقع", color = WarmIvory, fontWeight = FontWeight.Bold)
                            Text("📍 $selectedCity", color = SacredGold, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // 2. Calculation Method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMethodDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("طريقة حساب مواقيت الصلاة", color = WarmIvory, fontWeight = FontWeight.Bold)
                            Text(calculationMethodName, color = SacredGold, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // 3. Asr Madhhab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("مذهب صلاة العصر", color = WarmIvory, fontWeight = FontWeight.Bold)
                            Text(asrSchoolName, color = SacredGold, fontSize = 12.sp)
                        }
                        Row {
                            FilterChip(
                                selected = asrSchoolName.contains("الجمهور"),
                                onClick = { viewModel.setAsrSchool(0, "الجمهور (الشافعي / المالكي / الحنبلي)") },
                                label = { Text("الجمهور", fontSize = 10.sp) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = asrSchoolName.contains("الحنفي"),
                                onClick = { viewModel.setAsrSchool(1, "المذهب الحنفي") },
                                label = { Text("الحنفي", fontSize = 10.sp) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // 4. Muezzin Voice Selection & Audio Preview
                    Column {
                        Text("صوت المؤذن والأذان", color = WarmIvory, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        viewModel.muezzinOptions.forEach { muezzin ->
                            val isSelected = selectedMuezzin.nameArabic == muezzin.nameArabic
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SacredGold.copy(alpha = 0.18f) else Color.Transparent)
                                    .clickable { viewModel.selectMuezzin(muezzin) }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(muezzin.nameArabic, color = WarmIvory, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(muezzin.description, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                }

                                IconButton(
                                    onClick = {
                                        if (audioState.isPlaying) {
                                            viewModel.stopAudioPreview()
                                        } else {
                                            viewModel.playAdhanPreview(muezzin.audioUrl)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (audioState.isPlaying && audioState.currentTitle.contains("الأذان")) Icons.Default.PauseCircle else Icons.Default.VolumeUp,
                                        contentDescription = "معاينة",
                                        tint = SacredGold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    // 5. API Sync
                    Button(
                        onClick = { viewModel.fetchPrayerTimesFromApi() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = WarmIvory),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحديث ومزامنة البيانات من API الأفق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // City Selection Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("اختر المدينة", color = SacredGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "القاهرة" to "مصر",
                        "مكة المكرمة" to "السعودية",
                        "المدينة المنورة" to "السعودية",
                        "الرياض" to "السعودية",
                        "دبي" to "الإمارات",
                        "الكويت" to "الكويت",
                        "عمان" to "الأردن",
                        "القدس" to "فلسطين",
                        "الرباط" to "المغرب",
                        "الجزائر" to "الجزائر",
                        "إسطنبول" to "تركيا"
                    ).forEach { pair ->
                        TextButton(
                            onClick = {
                                viewModel.setCity(pair.first, pair.second)
                                showCityDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${pair.first} - ${pair.second}", color = WarmIvory)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("إلغاء", color = SacredGold)
                }
            },
            containerColor = MidnightNavy
        )
    }

    // Calculation Method Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = { Text("طريقة حساب الصلاة", color = SacredGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        5 to "الهيئة العامة المصرية للمساحة",
                        4 to "أم القرى - مكة المكرمة",
                        3 to "رابطة العالم الإسلامي",
                        2 to "الجمعية الإسلامية لشمال أمريكا (ISNA)",
                        1 to "جامعة العلوم الإسلامية بكراتشي",
                        9 to "وزارة الأوقاف بالكويت"
                    ).forEach { method ->
                        TextButton(
                            onClick = {
                                viewModel.setCalculationMethod(method.first, method.second)
                                showMethodDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(method.second, color = WarmIvory)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("إلغاء", color = SacredGold)
                }
            },
            containerColor = MidnightNavy
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardNavySurface)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SacredGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = WarmIvory,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
