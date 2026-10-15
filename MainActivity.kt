package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.audio.GameAudioManager
import com.example.data.UserProgressManager
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.StatsAndRewardsScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    GAME,
    LEVEL_SELECT,
    STATS_REWARDS
}

class MainActivity : ComponentActivity() {

    private lateinit var audioManager: GameAudioManager
    private lateinit var progressManager: UserProgressManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioManager = GameAudioManager(this)
        progressManager = UserProgressManager(this)

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    progressManager = progressManager,
                    audioManager = audioManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}

@Composable
fun MainAppContent(
    progressManager: UserProgressManager,
    audioManager: GameAudioManager
) {
    var currentScreen by remember { mutableStateOf(Screen.GAME) }
    val soundEnabled by progressManager.soundEnabled.collectAsState()
    val hapticEnabled by progressManager.hapticEnabled.collectAsState()

    // Handle back button smoothly
    BackHandler(enabled = currentScreen != Screen.GAME) {
        currentScreen = Screen.GAME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.GAME,
                    onClick = {
                        audioManager.playClick(soundEnabled, hapticEnabled)
                        currentScreen = Screen.GAME
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
                    label = { Text("Play") },
                    modifier = Modifier.testTag("nav_tab_game")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.LEVEL_SELECT,
                    onClick = {
                        audioManager.playClick(soundEnabled, hapticEnabled)
                        currentScreen = Screen.LEVEL_SELECT
                    },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Levels") },
                    label = { Text("500 Levels") },
                    modifier = Modifier.testTag("nav_tab_levels")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.STATS_REWARDS,
                    onClick = {
                        audioManager.playClick(soundEnabled, hapticEnabled)
                        currentScreen = Screen.STATS_REWARDS
                    },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Brain IQ") },
                    label = { Text("Brain IQ") },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.GAME -> GameScreen(
                    progressManager = progressManager,
                    audioManager = audioManager,
                    onNavigateToLevelSelect = { currentScreen = Screen.LEVEL_SELECT },
                    onNavigateToStats = { currentScreen = Screen.STATS_REWARDS }
                )

                Screen.LEVEL_SELECT -> LevelSelectScreen(
                    progressManager = progressManager,
                    audioManager = audioManager,
                    onNavigateBack = { currentScreen = Screen.GAME },
                    onLevelSelected = { levelId ->
                        progressManager.setCurrentLevel(levelId)
                        currentScreen = Screen.GAME
                    }
                )

                Screen.STATS_REWARDS -> StatsAndRewardsScreen(
                    progressManager = progressManager,
                    audioManager = audioManager,
                    onNavigateBack = { currentScreen = Screen.GAME }
                )
            }
        }
    }
}
