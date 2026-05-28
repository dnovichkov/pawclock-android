# CONTRIBUTING

Спасибо за интерес к PawClock! Этот документ описывает, как присылать
изменения в проект.

## Содержание

- [Подготовка окружения](#подготовка-окружения)
- [Workflow: GitHub Flow](#workflow-github-flow)
- [Conventional Commits](#conventional-commits)
- [PR checklist](#pr-checklist)
- [Code style: ktlint + detekt](#code-style-ktlint--detekt)
- [Pre-commit hook](#pre-commit-hook)
- [Как добавить новый вид животного](#как-добавить-новый-вид-животного)

## Подготовка окружения

### Требования

- **JDK 17** (Temurin или Zulu рекомендуются). Через Java toolchain Gradle
  скачает нужную версию автоматически, но для IDE и CLI лучше иметь её
  локально.
- **Android Studio** (Hedgehog 2023.1.1 или новее) или IntelliJ IDEA
  с Android-плагином.
- **Android SDK**: compileSdk=35, minSdk=24, targetSdk=35.
- **Git** 2.30+ для поддержки sparse-checkout и worktrees.

### Сборка debug-варианта

```bash
git clone https://github.com/dnovichkov/pawclock-android.git
cd pawclock-android
./gradlew :app:assembleDebug
```

Установка на устройство/эмулятор:

```bash
./gradlew :app:installDebug
adb shell am start -n app.pawclock.debug/app.pawclock.MainActivity
```

### Запуск тестов

```bash
# Unit tests (быстро, без эмулятора)
./gradlew testDebugUnitTest

# Compose UI + integration tests (нужен подключённый эмулятор/устройство)
./gradlew connectedDebugAndroidTest

# Maestro E2E (нужен запущенный эмулятор + установленное приложение)
maestro test maestro/
```

Подробнее: `docs/TESTING.md`.

## Workflow: GitHub Flow

PawClock использует упрощённый **GitHub Flow** (см. §8.2 спецификации):

- **`main`** — всегда зелёная, всегда релизуемая. Прямой push запрещён
  Branch Protection правилами.
- **`feature/<scope>-<short-desc>`** — короткоживущая ветка (≤ 1 неделя).
  Пример: `feature/calculator-rabbits`, `feature/ui-pets-filter`.
- **`fix/<issue-id>-<short-desc>`** — багфиксы. Пример: `fix/12-cat-formula-rounding`.
- **`chore/<short-desc>`** — рутина (обновление либ, рефакторинг без
  изменения поведения).
- **`docs/<short-desc>`** — только документация.

Merge в `main` — через **Squash & Merge**, чтобы история была линейной,
и каждый коммит соответствовал одному PR / одной задаче. Squash-сообщение
должно следовать формату Conventional Commits (см. ниже).

## Conventional Commits

Все commit-сообщения и PR-заголовки следуют [Conventional Commits 1.0](https://www.conventionalcommits.org/en/v1.0.0/)
(см. ADR-0007).

```
<type>(<scope>): <short description in imperative mood>

[optional body]

[optional footer(s)]
```

### Типы (§8.3)

| Тип | Когда использовать |
|---|---|
| `feat` | Новая функция (триггерит MINOR bump) |
| `fix` | Багфикс (триггерит PATCH bump) |
| `docs` | Только документация |
| `style` | Форматирование, точки с запятой (без изменения логики) |
| `refactor` | Рефакторинг без изменения поведения |
| `test` | Добавление или исправление тестов |
| `chore` | Рутина (обновление либ, скрипты) |
| `perf` | Изменение производительности |
| `ci` | Изменения в GitHub Actions / pre-commit / scripts |
| `build` | Изменения в Gradle / build system |
| `revert` | Откат предыдущего коммита |

### Scope = название модуля

Без префиксов `core:` / `feature:`. Например: `calculator`, `pets`, `editor`,
`quickcalc`, `settings`, `model`, `database`, `datastore`, `designsystem`,
`domain`, `app`.

### Примеры

```
feat(calculator): add Wang et al. 2020 epigenetic formula for dogs
fix(ui): correct date picker locale on Android 7
test(calculator): add property tests for cat age boundary cases
chore(deps): bump compose-bom to 2024.12.00
docs(adr): add ADR-0008 for foreground color choices
ci(nightly): bump android-emulator-runner to v2.32
```

### Breaking changes

Обозначаются `!` после type/scope **или** footer'ом `BREAKING CHANGE:`.
Триггерят MAJOR bump:

```
feat(api)!: rename CalculatePetAgeUseCase to ComputePetAgeUseCase

BREAKING CHANGE: callers of CalculatePetAgeUseCase must be updated.
```

### Subject line — правила

- ≤ 72 символа.
- Императив: "add", не "added" / "adds".
- Lower case (кроме acronyms / proper nouns).
- Без точки в конце.

CI проверяет формат через `wagoid/commitlint-github-action` (или эквивалент).
PR с невалидным заголовком отклоняется.

## PR checklist

Перед открытием PR пройдитесь по чеклисту в `.github/PULL_REQUEST_TEMPLATE.md`.
Ключевые пункты:

### TDD & correctness

- [ ] Для новой бизнес-логики тесты написаны **до** реализации (TDD).
- [ ] Все unit-тесты проходят: `./gradlew testDebugUnitTest`.
- [ ] Coverage не упал ниже порога: `./gradlew koverVerify` (см. §11.4).
- [ ] Edge cases покрыты тестами (нулевые значения, отрицательные, границы).

### Source attribution

- [ ] Для новых формул calculator'а в KDoc есть ссылка на первоисточник:
      DOI, URL, или название стандарта (AKC/AAHA/AAFP).
- [ ] Новые care-рекомендации в `assets/care/*.json` имеют `source_url` +
      `source_name` + `disclaimer`.

### Quality gates

- [ ] `./gradlew ktlintCheck` проходит.
- [ ] `./gradlew detekt` проходит.
- [ ] `./gradlew lintDebug` проходит.
- [ ] Compose UI tests запущены локально (если затронут UI).

### Privacy & safety

- [ ] Никакой INTERNET permission не добавлен (см. ADR-0005).
- [ ] Никакая аналитика / телеметрия не подключена.
- [ ] Никакие персональные данные пользователя не логируются.

## Code style: ktlint + detekt

### ktlint

PawClock использует [ktlint](https://github.com/pinterest/ktlint) через
[ktlint-gradle](https://github.com/jlleitschuh/ktlint-gradle). Конфиг:
`.editorconfig` в корне репозитория.

Автоформат:

```bash
./gradlew ktlintFormat
```

Проверка:

```bash
./gradlew ktlintCheck
```

CI блокирует PR с ktlint-нарушениями.

### detekt

[detekt](https://detekt.dev/) — статический анализатор кода. Конфиг: `detekt.yml`
в корне репозитория.

```bash
./gradlew detekt
```

Если detekt падает на оправданном кейсе (например, magic-number в таблице
официального стандарта), используйте `@Suppress` с **обязательным комментарием**:

```kotlin
@Suppress("detekt:MagicNumber") // AKC/AAHA 2019 published values, not magic
private val ANCHOR_AGES = doubleArrayOf(1.0, 2.0, 3.0, ...)
```

### Android Lint

```bash
./gradlew lintDebug
```

Конфиг: `lint.xml` в корне (несущественные warnings подавлены).

## Pre-commit hook

`scripts/pre-commit.sh` запускает ktlintFormat + detekt +
`:core:calculator:test` перед каждым коммитом. Установка:

```bash
ln -s ../../scripts/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

Или через [pre-commit framework](https://pre-commit.com/).

Hook автоматически re-stage'ит файлы после `ktlintFormat`, чтобы коммит
включал отформатированную версию.

## Как добавить новый вид животного

Это walkthrough на примере добавления **кролика** (Plan 2). Шаги для других
видов идентичны.

### Шаг 1 — Open issue

Используйте template `.github/ISSUE_TEMPLATE/species_request.md` и заполните:

- Название вида (en + ru).
- Научное название (Oryctolagus cuniculus).
- Источник формулы расчёта возраста — peer-reviewed paper / vet-org guidelines
  / textbook (без citation issue отклоняется).
- Стадии жизни (Kit, Junior, Adult, Senior) с порогами.

### Шаг 2 — TDD цикл для калькулятора

В ветке `feature/calculator-rabbits`:

1. **Red.** Создайте `core/calculator/src/test/.../RabbitAgeCalculatorTest.kt`
   с одним падающим тестом из source citation:

   ```kotlin
   @Test
   fun `rabbit at 1 year equals 18 human years`() {
       // Source: <vet org guideline URL>
       val result = RabbitAgeCalculator().toHumanYears(1.0)
       assertEquals(18.0, result, absoluteTolerance = 0.5)
   }
   ```

2. **Green.** Минимальная реализация `RabbitAgeCalculator`.
3. **Параметризовать.** `@CsvSource` со всей таблицей из источника.
4. **Edge cases.** Granica age=0, отрицательные возрасты, экстраполяция.
5. **Refactor.** KDoc со ссылкой на источник.
6. **Property-based.** Монотонность, позитивность.

См. полный пример в `docs/TESTING.md` (раздел TDD-цикл).

### Шаг 3 — Life stages

`RabbitLifeStageCalculator.determine(age, breed): LifeStage.Rabbit`. Тот же
TDD-цикл, отдельная задача.

### Шаг 4 — Расширить domain

- Добавить `LifeStage.Rabbit` в `:core:model/LifeStage.kt`.
- Расширить `CalculatePetAgeUseCase` в `:core:domain` (добавить branch для Rabbit).
- Установить `Species.Rabbit.isImplemented = true`.
- Тесты UseCase'а на новый species.

### Шаг 5 — Care assets

Заполнить `:app/src/main/assets/care/rabbit/{kit,junior,adult,senior}/{ru,en}.json`
по шаблону из `:app/src/main/assets/care/dog/`. Каждый файл должен содержать
все обязательные поля + `disclaimer` (§3.3) + `source_url`.

### Шаг 6 — UI

Никаких feature-изменений: `:feature:editor` автоматически предложит Rabbit
в species selector через `Species.implemented()`. Subcategory dropdown
заполнится из `Species.Rabbit.subcategories` (если у вида есть подкатегории).

### Шаг 7 — Localization

Добавить строки в `feature/editor/src/main/res/values{,-en}/strings.xml` для
названия вида и его подкатегорий.

### Шаг 8 — Maestro flow (опционально)

`maestro/create_first_rabbit.yaml` по образцу `create_first_pet.yaml`.

### Шаг 9 — PR

```
feat(calculator): add rabbit age and life-stage calculators

Source: <vet org guideline URL>
Closes #<issue-id>
```

PR-чеклист (TDD, source attribution, coverage) — обязателен.

## Вопросы и поддержка

- **Bug reports**: `.github/ISSUE_TEMPLATE/bug_report.md`.
- **Feature requests**: `.github/ISSUE_TEMPLATE/feature_request.md`.
- **Discussions**: GitHub Discussions репозитория.
- **Спецификация**: `docs/specs/pawclock-specification.md` (источник истины
  для всех решений; ADR'ы в `docs/adr/` фиксируют конкретные выборы).
