package app.pawclock.model

/**
 * Пол питомца. Опциональное поле — некоторые виды (рыбы, рептилии) могут иметь Unknown.
 */
enum class Gender(
    val id: String,
) {
    Male(id = "male"),
    Female(id = "female"),
    Unknown(id = "unknown"),
    ;

    companion object {
        fun fromId(id: String): Gender? = entries.firstOrNull { it.id == id }
    }
}
