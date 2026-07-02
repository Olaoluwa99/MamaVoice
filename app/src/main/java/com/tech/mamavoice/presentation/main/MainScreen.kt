package com.tech.mamavoice.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.presentation.components.MamaBottomBar
import com.tech.mamavoice.presentation.dashboard.DashboardViewModel
import com.tech.mamavoice.presentation.dashboard.HomeScreen
import com.tech.mamavoice.presentation.food.FoodDirectoryScreen
import com.tech.mamavoice.presentation.immunization.ImmunizationTimelineScreen
import com.tech.mamavoice.presentation.navigation.MainTab
import com.tech.mamavoice.presentation.tracker.HealthTrackerScreen

/**
 * Bottom-navigation host: Home · Food · Speak · Vaccines · Health.
 *
 * The central "Speak" button (and the Home/Health voice entry points) open the full-screen
 * [com.tech.mamavoice.presentation.voice.ConversationScreen] via [onOpenConversation]; a non-null
 * argument pre-submits a suggestion as a text question.
 */
@Composable
fun MainScreen(
    onNavigateToProfile: () -> Unit,
    onOpenConversation: (String?) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val dashboardData by viewModel.dashboardData.collectAsState()

    // Device back on a non-Home tab returns to Home instead of exiting the app.
    // Composed before the tab content so a tab's own BackHandler (e.g. food detail) wins.
    BackHandler(enabled = selectedTab != MainTab.HOME) {
        selectedTab = MainTab.HOME
    }

    Scaffold(
        bottomBar = {
            MamaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSpeakClick = { onOpenConversation(null) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    dashboardData = dashboardData,
                    onProfileClick = onNavigateToProfile,
                    onMicClick = { onOpenConversation(null) },
                    onSuggestionClick = { onOpenConversation(it) }
                )
                MainTab.FOOD -> FoodDirectoryScreen()
                MainTab.VACCINES -> ImmunizationTimelineScreen()
                MainTab.HEALTH -> HealthTrackerScreen(onSpeak = { onOpenConversation(null) })
            }
        }
    }
}
