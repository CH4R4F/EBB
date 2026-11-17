# Ebb

**Ebb** is a gentle, guilt-free Android day planner built for ADHD minds. The UI follows **Material 3** and **Material You** (dynamic color), with a calm tone: no red overdue states, forgiving rescheduling, and visual time on the timeline.

## Repository layout

| Path | Purpose |
|------|---------|
| [`app/`](app/) | Kotlin / Jetpack Compose Android application (Milestone 1 foundation in progress) |
| [`design/`](design/) | Design gallery, screen exports, prototypes (`design/index.html`) |
| [`docs/`](docs/) | Product specification and written docs |

## Build and run

**Requirements:** JDK **17**, Android SDK **API 35** + Build Tools **35.0.0**.

```bash
export JAVA_HOME=/path/to/jdk-17
# local.properties is gitignored; Gradle also respects ANDROID_HOME / ANDROID_SDK_ROOT
echo "sdk.dir=$ANDROID_HOME" > local.properties   # optional

./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
./gradlew installDebug   # device or emulator attached
```

Release signing is not configured yet.

Regenerate theme tokens after `design/system/tokens.json` changes:

```bash
python3 tools/generate_ebb_tokens.py
```

## Milestone roadmap

| Milestone | Focus |
|---------|--------|
| **M1 (current)** | Foundation — Gradle project, tokens/theme, Room + DataStore skeleton, onboarding, Today + quick capture, CI |
| **M2** | Reminders, notifications, exact-alarm permission flow |
| **M3** | Remaining screens per [`design/COVERAGE.md`](design/COVERAGE.md) |
| **M4** | Glance widgets, quick settings tile, shortcuts |
| **M5** | Polish, accessibility audit, Play listing assets |

## Technical stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, dynamic color |
| Architecture | MVVM, coroutines / `Flow`, Hilt |
| Persistence | Room, DataStore |
| SDK | min **26**, target / compile **35** |

## Package name (placeholder)

The application id **`app.ebbplanner`** is defined once in [`gradle.properties`](gradle.properties) (`ebb.applicationId`) and is a **PLACEHOLDER** until trademark and Play Store name checks complete. Rename there (and sync namespace if needed).

## Design vs implementation

- Compare implementation to PNG/HTML under `design/screens/` and `design/html/`.
- **Touch targets:** design widgets use **44 dp**; code enforces **48 dp** minimum (`EbbSpacing.touchMin`).
- **Known design gaps:** no tablet layouts, motion/redline deliverables pending (see product spec).

## Viewing the design gallery

Open [`design/index.html`](design/index.html) in a browser. Checksums: `design/MANIFEST.sha256`.

## License

To be decided with the first public release (GPL-3.0 suggested in the product spec).