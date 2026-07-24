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
- [x] write FAILING test `AgeCalculatorTest` в `:core:domain` (или `:core:calculator`): убеждается, что `AgeCalculator.forSpecies(Species.Dog)` возвращает не-null, `.toHumanYears(pet)` для Dog и Cat работает; `AgeCalculator.forSpecies(Species.Rabbit)` возвращает `UnsupportedSpeciesCalculator` или throws `UnsupportedSpeciesException` (зависит от выбранной API) — создан `AgeCalculatorTest` + `LifeStageCalculatorTest` в `:core:calculator`; `forSpecies(Rabbit)` возвращает `null` (выбранный API)
- [x] verify tests fail — **Red**
- [x] create `sealed interface AgeCalculator` в `:core:calculator` с методом `fun toHumanYears(ageInYears: Double, params: SpeciesParams): Double` (где `SpeciesParams` — sealed marker-class с вариантами `Dog(size, method)`, `Cat(catType)`, `Rabbit(size)` и т.д.) ИЛИ паттерн с per-calculator конкретным методом (тип-safe через sealed Species → один-к-одному calculator); выбрать декомпозицию которая лучше совместима с экзишн UseCase — выбран `SpeciesParams` sealed-маркер (`Dog(method,size)`, `Cat(type)`); runtime `require(params is …)` в каждой реализации
- [x] create `LifeStageCalculator` sealed interface аналогично (один-к-одному с calculator)
- [x] refactor `DogAgeCalculator` → `data object DogAgeCalculator : AgeCalculator` (сохраняем API toHumanYears(age, method, size?)) — companion-константы перенесены в тело object (companion у object недоступен), путь доступа сохранён
- [x] refactor `CatAgeCalculator` → `data object CatAgeCalculator : AgeCalculator`
- [x] refactor `CalculatePetAgeUseCase` чтобы использовать `AgeCalculator.forSpecies(pet.species)` вместо явного `when (pet.species) { Dog -> ..., Cat -> ... }` — калькуляторы больше не инжектируются (конструктор сокращён до `settingsReader, clock`); `when` остался только в `resolveParams` для сборки `SpeciesParams` + выбора метода
- [x] add factory `AgeCalculator.Companion.forSpecies(Species): AgeCalculator?` — null для not-yet-implemented видов
- [x] verify все pre-existing тесты Plan 1 продолжают проходить — **Green** для refactor (обновлены 3 места конструирования UseCase + 9 calc-тестов с `()` → object-ссылка + `DomainModule`)
- [x] write tests for refactor invariants: forSpecies(Dog) == DogAgeCalculator, forSpecies(Cat) == CatAgeCalculator, forSpecies(Rabbit) == null до Task 2
- [x] run `./gradlew :core:calculator:test :core:domain:test --no-daemon` — must pass before next task (также прогнаны `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt + koverVerify — всё зелёное)

### Task 2: RabbitAgeCalculator + life stages (TDD)
- [x] write FAILING test `RabbitAgeCalculatorTest` (по §4.3, кусочная формула): покрыты 0.1y=3, 0.33y=12, 0.5y≈13.36, 1y=21, 2y=27, 5y=45, 8y=63, throws on zero/negative + параметризованная таблица (12 кейсов) + монотонность
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `RabbitSize` enum в `:core:model` (Dwarf, Small, Medium, Large, Giant) по §4.3 + stable `id`
- [x] create `RabbitAgeCalculator : AgeCalculator` в `:core:calculator`
- [x] implement кусочную формулу (использован литерал `0.33` из спеки §4.3, как в эталонных значениях):
  - `age < 0.33 (4 months)` → `30 · age`
  - `0.33 ≤ age < 1` → `12 + 8 · (age − 0.33)`
  - `age ≥ 1` → `21 + 6 · (age − 1)`
  - ⚠️ формула §4.3 разрывна на стыках (привязана к опорным точкам 12 ЧГ и 21 ЧГ); монотонность сохраняется. Вопрос «continuity» в Task 11 будет переформулирован в bounded-jump/monotonicity
- [x] add KDoc со ссылкой на House Rabbit Society + AVMA + §4.3 spec
- [x] add ParameterizedTest с табличными значениями (12 кейсов)
- [x] verify все тесты — **Green**
- [x] add `LifeStage.Rabbit` sealed подтипы по §4.3 (Infancy, Adolescence, YoungAdult, Adult, Senior)
- [x] create `RabbitLifeStageCalculator : LifeStageCalculator` с порогами: Infancy 0-3мес, Adolescence 3-6мес, YoungAdult 6-12мес, Adult 1-5y, Senior 5+
- [x] write FAILING tests для `RabbitLifeStageCalculator` (граничные кейсы для каждого перехода)
- [x] implement and verify — **Green**
- [x] add `expectedLifespanRange(size: RabbitSize): ClosedFloatingPointRange<Double>` (8-12 base; Dwarf/Small ≤14; Large 7-10; Giant 6-8) с тестами
- [x] update `Species.Rabbit.isImplemented = true`
- [x] update `AgeCalculator.forSpecies(Rabbit)` + `LifeStageCalculator.forSpecies(Rabbit)` returns Rabbit calculators
- [x] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task (также прогнаны `:core:model:test`, `:core:domain:test`, feature-тесты, `:app:compileDebugKotlin`, detekt, koverVerify — всё зелёное)
- [x] ➕ cross-module sync (обнаружено): добавлена ветка `Rabbit` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Rabbit(size)`; exhaustive `when (lifeStage)` в `PetDetailScreen`/`QuickCalcResultSheet` дополнены ветками Rabbit + строковые ресурсы ru/en (предварительная локализация, финал — Task 22); domain-тесты «unsupported species» переключены с Rabbit на Fish; `SpeciesTest`/`LifeStageTest` обновлены

### Task 3: HamsterAgeCalculator + life stages (TDD)
- [x] write FAILING test `HamsterAgeCalculatorTest` (по §4.4): покрыты 1мес≈7.5, 2мес=18, 6мес=30, 12мес=48, 18мес=66, 24мес=90, throws on zero/negative + параметризованная таблица (9 кейсов) + монотонность
  - `1 month (0.083 year) ≈ 7.6 ЧГ` (0-1 мес: 1 ЧГ за каждые 4 дня → 30/4 ≈ 7.5)
  - `2 months ≈ 18 ЧГ`
  - `6 months ≈ 30 ЧГ`
  - `18 months ≈ 30 + 3·12 = 66 ЧГ`
  - `24 months ≈ 66 + 4·6 = 90 ЧГ`
- [x] verify tests fail — **Red**
- [x] create `HamsterType` enum в `:core:model` (Syrian, Dwarf, Roborovski, Chinese, WinterWhite — 5 видов по §4.4) + stable `id` + per-type `averageLifespanYears` (+ `HamsterTypeTest`)
- [x] create `HamsterAgeCalculator : AgeCalculator`
- [x] implement кусочную формулу по §4.4 (в месяцах; сегменты 2–6 и 6–18 мес. объединены — оба +3 ЧГ/мес.; формула **непрерывна** на всех стыках 1/2/18 мес., в отличие от §4.3)
- [x] add KDoc со ссылкой на RVC VetCompass + Animallama + §4.4
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.Hamster` sealed подтипы (Pup, Juvenile, Adult, Senior, VerySenior) по §4.4
- [x] create `HamsterLifeStageCalculator` + tests (Pup <3нед, Juvenile <2мес, Adult <1г, Senior <1.5г, VerySenior 1.5+ — «старость с 1.5 лет»)
- [x] add `expectedLifespanRange(type: HamsterType)` (Syrian/Chinese 2-3, Roborovski 3-3.5, Dwarf/WinterWhite 1.5-2) + tests
- [x] update `Species.Hamster.isImplemented = true` + `AgeCalculator.forSpecies(Hamster)` + `LifeStageCalculator.forSpecies(Hamster)`
- [x] run tests — must pass before next task (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, koverVerify — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Task 2): добавлена ветка `Hamster` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Hamster(type)` (дефолт `HamsterType.Syrian`); exhaustive `when (lifeStage)` в `PetDetailScreen`/`QuickCalcResultSheet` дополнены ветками Hamster + строковые ресурсы ru/en; `SpeciesTest`/`LifeStageTest` обновлены (implemented = 4)
- [x] ➕ refactor (обнаружено): `lifeStageLabelRes` в обоих экранах разбит на диспетчер `when (is LifeStage.X)` + под-функции по виду — цикломатическая сложность вышла за detekt-порог 18 при 21 ветке; теперь масштабируется на Tasks 4–10

### Task 4: GuineaPigAgeCalculator + life stages (TDD)
- [x] write FAILING test `GuineaPigAgeCalculatorTest` (по §4.5): покрыты 0.058y=6, 0.208y=11.5, 0.42y=20, 1y=24.64, 4y=48.64, 7y=78.64, throws on zero/negative + параметризованная таблица (9 кейсов) + монотонность
  - `3 weeks (0.058 year) ≈ 6 ЧГ` (точка отъёма)
  - `2-3 months ≈ 11.5 ЧГ`
  - `5 months (0.42 year) ≈ 20 ЧГ`
  - `1 year = 20 + 8·(7/12) ≈ 24.7` (после 5 месяцев)
  - `4 years ≈ 20 + 8·(4 − 0.42) ≈ 48.6`
  - `7 years ≈ 48.6 + 10·3 ≈ 78.6`
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `GuineaPigAgeCalculator : AgeCalculator`
- [x] implement кусочную формулу по §4.5 (5 непрерывных сегментов: 3 линейных интерполяции 0→6→11.5→20, затем +8/год до 4 лет, +10/год после — в отличие от §4.3 формула непрерывна на всех стыках)
- [x] add KDoc со ссылкой на Oxbow Animallama + §4.5
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.GuineaPig` sealed подтипы (Pup, Juvenile, Adult, Senior, Geriatric) по §4.5
- [x] create `GuineaPigLifeStageCalculator` + tests (senior с 4 лет; Geriatric с 6 лет; пороги Pup<0.058, Juvenile<0.42, Adult<4, Senior<6)
- [x] add `expectedLifespanRange()` = 5..7 + tests (без параметра — у вида нет подкатегорий)
- [x] update `Species.GuineaPig.isImplemented = true` + `AgeCalculator.forSpecies(GuineaPig)` + `LifeStageCalculator.forSpecies(GuineaPig)`
- [x] run tests — must pass before next task (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, koverVerify — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–3): добавлена ветка `GuineaPig` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.GuineaPig` (data object без подкатегории); диспетчер `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнен веткой GuineaPig + под-функция + строковые ресурсы ru/en; `SpeciesTest` (implemented = 5)/`LifeStageTest` обновлены

### Task 5: RatAgeCalculator + MouseAgeCalculator + life stages (TDD)
- [x] write FAILING test `RatAgeCalculatorTest` (Sengupta 2013): покрыты 0.5y=8.3, 1y=15.2, 2y=29.0, 3y=42.8, throws on zero/negative + параметризованная таблица (7 кейсов) + монотонность
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `RatAgeCalculator : AgeCalculator`
- [x] implement `13.8 · ageInYears + 1.4` (extract constants `RAT_COEFFICIENT`, `RAT_OFFSET`)
- [x] add KDoc со ссылкой на Sengupta 2013, Int J Prev Med 4(6):624-630 + §4.6
- [x] verify — **Green**
- [x] write FAILING test `MouseAgeCalculatorTest` (Dutta & Sengupta 2016 piecewise): покрыты 30дн=12.33, 100дн=24.41, 200дн=35.92, 500дн=58.73, 800дн=78.32, throws on zero/negative + параметризованная таблица (9 кейсов с границами фаз) + монотонность
  - ⚠️ эталонные значения §4.6 для возрастов >30 дней внутренне противоречивы («200дн ≈ 21 ЧГ» несовместимо с выведенным «30дн = 30·150/365 ≈ 12.33 ЧГ»). Реализация привязана к единственной математически выведенной опорной точке (30 дней) + чистой кусочной интеграции накопленных человеко-дней; тесты используют согласованные с моделью значения (см. KDoc `MouseAgeCalculator` + комментарий в тесте). Формула «12.33 + 58·45/365 ≈ 19.5» из плана ошибочна (стартует от 30-дневного значения вместо 42-дневного)
- [x] verify tests fail — **Red**
- [x] create `MouseAgeCalculator : AgeCalculator`
- [x] implement кусочную формулу с конвертацией age в дни → суммирование human-days по фазам → /365 для возврата в годах
- [x] add KDoc со ссылкой на Dutta & Sengupta 2016, Life Sciences 152:244-248 + §4.6
- [x] add ParameterizedTest для обоих калькуляторов
- [x] verify — **Green**
- [x] add `LifeStage.Rat` и `LifeStage.Mouse` sealed подтипы (Pup, Juvenile, Adult, Senior, EndOfLife)
- [x] create `RatLifeStageCalculator` и `MouseLifeStageCalculator` + tests (Rat senior с 18 мес = 1.5y; Mouse senior с 12 мес = 1.0y; EndOfLife Rat 2.5y / Mouse 2.0y)
- [x] add `expectedLifespanRange()` для обоих (Rat 2-3, Mouse 1-3) + tests
- [x] update `Species.Rat` и `Species.Mouse` `isImplemented = true` + `AgeCalculator.forSpecies()` + `LifeStageCalculator.forSpecies()`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, koverVerify — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–4): добавлены ветки `Rat`/`Mouse` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Rat`/`SpeciesParams.Mouse` (data object без подкатегории); диспетчер `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнен ветками Rat/Mouse + под-функции + строковые ресурсы ru/en; `SpeciesTest` (implemented = 7)/`LifeStageTest` обновлены; null-примеры в `AgeCalculatorTest`/`LifeStageCalculatorTest` переключены с Mouse на Ferret; добавлены end-to-end domain-тесты Rat/Mouse (восстановление kover ≥90%)

### Task 6: FerretAgeCalculator + life stages (TDD)
- [x] write FAILING test `FerretAgeCalculatorTest` (по §4.7): покрыты 2мес=10, 6мес=30, 1г=40, 3г=48, 7л=64, throws on zero/negative + параметризованная таблица (10 кейсов) + непрерывность на стыках 0.5/1.0 + монотонность
  - `2 months (0.167 year) = +5·2 = 10 ЧГ`
  - `6 months = 30 ЧГ`
  - `1 year = 40 ЧГ`
  - `3 years = 40 + 4·2 = 48 ЧГ`
  - `7 years = 40 + 4·6 = 64 ЧГ`
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `FerretAgeCalculator : AgeCalculator`
- [x] implement кусочную формулу по §4.7 (3 непрерывных сегмента: age<0.5 → 60·age; 0.5–1 → 30+20·(age−0.5); ≥1 → 40+4·(age−1) — непрерывна на стыках 0.5/1.0, как §4.4/§4.5)
- [x] add KDoc со ссылкой на PMC «Senior Ferret» PMC7129291 + Oxbow Ferret Life Stages + §4.7
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.Ferret` подтипы (Kit, Juvenile, Adult, Senior, Geriatric) по §4.7
- [x] create `FerretLifeStageCalculator` + tests (Kit<0.33, Juvenile<1, Adult<3, Senior 3–5, Geriatric 5+ — «senior с 3–4 лет»)
- [x] add `expectedLifespanRange()` = 5..10 + tests (без параметра — у вида нет подкатегорий)
- [x] update `Species.Ferret.isImplemented = true` + `AgeCalculator.forSpecies(Ferret)` + `LifeStageCalculator.forSpecies(Ferret)`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, koverVerify — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–5): добавлена ветка `Ferret` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Ferret` (data object без подкатегории); диспетчер `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнен веткой Ferret + под-функция + строковые ресурсы ru/en; `SpeciesTest` (implemented = 8)/`LifeStageTest` обновлены; null-примеры в `AgeCalculatorTest`/`LifeStageCalculatorTest` переключены с Ferret на Reptile; добавлен end-to-end domain-тест Ferret (восстановление kover ≥90%)

### Task 7: ScalarRatioFormula helper + BirdAgeCalculator + life stages (TDD)
- [x] write FAILING test `ScalarRatioFormulaTest` (helper для Bird/Reptile/Fish): 6 кейсов (baseline 1:1, линейность, короткая ЧЖ ускоряет, инвариант ratio=80/lifespan, throws на zero/negative lifespan)
- [x] verify tests fail — **Red** (написаны до реализации)
- [x] create `internal object ScalarRatioFormula { fun compute(age: Double, lifespan: Double): Double = age * 80.0 / lifespan }` (с `require(lifespan > 0)` + константой `HUMAN_REFERENCE_LIFESPAN`)
- [x] verify — **Green**
- [x] add KDoc со ссылкой на §4.8, §4.9, §4.11 (общий helper для Bird/Reptile/Fish — Tasks 7/8/10)
- [x] write FAILING test `BirdAgeCalculatorTest` (по §4.8): покрыты budgerigar 1y=11.43, 3y=34.29, cockatiel 5y=26.67, macaw 10y=16, canary 2y=16, 6 недель=1.71 (×1.3), стык 0.5 (отключение множителя), throws zero/negative + параметризованная таблица (7 видов/возрастов) + монотонность
- [x] verify tests fail — **Red**
- [x] create `BirdType` enum в `:core:model` (10 видов по §4.8) + stable `id` + per-type `averageLifespanYears` (Budgerigar=7, Cockatiel=15, Canary=10, Macaw/Amazon/AfricanGrey/Cockatoo=50, …) + `BirdTypeTest`
- [x] create `BirdAgeCalculator : AgeCalculator` использующий `ScalarRatioFormula.compute(age, type.averageLifespanYears)` + 1.3× для age < 0.5y (поправка ускоренного взросления птенцов)
- [x] add KDoc со ссылкой на AAV «Care for Senior Parrots» + Lafeber Vet + §4.8
- [x] add ParameterizedTest с 7 видами/возрастами птиц
- [x] verify — **Green**
- [x] add `LifeStage.Bird` подтипы (Hatchling, Juvenile, Adult, Senior, Geriatric) по §4.8 + `LifeStageTest`
- [x] create `BirdLifeStageCalculator` + tests (фракция от average lifespan: hatchling <5%, juvenile <20%, adult 20-70%, senior 70-90%, geriatric >90%; границы тестов подобраны внутри полос во избежание IEEE-754 неоднозначности на стыке доли)
- [x] add `expectedLifespanRange(type: BirdType)` (5-10 для волнистых, до 40-60 для ара/какаду) + tests
- [x] update `Species.Bird.isImplemented = true` + `AgeCalculator.forSpecies(Bird)` + `LifeStageCalculator.forSpecies(Bird)`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, `:core:calculator:koverVerify`, `:core:domain:koverVerify` — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–6): добавлена ветка `Bird` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Bird(type)` (дефолт `BirdType.Budgerigar`); диспетчеры `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнены `birdLifeStageLabelRes` + ветка объяснения метода (AAV scalar) + строковые ресурсы ru/en; `SpeciesTest` (implemented = 9)/`LifeStageTest`/`AgeCalculatorTest` (null-список → Reptile/Horse/Fish) обновлены; добавлены end-to-end domain-тесты Bird (default Budgerigar + macaw subcategory — восстановление kover ≥90%)

### Task 8: ReptileAgeCalculator + life stages (TDD)
- [x] write FAILING test `ReptileAgeCalculatorTest` (по §4.9): покрыты box_turtle 5y=10, corn_snake 3y=12, green_iguana 5y=20, leopard_gecko 2y=10.7, throws on zero/negative + параметризованная таблица (7 кейсов) + монотонность + инвариант ratio=80/lifespan
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `ReptileType` enum в `:core:model` (BoxTurtle, RedEaredSlider, BeardedDragon, BallPython, CornSnake, GreenIguana, LeopardGecko, CrestedGecko — 8 видов по §4.9) + stable `id` + per-type `averageLifespanYears` (+ `ReptileTypeTest`)
- [x] create `ReptileAgeCalculator : AgeCalculator` через `ScalarRatioFormula` (без поправки молодняка, в отличие от §4.8 птицы — рептилии не дают резкого ускорения взросления)
- [x] add KDoc со ссылкой на PetPlace + Reptile Centre + A-Z Animals + §4.9
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.Reptile` подтипы (Hatchling, Juvenile, Adult, Senior) — 4 фазы по §4.9
- [x] create `ReptileLifeStageCalculator` + tests (доля от видовой ЧЖ: Hatchling <5%, Juvenile <20%, Adult <75%, Senior 75%+; границы тестов внутри полос во избежание IEEE-754 неоднозначности)
- [x] add `expectedLifespanRange(type: ReptileType)` (10-20 гекконы, 40-60 черепахи) + tests
- [x] update `Species.Reptile.isImplemented = true` + `AgeCalculator.forSpecies(Reptile)` + `LifeStageCalculator.forSpecies(Reptile)`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, `:core:calculator:koverVerify`, `:core:domain:koverVerify` — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–7): добавлена ветка `Reptile` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Reptile(type)` (дефолт `ReptileType.BeardedDragon`); диспетчеры `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнены `reptileLifeStageLabelRes` + ветка объяснения метода (PetPlace scalar) + строковые ресурсы ru/en; `SpeciesTest` (implemented = 10)/`LifeStageTest`/`AgeCalculatorTest`/`LifeStageCalculatorTest` (null-список → Horse/Fish) обновлены; добавлены end-to-end domain-тесты Reptile (default BeardedDragon + box turtle subcategory — восстановление kover ≥90%)

### Task 9: HorseAgeCalculator + life stages (TDD)
- [x] write FAILING test `HorseAgeCalculatorTest` (по §4.10): покрыты 1y=6.5, 2y=13, 3y=18, 4y=20.5, 10y=35.5, 25y=73, throws on zero/negative + параметризованная таблица (10 кейсов) + непрерывность на стыках 1/2/3 + монотонность
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `HorseType` enum в `:core:model` (Pony, LightHorse, DraftHorse, Thoroughbred) + stable `id` + per-type `averageLifespanYears` (Pony 30, Light/Thoroughbred 28, Draft 25) + `HorseTypeTest`
- [x] create `HorseAgeCalculator : AgeCalculator`
- [x] implement 3-фазную формулу (1y=6.5, 2y=13, 3y=18, далее +2.5/год; сегменты «4-й год» и «>4 лет» из §4.10 имеют одинаковый наклон +2.5 → объединены в `age>3`; формула непрерывна на стыках 1/2/3)
- [x] add KDoc со ссылкой на AAEP Vaccination/Senior Horse Care + PetMD (Kaela Schraer DVM) + §4.10
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.Horse` подтипы (Foal, Yearling, YoungAdult, Adult, Senior) по §4.10 + `LifeStageTest`
- [x] create `HorseLifeStageCalculator` + tests (Foal<1, Yearling<2, YoungAdult<4, Adult<15, Senior 15+ — «senior с ~15 лет» по AAEP Senior Horse Care)
- [x] add `expectedLifespanRange(type: HorseType)` (база 25–30; Pony 25–35, Draft 20–25) + tests
- [x] update `Species.Horse.isImplemented = true` + `AgeCalculator.forSpecies(Horse)` + `LifeStageCalculator.forSpecies(Horse)`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, `:core:calculator:koverVerify`, `:core:domain:koverVerify` — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–8): добавлена ветка `Horse` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Horse(type)` (дефолт `HorseType.LightHorse`); диспетчеры `lifeStageLabelRes` в `PetDetailScreen`/`QuickCalcResultSheet` дополнены `horseLifeStageLabelRes` + ветка объяснения метода (AAEP 3-фаза) + строковые ресурсы ru/en; `SpeciesTest` (implemented = 11)/`LifeStageTest`/`AgeCalculatorTest`/`LifeStageCalculatorTest` (null-список → только Fish) обновлены; добавлены end-to-end domain-тесты Horse (default LightHorse + pony subcategory — восстановление kover ≥90%)
- [x] ➕ refactor (обнаружено): кластер `lifeStageLabelRes` + 11 под-функций вынесен из `PetDetailScreen.kt` в новый файл `LifeStageLabels.kt` — file-level `TooManyFunctions` detekt-порог (25) был превышен при добавлении ветки Horse; вынос масштабируется на Task 10 (Fish)

### Task 10: FishAgeCalculator + life stages (TDD)
- [x] write FAILING test `FishAgeCalculatorTest` (по §4.11): покрыты guppy 1y=40, betta 3y=48, goldfish 5y=26.67, koi 10y=26.67, neon_tetra 2y=20, throws on zero/negative + параметризованная таблица (7 кейсов) + монотонность + инвариант ratio=80/lifespan
- [x] verify tests fail — **Red** (написаны до реализации калькулятора)
- [x] create `FishType` enum в `:core:model` (Goldfish=15, Koi=30, Betta=5, Guppy=2, AngelFish=10, NeonTetra=8, Tropical=5, Discus=10 — 8 видов по §4.11) + stable `id` + per-type `averageLifespanYears` + `FishTypeTest`
- [x] create `FishAgeCalculator : AgeCalculator` через `ScalarRatioFormula` (без поправки молодняка, как §4.9 рептилия)
- [x] add KDoc со ссылкой на PetMD + Kodama Koi Farm + AquariumStoreDepot + §4.11
- [x] add ParameterizedTest
- [x] verify — **Green**
- [x] add `LifeStage.Fish` подтипы (Fry, Juvenile, Adult, Senior) — 4 фазы по §4.11 + `LifeStageTest`
- [x] create `FishLifeStageCalculator` + tests (доля от видовой ЧЖ: Fry <5%, Juvenile <20%, Adult <75%, Senior 75%+; границы тестов внутри полос во избежание IEEE-754 неоднозначности)
- [x] add `expectedLifespanRange(type: FishType)` (guppy 1–3, koi 25–40) + tests
- [x] update `Species.Fish.isImplemented = true` + `AgeCalculator.forSpecies(Fish)` + `LifeStageCalculator.forSpecies(Fish)`
- [x] run tests (прогнаны `:core:model:test`, `:core:calculator:test`, `:core:domain:test`, `:feature:quickcalc:test`, `:feature:pets:test`, `:app:compileDebugKotlin`, detekt, `:core:calculator:koverVerify`, `:core:domain:koverVerify` — всё зелёное)
- [x] ➕ cross-module sync (обнаружено, как в Tasks 2–9): добавлена ветка `Fish` в `CalculatePetAgeUseCase.resolveParams` + `SpeciesParams.Fish(type)` (дефолт `FishType.Goldfish`); диспетчеры `lifeStageLabelRes` в `LifeStageLabels.kt`/`QuickCalcResultSheet` дополнены `fishLifeStageLabelRes` + ветка объяснения метода (PetMD scalar) + строковые ресурсы ru/en. Поскольку Fish — последний нереализованный вид, `forSpecies`/`resolveParams` стали исчерпывающими `when` (убран `else`); тесты-«unsupported species» в `CalculatePetAgeUseCaseTest`/`SavePetUseCaseTest` переключены на end-to-end Fish + добавлен прямой `UnsupportedSpeciesExceptionTest`; `SpeciesTest`/`LifeStageTest`/`AgeCalculatorTest`/`LifeStageCalculatorTest` обновлены (implemented = 12)

### Task 11: Property-based tests for all new species (Kotest)
- [x] write `RabbitAgeCalculatorPropertyTest`:
  - monotonically increasing in age
  - continuity at piecewise boundaries (age=1/3, age=1) — ⚠️ формула §4.3 намеренно разрывна на стыках (привязка к опорным точкам 12/21 ЧГ); как и отмечено в Task 2, «continuity» переформулирована в **bounded-jump** (скачок положителен и ограничен ≤ 4.5 ЧГ) + глобальная монотонность
  - positivity for positive input
  - upper bound 200 ЧГ for age ≤ 30
- [x] write similar property tests для `HamsterAgeCalculator`, `GuineaPigAgeCalculator`, `FerretAgeCalculator` (continuity на piecewise boundaries — все три непрерывны: проверка `|f(b−ε) − f(b+ε)| < tol`)
- [x] write `RatAgeCalculatorPropertyTest` (linear → trivial monotonicity + affine-invariant slope = 13.8)
- [x] write `MouseAgeCalculatorPropertyTest` (continuity на 4 границах: 42дн, 180дн, 365дн, 730дн — границы берутся из internal-констант `PHASE_*_END / DAYS_PER_YEAR`)
- [x] write `BirdAgeCalculatorPropertyTest` (∀ BirdType: monotonicity **по обе стороны порога 0.5** — глобальная монотонность нарушена намеренным скачком вниз ×1.3; отдельный тест проверяет `f(0.5−ε)/f(0.5+ε) → 1.3`)
- [x] write `ReptileAgeCalculatorPropertyTest` (∀ ReptileType: monotonicity, ratio к lifespan = 80/lifespan)
- [x] write `HorseAgeCalculatorPropertyTest` (continuity на 1y, 2y, 3y + 4y — точка 4 г. внутри одного сегмента, разрыва нет)
- [x] write `FishAgeCalculatorPropertyTest` (∀ FishType: monotonicity, ratio invariant)
- [x] write `LifeStageCalculatorPropertyTest` для всех новых видов (если `age1 < age2` тогда `stage(age1).ordinal ≤ stage(age2).ordinal`) — расширен существующий файл: список `newSpeciesCalculators` (10 видов) + monotonicity-ordinal + throws-on-nonpositive через унифицированный `determine(age, params)`
- [x] run `./gradlew :core:calculator:test --no-daemon` — зелёное
- [x] verify coverage `:core:calculator` ≥ 95%: `./gradlew :core:calculator:koverHtmlReport && koverVerify` — koverVerify (minBound=95) прошёл
- [x] must pass before next task — detekt + koverVerify зелёные

### Task 12: Database migration + PetMapper expansion
- [x] write FAILING test `PetMapperTest` для каждого нового subcategory type:
  - `Pet(species=Rabbit, subcategory=RabbitSize.Dwarf) → PetEntity round-trip preserves subcategory` — реализован параметризованный round-trip `newSpeciesSubcategories` (все 6 видов × все их id: RabbitSize/HamsterType/BirdType/ReptileType/HorseType/FishType)
  - аналогично для Hamster, GuineaPig, Bird, Reptile, Horse, Fish (Rat/Mouse/Ferret не имеют подкатегорий) — добавлен `subcategorylessSpecies` round-trip с null для GuineaPig/Rat/Mouse/Ferret (⚠️ GuineaPig в исходном тексте указан ошибочно как имеющий подкатегорию — у вида её нет, см. Task 4)
  - `unknown subcategory id → IllegalStateException` (как с Dog/Cat в Plan 1) — ⚠️ переформулировано: subcategory — **opaque TEXT, не валидируется** на границе mapper'а (в отличие от species/gender); добавлен тест `mapper does not validate subcategory id`, фиксирующий это сознательное решение Plan 1 (именно оно избавляет от миграции). Тесты сразу **Green** — производственный код менять не требуется (нет Red-фазы; характеристические lock-in тесты)
- [x] update `PetMapper` чтобы маппил new subcategory types via stable `id` strings — **изменений не требуется**: mapper species-agnostic, передаёт subcategory-строку насквозь; новые id уже корректно сериализуются
- [x] verify tests — **Green** (`./gradlew :core:database:test` — 14 PetMapperTest-кейсов + параметризованные прошли)
- [x] check schema: должна ли увеличиться `DATABASE_VERSION` с 1 до 2? — **migration НЕ нужна**: subcategory хранится как `TEXT` и принимает любые id-строки, SQL-схема не меняется
- [x] **Если migration нужна:** N/A — миграция не требуется (см. решение ниже)
- [x] **Если migration не нужна:** документировать решение в KDoc `PawClockDatabase` + добавить explicit Mapper-test, что новые subcategory id'ы сериализуются корректно — KDoc `PawClockDatabase` дополнен блоком «Plan 2 — миграция НЕ требуется, версия остаётся 1»; `DATABASE_VERSION` остаётся 1, `Migrations.all()` пуст; round-trip тесты добавлены
- [x] run `./gradlew :core:database:test --no-daemon` — зелёное
- [x] run `./gradlew :core:database:assembleDebugAndroidTest --no-daemon` (smoke check, polnyj run в nightly.yml) — компилируется чисто
- [x] must pass before next task — `:core:database:test` + `assembleDebugAndroidTest` + `detekt` зелёные (⚠️ `:core:database:koverVerify` структурно недостижим на JVM ≥80%: DAO/DI/Room-обвязка покрывается instrumented androidTest, который kover JVM не измеряет — реальное покрытие достигается в nightly.yml; koverVerify для database намеренно НЕ в гейте Task 12)

### Task 13: Care recommendations placeholder JSON для всех 10 видов
- [x] create asset directory structure для каждого вида: `:app/src/main/assets/care/{species}/{stage}/{ru,en}.json` — созданы 48 стадий × 2 локали = 96 новых файлов (10 видов; всего с Plan 1 dog/cat — 58 стадий × 2 = 116 файлов)
- [x] create JSON files с placeholder контентом (поля по `CareRecommendation` модели: `stage_description`, `nutrition`, `activity`, `veterinary_check_frequency`, `dental_care`, `warning_signs`, `source_url`, `source_name`, `disclaimer`) — ⚠️ у `fish` поле `dental_care` опущено намеренно (неприменимо; `CareRecommendation.dentalCare` nullable)
- [x] для каждого файла:
  - `stage_description` = `"TODO(content-pass-after-plan-2): описание стадии «{stage}» для вида «{species}»."` (ru) / en-аналог
  - `nutrition`/`activity` = короткий placeholder с маркером `TODO(content-pass-after-plan-2)`
  - `source_url` = соответствующий URL из §14 spec (rabbit.org, RVC VetCompass, PMC3733029/Sengupta, lafeber.com/AAV, aaep.org и т.д.)
  - `disclaimer` = "Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача." (ru) / en-аналог из §3.3
- [x] write FAILING test `CareRepositoryTest` для каждого нового вида:
  - `loads rabbit infancy ru` (добавлен)
  - `loads bird hatchling en` (добавлен)
  - `loads horse foal ru` (добавлен, через ru→en fallback) — sample-проверки path-resolution через FakeAssetSource
- [x] verify load работает через расширения существующего CareRepository (без изменений в коде — только новые assets) — добавлен `CareAssetsIntegrityTest`: реальный `CareRepositoryImpl` поверх файлов на диске, 116 dynamic-тестов (каждый implemented вид × стадия × {ru,en}) проверяют наличие + десериализацию + непустые поля + дисклеймер. Был бы Red до Task 13, стал Green без правок production-кода
- [x] verify — **Green** (`:core:domain:test` — CareAssetsIntegrityTest 116/0/0, CareRepositoryTest 15/0/0, весь модуль зелёный)
- [x] add `scripts/verify-care-assets.sh` — проверяет наличие + непустоту + обязательные поля + JSON-валидность (node) для каждого вида × стадии × {ru,en}; маппинг species→stages зеркалит `LifeStage.*.all()`
- [x] run script → all green (`✅ All 116 care assets present and well-formed.`)
- [x] update care content tracker в `docs/CARE_CONTENT.md` (новый файл) — таблица статусов всех видов (TODO), content-pass checklist, инструкции по `grep`/скрипту/тесту
- [x] run tests — `:core:domain:test` + `:core:domain:detekt` + `:core:domain:koverVerify` (minBound=90) зелёные

### Task 14: Stylized species icons (vector drawables, §5.6)
- [x] research/select SVG-иконки для всех 12 видов в стиле Phosphor / Lucide (моно-линейные, 48dp viewBox); либо нарисовать собственные простые — нарисованы собственные простые моно-силуэты (голова+уши для млекопитающих, профиль для bird/horse, силуэты для reptile/fish); viewport 24×24, монохромные (fillColor чёрный, перекрашиваются через Compose `Icon(tint=…)`)
- [x] add `ic_species_{dog,cat,rabbit,hamster,guinea_pig,rat,mouse,ferret,bird,reptile,horse,fish}.xml` как Android Vector Drawables в `:core:designsystem/src/main/res/drawable/` — созданы 12 файлов (новый `res/drawable/` в модуле; ⚠️ `android:tint="?attr/colorControlNormal"` убран — appcompat-атрибут недоступен в модуле, tint задаётся в Compose)
- [x] create `SpeciesIcon` composable в `:core:designsystem` который маппит `Species` → соответствующий `painterResource` (when-блок exhaustive по sealed) — `SpeciesIcon.kt`: composable + `@DrawableRes speciesIconRes(species)` с exhaustive `when` (R = `app.pawclock.core.designsystem.R`)
- [x] write FAILING test `SpeciesIconTest`:
  - `Composable for Species.Dog renders successfully` (используя `createComposeRule` или Roborazzi screenshot test opt-in) — рендер-проверка через opt-in `SpeciesIconScreenshotTest` (captureRoboImage по всем 12 видам; падение composable = падение capture)
  - все 12 видов имеют не-null painter (через `LocalContext.resources.getIdentifier`) — реализовано через Robolectric + `ResourcesCompat.getDrawable` (резолв реальных ассетов модуля) + уникальность + исчерпываемость маппинга
- [x] verify tests — **Green** (`SpeciesIconTest` 3/3 PASSED; `SpeciesIconScreenshotTest` SKIPPED — opt-in)
- [x] update `PetCard` composable в `:feature:pets` — заменить emoji avatar на `SpeciesIcon(pet.species)` — `SpeciesAvatar` сохранил круг `primaryContainer`, emoji-`Text` заменён на `SpeciesIcon(tint=onPrimaryContainer)`
- [x] update PetEditor `SpeciesSelector` чтобы рядом с label каждого вида показывался `SpeciesIcon` (uniform layout, иконка слева) — добавлен `leadingIcon = { SpeciesIcon(... size 18dp) }` в `FilterChip`
- [x] update QuickCalculator `QuickCalcSpeciesSelector` аналогично — добавлен `leadingIcon` в `FilterChip`
- [x] add Roborazzi screenshot tests opt-in (как в Plan 1) для каждой иконки (12 snapshots) — `SpeciesIconScreenshotTest` (цикл по `Species.all()` → 12 PNG `SpeciesIcon_{id}.png`); baseline PNG не коммитятся (как и Plan 1 — тесты opt-in/skipped в CI)
- [x] update `PetsListScreenTest` если поменялся node tree (emoji → drawable) — изменений не требуется: тест ассертит имена питомцев + FAB, не аватар; emoji-ссылок в нём нет
- [x] run `./gradlew :core:designsystem:test :feature:pets:test :feature:editor:test :feature:quickcalc:test --no-daemon` — всё зелёное (также прогнаны detekt по 4 модулям + `:app:assembleDebug` для проверки merge ресурсов)

### Task 15: UI расширение PetEditor для всех 12 видов
- [x] write FAILING test `PetEditorViewModelTest`:
  - `SelectSpecies(Rabbit) → availableSubcategories = RabbitSize values`
  - `SelectSpecies(Hamster) → HamsterType values`
  - `SelectSpecies(Bird) → BirdType values`
  - `SelectSpecies(Reptile) → ReptileType values`
  - `SelectSpecies(Fish) → FishType values`
  - `SelectSpecies(Horse) → HorseType values`
  - `SelectSpecies(Rat) → emptyList` (нет подкатегории) — объединено в один параметризованный тест `selecting subcategoryless species exposes no subcategories` (Rat/Mouse/Ferret + ➕ GuineaPig — у него тоже нет подкатегорий, см. Task 4)
  - `SelectSpecies(Mouse) → emptyList`
  - `SelectSpecies(Ferret) → emptyList`
- [x] verify tests fail — **Red** (поскольку `subcategoriesFor(species)` пока возвращал emptyList для not-Dog/Cat)
- [x] update `PetEditorState.subcategoriesFor(species)` чтобы возвращать соответствующие subcategory enum'ы для всех 12 видов — exhaustive `when (Species?)` (8 видов с подкатегориями + 4 без + null); KDoc отмечает, что `label` — fallback, а локализация резолвится через `(species, id)`
- [x] update PetEditor `SpeciesSelector` чтобы показывал `Species.implemented()` (все 12 после Tasks 2-10) — уже итерирует `Species.implemented()` (изменений не требуется; локализация имён видов — Task 22)
- [x] update PetEditor `SubcategorySelector` чтобы корректно рендерил label для каждого subcategory type — ⚠️ `subcategoryLabelRes` стал **species-aware** `(species, id)`: id подкатегорий пересекаются между видами (`small`/`medium`/`large`/`giant` у Dog+Rabbit; `dwarf` у Rabbit+Hamster), плоский `when (id)` дал бы коллизии. Диспетчер по виду + per-species под-функции (низкая цикломатическая сложность); `species` прокинут из `PetEditorContent`
- [x] add stringResource keys для всех новых subcategories — добавлены ключи `rabbit_size_*` (5), `hamster_type_*` (5), `bird_type_*` (10), `reptile_type_*` (8), `horse_type_*` (4), `fish_type_*` (8) в ru + en (en добавлен сразу во избежание missing-translation lint; финальная вычитка — Task 22)
- [x] write Compose UI test `PetEditorScreenTest`:
  - `selecting Rabbit shows RabbitSize chips` (`subcategorySelector_showsRabbitSizeChipsForRabbit` — проверяет «Карликовый»/«Гигантский», а не собачьи метки)
  - `selecting Bird shows BirdType chips` (`subcategorySelector_showsBirdTypeChipsForBird`)
  - `selecting Rat hides subcategory section` (`subcategorySelector_hiddenForSubcategorylessSpecies`)
- [x] verify — **Green**
- [x] run `./gradlew :feature:editor:test :feature:editor:assembleDebug --no-daemon` — зелёное (также прогнаны `:feature:editor:compileDebugAndroidTestKotlin` для проверки Compose-тестов + `:feature:editor:detekt`)

### Task 16: UI расширение QuickCalculator для всех 12 видов
- [x] write FAILING test `QuickCalcViewModelTest`:
  - `SelectSpecies(Rabbit) → availableSubcategories = RabbitSize values + default = Medium` — добавлен `selecting Rabbit exposes RabbitSize subcategories` (как у Dog/Cat, subcategory НЕ предвыбирается; дефолт Medium применяется при расчёте, проверяется отдельным тестом `defaults to Medium`)
  - аналогично для Hamster/Bird/Reptile/Fish/Horse — объединено в параметризованный `selecting species with subcategories exposes matching enum ids` + `selecting subcategoryless species exposes no subcategories` (GuineaPig/Rat/Mouse/Ferret)
  - `Calculate for Rabbit 5y Medium → ~45 ЧГ + Adult` (добавлен; 4.999<5 → Adult)
  - `Calculate for Bird Budgerigar 3y → ~34.3 ЧГ + Adult` (добавлен)
  - `Calculate for Horse 10y LightHorse → ~35.5 ЧГ + Senior` — ⚠️ исправлено на **Adult**: Senior начинается с 15 лет (HorseLifeStageCalculator из Task 9, AAEP Senior Horse Care); 10y = Adult. План в чек-листе ошибочно указывал Senior
  - method toggle (Wang/Size) показывается только для Dog — добавлен `setting method on non-Dog Success does not change fixed method` (для не-собаки SetMethod не меняет результат, метод остаётся EPIGENETIC)
- [x] verify tests fail — **Red** (subcategory-exposure тесты падали: `subcategoriesFor` возвращал emptyList для новых видов; calc-тесты сразу зелёные — расчёт уже диспатчится через UseCase)
- [x] update `QuickCalcViewModel` чтобы корректно подставлять default subcategory для каждого вида + dispatchить через `AgeCalculator.forSpecies(species)?.toHumanYears(...)` — `QuickCalcState.subcategoriesFor` + `defaultSubcategoryFor` расширены на все 12 видов (зеркалят дефолты `CalculatePetAgeUseCase`); сам dispatch уже идёт через UseCase (synthetic Pet), `when` не дублируется
- [x] update Quick Calculator's species selector / subcategory selector / method toggle UI to support all 12 species — `speciesLabelRes` (exhaustive 12 видов), `QuickCalcSubcategorySelector` стал **species-aware** `(species, id)` (диспетчер по виду + per-species под-функции — id пересекаются между видами); method toggle уже показывался только для Dog (без изменений)
- [x] update `QuickCalcResultSheet` чтобы "Как это посчитано" блок показывал правильный источник через mapping function — `explanationTextRes` стал исчерпывающим `when (species)` (Dog→method-based под-функция, Cat→AAFP, Rabbit→House Rabbit Society, Hamster→RVC, GuineaPig→Oxbow, Rat→Sengupta, Mouse→Dutta&Sengupta, Ferret→PMC, Bird/Reptile/Horse/Fish уже были); добавлены 6 строк объяснений ru+en
- [x] write Compose UI test `QuickCalcScreenTest`:
  - `result sheet shows ~45 human years + Adult for Rabbit Medium 5y` — `resultSheet_showsRabbitHumanYearsAndAdultStage`
  - `result sheet shows ~34 human years for Budgerigar 3y` — `resultSheet_showsBirdHumanYears`
  - `method toggle hidden for non-Dog species` — проверено внутри Rabbit-теста (`quickCalcMethodTag(...).assertDoesNotExist()`); + добавлен `subcategorySelector_showsRabbitSizeChipsForRabbit`
- [x] verify — **Green** (`:feature:quickcalc:testDebugUnitTest` 22/0; androidTest компилируется)
- [x] run `./gradlew :feature:quickcalc:test :feature:quickcalc:assembleDebug --no-daemon` — зелёное (также прогнаны `:feature:quickcalc:compileDebugAndroidTestKotlin`, `:feature:quickcalc:detekt`, `:app:assembleDebug` для проверки слияния ресурсов)

### Task 17: Export Pets — JSON serializer + ExportPetsUseCase (TDD)
- [x] write FAILING test `PetsJsonSerializerTest`: покрыты `serializes single Dog pet with all populated fields` (⚠️ у `PetExportEntry` 7 полей, а не 9 — `id` и `photoPath` намеренно не экспортируются, см. ниже), `serializes list of three pets preserving subcategory types` (Dog/Cat/Rabbit), `serialized JSON contains schema version 1`, `null optional fields are omitted not serialized as null`, `birthDate is ISO-8601 date string`, `species id is used not Species toString` + `does not export id or photoPath` + `exportedAt is ISO-8601 instant string` (8 тестов)
- [x] verify tests fail — **Red** (тесты ссылались на несуществующие `PetsExportSchema`/`PetsJsonSerializer`/`ExportFormat`/`PetRepository.getAll` — заведомая compile-failure до реализации)
- [x] create `PetsExportSchema` data class в `:core:domain/export/` с `@Serializable`:
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
  — реализовано: snake_case-имена полей через `@SerialName` (`schema_version`, `exported_at`, `species_id`, `birth_date`, `subcategory_id`, `gender_id`, `weight_kg`); `CURRENT_SCHEMA_VERSION = 1` константа в companion; обязательные поля (name/species_id/birth_date) объявлены перед необязательными (Kotlin требует default-параметры после required), необязательные с дефолтом `null` (толерантность импорта в Task 19)
- [x] create `PetsJsonSerializer.encode(pets: List<Pet>, exportedAt: Instant): String` — pure-Kotlin без Android-зависимостей (`Json { prettyPrint=true; encodeDefaults=true; explicitNulls=false }` — `schema_version` пишется, null'ы опускаются)
- [x] verify — **Green** (`PetsJsonSerializerTest` 8/0/0)
- [x] write FAILING test `ExportPetsUseCaseTest` (с FakePetRepository, FakeClock):
  - `export empty list yields JSON with empty pets array`
  - `export multiple pets matches repository content` + `uses clock instant as exportedAt`
  - `propagates exception from repository` (⚠️ «IO exception from writer» переформулировано: UseCase возвращает String и не пишет в файл — запись через SAF в Task 21; тест проверяет проброс `IOException` из `getAll()` через приватный `ThrowingPetRepository`)
  - `csv export is not yet implemented` (временный тест: CSV-ветка бросает `NotImplementedError` через `TODO()` до Task 18 — покрывает ветку `when`, удаляется в Task 18)
- [x] create `ExportPetsUseCase(petRepo, clock)` в `:core:domain/export/`:
  - `suspend operator fun invoke(format: ExportFormat): String` где `enum ExportFormat { JSON, CSV }`
  - читает все pets через `petRepo.getAll()` (новый метод)
  - сериализует через `PetsJsonSerializer.encode()` или CSV serializer (Task 18)
- [x] update `PetRepository` interface добавить `suspend fun getAll(): List<Pet>` (в дополнение к `observeAll()` — одноразовый снимок для экспорта, не подписка)
- [x] update `RoomPetRepository` соответственно (+ `PetDao.getAll()` `@Query` с `ORDER BY name COLLATE NOCASE ASC`; Room KSP перегенерировал DAO-impl; ➕ обновлены 3 тест-фейка `FakePetRepository` в `:core:domain`/`:feature:pets`/`:feature:editor` + `PausedFakePetRepository`)
- [x] verify — **Green** (`ExportPetsUseCaseTest` 5/0/0)
- [x] run `./gradlew :core:domain:test --no-daemon` — зелёное (также прогнаны `:core:domain:detekt`, `:core:domain:koverVerify` ≥90%, `:core:database:compileDebugKotlin`, `:app:compileDebugKotlin`, `:feature:pets:test`, `:feature:editor:test` — изменение интерфейса не сломало зависимые модули)

### Task 18: Export Pets — CSV serializer (TDD)
- [x] write FAILING test `PetsCsvSerializerTest`: покрыты `serializes single Dog pet to CSV with header row`, `escapes commas and quotes in name and notes fields` (RFC 4180, удвоение `""`), `newlines in notes are escaped as quoted fields` (встроенный `\n` не разрывает запись), `null fields are empty cells not the string null`, `birthDate is ISO-8601 string`, `header row uses snake_case column names` (6 тестов)
- [x] verify tests fail — **Red** (тесты ссылались на несуществующий `PetsCsvSerializer` — заведомая compile-failure до реализации, как в Task 17)
- [x] create `PetsCsvSerializer.encode(pets: List<Pet>, exportedAt: Instant): String` — pure-Kotlin RFC 4180 implementation; header = `name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes`; разделитель записей CRLF (`\r\n`), без финального line break; `escape()` единым проходом по `List<String?>` (null → пустая ячейка, `,`/`"`/`\r`/`\n` → закавычивание с удвоением кавычек); `exportedAt` принят для симметрии сигнатуры с `PetsJsonSerializer`, но в плоский CSV не пишется (преамбула сломала бы контракт «первая строка = заголовок»; `@Suppress("UNUSED_PARAMETER")` + KDoc)
- [x] verify — **Green** (`PetsCsvSerializerTest` 6/0/0)
- [x] update `ExportPetsUseCase` чтобы поддерживать обе формы (CSV-ветка `when` теперь вызывает `PetsCsvSerializer.encode`; удалён `TODO()`; обновлён KDoc `ExportFormat.CSV`)
- [x] write tests для ExportPetsUseCase с CSV: `csv export yields header plus one data row per pet`, `csv export of empty list yields header only` (заменили временный тест `csv export is not yet implemented`)
- [x] run tests — `./gradlew :core:domain:test :core:domain:detekt :core:domain:koverVerify` зелёное (kover ≥90%; изменение локализовано в `:core:domain`, CSV пока нигде не потребляется — Settings UI в Task 21)

### Task 19: Import Pets — JSON deserializer + ImportPetsUseCase (TDD)
- [x] write FAILING test `PetsJsonDeserializerTest` (13 кейсов): valid schema v1, accepts null optional fields, missing name/species_id/birth_date (по отдельному тесту на каждое), unknown species_id (fail-fast), malformed JSON, empty input, schema_version>1 (forward-incompat), invalid birth_date format, blank name, unknown gender_id → warning, absent schema_version → default
  - ⚠️ «throws ImportException» переформулировано: `decode` НЕ бросает на ошибках данных — возвращает `PetsImportResult.Failure(error)` (Result-тип по контракту sealed `PetsImportResult`); бросает уже `ImportPetsUseCase` на `Failure`. Тесты ассертят `assertIs<PetsImportResult.Failure>` + конкретный подтип `ImportException`
  - ⚠️ `schema_version > 1` — выбран **fail-fast** (`UnsupportedSchemaVersion`), а не warning: несовместимая будущая схема могла бы исказить данные
  - ➕ robustness (обнаружено): добавлена валидация `invalid birth_date format` + `blank name` → `MalformedData` (иначе `DateTimeParseException`/`IllegalArgumentException` от `LocalDate.parse`/`Pet.init` протекли бы из UseCase)
- [x] verify tests fail — **Red** (заведомая compile-failure: типы `PetsJsonDeserializer`/`PetsImportResult`/`ImportException`/`ImportWarning` не существовали)
- [x] create `PetsJsonDeserializer.decode(input: String): PetsImportResult` (object, pure-Kotlin, `ignoreUnknownKeys=true`) + sealed `PetsImportResult` (Success(entries, warnings)/Failure(error)) в `:core:domain/import_/`
  - создан `ImportException` (sealed: `MalformedData`/`MissingRequiredField(fields)`/`UnknownSpecies(speciesId)`/`UnsupportedSchemaVersion(version, supported)`) — типизирован для маппинга на локализованные сообщения UI (Task 21)
  - создан `ImportWarning` (sealed: `UnknownGender(petName, genderId)`) — неизвестный `gender_id` lenient (пол отброшен к null, запись принята), в отличие от вида (fail-fast). Даёт тестируемое предупреждение
  - десериализатор переиспользует `PetExportEntry`/`PetsExportSchema` из пакета `export` — единый контракт схемы export↔import; порядок проверок: version → декод (MissingField/Malformed) → per-entry (blank name/unknown species/bad date) → gender warning
  - ⚠️ пакет назван `import_` (подчёркивание) — `import` зарезервирован в Kotlin; detekt `PackageNaming` ослаблен (allow `_` в сегментах, см. ➕ ниже)
- [x] verify — **Green** (`PetsJsonDeserializerTest` 13/0/0)
- [x] write FAILING test `ImportPetsUseCaseTest` (10 кейсов): imports+inserts, dry-run preview без вставки, REPLACE clears, MERGE keeps+inserts, REPLACE dry-run не очищает, propagates ImportException (malformed + unknown species), maps entry fields → domain Pet, unknown gender → null + warning
- [x] create `ImportPetsUseCase(petRepository, deserializer = PetsJsonDeserializer)` в `:core:domain/import_/`: `suspend operator fun invoke(content, strategy: ImportStrategy, dryRun=false): ImportSummary`; на `Failure` бросает `error`; конвертирует `PetExportEntry`→`Pet` (id=0L, photoPath=null); REPLACE → `clearAll()` затем insert; MERGE → insert поверх (без дедупа — у импорта нет стабильного ключа); `dryRun` короткозамыкает до любой мутации
  - создан `ImportStrategy { MERGE, REPLACE }` + `ImportSummary(importedCount, warnings, dryRun)` (тип-сводка для confirm-диалога Task 21)
- [x] verify — **Green** (`ImportPetsUseCaseTest` 10/0/0; suite `:core:domain:test` 22 новых теста зелёные)
- [x] add `PetRepository.clearAll()` для REPLACE strategy + test — добавлен в интерфейс + `RoomPetRepository` (+ `PetDao.clearAll()` `@Query("DELETE FROM pets")`, KSP перегенерировал DAO-impl) + 3 fake (`:core:domain`/`:feature:pets`/`:feature:editor`) + `PausedFakePetRepository` + `ThrowingPetRepository` (тест экспорта); добавлены DAO androidTest `clearAllRemovesEveryPet` + `clearAllOnEmptyTableIsNoOp`
- [x] run tests — `:core:domain:test` + `:core:domain:detekt` + `:core:domain:koverVerify` (≥90%) + `:core:database:test`/`compileDebugAndroidTestKotlin`/`detekt` + `:app:compileDebugKotlin` + `:feature:pets:test`/`:feature:editor:test` — всё зелёное
- [x] ➕ detekt (обнаружено): `PackageNaming.packagePattern` ослаблен до `[a-z]+(\.[a-z][A-Za-z0-9_]*)*` (разрешает `_` в сегментах пакета) — идиома для reserved-word пакета `import_`; задокументировано комментарием в `detekt.yml`

### Task 20: Import Pets — CSV deserializer (TDD)
- [x] write FAILING test `PetsCsvDeserializerTest` (11 кейсов): valid CSV with header, quoted commas/newlines round-trip (через `PetsCsvSerializer`), missing required column, unknown species_id (fail-fast), empty optional cells → null, BOM из Excel, empty input, blank name, non-numeric weight_kg, unknown gender_id → warning, trailing blank line ignored
  - ⚠️ «throws» переформулировано (как в Task 19): `decode` НЕ бросает на ошибках данных — возвращает `PetsImportResult.Failure` (Result-контракт sealed `PetsImportResult`); бросает уже `ImportPetsUseCase`. Тесты ассертят `assertIs<PetsImportResult.Failure>` + конкретный подтип `ImportException`
- [x] verify tests fail — **Red** (compile-failure: `PetsCsvDeserializer`/`ImportEntryValidator` не существовали)
- [x] create `PetsCsvDeserializer.decode(csv: String): PetsImportResult` — RFC 4180 parser (object, pure-Kotlin); конечный автомат с состоянием `inQuotes` (запятые/переносы/удвоенные кавычки внутри полей); разделитель записей CRLF **или** одиночный LF (терпим к разным инструментам); снятие ведущего BOM `U+FEFF` (иначе прилипает к первой колонке заголовка); пустые ячейки опциональных колонок → `null`; финальный перенос строки не создаёт пустую запись; non-numeric `weight_kg` → `MalformedData`
- [x] verify — **Green** (`PetsCsvDeserializerTest` 11/0/0)
- [x] update `ImportPetsUseCase` чтобы определять формат по содержимому (`detectFormat`: head после снятия BOM/пробелов начинается с `{`/`[` → JSON, иначе CSV) И принимать явный `format: ExportFormat?` параметр (`null` = авто); диспетчер `when (format ?: detectFormat) { JSON -> jsonDeserializer; CSV -> csvDeserializer }`
- [x] write tests для ImportPetsUseCase с CSV (5 кейсов): auto-detect CSV, auto-detect JSON, explicit CSV maps fields, CSV REPLACE clears, propagates `UnknownSpecies` (suite вырос 9 → 14)
- [x] run tests — `:core:domain:test` + `:core:domain:detekt` + `:core:domain:koverVerify` (≥90%) + `:app:compileDebugKotlin` — всё зелёное
- [x] ➕ refactor (обнаружено, DRY): идентичная пост-валидация записей (пустое имя / неизвестный вид / плохая дата / gender warning) вынесена из `PetsJsonDeserializer` в общий `internal object ImportEntryValidator` — JSON и CSV делят одну протестированную политику вместо двух копий; 13 JSON-тестов подтверждают неизменность поведения
- [x] ➕ refactor (обнаружено): введён `fun interface PetsDeserializer { decode(input): PetsImportResult }`; оба десериализатора реализуют его (Strategy, как `AgeCalculator` в Task 1), `ImportPetsUseCase` хранит обе стратегии и инжектируемо для тестов; конструктор сменил единый `deserializer` на `jsonDeserializer`/`csvDeserializer`
- [x] ➕ detekt (обнаружено): `decode` переписан с 3 → 2 return (`firstOrNull ?: return` + единый `when`) под `ReturnCount` лимит 2; `parseRows` помечен `@Suppress("CyclomaticComplexMethod", "NestedBlockDepth")` — конечный автомат CSV неизбежно ветвист, декомпозиция его только запутала бы

### Task 21: Settings UI — Export/Import buttons + SAF integration
- [x] write FAILING test `SettingsViewModelTest`: покрыты `ExportRequested JSON/CSV emits RequestSaveLocation` (filename+mime), `ExportLocationSelected writes file and emits ExportComplete with pet count`, `ExportLocationSelected emits ExportError when writer fails`, `ImportRequested emits RequestOpenLocation with json+csv mimes`, `ImportLocationSelected imports pets and emits ImportComplete`, `… with REPLACE clears existing then inserts`, `… with unknown species emits ImportError`, `… with malformed content emits ImportError malformed`, `… with unknown gender emits ImportComplete with warning`, `… emits ImportError when reader fails` (13 новых тестов через `Turbine` + `FakeBackupFile*`/`FakePetRepository`)
  - ⚠️ «ImportFailed → ImportError(errorMessageKey)» переформулировано: ошибка приходит не отдельным событием, а из самого `ImportLocationSelected` — `ImportException` (типизированный) маппится приватным `toMessageKey()` на стабильный ключ `SettingsMessages.*`; ViewModel остаётся свободным от Android `R`
- [x] verify tests fail — **Red** (тесты ссылались на несуществующие `SettingsEffect`/`SettingsEvent.Export*`/`Import*`/`BackupFileWriter`/`BackupFileReader` — заведомая compile-failure)
- [x] update `SettingsViewModel` чтобы:
  - инжектировать `ExportPetsUseCase` и `ImportPetsUseCase` (+ `BackupFileWriter`/`BackupFileReader` SAF-порты)
  - добавить новые `SettingsEvent` варианты: `ExportRequested`, `ExportLocationSelected`, `ImportRequested` (data object), `ImportLocationSelected(uri, strategy)`
  - использовать `Channel<SettingsEffect>(BUFFERED)` → `receiveAsFlow()` для one-time effects (SAF-запрос, результат) — отдельно от reactive `state`; `pendingExportFormat` запоминается между двумя фазами SAF
- [x] create `SettingsEffect` sealed interface для one-time UI effects (`RequestSaveLocation`/`RequestOpenLocation`/`ExportComplete`/`ExportError`/`ImportComplete`/`ImportError`) + `object SettingsMessages` со стабильными ключами ошибок
- [x] verify — **Green** (`SettingsViewModelTest` 23/0/0 — 10 Plan-1 + 13 Plan-2)
- [x] update `SettingsScreen` Composable:
  - добавлен раздел «Резервная копия» / «Backup» (`BackupSection` в `ui/section/`) с двумя `ListItem`: «Экспорт…» + «Импорт…»
  - sub-выбор формата через radio `AlertDialog`: JSON / CSV (`ExportFormatDialog`)
  - sub-выбор стратегии import через radio `AlertDialog`: Merge / Replace (`ImportStrategyDialog`)
  - `rememberLauncherForActivityResult(CreateDocument(mime))` для export (отдельные JSON/CSV launcher'ы — `CreateDocument` фиксирует mime на construction) → `ExportLocationSelected`
  - `rememberLauncherForActivityResult(OpenDocument())` для import → `ImportLocationSelected(uri, pendingStrategy)`
  - после export/import — `Snackbar` с локализованным сообщением (`importCompleteMessage` учитывает warnings); ошибки маппятся `backupErrorResId`
- [x] create `SafFileWriter` / `SafFileReader` обёртки в `:app/data/saf/` (через `contentResolver.openOutputStream/openInputStream` на `Dispatchers.IO`) — изолируют ViewModel от Android `Uri`; порты `BackupFileWriter`/`BackupFileReader` (`fun interface`) живут в `:feature:settings/backup/`, связываются `@Binds` в `SafModule` (ports&adapters)
- [x] write Compose UI test `SettingsScreenTest`: `backup_exportRowVisible`, `backup_importRowVisible`, `backup_clickingExportShowsFormatDialog` (+ JSON/CSV опции), `backup_clickingImportShowsStrategyDialog` (+ Merge/Replace опции), `backup_confirmingExportFormatInvokesCallback`, `backup_confirmingImportStrategyInvokesCallback` (через `SettingsContent` stateless + testTag'и)
- [x] verify — **Green** (`compileDebugAndroidTestKotlin` чистый; запуск на эмуляторе — nightly.yml)
- [x] run `./gradlew :feature:settings:test :feature:settings:assembleDebug --no-daemon` — зелёное (также прогнаны `:feature:settings:detekt`, `:feature:settings:koverVerify` ≥80%, `:app:detekt`, `:app:assembleDebug` для проверки Hilt-графа+SAF-проводки, `:feature:settings:compileDebugAndroidTestKotlin`)
- [x] ➕ DI (обнаружено): `DomainModule` дополнен `provideExportPetsUseCase`/`provideImportPetsUseCase`; новый `SafModule` (`:app/data/saf/di/`) с `@Binds` для SAF-портов; `:feature:settings` build.gradle получил `androidx.activity.compose` (Activity Result API)
- [x] ➕ i18n (обнаружено): добавлены строки `settings_section_backup`, `settings_export_*`, `settings_import_*` (заголовки, диалоги, форматы, стратегии, success/error-сообщения) в ru + en (финальная вычитка локализации — Task 22)
- [x] ➕ detekt (обнаружено): `LongParameterList.ignoreAnnotated: [Composable]` — `SettingsContent` принимает state + 7 callback'ов/модификаторов (8 параметров), что легитимная Compose-идиома, а не code smell; обычные функции/конструкторы держим под порогом 7

### Task 22: Локализация — strings для всех новых видов + подкатегорий + стадий
- [x] add стрингов в `:feature:editor/src/main/res/values/strings.xml` (ru, default) для:
  - имена всех 10 новых видов — добавлены `pet_editor_species_{rabbit,hamster,guinea_pig,rat,mouse,ferret,bird,reptile,horse,fish}` (Кролик/Хомяк/Морская свинка/Крыса/Мышь/Хорёк/Птица/Рептилия/Лошадь/Рыба)
  - имена всех подкатегорий — ⚠️ **уже были** добавлены инкрементально в Tasks 15/16 (`rabbit_size_*`, `hamster_type_*`, `bird_type_*`, `reptile_type_*`, `horse_type_*`, `fish_type_*`); Task 22 — финальная вычитка, изменений не потребовалось
- [x] add те же ключи в `values-en/strings.xml` — добавлены 10 `pet_editor_species_*` (Rabbit/Hamster/Guinea pig/…); subcategory-ключи уже были
- [x] add стрингов в `:feature:quickcalc/.../strings.xml` — ⚠️ **уже были** (Task 16 добавил все 12 `quick_calc_species_*` + subcategory `rabbit_size_*`/… + life stages + explanations в ru+en); изменений не потребовалось
- [x] add стрингов для `LifeStage` всех новых видов — ⚠️ **уже были** в `:feature:pets/.../values/strings.xml` (`life_stage_*` добавлялись по виду в Tasks 2–10); ru+en полны
- [x] update `lifeStageLabelRes(lifeStage)` helper чтобы покрывал все новые подтипы LifeStage exhaustive — ⚠️ **уже исчерпывающий** (вынесен в `detail/ui/LifeStageLabels.kt` в Task 9; 12 видов × все стадии, sealed `when` без `else`)
- [x] add стрингов для settings export/import labels — ⚠️ **уже были** (Task 21 добавил `settings_section_backup`, `settings_export_*`, `settings_import_*` в ru+en)
- [x] write FAILING test `LifeStageLabelLocalizationTest` — создан в `:feature:pets/src/test/.../detail/ui/` (Robolectric): `Species.implemented()` × `stagesFor(species)` (исчерпывающий `when`→`LifeStage.*.all()`) — каждая из 58 стадий имеет non-zero resId, resId'ы уникальны (нет copy-paste коллизий), строки непусты в ru+en. ⚠️ против пост-Task-9 кода уже Green (helper исчерпывающий) — характеристический regression-guard
- [x] update `AgePluralFormatter` если нужно — изменений не потребовалось: pure-Kotlin, форматирует только годы, видо-агностичен; месячные возрасты в UI идут через `pluralStringResource`, не через форматтер
- [x] write `SpeciesLocalizationTest` — создан в `:feature:editor/src/test/.../ui/section/` (Robolectric): каждый `Species.implemented()` → собственный non-zero resId имени (уникальность ловит прежний `else -> dog` fallback) + непустые ru/en строки; каждая подкатегория из `subcategoriesFor(species)` → non-zero resId + непустые ru/en. ⚠️ **Red против pre-Task-22 кода**: `speciesLabelRes` возвращал `pet_editor_species_dog` для 10 видов (коллизия) → исправлено на исчерпывающий `when` + `internal`; `subcategoryLabelRes` сделан `internal` для теста
- [x] verify — **Green** (`SpeciesLocalizationTest` 3/3, `LifeStageLabelLocalizationTest` 3/3)
- [x] run `./gradlew :feature:pets:test :feature:editor:test :feature:quickcalc:test :feature:settings:test --no-daemon` — BUILD SUCCESSFUL (также `:feature:editor:detekt` + `:feature:pets:detekt` зелёные)
- [x] ➕ build (обнаружено): Robolectric добавлен в `:feature:editor` и `:feature:pets` build.gradle (`isIncludeAndroidResources=true` + `robolectric`/`junit4`/vintage engine + `includeEngines("junit-jupiter","junit-vintage")`) — зеркалит настройку `:core:designsystem`; локализационные тесты идут в стандартном `:feature:X:test` гейте, а не только в nightly androidTest
- [x] ➕ production-bugfix (обнаружено): прежний `else -> R.string.pet_editor_species_dog` в `SpeciesSelector.speciesLabelRes` молча показывал все 10 новых видов как «Собака» в редакторе; заменён исчерпывающим `when` — теперь компилятор требует ветку на каждый sealed `Species`

### Task 23: Maestro E2E flows для new species + export/import
- [x] create `maestro/quick_calc_rabbit.yaml` — quick calc для кролика, выбор RabbitSize «Средний» + расчёт. ⚠️ эталонное «2 года = 27 ЧГ» НЕ ассертится: QuickCalcBirthDateField по умолчанию ставит «1 год назад» (кролик 1 г = 21 ЧГ), вождение Material DatePicker к точной дате из Maestro хрупко (та же причина, что у `quick_calc_dog.yaml` из Plan 1). Flow проверяет факт расчёта (дескриптор «В человеческих годах») + работу subcategory dropdown; точные значения формулы — в `RabbitAgeCalculatorTest`. Добавлен `scrollUntilVisible` (FlowRow из 12 видов уносит поздние chip'ы/поля под фолд)
- [x] create `maestro/quick_calc_bird.yaml` — quick calc для птицы, выбор BirdType «Корелла» (Cockatiel) + расчёт (аналогичная DatePicker-оговорка)
- [x] create `maestro/quick_calc_horse.yaml` — quick calc для лошади, выбор HorseType «Верховая» (LightHorse) + расчёт (аналогичная DatePicker-оговорка)
- [x] create `maestro/export_import_roundtrip.yaml`:
  - clearState
  - launch app
  - add Dog "Рекс" + Cat "Мурка" + Rabbit "Снежок" through PetEditor (Rabbit добавляется через `scrollUntilVisible "Кролик"`)
  - assertVisible 3 pets in list
  - navigate to Settings («Настройки» IconButton → assertVisible settings title)
  - tap export row (testTag `settings_export_row`) → assertVisible `settings_export_dialog` + опции JSON/CSV → выбор JSON
  - **NOTE (реализовано):** реальное взаимодействие с SAF (системные CreateDocument/OpenDocument) из Maestro недетерминированно (out-of-scope, см. блок «ГРАНИЦА SAF» в YAML). Поэтому кнопка «Продолжить» НЕ нажимается (иначе flow завис бы на системном пикере) — диалог закрывается «Отмена». Фактический файловый round-trip детерминированно покрыт `SettingsViewModelTest` (FakeBackup* порты) + `RoundTripTest` (Task 24)
  - tap import row (testTag `settings_import_row`) → assertVisible `settings_import_dialog` + опции Merge/Replace → выбор Merge → «Отмена»
  - back → assertVisible 3 pets unchanged (диалоги отменены, мутаций нет → данные сохранены)
- [x] update existing `create_first_pet.yaml` и `quick_calc_dog.yaml` — ⚠️ sequence изменился из-за UI расширения: species selector вырос с 2 (Plan 1 Dog/Cat) до 12 видов в `FlowRow` (3–4 ряда chip'ов) → subcategory section + birth date field уехали под фолд. Добавлен `scrollUntilVisible` перед `assertVisible`/`tapOn` для subcategory chip и date-field id в обоих flow (раньше они были на экране при 2 видах); tap'ы по видовым chip'ам (Собака 1-й / Кошка 2-й — первый ряд) не тронуты
- [x] update `.github/workflows/nightly.yml` — функционально изменений не требуется: run-шаг уже глобит весь каталог (`maestro test maestro/`), detect_flows + glob подхватывают новые flow автоматически. Обновлён только header-комментарий (ссылался лишь на Plan 1) — теперь перечисляет Plan 2 flow и поясняет авто-подхват
- [x] run `./gradlew :app:assembleDebugAndroidTest --no-daemon` — BUILD SUCCESSFUL (единственное предупреждение — pre-existing deprecation `createAndroidComposeRule` в `MainNavigationTest.kt`, не связано с Task 23; все 6 flow прошли YAML-валидацию multi-doc)

### Task 24: Final acceptance verification
- [x] verify all 12 species are implemented:
  - `assert Species.values().filter { it.isImplemented }.size == 12` — добавлен `Mvp1SpeciesAcceptanceTest` в `:core:model` (3 теста: ровно 12 implemented; каждый вид implemented; `implemented() == all()`); зелёный
- [x] verify all 12 species have a calculator:
  - `forAll Species: AgeCalculator.forSpecies(it) != null` — добавлен `Mvp1CalculatorAcceptanceTest` в `:core:calculator` (2 теста: каждый вид имеет не-null `AgeCalculator` и `LifeStageCalculator`, привязанный к самому себе через `.species == s`); зелёный
- [x] verify coverage:
  - `:core:calculator:koverVerify` (minBound=95) — **зелёный** (enforce сильнее, чем HtmlReport)
  - `:core:domain:koverVerify` (minBound=90, Export/Import включены) — **зелёный**
  - ⚠️ `:core:database` ≥ 80%: koverVerify структурно недостижим на JVM (DAO/DI/Room-обвязка покрывается instrumented androidTest, который kover JVM не измеряет) — решение зафиксировано в Task 12; реальное покрытие достигается в nightly.yml. Не в JVM-гейте сознательно
- [x] verify lint clean:
  - `./gradlew ktlintCheck` — **зелёный** (исправлены: `argument-list-wrapping` в новом `RoundTripTest` через ktlintFormat; `import-ordering` в `LifeStageCalculatorTest`; `package-name` для пакета `import_` отключён точечно в `.editorconfig` секцией `[**/import_/**.{kt,kts}]` — симметрично detekt-релаксации Task 19, т.к. `import` — reserved word)
  - `./gradlew detekt` — **зелёный** (все модули)
  - ⚠️ `./gradlew lintDebug` — **заблокирован тулчейн-багом AGP** `LintJarApiMigration.migrateClassNames` → `NegativeArraySizeException` при загрузке custom-lint-jar'ов (`lifecycle-lint`/`compose-runtime-lint`) на Kotlin 2.0.21 + AGP 8.7.3 + Compose 1.11.1. Это тот же бинарно-несовместимый тулчейн, что уже задокументирован в `lint.xml` (Task 17, «включить обратно когда Kotlin bump → 2.1.x»); краш происходит на этапе init реестра детекторов (до анализа), поэтому `severity=ignore` его не предотвращает. Не зависит от изменений Task 24. Статический анализ локально закрыт ktlint+detekt; `lintDebug` гоняется в CI-job `android-lint` (lint.yml)
- [x] verify build clean:
  - `./gradlew assembleDebug` — все 12 модулей, **BUILD SUCCESSFUL**
- [x] verify all unit tests pass:
  - `./gradlew testDebugUnitTest` — **BUILD SUCCESSFUL** (весь проект зелёный, включая 3 новых acceptance-теста: Mvp1Species 3/0, Mvp1Calculator 2/0, RoundTrip 2/0)
- [x] verify Maestro E2E (smoke build check):
  - `./gradlew :app:assembleDebugAndroidTest` — **BUILD SUCCESSFUL**, готов к nightly.yml
- [x] verify Export/Import round-trip (через unit tests, не E2E):
  - `RoundTripTest` (новый, `:core:domain`): создаёт 12 pets (по 1 каждого вида, с подкатегориями/полом/весом/заметками с запятыми/кавычками/`\n`) → export → `clearAll` → import → assert содержимое совпадает (id/photoPath нормализованы к 0L/null — не экспортируются by design)
  - аналогичный CSV-кейс — оба теста зелёные (JSON + CSV)
- [x] verify APK size:
  - `./gradlew :app:bundleRelease` (без keystore → unsigned AAB) — собран `app-release.aab`
  - `scripts/verify-bundle-size.sh`: AAB = **9.77 MB** — ✅ в пределах жёсткого лимита §8.5.2 (15 MB, энфорс в release.yml); ⚠️ превышает целевые §7.5 (8 MB) — закономерный рост с 2 до 12 видов (10 калькуляторов, 12 vector-иконок, 96 care-JSON, export/import, kotlinx.serialization). Оптимизация (Baseline Profiles, R8-тюнинг) отложена на Plan 3 (см. «Что НЕ входит в план»)
- [x] update `CHANGELOG.md` — Plan 2 deliverables перенесены в новую секцию `[1.0.0]`; `[Unreleased]` оставлен пустым (его проверяет verify-docs.sh); ссылки compare/tag обновлены. Дата релиза проставляется при тегировании
- [x] update `README.md` — статус → «полный MVP v1.0, все 12 групп животных»; «Why PawClock» расширен пунктом «самый широкий список видов» (USP §1.3) + экзотические формулы + перенос данных export/import
- [x] update `ADR-0006` — изменений нет: дефолт калькуляции для собак (Wang 2020) не менялся в Plan 2 (как и ожидалось)
- [x] verify GitHub Actions workflows валидны:
  - `scripts/verify-workflows.sh` — ✅ 4 workflow найдены, fallback grep-check passed
- [x] verify docs:
  - `bash scripts/verify-docs.sh` — ✅ все обязательные файлы и секции на месте
  - `bash scripts/verify-care-assets.sh` — ✅ All 116 care assets present and well-formed
- [x] verify ADRs:
  - `bash scripts/verify-adrs.sh` — ✅ All 9 ADRs validated (0001–0009 + template + обязательные секции); скрипт обновлён до 9 ADR
- [x] add ➕ ADR-0008 "AgeCalculator sealed interface for species dispatch" в `docs/adr/` — создан (Strategy-pattern, sealed dispatch, SpeciesParams trade-off)
- [x] add ➕ ADR-0009 "Export/Import schema versioning and format strategy" в `docs/adr/` — создан (schema_version=1, fail-fast на forward-incompat, JSON каноничный + CSV interop, исключение id/photoPath)

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
