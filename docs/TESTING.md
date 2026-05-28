# TESTING

Документ описывает test strategy и инструменты PawClock. Расширяет §11 спецификации
(`docs/specs/pawclock-specification.md`) фактическими командами и примерами из текущей
кодовой базы.

## Test pyramid (§11.2)

```
       ┌──────────────────┐
       │ E2E (Maestro)    │       ← 5%   медленно
       ├──────────────────┤
       │  Compose UI      │       ← 15%  средне
       │  Roborazzi       │
       ├──────────────────┤
       │  Integration     │       ← 20%  средне
       │  (Room, DataStore)
       ├──────────────────┤
       │  Unit (JVM)      │       ← 60%  быстро
       │  :core:calculator
       │  :core:domain
       └──────────────────┘
```

Идея пирамиды: чем выше — тем дороже и медленнее тесты, поэтому их меньше.
Чем ниже — тем быстрее и дешевле, поэтому их больше. PawClock закрепляет это
через типы Gradle-плагинов: `:core:calculator` и `:core:domain` — pure-Kotlin
JVM-модули, тесты выполняются за миллисекунды без эмулятора.

## Стек тестирования (§11.3)

| Слой | Инструмент | Где применяется |
|---|---|---|
| Unit (JVM) | JUnit 5 (Jupiter) + kotlin.test | `:core:calculator`, `:core:domain`, ViewModels |
| Параметризованные тесты | `@ParameterizedTest` + `@CsvSource` | DogSize × age табличные кейсы |
| Property-based | Kotest property module | `DogAgeCalculatorPropertyTest`, `LifeStageCalculatorPropertyTest` |
| Mocking | (не используется) | Предпочтительны fakes — `FakePetRepository`, `FakeCareRepository`, `FakeSettingsRepository` |
| Flow testing | Turbine | `SettingsViewModelTest.observe() emits...` |
| Coroutines | `kotlinx-coroutines-test`, `runTest`, `StandardTestDispatcher` | Все async-тесты |
| Room | in-memory database в `androidTest` | `:core:database/androidTest` |
| Compose UI | `createComposeRule()`, `androidx.compose.ui.test` | `:feature:*/androidTest/*ScreenTest.kt`, `:app/androidTest/MainNavigationTest.kt` |
| Screenshot tests | Roborazzi (Robolectric-based, без эмулятора) | `:core:designsystem/test/` (opt-in) |
| E2E flow | Maestro YAML flows | `maestro/create_first_pet.yaml`, `maestro/quick_calc_dog.yaml` |
| Coverage | Kover (Kotlin-нативный, лучше JaCoCo для Compose) | `:core:calculator`, `:core:domain`, `:feature:*` |
| Static analysis | detekt + ktlint + Android Lint | Всё |

## Coverage targets (§11.4)

| Модуль | Coverage target | Способ проверки |
|---|---|---|
| `:core:calculator` | **≥ 95%** (бизнес-критично) | `koverVerify` с `minBound(95)` |
| `:core:domain` | ≥ 90% | `koverVerify` с `minBound(90)` |
| `:core:database` | ≥ 80% (integration tests) | `koverVerify` с `minBound(80)` |
| `:core:datastore` | ≥ 80% | `koverVerify` с `minBound(80)` |
| `:feature:*` ViewModels | ≥ 80% | `koverVerify` с `minBound(80)`, Hilt-generated классы исключены |
| `:feature:*` Composables | ≥ 60% (через screenshot tests, opt-in в Plan 1) | — |
| `:app` | без жёсткой цели | — |

Команды:

```bash
# Все модули с правилами kover
./gradlew :core:calculator:koverVerify :core:domain:koverVerify
./gradlew :core:database:koverVerify :core:datastore:koverVerify
./gradlew :feature:pets:koverVerify :feature:editor:koverVerify
./gradlew :feature:quickcalc:koverVerify :feature:settings:koverVerify

# HTML-отчёт по конкретному модулю
./gradlew :core:calculator:koverHtmlReport
# затем: open core/calculator/build/reports/kover/html/index.html
```

PR с падением coverage ниже порога **блокируется** через `koverVerify`-шаг в
ci.yml workflow.

## Как запускать каждый уровень

### Unit (JVM) — самые частые

```bash
# Конкретный модуль (быстрее всего, доли секунды)
./gradlew :core:calculator:test
./gradlew :core:domain:test

# Все JVM unit-тесты по проекту
./gradlew testDebugUnitTest
```

Эти тесты выполняются без Android-эмулятора, не требуют network/disk access
(кроме чтения тестовых JSON-фикстур из assets через Robolectric, что
изолировано в `:app/test/`).

### Integration (Room, DataStore — `androidTest`)

```bash
# Требует подключённого устройства или работающего эмулятора
./gradlew :core:database:connectedDebugAndroidTest

# Или сборка androidTest APK без запуска (для CI)
./gradlew :core:database:assembleDebugAndroidTest
```

In-memory Room: см. `PetDaoIntegrationTest.kt` — создаёт DB через
`Room.inMemoryDatabaseBuilder()`, проверяет CRUD + Flow эмиссии.

### Compose UI tests

```bash
# Per-feature
./gradlew :feature:pets:connectedDebugAndroidTest
./gradlew :feature:editor:connectedDebugAndroidTest

# Все Compose UI tests
./gradlew connectedDebugAndroidTest
```

Тесты используют `createComposeRule()` + stateless `*Content(state, onEvent)`
composable для подачи произвольного `UiState` без Hilt-setup'а. Пример —
`PetsListScreenTest.kt`:

```kotlin
@Test
fun shows_empty_state_when_no_pets() {
    composeTestRule.setContent {
        PetsListContent(state = PetsListState.Empty, onPetClick = {}, onAddPetClick = {})
    }
    composeTestRule.onNodeWithText("Добавьте первого питомца").assertIsDisplayed()
}
```

### E2E (Maestro)

См. отдельную секцию ниже.

### Screenshot tests (Roborazzi)

Opt-in в Plan 1 (Robolectric SDK не доступен в полностью offline среде):

```bash
# Запись baseline-снимков
./gradlew :core:designsystem:recordRoboImages -Droborazzi.test.record=true

# Verify против baseline
./gradlew :core:designsystem:verifyRoborazziDebug -Droborazzi.test.verify=true
```

Файлы snapshots: `core/designsystem/build/outputs/roborazzi/`.

## TDD-цикл (на примере DogAgeCalculator)

PawClock следует строгому Red → Green → Refactor циклу для бизнес-логики
(см. ADR-0003). Ниже — пошаговое прохождение, как это делалось в Task 6
(`:core:calculator` Wang formula).

### Шаг 1 — Red: пишем падающий тест

Сначала тест, потом класс. Тест компилироваться не должен — класс ещё не
существует. Это **намеренно**: если код компилируется без класса, значит
тест не доказывает, что класс реально написан.

`core/calculator/src/test/kotlin/app/pawclock/calculator/DogAgeCalculatorTest.kt`:

```kotlin
class DogAgeCalculatorTest {

    private val calculator = DogAgeCalculator()

    @Test
    fun `Wang formula returns 31 human years for 1 year old dog`() {
        // 16 · ln(1) + 31 = 31
        val result = calculator.toHumanYears(
            ageInYears = 1.0,
            method = CalculationMethod.EPIGENETIC,
        )
        assertEquals(31.0, result, absoluteTolerance = 0.1)
    }

    @Test
    fun `throws on zero or negative age`() {
        assertFailsWith<IllegalArgumentException> {
            calculator.toHumanYears(ageInYears = 0.0, method = CalculationMethod.EPIGENETIC)
        }
    }
}
```

Запускаем `./gradlew :core:calculator:test` — `compileTestKotlin FAILED` с
"Unresolved reference 'DogAgeCalculator'". **Red.**

### Шаг 2 — Green: минимальная реализация

Минимально необходимый код, чтобы тест прошёл. Без преждевременной оптимизации,
без дополнительных кейсов:

```kotlin
package app.pawclock.calculator

import kotlin.math.ln

class DogAgeCalculator {
    fun toHumanYears(ageInYears: Double, method: CalculationMethod): Double {
        require(ageInYears > 0) { "ageInYears must be positive, got $ageInYears" }
        return 16.0 * ln(ageInYears) + 31.0
    }
}
```

`./gradlew :core:calculator:test` — Green. **Один зелёный тест.**

### Шаг 3 — Параметризовать с табличными значениями

Добавляем известные эталонные значения из источника (Wang T. et al.,
Cell Systems 2020):

```kotlin
@ParameterizedTest
@CsvSource(
    "1.0, 31.0",
    "2.0, 42.1",
    "5.0, 56.7",
    "10.0, 67.8",
    "12.0, 70.7",
)
fun `Wang formula matches published reference values`(age: Double, expected: Double) {
    val result = calculator.toHumanYears(age, CalculationMethod.EPIGENETIC)
    assertEquals(expected, result, absoluteTolerance = 0.2)
}
```

### Шаг 4 — Edge cases

Что если age < 1 (щенок)? Спецификация §4.1 говорит о кусочной интерполяции.
Документируем выбор в KDoc:

```kotlin
@Test
fun `puppy extension is continuous with Wang formula at age 1`() {
    val justBelow = calculator.toHumanYears(0.999, CalculationMethod.EPIGENETIC)
    val justAbove = calculator.toHumanYears(1.001, CalculationMethod.EPIGENETIC)
    assertEquals(justAbove, justBelow, absoluteTolerance = 0.5)
}
```

Реализация: для age < 1 используем `31 * age^0.6` — непрерывна с Wang в age=1,
даёт 0 в age=0, монотонно растёт.

### Шаг 5 — Refactor: извлекаем константы, добавляем KDoc

```kotlin
/**
 * Calculates dog age in human years.
 *
 * Uses one of two methods, switchable per spec §4.1:
 * - [CalculationMethod.EPIGENETIC]: Wang et al. 2020 formula
 *   `humanAge = 16 · ln(dogAge) + 31` for age ≥ 1; for puppies (<1 year)
 *   uses smooth power extension `31 · age^0.6`.
 *   Reference: Wang T. et al., Cell Systems 2020,
 *   DOI: [10.1016/j.cels.2020.06.006](https://doi.org/10.1016/j.cels.2020.06.006).
 * - [CalculationMethod.SIZE_BASED]: AKC/AAHA 2019 size lookup table.
 */
class DogAgeCalculator {
    private companion object {
        const val WANG_COEFFICIENT = 16.0
        const val WANG_OFFSET = 31.0
        const val WANG_PUPPY_EXPONENT = 0.6
    }
    // ...
}
```

### Шаг 6 — Property-based (отдельная задача)

После green'а всех табличных кейсов добавляем property-проверки в
`DogAgeCalculatorPropertyTest`:

```kotlin
@Test
fun `human age is monotonically increasing in dog age`() = runBlocking {
    checkAll(Arb.double(0.01, 30.0), Arb.double(0.01, 30.0)) { age1, age2 ->
        if (age1 < age2) {
            val human1 = calculator.toHumanYears(age1, CalculationMethod.EPIGENETIC)
            val human2 = calculator.toHumanYears(age2, CalculationMethod.EPIGENETIC)
            human1 shouldBeLessThan human2
        }
    }
}
```

### Краткая чек-листа TDD-цикла

1. **Red.** Тест из спецификации с известным эталонным значением — `compileTestKotlin FAILED`.
2. **Green.** Минимальная реализация — один зелёный тест.
3. **Параметризовать.** `@CsvSource` со всеми табличными кейсами.
4. **Edge cases.** Граница 0/1, отрицательные, экстремально большие.
5. **Refactor.** Извлечь константы, добавить KDoc со ссылкой на источник (DOI).
6. **Property-based.** Свойства (монотонность, позитивность, ограниченность).
7. **Coverage check.** `koverVerify` подтверждает ≥ 95% для `:core:calculator`.

Этот цикл повторён для каждой формулы в Tasks 6-11 плана Plan 1 — Wang formula,
AKC/AAHA size table, Dog life stages, AAHA/AAFP cat formula, Cat life stages,
property-based для всех. Каждый калькулятор — один цикл, занимающий ~1-2 часа.

## Локализационные тесты (§11.11)

Plurals для возраста (`R.plurals.age_years`) проверяются через JUnit-тест
`AgePluralFormatterTest` в `:core:domain`:

```kotlin
@ParameterizedTest
@CsvSource(
    "1, год", "2, года", "5, лет",
    "21, год", "22, года", "25, лет",
    "101, год", "111, лет", "121, год",
)
fun `russian plural form matches CLDR rules`(years: Int, expectedSuffix: String) {
    val formatter = AgePluralFormatter()
    assertTrue(formatter.format(years, "ru").endsWith(expectedSuffix))
}
```

Compose-уровень: `PetsListScreenLocalizedTest` подаёт
`CompositionLocalProvider(LocalConfiguration with Locale("ru"))` и проверяет,
что title рендерится по-русски.

## Maestro CLI (E2E flows)

[Maestro](https://maestro.mobile.dev) — YAML-driven framework для end-to-end UI
тестирования Android/iOS приложений. PawClock использует Maestro для smoke-flow'ов
по §11.10 спецификации.

### Установка

Maestro — это **external dev dependency**: ставится локально на машину разработчика,
не идёт в Gradle dependencies. Установка одной командой:

```bash
# macOS / Linux
curl -fsSL "https://get.maestro.mobile.dev" | bash

# затем добавить в PATH:
export PATH="$PATH:$HOME/.maestro/bin"
```

Windows: см. [официальную документацию](https://maestro.mobile.dev/getting-started/installing-maestro/windows).

Проверка установки:

```bash
maestro --version
```

### Структура flow'ов

Maestro flow'ы лежат в каталоге `maestro/` в корне репозитория:

- `maestro/create_first_pet.yaml` — первый запуск, добавление кошки «Барсик»,
  переход на PetDetail.
- `maestro/quick_calc_dog.yaml` — Quick Calculator для собаки, переключение
  метода расчёта (Wang ↔ AKC/AAHA size table).

### Локальный запуск

Перед запуском:

1. Поднять Android-эмулятор (API 30+, рекомендуется image с google_apis).
2. Установить debug-build приложения:

   ```bash
   ./gradlew :app:installDebug
   ```

3. Запустить flow:

   ```bash
   maestro test maestro/create_first_pet.yaml
   ```

Запуск всех flow'ов сразу:

```bash
maestro test maestro/
```

Полезные флаги:

- `--debug-output <dir>` — выгрузить view-hierarchy дамп для упавших шагов.
- `--continuous` — авто-перезапуск при изменении файлов (полезно при написании
  нового flow).

### CI (nightly.yml)

Maestro flow'ы запускаются автоматически в `nightly.yml` workflow на API 30
(см. `.github/workflows/nightly.yml`). Maestro устанавливается inline через
тот же `get.maestro.mobile.dev` скрипт; на CI-runner'е flow'ы исполняются
после `./gradlew :app:installDebug` на эмуляторе через
`reactivecircus/android-emulator-runner@v2`.

### applicationId

В flow'ах используется `appId: app.pawclock.debug` — debug-вариант
(см. `applicationIdSuffix = ".debug"` в `app/build.gradle.kts`).
Для release-build flow'ы нужно дублировать с `appId: app.pawclock` либо
параметризовать через Maestro `--env`.

### Локаль

Flow'ы написаны под русскую локаль (default по §6 спецификации). Для проверки
английской локализации создавать параллельные `*_en.yaml` версии после первого
зелёного запуска (Plan 2).

## Контроль качества тестов (§11.13)

- **Никаких `Thread.sleep` в тестах.** Только `runTest` + `TestDispatcher`.
- **Никаких реальных дат.** `Clock.fixed(...)` или явные `LocalDate.of(...)`.
- **Fakes > Mocks.** `FakePetRepository`, `FakeCareRepository`, `FakeSettingsRepository`
  предпочтительнее MockK. Mock'и используются только когда невозможно сделать
  простой fake (например, проверить отсутствие вызова `LocaleApplier`).
- **Каждый callable-кусок бизнес-логики проверяется через тест.** Если код
  не тестируется — он не нужен, или его нужно рефакторить так, чтобы стал
  тестируемым.
- **TDD-discipline для `:core:calculator` и `:core:domain`** — обязательна
  (ADR-0003). Для UI/feature-модулей TDD — рекомендация, не жёсткое правило.
