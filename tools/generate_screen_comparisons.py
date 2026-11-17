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
    "m2_inbox_populated_dark.png": "11-m2-inbox-populated-dark.png",
    "m2_inbox_empty_dark.png": "11-m2-inbox-empty-dark.png",
    "m2_inbox_filtered_empty_dark.png": "11-m2-inbox-filtered-empty-dark.png",
    "m2_inbox_many_dark.png": "11-m2-inbox-many-dark.png",
    "m2_inbox_loading_dark.png": "11-m2-inbox-loading-dark.png",
    "m2_inbox_multiselect_dark.png": "11-m2-inbox-multiselect-dark.png",
    "m2_inbox_swipe_left_dark.png": "11-m2-inbox-swipe-left-dark.png",
    "m2_inbox_swipe_right_dark.png": "11-m2-inbox-swipe-right-dark.png",
    "m2_inbox_tidy_up_dark.png": "11-m2-inbox-tidy-up-dark.png",
    "t2_task_detail_existing_dark.png": "17-t2-task-detail-existing-dark.png",
    "t2_task_detail_new_dark.png": "17-t2-task-detail-new-dark.png",
    "t2_task_detail_done_dark.png": "17-t2-task-detail-done-dark.png",
    "t2_task_detail_recurring_dark.png": "17-t2-task-detail-recurring-dark.png",
    "t2_task_detail_many_subtasks_dark.png": "17-t2-task-detail-many-subtasks-dark.png",
    "t3_estimate_picker_presets_dark.png": "18-t3-estimate-picker-presets-dark.png",
    "t3_estimate_picker_custom_dark.png": "18-t3-estimate-picker-custom-dark.png",
    "t3_estimate_picker_big_prompt_dark.png": "18-t3-estimate-picker-big-prompt-dark.png",
    "m3_week_portrait_dark.png": "12-m3-week-portrait-dark.png",
    "m3_week_landscape_dark.png": "12-m3-week-landscape-dark.png",
    "m3_week_empty_dark.png": "12-m3-week-empty-dark.png",
    "m3_week_drag_dark.png": "12-m3-week-drag-v1x-dark.png",
    "m4_routines_empty_dark.png": "13-m4-routines-empty-dark.png",
    "m4_routines_populated_dark.png": "13-m4-routines-populated-dark.png",
    "m4_routines_running_dark.png": "13-m4-routines-running-dark.png",
    "m5_search_recent_dark.png": "14-m5-search-recent-dark.png",
    "m5_search_results_dark.png": "14-m5-search-results-dark.png",
    "m5_search_no_results_dark.png": "14-m5-search-no-results-dark.png",
    "m6_done_history_empty_dark.png": "15-m6-done-history-empty-dark.png",
    "m6_done_history_populated_dark.png": "15-m6-done-history-populated-dark.png",
    "t4_color_icon_search_empty_dark.png": "19-t4-color-icon-search-empty-dark.png",
    "t4_color_icon_selected_dark.png": "19-t4-color-icon-selected-dark.png",
    "t5_reminder_picker_default_dark.png": "20-t5-reminder-picker-default-dark.png",
    "t6_repeat_editor_presets_dark.png": "21-t6-repeat-editor-presets-dark.png",
    "t7_break_it_down_empty_dark.png": "22-t7-break-it-down-empty-dark.png",
    "t8_schedule_picker_default_dark.png": "23-t8-schedule-picker-default-dark.png",
    "f1_focus_setup_dark.png": "35-f1-focus-setup-dark.png",
    "f1_focus_running_ring_dark.png": "35-f1-focus-running-ring-dark.png",
    "f1_focus_running_bar_dark.png": "35-f1-focus-running-bar-dark.png",
    "f2_times_up_in_app_dark.png": "36-f2-times-up-in-app-dark.png",
    "f2_times_up_lockscreen_dark.png": "36-f2-times-up-lockscreen-dark.png",
    "f3_hyperfocus_gentle_dark.png": "37-f3-hyperfocus-gentle-dark.png",
    "f4_break_ended_dark.png": "38-f4-break-ended-dark.png",
    "d1_shift_the_rest_default_dark.png": "27-d1-shift-the-rest-default-dark.png",
    "d2_overlap_helper_default_dark.png": "28-d2-overlap-helper-default-dark.png",
    "d3_make_it_fit_default_dark.png": "29-d3-make-it-fit-default-dark.png",
    "d4_plan_my_day_step1_dark.png": "30-d4-plan-my-day-step1-dark.png",
    "d5_end_of_day_review_step1_dark.png": "31-d5-end-of-day-review-step1-dark.png",
    "d6_welcome_back_default_dark.png": "32-d6-welcome-back-default-dark.png",
    "d7_event_detail_default_dark.png": "33-d7-event-detail-default-dark.png",
    "d8_jump_to_date_default_dark.png": "34-d8-jump-to-date-default-dark.png",
}


def find_snapshot(suffix: str) -> Path | None:
    matches = list(SNAPSHOT_DIR.glob(f"*{suffix}"))
    if not matches:
        return None
    matches.sort(key=lambda p: p.stat().st_mtime, reverse=True)
    return matches[0]


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