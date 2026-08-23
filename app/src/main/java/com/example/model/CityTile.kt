package com.example.model

data class CityTile(
    val x: Int,
    val y: Int,
    var type: TileType = TileType.GRASS,
    var zone: ZoneType = ZoneType.NONE,
    var level: Int = 1,
    var customName: String? = null,
    var occupants: Int = 0,
    var variation: Int = 0, // Visual model variation index
    var roadMask: Int = 0,  // Bitmask for roads & rails: N=1, E=2, S=4, W=8
    var hasPower: Boolean = true,
    var hasWater: Boolean = true,
    var elevation: Float = 0f,
    var buildTimestamp: Long = 0L // 0L means already completed/built
) {
    fun getConstructionDurationMs(): Long {
        return when (type.category) {
            BuildingCategory.TRANSIT -> if (type.isRail || type == TileType.ROAD_BRIDGE || type == TileType.RAIL_BRIDGE) 3000L else 1500L
            BuildingCategory.PARKS -> if (type == TileType.STADIUM) 8000L else if (type == TileType.PLAZA_FOUNTAIN) 4000L else 2500L
            BuildingCategory.RESIDENTIAL -> when (type) {
                TileType.RESIDENTIAL_SMALL -> 3500L
                TileType.RESIDENTIAL_MEDIUM -> 5500L
                TileType.RESIDENTIAL_HIGH -> 8000L
                TileType.RESIDENTIAL_LUXURY -> 10000L
                else -> 3500L
            }
            BuildingCategory.COMMERCIAL -> when (type) {
                TileType.COMMERCIAL_SMALL -> 3500L
                TileType.COMMERCIAL_MEDIUM -> 6000L
                TileType.COMMERCIAL_HIGH -> 8500L
                TileType.COMMERCIAL_TECH_HQ -> 10000L
                else -> 3500L
            }
            BuildingCategory.INDUSTRIAL -> when (type) {
                TileType.INDUSTRIAL_SMALL -> 3500L
                TileType.INDUSTRIAL_MEDIUM -> 6000L
                TileType.INDUSTRIAL_TECH -> 8000L
                TileType.INDUSTRIAL_LOGISTICS -> 7000L
                else -> 3500L
            }
            BuildingCategory.UTILITIES -> when (type) {
                TileType.POWER_PLANT -> 11000L
                TileType.WATER_TREATMENT -> 7000L
                TileType.SOLAR_PARK -> 5000L
                TileType.WIND_TURBINE -> 4000L
                TileType.WATER_TOWER -> 4000L
                else -> 5000L
            }
            BuildingCategory.CIVIC -> when (type) {
                TileType.TOWN_HALL -> 8000L
                TileType.HOSPITAL -> 9000L
                TileType.UNIVERSITY -> 10000L
                TileType.POLICE_STATION, TileType.FIRE_STATION -> 6000L
                else -> 6000L
            }
            else -> 0L
        }
    }

    fun getConstructionProgress(currentTime: Long = System.currentTimeMillis()): Float {
        if (buildTimestamp == 0L) return 1.0f
        val duration = getConstructionDurationMs()
        if (duration <= 0L) return 1.0f
        val elapsed = currentTime - buildTimestamp
        return (elapsed.toFloat() / duration).coerceIn(0f, 1f)
    }

    val isUnderConstruction: Boolean
        get() = getConstructionProgress() < 1.0f

    val currentMaxPop: Int
        get() = type.maxPopCapacity * level

    val currentJobs: Int
        get() = type.jobsCapacity * level

    val currentPowerNeeded: Int
        get() = type.powerNeeded * level

    val currentWaterNeeded: Int
        get() = type.waterNeeded * level

    val currentPowerGenerated: Int
        get() = type.powerGenerated * level

    val currentWaterGenerated: Int
        get() = type.waterGenerated * level

    val currentHappinessBoost: Int
        get() = type.happinessBoost * level

    val currentUpkeep: Int
        get() = type.upkeep * level
}
