// Single source of truth for Ebb design tokens (spec §6). Run: node tokens.js
const fs = require('fs'), path = require('path');
const OUT = path.join(__dirname, '..', 'system');

const color = {
  light: { // Tide light (§6.3.1) + derived M3 roles
    background:'#FAF8F5', surface:'#FFFFFF', surfaceVariant:'#F0ECE6', surfaceContainer:'#F5F2ED', surfaceContainerHigh:'#ECE8E2',
    onSurface:'#1F2421', onSurfaceVariant:'#535C58', onTintVariant:'#535C58',
    primary:'#2F6F6A', onPrimary:'#FFFFFF', primaryContainer:'#CDE8E4', onPrimaryContainer:'#0E3B37',
    secondary:'#5E5288', onSecondary:'#FFFFFF', secondaryContainer:'#E6E0F7', onSecondaryContainer:'#1E1640',
    tertiary:'#9A5B2E', onTertiary:'#FFFFFF', tertiaryContainer:'#FBE3D0', onTertiaryContainer:'#3A1E08',
    outline:'#C9C4BC', outlineStrong:'#857F76', destructive:'#A3402F', onDestructive:'#FFFFFF', success:'#2F6F6A',
    inverseSurface:'#2E3330', inverseOnSurface:'#F0F1EE', inversePrimary:'#8FD1C9', scrim:'rgba(0,0,0,0.32)'
  },
  dark: { // Tide dark (§6.3.2)
    background:'#121614', surface:'#1A1F1D', surfaceVariant:'#252B29', surfaceContainer:'#1E2321', surfaceContainerHigh:'#2A302E',
    onSurface:'#E3E6E3', onSurfaceVariant:'#A9B2AE', onTintVariant:'#D0D5D2',
    primary:'#8FD1C9', onPrimary:'#00332F', primaryContainer:'#1F4F4A', onPrimaryContainer:'#CDE8E4',
    secondary:'#C9BEF0', onSecondary:'#2F2757', secondaryContainer:'#3F3766', onSecondaryContainer:'#E6E0F7',
    tertiary:'#F0B98D', onTertiary:'#4A2808', tertiaryContainer:'#5C3A1F', onTertiaryContainer:'#FBE3D0',
    outline:'#3A423F', outlineStrong:'#7D8783', destructive:'#F2A190', onDestructive:'#4A120A', success:'#8FD1C9',
    inverseSurface:'#E3E6E3', inverseOnSurface:'#2E3330', inversePrimary:'#2F6F6A', scrim:'rgba(0,0,0,0.55)'
  },
  dynamicLight: { // sample dynamic scheme from a purple wallpaper (§6.3.4)
    background:'#FEF7FF', surface:'#FFFFFF', surfaceVariant:'#F0E9F4', surfaceContainer:'#F7F1FA', surfaceContainerHigh:'#ECE6F0',
    onSurface:'#1D1B20', onSurfaceVariant:'#49454F', onTintVariant:'#49454F',
    primary:'#65558F', onPrimary:'#FFFFFF', primaryContainer:'#EADDFF', onPrimaryContainer:'#21005D',
    secondary:'#625B71', onSecondary:'#FFFFFF', secondaryContainer:'#E8DEF8', onSecondaryContainer:'#1D192B',
    tertiary:'#7D5260', onTertiary:'#FFFFFF', tertiaryContainer:'#FFD8E4', onTertiaryContainer:'#31111D',
    outline:'#CAC4D0', outlineStrong:'#79747E', destructive:'#A3402F', onDestructive:'#FFFFFF', success:'#65558F',
    inverseSurface:'#322F35', inverseOnSurface:'#F5EFF7', inversePrimary:'#D0BCFF', scrim:'rgba(0,0,0,0.32)'
  },
  dynamicDark: {
    background:'#141218', surface:'#1D1B20', surfaceVariant:'#2B2930', surfaceContainer:'#211F26', surfaceContainerHigh:'#2B2930',
    onSurface:'#E6E0E9', onSurfaceVariant:'#CAC4D0', onTintVariant:'#D0D5D2',
    primary:'#D0BCFF', onPrimary:'#381E72', primaryContainer:'#4F378B', onPrimaryContainer:'#EADDFF',
    secondary:'#CCC2DC', onSecondary:'#332D41', secondaryContainer:'#4A4458', onSecondaryContainer:'#E8DEF8',
    tertiary:'#EFB8C8', onTertiary:'#492532', tertiaryContainer:'#633B48', onTertiaryContainer:'#FFD8E4',
    outline:'#49454F', outlineStrong:'#938F99', destructive:'#F2A190', onDestructive:'#4A120A', success:'#D0BCFF',
    inverseSurface:'#E6E0E9', inverseOnSurface:'#322F35', inversePrimary:'#65558F', scrim:'rgba(0,0,0,0.55)'
  },
  highContrastLight: { // §6.3.2 high contrast
    background:'#FFFFFF', surface:'#FFFFFF', surfaceVariant:'#F2F2F2', surfaceContainer:'#FFFFFF', surfaceContainerHigh:'#EDEDED',
    onSurface:'#000000', onSurfaceVariant:'#1F1F1F', onTintVariant:'#1F1F1F',
    primary:'#00504A', onPrimary:'#FFFFFF', primaryContainer:'#CDE8E4', onPrimaryContainer:'#00201D',
    secondary:'#3E3268', onSecondary:'#FFFFFF', secondaryContainer:'#E6E0F7', onSecondaryContainer:'#000000',
    tertiary:'#6B3A14', onTertiary:'#FFFFFF', tertiaryContainer:'#FBE3D0', onTertiaryContainer:'#000000',
    outline:'#000000', outlineStrong:'#000000', destructive:'#8A2B1C', onDestructive:'#FFFFFF', success:'#00504A',
    inverseSurface:'#000000', inverseOnSurface:'#FFFFFF', inversePrimary:'#A6EDE4', scrim:'rgba(0,0,0,0.6)'
  },
  highContrastDark: {
    background:'#000000', surface:'#000000', surfaceVariant:'#141414', surfaceContainer:'#000000', surfaceContainerHigh:'#1A1A1A',
    onSurface:'#FFFFFF', onSurfaceVariant:'#F0F0F0', onTintVariant:'#F0F0F0',
    primary:'#A6EDE4', onPrimary:'#00201D', primaryContainer:'#00504A', onPrimaryContainer:'#FFFFFF',
    secondary:'#E0D8FF', onSecondary:'#1E1640', secondaryContainer:'#3F3766', onSecondaryContainer:'#FFFFFF',
    tertiary:'#FFD3B0', onTertiary:'#2A1404', tertiaryContainer:'#5C3A1F', onTertiaryContainer:'#FFFFFF',
    outline:'#FFFFFF', outlineStrong:'#FFFFFF', destructive:'#FFB4A6', onDestructive:'#2A0700', success:'#A6EDE4',
    inverseSurface:'#FFFFFF', inverseOnSurface:'#000000', inversePrimary:'#00504A', scrim:'rgba(0,0,0,0.7)'
  }
};
const calmThemes = { // §6.3.5 accent swaps
  tide:{light:'#2F6F6A',dark:'#8FD1C9'}, sage:{light:'#4F6B3A',dark:'#B5D39C'},
  dusk:{light:'#5E5288',dark:'#C9BEF0'}, sand:{light:'#8A5A2B',dark:'#EBC193'}
};
const taskColors = { // §6.3.3 Okabe–Ito
  amber:{strong:'#E69F00',tintLight:'#FAEAC7',tintDark:'#574514',pattern:'solid'},
  sky:{strong:'#56B4E9',tintLight:'#DAEEFA',tintDark:'#2C4C5A',pattern:'dots'},
  green:{strong:'#009E73',tintLight:'#C7EAE0',tintDark:'#124537',pattern:'diagonal'},
  lemon:{strong:'#F0E442',tintLight:'#FCF9D5',tintDark:'#5A5A28',pattern:'horizontal'},
  blue:{strong:'#0072B2',tintLight:'#C7E0EE',tintDark:'#12384A',pattern:'vertical'},
  vermillion:{strong:'#D55E00',tintLight:'#F6DCC7',tintDark:'#523214',pattern:'cross-hatch'},
  rose:{strong:'#CC79A7',tintLight:'#F4E2EC',tintDark:'#4F3A46',pattern:'waves'},
  slate:{strong:'#8C8C8C',tintLight:'#E6E6E6',tintDark:'#3C403E',pattern:'none'}
};
const type = { // §6.4, sizes in sp (1rem = 16sp so system font scale maps to root size)
  display:{font:'Lexend',size:40,line:48,weight:500}, timerXL:{font:'Atkinson Hyperlegible Mono',size:64,line:72,weight:500},
  timerL:{font:'Atkinson Hyperlegible Mono',size:40,line:48,weight:500}, headlineL:{font:'Lexend',size:28,line:36,weight:500},
  headlineM:{font:'Lexend',size:24,line:32,weight:500}, titleL:{font:'Lexend',size:20,line:28,weight:500},
  titleM:{font:'Atkinson Hyperlegible Next',size:16,line:24,weight:700}, bodyL:{font:'Atkinson Hyperlegible Next',size:16,line:24,weight:400},
  bodyM:{font:'Atkinson Hyperlegible Next',size:14,line:20,weight:400}, labelL:{font:'Atkinson Hyperlegible Next',size:14,line:20,weight:700,tracking:0.1},
  labelM:{font:'Atkinson Hyperlegible Next',size:12,line:16,weight:700,tracking:0.1}, labelS:{font:'Atkinson Hyperlegible Next',size:11,line:16,weight:700,tracking:0.1}
};
const space = {1:4,2:8,3:12,4:16,5:20,6:24,8:32,10:40,12:48,16:64};
const layout = {marginCompact:16, marginMedium:24, marginExpanded:32, cardPadCompact:12, cardPadComfortable:16, listGap:8,
  hourRail:56, hourCompact:48, hourComfortable:72, hourSpacious:112, blockMin:28, blockInset:8, touchMin:48, touchBig:56};
const shape = {xs:4, sm:8, md:12, lg:16, xl:28, full:9999};
const elevation = {0:{dp:0,use:'background, timeline'},1:{dp:1,use:'cards, blocks'},2:{dp:3,use:'bottom nav, app bar on scroll'},
  3:{dp:6,use:'FAB, sheets'},4:{dp:8,use:'dragged item (+shadow, scale 1.03)'},5:{dp:12,use:'dialogs, celebration overlay'}};
const shadows = {0:'none',1:'0 1px 2px rgba(0,0,0,.08)',2:'0 1px 3px rgba(0,0,0,.10), 0 2px 6px rgba(0,0,0,.06)',
  3:'0 2px 6px rgba(0,0,0,.12), 0 6px 12px rgba(0,0,0,.08)',4:'0 6px 14px rgba(0,0,0,.18), 0 10px 24px rgba(0,0,0,.12)',5:'0 8px 20px rgba(0,0,0,.18), 0 16px 32px rgba(0,0,0,.12)'};
const motion = {short:{duration:'125ms',easing:'cubic-bezier(0.2,0,0,1)',use:'checks, chip select, ripples'},
  medium:{duration:'300ms',easing:'cubic-bezier(0.05,0.7,0.1,1)',use:'sheets in, block move, shift (stagger 20ms)'},
  long:{duration:'450ms',easing:'cubic-bezier(0.2,0,0,1)',use:'celebrations, onboarding transitions'},
  reduced:{duration:'150ms',easing:'linear',use:'cross-fade replacement for all movement when reduce motion is on'}};

// ---------- W3C design tokens JSON ----------
const kebab = s => s.replace(/([a-z0-9])([A-Z]+)/g, (m, a, b) => a + '-' + b.toLowerCase());
const j = { $description:'Ebb design tokens (spec §6). W3C Design Tokens format.', color:{}, taskColor:{}, calmTheme:{}, typography:{}, spacing:{}, layout:{}, shape:{}, elevation:{}, motion:{} };
for (const [scheme, roles] of Object.entries(color)) { j.color[scheme] = {}; for (const [k,v] of Object.entries(roles)) j.color[scheme][k] = {$type:'color',$value:v}; }
for (const [k,v] of Object.entries(taskColors)) j.taskColor[k] = {strong:{$type:'color',$value:v.strong},tintLight:{$type:'color',$value:v.tintLight},tintDark:{$type:'color',$value:v.tintDark},pattern:{$type:'string',$value:v.pattern}};
for (const [k,v] of Object.entries(calmThemes)) j.calmTheme[k] = {primaryLight:{$type:'color',$value:v.light},primaryDark:{$type:'color',$value:v.dark}};
for (const [k,v] of Object.entries(type)) j.typography[k] = {$type:'typography',$value:{fontFamily:v.font,fontSize:`${v.size}sp`,lineHeight:`${v.line}sp`,fontWeight:v.weight,letterSpacing:`${v.tracking||0}sp`}};
for (const [k,v] of Object.entries(space)) j.spacing[k] = {$type:'dimension',$value:`${v}dp`};
for (const [k,v] of Object.entries(layout)) j.layout[k] = {$type:'dimension',$value:`${v}dp`};
for (const [k,v] of Object.entries(shape)) j.shape[k] = {$type:'dimension',$value: k==='full'?'50%':`${v}dp`};
for (const [k,v] of Object.entries(elevation)) j.elevation[k] = {$type:'dimension',$value:`${v.dp}dp`,$description:v.use};
for (const [k,v] of Object.entries(motion)) j.motion[k] = {$type:'transition',$value:{duration:v.duration,timingFunction:v.easing},$description:v.use};
fs.writeFileSync(path.join(OUT,'tokens.json'), JSON.stringify(j,null,2));

// ---------- CSS ----------
const roleVars = r => Object.entries(r).map(([k,v]) => `  --${kebab(k)}: ${v};`).join('\n');
const tintVars = mode => Object.entries(taskColors).map(([k,v]) => `  --tc-${k}: ${v.strong};\n  --tc-${k}-tint: ${mode==='dark'?v.tintDark:v.tintLight};`).join('\n');
let css = `/* Ebb design tokens — generated from src/tokens.js (spec §6). 1 CSS px = 1 dp; 1rem = 16 sp (scale root font-size for system font scale). */
@font-face{font-family:'Lexend';src:url(fonts/lexend.woff2) format('woff2');font-weight:300 700;font-display:block}
@font-face{font-family:'Atkinson Hyperlegible Next';src:url(fonts/atkinson-next-400.woff2) format('woff2');font-weight:400;font-display:block}
@font-face{font-family:'Atkinson Hyperlegible Next';src:url(fonts/atkinson-next-700.woff2) format('woff2');font-weight:700;font-display:block}
@font-face{font-family:'Atkinson Hyperlegible Mono';src:url(fonts/atkinson-mono-500.woff2) format('woff2');font-weight:500;font-display:block}
@font-face{font-family:'Material Symbols Rounded';src:url(fonts/material-symbols-rounded.woff2) format('woff2');font-weight:400;font-display:block}
@font-face{font-family:'Noto Sans Arabic';src:url(fonts/noto-arabic.woff2) format('woff2');font-weight:400 700;font-display:block}
:root, .theme-light {
${roleVars(color.light)}
${tintVars('light')}
}
.theme-dark {
${roleVars(color.dark)}
${tintVars('dark')}
}
.theme-dyn-light {
${roleVars(color.dynamicLight)}
${tintVars('light')}
}
.theme-dyn-dark {
${roleVars(color.dynamicDark)}
${tintVars('dark')}
}
.theme-hc-light {
${roleVars(color.highContrastLight)}
${tintVars('light')}
  --hc-border: 2px solid #000000;
}
.theme-hc-dark {
${roleVars(color.highContrastDark)}
${tintVars('dark')}
  --hc-border: 2px solid #FFFFFF;
}
${Object.entries(calmThemes).map(([k,v])=>`.accent-${k}.theme-light{--primary:${v.light}} .accent-${k}.theme-dark{--primary:${v.dark}}`).join('\n')}
:root {
  --font-heading: 'Lexend', system-ui, sans-serif;
  --font-body: 'Atkinson Hyperlegible Next', 'Noto Sans Arabic', system-ui, sans-serif;
  --font-mono: 'Atkinson Hyperlegible Mono', ui-monospace, monospace;
${Object.entries(type).map(([k,v])=>`  --type-${kebab(k)}: ${v.weight} ${v.size/16}rem/${v.line/16}rem ${v.font.includes('Mono')?'var(--font-mono)':v.font==='Lexend'?'var(--font-heading)':'var(--font-body)'};`).join('\n')}
${Object.entries(space).map(([k,v])=>`  --space-${k}: ${v}px;`).join('\n')}
${Object.entries(layout).map(([k,v])=>`  --${kebab(k)}: ${v}px;`).join('\n')}
${Object.entries(shape).map(([k,v])=>`  --shape-${k}: ${v}px;`).join('\n')}
${Object.entries(shadows).map(([k,v])=>`  --elev-${k}: ${v};`).join('\n')}
${Object.entries(motion).map(([k,v])=>`  --motion-${k}: ${v.duration} ${v.easing};`).join('\n')}
}
`;
fs.writeFileSync(path.join(OUT,'tokens.css'), css);
module.exports = { color, taskColors, type, calmThemes };
if (require.main === module) console.log('tokens written');
