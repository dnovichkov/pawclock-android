package app.pawclock.feature.pets.detail.ui

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Android API level для Robolectric (должен соответствовать кэшированному android-all jar).
// API 30 — как в SCREENSHOT_TEST_SDK у :core:designsystem.
private const val LOCALIZATION_TEST_SDK = 30

/**
 * Тесты локализации меток стадий жизни в `:feature:pets` (Plan 2, Task 22).
 *
 * Проверяют чек-лист Task 22: для каждого `Species.implemented()` × каждой [LifeStage]
 * варианта `lifeStageLabelRes(stage)` возвращает ненулевой resId, и эта строка непуста в
 * обеих локалях (ru/en). Маппинг [stagesFor] — исчерпывающий `when` по [Species], поэтому
 * добавление нового вида в Plan 3 не скомпилируется без перечисления его стадий здесь.
 *
 * Robolectric нужен для загрузки строковых ресурсов (AAPT2) на JVM и переключения локали
 * через `createConfigurationContext`. Зеркалит подход `SpeciesIconTest` в `:core:designsystem`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [LOCALIZATION_TEST_SDK])
class LifeStageLabelLocalizationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val allStages: List<LifeStage> =
        Species.implemented().flatMap { stagesFor(it) }

    @Test
    fun everyLifeStageHasNonZeroLabelResource() {
        allStages.forEach { stage ->
            assertNotEquals(
                "Нет строки-метки для стадии ${stage.displayKey}",
                0,
                lifeStageLabelRes(stage),
            )
        }
    }

    @Test
    fun lifeStageLabelResourcesAreDistinctPerStage() {
        val resIds = allStages.map { lifeStageLabelRes(it) }
        assertEquals(
            "Каждая стадия должна иметь собственный строковый ресурс (нет copy-paste коллизий)",
            resIds.size,
            resIds.toSet().size,
        )
    }

    @Test
    fun everyLifeStageLabelResolvesToNonBlankStringInBothLocales() {
        allStages.forEach { stage ->
            LOCALES.forEach { locale ->
                val label = stringIn(locale, lifeStageLabelRes(stage))
                assertTrue(
                    "Пустая метка стадии ${stage.displayKey} в локали ${locale.language}",
                    label.isNotBlank(),
                )
            }
        }
    }

    private fun stringIn(
        locale: Locale,
        resId: Int,
    ): String {
        val configuration =
            Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(configuration).getString(resId)
    }

    private fun stagesFor(species: Species): List<LifeStage> =
        when (species) {
            Species.Dog -> LifeStage.Dog.all()
            Species.Cat -> LifeStage.Cat.all()
            Species.Rabbit -> LifeStage.Rabbit.all()
            Species.Hamster -> LifeStage.Hamster.all()
            Species.GuineaPig -> LifeStage.GuineaPig.all()
            Species.Rat -> LifeStage.Rat.all()
            Species.Mouse -> LifeStage.Mouse.all()
            Species.Ferret -> LifeStage.Ferret.all()
            Species.Bird -> LifeStage.Bird.all()
            Species.Reptile -> LifeStage.Reptile.all()
            Species.Horse -> LifeStage.Horse.all()
            Species.Fish -> LifeStage.Fish.all()
        }

    private companion object {
        val LOCALES = listOf(Locale("ru"), Locale("en"))
    }
}
