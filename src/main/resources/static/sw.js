const CACHE_STATIC = 'ptv-static-v1';
const CACHE_PAGES = 'ptv-pages-v1';

const PRECACHE_URLS = [
  '/vendor/bootstrap/bootstrap.min.css',
  '/vendor/bootstrap/bootstrap.bundle.min.js',
  '/css/store.css',
  '/css/retail.css',
  '/js/store.js',
  '/js/offline-db.js',
  '/js/offline-sync.js',
  '/images/offline-placeholder.svg',
  '/offline'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_STATIC).then((cache) =>
      Promise.allSettled(
        PRECACHE_URLS.map((url) =>
          cache.add(url).catch(() => null)
        )
      )
    ).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  const allowed = new Set([CACHE_STATIC, CACHE_PAGES]);
  event.waitUntil(
    caches.keys()
      .then((keys) =>
        Promise.all(
          keys.filter((key) => !allowed.has(key)).map((key) => caches.delete(key))
        )
      )
      .then(() => self.clients.claim())
  );
});

function isStaticAsset(pathname) {
  return (
    pathname.startsWith('/css/') ||
    pathname.startsWith('/js/') ||
    pathname.startsWith('/vendor/') ||
    pathname.startsWith('/uploads/') ||
    pathname.startsWith('/images/')
  );
}

function isCacheablePage(pathname) {
  return pathname === '/' || pathname === '/products' || pathname.startsWith('/products/');
}

self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;

  if (isStaticAsset(url.pathname)) {
    event.respondWith(
      caches.match(request).then((cached) => {
        if (cached) return cached;
        return fetch(request)
          .then((response) => {
            if (response && response.ok) {
              const copy = response.clone();
              caches.open(CACHE_STATIC).then((cache) => cache.put(request, copy));
            }
            return response;
          })
          .catch(() => {
            if (url.pathname.startsWith('/images/') || url.pathname.startsWith('/uploads/')) {
              return caches.match('/images/offline-placeholder.svg');
            }
            return new Response('', { status: 503, statusText: 'Offline' });
          });
      })
    );
    return;
  }

  if (request.mode === 'navigate' || isCacheablePage(url.pathname)) {
    event.respondWith(
      fetch(request)
        .then((response) => {
          if (response && response.ok && isCacheablePage(url.pathname)) {
            const copy = response.clone();
            caches.open(CACHE_PAGES).then((cache) => cache.put(request, copy));
          }
          return response;
        })
        .catch(() =>
          caches.match(request).then((cached) => cached || caches.match('/offline'))
        )
    );
  }
});
