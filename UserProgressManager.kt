package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProgressManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("brain_test_progress_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_LEVEL_STARS = "level_stars_map"
        private const val KEY_CURRENT_LEVEL = "current_level_id"
        private const val KEY_HINT_BULBS = "hint_bulbs_count"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_LAST_REWARD_TIME = "last_reward_time"
        private const val KEY_TOTAL_HINTS_USED = "total_hints_used"

        private const val INITIAL_BULBS = 50
    }

    private val _completedLevels = MutableStateFlow<Set<Int>>(emptySet())
    val completedLevels: StateFlow<Set<Int>> = _completedLevels.asStateFlow()

    private val _levelStars = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val levelStars: StateFlow<Map<Int, Int>> = _levelStars.asStateFlow()

    private val _currentLevelId = MutableStateFlow(1)
    val currentLevelId: StateFlow<Int> = _currentLevelId.asStateFlow()

    private val _hintBulbs = MutableStateFlow(INITIAL_BULBS)
    val hintBulbs: StateFlow<Int> = _hintBulbs.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val completedSet = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        _completedLevels.value = completedSet

        val starsRaw = prefs.getString(KEY_LEVEL_STARS, "") ?: ""
        val starsMap = mutableMapOf<Int, Int>()
        if (starsRaw.isNotEmpty()) {
            starsRaw.split(";").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val lvl = parts[0].toIntOrNull()
                    val st = parts[1].toIntOrNull()
                    if (lvl != null && st != null) {
                        starsMap[lvl] = st
                    }
                }
            }
        }
        _levelStars.value = starsMap

        _currentLevelId.value = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        _hintBulbs.value = prefs.getInt(KEY_HINT_BULBS, INITIAL_BULBS)
        _soundEnabled.value = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        _hapticEnabled.value = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
    }

    fun completeLevel(levelId: Int, starsEarned: Int, bulbsReward: Int = 2) {
        val newCompleted = _completedLevels.value + levelId
        _completedLevels.value = newCompleted

        val currentStars = _levelStars.value[levelId] ?: 0
        val bestStars = maxOf(currentStars, starsEarned.coerceIn(1, 3))
        val newStarsMap = _levelStars.value + (levelId to bestStars)
        _levelStars.value = newStarsMap

        // Progress current level if this was the latest
        if (levelId == _currentLevelId.value && levelId < LevelCatalog.TOTAL_LEVELS) {
            _currentLevelId.value = levelId + 1
            prefs.edit().putInt(KEY_CURRENT_LEVEL, _currentLevelId.value).apply()
        }

        // Award bulbs
        addBulbs(bulbsReward)

        // Save
        val stringSet = newCompleted.map { it.toString() }.toSet()
        val starsSerialized = newStarsMap.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit()
            .putStringSet(KEY_COMPLETED_LEVELS, stringSet)
            .putString(KEY_LEVEL_STARS, starsSerialized)
            .apply()
    }

    fun setCurrentLevel(levelId: Int) {
        val clamped = levelId.coerceIn(1, LevelCatalog.TOTAL_LEVELS)
        _currentLevelId.value = clamped
        prefs.edit().putInt(KEY_CURRENT_LEVEL, clamped).apply()
    }

    fun addBulbs(amount: Int) {
        val updated = (_hintBulbs.value + amount).coerceAtLeast(0)
        _hintBulbs.value = updated
        prefs.edit().putInt(KEY_HINT_BULBS, updated).apply()
    }

    fun consumeBulbs(amount: Int): Boolean {
        if (_hintBulbs.value >= amount) {
            _hintBulbs.value -= amount
            val totalUsed = prefs.getInt(KEY_TOTAL_HINTS_USED, 0) + amount
            prefs.edit()
                .putInt(KEY_HINT_BULBS, _hintBulbs.value)
                .putInt(KEY_TOTAL_HINTS_USED, totalUsed)
                .apply()
            return true
        }
        return false
    }

    fun toggleSound(): Boolean {
        val newValue = !_soundEnabled.value
        _soundEnabled.value = newValue
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, newValue).apply()
        return newValue
    }

    fun toggleHaptic(): Boolean {
        val newValue = !_hapticEnabled.value
        _hapticEnabled.value = newValue
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, newValue).apply()
        return newValue
    }

    fun canClaimDailyReward(): Boolean {
        val lastTime = prefs.getLong(KEY_LAST_REWARD_TIME, 0L)
        val now = System.currentTimeMillis()
        // Allow once every 4 hours for high testability & fun
        return (now - lastTime) >= (4 * 60 * 60 * 1000L)
    }

    fun claimReward(amount: Int) {
        addBulbs(amount)
        prefs.edit().putLong(KEY_LAST_REWARD_TIME, System.currentTimeMillis()).apply()
    }

    fun getUserStats(): UserStats {
        val completed = _completedLevels.value.size
        val stars = _levelStars.value.values.sum()
        val hints = prefs.getInt(KEY_TOTAL_HINTS_USED, 0)
        val baseIq = 70
        val iq = (baseIq + (completed * 0.4) + (stars * 0.15)).toInt().coerceIn(70, 260)

        val title = when (completed) {
            in 0..10 -> "Curious Amoeba 🦠"
            in 11..30 -> "Cheeky Chimp 🐵"
            in 31..60 -> "Clever Fox 🦊"
            in 61..100 -> "Sharp Detective 🕵️"
            in 101..175 -> "Puzzle Professor 🎓"
            in 176..250 -> "Mastermind Thinker 🧠"
            in 251..350 -> "Quantum Strategist ⚛️"
            in 351..450 -> "Cosmic Enigma 🌌"
            else -> "Galaxy Brain God 👑"
        }

        return UserStats(
            completedCount = completed,
            totalStars = stars,
            hintBulbs = _hintBulbs.value,
            currentLevelId = _currentLevelId.value,
            iqScore = iq,
            iqTitle = title,
            hintsUsedTotal = hints
        )
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        if (_completedLevels.value.contains(levelId)) return true
        if (_completedLevels.value.contains(levelId - 1)) return true
        return false
    }
}
