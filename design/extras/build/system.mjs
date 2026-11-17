import { ic } from './lib.mjs';
// --- custom monochrome 24dp glyphs (stroke-based, round caps) ---
const G = {
  capture: '<path d="M12 5.5v13M5.5 12h13"/>',
  braindump: '<path d="M5 6.5h14M5 10.5h10M5 14.5h6"/><path d="M4 17.5v1a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-1"/>',
  focus: '<circle cx="12" cy="13.5" r="7"/><path d="M12 13.5V9.8M10 3.5h4"/>',
  shift: '<path d="M6 4.5v6a3.5 3.5 0 0 0 3.5 3.5H19"/><path d="M15.5 10.5 19 14l-3.5 3.5"/>',
};
export const glyphSVG = (k, color='#FFFFFF', size=24, sw=2.2) => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="${size}" height="${size}" fill="none" stroke="${color}" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round">${G[k]}</svg>`;
// adaptive shortcut icon: 108dp canvas, background circle tint + glyph within 48dp centre
export const shortcutSVG = (k, bg='#CDE8E4', fg='#0E3B37') => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108"><rect width="108" height="108" fill="${bg}"/><g transform="translate(30 30) scale(2)" fill="none" stroke="${fg}" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">${G[k]}</g></svg>`;
export const SHORTCUTS = [ ['capture','Quick capture','Add a thought'], ['braindump','Brain dump','Empty your head'], ['focus','Start focus','On the current block'], ['shift','Shift my day (+15)','Move the rest 15 min'] ];

// --- system surface palettes (System UI, not the app theme; accent = Ebb teal for the app icon & actions) ---
export const SYS = {
  light: { shade:'#E9ECEA', card:'#FBFBF9', on:'#1B1D1C', on2:'#4D5451', accent:'#2F6F6A', accentOn:'#FFFFFF', chip:'#E3E8E5', tileOff:'#DDE3E0', tileOn:'#2F6F6A', tileOnFg:'#FFFFFF', tileOffFg:'#1B1D1C' },
  dark:  { shade:'#0F1211', card:'#232826', on:'#E3E6E3', on2:'#AAB3AF', accent:'#8FD1C9', accentOn:'#00332F', chip:'#2E3532', tileOff:'#2E3532', tileOn:'#8FD1C9', tileOnFg:'#00332F', tileOffFg:'#E3E6E3' },
};
const appBadge = (s, glyph='ebb') => `<span style="width:24px;height:24px;border-radius:50%;background:${s.accent};display:grid;place-items:center;flex:none">${glyph==='ebb'?`<svg width="16" height="16" viewBox="27 33 54 44"><path d="M41 51.2 a13 13 0 0 1 26 0 q0 1.6 -1.6 1.6 h-22.8 q-1.6 0 -1.6 -1.6 z" fill="${s.accentOn}"/><path d="M30.5 59.5 q7.83 -6.2 15.67 0 t15.67 0 t15.67 0" stroke="${s.accentOn}" stroke-width="5.2" fill="none" stroke-linecap="round"/><path d="M38.3 69.5 q7.83 -6.2 15.67 0 t15.67 0" stroke="${s.accentOn}" stroke-width="5.2" fill="none" stroke-linecap="round"/></svg>`:glyph}</span>`;
export const NOTIFS = [
  { id:'N1', name:'Start reminder', channel:'Task start reminders (high)', when:'now', title:'Time for: Call bank', text:'14:00 – 14:20 · Pinned', big:'Pinned block · 20 min. Tap Shift if you need the rest of the day to move.', actions:['Start focus','+10 min','Shift','Done'], pub:'You have a reminder' },
  { id:'N2', name:'Heads-up (early)', channel:'Heads-up & wrap-up (default)', when:'08:55', title:'In 5 min: Work start-up', text:'09:00 – 09:30', big:'Gentle heads-up. Nothing to do yet — just a time anchor.', actions:['Start now','Snooze 5'], pub:'You have a reminder' },
  { id:'N3', name:"Time's up / wrap-up", channel:'Heads-up & wrap-up (default)', when:'now', title:"Emails — time's up.", text:'Done?', big:'Running behind? No problem. Shift the rest?', actions:['Done','+15','Shift the rest'], pub:'You have a reminder' },
  { id:'N4', name:'Nag repeat (gentle second nudge)', channel:'Task start reminders (high)', when:'14:10', title:'Time for: Call bank', text:'A gentle second nudge — whenever you\u2019re ready.', big:'A gentle second nudge — whenever you\u2019re ready. Ebb will nudge 1 more time.', actions:["I'm on it",'Snooze 10','Shift'], pub:'You have a reminder', badge:'2nd nudge' },
  { id:'N5', name:'Grouped', channel:'Task start reminders (high)', when:'now', title:'3 things start now', text:'Take meds · Water plants · Stretch', group:['Take meds','Water plants','Stretch'], actions:[], pub:'3 reminders' },
  { id:'N6', name:'Focus ongoing', channel:'Focus timer (ongoing, silent)', when:'', ongoing:true, title:'Focus · Write report draft', text:'18:00 left', progress:72, chrono:true, actions:['Pause','+5','Done'], pub:'Focus timer running · 18:00 left' },
  { id:'N7', name:'Morning planning prompt', channel:'Planning & review prompts (low)', when:'08:30', title:'Ready to plan your day? (2 min)', text:'3 things from yesterday — want to place them?', big:'3 things from yesterday — want to place them? It takes about 2 minutes.', actions:['Plan my day','Later'], pub:'Ready to plan your day?' },
  { id:'N8', name:'Evening review prompt', channel:'Planning & review prompts (low)', when:'21:00', title:'Want to close your day?', text:'You did 6 things today.', big:'You did 6 things today. Re-home the rest in under a minute — or don\u2019t. Rest well.', actions:['Close my day','Not tonight'], pub:'Want to close your day?' },
  { id:'N9', name:'Ongoing "Now" (v1.x)', channel:'Focus timer / Now (ongoing, low)', when:'', ongoing:true, title:'Now: Emails', text:'12 min left · Next 14:00 Call bank', progress:27, chrono:true, actions:['Done','+10','Shift','Capture'], pub:'Now · 12 min left', tier:'v1.x' },
];
export function notifCard(n, theme, mode) {
  const s = SYS[theme]; const expanded = mode==='expanded'; const pub = mode==='public';
  const head = `<div style="display:flex;align-items:center;gap:8px;font-size:12px;color:${s.on2}">${appBadge(s)}<span style="font-weight:700;color:${s.on}">Ebb</span>${n.chrono&&!pub?`<span>· <b class="mono" style="color:${s.accent}">${n.id==='N6'?'18:00':'12:00'}</b></span>`:''}${n.when&&!n.ongoing?`<span>· ${n.when}</span>`:''}${n.badge&&!pub?`<span>· ${n.badge}</span>`:''}<span style="flex:1"></span>${ic(expanded?'expand_less':'expand_more',20,'',`color:${s.on2}`)}</div>`;
  if (pub) return `<div style="width:380px;border-radius:24px;background:${s.card};color:${s.on};padding:14px 16px">${head}<div style="margin-top:6px;margin-left:32px;font-size:15px;font-weight:700">${n.pub}</div><div style="margin-left:32px;font-size:13px;color:${s.on2}">Unlock to see details</div></div>`;
  const prog = n.progress!=null ? `<div style="margin:10px 0 2px 32px;height:6px;border-radius:9px;background:${s.chip};overflow:hidden"><div style="width:${n.progress}%;height:100%;background:${s.accent};border-radius:9px"></div></div>` : '';
  let body;
  if (n.group) {
    body = expanded ? `<div style="margin:6px 0 0 32px;display:flex;flex-direction:column;gap:6px">${n.group.map((g,i)=>`<div style="display:flex;align-items:center;gap:10px;background:${s.chip};border-radius:14px;padding:10px 12px"><div style="flex:1"><div style="font-weight:700;font-size:14px">Time for: ${g}</div><div style="font-size:12px;color:${s.on2}">14:00</div></div><span style="color:${s.accent};font-weight:700;font-size:13px">Done</span><span style="color:${s.accent};font-weight:700;font-size:13px">+10</span></div>`).join('')}</div>`
      : `<div style="margin:6px 0 0 32px"><div style="font-weight:700;font-size:15px">${n.title}</div><div style="font-size:14px;color:${s.on2}">${n.text}</div></div>`;
  } else {
    body = `<div style="margin:6px 0 0 32px"><div style="font-weight:700;font-size:15px;line-height:20px">${n.title}</div><div style="font-size:14px;line-height:20px;color:${s.on2}">${expanded&&n.big?n.big:n.text}</div></div>${prog}`;
  }
  const acts = expanded && n.actions.length ? `<div style="display:flex;gap:6px;margin:12px 0 0 26px;flex-wrap:wrap">${n.actions.map((a,i)=>`<span style="height:36px;padding:0 12px;border-radius:99px;display:inline-flex;align-items:center;font-weight:700;font-size:13.5px;${i===0?`background:${s.accent};color:${s.accentOn}`:`background:${s.chip};color:${s.on}`}">${a}</span>`).join('')}</div>` : '';
  return `<div style="width:380px;border-radius:24px;background:${s.card};color:${s.on};padding:14px 16px 16px">${head}${body}${acts}</div>`;
}
export function qsTile(kind, state, theme, wide=true) {
  const s = SYS[theme]; const on = state!=='inactive';
  const bg = on ? s.tileOn : s.tileOff, fg = on ? s.tileOnFg : s.tileOffFg;
  const label = kind==='capture' ? 'Capture' : 'Focus';
  const sub = kind==='capture' ? 'Add a thought' : (on ? '18 min left' : 'Off');
  const g = glyphSVG(kind==='capture'?'capture':'focus', fg, 24);
  if (!wide) return `<div style="width:72px;height:72px;border-radius:22px;background:${bg};display:grid;place-items:center">${g}</div>`;
  return `<div style="width:176px;height:72px;border-radius:22px;background:${bg};color:${fg};display:flex;align-items:center;gap:12px;padding:0 16px">${g}<div style="display:flex;flex-direction:column;min-width:0"><span style="font-weight:700;font-size:15px">${label}</span><span style="font-size:12.5px;opacity:.85">${sub}</span></div></div>`;
}
const sysTile = (glyph,label,sub,on,s) => `<div style="width:176px;height:72px;border-radius:22px;background:${on?s.tileOn:s.tileOff};color:${on?s.tileOnFg:s.tileOffFg};display:flex;align-items:center;gap:12px;padding:0 16px">${ic(glyph,24)}<div style="display:flex;flex-direction:column"><span style="font-weight:700;font-size:15px">${label}</span><span style="font-size:12.5px;opacity:.85">${sub}</span></div></div>`;
export function qsPanel(theme, focusOn) {
  const s = SYS[theme];
  return `<div style="width:412px;padding:20px 22px 24px;background:${s.shade};border-radius:28px;color:${s.on}"><div style="display:flex;font-size:14px;font-weight:700;margin-bottom:14px"><span>13:48</span><span style="flex:1"></span><span style="color:${s.on2}">Mon, 16 Nov</span></div>
  <div style="height:8px;border-radius:9px;background:${s.chip};margin-bottom:16px;position:relative"><div style="width:62%;height:100%;border-radius:9px;background:${s.accent}"></div></div>
  <div style="display:grid;grid-template-columns:176px 176px;gap:12px;justify-content:center">${sysTile('wifi','Internet','Home Wi-Fi',true,s)}${sysTile('bluetooth','Bluetooth','Off',false,s)}${qsTile('capture','active',theme)}${qsTile('focus',focusOn?'active':'inactive',theme)}${sysTile('flashlight_on','Flashlight','Off',false,s)}${sysTile('do_not_disturb_on','Do Not Disturb','Off',false,s)}</div></div>`;
}
