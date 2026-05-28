package app.pawclock.database.mapper

import app.pawclock.database.entity.PetEntity
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * Boundary-конвертор между domain [Pet] и persistence [PetEntity].
 *
 * Принципы:
 *  - Mapper — единственное место, где domain знает о persistence-формате;
 *  - все enum'ы сериализуются через стабильные id-строки (см. [Species.id], [Gender.id]);
 *  - дата хранится в ISO-8601 (`LocalDate.toString()` гарантирует формат `YYYY-MM-DD`);
 *  - **unknown id из БД трактуется как data corruption** — бросаем `IllegalStateException`,
 *    а не возвращаем null/default, чтобы не «лечить» битые данные UI-слоем.
 *
 * Не используем Room TypeConverters преднамеренно: они скрывают конверсию внутри Room
 * и затрудняют JVM-тестирование без Android runtime.
 */
object PetMapper {
    fun toEntity(pet: Pet): PetEntity =
        PetEntity(
            id = pet.id,
            name = pet.name,
            speciesId = pet.species.id,
            subcategory = pet.subcategory,
            birthDateIso = pet.birthDate.toString(),
            genderId = pet.gender?.id,
            weightKg = pet.weightKg,
            notes = pet.notes,
            photoPath = pet.photoPath,
        )

    fun toDomain(entity: PetEntity): Pet {
        val species =
            Species.fromString(entity.speciesId)
                ?: error("Unknown species id in DB: '${entity.speciesId}' (data corruption)")
        val gender =
            entity.genderId?.let { id ->
                Gender.fromId(id)
                    ?: error("Unknown gender id in DB: '$id' (data corruption)")
            }
        val birthDate = LocalDate.parse(entity.birthDateIso)

        return Pet(
            id = entity.id,
            name = entity.name,
            species = species,
            birthDate = birthDate,
            subcategory = entity.subcategory,
            gender = gender,
            weightKg = entity.weightKg,
            notes = entity.notes,
            photoPath = entity.photoPath,
        )
    }
}
