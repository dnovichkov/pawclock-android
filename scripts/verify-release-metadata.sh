#!/usr/bin/env bash
# Verifies release-readiness metadata for the PawClock v1.0.0 release (Plan 3, Tasks 14–17).
#
# Sections are added task by task (each stays green before the next task starts):
#   Task 14 — app version: versionName 1.0.0 + versionCode 10000 (§8.10 formula 1*10000+0*100+0)
#   Task 15 — docs/PRIVACY.md exists with the mandatory "No data collected / No data shared" statements (§9)
#   Task 16 — fastlane/metadata/android/{ru,en-US}/ with Google Play char limits
#   Task 17 — README has no stale "after Plan 2" release placeholders
#
# Exit codes: 0 = all checks pass; 1 = a violation was found.

set -euo pipefail

cd "$(dirname "$0")/.."

EXIT_CODE=0
fail() {
    echo "  ❌ $1" >&2
    EXIT_CODE=1
}
ok() { echo "  ✅ $1"; }

# ---------------------------------------------------------------------------
# Task 14 — application version
# ---------------------------------------------------------------------------
GRADLE="app/build.gradle.kts"
EXPECTED_VERSION_NAME="1.0.0"
EXPECTED_VERSION_CODE="10000"

echo "==> [version] checking $GRADLE ..."
if grep -Eq "versionName[[:space:]]*=[[:space:]]*\"${EXPECTED_VERSION_NAME}\"" "$GRADLE"; then
    ok "versionName = \"$EXPECTED_VERSION_NAME\""
else
    fail "versionName must be \"$EXPECTED_VERSION_NAME\" in $GRADLE (found: $(grep -E 'versionName[[:space:]]*=' "$GRADLE" | tr -s ' '))"
fi
if grep -Eq "versionCode[[:space:]]*=[[:space:]]*${EXPECTED_VERSION_CODE}\b" "$GRADLE"; then
    ok "versionCode = $EXPECTED_VERSION_CODE"
else
    fail "versionCode must be $EXPECTED_VERSION_CODE in $GRADLE (§8.10: MAJOR*10000+MINOR*100+PATCH) (found: $(grep -E 'versionCode[[:space:]]*=' "$GRADLE" | tr -s ' '))"
fi

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Release metadata checks passed."
else
    echo "❌ Release metadata verification failed." >&2
fi
exit $EXIT_CODE
