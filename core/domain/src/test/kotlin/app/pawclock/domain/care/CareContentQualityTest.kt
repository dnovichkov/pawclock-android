package app.pawclock.domain.care

import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

/**
 * Quality-гейт **готовности** care-контента (Plan 3, Task 1).
 *
 * В отличие от [CareAssetsIntegrityTest] (проверяет *структуру и наличие* для ВСЕХ видов — placeholder'ы
 * Plan 2 его уже проходят), этот тест кодирует *definition of done для контента* и применяется ТОЛЬКО
 * к видам из реестра [contentComplete]. Для каждого «готового» вида × стадии × {ru, en} проверяется:
 *  1. ни одно поле не содержит `TODO` (case-insensitive) — решающий признак, что placeholder заменён;
 *  2. `disclaimer` дословно равен канонической строке §3.3 (отдельно для ru/en);
 *  3. содержательность: `stage_description` ≥ [MIN_STAGE_DESCRIPTION], прочие текстовые поля ≥ [MIN_FIELD];
 *  4. `dental_care`: непустой (≥ [MIN_DENTAL]) для всех видов, кроме `fish` (там строго `null`);
 *  5. `source_url` — валидный `http(s)://` URL, `source_name` непустой.
 *
 * **Почему реестр держит suite зелёным** (инвариант ralphex «all tests pass before next task»): пустой
 * реестр в Task 1 порождает ноль динамических тестов; каждая content-задача Tasks 2–13 добавляет свой вид
 * в [contentComplete] ОДНОВРЕМЕННО с авторингом контента → строгие проверки этого вида становятся Green
 * синхронно. Виды не в реестре продолжают проверяться только структурно (в [CareAssetsIntegrityTest]).
 *
 * Task 13 добавит финальную инвариант-проверку `contentComplete == Species.implemented()` (no silent caps).
 *
 * Научную корректность текста этот тест НЕ проверяет — только формальную готовность (см. docs/CARE_CONTENT.md
 * и ADR-0010: контент по published guidelines §14, не заменяет экспертную вычитку).
 */
class CareContentQualityTest {
    /**
     * Реестр видов с готовым (не-placeholder) контентом — **единственный источник истины**.
     * Растёт по одной записи на каждую content-задачу Tasks 2–13. Зеркалится в
     * `scripts/verify-care-content.sh` (`CONTENT_COMPLETE`) и `docs/CARE_CONTENT.md`.
     */
    private val contentComplete: Set<Species> =
        setOf(
            Species.Dog,
            Species.Cat,
            Species.Rabbit,
            Species.Hamster,
            Species.GuineaPig,
            Species.Rat,
            // Task 8+: Species.Mouse, … добавляются здесь по мере авторинга контента.
        )

    /** Канонический дисклеймер §3.3 — дословно, сверен с существующими care-файлами (per-locale). */
    private val canonicalDisclaimer =
        mapOf(
            "ru" to
                "Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача.",
            "en" to
                "This information is for educational purposes only and does not " +
                    "replace consultation with a veterinarian.",
        )

    /** Стадии жизни для каждого вида (источник истины — `LifeStage.*.all()`), как в [CareAssetsIntegrityTest]. */
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

    private val assetsRoot: File = locateAppAssetsRoot()

    private val repository =
        CareRepositoryImpl(
            AssetSource { path -> File(assetsRoot, path).takeIf(File::isFile)?.inputStream() },
        )

    @TestFactory
    fun `content-complete species have real, non-placeholder care content`(): List<DynamicTest> =
        contentComplete.flatMap { species ->
            val stages =
                requireNotNull(stagesBySpecies[species]) {
                    "No LifeStage list mapped for content-complete species ${species.id}"
                }
            stages.flatMap { stage ->
                canonicalDisclaimer.keys.map { locale ->
                    DynamicTest.dynamicTest("${species.id}/${segment(species, stage)}/$locale") {
                        runTest {
                            val recommendation = repository.load(species, stage, locale)
                            assertNotNull(
                                recommendation,
                                "Missing care asset for ${species.id} ${stage.displayKey} ($locale)",
                            )
                            verifyQuality(requireNotNull(recommendation), species, stage, locale)
                        }
                    }
                }
            }
        }

    /** Реестр не должен ссылаться на нереализованные виды (защита от опечаток при расширении). */
    @Test
    fun `registry only references implemented species`() {
        val orphans = contentComplete.filterNot { it in Species.implemented() }
        assertTrue(orphans.isEmpty(), "contentComplete references non-implemented species: ${orphans.map { it.id }}")
    }

    private fun verifyQuality(
        rec: CareRecommendation,
        species: Species,
        stage: LifeStage,
        locale: String,
    ) {
        val where = "${species.id}/${segment(species, stage)}/$locale"

        val fields =
            buildList {
                add("stage_description" to rec.stageDescription)
                add("nutrition" to rec.nutrition)
                add("activity" to rec.activity)
                add("veterinary_check_frequency" to rec.veterinaryCheckFrequency)
                add("warning_signs" to rec.warningSigns)
                add("source_url" to rec.sourceUrl)
                add("source_name" to rec.sourceName)
                add("disclaimer" to rec.disclaimer)
                rec.dentalCare?.let { add("dental_care" to it) }
            }
        fields.forEach { (name, value) ->
            assertFalse(
                value.contains("TODO", ignoreCase = true),
                "$where: field '$name' still contains a TODO placeholder — content not authored",
            )
        }

        val expectedDisclaimer = requireNotNull(canonicalDisclaimer[locale])
        assertTrue(
            rec.disclaimer == expectedDisclaimer,
            "$where: disclaimer must equal the canonical §3.3 text verbatim.\n" +
                "  expected: $expectedDisclaimer\n  actual:   ${rec.disclaimer}",
        )

        assertMinLength(rec.stageDescription, MIN_STAGE_DESCRIPTION, "stage_description", where)
        assertMinLength(rec.nutrition, MIN_FIELD, "nutrition", where)
        assertMinLength(rec.activity, MIN_FIELD, "activity", where)
        assertMinLength(rec.veterinaryCheckFrequency, MIN_FIELD, "veterinary_check_frequency", where)
        assertMinLength(rec.warningSigns, MIN_FIELD, "warning_signs", where)

        if (species == Species.Fish) {
            assertNull(rec.dentalCare, "$where: fish dental_care must be null (not applicable, §3.3)")
        } else {
            val dental = rec.dentalCare
            assertNotNull(dental, "$where: dental_care must be present for non-fish species")
            assertMinLength(requireNotNull(dental), MIN_DENTAL, "dental_care", where)
        }

        assertTrue(
            URL_REGEX.matches(rec.sourceUrl),
            "$where: source_url must be a valid http(s) URL, was: ${rec.sourceUrl}",
        )
        assertTrue(rec.sourceName.isNotBlank(), "$where: source_name must not be blank")
    }

    private fun assertMinLength(
        value: String,
        min: Int,
        field: String,
        where: String,
    ) {
        assertTrue(
            value.trim().length >= min,
            "$where: field '$field' too short (${value.trim().length} < $min chars) — likely a terse placeholder",
        )
    }

    /** Сегмент пути стадии = displayKey без префикса `{species.id}_` (как в [CareRepositoryImpl]). */
    private fun segment(
        species: Species,
        stage: LifeStage,
    ): String = stage.displayKey.removePrefix("${species.id}_")

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

    private companion object {
        /** `stage_description` — 1–2 абзаца по §3.3, поэтому существенно длиннее прочих полей. */
        const val MIN_STAGE_DESCRIPTION = 120

        /** Минимальная длина прочих содержательных текстовых полей. */
        const val MIN_FIELD = 40

        /** Минимальная длина `dental_care`, когда поле применимо (не fish). */
        const val MIN_DENTAL = 30

        val URL_REGEX = Regex("^https?://\\S+$")
    }
}
