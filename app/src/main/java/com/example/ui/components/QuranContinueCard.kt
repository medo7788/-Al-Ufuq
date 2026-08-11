package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuranProgressEntity
import com.example.ui.theme.CardNavySurface
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SacredGold
import com.example.ui.theme.WarmIvory

@Composable
fun QuranContinueCard(
    progress: QuranProgressEntity?,
    onContinueReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surahName = progress?.surahNameArabic ?: "سورة الكهف"
    val verseNum = progress?.verseNumber ?: 18
    val totalVerses = progress?.totalVersesInSurah ?: 110
    val streakDays = progress?.streakDays ?: 4
    val percent = (verseNum.toFloat() / totalVerses.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SacredGold.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .testTag("quran_continue_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightNavy)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardNavySurface,
                            MidnightNavy
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "القرآن الكريم",
                        tint = SacredGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تابع رحلتك القرآنية",
                        style = MaterialTheme.typography.titleMedium,
                        color = SacredGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SacredGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "سلسلة $streakDays أيام متتالية",
                        style = MaterialTheme.typography.labelSmall,
                        color = SacredGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = surahName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = WarmIvory,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الآية $verseNum من $totalVerses • الجزء 15",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                IconButton(
                    onClick = onContinueReading,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SacredGold)
                        .testTag("continue_reading_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "متابعة القراءة",
                        tint = MidnightNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SacredGold,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
