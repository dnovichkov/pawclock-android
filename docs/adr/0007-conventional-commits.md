# 0007. Conventional Commits as the commit message convention

- **Status**: Accepted
- **Date**: 2026-05-27
- **Deciders**: @dnovichkov
- **Tags**: process, ci, release

## Context and Problem Statement

PawClock — open-source проект на GitHub с публичной историей коммитов. Нужна конвенция commit-сообщений, которая:

1. Обеспечивает читаемость истории.
2. Позволяет автоматически генерировать `CHANGELOG.md`.
3. Связывает каждый коммит с типом изменения (feature / fix / docs / chore).
4. Совместима с автоматизированным семвер-bump'ом (через `git-cliff` или Release Please).
5. Поддерживается major IDE / git tools (linters, hooks).

## Decision Drivers

- **CHANGELOG автоматизация**: spec §8.10 требует семвер, §8.11 — README с релиз-нотами.
- **F-Droid metadata** (Plan 3): release notes per version нужны автоматически.
- **PR review focus**: тип коммита сразу сигнализирует ревьюверу контекст.
- **Контрибьюторы**: широкое распространение Conventional Commits — низкая learning curve.

## Considered Options

1. **Conventional Commits 1.0** (наш выбор) — `<type>(<scope>): <description>`.
2. **Angular convention** — почти то же, но более строгий перечень типов.
3. **Plain prose** — никаких правил.
4. **gitmoji** — emoji prefix.

## Decision Outcome

Chosen option: **Conventional Commits 1.0**, потому что:

- Это de-facto стандарт в open-source (Vue, Nest, Yarn, Lerna, Storybook и др.).
- Прямая совместимость с `git-cliff`, Release Please, semantic-release.
- Поддержка типов из spec §8.3: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`, `build`, `revert`.
- Scopes привязываются к модулям проекта: `feat(calculator):`, `fix(ui/pets):`, `chore(deps):`.
- Breaking changes отмечаются `!` или `BREAKING CHANGE:` footer.

Конкретные правила:

1. **Subject ≤ 72 символов**, императив ("add", не "added" / "adds").
2. **Scope = название модуля без `core:` / `feature:`** (например, `calculator`, `pets`, `editor`).
3. **Breaking change**: `feat(api)!: rename CalculatePetAgeUseCase` → triggers MAJOR bump.
4. **PR title повторяет первую строку коммита** — merge через squash сохраняет CC-формат.
5. **Pre-commit hook** (`scripts/pre-commit.sh` в Task 3) не валидирует CC — это делается через GitHub Action в `ci.yml` (валидатор `wagoid/commitlint-github-action` или эквивалент в lint.yml).

### Positive Consequences

- `CHANGELOG.md` генерируется по тегам: `git cliff --tag v1.0.0 > CHANGELOG.md` — выполняется в release.yml (Task 4).
- Семвер-bump автоматизируется: `feat` → MINOR, `fix` → PATCH, `feat!` или `BREAKING CHANGE` → MAJOR.
- История читаема: можно за 30 секунд понять, что вошло в каждый PR.

### Negative Consequences

- Контрибьюторы должны выучить конвенцию. Митигация: `docs/CONTRIBUTING.md` содержит примеры + ссылку на conventionalcommits.org.
- Pre-commit или CI validator может отклонять PR с неправильным форматом → friction для новых контрибьюторов. Митигация: CI-валидация даёт чёткое error message с примером правильного формата.

## Pros and Cons of the Options

### Option 1: Conventional Commits (chosen)

- Good: standard, tools, auto-changelog, auto-semver, scope-discoverability.
- Bad: лёгкая learning curve.

### Option 2: Angular convention

- Good: schöпительнее.
- Bad: меньше распространения, чем CC; те же ограничения.

### Option 3: Plain prose

- Good: ноль правил, ноль friction.
- Bad: невозможно автоматизировать CHANGELOG; история деградирует.

### Option 4: gitmoji

- Good: визуально приятно.
- Bad: emoji не везде поддерживаются (Windows terminal, CI logs); сложнее grep.

## Links

- [Conventional Commits 1.0](https://www.conventionalcommits.org/en/v1.0.0/)
- [git-cliff](https://git-cliff.org/)
- spec §8.3 (типы коммитов), §8.10 (semver)
