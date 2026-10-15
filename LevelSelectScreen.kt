package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.LevelCatalog
import com.example.data.UserProgressManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    progressManager: UserProgressManager,
    audioManager: GameAudioManager,
    onNavigateBack: () -> Unit,
    onLevelSelected: (Int) -> Unit
) {
    val completedLevels by progressManager.completedLevels.collectAsState()
    val levelStars by progressManager.levelStars.collectAsState()
    val currentLevelId by progressManager.currentLevelId.collectAsState()
    val soundEnabled by progressManager.soundEnabled.collectAsState()
    val hapticEnabled by progressManager.hapticEnabled.collectAsState()

    var selectedChapter by remember { mutableIntStateOf(((currentLevelId - 1) / LevelCatalog.LEVELS_PER_CHAPTER) + 1) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpInputText by remember { mutableStateOf("") }

    val chapterLevels = remember(selectedChapter) {
        LevelCatalog.getLevelsForChapter(selectedChapter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "500 Brain Levels",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            audioManager.playClick(soundEnabled, hapticEnabled)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("level_select_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Jump to Level button
                    TextButton(
                        onClick = {
                            audioManager.playClick(soundEnabled, hapticEnabled)
                            showJumpDialog = true
                        },
                        modifier = Modifier.testTag("jump_to_level_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jump", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chapter Selector Scrollable TabRow
            ScrollableTabRow(
                selectedTabIndex = selectedChapter - 1,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                LevelCatalog.CHAPTER_NAMES.forEachIndexed { index, name ->
                    val chapterNum = index + 1
                    val startId = (chapterNum - 1) * LevelCatalog.LEVELS_PER_CHAPTER + 1
                    val endId = chapterNum * LevelCatalog.LEVELS_PER_CHAPTER
                    val chapterSolved = (startId..endId).count { completedLevels.contains(it) }

                    Tab(
                        selected = selectedChapter == chapterNum,
                        onClick = {
                            audioManager.playClick(soundEnabled, hapticEnabled)
                            selectedChapter = chapterNum
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Ch $chapterNum",
                                    fontWeight = if (selectedChapter == chapterNum) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "$chapterSolved/50",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedChapter == chapterNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("chapter_tab_$chapterNum")
                    )
                }
            }

            // Chapter Title & Info Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LevelCatalog.CHAPTER_NAMES[selectedChapter - 1],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Levels ${(selectedChapter - 1) * 50 + 1} to ${selectedChapter * 50}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 50-Level Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("levels_grid")
            ) {
                items(chapterLevels, key = { it.id }) { lvl ->
                    val isCompleted = completedLevels.contains(lvl.id)
                    val isCurrent = lvl.id == currentLevelId
                    val isUnlocked = progressManager.isLevelUnlocked(lvl.id)
                    val stars = levelStars[lvl.id] ?: 0

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .testTag("level_item_${lvl.id}")
                            .clickable(enabled = isUnlocked) {
                                audioManager.playClick(soundEnabled, hapticEnabled)
                                onLevelSelected(lvl.id)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primaryContainer
                            isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            isUnlocked -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isCurrent) 2.5.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else if (isCompleted) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        tonalElevation = if (isCurrent || isCompleted) 4.dp else 1.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${lvl.id}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCompleted) {
                                        Text(
                                            text = "⭐".repeat(stars.coerceAtLeast(1)),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Jump to level dialog
    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("Jump to Level", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter any level number from 1 to 500:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jumpInputText,
                        onValueChange = { jumpInputText = it.filter { ch -> ch.isDigit() }.take(3) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val target = jumpInputText.toIntOrNull()
                                if (target != null && target in 1..LevelCatalog.TOTAL_LEVELS) {
                                    showJumpDialog = false
                                    onLevelSelected(target)
                                }
                            }
                        ),
                        placeholder = { Text("e.g. 42") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jump_level_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = jumpInputText.toIntOrNull()
                        if (target != null && target in 1..LevelCatalog.TOTAL_LEVELS) {
                            showJumpDialog = false
                            onLevelSelected(target)
                        }
                    },
                    modifier = Modifier.testTag("confirm_jump_button")
                ) {
                    Text("Go!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
