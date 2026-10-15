package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.hypot

@Composable
fun InteractiveLevelCanvas(
    level: BrainLevel,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onSoundClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(12.dp)
            .testTag("interactive_level_canvas"),
        contentAlignment = Alignment.Center
    ) {
        when (level.type) {
            PuzzleType.DRAG_AND_DROP -> DragAndDropCanvas(
                level = level,
                onSuccess = onSuccess,
                onError = onError,
                onSoundClick = onSoundClick
            )

            PuzzleType.TRICK_TAP, PuzzleType.SPOT_DIFFERENCE, PuzzleType.WORD_RIDDLE -> TrickTapCanvas(
                level = level,
                onSuccess = onSuccess,
                onError = onError,
                onSoundClick = onSoundClick
            )

            PuzzleType.CODE_INPUT -> CodeInputCanvas(
                level = level,
                onSuccess = onSuccess,
                onError = onError,
                onSoundClick = onSoundClick
            )

            PuzzleType.ROTATION_PUZZLE -> RotationPipeCanvas(
                level = level,
                onSuccess = onSuccess,
                onSoundClick = onSoundClick
            )

            PuzzleType.BALANCE_SCALE -> BalanceScaleCanvas(
                level = level,
                onSuccess = onSuccess,
                onSoundClick = onSoundClick
            )

            PuzzleType.SCRATCH_REVEAL -> ScratchRevealCanvas(
                level = level,
                onSuccess = onSuccess,
                onSoundClick = onSoundClick
            )

            PuzzleType.SWITCH_BOARD -> SwitchBoardCanvas(
                level = level,
                onSuccess = onSuccess,
                onSoundClick = onSoundClick
            )

            PuzzleType.SHAKE_TRIGGER -> ShakeTriggerCanvas(
                level = level,
                onSuccess = onSuccess,
                onSoundClick = onSoundClick
            )

            PuzzleType.SEQUENCE_ORDER -> TrickTapCanvas(
                level = level,
                onSuccess = onSuccess,
                onError = onError,
                onSoundClick = onSoundClick
            )
        }
    }
}

// -------------------------------------------------------------
// 1. DRAG AND DROP
// -------------------------------------------------------------
@Composable
private fun DragAndDropCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onSoundClick: () -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    val itemOffsets = remember(level.id) {
        mutableStateMapOf<String, Offset>().apply {
            level.draggables.forEach { item ->
                put(item.id, Offset.Zero)
            }
        }
    }

    var solved by remember(level.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        // Render all items
        level.draggables.forEach { item ->
            val offset = itemOffsets[item.id] ?: Offset.Zero
            val initialPixelX = item.initialX * canvasSize.width
            val initialPixelY = item.initialY * canvasSize.height
            val currentCenterX = initialPixelX + offset.x
            val currentCenterY = initialPixelY + offset.y

            Box(
                modifier = Modifier
                    .offset(
                        x = (currentCenterX - 40).coerceAtLeast(0f).dp,
                        y = (currentCenterY - 40).coerceAtLeast(0f).dp
                    )
                    .size(item.widthDp.dp, item.heightDp.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (item.isTarget) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        2.dp,
                        if (item.isTarget) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                    .then(
                        if (item.isMovable && !solved) {
                            Modifier.pointerInput(item.id, canvasSize) {
                                detectDragGestures(
                                    onDragStart = { onSoundClick() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val cur = itemOffsets[item.id] ?: Offset.Zero
                                        itemOffsets[item.id] = cur + dragAmount
                                    },
                                    onDragEnd = {
                                        // Check target zone collision
                                        if (item.targetDropZoneId != null) {
                                            val targetItem = level.draggables.find { it.id == item.targetDropZoneId }
                                            if (targetItem != null) {
                                                val targetOffset = itemOffsets[targetItem.id] ?: Offset.Zero
                                                val targetX = targetItem.initialX * canvasSize.width + targetOffset.x
                                                val targetY = targetItem.initialY * canvasSize.height + targetOffset.y

                                                val dist = hypot(currentCenterX - targetX, currentCenterY - targetY)
                                                if (dist < 180f) {
                                                    solved = true
                                                    onSuccess()
                                                    return@detectDragGestures
                                                }
                                            }
                                        }

                                        // Check if level has a hidden item revealed by moving sofa/rock away
                                        if (level.draggables.any { it.id == "sofa" || it.id == "coin" }) {
                                            val sofa = level.draggables.find { it.id == "sofa" }
                                            if (sofa != null) {
                                                val sOffset = itemOffsets[sofa.id] ?: Offset.Zero
                                                if (hypot(sOffset.x, sOffset.y) > 120f) {
                                                    solved = true
                                                    onSuccess()
                                                    return@detectDragGestures
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.iconEmoji,
                        fontSize = 32.sp
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. TRICK TAP
// -------------------------------------------------------------
@Composable
private fun TrickTapCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onSoundClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val targets = level.tapTargets
        val rows = targets.chunked(2)

        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { target ->
                    val scale = target.sizeScale
                    Card(
                        modifier = Modifier
                            .size((120 * scale).dp, (95 * scale).dp)
                            .testTag("tap_target_${target.id}")
                            .clickable {
                                onSoundClick()
                                if (target.isCorrect) {
                                    onSuccess()
                                } else {
                                    onError(target.failMessage)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = target.emoji,
                                fontSize = (32 * scale).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = target.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (target.subLabel != null) {
                                Text(
                                    text = target.subLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. CODE INPUT CANVAS
// -------------------------------------------------------------
@Composable
private fun CodeInputCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onSoundClick: () -> Unit
) {
    var currentInput by remember(level.id) { mutableStateOf("") }
    val targetCode = level.correctCode ?: "123"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Screen
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = borderStroke()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (currentInput.isEmpty()) "ENTER CODE" else currentInput,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = if (currentInput.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }
        }

        // Keypad grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "OK")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                onSoundClick()
                                when (key) {
                                    "C" -> currentInput = ""
                                    "OK" -> {
                                        if (currentInput == targetCode) {
                                            onSuccess()
                                        } else {
                                            onError("Code $currentInput is incorrect! Try again.")
                                            currentInput = ""
                                        }
                                    }
                                    else -> {
                                        if (currentInput.length < 6) {
                                            currentInput += key
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(60.dp, 44.dp)
                                .testTag("keypad_$key"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == "OK") MaterialTheme.colorScheme.primary
                                else if (key == "C") MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (key == "OK") MaterialTheme.colorScheme.onPrimary
                                else if (key == "C") MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = key,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. ROTATION PIPE CANVAS
// -------------------------------------------------------------
@Composable
private fun RotationPipeCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onSoundClick: () -> Unit
) {
    val pipeRotations = remember(level.id) {
        mutableStateMapOf<Int, Int>().apply {
            level.pipeTiles.forEach { put(it.id, it.currentRotation) }
        }
    }

    var isSolved by remember(level.id) { mutableStateOf(false) }

    fun checkSolved() {
        val allValid = level.pipeTiles.all { tile ->
            val rot = (pipeRotations[tile.id] ?: 0) % 360
            tile.targetRotations.any { target -> (target % 360) == rot }
        }
        if (allValid && !isSolved) {
            isSolved = true
            onSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tap tiles to align the water pipeline:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        val tiles = level.pipeTiles
        val row1 = tiles.take(2)
        val row2 = tiles.drop(2).take(2)

        listOf(row1, row2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { tile ->
                    val currentRotation = pipeRotations[tile.id] ?: 0
                    val animatedAngle by animateFloatAsState(
                        targetValue = currentRotation.toFloat(),
                        animationSpec = spring(),
                        label = "pipe_rotation_${tile.id}"
                    )

                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .testTag("pipe_tile_${tile.id}")
                            .clickable {
                                onSoundClick()
                                pipeRotations[tile.id] = (currentRotation + 90) % 360
                                checkSolved()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = borderStroke()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animatedAngle),
                            contentAlignment = Alignment.Center
                        ) {
                            when (tile.shape) {
                                PipeShape.STRAIGHT -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .background(MaterialTheme.colorScheme.tertiary)
                                    )
                                }
                                PipeShape.CORNER -> {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val strokeWidth = 28.dp.toPx()
                                        val path = Path().apply {
                                            moveTo(size.width / 2, 0f)
                                            lineTo(size.width / 2, size.height / 2)
                                            lineTo(size.width, size.height / 2)
                                        }
                                        drawPath(
                                            path = path,
                                            color = Color(0xFF06B6D4),
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                else -> {
                                    Text("🚰", fontSize = 36.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 5. BALANCE SCALE CANVAS
// -------------------------------------------------------------
@Composable
private fun BalanceScaleCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onSoundClick: () -> Unit
) {
    val selectedItemIds = remember(level.id) { mutableStateListOf<String>() }
    val currentRightWeight = selectedItemIds.sumOf { id ->
        level.balanceItems.find { it.id == id }?.weight ?: 0
    }
    val targetWeight = level.balanceLeftTargetWeight

    val balanceAngle by animateFloatAsState(
        targetValue = when {
            currentRightWeight == targetWeight -> 0f
            currentRightWeight < targetWeight -> -10f
            else -> 10f
        },
        label = "scale_angle"
    )

    LaunchedEffect(currentRightWeight) {
        if (currentRightWeight == targetWeight) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Balance Beam Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated beam
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .rotate(balanceAngle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pan
                Card(
                    modifier = Modifier.size(90.dp, 80.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⚖️ Target", style = MaterialTheme.typography.labelSmall)
                        Text("$targetWeight kg", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                // Center Fulcrum
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔺", fontSize = 16.sp)
                }

                // Right Pan
                Card(
                    modifier = Modifier.size(90.dp, 80.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentRightWeight == targetWeight) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Your Pan", style = MaterialTheme.typography.labelSmall)
                        Text("$currentRightWeight kg", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }

        // Available items to toggle
        Text(
            text = "Select items to place on right pan:",
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            level.balanceItems.forEach { item ->
                val isSelected = selectedItemIds.contains(item.id)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSoundClick()
                        if (isSelected) selectedItemIds.remove(item.id)
                        else selectedItemIds.add(item.id)
                    },
                    label = { Text("${item.emoji} ${item.name} (${item.weight}kg)") },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.testTag("balance_chip_${item.id}")
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. SCRATCH REVEAL CANVAS
// -------------------------------------------------------------
@Composable
private fun ScratchRevealCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onSoundClick: () -> Unit
) {
    val scratchedPoints = remember(level.id) { mutableStateListOf<Offset>() }
    var hasSolved by remember(level.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(level.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        scratchedPoints.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        scratchedPoints.add(change.position)
                        if (scratchedPoints.size > 40 && !hasSolved) {
                            hasSolved = true
                            onSoundClick()
                            onSuccess()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Revealed Secret Underneath
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = level.scratchEmoji ?: "✨",
                fontSize = 54.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = level.scratchSecretText ?: "SECRET UNLOCKED!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // Scratch Cover
        if (!hasSolved) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF64748B).copy(alpha = 0.95f)
                )
                scratchedPoints.forEach { pt ->
                    drawCircle(
                        color = Color.Transparent,
                        radius = 45f,
                        center = pt,
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )
                }
            }

            Text(
                text = "👆 Scratch & Rub Here to Reveal!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// -------------------------------------------------------------
// 7. SWITCH BOARD CANVAS
// -------------------------------------------------------------
@Composable
private fun SwitchBoardCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onSoundClick: () -> Unit
) {
    val switchStates = remember(level.id) {
        mutableStateListOf<Boolean>().apply {
            addAll(level.initialSwitchStates.ifEmpty { listOf(false, false, false) })
        }
    }

    var isSolved by remember(level.id) { mutableStateOf(false) }

    fun checkSolved() {
        if (switchStates.all { it } && !isSolved) {
            isSolved = true
            onSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Neon Lights Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            switchStates.forEachIndexed { index, isOn ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(if (isOn) 12.dp else 0.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isOn) Color(0xFFFFB703) else Color(0xFF475569)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isOn) "💡" else "⚫", fontSize = 26.sp)
                    }
                    Text(
                        text = "Light ${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Switches Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            level.switchConfigs.forEach { cfg ->
                Button(
                    onClick = {
                        onSoundClick()
                        cfg.togglesIndices.forEach { targetIdx ->
                            if (targetIdx in switchStates.indices) {
                                switchStates[targetIdx] = !switchStates[targetIdx]
                            }
                        }
                        checkSolved()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = borderStroke(),
                    modifier = Modifier.testTag("switch_button_${cfg.id}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎚️", fontSize = 24.sp)
                        Text(
                            text = cfg.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. SHAKE TRIGGER CANVAS
// -------------------------------------------------------------
@Composable
private fun ShakeTriggerCanvas(
    level: BrainLevel,
    onSuccess: () -> Unit,
    onSoundClick: () -> Unit
) {
    var shakenTimes by remember(level.id) { mutableIntStateOf(0) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakenTimes % 2 == 1) 15f else -15f,
        animationSpec = spring(),
        label = "shake_offset"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .offset(x = (if (shakenTimes > 0) shakeOffset else 0f).dp)
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(borderStroke()),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (shakenTimes >= 3) "🍎🍎🍎" else "🌳",
                    fontSize = 54.sp
                )
                Text(
                    text = if (shakenTimes >= 3) "Awake & Ripe!" else "Resting Tree",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = {
                onSoundClick()
                shakenTimes++
                if (shakenTimes >= 3) {
                    onSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .height(52.dp)
                .testTag("shake_device_button")
        ) {
            Icon(Icons.Default.Vibration, contentDescription = "Shake device")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (shakenTimes == 0) "SHAKE DEVICE!" else "SHAKE AGAIN! (${3 - shakenTimes} left)",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    1.5.dp,
    MaterialTheme.colorScheme.outlineVariant
)
