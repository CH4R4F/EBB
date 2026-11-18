# Changelog

All notable changes to Ebb are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Onboarding flow: day shape (wake/bed), preferences, try-it capture, notification and exact-alarm education, calendar intro.
- **Today** timeline with scheduled and anytime tasks, calendar-event blocks, gaps, ring progress, multiselect, and quick capture with draft restore.
- **Inbox** and **Week** for triage and week-at-a-glance planning.
- **Routines (Repeat)** with daily/weekdays/weekly rules, rolling 14-day instance generation, skip rows, and link-from-task.
- **Task detail**: subtasks, estimates, color/icon, reminders, schedule placement, routine detach.
- **Focus** sessions: block-until-end, fixed minutes, count-up, and interval modes with in-app ring/bar and background alarms.
- **Day tools** navigation: shift the rest of the day, plan my day, overlap helper, make it fit.
- Reminders via `AlarmManager`, boot reschedule, and notification actions.
- **Settings**: Material You toggle, default reminders, wake/bed display, app version from `BuildConfig`.
- Room database (v12) with data-safe upgrade path, routine-occurrence repair, deferred routine work queue, and SQLite triggers for occurrence uniqueness.

### Changed

- Routine generation is gated and repair-aware; deferred link/sync drains before instance generation.

## [0.1.0] - TBD

_First tagged release — move items from [Unreleased] here when releasing._

[Unreleased]: https://github.com/CH4R4F/EBB/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/CH4R4F/EBB/releases/tag/v0.1.0