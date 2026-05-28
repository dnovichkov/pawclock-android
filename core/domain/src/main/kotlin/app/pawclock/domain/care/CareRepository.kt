package app.pawclock.domain.care

import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Репозиторий care-рекомендаций (§3.3 спецификации).
 *
 * Загружает [CareRecommendation] для конкретной пары (вид × стадия жизни × locale)
 * из read-only бандла приложения. Конкретный механизм чтения изолирован за [AssetSource]
 * — в production это Android `AssetManager`, в тестах — in-memory map.
 *
 * Контракт fallback locale (§6):
 *  - ru → en → null (если ни одного файла нет, возвращает null, а UI показывает пустое состояние)
 *  - en → en → null (без двойной попытки на en)
 *  - de/fr/zh... → en → null (любой не-en запрос пробует свой locale, затем en)
 *
 * НЕ бросает `IOException` для отсутствующих файлов — null-результат означает «нет данных».
 * Бросает исключение только при malformed JSON (kotlinx.serialization), так как это
 * indicates ошибку сборки / повреждённые ассеты — не runtime-условие, которое нужно молча проглатывать.
 *
 * Пример использования:
 * ```
 * val rec = careRepository.load(Species.Dog, LifeStage.Dog.Puppy, "ru")
 * if (rec != null) showRecommendations(rec) else showEmptyState()
 * ```
 */
interface CareRepository {
    /**
     * Загружает care-рекомендацию для заданной комбинации.
     *
     * @param species вид животного (только [Species.isImplemented] виды имеют ассеты в Plan 1)
     * @param stage стадия жизни — должна соответствовать [species] (Dog/Cat для своих видов)
     * @param locale BCP 47 language tag (`"ru"`, `"en"`, etc.)
     * @return [CareRecommendation] или `null` если ни запрошенная locale, ни en недоступны
     */
    suspend fun load(
        species: Species,
        stage: LifeStage,
        locale: String,
    ): CareRecommendation?
}
