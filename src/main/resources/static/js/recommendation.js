/**
 * recommendation.js — Module Recommendation & Vector DB (Vi)
 * Goi GET /api/recommendations/{productId} va khoi tao Swiper carousel.
 */
(function () {
  'use strict';

  const PLACEHOLDER_IMG = '/images/products/placeholder.svg';

  function formatPrice(value) {
    if (value == null || Number.isNaN(Number(value))) {
      return '';
    }
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(value));
  }

  function resolveImageUrl(rawImg, ctx) {
    const chosen = (typeof rawImg === 'string' && rawImg.trim().length > 0)
      ? rawImg.trim()
      : PLACEHOLDER_IMG;
    if (/^https?:\/\//i.test(chosen)) {
      return chosen;
    }
    const normalized = chosen.startsWith('/') ? chosen : '/' + chosen;
    return ctx + normalized;
  }

  function buildCardSlide(item, ctx) {
    const slide = document.createElement('div');
    slide.className = 'swiper-slide';

    const link = document.createElement('a');
    link.className = 'mini-product-card';
    link.href = ctx + '/products/' + encodeURIComponent(String(item.id));
    link.setAttribute('data-product-link', '');
    link.setAttribute('data-product-id', String(item.id));
    link.addEventListener('click', function () {
      if (typeof window.trackEvent === 'function') {
        window.trackEvent('PRODUCT_CLICK', { productId: Number(item.id) });
      }
    });

    const media = document.createElement('div');
    media.className = 'mini-card-media';

    if (item.originalPrice && item.price && Number(item.originalPrice) > Number(item.price)) {
      const pct = Math.round((1 - Number(item.price) / Number(item.originalPrice)) * 100);
      if (pct > 0) {
        const saleBadge = document.createElement('span');
        saleBadge.className = 'mini-card-badge-sale';
        saleBadge.textContent = '\u2212' + pct + '%';
        media.appendChild(saleBadge);
      }
    }

    const img = document.createElement('img');
    img.src = resolveImageUrl(item.image, ctx);
    img.alt = item.name || '';
    img.loading = 'lazy';
    img.addEventListener('error', function () {
      if (img.dataset.fallbackApplied === 'true') {
        return;
      }
      img.dataset.fallbackApplied = 'true';
      img.src = ctx + PLACEHOLDER_IMG;
    });
    media.appendChild(img);

    const info = document.createElement('div');
    info.className = 'mini-card-info';

    const brand = document.createElement('span');
    brand.className = 'mini-card-brand';
    brand.textContent = item.brand || '';

    const title = document.createElement('h3');
    title.className = 'mini-card-title';
    title.textContent = item.name || '';

    info.appendChild(brand);
    info.appendChild(title);

    if (item.chipset) {
      const chip = document.createElement('p');
      chip.className = 'mini-card-chipset';
      chip.textContent = item.chipset;
      info.appendChild(chip);
    }

    const priceRow = document.createElement('div');
    priceRow.className = 'mini-card-price-row';

    const price = document.createElement('strong');
    price.className = 'mini-card-price';
    price.textContent = formatPrice(item.price);
    priceRow.appendChild(price);

    if (item.originalPrice && Number(item.originalPrice) > Number(item.price)) {
      const oldPrice = document.createElement('del');
      oldPrice.className = 'mini-card-old-price';
      oldPrice.textContent = formatPrice(item.originalPrice);
      priceRow.appendChild(oldPrice);
    }

    const stock = document.createElement('span');
    stock.className = 'mini-card-stock' + (item.inStock ? '' : ' is-out');
    stock.textContent = item.inStock ? 'Còn hàng' : 'Tạm hết hàng';

    info.appendChild(priceRow);
    info.appendChild(stock);

    link.appendChild(media);
    link.appendChild(info);
    slide.appendChild(link);
    return slide;
  }

  function showFallback(section, swiperContainer, fallbackGrid) {
    if (swiperContainer) {
      swiperContainer.hidden = true;
    }
    if (fallbackGrid && fallbackGrid.children.length > 0) {
      fallbackGrid.hidden = false;
    } else if (section) {
      section.hidden = true;
    }
  }

  async function loadRecommendations() {
    const section = document.getElementById('similar-products');
    if (!section) {
      return;
    }
    const productId = section.dataset.productId;
    if (!productId) {
      return;
    }

    const ctx = document.querySelector('meta[name="ctx"]')?.content || '';
    const wrapper = document.getElementById('similar-products-wrapper');
    const swiperContainer = section.querySelector('.similar-swiper');
    const fallbackGrid = document.getElementById('similar-fallback-grid');
    if (!wrapper) {
      return;
    }

    try {
      const res = await fetch(ctx + '/api/recommendations/' + encodeURIComponent(productId) + '?limit=6', {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' }
      });
      if (!res.ok) {
        showFallback(section, swiperContainer, fallbackGrid);
        return;
      }

      const data = await res.json();
      const rawItems = Array.isArray(data.recommendations) ? data.recommendations : [];
      const items = rawItems.filter(function (item) {
        return item && item.id != null;
      });
      if (items.length === 0) {
        showFallback(section, swiperContainer, fallbackGrid);
        return;
      }

      wrapper.replaceChildren();
      items.forEach(function (item) {
        wrapper.appendChild(buildCardSlide(item, ctx));
      });
      section.dataset.recommendationSource = data.source || 'fallback';

      if (typeof window.Swiper === 'function' && swiperContainer) {
        new window.Swiper(swiperContainer, {
          slidesPerView: 1.25,
          spaceBetween: 16,
          watchOverflow: true,
          navigation: {
            nextEl: swiperContainer.querySelector('.swiper-button-next'),
            prevEl: swiperContainer.querySelector('.swiper-button-prev')
          },
          breakpoints: {
            576: { slidesPerView: 2 },
            768: { slidesPerView: 3 },
            1024: { slidesPerView: 4 }
          }
        });
      } else {
        showFallback(section, swiperContainer, fallbackGrid);
      }
    } catch (err) {
      console.warn('Could not load recommendations:', err);
      showFallback(section, swiperContainer, fallbackGrid);
    }
  }

  document.addEventListener('DOMContentLoaded', loadRecommendations);
})();
