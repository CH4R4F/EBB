// Build all screen states -> html/*.html -> screens/*.png (2x). Usage: node build.js [--only M1,T2] [--no-png]
const fs = require('fs'), path = require('path');
const { frame } = require('./ui');
const ROOT = path.join(__dirname, '..');
const HTML = path.join(ROOT, 'html'), PNG = path.join(ROOT, 'screens');
const args = process.argv.slice(2);
const only = (args.find(a => a.startsWith('--only=')) || '').replace('--only=', '').split(',').filter(Boolean);
const noPng = args.includes('--no-png');

const groups = ['onboarding', 'main', 'task', 'day', 'focus', 'routines', 'feedback', 'settings', 'system', 'widgetconfig'];
let screens = [];
for (const g of groups) { const f = path.join(__dirname, 'screens', g + '.js'); if (fs.existsSync(f)) screens.push(...require(f)); }
screens.sort((a, b) => a.nn - b.nn);
fs.writeFileSync(path.join(__dirname, 'manifest.json'), JSON.stringify(screens.map(s => ({ nn: s.nn, id: s.id, slug: s.slug, name: s.name, group: s.group, pri: s.pri, type: s.type, states: s.states.map(st => ({ key: st.key, label: st.label, variants: st.variants || [], opts: st.opts || {} })) })), null, 2));

const jobs = [];
for (const s of screens) {
  if (only.length && !only.includes(s.id)) continue;
  for (const st of s.states) {
    const o = st.opts || {};
    const themes = [['light', 'light', ''], ['dark', 'dark', '']];
    for (const v of (st.variants || [])) { if (v === 'dyn') themes.push(['dyn-light', 'light', '-dynamic'], ['dyn-dark', 'dark', '-dynamic']); if (v === 'hc') themes.push(['hc-light', 'light', '-hc'], ['hc-dark', 'dark', '-hc']); }
    for (const [theme, mode, suffix] of themes) {
      const base = `${String(s.nn).padStart(2, '0')}-${s.id.toLowerCase()}-${s.slug}-${st.key}${suffix}-${mode}`;
      const body = st.html({ theme, mode });
      fs.writeFileSync(path.join(HTML, base + '.html'), frame({ body, theme, ...o, title: `${s.id} ${s.name} — ${st.label}` }));
      jobs.push({ base, o });
    }
  }
}
console.log(`${jobs.length} html files`);
if (noPng) process.exit(0);
(async () => {
  const { chromium } = require('/workspace/ebb-build/node_modules/playwright-core');
  const browser = await chromium.launch({ executablePath: '/usr/bin/google-chrome', args: ['--font-render-hinting=none'] });
  const ctx = await browser.newContext({ viewport: { width: 940, height: 940 }, deviceScaleFactor: 2 });
  const N = 6; let i = 0;
  await Promise.all(Array.from({ length: N }, async () => {
    const p = await ctx.newPage();
    while (i < jobs.length) {
      const j = jobs[i++];
      await p.goto('file://' + path.join(HTML, j.base + '.html'));
      await p.evaluate(() => document.fonts.ready);
      await p.waitForTimeout(30);
      await (await p.$('.device')).screenshot({ path: path.join(PNG, j.base + '.png') });
    }
  }));
  await browser.close();
  console.log('rendered', jobs.length);
})();
