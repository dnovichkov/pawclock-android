# 0004. Room as the persistence library (over SQLDelight)

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: data, database, dependencies

## Context and Problem Statement

PawClock хранит локально:
- профили питомцев (`Pet` — id, name, species, subcategory, birthDate, gender, weight, notes, photoPath),
- ссылочные таблицы (опционально для будущих видов).

Бэкенда нет (см. [ADR-0005](./0005-no-network-permission.md)) — вся персистентность on-device.

Доступные библиотеки локальной БД на Android:
- **Room** (AndroidX) — annotation processor поверх SQLite, тесная интеграция с Hilt, LiveData, Flow.
- **SQLDelight** (Cash App) — type-safe SQL generator, KMP-готовый.
- **Realm Kotlin** — object DB, более сложный модель данных.
- **ObjectBox** — high-perf object DB, проприетарная лицензия.

## Decision Drivers

- **Интеграция с Hilt + Compose**: Room — стандарт de-facto в AndroidX.
- **KSP-поддержка**: Room с 2.5+ поддерживает KSP вместо kapt (быстрее сборка).
- **Тестируемость**: `Room.inMemoryDatabaseBuilder(...)` без эмулятора (с Robolectric) или с in-memory на эмуляторе.
- **F-Droid совместимость** (Plan 3): только Apache 2.0 / BSD-зависимости.
- **Migration story**: Room имеет встроенный `Migration` mechanism с автогенерируемыми спеками.
- **Минимальные внешние зависимости**: spec §3.6 (offline) и §9 (privacy-first).

## Considered Options

1. **Room (AndroidX) + KSP** — наш выбор.
2. **SQLDelight** — KMP-портабельность.
3. **Realm Kotlin** — object-based.
4. **Plain SQLite + cursors** — без abstraction.

## Decision Outcome

Chosen option: **Room + KSP**, потому что:

- Spec §7.1 прямо требует Room 2.6+ с KSP.
- Apache 2.0 license — совместима с F-Droid.
- Глубокая интеграция с Hilt (стандартные `@Provides` для DB и DAO).
- `Flow<List<Entity>>` из DAO работает с Compose `collectAsStateWithLifecycle()` без адаптеров.
- TypeConverters достаточно мощные для `LocalDate`, `Species`, `DogSize` без перехода на JSON-blob.

### Positive Consequences

- Стандартный testing pattern: `androidTest` с `inMemoryDatabaseBuilder` + KSP-генерированный DAO.
- Schema export в `:core:database/schemas/` для verification миграций (см. Room `@Database(exportSchema = true)`).
- Hilt DI прозрачен: `@Provides @Singleton fun provideDb(...): PawClockDatabase`.

### Negative Consequences

- KSP всё ещё новее kapt, периодические quirks с инкрементальной сборкой. Митигация: pin KSP version в `libs.versions.toml`.
- Привязка к Android — если в будущем понадобится KMP (iOS-порт), Room ограничит только desktop/Android. Митигация: бизнес-логика в `:core:calculator` и `:core:domain` независима от Room → можно мигрировать только данные.

## Pros and Cons of the Options

### Option 1: Room (chosen)

- Good: AndroidX стандарт, отличная документация, Hilt, KSP, Flow.
- Bad: Android-only.

### Option 2: SQLDelight

- Good: KMP-готовность, чистый SQL вместо аннотаций.
- Bad: меньше community resources, нет first-class Flow из DAO без обвязки, проще писать guarded queries — но overhead не нужен сейчас.

### Option 3: Realm Kotlin

- Good: реактивные query streams "из коробки".
- Bad: более тяжёлый runtime, схема не "чистый SQL", сложнее миграции.

### Option 4: Plain SQLite

- Good: нулевые зависимости.
- Bad: огромный объём boilerplate, ручные миграции, нет type safety.

## Links

- spec §7.1 (стек), §3.2 (хранение профилей)
- [Room — Android Developers](https://developer.android.com/training/data-storage/room)
- [Room with KSP migration guide](https://developer.android.com/build/migrate-to-ksp)
- Related: [ADR-0002](./0002-multi-module-architecture.md), [ADR-0005](./0005-no-network-permission.md)
