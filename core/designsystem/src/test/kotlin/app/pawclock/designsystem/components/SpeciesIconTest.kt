package app.pawclock.designsystem.components

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import app.pawclock.designsystem.SCREENSHOT_TEST_SDK
import app.pawclock.model.Species
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты маппинга [speciesIconRes] и доступности drawable-ассетов `ic_species_*`.
 *
 * Покрывают чек-лист Task 14:
 *  - все 12 видов имеют не-null painter (через резолв drawable из реальных ресурсов модуля);
 *  - маппинг уникален (нет двух видов с одной иконкой);
 *  - маппинг исчерпывающий по `Species.all()`.
 *
 * Robolectric нужен для загрузки Android-ресурсов (AAPT2) на JVM; рендер-проверка самого
 * composable выполняется в opt-in [SpeciesIconScreenshotTest] (captureRoboImage).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [SCREENSHOT_TEST_SDK])
class SpeciesIconTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun everySpeciesResolvesToNonNullDrawable() {
        Species.all().forEach { species ->
            val resId = speciesIconRes(species)
            assertNotEquals("Нет drawable для вида ${species.id}", 0, resId)
            val drawable = ResourcesCompat.getDrawable(context.resources, resId, null)
            assertNotNull("Drawable не загрузился для вида ${species.id}", drawable)
        }
    }

    @Test
    fun iconMappingIsUnique() {
        val resIds = Species.all().map { speciesIconRes(it) }
        assertEquals("Иконки видов должны быть уникальны", resIds.size, resIds.toSet().size)
    }

    @Test
    fun iconMappingCoversAllTwelveSpecies() {
        assertEquals(12, Species.all().size)
        assertEquals(12, Species.all().map { speciesIconRes(it) }.size)
    }
}
