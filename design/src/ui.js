// Ebb UI helpers — render HTML strings for Material 3 components using tokens.css + components.css
const I = (n, cls = '') => `<span class="ms ${cls}">${n}</span>`;
const esc = s => String(s);
const attrs = a => a ? Object.entries(a).map(([k, v]) => ` ${k}="${v}"`).join('') : '';
const go = id => id ? ` data-go="${id}"` : '';

function frame({ body, theme = 'light', small = false, land = false, fs200 = false, rtl = false, lang = 'en', title = 'Ebb', css = '../system' }) {
  return `<!doctype html><html lang="${rtl ? 'ar' : lang}" dir="${rtl ? 'rtl' : 'ltr'}" class="theme-${theme}${fs200 ? ' fs200' : ''}"><head><meta charset="utf-8"><title>${title}</title>
<meta name="viewport" content="width=device-width,initial-scale=1"><link rel="stylesheet" href="${css}/tokens.css"><link rel="stylesheet" href="${css}/components.css">
<style>body{background:transparent}${rtl ? 'body,.t-hl,.t-hm,.t-tl,.t-display{font-family:"Noto Sans Arabic",var(--font-body)!important}' : ''}</style></head>
<body><div class="device${small ? ' small' : ''}${land ? ' land' : ''} theme-${theme}">${body}</div></body></html>`;
}
const statusbar = (time = '10:42', cls = '') => `<div class="statusbar ${cls}"><span>${time}</span><span class="row g4">${I('signal_cellular_alt')}${I('wifi')}${I('battery_5_bar')}</span></div>`;
const gesture = () => `<div class="gesture"><i></i></div>`;
const page = (inner, { time, sbcls = '', bg = '' } = {}) => `${statusbar(time, sbcls)}<div class="screen" ${bg ? `style="${bg}"` : ''}>${inner}</div>${gesture()}`;

const iconbtn = (n, { cls = '', label = '', g = '', fill = false } = {}) => `<span class="iconbtn ${cls}" aria-label="${label || n}"${go(g)}>${I(n, fill ? 'fill' : '')}</span>`;
const btn = (kind, label, { icon = '', g = '', cls = '', trail = '' } = {}) => `<span class="btn ${kind} ${cls}"${go(g)}>${icon ? I(icon) : ''}${label}${trail ? I(trail) : ''}</span>`;
const chip = (label, { icon = '', sel = false, cls = '', g = '', trail = '' } = {}) => `<span class="chip ${sel ? 'sel' : ''} ${cls}"${go(g)}>${sel && !icon ? I('check') : icon ? I(icon) : ''}${label}${trail ? I(trail) : ''}</span>`;
const energy = l => `<span class="energy l${l}" aria-label="${['', 'Low', 'Medium', 'High'][l]} energy"><b></b><b></b><b></b></span>`;
const ring = (state = '') => `<span class="ring ${state}"></span>`;
const sw = on => `<span class="sw ${on ? 'on' : ''}"></span>`;
const cb = on => `<span class="cb ${on ? 'on' : ''}"></span>`;
const rb = on => `<span class="rb ${on ? 'on' : ''}"></span>`;
const seg = (opts, on, icons = []) => `<div class="seg">${opts.map((o, i) => `<span class="s ${i === on ? 'on' : ''}">${i === on ? I('check') : icons[i] ? I(icons[i]) : ''}${o}</span>`).join('')}</div>`;

function topbar({ title = '', nav = 'back', actions = [], cls = '', g = '', titleHtml = '' } = {}) {
  const n = nav === 'back' ? iconbtn('arrow_back', { cls: 'on', label: 'Back', g: g }).replace('class="ms ', 'class="ms flip ') : nav === 'close' ? iconbtn('close', { cls: 'on', label: 'Close', g }) : nav ? nav : '<span style="width:8px"></span>';
  return `<div class="topbar ${cls}">${n}${titleHtml || `<div class="title">${title}</div>`}${actions.map(a => typeof a === 'string' && a.startsWith('<') ? a : iconbtn(a)).join('')}</div>`;
}
function topbarMedium({ title, actions = [], g = 'S1', sub = '' }) {
  return `<div class="topbar-medium"><div class="bar">${iconbtn('arrow_back', { cls: 'on', label: 'Back', g }).replace('class="ms ', 'class="ms flip ')}<span class="grow"></span>${actions.map(a => iconbtn(a)).join('')}</div><div class="big">${title}</div>${sub ? `<div class="t-bm c2 px16 mt4">${sub}</div>` : ''}</div>`;
}
const NAV = [['today', 'Today', 'M1'], ['inbox', 'Inbox', 'M2'], ['view_week', 'Week', 'M3'], ['event_repeat', 'Routines', 'M4']];
const navbar = (on = 0, { labels = null, drop = -1 } = {}) => `<nav class="navbar">${NAV.map(([ic, l, g], i) => `<div class="it ${i === on ? 'on' : ''} ${i === drop ? 'droptarget' : ''}"${go(g)}><span class="pill">${I(ic)}</span>${labels ? labels[i] : l}</div>`).join('')}</nav>`;
const fab = ({ icon = 'add', label = '', g = 'T1', style = '' } = {}) => `<span class="fab ${label ? 'ext' : ''}" aria-label="${label || 'Quick capture'}"${go(g)} style="${style}">${I(icon)}${label}</span>`;

const C = { amber: 'amber', sky: 'sky', green: 'green', lemon: 'lemon', blue: 'blue', vermillion: 'vermillion', rose: 'rose', slate: 'slate' };
const ticon = (icon, color = 'slate') => `<span class="ticon" style="background:var(--tc-${color}-tint);color:var(--on-surface)">${I(icon)}</span>`;

function taskCard({ title, color = 'slate', icon = '', est = '', en = 0, sub = '', repeat = false, bell = false, done = false, tag = '', sel = false, selMode = false, handle = true, dragging = false, g = 'T2', style = '', extra = '' }) {
  return `<div class="task ${done ? 'done' : ''} ${sel ? 'sel' : ''} ${dragging ? 'dragging' : ''}" style="${style}"${go(g)}>
<span class="strip" style="background:var(--tc-${color})"></span>
<span class="check">${selMode ? ring(sel ? 'sel' : '') : ring(done ? 'done' : '')}</span>
${icon ? `<span class="ticon" style="background:var(--tc-${color}-tint);margin-right:8px">${I(icon)}</span>` : ''}
<div class="body"><div class="ttl clamp2">${title}</div><div class="meta">${tag ? `<span class="tag">${tag}</span>` : ''}${est ? `<span>${I('schedule')}${est}</span>` : ''}${en ? `<span>${energy(en)}</span>` : ''}${sub ? `<span>${I('checklist')}${sub}</span>` : ''}${repeat ? `<span>${I('repeat')}</span>` : ''}${bell ? `<span>${I('notifications')}</span>` : ''}${extra}</div></div>
${handle ? `<span class="handle">${I('drag_indicator')}</span>` : ''}</div>`;
}

// ----- timeline -----
const tm = s => { const [h, m] = s.split(':').map(Number); return h * 60 + m; };
const fmtDur = m => m >= 60 ? `${Math.floor(m / 60)} h${m % 60 ? ' ' + (m % 60) + ' min' : ''}` : `${m} min`;
function timeline({ from = '08:00', to = '16:00', hour = 72, now = null, items = [], past = true, hourLabels = null, height = null, rail12 = false }) {
  const f = tm(from), t = tm(to), y = s => (tm(s) - f) / 60 * hour;
  let html = '';
  for (let h = Math.ceil(f / 60); h * 60 <= t; h++) {
    const lbl = hourLabels ? hourLabels(h) : `${String(h).padStart(2, '0')}:00`;
    html += `<div class="hr" style="top:${(h * 60 - f) / 60 * hour}px"><span>${lbl}</span></div>`;
  }
  if (now && past) html += `<div class="pastzone" style="height:${y(now)}px"></div>`;
  for (const it of items) {
    const top = y(it.s), h = it.e ? Math.max((tm(it.e) - tm(it.s)) / 60 * hour, it.type === 'buffer' ? 4 : 28) : 28;
    const st = `top:${top + 1}px;height:${h - 2}px;${it.style || ''}`;
    if (it.type === 'buffer') { html += `<div class="buffer" style="${st}">${h >= 12 ? 'Breather' : ''}</div>`; continue; }
    if (it.type === 'gap') { html += `<div class="gap" style="${st}"${go(it.g || 'T1')}>${it.label || 'Free · ' + fmtDur(tm(it.e) - tm(it.s))}</div>`; continue; }
    const dur = tm(it.e) - tm(it.s), short = h < 46 || it.short;
    const isEv = it.type === 'event';
    const cls = ['blk', short ? 'short' : '', it.done ? 'done' : '', it.past ? 'past' : '', it.current ? 'current' : '', isEv ? 'event' : '', it.ghost ? 'ghost' : '', it.lifted ? 'lifted' : '', it.half ? 'half' : '', it.half2 ? 'half2' : '', it.cls || ''].join(' ');
    const col = it.color || 'slate';
    let bg = `background:var(--tc-${col}-tint);`;
    if (it.current && it.drain != null) bg = `background:linear-gradient(to bottom, var(--surface) 0 ${it.drain}%, var(--tc-${col}-tint) ${it.drain}% 100%);`;
    if (isEv) bg = `--evc:var(--tc-${col});`;
    const timeTxt = `${it.s}–${it.e}`;
    const meta = [];
    if (!short) meta.push(`<span>${timeTxt}</span>`, `<span class="hide200">${it.durLabel || fmtDur(dur)}</span>`);
    if (it.en) meta.push(`<span class="hide200">${energy(it.en)}</span>`);
    if (it.sub) meta.push(`<span class="hide200">${I('checklist')}${it.sub}</span>`);
    if (it.bell) meta.push(`<span class="hide200">${I('notifications')}</span>`);
    if (it.pin) meta.push(`<span>${I('keep', 'fill')}</span>`);
    if (it.routine) meta.push(`<span class="dots hide200">${Array.from({ length: it.routine[1] }, (_, i) => `<i class="${i < it.routine[0] ? 'on' : ''}"></i>`).join('')}</span><span class="hide200">${it.routine[0]}/${it.routine[1]}</span>`);
    if (isEv) meta.push(`<span class="hide200">${I('calendar_month')}${it.cal || 'Personal'}</span>`);
    const shortMeta = short ? `<span class="t-lm c2 hide200" style="flex:none">${it.s} · ${it.durLabel || fmtDur(dur)}${it.pin ? ' ' + I('keep', 'fill s16') : ''}</span>` : '';
    html += `<div class="${cls}" style="${st}${bg}"${go(it.g || (isEv ? 'D7' : 'T2'))}>
${isEv || it.ghost ? '' : `<span class="strip" style="background:var(--tc-${col})"></span>`}
<span class="ic">${I(isEv ? 'event' : it.icon || 'radio_button_unchecked', it.iconFill ? 'fill' : '')}</span>
<div class="bd"><div class="ttl">${it.title}</div>${meta.length ? `<div class="mt">${meta.join('')}</div>` : ''}</div>${shortMeta}
${isEv || it.ghost ? (it.pin === false ? '' : '') : `<span class="ck">${ring(it.done ? 'done' : it.sel ? 'sel' : '')}</span>`}
${it.left ? `<span class="left">${it.left}</span>` : ''}</div>`;
    if (it.tooltip) html += `<div class="tooltip" style="top:${top - 30}px;left:${it.tipLeft || 140}px">${it.tooltip}</div>`;
  }
  if (now) html += `<div class="nowline" style="top:${y(now)}px"><span class="pill">${now}</span></div>`;
  return `<div class="tl" style="height:${height || (t - f) / 60 * hour}px">${html}</div>`;
}

function pring(pct, size = 40, stroke = 4, inner = '', col = 'var(--primary)') {
  const r = (size - stroke) / 2, c = 2 * Math.PI * r;
  return `<span class="pring" style="width:${size}px;height:${size}px"><svg viewBox="0 0 ${size} ${size}"><circle cx="${size / 2}" cy="${size / 2}" r="${r}" fill="none" stroke="var(--surface-variant)" stroke-width="${stroke}"/><circle cx="${size / 2}" cy="${size / 2}" r="${r}" fill="none" stroke="${col}" stroke-width="${stroke}" stroke-linecap="round" stroke-dasharray="${c * pct} ${c}"/></svg>${inner}</span>`;
}

const banner = (icon, msg, action, { cls = '', g = '', close = true } = {}) => `<div class="banner ${cls}">${I(icon)}<span class="msg">${msg}</span>${action ? `<span class="btn text sm"${go(g)}>${action}</span>` : ''}${close ? iconbtn('close', { label: 'Dismiss' }).replace('iconbtn', 'iconbtn" style="width:40px;height:40px;color:inherit') : ''}</div>`;
const snack = (msg, act = 'Undo', style = '') => `<div class="snack" role="status" style="${style}"><span class="grow">${msg}</span>${act ? `<span class="act">${act}</span>` : ''}</div>`;
const sheet = ({ title = '', sub = '', body = '', actions = '', h = '', full = false, grab = true, top = '' }) => `<div class="scrim"></div><div class="sheet ${full ? 'full' : ''}" style="${h ? `height:${h}px;` : ''}${top}">${grab ? '<div class="grab"><i></i></div>' : ''}${title ? `<div class="sh-title">${title}</div>` : ''}${sub ? `<div class="sh-sub">${sub}</div>` : ''}<div class="sh-body">${body}</div>${actions ? `<div class="sh-actions">${actions}</div>` : ''}</div>`;
const dialog = ({ icon = '', title, body, actions, center = false, extra = '' }) => `<div class="scrim"></div><div class="dialog" role="dialog">${icon ? `<div class="d-icon">${I(icon, 's32')}</div>` : ''}<div class="d-title ${icon || center ? 'c' : ''}">${title}</div><div class="d-body">${body}</div>${extra}<div class="d-actions">${actions}</div></div>`;
const li = ({ icon = '', l1, l2 = '', trail = '', g = '', cls = '', lead = '' }) => `<div class="li ${l2 ? 'tall' : ''} ${cls}"${go(g)}>${lead}${icon ? `<span class="lic">${I(icon)}</span>` : ''}<div class="ltxt"><div class="l1">${l1}</div>${l2 ? `<div class="l2">${l2}</div>` : ''}</div>${trail}</div>`;
const chev = () => `<span class="c2">${I('chevron_right', 'flip')}</span>`;
const sect = t => `<div class="sect">${t}</div>`;
const field = ({ label = '', val = '', ph = '', focus = false, helper = '', style = '', multi = false }) => `<div class="field ${focus ? 'focus' : ''}" style="${style}">${label ? `<div class="lab">${label}</div>` : ''}<div class="val">${val ? val : `<span class="ph">${ph}</span>`}${focus ? '<span class="caret"></span>' : ''}</div>${helper ? `<div class="helper">${helper}</div>` : ''}</div>`;
const steps = (n, on) => `<div class="steps">${Array.from({ length: n }, (_, i) => `<i class="${i < on ? 'on' : ''}"></i>`).join('')}</div>`;
const coach = ({ text, style, arrow = 'top:-7px;left:24px', got = 'Got it' }) => `<div class="coach" style="${style}"><span class="ca" style="${arrow}"></span><div class="t-bm">${text}</div><div class="got"><span>${got}</span></div></div>`;

function keyboard(sugg = ['I', 'the', 'Call']) {
  const r = s => s.split('').map(c => `<span class="k">${c}</span>`).join('');
  return `<div class="kbd"><div class="sugg">${sugg.map(s => `<span>${s}</span>`).join('')}</div><div class="kr">${r('qwertyuiop')}</div><div class="kr" style="padding:0 16px">${r('asdfghjkl')}</div><div class="kr"><span class="k w">${I('shift', 's20')}</span>${r('zxcvbnm')}<span class="k w">${I('backspace', 's20')}</span></div><div class="kr"><span class="k w" style="font-size:14px">?123</span><span class="k">,</span><span class="k sp"></span><span class="k">.</span><span class="k en">${I('keyboard_return', 's20')}</span></div></div>`;
}

// calm theme-tinted illustration slot (final art by the illustration set in extras/)
function ill(motif = 'waves', tone = 'p', size = 160) {
  const bg = { p: 'var(--primary-container)', s: 'var(--secondary-container)', t: 'var(--tertiary-container)' }[tone];
  const fg = { p: 'var(--primary)', s: 'var(--secondary)', t: 'var(--tertiary)' }[tone];
  return `<svg viewBox="0 0 160 160" width="${size}" height="${size}" aria-hidden="true"><circle cx="80" cy="80" r="72" fill="${bg}"/>
<path d="M14 104 Q37 92 60 104 T106 104 T152 104 L152 120 Q120 156 80 152 Q40 156 8 120 Z" fill="${fg}" opacity=".18"/>
<path d="M20 118 Q42 108 64 118 T108 118 T150 118 L146 128 Q118 154 80 152 Q42 154 14 128 Z" fill="${fg}" opacity=".22"/>
<circle cx="122" cy="40" r="8" fill="${fg}" opacity=".25"/><circle cx="36" cy="52" r="5" fill="${fg}" opacity=".2"/>
<text x="80" y="92" text-anchor="middle" font-family="Material Symbols Rounded" font-size="56" fill="${fg}" style="font-variation-settings:'FILL' 1">${motif}</text></svg>`;
}

const skel = (w, h, r = 8, st = '') => `<div class="sk" style="width:${w};height:${h}px;border-radius:${r}px;${st}"></div>`;

module.exports = { I, esc, go, frame, statusbar, gesture, page, iconbtn, btn, chip, energy, ring, sw, cb, rb, seg, topbar, topbarMedium, navbar, fab, ticon, taskCard, timeline, tm, fmtDur, pring, banner, snack, sheet, dialog, li, chev, sect, field, steps, coach, keyboard, ill, skel, NAV };
