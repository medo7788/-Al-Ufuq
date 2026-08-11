package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.AlUfuqDatabase
import com.example.data.repository.AlUfuqRepository
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavTab
import com.example.ui.screens.*
import com.example.ui.theme.AlUfuqTheme
import com.example.ui.viewmodels.AlUfuqViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val database = remember { AlUfuqDatabase.getDatabase(context) }
            val repository = remember { AlUfuqRepository(database.alUfuqDao()) }
            val viewModel = remember { AlUfuqViewModel(application, repository) }

            var currentTab by remember { mutableStateOf(NavTab.HOME) }
            var worshipInitialTab by remember { mutableStateOf(WorshipTab.PRAYERS) }
            var servicesInitialTab by remember { mutableStateOf("zakat") }

            AlUfuqTheme(darkTheme = true) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            selectedTab = currentTab,
                            onTabSelected = { tab ->
                                currentTab = tab
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            NavTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToPrayers = {
                                    worshipInitialTab = WorshipTab.PRAYERS
                                    currentTab = NavTab.WORSHIP
                                },
                                onNavigateToQuran = {
                                    worshipInitialTab = WorshipTab.QURAN
                                    currentTab = NavTab.WORSHIP
                                },
                                onNavigateToAdhkar = {
                                    worshipInitialTab = WorshipTab.ADHKAR
                                    currentTab = NavTab.WORSHIP
                                },
                                onNavigateToTasbeeh = {
                                    worshipInitialTab = WorshipTab.TASBEEH
                                    currentTab = NavTab.WORSHIP
                                },
                                onNavigateToQibla = {
                                    worshipInitialTab = WorshipTab.QIBLA
                                    currentTab = NavTab.WORSHIP
                                },
                                onNavigateToZakat = {
                                    servicesInitialTab = "zakat"
                                    currentTab = NavTab.SERVICES
                                },
                                onNavigateToJourneySettings = {
                                    currentTab = NavTab.JOURNEY
                                }
                            )

                            NavTab.WORSHIP -> WorshipScreen(
                                viewModel = viewModel,
                                initialTab = worshipInitialTab
                            )

                            NavTab.SERVICES -> ServicesScreen(
                                viewModel = viewModel
                            )

                            NavTab.JOURNEY -> JourneyScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
