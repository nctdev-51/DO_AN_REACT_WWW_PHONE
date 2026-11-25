'use strict';

const accountInput = document.querySelector('#username');
const form = document.querySelector('[data-account-memory]');
const remember = document.querySelector('[data-remember-account]');
const cookieName = 'ptv_login_name';
const accountCookie = document.cookie.split(';').map(value => value.trim())
  .find(value => value.startsWith(cookieName + '='));

if (accountInput && accountCookie) {
  try {
    accountInput.value ||= decodeURIComponent(accountCookie.slice(cookieName.length + 1));
    if (remember) remember.checked = Boolean(accountInput.value);
  } catch {
    // Ignore an invalid or legacy cookie.
  }
}

if (form && remember && accountInput) {
  const context = form.dataset.context;
  const cookieOptions = '; Path=' + context + '/login; SameSite=Lax'
    + (location.protocol === 'https:' ? '; Secure' : '');
  const clearCookie = () => { document.cookie = cookieName + '=; Max-Age=0' + cookieOptions; };
  remember.addEventListener('change', () => { if (!remember.checked) clearCookie(); });
  form.addEventListener('submit', () => {
    const name = accountInput.value.trim();
    if (remember.checked && name && name.length <= 254) {
      document.cookie = cookieName + '=' + encodeURIComponent(name) + '; Max-Age=2592000' + cookieOptions;
    } else {
      clearCookie();
    }
  });

  if (window.isSecureContext && 'serviceWorker' in navigator) {
    navigator.serviceWorker.register(context + '/login-offline-worker.js', { scope: context + '/login' })
      .catch(() => {});
  }
}

const toggle = document.querySelector('[data-password-toggle]');
if (toggle) {
  const field = document.getElementById(toggle.getAttribute('aria-controls'));
  toggle.hidden = false;
  toggle.addEventListener('click', () => {
    const visible = field.type === 'password';
    field.type = visible ? 'text' : 'password';
    toggle.textContent = visible ? 'Ẩn' : 'Hiện';
    toggle.setAttribute('aria-pressed', String(visible));
  });
}
