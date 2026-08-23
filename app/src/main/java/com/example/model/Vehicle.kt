package com.example.model

enum class VehicleCategory {
    ROAD,
    RAIL,
    AIR,
    WATER
}

enum class VehicleType(
    val category: VehicleCategory,
    val speed: Float,
    val length: Float,
    val width: Float,
    val defaultColors: List<Long>
) {
    SEDAN(VehicleCategory.ROAD, speed = 0.05f, length = 0.5f, width = 0.28f, listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFFFFFFFF)),
    SPORTS_CAR(VehicleCategory.ROAD, speed = 0.075f, length = 0.52f, width = 0.3f, listOf(0xFFFF1744, 0xFFFFD600, 0xFF00E676, 0xFF00E5FF)),
    BUS(VehicleCategory.ROAD, speed = 0.035f, length = 0.85f, width = 0.32f, listOf(0xFFFFB300, 0xFF0288D1, 0xFF388E3C)),
    DELIVERY_VAN(VehicleCategory.ROAD, speed = 0.045f, length = 0.65f, width = 0.3f, listOf(0xFFFFFFFF, 0xFF78909C, 0xFF5D4037)),
    TRUCK(VehicleCategory.ROAD, speed = 0.035f, length = 0.95f, width = 0.34f, listOf(0xFF00897B, 0xFFD81B60, 0xFF3949AB)),
    POLICE_CRUISER(VehicleCategory.ROAD, speed = 0.065f, length = 0.52f, width = 0.28f, listOf(0xFF0D47A1)),
    FIRE_ENGINE(VehicleCategory.ROAD, speed = 0.055f, length = 0.85f, width = 0.34f, listOf(0xFFD50000)),
    AMBULANCE(VehicleCategory.ROAD, speed = 0.06f, length = 0.7f, width = 0.32f, listOf(0xFFFAFAFA)),

    TRAIN_COMMUTER(VehicleCategory.RAIL, speed = 0.08f, length = 1.1f, width = 0.32f, listOf(0xFF00ACC1, 0xFFE53935)),
    
    AIRPLANE_COMMUTER(VehicleCategory.AIR, speed = 0.12f, length = 1.4f, width = 1.4f, listOf(0xFFFFFFFF, 0xFF1565C0)),
    HELICOPTER(VehicleCategory.AIR, speed = 0.09f, length = 0.8f, width = 0.8f, listOf(0xFFE53935, 0xFFFFB300, 0xFF283593)),

    SPEEDBOAT(VehicleCategory.WATER, speed = 0.05f, length = 0.6f, width = 0.28f, listOf(0xFFFFFFFF, 0xFFFF5722)),
    FERRY(VehicleCategory.WATER, speed = 0.03f, length = 1.0f, width = 0.45f, listOf(0xFF37474F, 0xFF1976D2)),
    CARGO_SHIP(VehicleCategory.WATER, speed = 0.02f, length = 1.5f, width = 0.55f, listOf(0xFF263238, 0xFFB71C1C))
}

data class Vehicle(
    val id: Long,
    val type: VehicleType,
    var x: Float,
    var y: Float,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var altitude: Float = 0f, // For air vehicles
    var heading: Float = 0f, // in radians (0 = South-East, etc.)
    val color: Long = 0xFF1E88E5,
    var waypoints: List<Pair<Float, Float>> = emptyList(),
    var currentWaypointIndex: Int = 0,
    var waitTimer: Float = 0f,
    var isStoppedAtTrafficLight: Boolean = false,
    var passengerCount: Int = 0,
    var destinationName: String = "Downtown"
)
