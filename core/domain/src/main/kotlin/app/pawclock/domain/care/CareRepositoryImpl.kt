package app.pawclock.domain.care

import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import kotlinx.serialization.json.Json

/**
 * Реализация [CareRepository] поверх [AssetSource].
 *
 * Алгоритм [load]:
 *  1. Строит путь: `care/{species.id}/{stage_segment}/{locale}.json`,
 *     где `stage_segment` — это [LifeStage.displayKey] с обрезанным префиксом `{species.id}_`
 *     (например, `"dog_puppy"` → `"puppy"`). Так файлы лежат в иерархии папок,
 *     а не плоско с подчёркиваниями.
 *  2. Пытается открыть запрошенный locale.
 *  3. Если файл отсутствует И locale != "en", пытается en.
 *  4. Если en тоже отсутствует — возвращает null (graceful empty state в UI).
 *  5. Если файл найден — десериализует JSON через kotlinx.serialization.
 *     `ignoreUnknownKeys = true` чтобы новые поля в ассетах не ломали старые версии приложения.
 *
 * Production-ready без кэширования: read из assets дешёвый (mmap'd), а добавление кэша
 * без необходимости — premature optimization (см. CLAUDE.md guidance).
 *
 * Json-парсер инкапсулирован как private companion-константа — не утекает через API,
 * поэтому `:app` не нужен `kotlinx-serialization-json` на classpath.
 *
 * @param assetSource источник чтения файлов (Android AssetManager в production, fake в тестах)
 */
class CareRepositoryImpl(
    private val assetSource: AssetSource,
) : CareRepository {
    override suspend fun load(
        species: Species,
        stage: LifeStage,
        locale: String,
    ): CareRecommendation? {
        val stageSegment = extractStageSegment(species, stage)
        val localesToTry = if (locale == FALLBACK_LOCALE) listOf(locale) else listOf(locale, FALLBACK_LOCALE)
        return localesToTry
            .asSequence()
            .map { buildPath(species.id, stageSegment, it) }
            .mapNotNull { readFromPath(it) }
            .firstOrNull()
    }

    private fun readFromPath(path: String): CareRecommendation? {
        val stream = assetSource.open(path) ?: return null
        return stream.use { input ->
            val raw = input.bufferedReader(Charsets.UTF_8).readText()
            DefaultJson.decodeFromString(CareRecommendation.serializer(), raw)
        }
    }

    /**
     * Отсекает префикс `{species.id}_` от `LifeStage.displayKey`, чтобы получить
     * сегмент пути файла. Если префикс отсутствует (нештатный случай — теоретически
     * generic-стадия для будущих видов), возвращает displayKey как есть.
     */
    private fun extractStageSegment(
        species: Species,
        stage: LifeStage,
    ): String {
        val prefix = "${species.id}_"
        return stage.displayKey.removePrefix(prefix)
    }

    private fun buildPath(
        speciesId: String,
        stageSegment: String,
        locale: String,
    ): String = "$ASSET_ROOT/$speciesId/$stageSegment/$locale.json"

    private companion object {
        const val ASSET_ROOT = "care"
        const val FALLBACK_LOCALE = "en"

        /**
         * JSON-парсер для care-ассетов.
         *
         *  - `ignoreUnknownKeys = true` — forward-compat: ассеты могут добавлять новые поля.
         *  - `isLenient = false` — care-ассеты вычитываются вручную и должны быть строгим JSON.
         */
        val DefaultJson: Json =
            Json {
                ignoreUnknownKeys = true
                isLenient = false
            }
    }
}
