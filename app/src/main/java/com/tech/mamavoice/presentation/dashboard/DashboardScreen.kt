package com.tech.mamavoice.presentation.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tech.mamavoice.R
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.util.Resource
import com.tech.mamavoice.presentation.components.MamaVoiceLogoMark
import com.tech.mamavoice.ui.theme.MamaTheme

/**
 * Home tab — voice-first landing screen.
 * Greeting card + pulsing mic + quick suggestion chips. Purely presentational; the mic and chips
 * open the full-screen conversation via callbacks hoisted to
 * [com.tech.mamavoice.presentation.main.MainScreen].
 */
@Composable
fun HomeScreen(
    dashboardData: Resource<DashboardResponse>,
    onMicClick: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar: brand + profile avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MamaVoiceLogoMark(
                containerSize = 28.dp,
                logoSize = 22.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MamaVoice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            // Profile avatar lives in MainScreen as a static top-end overlay shared by every tab;
            // leaving the trailing space keeps the brand aligned left, clear of that avatar.
            Spacer(modifier = Modifier.size(40.dp))
        }

        // Greeting card
        GreetingCard(dashboardData)

        // Center mic
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PulsingMic(onClick = onMicClick)
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.home_tap_to_speak),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_speak_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                SuggestionChip(stringResource(R.string.suggestion_ugu), onSuggestionClick)
                Spacer(modifier = Modifier.height(10.dp))
                SuggestionChip(stringResource(R.string.suggestion_baby_size), onSuggestionClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingCard(dashboardData: Resource<DashboardResponse>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MamaTheme.colors.voiceGradientStart,
                        MamaTheme.colors.voiceGradientEnd
                    )
                )
            )
            .padding(20.dp)
    ) {
        when (dashboardData) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                }
            }
            is Resource.Error -> {
                Text(
                    text = dashboardData.message ?: stringResource(R.string.dashboard_load_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
            is Resource.Success -> {
                val data = dashboardData.data
                val vaccineTiming = when (val d = data?.daysToNextVaccine ?: -1) {
                    0 -> stringResource(R.string.vaccine_today)
                    1 -> stringResource(R.string.vaccine_tomorrow)
                    in 1..Int.MAX_VALUE -> stringResource(R.string.vaccine_in_days, d)
                    else -> "—"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_greeting, data?.firstName ?: stringResource(R.string.home_default_name)),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data?.statusText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.vaccine_next_label, vaccineTiming),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    WeekBadge(week = data?.currentWeek ?: 0)
                }
            }
        }
    }
}

@Composable
private fun WeekBadge(week: Int) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(3.dp, androidx.compose.ui.graphics.Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = stringResource(R.string.weeks_label),
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun PulsingMic(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MamaTheme.colors.voiceGradientEnd
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = stringResource(R.string.cd_microphone),
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        // The label carries display quotes (e.g. "Is ugu safe?"); send the query without them.
        modifier = Modifier.clickable { onClick(text.trim('"')) }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
