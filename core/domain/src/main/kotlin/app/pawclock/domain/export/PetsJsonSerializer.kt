package app.pawclock.domain.export

import app.pawclock.model.Pet
import java.time.Instant
import kotlinx.serialization.json.Json

/**
 * Сериализует список [Pet] в JSON по схеме [PetsExportSchema] (§3.5, Plan 2 Task 17).
 *
 * Pure-Kotlin, без Android-зависимостей — живёт в `:core:domain` и тестируется на JVM.
 * Конфигурация [Json]:
 *  - `prettyPrint = true` — файл бэкапа читаем человеком;
 *  - `encodeDefaults = true` — `schema_version` (поле с дефолтом) попадает в вывод для forward-compat;
 *  - `explicitNulls = false` — необязательные поля со значением `null` опускаются (а не пишутся как `null`).
 *
 * `photoPath` и `id` намеренно не экспортируются (см. KDoc [PetExportEntry]).
 */
object PetsJsonSerializer {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            explicitNulls = false
        }

    /**
     * @param pets питомцы для экспорта (в порядке, заданном вызывающим — обычно сортировка репозитория)
     * @param exportedAt момент создания бэкапа; сериализуется как ISO-8601 (`Instant.toString()`)
     * @return JSON-строка по схеме [PetsExportSchema]
     */
    fun encode(
        pets: List<Pet>,
        exportedAt: Instant,
    ): String {
        val schema =
            PetsExportSchema(
                exportedAt = exportedAt.toString(),
                pets = pets.map(::toEntry),
            )
        return json.encodeToString(PetsExportSchema.serializer(), schema)
    }

    private fun toEntry(pet: Pet): PetExportEntry =
        PetExportEntry(
            name = pet.name,
            speciesId = pet.species.id,
            birthDate = pet.birthDate.toString(),
            subcategoryId = pet.subcategory,
            genderId = pet.gender?.id,
            weightKg = pet.weightKg,
            notes = pet.notes,
        )
}
