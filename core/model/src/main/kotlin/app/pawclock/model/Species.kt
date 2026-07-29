package app.pawclock.model

/**
 * Поддерживаемые виды животных PawClock.
 *
 * Каждый вид имеет:
 *  - стабильный [id] для сериализации в Room/DataStore/JSON care-recommendations;
 *  - флаг [isImplemented], показывающий, реализована ли формула расчёта возраста и стадии жизни.
 *
 * После Plan 2 (Task 10) реализованы все 12 видов — полный MVP v1.0 (§12.1): у каждого есть
 * формула расчёта возраста ([app.pawclock.calculator.AgeCalculator]) и стадий жизни
 * ([app.pawclock.calculator.LifeStageCalculator]). Флаг [isImplemented] сохранён для совместимости
 * API и на случай добавления новых видов-stub в будущих планах.
 *
 * См. спецификацию PawClock §4 "Поддерживаемые виды животных".
 */
sealed class Species(
    val id: String,
    val isImplemented: Boolean,
) {
    data object Dog : Species(id = "dog", isImplemented = true)

    data object Cat : Species(id = "cat", isImplemented = true)

    data object Rabbit : Species(id = "rabbit", isImplemented = true)

    data object Hamster : Species(id = "hamster", isImplemented = true)

    data object GuineaPig : Species(id = "guinea_pig", isImplemented = true)

    data object Rat : Species(id = "rat", isImplemented = true)

    data object Mouse : Species(id = "mouse", isImplemented = true)

    data object Ferret : Species(id = "ferret", isImplemented = true)

    data object Bird : Species(id = "bird", isImplemented = true)

    data object Reptile : Species(id = "reptile", isImplemented = true)

    data object Horse : Species(id = "horse", isImplemented = true)

    data object Fish : Species(id = "fish", isImplemented = true)

    companion object {
        fun all(): List<Species> =
            listOf(Dog, Cat, Rabbit, Hamster, GuineaPig, Rat, Mouse, Ferret, Bird, Reptile, Horse, Fish)

        fun implemented(): List<Species> = all().filter { it.isImplemented }

        fun fromString(value: String): Species? {
            val normalized = value.lowercase()
            return all().firstOrNull { it.id == normalized }
        }
    }
}
