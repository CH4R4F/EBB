# Releasing Ebb

Ebb uses **semantic versioning** and **annotated Git tags** `vMAJOR.MINOR.PATCH`. Only **QA-passed** commits on `main` should be tagged for public releases.

## Version source of truth

Edit [`version.properties`](../version.properties):

```properties
VERSION_MAJOR=0
VERSION_MINOR=1
VERSION_PATCH=0
```

| Field | Formula |
|-------|---------|
| `versionName` | `MAJOR.MINOR.PATCH` |
| `versionCode` | `MAJOR * 10000 + MINOR * 100 + PATCH` |

Example: `0.1.0` → `versionName` `0.1.0`, `versionCode` **100**. **Settings** shows `BuildConfig.VERSION_NAME`.

## Debug vs release installs

| Variant | Application ID | Notes |
|---------|----------------|-------|
| **Debug** (CI `debug-latest`) | `app.ebbplanner.debug` | Label **Ebb Debug**, `-debug` version suffix; signed with the **shared** [`app/debug.keystore`](../app/debug.keystore) so every `debug-latest` build uses the same certificate and installs as an update (no uninstall/data loss). Safe to publish because the package id cannot replace a release install. |
| **Release** (tagged GitHub Release) | `app.ebbplanner` | R8 minify, not debuggable; signed with the **upload/release keystore** below. |

Debug APKs **cannot** install over a release build (different package name). Existing debug installs keep working as the separate debug app.

## Release signing keystore (required for tags)

**Never commit** a release/upload keystore. The only keystore in git is `app/debug.keystore` (debug builds only). Back up the **release** keystore offline — if it is lost, users must uninstall `app.ebbplanner` before installing a build signed with a new key.

### 1. Create the keystore

```bash
keytool -genkeypair -v \
  -keystore ebb-upload.jks \
  -alias ebb-upload \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storetype JKS
```

Use a strong store password and key password; record the alias (`ebb-upload` in this example).

### 2. Base64-encode for GitHub Actions

```bash
base64 -w 0 ebb-upload.jks > ebb-upload.jks.base64
# macOS: base64 -i ebb-upload.jks -o ebb-upload.jks.base64
```

### 3. GitHub Actions secrets

In the repository: **Settings → Secrets and variables → Actions → New repository secret**

| Secret name | Value |
|-------------|--------|
| `EBB_KEYSTORE_BASE64` | Full contents of `ebb-upload.jks.base64` (one line) |
| `EBB_KEYSTORE_PASSWORD` | Keystore store password |
| `EBB_KEY_ALIAS` | Key alias (e.g. `ebb-upload`) |
| `EBB_KEY_PASSWORD` | Key password |

The [**Release** workflow](../.github/workflows/release.yml) decodes the keystore to a temp file, sets `EBB_KEYSTORE_FILE`, and runs `./gradlew assembleRelease`. The job fails immediately with a clear message if any secret is missing.

### 4. Local release build (optional)

```bash
export EBB_KEYSTORE_FILE=/path/to/ebb-upload.jks
export EBB_KEYSTORE_PASSWORD='…'
export EBB_KEY_ALIAS='ebb-upload'
export EBB_KEY_PASSWORD='…'
./gradlew assembleRelease
```

Without those variables, `assembleRelease` fails with a configuration error (no debug-key fallback).

## Release checklist

1. QA sign-off on the commit.
2. Bump `version.properties` if needed.
3. Update [`CHANGELOG.md`](../CHANGELOG.md) (`[Unreleased]` → dated `## [X.Y.Z]`).
4. Commit on `main`.
5. Tag and push when ready:

   ```bash
   git tag -a v0.1.0 -m "Ebb 0.1.0"
   git push origin v0.1.0
   ```

6. [**Release** workflow](../.github/workflows/release.yml):
   - Verifies tag ↔ `version.properties`
   - Requires signing secrets
   - Publishes `ebb-vX.Y.Z.apk` (release, minified, upload-signed)

## CI vs releases

| Trigger | Workflow | Output |
|---------|----------|--------|
| Push to `main` | [android.yml](../.github/workflows/android.yml) | `assembleDebug` → `app.ebbplanner.debug`; `debug-latest` + `ebb-debug-<sha>` pre-releases |
| Tag `v*.*.*` | [release.yml](../.github/workflows/release.yml) | `assembleRelease` → `ebb-vX.Y.Z.apk` on GitHub Releases |

CI does not run unit tests per project policy.