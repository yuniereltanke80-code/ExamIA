const CACHE = 'examia-v1';

self.addEventListener('install', () => self.skipWaiting());

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', e => {
  e.respondWith(
    caches.match(e.request).then(r => r || fetch(e.request).then(res => {
      if (res.ok && e.request.method === 'GET') {
        const clone = res.clone();
        caches.open(CACHE).then(cache => cache.put(e.request, clone));
      }
      return res;
    }))
  );
});

// ── NOTIFICACIONES PUSH ──
self.addEventListener('message', e => {
  if (e.data && e.data.type === 'show-result') {
    self.registration.showNotification(e.data.title, {
      body: e.data.body,
      icon: 'icon-192.png',
      data: { whatsappUrl: e.data.whatsappUrl },
      tag: 'examia-result',
      requireInteraction: true
    });
  }
});

self.addEventListener('notificationclick', e => {
  e.notification.close();
  const url = e.notification.data && e.notification.data.whatsappUrl;
  if (url) {
    e.waitUntil(clients.openWindow(url));
  }
});
