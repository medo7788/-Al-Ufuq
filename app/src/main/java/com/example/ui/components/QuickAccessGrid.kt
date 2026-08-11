package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmIvory

data class ToolGridItem(
    val id: String,
    val titleArabic: String,
    val emoji: String,
    val testTag: String
)

@Composable
fun QuickAccessGrid(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        ToolGridItem("qibla", "القبلة", "🕋", "tool_qibla"),
        ToolGridItem("quran", "القرآن", "📖", "tool_quran"),
        ToolGridItem("adhkar", "الأذكار", "📿", "tool_adhkar"),
        ToolGridItem("calendar", "التقويم الهجري", "🌙", "tool_calendar"),
        ToolGridItem("zakat", "الزكاة", "💰", "tool_zakat"),
        ToolGridItem("adhan_settings", "إعدادات الأذان", "🔔", "tool_adhan_settings"),
        ToolGridItem("muezzin_voice", "صوت المؤذن", "🎙️", "tool_muezzin_voice"),
        ToolGridItem("settings", "الإعدادات", "⚙️", "tool_settings")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "كل الأدوات",
            style = MaterialTheme.typography.titleMedium,
            color = WarmIvory,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2 rows of 4 columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.take(4).forEach { item ->
                ToolCard(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.drop(4).take(4).forEach { item ->
                ToolCard(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    item: ToolGridItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3ECE0).copy(alpha = 0.06f))
                .border(1.dp, Color(0xFFF3ECE0).copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.emoji,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.titleArabic,
            style = MaterialTheme.typography.labelSmall,
            color = WarmIvory.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
