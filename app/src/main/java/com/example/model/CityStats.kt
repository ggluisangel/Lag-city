package com.example.model

enum class CityLevel(
    val title: String,
    val requiredPopulation: Int,
    val rewardGrant: Int,
    val unlockedDescription: String
) {
    SETTLEMENT("Wilderness Hamlet", 0, 1000, "Basic Roads, Cottages, Shops & Wind Energy"),
    VILLAGE("Thriving Village", 25, 2500, "Townhouses, Supermarkets, Workshops, Water Towers"),
    TOWN("Prosperous Town", 75, 5000, "Police, Fire Department, Rail Transit, Parks"),
    CITY("Booming City", 200, 12000, "Highrise Towers, Corporate Plazas, Solar Farms, Hospital"),
    METROPOLIS("Grand Metropolis", 500, 25000, "Sky Penthouses, Tech HQ, Nuclear Plant, Stadium, University"),
    MEGACITY("Global Megacity", 1200, 50000, "Airport Runways, Marina Seaports, Super Infrastructure")
}

data class CityStats(
    val cityName: String = "Emerald City",
    val funds: Long = 10000L,
    val population: Int = 0,
    val maxPopulationCapacity: Int = 0,
    val totalJobsAvailable: Int = 0,
    val residentialTaxRate: Float = 0.09f, // 9% default
    val commercialTaxRate: Float = 0.10f, // 10% default
    val industrialTaxRate: Float = 0.08f, // 8% default
    val hourlyTaxIncome: Long = 0L,
    val hourlyUpkeepCost: Long = 0L,
    val netHourlyIncome: Long = 0L,
    val happiness: Int = 85, // 0 to 100
    val powerGenerated: Int = 0,
    val powerNeeded: Int = 0,
    val waterGenerated: Int = 0,
    val waterNeeded: Int = 0,
    val pollutionLevel: Int = 0,
    val trafficIndex: Int = 20, // 0 (freeflow) to 100 (gridlock)
    val level: CityLevel = CityLevel.SETTLEMENT,
    val totalBuildingsCount: Int = 0,
    val totalRoadLength: Int = 0
) {
    val isPowerDeficit: Boolean
        get() = powerNeeded > powerGenerated

    val isWaterDeficit: Boolean
        get() = waterNeeded > waterGenerated

    val powerSurplusRatio: Float
        get() = if (powerNeeded == 0) 1.0f else (powerGenerated.toFloat() / powerNeeded.toFloat()).coerceIn(0f, 2f)

    val waterSurplusRatio: Float
        get() = if (waterNeeded == 0) 1.0f else (waterGenerated.toFloat() / waterNeeded.toFloat()).coerceIn(0f, 2f)
}
