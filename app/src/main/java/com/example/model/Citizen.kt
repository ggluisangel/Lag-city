package com.example.model

enum class CitizenActivity(val label: String, val emoji: String) {
    WALKING_HOME("Heading Home", "🚶‍♂️"),
    COMMUTING_WORK("Going to Work", "💼"),
    SHOPPING("Browsing Shops", "🛍️"),
    RELAXING_PARK("Relaxing in Park", "🌳"),
    VISITING_PLAZA("Enjoying the Plaza", "⛲"),
    DINING("Grabbing Coffee", "☕")
}

data class Citizen(
    val id: Long,
    val name: String,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    val homeX: Int,
    val homeY: Int,
    val workX: Int,
    val workY: Int,
    var activity: CitizenActivity = CitizenActivity.WALKING_HOME,
    var happiness: Int = 85,
    val shirtColor: Long = 0xFF1E88E5,
    var waypoints: List<Pair<Float, Float>> = emptyList(),
    var waypointIdx: Int = 0
)
