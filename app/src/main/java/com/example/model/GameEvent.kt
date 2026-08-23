package com.example.model

enum class EventType {
    INFO,
    MILESTONE,
    WARNING,
    CELEBRATION,
    ALERT
}

data class GameEvent(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val type: EventType = EventType.INFO,
    val iconEmoji: String = "📢",
    val timestamp: Long = System.currentTimeMillis()
)
