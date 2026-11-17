# Ebb

**Ebb** is a gentle, guilt-free Android day planner built for ADHD minds. The UI follows **Material 3** and **Material You** (dynamic color), with a calm tone: no red overdue states, forgiving rescheduling, and visual time on the timeline.

This repository is in **early setup**. Application source will land under `app/` in a follow-up change. Design assets and product documentation live here now.

## Repository layout

| Path | Purpose |
|------|---------|
| [`design/`](design/) | Design gallery, screen exports, prototypes, and tooling (`design/index.html` is the main entry) |
| [`docs/`](docs/) | Product specification and other written docs |
| `app/` | *(planned)* Kotlin / Jetpack Compose Android application |

## Viewing the design gallery

Open the design root gallery in a browser (local file or any static host):

```text
design/index.html
```

Sub-galleries and build scripts live under `design/extras/` and `design/html/`. The canonical checksum list for all design deliverables is `design/MANIFEST.sha256`.

## Planned technical stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, dynamic color (Material You) |
| Architecture | MVVM with coroutines and `Flow` |
| DI | Hilt |
| Persistence | Room, DataStore |
| Background / reminders | WorkManager, AlarmManager |
| Widgets | Glance |
| Build | Gradle Kotlin DSL with a version catalog |
| SDK | min **26**, target **35** |

## Package name (placeholder)

The intended application id **`app.ebbplanner`** is a **PLACEHOLDER**. The product name *Ebb* has not yet been checked for trademark or Google Play naming conflicts. Keep the application id and Gradle namespace easy to rename until legal and store checks are complete.

## Known design gaps (vs. implementation)

These are intentional or pending in the current design drop; implementation should close the gaps:

- **No tablet layouts** yet — phone-first only.
- **No motion / animation spec files** and **no redlines** in this delivery.
- **Home-screen widgets**: designs use **44 dp** minimum tap targets; Android accessibility and Play guidance expect **48 dp** minimum in code — implement **48 dp** even where mockups show 44 dp.

## License

To be decided with the first public app release (GPL-3.0 suggested in the product spec).