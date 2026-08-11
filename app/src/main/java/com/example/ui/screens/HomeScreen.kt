package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.ObsidianNavy
import com.example.ui.viewmodels.AlUfuqViewModel

@Composable
fun HomeScreen(
    viewModel: AlUfuqViewModel,
    onNavigateToPrayers: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToAdhkar: () -> Unit,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToJourneySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizonPhase by viewModel.horizonPhase.collectAsState()
    val prayerState by viewModel.prayerState.collectAsState()
    val quranProgress by viewModel.quranProgress.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val weatherInfo by viewModel.weatherState.collectAsState()
    val locationStatusText by viewModel.locationStatusText.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .verticalScroll(rememberScrollState())
            .testTag("home_screen")
    ) {
        // Horizon Scenic Header
        HorizonHeader(
            horizonPhase = horizonPhase,
            prayerDayState = prayerState,
            cityName = selectedCity,
            onLocationClick = onNavigateToJourneySettings
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Next Prayer Hero Card
            PrayerHeroCard(
                prayerState = prayerState,
                onNavigateToPrayers = onNavigateToPrayers,
                weatherInfo = weatherInfo,
                locationText = locationStatusText
            )

            // "الآن يناسبك" Context Scroller
            YourMomentCard(
                onChipClick = { target ->
                    when (target) {
                        "adhkar" -> onNavigateToAdhkar()
                        "qibla" -> onNavigateToQibla()
                        "zakat" -> onNavigateToZakat()
                        "quran" -> onNavigateToQuran()
                    }
                }
            )

            // "تابع رحلتك" Quran Continuation Card
            QuranContinueCard(
                progress = quranProgress,
                onContinueReading = onNavigateToQuran
            )

            // Adaptive Quick Access Grid
            QuickAccessGrid(
                onItemClick = { id ->
                    when (id) {
                        "qibla" -> onNavigateToQibla()
                        "quran" -> onNavigateToQuran()
                        "adhkar" -> onNavigateToAdhkar()
                        "calendar" -> onNavigateToPrayers()
                        "zakat" -> onNavigateToZakat()
                        "adhan_settings", "muezzin_voice", "settings" -> onNavigateToJourneySettings()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
