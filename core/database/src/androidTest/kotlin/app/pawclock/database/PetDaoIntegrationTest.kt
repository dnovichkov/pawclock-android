package app.pawclock.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.pawclock.database.db.PawClockDatabase
import app.pawclock.database.mapper.PetMapper
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end Room integration: domain Pet → Entity → Room → Entity → domain Pet.
 *
 * Покрывает требование плана Task 12: «integration test с in-memory Room DB:
 * создание Pet, чтение через Flow, удаление». Использует [PetMapper] для границы
 * domain/persistence (Pet всегда хранится через mapper, никогда напрямую).
 */
@RunWith(AndroidJUnit4::class)
class PetDaoIntegrationTest {
    private lateinit var db: PawClockDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, PawClockDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun roundTripPetThroughDatabasePreservesAllFields() =
        runTest {
            val pet =
                Pet(
                    id = 0L,
                    name = "Барсик",
                    species = Species.Cat,
                    birthDate = LocalDate.of(2020, 5, 15),
                    subcategory = CatType.IndoorShortHair.id,
                    gender = Gender.Male,
                    weightKg = 4.5,
                    notes = "Любит спать на подоконнике",
                    photoPath = null,
                )

            val dao = db.petDao()
            val assignedId = dao.insert(PetMapper.toEntity(pet))
            val fetchedEntity = dao.getById(assignedId)
            assertNotNull(fetchedEntity)

            val fetchedPet = PetMapper.toDomain(fetchedEntity!!)
            assertEquals(assignedId, fetchedPet.id)
            assertEquals(pet.name, fetchedPet.name)
            assertEquals(pet.species, fetchedPet.species)
            assertEquals(pet.subcategory, fetchedPet.subcategory)
            assertEquals(pet.birthDate, fetchedPet.birthDate)
            assertEquals(pet.gender, fetchedPet.gender)
            assertEquals(pet.weightKg, fetchedPet.weightKg)
            assertEquals(pet.notes, fetchedPet.notes)
            assertEquals(pet.photoPath, fetchedPet.photoPath)
        }

    @Test
    fun observeAllReflectsInsertAndDelete() =
        runTest {
            val dao = db.petDao()
            val dog =
                Pet(
                    id = 0L,
                    name = "Рекс",
                    species = Species.Dog,
                    birthDate = LocalDate.of(2018, 3, 1),
                    subcategory = DogSize.Medium.id,
                )
            val cat =
                Pet(
                    id = 0L,
                    name = "Мурка",
                    species = Species.Cat,
                    birthDate = LocalDate.of(2021, 6, 10),
                    subcategory = CatType.IndoorLongHair.id,
                )

            val dogId = dao.insert(PetMapper.toEntity(dog))
            val catId = dao.insert(PetMapper.toEntity(cat))

            val petsAfterInsert = dao.observeAll().first().map(PetMapper::toDomain)
            assertEquals(2, petsAfterInsert.size)
            // Сортировка по имени: "Мурка" (М) < "Рекс" (Р) в alphabetical order Unicode
            assertEquals("Мурка", petsAfterInsert[0].name)
            assertEquals("Рекс", petsAfterInsert[1].name)

            dao.deleteById(dogId)
            val petsAfterDelete = dao.observeAll().first().map(PetMapper::toDomain)
            assertEquals(1, petsAfterDelete.size)
            assertEquals(catId, petsAfterDelete[0].id)
        }
}
