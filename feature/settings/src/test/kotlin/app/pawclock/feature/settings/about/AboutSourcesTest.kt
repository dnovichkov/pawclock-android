package app.pawclock.feature.settings.about

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Тесты содержимого [AboutSources] (Task 21 / Plan 1).
 *
 * Цель — защититься от случайного удаления критичных научных ссылок при будущих
 * правках. AboutSources — это публичная attribution-страница приложения, и
 * исчезновение любого из источников ослабляет educational angle (§3.2 спецификации).
 *
 * Каждый источник проверяется по ключевому маркеру: DOI для рецензируемых публикаций,
 * имя организации для guidelines.
 */
class AboutSourcesTest {
    @Test
    fun `at least four sources are exposed`() {
        // Plan 1 покрывает 4 источника (Wang, AKC/AAHA dogs, AAFP cats, McMillan lifespan).
        assertTrue(
            AboutSources.all.size >= 4,
            "Plan 1 должен содержать как минимум 4 научных источника, найдено: ${AboutSources.all.size}",
        )
    }

    @Test
    fun `Wang 2020 DOI is present (epigenetic dog formula)`() {
        val wang = AboutSources.all.firstOrNull { it.authors.startsWith("Wang") }
        assertTrue(wang != null, "Wang T. et al. 2020 должен быть в списке источников")
        assertTrue(
            wang!!.reference.contains("10.1016/j.cels.2020.06.006"),
            "Wang DOI должен совпадать с published Cell Systems 2020",
        )
        assertEquals(2020, wang.year)
    }

    @Test
    fun `AAHA 2019 canine guidelines are present`() {
        val aaha = AboutSources.all.firstOrNull { it.title.contains("AAHA Canine") }
        assertTrue(aaha != null, "AAHA 2019 Canine Life Stage Guidelines должны быть в списке")
        assertEquals(2019, aaha!!.year)
    }

    @Test
    fun `AAFP 2021 feline DOI is present`() {
        val aafp = AboutSources.all.firstOrNull { it.title.contains("Feline") }
        assertTrue(aafp != null, "AAHA/AAFP 2021 Feline Life Stage Guidelines должны быть в списке")
        assertTrue(
            aafp!!.reference.contains("10.1177/1098612X21993657"),
            "AAFP DOI должен совпадать с published Journal of Feline Medicine and Surgery 2021",
        )
        assertEquals(2021, aafp.year)
    }

    @Test
    fun `McMillan 2024 lifespan reference is present`() {
        val mcmillan = AboutSources.all.firstOrNull { it.authors.startsWith("McMillan") }
        assertTrue(mcmillan != null, "McMillan et al. 2024 lifespan-source должен быть в списке")
        assertEquals(2024, mcmillan!!.year)
    }

    @Test
    fun `all sources have non-blank fields`() {
        AboutSources.all.forEach { source ->
            assertTrue(source.title.isNotBlank(), "title must be non-blank: $source")
            assertTrue(source.authors.isNotBlank(), "authors must be non-blank: $source")
            assertTrue(source.reference.isNotBlank(), "reference must be non-blank: $source")
            assertTrue(source.year in MIN_REASONABLE_YEAR..MAX_REASONABLE_YEAR, "implausible year: $source")
        }
    }

    private companion object {
        const val MIN_REASONABLE_YEAR: Int = 1990
        const val MAX_REASONABLE_YEAR: Int = 2100
    }
}
