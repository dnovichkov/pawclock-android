package app.pawclock.model

import java.time.LocalDate

/**
 * Доменная модель питомца.
 *
 * @property id уникальный идентификатор (0L для новых питомцев — выставляется Room при insert)
 * @property name отображаемое имя; обязательно, не может быть пустым или blank
 * @property species вид питомца ([Species.Dog], [Species.Cat] и т.д.)
 * @property subcategory стабильный id подкатегории (например, "medium" для [DogSize.Medium],
 *   "indoor_short_hair" для [CatType.IndoorShortHair]). Сериализуется как строка
 *   для cross-species persistence; декодируется в типизированный enum на границе домена.
 * @property birthDate дата рождения (для расчёта возраста)
 * @property gender пол питомца (опционально)
 * @property weightKg текущий вес в килограммах (опционально; влияет на авто-определение [DogSize])
 * @property notes свободные заметки владельца
 * @property photoPath абсолютный путь к фото внутри scoped storage приложения (опционально)
 */
data class Pet(
    val id: Long,
    val name: String,
    val species: Species,
    val birthDate: LocalDate,
    val subcategory: String? = null,
    val gender: Gender? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
    val photoPath: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Pet name must not be blank" }
    }
}
