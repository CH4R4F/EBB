# Changelog

All notable changes to Ebb are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-11-18

First public preview of Ebb — an ADHD-friendly day planner for Android.

### Added

- **Onboarding** — set your day boundaries, pick preferences, try capturing a task, and learn about reminders, exact alarms, and calendars.
- **Today** — scrollable timeline with scheduled tasks, calendar events, focus ring, multiselect, and **quick capture** with draft restore.
- **Inbox & Week** — capture later, triage, and see the week at a glance.
- **Routines (Repeat)** — daily, weekdays, or weekly repeats that materialise on your timeline; skip a day; turn an inbox task into a repeat.
- **Tasks** — titles, notes, subtasks, time estimates, colours/icons, reminders, and scheduling.
- **Focus** — work timers (fixed, until block end, count-up, intervals) with on-screen progress and alarms when the app is in the background.
- **Day tools** — shift the rest of the day, plan my day, overlap helper, and make it fit (from navigation).
- **Reminders** — notifications for planned tasks, rescheduled after reboot.
- **Settings** — Material You, default reminders, wake/bed summary, and app version.

### Changed

- Repeat and routine edits respect database repair and safely drain queued link/sync work before generating new days.

[Unreleased]: https://github.com/CH4R4F/EBB/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/CH4R4F/EBB/releases/tag/v0.1.0