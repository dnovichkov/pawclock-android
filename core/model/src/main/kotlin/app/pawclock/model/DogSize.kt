package app.pawclock.model

/**
 * Размер собаки по AKC / AAHA 2019 (см. §4.1 спецификации).
 *
 * Границы веса соответствуют McMillan et al., Scientific Reports 2024 (n=584 734):
 *  - Toy: < 4 кг (включена как стандартная подгруппа малых пород для размер-таблицы)
 *  - Small: 4–9 кг
 *  - Medium: 9–23 кг
 *  - Large: 23–45 кг
 *  - Giant: > 45 кг (старость с 5–6 лет)
 *
 * Используется в `DogAgeCalculator` (SIZE_BASED метод) и в `DogLifeStageCalculator`.
 */
enum class DogSize(
    val id: String,
    val minKg: Double,
    val maxKg: Double?,
) {
    Toy(id = "toy", minKg = 0.0, maxKg = 4.0),
    Small(id = "small", minKg = 4.0, maxKg = 9.0),
    Medium(id = "medium", minKg = 9.0, maxKg = 23.0),
    Large(id = "large", minKg = 23.0, maxKg = 45.0),
    Giant(id = "giant", minKg = 45.0, maxKg = null),
    ;

    companion object {
        fun fromId(id: String): DogSize? = entries.firstOrNull { it.id == id }

        /**
         * Определяет размер по весу. Возвращает null, если вес неположительный.
         *
         * Правило границ: вес `[min, max)` (нижняя граница включается, верхняя — исключается),
         * кроме [Giant], у которой верхняя граница отсутствует.
         */
        fun fromWeight(weightKg: Double): DogSize? {
            if (weightKg <= 0.0) return null
            return entries.firstOrNull { size ->
                val max = size.maxKg
                weightKg >= size.minKg && (max == null || weightKg < max)
            }
        }
    }
}
