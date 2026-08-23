package com.example.engine

import com.example.model.*
import kotlin.math.*
import kotlin.random.Random

class SimulationEngine {
    var timeOfDay: Float = 14.0f // 24.0 hours (starts at 2 PM)
    var isDayNightActive: Boolean = true
    var weatherState: String = "Clear"
    var isFestivalActive: Boolean = false

    private var nextVehicleId: Long = 100L
    private var nextCitizenId: Long = 500L

    val vehicles = mutableListOf<Vehicle>()
    val citizens = mutableListOf<Citizen>()
    val notifications = mutableListOf<GameEvent>()

    private var milestoneReached: CityLevel = CityLevel.SETTLEMENT
    private val citizenFirstNames = listOf("Alex", "Sophia", "Lucas", "Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Mia", "Leo", "Isabella", "Mateo", "Camila", "Carlos", "Elena", "Marcus", "Maya")
    private val citizenLastNames = listOf("Miller", "Garcia", "Kim", "Smith", "Chen", "Johnson", "Rodriguez", "Patel", "Silva", "Williams", "Mendoza", "Taylor", "Anderson", "Torres")

    fun updateTick(
        grid: Array<Array<CityTile>>,
        width: Int,
        height: Int,
        stats: CityStats,
        activePolicies: Set<CityPolicy>,
        speedMultiplier: Float
    ): Pair<CityStats, GameEvent?> {
        if (speedMultiplier <= 0f) return Pair(stats, null)

        // 1. Advance Time of Day (1 real second ~= 0.2 game hours at 1x speed)
        if (isDayNightActive) {
            timeOfDay = (timeOfDay + 0.05f * speedMultiplier) % 24f
        }

        // 2. Resource Accounting (Power, Water, Capacity, Jobs, Pollution)
        var totalPowerGen = 0
        var totalPowerNeed = 0
        var totalWaterGen = 0
        var totalWaterNeed = 0
        var totalMaxPop = 0
        var totalJobs = 0
        var totalUpkeep = 0L
        var totalHappinessSum = 0
        var totalBuildings = 0
        var totalRoads = 0
        var totalPollution = 0

        val residentialTiles = mutableListOf<CityTile>()
        val commercialTiles = mutableListOf<CityTile>()
        val industrialTiles = mutableListOf<CityTile>()
        val roadTiles = mutableListOf<CityTile>()
        val railTiles = mutableListOf<CityTile>()
        val waterTiles = mutableListOf<CityTile>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val tile = grid[y][x]
                if (tile.type.isRoad) {
                    totalRoads++
                    totalUpkeep += tile.currentUpkeep
                    roadTiles.add(tile)
                } else if (tile.type.isRail) {
                    totalUpkeep += tile.currentUpkeep
                    railTiles.add(tile)
                } else if (tile.type.isWaterTile) {
                    waterTiles.add(tile)
                }

                if (tile.type != TileType.GRASS && tile.type != TileType.WATER && tile.type != TileType.SAND && tile.type != TileType.FOREST && tile.type != TileType.MOUNTAIN) {
                    totalBuildings++
                    totalPowerGen += tile.currentPowerGenerated
                    totalPowerNeed += tile.currentPowerNeeded
                    totalWaterGen += tile.currentWaterGenerated
                    totalWaterNeed += tile.currentWaterNeeded
                    totalMaxPop += tile.currentMaxPop
                    totalJobs += tile.currentJobs
                    totalUpkeep += tile.currentUpkeep
                    totalHappinessSum += tile.currentHappinessBoost
                    totalPollution += tile.type.pollution

                    when (tile.type.category) {
                        BuildingCategory.RESIDENTIAL -> residentialTiles.add(tile)
                        BuildingCategory.COMMERCIAL -> commercialTiles.add(tile)
                        BuildingCategory.INDUSTRIAL -> industrialTiles.add(tile)
                        else -> {}
                    }
                }
            }
        }

        val hasPowerGrid = totalPowerGen >= totalPowerNeed
        val hasWaterGrid = totalWaterGen >= totalWaterNeed

        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x].hasPower = hasPowerGrid
                grid[y][x].hasWater = hasWaterGrid
            }
        }

        // 3. Population Simulation
        val utilityFactor = (if (hasPowerGrid) 1.0f else 0.4f) * (if (hasWaterGrid) 1.0f else 0.4f)
        var targetPopulation = (totalMaxPop * utilityFactor * (stats.happiness / 100f)).roundToInt()
        targetPopulation = minOf(targetPopulation, totalMaxPop)

        // Smooth population adjustment
        val popDelta = (targetPopulation - stats.population)
        val newPopulation = (stats.population + (popDelta * 0.15f * speedMultiplier).toInt()).coerceIn(0, totalMaxPop)

        // Distribute occupants across residential tiles
        var remainingPop = newPopulation
        for (res in residentialTiles) {
            val share = minOf(res.currentMaxPop, remainingPop)
            res.occupants = share
            remainingPop -= share
        }

        // 4. Economics & Tax Revenue
        var economicMultiplier = 1.0f
        var policyHappinessMod = 0
        var policyHourlyExpense = 0f

        for (policy in activePolicies) {
            economicMultiplier *= policy.economicModifier
            policyHappinessMod += policy.happinessModifier
            policyHourlyExpense += policy.hourlyCostPerCitizen * newPopulation
        }

        if (activePolicies.contains(CityPolicy.GREEN_ENERGY)) {
            totalPollution = (totalPollution * 0.6f).toInt()
        }

        val residentialTax = (newPopulation * 8L * (stats.residentialTaxRate / 0.10f)).toLong()
        val commercialTax = (minOf(newPopulation, totalJobs) * 12L * (stats.commercialTaxRate / 0.10f) * economicMultiplier).toLong()
        val industrialTax = (industrialTiles.size * 25L * (stats.industrialTaxRate / 0.10f) * economicMultiplier).toLong()

        val totalHourlyTaxIncome = residentialTax + commercialTax + industrialTax
        val totalHourlyExpenses = totalUpkeep + policyHourlyExpense.toLong()
        val netCashFlow = totalHourlyTaxIncome - totalHourlyExpenses

        // Incremental funds per tick
        val tickFundsDelta = (netCashFlow / 30f * speedMultiplier).toLong()
        val newFunds = (stats.funds + tickFundsDelta).coerceAtLeast(0L)

        // 5. Happiness Calculation
        var baseHappiness = 80
        if (!hasPowerGrid && totalPowerNeed > 0) baseHappiness -= 25
        if (!hasWaterGrid && totalWaterNeed > 0) baseHappiness -= 25
        if (totalPollution > 15) baseHappiness -= minOf(15, totalPollution / 3)

        // Tax impact on happiness
        val avgTax = (stats.residentialTaxRate + stats.commercialTaxRate + stats.industrialTaxRate) / 3f
        if (avgTax > 0.15f) baseHappiness -= ((avgTax - 0.15f) * 120).toInt()
        else if (avgTax < 0.08f) baseHappiness += ((0.08f - avgTax) * 80).toInt()

        baseHappiness += totalHappinessSum.coerceAtMost(30)
        baseHappiness += policyHappinessMod
        val newHappiness = baseHappiness.coerceIn(10, 100)

        // 6. Traffic Index
        val trafficScore = if (totalRoads > 0) {
            val baseTraffic = (newPopulation.toFloat() / (totalRoads * 4f) * 100f).toInt()
            if (activePolicies.contains(CityPolicy.FREE_TRANSIT)) (baseTraffic * 0.65f).toInt() else baseTraffic
        } else 10
        val newTrafficIndex = trafficScore.coerceIn(5, 95)

        // 7. Check Milestones
        var newLevel = stats.level
        var milestoneEvent: GameEvent? = null
        for (lvl in CityLevel.values().reversed()) {
            if (newPopulation >= lvl.requiredPopulation && lvl.ordinal > milestoneReached.ordinal) {
                milestoneReached = lvl
                newLevel = lvl
                milestoneEvent = GameEvent(
                    title = "🎉 Milestone: ${lvl.title}!",
                    description = "Reached ${lvl.requiredPopulation}+ citizens! Treasury rewarded with +$${lvl.rewardGrant}! ${lvl.unlockedDescription}",
                    type = EventType.MILESTONE,
                    iconEmoji = "🏆"
                )
                break
            }
        }

        val updatedStats = stats.copy(
            population = newPopulation,
            maxPopulationCapacity = totalMaxPop,
            totalJobsAvailable = totalJobs,
            hourlyTaxIncome = totalHourlyTaxIncome,
            hourlyUpkeepCost = totalHourlyExpenses,
            netHourlyIncome = netCashFlow,
            funds = if (milestoneEvent != null) newFunds + newLevel.rewardGrant else newFunds,
            happiness = newHappiness,
            powerGenerated = totalPowerGen,
            powerNeeded = totalPowerNeed,
            waterGenerated = totalWaterGen,
            waterNeeded = totalWaterNeed,
            pollutionLevel = totalPollution,
            trafficIndex = newTrafficIndex,
            level = newLevel,
            totalBuildingsCount = totalBuildings,
            totalRoadLength = totalRoads
        )

        // 8. Vehicle Simulation & Autonomous Spawning
        simulateVehicles(grid, width, height, roadTiles, railTiles, waterTiles, newPopulation, speedMultiplier)

        // 9. Pedestrian Simulation
        simulateCitizens(residentialTiles, commercialTiles, roadTiles, newPopulation, speedMultiplier)

        return Pair(updatedStats, milestoneEvent)
    }

    private fun simulateVehicles(
        grid: Array<Array<CityTile>>,
        width: Int,
        height: Int,
        roadTiles: List<CityTile>,
        railTiles: List<CityTile>,
        waterTiles: List<CityTile>,
        population: Int,
        speedMultiplier: Float
    ) {
        // Target vehicle count based on population and roads
        val maxRoadVehicles = (roadTiles.size / 3).coerceIn(2, 28)
        val currentRoadVehicles = vehicles.count { it.type.category == VehicleCategory.ROAD }

        if (currentRoadVehicles < maxRoadVehicles && roadTiles.isNotEmpty() && Random.nextFloat() < 0.15f * speedMultiplier) {
            val spawnTile = roadTiles[Random.nextInt(roadTiles.size)]
            val path = RoadNetwork.findConnectedRoadPath(grid, spawnTile.x, spawnTile.y, maxSteps = 25)
            if (path.size > 2) {
                val types = listOf(
                    VehicleType.SEDAN, VehicleType.SEDAN, VehicleType.SPORTS_CAR,
                    VehicleType.BUS, VehicleType.DELIVERY_VAN, VehicleType.TRUCK,
                    VehicleType.POLICE_CRUISER, VehicleType.AMBULANCE
                )
                val type = types[Random.nextInt(types.size)]
                val color = type.defaultColors[Random.nextInt(type.defaultColors.size)]
                vehicles.add(
                    Vehicle(
                        id = nextVehicleId++,
                        type = type,
                        x = path[0].first,
                        y = path[0].second,
                        targetX = path[1].first,
                        targetY = path[1].second,
                        heading = atan2(path[1].second - path[0].second, path[1].first - path[0].first),
                        color = color,
                        waypoints = path,
                        currentWaypointIndex = 1,
                        passengerCount = if (type == VehicleType.BUS) Random.nextInt(5, 30) else Random.nextInt(1, 4)
                    )
                )
            }
        }

        // Train spawning
        val currentTrains = vehicles.count { it.type.category == VehicleCategory.RAIL }
        if (currentTrains < 3 && railTiles.size >= 4 && Random.nextFloat() < 0.08f * speedMultiplier) {
            val spawnTile = railTiles[Random.nextInt(railTiles.size)]
            val path = RoadNetwork.findConnectedRailPath(grid, spawnTile.x, spawnTile.y, maxSteps = 30)
            if (path.size > 3) {
                vehicles.add(
                    Vehicle(
                        id = nextVehicleId++,
                        type = VehicleType.TRAIN_COMMUTER,
                        x = path[0].first,
                        y = path[0].second,
                        targetX = path[1].first,
                        targetY = path[1].second,
                        heading = atan2(path[1].second - path[0].second, path[1].first - path[0].first),
                        color = 0xFF00ACC1,
                        waypoints = path,
                        currentWaypointIndex = 1,
                        passengerCount = Random.nextInt(20, 120)
                    )
                )
            }
        }

        // Boats / Ferries on water
        val currentBoats = vehicles.count { it.type.category == VehicleCategory.WATER }
        if (currentBoats < 4 && waterTiles.size >= 8 && Random.nextFloat() < 0.06f * speedMultiplier) {
            val spawnTile = waterTiles[Random.nextInt(waterTiles.size)]
            val path = RoadNetwork.findWaterPath(grid, spawnTile.x, spawnTile.y, maxSteps = 30)
            if (path.size > 2) {
                val boatType = if (Random.nextBoolean()) VehicleType.SPEEDBOAT else VehicleType.FERRY
                vehicles.add(
                    Vehicle(
                        id = nextVehicleId++,
                        type = boatType,
                        x = path[0].first,
                        y = path[0].second,
                        targetX = path[1].first,
                        targetY = path[1].second,
                        heading = atan2(path[1].second - path[0].second, path[1].first - path[0].first),
                        color = boatType.defaultColors[Random.nextInt(boatType.defaultColors.size)],
                        waypoints = path,
                        currentWaypointIndex = 1
                    )
                )
            }
        }

        // Airplanes / Helicopters
        val currentAir = vehicles.count { it.type.category == VehicleCategory.AIR }
        if (currentAir < 2 && population >= 50 && Random.nextFloat() < 0.03f * speedMultiplier) {
            val airType = if (Random.nextBoolean()) VehicleType.AIRPLANE_COMMUTER else VehicleType.HELICOPTER
            val startX = if (Random.nextBoolean()) -2f else (width + 2).toFloat()
            val startY = Random.nextFloat() * height
            val targetX = if (startX < 0) (width + 4).toFloat() else -4f
            val targetY = Random.nextFloat() * height
            vehicles.add(
                Vehicle(
                    id = nextVehicleId++,
                    type = airType,
                    x = startX,
                    y = startY,
                    targetX = targetX,
                    targetY = targetY,
                    altitude = 2.5f,
                    heading = atan2(targetY - startY, targetX - startX),
                    color = 0xFFFFFFFF,
                    waypoints = listOf(Pair(startX, startY), Pair(targetX, targetY)),
                    currentWaypointIndex = 1
                )
            )
        }

        // Move all vehicles
        val iterator = vehicles.iterator()
        while (iterator.hasNext()) {
            val v = iterator.next()
            val dx = v.targetX - v.x
            val dy = v.targetY - v.y
            val dist = sqrt(dx * dx + dy * dy)
            val moveStep = v.type.speed * speedMultiplier

            if (dist <= moveStep) {
                // Arrived at waypoint
                v.x = v.targetX
                v.y = v.targetY
                v.currentWaypointIndex++

                if (v.currentWaypointIndex < v.waypoints.size) {
                    val nextWp = v.waypoints[v.currentWaypointIndex]
                    v.targetX = nextWp.first
                    v.targetY = nextWp.second
                    val newHeading = atan2(v.targetY - v.y, v.targetX - v.x)
                    // Smooth heading transition
                    v.heading = newHeading
                } else {
                    // Reached end of path, remove vehicle
                    iterator.remove()
                }
            } else {
                v.x += (dx / dist) * moveStep
                v.y += (dy / dist) * moveStep
            }
        }
    }

    private fun simulateCitizens(
        resTiles: List<CityTile>,
        commTiles: List<CityTile>,
        roadTiles: List<CityTile>,
        population: Int,
        speedMultiplier: Float
    ) {
        val maxCitizens = (population / 5).coerceIn(2, 20)
        if (citizens.size < maxCitizens && resTiles.isNotEmpty() && roadTiles.isNotEmpty() && Random.nextFloat() < 0.2f * speedMultiplier) {
            val home = resTiles[Random.nextInt(resTiles.size)]
            val work = if (commTiles.isNotEmpty()) commTiles[Random.nextInt(commTiles.size)] else home
            val name = "${citizenFirstNames[Random.nextInt(citizenFirstNames.size)]} ${citizenLastNames[Random.nextInt(citizenLastNames.size)]}"
            val shirtColors = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFF00ACC1, 0xFFD81B60)

            citizens.add(
                Citizen(
                    id = nextCitizenId++,
                    name = name,
                    x = home.x + 0.5f,
                    y = home.y + 0.5f,
                    targetX = work.x + 0.5f,
                    targetY = work.y + 0.5f,
                    homeX = home.x,
                    homeY = home.y,
                    workX = work.x,
                    workY = work.y,
                    shirtColor = shirtColors[Random.nextInt(shirtColors.size)],
                    activity = if (timeOfDay in 8.0..18.0) CitizenActivity.COMMUTING_WORK else CitizenActivity.RELAXING_PARK
                )
            )
        }

        // Move citizens
        val citIterator = citizens.iterator()
        while (citIterator.hasNext()) {
            val c = citIterator.next()
            val dx = c.targetX - c.x
            val dy = c.targetY - c.y
            val dist = sqrt(dx * dx + dy * dy)
            val speed = 0.025f * speedMultiplier

            if (dist <= speed) {
                c.x = c.targetX
                c.y = c.targetY
                // Pick new activity & destination
                if (Random.nextFloat() < 0.05f) {
                    if (c.activity == CitizenActivity.COMMUTING_WORK) {
                        c.activity = CitizenActivity.SHOPPING
                        c.targetX = (c.homeX + Random.nextInt(-2, 3)).toFloat() + 0.5f
                        c.targetY = (c.homeY + Random.nextInt(-2, 3)).toFloat() + 0.5f
                    } else {
                        c.activity = CitizenActivity.WALKING_HOME
                        c.targetX = c.homeX + 0.5f
                        c.targetY = c.homeY + 0.5f
                    }
                }
            } else {
                c.x += (dx / dist) * speed
                c.y += (dy / dist) * speed
            }
        }
    }
}
