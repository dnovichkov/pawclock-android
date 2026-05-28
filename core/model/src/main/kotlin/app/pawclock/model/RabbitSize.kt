package app.pawclock.model

/**
 * Размер (порода) кролика по House Rabbit Society / AVMA (см. §4.3 спецификации).
 *
 * В отличие от [DogSize], размер кролика **не влияет** на формулу расчёта возраста
 * (она едина для всех пород — House Rabbit Society), а используется только для оценки
 * ожидаемой продолжительности жизни ([app.pawclock.calculator.RabbitLifeStageCalculator.expectedLifespanRange]):
 *  - мелкие/карликовые породы живут дольше (до 14 лет),
 *  - гигантские — короче (6–8 лет).
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class RabbitSize(
    val id: String,
) {
    Dwarf(id = "dwarf"),
    Small(id = "small"),
    Medium(id = "medium"),
    Large(id = "large"),
    Giant(id = "giant"),
    ;

    companion object {
        fun fromId(id: String): RabbitSize? = entries.firstOrNull { it.id == id }
    }
}
