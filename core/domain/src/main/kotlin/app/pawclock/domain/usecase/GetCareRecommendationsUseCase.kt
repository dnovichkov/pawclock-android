package app.pawclock.domain.usecase

import app.pawclock.domain.care.CareRepository
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Возвращает care-рекомендацию для (species × stage × locale).
 *
 * Делегирует в [CareRepository], не добавляя бизнес-логики — UseCase здесь нужен для:
 *  - единообразного DI-входа из ViewModel (та инжектит только UseCase'ы);
 *  - возможности расширения в будущем (например, отслеживание прочитанных рекомендаций,
 *    кэширование на уровне сессии и т. п.).
 *
 * Возвращает `null` если ни в запрошенной locale, ни в `en` нет ассета (см. KDoc
 * [CareRepository.load]). UI отображает graceful empty-state в этом случае.
 *
 * @param locale BCP 47 language tag — обычно `Locale.getDefault().language` или
 *   `LocaleListCompat.getApplicationLocales().get(0).language`. Если null или пустой,
 *   используется fallback `en`.
 */
class GetCareRecommendationsUseCase(
    private val careRepository: CareRepository,
) {
    suspend operator fun invoke(
        species: Species,
        stage: LifeStage,
        locale: String,
    ): CareRecommendation? = careRepository.load(species, stage, locale)
}
