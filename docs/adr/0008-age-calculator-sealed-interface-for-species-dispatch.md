# 0008. AgeCalculator sealed interface for species dispatch

- **Status**: Accepted
- **Date**: 2026-05-28
- **Deciders**: @dnovichkov
- **Tags**: architecture, calculator, strategy-pattern

## Context and Problem Statement

Plan 1 реализовал расчёт возраста только для двух видов (Dog, Cat). `CalculatePetAgeUseCase`
содержал явный `when (pet.species) { Dog -> dogCalc..., Cat -> catCalc... }`. Plan 2 расширяет
приложение до **12 видов** (§12.1). При сохранении явного `when` в UseCase:

- UseCase превращается в god-object, который знает о каждой формуле и её параметрах;
- добавление вида требует правки UseCase (нарушение OCP/SRP);
- легко забыть ветку — компилятор не заставляет покрыть все виды в нескольких местах;
- параметры расчёта (метод для собак, тип для кошек, размер для кроликов) разнородны.

Нужен паттерн, который масштабируется на 12 видов, сохраняет type-safety и заставляет компилятор
проверять полноту.

## Decision Drivers

- **Расширяемость**: добавление вида = добавление одного `data object`, а не правка N мест.
- **Type-safety**: компилятор должен ловить пропущенный вид (exhaustive `when` по sealed).
- **Single Responsibility**: каждая формула живёт в своём калькуляторе; UseCase только диспатчит.
- **Тестируемость**: каждый калькулятор — pure-Kotlin `data object`, тестируется изолированно.
- **Отсутствие overkill**: 12 видов фиксированы; DI-registry `Map<Species, Calculator>` разрушил бы
  type-safety и был бы избыточен.

## Considered Options

1. **Sealed `AgeCalculator` interface + полиморфизм (Strategy)** — каждый вид реализует
   `data object X : AgeCalculator`; фабрика `forSpecies(Species): AgeCalculator?` диспатчит через
   exhaustive `when`.
2. **Явный `when (species)` в UseCase** — статус-кво Plan 1, не масштабируется.
3. **Registry `Map<Species, AgeCalculator>` через DI** — динамический lookup, теряет type-safety,
   overkill для фиксированного набора.

## Decision Outcome

Chosen option: **«Sealed `AgeCalculator` interface + Strategy»**, потому что это единственный вариант,
который одновременно даёт расширяемость (один `data object` на вид), compile-time exhaustiveness
(sealed `when` в `forSpecies`) и изолированную тестируемость, не вводя избыточный DI-механизм для
фиксированных 12 видов.

Параметры расчёта передаются через sealed `SpeciesParams` (`Dog(method, size)`, `Cat(type)`,
`Rabbit(size)`, …). Каждый калькулятор делает `require(params is SpeciesParams.X)` — несоответствие
вида и параметров трактуется как программерская ошибка (`IllegalArgumentException`). Парный
`LifeStageCalculator` следует тому же паттерну (один-к-одному с видом).

`forSpecies` возвращает `AgeCalculator?` (`null` для ещё не реализованных видов) — на этот контракт
опирался `CalculatePetAgeUseCase`, бросая `UnsupportedSpeciesException` при `null`. После Task 10
все 12 ветвей не-`null`, и `when` стал исчерпывающим без `else`.

### Positive Consequences

- Добавление вида (Tasks 2–10) свелось к шаблону: `data object XAgeCalculator : AgeCalculator` +
  ветка в `forSpecies` + тесты. UseCase не трогался.
- Компилятор требует ветку на каждый `Species` в обеих фабриках — пропуск вида = ошибка сборки.
- `CalculatePetAgeUseCase` больше не инжектирует калькуляторы — конструктор сократился до
  `settingsReader, clock`; `when` остался только в `resolveParams` (сборка `SpeciesParams`).
- `Mvp1CalculatorAcceptanceTest` (Task 24) подтверждает: `forSpecies(s) != null && .species == s`
  для всех 12 видов.

### Negative Consequences

- `SpeciesParams` + runtime `require(params is ...)` — небольшая «потеря» статической связи между
  калькулятором и его параметрами (компилятор не гарантирует, что вызывающий передал правильный тип).
  Митигация: фабрика `forSpecies` + `resolveParams` в одном месте собирают пару (calculator, params)
  согласованно; тесты `Dog calculator rejects Cat params` фиксируют контракт.
- `Species` (`:core:model`) и калькуляторы (`:core:calculator`) должны оставаться синхронными.
  Митигация: exhaustive `when` ломает сборку при рассинхроне.

## Pros and Cons of the Options

### Option 1: Sealed interface + Strategy (chosen)

- Good: расширяемость, compile-time exhaustiveness, изолированная тестируемость.
- Bad: `SpeciesParams` вводит runtime-проверку типа параметров.

### Option 2: Явный `when` в UseCase

- Good: нет дополнительных типов.
- Bad: god-object, нарушение SRP/OCP, правка UseCase на каждый вид.

### Option 3: DI Registry `Map<Species, Calculator>`

- Good: динамическая регистрация.
- Bad: теряет type-safety (`Map.get` → nullable без exhaustiveness), overkill для 12 фиксированных видов.

## Links

- spec §4 (формулы по видам), §12.1 (MVP v1.0 — 12 видов)
- `core/calculator/src/main/kotlin/app/pawclock/calculator/AgeCalculator.kt`
- `core/calculator/src/main/kotlin/app/pawclock/calculator/LifeStageCalculator.kt`
- Related: [ADR-0002](./0002-multi-module-architecture.md), [ADR-0006](./0006-wang-et-al-formula-as-default-for-dogs.md)
