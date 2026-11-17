# Ebb — design extras coverage (spec §9.3 / §9.4 / §9.5)

Everything is in `design/extras/`. The gallery is in `index.html`. Every PNG is rendered at 2× from HTML/CSS (or SVG) sources kept next to it in `*/html/`. Rebuild with `build/run.sh` (needs node, puppeteer-core and Chrome).
Tokens are the §6 values (the same hex as `design/system/tokens.css`). Fonts are Lexend, Atkinson Hyperlegible Next and Mono, plus Material Symbols Rounded, bundled in `assets/fonts/`.
Sample data used everywhere: Mon 16 Nov, now 13:48. The current block is "Emails" (12 min left) and the next is "14:00 Call bank" (pinned).

## §9.3 Widgets: 8/8 widgets, 19/19 size layouts ✅

The naming pattern is `widgets/png/<W>/<W>-<size>-<state>-<theme>.png`. Each layout is rendered in **every listed state in light and dark**. The default state is also rendered in **dynamic colour (purple wallpaper) light and dark** and in **50% transparent background, light and dark** (composited over the wallpaper).

| Item | Layouts (spec) | Files (sizes) | States rendered | ✓ |
|---|---|---|---|---|
| W1 Now & Next (MVP) | 2×1, 4×1, 4×2 | `W1-2x1-*`, `W1-4x1-*`, `W1-4x2-*` | now active, free gap, nothing planned, day finished, hidden titles, stale/tap to refresh | ✅ |
| W2 Today timeline (MVP) | 4×2 strip, 4×3–4×4 list, 5×4+ | `W2-4x2-*`, `W2-4x4-*` (list), `W2-5x4-*` (with Anytime tray) | populated, empty, all done, loading (skeleton), after bedtime | ✅ |
| W3 Quick capture (MVP) | 1×1, 2×1, 4×1 | `W3-1x1-*`, `W3-2x1-*`, `W3-4x1-*` | default, voice unavailable | ✅ |
| W4 Focus timer (MVP) | 2×2, 4×2 | `W4-2x2-*`, `W4-4x2-*` (presets 5·15·25·45 + interval dots) | idle, running, paused, break, finished, + no task ("Just 5 minutes") | ✅ |
| W5 Inbox glance (MVP) | 2×2, 4×2–4×3 | `W5-2x2-*`, `W5-4x3-*` (in-place "Today" buttons) | populated, empty ("Head clear ✨"), hidden titles | ✅ |
| W6 Day progress (MVP) | 1×1, 2×1, 2×2 | `W6-1x1-*`, `W6-2x1-*`, `W6-2x2-*` | morning, midday, evening, after bedtime | ✅ |
| W7 Routine checklist (v1.x) | 2×2, 4×3 | `W7-2x2-*`, `W7-4x3-*` | not started, in progress, completed ("Routine done 🎉"), none now | ✅ |
| W8 Week glance (v1.x) | 4×2 | `W8-4x2-*` | normal week (Light/Balanced/Full; Full uses tertiary, never red), empty week | ✅ |

- **234** widget PNGs: 79 layout×state combos × light/dark, plus 19 × (dynLight, dynDark, light-50%, dark-50%).
- **Widget picker preview images:** `widgets/picker/<W>-picker-preview-{light,dark}.png` (16).
- **Home-screen mocks:** `widgets/home/home-{A,B,C,D}-{light,dark,dynLight,dynDark}.png` (412×915dp phone at 2×) and `home-E-tablet-{light,dark,dynLight}.png` (1280×800dp). Between them, mocks A–E place all 19 layouts. The spec asks for two wallpapers (light and dark); a purple dynamic wallpaper is added.
- Ranged layouts are drawn at one size each: "4×3–4×4" at **4×4** and "4×2–4×3" at **4×3**. The smaller end is the same layout showing fewer rows.

## §9.3 Quick Settings tiles: 2/2 ✅
| Tile | States | Files |
|---|---|---|
| QS1 Capture | active (always), large + small | `quick-settings/png/QS1-capture-active-{large,small}-{light,dark}.png` |
| QS2 Focus | inactive ("Off") / active ("18 min left"), large + small | `quick-settings/png/QS2-focus-{inactive,active}-{large,small}-{light,dark}.png` |
| Panel context | focus off / on | `quick-settings/panel/qs-panel-focus-{inactive,active}-{light,dark}.png` |
| Monochrome 24dp tile icons | — | `quick-settings/icons/ic_qs_capture.svg`, `ic_qs_focus.svg` |

## §9.3 App shortcuts: 4/4 ✅
Capture, Brain dump, Start focus, Shift my day (+15).
- Adaptive shortcut icons (108dp SVG; light, dark and bare glyph): `shortcuts/icons/ic_shortcut_{capture,braindump,focus,shift}{,_dark,_glyph}.svg`
- PNG previews: `shortcuts/png/ic_shortcut_*-192.png` (8)
- Long-press menu on the home screen, with pin affordance: `shortcuts/longpress-menu-{light,dark}.png`

## §9.3 Notification templates: 9/9 ✅
Each template is drawn **collapsed, expanded and as the lock-screen public version, in light and dark** (`notifications/png/N#-{collapsed,expanded,public}-{light,dark}.png`, 54 files).

| ID | Template | Copy used (spec) |
|---|---|---|
| N1 | Start reminder | "Time for: Call bank" · Start focus · +10 min · Shift · Done |
| N2 | Heads-up | "In 5 min: Work start-up" · Start now · Snooze 5 |
| N3 | Time's up / wrap-up | "Emails — time's up." "Done?" · Done · +15 · Shift the rest (expanded: "Running behind? No problem. Shift the rest?") |
| N4 | Nag repeat | "Time for: Call bank" · "A gentle second nudge — whenever you're ready." · I'm on it · Snooze 10 · Shift |
| N5 | Grouped | "3 things start now" (expanded children with Done/+10) |
| N6 | Focus ongoing | "Focus · Write report draft", chronometer 18:00, progress · Pause · +5 · Done |
| N7 | Morning planning | "Ready to plan your day? (2 min)" · "3 things from yesterday — want to place them?" |
| N8 | Evening review | "Want to close your day?" · "You did 6 things today." |
| N9 | Ongoing "Now" (v1.x) | "Now: Emails" · 12 min left · Next 14:00 Call bank · Done · +10 · Shift · Capture |

In context: `notifications/composite/shade-{light,dark}.png`, `lockscreen-public-{light,dark}.png` and `heads-up-N1-{light,dark}.png`.

## §9.4.6–7 App icon & splash ✅
- Adaptive icon, "sun over the tide" (ebb and flow; 108dp canvas, art inside the 66dp safe zone):
  - `icon/svg/ebb-icon-foreground.svg`, `ebb-icon-background.svg`, `ebb-icon-monochrome.svg` (themed icon layer)
  - Composites: `ebb-icon-full-square.svg`, `-circle`, `-squircle`, `-rounded`
- Android sources: `icon/android/drawable/ic_launcher_{foreground,background,monochrome}.xml` (VectorDrawable) and `icon/android/mipmap-anydpi-v26/ic_launcher{,_round}.xml`
- PNGs:
  - `icon/png/ebb-play-store-icon-512.png`
  - 48dp launcher at mdpi→xxxhdpi (48/72/96/144/192 px): `icon/png/mipmap-*/ic_launcher{,_round}.png`
  - Layer PNGs at 432 px: `icon/png/layers/`
  - Review sheet with masks, safe zone and themed icons on 4 schemes: `icon/png/ebb-icon-sheet.png`
- Splash (Android 12+ SplashScreen API): `icon/splash/splash-{light,dark}.png`, with theme attributes in `icon/splash/splash-theme.xml`

## §9.4.4 Illustrations: 26 (spec ≈ 22–26) ✅
Every illustration exists as a light and a dark SVG: `illustrations/svg/<id>-{light,dark}.svg` (52 files). Contact sheets: `illustrations/illustrations-sheet-{light,dark}.png`.

| Spec item | id |
|---|---|
| Empty states | empty Today `empty-today` · empty Inbox `empty-inbox` · empty Week `empty-week` · empty Routines `empty-routines` · empty search `empty-search` |
| Onboarding | welcome O1 `welcome` · promise O2 `promise` · day shape O3 `day-shape` · notifications primer `notifications-primer` · exact alarms primer `exact-alarms-primer` · calendar primer `calendar-primer` · you're set `youre-set` |
| Recovery & review | welcome back `welcome-back` · end-of-day rest `end-of-day-rest` · nothing-done day `nothing-done` |
| Focus | focus break `focus-break` · hyperfocus water/stretch `hyperfocus` |
| Garden stages (v1.x) | `garden-1` … `garden-5` (5 stages; pebbles never decrease) |
| Settings & errors | backup `backup` · sync `sync` · error (generic) `error` · privacy `privacy` |

## §9.4.8 Play Store assets ✅
- 512×512 icon: `play-store/ebb-play-icon-512.png`
- Feature graphic 1024×500: `play-store/ebb-feature-graphic-1024x500.png`
- Phone screenshots, 1080×1920, captions from §9.4.8: `play-store/screenshots/ebb-screenshot-01…07.png`
  1. **Dump it**: T11 brain dump (built here)
  2. **Drop it on your day**: M1 Today, framed from `design/screens/10-m1-today-populated-light.png`
  3. **Running late? Shift in one tap**: D1 shift sheet (built here, over M1)
  4. **Reminders that actually arrive**: N1 heads-up over M1
  5. **Widgets that stay fresh**: home mock A
  6. **No guilt — welcome back anytime**: D6 (built here)
  7. **Free forever · Private · Open source**: O2, framed from `design/screens/02-o2-our-promise-default-light.png`
- Tablet screenshots (7″ and 10″, landscape): `play-store/screenshots/ebb-tablet-{7in,10in}.png`
- Short description draft, 72 of 80 characters: `play-store/listing-copy.md`
- The phone screens built for the store (`play-store/source-screens/`) were made because the other worker's T11, D1 and D6 renders didn't exist yet. Swap them for the canonical screens when they land.

## §9.5 acceptance checks that apply to these extras
- [x] No red for lateness. Vermillion is used only as a task colour and the destructive colour isn't used. No banned words (overdue, late, failed, missed, behind as judgement, should, must, streak).
  The store captions keep the spec's own wording ("Running late?", "never “overdue”", "No streaks"). That is marketing copy that names what Ebb avoids, not in-app UI.
- [x] Shift is reachable in ≤ 2 taps from W1 (4×2 "Shift" button) and N3 ("Shift the rest").
- [x] Capture is 1 tap from W3 (all sizes), QS1 and the app shortcut.
- [x] Widget empty states are kind and have one action (W1 "Quick capture", W2 "Add something").
- [x] Widgets are defined for every size and state listed in §9.3.
- [x] Positive-only numbers on widgets ("Done today: 4", "Inbox · 7 thoughts", "3 anytime").

## Known gaps / notes
- **Small check targets in the smallest widgets.** Widget check circles sit in a 44dp target to fit the 4×1 and 2×2 cells. Glance should pad these to 48dp where the cell allows.
- **Dynamic colour is a sample.** It is one hand-picked M3 scheme from a purple seed, as §6.3.4 asks, not the output of the real wallpaper algorithm.
- **RTL widget mirrors for W1/W2 (§9.1.4) are not drawn here.** The layouts are flex-based and mirror with `dir=rtl`, but no RTL PNGs were exported.
- **No motion specs or Lottie** (§9.4.5) for widgets or celebrations. That work isn't in this extras scope.
- **The tablet screenshots use the tablet home mock.** There is no tablet M1/M2 app screen yet.
- **"Ebb" is still a placeholder name.** Trademark and Play availability have not been checked (§6.2).
