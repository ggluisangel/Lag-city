package com.example.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

object EffectsRenderer {

    /**
     * Billowing chimney/factory smoke plumes that drift with the wind, expand and fade.
     */
    fun drawSmokePlume(
        scope: DrawScope,
        source: Offset,
        animProgress: Float,
        particleCount: Int = 4,
        baseColor: Color = Color(0xFF78909C)
    ) {
        for (i in 0 until particleCount) {
            val phase = (animProgress + (i.toFloat() / particleCount)) % 1.0f
            val riseDist = phase * 28f
            val windDrift = (phase * 14f) + (sin(phase * 6.28f) * 3f)

            val currentPos = Offset(source.x + windDrift, source.y - riseDist)
            val radius = 3.5f + (phase * 8f)
            val alpha = (1.0f - phase) * 0.75f

            scope.drawCircle(
                color = baseColor.copy(alpha = alpha),
                radius = radius,
                center = currentPos
            )
        }
    }

    /**
     * Billowing cooling tower or water plant pure white steam vapor.
     */
    fun drawSteamPlume(
        scope: DrawScope,
        source: Offset,
        animProgress: Float
    ) {
        for (i in 0..3) {
            val phase = (animProgress + (i * 0.25f)) % 1.0f
            val yOffset = phase * 24f
            val xDrift = sin(phase * 4f) * 4f + (phase * 6f)
            val size = 5f + (phase * 9f)
            val alpha = (1.0f - phase) * 0.6f

            scope.drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = size,
                center = Offset(source.x + xDrift, source.y - yOffset)
            )
        }
    }

    /**
     * Active fountain spray with central jet, side droplets, and concentric pool ripple rings.
     */
    fun drawFountainWaterEffect(
        scope: DrawScope,
        center: Offset,
        animProgress: Float,
        isNight: Boolean
    ) {
        // Pool Ripple Rings expanding outwards
        for (i in 0..1) {
            val ringPhase = (animProgress + (i * 0.5f)) % 1.0f
            val ringRadius = 4f + (ringPhase * 8f)
            val ringAlpha = (1f - ringPhase) * 0.6f
            scope.drawCircle(
                color = Color(0xFFE0F7FA).copy(alpha = ringAlpha),
                radius = ringRadius,
                center = center,
                style = Stroke(1.2f)
            )
        }

        // Central vertical water jet
        val sprayHeight = 12f + sin(animProgress * 8f) * 3f
        scope.drawLine(
            color = Color(0xFFE1F5FE),
            start = center,
            end = Offset(center.x, center.y - sprayHeight),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Arcing droplet particles
        val dropletSpread = 6f
        val leftDroplet = Offset(center.x - dropletSpread, center.y - sprayHeight * 0.6f + sin(animProgress * 10f) * 2f)
        val rightDroplet = Offset(center.x + dropletSpread, center.y - sprayHeight * 0.6f + cos(animProgress * 10f) * 2f)

        scope.drawCircle(Color.White, radius = 1.8f, center = leftDroplet)
        scope.drawCircle(Color.White, radius = 1.8f, center = rightDroplet)

        if (isNight) {
            scope.drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x9900E5FF), Color(0x0000E5FF)),
                    center = center,
                    radius = 18f
                ),
                radius = 18f,
                center = center
            )
        }
    }

    /**
     * Drifting cumulus sky clouds and soft translucent shadows cast on the city below.
     */
    fun drawSkyCloudsAndShadows(
        scope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        animProgress: Float
    ) {
        val clouds = listOf(
            Triple(0.2f, 0.25f, 50f),
            Triple(0.65f, 0.15f, 65f),
            Triple(0.85f, 0.55f, 45f)
        )

        for ((idx, cloud) in clouds.withIndex()) {
            val speed = 0.5f + (idx * 0.2f)
            val currentX = ((cloud.first * screenWidth) + (animProgress * screenWidth * speed * 0.3f)) % (screenWidth + 200f) - 100f
            val currentY = cloud.second * screenHeight
            val radius = cloud.third

            // Ground shadow (offset below and to the right)
            val shadowCenter = Offset(currentX + 30f, currentY + 120f)
            scope.drawOval(
                color = Color(0x18000000),
                topLeft = Offset(shadowCenter.x - radius * 0.8f, shadowCenter.y - radius * 0.4f),
                size = Size(radius * 1.6f, radius * 0.8f)
            )

            // Fluffy white cloud body
            val cloudBrush = Brush.radialGradient(
                listOf(Color(0x99FFFFFF), Color(0x33FFFFFF), Color(0x00FFFFFF)),
                center = Offset(currentX, currentY),
                radius = radius
            )
            scope.drawCircle(cloudBrush, radius = radius, center = Offset(currentX, currentY))
            scope.drawCircle(Color(0x77FFFFFF), radius = radius * 0.6f, center = Offset(currentX - radius * 0.3f, currentY + radius * 0.1f))
            scope.drawCircle(Color(0x77FFFFFF), radius = radius * 0.55f, center = Offset(currentX + radius * 0.35f, currentY + radius * 0.1f))
        }
    }

    /**
     * A small flock of birds / seagulls flying in V-formation smoothly across the sky.
     */
    fun drawBirdFlock(
        scope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        animProgress: Float
    ) {
        val flockX = ((animProgress * screenWidth * 0.4f) + 100f) % (screenWidth + 300f) - 150f
        val flockY = (screenHeight * 0.22f) + sin(animProgress * 3.14f) * 15f
        val wingFlap = sin(animProgress * 18f) * 2.5f

        val birdOffsets = listOf(
            Offset(0f, 0f),
            Offset(-14f, 8f),
            Offset(14f, 8f),
            Offset(-28f, 16f),
            Offset(28f, 16f)
        )

        for (off in birdOffsets) {
            val bx = flockX + off.x
            val by = flockY + off.y
            // Left wing & Right wing
            scope.drawLine(Color(0xFF37474F), Offset(bx, by), Offset(bx - 4f, by - 2f + wingFlap), strokeWidth = 1.5f)
            scope.drawLine(Color(0xFF37474F), Offset(bx, by), Offset(bx + 4f, by - 2f + wingFlap), strokeWidth = 1.5f)
        }
    }

    /**
     * Festival celebration balloons and shimmering confetti particles.
     */
    fun drawFestivalCelebration(
        scope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        animProgress: Float
    ) {
        val balloonColors = listOf(Color(0xFFFF1744), Color(0xFFFFEA00), Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFFAA00FF))

        // Floating Balloons
        for (i in 0..7) {
            val bx = (screenWidth * (0.1f + i * 0.12f)) + sin(animProgress * 4f + i) * 18f
            val by = (screenHeight * 0.9f) - ((animProgress + i * 0.125f) % 1.0f) * (screenHeight * 0.85f)
            val color = balloonColors[i % balloonColors.size]

            // Balloon Oval
            scope.drawOval(color, topLeft = Offset(bx - 6f, by - 8f), size = Size(12f, 16f))
            // Balloon String
            val stringEnd = Offset(bx + sin(animProgress * 6f + i) * 4f, by + 18f)
            scope.drawLine(Color(0x88FFFFFF), Offset(bx, by + 8f), stringEnd, strokeWidth = 1.2f)
        }

        // Falling Shimmering Confetti
        val confettiColors = listOf(Color(0xFFFF4081), Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFFFD600), Color(0xFF76FF03))
        for (c in 0..24) {
            val cx = (c * 37f + animProgress * 150f) % screenWidth
            val cy = ((c * 43f + animProgress * screenHeight * 0.6f) % screenHeight)
            val cColor = confettiColors[c % confettiColors.size]
            scope.drawRect(cColor, topLeft = Offset(cx, cy), size = Size(3.5f, 3.5f))
        }
    }
}
