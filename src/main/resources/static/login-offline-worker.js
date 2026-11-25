'use strict';

const loginPath = new URL('login', self.location.href).pathname;
const fallback = new URL('login-offline.html', self.location.href).href;
const accountScript = new URL('js/login-account.js', self.location.href).href;
const cacheName = 'ptv-login-offline-v1:' + loginPath;

self.addEventListener('install', event => {
  event.waitUntil(caches.open(cacheName).then(cache => cache.addAll([fallback, accountScript])));
});

self.addEventListener('activate', event => { event.waitUntil(self.clients.claim()); });

self.addEventListener('fetch', event => {
  const request = event.request;
  if (request.method !== 'GET') return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;
  if (request.mode === 'navigate' && url.pathname === loginPath) {
    event.respondWith(fetch(request).catch(async () => {
      const cache = await caches.open(cacheName);
      return (await cache.match(fallback)) || Response.error();
    }));
  } else if (url.href === accountScript) {
    event.respondWith(fetch(request).catch(async () => {
      const cache = await caches.open(cacheName);
      return (await cache.match(accountScript)) || Response.error();
    }));
  }
});
