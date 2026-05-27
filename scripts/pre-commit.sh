#!/usr/bin/env bash
# pre-commit hook for PawClock — see spec §8.6.
#
# Fast feedback loop: only formatting + the calculator TDD canary.
# Full unit tests + Android Lint stay in CI to keep the commit cycle snappy.
#
# Install:
#   pre-commit framework  ─→ pre-commit install (uses .pre-commit-config.yaml if added)
#   or manual             ─→ ln -sf ../../scripts/pre-commit.sh .git/hooks/pre-commit
#                            chmod +x .git/hooks/pre-commit
#
# Bypass (rare, document why):
#   git commit --no-verify

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

# Use the gradle daemon for speed inside a hook (subsequent invocations are warm).
# CI explicitly uses --no-daemon for repeatability; the hook is dev-loop, not CI.
GRADLE_OPTS="${GRADLE_OPTS:-}"
GRADLEW=./gradlew

if [ ! -x "$GRADLEW" ]; then
    echo "❌ gradlew is not executable at $GRADLEW" >&2
    exit 1
fi

echo "==> ktlintFormat + detekt"
"$GRADLEW" ktlintFormat detekt --daemon

# Re-stage anything ktlintFormat may have rewritten.
# Only re-add files already staged — never sneak in unrelated working-tree changes.
STAGED=$(git diff --cached --name-only --diff-filter=ACMR | tr '\n' ' ')
if [ -n "$STAGED" ]; then
    # shellcheck disable=SC2086
    git add -- $STAGED
fi

echo "==> :core:calculator:test (TDD canary)"
"$GRADLEW" :core:calculator:test --daemon

echo "✅ pre-commit hook passed."
