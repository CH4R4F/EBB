#!/usr/bin/env python3
"""Side-by-side Paparazzi snapshots vs design PNGs (Today, Quick capture, onboarding)."""
from __future__ import annotations

import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Pillow required: pip install Pillow", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[1]
SNAPSHOT_DIR = ROOT / "app/src/test/snapshots/images"
DESIGN_DIR = ROOT / "design/screens"
OUT_DIR = ROOT / "build/screen-comparisons"

PAIRS: dict[str, str] = {
    # Onboarding (kept for unified artifact)
    "o3_day_shape_dark.png": "03-o3-day-shape-default-dark.png",
    "o8_try_it_step2_drag_dark.png": "05-o8-try-it-step2-drag-dark.png",
    # Today M1
    "m1_today_populated_dark.png": "10-m1-today-populated-dark.png",
    "m1_today_populated_light.png": "10-m1-today-populated-light.png",
    "m1_today_empty_dark.png": "10-m1-today-empty-dark.png",
    "m1_today_loading_dark.png": "10-m1-today-loading-dark.png",
    "m1_today_drag_dark.png": "10-m1-today-drag-dark.png",
    "m1_today_multiselect_dark.png": "10-m1-today-multiselect-dark.png",
    "m1_today_reminders_banner_dark.png": "10-m1-today-reminders-banner-dark.png",
    "m1_today_overfull_dark.png": "10-m1-today-overfull-dark.png",
    "m1_today_overlap_dark.png": "10-m1-today-overlap-dark.png",
    "m1_today_dense_dark.png": "10-m1-today-dense-dark.png",
    "m1_today_past_day_dark.png": "10-m1-today-past-day-dark.png",
    "m1_today_future_day_dark.png": "10-m1-today-future-day-dark.png",
    "m1_today_after_bedtime_dark.png": "10-m1-today-after-bedtime-dark.png",
    "m1_today_font200_dark.png": "10-m1-today-font200-dark.png",
    "m1_today_rtl_dark.png": "10-m1-today-rtl-dark.png",
    # Quick capture T1
    "t1_quick_capture_empty_dark.png": "16-t1-quick-capture-empty-dark.png",
    "t1_quick_capture_typing_dark.png": "16-t1-quick-capture-typing-dark.png",
    "t1_quick_capture_counter_dark.png": "16-t1-quick-capture-counter-dark.png",
    "t1_quick_capture_draft_restored_dark.png": "16-t1-quick-capture-draft-restored-dark.png",
    "t1_quick_capture_paste_prompt_dark.png": "16-t1-quick-capture-paste-prompt-dark.png",
    "t1_quick_capture_nl_highlight_dark.png": "16-t1-quick-capture-nl-highlight-v1x-dark.png",
}


def find_snapshot(suffix: str) -> Path | None:
    matches = list(SNAPSHOT_DIR.glob(f"*{suffix}"))
    return matches[0] if matches else None


def composite(ours: Image.Image, design: Image.Image, label: str) -> Image.Image:
    target_h = max(ours.height, design.height)

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


if __name__ == "__main__":
    main()