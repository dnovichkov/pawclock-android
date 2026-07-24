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

# ---------------------------------------------------------------------------
# Task 15 — privacy policy (§9): "No data collected / No data shared"
# ---------------------------------------------------------------------------
PRIVACY="docs/PRIVACY.md"
echo "==> [privacy] checking $PRIVACY ..."
if [ ! -s "$PRIVACY" ]; then
    fail "$PRIVACY is missing or empty (§9 Data Safety statement required)"
else
    for phrase in "No data collected" "No data shared"; do
        if grep -qF "$phrase" "$PRIVACY"; then
            ok "privacy states \"$phrase\""
        else
            fail "$PRIVACY must state \"$phrase\" (§9 Google Play Data Safety)"
        fi
    done
    if grep -qiE "no (internet|network)|INTERNET" "$PRIVACY"; then
        ok "privacy notes absence of network/INTERNET permission"
    else
        fail "$PRIVACY should note the absence of the INTERNET permission (§9)"
    fi
fi

# ---------------------------------------------------------------------------
# Task 16 — fastlane metadata (Google Play) + character limits
# ---------------------------------------------------------------------------
FASTLANE="fastlane/metadata/android"

# charlen FILE -> prints the Unicode character count with a trailing newline stripped.
# Uses node for accurate code-point length; falls back to `wc -m` (byte-ish) otherwise.
charlen() {
    local f="$1"
    if [ "${HAS_NODE:-0}" -eq 1 ] || command -v node >/dev/null 2>&1; then
        FILE="$f" node -e 'process.stdout.write(String(require("fs").readFileSync(process.env.FILE,"utf8").replace(/\s+$/,"").length))'
    else
        printf '%s' "$(cat "$f")" | wc -m | tr -d ' '
    fi
}

# check_limit FILE MAX LABEL
check_limit() {
    local f="$1" max="$2" label="$3"
    if [ ! -s "$f" ]; then
        fail "$label missing or empty: $f"
        return
    fi
    local len
    len=$(charlen "$f")
    if [ "$len" -le "$max" ]; then
        ok "$label ok ($len ≤ $max): $f"
    else
        fail "$label too long ($len > $max): $f"
    fi
}

echo "==> [fastlane] checking $FASTLANE ..."
for locale in en-US ru; do
    dir="$FASTLANE/$locale"
    if [ ! -d "$dir" ]; then
        fail "missing fastlane locale directory: $dir"
        continue
    fi
    check_limit "$dir/title.txt" 30 "title[$locale]"
    check_limit "$dir/short_description.txt" 80 "short_description[$locale]"
    check_limit "$dir/full_description.txt" 4000 "full_description[$locale]"
    check_limit "$dir/changelogs/10000.txt" 500 "changelog[$locale]"
done

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Release metadata checks passed."
else
    echo "❌ Release metadata verification failed." >&2
fi
exit $EXIT_CODE
