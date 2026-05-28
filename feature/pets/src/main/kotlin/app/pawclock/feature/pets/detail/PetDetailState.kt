package app.pawclock.feature.pets.detail

import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.model.CareRecommendation
import app.pawclock.model.Pet

/**
 * Состояния экрана детального просмотра питомца (Task 18 / Plan 1).
 *
 *  - [Loading] — пока идёт загрузка из репозитория (одноразовый suspend через GetPetByIdUseCase).
 *  - [NotFound] — питомца с таким id больше нет (например, был удалён, пока экран был в backstack).
 *    UI показывает «Питомец не найден» + кнопку «Назад».
 *  - [Success] — питомец загружен; calculated age (Wang/SizeBased/AAFP) и care-рекомендация
 *    уже подсчитаны и доступны для рендеринга.
 *  - [Error] — непредвиденная ошибка (data corruption, IO). Показываем snackbar + retry.
 */
sealed interface PetDetailState {
    data object Loading : PetDetailState

    data object NotFound : PetDetailState

    data class Success(
        val pet: Pet,
        val calculatedAge: CalculatedAge,
        val careRecommendation: CareRecommendation?,
    ) : PetDetailState

    data class Error(
        val messageKey: String,
    ) : PetDetailState
}
