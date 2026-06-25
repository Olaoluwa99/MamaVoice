package com.tech.mamavoice.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.presentation.components.AppDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val appEnums by viewModel.appEnums.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val motherStage by viewModel.motherStage.collectAsState()
    val language by viewModel.language.collectAsState()
    val state by viewModel.state.collectAsState()
    val lga by viewModel.lga.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileSetupUiState.Success -> onSetupComplete()
            is ProfileSetupUiState.Error -> snackbarHostState.showSnackbar((uiState as ProfileSetupUiState.Error).message)
            else -> Unit
        }
    }

    val allValid = firstName.isNotBlank() && lastName.isNotBlank() &&
        motherStage.isNotBlank() && targetDate.isNotBlank() &&
        language.isNotBlank() && state.isNotBlank() && lga.isNotBlank()

    // Completion progress (7 required fields)
    val filledCount = listOf(firstName, lastName, motherStage, targetDate, language, state, lga)
        .count { it.isNotBlank() }
    val fraction = filledCount / 7f

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = viewModel::onCompleteProfile,
                    enabled = allValid && uiState !is ProfileSetupUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is ProfileSetupUiState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Complete Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Segmented progress
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(3) { index ->
                    val filled = fraction >= (index + 1) / 3f - 0.001f || (index == 0 && fraction > 0f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Complete your profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("We'll tailor MamaVoice to your journey.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            // First + Last name
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = { Text("First name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = { Text("Last name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = "Mother stage",
                options = appEnums.motherStages,
                selectedOption = motherStage,
                onOptionSelected = viewModel::onMotherStageChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = motherStage.isNotEmpty()) {
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDatePicker = false
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(java.time.ZoneId.of("UTC"))
                                        .toLocalDate()
                                    viewModel.onTargetDateChange(date.toString())
                                }
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                    ) { DatePicker(state = datePickerState) }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formatDate(targetDate),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(if (motherStage == "Pregnant") "Expected due date" else "Baby's date of birth") },
                        placeholder = { Text("Select a date") },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = "Language",
                options = appEnums.languages,
                selectedOption = language,
                onOptionSelected = viewModel::onLanguageChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // State + LGA
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AppDropdown(
                    label = "State",
                    options = appEnums.states,
                    selectedOption = state,
                    onOptionSelected = viewModel::onStateChange,
                    modifier = Modifier.weight(1f)
                )
                AppDropdown(
                    label = "LGA",
                    options = appEnums.stateLgas[state] ?: emptyList(),
                    selectedOption = lga,
                    onOptionSelected = viewModel::onLgaChange,
                    enabled = state.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Formats an ISO date (yyyy-MM-dd) as e.g. "15 June 2026"; falls back to the raw value. */
private fun formatDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val date = java.time.LocalDate.parse(iso)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH)
        date.format(formatter)
    } catch (e: Exception) {
        iso
    }
}
