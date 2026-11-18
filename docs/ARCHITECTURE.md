# Ebb — architecture overview

This document complements the [README](../README.md) with layering and data-flow detail. The authoritative product scope remains [`ADHD_Day_Planner_Product_Spec.md`](ADHD_Day_Planner_Product_Spec.md).

## Layers

### UI (`app.ebbplanner.ui`)

- **Compose** screens and design-system components (`ui.theme`, shared chrome).
- **MVVM**: `ViewModel` + `StateFlow` / `Flow`; Hilt-injected repositories and use cases.
- **Navigation**: `EbbNavHost` — onboarding graph, then `MainShell` tabs (Today, Inbox, Week, Routines) plus flow routes (task detail, focus, settings, day tools).

### Domain (`app.ebbplanner.domain`)

- Pure(ish) logic: focus timer, day planning/shift math, quick-capture parsing, routine instance generation orchestration.
- **Routine generation** is serialized through `RoutineGenerationGate` to avoid duplicate instances; cooperates with `RoutineOccurrenceRepairRunner` on startup.

### Data (`app.ebbplanner.data`)

- **Room** — `EbbDatabase` (v12): tasks, subtasks, routines, routine skips, day plans, reminders; raw SQL triggers for live routine-occurrence uniqueness.
- **DAOs / entities** under `data.local`; migrations in `EbbMigrations.kt`.
- **Repositories** — `TaskRepository`, `RoutineRepository`, preferences (`UserPreferencesRepository`, `FocusSessionStore`, repair/deferred-work DataStore).
- **Reminders** — scheduling, boot receiver, notification actions.

### DI (`app.ebbplanner.di`)

Hilt modules: database, repositories, clock, WorkManager, etc.

## Key runtime flows

### Cold start

1. `EbbApplication` schedules `RoutineGenerationWorker`.
2. `AppEntryViewModel` runs `RoutineOccurrenceRepairRunner` (pending repair, deferred link/sync, then rolling window).
3. Onboarding vs main UI from `UserPreferencesRepository.onboardingComplete`.

### Routine repeat

1. Enabled routines → `RoutineInstanceGenerator.ensureRollingWindow`.
2. Inserts use `INSERT OR IGNORE` + SQLite triggers to block duplicate `(sourceRoutineId, routineOccurrenceDate)` live rows.
3. Edits call `syncFutureInstances`; “Repeat from task” uses `linkExistingTask`.

### Focus

1. `FocusRunViewModel` restores `FocusSessionStore` or loads task block end.
2. `FocusTimerController` supports block-until-end, fixed, count-up, intervals; `FocusAlarmScheduler` for background expiry.

## Package map (main sources)

```
app.ebbplanner/
├── data/          # local (Room), preferences, repositories
├── domain/        # focus, routines, day, capture, time
├── di/
├── reminders/
├── ui/
│   ├── today/     # timeline, quick capture, chrome
│   ├── inbox/ week/ routines/ taskdetail/ focus/
│   ├── daytools/  # shift, plan, overlap, make-it-fit
│   ├── onboarding/ settings/ navigation/
│   └── theme/
└── MainActivity.kt EbbApplication.kt
```

## Testing strategy

- **Unit / Robolectric** — ViewModels, migrations sanity, parsers.
- **Paparazzi** — visual regression per screen family under `app/src/test/snapshots/`.
- **CI** — `./gradlew assembleDebug` on `main` (see `.github/workflows/android.yml`).