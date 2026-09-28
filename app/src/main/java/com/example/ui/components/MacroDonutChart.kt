package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CarbGold
import com.example.ui.theme.ProteinOrange
import com.example.ui.theme.VeggieSage

@Composable
fun MacroDonutChart(
    proteinPercent: Int,
    carbPercent: Int,
    fatPercent: Int,
    totalCalories: Int,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 18.dp
) {
    val animationProgress = remember { Animatable(0f) }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(proteinPercent, carbPercent, fatPercent, totalCalories) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasMin = size.toPx()
            val arcSize = Size(canvasMin - strokePx, canvasMin - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Base track adapts dynamically to theme
            drawCircle(
                color = trackColor,
                radius = (canvasMin - strokePx) / 2f,
                center = center,
                style = Stroke(width = strokePx)
            )

            val total = (proteinPercent + carbPercent + fatPercent).coerceAtLeast(1)
            val pSweep = (proteinPercent.toFloat() / total * 360f) * animationProgress.value
            val cSweep = (carbPercent.toFloat() / total * 360f) * animationProgress.value
            val fSweep = (fatPercent.toFloat() / total * 360f) * animationProgress.value

            var currentAngle = -90f

            // Protein (Orange)
            if (pSweep > 0.5f) {
                drawArc(
                    color = ProteinOrange,
                    startAngle = currentAngle,
                    sweepAngle = pSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                currentAngle += pSweep
            }

            // Carbs (Gold)
            if (cSweep > 0.5f) {
                drawArc(
                    color = CarbGold,
                    startAngle = currentAngle,
                    sweepAngle = cSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                currentAngle += cSweep
            }

            // Fat (Sage Green / Balance)
            if (fSweep > 0.5f) {
                drawArc(
                    color = VeggieSage,
                    startAngle = currentAngle,
                    sweepAngle = fSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$totalCalories",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "kcal / dia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
