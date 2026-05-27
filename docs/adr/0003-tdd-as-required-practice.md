# 0003. TDD as required practice for core calculation and domain modules

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: testing, quality, process

## Context and Problem Statement

PawClock — приложение, чья ценность завязана на математической корректности формул расчёта возраста. Ошибка в формуле = неверная медицинская рекомендация владельцу питомца. Это reputation risk и UX-фейл.

Дополнительно: проект open-source, контрибьюторы будут добавлять новые виды животных. Без процессной дисциплины формулы могут попасть в `main` без эталонных тестов из научных источников.

## Decision Drivers

- **Корректность формул выше скорости разработки**: одна ошибка типа `16 * ln(age) + 13` вместо `+31` уничтожает весь продукт.
- **Эталонные значения зашиты в литературе** (Wang T. 2020, AKC/AAHA 2019, AAFP 2021) — это идеальный кейс для test-first.
- **Coverage gates per module** (spec §11.4): `:core:calculator` ≥ 95%, `:core:domain` ≥ 90%.
- **Низкая стоимость TDD-цикла** благодаря выбору pure-Kotlin модуля (см. [ADR-0002](./0002-multi-module-architecture.md)) — тест запускается за < 1 секунды.

## Considered Options

1. **TDD обязателен для всего кода** — каждая строка должна иметь тест перед написанием.
2. **TDD обязателен для `:core:calculator` + `:core:domain`** (наш выбор) — для UI, DataStore, навигации — рекомендован, но не обязателен.
3. **Тесты пишутся после кода** — классический подход.

## Decision Outcome

Chosen option: **TDD обязателен для `:core:calculator` и `:core:domain`**, потому что:

- Эти модули содержат math + business invariants — идеальные кандидаты для test-first.
- Принудительный TDD для UI часто превращается в формальность (Compose-тесты пишутся после стабилизации экрана).
- Coverage gates в CI обеспечивают, что TDD-дисциплина не сорвётся под нагрузкой.

Конкретные правила:

1. Перед написанием новой формулы → новый тест с эталонным значением из научной статьи / стандарта.
2. KDoc функции расчёта **обязан** содержать ссылку на первоисточник (DOI, URL, имя стандарта AKC/AAHA/AAFP).
3. ParameterizedTest для табличных значений — каждое значение из таблицы спецификации → отдельная строка `@CsvSource`.
4. Edge cases (отрицательный возраст, ноль, очень большой возраст) — отдельные тесты с ожидаемым исключением.
5. Property-based тесты (Kotest) для математических свойств (monotonicity, positivity) — отдельная задача (Task 11).

### Positive Consequences

- Регрессии в формулах ловятся на этапе PR, не в production.
- Любой контрибьютор обязан читать научные источники и ссылаться на них (см. [[0006-wang-et-al-formula-as-default-for-dogs]]).
- Coverage `:core:calculator` ≥ 95% обеспечивается естественным образом.

### Negative Consequences

- Первоначальная скорость кода ниже, чем при classic test-after. Митигация: коэффициент Wang известен, эталонные значения есть в spec — TDD-цикл занимает 5-10 минут на функцию.
- Контрибьютор без опыта TDD должен обучиться. Митигация: `docs/CONTRIBUTING.md` содержит TDD walkthrough на `DogAgeCalculator`.

## Pros and Cons of the Options

### Option 1: TDD везде

- Good: максимальная дисциплина.
- Bad: формальные тесты на UI без реальной ценности; замедляет prototyping.

### Option 2: TDD для core (chosen)

- Good: фокус на критичной бизнес-логике.
- Bad: UI и feature-модули могут отставать по покрытию (компенсируется per-module gates).

### Option 3: Test-after

- Good: быстрый prototyping.
- Bad: формулы попадают в `main` без эталонных тестов; high-risk для продукта.

## Links

- spec §11.1 (TDD requirements), §11.4 (coverage thresholds), §11.5 (TDD example)
- [Kent Beck — Test-Driven Development: By Example](https://www.oreilly.com/library/view/test-driven-development/0321146530/)
- Related: [ADR-0002](./0002-multi-module-architecture.md), [ADR-0006](./0006-wang-et-al-formula-as-default-for-dogs.md)
