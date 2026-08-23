package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.IsometricMath
import com.example.graphics.IsometricRenderer
import com.example.model.*
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ToolMode
import kotlin.math.sin

@Composable
fun IsoCityScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val grid by viewModel.grid.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val citizens by viewModel.citizens.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTileType by viewModel.selectedTileType.collectAsStateWithLifecycle()
    val toolMode by viewModel.toolMode.collectAsStateWithLifecycle()
    val inspectedTile by viewModel.inspectedTile.collectAsStateWithLifecycle()
    val inspectedVehicle by viewModel.inspectedVehicle.collectAsStateWithLifecycle()
    val timeOfDay by viewModel.timeOfDay.collectAsStateWithLifecycle()
    val gameSpeed by viewModel.gameSpeed.collectAsStateWithLifecycle()
    val activePolicies by viewModel.activePolicies.collectAsStateWithLifecycle()
    val milestoneEvent by viewModel.milestoneEvent.collectAsStateWithLifecycle()
    val savedCities by viewModel.savedCities.collectAsStateWithLifecycle()

    var showManagementDialog by remember { mutableStateOf(false) }

    // Camera Transformation State
    var cameraOffsetX by remember { mutableFloatStateOf(0f) }
    var cameraOffsetY by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }

    // Infinite Animation Driver for 60fps smooth rendering
    val infiniteTransition = rememberInfiniteTransition(label = "game_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_progress"
    )

    // Dynamic Sky Gradient based on Time of Day
    val skyBrush = remember(timeOfDay) {
        when {
            timeOfDay in 5.0f..7.5f -> { // Dawn
                Brush.verticalGradient(listOf(Color(0xFF2C1654), Color(0xFFE27B58), Color(0xFFFDE68A)))
            }
            timeOfDay in 7.5f..16.5f -> { // Day
                Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD)))
            }
            timeOfDay in 16.5f..19.5f -> { // Sunset / Twilight
                Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFFC2410C), Color(0xFFFB923C)))
            }
            else -> { // Deep Night
                Brush.verticalGradient(listOf(Color(0xFF030712), Color(0xFF0B132B), Color(0xFF1C2541)))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(skyBrush)
    ) {
        // Isometric Game Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("isometric_game_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        cameraOffsetX += pan.x
                        cameraOffsetY += pan.y
                        zoom = (zoom * gestureZoom).coerceIn(0.45f, 2.5f)
                    }
                }
                .pointerInput(toolMode, selectedTileType, grid) {
                    detectTapGestures { tapOffset ->
                        val (gx, gy) = IsometricMath.screenToGrid(
                            screenX = tapOffset.x,
                            screenY = tapOffset.y,
                            offsetX = cameraOffsetX,
                            offsetY = cameraOffsetY,
                            zoom = zoom
                        )
                        viewModel.onTileTapped(gx, gy)
                    }
                }
        ) {
            // Auto-center camera on first layout if not yet initialized
            if (cameraOffsetX == 0f && cameraOffsetY == 0f && grid.isNotEmpty()) {
                cameraOffsetX = size.width / 2f
                cameraOffsetY = size.height * 0.32f
            }

            val width = if (grid.isNotEmpty()) grid[0].size else 0
            val height = grid.size

            if (width > 0 && height > 0) {
                // 1. Draw all ground tiles & buildings sorted by isometric depth (gx + gy)
                for (depth in 0 until (width + height - 1)) {
                    for (x in 0..depth) {
                        val y = depth - x
                        if (x in 0 until width && y in 0 until height) {
                            val tile = grid[y][x]
                            IsometricRenderer.drawTile(
                                scope = this,
                                tile = tile,
                                offsetX = cameraOffsetX,
                                offsetY = cameraOffsetY,
                                zoom = zoom,
                                timeOfDay = timeOfDay,
                                animProgress = animProgress,
                                isFestival = activePolicies.contains(CityPolicy.FESTIVAL_WEEK),
                                mapWidth = width,
                                mapHeight = height
                            )
                        }
                    }
                }

                // 2. Draw Autonomous Pedestrians
                for (citizen in citizens) {
                    IsometricRenderer.drawCitizen(
                        scope = this,
                        citizen = citizen,
                        offsetX = cameraOffsetX,
                        offsetY = cameraOffsetY,
                        zoom = zoom
                    )
                }

                // 3. Draw Autonomous Vehicles (Cars, Buses, Trains, Boats, Planes)
                for (vehicle in vehicles) {
                    IsometricRenderer.drawVehicle(
                        scope = this,
                        vehicle = vehicle,
                        offsetX = cameraOffsetX,
                        offsetY = cameraOffsetY,
                        zoom = zoom,
                        timeOfDay = timeOfDay
                    )
                }

                // 4. Environmental Sky FX: Clouds & Shadows, Birds Flock
                com.example.graphics.EffectsRenderer.drawSkyCloudsAndShadows(
                    scope = this,
                    screenWidth = size.width,
                    screenHeight = size.height,
                    animProgress = animProgress
                )

                com.example.graphics.EffectsRenderer.drawBirdFlock(
                    scope = this,
                    screenWidth = size.width,
                    screenHeight = size.height,
                    animProgress = animProgress
                )

                // 5. Festival Celebration Balloons & Confetti (if festival or milestone active)
                if (activePolicies.contains(CityPolicy.FESTIVAL_WEEK) || milestoneEvent != null) {
                    com.example.graphics.EffectsRenderer.drawFestivalCelebration(
                        scope = this,
                        screenWidth = size.width,
                        screenHeight = size.height,
                        animProgress = animProgress
                    )
                }

                // 6. Draw Tile Highlight / Selection Box if a tile is inspected
                if (inspectedTile != null) {
                    val itile = inspectedTile!!
                    IsometricRenderer.drawTileHighlight(
                        scope = this,
                        gridX = itile.x,
                        gridY = itile.y,
                        offsetX = cameraOffsetX,
                        offsetY = cameraOffsetY,
                        zoom = zoom,
                        highlightColor = if (toolMode == ToolMode.BULLDOZE) Color(0xFFFF1744) else Color(0xFF38BDF8),
                        isBulldoze = toolMode == ToolMode.BULLDOZE
                    )
                }
            }
        }

        // Overlay UI Elements:
        // Top Bar
        GameTopBar(
            stats = stats,
            timeOfDay = timeOfDay,
            gameSpeed = gameSpeed,
            onSpeedChanged = { viewModel.setGameSpeed(it) },
            onDayNightToggle = { viewModel.toggleDayNightCycle() },
            onOpenManagement = { showManagementDialog = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Floating Camera Reset Button on the right side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xDD0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = {
                        zoom = 1.0f
                        cameraOffsetX = 0f
                        cameraOffsetY = 0f
                    },
                    modifier = Modifier.testTag("center_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center Camera",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xDD0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = { zoom = (zoom + 0.25f).coerceAtMost(2.5f) },
                    modifier = Modifier.testTag("zoom_in_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xDD0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = { zoom = (zoom - 0.25f).coerceAtLeast(0.45f) },
                    modifier = Modifier.testTag("zoom_out_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Milestone Notification Banner
        MilestoneBanner(
            event = milestoneEvent,
            onDismiss = { viewModel.dismissMilestone() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
        )

        // Inspector Sheet or Bottom Palette Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (inspectedTile != null || inspectedVehicle != null) {
                TileInfoSheet(
                    tile = inspectedTile,
                    vehicle = inspectedVehicle,
                    currentFunds = stats.funds,
                    onUpgrade = { viewModel.upgradeTile(it) },
                    onDemolish = { x, y -> viewModel.demolishTile(x, y) },
                    onDismiss = { viewModel.dismissInspector() }
                )
            }

            BuildingPaletteDock(
                currentCategory = selectedCategory,
                selectedTileType = selectedTileType,
                toolMode = toolMode,
                currentFunds = stats.funds,
                onCategorySelected = { viewModel.selectCategory(it) },
                onTileTypeSelected = { viewModel.selectTileType(it) },
                onToolModeSelected = { viewModel.setToolMode(it) }
            )
        }

        // City Management & Governance Overview Dialog
        if (showManagementDialog) {
            CityOverviewDialog(
                stats = stats,
                activePolicies = activePolicies,
                savedCities = savedCities,
                onDismiss = { showManagementDialog = false },
                onTaxRatesChanged = { res, comm, ind -> viewModel.setTaxRates(res, comm, ind) },
                onPolicyToggle = { viewModel.togglePolicy(it) },
                onSaveCity = { viewModel.saveCurrentCity() },
                onLoadCity = {
                    viewModel.loadCity(it)
                    showManagementDialog = false
                },
                onDeleteCity = { viewModel.deleteSavedCity(it) },
                onNewMapPreset = {
                    viewModel.initMap(it)
                    showManagementDialog = false
                },
                onLaunchFestival = {
                    viewModel.triggerCelebrationFestival()
                    showManagementDialog = false
                }
            )
        }
    }
}
