package app.pawclock.model

/**
 * Метод расчёта возраста животного в человеческих годах.
 *
 * Помещён в `:core:model` (а не в `:core:calculator`), потому что используется
 * не только реализациями калькуляторов, но и:
 *  - `:core:datastore` — пользовательская настройка `AppSettings.defaultCalculationMethod`;
 *  - `:core:domain` — UseCase читает дефолтный метод из настроек;
 *  - `:feature:quickcalc` — UI-toggle Wang/Size для собак.
 *
 * Хранится в DataStore Preferences по стабильному имени enum-константы
 * (`enumValueOf<CalculationMethod>(name)`), поэтому переименование констант — breaking change
 * для миграций. См. ADR-0006 (Wang et al. формула как default для собак) и
 * спецификацию PawClock §4.1.
 */
enum class CalculationMethod {
    /**
     * Эпигенетическая формула.
     *
     * Для собак: Wang T. et al., Cell Systems 2020 — `ЧГ = 16 · ln(age) + 31`
     * (DOI: 10.1016/j.cels.2020.06.006).
     */
    EPIGENETIC,

    /**
     * Табличный размерный метод.
     *
     * Для собак: AKC/AAHA 2019 — табличные значения, зависящие от размерного класса
     * (Toy/Small/Medium/Large/Giant).
     */
    SIZE_BASED,
}
