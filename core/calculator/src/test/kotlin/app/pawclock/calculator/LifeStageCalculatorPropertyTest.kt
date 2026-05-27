@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [DogLifeStageCalculator] и [CatLifeStageCalculator] (см. §11.6).
 *
 * Ключевое свойство: **стадии не возвращаются назад с возрастом**.
 * Формально: для любых `age1 < age2`, `determine(age1).ordinal ≤ determine(age2).ordinal`.
 *
 * Это критический биологический инвариант: если бы стадия снижалась при росте возраста,
 * пользователь увидел бы «омоложение» собаки или кошки — это всегда баг в порогах.
 *
 * Кейсы, которые свойство покрывает (но табличные тесты могут пропустить):
 *  - случайно переставленные ordinal'ы в LifeStage;
 *  - условия `>=` vs `>` в неверном месте, дающие микро-возврат на границе;
 *  - неверный порядок веток `when {}` (Senior раньше EndOfLife и т.п.).
 *
 * Источники: AAHA 2019 (собаки), AAHA/AAFP 2021 (кошки), DOI: 10.1177/1098612X21993657.
 */
class LifeStageCalculatorPropertyTest {
    private val dogCalculator = DogLifeStageCalculator()
    private val catCalculator = CatLifeStageCalculator()

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `dog life stage ordinal is monotonically non-decreasing in age for every size`() =
        runBlocking {
            DogSize.entries.forEach { size ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_DOG_AGE),
                    Arb.double(MIN_AGE, MAX_DOG_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loStage = dogCalculator.determine(lo, size)
                    val hiStage = dogCalculator.determine(hi, size)
                    assertTrue(
                        loStage.ordinal <= hiStage.ordinal,
                        "Size $size: stage went backward " +
                            "f($lo)=$loStage[${loStage.ordinal}] > f($hi)=$hiStage[${hiStage.ordinal}]",
                    )
                }
            }
        }

    @Test
    fun `cat life stage ordinal is monotonically non-decreasing in age for every cat type`() =
        runBlocking {
            CatType.entries.forEach { type ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_CAT_AGE),
                    Arb.double(MIN_AGE, MAX_CAT_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loStage = catCalculator.determine(lo, type)
                    val hiStage = catCalculator.determine(hi, type)
                    assertTrue(
                        loStage.ordinal <= hiStage.ordinal,
                        "$type: stage went backward " +
                            "f($lo)=$loStage[${loStage.ordinal}] > f($hi)=$hiStage[${hiStage.ordinal}]",
                    )
                }
            }
        }

    @Test
    fun `dog life stage determine throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_DOG_AGE, 0.0)) { age ->
                DogSize.entries.forEach { size ->
                    val result = runCatching { dogCalculator.determine(age, size) }
                    assertTrue(
                        result.isFailure &&
                            result.exceptionOrNull() is IllegalArgumentException,
                        "Expected IAE for size=$size, age=$age, got $result",
                    )
                }
            }
        }

    @Test
    fun `cat life stage determine throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_CAT_AGE, 0.0)) { age ->
                CatType.entries.forEach { type ->
                    val result = runCatching { catCalculator.determine(age, type) }
                    assertTrue(
                        result.isFailure &&
                            result.exceptionOrNull() is IllegalArgumentException,
                        "Expected IAE for type=$type, age=$age, got $result",
                    )
                }
            }
        }

    @Test
    fun `expected lifespan range for dogs is always non-empty and positive`() =
        runBlocking {
            // Не зависит от age — но всё равно property-friendly: проверяем, что для каждого
            // DogSize диапазон корректен (start > 0 && start <= endInclusive).
            DogSize.entries.forEach { size ->
                val range = dogCalculator.expectedLifespanRange(size)
                assertTrue(
                    range.start > 0 && range.start <= range.endInclusive,
                    "Bad lifespan range for $size: $range",
                )
            }
        }

    @Test
    fun `expected lifespan range for cats is always non-empty and positive`() =
        runBlocking {
            CatType.entries.forEach { type ->
                val range = catCalculator.expectedLifespanRange(type)
                assertTrue(
                    range.start > 0 && range.start <= range.endInclusive,
                    "Bad lifespan range for $type: $range",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 400
        private const val MIN_AGE = 0.01
        private const val MAX_DOG_AGE = 25.0
        private const val MAX_CAT_AGE = 25.0
    }
}
