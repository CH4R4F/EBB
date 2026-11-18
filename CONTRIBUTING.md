# Contributing to Ebb

Thank you for helping make Ebb a calmer, more usable planner. This project targets people with ADHD and busy minds — please keep that audience in mind in UX and copy.

## Development setup

1. **Clone** the repository.
2. Install **JDK 17** and **Android SDK 35** (Build Tools 35.0.0).
3. Create `local.properties` with `sdk.dir=/path/to/Android/sdk` (or set `ANDROID_HOME`).
4. Open the project in **Android Studio** (recommended) or build from the CLI.

```bash
./gradlew assembleDebug
```

Optional quality checks used in local development:

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest
```

## Code style

- **Kotlin official** code style (configure in Android Studio: Settings → Editor → Code Style → Kotlin → Set from… → Kotlin style guide).
- Prefer clear names and small composables; match existing patterns in the module you edit.
- Run `ktlintCheck` before pushing when you touch Kotlin sources.

## Commits

Use [Conventional Commits](https://www.conventionalcommits.org/) where practical:

- `feat:` new user-visible behavior  
- `fix:` bug fix  
- `docs:` documentation only  
- `refactor:` behavior-preserving code change  
- `test:` tests only  
- `chore:` tooling, dependencies, CI (when allowed)

Example: `fix(today): prevent overlap chip clipping at font scale 2.0`

## Pull request checklist

- [ ] Builds locally: `./gradlew assembleDebug` (and tests if you changed logic).
- [ ] Screenshots or Paparazzi updates if UI changed materially.
- [ ] No unrelated drive-by refactors.
- [ ] Accessibility considered (touch targets ≥ 48 dp, content descriptions, contrast).
- [ ] ADHD-friendly tone: avoid guilt/shame language, harsh reds for “overdue,” or punitive empty states.

## Reporting bugs

Use the [bug report template](.github/ISSUE_TEMPLATE/bug_report.md). Include Android version, steps to reproduce, and expected vs actual behavior.

## Feature ideas

Use the [feature request template](.github/ISSUE_TEMPLATE/feature_request.md). Tie proposals to user problems, especially executive-function and time-blindness pain points.

## Design & accessibility principles

- **Visual time** over dense lists where possible (timeline, rings, gentle motion).
- **Forgiving rescheduling** — shifting the day should feel recoverable, not punitive.
- **Reduce cognitive load** — sensible defaults, progressive disclosure, clear primary actions.
- **Respect system settings** — font scale, dark theme, Material You when enabled.

## Questions

Open a [GitHub Discussion](https://github.com/CH4R4F/EBB/discussions) or issue if something is unclear. For security concerns, see [SECURITY.md](SECURITY.md).