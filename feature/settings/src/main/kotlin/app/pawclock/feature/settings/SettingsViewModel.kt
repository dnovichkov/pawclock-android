package app.pawclock.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.datastore.AppSettings
import app.pawclock.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel экрана настроек (Task 21 / Plan 1).
 *
 * Архитектура — read-through proxy для [SettingsRepository]:
 *  - [state] derived из `repository.observe()` через `map → stateIn`;
 *  - [handleEvent] делегирует в соответствующий suspend setter через
 *    `viewModelScope.launch { }` (fire-and-forget — UI наблюдает observe() для актуального
 *    значения; задержка DataStore.edit ≈10-50ms, для UX незаметна);
 *  - DataStore не выбрасывает исключений на успешный edit, поэтому отдельный error state
 *    не нужен; corrupted-values уже маскирует SettingsRepositoryImpl (см. Task 13).
 *
 * Параметр конструктора `repository: SettingsRepository` — Hilt-инжектируется через
 * SettingsRepositoryModule (см. `:core:datastore/di/DataStoreModule.kt`).
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) : ViewModel() {
        val state: StateFlow<SettingsState> =
            repository
                .observe()
                .map { it.toSettingsState() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = SettingsState.Default,
                )

        fun handleEvent(event: SettingsEvent) {
            when (event) {
                is SettingsEvent.SetThemeMode ->
                    viewModelScope.launch { repository.setThemeMode(event.themeMode) }
                is SettingsEvent.SetLanguageTag ->
                    viewModelScope.launch { repository.setLanguageTag(event.languageTag) }
                is SettingsEvent.SetDynamicColor ->
                    viewModelScope.launch { repository.setDynamicColor(event.enabled) }
                is SettingsEvent.SetDefaultCalculationMethod ->
                    viewModelScope.launch { repository.setDefaultCalculationMethod(event.method) }
            }
        }

        private fun AppSettings.toSettingsState(): SettingsState =
            SettingsState(
                themeMode = themeMode,
                languageTag = languageTag,
                dynamicColor = dynamicColor,
                defaultCalculationMethod = defaultCalculationMethod,
            )

        private companion object {
            /**
             * Таймаут перед отменой upstream-Flow когда последний subscriber отписался.
             * 5 секунд — стандартное значение для WhileSubscribed: переживает rotation
             * без перезаписи DataStore-state, но останавливает collection при background.
             */
            const val SUBSCRIPTION_TIMEOUT_MS: Long = 5_000L
        }
    }
