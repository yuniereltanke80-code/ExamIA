const CACHE = 'examia-v2';

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
  if (e.request.method !== 'GET') return;
  e.respondWith(
    fetch(e.request)
      .then(res => {
        if (res.ok) {
          const clone = res.clone();
          caches.open(CACHE).then(cache => cache.put(e.request, clone));
        }
        return res;
      })
      .catch(() => caches.match(e.request))
  );
});

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
