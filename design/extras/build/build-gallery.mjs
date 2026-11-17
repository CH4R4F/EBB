import fs from 'fs';
import { ROOT } from './render.mjs';
import { WIDGETS } from './widgets.mjs';
const W = JSON.parse(fs.readFileSync(ROOT+'/widgets/manifest.json'));
const N = JSON.parse(fs.readFileSync(ROOT+'/notifications/manifest.json'));
const IL = JSON.parse(fs.readFileSync(ROOT+'/illustrations/manifest.json'));
const PS = JSON.parse(fs.readFileSync(ROOT+'/play-store/manifest.json'));
const img = (f, cap='', h=null) => `<figure><a href="${f}" target="_blank"><img loading="lazy" src="${f}" ${h?`style="height:${h}px"`:''} alt="${cap}"></a>${cap?`<figcaption>${cap}</figcaption>`:''}</figure>`;
const TH = { light:'Light', dark:'Dark', dynLight:'Dynamic · light', dynDark:'Dynamic · dark', 'light-transparent50':'50% bg · light', 'dark-transparent50':'50% bg · dark' };
let widgets = '';
for (const w of WIDGETS) {
  const entries = W.widgets.filter(e=>e.widget===w.id);
  widgets += `<section class="card" id="${w.id}"><h3>${w.id} · ${w.name} <span class="tag">${w.tier}</span> <span class="muted">${entries.length} layout${entries.length>1?'s':''}: ${entries.map(e=>e.label).join(' · ')}</span></h3>`;
  const pk = entries.find(e=>e.picker)?.picker; if (pk) widgets += `<div class="row"><div><h4>Widget picker preview</h4><div class="grid">${img(pk.light,'Picker · light',180)}${img(pk.dark,'Picker · dark',180)}</div></div></div>`;
  for (const e of entries) {
    widgets += `<h4>${e.label} — ${w.stateNames[w.def]} in every theme</h4><div class="grid">${Object.entries(e.files[w.def]).map(([t,f])=>img(f,TH[t]||t)).join('')}</div>`;
    widgets += `<details><summary>${e.label} — all ${w.states.length} states (light + dark)</summary><div class="grid">${w.states.map(st=>img(e.files[st].light, w.stateNames[st]+' · light')+img(e.files[st].dark, w.stateNames[st]+' · dark')).join('')}</div></details>`;
  }
  widgets += `</section>`;
}
const homes = `<div class="grid">${W.homes.map(h=>img(h.file, `${h.id} · ${TH[h.theme]||h.theme} — ${h.title}`, h.id.startsWith('E')?300:520)).join('')}</div>`;
const qs = `<div class="grid">${N.qs.map(q=>img(q.file, `${q.tile} ${q.kind||''} ${q.state} ${q.size||''} · ${q.theme}`)).join('')}</div><p class="muted">Monochrome 24dp tile icons: <a href="quick-settings/icons/ic_qs_capture.svg">ic_qs_capture.svg</a> · <a href="quick-settings/icons/ic_qs_focus.svg">ic_qs_focus.svg</a></p>`;
const sc = `<div class="grid">${N.shortcuts.map(s=>img(s.file, `${s.label} · ${s.theme}`, s.key==='menu'?520:96)).join('')}</div><p class="muted">Adaptive shortcut SVGs (108dp, light/dark + bare glyph) in <code>shortcuts/icons/</code>.</p>`;
let nt = ''; for (const n of N.notifs) nt += `<h4>${n.id} · ${n.name} <span class="tag">${n.tier}</span> <span class="muted">${n.channel}</span></h4><div class="grid">${['collapsed','expanded','public'].flatMap(m=>['light','dark'].map(t=>img(n.files[`${m}-${t}`], `${m==='public'?'lock-screen public':m} · ${t}`))).join('')}</div>`;
nt += `<h4>In context</h4><div class="grid">${N.composites.map(c=>img(c.file, `${c.title} · ${c.theme}`, c.title.startsWith('Heads')?260:520)).join('')}</div>`;
const icon = `<div class="grid">${img('icon/png/ebb-icon-sheet.png','Adaptive icon: layers, masks, safe zone, themed (monochrome) icon')}${img('icon/png/ebb-play-store-icon-512.png','Play Store 512×512',160)}${['mdpi','hdpi','xhdpi','xxhdpi','xxxhdpi'].map(d=>img(`icon/png/mipmap-${d}/ic_launcher_round.png`,`48dp @${d}`)).join('')}${img('icon/splash/splash-light.png','Splash · light',520)}${img('icon/splash/splash-dark.png','Splash · dark',520)}</div>
<p class="muted">SVG layers: <a href="icon/svg/ebb-icon-foreground.svg">foreground</a> · <a href="icon/svg/ebb-icon-background.svg">background</a> · <a href="icon/svg/ebb-icon-monochrome.svg">monochrome</a> · <a href="icon/svg/ebb-icon-full-square.svg">full</a>. Android VectorDrawables + adaptive-icon XML in <code>icon/android/</code>; splash theme in <code>icon/splash/splash-theme.xml</code>.</p>`;
const il = `<div class="grid ill">${IL.map(i=>`<figure><img src="${i.svg.light}" alt=""><img class="dk" src="${i.svg.dark}" alt=""><figcaption>${i.name}<br><span class="muted">${i.where}</span></figcaption></figure>`).join('')}</div>`;
const ps = `<div class="grid">${img('play-store/ebb-play-icon-512.png','Icon 512×512',160)}${img('play-store/ebb-feature-graphic-1024x500.png','Feature graphic 1024×500',250)}</div><div class="grid">${PS.screenshots.map(s=>img(s.file, `${s.n}. ${s.caption}`, 420)).join('')}${img('play-store/screenshots/ebb-tablet-10in.png','Tablet 10″',260)}${img('play-store/screenshots/ebb-tablet-7in.png','Tablet 7″',260)}</div><p class="muted">Listing copy draft: <a href="play-store/listing-copy.md">listing-copy.md</a></p>`;
const html = `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Ebb — design extras gallery</title><link rel="stylesheet" href="assets/fonts/fonts.css">
<style>:root{--bg:#FAF8F5;--s:#FFFFFF;--sv:#F0ECE6;--on:#1F2421;--on2:#535C58;--p:#2F6F6A;--pc:#CDE8E4;--o:#C9C4BC}
@media (prefers-color-scheme:dark){:root{--bg:#121614;--s:#1A1F1D;--sv:#252B29;--on:#E3E6E3;--on2:#A9B2AE;--p:#8FD1C9;--pc:#1F4F4A;--o:#3A423F}.ill img{display:none}.ill img.dk{display:block!important}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--on);font-family:'Atkinson Hyperlegible Next',sans-serif;line-height:1.5}
header{padding:40px 32px 16px;max-width:1400px;margin:auto}h1,h2,h3{font-family:Lexend;font-weight:500}h1{font-size:36px;margin:0}h2{font-size:26px;margin:48px 0 12px;border-top:1px solid var(--o);padding-top:28px}h3{font-size:20px}h4{margin:18px 0 6px;font-size:15px}
main{max-width:1400px;margin:auto;padding:0 32px 80px}nav{display:flex;flex-wrap:wrap;gap:8px;margin-top:16px}nav a{background:var(--pc);color:var(--on);text-decoration:none;border-radius:99px;padding:6px 14px;font-weight:700;font-size:14px}
.muted{color:var(--on2);font-weight:400;font-size:14px}.tag{font-size:12px;background:var(--sv);border-radius:6px;padding:2px 8px;vertical-align:middle;font-family:'Atkinson Hyperlegible Next'}
.card{background:var(--s);border-radius:20px;padding:8px 24px 20px;margin:16px 0}.grid{display:flex;flex-wrap:wrap;gap:16px;align-items:flex-end}figure{margin:0}figure img{max-width:100%;display:block;border-radius:6px}figcaption{font-size:12px;color:var(--on2);margin-top:4px;max-width:300px}
details{margin:10px 0}summary{cursor:pointer;font-weight:700;color:var(--p)}.ill figure{width:200px}.ill img{width:200px;max-height:none}.ill img.dk{display:none}a{color:var(--p)}
.sum{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px;margin-top:20px}.sum div{background:var(--s);border-radius:16px;padding:12px 16px}.sum b{font-family:Lexend;font-size:26px;font-weight:500;display:block}</style></head><body>
<header><h1>Ebb — widgets, system surfaces, icon, illustrations &amp; Play assets</h1><p class="muted">Design extras for the Ebb ADHD-friendly day planner (spec §3, §6, §9.3–9.4). Tokens from spec §6 (same values as <code>design/system/tokens.css</code>). Every PNG is rendered at 2× from HTML/CSS sources in <code>*/html/</code>; rebuild with <code>build/run.sh</code>. Coverage map: <a href="EXTRAS-COVERAGE.md">EXTRAS-COVERAGE.md</a>.</p>
<div class="sum"><div><b>8 / 8</b>widgets</div><div><b>19 / 19</b>size layouts</div><div><b>2 / 2</b>Quick Settings tiles</div><div><b>4 / 4</b>app shortcuts</div><div><b>9 / 9</b>notification templates</div><div><b>${IL.length}</b>illustrations (≈22–26)</div><div><b>${PS.screenshots.length}</b>phone screenshots + 2 tablet</div></div>
<nav><a href="#widgets">Widgets</a><a href="#homes">Home screens</a><a href="#qs">Quick Settings</a><a href="#shortcuts">Shortcuts</a><a href="#notifs">Notifications</a><a href="#icon">Icon &amp; splash</a><a href="#illus">Illustrations</a><a href="#play">Play Store</a></nav></header><main>
<h2 id="widgets">Home-screen widgets</h2><p class="muted">Calm rules: no red, no unfinished counts, neutral wording ("3 anytime", "Inbox · 7 thoughts"), positive-only numbers ("Done today: 4"). Touch targets 44–48dp. Grid cell ≈ 95×96dp.</p>${widgets}
<h2 id="homes">Widgets on a home screen</h2><p class="muted">All 19 layouts placed across mocks A–E on light, dark and purple (dynamic colour) wallpapers.</p>${homes}
<h2 id="qs">Quick Settings tiles</h2>${qs}<h2 id="shortcuts">App shortcuts (launcher long-press)</h2>${sc}
<h2 id="notifs">Notification templates</h2><p class="muted">Copy from spec §2.8, §3.6, §6.13 and §9.3. Never "overdue/late/missed".</p>${nt}
<h2 id="icon">App icon &amp; splash</h2>${icon}<h2 id="illus">Illustrations</h2><p class="muted">Theme-tinted SVGs, light (dark shown when your system is in dark mode). Decorative — contentDescription null.</p>${il}
<h2 id="play">Play Store assets</h2>${ps}</main><script>document.querySelectorAll('.grid:not(.ill) img').forEach(i=>{if(i.style.height)return;const f=()=>{i.style.width=(i.naturalWidth/2)+'px'};i.complete?f():i.addEventListener('load',f)});</script></body></html>`;
fs.writeFileSync(ROOT+'/index.html', html); console.log('gallery ok');
