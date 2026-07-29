# fastlane metadata

Google Play Store listing metadata, consumed by
[`fastlane supply`](https://docs.fastlane.tools/actions/supply/) (and compatible with the
`r0adkll/upload-google-play` GitHub Action referenced in `.github/workflows/release.yml`).

## Layout

```
fastlane/metadata/android/
  en-US/
    title.txt               # ≤ 30 characters
    short_description.txt    # ≤ 80 characters
    full_description.txt     # ≤ 4000 characters
    changelogs/10000.txt     # ≤ 500 chars; filename = versionCode (§8.10)
  ru/
    title.txt
    short_description.txt
    full_description.txt
    changelogs/10000.txt
```

Character limits are enforced by `scripts/verify-release-metadata.sh`.

## Screenshots (not in the repo yet)

Store screenshots and the feature graphic live under `en-US/images/` and `ru/images/`
(`phoneScreenshots/`, `featureGraphic.png`, `icon.png`). They are **not** committed yet — they
require a separate graphic-design pass (see the Post-Completion section of
`docs/plans/completed/2026-07-24-care-content-and-v1-release.md`). Add them before the first
production submission.

## Adding a new release

Create `changelogs/<versionCode>.txt` in each locale for the new `versionCode`
(e.g. `10001.txt` for `1.0.1`), keeping each file ≤ 500 characters.
