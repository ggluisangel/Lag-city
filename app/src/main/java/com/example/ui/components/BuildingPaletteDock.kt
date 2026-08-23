package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildingCategory
import com.example.model.TileType
import com.example.viewmodel.ToolMode

@Composable
fun BuildingPaletteDock(
    currentCategory: BuildingCategory,
    selectedTileType: TileType?,
    toolMode: ToolMode,
    currentFunds: Long,
    onCategorySelected: (BuildingCategory) -> Unit,
    onTileTypeSelected: (TileType) -> Unit,
    onToolModeSelected: (ToolMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xEE0F172A),
        tonalElevation = 10.dp,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            // Category & Action Mode Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inspect Mode Pill
                CategoryPill(
                    icon = Icons.Default.Search,
                    label = "Inspect",
                    isSelected = toolMode == ToolMode.INSPECT,
                    activeColor = Color(0xFF38BDF8),
                    onClick = { onToolModeSelected(ToolMode.INSPECT) },
                    testTag = "category_inspect"
                )

                // Bulldoze Mode Pill
                CategoryPill(
                    icon = Icons.Default.DeleteForever,
                    label = "Demolish",
                    isSelected = toolMode == ToolMode.BULLDOZE,
                    activeColor = Color(0xFFEF4444),
                    onClick = { onCategorySelected(BuildingCategory.DEMOLISH) },
                    testTag = "category_bulldoze"
                )

                // Standard Categories
                val categories = listOf(
                    BuildingCategory.TRANSIT to (Icons.Default.DirectionsCar to Color(0xFF60A5FA)),
                    BuildingCategory.RESIDENTIAL to (Icons.Default.Home to Color(0xFF4ADE80)),
                    BuildingCategory.COMMERCIAL to (Icons.Default.Storefront to Color(0xFF38BDF8)),
                    BuildingCategory.INDUSTRIAL to (Icons.Default.Factory to Color(0xFFFBBF24)),
                    BuildingCategory.UTILITIES to (Icons.Default.Bolt to Color(0xFFA78BFA)),
                    BuildingCategory.PARKS to (Icons.Default.Park to Color(0xFF34D399)),
                    BuildingCategory.CIVIC to (Icons.Default.AccountBalance to Color(0xFFF472B6))
                )

                for ((cat, iconAndColor) in categories) {
                    val (icon, color) = iconAndColor
                    val isSelected = toolMode == ToolMode.BUILD && currentCategory == cat
                    CategoryPill(
                        icon = icon,
                        label = cat.title,
                        isSelected = isSelected,
                        activeColor = color,
                        onClick = { onCategorySelected(cat) },
                        testTag = "category_${cat.name.lowercase()}"
                    )
                }
            }

            // Sub-palette items (when in BUILD mode)
            AnimatedVisibility(
                visible = toolMode == ToolMode.BUILD,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val availableTiles = TileType.values().filter { it.category == currentCategory && it.isBuildable }
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (tile in availableTiles) {
                            val isSelected = selectedTileType == tile
                            val canAfford = currentFunds >= tile.cost

                            BuildingItemCard(
                                tile = tile,
                                isSelected = isSelected,
                                canAfford = canAfford,
                                onClick = { onTileTypeSelected(tile) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0x331E293B),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else null,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BuildingItemCard(
    tile: TileType,
    isSelected: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFF38BDF8)
        !canAfford -> Color(0x33EF4444)
        else -> Color(0x22FFFFFF)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E293B) else Color(0x660F172A),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(132.dp)
            .clickable { onClick() }
            .testTag("build_item_${tile.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Top Row: Name & Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tile.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cost Badge & Stat tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (canAfford) Color(0x334ADE80) else Color(0x33EF4444)
                ) {
                    Text(
                        text = "$${tile.cost}",
                        color = if (canAfford) Color(0xFF4ADE80) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // Relevant Mini Stat Tag
                val statTag = when {
                    tile.maxPopCapacity > 0 -> "👥 +${tile.maxPopCapacity}"
                    tile.jobsCapacity > 0 -> "💼 +${tile.jobsCapacity}"
                    tile.powerGenerated > 0 -> "⚡ +${tile.powerGenerated}"
                    tile.waterGenerated > 0 -> "💧 +${tile.waterGenerated}"
                    tile.happinessBoost > 0 -> "😊 +${tile.happinessBoost}"
                    else -> "🛣️ Pvd"
                }

                Text(
                    text = statTag,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
