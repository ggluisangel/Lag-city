package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CityTile
import com.example.model.TileType
import com.example.model.Vehicle

@Composable
fun TileInfoSheet(
    tile: CityTile?,
    vehicle: Vehicle?,
    currentFunds: Long,
    onUpgrade: (CityTile) -> Unit,
    onDemolish: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (tile == null && vehicle == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF00F172A),
        tonalElevation = 12.dp,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vehicle != null) Icons.Default.DirectionsCar else Icons.Default.Apartment,
                            contentDescription = "Details",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        val title = vehicle?.type?.name?.replace("_", " ") ?: tile?.type?.displayName ?: "Tile"
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val subtitle = if (vehicle != null) "Autonomous Transit Entity" else "${tile?.type?.category?.title} • (${tile?.x}, ${tile?.y})"
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_inspector_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (vehicle != null) {
                // Vehicle Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Category", vehicle.type.category.name, Icons.Default.AltRoute, Color(0xFF38BDF8), Modifier.weight(1f))
                    StatCard("Passengers", "${vehicle.passengerCount} on board", Icons.Default.People, Color(0xFF4ADE80), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Speed", "${(vehicle.type.speed * 100).toInt()} km/h", Icons.Default.Speed, Color(0xFFFBBF24), Modifier.weight(1f))
                    StatCard("Destination", vehicle.destinationName, Icons.Default.Place, Color(0xFFA78BFA), Modifier.weight(1f))
                }
            } else if (tile != null) {
                // Construction Progress Indicator if under construction
                if (tile.isUnderConstruction) {
                    val progress = tile.getConstructionProgress()
                    val percent = (progress * 100).toInt()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0x33F59E0B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Construction,
                                        contentDescription = "Under Construction",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Under Construction",
                                        color = Color(0xFFFDE68A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "$percent%",
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = Color(0x33F59E0B)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Building Details Grid
                val t = tile.type
                val maxPop = tile.currentMaxPop
                val jobs = tile.currentJobs

                if (maxPop > 0) {
                    // Residential Occupancy Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Residents Occupancy", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("${tile.occupants} / $maxPop", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (maxPop > 0) tile.occupants.toFloat() / maxPop else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0x3338BDF8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Grid of 4 key stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Power Status",
                        value = if (t.powerGenerated > 0) "+${tile.currentPowerGenerated} Gen" else "-${tile.currentPowerNeeded} Needed",
                        icon = Icons.Default.Bolt,
                        color = if (tile.hasPower) Color(0xFFFBBF24) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Water Status",
                        value = if (t.waterGenerated > 0) "+${tile.currentWaterGenerated} Gen" else "-${tile.currentWaterNeeded} Needed",
                        icon = Icons.Default.WaterDrop,
                        color = if (tile.hasWater) Color(0xFF38BDF8) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Hourly Upkeep",
                        value = "-$${tile.currentUpkeep}/hr",
                        icon = Icons.Default.AttachMoney,
                        color = Color(0xFFF87171),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Happiness",
                        value = "+${tile.currentHappinessBoost}% Boost",
                        icon = Icons.Default.Mood,
                        color = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Upgrade & Demolish
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tile.type.maxLevel > 1 && tile.level < tile.type.maxLevel) {
                        val upgradeCost = (tile.type.cost * 0.8f * (tile.level + 1)).toLong()
                        val canAfford = currentFunds >= upgradeCost
                        Button(
                            onClick = { onUpgrade(tile) },
                            enabled = canAfford,
                            modifier = Modifier.weight(1f).testTag("upgrade_tile_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upgrade, contentDescription = "Upgrade", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upgrade ($$upgradeCost)")
                        }
                    }

                    if (tile.type != TileType.GRASS && tile.type != TileType.WATER) {
                        OutlinedButton(
                            onClick = { onDemolish(tile.x, tile.y) },
                            modifier = Modifier.weight(1f).testTag("demolish_tile_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Demolish", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            val refund = (tile.type.cost * 0.4f).toInt()
                            Text("Demolish (+$refund)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x331E293B),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 10.sp)
                Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
