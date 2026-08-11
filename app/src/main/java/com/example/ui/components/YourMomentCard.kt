package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.WarmIvory

data class ContextChipItem(
    val id: String,
    val categoryLabel: String,
    val title: String,
    val target: String
)

@Composable
fun YourMomentCard(
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = listOf(
        ContextChipItem("adhkar", "قبل الإفطار", "أذكار المساء", "adhkar"),
        ContextChipItem("qibla", "اتجاه فوري", "بوصلة القبلة", "qibla"),
        ContextChipItem("zakat", "حساب سريع", "زكاة المال", "zakat"),
        ContextChipItem("quran", "متابعة الورد", "القرآن الكريم", "quran")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الآن يناسبك",
                style = MaterialTheme.typography.titleMedium,
                color = WarmIvory,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "حسب الوقت",
                style = MaterialTheme.typography.labelSmall,
                color = WarmIvory.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onChipClick(chip.target) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("context_chip_${chip.id}")
                ) {
                    Column {
                        Text(
                            text = chip.categoryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmIvory.copy(alpha = 0.55f),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = chip.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = WarmIvory,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
