package app.pawclock.feature.editor.ui.section

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import app.pawclock.feature.editor.PetEditorState
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
 * Тесты локализации имён видов и подкатегорий в `:feature:editor` (Plan 2, Task 22).
 *
 * Проверяют чек-лист Task 22:
 *  - каждый реализованный вид имеет собственную (не fallback) строку-метку — до Task 22
 *    `speciesLabelRes` возвращал `pet_editor_species_dog` для 10 новых видов, из-за чего
 *    они отображались как «Собака»; тест на уникальность resId ловит этот регресс;
 *  - метки видов и подкатегорий резолвятся в непустые строки в обеих локалях (ru/en) —
 *    отсутствие ключа в `values-en/` дало бы пустой fallback.
 *
 * Robolectric нужен для загрузки строковых ресурсов (AAPT2) на JVM и переключения локали
 * через `createConfigurationContext`. Зеркалит подход `SpeciesIconTest` в `:core:designsystem`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [LOCALIZATION_TEST_SDK])
class SpeciesLocalizationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun everyImplementedSpeciesHasDistinctNonZeroNameResource() {
        val resIds = Species.implemented().map { speciesLabelRes(it) }

        resIds.forEachIndexed { index, resId ->
            assertNotEquals(
                "Нет строки-метки для вида ${Species.implemented()[index].id}",
                0,
                resId,
            )
        }
        assertEquals(
            "Метки видов должны быть уникальны (нет fallback на species_dog)",
            resIds.size,
            resIds.toSet().size,
        )
    }

    @Test
    fun everySpeciesNameResolvesToNonBlankStringInBothLocales() {
        Species.implemented().forEach { species ->
            LOCALES.forEach { locale ->
                val label = stringIn(locale, speciesLabelRes(species))
                assertTrue(
                    "Пустая метка вида ${species.id} в локали ${locale.language}",
                    label.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun everySubcategoryIdResolvesToNonBlankLabelInBothLocales() {
        Species.implemented().forEach { species ->
            PetEditorState.subcategoriesFor(species).forEach { option ->
                val resId = subcategoryLabelRes(species, option.id)
                assertNotEquals(
                    "Нет строки для подкатегории ${species.id}/${option.id}",
                    0,
                    resId,
                )
                LOCALES.forEach { locale ->
                    assertTrue(
                        "Пустая метка ${species.id}/${option.id} в локали ${locale.language}",
                        stringIn(locale, resId).isNotBlank(),
                    )
                }
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

    private companion object {
        val LOCALES = listOf(Locale("ru"), Locale("en"))
    }
}
