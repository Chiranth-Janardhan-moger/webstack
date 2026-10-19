// WebStack PC Companion Service Worker
const CACHE_NAME = 'webstack-shell-v1';
const ASSETS_TO_CACHE = ['/', '/index.html', '/favicon.svg'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE);
    }).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // All API requests bypass service worker cache
  if (url.pathname.startsWith('/api/')) {
    return;
  }

  // Fast network-first with 2s timeout for HTML shell, fallback to cached shell when offline
  if (event.request.mode === 'navigate' || url.pathname === '/' || url.pathname === '/index.html') {
    event.respondWith(
      (async () => {
        try {
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), 2000);
          const response = await fetch(event.request, { signal: controller.signal });
          clearTimeout(timeoutId);
          if (response && response.status === 200) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
          }
          return response;
        } catch (_) {
          const cached = await caches.match('/index.html') || await caches.match('/');
          if (cached) return cached;
          return fetch(event.request);
        }
      })()
    );
  }
});
