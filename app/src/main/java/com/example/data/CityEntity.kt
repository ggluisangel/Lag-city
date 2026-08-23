package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val funds: Long,
    val population: Int,
    val happiness: Int,
    val timeOfDay: Float,
    val mapWidth: Int,
    val mapHeight: Int,
    val tilesData: String, // Serialized JSON or compact tile string
    val activePolicies: String, // Comma-separated active policy IDs
    val gameSpeed: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
