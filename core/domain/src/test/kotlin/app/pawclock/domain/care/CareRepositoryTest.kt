package app.pawclock.domain.care

import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * TDD-тесты [CareRepository] (см. Task 14 в плане pawclock-foundation-and-dog-cat-mvp.md).
 *
 * Тесты pure JVM. Чтение из реальных Android assets изолируется за интерфейсом
 * [AssetSource], который в тестах подменяется на [FakeAssetSource] с in-memory map.
 *
 * Покрываем:
 *  - happy path: загрузка существующего dog/puppy/ru → CareRecommendation
 *  - fallback locale: ru отсутствует → загружается en (§6 спецификации — ru → en → throw)
 *  - missing species/stage: возвращает null, без exception на верхнем уровне
 *  - malformed JSON: пробрасывается из serialization как ошибка вызова
 *  - все обязательные поля: проверка что сериализатор корректно мапит snake_case → camelCase
 */
class CareRepositoryTest {
    /** Минимальный валидный JSON для тестов — содержит все required поля. */
    private val validDogPuppyRuJson =
        """
        {
          "stage_description": "Щенок — стадия с 0 до примерно 9 месяцев.",
          "nutrition": "4 кормления в день специальным щенячьим кормом.",
          "activity": "Короткие игровые сессии, социализация.",
          "veterinary_check_frequency": "Каждые 3-4 недели до 16 недель, затем по графику вакцинаций.",
          "dental_care": "Чистка зубов с щенячьего возраста для приучения.",
          "warning_signs": "Понос, рвота, отказ от еды более 12 часов.",
          "source_url": "https://www.aaha.org/practice-resources/2019-aaha-canine-life-stage-guidelines/",
          "source_name": "AAHA 2019 Canine Life Stage Guidelines",
          "disclaimer": "Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача."
        }
        """.trimIndent()

    /** Английская версия — нужна для проверки fallback locale. */
    private val validDogPuppyEnJson =
        """
        {
          "stage_description": "Puppy — life stage from 0 to ~9 months.",
          "nutrition": "Four meals a day with puppy-specific food.",
          "activity": "Short play sessions, socialization.",
          "veterinary_check_frequency": "Every 3-4 weeks until 16 weeks, then per vaccination schedule.",
          "dental_care": "Start brushing teeth early to build habit.",
          "warning_signs": "Diarrhea, vomiting, no appetite for over 12 hours.",
          "source_url": "https://www.aaha.org/practice-resources/2019-aaha-canine-life-stage-guidelines/",
          "source_name": "AAHA 2019 Canine Life Stage Guidelines",
          "disclaimer": "This information is for educational purposes and does not replace a veterinarian's consultation."
        }
        """.trimIndent()

    @Test
    fun `load existing dog puppy ru recommendation succeeds`() =
        runTest {
            val source = FakeAssetSource("care/dog/puppy/ru.json" to validDogPuppyRuJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")

            assertNotNull(result)
            assertEquals("Щенок — стадия с 0 до примерно 9 месяцев.", result?.stageDescription)
            assertEquals("AAHA 2019 Canine Life Stage Guidelines", result?.sourceName)
            assertTrue(
                result?.disclaimer?.contains("не заменяет") == true,
                "Disclaimer must mention not-replacing-vet per §3.3",
            )
        }

    @Test
    fun `falls back to en when requested locale (ru) is missing`() =
        runTest {
            // Только en доступен — ru отсутствует. Согласно §6 спецификации,
            // в этом случае должен подгрузиться en, чтобы пользователь увидел контент.
            val source = FakeAssetSource("care/dog/puppy/en.json" to validDogPuppyEnJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")

            assertNotNull(result, "ru → en fallback must return English content rather than null")
            assertEquals("Puppy — life stage from 0 to ~9 months.", result?.stageDescription)
        }

    @Test
    fun `returns null when neither requested locale nor en exists (species not implemented)`() =
        runTest {
            val source = FakeAssetSource() // empty assets
            val repository = CareRepositoryImpl(source)

            // Rabbit ещё не реализован в Plan 1 — care-файла не существует.
            val result = repository.load(Species.Rabbit, LifeStage.Dog.Puppy, "ru")

            assertNull(result, "missing assets must return null, not throw — UI shows graceful empty state")
        }

    @Test
    fun `returns null for cat senior when only dog puppy is in assets`() =
        runTest {
            val source = FakeAssetSource("care/dog/puppy/ru.json" to validDogPuppyRuJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Cat, LifeStage.Cat.Senior, "ru")

            assertNull(result)
        }

    @Test
    fun `prefers requested locale over en when both are available`() =
        runTest {
            val source =
                FakeAssetSource(
                    "care/dog/puppy/ru.json" to validDogPuppyRuJson,
                    "care/dog/puppy/en.json" to validDogPuppyEnJson,
                )
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")

            assertEquals("Щенок — стадия с 0 до примерно 9 месяцев.", result?.stageDescription)
        }

    @Test
    fun `unknown locale tag falls back to en`() =
        runTest {
            // Кто-то с локалью de — у нас нет de.json, но есть en.json — fallback должен сработать.
            val source = FakeAssetSource("care/dog/puppy/en.json" to validDogPuppyEnJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.Puppy, "de")

            assertNotNull(result)
            assertEquals("Puppy — life stage from 0 to ~9 months.", result?.stageDescription)
        }

    @Test
    fun `requesting en directly does not double-attempt en (no infinite recursion)`() =
        runTest {
            val source = FakeAssetSource("care/dog/puppy/en.json" to validDogPuppyEnJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.Puppy, "en")

            assertEquals(1, source.callCount, "en should be opened exactly once when requested directly")
            assertNotNull(result)
        }

    @Test
    fun `loads cat kitten life stage successfully (different species + stage path)`() =
        runTest {
            val catJson =
                """
                {
                  "stage_description": "Котёнок — стадия с 0 до 1 года.",
                  "nutrition": "Корм для котят с высоким содержанием белка.",
                  "activity": "Игрушки, лазалки, социализация.",
                  "veterinary_check_frequency": "Каждые 3-4 недели до 16 недель.",
                  "warning_signs": "Понос, потеря аппетита.",
                  "source_url": "https://catvets.com/guidelines/life-stage/",
                  "source_name": "AAHA/AAFP 2021 Feline Life Stage Guidelines",
                  "disclaimer": "Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача."
                }
                """.trimIndent()
            val source = FakeAssetSource("care/cat/kitten/ru.json" to catJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Cat, LifeStage.Cat.Kitten, "ru")

            assertNotNull(result)
            assertEquals("AAHA/AAFP 2021 Feline Life Stage Guidelines", result?.sourceName)
            // dental_care optional — проверяем что null-поле корректно десериализуется.
            assertNull(result?.dentalCare)
        }

    @Test
    fun `malformed JSON throws SerializationException`() =
        runTest {
            val source = FakeAssetSource("care/dog/puppy/ru.json" to "{ not valid json")
            val repository = CareRepositoryImpl(source)

            assertThrows<Exception> {
                repository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")
            }
        }

    @Test
    fun `JSON missing required field throws on deserialization`() =
        runTest {
            // Нет обязательного поля nutrition — kotlinx.serialization выбросит MissingFieldException.
            val incomplete = """{ "stage_description": "x" }"""
            val source = FakeAssetSource("care/dog/puppy/ru.json" to incomplete)
            val repository = CareRepositoryImpl(source)

            assertThrows<Exception> {
                repository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")
            }
        }

    @Test
    fun `path is built from species id and lifestage segment (not displayKey)`() =
        runTest {
            // displayKey для Dog.Puppy = "dog_puppy", но в файловой структуре
            // мы хотим иерархию papки {species}/{stage}, а не плоское "dog_puppy".
            // Stage segment = displayKey без префикса species — "puppy".
            // Этот тест зафиксирует контракт: путь = "care/{species.id}/{stage_segment}/{locale}.json".
            val source = FakeAssetSource("care/dog/mature_adult/ru.json" to validDogPuppyRuJson)
            val repository = CareRepositoryImpl(source)

            val result = repository.load(Species.Dog, LifeStage.Dog.MatureAdult, "ru")

            assertNotNull(result)
        }

    @Test
    fun `loading caches no state between calls (each call hits AssetSource)`() =
        runTest {
            val source = FakeAssetSource("care/dog/puppy/en.json" to validDogPuppyEnJson)
            val repository = CareRepositoryImpl(source)

            repository.load(Species.Dog, LifeStage.Dog.Puppy, "en")
            repository.load(Species.Dog, LifeStage.Dog.Puppy, "en")
            repository.load(Species.Dog, LifeStage.Dog.Puppy, "en")

            // Plan 1: без кэширования — простой контракт. Если в будущем добавится cache,
            // этот тест нужно будет обновить (но в Plan 1 каждый load = свежий read).
            assertEquals(3, source.callCount)
        }

    /** In-memory реализация [AssetSource] для тестов. Хранит пути → содержимое. */
    private class FakeAssetSource(
        vararg entries: Pair<String, String>,
    ) : AssetSource {
        private val data: Map<String, String> = entries.toMap()
        var callCount: Int = 0
            private set

        override fun open(path: String): InputStream? {
            callCount++
            val content = data[path] ?: return null
            return ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        }
    }
}
