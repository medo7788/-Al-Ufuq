package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.SacredGold
import com.example.ui.theme.WarmIvory

enum class NavTab(
    val route: String,
    val titleArabic: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    WORSHIP("worship", "العبادات", Icons.Filled.Mosque, Icons.Outlined.Mosque, "nav_tab_worship"),
    SERVICES("services", "الخدمات", Icons.Filled.Widgets, Icons.Outlined.Widgets, "nav_tab_services"),
    JOURNEY("journey", "رحلتي", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_journey")
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .background(ObsidianNavy)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = MidnightNavy,
        tonalElevation = 8.dp
    ) {
        NavTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.titleArabic
                    )
                },
                label = {
                    Text(
                        text = tab.titleArabic,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SacredGold,
                    selectedTextColor = SacredGold,
                    indicatorColor = SacredGold.copy(alpha = 0.18f),
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
