package com.tech.mamavoice

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
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
import com.tech.mamavoice.presentation.language.ProvideAppLocale
import com.tech.mamavoice.presentation.navigation.MamaVoiceNavGraph
import com.tech.mamavoice.presentation.navigation.Screen
import com.tech.mamavoice.ui.theme.MamaVoiceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import android.graphics.Color as AndroidColor


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // enableEdgeToEdge leaves the window background transparent (which paints black on the very
        // first frames of a cold start); set it to the themed app background so it blends in.
        window.setBackgroundDrawableResource(R.color.window_background)

        // Resolve the persisted language up front so the first frame renders in the right language
        // instead of flashing the default and correcting a frame later.
        val initialLanguage = runBlocking { settingsManager.appLanguage.first() }

        setContent {
            val appTheme by settingsManager.appTheme.collectAsState(initial = AppTheme.SYSTEM)
            val appLanguage by settingsManager.appLanguage.collectAsState(initial = initialLanguage)

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

            // Drive the UI language from the persisted choice. Changing it recomposes string
            // resources in place — no Activity recreation, so no window-recreation flash.
            ProvideAppLocale(language = appLanguage) {
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
}
