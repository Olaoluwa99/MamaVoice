package com.tech.mamavoice.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            is ProfileSetupUiState.Error -> {
                snackbarHostState.showSnackbar(message = (uiState as ProfileSetupUiState.Error).message)
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We'll customize MamaVoice for your journey.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = { Text("First Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = viewModel::onLastNameChange,
                label = { Text("Last Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Last Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = "Mother Stage",
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
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetDate,
                            onValueChange = { },
                            readOnly = true,
                            label = {
                                Text(if (motherStage == "Pregnant") "Expected Due Date" else "Baby's Date of Birth")
                            },
                            placeholder = { Text("YYYY-MM-DD") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { showDatePicker = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            AppDropdown(
                label = "Language Preference",
                options = appEnums.languages,
                selectedOption = language,
                onOptionSelected = viewModel::onLanguageChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppDropdown(
                label = "State",
                options = appEnums.states,
                selectedOption = state,
                onOptionSelected = viewModel::onStateChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            val lgaOptions = appEnums.stateLgas[state] ?: emptyList()
            AppDropdown(
                label = "LGA",
                options = lgaOptions,
                selectedOption = lga,
                onOptionSelected = viewModel::onLgaChange,
                enabled = state.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is ProfileSetupUiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = viewModel::onCompleteProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = firstName.isNotBlank() && lastName.isNotBlank() &&
                              motherStage.isNotBlank() && targetDate.isNotBlank() &&
                              language.isNotBlank() && state.isNotBlank() && lga.isNotBlank()
                ) {
                    Text(
                        text = "Complete Setup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
