package app.pawclock.model

/**
 * Поддерживаемые виды животных PawClock.
 *
 * Каждый вид имеет:
 *  - стабильный [id] для сериализации в Room/DataStore/JSON care-recommendations;
 *  - флаг [isImplemented], показывающий, реализована ли формула расчёта возраста и стадии жизни.
 *
 * В рамках Plan 1 реализованы только [Dog] и [Cat] (см. §4.1, §4.2 спецификации).
 * Остальные виды объявлены как stubs для совместимости UI и будущей реализации в Plan 2.
 *
 * См. спецификацию PawClock §4 "Поддерживаемые виды животных".
 */
sealed class Species(
    val id: String,
    val isImplemented: Boolean,
) {
    data object Dog : Species(id = "dog", isImplemented = true)

    data object Cat : Species(id = "cat", isImplemented = true)

    data object Rabbit : Species(id = "rabbit", isImplemented = false)

    data object Hamster : Species(id = "hamster", isImplemented = false)

    data object GuineaPig : Species(id = "guinea_pig", isImplemented = false)

    data object Rat : Species(id = "rat", isImplemented = false)

    data object Mouse : Species(id = "mouse", isImplemented = false)

    data object Ferret : Species(id = "ferret", isImplemented = false)

    data object Bird : Species(id = "bird", isImplemented = false)

    data object Reptile : Species(id = "reptile", isImplemented = false)

    data object Horse : Species(id = "horse", isImplemented = false)

    data object Fish : Species(id = "fish", isImplemented = false)

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
