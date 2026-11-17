#!/usr/bin/env bash
# Rebuild all Ebb design extras. Needs node + puppeteer-core + /usr/bin/google-chrome.
set -e; cd "$(dirname "$0")"
node build-widgets.mjs && node build-system.mjs && node build-icon.mjs && node build-illus.mjs && node build-store.mjs && node build-gallery.mjs
