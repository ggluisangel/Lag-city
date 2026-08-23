package com.example.model

enum class BuildingCategory(
    val title: String,
    val iconName: String,
    val description: String
) {
    TRANSIT("Transit", "Road", "Roads, Rails, Bridges & Runways"),
    RESIDENTIAL("Residential", "House", "Homes, Townhouses & Skyscraper Living"),
    COMMERCIAL("Commercial", "Store", "Shops, Supermarkets & Highrise Offices"),
    INDUSTRIAL("Industrial", "Factory", "Workshops, Factories & Tech Parks"),
    UTILITIES("Utilities", "Bolt", "Power Plants, Solar, Wind & Water Towers"),
    PARKS("Parks & Nature", "Park", "Green Spaces, Plazas, Fountains & Trees"),
    CIVIC("Civic Services", "AccountBalance", "Town Hall, Police, Fire & Hospitals"),
    DEMOLISH("Bulldoze", "Delete", "Clear tiles and reclaim land")
}
