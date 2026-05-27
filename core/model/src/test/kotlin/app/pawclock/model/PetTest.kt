package app.pawclock.model

import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PetTest {
    private val sampleBirth = LocalDate.of(2020, 5, 15)

    @Test
    fun `two pets with identical fields are equal`() {
        val a =
            Pet(
                id = 1L,
                name = "Барсик",
                species = Species.Cat,
                subcategory = CatType.IndoorShortHair.id,
                birthDate = sampleBirth,
                gender = Gender.Male,
                weightKg = 4.2,
                notes = "любит сметану",
                photoPath = null,
            )
        val b =
            Pet(
                id = 1L,
                name = "Барсик",
                species = Species.Cat,
                subcategory = CatType.IndoorShortHair.id,
                birthDate = sampleBirth,
                gender = Gender.Male,
                weightKg = 4.2,
                notes = "любит сметану",
                photoPath = null,
            )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `different names produce non-equal pets`() {
        val a =
            Pet(
                id = 1L,
                name = "Барсик",
                species = Species.Cat,
                subcategory = null,
                birthDate = sampleBirth,
                gender = null,
                weightKg = null,
                notes = null,
                photoPath = null,
            )
        val b = a.copy(name = "Мурзик")
        assertNotEquals(a, b)
    }

    @Test
    fun `copy preserves unspecified fields`() {
        val original =
            Pet(
                id = 1L,
                name = "Рекс",
                species = Species.Dog,
                subcategory = DogSize.Medium.id,
                birthDate = sampleBirth,
                gender = Gender.Male,
                weightKg = 18.0,
                notes = "любит грызть тапки",
                photoPath = "/storage/photo.jpg",
            )
        val updated = original.copy(weightKg = 19.5)
        assertEquals(19.5, updated.weightKg)
        assertEquals(original.name, updated.name)
        assertEquals(original.species, updated.species)
        assertEquals(original.subcategory, updated.subcategory)
        assertEquals(original.birthDate, updated.birthDate)
        assertEquals(original.notes, updated.notes)
        assertEquals(original.photoPath, updated.photoPath)
    }

    @Test
    fun `optional fields default to null`() {
        val pet =
            Pet(
                id = 0L,
                name = "Кот",
                species = Species.Cat,
                birthDate = sampleBirth,
            )
        assertNull(pet.subcategory)
        assertNull(pet.gender)
        assertNull(pet.weightKg)
        assertNull(pet.notes)
        assertNull(pet.photoPath)
    }

    @Test
    fun `name is required and non-blank constraint is enforced`() {
        val exception =
            runCatching {
                Pet(
                    id = 0L,
                    name = "  ",
                    species = Species.Dog,
                    birthDate = sampleBirth,
                )
            }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class.java, exception?.javaClass)
    }
}
