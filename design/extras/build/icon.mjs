// Ebb app icon — "sun over the tide". 108dp adaptive canvas, 66dp safe zone.
export const IC = { bgTop:'#3B807A', bgMid:'#2F6F6A', bgBot:'#245B56', sun:'#FBE3D0', wave1:'#CDE8E4', wave2:'#FAF8F5' };
const SUN = { cx:54, cy:52, r:13 };
const SUNP = 'M41 51.2 a13 13 0 0 1 26 0 q0 1.6 -1.6 1.6 h-22.8 q-1.6 0 -1.6 -1.6 z';
const WAVE1 = 'M30.5 59.5 q7.83 -6.2 15.67 0 t15.67 0 t15.67 0';
const WAVE2 = 'M38.3 69.5 q7.83 -6.2 15.67 0 t15.67 0';
export const bgSVG = () => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108"><defs><linearGradient id="g" x1="0" y1="0" x2="0.35" y2="1"><stop offset="0" stop-color="${IC.bgTop}"/><stop offset=".55" stop-color="${IC.bgMid}"/><stop offset="1" stop-color="${IC.bgBot}"/></linearGradient></defs><rect width="108" height="108" fill="url(#g)"/><path d="M0 92 q13.5 -5 27 0 t27 0 t27 0 t27 0 V108 H0Z" fill="#ffffff" opacity=".05"/></svg>`;
const fgShapes = (sun, w1, w2, w2o=1) => `<path d="${SUNP}" fill="${sun}"/><path d="${WAVE1}" fill="none" stroke="${w1}" stroke-width="5.2" stroke-linecap="round" stroke-linejoin="round"/><path d="${WAVE2}" fill="none" stroke="${w2}" stroke-opacity="${w2o}" stroke-width="5.2" stroke-linecap="round" stroke-linejoin="round"/>`;
export const fgSVG = () => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">${fgShapes(IC.sun, IC.wave1, IC.wave2, .9)}</svg>`;
export const monoSVG = (c='#000000') => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">${fgShapes(c,c,c)}</svg>`;
export const fullSVG = (mask='none') => {
  const clip = mask==='circle' ? '<clipPath id="m"><circle cx="54" cy="54" r="36"/></clipPath>' : mask==='squircle' ? '<clipPath id="m"><path d="M54 18C82 18 90 26 90 54S82 90 54 90 18 82 18 54 26 18 54 18Z"/></clipPath>' : mask==='rounded' ? '<clipPath id="m"><rect x="18" y="18" width="72" height="72" rx="16"/></clipPath>' : '';
  const vb = mask==='none' ? '0 0 108 108' : '18 18 72 72';
  const inner = bgSVG().replace(/^<svg[^>]*>|<\/svg>$/g,'').replace('id="g"','id="gb"').replace('url(#g)','url(#gb)') + fgShapes(IC.sun, IC.wave1, IC.wave2, .9);
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${vb}" width="${mask==='none'?108:72}" height="${mask==='none'?108:72}"><defs>${clip}</defs><g ${clip?'clip-path="url(#m)"':''}>${inner}</g></svg>`;
};
// Android VectorDrawable sources
const circlePath = SUNP;
const vdHead = `<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">`;
const strokeP = (d,c,a='1') => `  <path android:pathData="${d}" android:strokeColor="${c}" android:strokeAlpha="${a}" android:strokeWidth="5.2" android:strokeLineCap="round" android:strokeLineJoin="round" android:fillColor="#00000000"/>`;
export const vdForeground = () => `${vdHead}\n  <path android:pathData="${circlePath}" android:fillColor="${IC.sun}"/>\n${strokeP(WAVE1,IC.wave1)}\n${strokeP(WAVE2,IC.wave2,'0.9')}\n</vector>\n`;
export const vdMonochrome = () => `${vdHead}\n  <path android:pathData="${circlePath}" android:fillColor="#FF000000"/>\n${strokeP(WAVE1,'#FF000000')}\n${strokeP(WAVE2,'#FF000000')}\n</vector>\n`;
export const vdBackground = () => `${vdHead}\n  <path android:pathData="M0,0h108v108h-108z">\n    <aapt:attr name="android:fillColor">\n      <gradient android:type="linear" android:startX="0" android:startY="0" android:endX="37.8" android:endY="108">\n        <item android:offset="0" android:color="${IC.bgTop}"/>\n        <item android:offset="0.55" android:color="${IC.bgMid}"/>\n        <item android:offset="1" android:color="${IC.bgBot}"/>\n      </gradient>\n    </aapt:attr>\n  </path>\n</vector>\n`;
export const adaptiveXML = () => `<?xml version="1.0" encoding="utf-8"?>\n<!-- res/mipmap-anydpi-v26/ic_launcher.xml (and ic_launcher_round.xml) -->\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n  <background android:drawable="@drawable/ic_launcher_background"/>\n  <foreground android:drawable="@drawable/ic_launcher_foreground"/>\n  <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>\n</adaptive-icon>\n`;
