package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserProgressManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Brain Test", appName)
    }

    @Test
    fun `user progress persistence works`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = UserProgressManager(context)

        assertEquals(1, manager.currentLevelId.value)
        assertEquals(50, manager.hintBulbs.value)

        manager.completeLevel(1, 3, 2)
        assertTrue(manager.completedLevels.value.contains(1))
        assertEquals(3, manager.levelStars.value[1])
        assertEquals(52, manager.hintBulbs.value)
        assertEquals(2, manager.currentLevelId.value)
    }
}
