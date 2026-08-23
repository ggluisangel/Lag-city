package com.example.model

enum class CityPolicy(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val hourlyCostPerCitizen: Float,
    val happinessModifier: Int,
    val economicModifier: Float // Multiplier for commercial/industrial tax
) {
    GREEN_ENERGY(
        "green_energy",
        "Clean Energy Subsidies",
        "Reduces industrial pollution by 40% and boosts civic happiness, minor upkeep cost.",
        "Eco",
        hourlyCostPerCitizen = 0.05f,
        happinessModifier = 6,
        economicModifier = 0.98f
    ),
    FREE_TRANSIT(
        "free_transit",
        "Zero-Fare Public Transit",
        "Reduces road traffic congestion by 35% and boosts commercial commerce.",
        "DirectionsBus",
        hourlyCostPerCitizen = 0.12f,
        happinessModifier = 8,
        economicModifier = 1.05f
    ),
    TECH_HUB(
        "tech_hub",
        "Silicon District Incentives",
        "Commercial and tech jobs generate +20% higher revenue for the treasury.",
        "Computer",
        hourlyCostPerCitizen = 0.08f,
        happinessModifier = 3,
        economicModifier = 1.20f
    ),
    NIGHTLIFE_DISTRICT(
        "nightlife_district",
        "24/7 Entertainment Zoning",
        "Commercial buildings stay open late with glowing neon lights and higher night revenue.",
        "Nightlife",
        hourlyCostPerCitizen = 0.04f,
        happinessModifier = 7,
        economicModifier = 1.10f
    ),
    FESTIVAL_WEEK(
        "festival_week",
        "City Grand Carnival",
        "Fireworks, parades, street markets and maximum happiness boost!",
        "Celebration",
        hourlyCostPerCitizen = 0.20f,
        happinessModifier = 15,
        economicModifier = 1.15f
    )
}
