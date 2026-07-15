package com.tech.mamavoice.presentation.immunization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.R
import com.tech.mamavoice.domain.model.VaccineItem
import com.tech.mamavoice.presentation.components.ErrorState
import com.tech.mamavoice.ui.theme.MamaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImmunizationTimelineScreen(
    viewModel: ImmunizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var vaccineToLog by remember { mutableStateOf<VaccineItem?>(null) }
    var selectedVaccineDetails by remember { mutableStateOf<VaccineItem?>(null) }

    selectedVaccineDetails?.let {
        VaccineDetailSheet(vaccine = it, onDismiss = { selectedVaccineDetails = null })
    }

    vaccineToLog?.let { v ->
        LogVaccineDialog(
            vaccine = v,
            onDismiss = { vaccineToLog = null },
            onConfirm = { date, sideEffects ->
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.logVaccine(
                    vaccineId = v.id,
                    vaccineName = v.name,
                    date = date,
                    isCompleted = date <= todayStr,
                    sideEffects = sideEffects.takeIf { it.isNotBlank() }
                )
                vaccineToLog = null
            }
        )
    }

    val completedCount = state.vaccines.count { it.isCompleted }
    val total = state.vaccines.size
    val activeIndex = state.vaccines.indexOfFirst { !it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.immun_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(stringResource(R.string.immun_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                state.error != null -> ErrorState(
                    error = state.error,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.align(Alignment.Center)
                )
                state.vaccines.isEmpty() -> Text(
                    text = stringResource(R.string.immun_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ProgressSummaryCard(completed = completedCount, total = total)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    itemsIndexed(state.vaccines) { index, vaccine ->
                        TimelineItem(
                            vaccine = vaccine,
                            isLast = index == state.vaccines.size - 1,
                            isActive = index == activeIndex,
                            onMarkDone = { vaccineToLog = vaccine },
                            onClick = { selectedVaccineDetails = vaccine }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(completed: Int, total: Int) {
    val fraction = if (total > 0) completed.toFloat() / total else 0f
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.immun_progress, completed, total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.immun_on_track),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MamaTheme.colors.success
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun TimelineItem(
    vaccine: VaccineItem,
    isLast: Boolean,
    isActive: Boolean,
    onMarkDone: () -> Unit,
    onClick: () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (vaccine.isCompleted) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (vaccine.isCompleted || isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (vaccine.isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.cd_done), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                } else if (isActive) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
            }
            if (!isLast) {
                Canvas(modifier = Modifier.width(2.dp).fillMaxHeight().padding(vertical = 2.dp)) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = vaccine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isActive) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MamaTheme.colors.accentContainer) {
                            Text(
                                stringResource(R.string.immun_due_now),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MamaTheme.colors.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = stringResource(R.string.immun_due, vaccine.dueDateString),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (vaccine.isCompleted && vaccine.administeredDate != null) {
                        Text(
                            text = stringResource(R.string.immun_given, vaccine.administeredDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onMarkDone,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.immun_mark_done), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.immun_details), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
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
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vaccine_log_title)) },
        text = {
            Column {
                Text(stringResource(R.string.vaccine_logging, vaccine.name), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.field_administered_date)) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sideEffectsText,
                    onValueChange = { sideEffectsText = it },
                    label = { Text(stringResource(R.string.field_side_effects_optional)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(dateText, sideEffectsText) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineDetailSheet(vaccine: VaccineItem, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 24.dp)) {
            Text(vaccine.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.immun_due, vaccine.dueDateString), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (vaccine.isCompleted && vaccine.administeredDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.vaccine_administered, vaccine.administeredDate), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            if (!vaccine.sideEffects.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.vaccine_side_effects), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(vaccine.sideEffects, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
