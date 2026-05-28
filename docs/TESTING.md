# TESTING

Документ описывает test strategy и инструменты PawClock. Этот файл — заготовка
из Task 23 (Plan 1) с заметкой про Maestro CLI. Полное расширение разделов
test pyramid / coverage / TDD-cycle — Task 24 (тот же plan).

## Test pyramid (краткое summary)

Подробности — `docs/specs/pawclock-specification.md` §11.

- **Unit tests (JVM, JUnit 5 + kotlin.test):** обязательны для каждой задачи.
  Запуск: `./gradlew testDebugUnitTest`.
- **Property-based tests (Kotest):** для математических свойств формул калькуляторов
  (monotonicity, positivity). Лежат в `:core:calculator/src/test/.../*PropertyTest.kt`.
- **Integration tests (androidTest):** Room с in-memory database, DataStore
  с PreferenceDataStoreFactory. Запуск: `./gradlew connectedDebugAndroidTest`.
- **Compose UI tests:** `createComposeRule()` для каждого экрана в
  `:feature:*/src/androidTest/.../*ScreenTest.kt` и `:app/src/androidTest/`.
- **E2E flows (Maestro):** см. секцию ниже.
- **Screenshot tests (Roborazzi):** базовый setup в `:core:designsystem` (opt-in,
  запускается через `-Droborazzi.test.record=true`).

## Coverage targets (§11.4)

- `:core:calculator` ≥ 95% (verified via `koverVerify`).
- `:core:domain` ≥ 90%.
- `:core:database` ≥ 80%.
- `:feature:*` ViewModels ≥ 80%.

Команды:

```
./gradlew :core:calculator:koverHtmlReport :core:calculator:koverVerify
./gradlew :core:domain:koverHtmlReport :core:domain:koverVerify
./gradlew :feature:pets:koverVerify
```

HTML-отчёты: `<module>/build/reports/kover/html/index.html`.

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

Maestro flow'ы запускаются автоматически в nightly.yml workflow на API 30
(см. `.github/workflows/nightly.yml`). Маэстро устанавливается inline через
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

## TDD-цикл (на примере DogAgeCalculator)

Подробное описание — Task 24 (Plan 1) добавит секцию с пошаговым прохождением.
До тех пор референс — Tasks 6-10 в `docs/plans/2026-05-27-pawclock-foundation-and-dog-cat-mvp.md`,
где каждый калькулятор проходит классический Red → Green → Refactor цикл.
