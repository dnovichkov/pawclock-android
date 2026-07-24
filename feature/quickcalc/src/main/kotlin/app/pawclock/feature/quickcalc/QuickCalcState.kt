package app.pawclock.feature.quickcalc

import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.model.BirdType
import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.FishType
import app.pawclock.model.HamsterType
import app.pawclock.model.HorseType
import app.pawclock.model.RabbitSize
import app.pawclock.model.ReptileType
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * Состояние Quick Calculator (Task 20 / Plan 1, MVI).
 *
 * В отличие от PetEditor, состояние не сохраняется в БД — Quick Calculator считает
 * возраст ad-hoc и показывает результат в bottom sheet (§5.3 спецификации).
 *
 *  - [species] / [subcategory] / [birthDate] — поля формы;
 *  - [method] — выбранный метод расчёта для собак (Wang / SizeBased);
 *    для кошек игнорируется (AAFP — единственный метод);
 *  - [availableSubcategories] — derive'нутые из [species] (DogSize для Dog,
 *    CatType для Cat, пусто для not-implemented видов);
 *  - [result] — текущее состояние вычисления ([QuickCalcResult.Idle] до Calculate,
 *    [QuickCalcResult.Success] после успешного вычисления,
 *    [QuickCalcResult.ValidationError] если форма невалидна).
 *
 * Любое изменение полей формы (SetSpecies/SetSubcategory/SetBirthDate) сбрасывает
 * [result] обратно в Idle, чтобы UI не показывал устаревший результат.
 * Исключение — [QuickCalcEvent.SetMethod] на Success-состоянии: ViewModel пересчитывает
 * с новым методом БЕЗ возврата в Idle (UX-affordance §5.3 для Wang/Size toggle).
 */
data class QuickCalcState(
    val species: Species? = null,
    val subcategory: String? = null,
    val birthDate: LocalDate? = null,
    val method: CalculationMethod = CalculationMethod.EPIGENETIC,
    val availableSubcategories: List<QuickCalcSubcategoryOption> = emptyList(),
    val result: QuickCalcResult = QuickCalcResult.Idle,
) {
    companion object {
        val Empty: QuickCalcState = QuickCalcState()

        /**
         * Возвращает список подкатегорий для заданного species (Plan 2, Task 16 — все 12 видов).
         *
         * Pure-функция — exposing as companion delegate чтобы переиспользовать в onSelectSpecies.
         *
         * Виды без подкатегорий (GuineaPig, Rat, Mouse, Ferret — формула возраста едина) возвращают
         * emptyList → UI скрывает секцию подкатегории. `label` хранит английское enum-имя как
         * fallback; локализованная метка резолвится в UI через `(species, id)` (см.
         * [app.pawclock.feature.quickcalc.ui.section.QuickCalcSubcategorySelector]), потому что
         * `id` пересекаются между видами (например, `small`/`medium` у [DogSize] и [RabbitSize]).
         */
        fun subcategoriesFor(species: Species?): List<QuickCalcSubcategoryOption> =
            when (species) {
                Species.Dog -> DogSize.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Cat -> CatType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Rabbit -> RabbitSize.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Hamster -> HamsterType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Bird -> BirdType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Reptile -> ReptileType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Horse -> HorseType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.Fish -> FishType.entries.map { QuickCalcSubcategoryOption(it.id, it.name) }
                Species.GuineaPig,
                Species.Rat,
                Species.Mouse,
                Species.Ferret,
                null,
                -> emptyList()
            }
    }
}

/**
 * Опция подкатегории питомца для Quick Calculator UI.
 *
 * @property id стабильный id (DogSize.id / CatType.id) для передачи в CalculatePetAgeUseCase.
 * @property label английское enum-имя; локализация в Task 22 через stringResource.
 */
data class QuickCalcSubcategoryOption(
    val id: String,
    val label: String,
)

/**
 * Результат Quick Calculator: либо ничего не считалось, либо успех, либо ошибки валидации.
 */
sealed interface QuickCalcResult {
    /**
     * Стартовое состояние или после сброса формы — bottom sheet не показывается.
     */
    data object Idle : QuickCalcResult

    /**
     * Успешный расчёт — UI показывает bottom sheet с [calculatedAge].
     */
    data class Success(
        val calculatedAge: CalculatedAge,
    ) : QuickCalcResult

    /**
     * Невалидная форма — UI показывает inline-список ошибок над полями.
     */
    data class ValidationError(
        val errors: List<QuickCalcValidationError>,
    ) : QuickCalcResult
}

/**
 * Типизированные ошибки валидации Quick Calculator.
 *
 * @property messageKey ключ для UI-локализации (Task 22 заменит на stringResource).
 */
enum class QuickCalcValidationError(
    val messageKey: String,
) {
    SpeciesRequired(messageKey = "quick_calc_error_species_required"),
    BirthDateRequired(messageKey = "quick_calc_error_birth_date_required"),
    BirthDateInFuture(messageKey = "quick_calc_error_birth_date_in_future"),
    UnsupportedSpecies(messageKey = "quick_calc_error_unsupported_species"),
    UnexpectedError(messageKey = "quick_calc_error_unexpected"),
}
