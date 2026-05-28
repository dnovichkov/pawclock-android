package app.pawclock.feature.editor

import app.pawclock.model.Gender
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * События UI редактора питомца (Task 19 / Plan 1, MVI).
 *
 * Каждое событие — иммутабельное намерение пользователя; [PetEditorViewModel.handleEvent]
 * мапит его в новый [PetEditorState]. Side-effects (вызов SavePetUseCase) запускаются
 * только в обработчике [Save]; UI наблюдает результат через `state.saveResult`.
 */
sealed interface PetEditorEvent {
    data class SelectSpecies(
        val species: Species,
    ) : PetEditorEvent

    data class SetName(
        val name: String,
    ) : PetEditorEvent

    /**
     * @param subcategory `null` — снять выбор; иначе — стабильный id (DogSize.id / CatType.id).
     */
    data class SetSubcategory(
        val subcategory: String?,
    ) : PetEditorEvent

    data class SetBirthDate(
        val birthDate: LocalDate,
    ) : PetEditorEvent

    data class SetGender(
        val gender: Gender?,
    ) : PetEditorEvent

    /**
     * @param weight «сырая» строка для weight; парсится только в момент Save. Невалидное
     * число → weightKg сохраняется как null в Pet (см. [PetEditorViewModel.parseWeight]).
     */
    data class SetWeight(
        val weight: String,
    ) : PetEditorEvent

    data class SetNotes(
        val notes: String,
    ) : PetEditorEvent

    data class SetPhotoPath(
        val photoPath: String?,
    ) : PetEditorEvent

    /**
     * Триггер сохранения. ViewModel валидирует форму, вызывает SavePetUseCase
     * и выставляет [PetEditorState.saveResult]. Idempotent: если уже выполняется
     * (isSaving == true) — повторное событие игнорируется.
     */
    data object Save : PetEditorEvent

    /**
     * UI вызывает после успешной навигации, чтобы стереть [PetEditorState.saveResult] —
     * иначе recomposition'у будет казаться, что Save только что произошёл, и он
     * попытается навигировать ещё раз.
     */
    data object ConsumeSaveResult : PetEditorEvent
}
