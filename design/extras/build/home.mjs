import { ic } from './lib.mjs';
import { fullSVG } from './icon.mjs';
export const WALL = {
  light:{ sky:['#F7F0E6','#E6F0EC'], sun:'#F6D2B4', waves:['#C9E2DD','#A9D0CA','#86BBB4','#6AA59E'], fg:'#1F2421', glass:'rgba(255,255,255,.55)' },
  dark:{ sky:['#0D171B','#14282A'], sun:'#E9E3D2', waves:['#173232','#1E403F','#25504D','#2E5F5B'], fg:'#E3E6E3', glass:'rgba(30,40,38,.6)' },
  dynLight:{ sky:['#F5EDFF','#E6DAF8'], sun:'#FFD9E3', waves:['#D9C8F5','#C3ABEC','#AA8FDD','#9177CB'], fg:'#1D1B20', glass:'rgba(255,255,255,.55)' },
  dynDark:{ sky:['#15111D','#231B33'], sun:'#E9DDFF', waves:['#2C2340','#382C52','#45376A','#53437F'], fg:'#E6E0E9', glass:'rgba(40,34,52,.6)' },
};
export function wallpaperSVG(k, W=412, H=915) {
  const w = WALL[k]; const y0 = H*0.56;
  const wave = (y, amp, len, c, ph=0) => { let d=`M0 ${y}`; for (let x=0; x<=W+len; x+=len) d += ` q${len/4} ${-amp} ${len/2} 0 t${len/2} 0`; return `<path d="${d} V${H} H0Z" fill="${c}" transform="translate(${-ph} 0)"/>`; };
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}" preserveAspectRatio="xMidYMid slice"><defs><linearGradient id="s${k}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="${w.sky[0]}"/><stop offset="1" stop-color="${w.sky[1]}"/></linearGradient></defs>
  <rect width="${W}" height="${H}" fill="url(#s${k})"/><circle cx="${W*0.72}" cy="${y0-40}" r="${W*0.16}" fill="${w.sun}" opacity="${k.includes('ark')?.18:.8}"/>
  ${wave(y0, 14, 180, w.waves[0], 20)}${wave(y0+70, 18, 220, w.waves[1], 60)}${wave(y0+160, 16, 200, w.waves[2], 10)}${wave(y0+260, 20, 240, w.waves[3], 90)}</svg>`;
}
const ICON_W = 95;
export const appIcon = (label, glyph, bg, fg, k) => `<div style="width:${ICON_W}px;display:flex;flex-direction:column;align-items:center;gap:6px"><div style="width:56px;height:56px;border-radius:50%;background:${bg};color:${fg};display:grid;place-items:center;overflow:hidden">${glyph}</div>${label?`<span style="font-size:12px;color:${WALL[k].fg};font-weight:700">${label}</span>`:''}</div>`;
export const ebbIcon = (size=56) => `<div style="width:${size}px;height:${size}px;border-radius:50%;overflow:hidden">${fullSVG('circle').replace('width="72" height="72"',`width="${size}" height="${size}"`)}</div>`;
export function statusBar(k, time='13:48') { const c = WALL[k].fg; return `<div style="position:absolute;left:0;right:0;top:0;height:36px;display:flex;align-items:center;padding:0 22px;color:${c};font-size:14px;font-weight:700"><span>${time}</span><span style="flex:1"></span>${ic('signal_cellular_alt',18,'f')}${ic('wifi',18,'f')}${ic('battery_5_bar',18,'f','transform:rotate(90deg)')}</div>`; }
export function dock(k) {
  const neutral = k.includes('ark') ? ['#2A3330','#C9D3CF'] : ['#FFFFFF','#3D4744'];
  return `<div style="position:absolute;left:16px;right:16px;bottom:26px"><div style="display:flex;justify-content:space-between;margin-bottom:16px">${['call','chat','photo_camera','public'].map(g=>appIcon('',ic(g,26),neutral[0],neutral[1],k)).join('')}</div>
  <div style="height:52px;border-radius:99px;background:${WALL[k].glass};display:flex;align-items:center;padding:0 18px;gap:12px;color:${WALL[k].fg};backdrop-filter:blur(8px)">${ic('search',22)}<span style="flex:1;opacity:.7;font-size:15px">Search</span>${ic('mic',22)}</div></div>`;
}
// place items on a 4-col grid: items = [{col,row,html}]
export function homeScreen(k, items, { W=412, H=915, time } = {}) {
  return `<div style="position:relative;width:${W}px;height:${H}px;overflow:hidden;border-radius:28px">${wallpaperSVG(k,W,H)}<div style="position:absolute;inset:0">${statusBar(k,time)}
  ${items.map(it=>`<div style="position:absolute;left:${16+it.col*ICON_W+4}px;top:${52+it.row*96+4}px">${it.html}</div>`).join('')}${W<600?dock(k):''}</div></div>`;
}
