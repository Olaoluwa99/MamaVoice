package com.tech.mamavoice.presentation.voice

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Draws a centered row of rounded bars whose heights come from [levels] (each 0..1).
 * No third-party dependency — pure [Canvas]. Used live during recording (mic amplitude)
 * and, via [SpeakingWaveform], as a decorative indicator during playback.
 */
@Composable
fun VoiceWaveform(
    levels: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = 4.dp,
    gap: Dp = 4.dp,
    height: Dp = 48.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (levels.isEmpty()) return@Canvas
        val barPx = barWidth.toPx()
        val gapPx = gap.toPx()
        val step = barPx + gapPx
        val maxBars = ((size.width + gapPx) / step).toInt().coerceAtLeast(1)
        val shown = if (levels.size > maxBars) levels.takeLast(maxBars) else levels
        val totalWidth = shown.size * step - gapPx
        var x = (size.width - totalWidth) / 2f + barPx / 2f
        val centerY = size.height / 2f
        val minH = barPx // never fully collapse

        shown.forEach { level ->
            val barHeight = max(minH, level.coerceIn(0f, 1f) * size.height)
            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = barPx,
                cap = StrokeCap.Round
            )
            x += step
        }
    }
}

/** A small looping waveform used while a reply's audio is playing ("Speaking…"). */
@Composable
fun SpeakingWaveform(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 5
) {
    val transition = rememberInfiniteTransition(label = "speaking")
    val levels = (0 until barCount).map { index ->
        val phase = 350 + index * 120
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(phase),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }
    VoiceWaveform(
        levels = levels.map { it.value },
        modifier = modifier,
        color = color,
        barWidth = 3.dp,
        gap = 3.dp,
        height = 16.dp
    )
}
