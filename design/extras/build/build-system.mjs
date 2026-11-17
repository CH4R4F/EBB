import fs from 'fs';
import { open, close, shot, ROOT } from './render.mjs';
import { ic } from './lib.mjs';
import { NOTIFS, notifCard, qsTile, qsPanel, SYS, glyphSVG, shortcutSVG, SHORTCUTS } from './system.mjs';
import { homeScreen, wallpaperSVG, appIcon, ebbIcon, WALL } from './home.mjs';
const M = { notifs:[], composites:[], qs:[], shortcuts:[] };
await open();
const pad = (html, bg) => `<div style="padding:16px;background:${bg};display:inline-block">${html}</div>`;
for (const n of NOTIFS) {
  const e = { id:n.id, name:n.name, channel:n.channel, tier:n.tier||'MVP', files:{} };
  for (const t of ['light','dark']) for (const m of ['collapsed','expanded','public']) {
    const f = `notifications/png/${n.id}-${m}-${t}.png`;
    await shot(`notifications/html/${n.id}-${m}-${t}.html`, pad(notifCard(n,t,m), SYS[t].shade), f);
    e.files[`${m}-${t}`] = f;
  }
  M.notifs.push(e);
}
// notification shade composite + lock screen (public versions)
for (const t of ['light','dark']) {
  const s = SYS[t];
  const shade = `<div style="width:412px;height:915px;background:${s.shade};border-radius:28px;padding:44px 16px;display:flex;flex-direction:column;gap:10px;align-items:center;color:${s.on}"><div style="width:380px;display:flex;font-weight:700;font-size:14px;margin-bottom:6px"><span>13:48</span><span style="flex:1"></span><span style="color:${s.on2}">Mon, 16 Nov</span></div>
   ${notifCard(NOTIFS[2],t,'expanded')}${notifCard(NOTIFS[5],t,'expanded')}${notifCard(NOTIFS[0],t,'collapsed')}${notifCard(NOTIFS[4],t,'collapsed')}${notifCard(NOTIFS[6],t,'collapsed')}</div>`;
  let f = `notifications/composite/shade-${t}.png`; await shot(`notifications/html/shade-${t}.html`, shade, f); M.composites.push({ title:'Notification shade', theme:t, file:f });
  const k = t==='light' ? 'light' : 'dark';
  const lock = `<div style="position:relative;width:412px;height:915px;border-radius:28px;overflow:hidden">${wallpaperSVG(k)}<div style="position:absolute;inset:0;background:rgba(0,0,0,${t==='light'?.12:.35})"></div><div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;padding-top:90px;gap:10px;color:${t==='light'?'#1F2421':'#E3E6E3'}">
   ${ic('lock',22)}<div class="lex" style="font-size:88px;font-weight:300;line-height:1">13:48</div><div style="font-size:16px;font-weight:700;margin-bottom:28px">Monday, 16 November</div>
   ${notifCard(NOTIFS[0],t,'public')}${notifCard(NOTIFS[5],t,'public')}${notifCard(NOTIFS[6],t,'public')}<div style="font-size:12px;margin-top:6px;opacity:.8;max-width:360px;text-align:center">"Private" mode: titles hidden on the lock screen (Settings › Reminders › Lock screen)</div></div></div>`;
  f = `notifications/composite/lockscreen-public-${t}.png`; await shot(`notifications/html/lock-${t}.html`, lock, f); M.composites.push({ title:'Lock screen — public (private mode)', theme:t, file:f });
  // heads-up over an app
  const hu = `<div style="position:relative;width:412px;height:420px;border-radius:28px;overflow:hidden">${wallpaperSVG(k,412,420)}<div style="position:absolute;top:40px;left:16px">${notifCard(NOTIFS[0],t,'expanded').replace('border-radius:24px','border-radius:24px;box-shadow:0 8px 24px rgba(0,0,0,.18)')}</div></div>`;
  f = `notifications/composite/heads-up-N1-${t}.png`; await shot(`notifications/html/headsup-${t}.html`, hu, f); M.composites.push({ title:'Heads-up N1 over home', theme:t, file:f });
}
// ---------- Quick Settings ----------
for (const t of ['light','dark']) {
  for (const [kind,state] of [['capture','active'],['focus','inactive'],['focus','active']]) for (const wide of [true,false]) {
    const f = `quick-settings/png/QS${kind==='capture'?1:2}-${kind}-${state}-${wide?'large':'small'}-${t}.png`;
    await shot(`quick-settings/html/${kind}-${state}-${wide}-${t}.html`, pad(qsTile(kind,state,t,wide), SYS[t].shade), f);
    M.qs.push({ tile:kind==='capture'?'QS1':'QS2', kind, state, size:wide?'large':'small', theme:t, file:f });
  }
  for (const on of [false,true]) { const f = `quick-settings/panel/qs-panel-focus-${on?'active':'inactive'}-${t}.png`; await shot(`quick-settings/html/panel-${on}-${t}.html`, qsPanel(t,on), f); M.qs.push({ tile:'panel', state:on?'focus active':'focus inactive', theme:t, file:f }); }
}
fs.mkdirSync(ROOT+'/quick-settings/icons',{recursive:true});
fs.writeFileSync(ROOT+'/quick-settings/icons/ic_qs_capture.svg', glyphSVG('capture','#FFFFFF'));
fs.writeFileSync(ROOT+'/quick-settings/icons/ic_qs_focus.svg', glyphSVG('focus','#FFFFFF'));
// ---------- App shortcuts ----------
fs.mkdirSync(ROOT+'/shortcuts/icons',{recursive:true});
for (const [k,label] of SHORTCUTS) {
  fs.writeFileSync(`${ROOT}/shortcuts/icons/ic_shortcut_${k}.svg`, shortcutSVG(k));
  fs.writeFileSync(`${ROOT}/shortcuts/icons/ic_shortcut_${k}_dark.svg`, shortcutSVG(k,'#1F4F4A','#CDE8E4'));
  fs.writeFileSync(`${ROOT}/shortcuts/icons/ic_shortcut_${k}_glyph.svg`, glyphSVG(k,'#000000'));
  for (const [suf,bg,fg] of [['',  '#CDE8E4','#0E3B37'],['_dark','#1F4F4A','#CDE8E4']]) {
    const f = `shortcuts/png/ic_shortcut_${k}${suf}-192.png`;
    await shot(`shortcuts/html/${k}${suf}.html`, `<div style="width:96px;height:96px;border-radius:50%;overflow:hidden">${shortcutSVG(k,bg,fg).replace('width="108" height="108"','width="96" height="96" style="display:block" preserveAspectRatio="xMidYMid slice"').replace('viewBox="0 0 108 108"','viewBox="18 18 72 72"')}</div>`, f);
    M.shortcuts.push({ key:k, label, theme:suf?'dark':'light', file:f });
  }
}
for (const t of ['light','dark']) {
  const k = t, s = t==='light' ? { card:'#FFFFFF', on:'#1F2421', on2:'#535C58', div:'#E4E0DA' } : { card:'#232826', on:'#E3E6E3', on2:'#A9B2AE', div:'#363D3A' };
  const [bg,fg] = t==='light' ? ['#CDE8E4','#0E3B37'] : ['#1F4F4A','#CDE8E4'];
  const menu = `<div style="width:252px;border-radius:24px;background:${s.card};color:${s.on};padding:8px 0;box-shadow:0 10px 30px rgba(0,0,0,.22)">${SHORTCUTS.map(([g,l])=>`<div style="display:flex;align-items:center;gap:14px;height:52px;padding:0 16px"><span style="width:36px;height:36px;border-radius:50%;background:${bg};display:grid;place-items:center">${glyphSVG(g,fg,20)}</span><span style="font-weight:700;font-size:15px;flex:1">${l}</span>${ic('keep',18,'',`color:${s.on2}`)}</div>`).join('')}<div style="height:1px;background:${s.div};margin:6px 0"></div><div style="display:flex;gap:18px;padding:8px 20px;color:${s.on2}">${ic('info',22)}${ic('widgets',22)}</div></div>`;
  const items = [ { col:0, row:2, html:`<div style="margin-left:-2px">${menu}</div>` }, { col:0, row:5, html:`<div style="padding:4px;border-radius:22px;background:rgba(255,255,255,${t==='light'?.6:.15})">${appIcon('Ebb', ebbIcon(56),'transparent','',k)}</div>` } ];
  const f = `shortcuts/longpress-menu-${t}.png`;
  await shot(`shortcuts/html/longpress-${t}.html`, homeScreen(k, items), f);
  M.shortcuts.push({ key:'menu', label:'Long-press menu', theme:t, file:f });
}
fs.writeFileSync(ROOT+'/notifications/manifest.json', JSON.stringify(M,null,1));
await close(); console.log('notifs', M.notifs.length, 'qs', M.qs.length, 'shortcuts', M.shortcuts.length);
