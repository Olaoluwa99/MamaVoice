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
import androidx.compose.ui.graphics.PathEffect
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
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (vaccine.isCompleted) 0.dp else 4.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = vaccine.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (vaccine.isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = "Due: ${vaccine.dueDateString}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (vaccine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
