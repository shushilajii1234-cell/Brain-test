package com.example

import com.example.data.LevelCatalog
import com.example.model.PuzzleType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun totalLevelsCount_is500() {
        assertEquals(500, LevelCatalog.TOTAL_LEVELS)
    }

    @Test
    fun chaptersCoverAllLevels() {
        assertEquals(10, LevelCatalog.CHAPTER_NAMES.size)
        var total = 0
        for (ch in 1..10) {
            val levels = LevelCatalog.getLevelsForChapter(ch)
            assertEquals(50, levels.size)
            total += levels.size
        }
        assertEquals(500, total)
    }

    @Test
    fun sampleLevels_haveValidContent() {
        // Level 1: Trick tap biggest animal
        val level1 = LevelCatalog.getLevel(1)
        assertEquals(1, level1.id)
        assertEquals(PuzzleType.TRICK_TAP, level1.type)
        assertTrue(level1.tapTargets.any { it.isCorrect })
        assertTrue(level1.hint1.isNotEmpty())
        assertTrue(level1.hint2.isNotEmpty())

        // Level 2: Drag and drop feed kitty
        val level2 = LevelCatalog.getLevel(2)
        assertEquals(2, level2.id)
        assertEquals(PuzzleType.DRAG_AND_DROP, level2.type)
        assertTrue(level2.draggables.any { it.targetDropZoneId != null })

        // Level 500: Grandmaster trial
        val level500 = LevelCatalog.getLevel(500)
        assertEquals(500, level500.id)
        assertEquals(10, level500.chapterIndex)
        assertTrue(level500.question.isNotEmpty())
    }

    @Test
    fun all500Levels_canBeGeneratedWithoutCrashing() {
        for (i in 1..500) {
            val lvl = LevelCatalog.getLevel(i)
            assertEquals(i, lvl.id)
            assertTrue(lvl.question.isNotEmpty())
            assertTrue(lvl.hint1.isNotEmpty())
            assertTrue(lvl.hint2.isNotEmpty())
            assertTrue(lvl.solutionExplanation.isNotEmpty())
        }
    }
}
