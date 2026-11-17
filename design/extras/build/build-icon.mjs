import fs from 'fs';
import { open, close, shot, ROOT } from './render.mjs';
import * as I from './icon.mjs';
import { THEMES } from './lib.mjs';
const D = ROOT + '/icon'; fs.mkdirSync(D+'/svg',{recursive:true}); fs.mkdirSync(D+'/android/drawable',{recursive:true}); fs.mkdirSync(D+'/android/mipmap-anydpi-v26',{recursive:true});
const w = (p,s) => fs.writeFileSync(p,s);
w(D+'/svg/ebb-icon-background.svg', I.bgSVG()); w(D+'/svg/ebb-icon-foreground.svg', I.fgSVG()); w(D+'/svg/ebb-icon-monochrome.svg', I.monoSVG());
w(D+'/svg/ebb-icon-full-square.svg', I.fullSVG('none')); w(D+'/svg/ebb-icon-circle.svg', I.fullSVG('circle')); w(D+'/svg/ebb-icon-squircle.svg', I.fullSVG('squircle')); w(D+'/svg/ebb-icon-rounded.svg', I.fullSVG('rounded'));
w(D+'/android/drawable/ic_launcher_background.xml', I.vdBackground()); w(D+'/android/drawable/ic_launcher_foreground.xml', I.vdForeground()); w(D+'/android/drawable/ic_launcher_monochrome.xml', I.vdMonochrome());
w(D+'/android/mipmap-anydpi-v26/ic_launcher.xml', I.adaptiveXML()); w(D+'/android/mipmap-anydpi-v26/ic_launcher_round.xml', I.adaptiveXML());
await open();
const sized = (svg, px) => svg.replace(/width="\d+" height="\d+"/, `width="${px}" height="${px}" style="display:block"`);
const out = [];
// Play Store 512 (full-bleed square; Play applies its own mask)
out.push(await shot('icon/html/play-512.html', sized(I.fullSVG('none'),512), 'icon/png/ebb-play-store-icon-512.png', { scale:1 }));
// 48dp launcher at every density (legacy rounded-square + round)
for (const [d,px] of [['mdpi',48],['hdpi',72],['xhdpi',96],['xxhdpi',144],['xxxhdpi',192]]) {
  out.push(await shot(`icon/html/l-${d}.html`, sized(I.fullSVG('rounded'),px), `icon/png/mipmap-${d}/ic_launcher.png`, { scale:1 }));
  out.push(await shot(`icon/html/r-${d}.html`, sized(I.fullSVG('circle'),px), `icon/png/mipmap-${d}/ic_launcher_round.png`, { scale:1 }));
}
// adaptive layers at 108dp xxxhdpi (432px)
out.push(await shot('icon/html/bg432.html', sized(I.bgSVG(),432), 'icon/png/layers/ic_launcher_background-432.png', { scale:1 }));
out.push(await shot('icon/html/fg432.html', sized(I.fgSVG(),432), 'icon/png/layers/ic_launcher_foreground-432.png', { scale:1 }));
out.push(await shot('icon/html/mono432.html', sized(I.monoSVG('#000000'),432), 'icon/png/layers/ic_launcher_monochrome-432.png', { scale:1 }));
// preview sheet: masks, safe zone, themed icons
const safe = `<div style="position:relative;width:216px;height:216px;background:repeating-conic-gradient(#e6e6e6 0 25%,#fff 0 50%) 0 0/16px 16px">${sized(I.fullSVG('none'),216)}<svg width="216" height="216" style="position:absolute;inset:0"><circle cx="108" cy="108" r="66" fill="none" stroke="#ff00aa" stroke-dasharray="4 4"/><circle cx="108" cy="108" r="72" fill="none" stroke="#0088ff" stroke-dasharray="2 4"/></svg></div>`;
const themed = (t) => `<div style="width:120px;height:120px;border-radius:50%;background:${THEMES[t].primaryContainer};overflow:hidden">${I.monoSVG(THEMES[t].onPrimaryContainer).replace('viewBox="0 0 108 108" width="108" height="108"','viewBox="18 18 72 72" width="120" height="120"')}</div>`;
const lab = (h,l) => `<div style="display:flex;flex-direction:column;align-items:center;gap:8px">${h}<span style="font:700 12px sans-serif;color:#535C58">${l}</span></div>`;
const sheet = `<div style="display:flex;gap:28px;align-items:flex-end;padding:28px;background:#FAF8F5;flex-wrap:wrap;width:1100px">
 ${lab(safe,'108dp canvas · 72dp visible · 66dp safe zone')}${lab(sized(I.fullSVG('circle'),120),'Circle mask')}${lab(sized(I.fullSVG('squircle'),120),'Squircle mask')}${lab(sized(I.fullSVG('rounded'),120),'Rounded square')}
 ${lab(themed('light'),'Themed (Tide light)')}${lab(themed('dark'),'Themed (Tide dark)')}${lab(themed('dynLight'),'Themed (purple wallpaper)')}${lab(themed('dynDark'),'Themed (purple, dark)')}
 ${lab(`<div style="width:216px;height:216px;background:repeating-conic-gradient(#e6e6e6 0 25%,#fff 0 50%) 0 0/16px 16px">${sized(I.fgSVG(),216)}</div>`,'Foreground layer')}${lab(sized(I.bgSVG(),216),'Background layer')}${lab(`<div style="width:216px;height:216px;background:repeating-conic-gradient(#e6e6e6 0 25%,#fff 0 50%) 0 0/16px 16px">${sized(I.monoSVG('#000'),216)}</div>`,'Monochrome layer')}</div>`;
out.push(await shot('icon/html/sheet.html', sheet, 'icon/png/ebb-icon-sheet.png'));
// splash (Android 12+ SplashScreen API): icon on window background, 412×915dp @2x
for (const [t,bg,fg2] of [['light','#FAF8F5','#535C58'],['dark','#121614','#A9B2AE']]) {
  const icon = `<div style="width:160px;height:160px;border-radius:50%;overflow:hidden">${I.fullSVG('circle').replace('width="72" height="72"','width="160" height="160" style="display:block"')}</div>`;
  const body = `<div style="position:relative;width:412px;height:915px;background:${bg};border-radius:28px;overflow:hidden;display:grid;place-items:center">${icon}<div style="position:absolute;bottom:56px;left:0;right:0;text-align:center;font-family:Lexend;font-weight:500;font-size:22px;letter-spacing:.5px;color:${fg2}">ebb</div></div>`;
  out.push(await shot(`icon/html/splash-${t}.html`, body, `icon/splash/splash-${t}.png`));
}
w(D+'/splash/splash-theme.xml', `<!-- res/values/themes.xml (light) & values-night/themes.xml (dark) -->
<style name="Theme.Ebb.Starting" parent="Theme.SplashScreen">
  <item name="windowSplashScreenBackground">@color/splash_bg</item>  <!-- light #FAF8F5 · dark #121614 -->
  <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
  <item name="windowSplashScreenIconBackgroundColor">#2F6F6A</item>   <!-- 160dp circle behind the 240dp icon -->
  <item name="android:windowSplashScreenBrandingImage">@drawable/splash_wordmark</item> <!-- optional 200×80dp "ebb" wordmark -->
  <item name="postSplashScreenTheme">@style/Theme.Ebb</item>
</style>
<!-- No animated icon loop (reduced motion friendly); keep on screen only until first frame. -->
`);
await close(); console.log(out.length,'icon pngs');
