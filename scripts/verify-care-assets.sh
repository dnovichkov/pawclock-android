#!/usr/bin/env bash
# Verifies that placeholder care-recommendation JSON assets exist for every
# implemented species × life stage × locale (Plan 2, Task 13).
#
# For each implemented species (mirrors Species.implemented() / LifeStage.*.all()
# in :core:model) it checks that app/src/main/assets/care/{species}/{stage}/{ru,en}.json:
#   1. exists and is non-empty
#   2. contains the mandatory disclaimer key (§3.3)
#   3. contains all required CareRecommendation fields
#   4. is valid JSON (only when `node` is available — skipped gracefully otherwise)
#
# Exit codes: 0 = ok; 1 = missing file / missing field / invalid JSON.

set -euo pipefail

cd "$(dirname "$0")/.."

CARE_ROOT="app/src/main/assets/care"
EXIT_CODE=0

# species_id|stage1,stage2,...  (source of truth: LifeStage.*.all() in :core:model)
SPECIES_STAGES=(
    "dog|puppy,young_adult,mature_adult,senior,end_of_life"
    "cat|kitten,young_adult,mature_adult,senior,end_of_life"
    "rabbit|infancy,adolescence,young_adult,adult,senior"
    "hamster|pup,juvenile,adult,senior,very_senior"
    "guinea_pig|pup,juvenile,adult,senior,geriatric"
    "rat|pup,juvenile,adult,senior,end_of_life"
    "mouse|pup,juvenile,adult,senior,end_of_life"
    "ferret|kit,juvenile,adult,senior,geriatric"
    "bird|hatchling,juvenile,adult,senior,geriatric"
    "reptile|hatchling,juvenile,adult,senior"
    "horse|foal,yearling,young_adult,adult,senior"
    "fish|fry,juvenile,adult,senior"
)

LOCALES=("ru" "en")

# Required CareRecommendation fields (dental_care is optional → not checked).
REQUIRED_FIELDS=(
    "stage_description"
    "nutrition"
    "activity"
    "veterinary_check_frequency"
    "warning_signs"
    "source_url"
    "source_name"
    "disclaimer"
)

HAS_NODE=0
if command -v node >/dev/null 2>&1; then
    HAS_NODE=1
fi

TOTAL=0
echo "==> Verifying care assets in $CARE_ROOT ..."
for row in "${SPECIES_STAGES[@]}"; do
    IFS='|' read -r species stages <<< "$row"
    IFS=',' read -ra STAGE_ARR <<< "$stages"
    for stage in "${STAGE_ARR[@]}"; do
        for locale in "${LOCALES[@]}"; do
            file="$CARE_ROOT/$species/$stage/$locale.json"
            TOTAL=$((TOTAL + 1))

            if [ ! -s "$file" ]; then
                echo "  ❌ Missing or empty: $file" >&2
                EXIT_CODE=1
                continue
            fi

            missing_fields=()
            for field in "${REQUIRED_FIELDS[@]}"; do
                if ! grep -q "\"$field\"" "$file"; then
                    missing_fields+=("$field")
                fi
            done
            if [ ${#missing_fields[@]} -ne 0 ]; then
                echo "  ❌ $file missing fields: ${missing_fields[*]}" >&2
                EXIT_CODE=1
                continue
            fi

            if [ "$HAS_NODE" -eq 1 ]; then
                if ! node -e "JSON.parse(require('fs').readFileSync('$file','utf8'))" 2>/dev/null; then
                    echo "  ❌ Invalid JSON: $file" >&2
                    EXIT_CODE=1
                    continue
                fi
            fi
        done
    done
done

echo ""
if [ "$HAS_NODE" -eq 0 ]; then
    echo "  ℹ️  node not found — JSON syntax validation skipped (field presence still checked)."
fi
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ All $TOTAL care assets present and well-formed."
else
    echo "❌ Care asset verification failed." >&2
fi

exit $EXIT_CODE
