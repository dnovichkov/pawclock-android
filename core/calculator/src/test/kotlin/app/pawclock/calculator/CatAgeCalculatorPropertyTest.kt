@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.CatType
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [CatAgeCalculator] по AAHA/AAFP 2021 (см. §11.6 спецификации).
 *
 * Свойства:
 *  1. **Monotonicity по возрасту:** для каждого [CatType] функция `age → humanYears` неубывающая.
 *  2. **Outdoor ≥ Indoor после 2 лет:** уличная кошка стареет быстрее домашней (множитель 1.15).
 *  3. **LargeBreed ≥ IndoorShortHair после 2 лет:** Мейн-Куны и др. стареют чуть быстрее
 *     (поправка +1 ЧГ/год после 2-х лет).
 *  4. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Эти инварианты ловят регрессии:
 *  - неправильный порядок применения поправок (× 1.15 + (age−2) vs (1.15 × (× + (age−2)))),
 *  - забытое условие `age > 2` для поправок (создавало бы скачок в age=2),
 *  - перепутанные флаги isOutdoor / isLargeBreed в CatType.
 *
 * Источник: Quimby J. et al. 2021 AAHA/AAFP Feline Life Stage Guidelines,
 * DOI: 10.1177/1098612X21993657.
 */
class CatAgeCalculatorPropertyTest {
    private val calculator = CatAgeCalculator()

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `cat age result is monotonically non-decreasing for every cat type`() =
        runBlocking {
            CatType.entries.forEach { type ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loYears = calculator.toHumanYears(lo, type)
                    val hiYears = calculator.toHumanYears(hi, type)
                    assertTrue(
                        loYears <= hiYears,
                        "$type: monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                    )
                }
            }
        }

    @Test
    fun `outdoor cat at age over 2 years ages at least as fast as indoor short hair`() =
        runBlocking {
            checkAll(config, Arb.double(POST_BOUNDARY_AGE, MAX_REASONABLE_AGE)) { age ->
                val outdoor = calculator.toHumanYears(age, CatType.Outdoor)
                val indoor = calculator.toHumanYears(age, CatType.IndoorShortHair)
                assertTrue(
                    outdoor >= indoor,
                    "Outdoor should be >= Indoor at age=$age but Outdoor=$outdoor, Indoor=$indoor",
                )
            }
        }

    @Test
    fun `large breed cat at age over 2 years ages at least as fast as indoor short hair`() =
        runBlocking {
            checkAll(config, Arb.double(POST_BOUNDARY_AGE, MAX_REASONABLE_AGE)) { age ->
                val large = calculator.toHumanYears(age, CatType.LargeBreed)
                val indoor = calculator.toHumanYears(age, CatType.IndoorShortHair)
                assertTrue(
                    large >= indoor,
                    "LargeBreed should be >= Indoor at age=$age but L=$large, I=$indoor",
                )
            }
        }

    @Test
    fun `cat age result is always strictly positive for positive input`() =
        runBlocking {
            CatType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(result > 0.0, "$type at age=$age: $result not positive")
                }
            }
        }

    @Test
    fun `cat age result is bounded above by 200 human years for cats up to 30 years`() =
        runBlocking {
            // При age=30, Outdoor: (24 + 4·28) · 1.15 = 156.4. Граница 200 ловит регрессии.
            CatType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(
                        result < UPPER_BOUND_HUMAN_YEARS,
                        "$type at age=$age: $result exceeds 200 ЧГ",
                    )
                }
            }
        }

    @Test
    fun `cat age result on the kitten interval scales linearly to 15 at age 1`() =
        runBlocking {
            // Property: для age в (0, 1], результат = 15·age (линейная интерполяция к 15 ЧГ).
            // Поправки на этом интервале не применяются (age <= 2), значит все типы равны.
            checkAll(config, Arb.double(MIN_AGE, KITTEN_UPPER)) { age ->
                CatType.entries.forEach { type ->
                    val expected = FIRST_YEAR_HUMAN_AGE * age
                    val actual = calculator.toHumanYears(age, type)
                    assertTrue(
                        kotlin.math.abs(actual - expected) < KITTEN_TOLERANCE,
                        "$type at age=$age: expected $expected ± $KITTEN_TOLERANCE, got $actual",
                    )
                }
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 300
        private const val MIN_AGE = 0.01
        private const val MAX_REASONABLE_AGE = 30.0

        /** Граница, после которой включаются поправки isOutdoor / isLargeBreed. */
        private const val POST_BOUNDARY_AGE = 2.01

        private const val KITTEN_UPPER = 1.0
        private const val FIRST_YEAR_HUMAN_AGE = 15.0
        private const val KITTEN_TOLERANCE = 1.0e-9

        private const val UPPER_BOUND_HUMAN_YEARS = 200.0
    }
}
