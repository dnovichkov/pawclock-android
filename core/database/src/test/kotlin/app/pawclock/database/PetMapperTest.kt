package app.pawclock.database

import app.pawclock.database.entity.PetEntity
import app.pawclock.database.mapper.PetMapper
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Pure JVM unit tests for [PetMapper] — Pet ↔ PetEntity boundary.
 *
 * Mapper'у не нужен Room runtime, поэтому тесты быстрые и работают на любом
 * хост-окружении. См. Task 12 в плане pawclock-foundation-and-dog-cat-mvp.md.
 */
class PetMapperTest {
    @Test
    fun `domain Pet round-trips through entity with all fields populated`() {
        val pet =
            Pet(
                id = 42L,
                name = "Барсик",
                species = Species.Cat,
                birthDate = LocalDate.of(2020, 5, 15),
                subcategory = CatType.IndoorShortHair.id,
                gender = Gender.Male,
                weightKg = 4.5,
                notes = "Любит спать",
                photoPath = "/storage/photos/barsik.jpg",
            )

        val entity = PetMapper.toEntity(pet)
        val restored = PetMapper.toDomain(entity)

        assertEquals(pet, restored)
    }

    @Test
    fun `domain Pet round-trips with null optional fields`() {
        val pet =
            Pet(
                id = 0L,
                name = "Шарик",
                species = Species.Dog,
                birthDate = LocalDate.of(2019, 1, 1),
                subcategory = null,
                gender = null,
                weightKg = null,
                notes = null,
                photoPath = null,
            )

        val entity = PetMapper.toEntity(pet)
        val restored = PetMapper.toDomain(entity)

        assertEquals(pet, restored)
    }

    @Test
    fun `entity preserves stable species id`() {
        val pet =
            samplePet(species = Species.Dog)
        val entity = PetMapper.toEntity(pet)

        assertEquals("dog", entity.speciesId)
    }

    @Test
    fun `entity preserves ISO birth date format`() {
        val pet = samplePet(birthDate = LocalDate.of(2021, 3, 7))
        val entity = PetMapper.toEntity(pet)

        assertEquals("2021-03-07", entity.birthDateIso)
    }

    @Test
    fun `entity preserves stable gender id`() {
        val pet = samplePet(gender = Gender.Female)
        val entity = PetMapper.toEntity(pet)

        assertEquals("female", entity.genderId)
    }

    @Test
    fun `null gender maps to null genderId`() {
        val pet = samplePet(gender = null)
        val entity = PetMapper.toEntity(pet)

        assertNull(entity.genderId)
    }

    @Test
    fun `toDomain throws on unknown species id (data corruption)`() {
        val entity =
            PetEntity(
                id = 1L,
                name = "Mystery",
                speciesId = "klingon",
                subcategory = null,
                birthDateIso = "2020-01-01",
                genderId = null,
                weightKg = null,
                notes = null,
                photoPath = null,
            )

        assertThrows<IllegalStateException> {
            PetMapper.toDomain(entity)
        }
    }

    @Test
    fun `toDomain throws on unknown gender id (data corruption)`() {
        val entity =
            PetEntity(
                id = 1L,
                name = "Mystery",
                speciesId = "dog",
                subcategory = null,
                birthDateIso = "2020-01-01",
                genderId = "alien",
                weightKg = null,
                notes = null,
                photoPath = null,
            )

        assertThrows<IllegalStateException> {
            PetMapper.toDomain(entity)
        }
    }

    @Test
    fun `toDomain throws on malformed birth date`() {
        val entity =
            PetEntity(
                id = 1L,
                name = "Mystery",
                speciesId = "dog",
                subcategory = null,
                birthDateIso = "not-a-date",
                genderId = null,
                weightKg = null,
                notes = null,
                photoPath = null,
            )

        // Точный тип фиксирует контракт: malformed ISO-дата → DateTimeParseException,
        // не любая RuntimeException. Иначе тест пройдёт даже на NPE из-за регрессии.
        assertThrows<DateTimeParseException> {
            PetMapper.toDomain(entity)
        }
    }

    @Test
    fun `dog with size subcategory survives mapping`() {
        val pet =
            samplePet(
                species = Species.Dog,
                subcategory = DogSize.Giant.id,
            )

        val restored = PetMapper.toDomain(PetMapper.toEntity(pet))

        assertNotNull(restored.subcategory)
        assertEquals("giant", restored.subcategory)
        assertEquals(DogSize.Giant, DogSize.fromId(restored.subcategory!!))
    }

    @Test
    fun `cat with type subcategory survives mapping`() {
        val pet =
            samplePet(
                species = Species.Cat,
                subcategory = CatType.Outdoor.id,
            )

        val restored = PetMapper.toDomain(PetMapper.toEntity(pet))

        assertEquals("outdoor", restored.subcategory)
        assertEquals(CatType.Outdoor, CatType.fromId(restored.subcategory!!))
    }

    // Test-builder покрывает все 9 полей Pet — разбиение на отдельные функции лишь
    // ухудшит читаемость тестов. Long-parameter-list здесь не code smell в production-смысле.
    @Suppress("LongParameterList")
    private fun samplePet(
        id: Long = 0L,
        name: String = "Test",
        species: Species = Species.Dog,
        birthDate: LocalDate = LocalDate.of(2022, 1, 1),
        subcategory: String? = "medium",
        gender: Gender? = Gender.Male,
        weightKg: Double? = 10.0,
        notes: String? = null,
        photoPath: String? = null,
    ) = Pet(
        id = id,
        name = name,
        species = species,
        birthDate = birthDate,
        subcategory = subcategory,
        gender = gender,
        weightKg = weightKg,
        notes = notes,
        photoPath = photoPath,
    )
}
