package com.example.graphics

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.BuildingCategory
import com.example.model.CityTile
import com.example.model.TileType
import kotlin.math.cos
import kotlin.math.sin

object ConstructionRenderer {

    /**
     * Draws an active construction site for buildings, utilities, and transit infrastructure.
     * Evolves smoothly from Foundation (0-30%) to Framing/Scaffolding (30-75%) to Final Assembly (75-99%).
     */
    fun drawConstructionSite(
        scope: DrawScope,
        tile: CityTile,
        center: Offset,
        tileW: Float,
        tileH: Float,
        animProgress: Float,
        progress: Float
    ) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f

        val isRoadOrTransit = tile.type.category == BuildingCategory.TRANSIT
        val maxTargetHeight = getEstimatedBuildingHeight(tile.type, tile.level)

        // 1. Draw Excavation Pit & Ground Work
        drawFoundationPit(scope, center, halfW, halfH, isRoadOrTransit)

        // 2. Draw Construction Materials & Safety Gear on Ground
        drawGroundMaterials(scope, center, halfW, halfH, animProgress)

        // 3. Draw Rising Structural Skeleton & Scaffolding
        val currentScaffoldH = maxTargetHeight * progress.coerceIn(0.15f, 1.0f)
        drawScaffoldingStructure(scope, center, halfW, halfH, currentScaffoldH, progress, animProgress)

        // 4. Draw Animated Construction Crane (for buildings with height > 20px)
        if (maxTargetHeight >= 20f && !isRoadOrTransit) {
            drawTowerCrane(scope, center, halfW, halfH, maxTargetHeight, animProgress)
        }

        // 5. Active FX: Welding Sparks & Dust Puffs
        drawConstructionSparksAndDust(scope, center, halfW, currentScaffoldH, animProgress, progress)

        // 6. Floating Progress Badge HUD
        val badgeY = center.y - currentScaffoldH - (if (maxTargetHeight >= 20f) 32f else 18f)
        drawFloatingProgressHUD(scope, Offset(center.x, badgeY), progress, tile.level)
    }

    private fun drawFoundationPit(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        isTransit: Boolean
    ) {
        val pitScale = if (isTransit) 0.85f else 0.75f
        val pitW = halfW * pitScale
        val pitH = halfH * pitScale

        // Excavated dark dirt diamond
        val pitPath = Path().apply {
            moveTo(center.x, center.y - pitH)
            lineTo(center.x + pitW, center.y)
            lineTo(center.x, center.y + pitH)
            lineTo(center.x - pitW, center.y)
            close()
        }
        scope.drawPath(pitPath, Color(0xFF5D4037))

        // Concrete base foundation slab
        val slabScale = pitScale * 0.8f
        val slabPath = Path().apply {
            moveTo(center.x, center.y - halfH * slabScale)
            lineTo(center.x + halfW * slabScale, center.y)
            lineTo(center.x, center.y + halfH * slabScale)
            lineTo(center.x - halfW * slabScale, center.y)
            close()
        }
        scope.drawPath(slabPath, Color(0xFF9E9E9E))
        scope.drawPath(slabPath, Color(0xFF757575), style = Stroke(1.5f))

        // Yellow and Black Hazard Striped Perimeter Barrier Tape
        val hazardColor = Color(0xFFFFD600)
        val blackTape = Color(0xFF212121)

        val corners = listOf(
            Offset(center.x, center.y - pitH),
            Offset(center.x + pitW, center.y),
            Offset(center.x, center.y + pitH),
            Offset(center.x - pitW, center.y)
        )

        for (i in corners.indices) {
            val p1 = corners[i]
            val p2 = corners[(i + 1) % corners.size]
            // Tape line
            scope.drawLine(hazardColor, Offset(p1.x, p1.y - 4f), Offset(p2.x, p2.y - 4f), strokeWidth = 2f)
            // Wooden support stakes
            scope.drawLine(Color(0xFF8D6E63), p1, Offset(p1.x, p1.y - 7f), strokeWidth = 2f)
        }
    }

    private fun drawGroundMaterials(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        animProgress: Float
    ) {
        // Traffic Safety Cones (Orange with reflective white stripe)
        val conePos1 = Offset(center.x - halfW * 0.65f, center.y + halfH * 0.3f)
        val conePos2 = Offset(center.x + halfW * 0.6f, center.y + halfH * 0.35f)

        fun drawCone(pos: Offset) {
            // Orange triangle body
            val conePath = Path().apply {
                moveTo(pos.x, pos.y - 9f)
                lineTo(pos.x + 4.5f, pos.y)
                lineTo(pos.x - 4.5f, pos.y)
                close()
            }
            scope.drawPath(conePath, Color(0xFFFF6D00))
            // Reflective white ring
            scope.drawLine(Color(0xFFFFFFFF), Offset(pos.x - 2.5f, pos.y - 4f), Offset(pos.x + 2.5f, pos.y - 4f), strokeWidth = 2f)
            // Black base
            scope.drawRect(Color(0xFF212121), topLeft = Offset(pos.x - 5f, pos.y), size = Size(10f, 2f))
        }

        drawCone(conePos1)
        drawCone(conePos2)

        // Stacks of red bricks on wooden pallet
        val palletPos = Offset(center.x - halfW * 0.45f, center.y - halfH * 0.3f)
        scope.drawRect(Color(0xFF8D6E63), topLeft = Offset(palletPos.x - 6f, palletPos.y), size = Size(12f, 3f))
        scope.drawRect(Color(0xFFD32F2F), topLeft = Offset(palletPos.x - 5f, palletPos.y - 4f), size = Size(10f, 4f))
        scope.drawRect(Color(0xFFB71C1C), topLeft = Offset(palletPos.x - 4f, palletPos.y - 7f), size = Size(8f, 3f))

        // Steel Rebar Rods bundle
        val rebarPos = Offset(center.x + halfW * 0.35f, center.y - halfH * 0.25f)
        scope.drawLine(Color(0xFFB0BEC5), Offset(rebarPos.x - 6f, rebarPos.y + 3f), Offset(rebarPos.x + 6f, rebarPos.y - 3f), strokeWidth = 2f)
        scope.drawLine(Color(0xFF90A4AE), Offset(rebarPos.x - 5f, rebarPos.y + 5f), Offset(rebarPos.x + 7f, rebarPos.y - 1f), strokeWidth = 2f)
    }

    private fun drawScaffoldingStructure(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        scaffoldHeight: Float,
        progress: Float,
        animProgress: Float
    ) {
        val widthScale = 0.7f
        val bW = halfW * widthScale
        val bH = halfH * widthScale

        val baseLeft = Offset(center.x - bW, center.y)
        val baseRight = Offset(center.x + bW, center.y)
        val baseTop = Offset(center.x, center.y - bH)
        val baseBottom = Offset(center.x, center.y + bH)

        val topLeft = Offset(baseLeft.x, baseLeft.y - scaffoldHeight)
        val topRight = Offset(baseRight.x, baseRight.y - scaffoldHeight)
        val topTop = Offset(baseTop.x, baseTop.y - scaffoldHeight)
        val topBottom = Offset(baseBottom.x, baseBottom.y - scaffoldHeight)

        // 1. Orange Steel I-beam Skeleton frame
        val steelColor = Color(0xFFE65100)
        // Vertical pillars
        scope.drawLine(steelColor, baseLeft, topLeft, strokeWidth = 3f)
        scope.drawLine(steelColor, baseRight, topRight, strokeWidth = 3f)
        scope.drawLine(steelColor, baseBottom, topBottom, strokeWidth = 3f)
        scope.drawLine(steelColor, baseTop, topTop, strokeWidth = 2.5f)

        // Horizontal floor tiers
        val tiers = (scaffoldHeight / 12f).toInt().coerceAtLeast(1)
        for (i in 1..tiers) {
            val f = i.toFloat() / tiers
            val yDrop = scaffoldHeight * f
            val pL = Offset(baseLeft.x, baseLeft.y - yDrop)
            val pR = Offset(baseRight.x, baseRight.y - yDrop)
            val pB = Offset(baseBottom.x, baseBottom.y - yDrop)
            val pT = Offset(baseTop.x, baseTop.y - yDrop)

            // Beam connections
            scope.drawLine(Color(0xFFFFB74D), pL, pB, strokeWidth = 2f)
            scope.drawLine(Color(0xFFFFB74D), pB, pR, strokeWidth = 2f)
            scope.drawLine(Color(0xFFFF9800), pL, pT, strokeWidth = 1.5f)
            scope.drawLine(Color(0xFFFF9800), pT, pR, strokeWidth = 1.5f)

            // Scaffolding Cross Diagonals (X-bracing)
            val prevDrop = scaffoldHeight * ((i - 1).toFloat() / tiers)
            val prevL = Offset(baseLeft.x, baseLeft.y - prevDrop)
            val prevB = Offset(baseBottom.x, baseBottom.y - prevDrop)
            val prevR = Offset(baseRight.x, baseRight.y - prevDrop)

            scope.drawLine(Color(0x88B0BEC5), prevL, pB, strokeWidth = 1.5f)
            scope.drawLine(Color(0x88B0BEC5), pL, prevB, strokeWidth = 1.5f)
            scope.drawLine(Color(0x88B0BEC5), prevB, pR, strokeWidth = 1.5f)
            scope.drawLine(Color(0x88B0BEC5), pB, prevR, strokeWidth = 1.5f)
        }

        // Green Safety Protection Netting on left/right faces
        if (progress > 0.4f) {
            val netLeft = Path().apply {
                moveTo(baseLeft.x, baseLeft.y)
                lineTo(topLeft.x, topLeft.y)
                lineTo(topBottom.x, topBottom.y)
                lineTo(baseBottom.x, baseBottom.y)
                close()
            }
            scope.drawPath(netLeft, Color(0x332E7D32))

            val netRight = Path().apply {
                moveTo(baseBottom.x, baseBottom.y)
                lineTo(topBottom.x, topBottom.y)
                lineTo(topRight.x, topRight.y)
                lineTo(baseRight.x, baseRight.y)
                close()
            }
            scope.drawPath(netRight, Color(0x222E7D32))
        }
    }

    private fun drawTowerCrane(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        buildingTargetHeight: Float,
        animProgress: Float
    ) {
        val craneBase = Offset(center.x + halfW * 0.65f, center.y)
        val craneMastH = buildingTargetHeight + 24f
        val craneTop = Offset(craneBase.x, craneBase.y - craneMastH)

        val craneYellow = Color(0xFFFFD600)
        val craneShadow = Color(0xFFC79100)

        // 1. Vertical Lattice Mast (Truss)
        scope.drawLine(craneYellow, craneBase, craneTop, strokeWidth = 4f)
        scope.drawLine(craneShadow, Offset(craneBase.x + 2f, craneBase.y), Offset(craneTop.x + 2f, craneTop.y), strokeWidth = 1.5f)

        // Lattice cross rungs
        val rungs = 7
        for (r in 0 until rungs) {
            val ry = craneBase.y - (r * (craneMastH / rungs))
            scope.drawLine(Color(0xFF212121), Offset(craneBase.x - 3f, ry), Offset(craneBase.x + 3f, ry), strokeWidth = 1.5f)
        }

        // 2. Operator Cabin
        scope.drawRect(Color(0xFF0288D1), topLeft = Offset(craneTop.x - 4f, craneTop.y + 4f), size = Size(6f, 6f))

        // 3. Horizontal Jib (Crane Arm)
        val jibArmLength = halfW * 1.2f
        val counterArmLength = halfW * 0.45f
        val jibEnd = Offset(craneTop.x - jibArmLength, craneTop.y)
        val counterEnd = Offset(craneTop.x + counterArmLength, craneTop.y)

        scope.drawLine(craneYellow, counterEnd, jibEnd, strokeWidth = 3f)

        // Guy wires / tension cables from top tower peak
        val peak = Offset(craneTop.x, craneTop.y - 8f)
        scope.drawLine(craneYellow, craneTop, peak, strokeWidth = 2.5f)
        scope.drawLine(Color(0xFF90A4AE), peak, jibEnd, strokeWidth = 1.2f)
        scope.drawLine(Color(0xFF90A4AE), peak, counterEnd, strokeWidth = 1.2f)

        // Counterweight block
        scope.drawRect(Color(0xFF37474F), topLeft = Offset(counterEnd.x - 6f, counterEnd.y), size = Size(8f, 7f))

        // 4. Swinging Hoist Cable and Steel Beam
        val swing = sin(animProgress * 5f) * 4f
        val trolleyPos = Offset(craneTop.x - jibArmLength * 0.6f, craneTop.y)
        val cableLength = 16f
        val hookPos = Offset(trolleyPos.x + swing, trolleyPos.y + cableLength)

        // Cable
        scope.drawLine(Color(0xFFECEFF1), trolleyPos, hookPos, strokeWidth = 1.2f)

        // Suspended Orange Steel I-Beam
        scope.drawLine(
            Color(0xFFFF6D00),
            Offset(hookPos.x - 9f, hookPos.y + 3f),
            Offset(hookPos.x + 9f, hookPos.y + 3f),
            strokeWidth = 3.5f
        )
    }

    private fun drawConstructionSparksAndDust(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        scaffoldHeight: Float,
        animProgress: Float,
        progress: Float
    ) {
        // Welding Sparks (Bright flashing yellow, white & cyan stars)
        val sparkPhase = (animProgress * 12f) % 1.0f
        if (sparkPhase > 0.4f && progress > 0.25f) {
            val sparkX = center.x - halfW * 0.15f + sin(animProgress * 15f) * 10f
            val sparkY = center.y - scaffoldHeight * 0.8f

            // Central spark glare
            scope.drawCircle(Color(0xFFFFFFFF), radius = 3.5f, center = Offset(sparkX, sparkY))
            scope.drawCircle(Color(0xFF00E5FF), radius = 6f, center = Offset(sparkX, sparkY), style = Stroke(1.5f))

            // Radiating spark rays
            for (i in 0..4) {
                val angle = i * 1.25f + (animProgress * 20f)
                val len = 6f + sin(animProgress * 30f + i) * 3f
                val sEnd = Offset(sparkX + cos(angle) * len, sparkY + sin(angle) * len)
                scope.drawLine(Color(0xFFFFD54F), Offset(sparkX, sparkY), sEnd, strokeWidth = 1.5f)
            }
        }

        // Dust Cloud Puffs at base
        val dustOffX = sin(animProgress * 4f) * 6f
        val dustY = center.y + 2f
        scope.drawCircle(
            Color(0x44D7CCC8),
            radius = 6f,
            center = Offset(center.x + halfW * 0.2f + dustOffX, dustY)
        )
        scope.drawCircle(
            Color(0x33BCAAA4),
            radius = 8f,
            center = Offset(center.x - halfW * 0.2f - dustOffX, dustY - 2f)
        )
    }

    private fun drawFloatingProgressHUD(
        scope: DrawScope,
        badgePos: Offset,
        progress: Float,
        level: Int
    ) {
        val badgeW = 54f
        val badgeH = 16f
        val topLeft = Offset(badgePos.x - badgeW / 2f, badgePos.y - badgeH / 2f)

        // Translucent dark pill container
        scope.drawRoundRect(
            color = Color(0xE60F172A),
            topLeft = topLeft,
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Glowing cyan/amber border
        scope.drawRoundRect(
            color = Color(0xFFF59E0B),
            topLeft = topLeft,
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(1.2f)
        )

        // Mini Circular Progress Arc / Icon on left
        val ringCenter = Offset(topLeft.x + 9f, badgePos.y)
        scope.drawCircle(Color(0x44F59E0B), radius = 5f, center = ringCenter)
        scope.drawArc(
            color = Color(0xFFFBBF24),
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = Offset(ringCenter.x - 5f, ringCenter.y - 5f),
            size = Size(10f, 10f),
            style = Stroke(2f)
        )

        // Progress Bar Line inside pill
        val barStartX = topLeft.x + 18f
        val barEndX = topLeft.x + badgeW - 6f
        val barY = badgePos.y
        val barLength = barEndX - barStartX

        // Background track
        scope.drawLine(
            Color(0x44FFFFFF),
            Offset(barStartX, barY),
            Offset(barEndX, barY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Filled track
        scope.drawLine(
            Color(0xFF38BDF8),
            Offset(barStartX, barY),
            Offset(barStartX + barLength * progress, barY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    private fun getEstimatedBuildingHeight(type: TileType, level: Int): Float {
        return when (type) {
            TileType.RESIDENTIAL_SMALL -> 16f + (level - 1) * 6f
            TileType.RESIDENTIAL_MEDIUM -> 28f + (level - 1) * 8f
            TileType.RESIDENTIAL_HIGH -> 50f + (level - 1) * 16f
            TileType.RESIDENTIAL_LUXURY -> 75f + (level - 1) * 20f
            TileType.COMMERCIAL_SMALL -> 18f + (level - 1) * 6f
            TileType.COMMERCIAL_MEDIUM -> 26f + (level - 1) * 8f
            TileType.COMMERCIAL_HIGH -> 65f + (level - 1) * 18f
            TileType.COMMERCIAL_TECH_HQ -> 55f + (level - 1) * 15f
            TileType.INDUSTRIAL_SMALL -> 18f
            TileType.INDUSTRIAL_MEDIUM -> 24f
            TileType.INDUSTRIAL_TECH -> 30f
            TileType.INDUSTRIAL_LOGISTICS -> 20f
            TileType.TOWN_HALL -> 34f
            TileType.POLICE_STATION, TileType.FIRE_STATION -> 22f
            TileType.HOSPITAL -> 32f
            TileType.UNIVERSITY -> 30f
            TileType.STADIUM -> 28f
            TileType.POWER_PLANT -> 38f
            TileType.WATER_TOWER -> 34f
            TileType.WIND_TURBINE -> 36f
            else -> 15f
        }
    }
}
