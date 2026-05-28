# 0005. No INTERNET permission — privacy-first architecture

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: privacy, security, dependencies, store-listing

## Context and Problem Statement

PawClock рассчитывает возраст питомца локально из birthDate и подкатегории. Сетевое взаимодействие не требуется ни для одной функции:
- Care recommendations — в `assets/care/`, bundled.
- Аналитика — отсутствует (spec §9).
- Crash-reports — опциональный ACRA с локальным сохранением.
- Шеринг результата — через системный `ACTION_SEND` (не сетевой запрос).

При этом transitively-pulled libraries (например, default WorkManager или старые версии Hilt) могут добавлять `<uses-permission android:name="android.permission.INTERNET"/>` через merged manifest.

## Decision Drivers

- **Privacy promise**: Data Safety listing → "No data collected, no data shared" (spec §9). Это маркетинговый USP — отличие от 90% pet-apps в Google Play.
- **F-Droid compatibility**: F-Droid публично помечает приложения с INTERNET permission как "Network access" — это снижает trust score.
- **Безопасность**: нет network = нет remote code execution surface через misconfigured TLS.
- **Регуляторика**: GDPR/PD compliance тривиальна, если данные не покидают устройство.

## Considered Options

1. **Явно удалить INTERNET через `tools:node="remove"`** (наш выбор) — гарантия, что даже transitive deps не добавят permission.
2. **Полагаться на отсутствие явного objявления** — рисковано из-за merged manifest.
3. **Разрешить INTERNET для будущей analytics** — против spec §9.

## Decision Outcome

Chosen option: **Явно удалить INTERNET permission**, через:

```xml
<uses-permission
    android:name="android.permission.INTERNET"
    tools:node="remove" />
```

в `:app/src/main/AndroidManifest.xml`.

Дополнительные правила:

1. **No Firebase, no Crashlytics, no Google Analytics, no AppMetrica** — никаких SDK с сетевым доступом (см. spec §9).
2. **Запретить в CI**: после `assembleDebug` проверять `aapt dump permissions app-debug.apk` → должен возвращать пусто или только локальные permissions. (Реализация — следующая итерация в release.yml.)
3. **Все assets — bundled**: care/, fonts/, images/.
4. **Опциональный ACRA**: использовать `org.acra:acra-mail` (mailto) или `acra-disk` — никаких HTTP-collector'ов.

### Positive Consequences

- Data Safety форма в Google Play Console заполняется как "No data collected" → меньше friction при review.
- F-Droid build server примет приложение без warning'ов на network access.
- Любая будущая зависимость с transitive INTERNET permission поломает сборку → мы заметим её до production.

### Negative Consequences

- Невозможно добавить crash reporting через Firebase / Sentry (только локальный ACRA или Logcat). Митигация: bug-reports собираются через GitHub Issues + опциональный share `Logcat` из приложения.
- Невозможно реализовать "поделиться в социальные сети" через embedded API — только через `ACTION_SEND` intent. Это OK по UX.
- Любые online-recommendations (например, "find a vet near you") — НЕДОПУСТИМЫ в этом приложении. Митигация: это явное продуктовое решение, отражённое в `docs/specs/pawclock-specification.md` §1.3 (USP).

## Pros and Cons of the Options

### Option 1: Явное удаление (chosen)

- Good: bullet-proof, проверяется в build-output, защищает от transitive deps.
- Bad: один лишний XML-блок в manifest (minor).

### Option 2: Просто не объявлять

- Good: проще конфигурация.
- Bad: transitive deps могут добавить permission через manifest merger → silent regression.

### Option 3: Разрешить INTERNET

- Good: возможность Firebase / Sentry / Maps.
- Bad: ломает USP (см. spec §1.3), Data Safety становится сложной, F-Droid trust снижается.

## Links

- spec §9 (приватность), §1.3 (USP)
- [Android — Data Safety form](https://support.google.com/googleplay/android-developer/answer/10787469)
- [F-Droid Anti-Features — NonFreeNet](https://f-droid.org/docs/Anti-Features/)
- Related: [ADR-0004](./0004-room-over-sqldelight.md) (всё локально)
