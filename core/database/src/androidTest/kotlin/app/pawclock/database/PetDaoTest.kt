package app.pawclock.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.pawclock.database.dao.PetDao
import app.pawclock.database.db.PawClockDatabase
import app.pawclock.database.entity.PetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [PetDao] backed by an in-memory Room database.
 *
 * Запускаются на эмуляторе/устройстве через `./gradlew :core:database:connectedDebugAndroidTest`
 * либо в nightly.yml workflow (Task 4).
 *
 * In-memory builder избегает file I/O и автоматически очищается между тестами.
 */
@RunWith(AndroidJUnit4::class)
class PetDaoTest {
    private lateinit var db: PawClockDatabase
    private lateinit var dao: PetDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, PawClockDatabase::class.java)
                .allowMainThreadQueries() // только для тестов; production-конфиг использует обычный builder
                .build()
        dao = db.petDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertPetReturnsIdAndPetCanBeFetchedById() =
        runTest {
            val entity =
                sampleEntity(
                    name = "Барсик",
                    speciesId = "cat",
                    birthDateIso = "2020-05-15",
                )
            val id = dao.insert(entity)
            assertNotEquals("Room should assign a non-zero auto-generated id", 0L, id)

            val fetched = dao.getById(id)
            assertNotNull(fetched)
            assertEquals("Барсик", fetched!!.name)
            assertEquals("cat", fetched.speciesId)
            assertEquals("2020-05-15", fetched.birthDateIso)
        }

    @Test
    fun getByIdReturnsNullForMissingPet() =
        runTest {
            val fetched = dao.getById(id = 9999L)
            assertNull(fetched)
        }

    @Test
    fun observeAllReturnsAllPetsSortedByName() =
        runTest {
            dao.insert(sampleEntity(name = "Чарли", speciesId = "dog"))
            dao.insert(sampleEntity(name = "Альфа", speciesId = "cat"))
            dao.insert(sampleEntity(name = "Бобик", speciesId = "dog"))

            val pets = dao.observeAll().first()
            assertEquals(3, pets.size)
            assertEquals("Альфа", pets[0].name)
            assertEquals("Бобик", pets[1].name)
            assertEquals("Чарли", pets[2].name)
        }

    @Test
    fun observeAllEmitsEmptyListWhenNoPets() =
        runTest {
            val pets = dao.observeAll().first()
            assertTrue(pets.isEmpty())
        }

    @Test
    fun deleteByIdRemovesPet() =
        runTest {
            val id = dao.insert(sampleEntity(name = "Шарик", speciesId = "dog"))
            val deletedRows = dao.deleteById(id)
            assertEquals(1, deletedRows)

            val fetched = dao.getById(id)
            assertNull(fetched)
        }

    @Test
    fun deleteByIdReturnsZeroForMissingPet() =
        runTest {
            val deletedRows = dao.deleteById(id = 9999L)
            assertEquals(0, deletedRows)
        }

    @Test
    fun updateChangesFields() =
        runTest {
            val id =
                dao.insert(
                    sampleEntity(
                        name = "Рекс",
                        speciesId = "dog",
                        weightKg = 12.5,
                        notes = null,
                    ),
                )
            val original = dao.getById(id)!!
            val updated =
                original.copy(
                    name = "Рекс Великий",
                    weightKg = 13.2,
                    notes = "Любит бегать",
                )
            dao.update(updated)

            val refetched = dao.getById(id)!!
            assertEquals("Рекс Великий", refetched.name)
            assertEquals(13.2, refetched.weightKg!!, 0.0001)
            assertEquals("Любит бегать", refetched.notes)
        }

    @Test
    fun insertAutoGeneratesUniqueIds() =
        runTest {
            val id1 = dao.insert(sampleEntity(name = "А"))
            val id2 = dao.insert(sampleEntity(name = "Б"))
            assertNotEquals(id1, id2)
        }

    // Test-builder. Long-parameter-list здесь не code smell — это просто фабрика
    // покрывающая все 8 nullable-полей PetEntity для granular-тестов.
    @Suppress("LongParameterList")
    private fun sampleEntity(
        name: String = "Test",
        speciesId: String = "dog",
        subcategory: String? = "medium",
        birthDateIso: String = "2022-01-01",
        genderId: String? = "male",
        weightKg: Double? = 10.0,
        notes: String? = null,
        photoPath: String? = null,
    ) = PetEntity(
        id = 0L,
        name = name,
        speciesId = speciesId,
        subcategory = subcategory,
        birthDateIso = birthDateIso,
        genderId = genderId,
        weightKg = weightKg,
        notes = notes,
        photoPath = photoPath,
    )
}
