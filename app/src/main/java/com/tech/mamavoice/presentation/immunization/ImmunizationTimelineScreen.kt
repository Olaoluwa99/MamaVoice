package com.tech.mamavoice.presentation.immunization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.mamavoice.domain.model.VaccineItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmunizationTimelineScreen(
    viewModel: ImmunizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Immunization Timeline", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                state.vaccines.isEmpty() -> {
                    Text(
                        text = "No upcoming vaccines.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.vaccines) { index, vaccine ->
                            val isLast = index == state.vaccines.size - 1
                            TimelineItem(
                                vaccine = vaccine,
                                isLast = isLast,
                                onMarkCompleted = { viewModel.markVaccineCompleted(vaccine.id) }
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
    onMarkCompleted: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            val indicatorColor = if (vaccine.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
                contentAlignment = Alignment.Center
            ) {
                if (vaccine.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (!isLast) {
                val lineColor = MaterialTheme.colorScheme.outlineVariant
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
                        strokeWidth = size.width
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (vaccine.isCompleted) 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (vaccine.isCompleted) 0.dp else 4.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = vaccine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${vaccine.dueDateString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                )

                if (!vaccine.isCompleted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onMarkCompleted,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Completed")
                    }
                }
            }
        }
    }
}
