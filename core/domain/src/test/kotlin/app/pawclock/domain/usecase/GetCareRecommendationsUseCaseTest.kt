package app.pawclock.domain.usecase

import app.pawclock.domain.fakes.FakeCareRepository
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetCareRecommendationsUseCaseTest {
    private fun sampleRec(label: String): CareRecommendation =
        CareRecommendation(
            stageDescription = label,
            nutrition = "n",
            activity = "a",
            veterinaryCheckFrequency = "v",
            dentalCare = null,
            warningSigns = "w",
            sourceUrl = "https://example.test",
            sourceName = "test",
            disclaimer = "Информация носит ознакомительный характер...",
        )

    @Test
    fun `returns recommendation when available`() =
        runTest {
            val repo = FakeCareRepository()
            val rec = sampleRec("Puppy ru")
            repo.seed(Species.Dog, LifeStage.Dog.Puppy, "ru", rec)
            val useCase = GetCareRecommendationsUseCase(repo)
            assertEquals(rec, useCase(Species.Dog, LifeStage.Dog.Puppy, "ru"))
        }

    @Test
    fun `returns null when not seeded`() =
        runTest {
            val repo = FakeCareRepository()
            val useCase = GetCareRecommendationsUseCase(repo)
            assertNull(useCase(Species.Cat, LifeStage.Cat.Senior, "ru"))
        }

    @Test
    fun `does not auto-fallback to en when ru missing - fallback is repository concern`() =
        runTest {
            val repo = FakeCareRepository()
            repo.seed(Species.Dog, LifeStage.Dog.Puppy, "en", sampleRec("Puppy en"))
            val useCase = GetCareRecommendationsUseCase(repo)
            // UseCase — pass-through: fallback логика реализована в CareRepositoryImpl,
            // а не в UseCase. Fake-repo не имитирует fallback.
            assertNull(useCase(Species.Dog, LifeStage.Dog.Puppy, "ru"))
        }
}
