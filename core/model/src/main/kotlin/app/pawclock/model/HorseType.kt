package app.pawclock.model

/**
 * Тип/группа лошади по §4.10 спецификации PawClock (AAEP + PetMD).
 *
 * Как и у [HamsterType] / [RabbitSize], [type] **не влияет на формулу расчёта возраста**:
 * 3-фазная формула AAEP §4.10 едина для всех лошадей (см. [app.pawclock.calculator.HorseAgeCalculator]).
 * [averageLifespanYears] используется только для оценки ожидаемой продолжительности жизни —
 * пони традиционно живут дольше, тяжеловозы — меньше.
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class HorseType(
    val id: String,
    val averageLifespanYears: Double,
) {
    Pony(id = "pony", averageLifespanYears = 30.0),
    LightHorse(id = "light_horse", averageLifespanYears = 28.0),
    DraftHorse(id = "draft_horse", averageLifespanYears = 25.0),
    Thoroughbred(id = "thoroughbred", averageLifespanYears = 28.0),
    ;

    companion object {
        fun fromId(id: String): HorseType? = entries.firstOrNull { it.id == id }
    }
}
