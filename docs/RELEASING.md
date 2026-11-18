# Releasing Ebb

Ebb uses **semantic versioning** and **annotated Git tags** `vMAJOR.MINOR.PATCH`. Only **QA-passed** commits on `main` should be tagged for public releases.

## Version source of truth

Edit [`version.properties`](../version.properties):

```properties
VERSION_MAJOR=0
VERSION_MINOR=1
VERSION_PATCH=0
```

Gradle derives:

| Field | Formula |
|-------|---------|
| `versionName` | `MAJOR.MINOR.PATCH` |
| `versionCode` | `MAJOR * 10000 + MINOR * 100 + PATCH` |

Example: `0.1.0` → `versionName` `0.1.0`, `versionCode` **100**.

The in-app **Settings** screen shows `BuildConfig.VERSION_NAME`.

## Release checklist

1. **Confirm QA** has signed off on the commit you are releasing.
2. **Bump** `version.properties` (and `versionCode` implication if needed).
3. **Update** [`CHANGELOG.md`](../CHANGELOG.md):
   - Move everything under `[Unreleased]` into a new `## [X.Y.Z] - YYYY-MM-DD` section.
   - Leave a fresh `[Unreleased]` section at the top.
   - Fix comparison links at the bottom of the file.
4. **Commit** on `main` (e.g. `chore(release): prepare v0.1.0`).
5. **Tag** and push (only when ready — do not tag WIP builds):

   ```bash
   git tag -a v0.1.0 -m "Ebb 0.1.0"
   git push origin v0.1.0
   ```

6. The [**Release** workflow](.github/workflows/release.yml) runs on tag push:
   - Fails if the tag (without `v`) does not match `version.properties`.
   - Runs `assembleDebug` (debug-signed APK for now).
   - Uploads `ebb-vX.Y.Z-debug.apk` to a **non-prerelease** GitHub Release.
   - Release notes from the matching `CHANGELOG` section, or GitHub-generated notes if missing.

## CI vs releases

| Trigger | Workflow | Output |
|---------|----------|--------|
| Push to `main` | [android.yml](.github/workflows/android.yml) | `assembleDebug`, `debug-latest` + `ebb-debug-<sha>` **pre-releases** |
| Tag `v*.*.*` | [release.yml](.github/workflows/release.yml) | Versioned **release** with debug APK (until release signing) |

No unit tests run on CI per project policy.

## Future: Play Store signing

Release builds are **debug-signed** today. Before Play Console upload:

1. Create a release keystore and store credentials in GitHub Actions **secrets** (e.g. `EBB_RELEASE_KEYSTORE`, passwords, alias).
2. Extend `release.yml` to build a signed `release` variant (or `bundleRelease`).
3. Document secret names here once configured.

Do **not** commit keystore files or passwords to the repository.