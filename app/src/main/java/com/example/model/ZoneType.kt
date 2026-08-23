package com.example.model

enum class ZoneType(val title: String, val colorHex: Long) {
    NONE("Unzoned", 0x00000000),
    RESIDENTIAL_LOW("Low Density Residential", 0x4481C784),
    RESIDENTIAL_HIGH("High Density Residential", 0x44388E3C),
    COMMERCIAL_LOW("Low Density Commercial", 0x4464B5F6),
    COMMERCIAL_HIGH("High Density Commercial", 0x441976D2),
    INDUSTRIAL_LOW("Light Industrial", 0x44FFF176),
    INDUSTRIAL_HIGH("Heavy Industrial", 0x44FBC02D)
}
