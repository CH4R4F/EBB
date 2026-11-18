# Security policy

## Supported versions

| Version | Supported |
|---------|-----------|
| `main` branch (0.1.x debug) | ✅ best effort |

Ebb is pre-release software. Security fixes land on `main` first.

## Reporting a vulnerability

**Please do not** open a public issue for exploitable security problems.

1. Open a [GitHub Security Advisory](https://github.com/CH4R4F/EBB/security/advisories/new) (preferred), **or**
2. File a private issue describing impact and reproduction steps if advisories are unavailable.

We aim to acknowledge reports within **7 days** and coordinate disclosure timing with you.

## Scope (typical)

- Local data exposure (Room, DataStore, backups)
- Reminder/alarm intents and notification actions
- Intent/deeplink handling in `MainActivity`
- Dependency vulnerabilities surfaced by Gradle / GitHub Dependabot

Out of scope: social engineering, physical device access, and issues in third-party libraries without a practical Ebb-specific exploit path.

## Safe harbor

We appreciate good-faith research. Do not access data that is not yours, degrade service for others, or violate applicable law.