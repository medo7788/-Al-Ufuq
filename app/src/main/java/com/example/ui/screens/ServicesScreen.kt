package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.AlUfuqViewModel

@Composable
fun ServicesScreen(
    viewModel: AlUfuqViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("zakat") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .testTag("services_screen")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
        ) {
            Text(
                text = "الخدمات الإسلامية",
                style = MaterialTheme.typography.headlineLarge,
                color = WarmIvory,
                fontWeight = FontWeight.Bold
            )
        }

        // Sub Navigation
        TabRow(
            selectedTabIndex = if (selectedSection == "zakat") 0 else 1,
            containerColor = MidnightNavy,
            contentColor = SacredGold
        ) {
            Tab(
                selected = selectedSection == "zakat",
                onClick = { selectedSection = "zakat" },
                text = { Text("حاسبة الزكاة", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                modifier = Modifier.testTag("services_tab_zakat")
            )
            Tab(
                selected = selectedSection == "calendar",
                onClick = { selectedSection = "calendar" },
                text = { Text("التقويم الهجري", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.testTag("services_tab_calendar")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedSection == "zakat") {
                ZakatSection(viewModel)
            } else {
                CalendarSection()
            }
        }
    }
}

@Composable
private fun ZakatSection(viewModel: AlUfuqViewModel) {
    var cashText by remember { mutableStateOf("100000") }
    var goldGramsText by remember { mutableStateOf("10") }
    var goldPriceText by remember { mutableStateOf("3800") }
    var debtsText by remember { mutableStateOf("0") }

    val cash = cashText.toDoubleOrNull() ?: 0.0
    val goldGrams = goldGramsText.toDoubleOrNull() ?: 0.0
    val goldPrice = goldPriceText.toDoubleOrNull() ?: 3800.0
    val debts = debtsText.toDoubleOrNull() ?: 0.0

    val nisabValue = 85.0 * goldPrice // Nisab is 85 grams of 24k gold
    val totalAssets = cash + (goldGrams * goldPrice) - debts
    val isNisabReached = totalAssets >= nisabValue
    val zakatPayable = if (isNisabReached) totalAssets * 0.025 else 0.0

    val zakatRecords by viewModel.zakatRecords.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Zakat Hero Card from HTML Design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                TerracottaSunset.copy(alpha = 0.18f),
                                EmeraldGreen.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, SacredGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "الزكاة المستحقة",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmIvory.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${String.format("%,.0f", zakatPayable)} ج.م",
                        style = MaterialTheme.typography.displayMedium,
                        color = SacredGold,
                        fontWeight = FontWeight.Light,
                        fontSize = 38.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "٢٫٥٪ من إجمالي المال الزكوي",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            // Nisab Status Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EmeraldGreen.copy(alpha = 0.18f))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "النصاب الحالي (بسعر الذهب اليوم)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmIvory.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )

                    Text(
                        text = if (isNisabReached) "مستوفى ✓" else "دون النصاب",
                        color = if (isNisabReached) LightEmerald else SacredGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "بيانات المال والمدخرات",
                style = MaterialTheme.typography.titleMedium,
                color = WarmIvory,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = cashText,
                onValueChange = { cashText = it },
                label = { Text("النقد والمدخرات (جنيه)", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
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
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = goldGramsText,
                    onValueChange = { goldGramsText = it },
                    label = { Text("الذهب (جرام 24)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
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

                OutlinedTextField(
                    value = goldPriceText,
                    onValueChange = { goldPriceText = it },
                    label = { Text("سعر الجرام (ج.م)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
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
            }
        }

        item {
            OutlinedTextField(
                value = debtsText,
                onValueChange = { debtsText = it },
                label = { Text("الديون المستحقة عليك (جنيه)", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
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
        }

        item {
            Button(
                onClick = {
                    viewModel.saveZakatCalculation(cash, goldGrams, goldPrice, debts, zakatPayable)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SacredGold, contentColor = MidnightNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ حاسبة الزكاة في السجل", fontWeight = FontWeight.Bold)
            }
        }

        if (zakatRecords.isNotEmpty()) {
            item {
                Text(
                    text = "سجل الحسابات السابقة",
                    style = MaterialTheme.typography.titleMedium,
                    color = SacredGold,
                    fontWeight = FontWeight.Bold
                )
            }

            items(zakatRecords) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "زكاة واجبة: ${String.format("%,.0f", record.totalZakatPayable)} ج.م",
                                color = WarmIvory,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "إجمالي المال: ${String.format("%,.0f", record.cashAmount + record.gold24kGrams * record.goldPriceGram)} ج.م",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Text("محفوظ ✓", color = LightEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun CalendarSection() {
    val events = listOf(
        Pair("بداية شهر رمضان المبارك", "1 رمضان 1448 هـ"),
        Pair("عيد الفطر السعيد", "1 شوال 1448 هـ"),
        Pair("يوم عرفة - الوقوف بعرفة", "9 ذو الحجة 1448 هـ"),
        Pair("عيد الأضحى المبارك", "10 ذو الحجة 1448 هـ"),
        Pair("رأس السنة الهجرية", "1 محرم 1449 هـ")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardNavySurface)
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "التقويم الهجري والمناسبات المباركة",
                        style = MaterialTheme.typography.titleLarge,
                        color = SacredGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مواعيد أهم الأيام والمناسبات الإسلامية في العام الهجري الحالي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(events) { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardNavySurface.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = SacredGold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = event.first,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmIvory,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SacredGold.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.second,
                        color = SacredGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
