// Ebb extras — shared tokens (spec §6) + helpers
export const THEMES = {
  light: { name:'Tide light', bg:'#FAF8F5', surface:'#FFFFFF', surfaceVariant:'#F0ECE6', onSurface:'#1F2421', onSurfaceVariant:'#535C58',
    primary:'#2F6F6A', onPrimary:'#FFFFFF', primaryContainer:'#CDE8E4', onPrimaryContainer:'#0E3B37',
    secondary:'#5E5288', onSecondary:'#FFFFFF', secondaryContainer:'#E6E0F7', onSecondaryContainer:'#2A2250',
    tertiary:'#9A5B2E', tertiaryContainer:'#FBE3D0', onTertiaryContainer:'#3D2412', outline:'#C9C4BC', destructive:'#A3402F',
    wbg:'#FAF8F5', winner:'#F0ECE6', dark:false },
  dark: { name:'Tide dark', bg:'#121614', surface:'#1A1F1D', surfaceVariant:'#252B29', onSurface:'#E3E6E3', onSurfaceVariant:'#A9B2AE',
    primary:'#8FD1C9', onPrimary:'#00332F', primaryContainer:'#1F4F4A', onPrimaryContainer:'#CDE8E4',
    secondary:'#C9BEF0', onSecondary:'#2A2250', secondaryContainer:'#3F3766', onSecondaryContainer:'#E6E0F7',
    tertiary:'#F0B98D', tertiaryContainer:'#5C3A1F', onTertiaryContainer:'#FBE3D0', outline:'#3A423F', destructive:'#F2A190',
    wbg:'#1A1F1D', winner:'#252B29', dark:true },
  // §6.3.4 dynamic-colour sample: purple wallpaper (M3 baseline-style scheme from a lavender seed)
  dynLight: { name:'Dynamic (purple wallpaper) light', bg:'#FEF7FF', surface:'#FFFFFF', surfaceVariant:'#ECE6F0', onSurface:'#1D1B20', onSurfaceVariant:'#49454F',
    primary:'#65558F', onPrimary:'#FFFFFF', primaryContainer:'#E9DDFF', onPrimaryContainer:'#22005D',
    secondary:'#625B71', onSecondary:'#FFFFFF', secondaryContainer:'#E8DEF8', onSecondaryContainer:'#1E192B',
    tertiary:'#7E5260', tertiaryContainer:'#FFD9E3', onTertiaryContainer:'#31101D', outline:'#CAC4D0', destructive:'#A3402F',
    wbg:'#F7F2FA', winner:'#ECE6F0', dark:false },
  dynDark: { name:'Dynamic (purple wallpaper) dark', bg:'#141218', surface:'#1D1B20', surfaceVariant:'#2B2930', onSurface:'#E6E0E9', onSurfaceVariant:'#CAC4D0',
    primary:'#CFBCFF', onPrimary:'#381E72', primaryContainer:'#4F378B', onPrimaryContainer:'#EADDFF',
    secondary:'#CCC2DC', onSecondary:'#332D41', secondaryContainer:'#4A4458', onSecondaryContainer:'#E8DEF8',
    tertiary:'#EFB8C8', tertiaryContainer:'#633B48', onTertiaryContainer:'#FFD8E4', outline:'#49454F', destructive:'#F2A190',
    wbg:'#211F26', winner:'#2B2930', dark:true },
};
// §6.3.3 Okabe–Ito task colours (fixed, never wallpaper-derived)
export const TASK = {
  amber:{s:'#E69F00',l:'#FAEAC7',d:'#574514'}, sky:{s:'#56B4E9',l:'#DAEEFA',d:'#2C4C5A'}, green:{s:'#009E73',l:'#C7EAE0',d:'#124537'},
  lemon:{s:'#F0E442',l:'#FCF9D5',d:'#5A5A28'}, blue:{s:'#0072B2',l:'#C7E0EE',d:'#12384A'}, vermillion:{s:'#D55E00',l:'#F6DCC7',d:'#523214'},
  rose:{s:'#CC79A7',l:'#F4E2EC',d:'#4F3A46'}, slate:{s:'#8C8C8C',l:'#E6E6E6',d:'#3C403E'},
};
export const hexA = (hex, a) => { const n=parseInt(hex.slice(1),16); return `rgba(${n>>16},${(n>>8)&255},${n&255},${a})`; };
export function themeVars(t, opts={}) {
  const T = THEMES[t]; const wbg = opts.alpha!=null ? hexA(T.wbg, opts.alpha) : T.wbg;
  const tasks = Object.entries(TASK).map(([k,v])=>`--t-${k}:${v.s};--t-${k}-tint:${T.dark?v.d:v.l};`).join('');
  return Object.entries(T).filter(([k,v])=>typeof v==='string' && v.startsWith('#')).map(([k,v])=>`--${k}:${v};`).join('') + `--wbg:${wbg};` + tasks + `color-scheme:${T.dark?'dark':'light'};`;
}
export const BASE_CSS = (rel='') => `
<link rel="stylesheet" href="${rel}assets/fonts/fonts.css">
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:'Atkinson Hyperlegible Next','Atkinson Hyperlegible',sans-serif;-webkit-font-smoothing:antialiased;background:transparent}
.ms{font-family:'Material Symbols Rounded';font-weight:400;font-style:normal;font-size:24px;line-height:1;letter-spacing:normal;text-transform:none;display:inline-block;white-space:nowrap;direction:ltr;font-feature-settings:'liga';font-variation-settings:'FILL' 0,'wght' 400,'GRAD' 0,'opsz' 24;user-select:none;vertical-align:middle}
.ms.f{font-variation-settings:'FILL' 1,'wght' 400,'GRAD' 0,'opsz' 24}
.lex{font-family:'Lexend',sans-serif}
.mono{font-family:'Atkinson Hyperlegible Mono',monospace;font-variant-numeric:tabular-nums}
.label-s{font-size:11px;line-height:16px;font-weight:700;letter-spacing:.1px}
.label-m{font-size:12px;line-height:16px;font-weight:700;letter-spacing:.1px}
.label-l{font-size:14px;line-height:20px;font-weight:700;letter-spacing:.1px}
.body-m{font-size:14px;line-height:20px}
.body-l{font-size:16px;line-height:24px}
.title-m{font-size:16px;line-height:24px;font-weight:700}
.title-l{font-family:'Lexend';font-size:20px;line-height:28px;font-weight:500}
.ell{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
</style>`;
export const ic = (n, size=24, cls='', style='') => `<span class="ms ${cls}" style="font-size:${size}px;${style}">${n}</span>`;
export const esc = s => String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;');
