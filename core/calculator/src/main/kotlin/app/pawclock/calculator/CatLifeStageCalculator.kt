// Числовые литералы здесь — табличные пороги стадий жизни кошек из AAHA/AAFP 2021.
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.CatType
import app.pawclock.model.LifeStage

/**
 * Калькулятор стадии жизни кошки по возрасту и типу содержания.
 *
 * Использует пороги AAHA/AAFP 2021 Feline Life Stage Guidelines (§4.2 спецификации):
 *
 * | Стадия       | Возраст начала                                                          |
 * |--------------|-------------------------------------------------------------------------|
 * | Kitten       | 0 — 1 год (граница 1.0 включительно в Kitten)                           |
 * | YoungAdult   | 1 — 7 лет                                                               |
 * | MatureAdult  | 7 — 11 лет (включает граничное 10.x; см. примечание ниже)               |
 * | Senior       | 11+ лет                                                                 |
 * | EndOfLife    | Indoor ≈ 16, Outdoor ≈ 4, LargeBreed ≈ 13 (см. [CatLifeStageThresholds])|
 *
 * **Примечание о пороге Senior.** AAFP 2021 формально определяет Senior как «10+ лет».
 * В этом коде Senior стартует в 11 лет — это соответствует «началу 11-го года жизни»
 * и согласуется с тестовым требованием спецификации `10 years → MatureAdult`.
 * Кошка в свой 10-й день рождения ещё не завершила Senior-переход.
 *
 * **Примечание об EndOfLife для уличных кошек.** EndOfLife для Outdoor наступает в 4 года —
 * это ниже порога MatureAdult (7) и YoungAdult-окна (1–7). Поэтому уличная кошка в 5 лет
 * «перепрыгивает» из YoungAdult в EndOfLife (без MatureAdult/Senior). Это отражает то,
 * что большинство уличных кошек не доживают до Senior. См. [CatLifeStageThresholds]
 * за обоснованием.
 *
 * Источники:
 *  - Quimby J., Gowland S., Carney H.C., DePorter T., Plummer P., Westropp J.
 *    "2021 AAHA/AAFP Feline Life Stage Guidelines".
 *    Journal of Feline Medicine and Surgery, 2021; 23(3):211-233.
 *    DOI: 10.1177/1098612X21993657
 *
 * См. также спецификацию PawClock §4.2 и ADR-0006.
 */
class CatLifeStageCalculator {
    /**
     * Возвращает стадию жизни кошки по возрасту и типу содержания.
     *
     * @param ageInYears возраст кошки в годах (должен быть > 0).
     * @param catType тип содержания, влияет только на порог EndOfLife.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(
        ageInYears: Double,
        catType: CatType,
    ): LifeStage.Cat {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val endOfLifeStart = CatLifeStageThresholds.endOfLifeStart(catType)
        return when {
            ageInYears >= endOfLifeStart -> LifeStage.Cat.EndOfLife
            ageInYears >= SENIOR_START -> LifeStage.Cat.Senior
            ageInYears >= MATURE_ADULT_START -> LifeStage.Cat.MatureAdult
            ageInYears > KITTEN_UPPER_BOUND -> LifeStage.Cat.YoungAdult
            else -> LifeStage.Cat.Kitten
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для типа кошки.
     *
     * Значения по AAFP 2021 / ASPCA / Cornell Feline Health Center:
     *  - IndoorShortHair, IndoorLongHair: 12–18 лет
     *  - Outdoor: 2–5 лет (короче из-за травм, инфекций, хищников)
     *  - LargeBreed (Maine Coon и др.): 12–15 лет (Maine Coon median ≈ 12.5)
     *
     * @param catType тип кошки.
     */
    fun expectedLifespanRange(catType: CatType): ClosedFloatingPointRange<Double> =
        when (catType) {
            CatType.IndoorShortHair, CatType.IndoorLongHair -> 12.0..18.0
            CatType.Outdoor -> 2.0..5.0
            CatType.LargeBreed -> 12.0..15.0
        }

    internal companion object {
        /**
         * Верхняя граница стадии Kitten (включительно): 1.0 год.
         * Спецификация PawClock §4.2: `Kitten = 0–1`, в т.ч. ровно 1 год.
         * Реализация использует строгое сравнение `age > KITTEN_UPPER_BOUND`,
         * чтобы 1.0 попадал в Kitten.
         */
        internal const val KITTEN_UPPER_BOUND: Double = 1.0

        /**
         * Возраст начала MatureAdult-стадии — по AAFP 2021 «Mature Adult: 7–10 years».
         */
        internal const val MATURE_ADULT_START: Double = 7.0

        /**
         * Возраст начала Senior-стадии — AAFP 2021 «Senior: 10+».
         * Сдвинут на 11.0 для соответствия требованию `10 years → MatureAdult`
         * (см. KDoc класса).
         */
        internal const val SENIOR_START: Double = 11.0
    }
}
