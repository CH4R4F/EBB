// Builds prototype/index.html: single self-contained clickable prototype of spec flows 5.1–5.5, 5.7–5.9
const fs = require('fs'), path = require('path');
const ROOT = path.join(__dirname, '..');
const groups = ['onboarding', 'main', 'task', 'day', 'focus', 'routines', 'feedback', 'settings', 'system', 'widgetconfig'];
const screens = []; for (const g of groups) screens.push(...require('./screens/' + g + '.js'));
const byId = Object.fromEntries(screens.map(s => [s.id, s]));
const FLOWS = [
  ['5.1', 'First launch', [['O1', 'default'], ['O2', 'default'], ['O3', 'default'], ['O4', 'default'], ['O8', 'step1-typed'], ['O8', 'step2-drag'], ['O8', 'success'], ['O5', 'default'], ['O5', 'granted'], ['O6', 'default'], ['O7', 'default'], ['O7', 'chooser'], ['O9', 'default'], ['M1', 'populated']]],
  ['5.2', 'Morning planning', [['M1', 'empty'], ['D4', 'step1'], ['D4', 'step2'], ['D4', 'step2-auto-placed'], ['D4', 'step3'], ['M1', 'populated']]],
  ['5.3', 'Capture on the go', [['T1', 'over-home'], ['T1', 'typing'], ['T1', 'counter'], ['M2', 'populated'], ['T2', 'existing']]],
  ['5.4', 'Running late', [['M1', 'populated'], ['D1', 'default'], ['D1', 'applied'], ['D1', 'pinned-conflict'], ['M1', 'populated']]],
  ['5.5', 'Hyperfocus', [['F1', 'setup'], ['F1', 'running-ring'], ['F3', 'gentle'], ['F3', 'strong'], ['F4', 'running'], ['F4', 'ended'], ['F2', 'in-app'], ['G2', 'small-calm']]],
  ['5.7', 'Building a routine', [['M4', 'populated'], ['R3', 'gallery'], ['R3', 'preview'], ['R1', 'editing'], ['R2', 'first'], ['R2', 'mid-behind'], ['R2', 'last'], ['R2', 'finished'], ['G2', 'medium-calm'], ['M4', 'populated']]],
  ['5.8', 'End of day', [['M1', 'after-bedtime'], ['D5', 'step1'], ['D5', 'step2'], ['D5', 'step3-v1x'], ['D5', 'step4'], ['M1', 'future-day']]],
  ['5.9', 'Welcome back', [['D6', 'default'], ['D6', 'review-list'], ['M1', 'empty'], ['D4', 'step1']]],
];
const need = new Set();
for (const s of screens) need.add(`${s.id}:${s.states.find(st => !st.opts || !Object.keys(st.opts).length).key}`);
for (const [, , st] of FLOWS) for (const [id, k] of st) need.add(`${id}:${k}`);
const html = {};
for (const key of need) { const [id, k] = key.split(':'); const st = byId[id].states.find(x => x.key === k); html[key] = st.html({ theme: 'light', mode: 'light' }); }
const defaults = Object.fromEntries(screens.map(s => [s.id, `${s.id}:${s.states.find(st => !st.opts || !Object.keys(st.opts).length).key}`]));
const names = Object.fromEntries(screens.flatMap(s => s.states.map(st => [`${s.id}:${st.key}`, `${s.id} ${s.name} — ${st.label}`])));
const b64 = f => fs.readFileSync(path.join(ROOT, 'system/fonts', f)).toString('base64');
let tokens = fs.readFileSync(path.join(ROOT, 'system/tokens.css'), 'utf8').replace(/url\(fonts\/([^)]+)\)/g, (m, f) => `url(data:font/woff2;base64,${b64(f)})`);
const comps = fs.readFileSync(path.join(ROOT, 'system/components.css'), 'utf8');
const screensHtml = [...need].map(k => `<template id="s-${k.replace(':', '__')}">${html[k]}</template>`).join('\n');
const out = `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Ebb — clickable prototype</title>
<style>${tokens}\n${comps}
body.proto{margin:0;background:#E8E4DD;font-family:var(--font-body);color:#1F2421;height:100vh;display:flex;overflow:hidden}
body.proto.dk{background:#0B0E0D;color:#E3E6E3}
.side{width:300px;flex:none;overflow:auto;padding:20px;box-sizing:border-box}
.side h1{font:500 22px/28px Lexend;margin:0 0 4px}.side p{font:14px/20px var(--font-body);margin:0 0 12px;opacity:.8}
.flow{border-radius:14px;padding:10px 12px;margin-bottom:8px;background:rgba(255,255,255,.55);cursor:pointer}
body.dk .flow{background:rgba(255,255,255,.06)}
.flow.on{background:#CDE8E4;color:#0E3B37}
body.dk .flow.on{background:#1F4F4A;color:#E3E6E3}
.flow b{font:700 14px/20px var(--font-body)}.flow .stp{display:none;margin-top:6px}.flow.on .stp{display:block}
.stp div{font:13px/18px var(--font-body);padding:3px 6px;border-radius:6px;cursor:pointer}.stp div.cur{background:#2F6F6A;color:#fff}
.stage{flex:1;display:flex;align-items:center;justify-content:center;position:relative}
.phone{border-radius:44px;padding:10px;background:#1B1F1D;box-shadow:0 20px 60px rgba(0,0,0,.25);transform-origin:center}
.phone .device{border-radius:34px}
.tools{position:absolute;top:16px;right:16px;display:flex;gap:8px}
.tools button{font:700 14px var(--font-body);border:0;border-radius:999px;padding:10px 16px;background:#2F6F6A;color:#fff;cursor:pointer}
.tools button.sec{background:rgba(0,0,0,.08);color:inherit}
body.dk .tools button.sec{background:rgba(255,255,255,.1)}
.cap{position:absolute;bottom:14px;left:0;right:0;text-align:center;font:13px var(--font-body);opacity:.75}
[data-go]{cursor:pointer}
.hint [data-go]{outline:2px dashed #E69F00!important;outline-offset:2px}
</style></head><body class="proto">
<div class="side"><h1>Ebb prototype</h1><p>Tap highlighted elements on the phone (anything that leads somewhere is live). Pick a flow from spec §5 to follow it step by step. Arrow keys also move through the flow.</p><div id="flows"></div></div>
<div class="stage"><div class="tools"><button class="sec" id="back">← Back</button><button class="sec" id="hint">Show tap targets</button><button id="theme">Dark mode</button></div><div class="phone" id="phone"><div class="device theme-light" id="dev"></div></div><div class="cap" id="cap"></div></div>
${screensHtml}
<script>
const FLOWS=${JSON.stringify(FLOWS)};const DEF=${JSON.stringify(defaults)};const NAMES=${JSON.stringify(names)};
let flow=0,step=0,cur=null,hist=[],dark=false;
const dev=document.getElementById('dev');
function show(key,push=true){const t=document.getElementById('s-'+key.replace(':','__'));if(!t)return;if(push&&cur)hist.push([cur,flow,step]);cur=key;dev.innerHTML=t.innerHTML;document.getElementById('cap').textContent=NAMES[key]||key;renderFlows();}
function go(id,el){const f=FLOWS[flow];if(f){const nx=f[2][step+1];if(nx&&nx[0]===id){step++;show(nx.join(':'));return;}if(nx&&el&&el.matches('.btn.filled,.btn.big,.fab')){step++;show(nx.join(':'));return;}const idx=f[2].findIndex((s,i)=>i>step&&s[0]===id);if(idx>0){step=idx;show(f[2][idx].join(':'));return;}}if(DEF[id])show(DEF[id]);}
dev.addEventListener('click',e=>{const el=e.target.closest('[data-go]');if(el){go(el.dataset.go,el);}else{const f=FLOWS[flow];const nx=f&&f[2][step+1];if(nx&&e.target.closest('.btn.filled,.fab,.btn.big')){step++;show(nx.join(':'));}}});
function renderFlows(){document.getElementById('flows').innerHTML=FLOWS.map((f,i)=>'<div class="flow '+(i===flow?'on':'')+'" data-f="'+i+'"><b>'+f[0]+' · '+f[1]+'</b><div class="stp">'+f[2].map((s,j)=>'<div data-s="'+j+'" class="'+(i===flow&&j===step&&cur===s.join(':')?'cur':'')+'">'+(j+1)+'. '+(NAMES[s.join(':')]||s.join(' '))+'</div>').join('')+'</div></div>').join('');}
document.getElementById('flows').addEventListener('click',e=>{const s=e.target.closest('[data-s]');const f=e.target.closest('[data-f]');if(!f)return;const fi=+f.dataset.f;if(s){flow=fi;step=+s.dataset.s;show(FLOWS[flow][2][step].join(':'));}else if(fi!==flow){flow=fi;step=0;show(FLOWS[flow][2][0].join(':'));}});
document.getElementById('back').onclick=()=>{const h=hist.pop();if(h){[cur,flow,step]=[null,h[1],h[2]];show(h[0],false);}};
document.getElementById('hint').onclick=()=>{document.body.classList.toggle('hint');};
document.getElementById('theme').onclick=()=>{dark=!dark;dev.className='device '+(dark?'theme-dark':'theme-light');document.documentElement.className=dark?'theme-dark':'theme-light';document.body.classList.toggle('dk',dark);document.getElementById('theme').textContent=dark?'Light mode':'Dark mode';};
document.addEventListener('keydown',e=>{const f=FLOWS[flow];if(e.key==='ArrowRight'&&f[2][step+1]){step++;show(f[2][step].join(':'));}if(e.key==='ArrowLeft'&&step>0){step--;show(f[2][step].join(':'));}});
function fit(){const h=window.innerHeight-70,w=window.innerWidth-340;const s=Math.min(1,h/935,w/432);document.getElementById('phone').style.transform='scale('+s+')';}
window.addEventListener('resize',fit);fit();show(FLOWS[0][2][0].join(':'),false);
</script></body></html>`;
fs.writeFileSync(path.join(ROOT, 'prototype', 'index.html'), out);
console.log('prototype', (out.length / 1024 / 1024).toFixed(2), 'MB,', need.size, 'screens');
