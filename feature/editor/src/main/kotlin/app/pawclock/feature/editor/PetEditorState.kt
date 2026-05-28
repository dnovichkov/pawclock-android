package app.pawclock.feature.editor

import app.pawclock.domain.pet.PetValidationError
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Gender
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * Состояние экрана редактора питомца (Task 19 / Plan 1, MVI).
 *
 * State хранит «сырое» содержимое формы как `String` для weight (а не Double?), потому что
 * пользователь во время набора проходит через невалидные промежуточные значения
 * («1.», «1.2.»). Парсинг в Double выполняется только в момент Save.
 *
 *  - [name], [birthDate], [species], [subcategory], [gender], [weightKg], [notes],
 *    [photoPath] — поля формы;
 *  - [availableSubcategories] — derive'нутые из [species] подкатегории (DogSize.entries
 *    для Dog, CatType.entries для Cat); пустой list для других видов и для null species;
 *  - [isLoading] — true пока [GetPetByIdUseCase] не вернул pet'а в режиме редактирования;
 *  - [isSaving] — true во время вызова [SavePetUseCase];
 *  - [saveResult] — non-null после успешного Save → UI вызывает navigation back;
 *    UI должен очистить через [PetEditorEvent.ConsumeSaveResult] после навигации,
 *    чтобы не сработать повторно на recomposition;
 *  - [validationErrors] — список ошибок от [SavePetUseCase] (NameBlank/BirthDateInFuture/...);
 *    непустой — пока пользователь не правит соответствующее поле;
 *  - [formErrorMessageKey] — высокоуровневая ошибка формы (например, не выбран species),
 *    локализуется UI через `stringResource(key)`;
 *  - [editingPetId] — null для нового питомца, иначе id существующего.
 */
data class PetEditorState(
    val name: String = "",
    val species: Species? = null,
    val subcategory: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val weightKg: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val availableSubcategories: List<SubcategoryOption> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: PetEditorSaveResult? = null,
    val validationErrors: List<PetValidationError> = emptyList(),
    val formErrorMessageKey: String? = null,
    val editingPetId: Long? = null,
) {
    companion object {
        /**
         * Стартовое значение для новой формы (без petId в SavedStateHandle).
         */
        val Empty: PetEditorState = PetEditorState()

        /**
         * Стартовое значение для режима редактирования — показываем Loading пока pet не загрузится.
         */
        fun loadingFor(petId: Long): PetEditorState = PetEditorState(isLoading = true, editingPetId = petId)

        /**
         * Возвращает подкатегории для заданного species. Для не-Dog/Cat видов
         * возвращает emptyList (Plan 1 не реализует остальные виды).
         */
        fun subcategoriesFor(species: Species?): List<SubcategoryOption> =
            when (species) {
                Species.Dog -> DogSize.entries.map { SubcategoryOption(it.id, it.name) }
                Species.Cat -> CatType.entries.map { SubcategoryOption(it.id, it.name) }
                else -> emptyList()
            }
    }
}

/**
 * Опция подкатегории питомца в UI (id для persistence + label для отображения).
 *
 * [label] — английское enum-имя на этом этапе; локализованные метки появятся в Task 22
 * через `stringResource(R.string.dog_size_${id})` mapping.
 */
data class SubcategoryOption(
    val id: String,
    val label: String,
)

/**
 * Результат операции сохранения, выставляемый ViewModel'ью после Save.
 *
 * UI наблюдает за [PetEditorState.saveResult]: если non-null Success → навигация back +
 * `ConsumeSaveResult` для очистки; если non-null [Error] → показывает snackbar.
 */
sealed interface PetEditorSaveResult {
    data class Success(
        val petId: Long,
    ) : PetEditorSaveResult

    data class Error(
        val messageKey: String,
    ) : PetEditorSaveResult
}
