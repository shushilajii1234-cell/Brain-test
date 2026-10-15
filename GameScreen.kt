package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.LevelCatalog
import com.example.data.UserProgressManager
import com.example.model.BrainLevel
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.InteractiveLevelCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    progressManager: UserProgressManager,
    audioManager: GameAudioManager,
    onNavigateToLevelSelect: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val currentLevelId by progressManager.currentLevelId.collectAsState()
    val hintBulbs by progressManager.hintBulbs.collectAsState()
    val soundEnabled by progressManager.soundEnabled.collectAsState()
    val hapticEnabled by progressManager.hapticEnabled.collectAsState()

    val level = remember(currentLevelId) { LevelCatalog.getLevel(currentLevelId) }

    var showHintDialog by remember { mutableStateOf(false) }
    var hintLevelRevealed by remember(currentLevelId) { mutableIntStateOf(0) } // 0=none, 1=nudge, 2=clue, 3=solve
    var showWinDialog by remember(currentLevelId) { mutableStateOf(false) }
    var earnedStars by remember(currentLevelId) { mutableIntStateOf(3) }
    var errorMessage by remember(currentLevelId) { mutableStateOf<String?>(null) }
    var keyWordTapped by remember(currentLevelId) { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun triggerError(msg: String) {
        audioManager.playError(soundEnabled, hapticEnabled)
        errorMessage = msg
        coroutineScope.launch {
            delay(3000)
            if (errorMessage == msg) errorMessage = null
        }
    }

    fun triggerSuccess() {
        audioManager.playSuccess(soundEnabled, hapticEnabled)
        val stars = when (hintLevelRevealed) {
            0 -> 3
            1 -> 2
            else -> 1
        }
        earnedStars = stars
        progressManager.completeLevel(level.id, stars, bulbsReward = if (stars == 3) 2 else 1)
        showWinDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Level ${level.id}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Ch ${level.chapterIndex}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = level.chapterName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            audioManager.playClick(soundEnabled, hapticEnabled)
                            onNavigateToLevelSelect()
                        },
                        modifier = Modifier.testTag("nav_level_select_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Level Selector")
                    }
                },
                actions = {
                    // Bulbs balance pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clickable {
                                audioManager.playClick(soundEnabled, hapticEnabled)
                                onNavigateToStats()
                            }
                            .testTag("bulbs_balance_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$hintBulbs",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add Bulbs",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = {
                            val now = progressManager.toggleSound()
                            if (now) audioManager.playClick(true, hapticEnabled)
                        },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        Icon(
                            if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Question Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = level.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        // Easter egg trick: if question keyword is tappable
                        if (level.questionKeywordTappable != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "(${level.questionKeywordTappable})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (keyWordTapped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .clickable {
                                        keyWordTapped = true
                                        triggerSuccess()
                                    }
                                    .testTag("keyword_trick_target")
                            )
                        }
                    }
                }

                // Interactive Puzzle Canvas
                InteractiveLevelCanvas(
                    level = level,
                    onSuccess = { triggerSuccess() },
                    onError = { msg -> triggerError(msg) },
                    onSoundClick = { audioManager.playClick(soundEnabled, hapticEnabled) }
                )

                // Error Feedback Toast
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Game Action Bar: Hints, Reset, Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Level Button
                    IconButton(
                        onClick = {
                            if (currentLevelId > 1) {
                                audioManager.playClick(soundEnabled, hapticEnabled)
                                progressManager.setCurrentLevel(currentLevelId - 1)
                            }
                        },
                        enabled = currentLevelId > 1,
                        modifier = Modifier.testTag("prev_level_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Level")
                    }

                    // Reset / Retry Canvas
                    OutlinedButton(
                        onClick = {
                            audioManager.playClick(soundEnabled, hapticEnabled)
                            // Re-triggers remember by toggling slightly
                            progressManager.setCurrentLevel(currentLevelId)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("reset_level_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }

                    // Hint Button
                    Button(
                        onClick = {
                            audioManager.playHint(soundEnabled, hapticEnabled)
                            showHintDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("hint_button")
                    ) {
                        Text("💡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HINT",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF451A03)
                        )
                    }

                    // Next Level Button
                    IconButton(
                        onClick = {
                            if (currentLevelId < LevelCatalog.TOTAL_LEVELS) {
                                audioManager.playClick(soundEnabled, hapticEnabled)
                                progressManager.setCurrentLevel(currentLevelId + 1)
                            }
                        },
                        enabled = currentLevelId < LevelCatalog.TOTAL_LEVELS,
                        modifier = Modifier.testTag("next_level_arrow_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Level")
                    }
                }
            }

            // Confetti Overlay when winning
            if (showWinDialog) {
                ConfettiEffect()
            }
        }
    }

    // ---------------------------------------------------------
    // HINT DIALOG MODAL
    // ---------------------------------------------------------
    if (showHintDialog) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            icon = { Text("💡", fontSize = 36.sp) },
            title = {
                Text(
                    text = "Brain Teaser Clues",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Current Bulbs: $hintBulbs 💡",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Tier 1 Hint: Nudge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hintLevelRevealed >= 1) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hint 1: Gentle Nudge", fontWeight = FontWeight.Bold)
                                if (hintLevelRevealed < 1) {
                                    Button(
                                        onClick = {
                                            if (progressManager.consumeBulbs(1)) {
                                                hintLevelRevealed = 1
                                                audioManager.playHint(soundEnabled, hapticEnabled)
                                            } else {
                                                triggerError("Need 1 bulb! Claim free bulbs in rewards!")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Unlock (1💡)", fontSize = 12.sp, color = Color(0xFF451A03))
                                    }
                                }
                            }
                            if (hintLevelRevealed >= 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(level.hint1, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Tier 2 Hint: Clear Clue
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hintLevelRevealed >= 2) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hint 2: Direct Clue", fontWeight = FontWeight.Bold)
                                if (hintLevelRevealed < 2) {
                                    Button(
                                        onClick = {
                                            if (progressManager.consumeBulbs(2)) {
                                                hintLevelRevealed = 2
                                                audioManager.playHint(soundEnabled, hapticEnabled)
                                            } else {
                                                triggerError("Need 2 bulbs! Claim free bulbs in rewards!")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Unlock (2💡)", fontSize = 12.sp, color = Color(0xFF451A03))
                                    }
                                }
                            }
                            if (hintLevelRevealed >= 2) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(level.hint2, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Tier 3: Auto-Solve / Instant Solution
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Skip / Auto-Solve", fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        if (progressManager.consumeBulbs(3)) {
                                            hintLevelRevealed = 3
                                            showHintDialog = false
                                            triggerSuccess()
                                        } else {
                                            triggerError("Need 3 bulbs to auto-solve!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Solve (3💡)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHintDialog = false },
                    modifier = Modifier.testTag("close_hint_dialog_button")
                ) {
                    Text("Got It!")
                }
            }
        )
    }

    // ---------------------------------------------------------
    // VICTORY CELEBRATION MODAL
    // ---------------------------------------------------------
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { /* forces user to choose next or replay */ },
            icon = { Text("🎉", fontSize = 42.sp) },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Level ${level.id} Cleared!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Stars
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { index ->
                            Text(
                                text = if (index < earnedStars) "⭐" else "☆",
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = level.funnySuccessComment,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Brain Explanation:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = level.solutionExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        audioManager.playClick(soundEnabled, hapticEnabled)
                        showWinDialog = false
                        if (currentLevelId < LevelCatalog.TOTAL_LEVELS) {
                            progressManager.setCurrentLevel(currentLevelId + 1)
                        } else {
                            onNavigateToStats()
                        }
                    },
                    modifier = Modifier.testTag("next_level_button")
                ) {
                    Text("Next Level ➡️", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        audioManager.playClick(soundEnabled, hapticEnabled)
                        showWinDialog = false
                    },
                    modifier = Modifier.testTag("replay_button")
                ) {
                    Text("Replay")
                }
            }
        )
    }
}
