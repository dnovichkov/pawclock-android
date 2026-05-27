# Техническая спецификация Android-приложения «PawClock»

> **Pet Age & Life Stage Calculator** — научно обоснованный офлайн-калькулятор возраста и стадий жизни для всех популярных домашних животных.

---

## 0. Идентификация проекта

| Параметр | Значение |
|---|---|
| **Рабочее название** | **PawClock** |
| **Полное название в Google Play** | PawClock — Pet Age & Life Stage |
| **Имя репозитория на GitHub** | `pawclock-android` |
| **Application ID (packageName)** | `app.pawclock` |
| **Module name** | `:app` (плюс модули `:core:calculator`, `:core:database`, `:feature:pets`, см. §7.3) |
| **License** | Apache License 2.0 |
| **Лицензия контента (рекомендации, иконки)** | CC BY 4.0 |
| **Платформа** | Android 7.0+ (API 24), целевая 35 |
| **Минимально жизнеспособный продукт** | См. §12.1 |

### Почему такое название

- **Лапа + часы** — мгновенно считываемая метафора возраста и времени.
- Не привязано к виду (наш продукт поддерживает 12 групп животных, см. §4), в отличие от `DogAge`, `CatAge`, `KittyTime`.
- На русском читается без перевода («ПоуКлок»), не требует адаптации.
- В момент проверки (декабрь 2025 — май 2026) свободно в Google Play, на GitHub, и доступно для регистрации как домена `pawclock.app` / `pawclock.dev` (рекомендуется проверить непосредственно перед публикацией).

### Запасные варианты названия (если основное окажется занято)

| Название | Идея | Имя репозитория |
|---|---|---|
| **PetSpan** | про lifespan, продолжительность жизни | `petspan-android` |
| **PetYears** | буквальное, отличный ASO | `petyears-android` |
| **TailTime** | игривое, broad species | `tailtime-android` |
| **Furcast** | fur + forecast, прогноз стадий | `furcast-android` |
| **PetChrono** | хроно — про время | `petchrono-android` |

---

## 1. Введение и цели

### 1.1. Описание приложения

**PawClock** — бесплатное офлайн-приложение для Android, которое:

1. Переводит реальный возраст домашнего питомца в «человеческие годы» с использованием современных научных формул, учитывающих вид, породу, размер и образ жизни.
2. Определяет текущую стадию жизни питомца (puppy / kitten / kit / adult / senior / geriatric и т. п.) и выдаёт информационные рекомендации по уходу для этой стадии — на основании руководств AAHA, AAFP, WSAVA, AVMA, AAV, AAEP, Oxbow и рецензируемых публикаций.
3. Хранит профили нескольких питомцев (имя, вид, порода, дата рождения, фото или стилизованная иконка) полностью локально, без сети, без аккаунтов и без аналитики.

Приложение задумано как «честная» альтернатива забитым рекламой Android-калькуляторам и как функционально более широкий аналог iOS-приложений `PawTime`, `Pets Age Calculator (Berkan Gunduz)`, `Pet Age Calculator Human Years (James Njoroge)` и `DogAge` (см. §2).

### 1.2. Целевая аудитория

- **Владельцы собак и кошек** — основной сегмент.
- **Владельцы экзотики** (грызуны, кролики, хорьки, попугаи, рептилии, аквариумные рыбы) — недообслуженный сегмент: в большинстве конкурентов нет формул, основанных на ветеринарных источниках.
- **Заводчики, волонтёры приютов и юные владельцы первого питомца** — ценят жизненные стадии и информационные подсказки по уходу.
- **Технически грамотные пользователи**, принципиально выбирающие приложения без рекламы и трекинга (FOSS-аудитория, F-Droid).

### 1.3. Уникальные преимущества (USP)

1. **Самый широкий список видов** среди бесплатных Android-калькуляторов: собаки (4 размерные группы), кошки (домашние/уличные), кролики, хомяки (5 видов), морские свинки, крысы, мыши, хорьки, попугаи (8+ видов), канарейки, голуби, черепахи, змеи, игуаны, гекконы, лошади, золотые рыбки/кои/тропические рыбы.
2. **Научно обоснованные формулы** — для собак: одновременно эпигенетическая формула Wang et al. (Cell Systems, 2020) и классическая таблица AKC/AAHA 2019, разделённая по размеру. Для кошек — формула AAHA/AAFP 2021. Для крыс/мышей — Sengupta (2013) / Dutta & Sengupta (2016).
3. **Стадии жизни и рекомендации по уходу** — отсутствуют у большинства конкурентов либо подаются как маркетинг кормов.
4. **Material You** — динамические цвета, выразительная типографика M3 Expressive, крупные скруглённые формы.
5. **Полная конфиденциальность**: нет рекламы, трекеров, интернет-разрешения. Соответствует Data Safety «No data collected».
6. **Малый APK** — целевой размер < 8 МБ.
7. **Открытый исходный код** (Apache 2.0) и подготовка к публикации в F-Droid.

### 1.4. Чем отличается от конкурентов

| Свойство | PawClock | Типичный Android-конкурент | iOS-конкуренты |
|---|---|---|---|
| Реклама | Нет | Полноэкранные баннеры/видео | Нет / опциональный premium |
| Список видов | 12 групп, 25+ подвидов | 2–5 видов | 5–7 видов |
| Источник формулы для собак | AKC/AAHA 2019 + Wang 2020 | «умножь на 7» | Размерная таблица |
| Стадии жизни | Да, с цитированием | Нет | Частично |
| Профили нескольких питомцев | Да | Часто только однократный расчёт | Иногда |
| Material You / тёмная тема | Да, динамические цвета | Material 2 / устаревший UI | Native iOS |
| Локализация ru/en | Да | Часто только en | en/de/es |
| Размер APK | < 8 МБ | Заметно крупнее | 20–40 МБ |
| Сетевые запросы | Отсутствуют | Постоянная телеметрия | Минимальные |
| Open source | Apache 2.0, GitHub | Нет | Нет |

---

## 2. Анализ конкурентов

### 2.1. iOS-аналоги — что хорошего

- **PawTime: Pet Age** (Kevin Waltz) — чистый UI, виджеты, Apple Watch, iCloud, кошки и собаки. В версии 1.2 добавлены постоянное сохранение и календарный выбор даты после жалоб пользователей. Урок: профили — обязательны с v1.0.
- **Pets Age Calculator** (Berkan Gunduz) — самый широкий охват видов на iOS, акцент на «aging processes».
- **DogAge** (Jeongmo Kang) — единственный конкурент, явно использующий формулу `human_age = 16 · ln(dog_age) + 31` Wang et al. с объяснением логики.
- **Pet Age Calculator Human Years** (James Njoroge) — есть Android-версия.

**Заимствуемые UX-паттерны:** постоянное сохранение профилей, виджеты «возраст одним взглядом», календарный выбор даты, объяснение «как посчитано», сортировка списка.

### 2.2. Android-аналоги — что плохого

В Google Play доминируют `Pet Age Calculator` (Scorepixel), `Animal: Pet Age Calculator` (Njoroge), `Pet Age Calculator` (Softwarebloat). Общие проблемы:

1. Полноэкранная реклама — главная жалоба в отзывах.
2. Формула «× 7» или непрозрачная.
3. 2–3 вида.
4. Material 2, нет тёмной темы.
5. Лишние разрешения (Network, Location, Contacts).
6. Нет рекомендаций по уходу.
7. Крупный APK из-за рекламных SDK.
8. Нет сохранения профилей.

### 2.3. Возможности для дифференциации

— см. сводную таблицу в §1.4.

---

## 3. Функциональные требования

### 3.1. Основной функционал: расчёт возраста

**Вход (минимум):** вид животного + дата рождения **или** возраст в годах/месяцах/неделях.

**Дополнительно (опционально):** подкатегория (порода-размер, подвид, indoor/outdoor), фактическая взрослая масса (для собак), дата расчёта.

**Выход:**
1. Возраст в человеческих годах.
2. Стадия жизни (цветной чип + таймлайн-прогресс на ширину ожидаемой ЧЖ).
3. Краткое описание стадии и 3–5 ключевых рекомендаций по уходу.
4. Раскрывающийся блок «Как это посчитано» с формулой и ссылкой на источник.
5. Полная таблица «возраст питомца → ЧГ» для данного вида.
6. Ожидаемая продолжительность жизни и текущее положение на шкале.

**Точность:** дата рождения → возраст с точностью до дня; формат «X лет Y месяцев» или для маленьких животных младше года «X месяцев Y недель».

### 3.2. Профили питомцев

- Неограниченное количество профилей.
- Поля: имя (required), вид (required), подкатегория, дата рождения, пол, вес, заметка, фото или стилизованная иконка.
- Редактирование/удаление со swipe + Snackbar Undo.
- Главный экран — список карточек, сортировка: имя/возраст/дата создания/вид; фильтр по виду.
- Карточка: фото/иконка, имя, чип стадии, возраст в годах и ЧГ.

### 3.3. Рекомендации по уходу по стадиям жизни

Для каждой пары (вид × стадия):
- Описание стадии (1–2 абзаца).
- Питание.
- Активность и обогащение среды.
- Ветеринарные осмотры (частота).
- Стоматология (если применимо).
- На что обращать внимание.
- Ссылка на первоисточник.

**Обязательный дисклеймер** на каждом экране рекомендаций:

> Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача.

### 3.4. Уведомления и напоминания (v2.0)

Локальные уведомления через `WorkManager`:
- Ежегодный/полугодовой ветосмотр по стадии.
- День рождения питомца.
- Переход в новую стадию.

По умолчанию выключены. Никаких push.

### 3.5. Экспорт/импорт данных

- Экспорт: JSON и CSV через SAF (`ACTION_CREATE_DOCUMENT`).
- Импорт: `ACTION_OPEN_DOCUMENT`.
- Основа миграции на новое устройство.

### 3.6. Темы и персонализация

- Светлая / тёмная / системная тема.
- Динамические цвета Material You на Android 12+, fallback на сгенерированную палитру.
- Выбор seed-цвета для пользователей до Android 12.
- Поддержка `fontScale`.
- Язык: системный / русский / английский (in-app picker).

---

## 4. Поддерживаемые виды животных

(Полные формулы и стадии см. ниже; формулы будут жить в `:core:calculator` и покрываться TDD согласно §11.)

> Условные обозначения: `ЧГ` — человеческие годы; `ln` — натуральный логарифм.

### 4.1. Собаки

**Биология.** ЧЖ 6–18 лет. По McMillan et al. (Scientific Reports, 2024, n=584 734): мелкие (< 9 кг) — 12–16 лет, средние (9–23 кг) — 10–14, крупные (23–45 кг) — 9–12, гигантские (> 45 кг) — 7–10. Старость у гигантских — с 5–6 лет.

**Основная формула (Wang et al., 2020):**
```
ЧГ = 16 · ln(возраст_в_годах) + 31      (при возраст ≥ 1 год)
```
Источник: Wang T., Tsui B., Kreisberg J.F. et al. *Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling of the DNA Methylome*. Cell Systems, 2020; 11(2):176-185. DOI: 10.1016/j.cels.2020.06.006.

**Размерная таблица (AKC / AAHA 2019):**

| Возраст | Малая ≤9 кг | Средняя 9–23 | Крупная 23–45 | Гигантская >45 |
|---|---|---|---|---|
| 1 | 15 | 15 | 15 | 12 |
| 2 | 24 | 24 | 24 | 22 |
| 3 | 28 | 28 | 28 | 31 |
| 4 | 32 | 32 | 32 | 38 |
| 5 | 36 | 36 | 36 | 45 |
| 6 | 40 | 42 | 45 | 49 |
| 7 | 44 | 47 | 50 | 56 |
| 8 | 48 | 51 | 55 | 64 |
| 10 | 56 | 60 | 66 | 79 |
| 12 | 64 | 69 | 77 | 93 |
| 14 | 72 | 78 | 88 | 107 |
| 15 | 76 | 83 | 93 | 114 |

**Стадии (AAHA 2019):** Puppy, Young Adult, Mature Adult, Senior, End of Life. Пороги senior различаются по размеру (см. предыдущую версию спеки).

### 4.2. Кошки

**Биология.** ЧЖ 12–18 (домашние), 2–5 (уличные).

**Формула (AAHA/AAFP 2021):**
```
1-й год = 15 ЧГ
2-й год = +9 (итого 24)
3+ год  = +4 за каждый год       → ЧГ = 24 + 4·(возраст−2) при возраст≥2
```

**Поправки:** outdoor × 1.15 после 2 лет (приближённая); крупные породы (Maine Coon и др.) +1 ЧГ/год после 2.

**Стадии (AAHA/AAFP 2021):** Kitten (0–1), Young Adult (1–6), Mature Adult (7–10), Senior (10+), End of Life.

### 4.3. Кролики

**ЧЖ:** 8–12 лет (мелкие до 14, гигантские 6–8).

**Формула (кусочная):**
```
0–4 мес.:    ЧГ ≈ 30 · возраст
4–12 мес.:   ЧГ = 12 + 8 · (возраст − 0.33)
1+ год:      ЧГ = 21 + 6 · (возраст − 1)
```
**Стадии (Oxbow):** Infancy (0–3 мес.), Adolescence (3–6 мес.), Young adult (6–12 мес.), Adult (1–5), Senior (5+).

### 4.4. Хомяки

**ЧЖ:** 1.5–3.5 года в зависимости от вида. Старость с 1.5 лет.

**Формула (кусочная):**
```
0–1 мес.: ЧГ = 1 за каждые 4 дня
1–2 мес.: ЧГ ≈ 18
2–6 мес.: ЧГ ≈ 30
6–18 мес.: +3 ЧГ/мес.
> 18 мес.: +4 ЧГ/мес.
```
**Стадии:** Pup, Juvenile, Adult, Senior, Very senior.

### 4.5. Морские свинки

**ЧЖ:** 5–7 лет; «senior» с 4.

**Формула (кусочная):**
```
0–3 нед.:   ЧГ ≈ 0.5
3 нед.:     ≈ 6 мес. (отъём)
2–3 мес.:   ≈ 11.5 (половая зрелость)
4–5 мес.:   ≈ 20
> 5 мес.:   +8 ЧГ/год
> 4 года:   +10 ЧГ/год
```

### 4.6. Крысы и мыши

**Крысы (Sengupta 2013):**
```
ЧГ = 13.8 · возраст_в_годах + 1.4
```

**Мыши (Dutta & Sengupta 2016):**
```
1–42 дня:    150 ЧДней / день мыши
42–180 дн.:  45 / день
180–365 дн.: 30 / день
365–730 дн.: 25 / день
> 730 дн.:   20 / день
```

### 4.7. Хорьки

**ЧЖ:** 5–10 лет; senior с 3–4 лет.

**Формула (кусочная):**
```
0–6 мес.:    +5 ЧГ/мес.
6 мес.:      ≈ 30 ЧГ
1 год:       ≈ 40 ЧГ
> 1 года:    +4 ЧГ/год
```

### 4.8. Птицы

**ЧЖ:** от 5–10 лет (волнистые) до 49 лет (амазоны).

**Формула:**
```
ЧГ = возраст_птицы · (80 / средняя_ЧЖ_вида)
```
С модификатором × 1.3 для возрастов до 6 месяцев.

### 4.9. Рептилии

**Формула:** `ЧГ = возраст · (80 / средняя_ЧЖ_вида)` с 4 фазами (Hatchling, Juvenile, Adult, Senior).

### 4.10. Лошади

**Формула (AAEP):**
```
1 год = 6.5 ЧГ
2 год = +6.5 (13)
3 год = +5 (18)
4 год = +2.5 (20.5)
> 4 лет = +2.5 ЧГ/год
```

### 4.11. Рыбы

**Формула:** `ЧГ = возраст · (80 / средняя_ЧЖ_вида)`, ЧЖ от 2 лет (гуппи) до 35+ (кои).

### 4.12. Сводная таблица источников

| Вид | Главная формула | Первичный источник |
|---|---|---|
| Собака | 16·ln(age)+31 | Wang et al., Cell Systems 2020 |
| Собака (alt) | Размерная таблица | AKC / AAHA 2019 |
| Кошка | 15 / +9 / +4 | AAHA/AAFP 2021 |
| Кролик | Кусочная | House Rabbit Society + AVMA |
| Хомяк | Кусочная | RVC + Animallama |
| Морская свинка | Кусочная | Oxbow + Animallama |
| Крыса | 13.8·age+1.4 | Sengupta 2013 |
| Мышь | Кусочная | Dutta & Sengupta 2016 |
| Хорёк | Кусочная | PMC «Senior Ferret» |
| Попугай | age·80/lifespan | AAV |
| Рептилии | age·80/lifespan | Petplace, Reptile Centre |
| Лошадь | 3-фазная | AAEP / PetMD |
| Рыбы | age·80/lifespan | PetMD, AquariumStoreDepot |

---

## 5. UX/UI требования

### 5.1. Дизайн-система Material 3 / Material You

- `androidx.compose.material3:material3` + `window-size-class` + `adaptive-navigation-suite`.
- `MaterialTheme(colorScheme, typography, shapes)`.
- Динамические цвета на Android 12+; fallback из Material Theme Builder.
- `RoundedCornerShape(24.dp)` карточки, `28.dp` кнопки.

### 5.2. Навигация

Single-Activity, Navigation Compose, typesafe routes. Иерархия: PetsList → PetDetail/PetEditor/QuickCalculator + Settings + About. `NavigationSuiteScaffold` для адаптива.

### 5.3. Основные экраны

**PetsList:** LargeTopAppBar, LazyColumn `ElevatedCard`, FAB.
**PetDetail:** collapsing toolbar, hero-блок, большая «карточка возраста», прогресс стадии, сворачиваемые секции рекомендаций, кнопка «Как это посчитано».
**PetEditor:** один длинный экран на phone, FAB Save.
**QuickCalculator:** PetEditor без сохранения, результат в bottom sheet.
**Settings:** ListItems с switches.

### 5.4. Анимации

Shared element transitions, Material Motion, spring-анимации, конфетти при первой стадии (Lottie).

### 5.5. Темы

Light/Dark/System; `dynamicColor` toggle; высокий контраст.

### 5.6. Иконки видов

Стилизованные SVG 48×48, моно-линейные, Phosphor / Lucide / собственный набор.

### 5.7. Типографика

Roboto + опционально Roboto Flex. Шкала M3 от displayLarge до labelMedium. Полная поддержка кириллицы.

### 5.8. Адаптивность

Window Size Classes Compact/Medium/Expanded, foldable через `WindowInfoTracker`.

---

## 6. Локализация

- `values/strings.xml` (русский) + `values-en/strings.xml`.
- `plurals` для возраста.
- `AppCompatDelegate.setApplicationLocales` + `LocaleConfig` (Android 13+).
- Care-рекомендации в `assets/care/{species}/{stage}/{locale}.json`.
- Roadmap: ru, en → de, es → zh-CN, pt-BR, fr.

---

## 7. Технические требования

### 7.1. Стек

- **Kotlin 2.0+**
- **Jetpack Compose** + Compose BOM 2024.12+
- **Material 3** 1.3+
- **Hilt** DI
- **Navigation Compose** 2.8+ (typesafe)
- **Coroutines + Flow** + **Turbine** для тестов
- **Room** 2.6+ (KSP)
- **DataStore Preferences**
- **kotlinx.serialization** для JSON

### 7.2. SDK versions

- `minSdk = 24` (Android 7.0) — покрытие ~99.1% по официальной статистике Google.
- `compileSdk = 35` / `targetSdk = 35` (Android 15).
- Java 17 toolchain.

### 7.3. Архитектура (multi-module)

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
:feature:settings              — настройки и экспорт/импорт
```

Преимущества для TDD: `:core:calculator` — чистый Kotlin без Android-зависимостей, тесты выполняются на JVM за миллисекунды, без эмулятора.

Архитектура внутри feature-модуля: **MVI** с однонаправленным потоком (`StateFlow<UiState>` + `sealed UiEvent`).

### 7.4. Локальное хранение

- **Room** — профили и история напоминаний.
- **DataStore Preferences** — настройки.
- **Assets** — care-рекомендации и формулы (read-only справочные данные).
- Фото — `app filesDir/pets/{id}.jpg`, путь в БД.

### 7.5. Размер APK

Цель < 8 МБ скачивание / < 15 МБ универсальный APK. Меры: R8 + resource shrinking, vector drawables, нет GMS/Firebase/AdMob, системный Roboto.

### 7.6. Производительность

- Startup < 500 мс Pixel 6, < 1500 мс на бюджетных Android 7.
- **Baseline Profiles** через `androidx.benchmark:benchmark-macro-junit4`.
- LazyColumn с `key`, `derivedStateOf`.

---

## 8. Разработка: GitHub workflow и CI/CD

### 8.1. Структура репозитория

```
pawclock-android/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml              # сборка + тесты на каждый PR
│   │   ├── release.yml         # сборка AAB по тегу
│   │   ├── nightly.yml         # ночные UI-тесты на эмуляторе
│   │   └── lint.yml            # ktlint, detekt, Android Lint
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   ├── feature_request.md
│   │   └── species_request.md
│   ├── PULL_REQUEST_TEMPLATE.md
│   ├── CODEOWNERS
│   └── dependabot.yml
├── app/                        # точка входа
├── core/                       # модули :core:*
├── feature/                    # модули :feature:*
├── docs/
│   ├── ARCHITECTURE.md
│   ├── TESTING.md
│   ├── CONTRIBUTING.md
│   ├── RELEASE.md
│   └── adr/                    # Architecture Decision Records
├── fastlane/                   # метаданные Google Play
│   └── metadata/android/{ru,en-US}/
├── gradle/
│   └── libs.versions.toml      # version catalog
├── scripts/
│   ├── pre-commit.sh
│   └── verify-bundle-size.sh
├── .editorconfig
├── .gitignore
├── CHANGELOG.md
├── LICENSE
├── README.md
└── build.gradle.kts
```

### 8.2. Стратегия ветвления — **GitHub Flow** (упрощённый Git Flow)

Для соло-разработки полный Git Flow избыточен. Принципы:

- **`main`** — всегда зелёная, всегда релизуемая. Прямой push запрещён настройками Branch Protection.
- **`feature/<scope>-<short-desc>`** — короткоживущая (≤ 1 неделя). Пример: `feature/calculator-dogs`, `feature/ui-pets-list`.
- **`fix/<issue-id>-<short-desc>`** — багфиксы. Пример: `fix/12-cat-formula-rounding`.
- **`chore/<short-desc>`** — рутина (обновление либ, рефакторинг без изменения поведения).
- **`docs/<short-desc>`** — только документация.

**Merge через Squash** в `main`, чтобы история была линейной и каждый коммит соответствовал одному PR / одной задаче.

### 8.3. Конвенция коммитов — **Conventional Commits**

```
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

Типы: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`, `build`, `revert`.

Примеры:
```
feat(calculator): add Wang et al. 2020 epigenetic formula for dogs
fix(ui): correct date picker locale on Android 7
test(calculator): add property tests for cat age boundary cases
chore(deps): bump compose-bom to 2024.12.00
```

Польза: автоматическая генерация `CHANGELOG.md` через `git-cliff` или `Release Please` GitHub Action.

### 8.4. Branch Protection rules для `main`

В Settings → Branches:

- Require pull request before merging.
- Require status checks to pass: `ci / unit-tests`, `ci / lint`, `ci / build`.
- Require branches to be up to date before merging.
- Require linear history (форс-сквош).
- Disallow force pushes.
- Disallow deletions.

### 8.5. GitHub Actions — конкретные workflow

#### 8.5.1. `ci.yml` — на каждый push и PR

```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v3
      - run: ./gradlew :core:calculator:test --no-daemon
      - run: ./gradlew testDebugUnitTest --no-daemon
      - uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Unit Tests
          path: '**/build/test-results/test*/TEST-*.xml'
          reporter: java-junit
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - run: ./gradlew ktlintCheck detekt lintDebug
  build:
    runs-on: ubuntu-latest
    needs: [unit-tests, lint]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - run: ./gradlew assembleDebug bundleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
  screenshot-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - run: ./gradlew verifyRoborazziDebug
      - if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshot-diffs
          path: '**/build/outputs/roborazzi/**'
```

#### 8.5.2. `release.yml` — по тегу `v*.*.*`

- Сборка `bundleRelease` с подписью из GitHub Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
- Проверка размера AAB (`scripts/verify-bundle-size.sh`, лимит 15 МБ).
- Создание GitHub Release с автогенерированным changelog.
- Опционально — загрузка в Google Play через `r0adkll/upload-google-play` в треки `internal` → `alpha` → `beta` → `production` (с ручным promote).

#### 8.5.3. `nightly.yml` — ночные интеграционные тесты

- `reactivecircus/android-emulator-runner@v2` с API 24, 30, 35.
- Прогон Compose UI Tests и Maestro flow-тестов.
- Скриншоты на Pixel 6 / Pixel Tablet / Fold.

#### 8.5.4. `lint.yml` — отдельный workflow

ktlint + detekt + Android Lint, чтобы не дублировать прогоны в `ci.yml`.

### 8.6. Pre-commit hook (локально)

`scripts/pre-commit.sh`:
```bash
#!/usr/bin/env bash
set -e
./gradlew ktlintFormat detekt --daemon
./gradlew :core:calculator:test --daemon
```

Установка через `pre-commit` framework или вручную в `.git/hooks/`.

### 8.7. Issue и PR templates

#### `bug_report.md`
```markdown
**Описание / Description**
**Шаги для воспроизведения / Steps to reproduce**
1.
2.
**Ожидаемое поведение / Expected**
**Фактическое поведение / Actual**
**Устройство / Device:** Pixel 6 / Android 14
**Версия PawClock:** 1.0.0
**Скриншоты / Screenshots**
**Logcat (если есть)**
```

#### `species_request.md`
Специальный шаблон для запросов на добавление новых видов: вид, научное название, источник формулы (статья / ветеринарная организация), стадии жизни.

#### `PULL_REQUEST_TEMPLATE.md`
```markdown
## Что сделано
## Связанные issue
Closes #
## Чеклист
- [ ] Я следовал TDD: тесты написаны ДО реализации
- [ ] Новые формулы имеют ссылку на первоисточник в KDoc
- [ ] Покрытие unit-тестами для `:core:calculator` ≥ 90%
- [ ] ktlint и detekt проходят локально
- [ ] Скриншот-тесты обновлены (если затронут UI)
- [ ] CHANGELOG.md обновлён
- [ ] Локализация (ru + en) обновлена
```

### 8.8. CODEOWNERS

Для соло-разработки минимально:
```
* @your-github-username
/core/calculator/ @your-github-username
/docs/adr/ @your-github-username
```

### 8.9. Dependabot

`.github/dependabot.yml`:
```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: "/"
    schedule: { interval: weekly }
    open-pull-requests-limit: 5
  - package-ecosystem: github-actions
    directory: "/"
    schedule: { interval: monthly }
```

### 8.10. Семантическое версионирование

`MAJOR.MINOR.PATCH` по [semver.org](https://semver.org):
- `MAJOR` — несовместимые изменения публичного API care-рекомендаций (актуально при экспорте/импорте JSON).
- `MINOR` — добавление видов или функций без поломки совместимости.
- `PATCH` — багфиксы.

`versionCode` в `build.gradle.kts` генерируется как `MAJOR*10000 + MINOR*100 + PATCH`.

### 8.11. ADR — Architecture Decision Records

В `docs/adr/` фиксируются ключевые решения по шаблону MADR. Стартовые ADR:

1. `0001-jetpack-compose-over-views.md`
2. `0002-multi-module-architecture.md`
3. `0003-tdd-as-required-practice.md`
4. `0004-room-over-sqldelight.md`
5. `0005-no-network-permission.md`
6. `0006-wang-et-al-formula-as-default-for-dogs.md`
7. `0007-conventional-commits.md`

### 8.12. README.md — обязательный минимум

- Бейджи: CI status, Coverage (Codecov), Latest release, License Apache 2.0, Google Play версия, F-Droid версия.
- Скриншоты (3–5 шт. через GitHub-hosted PNG).
- Раздел «Why PawClock» — USP.
- Раздел «Installation» — Google Play + APK from Releases + F-Droid.
- Раздел «Development» — quick start (`./gradlew :app:installDebug`).
- Раздел «Tech stack» — список технологий.
- Раздел «Testing» — ссылка на `docs/TESTING.md`.
- Раздел «Contributing» — ссылка на `docs/CONTRIBUTING.md`.
- Раздел «Privacy» — ссылка на политику.
- Раздел «Sources» — ссылка на §13 спецификации.
- Раздел «License» — Apache 2.0.

### 8.13. Подготовка к F-Droid

После публикации в Google Play:
- Создать `metadata/{packageName}.yml` в форке `fdroiddata`.
- Убедиться, что нет проприетарных зависимостей (нет Firebase, нет ML Kit, нет GMS).
- Reproducible builds: фиксировать версии Gradle и AGP, использовать `--no-build-cache` в release-сборке.

---

## 9. Приватность и безопасность

- **Никакой аналитики**: ни Firebase, ни Crashlytics, ни AppMetrica, ни Google Analytics. Для краш-репортов — опциональный ACRA с локальным сохранением.
- **Никаких разрешений** кроме:
  - `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` — только при выборе фото; лучше — Photo Picker без разрешений.
  - `CAMERA` — опционально.
  - `POST_NOTIFICATIONS` (API 33+) — только при включении напоминаний.
- **Никакого интернета**:
  ```xml
  <uses-permission android:name="android.permission.INTERNET"
      tools:node="remove"/>
  ```
- **Data Safety**: «No data collected», «No data shared».
- **Apache 2.0**, репозиторий публичный.

---

## 10. Доступность

- **TalkBack**: `contentDescription` на всех интерактивных, `Modifier.semantics { heading() }` на заголовках.
- **fontScale**: типографика в `sp`, тесты на `Largest`.
- **Контраст**: только цвета из `MaterialTheme.colorScheme`, AA-контраст гарантирован тональными палитрами M3.
- **Touch targets**: `Modifier.minimumInteractiveComponentSize()` ≥ 48 dp.
- **Animations**: уважаем `Settings → Accessibility → Remove animations`.
- **Ввод даты**: текстовый ввод параллельно с DatePicker dial.

---

## 11. Тестирование и TDD

### 11.1. Философия

**TDD — обязательная практика для модулей `:core:calculator`, `:core:domain` и любой бизнес-логики.**

Принципы:

1. **Red → Green → Refactor.** Цикл всегда начинается с падающего теста. Только после того, как тест красный, пишется минимально достаточный код для его прохождения. Только после зелёного теста — рефакторинг.
2. **Один сценарий — один тест.** Тестовый метод проверяет одно поведение и имеет говорящее имя.
3. **AAA-структура:** Arrange → Act → Assert. Без посторонних вызовов между ними.
4. **F.I.R.S.T.**: Fast (миллисекунды), Independent (нет порядка выполнения), Repeatable (без рандома, без времени, без сети), Self-validating (assertEquals, а не println), Timely (написан перед production-кодом).

### 11.2. Тестовая пирамида

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

### 11.3. Стек тестирования

| Слой | Инструмент |
|---|---|
| Unit (JVM) | **JUnit 5** (Jupiter) + **kotlin.test** assertions |
| Параметризованные тесты | `@ParameterizedTest` + `@MethodSource` / `@CsvSource` |
| Property-based | **Kotest** property testing module |
| Mocking | **MockK** (только когда нужно; fakes предпочтительнее) |
| Flow testing | **Turbine** (Cash App) |
| Coroutines | `kotlinx-coroutines-test`, `runTest`, `StandardTestDispatcher` |
| Room | in-memory database в `androidTest` |
| Compose UI | `createComposeRule()`, `androidx.compose.ui.test` |
| Screenshot tests | **Roborazzi** (Robolectric-based, не требует эмулятора) |
| E2E flow | **Maestro** YAML flows |
| Coverage | **Kover** (Kotlin-нативный, лучше JaCoCo для Compose) |
| Static analysis | **detekt** + ktlint + Android Lint |

### 11.4. Цели покрытия

| Модуль | Coverage target |
|---|---|
| `:core:calculator` | **≥ 95%** (бизнес-критично) |
| `:core:domain` | ≥ 90% |
| `:core:database` | ≥ 80% (integration tests) |
| `:feature:*` ViewModels | ≥ 80% |
| `:feature:*` Composables | ≥ 60% (через screenshot tests) |
| `:app` | без жёсткой цели |

Покрытие проверяется в CI через `kover` и публикуется в **Codecov**. PR с падением coverage > 1% блокируется.

### 11.5. TDD-цикл на конкретном примере: формула для собак

#### Шаг 1 — Red: пишем падающий тест

`core/calculator/src/test/kotlin/app/pawclock/calculator/DogAgeCalculatorTest.kt`:

```kotlin
class DogAgeCalculatorTest {

    private val calculator = DogAgeCalculator()

    @Test
    fun `Wang formula returns 31 human years for 1 year old dog`() {
        // 16 · ln(1) + 31 = 31
        val result = calculator.toHumanYears(
            ageInYears = 1.0,
            method = CalculationMethod.EPIGENETIC
        )
        assertEquals(31.0, result, absoluteTolerance = 0.1)
    }
}
```

Запускаем `./gradlew :core:calculator:test` — ошибка компиляции, классов не существует. **Red.**

#### Шаг 2 — Green: минимальная реализация

`core/calculator/src/main/kotlin/app/pawclock/calculator/DogAgeCalculator.kt`:

```kotlin
enum class CalculationMethod { EPIGENETIC, SIZE_BASED }

class DogAgeCalculator {
    fun toHumanYears(ageInYears: Double, method: CalculationMethod): Double {
        return 31.0  // минимум для прохождения теста
    }
}
```

Тест проходит. **Green.**

#### Шаг 3 — расширяем тест-набор

```kotlin
@ParameterizedTest
@CsvSource(
    "1.0, 31.0",
    "2.0, 42.1",
    "5.0, 56.7",
    "10.0, 67.8",
    "12.0, 70.7"
)
fun `Wang formula returns expected human years`(dogAge: Double, expectedHuman: Double) {
    val result = calculator.toHumanYears(dogAge, CalculationMethod.EPIGENETIC)
    assertEquals(expectedHuman, result, absoluteTolerance = 0.2)
}
```

Падает на 2.0, 5.0, 10.0, 12.0. **Red.**

#### Шаг 4 — настоящая реализация

```kotlin
class DogAgeCalculator {
    fun toHumanYears(ageInYears: Double, method: CalculationMethod): Double {
        require(ageInYears > 0) { "Age must be positive" }
        return when (method) {
            CalculationMethod.EPIGENETIC -> 16.0 * ln(ageInYears) + 31.0
            CalculationMethod.SIZE_BASED -> error("not implemented yet")
        }
    }
}
```

**Green.**

#### Шаг 5 — Refactor

Извлекаем константы:
```kotlin
internal const val WANG_COEFFICIENT = 16.0
internal const val WANG_OFFSET = 31.0
```

Добавляем KDoc со ссылкой на источник:
```kotlin
/**
 * Returns human-year equivalent of the dog's age.
 *
 * Source for [CalculationMethod.EPIGENETIC]:
 * Wang T., Tsui B., Kreisberg J.F., et al.
 * "Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling
 *  of the DNA Methylome". Cell Systems, 2020; 11(2):176-185.
 * DOI: 10.1016/j.cels.2020.06.006
 */
fun toHumanYears(ageInYears: Double, method: CalculationMethod): Double { ... }
```

Тесты по-прежнему зелёные. **Refactor complete.**

#### Шаг 6 — Edge cases (тоже TDD)

```kotlin
@Test
fun `throws on zero or negative age`() {
    assertFailsWith<IllegalArgumentException> {
        calculator.toHumanYears(0.0, CalculationMethod.EPIGENETIC)
    }
    assertFailsWith<IllegalArgumentException> {
        calculator.toHumanYears(-1.0, CalculationMethod.EPIGENETIC)
    }
}

@Test
fun `handles 7 weeks old puppy via piecewise extension`() {
    // Wang формула определена для age >= 1 года.
    // Для возрастов < 1 года используем кусочное расширение.
    val sevenWeeks = 7.0 / 52.0
    val result = calculator.toHumanYears(sevenWeeks, CalculationMethod.EPIGENETIC)
    assertTrue(result in 8.0..10.0, "7-week-old should map to ~9 human years")
}
```

Цикл повторяется до полного покрытия известных кейсов.

### 11.6. Property-based testing (Kotest)

Для математических свойств формул:

```kotlin
class DogAgeCalculatorPropertyTest : StringSpec({
    val calc = DogAgeCalculator()

    "human age is monotonically increasing in dog age" {
        checkAll(Arb.double(0.1, 20.0), Arb.double(0.1, 20.0)) { a, b ->
            if (a < b) {
                calc.toHumanYears(a, EPIGENETIC) shouldBeLessThan
                    calc.toHumanYears(b, EPIGENETIC)
            }
        }
    }

    "result is always positive for positive input" {
        checkAll(Arb.double(0.01, 30.0)) { age ->
            calc.toHumanYears(age, EPIGENETIC) shouldBeGreaterThan 0.0
        }
    }
})
```

### 11.7. Тестирование ViewModel с Turbine

```kotlin
@Test
fun `selecting species updates state with available subcategories`() = runTest {
    val viewModel = PetEditorViewModel(SavedStateHandle(), FakeSpeciesRepo())

    viewModel.state.test {
        assertEquals(PetEditorState.Initial, awaitItem())

        viewModel.onEvent(SelectSpecies(Species.Dog))

        val nextState = awaitItem()
        assertEquals(Species.Dog, nextState.selectedSpecies)
        assertEquals(
            listOf(DogSize.Toy, DogSize.Small, DogSize.Medium, DogSize.Large, DogSize.Giant),
            nextState.availableSubcategories
        )
    }
}
```

### 11.8. Тестирование Compose UI

```kotlin
class PetsListScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun shows_empty_state_when_no_pets() {
        composeTestRule.setContent {
            PawClockTheme {
                PetsListScreen(uiState = PetsListState.Empty, onEvent = {})
            }
        }

        composeTestRule
            .onNodeWithText("Добавьте первого питомца")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Добавить питомца")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
```

### 11.9. Screenshot tests с Roborazzi

```kotlin
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "ru-rUA-w411dp-h891dp-xxhdpi")
class PetDetailScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun pet_detail_dog_senior_stage() {
        composeTestRule.setContent {
            PawClockTheme(darkTheme = false) {
                PetDetailScreen(uiState = PetDetailFixtures.dogSeniorRu)
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

Эталонные PNG хранятся в `src/test/snapshots/`, при PR Roborazzi сравнивает с пиксельной точностью и публикует diff как artifact.

### 11.10. E2E flow на Maestro

`maestro/create_first_pet.yaml`:
```yaml
appId: app.pawclock
---
- launchApp
- assertVisible: "Добавьте первого питомца"
- tapOn: "Добавить питомца"
- inputText: "Барсик"
- tapOn: "Вид"
- tapOn: "Кошка"
- tapOn: "Дата рождения"
- tapOn: "2020"
- tapOn: "5"
- tapOn: "15"
- tapOn: "OK"
- tapOn: "Сохранить"
- assertVisible: "Барсик"
- assertVisible: "Senior"
```

### 11.11. Локализационные тесты

```kotlin
@Test
fun `Russian pluralization is correct for cat age`() {
    val cases = mapOf(
        1 to "1 год",
        2 to "2 года",
        5 to "5 лет",
        21 to "21 год",
        22 to "22 года",
        25 to "25 лет"
    )
    cases.forEach { (years, expected) ->
        assertEquals(expected, formatAge(years, Locale("ru")))
    }
}
```

### 11.12. Mutation testing (опционально, для зрелого проекта)

Через **Pitest** для `:core:calculator` — гарантирует, что тесты не только проходят, но и реально проверяют логику (мутации формул должны ловиться).

### 11.13. Контроль качества тестов

- **Test naming**: `methodName_condition_expectedResult` или backticked DSL (`fun \`returns 31 for 1 year old dog\``).
- **Никаких `Thread.sleep`** — только `runTest` и `TestDispatcher`.
- **Никаких реальных дат** — `Clock.fixed(...)` в тестах.
- **Никаких сетевых вызовов в unit-тестах** — это автоматически гарантируется отсутствием `INTERNET` permission и слоистой архитектурой.
- **Code review (даже соло)**: перед squash-merge перечитать PR через сутки.

---

## 12. План развития (Roadmap)

### 12.1. MVP (v1.0.0) — 2 месяца разработки

- Все 12 групп видов с формулами и стадиями.
- Сохранение профилей (Room).
- Текстовые рекомендации по уходу.
- Material You, тёмная/светлая темы.
- ru + en.
- Экспорт/импорт JSON.
- ≥ 95% coverage в `:core:calculator`.
- Релиз на GitHub + Google Play (internal testing → production).

### 12.2. v1.1 (3 месяца после v1.0)

- Виджеты Glance (возраст + дни до ДР).
- Локали de, es.
- Roborazzi-скриншоты в CI как блокирующий чек.
- F-Droid публикация.

### 12.3. v2.0 (6–12 мес.)

- Локальные напоминания (WorkManager).
- Журнал здоровья (вакцинации, вес, заметки).
- Графики Vico / compose-charts.
- Wear OS.
- Локали zh-CN, pt-BR, fr.
- Tablet/foldable enhancements (`material3-adaptive` stable).

---

## 13. Метрики успеха

### 13.1. KPI (через 6 мес. после релиза)

- ≥ 10 000 установок.
- Средний рейтинг в Google Play ≥ 4.6.
- Crash-free sessions ≥ 99.5%.
- Удержание D30 ≥ 25%.
- ≥ 60% пользователей добавили ≥ 1 профиль.
- Среднее число питомцев на пользователя ≥ 1.3.

### 13.2. Метрики качества кода (через GitHub)

- CI-стабильность (success rate `main`) ≥ 95%.
- Среднее время прогона `ci.yml` < 6 минут.
- Coverage `:core:calculator` ≥ 95%, `:core:domain` ≥ 90%.
- Mean time to merge PR < 48 часов (для соло — после собственного отстаивания).
- 0 проблем `error`-уровня в detekt и Android Lint.

### 13.3. Бенчмарки

- ANR rate < 0.1%.
- Cold startup < 500 мс (Pixel 6).
- APK ≤ 8 МБ.

---

## 14. Источники и литература

### Собаки
- Wang T. et al. *Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling of the DNA Methylome*. Cell Systems, 2020, 11(2):176-185.
- AAHA. *2019 AAHA Canine Life Stage Guidelines*.
- AKC. *Your Dog's Age in Human Years*.
- McMillan K.M. et al. *Longevity of companion dog breeds*. Scientific Reports 14, 531 (2024).
- Purina. *Your Dog's Age in Human Years*.

### Кошки
- AAHA/AAFP. *2021 Feline Life Stage Guidelines*. J Feline Med Surg, 23(3): 211–233.
- Ray M. et al. *2021 AAFP Feline Senior Care Guidelines*.

### Кролики
- Oxbow. *Rabbit Lifespan and Life Stages*.
- House Rabbit Society guidelines.
- Dutta S., Sengupta P. *Mapping the Age of Laboratory Rabbit Strains to Human*. PMC8000163.

### Хомяки, морские свинки
- PetMD. *How Long Do Hamsters Live?*
- Royal Veterinary College VetCompass hamster study.
- Oxbow. *Guinea Pig Lifespan and Life Stages*.

### Крысы и мыши
- Sengupta P. *The Laboratory Rat: Relating Its Age With Human's*. Int J Prev Med, 2013; 4(6): 624–630.
- Dutta S., Sengupta P. *Men and Mice: Relating Their Ages*. Life Sciences, 2016; 152: 244–248.

### Хорьки
- *The Senior Ferret*. PMC7129291.
- Oxbow Ferret Life Stages.

### Птицы
- AAV. *Care for Senior Parrots*.
- Lafeber Vet basic info sheets.

### Рептилии
- PetPlace, Reptile Centre, A-Z Animals lifespan sheets.

### Лошади
- AAEP Vaccination Guidelines, Senior Horse Care.
- PetMD (Kaela Schraer DVM). *Horse Lifespan*.

### Рыбы
- PetMD. *How Long Do Fish Live?*
- Kodama Koi Farm. Koi Life Stages.

### Технические
- Android Developers. *Material Design 3 in Compose*.
- Android Developers. DataStore, Room, Navigation Compose docs.
- Material Design 3 — m3.material.io.
- Composables.com Android distribution stats (декабрь 2025).
- Conventional Commits 1.0 — conventionalcommits.org.
- Semantic Versioning 2.0 — semver.org.
- Roborazzi — github.com/takahirom/roborazzi.
- Maestro — maestro.mobile.dev.

---

*Документ носит исключительно ознакомительный характер. Все рекомендации по уходу за животными должны быть согласованы с лицензированным ветеринарным врачом.*

---

**Версия спецификации:** 1.1
**Дата:** 27 мая 2026
**Изменения с v1.0:** добавлены §0 (название проекта, репозиторий), §8 (GitHub workflow и CI/CD), расширен §11 (Тестирование и TDD с полным циклом Red-Green-Refactor).
