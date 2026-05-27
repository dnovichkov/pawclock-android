#!/usr/bin/env bash
# verify-bundle-size.sh — placeholder bundle-size gate per Plan 1 Task 3.
#
# Spec §8.5.2 requires the AAB (release bundle) to stay under 15 MB.
# Real enforcement runs from release.yml (Task 4) after the bundleRelease
# task produces app/build/outputs/bundle/release/*.aab.
#
# This script:
#   • picks the most recent .aab in app/build/outputs/bundle/**/ if present
#   • compares its size against the limit (default 15 MB, override via env)
#   • exits 0 if no bundle exists yet (so it does not block local dev runs)
#
# Usage:
#   ./scripts/verify-bundle-size.sh                  # uses default 15 MB ceiling
#   BUNDLE_SIZE_LIMIT_MB=8 ./scripts/verify-bundle-size.sh   # release-track ceiling

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

LIMIT_MB="${BUNDLE_SIZE_LIMIT_MB:-15}"
LIMIT_BYTES=$((LIMIT_MB * 1024 * 1024))

# Find most recent .aab (any build type). Empty array if none.
BUNDLE_PATH=""
if compgen -G "app/build/outputs/bundle/**/*.aab" > /dev/null; then
    # shellcheck disable=SC2012
    BUNDLE_PATH=$(ls -t app/build/outputs/bundle/**/*.aab 2>/dev/null | head -n 1 || true)
fi

if [ -z "$BUNDLE_PATH" ]; then
    echo "ℹ️  No .aab found under app/build/outputs/bundle/. Skipping size check."
    echo "    (Run ./gradlew :app:bundleRelease to produce one.)"
    exit 0
fi

ACTUAL_BYTES=$(stat -c '%s' "$BUNDLE_PATH" 2>/dev/null || stat -f '%z' "$BUNDLE_PATH")
ACTUAL_MB=$(awk "BEGIN { printf \"%.2f\", $ACTUAL_BYTES / (1024*1024) }")

echo "📦 Bundle: $BUNDLE_PATH"
echo "📐 Size:   ${ACTUAL_MB} MB  (limit ${LIMIT_MB} MB)"

if [ "$ACTUAL_BYTES" -gt "$LIMIT_BYTES" ]; then
    echo "❌ Bundle exceeds ${LIMIT_MB} MB limit." >&2
    exit 1
fi

echo "✅ Bundle within budget."
