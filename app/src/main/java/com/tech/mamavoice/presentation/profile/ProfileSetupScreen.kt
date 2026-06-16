package com.tech.mamavoice.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val firstName by viewModel.firstName.collectAsState()
    val profileType by viewModel.profileType.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileSetupUiState.Success -> {
                onSetupComplete()
            }
            is ProfileSetupUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as ProfileSetupUiState.Error).message
                )
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tell us about yourself",
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
            
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = { Text("First Name") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "First Name")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "I am a...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileTypeCard(
                    title = "Pregnant",
                    isSelected = profileType == "Pregnant",
                    onClick = { viewModel.onProfileTypeChange("Pregnant") },
                    modifier = Modifier.weight(1f)
                )
                ProfileTypeCard(
                    title = "New Mom",
                    isSelected = profileType == "New Mom",
                    onClick = { viewModel.onProfileTypeChange("New Mom") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = profileType.isNotEmpty()) {
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

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { },
                        readOnly = true,
                        label = { 
                            Text(if (profileType == "Pregnant") "Expected Due Date" else "Baby's Date of Birth") 
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
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is ProfileSetupUiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = viewModel::onCompleteProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = firstName.isNotBlank() && targetDate.isNotBlank()
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

@Composable
fun ProfileTypeCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}
