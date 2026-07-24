#!/usr/bin/env bash
# Verifies that care-recommendation JSON assets contain REAL (non-placeholder) content
# for the set of "content-complete" species (Plan 3, Task 1).
#
# This is the shell mirror of CareContentQualityTest (:core:domain). It complements
# verify-care-assets.sh (which checks *structure & presence* for ALL species) by checking
# *content readiness* for a given set of species:
#   1. no field contains a `TODO` marker (case-insensitive) — the decisive done-signal
#   2. the mandatory §3.3 disclaimer is present verbatim (per locale)
#   3. min-length: stage_description >= 120, other text fields >= 40, dental_care >= 30
#      (dental_care must be absent for `fish`; present for all other species)
#   4. source_url is a valid http(s) URL
#
# Which species are checked:
#   - with NO args: the CONTENT_COMPLETE[] array below (grows once per content task, mirrors
#     the `contentComplete` registry in CareContentQualityTest.kt). Empty at Task 1 → passes.
#   - with args: the given species ids (used to prove the gate catches placeholders, e.g.
#     `verify-care-content.sh dog` fails while dog is still a placeholder).
#
# Full validation (min-length, disclaimer-exact, dental rule, url) requires `node`; without it
# the script gracefully degrades to no-TODO + disclaimer-substring checks only.
#
# Exit codes: 0 = all checked species have ready content; 1 = a violation was found.

set -euo pipefail

cd "$(dirname "$0")/.."

CARE_ROOT="app/src/main/assets/care"

# Content-complete species — mirror of contentComplete in CareContentQualityTest.kt.
# Grows by one entry per content task (Tasks 2–13). Empty at Task 1.
CONTENT_COMPLETE=(
    "dog"
    "cat"
    "rabbit"
    "hamster"
    # Task 6+: guinea_pig rat mouse ferret bird reptile horse fish
)

# species_id|stage1,stage2,...  (source of truth: LifeStage.*.all() in :core:model)
declare -A STAGES=(
    ["dog"]="puppy,young_adult,mature_adult,senior,end_of_life"
    ["cat"]="kitten,young_adult,mature_adult,senior,end_of_life"
    ["rabbit"]="infancy,adolescence,young_adult,adult,senior"
    ["hamster"]="pup,juvenile,adult,senior,very_senior"
    ["guinea_pig"]="pup,juvenile,adult,senior,geriatric"
    ["rat"]="pup,juvenile,adult,senior,end_of_life"
    ["mouse"]="pup,juvenile,adult,senior,end_of_life"
    ["ferret"]="kit,juvenile,adult,senior,geriatric"
    ["bird"]="hatchling,juvenile,adult,senior,geriatric"
    ["reptile"]="hatchling,juvenile,adult,senior"
    ["horse"]="foal,yearling,young_adult,adult,senior"
    ["fish"]="fry,juvenile,adult,senior"
)

LOCALES=("ru" "en")

# Canonical §3.3 disclaimer substrings (per locale) — used for the node-less fallback path.
declare -A DISCLAIMER_SUBSTR=(
    ["ru"]="не заменяет консультацию ветеринарного врача"
    ["en"]="does not replace consultation with a veterinarian"
)

# Species to check: CLI args override the CONTENT_COMPLETE array.
if [ "$#" -gt 0 ]; then
    TARGET_SPECIES=("$@")
else
    TARGET_SPECIES=("${CONTENT_COMPLETE[@]}")
fi

HAS_NODE=0
if command -v node >/dev/null 2>&1; then
    HAS_NODE=1
fi

EXIT_CODE=0
TOTAL=0

# Node-based full validator for one file. Args passed via env to avoid quoting/unicode issues.
check_with_node() {
    FILE="$1" LOCALE="$2" SPECIES="$3" node <<'NODE'
const fs = require('fs');
const { FILE, LOCALE, SPECIES } = process.env;

const CANON = {
  ru: "Информация носит ознакомительный характер и не заменяет консультацию ветеринарного врача.",
  en: "This information is for educational purposes only and does not replace consultation with a veterinarian.",
};
const MIN = { stage_description: 120, nutrition: 40, activity: 40, veterinary_check_frequency: 40, warning_signs: 40 };

let o;
try {
  o = JSON.parse(fs.readFileSync(FILE, 'utf8'));
} catch (e) {
  console.error(`  ❌ ${FILE}: invalid JSON (${e.message})`);
  process.exit(1);
}

const violations = [];
const strFields = ['stage_description','nutrition','activity','veterinary_check_frequency','warning_signs','source_url','source_name','disclaimer'];
if (o.dental_care != null) strFields.push('dental_care');

for (const f of strFields) {
  const v = o[f];
  if (typeof v === 'string' && /todo/i.test(v)) violations.push(`field '${f}' still contains a TODO placeholder`);
}

if (o.disclaimer !== CANON[LOCALE]) violations.push(`disclaimer must equal the canonical §3.3 text verbatim`);

for (const [f, min] of Object.entries(MIN)) {
  const len = (o[f] || '').trim().length;
  if (len < min) violations.push(`field '${f}' too short (${len} < ${min})`);
}

if (SPECIES === 'fish') {
  if (o.dental_care != null) violations.push(`fish dental_care must be null (not applicable)`);
} else {
  const d = (o.dental_care || '').trim();
  if (d.length < 30) violations.push(`dental_care too short or missing (${d.length} < 30)`);
}

if (!/^https?:\/\/\S+$/.test(o.source_url || '')) violations.push(`source_url is not a valid http(s) URL`);
if (!(o.source_name || '').trim()) violations.push(`source_name is blank`);

if (violations.length) {
  console.error(`  ❌ ${FILE}:`);
  for (const v of violations) console.error(`       - ${v}`);
  process.exit(1);
}
process.exit(0);
NODE
}

# Fallback (no node): no-TODO + disclaimer-substring only.
check_without_node() {
    local file="$1" locale="$2"
    local ok=0
    if grep -iq "todo" "$file"; then
        echo "  ❌ $file: contains a TODO placeholder" >&2
        ok=1
    fi
    if ! grep -qF "${DISCLAIMER_SUBSTR[$locale]}" "$file"; then
        echo "  ❌ $file: missing canonical §3.3 disclaimer ($locale)" >&2
        ok=1
    fi
    return $ok
}

echo "==> Verifying care CONTENT readiness in $CARE_ROOT ..."
if [ "${#TARGET_SPECIES[@]}" -eq 0 ]; then
    echo "  ℹ️  No content-complete species yet (empty set) — nothing to check."
fi

for species in "${TARGET_SPECIES[@]}"; do
    stages="${STAGES[$species]:-}"
    if [ -z "$stages" ]; then
        echo "  ❌ Unknown species id: $species" >&2
        EXIT_CODE=1
        continue
    fi
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
            if [ "$HAS_NODE" -eq 1 ]; then
                if ! check_with_node "$file" "$locale" "$species"; then
                    EXIT_CODE=1
                fi
            else
                if ! check_without_node "$file" "$locale"; then
                    EXIT_CODE=1
                fi
            fi
        done
    done
done

echo ""
if [ "$HAS_NODE" -eq 0 ]; then
    echo "  ℹ️  node not found — only no-TODO + disclaimer checks ran (min-length/url skipped)."
fi
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Content readiness OK for ${#TARGET_SPECIES[@]} species ($TOTAL files checked)."
else
    echo "❌ Care content verification failed." >&2
fi

exit $EXIT_CODE
