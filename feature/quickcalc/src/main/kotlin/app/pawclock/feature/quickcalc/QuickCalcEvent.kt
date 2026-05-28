package app.pawclock.feature.quickcalc

import app.pawclock.model.CalculationMethod
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * События UI Quick Calculator (Task 20 / Plan 1, MVI).
 *
 * Каждое событие — иммутабельное намерение пользователя; [QuickCalcViewModel.handleEvent]
 * мапит его в новый [QuickCalcState]. Вычислительный side-effect происходит только
 * в обработчике [Calculate] (а также при [SetMethod] поверх существующего Success — для
 * Wang/Size toggle без явного нажатия "Рассчитать").
 */
sealed interface QuickCalcEvent {
    data class SelectSpecies(
        val species: Species,
    ) : QuickCalcEvent

    /**
     * @param subcategory `null` — снять выбор; иначе — стабильный id (DogSize.id / CatType.id).
     */
    data class SetSubcategory(
        val subcategory: String?,
    ) : QuickCalcEvent

    data class SetBirthDate(
        val birthDate: LocalDate,
    ) : QuickCalcEvent

    /**
     * Переключение метода расчёта (Wang / SizeBased). Применяется только к собакам;
     * для кошек метод фиксирован (AAFP/AAHA), и событие записывает выбранный метод в
     * state, но result-расчёт остаётся [CalculationMethod.EPIGENETIC] (как метка
     * в [CalculatedAge.method]).
     */
    data class SetMethod(
        val method: CalculationMethod,
    ) : QuickCalcEvent

    /**
     * Триггер расчёта. ViewModel валидирует форму и публикует [QuickCalcResult.Success]
     * или [QuickCalcResult.ValidationError].
     */
    data object Calculate : QuickCalcEvent

    /**
     * Сброс result в [QuickCalcResult.Idle] — закрывает result-sheet'у при swipe-down
     * или нажатии вне sheet'ы. Без него ModalBottomSheet анимируется к hidden, но
     * recomposition заново показывает sheet'у, потому что `state.result` остаётся Success.
     */
    data object DismissResult : QuickCalcEvent
}
