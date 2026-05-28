#!/usr/bin/env bash
# verify-github-templates.sh — sanity-check .github/ scaffolding from Plan 1 Task 4.
#
# Checks (in order):
#   • required files exist
#   • each issue template has Markdown front-matter with name + about
#   • PR template has the TDD checklist marker
#   • CODEOWNERS lists the protected paths called out in spec §8.8
#   • dependabot.yml is valid YAML and lists both ecosystems
#
# Exit codes: 0 = ok; 1 = at least one check failed.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

FAIL=0
report_fail() {
    echo "  ❌ $*" >&2
    FAIL=1
}
report_ok() {
    echo "  ✅ $*"
}

require_file() {
    local path="$1"
    if [ ! -f "$path" ]; then
        report_fail "missing: $path"
        return 1
    fi
    return 0
}

echo "==> Required files"
REQUIRED_FILES=(
    ".github/CODEOWNERS"
    ".github/PULL_REQUEST_TEMPLATE.md"
    ".github/dependabot.yml"
    ".github/ISSUE_TEMPLATE/bug_report.md"
    ".github/ISSUE_TEMPLATE/feature_request.md"
    ".github/ISSUE_TEMPLATE/species_request.md"
    ".github/ISSUE_TEMPLATE/config.yml"
    ".github/workflows/ci.yml"
    ".github/workflows/lint.yml"
    ".github/workflows/release.yml"
    ".github/workflows/nightly.yml"
)
for f in "${REQUIRED_FILES[@]}"; do
    if require_file "$f"; then
        report_ok "$f"
    fi
done

echo ""
echo "==> Issue template front-matter"
for tpl in .github/ISSUE_TEMPLATE/bug_report.md \
           .github/ISSUE_TEMPLATE/feature_request.md \
           .github/ISSUE_TEMPLATE/species_request.md; do
    [ -f "$tpl" ] || continue
    # First non-empty line must be the front-matter opening "---"
    first=$(awk 'NF { print; exit }' "$tpl")
    if [ "$first" != "---" ]; then
        report_fail "$tpl: missing front-matter opening '---'"
        continue
    fi
    if ! grep -q '^name:' "$tpl" || ! grep -q '^about:' "$tpl"; then
        report_fail "$tpl: front-matter must define name: and about:"
        continue
    fi
    report_ok "$tpl"
done

echo ""
echo "==> species_request.md mandatory sections"
SR=".github/ISSUE_TEMPLATE/species_request.md"
if [ -f "$SR" ]; then
    for section in "Научное название" "Формула расчёта" "Источник формулы" "Стадии жизни"; do
        if ! grep -qF "$section" "$SR"; then
            report_fail "$SR: missing section header '$section'"
        else
            report_ok "section: $section"
        fi
    done
fi

echo ""
echo "==> PR template TDD checklist"
PR=".github/PULL_REQUEST_TEMPLATE.md"
if [ -f "$PR" ]; then
    if grep -q "тесты написаны ДО реализации" "$PR"; then
        report_ok "TDD marker present"
    else
        report_fail "$PR: missing TDD checkbox 'тесты написаны ДО реализации'"
    fi
    if grep -q "ktlintCheck" "$PR"; then
        report_ok "ktlint quality gate present"
    else
        report_fail "$PR: missing ktlintCheck quality gate"
    fi
fi

echo ""
echo "==> CODEOWNERS protected paths (spec §8.8)"
CO=".github/CODEOWNERS"
if [ -f "$CO" ]; then
    for path in '\*' '/core/calculator/' '/docs/adr/'; do
        if ! grep -qE "^${path}[[:space:]]" "$CO"; then
            report_fail "$CO: missing required path entry: $path"
        else
            report_ok "owns: $path"
        fi
    done
    if ! grep -qE "@[A-Za-z0-9_-]+" "$CO"; then
        report_fail "$CO: no @owner references found"
    fi
fi

echo ""
echo "==> dependabot.yml structure"
DB=".github/dependabot.yml"
if [ -f "$DB" ]; then
    for needed in 'package-ecosystem: gradle' 'package-ecosystem: github-actions' 'open-pull-requests-limit'; do
        if ! grep -qF "$needed" "$DB"; then
            report_fail "$DB: missing '$needed'"
        else
            report_ok "has: $needed"
        fi
    done
    if command -v python3 >/dev/null 2>&1 || command -v python >/dev/null 2>&1; then
        PY=$(command -v python3 || command -v python)
        if "$PY" - <<'PYEOF' 2>/dev/null
import yaml  # noqa: F401
PYEOF
        then
            "$PY" - "$DB" <<'PYEOF' || report_fail "dependabot.yml is not valid YAML"
import sys
import yaml
with open(sys.argv[1], "r", encoding="utf-8") as fh:
    data = yaml.safe_load(fh)
assert data.get("version") == 2, "dependabot.yml must set version: 2"
updates = data.get("updates") or []
ecosystems = {entry.get("package-ecosystem") for entry in updates}
expected = {"gradle", "github-actions"}
missing = expected - ecosystems
assert not missing, f"missing ecosystems: {missing}"
print("  parsed: dependabot.yml")
PYEOF
            report_ok "dependabot.yml YAML parsed and structurally valid"
        fi
    fi
fi

echo ""
if [ "$FAIL" -ne 0 ]; then
    echo "❌ verify-github-templates.sh failed"
    exit 1
fi
echo "✅ verify-github-templates.sh passed"
