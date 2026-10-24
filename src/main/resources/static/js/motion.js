'use strict';

(() => {
  const preference = window.matchMedia('(prefers-reduced-motion: reduce)');
  if (preference.matches || !('IntersectionObserver' in window)) return;

  const init = () => {
    const main = document.querySelector('.store-main, .admin-main');
    if (!main) return;
    let observer;
    const revealAll = () => {
      observer?.disconnect();
      document.documentElement.classList.remove('motion-ready');
      main.querySelectorAll('[data-motion]').forEach(element => element.dataset.motion = 'complete');
    };
    try {
      const explicit = [...main.querySelectorAll('[data-reveal]')];
      const automatic = [...main.querySelectorAll(
        '.page-heading, .section-heading, .brand-nav, .product-card, .panel, .empty-state, '
        + '.account-nav, .stat-grid > article, .benefits > div, '
        + '.catalog-filters, .catalog-sort, .comparison-suggestions > article'
      )];
      // Animate one level at a time; never transform sticky viewers or nested forms.
      const eligible = [...new Set([...explicit, ...automatic])].filter(element =>
        !element.matches('.product-gallery, .order-summary')
        && !element.closest('[data-motion-skip]')
        && !explicit.some(parent => parent !== element && parent.contains(element))
      );
      const blocks = eligible.filter(element =>
        !eligible.some(parent => parent !== element && parent.contains(element))
      );
      let anchor;
      try { anchor = document.getElementById(decodeURIComponent(location.hash.slice(1))); }
      catch { anchor = null; }
      observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (!entry.isIntersecting) return;
          entry.target.dataset.motion = 'visible';
          observer.unobserve(entry.target);
        });
      }, {threshold: 0, rootMargin: '0px 0px -24px 0px'});

      document.documentElement.classList.add('motion-ready');
      blocks.forEach(element => {
        if (element.contains(anchor) || element.contains(document.activeElement)) return;
        const siblings = [...element.parentElement.children].filter(child => blocks.includes(child));
        element.style.setProperty('--reveal-index', Math.min(siblings.indexOf(element), 3));
        element.dataset.motion = 'pending';
        observer.observe(element);
      });
      let pointerFocus = false;
      document.addEventListener('pointerdown', () => pointerFocus = true, true);
      document.addEventListener('keydown', () => pointerFocus = false, true);
      document.addEventListener('focusin', event => {
        // Moving a button between pointer-down and pointer-up cancels its click.
        // Only keyboard/programmatic focus should finish a running entrance early.
        if (pointerFocus) return;
        let element = event.target.closest('[data-motion]');
        while (element) {
          element.dataset.motion = 'complete'; observer.unobserve(element);
          element = element.parentElement?.closest('[data-motion]');
        }
      });
      preference.addEventListener('change', event => { if (event.matches) revealAll(); });
      window.addEventListener('pageshow', event => { if (event.persisted) revealAll(); });
      window.addEventListener('beforeprint', revealAll);
    } catch {
      // A failed animation must never leave products, forms, or prices invisible.
      revealAll();
    }
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, {once: true});
  else init();
})();
