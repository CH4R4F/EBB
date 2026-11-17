import fs from 'fs';
import { open, close, shot, ROOT } from './render.mjs';
import { ILLUS, illusSVG } from './illus.mjs';
const D = ROOT+'/illustrations'; fs.mkdirSync(D+'/svg',{recursive:true});
const M = [];
for (const [id,name,where,fn] of ILLUS) { for (const t of ['light','dark']) fs.writeFileSync(`${D}/svg/${id}-${t}.svg`, illusSVG(fn,t)); M.push({ id, name, where, svg:{light:`illustrations/svg/${id}-light.svg`, dark:`illustrations/svg/${id}-dark.svg`} }); }
await open();
for (const t of ['light','dark']) {
  const bg = t==='light'?'#FAF8F5':'#121614', fg = t==='light'?'#535C58':'#A9B2AE';
  const body = `<div style="display:grid;grid-template-columns:repeat(6,200px);gap:12px;padding:20px;background:${bg};width:1300px">${ILLUS.map(([id,name,w,fn])=>`<div style="display:flex;flex-direction:column;align-items:center;gap:4px">${illusSVG(fn,t).replace('width="240" height="180"','width="200" height="150"')}<span style="font:700 11px sans-serif;color:${fg};text-align:center">${name}</span></div>`).join('')}</div>`;
  await shot(`illustrations/html/sheet-${t}.html`, body, `illustrations/illustrations-sheet-${t}.png`, { viewport:[1400,1000] });
}
fs.writeFileSync(D+'/manifest.json', JSON.stringify(M,null,1));
await close(); console.log(M.length,'illustrations');
