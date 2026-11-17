import fs from 'fs';
import { open, close, shot, ROOT } from './render.mjs';
import { themeVars, ic, THEMES, TASK } from './lib.mjs';
import { fullSVG } from './icon.mjs';
import { ILLUS, illusSVG } from './illus.mjs';
import { NOTIFS, notifCard } from './system.mjs';
import { wallpaperSVG } from './home.mjs';
const SCR = ROOT + '/../screens';
const has = f => fs.existsSync(`${SCR}/${f}`);
const M = { screenshots:[], fallbackScreens:[], other:[] };
await open();
const T = THEMES.light;
const css = `<style>.scr{position:relative;width:412px;height:915px;overflow:hidden;background:var(--bg);color:var(--onSurface)}.scr .muted{color:var(--onSurfaceVariant)}
.sbar{height:36px;display:flex;align-items:center;padding:0 22px;font-size:14px;font-weight:700}.btnb{height:56px;border-radius:99px;display:flex;align-items:center;justify-content:center;gap:8px;font-weight:700;font-size:16px}
.chipb{height:40px;border-radius:10px;padding:0 14px;display:inline-flex;align-items:center;gap:6px;font-weight:700;font-size:14px;border:1px solid var(--outline)}</style>`;
const sbar = `<div class="sbar"><span>13:48</span><span style="flex:1"></span>${ic('signal_cellular_alt',18,'f')}${ic('wifi',18,'f')}${ic('battery_5_bar',18,'f','transform:rotate(90deg)')}</div>`;
const illus = id => illusSVG(ILLUS.find(x=>x[0]===id)[3],'light');
// ---------- fallback screens built here (T11, D1, D6) in the same tokens ----------
const brainDump = `<div class="scr" style="${themeVars('light')}">${sbar}<div style="display:flex;align-items:center;height:64px;padding:0 8px 0 4px">${ic('close',24,'','margin:0 12px')}<span class="lex" style="font-size:22px;font-weight:500;flex:1">Brain dump</span></div>
 <div style="padding:0 16px"><p class="body-l muted" style="margin-bottom:14px">Empty your head. One thought per line — sort it later.</p>
 <div style="background:var(--surfaceVariant);border-radius:16px 16px 4px 4px;border-bottom:2px solid var(--primary);padding:16px;min-height:420px;font-size:18px;line-height:40px;background-image:repeating-linear-gradient(transparent 0 39px,var(--outline) 39px 40px);background-position:0 16px">
 call bank about the card<br>buy cat food<br>email Mira re: Saturday<br>book dentist<br>fix bike light<br>look up pottery class<span style="display:inline-block;width:2px;height:24px;background:var(--primary);vertical-align:middle;margin-left:2px"></span></div>
 <div class="row" style="display:flex;justify-content:space-between;margin-top:8px"><span class="label-m muted">6 thoughts · saved as you type</span><span class="label-m muted">${ic('mic',16)} Voice</span></div></div>
 <div style="position:absolute;left:16px;right:16px;bottom:28px"><div class="btnb" style="background:var(--primary);color:var(--onPrimary)">${ic('inbox',20)}Save 6 to Inbox</div></div></div>`;
const tl = (rows, dim=false) => rows.map(([t,n,c,pin])=>`<div style="display:flex;gap:8px;align-items:center;height:38px"><span class="label-m muted" style="width:40px">${t}</span><div style="flex:1;height:32px;border-radius:8px;background:var(--t-${c}-tint);border-left:4px solid var(--t-${c});display:flex;align-items:center;padding:0 8px;font-size:13px;font-weight:700;${dim?'opacity:.55':''}">${n}${pin?ic('push_pin',14,'','margin-left:auto'):''}</div></div>`).join('');
const shiftSheet = (bgImg) => `<div class="scr" style="${themeVars('light')}">${bgImg?`<img src="${bgImg}" style="position:absolute;inset:0;width:412px;height:915px">`:''}<div style="position:absolute;inset:0;background:rgba(0,0,0,.32)"></div>
 <div style="position:absolute;left:0;right:0;bottom:0;background:var(--surface);border-radius:28px 28px 0 0;padding:10px 20px 28px"><div style="width:32px;height:4px;border-radius:4px;background:var(--outline);margin:0 auto 16px"></div>
 <div class="lex" style="font-size:24px;font-weight:500;line-height:32px">Shift the rest of today</div><p class="body-m muted" style="margin:4px 0 14px">Running behind? No problem. Everything after now moves together.</p>
 <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px">${['+5','+15','+30','+60'].map(p=>`<span class="chipb" style="${p==='+15'?'background:var(--primaryContainer);color:var(--onPrimaryContainer);border-color:transparent':''}">${p==='+15'?ic('check',18):''}${p} min</span>`).join('')}<span class="chipb">Start from now</span></div>
 <div style="display:flex;gap:14px;margin-bottom:10px"><div style="flex:1"><div class="label-m muted" style="margin-bottom:4px">Before</div>${tl([['14:00','Call bank','amber',1],['14:30','Write report','blue'],['16:15','School pickup','rose'],['17:00','Groceries','green']],true)}</div><div style="flex:1"><div class="label-m" style="color:var(--primary);margin-bottom:4px">After +15</div>${tl([['14:00','Call bank','amber',1],['14:45','Write report','blue'],['16:15','School pickup','rose'],['17:15','Groceries','green']])}</div></div>
 <div style="display:flex;gap:10px;align-items:center;background:var(--surfaceVariant);border-radius:12px;padding:10px 12px;margin-bottom:16px">${ic('push_pin',20,'','color:var(--primary)')}<span class="body-m">Call bank is pinned and stays at 14:00. Calendar events don't move.</span></div>
 <div class="btnb" style="background:var(--primary);color:var(--onPrimary)">${ic('update',20)}Shift 15 min</div><div class="btnb" style="height:48px;color:var(--primary)">Not now</div></div></div>`;
const welcomeBack = `<div class="scr" style="${themeVars('light')}">${sbar}<div style="padding:40px 24px 0;display:flex;flex-direction:column;align-items:center;text-align:center">${illus('welcome-back').replace('width="240" height="180"','width="280" height="210"')}
 <div class="lex" style="font-size:28px;font-weight:500;line-height:36px;margin-top:16px">Welcome back!<br>Let's make today simple.</div><p class="body-l muted" style="margin-top:10px">A few things are waiting from earlier. Nothing is lost.</p></div>
 <div style="position:absolute;left:20px;right:20px;bottom:32px;display:flex;flex-direction:column;gap:10px"><div class="btnb" style="background:var(--primary);color:var(--onPrimary)">${ic('waves',20)}Fresh start</div><div class="body-m muted" style="text-align:center;margin:-2px 0 4px">Moves everything open to your Inbox</div>
 <div class="btnb" style="background:var(--primaryContainer);color:var(--onPrimaryContainer)">Review what's waiting</div><div class="btnb" style="color:var(--primary);height:48px">Just show today</div></div></div>`;
const fb = { 'T11-brain-dump':brainDump, 'D6-welcome-back':welcomeBack };
for (const [id,html] of Object.entries(fb)) { const f=`play-store/source-screens/${id}-light.png`; await shot(`play-store/html/${id}.html`, html, f, { extraCss:css }); M.fallbackScreens.push(f); }
const m1 = has('10-m1-today-populated-light.png') ? '10-m1-today-populated-light.png' : null;
{ const f='play-store/source-screens/D1-shift-sheet-light.png'; await shot('play-store/html/D1-shift.html', shiftSheet(m1?`../../../screens/${m1}`:null), f, { extraCss:css }); M.fallbackScreens.push(f); }
// reminder: N1 heads-up over Today
{ const body = `<div class="scr" style="${themeVars('light')}">${m1?`<img src="../../../screens/${m1}" style="position:absolute;inset:0;width:412px;height:915px">`:''}<div style="position:absolute;top:44px;left:16px">${notifCard(NOTIFS[0],'light','expanded').replace('border-radius:24px','border-radius:24px;box-shadow:0 10px 30px rgba(0,0,0,.25)')}</div></div>`;
  const f='play-store/source-screens/N1-over-today-light.png'; await shot('play-store/html/N1-over-today.html', body, f, { extraCss:css }); M.fallbackScreens.push(f); }
// ---------- framed 1080×1920 screenshots ----------
const SHOTS = [
  ['01','Dump it','Empty your head in seconds. Sort it later.','play-store/source-screens/T11-brain-dump-light.png'],
  ['02','Drop it on your day','A visual timeline that shows time passing.', m1?`../screens/${m1}`:null],
  ['03','Running late? Shift in one tap','Everything after now moves together. Pins stay put.','play-store/source-screens/D1-shift-sheet-light.png'],
  ['04','Reminders that actually arrive','Gentle, on time — and never “overdue”.','play-store/source-screens/N1-over-today-light.png'],
  ['05','Widgets that stay fresh','Now & Next, timeline, focus and capture on your home screen.','widgets/home/home-A-light.png'],
  ['06','No guilt — welcome back anytime','No streaks. No red. Just a fresh start.','play-store/source-screens/D6-welcome-back-light.png'],
  ['07','Free forever · Private · Open source','No account. No ads. Your plans stay on your phone.', has('02-o2-our-promise-default-light.png')?'../screens/02-o2-our-promise-default-light.png':null],
];
const bgs = ['#E3F0ED','#F3EEE6','#E9E5F5','#E3F0ED','#F7E9DD','#F3EEE6','#E3F0ED'];
for (const [i,[n,cap,sub,src]] of SHOTS.entries()) {
  if (!src) { console.log('skip',n); continue; }
  const body = `<div style="position:relative;width:540px;height:960px;overflow:hidden;background:${bgs[i]};font-family:'Atkinson Hyperlegible Next'">
   <svg width="540" height="960" style="position:absolute;inset:0"><path d="M0 700 q67 -18 135 0 t135 0 t135 0 t135 0 V960 H0Z" fill="#2F6F6A" opacity=".08"/><path d="M0 780 q67 -18 135 0 t135 0 t135 0 t135 0 V960 H0Z" fill="#2F6F6A" opacity=".08"/></svg>
   <div style="position:absolute;top:58px;left:36px;right:36px;text-align:center"><div style="font-family:Lexend;font-weight:500;font-size:34px;line-height:42px;color:#1F2421">${cap}</div><div style="font-size:17px;line-height:24px;color:#535C58;margin-top:10px">${sub}</div></div>
   <div style="position:absolute;top:206px;left:100px;width:340px;height:735px;border-radius:40px;background:#1F2421;padding:9px;box-shadow:0 20px 50px rgba(31,36,33,.25)"><img src="../../${src}" style="width:322px;height:717px;border-radius:32px;display:block;object-fit:cover;object-position:top"></div></div>`;
  const f = `play-store/screenshots/ebb-screenshot-${n}.png`;
  await shot(`play-store/html/shot-${n}.html`, body, f);
  M.screenshots.push({ n, caption:cap, sub, source:src, file:f });
}
// ---------- feature graphic 1024×500 ----------
const fg = `<div style="position:relative;width:512px;height:250px;overflow:hidden;background:linear-gradient(160deg,#3B807A,#2F6F6A 55%,#245B56)">
 <svg width="512" height="250" style="position:absolute;inset:0"><circle cx="412" cy="170" r="34" fill="#FBE3D0" opacity=".95"/><path d="M0 160 q32 -10 64 0 t64 0 t64 0 t64 0 t64 0 t64 0 t64 0 t64 0 V250 H0Z" fill="#6FA59F"/><path d="M0 190 q40 -12 80 0 t80 0 t80 0 t80 0 t80 0 t80 0 t80 0 V250 H0Z" fill="#A3CCC6"/><path d="M0 220 q48 -12 96 0 t96 0 t96 0 t96 0 t96 0 t96 0 V250 H0Z" fill="#E2EEEB"/></svg>
 <div style="position:absolute;left:34px;top:46px;display:flex;align-items:center;gap:14px"><div style="width:64px;height:64px;border-radius:50%;overflow:hidden;box-shadow:0 4px 14px rgba(0,0,0,.18)">${fullSVG('circle').replace('width="72" height="72"','width="64" height="64"')}</div><span style="font-family:Lexend;font-weight:500;font-size:44px;color:#FAF8F5;letter-spacing:.5px">Ebb</span></div>
 <div style="position:absolute;left:36px;top:126px;font-family:Lexend;font-weight:400;font-size:22px;line-height:28px;color:#FAF8F5">A calm plan for a busy brain</div>
 <div style="position:absolute;left:36px;top:162px;font-size:12.5px;font-weight:700;color:#CDE8E4;letter-spacing:.3px">Free forever · Private · Open source</div>
 <div style="position:absolute;right:36px;top:40px;width:176px;padding:10px;border-radius:16px;background:#FAF8F5;color:#1F2421;box-shadow:0 8px 24px rgba(0,0,0,.18);font-family:'Atkinson Hyperlegible Next'"><div style="font-size:9px;font-weight:700;color:#2F6F6A">Now · 13:15–14:00</div><div style="font-family:Lexend;font-size:14px;font-weight:500;margin:2px 0 6px">Emails</div><div style="height:5px;border-radius:9px;background:#ECE8E2"><div style="width:27%;height:100%;border-radius:9px;background:#2F6F6A"></div></div><div style="font-size:9px;font-weight:700;color:#535C58;margin-top:5px">12 min left · Next: Call bank</div></div></div>`;
M.other.push(await shot('play-store/html/feature.html', fg, 'play-store/ebb-feature-graphic-1024x500.png'));
// Play icon copy
fs.copyFileSync(ROOT+'/icon/png/ebb-play-store-icon-512.png', ROOT+'/play-store/ebb-play-icon-512.png'); M.other.push('play-store/ebb-play-icon-512.png');
// tablet screenshots (7" and 10", landscape) from the tablet home mock
for (const [k,w,h,src,cap] of [['tablet-10in',1280,800,'widgets/home/home-E-tablet-light.png','Widgets that stay fresh — on tablets too'],['tablet-7in',960,600,'widgets/home/home-E-tablet-dark.png','Calm in the dark, too']]) {
  const body = `<div style="position:relative;width:${w}px;height:${h}px;overflow:hidden;background:#E3F0ED;font-family:Lexend"><div style="position:absolute;top:${h*0.05}px;width:100%;text-align:center;font-weight:500;font-size:${w/36}px;color:#1F2421">${cap}</div><img src="../../${src}" style="position:absolute;left:${w*0.1}px;top:${h*0.16}px;width:${w*0.8}px;border-radius:${w/60}px;border:${w/140}px solid #1F2421;box-shadow:0 20px 50px rgba(0,0,0,.2)"></div>`;
  M.other.push(await shot(`play-store/html/${k}.html`, body, `play-store/screenshots/ebb-${k}.png`, { viewport:[1400,900] }));
}
const short = 'A calm, free day planner for ADHD brains. Private, offline, open source.';
fs.writeFileSync(ROOT+'/play-store/listing-copy.md', `# Ebb — Play listing copy (draft)\n\n- **Title (≤30):** Ebb: Calm ADHD Day Planner (${'Ebb: Calm ADHD Day Planner'.length} chars — verify at submission)\n- **Short description (≤80):** ${short} (${short.length} chars)\n\n## Screenshot captions\n${SHOTS.map(s=>`${s[0]}. **${s[1]}** — ${s[2]}`).join('\n')}\n\n_Name "Ebb" is a placeholder (spec §6.2): availability/trademark not checked._\n`);
fs.writeFileSync(ROOT+'/play-store/manifest.json', JSON.stringify(M,null,1));
await close(); console.log(M.screenshots.length,'screenshots', short.length);
