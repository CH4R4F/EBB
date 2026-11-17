// Builds design/index.html (gallery) and design/COVERAGE.md from src/manifest.json + screens/
const fs = require('fs'), path = require('path');
const ROOT = path.join(__dirname, '..');
const man = JSON.parse(fs.readFileSync(path.join(__dirname, 'manifest.json'), 'utf8'));
const pngs = new Set(fs.readdirSync(path.join(ROOT, 'screens')));
const base = (s, st, suf, mode) => `${String(s.nn).padStart(2, '0')}-${s.id.toLowerCase()}-${s.slug}-${st.key}${suf}-${mode}.png`;
const order = ['Onboarding', 'Main', 'Task & capture', 'Day operations', 'Focus', 'Routines', 'Feedback & progress', 'Settings', 'System dialogs & special', 'Widget configuration'];
const brief = { Onboarding: 9, Main: 6, 'Task & capture': 11, 'Day operations': 8, Focus: 4, Routines: 3, 'Feedback & progress': 2, Settings: 15, 'System dialogs & special': 7, 'Widget configuration': 1 };
let total = 0, files = 0, missing = [];
const groups = order.map(g => [g, man.filter(s => s.group === g)]);
// ---------- gallery ----------
let nav = groups.map(([g, l]) => `<a href="#${g.replace(/\W+/g, '-')}">${g} (${l.length})</a>`).join('');
let body = '';
for (const [g, list] of groups) {
  body += `<h2 id="${g.replace(/\W+/g, '-')}">${g} <span>${list.length} of ${brief[g]} in brief</span></h2>`;
  for (const s of list) {
    body += `<div class="scr"><h3>${s.id} · ${s.name} <em>${s.type} · ${s.pri}</em></h3><div class="row">`;
    for (const st of s.states) {
      const variants = [['', 'light'], ['', 'dark']]; if (st.variants.includes('dyn')) variants.push(['-dynamic', 'light'], ['-dynamic', 'dark']); if (st.variants.includes('hc')) variants.push(['-hc', 'light'], ['-hc', 'dark']);
      for (const [suf, mode] of variants) {
        const f = base(s, st, suf, mode); files++; if (!pngs.has(f)) missing.push(f);
        const tag = `${st.label}${suf ? ' · ' + (suf === '-hc' ? 'high contrast' : 'dynamic colour') : ''} · ${mode}`;
        body += `<figure class="${st.opts.land ? 'land' : st.opts.small ? 'small' : ''}"><a href="screens/${f}" target="_blank"><img loading="lazy" src="screens/${f}" alt="${s.id} ${tag}"></a><figcaption>${tag}</figcaption></figure>`;
      }
    }
    body += `</div></div>`; total++;
  }
}
const extras = fs.existsSync(path.join(ROOT, 'extras')) ? `<a href="extras/">Extras (widgets, tiles, notifications, icon, store assets)</a>` : '';
const html = `<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Ebb — design gallery</title><meta name="viewport" content="width=device-width,initial-scale=1"><link rel="stylesheet" href="system/tokens.css"><style>
body{margin:0;font-family:var(--font-body);background:#F3F0EB;color:#1F2421}header{padding:32px 40px 8px}h1{font:500 36px/44px Lexend;margin:0}header p{max-width:900px;font-size:16px;line-height:24px;color:#535C58}
.links{display:flex;flex-wrap:wrap;gap:8px;margin:16px 0}.links a{background:#2F6F6A;color:#fff;text-decoration:none;padding:10px 16px;border-radius:999px;font-weight:700;font-size:14px}.links a.s{background:#CDE8E4;color:#0E3B37}
nav{position:sticky;top:0;background:#F3F0EBee;backdrop-filter:blur(6px);padding:10px 40px;display:flex;gap:6px;flex-wrap:wrap;z-index:2;border-bottom:1px solid #DDD8CF}nav a{font-size:13px;font-weight:700;color:#2F6F6A;text-decoration:none;padding:6px 10px;border-radius:8px;background:#fff}
main{padding:0 40px 80px}h2{font:500 26px/34px Lexend;margin:40px 0 8px}h2 span{font:14px var(--font-body);color:#535C58;margin-left:8px}h3{font:500 18px/24px Lexend;margin:24px 0 8px}h3 em{font:13px var(--font-body);color:#535C58;font-style:normal;margin-left:8px}
.row{display:flex;gap:14px;overflow-x:auto;padding-bottom:8px}figure{margin:0;flex:none;width:180px}figure.land{width:400px}figure.small{width:158px}figure img{width:100%;border-radius:14px;box-shadow:0 1px 3px rgba(0,0,0,.12);display:block;background:#ddd}figcaption{font-size:12px;line-height:16px;color:#535C58;margin-top:6px}
</style></head><body><header><h1>Ebb — mobile design</h1><p>Free, open-source, ADHD-friendly Android day planner (working name). Every screen, sheet, dialog and overlay from spec §9.2, in light and dark at 412×915 dp (rendered 2×). Key screens also in dynamic colour and high contrast; 200% font, RTL (Arabic) and small-phone (360×640) variants for the screens §9.1 names.</p>
<div class="links"><a href="prototype/index.html">Clickable prototype</a><a class="s" href="system/components.html">Components sheet</a><a class="s" href="system/tokens.json">tokens.json</a><a class="s" href="system/tokens.css">tokens.css</a><a class="s" href="system/contrast-report.md">Contrast report</a><a class="s" href="COVERAGE.md">Coverage checklist</a>${extras.replace('<a ', '<a class="s" ')}</div><p>${total} of 66 checklist items · ${files} PNGs</p></header><nav>${nav}</nav><main>${body}</main></body></html>`;
fs.writeFileSync(path.join(ROOT, 'index.html'), html);
// ---------- coverage ----------
const BRIEF_STATES = { O1: '—', O2: '—', O3: '— (+ invalid / after midnight per §4.4)', O4: '—', O8: '2 steps: dump, drag + success', O5: 'default / granted / denied', O6: '—', O7: '+ inline calendar chooser', O9: '—', M1: 'all §4.4.2 states incl. drag-in-progress, multi-select, overfull banner, overlap chip, reminders banner, past day, future day, after-bedtime, 20+ blocks compact zoom', M2: 'empty, populated, filtered-empty, swipe right/left, multi-select bar, tidy-up card', M3: 'portrait list + landscape/tablet columns; empty; drag state v1.x', M4: 'empty, populated, running', M5: 'recent, results, no results', M6: 'v1.x', T1: 'in-app sheet + over-home-screen variant; typing, multi-line paste prompt, counter, NL highlight v1.x', T2: 'new, existing, recurring instance, done, many subtasks', T3: '—', T4: '—', T5: 'with permission-missing row', T6: 'presets + custom + after-completion', T7: '—', T8: 'incl. "today is full"', T9: 'listening, processing, no match', T10: '—', T11: 'empty, draft, success', D1: 'default, some-won\u2019t-fit, pinned conflict, nothing to shift, applied + snackbar', D2: '—', D3: 'v1.x', D4: 'steps 1–3 + auto-place result + too-much note', D5: 'steps 1–4 + nothing-done variant', D6: '+ review list', D7: '—', D8: '—', F1: 'setup, running ring, running bar, hide-numbers, paused, interval, just-5-min, landscape', F2: '—', F3: 'gentle heads-up + strong full-screen', F4: '—', R1: '—', R2: 'first/mid/last step, running behind chip, finished', R3: '+ template preview sheet', G1: 'v1.x; empty + mature', G2: 'small, medium, large × calm/lively + reduced-motion static', S1: 'health pill good/warn', S2: '—', S3: '—', S4: 'all good / problems / test sent', S5: 'with live preview', S6: '—', S7: 'permission off / list', S8: '—', S9: '—', S10: 'no folder / folder set / last backup failed', S11: 'v1.x', S12: 'v1.x', S13: '—', S14: '—', S15: '—', X1: 'single, recurring, event', X2: 'notifications, exact alarms, calendar, full-screen', X3: '+ error', X4: 'v1.x', X5: 'edit/delete', X6: 'locked + unlocked', X7: 'bulk, series, delete all data', WC: 'with live preview' };
let md = `# Ebb design — coverage checklist (spec §9.2)\n\nGenerated by \`src/gallery.js\`. Every PNG is in \`screens/\` as \`<NN-id-slug>-<state>-<light|dark>.png\` (412×915 dp at 2×; landscape 915×412, small phone 360×640). Matching editable HTML for each PNG is in \`html/\`.\n\n**Done: ${total} of 66 items (60 MVP + 6 v1.x). ${files} PNGs. Missing PNGs: ${missing.length}.**\n\nLegend: ✅ done in light + dark · ➕ extra variants (dyn = dynamic colour sample, hc = high contrast, font200 = 200% font, rtl = Arabic RTL mirror, small = 360×640).\n\n`;
for (const [g, list] of groups) {
  md += `## ${g} (${list.length}/${brief[g]})\n\n| # | Item | Pri | Brief asks for | States delivered (light + dark) | Files |\n|---|---|---|---|---|---|\n`;
  for (const s of list) {
    const sts = s.states.map(st => `${st.key}${st.variants.length ? ' (+' + st.variants.join(', ') + ')' : ''}`).join(', ');
    const n = s.states.reduce((a, st) => a + 2 * (1 + st.variants.length), 0);
    md += `| ${s.nn} | ✅ ${s.id} ${s.name} | ${s.pri} | ${BRIEF_STATES[s.id] || '—'} | ${sts} | \`screens/${String(s.nn).padStart(2, '0')}-${s.id.toLowerCase()}-${s.slug}-*\` (${n} PNGs) |\n`;
  }
  md += '\n';
}
md += `## Other deliverables in this folder\n\n| Deliverable | Status | File |\n|---|---|---|\n| Design tokens (colour light/dark/dynamic/high-contrast, 4 calm themes, task colours + tints + patterns, type, spacing, layout, shape, elevation, motion) | ✅ | \`system/tokens.json\` (W3C format), \`system/tokens.css\` |\n| Component library (§6.10) with states, light + dark | ✅ | \`system/components.html\`, \`system/components-light.png\`, \`system/components-dark.png\`, \`system/components.css\` |\n| Contrast audit (WCAG AA) | ✅ 0 failures | \`system/contrast-report.md\` |\n| Clickable prototype — flows 5.1, 5.2, 5.3, 5.4, 5.5, 5.7, 5.8, 5.9, light/dark toggle | ✅ | \`prototype/index.html\` (single self-contained file) |\n| 200% font: M1, T1, T2, F1, D1 | ✅ | \`*-font200-*\` |\n| RTL (Arabic) mirror: M1, M2, T1, T2, D1 | ✅ (W1, W2 RTL belong to the widgets set in extras/) | \`*-rtl-*\` |\n| Small phone 360×640: M1, M2, T1, T2, F1, D1, D5 | ✅ | \`*-small-*\` |\n| Dynamic colour + high contrast: M1, M2, T1, T2, F1, D5 | ✅ | \`*-dynamic-*\`, \`*-hc-*\` |\n\n## Not done / out of scope for this part\n\n- **Widgets, Quick Settings tiles, app shortcuts, notification templates, illustration set, app icon, splash, Play Store assets** — produced separately in \`extras/\` (other worker). The illustrations in these screens are theme-tinted placeholder slots (simple shapes + Material Symbol motif) sized to spec (160 dp) for the final art to drop in.\n- **Tablet split views** (M1 + M2 side by side at 840 dp+) — not drawn; M3 has the landscape/tablet column layout.\n- **Motion specs / Lottie** — not produced as files; reduced-motion alternatives are noted inline (T9 listening, G2 reduced-motion state) and motion tokens are in \`tokens.json\`.\n- **Per-screen interaction / TalkBack annotation layers** — not drawn as overlays; drag alternatives are visible on screen (Schedule… on O8/T2/T8, ± estimate buttons in T2).\n- **Global loading/error states** are drawn where they apply (M1, M2 loading; T9, X3, S10 errors); screens that only read local data have no separate loading frame.\n${missing.length ? '\n### Missing PNGs\n' + missing.map(m => '- ' + m).join('\n') + '\n' : ''}`;
fs.writeFileSync(path.join(ROOT, 'COVERAGE.md'), md);
console.log({ items: total, files, missing: missing.length });
