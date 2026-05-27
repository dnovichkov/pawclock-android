package app.pawclock.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность для таблицы `pets`.
 *
 * Хранит только примитивы и строки — domain enum'ы (Species, Gender, DogSize/CatType)
 * сериализуются через стабильные id-строки (см. [Species.id]). Конверсия domain ↔ entity
 * выполняется в `PetMapper`, что позволяет:
 *  - тестировать маппинг как pure-JVM unit без Room runtime;
 *  - изменять domain-enum'ы без миграции БД (если id остаются стабильными);
 *  - хранить subcategory cross-species через единое строковое поле.
 *
 * Дата рождения хранится в ISO-8601 формате `YYYY-MM-DD` — удобно для отладки,
 * сортировки лексикографически = хронологически, не зависит от tz.
 *
 * @see app.pawclock.model.Pet domain-эквивалент
 * @see app.pawclock.database.mapper.PetMapper boundary-конвертор
 */
@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "species_id")
    val speciesId: String,
    @ColumnInfo(name = "subcategory")
    val subcategory: String?,
    @ColumnInfo(name = "birth_date_iso")
    val birthDateIso: String,
    @ColumnInfo(name = "gender_id")
    val genderId: String?,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
)
