package com.tech.mamavoice.presentation.immunization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.domain.model.VaccineItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmunizationTimelineScreen(
    viewModel: ImmunizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var vaccineToLog by remember { mutableStateOf<VaccineItem?>(null) }

    var selectedVaccineDetails by remember { mutableStateOf<VaccineItem?>(null) }
    
    if (selectedVaccineDetails != null) {
        VaccineDetailSheet(
            vaccine = selectedVaccineDetails!!,
            onDismiss = { selectedVaccineDetails = null }
        )
    }

    if (vaccineToLog != null) {
        LogVaccineDialog(
            vaccine = vaccineToLog!!,
            onDismiss = { vaccineToLog = null },
            onConfirm = { date, sideEffects ->
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val isCompleted = date <= todayStr
                viewModel.logVaccine(
                    vaccineId = vaccineToLog!!.id,
                    vaccineName = vaccineToLog!!.name,
                    date = date,
                    isCompleted = isCompleted,
                    sideEffects = sideEffects.takeIf { it.isNotBlank() }
                )
                vaccineToLog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Immunization Timeline", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                state.vaccines.isEmpty() && !state.isLoading -> {
                    Text(
                        text = "No upcoming vaccines.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.vaccines) { index, vaccine ->
                            val isLast = index == state.vaccines.size - 1
                            TimelineItem(
                                vaccine = vaccine,
                                isLast = isLast,
                                onMarkCompleted = { vaccineToLog = vaccine },
                                onClick = { selectedVaccineDetails = vaccine }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    vaccine: VaccineItem,
    isLast: Boolean,
    onMarkCompleted: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            val indicatorColor = if (vaccine.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
                contentAlignment = Alignment.Center
            ) {
                if (vaccine.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
            if (!isLast) {
                val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 4f,
                        pathEffect = pathEffect
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (vaccine.isCompleted) 
                    MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (vaccine.isCompleted) 0.dp else 4.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = vaccine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = "Due: ${vaccine.dueDateString}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                
                if (vaccine.isCompleted && vaccine.administeredDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Given: ${vaccine.administeredDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!vaccine.isCompleted) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onMarkCompleted,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Completed", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogVaccineDialog(
    vaccine: VaccineItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var dateText by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var sideEffectsText by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Vaccine") },
        text = {
            Column {
                Text("Logging: ${vaccine.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Administered Date") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = sideEffectsText,
                    onValueChange = { sideEffectsText = it },
                    label = { Text("Side Effects (Optional)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(dateText, sideEffectsText) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineDetailSheet(vaccine: VaccineItem, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 24.dp)) {
            Text(vaccine.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Due: ${vaccine.dueDateString}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (vaccine.isCompleted && vaccine.administeredDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Administered: ${vaccine.administeredDate}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            if (!vaccine.sideEffects.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Side Effects:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(vaccine.sideEffects, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
