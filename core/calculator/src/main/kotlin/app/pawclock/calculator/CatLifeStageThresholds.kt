// Числовые литералы здесь — табличные пороги начала EndOfLife-стадии по AAHA/AAFP 2021.
// Каждое значение имеет ветеринарный смысл (возраст в годах) и собрано в KDoc.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.CatType

/**
 * Пороги начала EndOfLife-стадии для кошек по типу содержания
 * (AAHA/AAFP 2021, §4.2 спецификации).
 *
 * Возрастные пороги Kitten/YoungAdult/MatureAdult/Senior не зависят от [CatType] —
 * по AAFP они определяются абсолютным возрастом (см. [CatLifeStageCalculator]).
 *
 * А вот EndOfLife наступает при приближении к верхней границе expected lifespan,
 * которая для разных типов сильно различается:
 *
 * | Тип                | Lifespan upper | EndOfLife с | Зазор к смерти |
 * |--------------------|----------------|-------------|----------------|
 * | IndoorShortHair    |       18       |     16      |      ~2 года   |
 * | IndoorLongHair     |       18       |     16      |      ~2 года   |
 * | Outdoor            |        5       |      4      |      ~1 год    |
 * | LargeBreed         |       15       |     13      |      ~2 года   |
 *
 * Для уличных кошек EndOfLife наступает уже в возрасте 4 лет — большинство уличных
 * кошек не доживают до Senior-стадии (которая по AAFP стартует в 11 лет).
 * Это не баг: для конкретного питомца стадия EndOfLife может «перепрыгнуть» через
 * MatureAdult/Senior, что и отражает биологическую реальность.
 *
 * Источники:
 *  - Quimby J. et al. "2021 AAHA/AAFP Feline Life Stage Guidelines",
 *    Journal of Feline Medicine and Surgery, 2021; 23(3):211-233.
 *    DOI: 10.1177/1098612X21993657
 *  - ASPCA / Cornell Feline Health Center: outdoor cat lifespan statistics.
 */
internal object CatLifeStageThresholds {
    internal fun endOfLifeStart(catType: CatType): Double =
        when (catType) {
            CatType.IndoorShortHair, CatType.IndoorLongHair -> INDOOR_END_OF_LIFE_START
            CatType.Outdoor -> OUTDOOR_END_OF_LIFE_START
            CatType.LargeBreed -> LARGE_BREED_END_OF_LIFE_START
        }

    /** Домашние кошки достигают EndOfLife в 16 лет (последние ~2 года из верхней границы 18). */
    private const val INDOOR_END_OF_LIFE_START: Double = 16.0

    /** Уличные кошки достигают EndOfLife в 4 года (последний год до медианной смертности ~5). */
    private const val OUTDOOR_END_OF_LIFE_START: Double = 4.0

    /** Крупные породы (Maine Coon и др.) — в 13 лет (Maine Coon median lifespan ≈ 12.5 года). */
    private const val LARGE_BREED_END_OF_LIFE_START: Double = 13.0
}
