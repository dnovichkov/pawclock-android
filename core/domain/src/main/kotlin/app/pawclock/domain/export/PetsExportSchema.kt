package app.pawclock.domain.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Версионируемая схема экспорта данных питомцев в JSON (§3.5 спецификации, Plan 2 Task 17).
 *
 * [schemaVersion] позволяет будущим версиям приложения распознавать формат и (при необходимости)
 * мигрировать старые бэкапы. Импорт (Task 19) отклоняет `schemaVersion > CURRENT_SCHEMA_VERSION`
 * как forward-incompatible. Имена полей сериализуются в `snake_case` через [SerialName],
 * чтобы соответствовать заголовкам CSV (Task 18) и быть стабильными независимо от Kotlin-нейминга.
 *
 * @property schemaVersion версия формата экспорта (текущая — [CURRENT_SCHEMA_VERSION])
 * @property exportedAt момент создания бэкапа в ISO-8601 (UTC `Instant.toString()`)
 * @property pets список экспортированных питомцев
 */
@Serializable
data class PetsExportSchema(
    @SerialName("schema_version") val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    @SerialName("exported_at") val exportedAt: String,
    val pets: List<PetExportEntry>,
) {
    companion object {
        /** Текущая версия схемы экспорта. Увеличивается при несовместимых изменениях формата. */
        const val CURRENT_SCHEMA_VERSION: Int = 1
    }
}

/**
 * Одна запись питомца в экспортном файле.
 *
 * Соответствует [app.pawclock.model.Pet] минус локальные artifact-поля: `id` (Room-PK,
 * назначается заново при импорте) и `photoPath` (путь в scoped storage конкретного устройства,
 * не переносим). Виды/пол/подкатегория сохраняются как стабильные строковые `id`, а не как
 * Kotlin-имена классов — это и есть контракт сериализации.
 *
 * Необязательные поля имеют дефолт `null`, благодаря чему импорт (Task 19) толерантен к
 * отсутствующим ключам, а экспорт опускает их (см. `explicitNulls = false` в [PetsJsonSerializer]).
 *
 * @property name отображаемое имя (обязательно)
 * @property speciesId стабильный id вида ([app.pawclock.model.Species.id])
 * @property birthDate дата рождения в ISO-8601 (`LocalDate.toString()`, обязательно)
 * @property subcategoryId стабильный id подкатегории или `null` для видов без подкатегорий
 * @property genderId стабильный id пола ([app.pawclock.model.Gender.id]) или `null`
 * @property weightKg вес в килограммах или `null`
 * @property notes свободные заметки или `null`
 */
@Serializable
data class PetExportEntry(
    val name: String,
    @SerialName("species_id") val speciesId: String,
    @SerialName("birth_date") val birthDate: String,
    @SerialName("subcategory_id") val subcategoryId: String? = null,
    @SerialName("gender_id") val genderId: String? = null,
    @SerialName("weight_kg") val weightKg: Double? = null,
    val notes: String? = null,
)
