package app.pawclock.model

/**
 * Вид рептилии по §4.9 спецификации PawClock (PetPlace + Reptile Centre + A-Z Animals).
 *
 * Как и у [BirdType], [averageLifespanYears] **напрямую участвует в формуле расчёта возраста**:
 * скалярная формула §4.9 `ЧГ = age · 80 / lifespan` масштабируется по видовой ЧЖ
 * (см. [app.pawclock.calculator.ReptileAgeCalculator]). Долгоживущие черепахи стареют
 * «медленнее» в человеческих годах, чем короткоживущие гекконы.
 *
 * Стабильный [id] используется для сериализации в Room/JSON (см. `PetMapper`, экспорт/импорт).
 */
enum class ReptileType(
    val id: String,
    val averageLifespanYears: Double,
) {
    BoxTurtle(id = "box_turtle", averageLifespanYears = 40.0),
    RedEaredSlider(id = "red_eared_slider", averageLifespanYears = 30.0),
    BeardedDragon(id = "bearded_dragon", averageLifespanYears = 12.0),
    BallPython(id = "ball_python", averageLifespanYears = 30.0),
    CornSnake(id = "corn_snake", averageLifespanYears = 20.0),
    GreenIguana(id = "green_iguana", averageLifespanYears = 20.0),
    LeopardGecko(id = "leopard_gecko", averageLifespanYears = 15.0),
    CrestedGecko(id = "crested_gecko", averageLifespanYears = 15.0),
    ;

    companion object {
        fun fromId(id: String): ReptileType? = entries.firstOrNull { it.id == id }
    }
}
