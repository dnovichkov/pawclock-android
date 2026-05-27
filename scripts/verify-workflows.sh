#!/usr/bin/env bash
# verify-workflows.sh — sanity-check GitHub Actions YAML.
#
# Tries (in order):
#   1. actionlint            — best validator; covers expression syntax + action versions
#   2. yamllint -s           — strict YAML grammar only
#   3. python3 yaml.safe_load — minimal parse check
#   4. file-existence check  — last-resort fallback so the script still runs cross-platform
#
# Exit codes: 0 = ok; 1 = at least one workflow failed validation.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

WORKFLOW_DIR=".github/workflows"

if [ ! -d "$WORKFLOW_DIR" ]; then
    echo "❌ Missing $WORKFLOW_DIR" >&2
    exit 1
fi

shopt -s nullglob
WORKFLOWS=("$WORKFLOW_DIR"/*.yml "$WORKFLOW_DIR"/*.yaml)
shopt -u nullglob

if [ ${#WORKFLOWS[@]} -eq 0 ]; then
    echo "❌ No workflow files found under $WORKFLOW_DIR" >&2
    exit 1
fi

echo "Found ${#WORKFLOWS[@]} workflow file(s):"
for f in "${WORKFLOWS[@]}"; do echo "  - $f"; done
echo ""

if command -v actionlint >/dev/null 2>&1; then
    echo "==> actionlint"
    actionlint "${WORKFLOWS[@]}"
    echo "✅ actionlint clean"
    exit 0
fi

if command -v yamllint >/dev/null 2>&1; then
    echo "==> yamllint -s (actionlint not found)"
    yamllint -s "${WORKFLOWS[@]}"
    echo "✅ yamllint clean"
    exit 0
fi

if command -v python3 >/dev/null 2>&1 || command -v python >/dev/null 2>&1; then
    PY=$(command -v python3 || command -v python)
    # Probe pyyaml first — fall through to grep fallback if not installed.
    if "$PY" - <<'PYEOF' 2>/dev/null
import yaml  # noqa: F401
PYEOF
    then
        echo "==> python YAML parse check (actionlint/yamllint not found)"
        for f in "${WORKFLOWS[@]}"; do
            "$PY" - "$f" <<'PYEOF'
import sys
import yaml
path = sys.argv[1]
with open(path, "r", encoding="utf-8") as fh:
    yaml.safe_load(fh)
print(f"  parsed: {path}")
PYEOF
        done
        echo "✅ python parse check passed"
        exit 0
    fi
fi

# Final fallback — only verify required keys exist textually.
echo "⚠️  No validator available (actionlint / yamllint / python+yaml). Falling back to grep checks." >&2
FAIL=0
for f in "${WORKFLOWS[@]}"; do
    for required in '^name:' '^on:' '^jobs:'; do
        if ! grep -qE "$required" "$f"; then
            echo "  ❌ $f missing required key: $required" >&2
            FAIL=1
        fi
    done
done
if [ "$FAIL" -ne 0 ]; then
    exit 1
fi
echo "✅ fallback grep check passed"
