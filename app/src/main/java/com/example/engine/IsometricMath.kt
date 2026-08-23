package com.example.engine

import androidx.compose.ui.geometry.Offset

object IsometricMath {
    const val BASE_TILE_WIDTH = 80f
    const val BASE_TILE_HEIGHT = 40f

    fun gridToScreen(
        gx: Float,
        gy: Float,
        offsetX: Float,
        offsetY: Float,
        zoom: Float,
        elevation: Float = 0f
    ): Offset {
        val tileW = BASE_TILE_WIDTH * zoom
        val tileH = BASE_TILE_HEIGHT * zoom
        val screenX = (gx - gy) * (tileW / 2f) + offsetX
        val screenY = (gx + gy) * (tileH / 2f) + offsetY - (elevation * 24f * zoom)
        return Offset(screenX, screenY)
    }

    fun screenToGrid(
        screenX: Float,
        screenY: Float,
        offsetX: Float,
        offsetY: Float,
        zoom: Float
    ): Pair<Int, Int> {
        val tileW = BASE_TILE_WIDTH * zoom
        val tileH = BASE_TILE_HEIGHT * zoom
        val relX = screenX - offsetX
        val relY = screenY - offsetY

        val gx = (relX / (tileW / 2f) + relY / (tileH / 2f)) / 2f
        val gy = (relY / (tileH / 2f) - relX / (tileW / 2f)) / 2f

        return Pair(kotlin.math.floor(gx).toInt(), kotlin.math.floor(gy).toInt())
    }

    fun screenToExactGrid(
        screenX: Float,
        screenY: Float,
        offsetX: Float,
        offsetY: Float,
        zoom: Float
    ): Pair<Float, Float> {
        val tileW = BASE_TILE_WIDTH * zoom
        val tileH = BASE_TILE_HEIGHT * zoom
        val relX = screenX - offsetX
        val relY = screenY - offsetY

        val gx = (relX / (tileW / 2f) + relY / (tileH / 2f)) / 2f
        val gy = (relY / (tileH / 2f) - relX / (tileW / 2f)) / 2f

        return Pair(gx, gy)
    }
}
