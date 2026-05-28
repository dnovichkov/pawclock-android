package app.pawclock.model

/**
 * Тип содержания кошки. Влияет на поправки в формуле AAHA/AAFP 2021 (§4.2 спецификации):
 *  - [Outdoor]: возраст × 1.15 после 2 лет (приближённая поправка)
 *  - [LargeBreed]: +1 ЧГ/год после 2 (Maine Coon и другие крупные породы)
 *  - [IndoorShortHair], [IndoorLongHair]: базовая формула без поправок
 *
 * Используется в `CatAgeCalculator` и в `CatLifeStageCalculator`.
 */
enum class CatType(
    val id: String,
    val isOutdoor: Boolean,
    val isLargeBreed: Boolean,
) {
    IndoorShortHair(id = "indoor_short_hair", isOutdoor = false, isLargeBreed = false),
    IndoorLongHair(id = "indoor_long_hair", isOutdoor = false, isLargeBreed = false),
    Outdoor(id = "outdoor", isOutdoor = true, isLargeBreed = false),
    LargeBreed(id = "large_breed", isOutdoor = false, isLargeBreed = true),
    ;

    companion object {
        fun fromId(id: String): CatType? = entries.firstOrNull { it.id == id }
    }
}
