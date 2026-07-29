# 0009. Export/Import schema versioning and format strategy

- **Status**: Accepted
- **Date**: 2026-05-28
- **Deciders**: @dnovichkov
- **Tags**: architecture, data, export-import, serialization

## Context and Problem Statement

§3.5 спецификации требует экспорт/импорт данных питомцев для переноса между устройствами (у
PawClock нет облачной синхронизации по [ADR-0005] — это единственный путь миграции). Формат бэкапа —
долгоживущий контракт: пользователь может импортировать файл, созданный гораздо более старой или
новой версией приложения. Нужно решить:

1. Как версионировать формат, чтобы будущие версии распознавали старые бэкапы и не повреждали данные?
2. Что делать с бэкапом из **будущей** (несовместимой) версии схемы?
3. Какие поля включать (в частности, локальные artifact-поля `id`, `photoPath`)?
4. JSON, CSV или оба?

## Decision Drivers

- **Сохранность данных**: импорт не должен молча искажать данные при несовпадении версий.
- **Переносимость**: бэкап не должен тащить device-локальные пути (`photoPath`) или Room-PK (`id`).
- **Совместимость с инструментами**: пользователи ожидают редактировать бэкап в Excel/Sheets (CSV).
- **Толерантность к будущим полям**: добавление необязательного поля не должно ломать старый импорт.
- **Стабильность ключей**: имена полей не должны зависеть от Kotlin-нейминга.

## Considered Options

1. **Версионируемая JSON-схема (`schema_version`) + CSV, fail-fast на будущих версиях** — поле
   `schema_version` в JSON; `CURRENT_SCHEMA_VERSION = 1`; `> 1` → `UnsupportedSchemaVersion`.
2. **Невёрсионированный JSON** — без `schema_version`; полагаться на best-effort парсинг.
3. **Только CSV** — простой табличный формат, но без места для метаданных версии/времени.
4. **Lenient на будущих версиях** — пытаться импортировать `schema_version > 1`, игнорируя незнакомое.

## Decision Outcome

Chosen option: **«Версионируемая JSON-схема + CSV, fail-fast на несовместимых версиях»**.

- **JSON — каноничный формат** с конвертом `PetsExportSchema { schema_version, exported_at, pets[] }`.
  `schema_version = 1` встроен в файл. Импорт отклоняет `schema_version > CURRENT_SCHEMA_VERSION`
  как **forward-incompatible** (`ImportException.UnsupportedSchemaVersion`) — лучше явная ошибка, чем
  риск исказить данные неизвестной будущей схемой.
- **CSV — вторичный формат** (RFC 4180) для совместимости с Excel/Sheets. Это плоская таблица без
  конверта: первая строка обязана быть заголовком `name,species_id,...`, поэтому `schema_version`/
  `exported_at` в тело CSV **не пишутся** (преамбула сломала бы контракт «строка 1 = заголовок»).
  CSV неявно соответствует schema v1 по набору колонок.
- **Толерантность вперёд внутри версии**: необязательные поля имеют дефолт `null`
  (`ignoreUnknownKeys = true`, `explicitNulls = false`) — будущее необязательное поле не сломает
  импорт v1.
- **Стабильные строковые id**: вид/пол/подкатегория сериализуются как `species_id`/`gender_id`/
  `subcategory_id` (стабильные id из `:core:model`), а не как Kotlin-имена классов. Имена полей —
  `snake_case` через `@SerialName`, общие для JSON и CSV-заголовка.
- **Локальные artifact-поля исключены**: `id` (Room-PK, переназначается при импорте) и `photoPath`
  (device-локальный путь в scoped storage) **не экспортируются**. При импорте `id = 0L`,
  `photoPath = null`.
- **Деградация пола, fail-fast вида**: неизвестный `gender_id` → `null` + `ImportWarning`
  (lenient — запись принимается); неизвестный `species_id` → fail-fast (вид определяет всю формулу
  расчёта, ошибаться нельзя).
- **Стратегии слияния**: `ImportStrategy.MERGE` (добавить рядом) / `REPLACE` (очистить и вставить);
  `dryRun` даёт предпросмотр без мутаций для confirm-диалога.

Контракт «бэкап сохраняет всё, кроме локальных артефактов» закреплён `RoundTripTest` (Task 24):
12 видов × {JSON, CSV} проходят `export → clear → import` с побайтовым совпадением переносимых полей.

### Positive Consequences

- Будущая версия со `schema_version = 2` сможет распознать v1-бэкап и при необходимости мигрировать;
  v1-приложение явно откажет в импорте v2-файла, а не повредит данные.
- `id`/`photoPath` не утекают между устройствами — импорт чистый.
- Один набор `snake_case` ключей переиспользуется JSON и CSV; десериализаторы делят
  `ImportEntryValidator` (одна протестированная политика валидации).
- CSV открывается/правится в Excel/Sheets без потери совместимости.

### Negative Consequences

- CSV не несёт `schema_version`/`exported_at` — при будущем breaking-изменении формата CSV придётся
  различать версии по набору колонок (эвристика), а не по явному полю. Митигация: JSON остаётся
  каноничным форматом для долгого хранения; CSV позиционируется как interop-экспорт.
- Fail-fast на `schema_version > 1` означает, что бэкап из будущей версии нельзя импортировать в
  старую даже частично. Это сознательный выбор в пользу сохранности данных над частичным импортом.
- `subcategory_id` — opaque строка, не валидируется на границе импорта (как и в Room-mapper'е): чужой
  id молча сохранится. Митигация: домен декодирует подкатегорию в типизированный enum позже, неизвестный
  id деградирует к дефолту вида.

## Pros and Cons of the Options

### Option 1: Версионируемый JSON + CSV, fail-fast (chosen)

- Good: безопасная эволюция формата, явная ошибка вместо порчи данных, interop через CSV.
- Bad: CSV без версии; fail-fast запрещает частичный импорт будущих бэкапов.

### Option 2: Невёрсионированный JSON

- Good: проще.
- Bad: нет способа отличить несовместимый будущий формат → риск тихого искажения данных.

### Option 3: Только CSV

- Good: максимальная простота и interop.
- Bad: негде хранить `schema_version`/`exported_at`; теряет null-семантику и структуру.

### Option 4: Lenient на будущих версиях

- Good: «всегда что-то импортируется».
- Bad: незнакомая семантика полей может исказить данные — противоречит главному драйверу (сохранность).

## Links

- spec §3.5 (export/import данных)
- `core/domain/src/main/kotlin/app/pawclock/domain/export/PetsExportSchema.kt`
- `core/domain/src/main/kotlin/app/pawclock/domain/import_/PetsJsonDeserializer.kt`
- `core/domain/src/main/kotlin/app/pawclock/domain/import_/ImportException.kt`
- Related: [ADR-0004](./0004-room-over-sqldelight.md), [ADR-0005](./0005-no-network-permission.md)
