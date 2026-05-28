<!--
Spec §8.7 — TDD-focused PR template. The checklist is the gate: an unchecked
TDD or KDoc-source box should be a blocker, not a nit. See CONTRIBUTING.md
(arrives in Plan 1 Task 24) for the full review protocol.
-->

## Что сделано / What changed


## Связанные issue / Related issues
<!-- e.g. Closes #123 / Refs #456 -->
Closes #

## Тип изменения / Change type
<!-- Conventional Commits, spec §8.3 -->
- [ ] feat — new user-visible feature
- [ ] fix — bug fix
- [ ] perf — performance improvement
- [ ] refactor — no behaviour change
- [ ] docs — documentation only
- [ ] test — tests only
- [ ] build / ci / chore

## Чеклист / Checklist

### TDD & correctness
- [ ] Я следовал TDD: тесты написаны ДО реализации / Tests written before implementation
- [ ] Все новые/изменённые ветки покрыты тестами / All new/changed branches covered by tests
- [ ] Edge cases (0, negative, very large) проверены / Edge cases tested
- [ ] Если затронут `:core:calculator` — coverage ≥ 95% / If `:core:calculator` touched, coverage ≥ 95%
- [ ] Если затронут `:core:domain` — coverage ≥ 90%

### Source attribution (для научного контента / for scientific content)
- [ ] Новые формулы имеют ссылку на первоисточник в KDoc (DOI / AKC / AAHA / AAFP) / New formulas link to a primary source in KDoc
- [ ] Care recommendations: добавлен `source_url` + `source_name` в JSON / Care recommendation sources cited

### Quality gates
- [ ] `./gradlew ktlintCheck detekt lintDebug` проходит локально / passes locally
- [ ] `./gradlew testDebugUnitTest` проходит локально / passes locally
- [ ] Screenshot tests обновлены (если затронут UI) / Roborazzi baselines refreshed if UI changed
- [ ] CHANGELOG.md обновлён / CHANGELOG.md updated
- [ ] Локализация (ru + en) обновлена / ru + en localisations updated
- [ ] Если затронут публичный модуль (`:core:model`, `:core:domain`) — учтена обратная совместимость / public-module compatibility considered

### Privacy & safety
- [ ] Не добавлено `INTERNET`, аналитики, crash-репортинга / No `INTERNET`, analytics, or crash reporting added
- [ ] Не добавлено новых required-разрешений без обоснования / No new dangerous permissions without justification

## Скриншоты / Screenshots
<!-- Required for UI changes. Light + dark mode if relevant. -->

## Заметки для ревьюера / Reviewer notes
