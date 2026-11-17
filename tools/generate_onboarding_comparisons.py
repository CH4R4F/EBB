#!/usr/bin/env python3
"""Side-by-side onboarding snapshots (Paparazzi) vs design PNGs."""
from __future__ import annotations

import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Pillow required: pip install Pillow", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[1]
SNAPSHOT_DIR = ROOT / "app/src/test/snapshots/images"
DESIGN_DIR = ROOT / "design/screens"
OUT_DIR = ROOT / "build/onboarding-comparisons"

# Paparazzi snapshot suffix -> design screen filename
PAIRS = {
    "o1_welcome_light.png": "01-o1-welcome-default-light.png",
    "o1_welcome_dark.png": "01-o1-welcome-default-dark.png",
    "o2_promise_light.png": "02-o2-our-promise-default-light.png",
    "o2_promise_dark.png": "02-o2-our-promise-default-dark.png",
    "o3_day_shape_light.png": "03-o3-day-shape-default-light.png",
    "o3_day_shape_dark.png": "03-o3-day-shape-default-dark.png",
    "o4_preferences_light.png": "04-o4-preferences-default-light.png",
    "o4_preferences_dark.png": "04-o4-preferences-default-dark.png",
    "o5_notifications_light.png": "06-o5-notifications-primer-default-light.png",
    "o5_notifications_dark.png": "06-o5-notifications-primer-default-dark.png",
    "o6_exact_alarms_light.png": "07-o6-exact-alarms-primer-default-light.png",
    "o6_exact_alarms_dark.png": "07-o6-exact-alarms-primer-default-dark.png",
    "o7_calendar_light.png": "08-o7-calendar-primer-default-light.png",
    "o7_calendar_dark.png": "08-o7-calendar-primer-default-dark.png",
    "o8_try_it_light.png": "05-o8-try-it-step1-empty-light.png",
    "o8_try_it_dark.png": "05-o8-try-it-step1-empty-dark.png",
    "o9_done_light.png": "09-o9-youre-set-default-light.png",
    "o9_done_dark.png": "09-o9-youre-set-default-dark.png",
}


def find_snapshot(suffix: str) -> Path | None:
    matches = list(SNAPSHOT_DIR.glob(f"*{suffix}"))
    return matches[0] if matches else None


def composite(ours: Image.Image, design: Image.Image, label: str) -> Image.Image:
    target_h = max(ours.height, design.height)
    target_w = 412 * 2 + 8

    def fit(img: Image.Image) -> Image.Image:
        scale = target_h / img.height
        new_w = int(img.width * scale)
        return img.resize((new_w, target_h), Image.Resampling.LANCZOS)

    o = fit(ours)
    d = fit(design)
    canvas = Image.new("RGB", (o.width + d.width + 8, target_h + 28), (32, 32, 32))
    canvas.paste(o, (0, 28))
    canvas.paste(d, (o.width + 8, 28))
    draw = ImageDraw.Draw(canvas)
    draw.text((8, 6), f"{label}  |  OURS (left)  |  DESIGN (right)", fill=(220, 220, 220))
    return canvas


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    written = 0
    for snap_suffix, design_name in PAIRS.items():
        snap = find_snapshot(snap_suffix)
        design = DESIGN_DIR / design_name
        if not snap or not design.is_file():
            print(f"skip missing: snap={snap} design={design}")
            continue
        out = OUT_DIR / f"compare_{snap_suffix}"
        img = composite(Image.open(snap).convert("RGB"), Image.open(design).convert("RGB"), snap_suffix)
        img.save(out)
        written += 1
        print(f"wrote {out}")
    print(f"done: {written} comparisons in {OUT_DIR}")
    if written == 0:
        sys.exit(1)


if __name__ == "__main__":
    main()