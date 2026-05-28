# Changelog

Все значимые изменения в PawClock документируются в этом файле.

Формат соответствует [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
проект следует [семантическому версионированию](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

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
- Care recommendations для Dog/Cat по стадиям жизни (placeholder content,
  заменится реальным научным контентом в Plan 2).
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

[Unreleased]: https://github.com/dnovichkov/pawclock-android/commits/main
