package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class Particle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFB703),
            Color(0xFFFB8500),
            Color(0xFF4F46E5),
            Color(0xFF06B6D4),
            Color(0xFF10B981),
            Color(0xFFEC4899),
            Color(0xFF8B5CF6)
        )
        List(particleCount) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat() * 0.3f,
                velocityX = (Random.nextFloat() - 0.5f) * 400f,
                velocityY = Random.nextFloat() * 500f + 250f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val currentX = (p.initialX * canvasWidth + p.velocityX * progress)
                .coerceIn(0f, canvasWidth)
            val currentY = p.initialY * canvasHeight + p.velocityY * progress
            val rotation = progress * p.rotationSpeed

            if (currentY <= canvasHeight) {
                rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(currentX, currentY),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
