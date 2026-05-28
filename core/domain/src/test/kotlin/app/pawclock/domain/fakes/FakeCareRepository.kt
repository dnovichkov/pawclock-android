package app.pawclock.domain.fakes

import app.pawclock.domain.care.CareRepository
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * In-memory fake [CareRepository] для юнит-тестов.
 *
 * Маппинг (species, stage, locale) → опциональный [CareRecommendation] задаётся через
 * [seed]. Тесты могут проверять как happy-path (значение присутствует), так и null-fallback
 * (значение отсутствует). НЕ имитирует locale-fallback ru → en в CareRepositoryImpl —
 * тесты UseCase'а должны явно проверять только пассивную делегацию.
 */
class FakeCareRepository : CareRepository {
    private val data = mutableMapOf<Key, CareRecommendation>()

    override suspend fun load(
        species: Species,
        stage: LifeStage,
        locale: String,
    ): CareRecommendation? = data[Key(species, stage, locale)]

    fun seed(
        species: Species,
        stage: LifeStage,
        locale: String,
        recommendation: CareRecommendation,
    ) {
        data[Key(species, stage, locale)] = recommendation
    }

    private data class Key(
        val species: Species,
        val stage: LifeStage,
        val locale: String,
    )
}
