package com.tech.mamavoice.presentation.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.domain.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToFoodDirectory: () -> Unit,
    onNavigateToImmunization: () -> Unit,
    onNavigateToHealthTracker: () -> Unit
) {
    val context = LocalContext.current
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isDangerSign by viewModel.isDangerSign.collectAsState()

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(speechRecognizer, viewModel)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                viewModel.setRecordingState(false)
            }
            override fun onError(error: Int) {
                viewModel.setRecordingState(false)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.queryAi(matches[0])
                }
                viewModel.setRecordingState(false)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MamaVoice") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section
            when (val result = dashboardData) {
                is Resource.Loading -> {
                    CircularProgressIndicator()
                }
                is Resource.Error -> {
                    Text(
                        text = "Error: ${result.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is Resource.Success -> {
                    result.data?.let { data ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Hello, ${data.firstName}!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = data.statusText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Week ${data.currentWeek} | Next Vaccine in ${data.daysToNextVaccine} days (${data.nextVaccineName})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Middle Section: Microphone
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAnimation"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.primary
                    )
                    .clickable {
                        if (!isRecording) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startListening(speechRecognizer, viewModel)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            speechRecognizer.stopListening()
                            viewModel.setRecordingState(false)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(64.dp),
                    tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (aiResponse.isNotEmpty()) {
                if (isDangerSign) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = aiResponse, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiResponse,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section: Quick Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionCard(
                    title = "Food",
                    icon = Icons.Default.ShoppingCart,
                    onClick = onNavigateToFoodDirectory
                )
                QuickActionCard(
                    title = "Vaccines",
                    icon = Icons.Default.AddCircle,
                    onClick = onNavigateToImmunization
                )
                QuickActionCard(
                    title = "Health",
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToHealthTracker
                )
            }
        }
    }
}

private fun startListening(speechRecognizer: SpeechRecognizer, viewModel: DashboardViewModel) {
    viewModel.setRecordingState(true)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }
    speechRecognizer.startListening(intent)
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
