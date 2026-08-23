package com.example.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CityTile
import com.example.model.TileType
import kotlin.math.cos
import kotlin.math.sin

object TerrainRenderer {

    /**
     * Draws the 3D ground base diamond and enriched terrain surface textures:
     * grass tufts, wildflowers, ocean depth, animated wave caustics, coastline foam,
     * sand pebbles, mountain rock facets, and perimeter cliff slab drop-offs.
     */
    fun drawTerrain(
        scope: DrawScope,
        tile: CityTile,
        center: Offset,
        tileW: Float,
        tileH: Float,
        timeOfDay: Float,
        animProgress: Float,
        isMapEdgeSE: Boolean,
        isMapEdgeSW: Boolean
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

        // 1. Draw 3D Perimeter Cliff Walls (Bedrock & Soil Layers) for the island edge
        drawIslandPerimeterCliffs(scope, center, halfW, halfH, tile.type, isMapEdgeSE, isMapEdgeSW)

        // 2. Draw Elevation Cliffs if elevated (Mountain or custom elevation)
        if (tile.elevation > 0f || tile.type == TileType.MOUNTAIN) {
            drawElevationCliffFaces(scope, center, halfW, halfH, tile)
        }

        // 3. Draw Specific Terrain Surface
        when (tile.type) {
            TileType.WATER, TileType.MARINA -> {
                drawWaterTerrain(scope, center, halfW, halfH, diamondPath, animProgress, timeOfDay)
            }
            TileType.SAND -> {
                drawSandTerrain(scope, center, halfW, halfH, diamondPath, tile)
            }
            TileType.MOUNTAIN -> {
                drawMountainTerrain(scope, center, halfW, halfH, diamondPath, tile)
            }
            TileType.FOREST -> {
                drawForestFloor(scope, center, halfW, halfH, diamondPath, tile)
            }
            TileType.ROAD_BRIDGE, TileType.RAIL_BRIDGE -> {
                drawWaterTerrain(scope, center, halfW, halfH, diamondPath, animProgress, timeOfDay)
            }
            else -> {
                drawGrassMeadow(scope, center, halfW, halfH, diamondPath, tile, animProgress)
            }
        }

        // 4. Subtle Ambient Tile Grid Outline
        val gridColor = when {
            tile.type.isWaterTile -> Color(0x2201579B)
            tile.type == TileType.SAND -> Color(0x22D4A373)
            tile.type == TileType.MOUNTAIN -> Color(0x3337474F)
            else -> Color(0x181B5E20)
        }
        scope.drawPath(diamondPath, color = gridColor, style = Stroke(width = 1f))
    }

    private fun drawGrassMeadow(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        path: Path,
        tile: CityTile,
        animProgress: Float
    ) {
        // Multi-tone lush grass gradient (sunlit top, richer depth base)
        val grassBrush = Brush.radialGradient(
            listOf(Color(0xFF5CB85C), Color(0xFF43A047), Color(0xFF388E3C)),
            center = Offset(center.x - halfW * 0.2f, center.y - halfH * 0.3f),
            radius = halfW * 1.3f
        )
        scope.drawPath(path, brush = grassBrush)

        // Seeded deterministic variation based on tile coordinates
        val hash = (tile.x * 73 + tile.y * 97)
        val varType = kotlin.math.abs(hash) % 6

        // Subtle grass blade tufts with gentle wind sway
        val sway = sin(animProgress * 4f + tile.x * 0.5f) * 1.5f
        when (varType) {
            0, 1 -> {
                // Two small grass tufts
                val gx = center.x - halfW * 0.25f
                val gy = center.y + halfH * 0.15f
                scope.drawLine(Color(0xFF2E7D32), Offset(gx, gy), Offset(gx - 2f + sway, gy - 4f), strokeWidth = 1.5f)
                scope.drawLine(Color(0xFF66BB6A), Offset(gx + 2f, gy), Offset(gx + 4f + sway, gy - 5f), strokeWidth = 1.5f)
            }
            2 -> {
                // Tiny yellow buttercup flowers
                val fx = center.x + halfW * 0.2f
                val fy = center.y - halfH * 0.1f
                scope.drawCircle(Color(0xFFFFD54F), radius = 1.8f, center = Offset(fx, fy))
                scope.drawCircle(Color(0xFFFFF59D), radius = 1.0f, center = Offset(fx, fy))
                scope.drawCircle(Color(0xFFFFD54F), radius = 1.6f, center = Offset(fx - 6f, fy + 5f))
            }
            3 -> {
                // Delicate white daisies with golden center
                val fx = center.x - halfW * 0.2f
                val fy = center.y - halfH * 0.15f
                scope.drawCircle(Color(0xFFFFFFFF), radius = 2.0f, center = Offset(fx, fy))
                scope.drawCircle(Color(0xFFFFB300), radius = 0.9f, center = Offset(fx, fy))
                // Daisy 2
                val fx2 = center.x + halfW * 0.28f
                val fy2 = center.y + halfH * 0.2f
                scope.drawCircle(Color(0xFFFFFFFF), radius = 1.8f, center = Offset(fx2, fy2))
                scope.drawCircle(Color(0xFFFFB300), radius = 0.8f, center = Offset(fx2, fy2))
            }
            4 -> {
                // Vibrant coral poppy / lavender bloom
                val fx = center.x + halfW * 0.15f
                val fy = center.y + halfH * 0.22f
                scope.drawCircle(Color(0xFFFF5252), radius = 2.0f, center = Offset(fx, fy))
                scope.drawCircle(Color(0xFF7E57C2), radius = 1.6f, center = Offset(fx - 8f, fy - 6f))
            }
            5 -> {
                // Subtle clover / rich patch
                scope.drawOval(
                    Color(0x332E7D32),
                    topLeft = Offset(center.x - 7f, center.y - 4f),
                    size = Size(14f, 8f)
                )
            }
        }
    }

    private fun drawWaterTerrain(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        path: Path,
        animProgress: Float,
        timeOfDay: Float
    ) {
        // Deep turquoise to ocean sapphire water gradient
        val waterBrush = Brush.radialGradient(
            listOf(Color(0xFF00ACC1), Color(0xFF0288D1), Color(0xFF01579B)),
            center = center,
            radius = halfW * 1.2f
        )
        scope.drawPath(path, brush = waterBrush)

        // Animated Dual-Layer Water Caustics / Ripple Shimmer
        val wave1 = sin(animProgress * 5.0f + center.x * 0.08f + center.y * 0.04f) * 3f
        val wave2 = cos(animProgress * 4.0f + center.y * 0.08f - center.x * 0.04f) * 2.5f

        // Ripple layer 1
        scope.drawLine(
            color = Color(0x66E0F7FA),
            start = Offset(center.x - halfW * 0.45f + wave1, center.y - halfH * 0.2f),
            end = Offset(center.x + halfW * 0.35f + wave1, center.y - halfH * 0.2f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Ripple layer 2
        scope.drawLine(
            color = Color(0x55B2EBF2),
            start = Offset(center.x - halfW * 0.3f + wave2, center.y + halfH * 0.25f),
            end = Offset(center.x + halfW * 0.45f + wave2, center.y + halfH * 0.25f),
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        // Specular Sun Glint
        val glintAlpha = (sin(animProgress * 6f + center.x * 0.1f) * 0.5f + 0.5f) * 0.7f
        if (glintAlpha > 0.4f && timeOfDay in 7.0f..18.0f) {
            scope.drawCircle(
                color = Color.White.copy(alpha = glintAlpha),
                radius = 1.5f,
                center = Offset(center.x + halfW * 0.15f + wave1 * 0.5f, center.y - 2f)
            )
        }
    }

    private fun drawSandTerrain(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        path: Path,
        tile: CityTile
    ) {
        // Warm sunny beach gradient
        val sandBrush = Brush.radialGradient(
            listOf(Color(0xFFFDE68A), Color(0xFFF6D58C), Color(0xFFE5C07B)),
            center = center,
            radius = halfW * 1.2f
        )
        scope.drawPath(path, brush = sandBrush)

        // Tiny sand specks / pebbles
        val hash = (tile.x * 47 + tile.y * 83)
        val p1 = Offset(center.x - halfW * 0.25f, center.y + halfH * 0.15f)
        val p2 = Offset(center.x + halfW * 0.3f, center.y - halfH * 0.1f)
        scope.drawCircle(Color(0xFFD4A373), radius = 1.2f, center = p1)
        scope.drawCircle(Color(0xFFD4A373), radius = 1.4f, center = p2)
        if (hash % 3 == 0) {
            scope.drawCircle(Color(0xFFB08968), radius = 1.6f, center = Offset(center.x, center.y + halfH * 0.3f))
        }
    }

    private fun drawForestFloor(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        path: Path,
        tile: CityTile
    ) {
        // Rich, dark humus mossy earth
        val forestBrush = Brush.radialGradient(
            listOf(Color(0xFF33691E), Color(0xFF2E7D32), Color(0xFF1B5E20)),
            center = center,
            radius = halfW * 1.3f
        )
        scope.drawPath(path, brush = forestBrush)

        // Fallen leaf / moss patches
        scope.drawOval(
            Color(0x441B5E20),
            topLeft = Offset(center.x - 8f, center.y - 4f),
            size = Size(16f, 8f)
        )
    }

    private fun drawMountainTerrain(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        path: Path,
        tile: CityTile
    ) {
        // Rocky slate base
        val rockBrush = Brush.radialGradient(
            listOf(Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFF546E7A)),
            center = center,
            radius = halfW * 1.3f
        )
        scope.drawPath(path, brush = rockBrush)

        // Rock crags & faceted contours
        val crag1 = Path().apply {
            moveTo(center.x - halfW * 0.4f, center.y)
            lineTo(center.x, center.y - halfH * 0.4f)
            lineTo(center.x + halfW * 0.2f, center.y)
            close()
        }
        scope.drawPath(crag1, Color(0xFF607D8B))

        // Peak snow cap
        scope.drawCircle(Color(0xEEFFFFFF), radius = 3.5f, center = Offset(center.x, center.y - halfH * 0.35f))
    }

    /**
     * Draws the 3D isometric perimeter cliff drop-off along the outer borders of the island.
     */
    private fun drawIslandPerimeterCliffs(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        tileType: TileType,
        isMapEdgeSE: Boolean,
        isMapEdgeSW: Boolean
    ) {
        if (!isMapEdgeSE && !isMapEdgeSW) return

        val cliffDrop = 22f

        val right = Offset(center.x + halfW, center.y)
        val bottom = Offset(center.x, center.y + halfH)
        val left = Offset(center.x - halfW, center.y)

        // South-East Face (Right to Bottom)
        if (isMapEdgeSE) {
            val seCliffPath = Path().apply {
                moveTo(bottom.x, bottom.y)
                lineTo(right.x, right.y)
                lineTo(right.x, right.y + cliffDrop)
                lineTo(bottom.x, bottom.y + cliffDrop)
                close()
            }
            // Bedrock / Soil gradient
            val seBrush = Brush.verticalGradient(
                listOf(
                    if (tileType.isWaterTile) Color(0xFF01579B) else Color(0xFF5D4037),
                    Color(0xFF3E2723),
                    Color(0xFF212121)
                ),
                startY = right.y,
                endY = bottom.y + cliffDrop
            )
            scope.drawPath(seCliffPath, brush = seBrush)

            // Top turf overhang stripe
            if (!tileType.isWaterTile) {
                scope.drawLine(
                    Color(0xFF2E7D32),
                    bottom,
                    right,
                    strokeWidth = 3f
                )
            }
            // Strata lines
            scope.drawLine(
                Color(0x33000000),
                Offset(bottom.x, bottom.y + 8f),
                Offset(right.x, right.y + 8f),
                strokeWidth = 1.5f
            )
        }

        // South-West Face (Bottom to Left)
        if (isMapEdgeSW) {
            val swCliffPath = Path().apply {
                moveTo(left.x, left.y)
                lineTo(bottom.x, bottom.y)
                lineTo(bottom.x, bottom.y + cliffDrop)
                lineTo(left.x, left.y + cliffDrop)
                close()
            }
            // Slightly darker shadow for SW face
            val swBrush = Brush.verticalGradient(
                listOf(
                    if (tileType.isWaterTile) Color(0xFF004D40) else Color(0xFF4E342E),
                    Color(0xFF2D1E18),
                    Color(0xFF181818)
                ),
                startY = left.y,
                endY = bottom.y + cliffDrop
            )
            scope.drawPath(swCliffPath, brush = swBrush)

            // Top turf overhang stripe
            if (!tileType.isWaterTile) {
                scope.drawLine(
                    Color(0xFF1B5E20),
                    left,
                    bottom,
                    strokeWidth = 3f
                )
            }
            // Strata lines
            scope.drawLine(
                Color(0x33000000),
                Offset(left.x, left.y + 8f),
                Offset(bottom.x, bottom.y + 8f),
                strokeWidth = 1.5f
            )
        }
    }

    /**
     * Draws vertical 3D rock cliff sides for elevated terrain (mountains / elevated blocks).
     */
    private fun drawElevationCliffFaces(
        scope: DrawScope,
        center: Offset,
        halfW: Float,
        halfH: Float,
        tile: CityTile
    ) {
        val elevH = if (tile.type == TileType.MOUNTAIN) 28f else (tile.elevation * 16f)
        if (elevH <= 0f) return

        val right = Offset(center.x + halfW, center.y)
        val bottom = Offset(center.x, center.y + halfH)
        val left = Offset(center.x - halfW, center.y)

        // Left Face (SW)
        val swPath = Path().apply {
            moveTo(left.x, left.y)
            lineTo(bottom.x, bottom.y)
            lineTo(bottom.x, bottom.y + elevH)
            lineTo(left.x, left.y + elevH)
            close()
        }
        scope.drawPath(swPath, Color(0xFF37474F))

        // Right Face (SE)
        val sePath = Path().apply {
            moveTo(bottom.x, bottom.y)
            lineTo(right.x, right.y)
            lineTo(right.x, right.y + elevH)
            lineTo(bottom.x, bottom.y + elevH)
            close()
        }
        scope.drawPath(sePath, Color(0xFF455A64))

        // Ridge highlights
        scope.drawLine(Color(0xFF78909C), bottom, Offset(bottom.x, bottom.y + elevH), strokeWidth = 2f)
    }
}
