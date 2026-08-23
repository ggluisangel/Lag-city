package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CityDao
import com.example.data.CityEntity
import com.example.data.IsoCityDatabase
import com.example.engine.MapGenerator
import com.example.engine.MapPreset
import com.example.engine.RoadNetwork
import com.example.engine.SimulationEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ToolMode {
    INSPECT,
    BUILD,
    BULLDOZE
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = IsoCityDatabase.getDatabase(application)
    private val cityDao: CityDao = db.cityDao()
    private val simulation = SimulationEngine()

    private var mapWidth = 20
    private var mapHeight = 20

    private val _grid = MutableStateFlow<Array<Array<CityTile>>>(emptyArray())
    val grid: StateFlow<Array<Array<CityTile>>> = _grid.asStateFlow()

    private val _stats = MutableStateFlow(CityStats())
    val stats: StateFlow<CityStats> = _stats.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _citizens = MutableStateFlow<List<Citizen>>(emptyList())
    val citizens: StateFlow<List<Citizen>> = _citizens.asStateFlow()

    private val _selectedCategory = MutableStateFlow(BuildingCategory.TRANSIT)
    val selectedCategory: StateFlow<BuildingCategory> = _selectedCategory.asStateFlow()

    private val _selectedTileType = MutableStateFlow<TileType?>(TileType.ROAD_PAVED)
    val selectedTileType: StateFlow<TileType?> = _selectedTileType.asStateFlow()

    private val _toolMode = MutableStateFlow(ToolMode.BUILD)
    val toolMode: StateFlow<ToolMode> = _toolMode.asStateFlow()

    private val _inspectedTile = MutableStateFlow<CityTile?>(null)
    val inspectedTile: StateFlow<CityTile?> = _inspectedTile.asStateFlow()

    private val _inspectedVehicle = MutableStateFlow<Vehicle?>(null)
    val inspectedVehicle: StateFlow<Vehicle?> = _inspectedVehicle.asStateFlow()

    private val _timeOfDay = MutableStateFlow(14.0f)
    val timeOfDay: StateFlow<Float> = _timeOfDay.asStateFlow()

    private val _gameSpeed = MutableStateFlow(1.0f)
    val gameSpeed: StateFlow<Float> = _gameSpeed.asStateFlow()

    private val _activePolicies = MutableStateFlow<Set<CityPolicy>>(emptySet())
    val activePolicies: StateFlow<Set<CityPolicy>> = _activePolicies.asStateFlow()

    private val _gameEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val gameEvents: StateFlow<List<GameEvent>> = _gameEvents.asStateFlow()

    private val _milestoneEvent = MutableStateFlow<GameEvent?>(null)
    val milestoneEvent: StateFlow<GameEvent?> = _milestoneEvent.asStateFlow()

    private val _savedCities = MutableStateFlow<List<CityEntity>>(emptyList())
    val savedCities: StateFlow<List<CityEntity>> = _savedCities.asStateFlow()

    private val _currentCityId = MutableStateFlow<Long?>(null)
    val currentCityId: StateFlow<Long?> = _currentCityId.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Initialize with default preset
        initMap(MapPreset.EMERALD_VALLEY)
        startSimulationLoop()
        observeSavedCities()
    }

    private fun observeSavedCities() {
        viewModelScope.launch {
            cityDao.getAllCities().collect { list ->
                _savedCities.value = list
            }
        }
    }

    fun initMap(preset: MapPreset, customName: String = preset.title) {
        mapWidth = preset.width
        mapHeight = preset.height
        val newGrid = MapGenerator.generateMap(preset)
        _grid.value = newGrid
        _stats.value = CityStats(cityName = customName, funds = 15000L)
        simulation.vehicles.clear()
        simulation.citizens.clear()
        _vehicles.value = emptyList()
        _citizens.value = emptyList()
        _inspectedTile.value = null
        _inspectedVehicle.value = null

        addEvent(
            GameEvent(
                title = "Welcome to $customName!",
                description = "Build roads, houses, shops, and power grids to grow your metropolis!",
                type = EventType.INFO,
                iconEmoji = "🏙️"
            )
        )
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                val currentGrid = _grid.value
                val speed = _gameSpeed.value
                if (currentGrid.isNotEmpty() && speed > 0f) {
                    val (updatedStats, milestone) = simulation.updateTick(
                        grid = currentGrid,
                        width = mapWidth,
                        height = mapHeight,
                        stats = _stats.value,
                        activePolicies = _activePolicies.value,
                        speedMultiplier = speed
                    )
                    _stats.value = updatedStats
                    _timeOfDay.value = simulation.timeOfDay
                    _vehicles.value = simulation.vehicles.toList()
                    _citizens.value = simulation.citizens.toList()

                    if (milestone != null) {
                        _milestoneEvent.value = milestone
                        addEvent(milestone)
                    }
                }
                delay(100L) // 10 ticks per second
            }
        }
    }

    fun setToolMode(mode: ToolMode) {
        _toolMode.value = mode
        if (mode == ToolMode.BULLDOZE) {
            _selectedTileType.value = null
        } else if (mode == ToolMode.INSPECT) {
            _selectedTileType.value = null
        } else if (mode == ToolMode.BUILD && _selectedTileType.value == null) {
            _selectedTileType.value = TileType.ROAD_PAVED
        }
    }

    fun selectCategory(category: BuildingCategory) {
        _selectedCategory.value = category
        if (category == BuildingCategory.DEMOLISH) {
            setToolMode(ToolMode.BULLDOZE)
        } else {
            setToolMode(ToolMode.BUILD)
            // Pick first buildable tile type in this category
            val firstInCat = TileType.values().firstOrNull { it.category == category && it.isBuildable }
            _selectedTileType.value = firstInCat
        }
    }

    fun selectTileType(type: TileType?) {
        _selectedTileType.value = type
        if (type != null) {
            _toolMode.value = ToolMode.BUILD
            _selectedCategory.value = type.category
        }
    }

    fun onTileTapped(x: Int, y: Int) {
        if (x !in 0 until mapWidth || y !in 0 until mapHeight) return
        val currentGrid = _grid.value
        val tile = currentGrid[y][x]

        when (_toolMode.value) {
            ToolMode.INSPECT -> {
                // Check if any vehicle is on this tile
                val vehicleOnTile = _vehicles.value.firstOrNull {
                    kotlin.math.abs(it.x - (x + 0.5f)) < 0.6f && kotlin.math.abs(it.y - (y + 0.5f)) < 0.6f
                }
                if (vehicleOnTile != null) {
                    _inspectedVehicle.value = vehicleOnTile
                    _inspectedTile.value = null
                } else {
                    _inspectedTile.value = tile
                    _inspectedVehicle.value = null
                }
            }
            ToolMode.BULLDOZE -> {
                demolishTile(x, y)
            }
            ToolMode.BUILD -> {
                val toBuild = _selectedTileType.value ?: return
                placeTile(x, y, toBuild)
            }
        }
    }

    fun placeTile(x: Int, y: Int, type: TileType) {
        val currentGrid = _grid.value
        val currentStats = _stats.value
        val existingTile = currentGrid[y][x]

        if (existingTile.type == type) return
        if (currentStats.funds < type.cost) {
            addEvent(
                GameEvent(
                    title = "Insufficient Funds!",
                    description = "Need $${type.cost} to construct ${type.displayName}.",
                    type = EventType.WARNING,
                    iconEmoji = "💸"
                )
            )
            return
        }

        // Special water rules
        if (existingTile.type.isWaterTile && !type.isWaterTile) {
            addEvent(
                GameEvent(
                    title = "Cannot Build on Water!",
                    description = "Use Bridges or Marina for water tiles.",
                    type = EventType.WARNING,
                    iconEmoji = "🌊"
                )
            )
            return
        }

        // Apply construction
        val newFunds = currentStats.funds - type.cost
        existingTile.type = type
        existingTile.level = 1
        existingTile.occupants = 0
        existingTile.buildTimestamp = System.currentTimeMillis()

        RoadNetwork.updateConnections(currentGrid, mapWidth, mapHeight)
        _stats.value = currentStats.copy(funds = newFunds)
        _grid.value = currentGrid.map { it.clone() }.toTypedArray()

        _inspectedTile.value = existingTile
    }

    fun demolishTile(x: Int, y: Int) {
        val currentGrid = _grid.value
        val currentStats = _stats.value
        val tile = currentGrid[y][x]

        if (tile.type == TileType.GRASS) return

        val refund = (tile.type.cost * 0.4f).toLong()
        tile.type = if (tile.type.isWaterTile && tile.type != TileType.WATER) TileType.WATER else TileType.GRASS
        tile.level = 1
        tile.occupants = 0

        RoadNetwork.updateConnections(currentGrid, mapWidth, mapHeight)
        _stats.value = currentStats.copy(funds = currentStats.funds + refund)
        _grid.value = currentGrid.map { it.clone() }.toTypedArray()
        _inspectedTile.value = null
    }

    fun upgradeTile(tile: CityTile) {
        if (tile.level >= tile.type.maxLevel) return
        val upgradeCost = (tile.type.cost * 0.8f * (tile.level + 1)).toLong()
        val currentStats = _stats.value
        if (currentStats.funds < upgradeCost) {
            addEvent(
                GameEvent(
                    title = "Insufficient Funds for Upgrade",
                    description = "Need $${upgradeCost} to upgrade ${tile.type.displayName} to Level ${tile.level + 1}.",
                    type = EventType.WARNING,
                    iconEmoji = "💸"
                )
            )
            return
        }

        val currentGrid = _grid.value
        tile.level += 1
        tile.buildTimestamp = System.currentTimeMillis()
        _stats.value = currentStats.copy(funds = currentStats.funds - upgradeCost)
        _grid.value = currentGrid.map { it.clone() }.toTypedArray()
        _inspectedTile.value = tile

        addEvent(
            GameEvent(
                title = "${tile.type.displayName} Upgraded!",
                description = "Reached Level ${tile.level}! Boosted capacity and productivity.",
                type = EventType.INFO,
                iconEmoji = "⬆️"
            )
        )
    }

    fun setGameSpeed(speed: Float) {
        _gameSpeed.value = speed
    }

    fun toggleDayNightCycle() {
        simulation.isDayNightActive = !simulation.isDayNightActive
        if (!simulation.isDayNightActive) {
            simulation.timeOfDay = 12.0f // Lock to noon
            _timeOfDay.value = 12.0f
        }
    }

    fun togglePolicy(policy: CityPolicy) {
        val current = _activePolicies.value.toMutableSet()
        if (current.contains(policy)) {
            current.remove(policy)
            addEvent(GameEvent(title = "Policy Repealed", description = policy.title, type = EventType.INFO, iconEmoji = "📜"))
        } else {
            current.add(policy)
            addEvent(GameEvent(title = "Policy Enacted", description = "${policy.title} is now active across your city.", type = EventType.CELEBRATION, iconEmoji = "📜"))
        }
        _activePolicies.value = current
    }

    fun setTaxRates(res: Float, comm: Float, ind: Float) {
        _stats.value = _stats.value.copy(
            residentialTaxRate = res.coerceIn(0.01f, 0.25f),
            commercialTaxRate = comm.coerceIn(0.01f, 0.25f),
            industrialTaxRate = ind.coerceIn(0.01f, 0.25f)
        )
    }

    fun triggerCelebrationFestival() {
        simulation.isFestivalActive = true
        addEvent(
            GameEvent(
                title = "🎉 Grand City Festival Launched!",
                description = "Citizens are celebrating with fireworks, parades and joyful music across the city!",
                type = EventType.CELEBRATION,
                iconEmoji = "🎆"
            )
        )
        viewModelScope.launch {
            delay(15000L)
            simulation.isFestivalActive = false
        }
    }

    fun addEvent(event: GameEvent) {
        val current = _gameEvents.value.toMutableList()
        current.add(0, event)
        if (current.size > 20) current.removeAt(current.lastIndex)
        _gameEvents.value = current
    }

    fun dismissMilestone() {
        _milestoneEvent.value = null
    }

    fun dismissInspector() {
        _inspectedTile.value = null
        _inspectedVehicle.value = null
    }

    fun saveCurrentCity() {
        val currentGrid = _grid.value
        val currentStats = _stats.value
        if (currentGrid.isEmpty()) return

        // Serialize tiles compactly: x,y,type,level;...
        val sb = StringBuilder()
        for (row in currentGrid) {
            for (tile in row) {
                sb.append("${tile.x},${tile.y},${tile.type.name},${tile.level};")
            }
        }

        val policyStr = _activePolicies.value.joinToString(",") { it.name }
        val entity = CityEntity(
            id = _currentCityId.value ?: 0L,
            name = currentStats.cityName,
            funds = currentStats.funds,
            population = currentStats.population,
            happiness = currentStats.happiness,
            timeOfDay = _timeOfDay.value,
            mapWidth = mapWidth,
            mapHeight = mapHeight,
            tilesData = sb.toString(),
            activePolicies = policyStr,
            gameSpeed = _gameSpeed.value,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val id = cityDao.insertCity(entity)
            _currentCityId.value = id
            addEvent(GameEvent(title = "City Saved!", description = "${currentStats.cityName} saved to slot #${id}", type = EventType.INFO, iconEmoji = "💾"))
        }
    }

    fun loadCity(city: CityEntity) {
        try {
            mapWidth = city.mapWidth
            mapHeight = city.mapHeight
            val newGrid = Array(mapHeight) { y ->
                Array(mapWidth) { x ->
                    CityTile(x = x, y = y, type = TileType.GRASS)
                }
            }

            val tileEntries = city.tilesData.split(";")
            for (entry in tileEntries) {
                if (entry.isBlank()) continue
                val parts = entry.split(",")
                if (parts.size >= 4) {
                    val x = parts[0].toIntOrNull() ?: continue
                    val y = parts[1].toIntOrNull() ?: continue
                    val typeName = parts[2]
                    val level = parts[3].toIntOrNull() ?: 1
                    if (x in 0 until mapWidth && y in 0 until mapHeight) {
                        val tileType = try { TileType.valueOf(typeName) } catch (e: Exception) { TileType.GRASS }
                        newGrid[y][x] = CityTile(x = x, y = y, type = tileType, level = level)
                    }
                }
            }

            RoadNetwork.updateConnections(newGrid, mapWidth, mapHeight)
            _grid.value = newGrid
            _stats.value = CityStats(
                cityName = city.name,
                funds = city.funds,
                population = city.population,
                happiness = city.happiness
            )
            _timeOfDay.value = city.timeOfDay
            _currentCityId.value = city.id

            val loadedPolicies = mutableSetOf<CityPolicy>()
            if (city.activePolicies.isNotBlank()) {
                for (pName in city.activePolicies.split(",")) {
                    try { loadedPolicies.add(CityPolicy.valueOf(pName)) } catch (e: Exception) {}
                }
            }
            _activePolicies.value = loadedPolicies

            addEvent(GameEvent(title = "City Loaded", description = "Resumed ${city.name} successfully.", type = EventType.INFO, iconEmoji = "📂"))
        } catch (e: Exception) {
            addEvent(GameEvent(title = "Error Loading City", description = e.localizedMessage ?: "Unknown error", type = EventType.WARNING, iconEmoji = "⚠️"))
        }
    }

    fun deleteSavedCity(city: CityEntity) {
        viewModelScope.launch {
            cityDao.deleteCity(city)
        }
    }
}
