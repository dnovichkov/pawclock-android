package app.pawclock.domain.care

import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * Integration-тест над **реальными** care-ассетами из `:app/src/main/assets/care` (Task 13).
 *
 * В отличие от [CareRepositoryTest] (который подменяет [AssetSource] in-memory fake'ом и проверяет
 * логику path-building/fallback), этот тест прогоняет *настоящий* [CareRepositoryImpl] поверх
 * файлов на диске и убеждается, что:
 *  1. Для каждого `Species.implemented()` × каждой [LifeStage] стадии × {ru, en} файл существует
 *     и десериализуется в валидный [app.pawclock.model.CareRecommendation].
 *  2. Все обязательные поля непусты.
 *  3. Присутствует обязательный disclaimer (§3.3 спецификации) — отдельно для ru и en.
 *
 * Тест был бы **Red** до Task 13 (файлов 10 новых видов не существовало) и становится **Green**
 * после генерации placeholder-ассетов — без изменений в production-коде репозитория
 * (CareRepositoryImpl species-agnostic, см. KDoc к нему).
 *
 * Реальный контент пока placeholder (`TODO(content-pass-after-plan-2)`); этот тест проверяет
 * *структуру и наличие*, а не научную корректность текста (см. docs/CARE_CONTENT.md).
 */
class CareAssetsIntegrityTest {
    /** Подстрока обязательного disclaimer'а §3.3 — отдельно для каждой локали. */
    private val disclaimerSubstring = mapOf("ru" to "не заменяет", "en" to "does not replace")

    /** Стадии жизни для каждого вида (источник истины — `LifeStage.*.all()`). */
    private val stagesBySpecies: Map<Species, List<LifeStage>> =
        mapOf(
            Species.Dog to LifeStage.Dog.all(),
            Species.Cat to LifeStage.Cat.all(),
            Species.Rabbit to LifeStage.Rabbit.all(),
            Species.Hamster to LifeStage.Hamster.all(),
            Species.GuineaPig to LifeStage.GuineaPig.all(),
            Species.Rat to LifeStage.Rat.all(),
            Species.Mouse to LifeStage.Mouse.all(),
            Species.Ferret to LifeStage.Ferret.all(),
            Species.Bird to LifeStage.Bird.all(),
            Species.Reptile to LifeStage.Reptile.all(),
            Species.Horse to LifeStage.Horse.all(),
            Species.Fish to LifeStage.Fish.all(),
        )

    /** Корень `assets` для :app, найденный относительно корня репозитория. */
    private val assetsRoot: File = locateAppAssetsRoot()

    /** [AssetSource] поверх реальной файловой системы (путь вида `care/rabbit/infancy/ru.json`). */
    private val fileSystemSource =
        object : AssetSource {
            override fun open(path: String): InputStream? {
                val file = File(assetsRoot, path)
                return if (file.isFile) file.inputStream() else null
            }
        }

    private val repository = CareRepositoryImpl(fileSystemSource)

    @TestFactory
    fun `every implemented species and stage has valid ru and en care assets`(): List<DynamicTest> =
        Species.implemented().flatMap { species ->
            val stages =
                requireNotNull(stagesBySpecies[species]) {
                    "No LifeStage list mapped for implemented species ${species.id}"
                }
            stages.flatMap { stage ->
                listOf("ru", "en").map { locale ->
                    DynamicTest.dynamicTest("${species.id}/${segment(species, stage)}/$locale") {
                        runTest {
                            val recommendation = repository.load(species, stage, locale)

                            assertNotNull(
                                recommendation,
                                "Missing care asset for ${species.id} ${stage.displayKey} ($locale)",
                            )
                            requireNotNull(recommendation)

                            assertTrue(recommendation.stageDescription.isNotBlank(), "stage_description blank")
                            assertTrue(recommendation.nutrition.isNotBlank(), "nutrition blank")
                            assertTrue(recommendation.activity.isNotBlank(), "activity blank")
                            assertTrue(
                                recommendation.veterinaryCheckFrequency.isNotBlank(),
                                "veterinary_check_frequency blank",
                            )
                            assertTrue(recommendation.warningSigns.isNotBlank(), "warning_signs blank")
                            assertTrue(recommendation.sourceUrl.isNotBlank(), "source_url blank")
                            assertTrue(recommendation.sourceName.isNotBlank(), "source_name blank")

                            val expected = requireNotNull(disclaimerSubstring[locale])
                            assertTrue(
                                recommendation.disclaimer.contains(expected),
                                "Disclaimer ($locale) must contain \"$expected\" per §3.3, " +
                                    "was: ${recommendation.disclaimer}",
                            )
                        }
                    }
                }
            }
        }

    /** Сегмент пути стадии = displayKey без префикса `{species.id}_` (как в [CareRepositoryImpl]). */
    private fun segment(
        species: Species,
        stage: LifeStage,
    ): String = stage.displayKey.removePrefix("${species.id}_")

    /**
     * Поднимается от рабочей директории теста (модуль `core/domain`) вверх до корня репозитория
     * (директория с `settings.gradle.kts`) и резолвит `app/src/main/assets`.
     */
    private fun locateAppAssetsRoot(): File {
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        while (dir != null) {
            if (File(dir, "settings.gradle.kts").isFile) {
                return File(dir, "app/src/main/assets")
            }
            dir = dir.parentFile
        }
        error("Could not locate repository root (settings.gradle.kts) from ${System.getProperty("user.dir")}")
    }
}
