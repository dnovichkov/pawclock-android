package app.pawclock.model

/**
 * Вид рыбы по §4.11 спецификации PawClock (PetMD + Kodama Koi Farm + AquariumStoreDepot).
 *
 * Как и у [BirdType] и [ReptileType], [averageLifespanYears] **напрямую участвует в формуле
 * расчёта возраста**: скалярная формула §4.11 `ЧГ = age · 80 / lifespan` масштабируется по
 * видовой ЧЖ (см. [app.pawclock.calculator.FishAgeCalculator]). Короткоживущие гуппи (ЧЖ ~2)
 * стареют «быстрее» в человеческих годах, чем долгоживущие кои (ЧЖ 30+).
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class FishType(
    val id: String,
    val averageLifespanYears: Double,
) {
    Goldfish(id = "goldfish", averageLifespanYears = 15.0),
    Koi(id = "koi", averageLifespanYears = 30.0),
    Betta(id = "betta", averageLifespanYears = 5.0),
    Guppy(id = "guppy", averageLifespanYears = 2.0),
    AngelFish(id = "angelfish", averageLifespanYears = 10.0),
    NeonTetra(id = "neon_tetra", averageLifespanYears = 8.0),
    Tropical(id = "tropical", averageLifespanYears = 5.0),
    Discus(id = "discus", averageLifespanYears = 10.0),
    ;

    companion object {
        fun fromId(id: String): FishType? = entries.firstOrNull { it.id == id }
    }
}
