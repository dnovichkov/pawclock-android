# PawClock — Plan 2: All Remaining Species + Export/Import (MVP v1.0 completion)

## Overview

Второй план для проекта **PawClock**. Plan 1 (`2026-05-27-pawclock-foundation-and-dog-cat-mvp.md`) завершил foundation + MVP для собак и кошек. Plan 2 расширяет приложение до **полного MVP v1.0** по §12.1 спецификации: добавляет оставшиеся 10 групп животных и реализует экспорт/импорт данных (§3.5).

**Что входит в план:**

1. **Архитектурный refactor:** введение `AgeCalculator` sealed interface в `:core:domain`, миграция `DogAgeCalculator` / `CatAgeCalculator` под него — единая точка диспатча для `CalculatePetAgeUseCase` (выбран пользователем как Strategy pattern, см. §"Approaches discussed" ниже).
2. **10 новых калькуляторов возраста с life stages (строгий TDD):**
   - `RabbitAgeCalculator` — кусочная формула (§4.3, House Rabbit Society + AVMA)
   - `HamsterAgeCalculator` — кусочная формула (§4.4, RVC + Animallama)
   - `GuineaPigAgeCalculator` — кусочная формула (§4.5, Oxbow + Animallama)
   - `RatAgeCalculator` — Sengupta 2013 линейная формула `13.8·age + 1.4` (§4.6)
   - `MouseAgeCalculator` — Dutta & Sengupta 2016 piecewise human-days/mouse-day (§4.6)
   - `FerretAgeCalculator` — кусочная формула (§4.7, PMC «Senior Ferret»)
   - `BirdAgeCalculator` — scalar ratio `ЧГ = age · 80 / lifespan` с 1.3× модификатором для возрастов < 6 месяцев (§4.8, AAV)
   - `ReptileAgeCalculator` — scalar ratio с 4 фазами (§4.9, Petplace + Reptile Centre)
   - `HorseAgeCalculator` — 3-фазная формула AAEP (§4.10)
   - `FishAgeCalculator` — scalar ratio (§4.11, PetMD + AquariumStoreDepot)
3. **Подкатегории видов:** новые sealed/enum типы (`RabbitSize`, `HamsterType`, `BirdType`, `ReptileType`, `FishType`, `HorseType`) в `:core:model` со стабильными `id` для DB/JSON сериализации.
4. **LifeStage expansion:** добавление подтипов `LifeStage.Rabbit`, `.Hamster`, `.GuineaPig`, `.Rat`, `.Mouse`, `.Ferret`, `.Bird`, `.Reptile`, `.Horse`, `.Fish` по §4.
5. **Property-based testing** для всех новых калькуляторов (monotonicity, positivity, continuity на границах кусочных формул).
6. **Database migration:** `MIGRATION_1_2` для расширения subcategory column'а — проверяется через MigrationTestHelper.
7. **Care recommendations placeholder JSON:** 10 видов × N стадий × ru/en (~80-100 файлов с TODO-контентом + обязательный дисклеймер из §3.3).
8. **Stylized species icons:** замена emoji 🐶/🐱 placeholder на векторные SVG-иконки для всех 12 видов (Phosphor / Lucide стиль, §5.6).
9. **UI расширение:** PetEditor + QuickCalculator поддерживают все 12 видов и их подкатегории; UI работает на all `Species.implemented()` без хардкодов.
10. **Export/Import (§3.5):**
    - `ExportPetsUseCase` — сериализация всех Pet'ов в JSON (kotlinx.serialization) и CSV
    - `ImportPetsUseCase` — десериализация + validation + merge/replace стратегия
    - UI в `:feature:settings` — кнопки Export / Import через SAF (`ACTION_CREATE_DOCUMENT`, `ACTION_OPEN_DOCUMENT`), Activity Result API
11. **Локализация:** добавление новых строк для всех 10 видов + подкатегории + стадий в `values/strings.xml` (ru) и `values-en/strings.xml`; plurals остаются совместимыми с Plan 1.
12. **E2E:** новые Maestro flows (quick calc с экзотикой; export/import roundtrip); обновление существующих flows если они нарушены.

**Что НЕ входит в этот план (отложено на Plan 3+):**

- Реальный научный контент care/*.json (сейчас placeholder во всех видах) — отдельный content-task pass с привлечением vet-источников §14
- Photo picker для фото питомца (выбрано пользователем как opt-out в Plan 2; SVG-иконок достаточно по §5.6)
- Виджеты Glance (v1.1 по §12.2)
- F-Droid публикация (v1.1 по §12.2)
- Локали de/es (v1.1 по §12.2)
- Roborazzi screenshot tests как блокирующий CI check (v1.1 по §12.2)
- WorkManager уведомления, journal здоровья, графики Vico, Wear OS (v2.0 по §12.3)
- Mutation testing (Pitest), Baseline Profiles (Plan 3+)

**Проблема, которую решает план:** довести проект от 2-видного MVP до **полного MVP v1.0**, готового к internal testing → alpha → beta → production в Google Play (§8.5.2 release.yml workflow готов из Plan 1). После Plan 2 проект соответствует USP «самый широкий список видов среди бесплатных Android-калькуляторов» (§1.3) и поддерживает миграцию данных между устройствами (§3.5).

### Approaches discussed (Step 1.5)

Пользователь выбрал **Strategy pattern через sealed `AgeCalculator` interface** среди трёх альтернатив:

1. ✓ **Sealed AgeCalculator + полиморфизм (Strategy)** — выбрано. UseCase зависит от sealed-set реализаций, exhaustive `when` на компиляции, расширение через добавление нового data class. Plan 1 калькуляторы migrируются под общий интерфейс на Task 1.
2. ✗ Явный `when (species)` в UseCase — отвергнуто: при 12 видах UseCase становится god-object, нарушение SRP.
3. ✗ Registry pattern `Map<Species, Calculator>` через DI — отвергнуто: overkill для фиксированных 12 видов, разрушает type-safety.

Пользователь также выбрал **отсутствие Photo Picker** (только SVG-иконки видов) и **placeholder care content** (реальный контент — отдельный pass после Plan 2).

## Context (from discovery)

**Текущее состояние репозитория (после Plan 1):**

- **12 Gradle модулей** скомпилированы, тесты зелёные: `:app`, `:core:{designsystem,model,calculator,database,datastore,domain,testing}`, `:feature:{pets,editor,quickcalc,settings}`
- **`:core:calculator`** содержит `DogAgeCalculator`, `DogLifeStageCalculator`, `CatAgeCalculator`, `CatLifeStageCalculator`, `DogSizeTable`, `DogLifeStageThresholds`, `CatLifeStageThresholds`, `ExpectedLifespan`; покрытие **95.9%** (kover minBound=95 enforce'ит)
- **`:core:domain`** содержит `CalculatePetAgeUseCase`, `GetPetsUseCase`, `SavePetUseCase`, `DeletePetUseCase`, `GetCareRecommendationsUseCase`, `GetPetByIdUseCase`, `AgePluralFormatter`; покрытие **98.1%** (kover minBound=90)
- **`:core:model`** содержит `Species` (sealed, **все 12 видов уже объявлены**, `isImplemented = true` только у Dog/Cat), `DogSize`, `CatType`, `Gender`, `LifeStage` (только Dog/Cat подтипы), `Pet`, `CareRecommendation`, `CalculationMethod`, `ThemeMode`
- **`:core:database`** Room 2.8.4 + KSP, `PetEntity` (id, name, species_id, subcategory, birth_date_iso, gender_id, weight_kg, notes, photo_path), `PetDao`, `PawClockDatabase` v1, `PetMapper` (Pet↔PetEntity), schema export в `core/database/schemas/`
- **`:core:datastore`** DataStore Preferences с `SettingsRepository` (themeMode, language, dynamicColor, defaultCalculationMethod)
- **`:core:designsystem`** Material You + dynamic colors + fallback palette + Roborazzi screenshot tests (opt-in)
- **`:feature:pets`** PetsList (state-based: Loading/Empty/Success/Error) + PetDetail (использует CalculatePetAgeUseCase + GetCareRecommendationsUseCase)
- **`:feature:editor`** PetEditor с MVI (SelectSpecies → availableSubcategories), Save FAB
- **`:feature:quickcalc`** QuickCalculator с method toggle (Wang/Size) для собак, AAFP для кошек
- **`:feature:settings`** Settings + About; ThemeMode + Language + DynamicColor + CalculationMethod toggles
- **CI/CD:** 4 workflows (ci.yml, lint.yml, release.yml, nightly.yml), 7 ADR, документация (README, ARCHITECTURE, TESTING, CONTRIBUTING, RELEASE), Maestro flows: `create_first_pet.yaml`, `quick_calc_dog.yaml`
- **Care recommendations:** **только** `app/src/main/assets/care/{dog,cat}/{puppy|kitten,young_adult,mature_adult,senior,end_of_life}/{ru,en}.json` (20 файлов с TODO-placeholder контентом)
- **AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2026.05.00, Material3 1.4.0, Navigation 2.9.0, Room 2.8.4, AppCompat 1.7.0**

**Ключевые формулы для реализации (см. §4 спецификации):**

| Вид | Формула | Источник |
|---|---|---|
| Rabbit | Piecewise: 0-4мес=30·age, 4-12мес=12+8·(age-0.33), 1+г=21+6·(age-1) | House Rabbit Society + AVMA |
| Hamster | Piecewise: 0-1мес=1/4дн, 1-2мес≈18, 2-6мес≈30, 6-18мес=+3/мес, >18мес=+4/мес | RVC + Animallama |
| GuineaPig | Piecewise: 0-3нед=0.5, 3нед≈6, 2-3мес≈11.5, 4-5мес≈20, >5мес=+8/год, >4г=+10/год | Oxbow + Animallama |
| Rat | Linear: ЧГ = 13.8·age + 1.4 | Sengupta 2013, Int J Prev Med 4(6):624-630 |
| Mouse | Piecewise: 1-42дн=150ЧДней/день, 42-180=45/день, 180-365=30/день, 365-730=25/день, >730=20/день | Dutta & Sengupta 2016, Life Sciences 152:244-248 |
| Ferret | Piecewise: 0-6мес=+5/мес, 6мес≈30, 1г≈40, >1г=+4/год | PMC «Senior Ferret» PMC7129291 |
| Bird | Scalar ratio: ЧГ = age · (80 / lifespan_per_species), × 1.3 для age < 6мес | AAV + Lafeber Vet |
| Reptile | Scalar ratio: ЧГ = age · (80 / lifespan_per_species), 4 фазы | PetPlace + Reptile Centre |
| Horse | 3-фаза: 1г=6.5, 2г=13, 3г=18, 4г=20.5, >4г=+2.5/год | AAEP + PetMD |
| Fish | Scalar ratio: ЧГ = age · (80 / lifespan_per_species) | PetMD + AquariumStoreDepot |

**Эталонные тестовые значения (для Red-фазы TDD):**

- Rabbit 1y = 21 ЧГ; 2y = 27; 5y = 45; 8y = 63
- Hamster 6мес = 30; 12мес ≈ 48; 18мес ≈ 66
- GuineaPig 6мес = 28; 2y = 36; 5y ≈ 60; 7y ≈ 80
- Rat 1y = 15.2; 2y = 29; 3y = 42.8
- Mouse 30дн ≈ 6.6 (30·150=4500/365=12.3 ЧГ — пересчитать: actually 30·150 ЧДней / 365 ≈ 12.33 ЧЛет); 200дн ≈ 21 ЧГ
- Ferret 6мес = 30; 1y = 40; 3y = 48; 5y = 56
- Bird budgerigar (lifespan 7 лет): 1y = 11.4 ЧГ; macaw (lifespan 50): 10y = 16
- Reptile turtle (lifespan 40): 5y = 10; iguana (lifespan 20): 5y = 20
- Horse 1y = 6.5; 4y = 20.5; 10y = 35.5; 20y = 60.5
- Fish goldfish (lifespan 15): 5y ≈ 26.7; koi (lifespan 30): 10y ≈ 26.7

**Стек технологий не меняется по сравнению с Plan 1** (см. §7.1 спеки и `gradle/libs.versions.toml`). Возможные bump'ы во время Plan 2 фиксируются как ➕ task'и.

## Development Approach

- **Testing approach: TDD (tests first)** — обязательно для всех калькуляторов (§11.1), Repository/UseCase в `:core:domain`, любой бизнес-логики Export/Import (см. [[feedback_tdd_workflow]]). Цикл Red → Green → Refactor строго.
- Complete each task fully before moving to the next.
- Make small, focused changes — один калькулятор = одна задача.
- **CRITICAL: every task MUST include new/updated tests** для изменений в этой задаче.
  - tests не optional — обязательная часть чек-листа
  - unit tests для новых функций/методов
  - параметризованные тесты для табличных данных §4
  - property-based tests (Kotest) для математических свойств — отдельной задачей в конце калькуляторного блока
  - tests covers success + error scenarios
- **CRITICAL: all tests must pass before starting next task** — без исключений.
- **CRITICAL: update this plan file when scope changes** (с пометкой ➕ для discovered tasks или ⚠️ для blockers).
- Run tests after each change.
- Maintain backward compatibility — Plan 1 API не ломаем; Room migration `MIGRATION_1_2` пишется только если меняется schema.
- **TDD-discipline для :core:calculator (поддерживаем ≥95% coverage):** для каждой новой формулы — сначала падающий тест с эталонным значением из спецификации (см. таблицу выше), затем минимально работающая реализация, затем параметризованные тесты, затем edge cases, затем property-based проверки в Kotest. Только потом — следующий вид.
- **KDoc для каждой формулы ОБЯЗАТЕЛЬНО** содержит ссылку на первоисточник (DOI, URL, либо имя стандарта). Дублирующие источники из §14 спецификации.
- Никаких `Thread.sleep` в тестах. Только `runTest` + `TestDispatcher`. Никаких реальных дат — `Clock.fixed(...)`.
- **AgeCalculator refactor (Task 1) выполняется первым** — это гарантирует, что все последующие виды добавляются по единому паттерну и UseCase не нужно править N раз.
- **Care content placeholder** — каждый JSON содержит обязательные поля по §3.3 + дисклеймер; реальный научный текст помечен `TODO(content-pass-after-plan-2)`.

## Testing Strategy

- **Unit tests (JVM, JUnit 5 + kotlin.test):** обязательны для каждой задачи. Целевое покрытие по §11.4:
  - `:core:calculator` ≥ 95% (поддерживаем; новые виды добавляют code → нужно добавлять тесты пропорционально)
  - `:core:domain` ≥ 90% (Export/Import UseCases вносят новый код)
  - `:core:database` ≥ 80% (migration tests, новая колонка subcategory)
  - `:feature:*` ViewModels ≥ 80%
- **Property-based tests (Kotest):** для каждого нового калькулятора — monotonicity, positivity; для кусочных формул — continuity на границах. Группируется в Task 11 "property tests for all species".
- **Integration tests (androidTest):** `:core:database` — migration test `MIGRATION_1_2` через `MigrationTestHelper`; in-memory Room для CRUD проверки subcategory roundtrip.
- **Compose UI tests:** PetEditor / QuickCalculator демонстрируют все 12 видов в species selector; ValidationErrorsBanner показывает локализованные ошибки.
- **E2E flow (Maestro):**
  - `maestro/quick_calc_rabbit.yaml` (новый) — экзотика, проверка subcategory dropdown
  - `maestro/export_import_roundtrip.yaml` (новый) — экспорт всех питомцев → импорт обратно → assertVisible all
  - Существующие flows из Plan 1 — sanity check, обновить selectors если они изменились
- **Screenshot tests (Roborazzi):** не блокирующий чек (по решению Plan 1) — добавить только новые snapshots для иконок видов.
- **Локализационные тесты:** добавить пары `lifeStageLabel`/`subcategoryLabel` для каждого нового вида в `PetsListScreenLocalizedTest` или новом `SpeciesLocalizationTest`.
- Coverage measurement через **Kover**.

## Progress Tracking

- Mark completed items with `[x]` immediately when done.
- Add newly discovered tasks with ➕ prefix.
- Document issues/blockers with ⚠️ prefix.
- Update plan if implementation deviates from original scope.
- Keep plan in sync with actual work done.

## What Goes Where

- **Implementation Steps** (`[ ]` checkboxes): код, тесты, конфиги, assets, документация внутри репозитория — автоматизируемые агентом действия.
- **Post-Completion** (no checkboxes): manual content authoring (реальный научный текст care), manual Google Play submission, manual проверка SAF на разных Android-устройствах — всё, что требует внешних действий.

## Implementation Steps

### Task 1: Sealed AgeCalculator interface + Dog/Cat migration
- [ ] write FAILING test `AgeCalculatorTest` в `:core:domain` (или `:core:calculator`): убеждается, что `AgeCalculator.forSpecies(Species.Dog)` возвращает не-null, `.toHumanYears(pet)` для Dog и Cat работает; `AgeCalculator.forSpecies(Species.Rabbit)` возвращает `UnsupportedSpeciesCalculator` или throws `UnsupportedSpeciesException` (зависит от выбранной API)
- [ ] verify tests fail — **Red**
- [ ] create `sealed interface AgeCalculator` в `:core:calculator` с методом `fun toHumanYears(ageInYears: Double, params: SpeciesParams): Double` (где `SpeciesParams` — sealed marker-class с вариантами `Dog(size, method)`, `Cat(catType)`, `Rabbit(size)` и т.д.) ИЛИ паттерн с per-calculator конкретным методом (тип-safe через sealed Species → один-к-одному calculator); выбрать декомпозицию которая лучше совместима с экзишн UseCase
- [ ] create `LifeStageCalculator` sealed interface аналогично (один-к-одному с calculator)
- [ ] refactor `DogAgeCalculator` → `data object DogAgeCalculator : AgeCalculator` (сохраняем API toHumanYears(age, method, size?))
- [ ] refactor `CatAgeCalculator` → `data object CatAgeCalculator : AgeCalculator`
- [ ] refactor `CalculatePetAgeUseCase` чтобы использовать `AgeCalculator.forSpecies(pet.species)` вместо явного `when (pet.species) { Dog -> ..., Cat -> ... }`
- [ ] add factory `AgeCalculator.Companion.forSpecies(Species): AgeCalculator?` — null для not-yet-implemented видов
- [ ] verify все pre-existing тесты Plan 1 продолжают проходить — **Green** для refactor
- [ ] write tests for refactor invariants: forSpecies(Dog) == DogAgeCalculator, forSpecies(Cat) == CatAgeCalculator, forSpecies(Rabbit) == null до Task 2
- [ ] run `./gradlew :core:calculator:test :core:domain:test --no-daemon` — must pass before next task

### Task 2: RabbitAgeCalculator + life stages (TDD)
- [ ] write FAILING test `RabbitAgeCalculatorTest` (по §4.3, кусочная формула):
  - `0.1 year (1.2 month) = 3 human years` (30·0.1 = 3)
  - `0.33 year (4 months) = 12 human years` (граница первого сегмента)
  - `0.5 year = 12 + 8·(0.5 − 0.33) ≈ 13.36`
  - `1 year = 21 ЧГ`
  - `5 years = 21 + 6·4 = 45 ЧГ`
  - `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `RabbitSize` enum в `:core:model` (Dwarf, Small, Medium, Large, Giant) по §4.3 + stable `id`
- [ ] create `RabbitAgeCalculator : AgeCalculator` в `:core:calculator`
- [ ] implement кусочную формулу:
  - `0 ≤ age ≤ 1/3 (4 months)` → `30 · age`
  - `1/3 < age ≤ 1` → `12 + 8 · (age − 1/3)`
  - `age > 1` → `21 + 6 · (age − 1)`
- [ ] add KDoc со ссылкой на House Rabbit Society + AVMA + §4.3 spec
- [ ] add ParameterizedTest с табличными значениями (10+ кейсов)
- [ ] verify все тесты — **Green**
- [ ] add `LifeStage.Rabbit` sealed подтипы по §4.3 (Infancy, Adolescence, YoungAdult, Adult, Senior)
- [ ] create `RabbitLifeStageCalculator : LifeStageCalculator` с порогами: Infancy 0-3мес, Adolescence 3-6мес, YoungAdult 6-12мес, Adult 1-5y, Senior 5+
- [ ] write FAILING tests для `RabbitLifeStageCalculator` (граничные кейсы для каждого перехода)
- [ ] implement and verify — **Green**
- [ ] add `expectedLifespanRange(size: RabbitSize): ClosedFloatingPointRange<Double>` (8-12 base; small ≤14; giant 6-8) с тестами
- [ ] update `Species.Rabbit.isImplemented = true`
- [ ] update `AgeCalculator.forSpecies(Rabbit)` returns RabbitAgeCalculator
- [ ] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task

### Task 3: HamsterAgeCalculator + life stages (TDD)
- [ ] write FAILING test `HamsterAgeCalculatorTest` (по §4.4):
  - `1 month (0.083 year) ≈ 7.6 ЧГ` (0-1 мес: 1 ЧГ за каждые 4 дня → 30/4 ≈ 7.5)
  - `2 months ≈ 18 ЧГ`
  - `6 months ≈ 30 ЧГ`
  - `18 months ≈ 30 + 3·12 = 66 ЧГ`
  - `24 months ≈ 66 + 4·6 = 90 ЧГ`
- [ ] verify tests fail — **Red**
- [ ] create `HamsterType` enum в `:core:model` (Syrian, Dwarf, Roborovski, Chinese, WinterWhite — 5 видов по §4.4) + stable `id` + per-type `averageLifespanYears`
- [ ] create `HamsterAgeCalculator : AgeCalculator`
- [ ] implement кусочную формулу по §4.4
- [ ] add KDoc со ссылкой на RVC VetCompass + Animallama + §4.4
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.Hamster` sealed подтипы (Pup, Juvenile, Adult, Senior, VerySenior) по §4.4
- [ ] create `HamsterLifeStageCalculator` + tests
- [ ] add `expectedLifespanRange(type: HamsterType)` (Syrian 2-3, Dwarf 1.5-2, etc.) + tests
- [ ] update `Species.Hamster.isImplemented = true` + `AgeCalculator.forSpecies(Hamster)`
- [ ] run tests — must pass before next task

### Task 4: GuineaPigAgeCalculator + life stages (TDD)
- [ ] write FAILING test `GuineaPigAgeCalculatorTest` (по §4.5):
  - `3 weeks (0.058 year) ≈ 6 ЧГ` (точка отъёма)
  - `2-3 months ≈ 11.5 ЧГ`
  - `5 months (0.42 year) ≈ 20 ЧГ`
  - `1 year = 20 + 8·(7/12) ≈ 24.7` (после 5 месяцев)
  - `4 years ≈ 20 + 8·(4 − 0.42) ≈ 48.6`
  - `7 years ≈ 48.6 + 10·3 ≈ 78.6`
- [ ] verify tests fail — **Red**
- [ ] create `GuineaPigAgeCalculator : AgeCalculator`
- [ ] implement кусочную формулу по §4.5
- [ ] add KDoc со ссылкой на Oxbow Animallama + §4.5
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.GuineaPig` sealed подтипы (Pup, Juvenile, Adult, Senior, Geriatric) по §4.5
- [ ] create `GuineaPigLifeStageCalculator` + tests (senior с 4 лет)
- [ ] add `expectedLifespanRange()` = 5..7 + tests
- [ ] update `Species.GuineaPig.isImplemented = true` + `AgeCalculator.forSpecies(GuineaPig)`
- [ ] run tests

### Task 5: RatAgeCalculator + MouseAgeCalculator + life stages (TDD)
- [ ] write FAILING test `RatAgeCalculatorTest` (Sengupta 2013):
  - `0.5 year = 13.8·0.5 + 1.4 = 8.3 ЧГ`
  - `1 year = 15.2 ЧГ`
  - `2 years = 29.0 ЧГ`
  - `3 years = 42.8 ЧГ`
  - `throws on age = 0 или negative`
- [ ] verify tests fail — **Red**
- [ ] create `RatAgeCalculator : AgeCalculator`
- [ ] implement `13.8 · ageInYears + 1.4` (extract constants `RAT_COEFFICIENT`, `RAT_OFFSET`)
- [ ] add KDoc со ссылкой на Sengupta 2013, Int J Prev Med 4(6):624-630 + §4.6
- [ ] verify — **Green**
- [ ] write FAILING test `MouseAgeCalculatorTest` (Dutta & Sengupta 2016 piecewise):
  - `30 days (0.082 year) ≈ 4500 human-days = 12.33 human-years` (30·150ЧДней/365)
  - `100 days ≈ 12.33 + 58·45/365 ≈ 19.5 ЧГ` (42-180 фаза: 45 ЧДней/день)
  - `250 days ≈ ...` (180-365 фаза)
  - `500 days ≈ ...` (365-730 фаза)
  - `800 days ≈ ...` (>730 фаза)
- [ ] verify tests fail — **Red**
- [ ] create `MouseAgeCalculator : AgeCalculator`
- [ ] implement кусочную формулу с конвертацией age в дни → суммирование human-days по фазам → /365 для возврата в годах
- [ ] add KDoc со ссылкой на Dutta & Sengupta 2016, Life Sciences 152:244-248 + §4.6
- [ ] add ParameterizedTest для обоих калькуляторов
- [ ] verify — **Green**
- [ ] add `LifeStage.Rat` и `LifeStage.Mouse` sealed подтипы (Pup, Juvenile, Adult, Senior, EndOfLife)
- [ ] create `RatLifeStageCalculator` и `MouseLifeStageCalculator` + tests (Rat senior с 18 мес, Mouse senior с 12 мес — по §4.6 ссылкам)
- [ ] add `expectedLifespanRange()` для обоих (Rat 2-3, Mouse 1-3) + tests
- [ ] update `Species.Rat` и `Species.Mouse` `isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 6: FerretAgeCalculator + life stages (TDD)
- [ ] write FAILING test `FerretAgeCalculatorTest` (по §4.7):
  - `2 months (0.167 year) = +5·2 = 10 ЧГ`
  - `6 months = 30 ЧГ`
  - `1 year = 40 ЧГ`
  - `3 years = 40 + 4·2 = 48 ЧГ`
  - `7 years = 40 + 4·6 = 64 ЧГ`
- [ ] verify tests fail — **Red**
- [ ] create `FerretAgeCalculator : AgeCalculator`
- [ ] implement кусочную формулу по §4.7
- [ ] add KDoc со ссылкой на PMC «Senior Ferret» PMC7129291 + Oxbow Ferret Life Stages + §4.7
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.Ferret` подтипы (Kit, Juvenile, Adult, Senior, Geriatric) по §4.7
- [ ] create `FerretLifeStageCalculator` + tests (senior с 3-4 лет)
- [ ] add `expectedLifespanRange()` = 5..10 + tests
- [ ] update `Species.Ferret.isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 7: ScalarRatioFormula helper + BirdAgeCalculator + life stages (TDD)
- [ ] write FAILING test `ScalarRatioFormulaTest` (helper для Bird/Reptile/Fish):
  - `scalarRatio(age=1, lifespan=80) = 1` (baseline)
  - `scalarRatio(age=10, lifespan=80) = 10`
  - `scalarRatio(age=5, lifespan=40) = 10` (formula: age · 80 / lifespan)
  - `throws on non-positive lifespan`
- [ ] verify tests fail — **Red**
- [ ] create `internal object ScalarRatioFormula { fun compute(age: Double, lifespan: Double): Double = age * 80.0 / lifespan }`
- [ ] verify — **Green**
- [ ] add KDoc со ссылкой на §4.8, §4.9, §4.11
- [ ] write FAILING test `BirdAgeCalculatorTest` (по §4.8):
  - Budgerigar (lifespan 7y): 1y = 11.4 ЧГ; 3y = 34.3
  - Cockatiel (lifespan 15): 5y = 26.7
  - Macaw / Amazon (lifespan 50): 10y = 16
  - Canary (lifespan 10): 2y = 16
  - Возраст < 6 месяцев → результат × 1.3 (`6 weeks budgerigar = 0.115 · 11.4 · 1.3 ≈ 1.7`)
  - `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `BirdType` enum в `:core:model` (Budgerigar, Cockatiel, Canary, Lovebird, Conure, Amazon, AfricanGrey, Cockatoo, Macaw, Pigeon — 10 видов по §4.8) + stable `id` + per-type `averageLifespanYears` (Budgerigar=7, Cockatiel=15, Canary=10, ...)
- [ ] create `BirdAgeCalculator : AgeCalculator` использующий `ScalarRatioFormula.compute(age, type.averageLifespanYears)` + 1.3× для age < 0.5y
- [ ] add KDoc со ссылкой на AAV «Care for Senior Parrots» + Lafeber Vet + §4.8
- [ ] add ParameterizedTest с 5+ видами птиц
- [ ] verify — **Green**
- [ ] add `LifeStage.Bird` подтипы (Hatchling, Juvenile, Adult, Senior, Geriatric) по §4.8
- [ ] create `BirdLifeStageCalculator` + tests (фракция от average lifespan: hatchling <5%, juvenile <20%, adult 20-70%, senior 70-90%, geriatric >90%)
- [ ] add `expectedLifespanRange(type: BirdType)` (5-10 для волнистых, до 49 для амазонов) + tests
- [ ] update `Species.Bird.isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 8: ReptileAgeCalculator + life stages (TDD)
- [ ] write FAILING test `ReptileAgeCalculatorTest` (по §4.9):
  - Tortoise / sea turtle (lifespan 40): 5y = 10 ЧГ
  - Snake corn (lifespan 20): 3y = 12
  - Iguana (lifespan 20): 5y = 20
  - Gecko leopard (lifespan 15): 2y = 10.7
  - `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `ReptileType` enum в `:core:model` (BoxTurtle, RedEaredSlider, BeardedDragon, BallPython, CornSnake, GreenIguana, LeopardGecko, CrestedGecko — 8 видов по §4.9) + stable `id` + per-type `averageLifespanYears`
- [ ] create `ReptileAgeCalculator : AgeCalculator` через `ScalarRatioFormula`
- [ ] add KDoc со ссылкой на PetPlace + Reptile Centre + A-Z Animals + §4.9
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.Reptile` подтипы (Hatchling, Juvenile, Adult, Senior) — 4 фазы по §4.9
- [ ] create `ReptileLifeStageCalculator` + tests
- [ ] add `expectedLifespanRange(type: ReptileType)` + tests
- [ ] update `Species.Reptile.isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 9: HorseAgeCalculator + life stages (TDD)
- [ ] write FAILING test `HorseAgeCalculatorTest` (по §4.10):
  - `1 year = 6.5 ЧГ`
  - `2 years = 13 ЧГ`
  - `3 years = 18 ЧГ`
  - `4 years = 20.5 ЧГ`
  - `10 years = 20.5 + 2.5·6 = 35.5 ЧГ`
  - `25 years = 20.5 + 2.5·21 = 73 ЧГ`
  - `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `HorseType` enum в `:core:model` (Pony, LightHorse, DraftHorse, Thoroughbred — упрощённо по §4.10) + stable `id` + per-type `averageLifespanYears`
- [ ] create `HorseAgeCalculator : AgeCalculator`
- [ ] implement 3-фазную формулу (1y=6.5, 2y=13, 3y=18, 4y=20.5, далее +2.5/год)
- [ ] add KDoc со ссылкой на AAEP Vaccination Guidelines + PetMD (Kaela Schraer DVM) + §4.10
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.Horse` подтипы (Foal, Yearling, YoungAdult, Adult, Senior) по §4.10
- [ ] create `HorseLifeStageCalculator` + tests
- [ ] add `expectedLifespanRange(type: HorseType)` (25-30 base) + tests
- [ ] update `Species.Horse.isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 10: FishAgeCalculator + life stages (TDD)
- [ ] write FAILING test `FishAgeCalculatorTest` (по §4.11):
  - Guppy (lifespan 2): 1y = 40 ЧГ
  - Betta (lifespan 5): 3y = 48
  - Goldfish (lifespan 15): 5y = 26.7
  - Koi (lifespan 30): 10y = 26.7
  - Tropical neon (lifespan 8): 2y = 20
  - `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `FishType` enum в `:core:model` (Goldfish, Koi, Betta, Guppy, AngelFish, NeonTetra, Tropical, Discus — 8 видов по §4.11) + stable `id` + per-type `averageLifespanYears`
- [ ] create `FishAgeCalculator : AgeCalculator` через `ScalarRatioFormula`
- [ ] add KDoc со ссылкой на PetMD + Kodama Koi Farm + AquariumStoreDepot + §4.11
- [ ] add ParameterizedTest
- [ ] verify — **Green**
- [ ] add `LifeStage.Fish` подтипы (Fry, Juvenile, Adult, Senior) по §4.11
- [ ] create `FishLifeStageCalculator` + tests
- [ ] add `expectedLifespanRange(type: FishType)` (2 для guppy, 35+ для koi) + tests
- [ ] update `Species.Fish.isImplemented = true` + `AgeCalculator.forSpecies()`
- [ ] run tests

### Task 11: Property-based tests for all new species (Kotest)
- [ ] write `RabbitAgeCalculatorPropertyTest`:
  - monotonically increasing in age
  - continuity at piecewise boundaries (age=1/3, age=1)
  - positivity for positive input
  - upper bound 200 ЧГ for age ≤ 30
- [ ] write similar property tests для `HamsterAgeCalculator`, `GuineaPigAgeCalculator`, `FerretAgeCalculator` (continuity на piecewise boundaries)
- [ ] write `RatAgeCalculatorPropertyTest` (linear → trivial monotonicity)
- [ ] write `MouseAgeCalculatorPropertyTest` (continuity на 4 границах: 42дн, 180дн, 365дн, 730дн)
- [ ] write `BirdAgeCalculatorPropertyTest` (∀ BirdType: monotonicity; 1.3× correction continuity на age=0.5)
- [ ] write `ReptileAgeCalculatorPropertyTest` (∀ ReptileType: monotonicity, ratio к lifespan = 80/lifespan)
- [ ] write `HorseAgeCalculatorPropertyTest` (continuity на 3 границах: 1y, 2y, 3y, 4y)
- [ ] write `FishAgeCalculatorPropertyTest` (∀ FishType: monotonicity, ratio invariant)
- [ ] write `LifeStageCalculatorPropertyTest` для всех новых видов (если `age1 < age2` тогда `stage(age1).ordinal ≤ stage(age2).ordinal`)
- [ ] run `./gradlew :core:calculator:test --no-daemon`
- [ ] verify coverage `:core:calculator` ≥ 95%: `./gradlew :core:calculator:koverHtmlReport && koverVerify`
- [ ] must pass before next task

### Task 12: Database migration + PetMapper expansion
- [ ] write FAILING test `PetMapperTest` для каждого нового subcategory type:
  - `Pet(species=Rabbit, subcategory=RabbitSize.Dwarf) → PetEntity round-trip preserves subcategory`
  - аналогично для Hamster, GuineaPig, Bird, Reptile, Horse, Fish (Rat/Mouse/Ferret не имеют подкатегорий)
  - `unknown subcategory id → IllegalStateException` (как с Dog/Cat в Plan 1)
- [ ] update `PetMapper` чтобы маппил new subcategory types via stable `id` strings
- [ ] verify tests — **Green**
- [ ] check schema: должна ли увеличиться `DATABASE_VERSION` с 1 до 2? Если subcategory column хранится как `TEXT` (что сейчас) и принимает любые id-строки, migration не нужна. Если schema требует расширения (например, новая колонка subspecies_type) — написать `MIGRATION_1_2`.
- [ ] **Если migration нужна:**
  - bump `DATABASE_VERSION = 2`
  - export new schema через `./gradlew :core:database:assembleDebug` → проверить `core/database/schemas/.../2.json`
  - write `MIGRATION_1_2` в `Migrations.kt`
  - write androidTest `MigrationsTest` через `MigrationTestHelper` (Room) — миграция 1→2 не теряет данные
  - update `DatabaseModule` чтобы регистрировать миграцию
- [ ] **Если migration не нужна:** документировать решение в KDoc `PawClockDatabase` + добавить explicit Mapper-test, что новые subcategory id'ы сериализуются корректно
- [ ] run `./gradlew :core:database:test --no-daemon`
- [ ] run `./gradlew :core:database:assembleDebugAndroidTest --no-daemon` (smoke check, polnyj run в nightly.yml)
- [ ] must pass before next task

### Task 13: Care recommendations placeholder JSON для всех 10 видов
- [ ] create asset directory structure для каждого вида: `:app/src/main/assets/care/{species}/{stage}/{ru,en}.json`
- [ ] create JSON files с placeholder контентом (поля по `CareRecommendation` модели: `stage_description`, `nutrition`, `activity`, `veterinary_check_frequency`, `dental_care`, `warning_signs`, `source_url`, `source_name`, `disclaimer`)
- [ ] для каждого файла:
  - `stage_description` = `"TODO(content-pass): описание стадии {species} {stage}"`
  - `nutrition`/`activity` = коротки placeholder
  - `source_url` = соответствующий URL из §14 spec (House Rabbit Society, RVC, Sengupta, AAV, AAEP и т.д.)
  - `disclaimer` = "Информация носит ознакомительный характер..." из §3.3
- [ ] write FAILING test `CareRepositoryTest` для каждого нового вида:
  - `loads rabbit infancy ru`
  - `loads bird hatchling en`
  - и т.д. для всех 10 видов × 5 стадий × 2 локали (sample проверки, не каждый файл)
- [ ] verify load работает через расширения существующего CareRepository (без изменений в коде — только новые assets)
- [ ] verify — **Green**
- [ ] add `scripts/verify-care-assets.sh` — проверяет, что для каждого `Species.implemented()` существуют JSON-файлы для каждой LifeStage × {ru,en}; завершает с ошибкой если отсутствует файл
- [ ] run script → all green
- [ ] update care content tracker в `docs/CARE_CONTENT.md` (новый файл) — список всех placeholder файлов с status "TODO" для content-pass после Plan 2
- [ ] run tests

### Task 14: Stylized species icons (vector drawables, §5.6)
- [ ] research/select SVG-иконки для всех 12 видов в стиле Phosphor / Lucide (моно-линейные, 48dp viewBox); либо нарисовать собственные простые
- [ ] add `ic_species_{dog,cat,rabbit,hamster,guinea_pig,rat,mouse,ferret,bird,reptile,horse,fish}.xml` как Android Vector Drawables в `:core:designsystem/src/main/res/drawable/`
- [ ] create `SpeciesIcon` composable в `:core:designsystem` который маппит `Species` → соответствующий `painterResource` (when-блок exhaustive по sealed)
- [ ] write FAILING test `SpeciesIconTest`:
  - `Composable for Species.Dog renders successfully` (используя `createComposeRule` или Roborazzi screenshot test opt-in)
  - все 12 видов имеют не-null painter (через `LocalContext.resources.getIdentifier`)
- [ ] verify tests — **Green**
- [ ] update `PetCard` composable в `:feature:pets` — заменить emoji avatar на `SpeciesIcon(pet.species)`
- [ ] update PetEditor `SpeciesSelector` чтобы рядом с label каждого вида показывался `SpeciesIcon` (uniform layout, иконка слева)
- [ ] update QuickCalculator `QuickCalcSpeciesSelector` аналогично
- [ ] add Roborazzi screenshot tests opt-in (как в Plan 1) для каждой иконки (12 snapshots)
- [ ] update `PetsListScreenTest` если поменялся node tree (emoji → drawable)
- [ ] run `./gradlew :core:designsystem:test :feature:pets:test :feature:editor:test :feature:quickcalc:test --no-daemon`

### Task 15: UI расширение PetEditor для всех 12 видов
- [ ] write FAILING test `PetEditorViewModelTest`:
  - `SelectSpecies(Rabbit) → availableSubcategories = RabbitSize values`
  - `SelectSpecies(Hamster) → HamsterType values`
  - `SelectSpecies(Bird) → BirdType values`
  - `SelectSpecies(Reptile) → ReptileType values`
  - `SelectSpecies(Fish) → FishType values`
  - `SelectSpecies(Horse) → HorseType values`
  - `SelectSpecies(Rat) → emptyList` (нет подкатегории)
  - `SelectSpecies(Mouse) → emptyList`
  - `SelectSpecies(Ferret) → emptyList`
- [ ] verify tests fail — **Red** (поскольку `subcategoriesFor(species)` пока возвращает emptyList для not-Dog/Cat)
- [ ] update `PetEditorState.subcategoriesFor(species)` чтобы возвращать соответствующие subcategory enum'ы для всех 12 видов
- [ ] update PetEditor `SpeciesSelector` чтобы показывал `Species.implemented()` (все 12 после Tasks 2-10) — UI автоматически расширится через iteration по списку
- [ ] update PetEditor `SubcategorySelector` чтобы корректно рендерил label для каждого subcategory type (использует stringResource через mapping function)
- [ ] add stringResource keys для всех новых subcategories (Task 22 будет добавлять переводы; здесь — структура key + ru default)
- [ ] write Compose UI test `PetEditorScreenTest`:
  - `selecting Rabbit shows RabbitSize chips`
  - `selecting Bird shows BirdType chips`
  - `selecting Rat hides subcategory section` (для видов без подкатегорий)
- [ ] verify — **Green**
- [ ] run `./gradlew :feature:editor:test :feature:editor:assembleDebug --no-daemon`

### Task 16: UI расширение QuickCalculator для всех 12 видов
- [ ] write FAILING test `QuickCalcViewModelTest`:
  - `SelectSpecies(Rabbit) → availableSubcategories = RabbitSize values + default = Medium`
  - аналогично для Hamster/Bird/Reptile/Fish/Horse
  - `Calculate for Rabbit 5y Medium → ~45 ЧГ + Adult`
  - `Calculate for Bird Budgerigar 3y → ~34.3 ЧГ + Adult`
  - `Calculate for Horse 10y LightHorse → ~35.5 ЧГ + Senior`
  - method toggle (Wang/Size) показывается только для Dog (остальные виды — единственный метод)
- [ ] verify tests fail — **Red**
- [ ] update `QuickCalcViewModel` чтобы корректно подставлять default subcategory для каждого вида + dispatchить через `AgeCalculator.forSpecies(species)?.toHumanYears(...)`
- [ ] update Quick Calculator's species selector / subcategory selector / method toggle UI to support all 12 species
- [ ] update `QuickCalcResultSheet` чтобы "Как это посчитано" блок показывал правильный источник (Wang для Dog EPIGENETIC, AKC для Dog SIZE_BASED, AAFP для Cat, House Rabbit Society для Rabbit, и т.д.) через mapping function
- [ ] write Compose UI test `QuickCalcScreenTest`:
  - `result sheet shows ~45 human years + Adult for Rabbit Medium 5y`
  - `result sheet shows ~34 human years for Budgerigar 3y`
  - `method toggle hidden for non-Dog species`
- [ ] verify — **Green**
- [ ] run `./gradlew :feature:quickcalc:test :feature:quickcalc:assembleDebug --no-daemon`

### Task 17: Export Pets — JSON serializer + ExportPetsUseCase (TDD)
- [ ] write FAILING test `PetsJsonSerializerTest`:
  - `serializes single Dog pet to JSON with all 9 fields`
  - `serializes list of 3 pets (Dog, Cat, Rabbit) preserving subcategory types`
  - `serialized JSON contains schema_version = 1` (forward-compat)
  - `null fields are omitted` (не сериализуются как `null` строки)
  - `birthDate is ISO-8601 string`
  - `species id используется (а не Species.toString())`
- [ ] verify tests fail — **Red**
- [ ] create `PetsExportSchema` data class в `:core:domain/export/` с `@Serializable`:
  ```kotlin
  data class PetsExportSchema(
      val schemaVersion: Int = 1,
      val exportedAt: String, // ISO-8601 timestamp
      val pets: List<PetExportEntry>
  )
  data class PetExportEntry(
      val name: String,
      val speciesId: String,
      val subcategoryId: String?,
      val birthDate: String,
      val genderId: String?,
      val weightKg: Double?,
      val notes: String?
      // photoPath не экспортируется — фото локальный artifact
  )
  ```
- [ ] create `PetsJsonSerializer.encode(pets: List<Pet>, exportedAt: Instant): String` — pure-Kotlin без Android-зависимостей
- [ ] verify — **Green**
- [ ] write FAILING test `ExportPetsUseCaseTest` (с FakePetRepository, FakeClock):
  - `export empty list → JSON with empty pets array`
  - `export multiple pets → JSON content matches expected schema`
  - `propagates IO exception from writer`
- [ ] create `ExportPetsUseCase(petRepo, clock)` в `:core:domain/export/`:
  - `suspend operator fun invoke(format: ExportFormat): String` где `enum ExportFormat { JSON, CSV }`
  - читает все pets через `petRepo.getAll()` (новый метод)
  - сериализует через `PetsJsonSerializer.encode()` или CSV serializer (Task 18)
- [ ] update `PetRepository` interface добавить `suspend fun getAll(): List<Pet>` (вместо/в дополнение к `observe()`)
- [ ] update `RoomPetRepository` соответственно
- [ ] verify — **Green**
- [ ] run `./gradlew :core:domain:test --no-daemon`

### Task 18: Export Pets — CSV serializer (TDD)
- [ ] write FAILING test `PetsCsvSerializerTest`:
  - `serializes single Dog pet to CSV with header row`
  - `escapes commas and quotes in name/notes fields` (RFC 4180)
  - `newlines in notes are escaped as quoted fields`
  - `null fields are empty cells, not the string "null"`
  - `birthDate is ISO-8601 string`
  - `header row uses snake_case column names`
- [ ] verify tests fail — **Red**
- [ ] create `PetsCsvSerializer.encode(pets: List<Pet>, exportedAt: Instant): String` — pure-Kotlin RFC 4180 implementation; header = `name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes`
- [ ] verify — **Green**
- [ ] update `ExportPetsUseCase` чтобы поддерживать обе формы (когда `format = CSV`, использовать `PetsCsvSerializer`)
- [ ] write tests для ExportPetsUseCase с CSV
- [ ] run tests

### Task 19: Import Pets — JSON deserializer + ImportPetsUseCase (TDD)
- [ ] write FAILING test `PetsJsonDeserializerTest`:
  - `parses valid export schema v1 → List<PetExportEntry>`
  - `throws ImportException on missing required field (name, species_id, birth_date)`
  - `throws on unknown species_id` (НЕ silent skip — fail-fast)
  - `throws on malformed JSON`
  - `throws on schema_version > 1` (forward-incompat) или показывает warning
  - `accepts null optional fields (subcategory, gender, weight, notes)`
- [ ] verify tests fail — **Red**
- [ ] create `PetsJsonDeserializer.decode(json: String): PetsImportResult` где:
  ```kotlin
  sealed interface PetsImportResult {
      data class Success(val entries: List<PetExportEntry>, val warnings: List<ImportWarning>) : PetsImportResult
      data class Failure(val error: ImportException) : PetsImportResult
  }
  ```
- [ ] verify — **Green**
- [ ] write FAILING test `ImportPetsUseCaseTest`:
  - `imports valid pets and inserts into PetRepository`
  - `dry-run mode returns preview without insertion` (для UI confirm dialog)
  - `replace strategy = clears existing pets before insert`
  - `merge strategy = keeps existing pets, inserts new`
  - `propagates ImportException for malformed input`
- [ ] create `ImportPetsUseCase(petRepo, deserializer)` с параметрами `ImportStrategy { MERGE, REPLACE }` + `dryRun: Boolean`
- [ ] verify — **Green**
- [ ] add `PetRepository.clearAll()` для REPLACE strategy + test
- [ ] run tests

### Task 20: Import Pets — CSV deserializer (TDD)
- [ ] write FAILING test `PetsCsvDeserializerTest`:
  - `parses valid CSV with header row → List<PetExportEntry>`
  - `handles quoted fields with commas and newlines (RFC 4180)`
  - `throws on missing required column (name, species_id, birth_date)`
  - `throws on row with unknown species_id`
  - `accepts empty optional cells`
  - `handles BOM (byte order mark) in UTF-8 encoded CSV from Excel`
- [ ] verify tests fail — **Red**
- [ ] create `PetsCsvDeserializer.decode(csv: String): PetsImportResult` — RFC 4180 parser
- [ ] verify — **Green**
- [ ] update `ImportPetsUseCase` чтобы определять формат по содержимому (JSON начинается с `{`, CSV — с `name,` или похожего) или принимать `ImportFormat` параметром
- [ ] write tests для ImportPetsUseCase с CSV
- [ ] run tests

### Task 21: Settings UI — Export/Import buttons + SAF integration
- [ ] write FAILING test `SettingsViewModelTest`:
  - `ExportRequested(format=JSON) emits Effect.RequestSaveLocation(suggestedFilename, mime)` — Effect/Event для UI чтобы запустить `ACTION_CREATE_DOCUMENT`
  - `ExportLocationSelected(uri) → exports pets and emits Effect.ExportComplete(petCount)`
  - `ImportRequested → emits Effect.RequestOpenLocation(mimes=["application/json","text/csv"])`
  - `ImportLocationSelected(uri, strategy=MERGE) → imports and emits Effect.ImportComplete(petCount, warnings)`
  - `ImportFailed → emits Effect.ImportError(errorMessageKey)`
- [ ] verify tests fail — **Red**
- [ ] update `SettingsViewModel` чтобы:
  - инжектировать `ExportPetsUseCase` и `ImportPetsUseCase`
  - добавить новые `SettingsEvent` варианты: `ExportRequested`, `ExportLocationSelected`, `ImportRequested`, `ImportLocationSelected`
  - использовать `Channel<SettingsEffect>` (или `SharedFlow`) для one-time effects (request SAF, show snackbar) — отдельно от reactive state
- [ ] create `SettingsEffect` sealed interface для one-time UI effects
- [ ] verify — **Green**
- [ ] update `SettingsScreen` Composable:
  - добавить раздел "Резервная копия" / "Backup" с двумя ListItem: "Экспорт..." + "Импорт..."
  - sub-выбор формата через radio dialog: JSON / CSV
  - sub-выбор стратегии import: Merge / Replace
  - integrate `rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument)` для export → передать uri в SettingsViewModel
  - integrate `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument)` для import
  - после успешного export/import — Snackbar с локализованным сообщением
- [ ] create `SafFileWriter` / `SafFileReader` обёртки в `:app/data/saf/` для записи/чтения через `Context.contentResolver.openOutputStream(uri)` — изолирует ViewModel от Android `Uri`
- [ ] write Compose UI test `SettingsScreenTest`:
  - `Export button visible`
  - `clicking Export shows format selector dialog`
  - `Import button visible`
  - `clicking Import shows strategy selector dialog`
- [ ] verify — **Green**
- [ ] run `./gradlew :feature:settings:test :feature:settings:assembleDebug --no-daemon`

### Task 22: Локализация — strings для всех новых видов + подкатегорий + стадий
- [ ] add стрингов в `:feature:editor/src/main/res/values/strings.xml` (ru, default) для:
  - имена всех 10 новых видов (`species_rabbit = "Кролик"`, `species_hamster = "Хомяк"`, etc.)
  - имена всех подкатегорий (`rabbit_size_dwarf = "Карликовый"`, `hamster_type_syrian = "Сирийский"`, etc.)
- [ ] add те же ключи в `values-en/strings.xml`
- [ ] add стрингов в `:feature:quickcalc/.../strings.xml` (или extract в shared :core:designsystem strings) — те же ключи для subcategory labels
- [ ] add стрингов для `LifeStage` всех новых видов (`life_stage_rabbit_infancy = "Младенчество"`, etc.) в `:feature:pets/src/main/res/values/strings.xml`
- [ ] update `lifeStageLabelRes(lifeStage)` helper в `:feature:pets/common/` чтобы покрывал все новые подтипы LifeStage exhaustive
- [ ] add стрингов для settings export/import labels (`settings_export_title`, `settings_export_format_json`, `settings_export_format_csv`, `settings_import_strategy_merge`, `settings_import_strategy_replace`, `settings_export_success_message`, etc.)
- [ ] write FAILING test `LifeStageLabelLocalizationTest` (новый): для каждого `Species.implemented()` × каждой LifeStage variant → `lifeStageLabelRes(stage)` возвращает non-zero resId
- [ ] update `AgePluralFormatter` если нужно (1 месяц / 2 месяца / 5 месяцев для возрастов младенцев — уже есть, но проверить что используется в новых видах с месячными возрастами)
- [ ] write `SpeciesLocalizationTest` (новый): через `Context.getString(resId)` для каждого species/subcategory ключа в ru и en — non-empty strings
- [ ] verify — **Green**
- [ ] run `./gradlew :feature:pets:test :feature:editor:test :feature:quickcalc:test :feature:settings:test --no-daemon`

### Task 23: Maestro E2E flows для new species + export/import
- [ ] create `maestro/quick_calc_rabbit.yaml` — quick calc для кролика 2 года Medium, проверка результата ~27 ЧГ
- [ ] create `maestro/quick_calc_bird.yaml` — quick calc для попугая Cockatiel 5 лет
- [ ] create `maestro/quick_calc_horse.yaml` — quick calc для лошади 10 лет
- [ ] create `maestro/export_import_roundtrip.yaml`:
  - clearState
  - launch app
  - add Dog "Рекс" + Cat "Мурка" + Rabbit "Снежок" through PetEditor
  - assertVisible 3 pets in list
  - navigate to Settings
  - tap "Экспорт" → выбор JSON → SAF dialog (используем testTag для FAB, реальный SAF dialog — out-of-scope, Maestro Android storage UI variability проверяется отдельно или через FakeStorage в test build)
  - **NOTE:** реальное взаимодействие с SAF из Maestro сложно — задокументировать в YAML, что E2E test проверяет UI-flow до и после SAF dialog (mock через test-only deep link)
  - tap "Импорт" с merge strategy
  - assertVisible 3 pets unchanged (round-trip preserves data)
- [ ] update existing `create_first_pet.yaml` и `quick_calc_dog.yaml` если sequence изменился из-за UI расширения (новый species selector layout)
- [ ] update `.github/workflows/nightly.yml` чтобы запускал все Maestro flows (detect_flows автоматически найдёт новые)
- [ ] run `./gradlew :app:assembleDebugAndroidTest --no-daemon` (sanity check, реальный запуск — в nightly.yml)

### Task 24: Final acceptance verification
- [ ] verify all 12 species are implemented:
  - `assert Species.values().filter { it.isImplemented }.size == 12` — добавить как property test в `:core:model`
- [ ] verify all 12 species have a calculator:
  - `forAll Species: AgeCalculator.forSpecies(it) != null` — добавить exhaustive test
- [ ] verify coverage:
  - `./gradlew :core:calculator:koverHtmlReport :core:domain:koverHtmlReport :core:database:koverHtmlReport`
  - `:core:calculator` ≥ 95%
  - `:core:domain` ≥ 90% (Export/Import включаются)
  - `:core:database` ≥ 80%
- [ ] verify lint clean:
  - `./gradlew ktlintCheck detekt lintDebug` — zero errors
- [ ] verify build clean:
  - `./gradlew assembleDebug` — все 12 модулей, BUILD SUCCESSFUL
- [ ] verify all unit tests pass:
  - `./gradlew testDebugUnitTest` — all green
- [ ] verify Maestro E2E (smoke build check):
  - `./gradlew :app:assembleDebugAndroidTest` — BUILD SUCCESSFUL, готов к nightly.yml
- [ ] verify Export/Import round-trip (через unit tests, не E2E):
  - `RoundTripTest` (новый): create 12 pets (по 1 каждого вида) → export to JSON → clear → import JSON → assert content matches
  - аналогичный test для CSV
- [ ] verify APK size:
  - `./gradlew :app:assembleRelease` (если keystore доступен; иначе :app:assembleDebug + проверка приближённо)
  - `scripts/verify-bundle-size.sh` — debug < 15 MB, release < 8 MB (целевое из §7.5)
- [ ] update `CHANGELOG.md` — переместить Plan 2 deliverables из `[Unreleased]` в `[1.0.0]` секцию (опционально; решение о версии — пользовательское при тегировании)
- [ ] update `README.md` — статус видов в "Why PawClock" → 12/12 поддерживаемых
- [ ] update `ADR-0006` если есть изменения в дефолте калькуляции для собак (не должно быть)
- [ ] verify GitHub Actions workflows валидны:
  - `scripts/verify-workflows.sh`
- [ ] verify docs:
  - `bash scripts/verify-docs.sh` — все обязательные секции на месте
  - `bash scripts/verify-care-assets.sh` — все care JSON присутствуют для implemented видов
- [ ] verify ADRs:
  - `bash scripts/verify-adrs.sh` — все 7 ADR на месте + любые новые ADR (опционально: ADR-0008 для AgeCalculator strategy pattern, ADR-0009 для Export/Import schema)
- [ ] add ➕ ADR-0008 "AgeCalculator sealed interface for species dispatch" в `docs/adr/`
- [ ] add ➕ ADR-0009 "Export/Import JSON schema versioning" в `docs/adr/` (schemaVersion=1, forward-compat strategy)

## Technical Details

**Структура `:core:calculator` после Plan 2:**
```
core/calculator/src/main/kotlin/app/pawclock/calculator/
├── AgeCalculator.kt              — sealed interface + Companion.forSpecies()
├── LifeStageCalculator.kt        — sealed interface
├── ScalarRatioFormula.kt         — helper для Bird/Reptile/Fish (age · 80 / lifespan)
├── DogAgeCalculator.kt           — refactored to AgeCalculator
├── DogLifeStageCalculator.kt
├── DogSizeTable.kt
├── DogLifeStageThresholds.kt
├── CatAgeCalculator.kt           — refactored to AgeCalculator
├── CatLifeStageCalculator.kt
├── CatLifeStageThresholds.kt
├── RabbitAgeCalculator.kt        — кусочная, House Rabbit Society / AVMA
├── RabbitLifeStageCalculator.kt
├── HamsterAgeCalculator.kt       — кусочная, RVC
├── HamsterLifeStageCalculator.kt
├── GuineaPigAgeCalculator.kt
├── GuineaPigLifeStageCalculator.kt
├── RatAgeCalculator.kt           — Sengupta 2013 linear
├── RatLifeStageCalculator.kt
├── MouseAgeCalculator.kt         — Dutta & Sengupta 2016 piecewise
├── MouseLifeStageCalculator.kt
├── FerretAgeCalculator.kt
├── FerretLifeStageCalculator.kt
├── BirdAgeCalculator.kt          — ScalarRatio + 1.3× для <6мес
├── BirdLifeStageCalculator.kt
├── ReptileAgeCalculator.kt       — ScalarRatio 4 фазы
├── ReptileLifeStageCalculator.kt
├── HorseAgeCalculator.kt         — 3-фаза AAEP
├── HorseLifeStageCalculator.kt
├── FishAgeCalculator.kt          — ScalarRatio
├── FishLifeStageCalculator.kt
└── ExpectedLifespan.kt           — расширенный по всем видам

core/calculator/src/test/kotlin/.../
├── AgeCalculatorTest.kt          — forSpecies dispatch
├── DogAgeCalculatorTest.kt
├── DogAgeCalculatorSizeBasedTest.kt
├── DogAgeCalculatorPropertyTest.kt
├── DogLifeStageCalculatorTest.kt
├── CatAgeCalculatorTest.kt
├── CatLifeStageCalculatorTest.kt
├── ScalarRatioFormulaTest.kt
├── RabbitAgeCalculatorTest.kt
├── RabbitAgeCalculatorPropertyTest.kt
├── RabbitLifeStageCalculatorTest.kt
├── ... (аналогично для каждого вида)
└── (all property tests in Task 11)
```

**Структура `:core:model` после Plan 2:**
```
core/model/src/main/kotlin/app/pawclock/model/
├── Species.kt                    — все 12 видов с isImplemented = true
├── DogSize.kt                    — без изменений
├── CatType.kt                    — без изменений
├── RabbitSize.kt                 — новый: Dwarf, Small, Medium, Large, Giant
├── HamsterType.kt                — новый: Syrian, Dwarf, Roborovski, Chinese, WinterWhite
├── BirdType.kt                   — новый: Budgerigar, Cockatiel, Canary, Lovebird, Conure, Amazon, AfricanGrey, Cockatoo, Macaw, Pigeon
├── ReptileType.kt                — новый: BoxTurtle, RedEaredSlider, BeardedDragon, BallPython, CornSnake, GreenIguana, LeopardGecko, CrestedGecko
├── FishType.kt                   — новый: Goldfish, Koi, Betta, Guppy, AngelFish, NeonTetra, Tropical, Discus
├── HorseType.kt                  — новый: Pony, LightHorse, DraftHorse, Thoroughbred
├── Gender.kt
├── LifeStage.kt                  — расширенный sealed (Dog, Cat, Rabbit, Hamster, GuineaPig, Rat, Mouse, Ferret, Bird, Reptile, Horse, Fish)
├── Pet.kt
├── CareRecommendation.kt
├── CalculationMethod.kt
└── ThemeMode.kt
```

**Структура `:core:domain` после Plan 2:**
```
core/domain/src/main/kotlin/app/pawclock/domain/
├── usecase/
│   ├── CalculatePetAgeUseCase.kt — рефакторен под AgeCalculator.forSpecies()
│   ├── GetPetsUseCase.kt
│   ├── SavePetUseCase.kt
│   ├── DeletePetUseCase.kt
│   ├── GetCareRecommendationsUseCase.kt
│   └── GetPetByIdUseCase.kt
├── export/
│   ├── ExportFormat.kt           — enum JSON, CSV
│   ├── ExportPetsUseCase.kt
│   ├── PetsExportSchema.kt
│   ├── PetsJsonSerializer.kt
│   ├── PetsCsvSerializer.kt
├── import_/
│   ├── ImportStrategy.kt         — enum MERGE, REPLACE
│   ├── ImportPetsUseCase.kt
│   ├── ImportException.kt
│   ├── ImportWarning.kt
│   ├── PetsImportResult.kt
│   ├── PetsJsonDeserializer.kt
│   └── PetsCsvDeserializer.kt
├── pet/PetRepository.kt          — расширенный (getAll, clearAll)
├── settings/SettingsReader.kt
├── care/CareRepository.kt + AssetSource.kt
├── locale/LocaleApplier.kt
└── format/AgePluralFormatter.kt
```

**Ключевые формулы и эталонные тестовые значения:**

| Вид | Эталонное значение | Метод |
|---|---|---|
| Rabbit 1y | 21.0 ЧГ | piecewise (3-я ветка) |
| Rabbit 5y Medium | 45.0 ЧГ | |
| Hamster 6мес | 30.0 ЧГ | piecewise (3-я ветка) |
| GuineaPig 5мес | 20.0 ЧГ | piecewise (4-я ветка) |
| Rat 2y | 29.0 ЧГ | linear |
| Mouse 100дн | ~19.5 ЧГ | piecewise (2-я фаза) |
| Ferret 1y | 40.0 ЧГ | piecewise |
| Bird Budgerigar 3y | 34.3 ЧГ | scalar `3·80/7` |
| Reptile Iguana 5y | 20.0 ЧГ | scalar `5·80/20` |
| Horse 10y | 35.5 ЧГ | 3-phase |
| Fish Goldfish 5y | 26.7 ЧГ | scalar `5·80/15` |

**TDD-цикл для каждого калькулятора (Tasks 2-10):**
1. Red — написать тест с эталонным значением из таблицы выше
2. Green — минимальная реализация
3. Refactor — извлечь константы в `private const val`, добавить KDoc с DOI/URL
4. Параметризовать с табличными значениями (10+ кейсов per калькулятор)
5. Edge cases (age=0, negative, very large, граница piecewise сегментов)
6. Property-based (monotonicity, positivity, continuity) — отдельная Task 11

**Export/Import JSON schema (Task 17):**
```json
{
  "schema_version": 1,
  "exported_at": "2026-05-28T12:34:56Z",
  "pets": [
    {
      "name": "Рекс",
      "species_id": "dog",
      "subcategory_id": "medium",
      "birth_date": "2020-06-15",
      "gender_id": "male",
      "weight_kg": 18.5,
      "notes": "Любит играть с мячом"
    }
  ]
}
```

**Export/Import CSV schema (Task 18):**
```csv
name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes
"Рекс",dog,medium,2020-06-15,male,18.5,"Любит играть с мячом"
```

**Database migration plan (Task 12):**
- subcategory столбец уже хранится как `TEXT` (nullable) в Plan 1's `PetEntity.subcategory` — значит формальная schema не меняется
- DATABASE_VERSION остаётся = 1, миграция не нужна
- Расширение тестируется через extended `PetMapperTest` для каждого нового subcategory type's `id`
- Документировать в `PawClockDatabase` KDoc решение почему миграция не нужна для Plan 2
- ⚠️ Если в процессе обнаружится несовместимость — bump version + написать migration

**Dependency graph (после Plan 2):**
```
:app
  ↓
:feature:* ─→ :core:domain ─→ :core:model + :core:calculator
                ↓
              :core:database + :core:datastore
:feature:* ─→ :core:designsystem ─→ :core:model

[Все 12 модулей не меняются — только содержимое.]
```

## Post-Completion

*Items requiring manual intervention or external systems — informational only*

**Manual content authoring (рекомендуется после Plan 2):**
- Заменить placeholder TODO-тексты в `app/src/main/assets/care/*.json` на реальный научный контент по §3.3 и §14 (требует ветеринара-консультанта или работы с published guidelines)
- Каждый care-файл должен содержать научно обоснованные рекомендации со ссылкой на первоисточник
- Это самостоятельный «content pass», который может потребовать 1-2 недели работы при привлечении эксперта

**Manual verification:**
- Реальное тестирование SAF Export/Import на разных Android-устройствах (API 24/26/30/33/35), потому что behaviour Storage Access Framework исторически нестабилен между OEM
- Проверить, что приложение корректно работает при выборе файла из Google Drive / Yandex Disk / Dropbox через SAF (некоторые provider'ы возвращают content:// URI без gettable filename)
- Manual UX проверка: ContextMenu для удаления питомца, swipe gestures, A11Y через TalkBack
- Performance check: cold start < 500ms на Pixel 6 (требует Baseline Profiles — Plan 3)
- ANR rate baseline — добавить в Crashlytics-альтернативу (если решим ACRA в Plan 3)

**External system updates:**
- Перед v1.0.0 production release: проверить занятость названия PawClock в Google Play (см. §0); если занято — переключиться на запасной (PetSpan, PetYears, TailTime, Furcast, PetChrono)
- Зарегистрировать domain `pawclock.app` / `pawclock.dev` (decide)
- Создать `fastlane/metadata/{ru,en-US}/short_description.txt`, `full_description.txt`, `release_notes.txt`, screenshots — для Google Play Store listing (отдельный graphic-design pass)
- Подготовить privacy policy на pawclock.app/privacy (минимум — текст «No data collected, no data shared»)
- Зарегистрировать Codecov для repo + добавить `CODECOV_TOKEN` в GitHub Secrets (если ещё не сделано)
- Сгенерировать release keystore + добавить `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` в GitHub Secrets (для release.yml workflow из Plan 1 Task 4)
- Установить Branch Protection через GitHub UI (см. Post-Completion из Plan 1)

**Когда план завершён:**
- ralphex автоматически переместит `docs/plans/2026-05-28-all-species-and-export-import.md` в `docs/plans/completed/`
- Создать tag `v1.0.0-rc1` для первого release candidate → автоматический trigger `release.yml`
- После manual QA — promote к `v1.0.0`
- Promote в Google Play Console: internal testing → closed alpha → open beta → production
