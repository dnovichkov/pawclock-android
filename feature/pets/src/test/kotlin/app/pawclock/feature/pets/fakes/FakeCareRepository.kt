package app.pawclock.feature.pets.fakes

import app.pawclock.domain.care.CareRepository
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * In-memory fake [CareRepository] для юнит-тестов feature-ViewModel'ей.
 *
 * Маппинг (species, stage, locale) → опциональный [CareRecommendation] задаётся через
 * [seed]. Тесты могут проверять как happy-path (значение присутствует), так и null-fallback
 * (значение отсутствует).
 *
 * НЕ имитирует locale-fallback ru → en в production [CareRepository] —
 * fallback это ответственность production [app.pawclock.domain.care.CareRepositoryImpl];
 * тесты ViewModel'и должны только проверять прозрачную делегацию.
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
