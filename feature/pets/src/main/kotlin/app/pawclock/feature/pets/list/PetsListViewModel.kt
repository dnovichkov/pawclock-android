package app.pawclock.feature.pets.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.domain.usecase.GetPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel экрана списка питомцев (Task 18 / Plan 1).
 *
 * Реактивно наблюдает за репозиторием через [GetPetsUseCase] и публикует
 * [PetsListState] в `state` для подписки UI.
 *
 * Поведение:
 *  1. Стартовое состояние — [PetsListState.Loading] (через `stateIn(initialValue = Loading)`);
 *  2. Каждая эмиссия из репозитория классифицируется:
 *     - пустой список → [PetsListState.Empty];
 *     - непустой → [PetsListState.Success];
 *     - исключение → [PetsListState.Error] (использует `catch`, чтобы не убить collector);
 *  3. `stateIn(SharingStarted.WhileSubscribed(5000))` — стандартный паттерн Compose'а:
 *     удерживает Flow активным 5с после отписки UI, чтобы пережить configuration change.
 *
 * Тестируется как чистый StateFlow-machine (см. [PetsListViewModelTest]).
 */
@HiltViewModel
class PetsListViewModel
    @Inject
    constructor(
        private val getPets: GetPetsUseCase,
    ) : ViewModel() {
        val state: StateFlow<PetsListState> =
            getPets()
                .map<List<app.pawclock.model.Pet>, PetsListState> { pets ->
                    if (pets.isEmpty()) PetsListState.Empty else PetsListState.Success(pets)
                }.onStart { /* первая эмиссия запросит данные; до неё state = Loading */ }
                .catch { emit(PetsListState.Error(messageKey = ERROR_KEY)) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STATE_RETENTION_MS),
                    initialValue = PetsListState.Loading,
                )

        private companion object {
            const val STATE_RETENTION_MS: Long = 5_000L
            const val ERROR_KEY: String = "pets_list_error_loading"
        }
    }
