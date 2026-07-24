# PawClock — Plan 3: Care Content Pass + v1.0 Release Readiness

## Overview

Третий план для проекта **PawClock**. Plan 1 (`2026-05-27-pawclock-foundation-and-dog-cat-mvp.md`)
заложил foundation + MVP для собак и кошек. Plan 2 (`2026-05-28-all-species-and-export-import.md`)
довёл приложение до полного **код-MVP**: все 12 групп животных с калькуляторами, стадиями жизни,
export/import, UI на все виды. **Единственный оставшийся блокер релиза v1.0** — care-рекомендации
всё ещё placeholder: 116 JSON-файлов (58 стадий × 2 локали) содержат `TODO(content-pass-after-plan-2)`.

Plan 3 закрывает этот блокер и доводит проект до **релизной готовности v1.0.0**:

1. **Care Content Pass** — замена всех 116 placeholder-файлов реальным научно-обоснованным контентом
   по опубликованным руководствам ветеринарных организаций (§14 спецификации), для всех 12 видов ×
   их стадий × ru/en. Каждый файл проходит усиленный quality-гейт (нет `TODO`, обязательный дисклеймер
   §3.3 дословно, содержательные поля).
2. **Release readiness** — то, что нужно репозиторию для первого production-релиза:
   - fastlane-метаданные Google Play (`fastlane/metadata/android/{ru,en-US}/`)
   - privacy policy `docs/PRIVACY.md` («No data collected, No data shared», §9)
   - bump версии `0.1.0` → `1.0.0` (`versionCode` 1 → 10000 по формуле §8.10)
   - финализация README (бейджи, installation, удаление «после Plan 2» placeholder'ов)
   - CHANGELOG: перенос записей в `[1.0.0]` с датой релиза
   - финальная acceptance-верификация всех критериев §12.1

**Что НЕ входит в этот план (отложено на Plan 4 / v1.1+):**

- **Pitest mutation testing** (§11.12) — было помечено «Plan 3» в Plan 1, сознательно перенесено
  (не блокирует релиз; YAGNI до первого пользователя)
- **Baseline Profiles** (`benchmark-macro-junit4`, §7.6) — требует managed device в CI, тяжёлая
  инфраструктура, не блокирует v1.0
- **ACRA** краш-репорты (§9) — опционально, Plan 4
- **Виджеты Glance**, локали de/es, Roborazzi как блокирующий CI-чек, F-Droid публикация — всё v1.1 (§12.2)
- **WorkManager уведомления**, журнал здоровья, графики Vico, Wear OS — v2.0 (§12.3)
- **Photo Picker** (opt-out ещё в Plan 2; SVG-иконок достаточно, §5.6)
- **Профессиональная ветеринарная вычитка контента** — рекомендуется как external Post-Completion
  (см. ниже); Plan 3 создаёт контент по published guidelines, но не заменяет экспертную рецензию

**Проблема, которую решает план:** снять последний технический блокер публикации (placeholder-контент)
и подготовить репозиторий к тегированию `v1.0.0-rc1` → internal testing → production в Google Play.
После Plan 3 приложение полностью соответствует критериям MVP v1.0 из §12.1 спецификации.

### Approaches discussed (Step 1.5)

**Цель Plan 3** (выбрано пользователем): «Контент + релиз v1.0» среди альтернатив (v1.1 виджеты/локали;
объединённый контент+v1.1; v2.0 напоминания). Обоснование: контент — единственный блокер, всё
остальное — post-release enhancement.

**Инженерный объём** (выбрано пользователем): **lean-план** — только content pass + release readiness.
Pitest / Baseline Profiles / ACRA сознательно отложены в Plan 4 (не блокируют релиз).

**TDD для контента** (методологическое решение, см. Development Approach): поскольку контент — это данные,
а не код, «тест» кодирует *definition of done*: усиленный `CareContentQualityTest` в `:core:domain`
(нет `TODO`-маркеров, дисклеймер §3.3 дословно, минимальная содержательность полей, валидный source_url).
Красная фаза — placeholder не проходит гейт; зелёная — авторский контент проходит. Порядок задач
решён через реестр `contentComplete: Set<Species>` (см. Task 1), чтобы suite оставался зелёным после
каждой задачи (инвариант ralphex).

**Git-логистика** (выбрано пользователем): план коммитится в текущую ветку `all-species-and-export-import`
(не мержим Plan 2 отдельно). План попадёт в `main` вместе с PR Plan 2. Реализация Plan 3 стартует
поверх этой ветки.

## Context (from discovery)

**Текущее состояние репозитория (после Plan 2):**

- **Ветка:** `all-species-and-export-import` (27 коммитов впереди `main`, PR не влит; `main` содержит
  только Plan 1 + docs Plan 2). Remote: `git@github.com:dnovichkov/pawclock-android.git`. `gh` CLI
  **не установлен** локально — GitHub-операции (PR, merge) выполняются пользователем через UI.
- **12 Gradle модулей** скомпилированы, тесты зелёные. Все 12 видов `isImplemented = true`.
- **`:core:model`** содержит `CareRecommendation` (data class, kotlinx.serialization) с полями:
  `stage_description`, `nutrition`, `activity`, `veterinary_check_frequency`, `dental_care` (nullable),
  `warning_signs`, `source_url`, `source_name`, `disclaimer`.
- **`:core:domain`** содержит `CareRepositoryImpl` (species-agnostic loader), `GetCareRecommendationsUseCase`,
  и **`CareAssetsIntegrityTest`** (`@TestFactory` — для каждого `Species.implemented()` × стадии × {ru,en}
  проверяет наличие файла, непустоту полей, присутствие дисклеймера). Покрытие ≥ 90% (kover minBound=90).
- **Care-ассеты:** `app/src/main/assets/care/{species}/{stage}/{ru,en}.json` — **116 файлов, ВСЕ 116
  содержат `TODO`** (58 стадий × 2 локали). Структура валидна, контент — placeholder.
- **Трекер:** `docs/CARE_CONTENT.md` — таблица статусов (все 12 видов = `TODO`), content-pass checklist.
- **Верификация:** `scripts/verify-care-assets.sh` (наличие + обязательные поля + дисклеймер; JSON-валидность
  при наличии `node`).
- **Версия:** `app/build.gradle.kts` → `versionCode = 1`, `versionName = "0.1.0"`.
- **fastlane:** директории **нет** (`fastlane/` отсутствует).
- **README:** placeholder-бейджи, «Google Play: _появится после Plan 2_», «F-Droid: запланировано на Plan 3».
- **CHANGELOG:** запись `[1.0.0] - 2026-06-01` уже подготовлена (Plan 1+2 Added), но контент-pass туда ещё
  не внесён; `[Unreleased]` пуст. Дата проставляется при тегировании.
- **CI/CD:** 4 workflow (ci.yml, lint.yml, release.yml, nightly.yml). `release.yml` готов из Plan 1
  (триггер на тег `v*.*.*`, `bundleRelease` с подписью из Secrets, verify-bundle-size, gh release).
- **ADR:** 0001–0009 (последние два из Plan 2). Следующий свободный номер — **0010**.

**Стадии жизни по видам (источник истины — `LifeStage.*.all()` в `:core:model`; отражено в
`scripts/verify-care-assets.sh` и `docs/CARE_CONTENT.md`):**

| Вид (`id`) | Стадии | Первоисточник (§14) |
|---|---|---|
| Dog (`dog`) | puppy, young_adult, mature_adult, senior, end_of_life | AAHA 2019 Canine Life Stage Guidelines |
| Cat (`cat`) | kitten, young_adult, mature_adult, senior, end_of_life | AAHA/AAFP 2021 Feline Life Stage Guidelines |
| Rabbit (`rabbit`) | infancy, adolescence, young_adult, adult, senior | House Rabbit Society & Oxbow |
| Hamster (`hamster`) | pup, juvenile, adult, senior, very_senior | RVC VetCompass & PetMD |
| Guinea Pig (`guinea_pig`) | pup, juvenile, adult, senior, geriatric | Oxbow Guinea Pig Life Stages |
| Rat (`rat`) | pup, juvenile, adult, senior, end_of_life | Sengupta 2013 + RSPCA/AFRMA husbandry |
| Mouse (`mouse`) | pup, juvenile, adult, senior, end_of_life | Dutta & Sengupta 2016 + husbandry |
| Ferret (`ferret`) | kit, juvenile, adult, senior, geriatric | The Senior Ferret (PMC7129291) & Oxbow |
| Bird (`bird`) | hatchling, juvenile, adult, senior, geriatric | AAV Care for Senior Parrots & Lafeber Vet |
| Reptile (`reptile`) | hatchling, juvenile, adult, senior | PetPlace, Reptile Centre & A-Z Animals |
| Horse (`horse`) | foal, yearling, young_adult, adult, senior | AAEP Senior Horse Care & PetMD |
| Fish (`fish`) | fry, juvenile, adult, senior | PetMD & Kodama Koi Farm |

**Всего: 58 стадий × 2 локали = 116 файлов.** У `fish` поле `dental_care` намеренно `null`
(неприменимо к рыбам) — quality-гейт это учитывает.

**Канонический дисклеймер (§3.3, дословно — quality-гейт проверяет точное совпадение):**
- ru: `Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача.`
- en: `This information is for educational purposes only and does not replace consultation with a veterinarian.`

**Стек не меняется** (§7.1). Никаких новых зависимостей: content-pass — это данные + усиление
существующего JVM-теста. Release-задачи — конфиги и документация.

## Development Approach

- **Testing approach: TDD (tests first)** — адаптирован под content pass (см. [[feedback_tdd_workflow]]).
  Для каждого вида: сначала расширяется/включается quality-гейт (Red — placeholder не проходит),
  затем авторится реальный контент (Green). Для release-задач — обычный TDD (тест наличия/лимитов → конфиг).
- Complete each task fully before moving to the next.
- Make small, focused changes — **один вид = одна задача** (мирроринг ритма Plan 2: один калькулятор =
  одна задача). ~10 файлов на задачу, обозримый diff.
- **CRITICAL: every task MUST include new/updated tests** для изменений в этой задаче.
  - для content-задач «тест» = usиление quality-гейта + добавление вида в `contentComplete` реестр
  - для release-задач = JVM-тест или verify-скрипт проверяющий наличие/лимиты/отсутствие placeholder'ов
  - tests cover success + error scenarios (например: title.txt превышает 30 символов → тест падает)
- **CRITICAL: all tests must pass before starting next task** — без исключений. Реестр `contentComplete`
  гарантирует, что suite остаётся зелёным после каждой content-задачи (строгие проверки идут только по
  «готовым» видам; структурные — по всем, они уже зелёные с Plan 2).
- **CRITICAL: update this plan file when scope changes** (➕ для discovered tasks, ⚠️ для blockers).
- Run tests after each change. Maintain backward compatibility — Plan 1/2 API не ломаем.

**Content authoring standard (применяется к КАЖДОЙ content-задаче Tasks 2–13):**

- **Высота контента — общая husbandry-гид по стадиям жизни**, НЕ индивидуальная медицина: тип/режим
  корма (без брендов), активность и обогащение среды, рекомендованная частота ветосмотров, тревожные
  признаки для обращения к врачу, стоматология (где применимо). **Никаких** диагнозов, дозировок,
  протоколов лечения болезней.
- **Grounded in §14 sources** — контент согласован с процитированным первоисточником (AAHA/AAFP/House
  Rabbit Society/Oxbow/Sengupta/AAV/AAEP/PetMD и т.д.). `source_url` / `source_name` соответствуют
  таблице `docs/CARE_CONTENT.md`. Где доступен веб-доступ при исполнении — сверять с источником;
  иначе опираться на устоявшиеся положения этих guidelines, оставаясь на уровне общих рекомендаций.
- **Обязательный дисклеймер §3.3** — поле `disclaimer` дословно равно канонической строке (см. Context),
  НЕ редактируется.
- **Обе локали параллельно** — ru и en авторятся вместе для согласованности; en — не машинный перевод,
  а самостоятельный корректный текст.
- **`dental_care`**: заполняется для всех видов кроме `fish` (там остаётся `null`). Для видов, где
  стоматология малоприменима (птицы/рептилии) — краткая релевантная заметка (клюв/зубы по виду).
- **Поля содержательны**: `stage_description` — 1–2 абзаца (§3.3), остальные текстовые поля — конкретные
  практические рекомендации, не общие фразы.

## Testing Strategy

- **Content quality gate (JVM, JUnit 5, `:core:domain`):** новый `CareContentQualityTest` — для каждого
  вида из реестра `contentComplete` × стадии × {ru,en} проверяет:
  1. ни одно поле не содержит `TODO` (case-insensitive) — **решающий гейт готовности**;
  2. `disclaimer` дословно равен канонической строке §3.3 (per-locale);
  3. `stage_description` ≥ 120 символов; `nutrition`/`activity`/`veterinary_check_frequency`/`warning_signs`
     ≥ 40 символов (защита от терсовых заглушек);
  4. `dental_care`: непустой (≥ 30 символов) для всех видов кроме `fish` (где `null`);
  5. `source_url` — валидный `http(s)://` URL, `source_name` непустой и без `TODO`.
  Существующий `CareAssetsIntegrityTest` (наличие/структура/дисклеймер по ВСЕМ видам) остаётся и продолжает
  проходить.
- **Shell-верификация:** `scripts/verify-care-content.sh` — CI-friendly обёртка (no-`TODO` + дисклеймер +
  min-length) поверх существующего `verify-care-assets.sh`; graceful fallback без `node`.
- **Release-задачи:** `scripts/verify-release-metadata.sh` (или JVM-тест) — наличие fastlane-файлов,
  Google Play char-лимиты (title ≤ 30, short ≤ 80, full ≤ 4000, changelog ≤ 500), наличие privacy-фраз,
  корректность `versionName`/`versionCode`, отсутствие «после Plan 2» placeholder'ов в README.
- **Регрессия:** после каждой задачи прогоняются `:core:domain:test` + `detekt` + `koverVerify`
  (domain ≥ 90%). Никаких изменений в `:core:calculator` не ожидается (kover 95% не затрагивается).
- **E2E (Maestro):** существующие flows Plan 1/2 — sanity check в финальной задаче (контент отображается,
  дисклеймер виден). Не блокирующий.
- **Compose UI:** в acceptance — verify/add тест что `disclaimer` рендерится на care-секции PetDetail (§3.3).

## Progress Tracking

- Mark completed items with `[x]` immediately when done.
- Add newly discovered tasks with ➕ prefix.
- Document issues/blockers with ⚠️ prefix.
- Update plan if implementation deviates from original scope.
- Keep plan in sync with actual work done.

## What Goes Where

- **Implementation Steps** (`[ ]` checkboxes): контент JSON, тесты, verify-скрипты, конфиги, документация
  внутри репозитория — автоматизируемые агентом действия.
- **Post-Completion** (no checkboxes): профессиональная ветеринарная вычитка, реальная Google Play submission,
  генерация keystore + Secrets, скриншоты для store listing (graphic pass), Branch Protection через UI,
  регистрация домена/Codecov, тегирование релиза — всё, что требует внешних систем или человеческого решения.

## Implementation Steps

### Task 1: Care content quality gate + registry infrastructure (TDD harness) ✅
- [x] write test `CareContentQualityTest` в `:core:domain/src/test/kotlin/app/pawclock/domain/care/`
  (`@TestFactory`), итерирующий по `contentComplete` реестру × стадии × {ru,en}; проверки: no-`TODO`,
  дисклеймер §3.3 дословно (per-locale), min-length (stage_description ≥ 120, прочие ≥ 40, dental ≥ 30
  или null для fish), валидный `source_url`, непустой `source_name` — extract'нута `verifyQuality()`
  ради detekt; + always-green `@Test registry only references implemented species`
- [x] define реестр `contentComplete: Set<Species>` как single source of truth в тесте (initially **пустой**)
  — строгие проверки идут только по нему, пустой реестр → 0 динамических тестов = зелёно (нет Red-фазы
  инфраструктуры, Red наступает per-species в Tasks 2–13)
- [x] add канонические дисклеймеры (ru/en) как test-константы, сверенные с существующими файлами
- [x] create `scripts/verify-care-content.sh` — shell-зеркало гейта (no-`TODO` + дисклеймер + min-length),
  принимает список «готовых» видов (CLI args override `CONTENT_COMPLETE[]`), node-валидатор через quoted
  heredoc + graceful fallback без `node`; `chmod +x` через `git update-index --chmod=+x`
- [x] write test для скрипта: `verify-care-content.sh dog` на placeholder корректно репортит `TODO`-нарушения
  по всем полям (exit 1); `verify-care-content.sh` (пустой набор) → exit 0
- [x] run `./gradlew :core:domain:test :core:domain:detekt --no-daemon` + `bash scripts/verify-care-content.sh`
  — зелёно (пустой реестр; existing `CareAssetsIntegrityTest` зелёный; detekt clean)

### Task 2: Dog care content (5 stages × ru/en) ✅
- [x] author реальный контент в `app/src/main/assets/care/dog/{puppy,young_adult,mature_adult,senior,end_of_life}/{ru,en}.json`
  по AAHA 2019 Canine Life Stage Guidelines (Content authoring standard выше); size-зависимость
  порогов senior (мелкие 11+, средние 9+, крупные 7+, гигантские 5–6) отражена в stage_description
- [x] заполнены все поля (`stage_description` 1–2 абзаца, nutrition/activity/vet/warning/dental), `source_url`
  и `source_name` = AAHA 2019, `disclaimer` дословно §3.3
- [x] add `Species.Dog` в реестр `contentComplete` (Kotlin) + `CONTENT_COMPLETE` (shell)
- [x] run `./gradlew :core:domain:test :core:domain:koverVerify --no-daemon` — 10 dog quality-тестов **Green**;
  `verify-care-content.sh` OK (10 файлов); koverVerify (domain ≥ 90%) зелёный — must pass before next task

### Task 3: Cat care content (5 stages × ru/en) ✅
- [x] author контент `care/cat/{kitten,young_adult,mature_adult,senior,end_of_life}/{ru,en}.json` по
  AAHA/AAFP 2021 Feline Life Stage Guidelines; indoor/outdoor различия и senior-переход (10+, объединение
  senior/super-senior) отражены в описаниях
- [x] заполнены все поля + `source_url`/`source_name` = AAHA/AAFP 2021 (catvets.com) + дисклеймер §3.3
- [x] add `Species.Cat` в `contentComplete` (Kotlin + shell)
- [x] run `./gradlew :core:domain:test :core:domain:koverVerify --no-daemon` — 10 cat quality-тестов Green;
  `verify-care-content.sh` OK (20 файлов) — must pass before next task

### Task 4: Rabbit care content (5 stages × ru/en) ✅
- [x] author контент `care/rabbit/{infancy,adolescence,young_adult,adult,senior}/{ru,en}.json` по House
  Rabbit Society + Oxbow Rabbit Life Stages; сено-ориентированное питание, важность стоматологии (постоянно
  растущие зубы), senior с 5 лет
- [x] заполнить поля + source (rabbit.org / oxbowanimalhealth.com) + дисклеймер §3.3
- [x] add `Species.Rabbit` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green для rabbit; detekt + koverVerify — must pass before next task

### Task 5: Hamster care content (5 stages × ru/en) ✅
- [x] author контент `care/hamster/{pup,juvenile,adult,senior,very_senior}/{ru,en}.json` по RVC VetCompass
  Hamster Study + PetMD; короткая ЧЖ, старость с 1.5 лет, признаки старения
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Hamster` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green для hamster; detekt + koverVerify — must pass before next task

### Task 6: Guinea Pig care content (5 stages × ru/en) ✅
- [x] author контент `care/guinea_pig/{pup,juvenile,adult,senior,geriatric}/{ru,en}.json` по Oxbow Guinea
  Pig Life Stages; критичность витамина C, сено, senior с 4 лет, geriatric с 6
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.GuineaPig` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 7: Rat care content (5 stages × ru/en) ✅
- [x] author контент `care/rat/{pup,juvenile,adult,senior,end_of_life}/{ru,en}.json` по Sengupta 2013 +
  RSPCA/AFRMA husbandry; социальность, склонность к респираторным/опухолевым проблемам в senior, EndOfLife 2.5г
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Rat` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 8: Mouse care content (5 stages × ru/en) ✅
- [x] author контент `care/mouse/{pup,juvenile,adult,senior,end_of_life}/{ru,en}.json` по Dutta & Sengupta
  2016 + husbandry; очень короткая ЧЖ, senior с 12 мес, EndOfLife 2.0г
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Mouse` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 9: Ferret care content (5 stages × ru/en) ✅
- [x] author контент `care/ferret/{kit,juvenile,adult,senior,geriatric}/{ru,en}.json` по The Senior Ferret
  (PMC7129291) + Oxbow Ferret Life Stages; облигатный хищник (высокобелковый корм), senior с 3–4 лет
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Ferret` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 10: Bird care content (5 stages × ru/en) ✅
- [x] author контент `care/bird/{hatchling,juvenile,adult,senior,geriatric}/{ru,en}.json` по AAV Care for
  Senior Parrots + Lafeber Vet; широкий разброс ЧЖ по видам, обогащение среды, `dental_care` → заметка про
  клюв (не зубы)
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Bird` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 11: Reptile care content (4 stages × ru/en) ✅
- [x] author контент `care/reptile/{hatchling,juvenile,adult,senior}/{ru,en}.json` по PetPlace + Reptile
  Centre + A-Z Animals; критичность температуры/UVB/влажности, видовые различия, `dental_care` → релевантная
  заметка по виду
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Reptile` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 12: Horse care content (5 stages × ru/en) ✅
- [x] author контент `care/horse/{foal,yearling,young_adult,adult,senior}/{ru,en}.json` по AAEP Senior Horse
  Care + PetMD; фуражное питание, копыта/зубы (плановая стоматология лошадей), senior с ~15 лет, вакцинация
- [x] заполнить поля + source + дисклеймер §3.3
- [x] add `Species.Horse` в `contentComplete`
- [x] run `./gradlew :core:domain:test --no-daemon` — Green; detekt + koverVerify — must pass before next task

### Task 13: Fish care content (4 stages × ru/en) + registry completeness assert ✅
- [x] author контент `care/fish/{fry,juvenile,adult,senior}/{ru,en}.json` по PetMD + Kodama Koi Farm;
  качество воды/параметры, видовой разброс ЧЖ (guppy 2г … koi 30г), `dental_care` остаётся `null`
- [x] заполнить поля + source + дисклеймер §3.3 (без `dental_care`)
- [x] add `Species.Fish` в `contentComplete`
- [x] add assertion в `CareContentQualityTest`: `contentComplete == Species.implemented().toSet()`
  (все 12 видов — гарантия что ни один вид не пропущен; **no silent caps**)
- [x] run `./gradlew :core:domain:test --no-daemon` + `bash scripts/verify-care-content.sh` — все 116 файлов
  Green; `grep -rl "TODO" app/src/main/assets/care` возвращает пусто — must pass before next task

### Task 14: Version bump 1.0.0 + CHANGELOG finalization ✅
- [x] write FAILING test/verify `scripts/verify-release-metadata.sh` (или JVM-тест в `:app`): проверяет
  `versionName == "1.0.0"` и `versionCode == 10000` в `app/build.gradle.kts` (формула §8.10: 1*10000+0*100+0)
- [x] update `app/build.gradle.kts`: `versionCode = 1` → `10000`, `versionName = "0.1.0"` → `"1.0.0"`
- [x] update `CHANGELOG.md`: внести Care Content Pass в раздел `[1.0.0]` (Changed: placeholder → реальный
  контент по §14); проставить корректную дату релиза-подготовки; `[Unreleased]` оставить пустым
- [x] run verify-скрипт — Green; must pass before next task

### Task 15: Privacy policy (§9)
- [ ] write FAILING test: `scripts/verify-release-metadata.sh` проверяет наличие `docs/PRIVACY.md` +
  обязательные фразы («No data collected», «No data shared» / ru-эквиваленты) + отсутствие противоречий
- [ ] create `docs/PRIVACY.md` (ru + en секции): «No data collected, No data shared», нет INTERNET-разрешения
  (§9), нет аналитики/трекеров, все данные локально, export/import — по явному действию пользователя через SAF
- [ ] link из README (раздел Privacy) на `docs/PRIVACY.md`
- [ ] run verify-скрипт — Green; must pass before next task

### Task 16: fastlane metadata для Google Play
- [ ] write FAILING test: `scripts/verify-release-metadata.sh` проверяет структуру
  `fastlane/metadata/android/{ru,en-US}/` и char-лимиты (title ≤ 30, short_description ≤ 80,
  full_description ≤ 4000, `changelogs/10000.txt` ≤ 500)
- [ ] create `fastlane/metadata/android/en-US/`: `title.txt`, `short_description.txt`, `full_description.txt`
  (USP из §1.3: все виды, научные формулы, no ads/tracking, Material You, open source), `changelogs/10000.txt`
- [ ] create `fastlane/metadata/android/ru/` с ru-переводами тех же файлов
- [ ] add `fastlane/README.md` объясняющий что `images/` (скриншоты) — external graphic pass (Post-Completion)
- [ ] run verify-скрипт — все лимиты Green; must pass before next task

### Task 17: README finalization + ADR-0010 (content provenance)
- [ ] write FAILING test: `scripts/verify-release-metadata.sh` проверяет что README не содержит placeholder'ов
  `_появится после Plan 2_` / `planned (Plan 3)` в F-Droid-контексте (обновлены на актуальный статус)
- [ ] update `README.md`: убрать «после Plan 2» placeholder'ы, обновить статус (v1.0 ready), installation
  (APK from Releases + Google Play pending), ссылка на PRIVACY.md, актуализировать F-Droid статус (v1.1)
- [ ] write `docs/adr/0010-care-content-from-published-guidelines.md` (MADR): решение — care-контент авторится
  по опубликованным guidelines ветеринарных организаций (§14) с source-attribution + дисклеймером §3.3,
  на уровне общей husbandry-гид; Consequences: не заменяет экспертную рецензию (external Post-Completion),
  граница «не диагностика/не лечение», quality-гейт как enforcement
- [ ] update `docs/CARE_CONTENT.md`: все 12 видов статус `TODO` → `DONE`; отметить дату content-pass
- [ ] run verify-скрипт + `scripts/verify-adrs.sh` (если проверяет наличие) — Green; must pass before next task

### Task 18: Verify acceptance criteria (§12.1 MVP v1.0)
- [ ] verify все 116 care-файлов реальны: `grep -rl "TODO" app/src/main/assets/care` пусто;
  `CareContentQualityTest` + `CareAssetsIntegrityTest` зелёные для всех 12 видов
- [ ] verify §3.3 display requirement: дисклеймер рендерится на care-секции PetDetail — verify существующий
  Compose UI тест или добавить `assertIsDisplayed` на текст дисклеймера
- [ ] run полный unit-suite: `./gradlew test --no-daemon` (все модули) — зелёный
- [ ] run `./gradlew ktlintCheck detekt lintDebug` — 0 error-уровня issues
- [ ] verify coverage-гейты: `:core:calculator:koverVerify` (≥95%), `:core:domain:koverVerify` (≥90%) — held
- [ ] run `./gradlew assembleDebug` (или `bundleDebug`) — сборка проходит; при возможности `verify-bundle-size.sh`
  (лимит §7.5 < 15 МБ универсальный)
- [ ] run существующие Maestro flows sanity check (или verify их YAML-валидность если эмулятор недоступен)
- [ ] verify §12.1 чек-лист: 12 видов ✓, профили (Room) ✓, care-рекомендации (реальные) ✓, Material You ✓,
  ru+en ✓, export/import ✓, coverage ✓ — все пункты MVP v1.0 закрыты

### Task 19: [Final] Documentation sync
- [ ] verify `docs/CARE_CONTENT.md`, README, CHANGELOG, ADR-0010 консистентны и отражают финальное состояние
- [ ] verify `docs/ARCHITECTURE.md` / `docs/TESTING.md` упоминают content quality gate (если релевантно —
  добавить краткую заметку про `CareContentQualityTest`)
- [ ] final `git status` clean после коммитов задач; план готов к перемещению в `completed/` (ralphex авто)

## Technical Details

**CareRecommendation schema (не меняется, `:core:model`):**
```
stage_description, nutrition, activity, veterinary_check_frequency,
dental_care (nullable — null только для fish), warning_signs,
source_url, source_name, disclaimer
```

**Content quality gate — уровни проверок:**
```
СТРУКТУРА (все виды, из Plan 2)     → CareAssetsIntegrityTest (наличие + непустота + дисклеймер)
КАЧЕСТВО  (виды из contentComplete) → CareContentQualityTest:
    no-TODO | disclaimer==§3.3 | min-length | dental-rule | valid source_url
ЗАВЕРШЁННОСТЬ (Task 13)             → contentComplete == Species.implemented()
```

**Реестр `contentComplete` — почему он держит suite зелёным:**
- Строгие quality-проверки применяются ТОЛЬКО к видам из реестра.
- Task 1 создаёт пустой реестр → строгих проверок нет → зелено.
- Каждая content-задача: авторит контент И добавляет вид в реестр в ОДНОЙ задаче → его строгие тесты
  становятся Green синхронно с появлением контента.
- Виды НЕ в реестре продолжают проверяться только структурно (уже зелёные placeholder'ы) → suite зелёный
  после каждой задачи (инвариант ralphex «all tests pass before next task»).

**Version code (§8.10):** `versionCode = MAJOR*10000 + MINOR*100 + PATCH` → `1.0.0` = `10000`.

**fastlane структура (§8.1):**
```
fastlane/metadata/android/
  en-US/{title,short_description,full_description}.txt + changelogs/10000.txt
  ru/{title,short_description,full_description}.txt    + changelogs/10000.txt
  (images/ — скриншоты, external graphic pass, Post-Completion)
```
Google Play лимиты: title ≤ 30, short_description ≤ 80, full_description ≤ 4000, changelog ≤ 500.

**Границы контента (safety):** общая husbandry-гид по стадиям жизни со ссылкой на источник и обязательным
дисклеймером §3.3. НЕ включает: диагнозы, дозировки лекарств, протоколы лечения болезней, индивидуальные
медицинские рекомендации. Дисклеймер на каждом care-экране обязателен (§3.3, enforced UI-тестом в Task 18).

## Post-Completion

*Items requiring manual intervention or external systems — informational only*

**Профессиональная вычитка контента (настоятельно рекомендуется):**
- Care-контент Plan 3 создан по опубликованным guidelines (§14), но НЕ прошёл рецензию практикующего
  ветеринара. Перед широким production-релизом рекомендуется экспертная вычитка каждого вида × стадии.
- Спорные/видоспецифичные утверждения (питание экзотики, рептильные параметры среды) особенно требуют
  проверки специалистом.

**External system updates (перед v1.0.0 production):**
- Проверить занятость названия «PawClock» в Google Play (§0); при занятости — запасной вариант
  (PetSpan / PetYears / TailTime / Furcast / PetChrono).
- Сгенерировать release keystore + добавить `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD` в GitHub Secrets (для `release.yml` из Plan 1).
- Скриншоты для Google Play listing (`fastlane/metadata/android/{ru,en-US}/images/`) — отдельный graphic pass
  (3–5 экранов: PetsList, PetDetail, QuickCalc, Settings).
- Зарегистрировать Codecov + `CODECOV_TOKEN` в Secrets (если ещё нет).
- Установить Branch Protection для `main` через GitHub UI (§8.4).
- Разместить privacy policy на публичном URL (pawclock.app/privacy) — контент из `docs/PRIVACY.md`.
- (Опц.) Зарегистрировать домен `pawclock.app` / `pawclock.dev`.

**Manual verification:**
- Реальное SAF export/import тестирование на устройствах API 24/26/30/33/35 (поведение SAF нестабильно
  между OEM).
- UX-проверка care-экранов: читаемость контента, отображение дисклеймера, fontScale Largest, TalkBack.
- Performance: cold start < 500 мс на Pixel 6 (Baseline Profiles — Plan 4).

**Git / release flow:**
- Merge ветки `all-species-and-export-import` (Plan 2) + Plan 3 в `main` через squash-PR (GitHub UI; `gh`
  локально не установлен).
- ralphex автоматически переместит `docs/plans/2026-07-24-care-content-and-v1-release.md` в
  `docs/plans/completed/`.
- Создать тег `v1.0.0-rc1` → авто-trigger `release.yml` (bundleRelease + gh release).
- После manual QA + вычитки контента — promote `v1.0.0` → Google Play internal → alpha → beta → production.
