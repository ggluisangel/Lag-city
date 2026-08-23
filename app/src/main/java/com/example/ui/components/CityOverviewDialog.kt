package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CityEntity
import com.example.engine.MapPreset
import com.example.model.CityLevel
import com.example.model.CityPolicy
import com.example.model.CityStats

@Composable
fun CityOverviewDialog(
    stats: CityStats,
    activePolicies: Set<CityPolicy>,
    savedCities: List<CityEntity>,
    onDismiss: () -> Unit,
    onTaxRatesChanged: (Float, Float, Float) -> Unit,
    onPolicyToggle: (CityPolicy) -> Unit,
    onSaveCity: () -> Unit,
    onLoadCity: (CityEntity) -> Unit,
    onDeleteCity: (CityEntity) -> Unit,
    onNewMapPreset: (MapPreset) -> Unit,
    onLaunchFestival: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Budget", "Policies", "Milestones", "Saves & Maps")

    var resTax by remember(stats.residentialTaxRate) { mutableStateOf(stats.residentialTaxRate) }
    var commTax by remember(stats.commercialTaxRate) { mutableStateOf(stats.commercialTaxRate) }
    var indTax by remember(stats.industrialTaxRate) { mutableStateOf(stats.industrialTaxRate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${stats.cityName} Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "City Hall Municipal Governance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_dashboard_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0x331E293B),
                    contentColor = Color(0xFF38BDF8),
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier.testTag("tab_${title.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Contents
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> BudgetTab(
                            stats = stats,
                            resTax = resTax,
                            commTax = commTax,
                            indTax = indTax,
                            onResTaxChanged = {
                                resTax = it
                                onTaxRatesChanged(resTax, commTax, indTax)
                            },
                            onCommTaxChanged = {
                                commTax = it
                                onTaxRatesChanged(resTax, commTax, indTax)
                            },
                            onIndTaxChanged = {
                                indTax = it
                                onTaxRatesChanged(resTax, commTax, indTax)
                            }
                        )
                        1 -> PoliciesTab(
                            activePolicies = activePolicies,
                            onPolicyToggle = onPolicyToggle,
                            onLaunchFestival = onLaunchFestival
                        )
                        2 -> MilestonesTab(stats = stats)
                        3 -> SavesAndMapsTab(
                            savedCities = savedCities,
                            onSave = onSaveCity,
                            onLoad = onLoadCity,
                            onDelete = onDeleteCity,
                            onNewPreset = onNewMapPreset
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetTab(
    stats: CityStats,
    resTax: Float,
    commTax: Float,
    indTax: Float,
    onResTaxChanged: (Float) -> Unit,
    onCommTaxChanged: (Float) -> Unit,
    onIndTaxChanged: (Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Financial Summary Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x331E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Treasury & Cash Flow", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Funds", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$${stats.funds}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("Hourly Revenue", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("+$${stats.hourlyTaxIncome}", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("Hourly Expenses", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("-$${stats.hourlyUpkeepCost}", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Tax Rates Adjustment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            TaxSliderCard("Residential Property Tax", resTax, onResTaxChanged, "Citizens pay based on housing population.")
        }
        item {
            TaxSliderCard("Commercial Sales Tax", commTax, onCommTaxChanged, "Retail, shops and corporate commerce revenue.")
        }
        item {
            TaxSliderCard("Industrial Production Tax", indTax, onIndTaxChanged, "Manufacturing, logistics and energy facilities.")
        }
    }
}

@Composable
private fun TaxSliderCard(
    title: String,
    rate: Float,
    onRateChanged: (Float) -> Unit,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x221E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "${(rate * 100).toInt()}%",
                    color = Color(0xFF38BDF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(description, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Slider(
                value = rate,
                onValueChange = onRateChanged,
                valueRange = 0.01f..0.22f,
                steps = 20,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF38BDF8)
                )
            )
        }
    }
}

@Composable
private fun PoliciesTab(
    activePolicies: Set<CityPolicy>,
    onPolicyToggle: (CityPolicy) -> Unit,
    onLaunchFestival: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Festival Special Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x33F59E0B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🎉 Host City Carnival", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Launch fireworks, parades, and joyful street festivities!", color = Color(0xFFFDE68A), fontSize = 11.sp)
                    }
                    Button(
                        onClick = onLaunchFestival,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("launch_festival_btn")
                    ) {
                        Text("Celebrate!", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Civic & Economic Ordinances", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(CityPolicy.values()) { policy ->
            val isActive = activePolicies.contains(policy)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isActive) Color(0x330284C7) else Color(0x221E293B),
                border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPolicyToggle(policy) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(policy.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(policy.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onPolicyToggle(policy) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF38BDF8)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestonesTab(stats: CityStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Metropolis Progression Track", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(CityLevel.values()) { lvl ->
            val isReached = stats.population >= lvl.requiredPopulation
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isReached) Color(0x3310B981) else Color(0x221E293B),
                border = if (isReached) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isReached) Color(0xFF10B981) else Color(0x44475569)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReached) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = "Milestone Status",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(lvl.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Requires ${lvl.requiredPopulation}+ citizens • Reward: +$${lvl.rewardGrant}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        Text(lvl.unlockedDescription, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SavesAndMapsTab(
    savedCities: List<CityEntity>,
    onSave: () -> Unit,
    onLoad: (CityEntity) -> Unit,
    onDelete: (CityEntity) -> Unit,
    onNewPreset: (MapPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Save Current City Button
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_city_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Current City State")
            }
        }

        if (savedCities.isNotEmpty()) {
            item {
                Text("Saved City Slots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            items(savedCities) { city ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x331E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(city.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Pop: ${city.population} • Funds: $${city.funds} • Happiness: ${city.happiness}%", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onLoad(city) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load", fontSize = 11.sp)
                            }
                            IconButton(onClick = { onDelete(city) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Generate New Map Preset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(MapPreset.values()) { preset ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x221E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNewPreset(preset) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(preset.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onNewPreset(preset) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Build", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
