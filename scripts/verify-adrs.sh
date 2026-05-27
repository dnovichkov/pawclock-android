#!/usr/bin/env bash
# Verifies that all required Architecture Decision Records exist in docs/adr/
# and contain the mandatory MADR sections.
#
# Checks per spec §8.11:
#   1. template-madr.md exists
#   2. All 7 ADRs (0001..0007) exist with exact filenames from spec
#   3. Each ADR contains "Status", "Context", "Decision Outcome", "Consequences"
#
# Exit codes: 0 = ok; 1 = missing file or missing section.

set -euo pipefail

cd "$(dirname "$0")/.."

ADR_DIR="docs/adr"

REQUIRED_ADRS=(
    "0001-jetpack-compose-over-views.md"
    "0002-multi-module-architecture.md"
    "0003-tdd-as-required-practice.md"
    "0004-room-over-sqldelight.md"
    "0005-no-network-permission.md"
    "0006-wang-et-al-formula-as-default-for-dogs.md"
    "0007-conventional-commits.md"
)

# Sections required by MADR 3.0 (template-madr.md). We check core ones.
# Note: "Status" is on a metadata line (- **Status**: ...), not a heading,
# so we accept both `## Status` and `**Status**:` patterns.
REQUIRED_SECTIONS=(
    "Status"
    "Context and Problem Statement"
    "Decision Outcome"
    "Consequences"
)

EXIT_CODE=0

echo "==> Checking template-madr.md exists ..."
if [ ! -f "$ADR_DIR/template-madr.md" ]; then
    echo "  ❌ Missing $ADR_DIR/template-madr.md" >&2
    EXIT_CODE=1
else
    echo "  ✅ template-madr.md present"
fi

echo ""
echo "==> Checking 7 ADRs exist ..."
for adr in "${REQUIRED_ADRS[@]}"; do
    if [ ! -f "$ADR_DIR/$adr" ]; then
        echo "  ❌ Missing $ADR_DIR/$adr" >&2
        EXIT_CODE=1
    else
        echo "  ✅ $adr"
    fi
done

echo ""
echo "==> Checking required sections in each ADR ..."
for adr in "${REQUIRED_ADRS[@]}"; do
    FILE="$ADR_DIR/$adr"
    if [ ! -f "$FILE" ]; then
        continue
    fi
    MISSING_SECTIONS=()
    for section in "${REQUIRED_SECTIONS[@]}"; do
        # Accept any heading level (## / ###) or metadata form (**Section**:).
        # "Consequences" matches both `### Positive Consequences` and `## Consequences`.
        if ! grep -qE "(^#+[[:space:]].*$section|\*\*$section\*\*)" "$FILE"; then
            MISSING_SECTIONS+=("$section")
        fi
    done
    if [ ${#MISSING_SECTIONS[@]} -ne 0 ]; then
        echo "  ❌ $adr is missing sections: ${MISSING_SECTIONS[*]}" >&2
        EXIT_CODE=1
    else
        echo "  ✅ $adr has all required sections"
    fi
done

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ All ${#REQUIRED_ADRS[@]} ADRs validated."
else
    echo "❌ ADR validation failed." >&2
fi

exit $EXIT_CODE
