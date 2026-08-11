package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.HorizonPhase
import com.example.data.models.PrayerDayState
import com.example.ui.theme.*

@Composable
fun HorizonHeader(
    horizonPhase: HorizonPhase,
    prayerDayState: PrayerDayState,
    cityName: String = "القاهرة",
    onLocationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val startColor by animateColorAsState(
        targetValue = SkyDarkNavy,
        animationSpec = tween(durationMillis = 1000),
        label = "StartColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to SkyDarkNavy,
                        0.4f to SkyMidBlue,
                        0.68f to SkyDuskPurple,
                        1.0f to SkySunsetOrange
                    )
                )
            )
            .testTag("horizon_header")
    ) {
        // Celestial Arc Canvas Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Sky Quadratic Horizon Arc Curve
            val path = Path().apply {
                moveTo(-20f, height * 0.95f)
                quadraticTo(width * 0.5f, height * 0.05f, width + 20f, height * 0.95f)
            }

            drawPath(
                path = path,
                color = Color(0xFFF3ECE0).copy(alpha = 0.22f),
                style = Stroke(width = 2.5f)
            )

            // Draw Stars
            val starPositions = listOf(
                Offset(width * 0.1f, height * 0.2f),
                Offset(width * 0.25f, height * 0.35f),
                Offset(width * 0.45f, height * 0.15f),
                Offset(width * 0.65f, height * 0.3f),
                Offset(width * 0.85f, height * 0.18f),
                Offset(width * 0.92f, height * 0.4f)
            )
            starPositions.forEach { pos ->
                drawCircle(
                    color = Color(0xFFF3ECE0).copy(alpha = 0.6f),
                    radius = 2.5f,
                    center = pos
                )
            }

            // Draw 5 Prayer Dots along the Arc
            val dotRatios = listOf(0.12f, 0.32f, 0.5f, 0.7f, 0.88f)
            prayerDayState.prayers.take(5).forEachIndexed { index, prayer ->
                val ratio = dotRatios.getOrElse(index) { 0.5f }
                val x = width * ratio
                val y = height * (0.95f - 0.9f * (4 * ratio * (1 - ratio)))

                if (prayer.isNext) {
                    // Active Sun/Moon Glow
                    drawCircle(
                        color = SacredGold.copy(alpha = 0.35f),
                        radius = 24f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = SacredGold,
                        radius = 12f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = WarmIvory,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                } else if (prayer.isCompleted) {
                    drawCircle(
                        color = TerracottaSunset,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = Color(0xFFF3ECE0).copy(alpha = 0.35f),
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Top Status Strip (Brand Mark + Location Pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الأُفق",
                style = MaterialTheme.typography.headlineSmall,
                color = WarmIvory,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, SacredGold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .clickable { onLocationClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "الموقع",
                        tint = SacredGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "📍 $cityName",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmIvory.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Arc Prayer Labels at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
            prayerNames.forEachIndexed { index, name ->
                val isNext = prayerDayState.prayers.getOrNull(index)?.isNext == true
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNext) SacredGold else WarmIvory.copy(alpha = 0.6f),
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
        }
    }
}
