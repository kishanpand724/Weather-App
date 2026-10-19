package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.data.WeatherVisualType
import kotlin.math.cos
import kotlin.math.sin

object AtmosphericGradients {
    fun backgroundFor(visualType: WeatherVisualType?): Brush {
        val colors = when (visualType) {
            WeatherVisualType.CLEAR_DAY -> listOf(
                Color(0xFF0C4A6E),
                Color(0xFF0284C7),
                Color(0xFF1E3A8A)
            )
            WeatherVisualType.CLEAR_NIGHT -> listOf(
                Color(0xFF060B19),
                Color(0xFF0F172A),
                Color(0xFF1E1B4B)
            )
            WeatherVisualType.PARTLY_CLOUDY -> listOf(
                Color(0xFF0F172A),
                Color(0xFF1E3A8A),
                Color(0xFF334155)
            )
            WeatherVisualType.OVERCAST, WeatherVisualType.FOG -> listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B),
                Color(0xFF334155)
            )
            WeatherVisualType.DRIZZLE, WeatherVisualType.RAIN -> listOf(
                Color(0xFF091326),
                Color(0xFF172554),
                Color(0xFF1E3A8A)
            )
            WeatherVisualType.SNOW -> listOf(
                Color(0xFF0F172A),
                Color(0xFF1E3A8A),
                Color(0xFF38BDF8).copy(alpha = 0.65f)
            )
            WeatherVisualType.THUNDERSTORM -> listOf(
                Color(0xFF090D16),
                Color(0xFF1E1B4B),
                Color(0xFF311042)
            )
            null -> listOf(
                Color(0xFF0B132B),
                Color(0xFF1C2541),
                Color(0xFF1E3A8A)
            )
        }
        return Brush.verticalGradient(colors = colors)
    }
}

@Composable
fun WeatherArtIllustration(
    visualType: WeatherVisualType,
    contentDesc: String,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_art_transition")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animate) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = if (animate) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_float"
    )

    Canvas(
        modifier = modifier.semantics {
            contentDescription = contentDesc
        }
    ) {
        val w = size.width
        val h = size.height
        val minDim = size.minDimension
        val center = Offset(w / 2f, h / 2f)
        val gentleYShift = floatOffset * (minDim * 0.03f)

        when (visualType) {
            WeatherVisualType.CLEAR_DAY -> {
                drawSunGraphic(
                    center = center,
                    radius = minDim * 0.28f,
                    rotationDegrees = sunRotation
                )
            }
            WeatherVisualType.CLEAR_NIGHT -> {
                drawMoonAndStarsGraphic(
                    center = Offset(center.x, center.y + gentleYShift * 0.5f),
                    radius = minDim * 0.28f
                )
            }
            WeatherVisualType.PARTLY_CLOUDY -> {
                drawSunGraphic(
                    center = Offset(w * 0.36f, h * 0.36f),
                    radius = minDim * 0.21f,
                    rotationDegrees = sunRotation
                )
                drawCloudShape(
                    centerX = w * 0.54f,
                    centerY = h * 0.58f + gentleYShift,
                    scale = minDim * 0.42f,
                    topColor = Color(0xFFFFFFFF),
                    bottomColor = Color(0xFFDBEAFE)
                )
            }
            WeatherVisualType.OVERCAST -> {
                drawCloudShape(
                    centerX = w * 0.42f,
                    centerY = h * 0.44f - gentleYShift * 0.6f,
                    scale = minDim * 0.35f,
                    topColor = Color(0xFFCBD5E1),
                    bottomColor = Color(0xFF94A3B8)
                )
                drawCloudShape(
                    centerX = w * 0.54f,
                    centerY = h * 0.58f + gentleYShift,
                    scale = minDim * 0.44f,
                    topColor = Color(0xFFF8FAFC),
                    bottomColor = Color(0xFFCBD5E1)
                )
            }
            WeatherVisualType.FOG -> {
                drawCloudShape(
                    centerX = w * 0.5f,
                    centerY = h * 0.44f + gentleYShift * 0.5f,
                    scale = minDim * 0.38f,
                    topColor = Color(0xFFE2E8F0),
                    bottomColor = Color(0xFF94A3B8)
                )
                val barWidth = minDim * 0.56f
                val stroke = minDim * 0.055f
                for (i in 0..2) {
                    val yPos = h * (0.68f + i * 0.09f)
                    val shrink = i * (minDim * 0.06f)
                    drawLine(
                        color = Color(0xFFBAE6FD).copy(alpha = 0.85f - i * 0.2f),
                        start = Offset(center.x - (barWidth / 2f) + shrink, yPos),
                        end = Offset(center.x + (barWidth / 2f) - shrink, yPos),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
            WeatherVisualType.DRIZZLE, WeatherVisualType.RAIN -> {
                drawCloudShape(
                    centerX = w * 0.5f,
                    centerY = h * 0.43f + gentleYShift * 0.6f,
                    scale = minDim * 0.42f,
                    topColor = Color(0xFFE2E8F0),
                    bottomColor = Color(0xFF94A3B8)
                )
                val dropCount = if (visualType == WeatherVisualType.RAIN) 4 else 3
                val spacing = minDim * 0.13f
                val startX = center.x - ((dropCount - 1) * spacing / 2f)
                for (i in 0 until dropCount) {
                    val dropX = startX + i * spacing
                    val dropY = h * 0.68f + (i % 2) * (minDim * 0.05f)
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        ),
                        start = Offset(dropX + minDim * 0.03f, dropY),
                        end = Offset(dropX - minDim * 0.03f, dropY + minDim * 0.14f),
                        strokeWidth = minDim * 0.055f,
                        cap = StrokeCap.Round
                    )
                }
            }
            WeatherVisualType.SNOW -> {
                drawCloudShape(
                    centerX = w * 0.5f,
                    centerY = h * 0.43f + gentleYShift * 0.5f,
                    scale = minDim * 0.42f,
                    topColor = Color(0xFFF8FAFC),
                    bottomColor = Color(0xFFCBD5E1)
                )
                val flakeOffsets = listOf(-0.16f, 0f, 0.16f)
                flakeOffsets.forEachIndexed { idx, xRatio ->
                    val cx = center.x + minDim * xRatio
                    val cy = h * 0.74f + (idx % 2) * (minDim * 0.05f)
                    drawCircle(
                        color = Color(0xFFE0F2FE),
                        radius = minDim * 0.045f,
                        center = Offset(cx, cy)
                    )
                }
            }
            WeatherVisualType.THUNDERSTORM -> {
                drawCloudShape(
                    centerX = w * 0.5f,
                    centerY = h * 0.42f + gentleYShift * 0.5f,
                    scale = minDim * 0.42f,
                    topColor = Color(0xFF94A3B8),
                    bottomColor = Color(0xFF475569)
                )
                val boltPath = Path().apply {
                    moveTo(center.x + minDim * 0.04f, h * 0.56f)
                    lineTo(center.x - minDim * 0.09f, h * 0.73f)
                    lineTo(center.x + minDim * 0.01f, h * 0.73f)
                    lineTo(center.x - minDim * 0.05f, h * 0.91f)
                    lineTo(center.x + minDim * 0.12f, h * 0.69f)
                    lineTo(center.x + minDim * 0.02f, h * 0.69f)
                    close()
                }
                drawPath(
                    path = boltPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawSunGraphic(
    center: Offset,
    radius: Float,
    rotationDegrees: Float
) {
    // Outer soft halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFBBF24).copy(alpha = 0.42f),
                Color(0xFFF59E0B).copy(alpha = 0.0f)
            ),
            center = center,
            radius = radius * 1.75f
        ),
        radius = radius * 1.75f,
        center = center
    )

    // Rotating rays
    rotate(degrees = rotationDegrees, pivot = center) {
        val rayCount = 8
        for (i in 0 until rayCount) {
            val angleRad = Math.toRadians((i * (360.0 / rayCount)))
            val innerR = radius * 1.18f
            val outerR = radius * 1.44f
            val start = Offset(
                x = center.x + (innerR * cos(angleRad)).toFloat(),
                y = center.y + (innerR * sin(angleRad)).toFloat()
            )
            val end = Offset(
                x = center.x + (outerR * cos(angleRad)).toFloat(),
                y = center.y + (outerR * sin(angleRad)).toFloat()
            )
            drawLine(
                color = Color(0xFFFDE68A),
                start = start,
                end = end,
                strokeWidth = radius * 0.22f,
                cap = StrokeCap.Round
            )
        }
    }

    // Core sun sphere
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawMoonAndStarsGraphic(
    center: Offset,
    radius: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF818CF8).copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.7f
        ),
        radius = radius * 1.7f,
        center = center
    )

    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
    )

    // Crescent cutout glow
    drawCircle(
        color = Color(0xFF0F172A),
        radius = radius * 0.82f,
        center = Offset(center.x + radius * 0.38f, center.y - radius * 0.28f)
    )

    // Twinkling stars
    drawCircle(
        color = Color(0xFFFef08a),
        radius = radius * 0.12f,
        center = Offset(center.x - radius * 1.15f, center.y - radius * 0.65f)
    )
    drawCircle(
        color = Color(0xFFBAE6FD),
        radius = radius * 0.09f,
        center = Offset(center.x + radius * 1.05f, center.y + radius * 0.75f)
    )
}

private fun DrawScope.drawCloudShape(
    centerX: Float,
    centerY: Float,
    scale: Float,
    topColor: Color,
    bottomColor: Color
) {
    val brush = Brush.verticalGradient(
        colors = listOf(topColor, bottomColor),
        startY = centerY - scale * 0.6f,
        endY = centerY + scale * 0.5f
    )

    val pillWidth = scale * 1.55f
    val pillHeight = scale * 0.62f
    drawRoundRect(
        brush = brush,
        topLeft = Offset(centerX - pillWidth / 2f, centerY - pillHeight / 4f),
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f)
    )

    drawCircle(
        brush = brush,
        radius = scale * 0.42f,
        center = Offset(centerX - scale * 0.22f, centerY - scale * 0.14f)
    )

    drawCircle(
        brush = brush,
        radius = scale * 0.32f,
        center = Offset(centerX + scale * 0.26f, centerY - scale * 0.06f)
    )
}
