package app.pawclock.feature.pets.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.domain.usecase.CalculatePetAgeUseCase
import app.pawclock.domain.usecase.GetCareRecommendationsUseCase
import app.pawclock.domain.usecase.GetPetByIdUseCase
import app.pawclock.feature.pets.common.LocaleProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel экрана детального просмотра питомца (Task 18 / Plan 1).
 *
 * Аггрегирует три источника данных в одно состояние:
 *  - [GetPetByIdUseCase] — одноразовая suspend-загрузка питомца по id;
 *  - [CalculatePetAgeUseCase] — расчёт возраста (Wang/SIZE_BASED для собак, AAFP для кошек)
 *    и стадии жизни (AAHA 2019 / AAFP 2021);
 *  - [GetCareRecommendationsUseCase] — рекомендации по уходу для текущей стадии.
 *
 * Аргумент `petId` извлекается из [SavedStateHandle] (Navigation Compose 2.8+ typesafe
 * route encode'ит параметры под ключами полей класса — см. `Route.PetDetail(petId)`).
 * Если ключ отсутствует — переход в [PetDetailState.NotFound] (defense-in-depth).
 *
 * `localeProvider` — параметр для тестируемости (production: `{ Locale.getDefault().language }`).
 * Использование Locale.getDefault() напрямую в init { } сделало бы тесты flaky на CI
 * с не-ru system locale.
 */
@HiltViewModel
class PetDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getPetById: GetPetByIdUseCase,
        private val calculatePetAge: CalculatePetAgeUseCase,
        private val getCareRecommendations: GetCareRecommendationsUseCase,
        private val localeProvider: LocaleProvider,
    ) : ViewModel() {
        private val _state = MutableStateFlow<PetDetailState>(PetDetailState.Loading)
        val state: StateFlow<PetDetailState> = _state.asStateFlow()

        init {
            val petId: Long? = savedStateHandle.get<Long>("petId")
            if (petId == null) {
                _state.value = PetDetailState.NotFound
            } else {
                loadDetail(petId)
            }
        }

        private fun loadDetail(petId: Long) {
            viewModelScope.launch {
                try {
                    val pet = getPetById(petId)
                    if (pet == null) {
                        _state.value = PetDetailState.NotFound
                        return@launch
                    }
                    val calculated = calculatePetAge(pet = pet, methodOverride = null)
                    val recommendation =
                        runCatching {
                            getCareRecommendations(
                                species = pet.species,
                                stage = calculated.lifeStage,
                                locale = localeProvider.current(),
                            )
                        }.getOrNull()
                    _state.value =
                        PetDetailState.Success(
                            pet = pet,
                            calculatedAge = calculated,
                            careRecommendation = recommendation,
                        )
                } catch (_: UnsupportedSpeciesException) {
                    _state.value = PetDetailState.Error(messageKey = ERROR_UNSUPPORTED_SPECIES)
                } catch (_: IllegalArgumentException) {
                    // calendarAgeInYears бросает IAE если birthDate в будущем — defensive:
                    // должен быть пойман валидацией в SavePetUseCase, но защищаемся.
                    _state.value = PetDetailState.Error(messageKey = ERROR_INVALID_BIRTH_DATE)
                }
            }
        }

        private companion object {
            const val ERROR_UNSUPPORTED_SPECIES: String = "pet_detail_error_unsupported_species"
            const val ERROR_INVALID_BIRTH_DATE: String = "pet_detail_error_invalid_birth_date"
        }
    }
