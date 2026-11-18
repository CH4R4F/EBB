# Changelog

All notable changes to Ebb are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- **Today timeline (overlap)** — removed horizontal scroll; per-overlap-group lane caps with font-scaled minimum width; overflow tasks open via tappable `+N` sheet; track uses full timeline height; draw-time overlap uses dp-accurate min heights so lane assignment matches painted blocks (Kilo/Lima at 2.0); non-overlapping groups keep full width.
- **Settings & chrome** — zero window insets on Settings `Scaffold`/`TopAppBar`; removed duplicate Today ⋮ (settings gear only).
- **Timeline taps** — time-left pill no longer steals clicks; bottom-edge / overflow reachability UI tests and Paparazzi at 1.0 and 2.0.
- **Font scale 2.0** — week day labels, hour rail, make-it-fit meters, onboarding wake/bed/breather chips; extra Paparazzi shots.
- **Week** — header `softWrap=false`; empty days no longer show duplicate “Open”; load label hidden when day is open.
- **Inbox** — empty message when search has no matches.
- **Shift** — empty state when nothing to move; primary action disabled.
- **Focus** — setup shows minutes remaining in the current block, not full task length.
- **Week view** — changing week start re-anchors to the current week; navigation keeps the selected week offset (`WeekAnchor`, `WeekViewModel`).
- **Insets** — tab Scaffolds use `contentWindowInsets = WindowInsets(0)` so MainShell does not double-apply system bar padding.
- **Task sheets** — duration chips and color/icon picker use FlowRow; icons render via `EbbSymbol` (not raw names).
- **Day tools** — Shift and Overlap previews use real task times; overlap hatch tracks actual overlap window.
- **Large text** — week header compact range + two-line header at 2.0; onboarding wake time, notification actions (FlowRow), Skip min-width; overlap tool time rail; tall timeline block titles top-aligned; Paparazzi font scale 2.0 QA shots.
- **Inbox** — working search toggle/query and “Quick ≤15 min” filter; removed inert sort/menu actions.
- **Onboarding** — “Not now” on reminders persisted; removed dead Try-it Schedule control.
- **Today** — quick capture footer matches save target; duplicate estimate chip guarded; anytime “+ add” stays visible; overlapping timeline tasks lay out side by side.
- **Quick capture** — single interactive sheet (one field, one Add); working estimate chip; ModalBottomSheet drag handle only.
- **Navigation** — removed duplicate bottom bars on Inbox, Week, and Routines (MainShell owns the real bar).
- **Week start** — persisted DataStore preference with onboarding picker and Settings row; Week view respects it.
- **Today timeline** — visible hours expand to fit scheduled tasks and events (minimum 09:00–17:00).
- **Anytime tray** — normal chip height; “+ add” opens quick capture for an anytime task.
- **Day tool sheets** — Shift, Overlap, and Make it fit no longer render a fake Today screen behind the sheet.

## [0.1.0] - 2026-11-18

First public preview of Ebb — an ADHD-friendly day planner for Android.

### Added

- **Onboarding** — set your day boundaries, pick preferences, try capturing a task, and learn about reminders, exact alarms, and calendars.
- **Today** — scrollable timeline with scheduled tasks, calendar events, day progress ring, multiselect, and **quick capture** with draft restore.
- **Inbox & Week** — capture later, triage, and see the week at a glance.
- **Routines (Repeat)** — daily, weekdays, or weekly repeats on your timeline; remove or move an instance to skip that day; set up **Repeat** from **Task detail** for any task.
- **Tasks** — titles, notes, subtasks, time estimates, colours/icons, reminders, and scheduling.
- **Focus** — work timers (fixed, until block end, count-up, intervals) with on-screen progress and alarms when the app is in the background.
- **Day tools** — shift the rest of the day, plan my day, overlap helper, and make it fit (from navigation).
- **Reminders** — notifications for planned tasks, rescheduled after reboot.
- **Settings** — Material You, default reminders, wake/bed summary, and app version.

[Unreleased]: https://github.com/CH4R4F/EBB/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/CH4R4F/EBB/releases/tag/v0.1.0