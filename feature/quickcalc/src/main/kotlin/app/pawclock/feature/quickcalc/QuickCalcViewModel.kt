package app.pawclock.feature.quickcalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.domain.usecase.CalculatePetAgeUseCase
import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Pet
import app.pawclock.model.Species
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel Quick Calculator (Task 20 / Plan 1).
 *
 * Quick Calculator — это одноразовый расчёт возраста без сохранения питомца в БД
 * (§3.2 спецификации). UI собирает форму (species/subcategory/birthDate/method),
 * нажимает "Рассчитать", получает [CalculatedAge] в bottom sheet'е (§5.3).
 *
 * Архитектура:
 *  - state — `MutableStateFlow<QuickCalcState>`, мутируется через explicit `handleEvent`;
 *  - расчёт делегируется в [CalculatePetAgeUseCase] поверх synthetic [Pet] с
 *    placeholder-именем "__quickcalc__" (Pet не сохраняется, имя нигде не отображается);
 *  - валидация (species/birthDate required + future-date) выполняется на UI-уровне
 *    в [validate]; доменная защита от future-date остаётся в [CalculatePetAgeUseCase]
 *    как defense-in-depth.
 *
 * Reactive вычисление при SetMethod:
 *  - если предыдущий [QuickCalcResult] был Success — пересчитываем сразу
 *    с новым методом (UX-affordance: пользователь видит Wang→Size toggle мгновенно);
 *  - если Idle/ValidationError — просто обновляем method в state, без вычислений
 *    (нет данных для расчёта).
 */
@HiltViewModel
class QuickCalcViewModel
    @Inject
    constructor(
        private val calculatePetAge: CalculatePetAgeUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(QuickCalcState.Empty)
        val state: StateFlow<QuickCalcState> = _state.asStateFlow()

        fun handleEvent(event: QuickCalcEvent) {
            when (event) {
                is QuickCalcEvent.SelectSpecies -> onSelectSpecies(event.species)
                is QuickCalcEvent.SetSubcategory -> onSetSubcategory(event.subcategory)
                is QuickCalcEvent.SetBirthDate -> onSetBirthDate(event.birthDate)
                is QuickCalcEvent.SetMethod -> onSetMethod(event.method)
                QuickCalcEvent.Calculate -> performCalculation(_state.value)
            }
        }

        private fun onSelectSpecies(species: Species) {
            _state.update {
                it.copy(
                    species = species,
                    // Смена species делает любую старую subcategory невалидной.
                    subcategory = null,
                    availableSubcategories = QuickCalcState.subcategoriesFor(species),
                    result = QuickCalcResult.Idle,
                )
            }
        }

        private fun onSetSubcategory(subcategory: String?) {
            _state.update {
                it.copy(subcategory = subcategory, result = QuickCalcResult.Idle)
            }
        }

        private fun onSetBirthDate(birthDate: LocalDate) {
            _state.update {
                it.copy(birthDate = birthDate, result = QuickCalcResult.Idle)
            }
        }

        private fun onSetMethod(method: CalculationMethod) {
            val snapshot = _state.value
            val previousResult = snapshot.result
            _state.update { it.copy(method = method) }
            // Если у нас был успешный результат — пересчитываем сразу с новым методом.
            // Это нужно для Wang/Size toggle на bottom sheet'е (§5.3, §3.2.UX).
            if (previousResult is QuickCalcResult.Success) {
                performCalculation(_state.value)
            }
        }

        /**
         * Валидирует форму и выполняет расчёт через [CalculatePetAgeUseCase].
         *
         * Дефолтит subcategory к Medium (Dog) / IndoorShortHair (Cat), если пользователь
         * не выбрал — это делает UX мягче (можно «погуглить» возраст не выбирая подкатегорию)
         * и совпадает с дефолтом самого UseCase. Subcategory нужна для построения Pet'а.
         */
        private fun performCalculation(snapshot: QuickCalcState) {
            val errors = validate(snapshot)
            if (errors.isNotEmpty()) {
                _state.update { it.copy(result = QuickCalcResult.ValidationError(errors)) }
                return
            }

            val species = snapshot.species
            val birthDate = snapshot.birthDate
            // validate() гарантирует non-null species и birthDate.
            requireNotNull(species) { "species must be non-null after validation passes" }
            requireNotNull(birthDate) { "birthDate must be non-null after validation passes" }

            val resolvedSubcategory = snapshot.subcategory ?: defaultSubcategoryFor(species)
            val syntheticPet =
                Pet(
                    id = 0L,
                    name = QUICK_CALC_PLACEHOLDER_NAME,
                    species = species,
                    birthDate = birthDate,
                    subcategory = resolvedSubcategory,
                )
            val methodOverride = methodOverrideFor(species, snapshot.method)

            viewModelScope.launch {
                runCatching {
                    calculatePetAge(pet = syntheticPet, methodOverride = methodOverride)
                }.onSuccess { calculatedAge ->
                    _state.update {
                        it.copy(result = QuickCalcResult.Success(calculatedAge))
                    }
                }.onFailure { throwable ->
                    _state.update { it.copy(result = mapThrowableToResult(throwable)) }
                }
            }
        }

        private fun validate(snapshot: QuickCalcState): List<QuickCalcValidationError> =
            buildList {
                if (snapshot.species == null) {
                    add(QuickCalcValidationError.SpeciesRequired)
                }
                if (snapshot.birthDate == null) {
                    add(QuickCalcValidationError.BirthDateRequired)
                } else if (snapshot.birthDate.isAfter(LocalDate.now())) {
                    // Fast-fail на UI-уровне для better UX; UseCase сделает то же на доменном уровне.
                    add(QuickCalcValidationError.BirthDateInFuture)
                }
            }

        private fun mapThrowableToResult(throwable: Throwable): QuickCalcResult =
            when (throwable) {
                is UnsupportedSpeciesException ->
                    QuickCalcResult.ValidationError(
                        listOf(QuickCalcValidationError.UnsupportedSpecies),
                    )
                is IllegalArgumentException ->
                    // Защита от future-date — мы проверили в validate(), но clock на UseCase
                    // мог дать другую сегодняшнюю дату (тест с fixedClock). Маппим в типизированную.
                    QuickCalcResult.ValidationError(
                        listOf(QuickCalcValidationError.BirthDateInFuture),
                    )
                else ->
                    // Любое неожиданное исключение (например, IO-сбой при загрузке care-ассетов)
                    // не должно крашить приложение из viewModelScope. Превращаем в общий
                    // validation error — UI отобразит безопасный fallback вместо краша.
                    QuickCalcResult.ValidationError(
                        listOf(QuickCalcValidationError.UnexpectedError),
                    )
            }

        private fun defaultSubcategoryFor(species: Species): String =
            when (species) {
                Species.Dog -> DogSize.Medium.id
                Species.Cat -> CatType.IndoorShortHair.id
                else -> ""
            }

        /**
         * Для собак передаём явный method override (Wang или SizeBased).
         * Для кошек — null: метод фиксирован AAFP'ом и игнорируется UseCase'ом.
         */
        private fun methodOverrideFor(
            species: Species,
            method: CalculationMethod,
        ): CalculationMethod? = if (species == Species.Dog) method else null

        private companion object {
            /**
             * Placeholder-имя для synthetic Pet'а в Quick Calculator. Никогда не отображается
             * (Pet не сохраняется и UI не показывает name из synthetic Pet'а).
             * Используется только чтобы пройти `require(name.isNotBlank())` в Pet-конструкторе.
             */
            const val QUICK_CALC_PLACEHOLDER_NAME: String = "__quickcalc__"
        }
    }
