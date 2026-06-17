package com.tech.mamavoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tech.mamavoice.data.local.AppTheme
import com.tech.mamavoice.data.local.SettingsManager
import com.tech.mamavoice.presentation.navigation.MamaVoiceNavGraph
import com.tech.mamavoice.presentation.navigation.Screen
import com.tech.mamavoice.ui.theme.MamaVoiceTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.graphics.Color as AndroidColor


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by settingsManager.appTheme.collectAsState(initial = AppTheme.SYSTEM)
            
            val darkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }

            MamaVoiceTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    MamaVoiceNavGraph(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}