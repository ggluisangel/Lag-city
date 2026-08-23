package com.example.model

enum class TileType(
    val displayName: String,
    val category: BuildingCategory,
    val cost: Int,
    val upkeep: Int,
    val maxPopCapacity: Int = 0,
    val jobsCapacity: Int = 0,
    val powerGenerated: Int = 0,
    val powerNeeded: Int = 0,
    val waterGenerated: Int = 0,
    val waterNeeded: Int = 0,
    val happinessBoost: Int = 0,
    val pollution: Int = 0,
    val description: String = "",
    val maxLevel: Int = 1,
    val isWaterTile: Boolean = false,
    val isRoad: Boolean = false,
    val isRail: Boolean = false
) {
    // Terrain
    GRASS("Meadow", BuildingCategory.PARKS, cost = 10, upkeep = 0, description = "Natural green grass landscape"),
    WATER("Water", BuildingCategory.PARKS, cost = 50, upkeep = 0, isWaterTile = true, description = "Deep river or ocean water"),
    SAND("Sand Beach", BuildingCategory.PARKS, cost = 15, upkeep = 0, description = "Soft sand coast"),
    FOREST("Lush Forest", BuildingCategory.PARKS, cost = 25, upkeep = 0, happinessBoost = 2, description = "Dense cluster of pine and oak trees"),
    MOUNTAIN("Rocky Mountain", BuildingCategory.PARKS, cost = 0, upkeep = 0, description = "Elevated rocky terrain"),

    // Transit
    ROAD_PAVED("Paved Road", BuildingCategory.TRANSIT, cost = 20, upkeep = 1, isRoad = true, description = "Standard 2-lane asphalt road for cars & buses"),
    ROAD_HIGHWAY("Avenue Highway", BuildingCategory.TRANSIT, cost = 60, upkeep = 3, isRoad = true, description = "4-lane wide avenue with central streetlights"),
    ROAD_BRIDGE("Overwater Bridge", BuildingCategory.TRANSIT, cost = 120, upkeep = 5, isRoad = true, isWaterTile = true, description = "Sturdy suspension bridge over water"),
    RAILROAD("Rail Track", BuildingCategory.TRANSIT, cost = 40, upkeep = 2, isRail = true, description = "High-speed rail line for commuter trains"),
    RAIL_BRIDGE("Rail Bridge", BuildingCategory.TRANSIT, cost = 150, upkeep = 6, isRail = true, isWaterTile = true, description = "Steel truss train bridge over water"),
    AIRPORT_RUNWAY("Airport Runway", BuildingCategory.TRANSIT, cost = 1500, upkeep = 40, jobsCapacity = 30, powerNeeded = 15, happinessBoost = 8, description = "International terminal with landing planes"),
    HELIPAD("Helipad Station", BuildingCategory.TRANSIT, cost = 450, upkeep = 12, jobsCapacity = 10, powerNeeded = 5, happinessBoost = 4, description = "Rooftop helicopter transport pad"),
    MARINA("Harbor Marina", BuildingCategory.TRANSIT, cost = 800, upkeep = 25, jobsCapacity = 20, powerNeeded = 8, happinessBoost = 10, isWaterTile = true, description = "Seaport for boats and water transit"),

    // Residential
    RESIDENTIAL_SMALL("Cozy Cottage", BuildingCategory.RESIDENTIAL, cost = 100, upkeep = 2, maxPopCapacity = 6, powerNeeded = 1, waterNeeded = 1, maxLevel = 3, description = "Charming suburban home with red tile roof and garden"),
    RESIDENTIAL_MEDIUM("Townhouse Block", BuildingCategory.RESIDENTIAL, cost = 300, upkeep = 5, maxPopCapacity = 24, powerNeeded = 3, waterNeeded = 3, maxLevel = 3, description = "Multi-story brick apartment units with balconies"),
    RESIDENTIAL_HIGH("Modern Tower", BuildingCategory.RESIDENTIAL, cost = 900, upkeep = 15, maxPopCapacity = 80, powerNeeded = 8, waterNeeded = 8, maxLevel = 3, description = "High-density glass and steel residential skyscraper"),
    RESIDENTIAL_LUXURY("Sky Penthouse", BuildingCategory.RESIDENTIAL, cost = 2500, upkeep = 40, maxPopCapacity = 200, powerNeeded = 20, waterNeeded = 15, happinessBoost = 12, maxLevel = 3, description = "Luxury megatower with rooftop infinity garden"),

    // Commercial
    COMMERCIAL_SMALL("Corner Bistro & Shop", BuildingCategory.COMMERCIAL, cost = 150, upkeep = 3, jobsCapacity = 8, powerNeeded = 2, waterNeeded = 1, maxLevel = 3, description = "Local bakery, cafe, and convenience boutique"),
    COMMERCIAL_MEDIUM("Shopping Center", BuildingCategory.COMMERCIAL, cost = 500, upkeep = 8, jobsCapacity = 35, powerNeeded = 6, waterNeeded = 4, maxLevel = 3, description = "Multi-floor shopping complex and entertainment center"),
    COMMERCIAL_HIGH("Corporate Plaza", BuildingCategory.COMMERCIAL, cost = 1400, upkeep = 22, jobsCapacity = 120, powerNeeded = 14, waterNeeded = 10, maxLevel = 3, description = "Sleek glass high-rise office tower with neon antenna"),
    COMMERCIAL_TECH_HQ("Tech Innovation HQ", BuildingCategory.COMMERCIAL, cost = 3200, upkeep = 50, jobsCapacity = 300, powerNeeded = 25, waterNeeded = 15, happinessBoost = 15, maxLevel = 3, description = "Futuristic silicon campus with solar glass exterior"),

    // Industrial
    INDUSTRIAL_SMALL("Craft Workshop", BuildingCategory.INDUSTRIAL, cost = 120, upkeep = 2, jobsCapacity = 10, powerNeeded = 2, waterNeeded = 1, pollution = 2, maxLevel = 3, description = "Light manufacturing and carpentry workshop"),
    INDUSTRIAL_MEDIUM("Heavy Factory", BuildingCategory.INDUSTRIAL, cost = 450, upkeep = 7, jobsCapacity = 45, powerNeeded = 8, waterNeeded = 5, pollution = 8, maxLevel = 3, description = "Industrial manufacturing plant with smoking chimneys"),
    INDUSTRIAL_TECH("Robotics Lab", BuildingCategory.INDUSTRIAL, cost = 1200, upkeep = 18, jobsCapacity = 100, powerNeeded = 12, waterNeeded = 6, pollution = 1, happinessBoost = 5, maxLevel = 3, description = "Clean automated robotic production facility"),
    INDUSTRIAL_LOGISTICS("Freight Depot", BuildingCategory.INDUSTRIAL, cost = 800, upkeep = 14, jobsCapacity = 60, powerNeeded = 7, waterNeeded = 3, pollution = 4, maxLevel = 3, description = "Cargo warehouse distribution hub for freight trucks"),

    // Utilities
    WIND_TURBINE("Wind Turbine", BuildingCategory.UTILITIES, cost = 350, upkeep = 4, powerGenerated = 35, happinessBoost = 2, description = "Clean renewable aerodynamic wind energy"),
    SOLAR_PARK("Solar Array", BuildingCategory.UTILITIES, cost = 600, upkeep = 6, powerGenerated = 70, happinessBoost = 3, description = "High-efficiency photovoltaic solar panel grid"),
    POWER_PLANT("Nuclear Plant", BuildingCategory.UTILITIES, cost = 3000, upkeep = 45, powerGenerated = 450, pollution = 6, description = "High-output atomic energy reactor"),
    WATER_TOWER("Water Reservoir", BuildingCategory.UTILITIES, cost = 250, upkeep = 3, waterGenerated = 80, description = "Pressurized municipal clean water storage tower"),
    WATER_TREATMENT("Pumping Facility", BuildingCategory.UTILITIES, cost = 1100, upkeep = 15, waterGenerated = 350, powerNeeded = 10, description = "Advanced water purification filtration plant"),

    // Parks & Amenities
    PARK_SMALL("Community Park", BuildingCategory.PARKS, cost = 80, upkeep = 1, happinessBoost = 4, description = "Green lawn with park benches, blossom trees and flowers"),
    PLAZA_FOUNTAIN("Civic Fountain Plaza", BuildingCategory.PARKS, cost = 220, upkeep = 3, happinessBoost = 8, description = "Ornamental marble plaza with active water fountain"),
    STADIUM("Grand Sports Arena", BuildingCategory.PARKS, cost = 2400, upkeep = 35, jobsCapacity = 80, powerNeeded = 15, waterNeeded = 10, happinessBoost = 20, description = "Multipurpose sports arena for championship events"),

    // Civic Services
    TOWN_HALL("City Hall", BuildingCategory.CIVIC, cost = 1000, upkeep = 15, jobsCapacity = 25, powerNeeded = 6, waterNeeded = 4, happinessBoost = 12, description = "Administrative municipal center and civic headquarters"),
    POLICE_STATION("Police HQ", BuildingCategory.CIVIC, cost = 600, upkeep = 10, jobsCapacity = 18, powerNeeded = 5, waterNeeded = 3, happinessBoost = 10, description = "Patrol station ensuring safety and law enforcement"),
    FIRE_STATION("Fire Department", BuildingCategory.CIVIC, cost = 600, upkeep = 10, jobsCapacity = 18, powerNeeded = 5, waterNeeded = 5, happinessBoost = 10, description = "Rapid response station with fire engines"),
    HOSPITAL("Metro Medical Center", BuildingCategory.CIVIC, cost = 1600, upkeep = 25, jobsCapacity = 50, powerNeeded = 12, waterNeeded = 10, happinessBoost = 18, description = "Emergency hospital and advanced healthcare clinic"),
    UNIVERSITY("Polytechnic Campus", BuildingCategory.CIVIC, cost = 2800, upkeep = 40, jobsCapacity = 90, powerNeeded = 18, waterNeeded = 12, happinessBoost = 22, description = "Higher education research center boosting city productivity");

    val isBuildable: Boolean
        get() = cost > 0 && this != MOUNTAIN
}
