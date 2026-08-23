package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.CityStats

@Composable
fun GameTopBar(
    stats: CityStats,
    timeOfDay: Float,
    gameSpeed: Float,
    onSpeedChanged: (Float) -> Unit,
    onDayNightToggle: () -> Unit,
    onOpenManagement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xDD0F172A), // Dark slate glassmorphism
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Row: City Name, Level Rank, Management Button & Time/Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // City Title & Rank Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenManagement() }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF1E88E5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = "City",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stats.cityName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stats.level.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Time, Day/Night & Speed Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Time of Day Pill
                    val hour = timeOfDay.toInt()
                    val minute = ((timeOfDay - hour) * 60).toInt()
                    val timeStr = String.format("%02d:%02d", hour, minute)
                    val isNight = timeOfDay < 6.0f || timeOfDay > 19.5f

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x3338BDF8),
                        modifier = Modifier.clickable { onDayNightToggle() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isNight) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                                contentDescription = "Time",
                                tint = if (isNight) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = timeStr,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Speed buttons (0x, 1x, 2x, 5x)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x331E293B))
                            .padding(2.dp)
                    ) {
                        val speeds = listOf(0f to "⏸", 1f to "1x", 2f to "2x", 5f to "5x")
                        for ((speedVal, label) in speeds) {
                            val isSelected = (gameSpeed == speedVal) || (speedVal == 0f && gameSpeed <= 0f)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF38BDF8) else Color.Transparent)
                                    .clickable { onSpeedChanged(speedVal) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("speed_btn_$label"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // City Management button
                    IconButton(
                        onClick = onOpenManagement,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("city_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = "Manage City",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Funds ($), Population, Happiness, Power, Water
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Funds & Hourly Flow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$",
                        color = Color(0xFF4ADE80),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${stats.funds}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val sign = if (stats.netHourlyIncome >= 0) "+$" else "-$"
                    val flowColor = if (stats.netHourlyIncome >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
                    Text(
                        text = "($sign${kotlin.math.abs(stats.netHourlyIncome)}/hr)",
                        color = flowColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Population
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Population",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${stats.population}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/${stats.maxPopulationCapacity}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Happiness
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val happyEmoji = when {
                        stats.happiness >= 80 -> "😄"
                        stats.happiness >= 60 -> "🙂"
                        stats.happiness >= 40 -> "😐"
                        else -> "😡"
                    }
                    Text(text = happyEmoji, fontSize = 14.sp)
                    Text(
                        text = "${stats.happiness}%",
                        color = when {
                            stats.happiness >= 75 -> Color(0xFF4ADE80)
                            stats.happiness >= 50 -> Color(0xFFFBBF24)
                            else -> Color(0xFFF87171)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Power & Water Status Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Power
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (stats.isPowerDeficit) Color(0x33EF4444) else Color(0x22FBBF24))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power",
                            tint = if (stats.isPowerDeficit) Color(0xFFEF4444) else Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${stats.powerGenerated}",
                            color = if (stats.isPowerDeficit) Color(0xFFEF4444) else Color(0xFFFDE68A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Water
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (stats.isWaterDeficit) Color(0x33EF4444) else Color(0x2238BDF8))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water",
                            tint = if (stats.isWaterDeficit) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${stats.waterGenerated}",
                            color = if (stats.isWaterDeficit) Color(0xFFEF4444) else Color(0xFFBAE6FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
