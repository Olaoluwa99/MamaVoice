package com.tech.mamavoice.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.presentation.dashboard.DashboardViewModel
import com.tech.mamavoice.presentation.dashboard.HomeScreen
import com.tech.mamavoice.presentation.dashboard.VoiceAssistantBottomSheetContent
import com.tech.mamavoice.presentation.components.MamaBottomBar
import com.tech.mamavoice.presentation.food.FoodDirectoryScreen
import com.tech.mamavoice.presentation.immunization.ImmunizationTimelineScreen
import com.tech.mamavoice.presentation.navigation.MainTab
import com.tech.mamavoice.presentation.tracker.HealthTrackerScreen

/**
 * Bottom-navigation host for the main app experience.
 *
 * Owns the voice assistant pipeline (speech recognizer, mic permission, TTS overlay) so the
 * central "Speak" button works from any tab. Hosts Home · Food · Vaccines · Health.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    val dashboardData by viewModel.dashboardData.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val showVoiceOverlay by viewModel.showVoiceOverlay.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val draftQuery by viewModel.draftQuery.collectAsState()
    val isPlayingTts by viewModel.isPlayingTts.collectAsState()

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceOverlay(true)
            startListening(speechRecognizer, viewModel)
        }
    }

    fun openVoice(prefill: String? = null) {
        prefill?.let { viewModel.updateDraftQuery(it) }
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.toggleVoiceOverlay(true)
            if (prefill == null) startListening(speechRecognizer, viewModel)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { viewModel.setRecordingState(false) }
            override fun onError(error: Int) { viewModel.setRecordingState(false) }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.updateDraftQuery(matches[0])
                }
                viewModel.setRecordingState(false)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.updateDraftQuery(matches[0])
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    Scaffold(
        bottomBar = {
            MamaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSpeakClick = { openVoice() },
                speakActive = showVoiceOverlay
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
                    onMicClick = { openVoice() },
                    onSuggestionClick = { openVoice(prefill = it) }
                )
                MainTab.FOOD -> FoodDirectoryScreen()
                MainTab.VACCINES -> ImmunizationTimelineScreen()
                MainTab.HEALTH -> HealthTrackerScreen(onSpeak = { openVoice() })
            }
        }
    }

    if (showVoiceOverlay) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                speechRecognizer.stopListening()
                viewModel.toggleVoiceOverlay(false)
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            VoiceAssistantBottomSheetContent(
                isRecording = isRecording,
                isPlayingTts = isPlayingTts,
                chatHistory = chatHistory,
                draftQuery = draftQuery,
                onDraftQueryChange = { viewModel.updateDraftQuery(it) },
                onSubmitClicked = { viewModel.submitDraftQuery() },
                onMicClicked = {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.stopTts()
                        startListening(speechRecognizer, viewModel)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopTtsClicked = { viewModel.stopTts() },
                onCloseClicked = {
                    speechRecognizer.stopListening()
                    viewModel.toggleVoiceOverlay(false)
                }
            )
        }
    }
}

internal fun startListening(speechRecognizer: SpeechRecognizer, viewModel: DashboardViewModel) {
    viewModel.setRecordingState(true)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // Ask the recognizer for the user's chosen language. Built-in engine support for
        // yo/ig/ha/pcm is device-dependent (Spitch is the planned proper STT); English works.
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, viewModel.currentLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    speechRecognizer.startListening(intent)
}
