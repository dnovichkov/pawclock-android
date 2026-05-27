# 0001. Jetpack Compose as the only UI toolkit

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: ui, architecture

## Context and Problem Statement

PawClock — greenfield Android-проект (см. `docs/specs/pawclock-specification.md`, версия 1.1).
Android-разработка сегодня предлагает два UI-toolkit'а: классический View-based stack (XML layouts + Activities/Fragments + AppCompat) и declarative Jetpack Compose.
Нужно зафиксировать выбор для всех UI-модулей (`:core:designsystem`, `:feature:*`, `:app`).

## Decision Drivers

- **Time-to-feature**: декларативный UI сокращает количество boilerplate-кода.
- **Material You / dynamic colors**: Compose Material 3 имеет first-class поддержку с Android 12+.
- **Тестируемость UI**: `createComposeRule()` + Roborazzi проще, чем Espresso + Robolectric.
- **TalkBack / accessibility**: Compose semantics tree единый и явный.
- **Размер APK**: spec §11.4 требует release APK < 8 МБ; AppCompat + Material Components тянут дополнительный вес.
- **Долгосрочная поддержка**: Google официально позиционирует Compose как рекомендованный toolkit с 2021 года.

## Considered Options

1. **Pure Jetpack Compose** — Material 3, Navigation Compose, без View-interop.
2. **Hybrid (Compose + Fragments + XML)** — постепенная миграция.
3. **Pure View-system** — XML layouts, AppCompat, Material Components.

## Decision Outcome

Chosen option: **Pure Jetpack Compose**, потому что проект новый — нет legacy-кода, который требовал бы View-interop. Compose даёт прямой выигрыш по time-to-feature, тестируемости и Material You без компромиссов.

### Positive Consequences

- Нет XML-layout'ов в `app/src/main/res/layout/` — только `res/values/` для strings, themes, colors.
- Единая модель состояния (`StateFlow` + `collectAsStateWithLifecycle()`).
- Screenshot-тесты через Roborazzi (см. §11.9) без эмулятора.
- Dynamic colors API 31+ из коробки.

### Negative Consequences

- minSdk = 24 ограничивает некоторые новые Compose API — компенсируется условными ветками и fallback палитрой (см. [[0001-jetpack-compose-over-views]] interplay с Material You strategy).
- Команда обязана знать Compose; для onboarding новых контрибьюторов добавлен раздел в `docs/CONTRIBUTING.md`.

## Pros and Cons of the Options

### Option 1: Pure Compose

- Good: меньше кода, явные state-flows, лучший DX, official direction.
- Bad: чуть большая стартовая сложность для разработчиков из мира XML.

### Option 2: Hybrid

- Good: возможность переиспользовать готовые View-компоненты.
- Bad: два механизма стилей, два механизма навигации, дублирование тестов.

### Option 3: Pure Views

- Good: широкая поддержка, много готовых решений.
- Bad: больше boilerplate, хуже тестируется, не получает новых Material 3 / dynamic colors features в первую очередь.

## Links

- [Jetpack Compose — Android Developers](https://developer.android.com/jetpack/compose)
- [Material 3 in Compose](https://m3.material.io/develop/android/jetpack-compose)
- spec §5 (UI/UX), §7.1 (стек технологий)
- Related: [ADR-0002](./0002-multi-module-architecture.md)
