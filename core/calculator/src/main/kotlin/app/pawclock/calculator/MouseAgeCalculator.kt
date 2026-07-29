// Числовые литералы здесь — границы фаз (в днях) и скорости старения (человеко-дней на
// мышиный день) из Dutta & Sengupta 2016. Каждое значение задокументировано в KDoc-таблице
// ниже с указанием первоисточника §4.6.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Калькулятор возраста мыши в человеческих годах по Dutta & Sengupta 2016
 * (см. §4.6 спецификации PawClock).
 *
 * Кусочная модель «человеко-дней на мышиный день»: возраст переводится в дни, затем по
 * фазам накапливаются эквивалентные человеческие дни (интеграл скорости старения), и сумма
 * делится на 365 для возврата в человеческих годах. Формула едина для всех — у мыши нет
 * подкатегорий.
 *
 * | Фаза (мышиные дни) | Скорость (человеко-дней / мышиный день) |
 * |--------------------|-----------------------------------------|
 * | 0 – 42 (≈6 нед.)   | 150                                     |
 * | 42 – 180           | 45                                      |
 * | 180 – 365          | 30                                      |
 * | 365 – 730          | 25                                      |
 * | > 730              | 20                                      |
 *
 * Опорная точка (из §4.6): 30 дней = 30 · 150 / 365 ≈ 12.33 ЧГ. Накопление непрерывно на
 * всех границах фаз (каждый сегмент стартует с накопленной суммы предыдущего), функция
 * строго монотонно возрастает; скорость старения убывает с возрастом — отражает быстрый
 * рост в первые недели и замедление во взрослой жизни.
 *
 * **Примечание о расхождении в спецификации.** Эталонные значения §4.6 для возрастов
 * > 30 дней внутренне противоречивы (например, «200дн ≈ 21 ЧГ» несовместимо с
 * выведенным «30дн ≈ 12.33 ЧГ» при заданных скоростях). Реализация привязана к единственной
 * математически выведенной опорной точке (30 дней) и кусочной интеграции; тесты используют
 * значения, согласованные с этой моделью. См. план Plan 2, Task 5 (пометка ⚠️).
 *
 * Источник:
 *  - Dutta, S., & Sengupta, P. (2016). *Men and mice: Relating their ages.*
 *    Life Sciences, 152, 244–248. DOI: 10.1016/j.lfs.2015.10.025.
 *
 * См. также спецификацию PawClock §4.6 и ADR-0006.
 */
data object MouseAgeCalculator : AgeCalculator {
    override val species: Species = Species.Mouse

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Mouse].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Mouse) {
            "MouseAgeCalculator requires SpeciesParams.Mouse, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears)
    }

    /**
     * Возвращает возраст мыши в человеческих годах по кусочной модели Dutta & Sengupta 2016.
     *
     * @param ageInYears возраст мыши в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(ageInYears: Double): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val ageInDays = ageInYears * DAYS_PER_YEAR
        val humanDays =
            segment(ageInDays, 0.0, PHASE_1_END) * RATE_PHASE_1 +
                segment(ageInDays, PHASE_1_END, PHASE_2_END) * RATE_PHASE_2 +
                segment(ageInDays, PHASE_2_END, PHASE_3_END) * RATE_PHASE_3 +
                segment(ageInDays, PHASE_3_END, PHASE_4_END) * RATE_PHASE_4 +
                segment(ageInDays, PHASE_4_END, Double.MAX_VALUE) * RATE_PHASE_5
        return humanDays / DAYS_PER_YEAR
    }

    /** Возвращает число мышиных дней, попавших в полуинтервал `(start, end]` для возраста `ageInDays`. */
    private fun segment(
        ageInDays: Double,
        start: Double,
        end: Double,
    ): Double = (minOf(ageInDays, end) - start).coerceAtLeast(0.0)

    /** Число дней в году для конвертации (как в §4.6: 30·150/365). */
    internal const val DAYS_PER_YEAR: Double = 365.0

    /** Конец фазы 1 (≈6 недель, отъём/половая зрелость). */
    internal const val PHASE_1_END: Double = 42.0

    /** Конец фазы 2. */
    internal const val PHASE_2_END: Double = 180.0

    /** Конец фазы 3 (1 год). */
    internal const val PHASE_3_END: Double = 365.0

    /** Конец фазы 4 (2 года). */
    internal const val PHASE_4_END: Double = 730.0

    /** Скорость старения в фазе 1 (0–42 дн.): 150 человеко-дней на мышиный день. */
    internal const val RATE_PHASE_1: Double = 150.0

    /** Скорость старения в фазе 2 (42–180 дн.). */
    internal const val RATE_PHASE_2: Double = 45.0

    /** Скорость старения в фазе 3 (180–365 дн.). */
    internal const val RATE_PHASE_3: Double = 30.0

    /** Скорость старения в фазе 4 (365–730 дн.). */
    internal const val RATE_PHASE_4: Double = 25.0

    /** Скорость старения в фазе 5 (>730 дн.). */
    internal const val RATE_PHASE_5: Double = 20.0
}
