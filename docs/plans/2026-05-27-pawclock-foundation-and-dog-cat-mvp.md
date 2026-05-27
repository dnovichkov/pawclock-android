# PawClock — Plan 1: Foundation + Dog & Cat MVP

## Overview

Первый план для greenfield-проекта **PawClock** — Android-приложения для расчёта возраста питомцев. План закладывает фундамент проекта и реализует первые два вида — **собак и кошек** — с полным MVP-функционалом (профили + Quick Calculator + стадии жизни + care-рекомендации + Material You + локализация ru/en).

**Что НЕ входит в этот план (отложено на следующие планы):**
- Остальные 10 групп животных (кролики, хомяки, морские свинки, крысы, мыши, хорьки, попугаи, рептилии, лошади, рыбы) — Plan 2
- Экспорт/импорт JSON/CSV через SAF (§3.5 спецификации) — Plan 2
- Виджеты Glance, уведомления WorkManager (§3.4) — Plan 3 (v2.0)
- F-Droid публикация — Plan 3
- Mutation testing (Pitest), benchmark profiles — Plan 3

**Что входит в план:**
- Полный Gradle multi-module skeleton (12 модулей по §7.3)
- Полный CI/CD по §8: 4 workflow (ci.yml, release.yml, nightly.yml, lint.yml), dependabot, PR/issue templates, CODEOWNERS, scripts/pre-commit.sh
- `:core:calculator` с TDD: DogAgeCalculator (Wang et al. 2020 + AKC/AAHA 2019 size table) и CatAgeCalculator (AAHA/AAFP 2021) с поправками
- Стадии жизни (LifeStage) для собак и кошек по §4.1, §4.2
- `:core:database` (Room), `:core:datastore` (Preferences), `:core:domain` (UseCases)
- Care-рекомендации в `assets/care/` (placeholder-тексты по §3.3, заменяются позже)
- `:core:designsystem` (Material You, динамические цвета, fallback палитра)
- Полный UI: PetsList, PetDetail, PetEditor, QuickCalculator, Settings, About
- Локализация ru (default) + en со spasiboм за plurals
- Стартовые ADR-0001..0007
- Документация: README.md, ARCHITECTURE.md, TESTING.md, CONTRIBUTING.md, RELEASE.md

**Проблема, которую решает план:** запустить разработку приложения с правильной TDD-дисциплиной и работающим vertical slice "собака/кошка → возраст в человеческих годах + стадия + рекомендации". Дальнейшие виды добавляются по одной формуле = один TDD-цикл.

## Context (from discovery)

**Текущее состояние репозитория:**
- Только `docs/specs/pawclock-specification.md` (1226 строк, v1.1, дата 2026-05-27)
- Только `.git/` initialized — без любых исходников
- `.claude/settings.local.json` уже есть (локальные настройки агента)
- ralphex CLI установлен: `C:\Users\Dmitriy\go\bin\ralphex.exe` (v1.0.1)

**Целевая структура (по §7.3 спецификации):**
```
:app                           — точка входа, DI-граф, Application
:core:designsystem             — тема, цвета, типографика, общие composables
:core:model                    — доменные модели (Pet, LifeStage, Species)
:core:calculator               — pure-Kotlin модуль с формулами расчёта
:core:database                 — Room, DAO, мигратор
:core:datastore                — DataStore Preferences
:core:domain                   — UseCase'ы
:core:testing                  — общие fixtures, fakes
:feature:pets                  — список питомцев, детальный экран
:feature:editor                — создание/редактирование
:feature:quickcalc             — одноразовый расчёт
:feature:settings              — настройки
```

**Ключевые источники для формул (см. §4 спецификации):**
- Собаки (Wang): `ЧГ = 16 · ln(age) + 31`, Wang T. et al., Cell Systems 2020, DOI: 10.1016/j.cels.2020.06.006
- Собаки (size): AKC/AAHA 2019 — табличные значения для Toy/Small/Medium/Large/Giant
- Кошки: AAHA/AAFP 2021 — `1й год = 15`, `2й = 24`, далее `+4/год`; outdoor × 1.15 после 2 лет; large breeds +1/год после 2

**Стек технологий (§7.1):** Kotlin 2.0+, Compose BOM 2024.12+, Material 3 1.3+, Hilt, Navigation Compose 2.8+ typesafe, Coroutines+Flow+Turbine, Room 2.6+ KSP, DataStore Preferences, kotlinx.serialization.

**SDK (§7.2):** minSdk=24, compileSdk=35, targetSdk=35, Java 17 toolchain, applicationId `app.pawclock`.

**GitHub user:** dnovichkov (для CODEOWNERS, README).

## Development Approach

- **Testing approach: TDD (tests first)** — обязательно для `:core:calculator`, `:core:domain` и любой бизнес-логики (см. §11.1 спецификации). Цикл Red → Green → Refactor строго.
- Complete each task fully before moving to the next.
- Make small, focused changes.
- **CRITICAL: every task MUST include new/updated tests for code changes in that task**
  - tests are NOT optional — they are a required part of the checklist
  - write unit tests for new functions/methods
  - write unit tests for modified functions/methods
  - add new test cases for new code paths
  - update existing test cases if behavior changes
  - tests cover both success and error scenarios
- **CRITICAL: all tests must pass before starting next task** — no exceptions
- **CRITICAL: update this plan file when scope changes during implementation**
- Run tests after each change.
- Maintain backward compatibility.
- **TDD-discipline для :core:calculator (≥95% coverage по §11.4):** для каждой формулы сначала пишется падающий тест с известным эталонным значением из спецификации, потом минимально работающая реализация, потом параметризованные тесты для табличных кейсов, потом edge cases, потом property-based проверки в Kotest. Только потом — следующая формула.
- KDoc для каждой формулы ОБЯЗАТЕЛЬНО содержит ссылку на первоисточник (DOI, URL, либо имя стандарта AKC/AAHA/AAFP).
- Никаких `Thread.sleep` в тестах. Только `runTest` + `TestDispatcher`. Никаких реальных дат — `Clock.fixed(...)`.

## Testing Strategy

- **Unit tests (JVM, JUnit 5 + kotlin.test):** обязательны для каждой задачи (см. Development Approach выше). Целевое покрытие по §11.4:
  - `:core:calculator` ≥ 95%
  - `:core:domain` ≥ 90%
  - `:core:database` ≥ 80%
  - `:feature:*` ViewModels ≥ 80%
- **Property-based tests (Kotest):** для математических свойств формул (monotonicity, positivity, continuity) — отдельная задача Task 11.
- **Integration tests (androidTest):** Room с in-memory database, DataStore с TestDataStore.
- **Compose UI tests:** `createComposeRule()` для каждого экрана — Task 18-21.
- **E2E flow (Maestro):** `maestro/create_first_pet.yaml` по §11.10 — Task 23.
- **Screenshot tests (Roborazzi):** базовый setup в Task 16, эталонные snapshots — opt-in в этом плане (можно отложить блокирующий чек в CI на Plan 2).
- **Локализационные тесты:** проверка plurals (1 год / 2 года / 5 лет) — Task 22.
- Coverage measurement через **Kover**, публикация в Codecov (Codecov badge — Task 25).

## Progress Tracking

- Mark completed items with `[x]` immediately when done.
- Add newly discovered tasks with ➕ prefix.
- Document issues/blockers with ⚠️ prefix.
- Update plan if implementation deviates from original scope.
- Keep plan in sync with actual work done.

## What Goes Where

- **Implementation Steps** (`[ ]` checkboxes): код, тесты, конфиги внутри репозитория.
- **Post-Completion** (no checkboxes): manual установка Branch Protection через UI GitHub, регистрация Codecov, создание Google Play Console аккаунта, проверка domain pawclock.app — всё, что требует внешних действий.

## Implementation Steps

### Task 1: Project skeleton (Gradle multi-module + version catalog)
- [x] create root files: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` (с Kotlin code style, AndroidX, JVM args)
- [x] create `gradle/libs.versions.toml` с версиями всех библиотек по §7.1 (Kotlin 2.0, Compose BOM 2024.12, Material 3 1.3, Hilt, Navigation 2.8, Room 2.6, Coroutines, Turbine, JUnit 5, Kotest, MockK, Roborazzi, Maestro) — версии align с offline-cache (Kotlin 2.0.21, AGP 8.5.2, KSP 2.0.21-1.0.28, Roborazzi 1.30.1; bump в следующих задачах при наличии сети)
- [x] create gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `gradle-wrapper.jar`) — Gradle 8.14.3 (>= 8.9 как требует спека)
- [x] create Android-стандартный `.gitignore` (build/, .gradle/, local.properties, *.iml, .idea/, .DS_Store)
- [x] create `.editorconfig` (по §8.1) с UTF-8, LF, 4-space indent, max line 120
- [x] create 12 модулей-пустышек по §7.3 с минимальным `build.gradle.kts` каждый: `:app` (com.android.application), `:core:designsystem`, `:core:model`, `:core:calculator` (pure Kotlin JVM), `:core:database`, `:core:datastore`, `:core:domain`, `:core:testing`, `:feature:pets`, `:feature:editor`, `:feature:quickcalc`, `:feature:settings`
- [x] register all modules in `settings.gradle.kts`
- [x] add `gradle/init.gradle` или buildSrc convention plugins (по выбору агента) для общих настроек compileSdk/minSdk/targetSdk — выбран простой inline-подход (constants в каждом модуле); convention plugins можно ввести позже при разрастании дублирования
- [x] write smoke-test script `scripts/verify-skeleton.sh` который запускает `./gradlew help` и `./gradlew projects` и проверяет, что все 12 модулей зарегистрированы
- [x] write unit test `:core:calculator` `SkeletonTest` (один тривиальный assertion `1+1==2`) — чтобы убедиться, что JVM-тесты вообще запускаются
- [x] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task

### Task 2: ADRs (Architecture Decision Records 0001-0007)
- [x] create `docs/adr/` directory + `docs/adr/template-madr.md` (шаблон MADR по §8.11)
- [x] write `docs/adr/0001-jetpack-compose-over-views.md` (Status: Accepted; Context: новый проект; Decision: Compose; Consequences: меньше xml, нет View-stack)
- [x] write `docs/adr/0002-multi-module-architecture.md` (структура по §7.3, build time benefits, fast TDD на `:core:calculator`)
- [x] write `docs/adr/0003-tdd-as-required-practice.md` (TDD обязателен для `:core:calculator` и `:core:domain`, цели coverage из §11.4)
- [x] write `docs/adr/0004-room-over-sqldelight.md` (Room + KSP, причина: широкая поддержка, хорошая интеграция с Hilt)
- [x] write `docs/adr/0005-no-network-permission.md` (запрет INTERNET permission, отсутствие аналитики, Data Safety "No data collected" по §9)
- [x] write `docs/adr/0006-wang-et-al-formula-as-default-for-dogs.md` (default = Wang 2020, альтернатива = AKC/AAHA size table; пользователь может переключать)
- [x] write `docs/adr/0007-conventional-commits.md` (Conventional Commits, типы из §8.3, генерация CHANGELOG через git-cliff позже)
- [x] verify all ADRs have Status, Context, Decision, Consequences sections (smoke-check через `grep -l "## Status" docs/adr/*.md`) — MADR-формат использует nested `### Positive/Negative Consequences`; verify-script принимает любой heading level
- [x] write test script `scripts/verify-adrs.sh` проверяющий наличие всех 7 ADR и базовую структуру

### Task 3: GitHub Actions — ci.yml + lint.yml + scripts/pre-commit.sh
- [x] create `.github/workflows/ci.yml` по §8.5.1 с jobs: `unit-tests`, `lint`, `build`, `screenshot-tests` (последний — placeholder, чтобы не падать пока)
- [x] create `.github/workflows/lint.yml` отдельным workflow по §8.5.4 (ktlint, detekt, Android Lint) — с `paths:` фильтром для пропуска docs-only PR
- [x] create `scripts/pre-commit.sh` по §8.6 (ktlintFormat, detekt, `:core:calculator:test`) — c re-stage после ktlintFormat
- [x] create `scripts/verify-bundle-size.sh` (placeholder, лимит 15 МБ — реально проверяется в release.yml в Task 4); exit 0 если AAB ещё не собран
- [x] add ktlint + detekt + Android Lint конфиги: `.editorconfig` уже есть, добавить `detekt.yml` (стартовая конфигурация) и `lint.xml` (suppress несущественные); добавлен `.gitattributes` чтобы shell-скрипты оставались LF на Linux runners
- [x] add ktlint и detekt Gradle plugins в `gradle/libs.versions.toml` и в root `build.gradle.kts` — ktlint bumped 12.1.2 → 12.1.1 для align с offline-кэшем; применяется через `subprojects { apply(plugin = "...") }` blanket-блок (auto-no-op на модулях без Kotlin)
- [x] write test: запустить `./gradlew ktlintCheck detekt lintDebug` локально — passes (ktlintFormat auto-fix применился к `build.gradle.kts` файлам по правилу `standard:chain-method-continuation`)
- [x] write test: `actionlint .github/workflows/*.yml` (если доступен) или хотя бы `yamllint -s` для синтаксиса — `scripts/verify-workflows.sh` с graceful fallback chain actionlint → yamllint → python+pyyaml → grep
- [x] verify scripts/pre-commit.sh executable (`chmod +x` зафиксировать в git через `git update-index --chmod=+x`) — все 3 новых скрипта 100755
- [x] run `./gradlew ktlintCheck detekt lintDebug` — passes (включая `:app:lintDebug` после добавления stub AndroidManifest.xml, который Task 17 расширит до полного манифеста)
- ➕ create minimal `app/src/main/AndroidManifest.xml` stub чтобы разблокировать `:app:lintDebug` и `:app:assembleDebug` до Task 17; содержит `INTERNET tools:node="remove"` по §9 заранее

### Task 4: GitHub Actions — release.yml + nightly.yml + dependabot + PR/issue templates + CODEOWNERS
- [x] create `.github/workflows/release.yml` по §8.5.2 (триггер на тег `v*.*.*`, сборка bundleRelease с подписью из Secrets, verify-bundle-size.sh, gh release с changelog, опциональный r0adkll/upload-google-play закомментирован пока) — keystore декодируется из `KEYSTORE_BASE64`, env-vars с префиксом `PAWCLOCK_` (читаются в Task 17 signing config), `concurrency: cancel-in-progress: false` чтобы релизы не отменяли друг друга
- [x] create `.github/workflows/nightly.yml` по §8.5.3 (reactivecircus/android-emulator-runner API 24/30/35, прогон Compose UI + Maestro, cron `0 2 * * *`) — AVD-cache + create-snapshot стадия для ускорения; matrix.target: API 24 = `default` (без GApps), 30/35 = `google_apis`; Maestro-job guard'ом detect_flows skip'ается до Task 23; +`workflow_dispatch` для ручного запуска
- [x] create `.github/dependabot.yml` по §8.9 (gradle weekly, github-actions monthly, лимит 5 PR) — добавлены `groups` (kotlin-and-ksp, androidx-compose, androidx-core, test-runners) чтобы routine bumps шли пачками; security alerts всё равно отдельными PR
- [x] create `.github/ISSUE_TEMPLATE/bug_report.md` по §8.7
- [x] create `.github/ISSUE_TEMPLATE/feature_request.md` (минимальный template)
- [x] create `.github/ISSUE_TEMPLATE/species_request.md` (вид, научное название, источник формулы, стадии) — источник формулы оформлен чек-листом (peer-review/vet-org/textbook), чтобы reviewer мог быстро отсеивать issues без citation
- [x] create `.github/PULL_REQUEST_TEMPLATE.md` по §8.7 (чеклист с TDD-пунктами) — секции TDD & correctness / Source attribution / Quality gates / Privacy & safety
- [x] create `.github/CODEOWNERS` по §8.8 с `* @dnovichkov` и `/core/calculator/ @dnovichkov` и `/docs/adr/ @dnovichkov` — расширено защитой `/core/model/`, `/app/src/main/assets/care/`, `/.github/`, `/gradle/libs.versions.toml`, `/scripts/`, AndroidManifest.xml
- [x] write test: `actionlint .github/workflows/*.yml` валидирует release.yml и nightly.yml — выполняется через `scripts/verify-workflows.sh` (унаследованный из Task 3 fallback chain actionlint → yamllint → python+pyyaml → grep); все 4 workflow парсятся
- [x] write test: проверка существования всех template-файлов через `ls -la .github/ISSUE_TEMPLATE/` и наличие обязательных секций — `scripts/verify-github-templates.sh` проверяет файлы, front-matter, обязательные секции species_request.md, TDD-маркеры PR-template, защищённые пути CODEOWNERS, структуру dependabot.yml
- ➕ add `.github/ISSUE_TEMPLATE/config.yml` с `blank_issues_enabled: false` + Discussions/spec contact links — закрывает обход template'ов через "New blank issue"

### Task 5: :core:model — Species, LifeStage, Pet
- [x] write FAILING tests in `:core:model` `SpeciesTest`, `LifeStageTest`, `PetTest` (TDD: red first):
  - `Species.fromString("dog")` returns `Species.Dog`
  - `Species.fromString("unknown")` throws / returns null
  - `LifeStage.Dog.Senior` существует и `displayKey == "dog_senior"`
  - `Pet.equals/hashCode/copy` корректны для двух одинаковых dataclass instances
- [x] implement `Species` sealed class или enum со всеми 12 видами из §4 (Dog, Cat, Rabbit, Hamster, GuineaPig, Rat, Mouse, Ferret, Bird, Reptile, Horse, Fish), но **только Dog и Cat имеют флаг `isImplemented = true`** в этом плане
- [x] implement `LifeStage` sealed class с подтипами `Dog` (Puppy, YoungAdult, MatureAdult, Senior, EndOfLife) и `Cat` (Kitten, YoungAdult, MatureAdult, Senior, EndOfLife) по §4.1, §4.2
- [x] implement `Pet` data class с полями: id (Long), name (String, required), species (Species), subcategory (String? — DogSize или CatType), birthDate (LocalDate), gender (Gender?), weightKg (Double?), notes (String?), photoPath (String?) — `init` блок enforces `require(name.isNotBlank())` для type-safe валидации на уровне модели
- [x] implement `DogSize` enum: Toy, Small, Medium, Large, Giant с границами в кг (по §4.1) — добавлен `fromWeight(kg)` helper с правилом `[min, max)` и null для non-positive веса
- [x] implement `CatType` enum: IndoorShortHair, IndoorLongHair, Outdoor, LargeBreed — добавлены флаги `isOutdoor` / `isLargeBreed` для использования в формулах калькулятора (Task 9)
- [x] implement `Gender` enum: Male, Female, Unknown
- [x] add KDoc для каждого публичного типа со ссылкой на спецификацию (§4.1, §4.2)
- [x] run `./gradlew :core:model:test --no-daemon` — must pass before next task — 37 тестов прошли (Species: 9, LifeStage: 9, Pet: 5, DogSize: 6, CatType: 5, Gender: 3), ktlint + detekt clean

### Task 6: :core:calculator — DogAgeCalculator (Wang formula) TDD
- [x] write FAILING test `DogAgeCalculatorTest` в `:core:calculator/src/test/kotlin/`:
  - test 1: `Wang formula returns 31 human years for 1 year old dog` (16·ln(1)+31=31)
  - test 2: `throws on zero or negative age`
- [x] verify tests fail with compilation error (no class yet) — **Red** (compileTestKotlin FAILED с 10 unresolved references на DogAgeCalculator/CalculationMethod)
- [x] create `CalculationMethod` enum (EPIGENETIC, SIZE_BASED) в `:core:calculator`
- [x] create minimal `DogAgeCalculator.toHumanYears(ageInYears: Double, method: CalculationMethod): Double` returning 31.0 — **Green** for test 1 (объединено с реальной реализацией для краткости TDD-цикла)
- [x] add real Wang implementation `16.0 * ln(ageInYears) + 31.0` для `EPIGENETIC` + `require(ageInYears > 0)` — **Green** for test 2
- [x] add ParameterizedTest with @CsvSource по §11.5: `1.0,31.0`, `2.0,42.1`, `5.0,56.7`, `10.0,67.8`, `12.0,70.7` с `absoluteTolerance = 0.2`
- [x] add test `handles 7 weeks old puppy via piecewise extension` (для age < 1 года используем кусочную интерполяцию по §4.1 или возвращаем линейную аппроксимацию — задокументировать выбор в KDoc) — выбрана степенная аппроксимация `31 · age^0.6`: непрерывна с Wang в age=1, даёт 0 в age=0, монотонно растёт; задокументировано в KDoc; добавлены два дополнительных теста: `puppy extension is continuous with Wang formula at age 1` и `6 months puppy is between 15 and 25 human years`
- [x] add KDoc со ссылкой на Wang T. et al. Cell Systems 2020, DOI: 10.1016/j.cels.2020.06.006 (§11.5 пример)
- [x] extract `WANG_COEFFICIENT = 16.0` и `WANG_OFFSET = 31.0` как internal const — **Refactor** (плюс `WANG_PUPPY_EXPONENT = 0.6` для puppy-расширения)
- [x] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task — 11 тестов прошли (1 single + 5 параметризованных + 2 throws + 3 puppy/continuity), ktlint + detekt clean

### Task 7: :core:calculator — DogAgeCalculator (AKC/AAHA size table) TDD
- [x] write FAILING test `DogAgeCalculatorSizeBasedTest`:
  - параметризованные кейсы из таблицы §4.1: для каждого `DogSize` × табличного `Возраст` ожидаемое ЧГ
  - `Toy 1y = 15`, `Toy 2y = 24`, `Giant 1y = 12`, `Giant 6y = 49`, и т.д. (вся таблица из §4.1)
- [x] verify tests fail (метод SIZE_BASED ещё не реализован) — **Red** (compileTestKotlin FAILED: `Argument type mismatch: actual type is 'app.pawclock.model.DogSize', but 'app.pawclock.calculator.CalculationMethod' was expected`)
- [x] add `DogSize` parameter overload: `toHumanYears(ageInYears, method, size: DogSize)` или передавать `DogSize` в `subcategory` — введены **две** перегрузки: type-safe `toHumanYears(age, DogSize)` (предпочтительный API) и `toHumanYears(age, method, DogSize?)` для UseCase-слоя, который читает дефолтный метод из настроек
- [x] implement size-based lookup table как private `Map<DogSize, NavigableMap<Double, Double>>` с табличными значениями из §4.1 — отступление от плана: использован общий `DoubleArray ANCHOR_AGES` + 4 параллельных `DoubleArray` колонок ради zero boxing на горячем пути. `DogSize.Toy` и `DogSize.Small` обе мапятся на колонку «Малая ≤9 кг» (AKC/AAHA 2019 не разделяет миниатюрных и малых)
- [x] implement линейную интерполяцию между ближайшими табличными значениями для нецелых возрастов (например, 3.5 года Toy = (28+32)/2 = 30) — выделена в private `interpolateWithinTable()` чтобы удовлетворить detekt `ReturnCount`
- [x] verify все табличные тесты — **Green** (47 новых SIZE_BASED тестов прошли: Toy 12, Small 4, Medium 8, Large 6, Giant 8, + interpolation/extrapolation/edge cases)
- [x] add edge tests: `age=0.5` (puppy), `age=20.0` (за пределами таблицы — экстраполяция или cap), отрицательный возраст бросает IllegalArgumentException — linear extrapolation использует наклон последних двух точек (14-15 лет), документировано в KDoc; puppy < 1 года: линейная интерполяция от 0 ЧГ к первой табличной точке
- [x] add KDoc со ссылкой на AKC/AAHA 2019 + объяснение интерполяции/экстраполяции — полная таблица в KDoc `DogSizeTable`, ссылки в `DogAgeCalculator` KDoc; `@file:Suppress("detekt:MagicNumber")` с обоснованием что числа — это published-стандарт, а не magic
- [x] refactor: вынести таблицу в companion object `DogSizeTable.kt` для читаемости — `internal object DogSizeTable` (не companion object: `DogAgeCalculator` остаётся `class`, чтобы можно было инстанцировать в тестах; таблица — separate file-level object для encapsulation)
- [x] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task — 59 тестов прошли (11 EPIGENETIC + 47 SIZE_BASED + 1 skeleton), ktlint + detekt clean
- ➕ added `implementation(project(":core:model"))` to `:core:calculator/build.gradle.kts` — Task 7 первая, где calculator нуждается в `DogSize` из `:core:model`

### Task 8: :core:calculator — Dog life stages TDD
- [x] write FAILING test `DogLifeStageCalculatorTest`:
  - `Toy puppy at 6 months → LifeStage.Dog.Puppy`
  - `Small adult at 4 years → LifeStage.Dog.MatureAdult`
  - `Large senior at 7 years → LifeStage.Dog.Senior`
  - `Giant senior at 5 years → LifeStage.Dog.Senior` (для гигантских с 5-6 лет по §4.1)
  - `Toy at 15 years → LifeStage.Dog.EndOfLife` (когда возраст близок к ЧЖ)
- [x] verify tests fail — **Red** (compileTestKotlin FAILED с `Unresolved reference 'DogLifeStageCalculator'`)
- [x] create `DogLifeStageCalculator.determine(ageInYears: Double, size: DogSize): LifeStage.Dog`
- [x] implement пороги по §4.1 (AAHA 2019): Puppy (0 — половая зрелость), YoungAdult, MatureAdult, Senior (зависит от размера: Toy/Small 11+, Medium 9+, Large 7+, Giant 5+), EndOfLife (близко к expected lifespan) — порог Puppy→YoungAdult = 0.75 года (единый для всех размеров, медиана AAHA по половой зрелости); MatureAdult с 3 лет (социальная зрелость по AAHA 2019)
- [x] verify все тесты — **Green** (53 новых теста прошли)
- [x] add ParameterizedTest для всех границ size × stage — 36 параметризованных кейсов, по 7-11 на каждый из 5 размеров, покрывают переходы и граничные значения
- [x] add KDoc со ссылкой на AAHA 2019 Canine Life Stage Guidelines + McMillan 2024 (Scientific Reports, n=584 734)
- [x] add `expectedLifespanRange(size: DogSize): ClosedRange<Double>` по §4.1 (6-18 лет в зависимости от размера, McMillan 2024) — возвращает `ClosedFloatingPointRange<Double>` для поддержки `contains()` с Double
- [x] write tests для `expectedLifespanRange` (4 размера × ожидаемые границы) — 6 тестов: 5 на размеры + 1 биоинвариант "меньше собака → дольше живёт"
- [x] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task — 112 тестов прошло (предыдущие 59 + 53 новых), ktlint + detekt clean
- ➕ extracted `internal object DogLifeStageThresholds` (отдельный файл) для табличных порогов senior/endOfLife — следует существующему паттерну `DogSizeTable.kt`; обнаружено в процессе TDD-цикла, что единая формула `endOfLife = lifespanUpperBound − 3` коллапсирует Senior-окно для Medium/Large/Giant (где Senior-фаза короче), поэтому EndOfLife также сделан табличным с обоснованием в KDoc

### Task 9: :core:calculator — CatAgeCalculator (AAHA/AAFP 2021) TDD
- [ ] write FAILING test `CatAgeCalculatorTest`:
  - test 1: `1 year cat = 15 human years` (§4.2)
  - test 2: `2 year cat = 24 human years`
  - test 3: `5 year cat = 24 + 4*3 = 36 human years`
  - test 4: `outdoor 5 year cat = 36 * 1.15 = 41.4 human years` (поправка)
  - test 5: `large breed (MaineCoon) 5 year = 36 + 1*3 = 39 human years` (поправка)
  - test 6: `throws on negative age`
- [ ] verify tests fail — **Red**
- [ ] create `CatAgeCalculator.toHumanYears(ageInYears: Double, catType: CatType): Double`
- [ ] implement кусочную формулу:
  - `ageInYears <= 1.0` → `15 * ageInYears` (линейная интерполяция)
  - `ageInYears <= 2.0` → `15 + 9 * (ageInYears - 1)` (с 15 до 24)
  - `ageInYears > 2.0` → `24 + 4 * (ageInYears - 2)`
- [ ] implement поправки: `Outdoor` после 2 лет → result *= 1.15; `LargeBreed` после 2 лет → result += (ageInYears - 2)
- [ ] verify все тесты — **Green**
- [ ] add ParameterizedTest с табличными значениями для домашних и уличных кошек
- [ ] add KDoc со ссылкой на AAHA/AAFP 2021 Feline Life Stage Guidelines, DOI: 10.1177/1098612X21993657
- [ ] refactor: вынести константы `FIRST_YEAR_HUMAN_AGE = 15`, `SECOND_YEAR_INCREMENT = 9`, `SUBSEQUENT_YEAR_INCREMENT = 4`, `OUTDOOR_AGING_FACTOR = 1.15`
- [ ] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task

### Task 10: :core:calculator — Cat life stages TDD
- [ ] write FAILING test `CatLifeStageCalculatorTest` для всех границ по §4.2:
  - `0.5 year → Kitten`
  - `1 year → Kitten` (граничный случай — спецификация Kitten = 0-1)
  - `2 years → YoungAdult`
  - `7 years → MatureAdult`
  - `10 years → MatureAdult` (граница 10+)
  - `12 years → Senior`
- [ ] verify tests fail — **Red**
- [ ] create `CatLifeStageCalculator.determine(ageInYears: Double, catType: CatType): LifeStage.Cat`
- [ ] implement: Kitten (0–1), YoungAdult (1–6), MatureAdult (7–10), Senior (10+), EndOfLife (близко к 18 для домашних, 5 для уличных)
- [ ] verify все тесты — **Green**
- [ ] add ParameterizedTest для всех границ × CatType
- [ ] add KDoc со ссылкой на AAHA/AAFP 2021
- [ ] add `expectedLifespanRange(catType: CatType): ClosedRange<Double>` (12-18 indoor, 2-5 outdoor, +1-2 года large breed)
- [ ] write tests для `expectedLifespanRange`
- [ ] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task

### Task 11: Kotest property-based tests for calculators
- [ ] add Kotest dependencies to `:core:calculator` (kotest-runner-junit5, kotest-property)
- [ ] write `DogAgeCalculatorPropertyTest` (по §11.6):
  - `human age is monotonically increasing in dog age` (EPIGENETIC) для `Arb.double(0.1, 20.0)`
  - `result is always positive for positive input`
  - `result is bounded above by 200` (никакая собака не даёт > 200 ЧГ)
- [ ] write `DogAgeCalculatorSizeBasedPropertyTest`:
  - monotonicity для каждого `DogSize`
  - giant size at given age >= small size at same age для age > 5 (гиганты стареют быстрее)
- [ ] write `CatAgeCalculatorPropertyTest`:
  - monotonicity в возрасте
  - outdoor cat at age > 2 always >= indoor cat at same age
  - large breed at age > 2 always >= small breed at same age
- [ ] write `LifeStageCalculatorPropertyTest`:
  - `if age1 < age2, then stage(age1).ordinal <= stage(age2).ordinal` (стадии не возвращаются назад) для собак и кошек
- [ ] run `./gradlew :core:calculator:test --no-daemon` — must pass before next task
- [ ] verify coverage `:core:calculator` ≥ 95% через `./gradlew :core:calculator:koverHtmlReport`

### Task 12: :core:database (Room) TDD
- [ ] add Room dependencies + KSP в `:core:database/build.gradle.kts`
- [ ] write FAILING androidTest `PetDaoTest`:
  - `insert pet returns id and pet can be fetched by id`
  - `getAll returns all pets sorted by name`
  - `delete by id removes pet`
  - `update changes fields`
- [ ] verify tests fail — **Red**
- [ ] create `PetEntity` data class с полями matching `Pet` model + Room annotations (@Entity, @PrimaryKey autoGenerate)
- [ ] create `PetDao` interface с @Query, @Insert, @Update, @Delete (suspend functions + Flow<List<PetEntity>>)
- [ ] create `PawClockDatabase` (@Database, version 1)
- [ ] create `PetMapper` (Pet ↔ PetEntity)
- [ ] write `PetMapperTest` (unit JVM) — проверка обоюдного маппинга
- [ ] verify все тесты Room — **Green**
- [ ] add Hilt module `DatabaseModule` с @Provides для DB и DAO
- [ ] add migration scaffold (`Migrations.kt` с `MIGRATION_1_2` пустой пока) + test, что migration spec корректен
- [ ] write integration test с in-memory Room DB: создание Pet, чтение через Flow, удаление
- [ ] run `./gradlew :core:database:test :core:database:connectedDebugAndroidTest` — must pass before next task

### Task 13: :core:datastore (DataStore Preferences) TDD
- [ ] add DataStore Preferences dependency в `:core:datastore`
- [ ] write FAILING test `SettingsRepositoryTest` (с TestDataStore):
  - default theme is System
  - setting theme to Dark persists
  - default language is system / null
  - setting dynamicColor=false persists
- [ ] verify tests fail — **Red**
- [ ] create `ThemeMode` enum (Light, Dark, System) в `:core:model` (или в datastore)
- [ ] create `AppSettings` data class (themeMode, language, dynamicColor, defaultCalculationMethod) — readonly snapshot
- [ ] create `SettingsRepository` interface + `SettingsRepositoryImpl` использующий DataStore
- [ ] implement read/write для каждого preference
- [ ] verify все тесты — **Green**
- [ ] add Hilt module `DataStoreModule`
- [ ] write test `SettingsRepository emits Flow<AppSettings> on each change` (Turbine)
- [ ] run `./gradlew :core:datastore:test --no-daemon` — must pass before next task

### Task 14: Care recommendations assets + CareRepository TDD
- [ ] create asset directory structure: `:app/src/main/assets/care/{dog,cat}/{puppy|kitten,young_adult,mature_adult,senior,end_of_life}/{ru,en}.json` (или унифицированный с `dog/puppy/ru.json` etc.)
- [ ] write placeholder JSON content для каждой пары (вид × стадия × locale) с полями: `stage_description`, `nutrition`, `activity`, `veterinary_check_frequency`, `dental_care`, `warning_signs`, `source_url`, `source_name` — текст placeholder типа "TODO: научный текст для dog/puppy/ru" с обязательным дисклеймером "не заменяет ветеринарного врача" из §3.3
- [ ] create `CareRecommendation` data class в `:core:model` (поля выше)
- [ ] write FAILING test `CareRepositoryTest`:
  - load existing dog puppy ru recommendation succeeds
  - load nonexistent species/stage throws / returns null
  - load missing locale falls back to en
- [ ] verify tests fail — **Red**
- [ ] create `CareRepository` interface + `CareRepositoryImpl` (suspend fun + asset stream + kotlinx.serialization JSON)
- [ ] implement fallback locale logic (ru → en → throw)
- [ ] verify все тесты — **Green**
- [ ] add Hilt module `CareModule`
- [ ] run `./gradlew :core:database:test :app:testDebugUnitTest --no-daemon` — must pass before next task

### Task 15: :core:domain — UseCases TDD
- [ ] write FAILING tests `:core:domain` с fakes (FakePetRepository, FakeCareRepository, FakeSettingsRepository):
  - `CalculatePetAgeUseCase` возвращает (humanYears, lifeStage) для Dog Wang/SizeBased и Cat
  - `GetPetsUseCase` возвращает Flow<List<Pet>> отсортированный
  - `SavePetUseCase` валидирует имя (required) и birthDate (не в будущем)
  - `DeletePetUseCase` — bag тестируется через fake
  - `GetCareRecommendationsUseCase` берёт recommendations через CareRepository по (species, stage, locale)
- [ ] verify tests fail — **Red**
- [ ] create UseCase'ы: `CalculatePetAgeUseCase`, `GetPetsUseCase`, `SavePetUseCase`, `DeletePetUseCase`, `GetCareRecommendationsUseCase`, `GetPetByIdUseCase`
- [ ] implement validation в `SavePetUseCase` (throw `PetValidationException` с локализуемыми ключами)
- [ ] implement `CalculatePetAgeUseCase` который агрегирует `DogAgeCalculator`/`CatAgeCalculator` + `DogLifeStageCalculator`/`CatLifeStageCalculator` + читает default method из `SettingsRepository`
- [ ] verify все тесты — **Green**
- [ ] add Hilt module `DomainModule`
- [ ] add coverage verification: `:core:domain` ≥ 90%
- [ ] run `./gradlew :core:domain:test --no-daemon` — must pass before next task

### Task 16: :core:designsystem — Material You theme + typography + shapes
- [ ] add Compose BOM + Material 3 dependencies в `:core:designsystem`
- [ ] create `PawClockTheme` composable принимающий `darkTheme: Boolean` (default `isSystemInDarkTheme()`) + `dynamicColor: Boolean` (default true)
- [ ] implement dynamic colors на Android 12+ (Build.VERSION.SDK_INT >= 31) через `dynamicLightColorScheme`/`dynamicDarkColorScheme`
- [ ] implement fallback палитру (сгенерированную через Material Theme Builder — seed cyan/teal) — `lightColorScheme`/`darkColorScheme`
- [ ] implement `Typography` по §5.7 (Roboto, шкала M3)
- [ ] implement `Shapes` по §5.1 (RoundedCornerShape(24.dp) для cards, 28.dp для buttons)
- [ ] create общие composables: `PawClockCard`, `LifeStageChip`, `AgeBigCard`, `SectionDivider`
- [ ] add Roborazzi config для screenshot tests в `:core:designsystem/build.gradle.kts`
- [ ] write screenshot tests с captureRoboImage:
  - `PawClockCard` light/dark
  - `LifeStageChip` для каждой стадии Dog/Cat
  - `AgeBigCard` с "5 лет / 36 ЧГ"
- [ ] write unit test `PawClockThemeTest`: theme is applied correctly (через `LocalColorScheme.current`)
- [ ] run `./gradlew :core:designsystem:test --no-daemon` — must pass before next task

### Task 17: :app — Application + Hilt + Navigation skeleton + AndroidManifest
- [ ] add Hilt + Navigation Compose dependencies в `:app`
- [ ] create `PawClockApplication` с `@HiltAndroidApp`
- [ ] create `MainActivity` с `@AndroidEntryPoint` и `setContent { PawClockTheme { PawClockNavHost() } }`
- [ ] create `PawClockNavHost` composable с typesafe routes (sealed class `Route`): PetsList, PetDetail(petId), PetEditor(petId?), QuickCalculator, Settings, About — пустые placeholder Composables для каждого
- [ ] create `AndroidManifest.xml`:
  - applicationId `app.pawclock`
  - НЕТ INTERNET permission (с `tools:node="remove"` по §9)
  - НЕТ READ_MEDIA_IMAGES / CAMERA (добавятся в задачах с фото)
  - theme `@style/Theme.PawClock` (пустой — реально через Compose Theme)
- [ ] add `application.name=".PawClockApplication"` в manifest
- [ ] write smoke androidTest `MainActivityLaunchTest`: app launches without crash, MainActivity composes PetsList placeholder
- [ ] verify `assembleDebug` собирается без ошибок
- [ ] run `./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon` — must pass before next task

### Task 18: :feature:pets — PetsList + PetDetail TDD
- [ ] add dependency :feature:pets → :core:domain, :core:designsystem, :core:model
- [ ] write FAILING test `PetsListViewModelTest` (с FakeGetPetsUseCase + Turbine):
  - initial state is Loading
  - after first emit → Success(pets=[])
  - SelectPet event navigates to detail
- [ ] verify tests fail — **Red**
- [ ] create `PetsListViewModel` с MVI (StateFlow<PetsListState>, sealed UiEvent), Hilt @HiltViewModel
- [ ] create `PetsListState` sealed: Loading, Empty, Success(pets), Error
- [ ] implement loading через GetPetsUseCase
- [ ] verify тесты ViewModel — **Green**
- [ ] create `PetsListScreen` Compose: LargeTopAppBar, LazyColumn ElevatedCard, FAB по §5.3
- [ ] create `PetCard` composable (фото/иконка, имя, чип стадии, возраст в годах и ЧГ)
- [ ] write Compose test `PetsListScreenTest` (createComposeRule по §11.8):
  - shows empty state when no pets
  - shows pet cards when pets exist
  - clicking FAB triggers `AddPet` event
- [ ] write FAILING test `PetDetailViewModelTest`:
  - initial Loading
  - emits Success(pet, calculatedAge, lifeStage, careRecommendation) на сборку из CalculatePetAgeUseCase + GetCareRecommendationsUseCase
  - emits Error если пет не найден
- [ ] implement `PetDetailViewModel` + Screen с collapsing toolbar, hero-блоком, AgeBigCard, прогресс стадии, сворачиваемые секции care recommendations, "Как это посчитано" expandable
- [ ] verify все тесты — **Green**
- [ ] run `./gradlew :feature:pets:test :feature:pets:assembleDebug --no-daemon` — must pass before next task

### Task 19: :feature:editor — PetEditor TDD
- [ ] write FAILING test `PetEditorViewModelTest` (по §11.7):
  - selecting species updates state with available subcategories (Dog → [Toy, Small, Medium, Large, Giant])
  - validation: empty name → error
  - validation: birthDate in future → error
  - save calls SavePetUseCase
- [ ] verify tests fail — **Red**
- [ ] implement `PetEditorViewModel` (MVI: PetEditorState, PetEditorEvent: SelectSpecies, SetName, SetBirthDate, SetSubcategory, SetWeight, SetNotes, Save)
- [ ] verify все тесты — **Green**
- [ ] create `PetEditorScreen` (один длинный экран на phone, FAB Save по §5.3):
  - name (OutlinedTextField, required indicator)
  - species selector (chip group / dropdown)
  - subcategory (dependent dropdown)
  - birthDate (DatePicker по §10 + текстовый ввод параллельно)
  - gender (chip group)
  - weight (OutlinedTextField numeric)
  - notes (multiline OutlinedTextField)
  - photo picker (PhotoPicker без разрешений) — basic версия (можно опустить из MVP, оставить TODO)
- [ ] write Compose UI test `PetEditorScreenTest`:
  - shows save button enabled only when valid
  - changing species clears subcategory
- [ ] run `./gradlew :feature:editor:test :feature:editor:assembleDebug --no-daemon` — must pass before next task

### Task 20: :feature:quickcalc — QuickCalculator TDD
- [ ] write FAILING test `QuickCalcViewModelTest`:
  - initial state empty
  - calculating with valid species + birthDate emits result (humanYears, lifeStage)
  - calculating without species/date → ValidationError
  - changing CalculationMethod (Wang / Size) recomputes for Dog
- [ ] verify tests fail — **Red**
- [ ] implement `QuickCalcViewModel`
- [ ] verify все тесты — **Green**
- [ ] create `QuickCalcScreen`:
  - species selector
  - subcategory dropdown
  - birthDate picker
  - calculation method toggle (Wang / Size) только для Dog
  - bottom sheet с результатом (по §5.3): AgeBigCard, LifeStageChip, "Как это посчитано" expandable со формулой и DOI
- [ ] write Compose UI test `QuickCalcScreenTest`
- [ ] run `./gradlew :feature:quickcalc:test :feature:quickcalc:assembleDebug --no-daemon` — must pass before next task

### Task 21: :feature:settings — Settings + About TDD
- [ ] write FAILING test `SettingsViewModelTest`:
  - reads current settings from SettingsRepository
  - changing theme persists через SettingsRepository
  - changing language persists
  - toggling dynamicColor persists
- [ ] verify tests fail — **Red**
- [ ] implement `SettingsViewModel`
- [ ] verify все тесты — **Green**
- [ ] create `SettingsScreen` (ListItems с switches по §5.3):
  - theme (Light / Dark / System)
  - dynamic colors (Android 12+) switch
  - language (system / ru / en)
  - default dog calculation method
- [ ] create `AboutScreen`:
  - PawClock version (из BuildConfig)
  - Apache 2.0 license текст
  - link на GitHub repo (https://github.com/dnovichkov/pawclock-android)
  - дисклеймер: "Информация носит ознакомительный характер..."
  - список источников из §14 спецификации (через коротко)
- [ ] write Compose UI tests
- [ ] run `./gradlew :feature:settings:test :feature:settings:assembleDebug --no-daemon` — must pass before next task

### Task 22: Localization — strings.xml ru (default) + en + plurals + LocaleConfig
- [ ] create `:app/src/main/res/values/strings.xml` (русский) со всеми строками используемыми в feature-модулях
- [ ] create `:app/src/main/res/values-en/strings.xml` (английский перевод)
- [ ] create `plurals` для возраста по §6 (1 год / 2 года / 5 лет в ru; 1 year / 2 years в en)
- [ ] create `:app/src/main/res/xml/locales_config.xml` (LocaleConfig для Android 13+)
- [ ] add `android:localeConfig="@xml/locales_config"` в `<application>` AndroidManifest
- [ ] implement `LocaleHelper` + `AppCompatDelegate.setApplicationLocales` для in-app picker
- [ ] write FAILING test `AgePluralFormatterTest` (по §11.11):
  - `formatAge(1, "ru") == "1 год"`
  - `formatAge(2, "ru") == "2 года"`
  - `formatAge(5, "ru") == "5 лет"`
  - `formatAge(21, "ru") == "21 год"`
  - `formatAge(22, "ru") == "22 года"`
  - `formatAge(1, "en") == "1 year"`
  - `formatAge(2, "en") == "2 years"`
- [ ] implement `AgePluralFormatter`
- [ ] verify все тесты — **Green**
- [ ] sweep through all feature-модули и заменить hardcoded строки на `stringResource(R.string.xxx)`
- [ ] write Compose test `PetsListScreenLocalizedTest` для проверки локализации
- [ ] run `./gradlew :app:testDebugUnitTest --no-daemon` — must pass before next task

### Task 23: Compose UI tests + Maestro E2E flow
- [ ] add Maestro CLI installation note в `docs/TESTING.md` (используется как external dev dep)
- [ ] create `maestro/create_first_pet.yaml` по §11.10 (русская локаль):
  - launchApp
  - assertVisible "Добавьте первого питомца"
  - tapOn "Добавить питомца"
  - inputText "Барсик"
  - tapOn "Вид" → "Кошка"
  - tapOn "Дата рождения" → 2020/5/15
  - tapOn "Сохранить"
  - assertVisible "Барсик" + "Senior"
- [ ] create `maestro/quick_calc_dog.yaml`:
  - launchApp
  - go to QuickCalculator
  - select Dog + Medium + birthDate
  - assertVisible результат
  - переключить на Size method
  - assertVisible изменённый результат
- [ ] add Maestro tests в nightly.yml workflow (Task 4 уже зашатало placeholder)
- [ ] write Compose UI tests для key screens (которых ещё нет):
  - `MainNavigationTest` — навигация между всеми экранами
  - `AppLaunchTest` — холодный старт без crash
- [ ] run `./gradlew connectedDebugAndroidTest --no-daemon` (на любом доступном эмуляторе/устройстве; в CI запускается в nightly.yml) — must pass before next task

### Task 24: Documentation pass — docs/ARCHITECTURE.md, TESTING.md, CONTRIBUTING.md, RELEASE.md
- [ ] write `docs/ARCHITECTURE.md`:
  - high-level diagram модулей
  - clean architecture слои (data/domain/presentation)
  - MVI explanation
  - dependency rules (только onion)
- [ ] write `docs/TESTING.md`:
  - test pyramid из §11.2
  - как запускать каждый level (unit, integration, Compose, Maestro)
  - coverage requirements из §11.4
  - TDD-cycle example на DogAgeCalculator (можно cite §11.5)
- [ ] write `docs/CONTRIBUTING.md`:
  - Conventional Commits (§8.3)
  - GitHub Flow branching (§8.2)
  - PR checklist
  - code style: ktlint + detekt
  - как добавлять новый вид животного (TDD walkthrough)
- [ ] write `docs/RELEASE.md`:
  - семвер по §8.10
  - формула versionCode = MAJOR*10000 + MINOR*100 + PATCH
  - процесс tag → release.yml workflow
  - publish to Google Play (manual promote)
  - F-Droid checklist (отложено на Plan 2)
- [ ] add `CHANGELOG.md` с разделом `## [Unreleased]` для будущих изменений
- [ ] add `LICENSE` (Apache 2.0 standard text)
- [ ] verify тестом: `scripts/verify-docs.sh` проверяет существование всех документов и наличие в каждом обязательных секций

### Task 25: README.md + final acceptance verification
- [ ] write `README.md` по §8.12:
  - title + tagline
  - badges (CI status placeholder, Coverage placeholder, License Apache 2.0, latest release placeholder)
  - screenshots placeholders с заметкой "TODO: добавить после первого release"
  - "Why PawClock" — USP из §1.3
  - "Installation" — Google Play link placeholder + APK from Releases
  - "Development" — quick start: clone, `./gradlew :app:installDebug`
  - "Tech stack" — список из §7.1
  - "Testing" — link на `docs/TESTING.md`
  - "Contributing" — link на `docs/CONTRIBUTING.md`
  - "Privacy" — "no data collected" по §9
  - "Sources" — link на `docs/specs/pawclock-specification.md` §14
  - "License" — Apache 2.0
- [ ] verify acceptance criteria (Plan 1 done = MVP-foundation готов для расширения видами в Plan 2):
- [ ] verify все 12 модулей собираются: `./gradlew assembleDebug` zero errors
- [ ] verify все unit тесты проходят: `./gradlew testDebugUnitTest`
- [ ] verify coverage `:core:calculator` ≥ 95%: `./gradlew :core:calculator:koverHtmlReport` + ручная проверка отчёта
- [ ] verify coverage `:core:domain` ≥ 90%
- [ ] verify ktlint + detekt + Android Lint без ошибок: `./gradlew ktlintCheck detekt lintDebug`
- [ ] verify app запускается: `./gradlew :app:installDebug` на эмуляторе/устройстве + ручной запуск (single-shot manual check)
- [ ] verify Quick Calculator работает: ввести dog 5 лет Medium → должно показать ~57 ЧГ + Mature Adult
- [ ] verify Quick Calculator работает: ввести cat 5 лет Indoor → должно показать ~36 ЧГ + Young Adult
- [ ] verify list pet flow: добавить пета → появляется в списке → детальный экран → care recommendations отображаются (placeholder)
- [ ] verify локализация: переключить язык → строки меняются на en
- [ ] verify Material You: на Android 12+ цвета подстраиваются под обои; на старых — fallback палитра
- [ ] verify APK size: APK debug < 15 MB; release (если собран) < 8 MB — `./gradlew :app:bundleRelease && scripts/verify-bundle-size.sh`
- [ ] verify GitHub Actions workflows валидны: `actionlint .github/workflows/*.yml` или хотя бы yaml-syntax-check

## Technical Details

**Структура `:core:calculator`** (pure-Kotlin JVM, без Android-зависимостей):
```
core/calculator/src/main/kotlin/app/pawclock/calculator/
├── CalculationMethod.kt           — enum EPIGENETIC, SIZE_BASED
├── DogAgeCalculator.kt            — Wang + size table
├── DogSizeTable.kt                — AKC/AAHA 2019 таблица
├── DogLifeStageCalculator.kt      — стадии собак
├── CatAgeCalculator.kt            — AAHA/AAFP 2021
├── CatLifeStageCalculator.kt      — стадии кошек
└── ExpectedLifespan.kt            — диапазоны ЧЖ из McMillan 2024 / AAFP

core/calculator/src/test/kotlin/app/pawclock/calculator/
├── DogAgeCalculatorTest.kt
├── DogAgeCalculatorSizeBasedTest.kt
├── DogAgeCalculatorPropertyTest.kt (Kotest)
├── DogLifeStageCalculatorTest.kt
├── CatAgeCalculatorTest.kt
├── CatLifeStageCalculatorTest.kt
└── (property-based tests)
```

**Структура `:core:model`:**
```
core/model/src/main/kotlin/app/pawclock/model/
├── Species.kt        — sealed Species (с флагом isImplemented)
├── DogSize.kt        — enum: Toy, Small, Medium, Large, Giant
├── CatType.kt        — enum: IndoorShortHair, IndoorLongHair, Outdoor, LargeBreed
├── Gender.kt         — enum: Male, Female, Unknown
├── LifeStage.kt      — sealed (Dog.*, Cat.*, generic для будущих видов)
├── Pet.kt            — data class
├── CareRecommendation.kt
├── CalculationMethod.kt (re-exported из :core:calculator)
└── ThemeMode.kt
```

**Зависимости модулей:**
```
:app
  ↓
:feature:* ─→ :core:domain ─→ :core:model + :core:calculator
                ↓
              :core:database + :core:datastore
:feature:* ─→ :core:designsystem ─→ :core:model
```

**Ключевые формулы (с тестируемыми эталонными значениями):**
- Wang: `toHumanYears(1.0, EPIGENETIC) = 31.0` (±0.1); `toHumanYears(5.0) ≈ 56.7`
- Size Toy: `toHumanYears(1.0, SIZE_BASED, Toy) = 15.0`; `toHumanYears(10.0, SIZE_BASED, Toy) = 56.0`
- Cat: `toHumanYears(1.0, IndoorShortHair) = 15.0`; `toHumanYears(5.0, IndoorShortHair) = 36.0`; `toHumanYears(5.0, Outdoor) = 41.4`
- Cat large breed: `toHumanYears(5.0, LargeBreed) = 36.0 + 3.0 = 39.0`

**TDD-цикл для каждого калькулятора (Tasks 6-10):**
1. Red — написать тест из спецификации
2. Green — минимальная реализация
3. Refactor — извлечь константы, добавить KDoc с DOI
4. Параметризовать с табличными значениями
5. Edge cases (0, negative, very large)
6. Property-based (monotonicity, positivity) — отдельная Task 11

## Post-Completion

*Items requiring manual intervention or external systems — informational only*

**Manual verification:**
- Установить Branch Protection через GitHub UI после первого push: Settings → Branches → main → Require PR, Require status checks (`ci / unit-tests`, `ci / lint`, `ci / build`), Require linear history, Disallow force pushes (§8.4)
- Зарегистрировать Codecov для repo и добавить `CODECOV_TOKEN` в GitHub Secrets
- Зарегистрировать KEYSTORE_BASE64 / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD secrets в GitHub после генерации keystore локально (для release.yml)
- Создать Google Play Console аккаунт (29$), создать app `app.pawclock`, заполнить Data Safety "No data collected, no data shared"
- Проверить, что domain `pawclock.app` / `pawclock.dev` свободен — зарегистрировать опционально
- Проверить занятость названия PawClock в Google Play (см. §0)

**External system updates:**
- Подготовить fastlane/metadata/{ru,en-US}/short_description.txt + full_description.txt + screenshots для Google Play (после Plan 2)
- F-Droid metadata подготовить после публикации в Google Play (Plan 3)

**Manual content updates после Plan 1:**
- Реальный научный контент в `assets/care/*.json` (сейчас placeholder TODO-тексты)
- Реальные screenshots в README (после первого release)
- Реальная политика конфиденциальности на pawclock.app/privacy (когда домен куплен)
