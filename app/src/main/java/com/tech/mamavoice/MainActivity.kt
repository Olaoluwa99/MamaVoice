package com.tech.mamavoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tech.mamavoice.presentation.navigation.MamaVoiceNavGraph
import com.tech.mamavoice.presentation.navigation.Screen
import com.tech.mamavoice.ui.theme.MamaVoiceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MamaVoiceTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MamaVoiceNavGraph(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}