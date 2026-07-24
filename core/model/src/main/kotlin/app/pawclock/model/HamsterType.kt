package app.pawclock.model

/**
 * Вид хомяка по §4.4 спецификации PawClock (RVC VetCompass + Animallama).
 *
 * Подобно [RabbitSize], вид хомяка **не влияет** на кусочную формулу расчёта возраста
 * (она едина для всех видов), а используется для оценки ожидаемой продолжительности
 * жизни ([app.pawclock.calculator.HamsterLifeStageCalculator.expectedLifespanRange]).
 *
 * [averageLifespanYears] — средняя продолжительность жизни вида в годах
 * (ЧЖ 1.5–3.5 года в зависимости от вида, §4.4): Roborovski живут дольше всех,
 * карликовые (Dwarf/WinterWhite) — меньше.
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class HamsterType(
    val id: String,
    val averageLifespanYears: Double,
) {
    Syrian(id = "syrian", averageLifespanYears = 2.5),
    Dwarf(id = "dwarf", averageLifespanYears = 2.0),
    Roborovski(id = "roborovski", averageLifespanYears = 3.25),
    Chinese(id = "chinese", averageLifespanYears = 2.5),
    WinterWhite(id = "winter_white", averageLifespanYears = 2.0),
    ;

    companion object {
        fun fromId(id: String): HamsterType? = entries.firstOrNull { it.id == id }
    }
}
