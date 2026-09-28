package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CarbGold
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ProteinOrange
import com.example.ui.theme.VeggieSage

/**
 * Brand visual: The divided nutrition plate.
 * - Half (180 deg) in sage green (#8FBF8B) representing vegetables and micronutrients
 * - One quarter (90 deg) in orange (#F2994A) representing protein
 * - One quarter (90 deg) in gold (#F6C453) representing carbohydrates
 * - Central dark hub (#16261F) with fork and knife emblem
 * - Adapts cleanly to both light and dark themes using theme tokens for rim and dividers.
 */
@Composable
fun NutritionPlateGraphic(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    showCenterHub: Boolean = true,
    animated: Boolean = true
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = if (animated) 800 else 0),
        label = "plate_anim"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val dividerColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val outerRadius = (canvasWidth.coerceAtMost(canvasHeight) / 2f) - 6.dp.toPx()
            val plateDiameter = outerRadius * 2f
            val plateTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)
            val plateSize = Size(plateDiameter, plateDiameter)

            // Outer plate rim (adapts to surface & outline)
            drawCircle(
                color = surfaceColor,
                radius = outerRadius + 4.dp.toPx(),
                center = center
            )
            drawCircle(
                color = outlineColor,
                radius = outerRadius + 4.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer plate inner margin ring
            drawCircle(
                color = outlineVariantColor,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 1. Half plate: Sage Green (Vegetables / Balance) - left hemisphere
            drawArc(
                color = VeggieSage,
                startAngle = 90f,
                sweepAngle = 180f * progress,
                useCenter = true,
                topLeft = plateTopLeft,
                size = plateSize,
                style = Fill
            )

            // 2. Quarter plate: Protein Orange - top right
            drawArc(
                color = ProteinOrange,
                startAngle = 270f,
                sweepAngle = 90f * progress,
                useCenter = true,
                topLeft = plateTopLeft,
                size = plateSize,
                style = Fill
            )

            // 3. Quarter plate: Carb Gold - bottom right
            drawArc(
                color = CarbGold,
                startAngle = 0f,
                sweepAngle = 90f * progress,
                useCenter = true,
                topLeft = plateTopLeft,
                size = plateSize,
                style = Fill
            )

            // Division dividers between sections for crisp graphic definition
            val dividerStroke = 3.dp.toPx()
            // Divider 1: Top vertical line (between Veggie and Protein)
            drawLine(
                color = dividerColor,
                start = center,
                end = Offset(center.x, center.y - outerRadius),
                strokeWidth = dividerStroke
            )
            // Divider 2: Bottom vertical line (between Veggie and Carbs)
            drawLine(
                color = dividerColor,
                start = center,
                end = Offset(center.x, center.y + outerRadius),
                strokeWidth = dividerStroke
            )
            // Divider 3: Right horizontal line (between Protein and Carbs)
            drawLine(
                color = dividerColor,
                start = center,
                end = Offset(center.x + outerRadius, center.y),
                strokeWidth = dividerStroke
            )

            if (showCenterHub) {
                val hubRadius = outerRadius * 0.32f
                // Central dark hub circle
                drawCircle(
                    color = dividerColor,
                    radius = hubRadius + 3.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = ForestGreen,
                    radius = hubRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF2B443A),
                    radius = hubRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        if (showCenterHub) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = "Ícone Prato e Talheres",
                tint = CreamBackground,
                modifier = Modifier.size(size * 0.22f)
            )
        }
    }
}
