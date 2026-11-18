'use strict';

(function () {
  const ctx = document.querySelector('meta[name="ctx"]')?.content || '';

  function getProductId(node) {
    const value =
      node.dataset.productId ||
      node.closest('[data-product-card]')?.dataset.productId ||
      node.closest('[data-product-id]')?.dataset.productId;
    return value ? Number(value) : null;
  }

  async function trackEvent(eventType, payload) {
    try {
      await fetch(ctx + '/api/tracking', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        credentials: 'same-origin',
        keepalive: true,
        body: JSON.stringify({
          eventType: eventType,
          pageUrl: window.location.pathname,
          ...payload
        })
      });
    } catch (error) {
      console.warn('Tracking event was not recorded:', error);
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-product-link]').forEach(function (link) {
      link.addEventListener('click', function () {
        trackEvent('PRODUCT_CLICK', {productId: getProductId(link)});
      });
    });

    const buyButton = document.getElementById('buy-button');
    if (buyButton) {
      buyButton.addEventListener('click', function () {
        const productId = getProductId(buyButton);
        trackEvent('ADD_TO_CART', {productId: productId});
      });
    }

    document.querySelectorAll('form').forEach(function (searchForm) {
      const action = searchForm.getAttribute('action') || '';
      const searchInput = searchForm.querySelector('[name="keyword"], [name="q"]');
      if (searchInput && (action.includes('/products') || searchForm.classList.contains('header-search'))) {
        searchForm.addEventListener('submit', function () {
          trackEvent('SEARCH', {keyword: searchInput.value.trim()});
        });
      }
    });
  });

  window.trackEvent = trackEvent;
})();
