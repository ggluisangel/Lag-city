package com.example.engine

import com.example.model.CityTile
import com.example.model.TileType
import kotlin.math.sin
import kotlin.random.Random

enum class MapPreset(val title: String, val description: String, val width: Int, val height: Int) {
    EMERALD_VALLEY("Emerald Valley", "Meandering river, rolling green meadows, scattered forests", 20, 20),
    COASTAL_BAY("Coastal Harbor", "Sweeping ocean bay, sandy beaches, natural seaport", 22, 22),
    TWIN_RIVERS("Twin Rivers Delta", "Two converging rivers with fertile central islands", 20, 20),
    SANDBOX_PLAIN("Open Sandbox", "Pristine flat green plain ready for pure creativity", 18, 18)
}

object MapGenerator {
    fun generateMap(preset: MapPreset): Array<Array<CityTile>> {
        val width = preset.width
        val height = preset.height
        val grid = Array(height) { y ->
            Array(width) { x ->
                CityTile(x = x, y = y, type = TileType.GRASS)
            }
        }

        when (preset) {
            MapPreset.EMERALD_VALLEY -> generateEmeraldValley(grid, width, height)
            MapPreset.COASTAL_BAY -> generateCoastalBay(grid, width, height)
            MapPreset.TWIN_RIVERS -> generateTwinRivers(grid, width, height)
            MapPreset.SANDBOX_PLAIN -> generateSandbox(grid, width, height)
        }

        RoadNetwork.updateConnections(grid, width, height)
        return grid
    }

    private fun generateEmeraldValley(grid: Array<Array<CityTile>>, width: Int, height: Int) {
        // Meandering river from top to bottom
        for (y in 0 until height) {
            val riverCenter = (width / 2.0 + sin(y * 0.45) * 3.5).toInt().coerceIn(2, width - 3)
            for (dx in -1..1) {
                val rx = riverCenter + dx
                if (rx in 0 until width) {
                    grid[y][rx].type = TileType.WATER
                }
            }
            // Sand along river banks
            val leftBank = riverCenter - 2
            val rightBank = riverCenter + 2
            if (leftBank in 0 until width && grid[y][leftBank].type != TileType.WATER) {
                if (Random.nextFloat() > 0.3f) grid[y][leftBank].type = TileType.SAND
            }
            if (rightBank in 0 until width && grid[y][rightBank].type != TileType.WATER) {
                if (Random.nextFloat() > 0.3f) grid[y][rightBank].type = TileType.SAND
            }
        }

        // Forests
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (grid[y][x].type == TileType.GRASS) {
                    val distFromEdge = minOf(x, y, width - 1 - x, height - 1 - y)
                    if (distFromEdge <= 2 && Random.nextFloat() < 0.45f) {
                        grid[y][x].type = TileType.FOREST
                    } else if (Random.nextFloat() < 0.12f) {
                        grid[y][x].type = TileType.FOREST
                    }
                }
            }
        }

        // Seed a small starter town!
        val startY = height / 2
        val startX = 3
        // Starter road
        for (x in 2..6) {
            grid[startY][x].type = TileType.ROAD_PAVED
        }
        for (y in (startY - 2)..(startY + 2)) {
            grid[y][4].type = TileType.ROAD_PAVED
        }
        // Starter houses & shops & wind turbine
        grid[startY - 1][3].type = TileType.RESIDENTIAL_SMALL
        grid[startY + 1][3].type = TileType.RESIDENTIAL_SMALL
        grid[startY - 1][5].type = TileType.COMMERCIAL_SMALL
        grid[startY + 1][5].type = TileType.COMMERCIAL_SMALL
        grid[startY - 2][2].type = TileType.WIND_TURBINE
        grid[startY + 2][2].type = TileType.WATER_TOWER
        grid[startY][2].type = TileType.PARK_SMALL
    }

    private fun generateCoastalBay(grid: Array<Array<CityTile>>, width: Int, height: Int) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val coastCurve = (width * 0.45 + sin(y * 0.3) * 3.0).toInt()
                if (x >= coastCurve + 1) {
                    grid[y][x].type = TileType.WATER
                } else if (x == coastCurve) {
                    grid[y][x].type = TileType.SAND
                } else if (Random.nextFloat() < 0.15f) {
                    grid[y][x].type = TileType.FOREST
                }
            }
        }

        // Starter coastal road and marina
        val midY = height / 2
        for (y in (midY - 3)..(midY + 3)) {
            grid[y][3].type = TileType.ROAD_PAVED
        }
        for (x in 1..4) {
            grid[midY][x].type = TileType.ROAD_PAVED
        }
        grid[midY - 1][2].type = TileType.RESIDENTIAL_SMALL
        grid[midY + 1][2].type = TileType.COMMERCIAL_SMALL
        grid[midY - 2][2].type = TileType.WIND_TURBINE
        grid[midY + 2][2].type = TileType.WATER_TOWER
    }

    private fun generateTwinRivers(grid: Array<Array<CityTile>>, width: Int, height: Int) {
        for (y in 0 until height) {
            val r1 = (width * 0.3 + sin(y * 0.35) * 2.0).toInt().coerceIn(1, width - 2)
            val r2 = (width * 0.7 + sin(y * 0.35 + 1.5) * 2.0).toInt().coerceIn(1, width - 2)
            grid[y][r1].type = TileType.WATER
            grid[y][r2].type = TileType.WATER
            if (Random.nextFloat() < 0.3f && r1 + 1 < width) grid[y][r1 + 1].type = TileType.SAND
            if (Random.nextFloat() < 0.3f && r2 - 1 >= 0) grid[y][r2 - 1].type = TileType.SAND
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (grid[y][x].type == TileType.GRASS && Random.nextFloat() < 0.15f) {
                    grid[y][x].type = TileType.FOREST
                }
            }
        }
        val midY = height / 2
        val midX = width / 2
        grid[midY][midX].type = TileType.ROAD_PAVED
        grid[midY - 1][midX].type = TileType.ROAD_PAVED
        grid[midY + 1][midX].type = TileType.ROAD_PAVED
        grid[midY][midX - 1].type = TileType.RESIDENTIAL_SMALL
        grid[midY][midX + 1].type = TileType.COMMERCIAL_SMALL
        grid[midY - 2][midX].type = TileType.WIND_TURBINE
    }

    private fun generateSandbox(grid: Array<Array<CityTile>>, width: Int, height: Int) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (Random.nextFloat() < 0.08f) {
                    grid[y][x].type = TileType.FOREST
                }
            }
        }
    }
}
