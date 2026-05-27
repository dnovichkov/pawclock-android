@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.DogSize
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки табличного метода AKC/AAHA 2019 (см. §11.6 и §4.1 спецификации).
 *
 * Свойства:
 *  1. **Monotonicity по возрасту:** для каждого [DogSize] функция `age → humanYears` неубывающая.
 *  2. **Биологический инвариант:** при `age > GIANT_VS_SMALL_THRESHOLD` гигантская собака
 *     стареет быстрее малой (по таблице AKC/AAHA в 5+ лет Giant >= Small уже значительно).
 *  3. **Positivity:** все результаты положительные при положительном входе.
 *
 * Эти проверки ловят регрессии:
 *  - случайная перестановка колонок таблицы (Small ↔ Giant);
 *  - некорректная линейная интерполяция (немонотонный наклон);
 *  - сбой экстраполяции за `age > 15`.
 *
 * Источник: American Kennel Club + AAHA, 2019 AAHA Canine Life Stage Guidelines.
 */
class DogAgeCalculatorSizeBasedPropertyTest {
    private val calculator = DogAgeCalculator()

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `size-based result is monotonically non-decreasing in age for every size`() =
        runBlocking {
            DogSize.entries.forEach { size ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loYears = calculator.toHumanYears(lo, size)
                    val hiYears = calculator.toHumanYears(hi, size)
                    assertTrue(
                        loYears <= hiYears,
                        "Size $size: monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                    )
                }
            }
        }

    @Test
    fun `giant dog at age over 5 years has at least as many human years as small dog`() =
        runBlocking {
            // По таблице AKC/AAHA: с 5 лет (Giant=45 vs Small=36) и далее разрыв растёт.
            // Это ключевой биологический инвариант для гигантских пород.
            checkAll(config, Arb.double(GIANT_VS_SMALL_THRESHOLD, MAX_REASONABLE_AGE)) { age ->
                val giant = calculator.toHumanYears(age, DogSize.Giant)
                val small = calculator.toHumanYears(age, DogSize.Small)
                assertTrue(
                    giant >= small,
                    "Giant should age >= Small at age=$age but Giant=$giant, Small=$small",
                )
            }
        }

    @Test
    fun `large dog at age over 6 years has at least as many human years as small dog`() =
        runBlocking {
            // По таблице AKC/AAHA: с 6 лет (Large=45 vs Small=40) и далее.
            checkAll(config, Arb.double(LARGE_VS_SMALL_THRESHOLD, MAX_REASONABLE_AGE)) { age ->
                val large = calculator.toHumanYears(age, DogSize.Large)
                val small = calculator.toHumanYears(age, DogSize.Small)
                assertTrue(
                    large >= small,
                    "Large should age >= Small at age=$age but Large=$large, Small=$small",
                )
            }
        }

    @Test
    fun `size-based result is always strictly positive for positive input`() =
        runBlocking {
            DogSize.entries.forEach { size ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, size)
                    assertTrue(
                        result > 0.0,
                        "Size $size at age=$age expected > 0 but was $result",
                    )
                }
            }
        }

    @Test
    fun `size-based result is bounded above by 250 human years for dogs up to 30 years`() =
        runBlocking {
            // Линейная экстраполяция с наклоном Giant ≈ 7 ЧГ/год даёт ~114 + 7*15 ≈ 219 при age=30.
            // Граница 250 ловит регрессии с переполнением или квадратичным ростом.
            DogSize.entries.forEach { size ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, size)
                    assertTrue(
                        result < UPPER_BOUND_HUMAN_YEARS,
                        "Size $size at age=$age: $result exceeds 250 ЧГ bound",
                    )
                }
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 300
        private const val MIN_AGE = 0.01
        private const val MAX_REASONABLE_AGE = 30.0

        /** Возраст, начиная с которого Giant ≥ Small по AKC/AAHA таблице. */
        private const val GIANT_VS_SMALL_THRESHOLD = 5.0

        /** Возраст, начиная с которого Large ≥ Small по AKC/AAHA таблице. */
        private const val LARGE_VS_SMALL_THRESHOLD = 6.0

        private const val UPPER_BOUND_HUMAN_YEARS = 250.0
    }
}
