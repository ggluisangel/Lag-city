package com.example.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.engine.IsometricMath
import com.example.model.*
import kotlin.math.*

object IsometricRenderer {

    // Ambient Lighting Colors based on Time of Day (0 to 24 hours)
    fun getAmbientColorFilter(timeOfDay: Float): Color {
        // 0-4: Deep Night (Dark Indigo)
        // 5-7: Dawn / Sunrise (Warm Rose-Gold)
        // 8-16: Daylight (Crisp Clear White-Amber)
        // 17-19: Golden Hour / Sunset (Vibrant Orange-Coral)
        // 20-23: Twilight / Nightfall (Deep Navy)
        return when {
            timeOfDay in 5.0f..7.5f -> {
                val t = (timeOfDay - 5.0f) / 2.5f
                lerpColor(Color(0xFF1E1B4B), Color(0xFFFFFBEB), t)
            }
            timeOfDay in 7.5f..16.5f -> {
                Color(0xFFFFFFFF)
            }
            timeOfDay in 16.5f..19.5f -> {
                val t = (timeOfDay - 16.5f) / 3.0f
                lerpColor(Color(0xFFFFF7ED), Color(0xFF312E81), t)
            }
            else -> {
                Color(0xFF1E1B4B)
            }
        }
    }

    private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
        val t = fraction.coerceIn(0f, 1f)
        return Color(
            red = c1.red + (c2.red - c1.red) * t,
            green = c1.green + (c2.green - c1.green) * t,
            blue = c1.blue + (c2.blue - c1.blue) * t,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * t
        )
    }

    fun isNightTime(timeOfDay: Float): Boolean {
        return timeOfDay < 6.0f || timeOfDay > 19.5f
    }

    fun drawTile(
        scope: DrawScope,
        tile: CityTile,
        offsetX: Float,
        offsetY: Float,
        zoom: Float,
        timeOfDay: Float,
        animProgress: Float,
        isFestival: Boolean,
        mapWidth: Int = 20,
        mapHeight: Int = 20
    ) {
        val center = IsometricMath.gridToScreen(tile.x.toFloat(), tile.y.toFloat(), offsetX, offsetY, zoom, tile.elevation)
        val tileW = IsometricMath.BASE_TILE_WIDTH * zoom
        val tileH = IsometricMath.BASE_TILE_HEIGHT * zoom
        val isNight = isNightTime(timeOfDay)
        val isMapEdgeSE = tile.x == mapWidth - 1
        val isMapEdgeSW = tile.y == mapHeight - 1

        // 1. Draw Enhanced Terrain Surface & 3D Cliff Borders
        TerrainRenderer.drawTerrain(
            scope = scope,
            tile = tile,
            center = center,
            tileW = tileW,
            tileH = tileH,
            timeOfDay = timeOfDay,
            animProgress = animProgress,
            isMapEdgeSE = isMapEdgeSE,
            isMapEdgeSW = isMapEdgeSW
        )

        // 2. Check if Tile is Under Construction (Progress < 100%)
        val progress = tile.getConstructionProgress()
        val isBuildingType = tile.type != TileType.GRASS &&
                tile.type != TileType.WATER &&
                tile.type != TileType.SAND &&
                tile.type != TileType.FOREST &&
                tile.type != TileType.MOUNTAIN

        if (progress < 1.0f && tile.type.isBuildable && isBuildingType) {
            ConstructionRenderer.drawConstructionSite(
                scope = scope,
                tile = tile,
                center = center,
                tileW = tileW,
                tileH = tileH,
                animProgress = animProgress,
                progress = progress
            )
            return
        }

        // 3. Draw Completed Specific Building / Infrastructure
        when (tile.type) {
            TileType.ROAD_PAVED, TileType.ROAD_HIGHWAY, TileType.ROAD_BRIDGE -> {
                drawRoad(scope, center, tileW, tileH, tile, isNight)
            }
            TileType.RAILROAD, TileType.RAIL_BRIDGE -> {
                drawRailroad(scope, center, tileW, tileH, tile)
            }
            TileType.AIRPORT_RUNWAY -> {
                drawAirportRunway(scope, center, tileW, tileH, isNight)
            }
            TileType.HELIPAD -> {
                drawHelipad(scope, center, tileW, tileH, isNight)
            }
            TileType.MARINA -> {
                drawMarina(scope, center, tileW, tileH, isNight)
            }
            TileType.FOREST -> {
                drawForest(scope, center, tileW, tileH, animProgress)
            }
            TileType.PARK_SMALL -> {
                drawPark(scope, center, tileW, tileH, animProgress)
            }
            TileType.PLAZA_FOUNTAIN -> {
                drawPlazaFountain(scope, center, tileW, tileH, animProgress, isNight)
            }
            TileType.STADIUM -> {
                drawStadium(scope, center, tileW, tileH, isNight, isFestival)
            }
            TileType.WIND_TURBINE -> {
                drawWindTurbine(scope, center, tileW, tileH, animProgress)
            }
            TileType.SOLAR_PARK -> {
                drawSolarPark(scope, center, tileW, tileH)
            }
            TileType.POWER_PLANT -> {
                drawNuclearPowerPlant(scope, center, tileW, tileH, animProgress)
            }
            TileType.WATER_TOWER -> {
                drawWaterTower(scope, center, tileW, tileH)
            }
            TileType.WATER_TREATMENT -> {
                drawWaterTreatment(scope, center, tileW, tileH)
            }
            // Residential
            TileType.RESIDENTIAL_SMALL -> {
                drawCottage(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.RESIDENTIAL_MEDIUM -> {
                drawTownhouse(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.RESIDENTIAL_HIGH -> {
                drawHighriseApartments(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.RESIDENTIAL_LUXURY -> {
                drawLuxurySkyTower(scope, center, tileW, tileH, tile.level, isNight)
            }
            // Commercial
            TileType.COMMERCIAL_SMALL -> {
                drawCornerShop(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.COMMERCIAL_MEDIUM -> {
                drawShoppingMall(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.COMMERCIAL_HIGH -> {
                drawCorporateSkyscraper(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.COMMERCIAL_TECH_HQ -> {
                drawTechHQ(scope, center, tileW, tileH, tile.level, isNight)
            }
            // Industrial
            TileType.INDUSTRIAL_SMALL -> {
                drawWorkshop(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.INDUSTRIAL_MEDIUM -> {
                drawHeavyFactory(scope, center, tileW, tileH, tile.level, animProgress, isNight)
            }
            TileType.INDUSTRIAL_TECH -> {
                drawRoboticsLab(scope, center, tileW, tileH, tile.level, isNight)
            }
            TileType.INDUSTRIAL_LOGISTICS -> {
                drawLogisticsDepot(scope, center, tileW, tileH, tile.level, isNight)
            }
            // Civic
            TileType.TOWN_HALL -> {
                drawTownHall(scope, center, tileW, tileH, isNight)
            }
            TileType.POLICE_STATION -> {
                drawPoliceStation(scope, center, tileW, tileH, isNight)
            }
            TileType.FIRE_STATION -> {
                drawFireStation(scope, center, tileW, tileH, isNight)
            }
            TileType.HOSPITAL -> {
                drawHospital(scope, center, tileW, tileH, isNight)
            }
            TileType.UNIVERSITY -> {
                drawUniversity(scope, center, tileW, tileH, isNight)
            }
            else -> {}
        }
    }

    private fun drawBaseTerrain(
        scope: DrawScope,
        center: Offset,
        tileW: Float,
        tileH: Float,
        type: TileType,
        timeOfDay: Float,
        animProgress: Float
    ) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val top = Offset(center.x, center.y - halfH)
        val right = Offset(center.x + halfW, center.y)
        val bottom = Offset(center.x, center.y + halfH)
        val left = Offset(center.x - halfW, center.y)

        val diamondPath = Path().apply {
            moveTo(top.x, top.y)
            lineTo(right.x, right.y)
            lineTo(bottom.x, bottom.y)
            lineTo(left.x, left.y)
            close()
        }

        val baseColor = when (type) {
            TileType.WATER, TileType.MARINA -> {
                val waveShift = sin(animProgress * 6.28f + center.x * 0.05f) * 0.05f
                Color(0xFF0288D1).copy(alpha = 0.9f + waveShift)
            }
            TileType.SAND -> Color(0xFFF6D58C)
            TileType.MOUNTAIN -> Color(0xFF78909C)
            TileType.FOREST -> Color(0xFF2E7D32)
            TileType.ROAD_BRIDGE, TileType.RAIL_BRIDGE -> Color(0xFF0277BD)
            else -> Color(0xFF4CAF50) // Vibrant grass green
        }

        // Draw top face
        scope.drawPath(diamondPath, baseColor)

        // Subtle grid border
        scope.drawPath(
            diamondPath,
            color = if (type.isWaterTile) Color(0x3301579B) else Color(0x1A2E7D32),
            style = Stroke(width = 1f)
        )

        // Water wave shimmer ripples
        if (type.isWaterTile) {
            val rippleOffset = sin(animProgress * 4f + center.x * 0.1f) * 4f
            scope.drawLine(
                color = Color(0x80E0F7FA),
                start = Offset(center.x - halfW * 0.4f + rippleOffset, center.y - 2f),
                end = Offset(center.x + halfW * 0.4f + rippleOffset, center.y - 2f),
                strokeWidth = 1.5f
            )
        }
    }

    private fun drawRoad(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, tile: CityTile, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val mask = tile.roadMask

        // Asphalt road surface diamond
        val roadColor = if (tile.type == TileType.ROAD_HIGHWAY) Color(0xFF263238) else Color(0xFF37474F)
        val roadInsetPath = Path().apply {
            moveTo(center.x, center.y - halfH * 0.85f)
            lineTo(center.x + halfW * 0.85f, center.y)
            lineTo(center.x, center.y + halfH * 0.85f)
            lineTo(center.x - halfW * 0.85f, center.y)
            close()
        }
        scope.drawPath(roadInsetPath, roadColor)

        // Road markings & centerlines
        val lineColor = Color(0xFFFFD54F)
        val dashColor = Color(0xFFFFFFFF)

        // Center dot/intersection
        scope.drawCircle(color = roadColor, radius = 4f, center = center)

        // North arm (mask & 1)
        if (mask and 1 != 0) {
            scope.drawLine(dashColor, center, Offset(center.x + halfW * 0.5f, center.y - halfH * 0.5f), strokeWidth = 1.5f)
        }
        // East arm (mask & 2)
        if (mask and 2 != 0) {
            scope.drawLine(dashColor, center, Offset(center.x + halfW * 0.5f, center.y + halfH * 0.5f), strokeWidth = 1.5f)
        }
        // South arm (mask & 4)
        if (mask and 4 != 0) {
            scope.drawLine(dashColor, center, Offset(center.x - halfW * 0.5f, center.y + halfH * 0.5f), strokeWidth = 1.5f)
        }
        // West arm (mask & 8)
        if (mask and 8 != 0) {
            scope.drawLine(dashColor, center, Offset(center.x - halfW * 0.5f, center.y - halfH * 0.5f), strokeWidth = 1.5f)
        }

        // Night Streetlamp glow
        if (isNight) {
            scope.drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x99FFE082), Color(0x00FFE082)),
                    center = center,
                    radius = halfW * 0.8f
                ),
                radius = halfW * 0.8f,
                center = center
            )
        }
    }

    private fun drawRailroad(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, tile: CityTile) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val ballastColor = Color(0xFF616161)
        val railColor = Color(0xFFB0BEC5)
        val tieColor = Color(0xFF4E342E)

        // Gravel ballast bed
        val ballastPath = Path().apply {
            moveTo(center.x, center.y - halfH * 0.6f)
            lineTo(center.x + halfW * 0.6f, center.y)
            lineTo(center.x, center.y + halfH * 0.6f)
            lineTo(center.x - halfW * 0.6f, center.y)
            close()
        }
        scope.drawPath(ballastPath, ballastColor)

        // Cross ties
        for (i in -3..3) {
            val ox = i * (halfW * 0.12f)
            val oy = i * (halfH * 0.12f)
            scope.drawLine(
                tieColor,
                start = Offset(center.x + ox - 6f, center.y + oy + 3f),
                end = Offset(center.x + ox + 6f, center.y + oy - 3f),
                strokeWidth = 2.5f
            )
        }

        // Steel double rails
        scope.drawLine(railColor, Offset(center.x - halfW * 0.6f, center.y - halfH * 0.6f + 3f), Offset(center.x + halfW * 0.6f, center.y + halfH * 0.6f + 3f), strokeWidth = 1.5f)
        scope.drawLine(railColor, Offset(center.x - halfW * 0.6f, center.y - halfH * 0.6f - 3f), Offset(center.x + halfW * 0.6f, center.y + halfH * 0.6f - 3f), strokeWidth = 1.5f)
    }

    private fun drawAirportRunway(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Dark tarmac
        val tarmac = Path().apply {
            moveTo(center.x, center.y - halfH * 0.9f)
            lineTo(center.x + halfW * 0.9f, center.y)
            lineTo(center.x, center.y + halfH * 0.9f)
            lineTo(center.x - halfW * 0.9f, center.y)
            close()
        }
        scope.drawPath(tarmac, Color(0xFF1E293B))

        // Centerline stripes
        scope.drawLine(Color(0xFFFFFFFF), Offset(center.x - halfW * 0.7f, center.y - halfH * 0.7f), Offset(center.x + halfW * 0.7f, center.y + halfH * 0.7f), strokeWidth = 3f)

        // Threshold numbers / markings
        scope.drawLine(Color(0xFFFFEB3B), Offset(center.x - halfW * 0.5f, center.y - halfH * 0.5f), Offset(center.x - halfW * 0.3f, center.y - halfH * 0.7f), strokeWidth = 2f)

        // Night landing strobe lights
        if (isNight) {
            scope.drawCircle(Color(0xFF00E676), radius = 3f, center = Offset(center.x - halfW * 0.7f, center.y - halfH * 0.7f))
            scope.drawCircle(Color(0xFFFF1744), radius = 3f, center = Offset(center.x + halfW * 0.7f, center.y + halfH * 0.7f))
        }
    }

    private fun drawHelipad(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Concrete square
        scope.drawCircle(Color(0xFF455A64), radius = halfW * 0.65f, center = center)
        scope.drawCircle(Color(0xFFFFD600), radius = halfW * 0.55f, center = center, style = Stroke(2f))

        // 'H' marking
        scope.drawLine(Color(0xFFFFFFFF), Offset(center.x - 6f, center.y - 8f), Offset(center.x - 6f, center.y + 8f), strokeWidth = 3f)
        scope.drawLine(Color(0xFFFFFFFF), Offset(center.x + 6f, center.y - 8f), Offset(center.x + 6f, center.y + 8f), strokeWidth = 3f)
        scope.drawLine(Color(0xFFFFFFFF), Offset(center.x - 6f, center.y), Offset(center.x + 6f, center.y), strokeWidth = 3f)

        if (isNight) {
            scope.drawCircle(Color(0xFF00E5FF), radius = 3f, center = Offset(center.x, center.y - halfH * 0.5f))
        }
    }

    private fun drawMarina(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Wooden pier boardwalk
        val pier = Path().apply {
            moveTo(center.x - 10f, center.y - halfH * 0.5f)
            lineTo(center.x + 10f, center.y - halfH * 0.5f)
            lineTo(center.x + 10f, center.y + halfH * 0.5f)
            lineTo(center.x - 10f, center.y + halfH * 0.5f)
            close()
        }
        scope.drawPath(pier, Color(0xFF6D4C41))
        // Dock pylons
        scope.drawCircle(Color(0xFF3E2723), radius = 3f, center = Offset(center.x - 10f, center.y))
        scope.drawCircle(Color(0xFF3E2723), radius = 3f, center = Offset(center.x + 10f, center.y))

        // Small moored boat
        val boatPath = Path().apply {
            moveTo(center.x + 15f, center.y - 12f)
            lineTo(center.x + 28f, center.y - 6f)
            lineTo(center.x + 28f, center.y + 6f)
            lineTo(center.x + 15f, center.y + 12f)
            close()
        }
        scope.drawPath(boatPath, Color(0xFFFFFFFF))
        scope.drawPath(boatPath, Color(0xFF0288D1), style = Stroke(1.5f))
    }

    private fun drawForest(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, animProgress: Float) {
        val treeOffsets = listOf(
            Offset(-12f, -8f),
            Offset(14f, -4f),
            Offset(0f, 6f),
            Offset(-8f, 10f),
            Offset(10f, 8f)
        )
        for ((idx, offset) in treeOffsets.withIndex()) {
            val sway = sin(animProgress * 3f + idx) * 1.5f
            val base = Offset(center.x + offset.x, center.y + offset.y)
            // Trunk
            scope.drawLine(Color(0xFF5D4037), base, Offset(base.x, base.y - 12f), strokeWidth = 2.5f)
            // Foliage Canopy (Conical / Layered)
            val treeColor = if (idx % 2 == 0) Color(0xFF1B5E20) else Color(0xFF2E7D32)
            val leafPath = Path().apply {
                moveTo(base.x + sway, base.y - 24f)
                lineTo(base.x + 9f, base.y - 10f)
                lineTo(base.x - 9f, base.y - 10f)
                close()
            }
            scope.drawPath(leafPath, treeColor)
        }
    }

    private fun drawPark(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, animProgress: Float) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Cobblestone path crossing
        scope.drawLine(Color(0xFFCFD8DC), Offset(center.x - halfW * 0.4f, center.y), Offset(center.x + halfW * 0.4f, center.y), strokeWidth = 4f)
        scope.drawLine(Color(0xFFCFD8DC), Offset(center.x, center.y - halfH * 0.4f), Offset(center.x, center.y + halfH * 0.4f), strokeWidth = 4f)

        // Flower beds (pink & orange)
        scope.drawCircle(Color(0xFFE91E63), radius = 3f, center = Offset(center.x - 10f, center.y - 6f))
        scope.drawCircle(Color(0xFFFF9800), radius = 3f, center = Offset(center.x + 10f, center.y + 6f))

        // Park Bench
        scope.drawRect(Color(0xFF795548), topLeft = Offset(center.x - 12f, center.y + 4f), size = Size(8f, 3f))

        // Central blossom tree
        val base = Offset(center.x, center.y - 4f)
        scope.drawLine(Color(0xFF5D4037), base, Offset(base.x, base.y - 14f), strokeWidth = 2.5f)
        scope.drawCircle(Color(0xFF81C784), radius = 10f, center = Offset(base.x, base.y - 18f))
    }

    private fun drawPlazaFountain(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, animProgress: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Marble Plaza Base
        val marblePath = Path().apply {
            moveTo(center.x, center.y - halfH * 0.7f)
            lineTo(center.x + halfW * 0.7f, center.y)
            lineTo(center.x, center.y + halfH * 0.7f)
            lineTo(center.x - halfW * 0.7f, center.y)
            close()
        }
        scope.drawPath(marblePath, Color(0xFFECEFF1))

        // Fountain Tier Pool
        scope.drawCircle(Color(0xFF90CAF9), radius = 14f, center = center)
        scope.drawCircle(Color(0xFF0288D1), radius = 11f, center = center)

        // Center fountain pillar
        scope.drawCircle(Color(0xFFCFD8DC), radius = 4f, center = Offset(center.x, center.y - 4f))

        // Dynamic Cascading Water Spray & Concentric Ripples
        EffectsRenderer.drawFountainWaterEffect(scope, Offset(center.x, center.y - 4f), animProgress, isNight)
    }

    private fun drawStadium(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean, isFestival: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Outer arena shell
        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 1.5f,
            blockH = halfH * 1.5f,
            heightPx = 28f,
            topColor = Color(0xFFB0BEC5),
            leftColor = Color(0xFF78909C),
            rightColor = Color(0xFF90A4AE)
        )

        // Inner football/soccer green pitch
        val pitchCenter = Offset(center.x, center.y - 28f)
        val pitchPath = Path().apply {
            moveTo(pitchCenter.x, pitchCenter.y - 10f)
            lineTo(pitchCenter.x + 18f, pitchCenter.y)
            lineTo(pitchCenter.x, pitchCenter.y + 10f)
            lineTo(pitchCenter.x - 18f, pitchCenter.y)
            close()
        }
        scope.drawPath(pitchPath, Color(0xFF388E3C))
        scope.drawLine(Color(0xFFFFFFFF), Offset(pitchCenter.x, pitchCenter.y - 10f), Offset(pitchCenter.x, pitchCenter.y + 10f), strokeWidth = 1f)

        // Stadium floodlight towers
        if (isNight || isFestival) {
            scope.drawCircle(Color(0xFFFFEB3B), radius = 4f, center = Offset(pitchCenter.x - 18f, pitchCenter.y - 12f))
            scope.drawCircle(Color(0xFFFFEB3B), radius = 4f, center = Offset(pitchCenter.x + 18f, pitchCenter.y - 12f))
        }
    }

    private fun drawWindTurbine(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, animProgress: Float) {
        val base = center
        val towerHeight = 36f
        val hub = Offset(base.x, base.y - towerHeight)

        // White tapered mast
        scope.drawLine(Color(0xFFECEFF1), base, hub, strokeWidth = 3.5f)

        // Rotor Hub
        scope.drawCircle(Color(0xFFCFD8DC), radius = 3.5f, center = hub)

        // 3 Spinning blades
        val angle = animProgress * 6.283f
        for (i in 0..2) {
            val bladeAngle = angle + (i * 2.094f) // 120 degrees
            val bladeLen = 20f
            val end = Offset(hub.x + cos(bladeAngle) * bladeLen, hub.y + sin(bladeAngle) * bladeLen)
            scope.drawLine(Color(0xFFFFFFFF), hub, end, strokeWidth = 2f)
        }
    }

    private fun drawSolarPark(scope: DrawScope, center: Offset, tileW: Float, tileH: Float) {
        val offsets = listOf(
            Offset(-12f, -6f),
            Offset(12f, -6f),
            Offset(-12f, 6f),
            Offset(12f, 6f)
        )
        for (off in offsets) {
            val panelCenter = Offset(center.x + off.x, center.y + off.y - 4f)
            // Angled blue solar panel
            val panelPath = Path().apply {
                moveTo(panelCenter.x - 9f, panelCenter.y - 3f)
                lineTo(panelCenter.x + 9f, panelCenter.y - 5f)
                lineTo(panelCenter.x + 9f, panelCenter.y + 3f)
                lineTo(panelCenter.x - 9f, panelCenter.y + 5f)
                close()
            }
            scope.drawPath(panelPath, Color(0xFF1565C0))
            scope.drawPath(panelPath, Color(0xFF90CAF9), style = Stroke(1f))
            // Stand
            scope.drawLine(Color(0xFF78909C), Offset(panelCenter.x, panelCenter.y + 4f), Offset(panelCenter.x, panelCenter.y + 9f), strokeWidth = 1.5f)
        }
    }

    private fun drawNuclearPowerPlant(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, animProgress: Float) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Reactor Dome
        draw3DBlock(
            scope = scope,
            baseCenter = Offset(center.x + 12f, center.y + 4f),
            blockW = halfW * 0.7f,
            blockH = halfH * 0.7f,
            heightPx = 18f,
            topColor = Color(0xFFECEFF1),
            leftColor = Color(0xFFB0BEC5),
            rightColor = Color(0xFFCFD8DC)
        )

        // Hyperbolic Cooling Tower
        val towerCenter = Offset(center.x - 10f, center.y - 4f)
        val towerHeight = 38f
        val towerBase = towerCenter
        val towerTop = Offset(towerCenter.x, towerCenter.y - towerHeight)

        val towerPath = Path().apply {
            moveTo(towerTop.x - 10f, towerTop.y)
            lineTo(towerTop.x + 10f, towerTop.y)
            lineTo(towerBase.x + 14f, towerBase.y)
            lineTo(towerBase.x - 14f, towerBase.y)
            close()
        }
        scope.drawPath(towerPath, Color(0xFFECEFF1))
        scope.drawPath(towerPath, Color(0xFF78909C), style = Stroke(1.5f))

        // Dynamic Billowing Cooling Tower Steam
        EffectsRenderer.drawSteamPlume(scope, Offset(towerTop.x, towerTop.y - 4f), animProgress)
    }

    private fun drawWaterTower(scope: DrawScope, center: Offset, tileW: Float, tileH: Float) {
        val base = center
        val towerH = 34f
        val tankCenter = Offset(base.x, base.y - towerH)

        // Metal Legs Truss
        val legLeft = Offset(base.x - 10f, base.y + 4f)
        val legRight = Offset(base.x + 10f, base.y + 4f)
        scope.drawLine(Color(0xFF546E7A), legLeft, tankCenter, strokeWidth = 2f)
        scope.drawLine(Color(0xFF546E7A), legRight, tankCenter, strokeWidth = 2f)
        scope.drawLine(Color(0xFF546E7A), base, tankCenter, strokeWidth = 2.5f)

        // Water Spherical / Cylindrical Tank
        scope.drawCircle(Color(0xFF0288D1), radius = 11f, center = tankCenter)
        scope.drawCircle(Color(0xFFB3E5FC), radius = 8f, center = Offset(tankCenter.x - 2f, tankCenter.y - 2f))
    }

    private fun drawWaterTreatment(scope: DrawScope, center: Offset, tileW: Float, tileH: Float) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        // Filter Pool
        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 1.1f,
            blockH = halfH * 1.1f,
            heightPx = 12f,
            topColor = Color(0xFF0288D1),
            leftColor = Color(0xFF607D8B),
            rightColor = Color(0xFF78909C)
        )
        // Water surface inside
        scope.drawCircle(Color(0xFF4FC3F7), radius = 10f, center = Offset(center.x, center.y - 12f))
    }

    // RESIDENTIAL BUILDINGS
    private fun drawCottage(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 16f + (level - 1) * 6f

        // House body
        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.7f,
            blockH = halfH * 0.7f,
            heightPx = height,
            topColor = Color(0xFFE57373), // Red tiled roof
            leftColor = Color(0xFFFFCCBC),
            rightColor = Color(0xFFFFE0B2)
        )

        // Windows (glow at night)
        val winColor = if (isNight) Color(0xFFFFEB3B) else Color(0xFF81D4FA)
        val roofPeak = Offset(center.x, center.y - height - 8f)
        scope.drawRect(winColor, topLeft = Offset(center.x - 7f, center.y - height * 0.6f), size = Size(4f, 4f))
        scope.drawRect(winColor, topLeft = Offset(center.x + 3f, center.y - height * 0.6f), size = Size(4f, 4f))

        // Chimney
        scope.drawRect(Color(0xFF8D6E63), topLeft = Offset(center.x + 6f, center.y - height - 10f), size = Size(3f, 6f))
    }

    private fun drawTownhouse(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 28f + (level - 1) * 8f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFF6D4C41),
            leftColor = Color(0xFFBF360C),
            rightColor = Color(0xFFD84315)
        )

        // Multiple window grids
        val winColor = if (isNight) Color(0xFFFFD54F) else Color(0xFFE0F7FA)
        for (floor in 1..3) {
            val wy = center.y - (floor * 8f)
            scope.drawRect(winColor, topLeft = Offset(center.x - 10f, wy), size = Size(3.5f, 4f))
            scope.drawRect(winColor, topLeft = Offset(center.x - 3f, wy), size = Size(3.5f, 4f))
            scope.drawRect(winColor, topLeft = Offset(center.x + 5f, wy), size = Size(3.5f, 4f))
        }
    }

    private fun drawHighriseApartments(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 50f + (level - 1) * 16f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFF37474F),
            leftColor = Color(0xFF455A64),
            rightColor = Color(0xFF546E7A)
        )

        // Glowing vertical window columns
        val winColor = if (isNight) Color(0xFFFFEE58) else Color(0xFF80DEEA)
        val floors = 6 + (level * 2)
        for (f in 1..floors) {
            val wy = center.y - (f * (height / (floors + 1)))
            scope.drawRect(winColor, topLeft = Offset(center.x - 12f, wy), size = Size(4f, 3f))
            scope.drawRect(winColor, topLeft = Offset(center.x - 4f, wy), size = Size(4f, 3f))
            scope.drawRect(winColor, topLeft = Offset(center.x + 4f, wy), size = Size(4f, 3f))
            scope.drawRect(winColor, topLeft = Offset(center.x + 10f, wy), size = Size(4f, 3f))
        }

        // Rooftop antenna
        val roofTop = Offset(center.x, center.y - height)
        scope.drawLine(Color(0xFFB0BEC5), roofTop, Offset(roofTop.x, roofTop.y - 12f), strokeWidth = 2f)
        if (isNight) {
            scope.drawCircle(Color(0xFFFF1744), radius = 2f, center = Offset(roofTop.x, roofTop.y - 12f))
        }
    }

    private fun drawLuxurySkyTower(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 75f + (level - 1) * 20f

        // Tier 1 Base
        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.9f,
            blockH = halfH * 0.9f,
            heightPx = height * 0.5f,
            topColor = Color(0xFF0D47A1),
            leftColor = Color(0xFF1565C0),
            rightColor = Color(0xFF1976D2)
        )

        // Tier 2 Spire Tower
        draw3DBlock(
            scope = scope,
            baseCenter = Offset(center.x, center.y - height * 0.5f),
            blockW = halfW * 0.6f,
            blockH = halfH * 0.6f,
            heightPx = height * 0.5f,
            topColor = Color(0xFF00E5FF),
            leftColor = Color(0xFF00B0FF),
            rightColor = Color(0xFF40C4FF)
        )

        // Golden Spire
        val tip = Offset(center.x, center.y - height - 16f)
        scope.drawLine(Color(0xFFFFD700), Offset(center.x, center.y - height), tip, strokeWidth = 2.5f)
        if (isNight) {
            scope.drawCircle(Color(0xFF00E5FF), radius = 3.5f, center = tip)
        }
    }

    // COMMERCIAL BUILDINGS
    private fun drawCornerShop(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 18f + (level - 1) * 6f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.75f,
            blockH = halfH * 0.75f,
            heightPx = height,
            topColor = Color(0xFF00897B),
            leftColor = Color(0xFF004D40),
            rightColor = Color(0xFF00695C)
        )

        // Striped awning over door
        val awningY = center.y - 8f
        scope.drawRect(Color(0xFFFF5252), topLeft = Offset(center.x - 10f, awningY), size = Size(8f, 3f))
        scope.drawRect(Color(0xFFFFFFFF), topLeft = Offset(center.x - 2f, awningY), size = Size(6f, 3f))

        // Storefront display
        val dispColor = if (isNight) Color(0xFFFFD54F) else Color(0xFFB2EBF2)
        scope.drawRect(dispColor, topLeft = Offset(center.x - 9f, center.y - 4f), size = Size(14f, 4f))
    }

    private fun drawShoppingMall(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 26f + (level - 1) * 8f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.9f,
            blockH = halfH * 0.9f,
            heightPx = height,
            topColor = Color(0xFF5E35B1),
            leftColor = Color(0xFF4527A0),
            rightColor = Color(0xFF512DA8)
        )

        // Glass skylight prism on top
        scope.drawCircle(Color(0xFF80DEEA), radius = 6f, center = Offset(center.x, center.y - height - 3f))

        // Neon Billboard / Sign
        val neonColor = if (isNight) Color(0xFFFF4081) else Color(0xFFC2185B)
        scope.drawRect(neonColor, topLeft = Offset(center.x - 12f, center.y - height * 0.7f), size = Size(20f, 5f))
    }

    private fun drawCorporateSkyscraper(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 65f + (level - 1) * 18f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFF00ACC1),
            leftColor = Color(0xFF006064),
            rightColor = Color(0xFF00838F)
        )

        // Grid of glass offices
        val winColor = if (isNight) Color(0xFFE0F7FA) else Color(0xFF80DEEA)
        val floors = 8 + (level * 2)
        for (f in 1..floors) {
            val wy = center.y - (f * (height / (floors + 1)))
            scope.drawLine(winColor, Offset(center.x - 14f, wy), Offset(center.x + 14f, wy), strokeWidth = 1.5f)
        }
    }

    private fun drawTechHQ(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 55f + (level - 1) * 15f

        // Futuristic tiered glass prism
        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.9f,
            blockH = halfH * 0.9f,
            heightPx = height,
            topColor = Color(0xFF7C4DFF),
            leftColor = Color(0xFF311B92),
            rightColor = Color(0xFF4527A0)
        )

        // Glowing neon energy lines
        val neon = if (isNight) Color(0xFF00E5FF) else Color(0xFF651FFF)
        scope.drawLine(neon, Offset(center.x - 14f, center.y - height * 0.5f), Offset(center.x + 14f, center.y - height * 0.5f), strokeWidth = 2f)
        scope.drawCircle(neon, radius = 5f, center = Offset(center.x, center.y - height - 4f))
    }

    // INDUSTRIAL BUILDINGS
    private fun drawWorkshop(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 18f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.8f,
            blockH = halfH * 0.8f,
            heightPx = height,
            topColor = Color(0xFF795548),
            leftColor = Color(0xFF4E342E),
            rightColor = Color(0xFF5D4037)
        )
        // Loading garage door
        scope.drawRect(Color(0xFF37474F), topLeft = Offset(center.x - 8f, center.y - 6f), size = Size(10f, 6f))
    }

    private fun drawHeavyFactory(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, animProgress: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 24f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFF455A64),
            leftColor = Color(0xFF263238),
            rightColor = Color(0xFF37474F)
        )

        // Smokestacks
        val stack1 = Offset(center.x - 10f, center.y - height)
        val stack2 = Offset(center.x + 8f, center.y - height)
        scope.drawLine(Color(0xFFB71C1C), stack1, Offset(stack1.x, stack1.y - 14f), strokeWidth = 3.5f)
        scope.drawLine(Color(0xFFB71C1C), stack2, Offset(stack2.x, stack2.y - 14f), strokeWidth = 3.5f)

        // Dynamic Billowing Chimney Smoke
        EffectsRenderer.drawSmokePlume(scope, Offset(stack1.x, stack1.y - 14f), animProgress)
        EffectsRenderer.drawSmokePlume(scope, Offset(stack2.x, stack2.y - 14f), (animProgress + 0.5f) % 1f)
    }

    private fun drawRoboticsLab(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 30f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFFECEFF1),
            leftColor = Color(0xFF90A4AE),
            rightColor = Color(0xFFB0BEC5)
        )
        // Cyan cleanroom glow
        val cleanGlow = if (isNight) Color(0xFF00E5FF) else Color(0xFF00BCD4)
        scope.drawRect(cleanGlow, topLeft = Offset(center.x - 12f, center.y - 14f), size = Size(18f, 5f))
    }

    private fun drawLogisticsDepot(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, level: Int, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 20f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.9f,
            blockH = halfH * 0.9f,
            heightPx = height,
            topColor = Color(0xFFFFB300),
            leftColor = Color(0xFFFF8F00),
            rightColor = Color(0xFFFFA000)
        )
        // 3 Loading bays
        for (i in -1..1) {
            scope.drawRect(Color(0xFF212121), topLeft = Offset(center.x + (i * 9f) - 3f, center.y - 6f), size = Size(6f, 6f))
        }
    }

    // CIVIC BUILDINGS
    private fun drawTownHall(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 34f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFFECEFF1),
            leftColor = Color(0xFFB0BEC5),
            rightColor = Color(0xFFCFD8DC)
        )

        // Clock Tower Spire
        val towerTop = Offset(center.x, center.y - height - 16f)
        scope.drawRect(Color(0xFFECEFF1), topLeft = Offset(center.x - 5f, center.y - height - 12f), size = Size(10f, 12f))
        // Clock face
        scope.drawCircle(Color(0xFFFFD54F), radius = 3f, center = Offset(center.x, center.y - height - 6f))
        // Flagpole
        scope.drawLine(Color(0xFF37474F), towerTop, Offset(towerTop.x, towerTop.y - 8f), strokeWidth = 1.5f)
        scope.drawRect(Color(0xFFE53935), topLeft = Offset(towerTop.x, towerTop.y - 8f), size = Size(5f, 3f))
    }

    private fun drawPoliceStation(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 28f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.8f,
            blockH = halfH * 0.8f,
            heightPx = height,
            topColor = Color(0xFF0D47A1),
            leftColor = Color(0xFF1565C0),
            rightColor = Color(0xFF1976D2)
        )

        // Blue Police Badge Emblem & Flashing Siren
        scope.drawCircle(Color(0xFF00E5FF), radius = 4f, center = Offset(center.x, center.y - height - 3f))
        if (isNight) {
            scope.drawCircle(Color(0xFF2979FF), radius = 6f, center = Offset(center.x, center.y - height - 3f))
        }
    }

    private fun drawFireStation(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 26f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.8f,
            blockH = halfH * 0.8f,
            heightPx = height,
            topColor = Color(0xFFB71C1C),
            leftColor = Color(0xFFC62828),
            rightColor = Color(0xFFD32F2F)
        )

        // Red Garage Roll Doors
        scope.drawRect(Color(0xFFFFFFFF), topLeft = Offset(center.x - 10f, center.y - 8f), size = Size(8f, 8f))
        scope.drawRect(Color(0xFFFFFFFF), topLeft = Offset(center.x + 2f, center.y - 8f), size = Size(8f, 8f))
    }

    private fun drawHospital(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 40f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.85f,
            blockH = halfH * 0.85f,
            heightPx = height,
            topColor = Color(0xFFFAFAFA),
            leftColor = Color(0xFFCFD8DC),
            rightColor = Color(0xFFECEFF1)
        )

        // Red Cross Symbol
        val crossCenter = Offset(center.x, center.y - height * 0.6f)
        scope.drawRect(Color(0xFFD50000), topLeft = Offset(crossCenter.x - 6f, crossCenter.y - 2f), size = Size(12f, 4f))
        scope.drawRect(Color(0xFFD50000), topLeft = Offset(crossCenter.x - 2f, crossCenter.y - 6f), size = Size(4f, 12f))
    }

    private fun drawUniversity(scope: DrawScope, center: Offset, tileW: Float, tileH: Float, isNight: Boolean) {
        val halfW = tileW / 2f
        val halfH = tileH / 2f
        val height = 36f

        draw3DBlock(
            scope = scope,
            baseCenter = center,
            blockW = halfW * 0.9f,
            blockH = halfH * 0.9f,
            heightPx = height,
            topColor = Color(0xFF8D6E63),
            leftColor = Color(0xFF4E342E),
            rightColor = Color(0xFF5D4037)
        )

        // Neoclassical Columns
        for (i in -2..2) {
            val cx = center.x + (i * 6f)
            scope.drawLine(Color(0xFFECEFF1), Offset(cx, center.y - 2f), Offset(cx, center.y - 16f), strokeWidth = 2f)
        }
    }

    // HELPER FOR 3D ISOMETRIC BLOCKS
    private fun draw3DBlock(
        scope: DrawScope,
        baseCenter: Offset,
        blockW: Float,
        blockH: Float,
        heightPx: Float,
        topColor: Color,
        leftColor: Color,
        rightColor: Color
    ) {
        val halfW = blockW / 2f
        val halfH = blockH / 2f

        // Base Diamond vertices
        val bTop = Offset(baseCenter.x, baseCenter.y - halfH)
        val bRight = Offset(baseCenter.x + halfW, baseCenter.y)
        val bBottom = Offset(baseCenter.x, baseCenter.y + halfH)
        val bLeft = Offset(baseCenter.x - halfW, baseCenter.y)

        // Elevated Top Diamond vertices
        val tTop = Offset(bTop.x, bTop.y - heightPx)
        val tRight = Offset(bRight.x, bRight.y - heightPx)
        val tBottom = Offset(bBottom.x, bBottom.y - heightPx)
        val tLeft = Offset(bLeft.x, bLeft.y - heightPx)

        // Left Face
        val leftFace = Path().apply {
            moveTo(bLeft.x, bLeft.y)
            lineTo(tLeft.x, tLeft.y)
            lineTo(tBottom.x, tBottom.y)
            lineTo(bBottom.x, bBottom.y)
            close()
        }
        scope.drawPath(leftFace, leftColor)

        // Right Face
        val rightFace = Path().apply {
            moveTo(bBottom.x, bBottom.y)
            lineTo(tBottom.x, tBottom.y)
            lineTo(tRight.x, tRight.y)
            lineTo(bRight.x, bRight.y)
            close()
        }
        scope.drawPath(rightFace, rightColor)

        // Top Face
        val topFace = Path().apply {
            moveTo(tTop.x, tTop.y)
            lineTo(tRight.x, tRight.y)
            lineTo(tBottom.x, tBottom.y)
            lineTo(tLeft.x, tLeft.y)
            close()
        }
        scope.drawPath(topFace, topColor)
    }

    // DRAW VEHICLES
    fun drawVehicle(
        scope: DrawScope,
        vehicle: Vehicle,
        offsetX: Float,
        offsetY: Float,
        zoom: Float,
        timeOfDay: Float
    ) {
        val pos = IsometricMath.gridToScreen(vehicle.x, vehicle.y, offsetX, offsetY, zoom, vehicle.altitude)
        val isNight = isNightTime(timeOfDay)

        when (vehicle.type.category) {
            VehicleCategory.AIR -> {
                // Airplane / Helicopter
                val shadowPos = IsometricMath.gridToScreen(vehicle.x, vehicle.y, offsetX, offsetY, zoom, 0f)
                // Ground shadow
                scope.drawOval(Color(0x33000000), topLeft = Offset(shadowPos.x - 10f * zoom, shadowPos.y - 5f * zoom), size = Size(20f * zoom, 10f * zoom))

                // Aircraft body
                scope.drawCircle(Color(vehicle.color), radius = 6f * zoom, center = pos)
                // Wings
                val wingSpan = 14f * zoom
                val heading = vehicle.heading
                val leftWing = Offset(pos.x + cos(heading + 1.57f) * wingSpan, pos.y + sin(heading + 1.57f) * (wingSpan * 0.5f))
                val rightWing = Offset(pos.x + cos(heading - 1.57f) * wingSpan, pos.y + sin(heading - 1.57f) * (wingSpan * 0.5f))
                scope.drawLine(Color(0xFFECEFF1), leftWing, rightWing, strokeWidth = 3f * zoom)

                // Blinking strobe wing lights
                if (isNight) {
                    scope.drawCircle(Color(0xFFFF1744), radius = 2.5f * zoom, center = leftWing)
                    scope.drawCircle(Color(0xFF00E676), radius = 2.5f * zoom, center = rightWing)
                }
            }
            VehicleCategory.RAIL -> {
                // Train Engine / Carriages
                scope.drawRoundRect(
                    color = Color(vehicle.color),
                    topLeft = Offset(pos.x - 8f * zoom, pos.y - 10f * zoom),
                    size = Size(16f * zoom, 12f * zoom),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Train Headlight beam at night
                if (isNight) {
                    scope.drawCircle(Color(0xFFFFF9C4), radius = 8f * zoom, center = pos)
                }
            }
            VehicleCategory.WATER -> {
                // Boat on water with wake foam
                scope.drawOval(Color(0x44FFFFFF), topLeft = Offset(pos.x - 10f * zoom, pos.y - 4f * zoom), size = Size(20f * zoom, 8f * zoom))
                scope.drawRoundRect(
                    color = Color(vehicle.color),
                    topLeft = Offset(pos.x - 6f * zoom, pos.y - 6f * zoom),
                    size = Size(12f * zoom, 8f * zoom),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
            VehicleCategory.ROAD -> {
                // Car / Bus / Truck
                val isBusOrTruck = vehicle.type == VehicleType.BUS || vehicle.type == VehicleType.TRUCK || vehicle.type == VehicleType.FIRE_ENGINE
                val carW = (if (isBusOrTruck) 14f else 9f) * zoom
                val carH = (if (isBusOrTruck) 8f else 6f) * zoom

                // Car Body
                scope.drawRoundRect(
                    color = Color(vehicle.color),
                    topLeft = Offset(pos.x - carW / 2f, pos.y - carH / 2f - (4f * zoom)),
                    size = Size(carW, carH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // Car Cabin Roof
                scope.drawRoundRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(pos.x - carW * 0.3f, pos.y - carH * 0.8f - (4f * zoom)),
                    size = Size(carW * 0.6f, carH * 0.7f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
                )

                // Headlight Beams at Night
                if (isNight) {
                    val hx = pos.x + cos(vehicle.heading) * (12f * zoom)
                    val hy = pos.y + sin(vehicle.heading) * (6f * zoom)
                    scope.drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0x99FFF9C4), Color(0x00FFF9C4)),
                            center = Offset(hx, hy),
                            radius = 16f * zoom
                        ),
                        radius = 16f * zoom,
                        center = Offset(hx, hy)
                    )
                }
            }
        }
    }

    // DRAW PEDESTRIANS
    fun drawCitizen(
        scope: DrawScope,
        citizen: Citizen,
        offsetX: Float,
        offsetY: Float,
        zoom: Float
    ) {
        val pos = IsometricMath.gridToScreen(citizen.x, citizen.y, offsetX, offsetY, zoom, 0f)
        val r = 2.5f * zoom
        // Head
        scope.drawCircle(Color(0xFFFFCC80), radius = r, center = Offset(pos.x, pos.y - 7f * zoom))
        // Shirt body
        scope.drawRect(Color(citizen.shirtColor), topLeft = Offset(pos.x - r * 0.9f, pos.y - 4.5f * zoom), size = Size(r * 1.8f, 3.5f * zoom))
        // Pants
        scope.drawRect(Color(0xFF1E3A8A), topLeft = Offset(pos.x - r * 0.8f, pos.y - 1f * zoom), size = Size(r * 1.6f, 3f * zoom))
    }

    // TILE HIGHLIGHT / SELECTION
    fun drawTileHighlight(
        scope: DrawScope,
        gridX: Int,
        gridY: Int,
        offsetX: Float,
        offsetY: Float,
        zoom: Float,
        highlightColor: Color,
        isBulldoze: Boolean = false
    ) {
        val center = IsometricMath.gridToScreen(gridX.toFloat(), gridY.toFloat(), offsetX, offsetY, zoom, 0f)
        val halfW = (IsometricMath.BASE_TILE_WIDTH * zoom) / 2f
        val halfH = (IsometricMath.BASE_TILE_HEIGHT * zoom) / 2f

        val diamondPath = Path().apply {
            moveTo(center.x, center.y - halfH)
            lineTo(center.x + halfW, center.y)
            lineTo(center.x, center.y + halfH)
            lineTo(center.x - halfW, center.y)
            close()
        }

        // Fill glow
        scope.drawPath(diamondPath, highlightColor.copy(alpha = 0.35f))
        // Outline
        scope.drawPath(diamondPath, highlightColor, style = Stroke(width = 2.5f))

        if (isBulldoze) {
            // Draw red X
            scope.drawLine(Color(0xFFFF1744), Offset(center.x - 8f * zoom, center.y - 4f * zoom), Offset(center.x + 8f * zoom, center.y + 4f * zoom), strokeWidth = 3f * zoom)
            scope.drawLine(Color(0xFFFF1744), Offset(center.x + 8f * zoom, center.y - 4f * zoom), Offset(center.x - 8f * zoom, center.y + 4f * zoom), strokeWidth = 3f * zoom)
        }
    }
}
