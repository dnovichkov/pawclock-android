package app.pawclock.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.domain.pet.PetValidationException
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.domain.usecase.GetPetByIdUseCase
import app.pawclock.domain.usecase.SavePetUseCase
import app.pawclock.model.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана создания/редактирования питомца (Task 19 / Plan 1, MVI).
 *
 * Источник данных:
 *  - [SavedStateHandle] передаёт `petId: Long?` из Navigation route (см. Route.PetEditor).
 *    `null` → режим создания; не-null → режим редактирования, [GetPetByIdUseCase] заполняет
 *    форму существующими полями.
 *
 * Бизнес-логика:
 *  - [handleEvent] — единственный публичный mutator, мапит [PetEditorEvent] → новый [PetEditorState];
 *  - SetSpecies очищает [PetEditorState.subcategory] и переопределяет
 *    [PetEditorState.availableSubcategories];
 *  - Save проверяет «обязательные to-construct поля» (species, birthDate, name) на UI-уровне
 *    и делегирует доменную валидацию (future/unrealistic date) в [SavePetUseCase].
 *
 * Поток состояния: `MutableStateFlow<PetEditorState>` (а не `stateIn`), потому что
 * state мутируется явными событиями, а не реактивно из репозитория.
 */
@HiltViewModel
class PetEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getPetById: GetPetByIdUseCase,
        private val savePet: SavePetUseCase,
    ) : ViewModel() {
        private val editingPetId: Long? = savedStateHandle.get<Long>("petId")
        private val _state =
            MutableStateFlow(
                if (editingPetId == null) {
                    PetEditorState.Empty
                } else {
                    PetEditorState.loadingFor(editingPetId)
                },
            )
        val state: StateFlow<PetEditorState> = _state.asStateFlow()

        init {
            if (editingPetId != null) {
                loadExisting(editingPetId)
            }
        }

        fun handleEvent(event: PetEditorEvent) {
            when (event) {
                is PetEditorEvent.SelectSpecies -> onSelectSpecies(event)
                is PetEditorEvent.SetName -> onSetField { it.copy(name = event.name) }
                is PetEditorEvent.SetSubcategory ->
                    onSetField { it.copy(subcategory = event.subcategory) }
                is PetEditorEvent.SetBirthDate -> onSetField { it.copy(birthDate = event.birthDate) }
                is PetEditorEvent.SetGender -> onSetField { it.copy(gender = event.gender) }
                is PetEditorEvent.SetWeight -> onSetField { it.copy(weightKg = event.weight) }
                is PetEditorEvent.SetNotes -> onSetField { it.copy(notes = event.notes) }
                is PetEditorEvent.SetPhotoPath ->
                    onSetField { it.copy(photoPath = event.photoPath) }
                PetEditorEvent.Save -> onSave()
                PetEditorEvent.ConsumeSaveResult ->
                    _state.update { it.copy(saveResult = null) }
            }
        }

        private fun onSelectSpecies(event: PetEditorEvent.SelectSpecies) {
            _state.update {
                it.copy(
                    species = event.species,
                    // Смена species делает любую старую subcategory невалидной → обнуляем.
                    subcategory = null,
                    availableSubcategories =
                        PetEditorState.subcategoriesFor(event.species),
                    formErrorMessageKey = null,
                )
            }
        }

        /**
         * Универсальный setter с обнулением [PetEditorState.validationErrors] и
         * [PetEditorState.formErrorMessageKey] — пользователь начал править форму,
         * старые ошибки больше не показываем.
         */
        private fun onSetField(update: (PetEditorState) -> PetEditorState) {
            _state.update {
                update(it).copy(validationErrors = emptyList(), formErrorMessageKey = null)
            }
        }

        private fun onSave() {
            val snapshot = _state.value
            if (snapshot.isSaving) return
            val pet = validateAndBuildPet(snapshot) ?: return
            performSave(pet)
        }

        /**
         * Возвращает Pet, если форма прошла UI-уровень валидации; иначе обновляет
         * [PetEditorState] с соответствующей ошибкой и возвращает null.
         *
         * UI-валидация покрывает только то, что Pet-конструктор не может выразить через
         * non-null типы и `require(name.isNotBlank())`. Доменная валидация (BirthDateInFuture,
         * BirthDateUnrealistic) остаётся в [SavePetUseCase] — здесь мы её не дублируем.
         */
        private fun validateAndBuildPet(snapshot: PetEditorState): Pet? {
            val species = snapshot.species
            val birthDate = snapshot.birthDate
            val trimmedName = snapshot.name.trim()

            return when {
                species == null -> {
                    _state.update { it.copy(formErrorMessageKey = ERROR_SPECIES_REQUIRED) }
                    null
                }
                birthDate == null -> {
                    _state.update { it.copy(formErrorMessageKey = ERROR_BIRTH_DATE_REQUIRED) }
                    null
                }
                trimmedName.isEmpty() -> {
                    // Pet-конструктор бросил бы IAE на blank — перехватываем заранее и
                    // мапим в типизированную доменную ошибку NameBlank.
                    _state.update {
                        it.copy(validationErrors = listOf(PetValidationError.NameBlank))
                    }
                    null
                }
                else ->
                    Pet(
                        id = editingPetId ?: 0L,
                        name = trimmedName,
                        species = species,
                        birthDate = birthDate,
                        subcategory = snapshot.subcategory,
                        gender = snapshot.gender,
                        weightKg = parseWeight(snapshot.weightKg),
                        notes = snapshot.notes.ifBlank { null },
                        photoPath = snapshot.photoPath,
                    )
            }
        }

        private fun performSave(pet: Pet) {
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                try {
                    val id = savePet(pet)
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveResult = PetEditorSaveResult.Success(petId = id),
                            validationErrors = emptyList(),
                        )
                    }
                } catch (e: PetValidationException) {
                    _state.update {
                        it.copy(isSaving = false, validationErrors = e.errors)
                    }
                } catch (_: UnsupportedSpeciesException) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveResult =
                                PetEditorSaveResult.Error(
                                    messageKey = ERROR_UNSUPPORTED_SPECIES,
                                ),
                        )
                    }
                }
            }
        }

        private fun loadExisting(petId: Long) {
            viewModelScope.launch {
                try {
                    val pet = getPetById(petId)
                    if (pet == null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                formErrorMessageKey = ERROR_PET_NOT_FOUND,
                            )
                        }
                        return@launch
                    }
                    _state.update {
                        PetEditorState(
                            name = pet.name,
                            species = pet.species,
                            subcategory = pet.subcategory,
                            birthDate = pet.birthDate,
                            gender = pet.gender,
                            weightKg = pet.weightKg?.toString().orEmpty(),
                            notes = pet.notes.orEmpty(),
                            photoPath = pet.photoPath,
                            availableSubcategories =
                                PetEditorState.subcategoriesFor(pet.species),
                            isLoading = false,
                            editingPetId = petId,
                        )
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Структурированная отмена должна пробрасываться выше — иначе
                    // отменённая корутина «успешно» завершается, нарушая контракт scope'а.
                    throw e
                } catch (_: IllegalStateException) {
                    // PetMapper.toDomain бросает ISE при corrupt species/gender id из БД.
                    // Без этого catch'а форма зависнет в isLoading = true навсегда.
                    _state.update {
                        it.copy(isLoading = false, formErrorMessageKey = ERROR_LOAD_FAILED)
                    }
                } catch (_: RuntimeException) {
                    // Финальная защита от любых IO/маппер-исключений.
                    _state.update {
                        it.copy(isLoading = false, formErrorMessageKey = ERROR_LOAD_FAILED)
                    }
                }
            }
        }

        /**
         * Парсит строку weight в Double, возвращая null при невалидном вводе.
         * Допускает и точку, и запятую как десятичный разделитель (русская локаль).
         */
        private fun parseWeight(input: String): Double? {
            if (input.isBlank()) return null
            val normalized = input.replace(',', '.')
            return normalized.toDoubleOrNull()
        }

        private companion object {
            const val ERROR_SPECIES_REQUIRED: String = "pet_editor_error_species_required"
            const val ERROR_BIRTH_DATE_REQUIRED: String = "pet_editor_error_birth_date_required"
            const val ERROR_UNSUPPORTED_SPECIES: String = "pet_editor_error_unsupported_species"
            const val ERROR_PET_NOT_FOUND: String = "pet_editor_error_pet_not_found"
            const val ERROR_LOAD_FAILED: String = "pet_editor_error_load_failed"
        }
    }
