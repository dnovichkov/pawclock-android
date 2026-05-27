# 0006. Wang et al. 2020 (epigenetic) as the default age formula for dogs

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: science, formulas, product

## Context and Problem Statement

PawClock рассчитывает «возраст в человеческих годах» (ЧГ) для собак. В литературе и популярной культуре существует несколько подходов:

1. **«Каждый год собаки = 7 человеческих лет»** — мифологическая упрощённая формула. Не научна.
2. **Wang T. et al., Cell Systems 2020** — эпигенетическая (DNA methylation): `humanYears = 16 · ln(dogAge) + 31`. DOI: [10.1016/j.cels.2020.06.006](https://doi.org/10.1016/j.cels.2020.06.006).
3. **AKC / AAHA 2019** — таблица по размерам (Toy / Small / Medium / Large / Giant) × возраст. Не математическая формула, а медицинская конвенция.
4. **McMillan 2024** — расширенная expected lifespan model по размерам и породам.

Spec §4.1 явно требует, чтобы оба подхода (Wang и size-based) были доступны, с возможностью переключения. Нужно зафиксировать **default**.

## Decision Drivers

- **Научная валидность**: Wang — единственная peer-reviewed формула, основанная на эпигенетических часах. Она даёт строго монотонную математическую функцию.
- **Простота для пользователя**: одна функция без необходимости знать размер собаки.
- **Возможность калибровки по размеру**: spec §4.1 приводит AKC/AAHA как альтернативу — это конвенциональный консенсус ветеринаров.
- **Объяснимость**: Wang легко визуализируется на графике, AKC легко представляется таблицей.
- **Корректность для размеров**: Wang НЕ учитывает, что гиганты живут меньше (4-8 vs 12-18 лет). AKC/AAHA учитывает.

## Considered Options

1. **Default = Wang, alternative = Size-based (AKC/AAHA)** (наш выбор).
2. **Default = Size-based, alternative = Wang**.
3. **Default = «×7»** — научно неверно.
4. **Default = автовыбор по подкатегории** — если пользователь указал size, использовать size-based, иначе Wang.

## Decision Outcome

Chosen option: **Default = Wang 2020 epigenetic, alternative = AKC/AAHA size table**, потому что:

- Wang — единственный peer-reviewed scientific метод. Default должен быть наиболее научно обоснованным.
- Wang НЕ требует знания размера → меньше friction в UI Quick Calculator.
- Пользователь, явно указавший размер собаки в профиле, может в Settings выбрать Size-based для своего pet → spec §3.3 (calculation method override).
- В Pet Detail экране показывается **обе** оценки одновременно (опция «Compare methods») — пользователь видит расхождение явно и принимает решение осознанно.

### Positive Consequences

- В UI всегда указан **источник** через "Как это посчитано" expandable: "Wang T. et al., Cell Systems 2020, DOI: 10.1016/j.cels.2020.06.006".
- Property-based test проверяет, что обе формулы монотонно возрастают на `(0, +∞)`.
- TDD-цикл для каждой формулы независим (Task 6 = Wang, Task 7 = Size).

### Negative Consequences

- Wang даёт **завышенные** значения для гигантов (например, mastiff в 5 лет по Wang ≈ 57 ЧГ, по AKC Giant 5y = 49). Митигация: пользователь видит обе оценки + warning в LifeStage UI ("гигантские породы стареют быстрее").
- Wang НЕ работает для возраста < 1 года (`ln(0.5) < 0` → ЧГ может быть отрицательной). Митигация: piecewise extension в `:core:calculator` для puppy stages (см. Task 6).

## Pros and Cons of the Options

### Option 1: Wang default (chosen)

- Good: science-first, простой UX, объяснимая формула.
- Bad: не учитывает размер для гигантов → нужна compensation в UI.

### Option 2: Size-based default

- Good: учитывает размер.
- Bad: требует от пользователя знать категорию (Toy/Small/Medium/Large/Giant) сразу — friction в Quick Calc.

### Option 3: «×7»

- Good: знакомо.
- Bad: научно неверно — обманывает пользователя.

### Option 4: Auto-switch

- Good: лучшее из обоих миров.
- Bad: пользователь не понимает, какая формула применилась → непредсказуемо. Можно вернуться к этому решению, если UX-тестирование покажет потребность.

## Links

- Wang T., et al. *Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling of the DNA Methylome*. Cell Systems, 2020. [DOI: 10.1016/j.cels.2020.06.006](https://doi.org/10.1016/j.cels.2020.06.006)
- AAHA 2019 Canine Life Stage Guidelines
- McMillan K. M., et al. *Mortality of dogs in the UK from 2016 to 2020*. Scientific Reports, 2024.
- spec §4.1 (формулы для собак), §11.5 (TDD example)
- Related: [ADR-0003](./0003-tdd-as-required-practice.md)
