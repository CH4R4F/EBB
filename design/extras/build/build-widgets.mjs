import fs from 'fs';
import { open, close, shot, ROOT } from './render.mjs';
import { themeVars, ic } from './lib.mjs';
import { WIDGETS, WIDGET_CSS, widgetHTML, dims } from './widgets.mjs';
import { homeScreen, wallpaperSVG, appIcon, ebbIcon, WALL } from './home.mjs';
const manifest = { widgets:[], homes:[] };
await open();
const W = id => WIDGETS.find(w=>w.id===id);
const L = (id, size) => W(id).layouts.find(l=>l[0]===size);
const wh = (id, size, theme, state, opts={}) => { const w=W(id), [s,c,r]=L(id,size); return widgetHTML(w,s,c,r,state||w.def,themeVars(theme,opts)); };
for (const w of WIDGETS) {
  for (const [size,c,r,label] of w.layouts) {
    const entry = { widget:w.id, name:w.name, tier:w.tier, size, label:label||size.replace('x','×'), files:{} };
    // every state, light + dark
    for (const st of w.states) for (const t of ['light','dark']) {
      const f = `widgets/png/${w.id}/${w.id}-${size}-${st}-${t}.png`;
      await shot(`widgets/html/${w.id}-${size}-${st}-${t}.html`, widgetHTML(w,size,c,r,st,themeVars(t)), f, { extraCss:WIDGET_CSS });
      (entry.files[st] ||= {})[t] = f;
    }
    // dynamic colour (light+dark) and 50% transparent (light+dark, shown over wallpaper) for default state
    for (const t of ['dynLight','dynDark']) {
      const f = `widgets/png/${w.id}/${w.id}-${size}-${w.def}-${t}.png`;
      await shot(`widgets/html/${w.id}-${size}-${w.def}-${t}.html`, widgetHTML(w,size,c,r,w.def,themeVars(t)), f, { extraCss:WIDGET_CSS });
      entry.files[w.def][t] = f;
    }
    for (const t of ['light','dark']) {
      const { w:ww, h:hh } = dims(c,r); const k=t;
      const body = `<div style="position:relative;width:${ww+32}px;height:${hh+32}px;overflow:hidden;border-radius:12px"><div style="position:absolute;left:-60px;top:${-420+hh/2}px">${wallpaperSVG(k,640,915)}</div><div style="position:absolute;left:16px;top:16px">${widgetHTML(w,size,c,r,w.def,themeVars(t,{alpha:.5}))}</div></div>`;
      const f = `widgets/png/${w.id}/${w.id}-${size}-${w.def}-${t}-transparent50.png`;
      await shot(`widgets/html/${w.id}-${size}-${w.def}-${t}-transparent50.html`, body, f, { extraCss:WIDGET_CSS });
      entry.files[w.def][t+'-transparent50'] = f;
    }
    manifest.widgets.push(entry);
  }
  // widget picker preview (default layout = largest MVP-typical), light + dark
  const pick = w.layouts[w.layouts.length > 2 ? w.layouts.length-1 === 2 && w.id==='W2' ? 1 : w.layouts.length-1 : w.layouts.length-1];
  for (const t of ['light','dark']) {
    const T = t==='light' ? ['#F3F0EB','#1F2421','#535C58'] : ['#202624','#E3E6E3','#A9B2AE'];
    const [s,c,r] = w.id==='W2' ? w.layouts[1] : pick;
    const body = `<div style="width:420px;padding:20px;border-radius:28px;background:${T[0]};color:${T[1]};display:flex;flex-direction:column;align-items:center;gap:12px"><div style="transform:scale(${c>=4?0.98:1});transform-origin:top center">${widgetHTML(w,s,c,r,w.def,themeVars(t))}</div><div style="text-align:center"><div style="font-weight:700;font-size:15px">Ebb · ${w.name}</div><div style="font-size:12px;color:${T[2]}">${s.replace('x','×')}${w.tier==='v1.x'?' · v1.x':''}</div></div></div>`;
    const f = `widgets/picker/${w.id}-picker-preview-${t}.png`;
    await shot(`widgets/html/${w.id}-picker-${t}.html`, body, f, { extraCss:WIDGET_CSS });
    manifest.widgets.find(e=>e.widget===w.id).picker = { ...(manifest.widgets.find(e=>e.widget===w.id).picker||{}), [t]: f };
  }
}
// ---------- home-screen mocks ----------
const themeFor = k => ({light:'light',dark:'dark',dynLight:'dynLight',dynDark:'dynDark'})[k];
const ebbApp = k => appIcon('Ebb', ebbIcon(56), 'transparent', '', k);
const apps = (k) => { const n = k.includes('ark')?['#2A3330','#C9D3CF']:['#FFFFFF','#3D4744']; return [ebbApp(k), appIcon('Notes',ic('sticky_note_2',26),n[0],n[1],k), appIcon('Clock',ic('schedule',26),n[0],n[1],k), appIcon('Files',ic('folder',26),n[0],n[1],k)]; };
const HOMES = {
  A: { title:'Now & Next, Day progress, Focus, Capture', items: k => [ {col:0,row:0,html:wh('W1','4x2',themeFor(k))}, {col:0,row:2,html:wh('W6','2x2',themeFor(k))}, {col:2,row:2,html:wh('W4','2x2',themeFor(k))}, {col:0,row:4,html:wh('W3','4x1',themeFor(k))}, {col:0,row:5,html:wh('W1','2x1',themeFor(k))}, {col:2,row:5,html:wh('W6','2x1',themeFor(k))}, ...apps(k).map((h,i)=>({col:i,row:6,html:h})) ] },
  B: { title:'Today list, Inbox, Routine, small capture & progress', items: k => [ {col:0,row:0,html:wh('W2','4x4',themeFor(k))}, {col:0,row:4,html:wh('W5','2x2',themeFor(k))}, {col:2,row:4,html:wh('W7','2x2',themeFor(k))}, {col:0,row:6,html:wh('W3','1x1',themeFor(k))}, {col:1,row:6,html:wh('W6','1x1',themeFor(k))}, {col:2,row:6,html:wh('W3','2x1',themeFor(k))} ] },
  C: { title:'Timeline strip, Focus 4×2, Week, Now & Next 4×1', items: k => [ {col:0,row:0,html:wh('W2','4x2',themeFor(k))}, {col:0,row:2,html:wh('W4','4x2',themeFor(k))}, {col:0,row:4,html:wh('W8','4x2',themeFor(k))}, {col:0,row:6,html:wh('W1','4x1',themeFor(k))} ] },
  D: { title:'Routine 4×3, Inbox 4×3', items: k => [ {col:0,row:0,html:wh('W7','4x3',themeFor(k))}, {col:0,row:3,html:wh('W5','4x3',themeFor(k))}, ...apps(k).map((h,i)=>({col:i,row:6,html:h})) ] },
};
for (const [id, H] of Object.entries(HOMES)) for (const k of ['light','dark','dynLight','dynDark']) {
  const f = `widgets/home/home-${id}-${k}.png`;
  await shot(`widgets/html/home-${id}-${k}.html`, homeScreen(k, H.items(k)), f, { extraCss:WIDGET_CSS });
  manifest.homes.push({ id, theme:k, title:H.title, file:f });
}
// tablet landscape (W2 5×4+)
for (const k of ['light','dark','dynLight']) {
  const t = themeFor(k);
  const items = [ {col:0,row:0,html:wh('W2','5x4',t)}, {col:5,row:0,html:wh('W1','4x2',t)}, {col:5,row:2,html:wh('W4','2x2',t)}, {col:7,row:2,html:wh('W6','2x2',t)}, {col:9,row:0,html:wh('W8','4x2',t)}, {col:9,row:2,html:wh('W5','2x2',t)}, {col:11,row:2,html:wh('W3','2x1',t)}, {col:11,row:3,html:wh('W6','2x1',t)}, ...apps(k).map((h,i)=>({col:i,row:5,html:h})) ];
  const f = `widgets/home/home-E-tablet-${k}.png`;
  await shot(`widgets/html/home-E-tablet-${k}.html`, homeScreen(k, items, { W:1280, H:800 }), f, { extraCss:WIDGET_CSS, viewport:[1400,900] });
  manifest.homes.push({ id:'E-tablet', theme:k, title:'Tablet: Today timeline 5×4+ with tray', file:f });
}
fs.writeFileSync(ROOT+'/widgets/manifest.json', JSON.stringify(manifest,null,1));
await close();
console.log('widget layouts', manifest.widgets.length, 'homes', manifest.homes.length);
