#!/usr/bin/env node
// 简单的 API smoke test（仅作快速连通性检查）
(async () => {
  try {
    const base = 'http://localhost:8080';
    const fetch = (...args) => import('node-fetch').then(m => m.default(...args));
    const endpoints = ['/api/transactions?page=0&size=1', '/api/transactions/reconcile'];
    for (const ep of endpoints) {
      const url = base + ep;
      const r = await fetch(url).catch(e => { console.error('fetch error', url, e); return null; });
      console.log(ep, r ? r.status : 'no-response');
      if (r && r.status >= 400) {
        const t = await r.text();
        console.log('body:', t);
      }
    }
  } catch (e) {
    console.error('smoke test failed', e);
    process.exit(1);
  }
})();







