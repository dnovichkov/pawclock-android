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
                    val recommendation = loadCareRecommendation(pet.species, calculated.lifeStage)
                    _state.value =
                        PetDetailState.Success(
                            pet = pet,
                            calculatedAge = calculated,
                            careRecommendation = recommendation,
                        )
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Структурированная отмена корутины должна пробрасываться ДО любых других
                    // catch'ей. CancellationException на JVM наследуется от IllegalStateException,
                    // поэтому если catch (IllegalStateException) поставить выше — он съест отмену.
                    throw e
                } catch (_: UnsupportedSpeciesException) {
                    _state.value = PetDetailState.Error(messageKey = ERROR_UNSUPPORTED_SPECIES)
                } catch (_: IllegalArgumentException) {
                    // calendarAgeInYears бросает IAE если birthDate в будущем — defensive:
                    // должен быть пойман валидацией в SavePetUseCase, но защищаемся.
                    _state.value = PetDetailState.Error(messageKey = ERROR_INVALID_BIRTH_DATE)
                } catch (_: IllegalStateException) {
                    // PetMapper.toDomain бросает ISE при corrupt species/gender id из БД
                    // (например, после миграции которая переименовала константы). Без этого
                    // catch'а корутина падает в default-handler и UI остаётся в Loading.
                    _state.value = PetDetailState.Error(messageKey = ERROR_DATA_CORRUPTED)
                } catch (_: RuntimeException) {
                    // Финальная защита: любая другая ошибка из use case'ов / mapper'а / IO
                    // не должна крашить ViewModel-корутину. Маппим в общий error-state.
                    _state.value = PetDetailState.Error(messageKey = ERROR_UNEXPECTED)
                }
            }
        }

        /**
         * Загружает рекомендации по уходу, не блокируя расчёт возраста при IO-ошибке.
         * Вынесено отдельным методом, чтобы явный try/catch перехватывал только Throwable
         * use case'а и пробрасывал CancellationException — `runCatching` ловит её и
         * нарушает структурированную отмену.
         */
        private suspend fun loadCareRecommendation(
            species: app.pawclock.model.Species,
            stage: app.pawclock.model.LifeStage,
        ): app.pawclock.model.CareRecommendation? =
            try {
                getCareRecommendations(
                    species = species,
                    stage = stage,
                    locale = localeProvider.current(),
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: RuntimeException) {
                null
            }

        private companion object {
            const val ERROR_UNSUPPORTED_SPECIES: String = "pet_detail_error_unsupported_species"
            const val ERROR_INVALID_BIRTH_DATE: String = "pet_detail_error_invalid_birth_date"
            const val ERROR_DATA_CORRUPTED: String = "pet_detail_error_data_corrupted"
            const val ERROR_UNEXPECTED: String = "pet_detail_error_unexpected"
        }
    }
