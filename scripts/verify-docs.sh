#!/usr/bin/env bash
# Verifies that all required documentation files exist and contain the
# mandatory sections defined in Task 24 of Plan 1.
#
# Checks:
#   1. docs/ARCHITECTURE.md       — high-level diagram + clean architecture +
#                                   MVI + dependency rules sections
#   2. docs/TESTING.md            — test pyramid + coverage + TDD-cycle +
#                                   Maestro sections
#   3. docs/CONTRIBUTING.md       — Conventional Commits + GitHub Flow +
#                                   PR checklist + code style sections
#   4. docs/RELEASE.md            — semver + versionCode + tag/workflow +
#                                   Google Play sections
#   5. CHANGELOG.md               — keep-a-changelog format with [Unreleased]
#   6. LICENSE                    — Apache 2.0 standard text
#
# Exit codes: 0 = ok; 1 = missing file or missing section.

set -euo pipefail

cd "$(dirname "$0")/.."

EXIT_CODE=0

check_file_exists() {
    local file="$1"
    if [ ! -f "$file" ]; then
        echo "  ❌ Missing: $file" >&2
        EXIT_CODE=1
        return 1
    fi
    echo "  ✅ Present: $file"
    return 0
}

check_section() {
    local file="$1"
    local pattern="$2"
    local label="$3"
    if ! grep -qiE "$pattern" "$file"; then
        echo "  ❌ $file missing section: $label" >&2
        EXIT_CODE=1
    else
        echo "  ✅ $file has: $label"
    fi
}

echo "==> Checking documentation files exist ..."
check_file_exists "docs/ARCHITECTURE.md"
check_file_exists "docs/TESTING.md"
check_file_exists "docs/CONTRIBUTING.md"
check_file_exists "docs/RELEASE.md"
check_file_exists "CHANGELOG.md"
check_file_exists "LICENSE"

echo ""
echo "==> Checking docs/ARCHITECTURE.md sections ..."
if [ -f "docs/ARCHITECTURE.md" ]; then
    check_section "docs/ARCHITECTURE.md" "^##.*[Дд]иаграмма|^##.*[Mм]одул" "module diagram"
    check_section "docs/ARCHITECTURE.md" "[Cc]lean [Aa]rchitecture" "clean architecture mention"
    check_section "docs/ARCHITECTURE.md" "MVI" "MVI"
    check_section "docs/ARCHITECTURE.md" "[Зз]ависимост|[Dd]ependency" "dependency rules"
fi

echo ""
echo "==> Checking docs/TESTING.md sections ..."
if [ -f "docs/TESTING.md" ]; then
    check_section "docs/TESTING.md" "[Tt]est [Pp]yramid|[Тт]естовая пирамида" "test pyramid"
    check_section "docs/TESTING.md" "[Cc]overage" "coverage targets"
    check_section "docs/TESTING.md" "TDD" "TDD cycle"
    check_section "docs/TESTING.md" "[Mm]aestro" "Maestro"
fi

echo ""
echo "==> Checking docs/CONTRIBUTING.md sections ..."
if [ -f "docs/CONTRIBUTING.md" ]; then
    check_section "docs/CONTRIBUTING.md" "[Cc]onventional [Cc]ommits" "Conventional Commits"
    check_section "docs/CONTRIBUTING.md" "GitHub [Ff]low|[Bb]ranch" "branching strategy"
    check_section "docs/CONTRIBUTING.md" "PR [Cc]hecklist|[Чч]еклист" "PR checklist"
    check_section "docs/CONTRIBUTING.md" "ktlint|detekt" "code style tools"
    check_section "docs/CONTRIBUTING.md" "[Нн]овый вид|new species|new animal|new species|add.*species|[Дд]обавить.*вид" "new species walkthrough"
fi

echo ""
echo "==> Checking docs/RELEASE.md sections ..."
if [ -f "docs/RELEASE.md" ]; then
    check_section "docs/RELEASE.md" "[Сс]емвер|[Ss]emver|[Ss]emantic [Vv]ersion" "semver"
    check_section "docs/RELEASE.md" "versionCode" "versionCode formula"
    check_section "docs/RELEASE.md" "[Tt]ag|[Тт]ег|tag" "tag flow"
    check_section "docs/RELEASE.md" "[Gg]oogle [Pp]lay" "Google Play"
    check_section "docs/RELEASE.md" "[Ff]-[Dd]roid" "F-Droid"
fi

echo ""
echo "==> Checking CHANGELOG.md format ..."
if [ -f "CHANGELOG.md" ]; then
    check_section "CHANGELOG.md" "[Kk]eep a [Cc]hangelog" "Keep a Changelog reference"
    check_section "CHANGELOG.md" "^## \[Unreleased\]" "Unreleased section"
fi

echo ""
echo "==> Checking LICENSE format ..."
if [ -f "LICENSE" ]; then
    check_section "LICENSE" "Apache License" "Apache License header"
    check_section "LICENSE" "Version 2.0" "version 2.0"
    check_section "LICENSE" "Copyright" "copyright notice"
fi

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ All documentation files validated."
else
    echo "❌ Documentation validation failed." >&2
fi

exit $EXIT_CODE
