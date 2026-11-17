import puppeteer from 'puppeteer-core';
import fs from 'fs'; import path from 'path';
import { BASE_CSS } from './lib.mjs';
export const ROOT = path.resolve(path.dirname(new URL(import.meta.url).pathname), '..');
let browser, page;
export async function open() {
  browser = await puppeteer.launch({ executablePath:'/usr/bin/google-chrome', headless:true, args:['--no-sandbox','--allow-file-access-from-files','--font-render-hinting=none'] });
  page = await browser.newPage();
}
export async function close() { await browser.close(); }
// write a standalone HTML source file (kept as deliverable source) and screenshot #cap at 2x
export async function shot(relHtml, body, relPng, { extraCss='', bg='transparent', scale=2, viewport=[1400,1000] } = {}) {
  const htmlPath = path.join(ROOT, relHtml); fs.mkdirSync(path.dirname(htmlPath), { recursive:true });
  const depth = path.relative(path.dirname(htmlPath), ROOT); const rel = depth ? depth + '/' : '';
  const doc = `<!doctype html><html><head><meta charset="utf-8">${BASE_CSS(rel)}${extraCss}<style>body{background:${bg}}#cap{display:inline-block}</style></head><body><div id="cap">${body}</div></body></html>`;
  fs.writeFileSync(htmlPath, doc);
  await page.setViewport({ width:viewport[0], height:viewport[1], deviceScaleFactor:scale });
  await page.goto('file://' + htmlPath, { waitUntil:'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  const el = await page.$('#cap');
  const pngPath = path.join(ROOT, relPng); fs.mkdirSync(path.dirname(pngPath), { recursive:true });
  await el.screenshot({ path:pngPath, omitBackground: bg==='transparent' });
  return relPng;
}
