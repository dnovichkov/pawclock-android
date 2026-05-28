# 0002. Multi-module Gradle architecture

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: architecture, build, testing

## Context and Problem Statement

PawClock содержит четыре отчётливых слоя: чистая бизнес-логика (формулы), доменный слой (UseCases), данные (Room + DataStore + assets), UI (feature screens). Все четыре имеют разный темп изменений, разные требования к тестам и разные зависимости.

Возможные варианты структуры репозитория: монолитный `:app` (всё в одном модуле), или multi-module по `:core:*` + `:feature:*` (clean architecture style), либо чистая модульная feature-decomposition (`:pets`, `:cats`, ...).

## Decision Drivers

- **Скорость TDD-цикла**: формула расчёта возраста должна тестироваться без поднятия Android Gradle Plugin — только JVM.
- **Build time**: компиляция `:core:calculator` должна занимать секунды, не минуты.
- **Чёткие границы зависимостей** (см. spec §7.3 — onion-architecture rule).
- **Замещение реализаций в тестах**: feature-модули используют только интерфейсы из `:core:domain`.
- **Code review focus**: PR, меняющий формулу для крыс, не должен трогать UI Settings.

## Considered Options

1. **Monolithic `:app`** — весь код в одном модуле.
2. **Multi-module по clean-architecture слоям** (наш выбор): `:core:*` (бизнес и данные) + `:feature:*` (UI экраны) + `:app` (DI-граф и entry point).
3. **Multi-module по фичам** (`:dogs`, `:cats`) — каждый вид животного отдельный модуль.

## Decision Outcome

Chosen option: **Multi-module по слоям**, потому что:

- Spec §7.3 явно требует 12 модулей с фиксированной структурой.
- `:core:calculator` — pure Kotlin JVM (без Android dependencies), тесты выполняются за < 1 сек на CI.
- Формулы для нового вида (Plan 2: кролик, хомяк и т.д.) добавляются в `:core:calculator` без изменения UI-кода.
- Configuration cache + parallel builds Gradle 8.9+ дают почти линейный speed-up при холодной сборке.

### Positive Consequences

- `:core:calculator` остаётся pure-Kotlin → 95% coverage без Android-mocks.
- `:feature:*` модули зависят от `:core:domain` через интерфейсы → fakes в тестах вместо моков.
- Coverage requirements per module (см. spec §11.4) проверяются раздельно через Kover.

### Negative Consequences

- Больше boilerplate `build.gradle.kts` файлов — 12 штук вместо 1. Митигация: convention plugins в `buildSrc/` или общие constants (см. Task 1 — выбрали inline constants для простоты, конвертация в convention plugins при разрастании).
- Сложнее onboard для разработчика, не работавшего с multi-module setup — компенсируется `docs/ARCHITECTURE.md`.

## Pros and Cons of the Options

### Option 1: Monolithic

- Good: проще конфигурация, один `build.gradle.kts`.
- Bad: любое изменение пересобирает весь проект; невозможно изолировать pure-Kotlin тесты.

### Option 2: Layered multi-module (chosen)

- Good: чёткие слои, fast TDD, изоляция, переиспользование `:core:*` в будущих платформах (KMM возможен).
- Bad: больше Gradle-конфигурации.

### Option 3: Feature-based modules

- Good: каждый вид животного полностью изолирован.
- Bad: дублирование общей UI-логики; теряется reuse `:core:calculator` для разных видов.

## Links

- spec §7.3 (структура модулей), §11.4 (per-module coverage)
- [Now in Android architecture](https://github.com/android/nowinandroid) — reference multi-module Android sample
- Related: [ADR-0001](./0001-jetpack-compose-over-views.md), [ADR-0003](./0003-tdd-as-required-practice.md)
