package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.api.WeatherInfo
import com.example.data.models.PrayerDayState
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SacredGold
import com.example.ui.theme.TerracottaSunset
import com.example.ui.theme.WarmIvory

@Composable
fun PrayerHeroCard(
    prayerState: PrayerDayState,
    onNavigateToPrayers: () -> Unit,
    weatherInfo: WeatherInfo? = null,
    locationText: String? = null,
    modifier: Modifier = Modifier
) {
    val nextPrayer = prayerState.nextPrayer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        TerracottaSunset.copy(alpha = 0.18f),
                        EmeraldGreen.copy(alpha = 0.12f)
                    )
                )
            )
            .border(1.dp, SacredGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .clickable { onNavigateToPrayers() }
            .padding(20.dp)
            .testTag("prayer_hero_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الصلاة القادمة",
                    style = MaterialTheme.typography.labelMedium,
                    color = SacredGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SacredGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "التنبيه",
                            tint = SacredGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nextPrayer.timeFormatted,
                            color = WarmIvory,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = nextPrayer.nameArabic,
                style = MaterialTheme.typography.headlineLarge,
                color = WarmIvory,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prayerState.remainingTimeString,
                    style = MaterialTheme.typography.displayMedium,
                    color = WarmIvory,
                    fontWeight = FontWeight.Light,
                    fontSize = 36.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "متبقي",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmIvory.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }

            if (weatherInfo != null || !locationText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (weatherInfo != null) {
                        Text(
                            text = "${weatherInfo.iconEmoji} ${weatherInfo.temperatureCelcius}°م • ${weatherInfo.conditionArabic}",
                            color = WarmIvory,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!locationText.isNullOrEmpty()) {
                        Text(
                            text = locationText,
                            color = SacredGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
