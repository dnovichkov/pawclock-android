# ARCHITECTURE

Этот документ описывает структуру кодовой базы PawClock, выбранный архитектурный
стиль и правила зависимостей между модулями. Документ — фотография архитектуры
на момент окончания Plan 1 (foundation + Dog/Cat MVP) и обновляется при добавлении
новых модулей и слоёв.

Ключевые архитектурные решения зафиксированы в `docs/adr/` (ADR-0001..0007).
Этот документ агрегирует их в одной читаемой картинке.

## Высокоуровневая диаграмма модулей

```
              ┌─────────────────────────────────────────────┐
              │                   :app                      │
              │  PawClockApplication, MainActivity, NavHost │
              │  Hilt root, DI-modules, Manifest, Themes    │
              └────────────┬────────────────────────────────┘
                           │ depends on
        ┌──────────────────┼─────────────────────────────────┐
        ▼                  ▼                                 ▼
┌────────────────┐ ┌─────────────────┐         ┌──────────────────────┐
│ :feature:pets  │ │ :feature:editor │   ...   │ :feature:settings    │
│ :feature:      │ │                 │         │                      │
│   quickcalc    │ │                 │         │                      │
└────────┬───────┘ └────────┬────────┘         └──────────┬───────────┘
         │ depends on       │                             │
         ▼                  ▼                             ▼
        ┌──────────────────────────────────────────────────────┐
        │                  :core:domain                        │
        │  UseCases, port-interfaces (PetRepository,           │
        │  SettingsReader, LocaleApplier, CareRepository)      │
        └──┬─────────────────┬────────────────────────┬───────┘
           │                 │                        │
           ▼                 ▼                        ▼
  ┌─────────────────┐ ┌──────────────┐ ┌─────────────────────────┐
  │ :core:model     │ │ :core:       │ │  Implementations live   │
  │ Pet, Species,   │ │ calculator   │ │  in :app (data layer):  │
  │ LifeStage,      │ │ Wang / size  │ │   RoomPetRepository     │
  │ CalculationMtd, │ │ table /      │ │   DataStoreSettings     │
  │ ThemeMode,      │ │ AAFP +       │ │     Reader              │
  │ CareRec.        │ │ life-stages  │ │   AndroidAssetSource    │
  └─────────────────┘ │ + property   │ │   LocaleHelper          │
                      │ tests        │ └─────────────────────────┘
                      └──────────────┘             │
                                                   ▼
                                ┌───────────────────────────────┐
                                │ :core:database (Room)         │
                                │ :core:datastore (DataStore)   │
                                └───────────────────────────────┘

       :core:designsystem ─── используется всеми feature-модулями
                              (Material 3, PawClockTheme, PawClockCard,
                               LifeStageChip, AgeBigCard, Typography)
```

Полный список модулей (12 шт. по §7.3 спецификации):

| Слой | Модуль | Назначение | Android-зависимости? |
|---|---|---|---|
| app | `:app` | Точка входа, DI-граф, NavHost, Manifest | да |
| presentation | `:feature:pets` | Список питомцев + детальный экран | да (Compose) |
| presentation | `:feature:editor` | Создание/редактирование питомца | да (Compose) |
| presentation | `:feature:quickcalc` | Одноразовый расчёт без сохранения | да (Compose) |
| presentation | `:feature:settings` | Настройки + About | да (Compose) |
| domain | `:core:domain` | UseCases + port-интерфейсы | нет |
| domain | `:core:calculator` | Pure-Kotlin формулы и стадии жизни | нет |
| domain | `:core:model` | Доменные модели (Pet, Species, LifeStage) | нет |
| data | `:core:database` | Room — entities, DAO, mappers, migrations | да |
| data | `:core:datastore` | DataStore Preferences (AppSettings) | да |
| ui-shared | `:core:designsystem` | Material You theme, общие composables | да (Compose) |
| testing | `:core:testing` | Зарезервирован для общих fixtures/fakes (Plan 2) | планируется нет |

## Clean Architecture слои

PawClock следует упрощённой clean architecture / onion architecture с тремя
концептуальными слоями (а не пяти, как в каноне Боба Мартина — для соло-проекта
этого достаточно):

### Domain (центр, без зависимостей)

- **`:core:model`** — pure data классы. Не знают про Room, DataStore, Compose.
  Pet, Species, LifeStage, CareRecommendation, CalculationMethod, ThemeMode.
- **`:core:calculator`** — формулы (Wang 2020, AKC/AAHA 2019 size table,
  AAHA/AAFP 2021) + life-stage калькуляторы. Pure-Kotlin JVM-модуль, тесты
  выполняются за миллисекунды без эмулятора.
- **`:core:domain`** — UseCases (`CalculatePetAgeUseCase`, `GetPetsUseCase`,
  `SavePetUseCase`, `DeletePetUseCase`, `GetCareRecommendationsUseCase`,
  `GetPetByIdUseCase`) и **port-интерфейсы** для всех инфраструктурных
  зависимостей: `PetRepository`, `SettingsReader`, `LocaleApplier`,
  `CareRepository`, `AssetSource`.

Domain-слой не импортирует ни одного Android-API. Это enforces'я физически тем,
что `:core:model`, `:core:calculator` и `:core:domain` — это `kotlin("jvm")`
модули, а не `com.android.library`.

### Data (адаптеры)

Реализации port-интерфейсов из domain. Живут в `:app/data/` (а не в
отдельном `:core:data` модуле, потому что каждая реализация — тонкий адаптер
≤30 строк, и выделять её в отдельный модуль = overkill для текущего размера
проекта):

- `RoomPetRepository` (`:app/data/pet/`) — реализует `PetRepository`,
  делегирует в `PetDao` через `PetMapper`.
- `DataStoreSettingsReader` (`:app/data/settings/`) — реализует
  `SettingsReader`, оборачивает `SettingsRepository.observe()`.
- `AndroidAssetSource` (`:app/data/care/`) — реализует `AssetSource`,
  оборачивает `AssetManager.open()`.
- `LocaleHelper` (`:app/locale/`) — реализует `LocaleApplier`, делегирует
  в `AppCompatDelegate.setApplicationLocales`.

Технологии:

- **Room** (`:core:database`) для профилей питомцев. Schema export включён,
  миграции через `Migration` API (scaffold готов в `Migrations.kt`,
  реальные миграции — с версии 2 в Plan 2).
- **DataStore Preferences** (`:core:datastore`) для настроек.
- **Assets** (`:app/src/main/assets/care/`) для care-рекомендаций — read-only
  справочные JSON-файлы (см. ADR-0005, никаких сетевых запросов).

### Presentation (UI)

Каждый `:feature:*` модуль — Compose + Material 3, архитектура MVI (см. ниже).
Использует `:core:designsystem` для общих composables и темы.

Навигация — Navigation Compose 2.8+ с **typesafe routes** (`@Serializable
sealed class Route`). Маршруты живут в `:app/navigation/Route.kt`.

## MVI (Model-View-Intent)

Внутри каждого `:feature:*` модуля экраны построены по MVI с однонаправленным
потоком данных:

```
   UI (Composable) ──── send Event ────► ViewModel
        ▲                                    │
        │                                    │
        │                                    ▼
        │                              handleEvent()
        │                                    │
        └───── collect StateFlow<State> ─────┘
```

Конкретная реализация PawClock:

- **State** — `sealed interface` или `data class`, immutable. Пример:
  `PetsListState` (Loading / Empty / Success / Error),
  `QuickCalcState.Form` / `QuickCalcState.Success`,
  `PetEditorState` (data class с полями формы).
- **Event** — `sealed interface` с конкретными data class'ами. Пример:
  `PetEditorEvent.SelectSpecies(Species)`, `PetEditorEvent.SetName(String)`,
  `PetEditorEvent.Save`, `PetEditorEvent.ConsumeSaveResult`.
- **ViewModel** — `@HiltViewModel`, экспонирует `StateFlow<State>` через
  `stateIn(WhileSubscribed(5_000), initialValue = ...)`. События принимает
  через `fun handleEvent(event: Event)` или конкретные методы (для
  простоты в `PetEditorViewModel` сделан единый dispatcher `handleEvent`,
  в `QuickCalcViewModel` — то же).
- **Composable** — stateless `*Content(state, onEvent)` отделён от stateful
  `*Screen(viewModel = hiltViewModel())`. Это позволяет тестировать UI без
  Hilt-setup'а: в `*ScreenTest.kt` подаётся произвольный state.

Навигация **не** передаётся в ViewModel — Compose-функции принимают
`onBack: () -> Unit`, `onPetClick: (Long) -> Unit` и т.п. колбэки от вышестоящего
NavHost. Это упрощает тестирование и соответствует Compose Navigation guidance.

## Правила зависимостей (onion / dependency rule)

**Зависимости направлены только внутрь луковицы**: presentation → domain → model.

| Слой | Может зависеть от | НЕ может зависеть от |
|---|---|---|
| `:core:model` | (ничего) | — |
| `:core:calculator` | `:core:model` | `:core:domain`, `:feature:*`, `:app` |
| `:core:domain` | `:core:model`, `:core:calculator` | `:core:database`, `:core:datastore`, `:feature:*`, `:app` |
| `:core:database` | `:core:model` | `:core:domain`, `:feature:*` |
| `:core:datastore` | `:core:model` | `:core:domain`, `:feature:*` |
| `:core:designsystem` | `:core:model` (для LifeStageChip enum mapping) | `:core:domain`, `:feature:*` |
| `:feature:*` | `:core:domain`, `:core:model`, `:core:designsystem`, `:core:calculator` | другие `:feature:*` |
| `:app` | всё выше | — |

Ключевые следствия:

- **`:core:domain` НЕ зависит от `:core:database` / `:core:datastore`**. Вместо
  прямой зависимости domain объявляет port-интерфейс (`PetRepository`,
  `SettingsReader`), а реализации этих интерфейсов живут в `:app` и связываются
  через Hilt `@Binds`. Это позволяет domain-слою оставаться pure-Kotlin JVM
  модулем (тесты без Robolectric/AndroidX).
- **Feature-модули не зависят друг от друга**. Если двум фичам нужен общий
  компонент — он переезжает в `:core:designsystem` (UI) или `:core:domain`
  (логика).
- **`:core:calculator` и `:core:model` — pure-Kotlin JVM модули** (`kotlin("jvm")`,
  не `com.android.library`). Это enforces'ся типом Gradle-плагина и физически
  блокирует случайный импорт `android.*` API.
- **Hilt @Binds-модули живут в `:app/data/*/di/`** — не в `:core:*` модулях,
  потому что биндинг конкретной реализации к port-интерфейсу — это
  composition-root concern, а composition root — это `:app`.

## Чем НЕ является эта архитектура

Чтобы не вводить в заблуждение тех, кто привык к более тяжёлым стилям:

- **Нет отдельного `:core:data` модуля.** Реализации репозиториев — тонкие
  адаптеры в `:app/data/`, не отдельный Gradle-модуль. Если объём data-слоя
  вырастет (Plan 2 добавит JSON export/import + ещё ~10 видов = больше
  адаптеров), модуль будет выделен.
- **Нет MVVM-style "Repository pattern" повсюду.** Repository есть только
  там, где это даёт ценность: для абстракции хранилища (Pet) и для
  тестируемости (CareRepository, SettingsRepository). Не каждая read-only
  asset-загрузка обёрнута в Repository.
- **Нет use-case'а на каждый клик кнопки.** UseCases выделены только для
  операций с реальной доменной логикой (`CalculatePetAgeUseCase`,
  `SavePetUseCase` с валидацией). UI-state менеджмент — целиком в ViewModel'е.
- **Нет mapper'ов между слоями presentation ↔ domain.** Доменные модели
  (`Pet`, `LifeStage`) используются напрямую в Composable'ах. Мапперы есть
  только между data ↔ domain (`PetMapper`: Pet ↔ PetEntity), потому что
  PetEntity содержит Room-аннотации, которые не должны утекать в domain.

## Связь с ADR

| ADR | Что зафиксировано | Где отражено в коде |
|---|---|---|
| 0001 | Jetpack Compose, no XML Views | Все `:feature:*` + `:core:designsystem` |
| 0002 | Multi-module structure | `settings.gradle.kts`, эта диаграмма |
| 0003 | TDD обязателен для `:core:calculator` и `:core:domain` | `koverVerify` rules с порогами 95% / 90% |
| 0004 | Room over SQLDelight | `:core:database` |
| 0005 | No INTERNET permission | `AndroidManifest.xml`: `<uses-permission tools:node="remove">` |
| 0006 | Wang 2020 default for dogs | `AppSettings.defaultCalculationMethod = EPIGENETIC` |
| 0007 | Conventional Commits | `docs/CONTRIBUTING.md`, pre-commit hook |

## Дальнейшее развитие

- **Plan 2**: добавление 10 видов = 10 новых калькуляторов в `:core:calculator`
  по тому же TDD-паттерну. Структура модулей не меняется.
- **Plan 3 (v2.0)**: уведомления и Glance-виджеты могут потребовать новый
  модуль `:feature:reminders` и/или `:core:notifications`.
- **`:core:testing`** — сейчас пустой модуль-заглушка. Будет наполнен общими
  fakes (FakePetRepository из feature/pets/test/fakes уже есть, при росте
  числа дубликатов мигрирует сюда).
