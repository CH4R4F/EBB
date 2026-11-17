// Shared Today (M1) scene builder — reused as the background for sheets/dialogs.
const U = require('../ui');
const { I, iconbtn, btn, chip, topbar, navbar, fab, timeline, pring, banner } = U;

const ITEMS = [
  { s: '08:45', e: '09:05', title: 'Work start-up', icon: 'wb_sunny', color: 'rose', done: true, past: true, routine: [5, 5], g: 'R2' },
  { type: 'buffer', s: '09:05', e: '09:10' },
  { s: '09:10', e: '09:55', title: 'Emails', icon: 'mail', color: 'sky', done: true, past: true, en: 1 },
  { type: 'buffer', s: '09:55', e: '10:00' },
  { s: '10:00', e: '11:15', title: 'Write project brief', icon: 'edit_note', color: 'amber', current: true, drain: 56, left: '33 min left', sub: '2/5', bell: true, en: 3 },
  { type: 'buffer', s: '11:15', e: '11:20' },
  { s: '11:20', e: '11:45', title: 'Call bank', icon: 'call', color: 'green', bell: true, en: 2 },
  { type: 'event', s: '12:00', e: '13:00', title: 'Lunch with Ines', color: 'blue', cal: 'Personal', pin: true },
  { type: 'gap', s: '13:05', e: '13:30' },
  { s: '13:30', e: '13:45', title: 'Buy stamps', icon: 'local_post_office', color: 'lemon' },
  { s: '14:00', e: '15:00', title: 'Design poster', icon: 'brush', color: 'blue', sub: '0/3', en: 3 },
  { s: '15:30', e: '16:15', title: 'Dentist', icon: 'medical_services', color: 'vermillion', pin: true, bell: true },
];

function appbar({ date = 'Mon 16 Nov', sub = '', today = true } = {}) {
  const t = `<div class="row grow" style="min-width:0">${iconbtn('chevron_left', { cls: 'on', label: 'Previous day' }).replace('class="ms ', 'class="ms flip ')}<div class="grow" style="min-width:0;text-align:center" data-go="D8"><div class="t-hm ell">${date}</div>${sub ? `<div class="t-lm c2 ell">${sub}</div>` : ''}</div>${iconbtn('chevron_right', { cls: 'on', label: 'Next day' }).replace('class="ms ', 'class="ms flip ')}</div>`;
  return topbar({ nav: '', titleHtml: t, actions: [iconbtn('search', { g: 'M5', label: 'Search' }), iconbtn('settings', { g: 'S1', label: 'Settings' }), iconbtn('more_vert', { label: 'More' })] });
}
function dayhead({ left = '12 h 18 min left', sub = 'today · bedtime 23:00 · 2 done', pct = .3, shift = true, energy = true } = {}) {
  return `<div class="dayhead">${pring(pct, 44, 4, `<span class="t-ls">${Math.round(pct * 100)}%</span>`)}
<div class="grow"><div class="t-tl">${left}</div><div class="t-bm c2 hide200">${sub}</div></div>
${shift ? `<span class="btn tonal sm" data-go="D1">${I('more_time')}Shift</span>` : ''}</div>`;
}
function tray({ items = ['Water plants', 'Reply to Sam'], yesterday = 0, empty = false } = {}) {
  const chips = empty ? `<span class="t-bm c2 grow">Things for today, any time</span>` : items.map(t => U.chip(t, { cls: 'tonal', g: 'T2' })).join('');
  return `<div class="tray"><span class="lbl">Anytime</span>${chips}<span class="chip" data-go="T1">${I('add')}add</span></div>
${yesterday ? `<div class="row px16" style="height:40px;gap:8px" data-go="D5"><span class="ms s20 c2">history</span><span class="t-bm grow">From yesterday (${yesterday})</span><span class="ms c2">expand_more</span></div>` : ''}`;
}
function todayScreen({ items = ITEMS, from = '08:30', to = '16:30', now = '10:42', banners = '', head = {}, trayOpt = {}, bar = {}, after = '', fabHtml = null, nav = true, tlOpt = {}, scrollTop = 0, headHtml = null } = {}) {
  return `${appbar(bar)}${headHtml != null ? headHtml : dayhead(head)}${banners}${tray(trayOpt)}
<div class="scroll"><div style="margin-top:${8 - scrollTop}px">${timeline({ from, to, now, items, ...tlOpt })}</div></div>
${fabHtml != null ? fabHtml : fab({ style: 'bottom:96px' })}${nav ? U.navbar(0) : ''}${after}`;
}
module.exports = { ITEMS, appbar, dayhead, tray, todayScreen };
