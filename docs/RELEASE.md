# RELEASE

Документ описывает процесс выпуска новой версии PawClock: семвер,
versionCode, тегирование, автоматизированная сборка через `release.yml`
workflow, publish в Google Play. F-Droid — отдельный путь, описан в конце.

## Семантическое версионирование (§8.10)

PawClock следует [semver.org](https://semver.org) `MAJOR.MINOR.PATCH`:

| Bump | Когда |
|---|---|
| **MAJOR** | Несовместимые изменения публичного API care-рекомендаций (актуально для JSON export/import format). Триггерится `BREAKING CHANGE:` footer'ом или `feat!:` / `fix!:` префиксом в Conventional Commits. |
| **MINOR** | Добавление новых видов животных, новых калькуляторов, новых экранов — без поломки совместимости. Триггерится `feat:` коммитами с момента последнего тега. |
| **PATCH** | Багфиксы. Триггерится `fix:` коммитами. |

### Examples

| Изменение | Версионный bump |
|---|---|
| Добавление кролика | 1.0.0 → 1.1.0 (MINOR) |
| Исправление округления Cat formula | 1.1.0 → 1.1.1 (PATCH) |
| Смена JSON-формата экспорта (для импорта старого нужна миграция) | 1.1.1 → 2.0.0 (MAJOR) |
| Bump compose-bom | 1.0.0 → 1.0.0 (нет bump'а — chore не триггерит релиз) |

### versionCode formula

`versionCode` в `app/build.gradle.kts` генерируется по формуле:

```kotlin
versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
```

Примеры:

| versionName | versionCode |
|---|---|
| 1.0.0 | 10000 |
| 1.0.1 | 10001 |
| 1.1.0 | 10100 |
| 1.2.3 | 10203 |
| 2.0.0 | 20000 |

Эта формула монотонно возрастает с каждым валидным семвер-bump'ом (Google Play
требует строго возрастающие versionCode для каждого upload'а), при этом
читаема — по числу видно версию.

**Ограничения формулы:**

- MINOR < 100 и PATCH < 100 — после 99 нужно bump'ить MAJOR. На практике
  для PawClock это не блокирующее ограничение: 100 minor releases — это
  годы работы.
- Если когда-нибудь нужно будет hot-fix'ить past-release ветку, formula
  потребует ручной корректировки (например, +1000 offset для hot-fix
  series). Сейчас этого не нужно — `main` всегда single track.

## Процесс релиза (happy path)

### 1. Подготовка коммитов

Все feature/fix-коммиты для релиза уже мерджены в `main` через PR с
Conventional Commits-сообщениями. CI зелёный.

### 2. Решить version bump

Посмотрите коммиты с момента последнего тега:

```bash
git log $(git describe --tags --abbrev=0)..HEAD --oneline
```

Определите bump по правилам выше. Если есть `BREAKING CHANGE:` или `!`-коммит —
MAJOR. Иначе если есть `feat:` — MINOR. Иначе если есть `fix:` — PATCH.

### 3. Обновить CHANGELOG.md

В корне репозитория `CHANGELOG.md` следует
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) формату.

Переместите содержимое секции `## [Unreleased]` в новую секцию с версией
и датой:

```markdown
## [Unreleased]

## [1.1.0] — 2026-08-15

### Added
- Поддержка кроликов (Rabbit) — TDD цикл, care recommendations, UI.
- Onboarding screen при первом запуске.

### Fixed
- Округление Cat formula для возрастов > 15 лет.

### Changed
- Material You палитра обновлена под Material 3 1.4.
```

Альтернатива: автоматическая генерация через `git-cliff`:

```bash
git cliff --tag v1.1.0 --output CHANGELOG.md
```

(см. ADR-0007). На Plan 1 git-cliff не подключён, делаем вручную; добавление
git-cliff — задача Plan 2.

### 4. Бамп versionName + versionCode

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionName = "1.1.0"
    versionCode = 10100
}
```

### 5. Commit + tag

```bash
git checkout -b release/v1.1.0
git add CHANGELOG.md app/build.gradle.kts
git commit -m "chore(release): v1.1.0"
git push origin release/v1.1.0
# Открыть PR → merge в main → дождаться зелёного CI

git checkout main && git pull
git tag -a v1.1.0 -m "Release 1.1.0"
git push origin v1.1.0
```

### 6. Дождаться release.yml workflow

`release.yml` (`.github/workflows/release.yml`) триггерится на push тега
`v*.*.*`. Что он делает:

1. **Checkout** + Java 17 setup.
2. **Decode keystore** из `KEYSTORE_BASE64` secret в `app/keystore.jks`.
3. **`./gradlew bundleRelease`** с подписью через env-vars
   `PAWCLOCK_KEYSTORE_PATH`, `PAWCLOCK_KEYSTORE_PASSWORD`,
   `PAWCLOCK_KEY_ALIAS`, `PAWCLOCK_KEY_PASSWORD`.
4. **`scripts/verify-bundle-size.sh`** проверяет, что AAB < 15 МБ (лимит §7.5).
5. **`gh release create v1.1.0`** — создаёт GitHub Release с changelog'ом
   из `CHANGELOG.md` и аттачит AAB.
6. **Upload to Google Play** (опционально, закомментировано до настройки
   service account) через `r0adkll/upload-google-play@v1` в трек `internal`.

Concurrency: `cancel-in-progress: false` — релизы не отменяют друг друга,
если случайно созданы два тега подряд.

### 7. Google Play promotion (manual)

После того как AAB загружен в `internal` track (через workflow или вручную):

1. Зайти на [Google Play Console](https://play.google.com/console).
2. PawClock → Release → Testing → Internal testing → Promote release →
   **Closed alpha** → confirm.
3. Через 1-3 дня (после feedback) — Promote → **Open beta**.
4. Через 1-2 недели (после feedback) — Promote → **Production**.

Каждый promotion вручную, потому что:

- Хочется review screenshot'ов и release notes на Google Play.
- Может потребоваться обновить Data Safety (например, при добавлении
  фото — фотопикер не требует permission, но Data Safety нужно обновить).
- Финальный production rollout лучше делать staged (10% → 50% → 100%).

### 8. Annonce

- Create entry в GitHub Releases (release.yml уже сделал это, но можно
  расширить markdown'ом — скриншоты, full changelog).
- Опционально: пост в Discussions, README badge обновляется автоматически
  через shields.io.

## GitHub Secrets для release.yml

В Settings → Secrets and variables → Actions нужны:

| Secret | Значение |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 keystore.jks` |
| `KEYSTORE_PASSWORD` | пароль от keystore |
| `KEY_ALIAS` | alias ключа (например, `pawclock-release`) |
| `KEY_PASSWORD` | пароль от ключа |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | service-account JSON (когда подключим Google Play upload) |

### Генерация keystore

Если keystore ещё нет:

```bash
keytool -genkeypair \
  -alias pawclock-release \
  -keyalg RSA -keysize 2048 \
  -validity 25000 \
  -keystore keystore.jks
# Сохраните пароли в password manager!
```

**Резервная копия keystore — критична**: потеря keystore означает
невозможность выпустить обновление в Google Play (приложение придётся
перезаливать как новое). Сделайте offline-копию (USB, password manager,
encrypted cloud).

## F-Droid (Plan 3, отложено)

F-Droid публикация требует дополнительной подготовки:

- [ ] Confirm: zero proprietary dependencies (Hilt — Apache 2.0, OK; Room — Apache 2.0, OK; Material — Apache 2.0, OK).
- [ ] Reproducible builds: F-Droid собирает APK сам из исходников, нужно
      убедиться, что build deterministic. Может потребоваться отказ от
      Crashlytics / Firebase (у нас их нет — OK) и pin'инг всех версий.
- [ ] `fastlane/metadata/android/{ru,en-US}/full_description.txt` +
      `short_description.txt` + `images/icon.png` + screenshots.
- [ ] [F-Droid metadata file](https://f-droid.org/en/docs/Build_Metadata_Reference/)
      `metadata/app.pawclock.yml` в [fdroiddata repo](https://gitlab.com/fdroid/fdroiddata).
- [ ] Submit merge request в `fdroiddata` через GitLab.

F-Droid review process занимает 1-4 недели. После approval — каждый новый
тег `v*.*.*` автоматически собирается F-Droid build server'ом.

## Hot-fix процесс

Если в production обнаружен критичный баг:

1. Создать ветку `fix/<short-desc>` от тега последнего production-релиза
   (НЕ от `main`, если на main уже есть незарелиженные feature-коммиты).
2. Минимальный фикс + тест на регрессию.
3. PR → merge в `main` через стандартный flow.
4. Cherry-pick фикса в release-ветку, если у нас есть отдельные release-ветки
   (на Plan 1 их нет — `main` это и есть release-ветка).
5. Bump PATCH version, tag `v1.1.1`, push — release.yml сработает.
6. Promote через Google Play tracks (быстрее обычного, можно сразу staged
   production 50%).

## Rollback процесс

Если production-release сломал что-то критичное:

1. **Google Play**: открыть App releases → Production → Halt rollout.
   Это останавливает дальнейшую раздачу, но уже скачавшие пользователи
   на сломанной версии остаются.
2. **Hot-fix release**: см. выше, как можно быстрее.
3. **Не удаляйте git tag** — это сломает GitHub Release historcy. Если
   нужно "откатить" артефакт — создайте новый PATCH release, явно
   обозначив в CHANGELOG'е что это rollback.

## Чек-лист релиза

- [ ] Все CI checks зелёные на `main`.
- [ ] `./gradlew testDebugUnitTest` локально проходит.
- [ ] `./gradlew :app:bundleRelease` локально собирается (если есть keystore).
- [ ] Coverage thresholds: `./gradlew koverVerify`.
- [ ] `CHANGELOG.md` обновлён.
- [ ] `versionName` + `versionCode` подняты по формуле.
- [ ] Git tag создан и запушен.
- [ ] release.yml workflow зелёный.
- [ ] GitHub Release создан, AAB прикреплён.
- [ ] Google Play Internal track обновлён.
- [ ] Manual smoke-test debug-build'а на физическом устройстве.

## Связанные документы

- ADR-0007: Conventional Commits (для CHANGELOG автогенерации).
- `docs/ARCHITECTURE.md`: структура модулей.
- `docs/TESTING.md`: coverage requirements.
- `.github/workflows/release.yml`: реализация workflow.
- `scripts/verify-bundle-size.sh`: проверка размера AAB.
