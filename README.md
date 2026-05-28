# PawClock

> **Pet Age & Life Stage Calculator** — научно обоснованный офлайн-калькулятор возраста и стадий жизни для домашних животных.

<!-- BADGES: placeholder'ы. Реальные ссылки появятся после первого push в `main` и регистрации Codecov / Google Play / F-Droid. -->

[![CI](https://img.shields.io/badge/CI-pending-lightgrey)](https://github.com/dnovichkov/pawclock-android/actions)
[![Coverage](https://img.shields.io/badge/coverage-pending-lightgrey)](https://codecov.io/gh/dnovichkov/pawclock-android)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Release](https://img.shields.io/badge/release-pending-lightgrey)](https://github.com/dnovichkov/pawclock-android/releases)
[![Google Play](https://img.shields.io/badge/Google%20Play-pending-lightgrey)](https://play.google.com/store/apps/details?id=app.pawclock)
[![F-Droid](https://img.shields.io/badge/F--Droid-planned%20(Plan%203)-lightgrey)](https://f-droid.org/packages/app.pawclock/)

> **Статус проекта:** Plan 1 (foundation + Dog/Cat MVP). Поддерживаются собаки и кошки. Остальные 10 групп животных (см. §4 спецификации) приедут в Plan 2.

## Screenshots

<!-- TODO: добавить реальные screenshots после первого release (Plan 2). -->

| PetsList | PetDetail | QuickCalculator |
|---|---|---|
| _placeholder_ | _placeholder_ | _placeholder_ |

| PetEditor | Settings | About |
|---|---|---|
| _placeholder_ | _placeholder_ | _placeholder_ |

## Why PawClock

PawClock существует, потому что среди бесплатных Android-калькуляторов возраста животных:

1. **Большинство забиты рекламой и трекерами.** PawClock не имеет ни сети, ни аналитики, ни рекламы — Data Safety «No data collected».
2. **Большинство используют миф «умножь на 7».** PawClock применяет научно опубликованные формулы:
   - **Собаки:** Wang et al. 2020 (Cell Systems, DOI [10.1016/j.cels.2020.06.006](https://doi.org/10.1016/j.cels.2020.06.006)) — эпигенетическая формула `16 · ln(age) + 31`. Альтернатива — табличный метод AKC/AAHA 2019 с разбиением по размеру (Toy/Small/Medium/Large/Giant).
   - **Кошки:** AAHA/AAFP 2021 (DOI [10.1177/1098612X21993657](https://doi.org/10.1177/1098612X21993657)) — кусочная формула с поправками на outdoor/large breed.
3. **Большинство не знают про стадии жизни.** PawClock определяет текущую стадию (Puppy/Kitten → YoungAdult → MatureAdult → Senior → EndOfLife) по AAHA 2019 и AAHA/AAFP 2021.
4. **Большинство не локализованы.** PawClock — ru (default) + en с корректными CLDR plurals (`1 год` / `2 года` / `5 лет`).
5. **Material You.** Динамические цвета на Android 12+, тональная палитра fallback на старых версиях.
6. **Малый APK.** Целевой размер < 8 МБ release. Достижимо благодаря отсутствию Firebase / GMS / ads-SDK и lightweight-зависимостям.
7. **Open source.** Apache 2.0, готовится публикация в F-Droid (Plan 3).

Подробное сравнение с конкурентами — см. [§2 спецификации](docs/specs/pawclock-specification.md#2-анализ-конкурентов).

## Installation

PawClock пока **не опубликован** в магазинах. Доступные способы установки в текущей версии:

- **Google Play:** _появится после Plan 2_
- **APK from GitHub Releases:** _появится после первого тегированного релиза (см. [docs/RELEASE.md](docs/RELEASE.md))_
- **F-Droid:** _запланировано на Plan 3_

Для сборки локально из исходников — см. раздел **Development** ниже.

## Development

### Требования

- **JDK 17** (рекомендуется Temurin / Zulu).
- **Android Studio** Hedgehog (2023.1) или новее (рекомендуется Iguana / Koala 2024.x).
- **Android SDK** с `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`.
- Эмулятор API 24/30/35 либо физическое устройство для UI/Maestro-тестов.

### Quick start

```bash
git clone https://github.com/dnovichkov/pawclock-android.git
cd pawclock-android

# Сборка debug APK
./gradlew :app:assembleDebug

# Установка на подключённое устройство/эмулятор
./gradlew :app:installDebug

# Прогон unit-тестов всего проекта
./gradlew testDebugUnitTest

# Pre-commit (ktlintFormat → detekt → :core:calculator:test)
bash scripts/pre-commit.sh
```

### Структура репозитория

```
.github/             — workflow'ы (CI / lint / release / nightly), CODEOWNERS, templates
app/                 — :app модуль (Application, MainActivity, NavHost, Hilt root)
core/                — :core:* библиотеки (model, calculator, database, datastore, domain, designsystem)
feature/             — :feature:* экраны (pets, editor, quickcalc, settings)
docs/                — ARCHITECTURE, TESTING, CONTRIBUTING, RELEASE + ADR + specs + plans
maestro/             — E2E flow'ы (create_first_pet, quick_calc_dog)
scripts/             — pre-commit и verify-* скрипты
gradle/              — version catalog (libs.versions.toml) и wrapper
```

Подробный architecture overview — см. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Tech stack

- **Kotlin 2.0.21** + **Java 17** toolchain
- **Jetpack Compose** + **Compose BOM 2026.05.00** + **Material 3 1.4.0**
- **Hilt** для DI
- **Navigation Compose 2.9.0** (typesafe routes)
- **Coroutines + Flow** + **Turbine** для асинхронного тестирования
- **Room 2.8.4** (KSP) — локальная БД профилей
- **DataStore Preferences 1.1.1** — настройки приложения
- **kotlinx.serialization** — JSON (care recommendations, navigation arguments)
- **JUnit 5** + **kotlin.test** + **Kotest** (property-based)
- **Roborazzi** + **Robolectric** — screenshot tests (opt-in)
- **Maestro** — E2E flow'ы
- **Kover** — coverage measurement
- **ktlint** + **detekt** + **Android Lint** — линтеры

Минимальная поддерживаемая версия — **Android 7.0** (API 24), целевая — **Android 15** (API 35).

Полный стек и обоснование выборов — см. [§7 спецификации](docs/specs/pawclock-specification.md#7-архитектура) и [docs/adr/](docs/adr/).

## Testing

PawClock следует **TDD-дисциплине** для `:core:calculator` и `:core:domain` (см. [ADR-0003](docs/adr/0003-tdd-as-required-practice.md)). Coverage thresholds enforced через `koverVerify`:

| Модуль | Минимальное покрытие | Текущее (Plan 1) |
|---|---|---|
| `:core:calculator` | 95% | ~96% |
| `:core:domain` | 90% | ~98% |
| `:core:datastore` | 80% | ~92% |
| `:feature:*` ViewModels | 80% | ≥80% |

Test pyramid, TDD-cycle на примере `DogAgeCalculator`, инструкции по Maestro, screenshot tests и локализационным тестам — см. [docs/TESTING.md](docs/TESTING.md).

## Contributing

PRs приветствуются. Перед открытием PR ознакомьтесь с:

- [Conventional Commits 1.0](https://www.conventionalcommits.org/) — см. [ADR-0007](docs/adr/0007-conventional-commits.md)
- [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) — TDD-walkthrough для добавления нового вида животного, GitHub Flow, code style (ktlint + detekt)
- [PR template](.github/PULL_REQUEST_TEMPLATE.md) — чек-лист TDD / Source attribution / Quality gates / Privacy & safety
- Issue templates: [bug](.github/ISSUE_TEMPLATE/bug_report.md), [feature](.github/ISSUE_TEMPLATE/feature_request.md), [species](.github/ISSUE_TEMPLATE/species_request.md)

## Privacy

PawClock — **No data collected, no data shared** (Google Play Data Safety):

- ❌ Без рекламы.
- ❌ Без Firebase / Crashlytics / AppMetrica / Google Analytics.
- ❌ Без сетевых запросов: `<uses-permission android:name="android.permission.INTERNET" tools:node="remove"/>` форсирует удаление INTERNET-permission даже из transitive AAR (см. [ADR-0005](docs/adr/0005-no-network-permission.md)).
- ❌ Без аккаунтов и облачной синхронизации.
- ✅ Все профили питомцев хранятся локально в Room SQLite (`/data/data/app.pawclock/databases/pawclock.db`).
- ✅ Все настройки — в DataStore Preferences (локально).
- ✅ Фото питомца (опционально) — через Photo Picker (`ACTION_PICK_IMAGES`) **без runtime-разрешений** на READ_MEDIA_IMAGES.

Подробности — [§9 спецификации](docs/specs/pawclock-specification.md#9-приватность-и-безопасность).

## Sources

Все формулы и пороги стадий жизни в PawClock основаны на публикуемых ветеринарных источниках:

- **Собаки:** Wang et al. 2020 (Cell Systems), AAHA 2019 Canine Life Stage Guidelines, AKC/AAHA size table, McMillan et al. 2024 (Scientific Reports, n=584 734).
- **Кошки:** AAHA/AAFP 2021 Feline Life Stage Guidelines, Quimby et al.
- **Технические:** Material Design 3, Compose, Room, DataStore, Navigation Compose, Roborazzi, Maestro.

Полная библиография — [§14 спецификации](docs/specs/pawclock-specification.md#14-источники-и-литература). Каждая формула в `:core:calculator` имеет KDoc со ссылкой на первоисточник (DOI / URL).

## License

```
Copyright 2026 Dmitriy Novichkov and PawClock contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

Полный текст — [LICENSE](LICENSE). Контент care-рекомендаций (Plan 2+) лицензируется отдельно под [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).

## Disclaimer

> Информация в PawClock носит исключительно ознакомительный характер и не заменяет визита к лицензированному ветеринарному врачу. Всегда консультируйтесь с ветеринаром по вопросам здоровья, кормления и ухода за вашим питомцем.
