package app.pawclock.model

/**
 * Вид птицы по §4.8 спецификации PawClock (AAV «Care for Senior Parrots» + Lafeber Vet).
 *
 * В отличие от [HamsterType] (где вид влияет лишь на оценку продолжительности жизни),
 * у птиц [averageLifespanYears] **напрямую участвует в формуле расчёта возраста**:
 * скалярная формула §4.8 `ЧГ = age · 80 / lifespan` масштабируется по видовой ЧЖ
 * (см. [app.pawclock.calculator.BirdAgeCalculator]). Долгоживущие попугаи (амазоны, ара,
 * жако) стареют «медленнее» в человеческих годах, чем короткоживущие волнистые.
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class BirdType(
    val id: String,
    val averageLifespanYears: Double,
) {
    Budgerigar(id = "budgerigar", averageLifespanYears = 7.0),
    Cockatiel(id = "cockatiel", averageLifespanYears = 15.0),
    Canary(id = "canary", averageLifespanYears = 10.0),
    Lovebird(id = "lovebird", averageLifespanYears = 12.0),
    Conure(id = "conure", averageLifespanYears = 20.0),
    Amazon(id = "amazon", averageLifespanYears = 50.0),
    AfricanGrey(id = "african_grey", averageLifespanYears = 50.0),
    Cockatoo(id = "cockatoo", averageLifespanYears = 50.0),
    Macaw(id = "macaw", averageLifespanYears = 50.0),
    Pigeon(id = "pigeon", averageLifespanYears = 10.0),
    ;

    companion object {
        fun fromId(id: String): BirdType? = entries.firstOrNull { it.id == id }
    }
}
