package com.example.engine

import com.example.model.CityTile
import com.example.model.TileType
import kotlin.random.Random

object RoadNetwork {
    // Road bitmasks:
    // NORTH = 1 (gx, gy - 1)
    // EAST  = 2 (gx + 1, gy)
    // SOUTH = 4 (gx, gy + 1)
    // WEST  = 8 (gx - 1, gy)

    fun updateConnections(grid: Array<Array<CityTile>>, width: Int, height: Int) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val tile = grid[y][x]
                if (tile.type.isRoad) {
                    var mask = 0
                    if (y > 0 && grid[y - 1][x].type.isRoad) mask = mask or 1 // North
                    if (x < width - 1 && grid[y][x + 1].type.isRoad) mask = mask or 2 // East
                    if (y < height - 1 && grid[y + 1][x].type.isRoad) mask = mask or 4 // South
                    if (x > 0 && grid[y][x - 1].type.isRoad) mask = mask or 8 // West
                    tile.roadMask = mask
                } else if (tile.type.isRail) {
                    var mask = 0
                    if (y > 0 && grid[y - 1][x].type.isRail) mask = mask or 1
                    if (x < width - 1 && grid[y][x + 1].type.isRail) mask = mask or 2
                    if (y < height - 1 && grid[y + 1][x].type.isRail) mask = mask or 4
                    if (x > 0 && grid[y][x - 1].type.isRail) mask = mask or 8
                    tile.roadMask = mask
                }
            }
        }
    }

    fun findConnectedRoadPath(
        grid: Array<Array<CityTile>>,
        startX: Int,
        startY: Int,
        maxSteps: Int = 30
    ): List<Pair<Float, Float>> {
        val path = mutableListOf<Pair<Float, Float>>()
        var currX = startX
        var currY = startY
        var prevX = -1
        var prevY = -1

        path.add(Pair(currX + 0.5f, currY + 0.5f))

        val dirs = listOf(
            Pair(0, -1), // North
            Pair(1, 0),  // East
            Pair(0, 1),  // South
            Pair(-1, 0)  // West
        )

        val height = grid.size
        val width = if (height > 0) grid[0].size else 0

        for (step in 0 until maxSteps) {
            val candidates = mutableListOf<Pair<Int, Int>>()
            for ((dx, dy) in dirs) {
                val nx = currX + dx
                val ny = currY + dy
                if (nx in 0 until width && ny in 0 until height) {
                    if (grid[ny][nx].type.isRoad && !(nx == prevX && ny == prevY)) {
                        candidates.add(Pair(nx, ny))
                    }
                }
            }

            if (candidates.isEmpty()) {
                // If dead end, try reversing
                for ((dx, dy) in dirs) {
                    val nx = currX + dx
                    val ny = currY + dy
                    if (nx in 0 until width && ny in 0 until height && grid[ny][nx].type.isRoad) {
                        candidates.add(Pair(nx, ny))
                    }
                }
            }

            if (candidates.isEmpty()) break

            val next = candidates[Random.nextInt(candidates.size)]
            prevX = currX
            prevY = currY
            currX = next.first
            currY = next.second
            path.add(Pair(currX + 0.5f, currY + 0.5f))
        }

        return path
    }

    fun findConnectedRailPath(
        grid: Array<Array<CityTile>>,
        startX: Int,
        startY: Int,
        maxSteps: Int = 40
    ): List<Pair<Float, Float>> {
        val path = mutableListOf<Pair<Float, Float>>()
        var currX = startX
        var currY = startY
        var prevX = -1
        var prevY = -1

        path.add(Pair(currX + 0.5f, currY + 0.5f))

        val dirs = listOf(Pair(0, -1), Pair(1, 0), Pair(0, 1), Pair(-1, 0))
        val height = grid.size
        val width = if (height > 0) grid[0].size else 0

        for (step in 0 until maxSteps) {
            val candidates = mutableListOf<Pair<Int, Int>>()
            for ((dx, dy) in dirs) {
                val nx = currX + dx
                val ny = currY + dy
                if (nx in 0 until width && ny in 0 until height) {
                    if (grid[ny][nx].type.isRail && !(nx == prevX && ny == prevY)) {
                        candidates.add(Pair(nx, ny))
                    }
                }
            }
            if (candidates.isEmpty()) break
            val next = candidates[Random.nextInt(candidates.size)]
            prevX = currX
            prevY = currY
            currX = next.first
            currY = next.second
            path.add(Pair(currX + 0.5f, currY + 0.5f))
        }
        return path
    }

    fun findWaterPath(
        grid: Array<Array<CityTile>>,
        startX: Int,
        startY: Int,
        maxSteps: Int = 35
    ): List<Pair<Float, Float>> {
        val path = mutableListOf<Pair<Float, Float>>()
        var currX = startX
        var currY = startY
        var prevX = -1
        var prevY = -1

        path.add(Pair(currX + 0.5f, currY + 0.5f))
        val dirs = listOf(Pair(0, -1), Pair(1, 0), Pair(0, 1), Pair(-1, 0))
        val height = grid.size
        val width = if (height > 0) grid[0].size else 0

        for (step in 0 until maxSteps) {
            val candidates = mutableListOf<Pair<Int, Int>>()
            for ((dx, dy) in dirs) {
                val nx = currX + dx
                val ny = currY + dy
                if (nx in 0 until width && ny in 0 until height) {
                    if (grid[ny][nx].type.isWaterTile && !(nx == prevX && ny == prevY)) {
                        candidates.add(Pair(nx, ny))
                    }
                }
            }
            if (candidates.isEmpty()) break
            val next = candidates[Random.nextInt(candidates.size)]
            prevX = currX
            prevY = currY
            currX = next.first
            currY = next.second
            path.add(Pair(currX + 0.5f, currY + 0.5f))
        }
        return path
    }
}
