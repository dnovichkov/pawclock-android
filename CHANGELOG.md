# Changelog

Все значимые изменения в PawClock документируются в этом файле.

Формат соответствует [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
проект следует [семантическому версионированию](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

_Пока пусто. Следующие изменения после v1.0.0 попадут сюда._

## [1.0.0] - 2026-06-01

Полный MVP v1.0 (§12.1): все 12 групп животных + миграция данных между устройствами (§3.5).
Дата релиза проставляется при тегировании `v1.0.0`.

### Added

#### Plan 2 — все виды + export/import

- **10 новых калькуляторов возраста** (строгий TDD, KDoc с первоисточником у каждой формулы):
  Rabbit (House Rabbit Society/AVMA), Hamster (RVC), GuineaPig (Oxbow), Rat (Sengupta 2013),
  Mouse (Dutta & Sengupta 2016), Ferret (PMC), Bird (AAV scalar), Reptile (PetPlace scalar),
  Horse (AAEP 3-фаза), Fish (PetMD scalar). Все 12 видов теперь `isImplemented = true`.
- **Strategy-pattern диспатч**: sealed `AgeCalculator` / `LifeStageCalculator` + фабрики
  `forSpecies()`; `CalculatePetAgeUseCase` диспатчит без god-object `when` (см. ADR-0008).
- **Подкатегории видов**: `RabbitSize`, `HamsterType`, `BirdType`, `ReptileType`, `FishType`,
  `HorseType` со стабильными `id` для сериализации; расширенный sealed `LifeStage` (12 видов).
- **Property-based tests** (Kotest) для всех новых калькуляторов: монотонность, позитивность,
  непрерывность/bounded-jump на стыках кусочных формул.
- **Export/Import** (§3.5): `ExportPetsUseCase` / `ImportPetsUseCase`, JSON (версионируемая схема
  `schema_version = 1`) и CSV (RFC 4180); MERGE/REPLACE стратегии, dry-run preview (см. ADR-0009).
- **Settings UI** для Export/Import через Storage Access Framework (`ACTION_CREATE_DOCUMENT` /
  `ACTION_OPEN_DOCUMENT`, Activity Result API) с выбором формата и стратегии.
- **Care recommendations placeholder** для всех 10 новых видов (96 JSON-файлов ru/en с TODO-контентом
  + обязательный дисклеймер §3.3); трекер `docs/CARE_CONTENT.md` + `scripts/verify-care-assets.sh`.
- **Векторные иконки видов** (`ic_species_*`, моно-линейные) + `SpeciesIcon` composable — замена
  emoji-плейсхолдеров в PetCard / PetEditor / QuickCalculator.
- **UI на все 12 видов**: PetEditor и QuickCalculator поддерживают все виды и их подкатегории без
  хардкодов (`Species.implemented()`).
- **Локализация** ru/en для всех новых видов, подкатегорий, стадий жизни и строк Export/Import.
- **Maestro E2E**: `quick_calc_rabbit/bird/horse`, `export_import_roundtrip`; обновлены flow Plan 1.
- **2 новых ADR**: ADR-0008 (AgeCalculator sealed interface), ADR-0009 (export/import schema versioning).

#### Plan 1 — foundation + Dog/Cat MVP

- Foundation проекта: Gradle multi-module skeleton (12 модулей по §7.3 спецификации).
- `:core:calculator` — формулы расчёта возраста для собак (Wang 2020 + AKC/AAHA 2019)
  и кошек (AAHA/AAFP 2021) с покрытием тестами ≥ 95%.
- `:core:model` — доменные модели (Pet, Species, LifeStage, CalculationMethod, ThemeMode).
- `:core:database` — Room persistence для профилей питомцев с миграциями.
- `:core:datastore` — DataStore Preferences для настроек приложения.
- `:core:domain` — UseCases с TDD-разработкой (≥ 90% coverage).
- `:core:designsystem` — Material You theme + типографика + общие composables
  (PawClockCard, LifeStageChip, AgeBigCard).
- `:feature:pets` — PetsList + PetDetail экраны.
- `:feature:editor` — PetEditor для создания/редактирования питомца.
- `:feature:quickcalc` — Quick Calculator для одноразового расчёта без сохранения.
- `:feature:settings` — Settings + About экраны.
- Care recommendations для Dog/Cat по стадиям жизни (placeholder content).
- Локализация ru (default) + en с plurals по CLDR-правилам.
- LocaleConfig для Android 13+ in-app locale picker.
- 7 стартовых ADR (Jetpack Compose, multi-module, TDD, Room, no-internet,
  Wang formula default, Conventional Commits).
- 4 GitHub Actions workflow (ci.yml, release.yml, nightly.yml, lint.yml).
- Dependabot configuration с группировкой Kotlin/Compose/AndroidX/test deps.
- Issue + PR templates с TDD-чеклистом.
- CODEOWNERS с защитой критичных путей (calculator, model, care assets,
  AndroidManifest, libs.versions.toml).
- Compose UI tests + Maestro E2E flows (create_first_pet, quick_calc_dog).
- 5 documentation files: ARCHITECTURE, TESTING, CONTRIBUTING, RELEASE, README.
- LICENSE (Apache 2.0).

### Security

- Запрет INTERNET permission через `<uses-permission tools:node="remove">`
  (см. ADR-0005). Data Safety на Google Play — "No data collected".

[Unreleased]: https://github.com/dnovichkov/pawclock-android/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/dnovichkov/pawclock-android/releases/tag/v1.0.0
