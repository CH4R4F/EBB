import { ic, esc } from './lib.mjs';
export const CELL_W = 95, CELL_H = 96;
export const dims = (c, r) => ({ w: c*CELL_W - 8, h: r*CELL_H - 8 });

export const WIDGET_CSS = `<style>
.wg{position:relative;overflow:hidden;border-radius:24px;background:var(--wbg);color:var(--onSurface);padding:12px}
.wg.p16{padding:16px}
.wg .muted{color:var(--onSurfaceVariant)}
.bar{height:8px;border-radius:99px;background:var(--surfaceVariant);overflow:hidden;position:relative}
.wg .bar{background:color-mix(in srgb,var(--onSurface) 10%,transparent)}
.bar>i{position:absolute;left:0;top:0;bottom:0;border-radius:99px;background:var(--primary)}
.chk{width:28px;height:28px;border-radius:50%;border:2px solid var(--onSurfaceVariant);flex:none;display:grid;place-items:center}
.chk.on{background:var(--primary);border-color:var(--primary);color:var(--onPrimary)}
.tgt{width:44px;height:44px;display:grid;place-items:center;flex:none;border-radius:50%}
.fab{width:44px;height:44px;border-radius:16px;background:var(--primaryContainer);color:var(--onPrimaryContainer);display:grid;place-items:center;flex:none}
.btn{height:40px;border-radius:99px;padding:0 14px;display:inline-flex;align-items:center;justify-content:center;gap:6px;font-weight:700;font-size:14px;letter-spacing:.1px;white-space:nowrap}
.btn.fill{background:var(--primary);color:var(--onPrimary)}
.btn.tonal{background:var(--primaryContainer);color:var(--onPrimaryContainer)}
.btn.out{border:1px solid var(--outline);color:var(--primary)}
.wg .btn.out{border-color:color-mix(in srgb,var(--onSurface) 22%,transparent)}
.btn.sec{background:var(--secondaryContainer);color:var(--onSecondaryContainer)}
.chip{height:32px;border-radius:8px;padding:0 10px;display:inline-flex;align-items:center;gap:4px;background:var(--winner);font-size:12px;font-weight:700;white-space:nowrap}
.row{display:flex;align-items:center;gap:8px}
.col{display:flex;flex-direction:column}
.strip{width:4px;border-radius:4px;align-self:stretch;flex:none}
.tic{width:32px;height:32px;border-radius:10px;display:grid;place-items:center;flex:none}
.skel{background:color-mix(in srgb,var(--onSurface) 9%,transparent);border-radius:8px}
.cnt{display:flex;flex-direction:column;justify-content:center;align-items:center;text-align:center;height:100%}
.divv{width:1px;align-self:stretch;background:color-mix(in srgb,var(--onSurface) 14%,transparent);margin:4px 2px}
.sub{font-size:12px;line-height:16px;font-weight:700;color:var(--onSurfaceVariant)}
</style>`;

// ---- sample day (Mon 16 Nov, now 13:48) ----
const NOW = { t:'Emails', icon:'mail', c:'sky', range:'13:15–14:00', left:'12 min left', pct:27, subs:'2/4' };
const NEXT = { time:'14:00', t:'Call bank', icon:'call', c:'amber', pin:true };
const DAY = [
  { time:'13:15', t:'Emails', icon:'mail', c:'sky', dur:'45 min', cur:true },
  { time:'14:00', t:'Call bank', icon:'call', c:'amber', dur:'20 min', pin:true },
  { time:'14:30', t:'Write report draft', icon:'description', c:'blue', dur:'1 h 30 min' },
  { time:'16:15', t:'School pickup', icon:'event', c:'rose', dur:'30 min', event:true },
  { time:'17:00', t:'Groceries', icon:'shopping_basket', c:'green', dur:'45 min' },
  { time:'19:30', t:'Plan tomorrow', icon:'edit_calendar', c:'slate', dur:'10 min' },
  { time:'21:30', t:'Evening wind-down', icon:'bedtime', c:'rose', dur:'35 min' },
];
const INBOX = ['Return library books','Ask Mira about Saturday','Renew passport','Gift idea: plant mister','Fix bike light','Look up pottery class','Email landlord about heater'];
const ROUTINE = { name:'Morning start-up', steps:[['Glass of water','2 min'],['Take meds','1 min'],['Shower','10 min'],['Get dressed','5 min'],['Breakfast','15 min']] };

const tic = (icon, c, s=18) => `<span class="tic" style="background:var(--t-${c}-tint);color:var(--onSurface)">${ic(icon,s)}</span>`;
const chk = (on=false) => `<span class="tgt" title="48dp target"><span class="chk ${on?'on':''}">${on?ic('check',18):''}</span></span>`;
const ring = (size, stroke, pct, color='var(--primary)', track='color-mix(in srgb,var(--onSurface) 10%,transparent)', inner='') => {
  const r=(size-stroke)/2, C=2*Math.PI*r;
  return `<div style="position:relative;width:${size}px;height:${size}px;flex:none"><svg width="${size}" height="${size}" style="transform:rotate(-90deg)"><circle cx="${size/2}" cy="${size/2}" r="${r}" fill="none" stroke="${track}" stroke-width="${stroke}"/><circle cx="${size/2}" cy="${size/2}" r="${r}" fill="none" stroke="${color}" stroke-width="${stroke}" stroke-linecap="round" stroke-dasharray="${C*pct/100} ${C}"/></svg><div style="position:absolute;inset:0;display:grid;place-items:center;text-align:center">${inner}</div></div>`;
};

// ============ W1 Now & Next ============
function W1(size, st) {
  const hid = st==='hidden';
  const nowT = hid ? 'Current task' : NOW.t, nextT = hid ? `Next task at ${NEXT.time}` : `Next · ${NEXT.time} ${NEXT.t}`;
  if (size==='2x1') {
    if (st==='now'||st==='hidden') return `<div class="col" style="height:100%;justify-content:center;gap:8px"><div class="row" style="gap:6px">${ic(hid?'visibility_off':NOW.icon,18,'', 'color:var(--primary)')}<span class="title-m ell">${nowT}</span></div><div class="bar"><i style="width:${NOW.pct}%"></i></div><span class="sub">${NOW.left}</span></div>`;
    if (st==='free') return `<div class="col" style="height:100%;justify-content:center;gap:4px"><span class="title-m">Free until 14:00</span><span class="sub ell">${nextT}</span></div>`;
    if (st==='nothing') return `<div class="row" style="height:100%;justify-content:space-between"><span class="body-m" style="font-weight:700">Nothing planned.<br><span class="muted" style="font-weight:400">Add something?</span></span><span class="fab">${ic('add')}</span></div>`;
    if (st==='finished') return `<div class="cnt"><span class="title-m">All done for today 🌙</span><span class="sub">Rest well</span></div>`;
    if (st==='stale') return `<div class="row" style="height:100%;gap:10px">${ic('refresh',24,'','color:var(--primary)')}<span class="title-m">Tap to refresh</span></div>`;
  }
  if (size==='4x1') {
    const left = {
      now:`${tic(NOW.icon,NOW.c)}<div class="col" style="flex:1;min-width:0;gap:4px"><span class="title-m ell">${NOW.t}</span><div class="row" style="gap:6px"><div class="bar" style="flex:1"><i style="width:${NOW.pct}%"></i></div><span class="sub">12 min</span></div></div>${chk()}`,
      hidden:`${tic('visibility_off','slate')}<div class="col" style="flex:1;min-width:0;gap:4px"><span class="title-m ell">Until 14:00</span><div class="row" style="gap:6px"><div class="bar" style="flex:1"><i style="width:${NOW.pct}%"></i></div><span class="sub">12 min</span></div></div>${chk()}`,
      free:`${ic('wb_sunny',24,'','color:var(--primary)')}<div class="col" style="flex:1"><span class="title-m">Free until 14:00</span><span class="sub">Breathe — nothing right now</span></div>`,
      nothing:`<div class="col" style="flex:1"><span class="title-m">Nothing planned.</span><span class="sub">Add something?</span></div>`,
      finished:`<span style="font-size:22px">🌙</span><div class="col" style="flex:1"><span class="title-m">All done for today</span><span class="sub">Day's over — rest well</span></div>`,
      stale:`${ic('refresh',24,'','color:var(--primary)')}<div class="col" style="flex:1"><span class="title-m">Tap to refresh</span><span class="sub">Couldn't load your day</span></div>`,
    }[st];
    const next = (st==='now'||st==='free') ? `<div class="divv"></div><div class="col" style="width:92px"><span class="sub">Next · ${NEXT.time}</span><span class="label-l ell">${NEXT.t}</span></div>` : st==='hidden' ? `<div class="divv"></div><div class="col" style="width:92px"><span class="sub">Next</span><span class="label-l">at ${NEXT.time}</span></div>` : '';
    return `<div class="row" style="height:100%;gap:8px">${left}${next}${st==='finished'||st==='stale'?'':`<span class="fab">${ic('add')}</span>`}</div>`;
  }
  // 4x2
  if (st==='now'||st==='hidden') return `<div class="col" style="height:100%;gap:8px">
    <div class="row"><span class="sub" style="color:var(--primary)">Now · ${NOW.range}</span><span style="flex:1"></span>${hid?'':`<span class="sub">${ic('checklist',16)} ${NOW.subs}</span>`}</div>
    <div class="row" style="gap:10px">${hid?tic('visibility_off','slate',20):tic(NOW.icon,NOW.c,20)}<span class="title-l ell" style="flex:1">${hid?'Current task':NOW.t}</span></div>
    <div class="row" style="gap:8px"><div class="bar" style="flex:1;height:10px"><i style="width:${NOW.pct}%"></i></div><span class="sub">${NOW.left}</span></div>
    <div class="row" style="gap:6px"><span class="btn tonal">${ic('check',18)}Done</span><span class="btn out">+10 min</span><span class="btn out">${ic('update',18)}Shift</span></div>
    <div class="row" style="margin-top:auto;gap:6px;border-top:1px solid color-mix(in srgb,var(--onSurface) 12%,transparent);padding-top:6px"><span class="sub">${hid?'':ic('push_pin',14)} ${hid?nextT:`Next · ${NEXT.time}`}</span>${hid?'':`<span class="label-l ell">${NEXT.t}</span>`}</div></div>`;
  if (st==='free') return `<div class="col" style="height:100%;gap:6px"><span class="sub" style="color:var(--primary)">Between blocks</span><span class="title-l">Free until 14:00</span><span class="body-m muted">A good moment for water or a stretch.</span><div class="row" style="margin-top:auto;gap:8px">${tic(NEXT.icon,NEXT.c)}<div class="col" style="flex:1"><span class="sub">Next · ${NEXT.time}</span><span class="label-l">${NEXT.t}</span></div><span class="btn out">${ic('timer',18)}Start early</span></div></div>`;
  if (st==='nothing') return `<div class="cnt" style="gap:10px"><span class="title-l">Nothing planned.</span><span class="body-m muted">Add something?</span><span class="btn tonal">${ic('add',18)}Quick capture</span></div>`;
  if (st==='finished') return `<div class="cnt" style="gap:6px"><span style="font-size:30px">🌙</span><span class="title-l">All done for today</span><span class="body-m muted">Day's over — rest well</span></div>`;
  if (st==='stale') return `<div class="cnt" style="gap:8px">${ic('refresh',32,'','color:var(--primary)')}<span class="title-m">Tap to refresh</span><span class="body-m muted">Ebb couldn't update this widget.</span></div>`;
}

// ============ W2 Today timeline ============
function W2(size, st) {
  const header = (extra='') => `<div class="row" style="margin-bottom:8px"><div class="col" style="flex:1"><span class="lex" style="font-size:18px;font-weight:500;line-height:24px">Mon 16 Nov</span><span class="sub">${extra||'9 h 12 min left'}</span></div><span class="fab">${ic('add')}</span></div>`;
  if (st==='loading') {
    if (size==='4x2') return `<div class="col" style="height:100%;gap:10px"><div class="skel" style="width:40%;height:16px"></div><div class="row" style="gap:6px;flex:1"><div class="skel" style="flex:2;height:56px"></div><div class="skel" style="flex:1;height:56px"></div><div class="skel" style="flex:3;height:56px"></div></div><div class="skel" style="width:70%;height:10px"></div></div>`;
    return `<div class="col" style="height:100%;gap:12px"><div class="skel" style="width:45%;height:18px"></div><div class="skel" style="width:30%;height:12px"></div>${[1,2,3,4,5].map(i=>`<div class="row" style="gap:10px"><div class="skel" style="width:40px;height:12px"></div><div class="skel" style="flex:1;height:40px"></div></div>`).join('')}</div>`;
  }
  if (st==='empty') return `<div class="cnt" style="gap:8px">${size==='4x2'?'':ic('wb_sunny',36,'','color:var(--primary)')}<span class="title-l">Your day is open</span><span class="body-m muted">Drop something in?</span><span class="btn tonal">${ic('add',18)}Add something</span></div>`;
  if (st==='alldone') return `<div class="cnt" style="gap:6px"><span style="font-size:30px">🌙</span><span class="title-l">All done for today</span><span class="body-m muted">7 things done. Rest well.</span></div>`;
  if (st==='bedtime') return `<div class="cnt" style="gap:8px">${size==='4x2'?'':ic('bedtime',36,'','color:var(--secondary)')}<span class="sub">Day's over — rest well</span><span class="title-m">Tomorrow starts with:</span><div class="row" style="gap:8px">${tic('routine','green')}<span class="label-l">08:00 Morning start-up</span></div></div>`;
  if (size==='4x2') {
    // horizontal strip 13:00 → 16:00
    const W=348, t0=13*60, span=180, x = m => ((m-t0)/span*W);
    const blocks=[[13*60+15,14*60,'Emails','sky',true],[14*60,14*60+20,'Call bank','amber'],[14*60+30,16*60,'Write report draft','blue']];
    return `<div class="col" style="height:100%"><div class="row" style="margin-bottom:6px"><span class="lex" style="font-size:16px;font-weight:500">Next 3 hours</span><span style="flex:1"></span><span class="chip">${ic('wb_twilight',16)}3 anytime</span></div>
    <div style="position:relative;height:96px;margin-top:4px">
      ${[0,60,120,180].map(m=>`<div style="position:absolute;left:${x(t0+m)}px;top:0;bottom:22px;border-left:1px dashed color-mix(in srgb,var(--onSurface) 16%,transparent)"></div><span class="label-s muted" style="position:absolute;left:${Math.min(x(t0+m)+3,W-34)}px;bottom:2px">${13+m/60}:00</span>`).join('')}
      ${blocks.map(([a,b,t,c,cur])=>`<div style="position:absolute;left:${x(a)+1}px;width:${x(b)-x(a)-3}px;top:10px;height:56px;border-radius:12px;background:var(--t-${c}-tint);border-left:4px solid var(--t-${c});padding:6px 6px;overflow:hidden;${cur?'outline:2px solid var(--primary);outline-offset:-2px':''}"><div class="label-m ell">${t}</div><div class="label-s muted">${Math.round(b-a)} min</div></div>`).join('')}
      <div style="position:absolute;left:${x(13*60+48)}px;top:2px;bottom:18px;width:2px;background:var(--primary)"></div><div style="position:absolute;left:${x(13*60+48)-4}px;top:0;width:10px;height:10px;border-radius:50%;background:var(--primary)"></div>
    </div></div>`;
  }
  const rows = DAY.slice(0, size==='5x4'?5:6).map(b => `<div class="row" style="gap:8px;min-height:44px;${b.cur?'background:var(--primaryContainer);color:var(--onPrimaryContainer);border-radius:12px;margin:0 -6px;padding:0 6px':''}">
    <span class="label-m ${b.cur?'':'muted'}" style="width:38px">${b.time}</span>
    <span class="strip" style="background:var(--t-${b.c});margin:6px 0;${b.event?'background:none;border:1.5px solid var(--t-'+b.c+')':''}"></span>
    <div class="col" style="flex:1;min-width:0"><span class="label-l ell">${b.t} ${b.pin?ic('push_pin',14,'','color:var(--onSurfaceVariant)'):''}</span><span class="label-s" style="opacity:.8">${b.event?'Calendar · ':''}${b.cur?'12 min left':b.dur}</span></div>
    ${b.event?`<span class="tgt">${ic('event',20,'','color:var(--onSurfaceVariant)')}</span>`:chk()}</div>`).join('');
  const tray = size==='5x4' ? `<div class="row" style="gap:6px;margin-bottom:8px;overflow:hidden"><span class="sub" style="flex:none">Anytime</span>${['Reply to Sam','Book dentist','Water plants'].map(x=>`<span class="chip">${x}</span>`).join('')}</div>` : '';
  return `<div class="col" style="height:100%">${header()}${tray}<div class="col" style="gap:2px;overflow:hidden">${rows}</div></div>`;
}

// ============ W3 Quick capture ============
function W3(size, st) {
  const voice = st!=='novoice';
  if (size==='1x1') return `<div class="cnt"><span style="width:56px;height:56px;border-radius:18px;background:var(--primary);color:var(--onPrimary);display:grid;place-items:center">${ic('add',32)}</span></div>`;
  if (size==='2x1') return voice ? `<div class="row" style="height:100%;gap:8px"><span class="btn fill" style="flex:1;height:56px;border-radius:18px;padding:0 6px;gap:4px">${ic('add',20)}Add</span><span class="btn tonal" style="flex:1;height:56px;border-radius:18px;padding:0 6px;gap:4px">${ic('mic',20)}Voice</span></div>`
    : `<div class="row" style="height:100%"><span class="btn fill" style="flex:1;height:56px;border-radius:18px">${ic('add',22)}Add a thought</span></div>`;
  return `<div class="row" style="height:100%;gap:6px"><div class="row" style="flex:1;height:56px;border-radius:99px;background:var(--winner);padding:0 8px 0 16px;gap:10px">${ic('add',22,'','color:var(--primary)')}<span class="body-l muted" style="flex:1">Dump a thought…</span>${voice?`<span class="tgt">${ic('mic',22)}</span>`:''}</div><span class="tgt" style="width:56px;height:56px;border-radius:18px;background:var(--secondaryContainer);color:var(--onSecondaryContainer)" title="Brain dump">${ic('edit_note',24)}</span></div>`;
}

// ============ W4 Focus timer ============
function W4(size, st) {
  const sec = 'var(--secondary)';
  const ringFor = (s) => {
    const sz = 112, k = 10;
    if (s==='idle'||s==='notask') return ring(sz,k,100,'color-mix(in srgb,var(--secondary) 30%,transparent)',undefined,`<span class="ms f" style="font-size:36px;color:var(--secondary)">play_arrow</span>`);
    if (s==='running') return ring(sz,k,72,sec,undefined,`<div class="col" style="align-items:center"><span class="mono" style="font-size:28px;font-weight:500;line-height:32px">18:00</span><span class="label-s muted">left</span></div>`);
    if (s==='paused') return ring(sz,k,72,'color-mix(in srgb,var(--secondary) 55%,transparent)',undefined,`<div class="col" style="align-items:center">${ic('pause',22,'','color:var(--secondary)')}<span class="label-s muted">Paused</span></div>`);
    if (s==='break') return ring(sz,k,60,'var(--primary)',undefined,`<div class="col" style="align-items:center">${ic('local_cafe',20,'','color:var(--primary)')}<span class="mono" style="font-size:20px;font-weight:500">3:00</span></div>`);
    if (s==='finished') return ring(sz,k,100,'var(--tertiary)',undefined,`${ic('check',40,'','color:var(--tertiary)')}`);
  };
  const title = {idle:'Write report draft',notask:'No task right now',running:'Write report draft',paused:'Write report draft',break:'Break',finished:'Nice work! Break?'}[st];
  const ctrls = {
    idle:`<span class="btn fill">${ic('timer',18)}Start focus</span>`,
    notask:`<span class="btn fill">${ic('timer',18)}Just 5 minutes</span>`,
    running:`<span class="tgt" style="background:var(--secondaryContainer);color:var(--onSecondaryContainer)">${ic('pause',22)}</span><span class="tgt" style="border:1px solid color-mix(in srgb,var(--onSurface) 20%,transparent)"><span class="label-l">+5</span></span><span class="tgt" style="border:1px solid color-mix(in srgb,var(--onSurface) 20%,transparent)">${ic('check',22)}</span>`,
    paused:`<span class="tgt" style="background:var(--secondaryContainer);color:var(--onSecondaryContainer)">${ic('play_arrow',22,'f')}</span><span class="tgt" style="border:1px solid color-mix(in srgb,var(--onSurface) 20%,transparent)"><span class="label-l">+5</span></span><span class="tgt" style="border:1px solid color-mix(in srgb,var(--onSurface) 20%,transparent)">${ic('check',22)}</span>`,
    break:`<span class="btn out">${ic('skip_next',18)}Back to it</span>`,
    finished:`<span class="btn tonal">${ic('local_cafe',18)}Break</span><span class="btn out">Done</span>`,
  }[st];
  if (size==='2x2') {
    if (st==='idle'||st==='notask') return `<div class="cnt" style="gap:10px"><span class="ms f" style="font-size:40px;color:var(--secondary)">timer</span><span class="label-m muted ell" style="max-width:150px">${title}</span><span class="btn fill" style="width:100%">${st==='idle'?'Start focus':'Just 5 minutes'}</span></div>`;
    const small = ring(96,9,{running:72,paused:72,break:60,finished:100}[st], {running:sec,paused:'color-mix(in srgb,var(--secondary) 55%,transparent)',break:'var(--primary)',finished:'var(--tertiary)'}[st], undefined,
      {running:`<span class="mono" style="font-size:22px;font-weight:500">18:00</span>`,paused:`<div class="col" style="align-items:center">${ic('pause',20,'','color:var(--secondary)')}<span class="label-s muted">Paused</span></div>`,break:`<div class="col" style="align-items:center">${ic('local_cafe',18,'','color:var(--primary)')}<span class="mono label-l">3:00</span></div>`,finished:ic('check',34,'','color:var(--tertiary)')}[st]);
    const c2 = {running:`<span class="label-s muted">Tap ring to pause</span>`,paused:`<span class="label-s muted">Tap to resume</span>`,break:`<span class="label-s muted">Break · back at 14:35</span>`,finished:`<span class="label-s" style="color:var(--tertiary)">25 min focused</span>`}[st];
    return `<div class="cnt" style="gap:6px">${small}<span class="label-l ell" style="max-width:158px">${title}</span>${c2}</div>`;
  }
  // 4x2
  const presets = `<div class="row" style="gap:6px">${[5,15,25,45].map(p=>`<span class="chip" style="height:32px;${p===25&&st!=='idle'&&st!=='notask'?'background:var(--secondaryContainer);color:var(--onSecondaryContainer)':''}">${p}</span>`).join('')}<span class="label-s muted" style="margin-left:2px">min</span></div>`;
  const interval = `<div class="row" style="gap:4px"><span class="label-s muted">${st==='break'?'Break':'Work'} 2 of 4</span>${[1,2,3,4].map(i=>`<span style="width:${i===2?14:6}px;height:6px;border-radius:9px;background:${i<2?'var(--secondary)':i===2?(st==='break'?'var(--primary)':'var(--secondary)'):'color-mix(in srgb,var(--onSurface) 18%,transparent)'}"></span>`).join('')}</div>`;
  return `<div class="row" style="height:100%;gap:14px">${ringFor(st)}<div class="col" style="flex:1;min-width:0;gap:8px"><span class="sub" style="color:var(--secondary)">Focus</span><span class="title-m ell" style="margin-top:-6px">${title}</span>${(st==='running'||st==='paused'||st==='break')?interval:''}${presets}<div class="row" style="gap:6px">${ctrls}</div></div></div>`;
}

// ============ W5 Inbox glance ============
function W5(size, st) {
  const head = (n) => `<div class="row" style="margin-bottom:6px"><span class="ms" style="color:var(--primary)">inbox</span><span class="label-l" style="flex:1">Inbox${n?` · ${n}`:''}</span><span class="fab">${ic('add')}</span></div>`;
  if (st==='empty') return `<div class="col" style="height:100%">${head('')}<div class="cnt" style="flex:1;gap:2px"><span class="title-m">Head clear ✨</span><span class="label-s muted">Nothing waiting</span></div></div>`;
  if (st==='hidden') return `<div class="col" style="height:100%">${head(size==='2x2'?'7':'7 thoughts')}<div class="cnt" style="flex:1;gap:4px">${ic('visibility_off',28,'','color:var(--onSurfaceVariant)')}<span class="label-m muted">Titles hidden on widgets</span></div></div>`;
  if (size==='2x2') return `<div class="col" style="height:100%">${head(size==='2x2'?'7':'7 thoughts')}<div class="col" style="gap:4px">${INBOX.slice(0,3).map(t=>`<div class="row" style="min-height:34px;gap:6px"><span style="width:6px;height:6px;border-radius:50%;background:var(--primary);flex:none"></span><span class="body-m ell">${t}</span></div>`).join('')}</div></div>`;
  return `<div class="col" style="height:100%">${head('7 thoughts')}<div class="col" style="gap:4px">${INBOX.slice(0,3).map(t=>`<div class="row" style="min-height:48px;gap:8px;background:var(--winner);border-radius:12px;padding:0 6px 0 12px"><span class="body-m ell" style="flex:1">${t}</span><span class="btn tonal" style="height:36px;padding:0 12px;font-size:13px">${ic('wb_sunny',16)}Today</span></div>`).join('')}<span class="label-s muted" style="text-align:center;margin-top:2px">+4 more · open Inbox</span></div></div>`;
}

// ============ W6 Day progress ============
const W6S = { morning:{pct:93,left:'14 h 50 min left',short:'14h 50m',done:'Fresh day',next:'09:00 Work start-up',time:'08:10'}, midday:{pct:58,left:'9 h 12 min left',short:'9h 12m',done:'Done today: 4',next:'14:00 Call bank',time:'13:48'},
  evening:{pct:16,left:'2 h 30 min left',short:'2h 30m',done:'Done today: 7',next:'21:30 Evening wind-down',time:'20:30'}, bedtime:{pct:0,left:"Day's over — rest well",short:'',done:'Done today: 8',next:'Tomorrow · 08:00 Morning start-up',time:'23:40'} };
function W6(size, st) {
  const s = W6S[st]; const bed = st==='bedtime';
  const inner = (fs) => bed ? ic('bedtime',fs+6,'f','color:var(--secondary)') : `<span class="lex" style="font-size:${fs}px;font-weight:500;line-height:1.1">${s.short.replace(' ','<br>')}</span>`;
  // arc shows the remaining part of the waking day (primary); track = elapsed
  if (size==='1x1') return `<div class="cnt">${ring(68,8,s.pct,'var(--primary)',undefined,inner(13))}</div>`;
  if (size==='2x1') return `<div class="row" style="height:100%;gap:10px">${ring(60,7,s.pct,'var(--primary)',undefined,bed?ic('bedtime',24,'f','color:var(--secondary)'):'')}<div class="col"><span class="label-l">${bed?'Rest well':s.left.replace(' left','')}</span><span class="label-s muted">${bed?'Day is over':'left today'}</span></div></div>`;
  return `<div class="cnt" style="gap:6px">${ring(92,10,s.pct,'var(--primary)',undefined,inner(15))}<span class="label-l" style="color:var(--tertiary)">${ic('check_circle',16,'f')} ${s.done}</span><span class="label-s muted ell" style="max-width:156px">Next · ${s.next}</span></div>`;
}

// ============ W7 Routine checklist (v1.x) ============
function W7(size, st) {
  if (st==='none') return `<div class="cnt" style="gap:6px">${ic('routine',32,'','color:var(--secondary)')}<span class="title-m">No routine right now</span><span class="label-s muted">Next: Evening wind-down · 21:30</span>${size==='4x3'?`<span class="btn out" style="margin-top:6px">Start a routine</span>`:''}</div>`;
  if (st==='done') return `<div class="cnt" style="gap:6px"><span style="font-size:32px">🎉</span><span class="title-m">Routine done</span><span class="label-s muted">Morning start-up · 5 steps</span></div>`;
  const doneN = st==='inprogress'?2:0;
  const dots = `<div class="row" style="gap:4px">${ROUTINE.steps.map((_,i)=>`<span style="width:8px;height:8px;border-radius:50%;background:${i<doneN?'var(--secondary)':'color-mix(in srgb,var(--onSurface) 18%,transparent)'}"></span>`).join('')}</div>`;
  const head = `<div class="row" style="margin-bottom:4px">${ic('routine',20,'','color:var(--secondary)')}<span class="label-l ell" style="flex:1">${ROUTINE.name}</span>${size==='4x3'?`<span class="sub">${st==='inprogress'?'30 min left':'33 min'}</span>`:''}</div><div class="row" style="margin-bottom:6px">${dots}${size==='2x2'?`<span class="label-s muted" style="margin-left:auto">${st==='inprogress'?'30 min':'33 min'}</span>`:''}</div>`;
  const step = ([n,d],i) => `<div class="row" style="min-height:${size==='2x2'?32:36}px;gap:6px"><span class="chk ${i<doneN?'on':''}" style="width:22px;height:22px;border-radius:6px;${i<doneN?'background:var(--secondary);border-color:var(--secondary)':''}">${i<doneN?ic('check',16):''}</span><span class="${size==='2x2'?'body-m':'body-l'} ell" style="flex:1;${i<doneN?'opacity:.6':''}">${n}</span>${size==='4x3'?`<span class="label-s muted">${d}</span>`:''}</div>`;
  const list = size==='2x2' ? ROUTINE.steps.slice(doneN, doneN+3).map((s,i)=>step(s,i+doneN)).join('') : ROUTINE.steps.map(step).join('');
  const btn = size==='4x3' ? `<span class="btn ${st==='notstarted'?'fill':'sec'}" style="margin-top:auto;align-self:flex-start">${ic('play_arrow',18,'f')}${st==='notstarted'?'Start':'Continue in run mode'}</span>` : '';
  return `<div class="col" style="height:100%">${head}<div class="col">${list}</div>${btn}</div>`;
}

// ============ W8 Week glance (v1.x) ============
function W8(size, st) {
  const days = [['M',16,'Balanced','Call bank'],['T',17,'Light','Dentist'],['W',18,'Full','Report due'],['T',19,'Balanced','Yoga'],['F',20,'Light','Shop'],['S',21,'Light','Market'],['S',22,'Light','']];
  const lv = {Light:[30,'var(--primary)'],Balanced:[62,'var(--primary)'],Full:[95,'var(--tertiary)']};
  return `<div class="col" style="height:100%"><div class="row" style="margin-bottom:8px">${ic('calendar_view_week',20,'','color:var(--primary)')}<span class="label-l" style="flex:1">${st==='empty'?'This week':'This week · 16–22 Nov'}</span>${st==='empty'?'':`<span class="label-s muted">${ic('square',10,'f','color:var(--primary)')} Light/Balanced ${ic('square',10,'f','color:var(--tertiary)')} Full</span>`}</div>
  <div class="row" style="gap:4px;flex:1;align-items:stretch">${days.map(([d,n,l,f],i)=>{const [p,c]=lv[l]; const today=i===0; return `<div class="col" style="flex:1;min-width:0;border-radius:14px;padding:6px 4px;gap:4px;align-items:center;background:${today?'var(--primaryContainer)':'var(--winner)'};color:${today?'var(--onPrimaryContainer)':'inherit'}">
    <span class="label-s" style="opacity:.8">${d}</span><span class="lex" style="font-size:15px;font-weight:${today?600:500}">${n}</span>
    <div style="width:100%;height:6px;border-radius:9px;background:color-mix(in srgb,var(--onSurface) 12%,transparent);overflow:hidden">${st==='empty'?'':`<div style="width:${p}%;height:100%;background:${c};border-radius:9px"></div>`}</div>
    <span class="label-s" style="font-weight:400;font-size:10px;line-height:12px;text-align:center;opacity:.85;overflow:hidden;max-height:24px;max-width:100%;text-overflow:ellipsis;white-space:nowrap">${st==='empty'?'':f}</span></div>`;}).join('')}</div>
  ${st==='empty'?`<span class="label-s muted" style="text-align:center;margin-top:6px">Your week is open — tap a day to plan it</span>`:''}</div>`;
}

export const WIDGETS = [
  { id:'W1', name:'Now & Next', tier:'MVP', fn:W1, layouts:[['2x1',2,1],['4x1',4,1],['4x2',4,2]], states:['now','free','nothing','finished','hidden','stale'], def:'now',
    stateNames:{now:'Now active',free:'Free gap',nothing:'Nothing planned',finished:'Day finished',hidden:'Hidden titles',stale:'Stale · tap to refresh'} },
  { id:'W2', name:'Today timeline', tier:'MVP', fn:W2, layouts:[['4x2',4,2],['4x4',4,4,'4×3–4×4 list'],['5x4',5,4,'5×4+ / tablet']], states:['populated','empty','alldone','loading','bedtime'], def:'populated',
    stateNames:{populated:'Populated',empty:'Empty',alldone:'All done',loading:'Loading',bedtime:'After bedtime'} },
  { id:'W3', name:'Quick capture', tier:'MVP', fn:W3, layouts:[['1x1',1,1],['2x1',2,1],['4x1',4,1]], states:['default','novoice'], def:'default', stateNames:{default:'Default',novoice:'Voice unavailable'} },
  { id:'W4', name:'Focus timer', tier:'MVP', fn:W4, layouts:[['2x2',2,2],['4x2',4,2]], states:['idle','running','paused','break','finished','notask'], def:'running',
    stateNames:{idle:'Idle',running:'Running',paused:'Paused',break:'Break',finished:'Finished',notask:'No task (generic focus)'} },
  { id:'W5', name:'Inbox glance', tier:'MVP', fn:W5, layouts:[['2x2',2,2],['4x3',4,3,'4×2–4×3']], states:['populated','empty','hidden'], def:'populated', stateNames:{populated:'Populated',empty:'Empty',hidden:'Hidden titles'} },
  { id:'W6', name:'Day progress', tier:'MVP', fn:W6, layouts:[['1x1',1,1],['2x1',2,1],['2x2',2,2]], states:['morning','midday','evening','bedtime'], def:'midday', stateNames:{morning:'Morning',midday:'Midday',evening:'Evening',bedtime:'After bedtime'} },
  { id:'W7', name:'Routine checklist', tier:'v1.x', fn:W7, layouts:[['2x2',2,2],['4x3',4,3]], states:['notstarted','inprogress','done','none'], def:'inprogress', stateNames:{notstarted:'Not started',inprogress:'In progress',done:'Completed',none:'None now'} },
  { id:'W8', name:'Week glance', tier:'v1.x', fn:W8, layouts:[['4x2',4,2]], states:['normal','empty'], def:'normal', stateNames:{normal:'Normal week',empty:'Empty week'} },
];
export function widgetHTML(w, size, c, r, state, vars) {
  const { w:W, h:H } = dims(c, r);
  const pad = (c>=4 && r>=2) ? 'p16' : '';
  return `<div class="wg ${pad}" style="width:${W}px;height:${H}px;${vars}">${w.fn(size, state)}</div>`;
}
