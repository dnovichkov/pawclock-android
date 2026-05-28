#!/usr/bin/env bash
# Verifies that the Gradle multi-module skeleton is properly registered.
#
# Checks:
#   1. `./gradlew help` succeeds (build evaluates without errors)
#   2. `./gradlew projects` lists all 12 expected modules per spec §7.3
#
# Exit codes: 0 = ok; 1 = missing module(s) or gradle failure.

set -euo pipefail

cd "$(dirname "$0")/.."

EXPECTED_MODULES=(
    ":app"
    ":core:designsystem"
    ":core:model"
    ":core:calculator"
    ":core:database"
    ":core:datastore"
    ":core:domain"
    ":core:testing"
    ":feature:pets"
    ":feature:editor"
    ":feature:quickcalc"
    ":feature:settings"
)

echo "==> Running ./gradlew help ..."
./gradlew --no-daemon -q help > /dev/null

echo "==> Running ./gradlew projects ..."
PROJECTS_OUTPUT=$(./gradlew --no-daemon -q projects)
echo "$PROJECTS_OUTPUT"

MISSING=()
for module in "${EXPECTED_MODULES[@]}"; do
    if ! echo "$PROJECTS_OUTPUT" | grep -qE "'$module'"; then
        MISSING+=("$module")
    fi
done

if [ ${#MISSING[@]} -ne 0 ]; then
    echo ""
    echo "❌ Missing modules in settings.gradle.kts:" >&2
    for m in "${MISSING[@]}"; do
        echo "    $m" >&2
    done
    exit 1
fi

echo ""
echo "✅ All ${#EXPECTED_MODULES[@]} modules registered."
