'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const money = value => new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(value);
  const resolveColor = window.ptvResolveColor || (() => ({bg: '#8e949b', light: false}));

  document.querySelectorAll('[data-product-card]').forEach(card => {
    const entries = [...card.querySelectorAll('[data-variant]')].map(node => node.dataset);
    if (!entries.length) return;
    let selected = entries.find(v => v.id === card.dataset.selectedVariant) || entries.find(v => Number(v.stock) > 0) || entries[0];
    const controls = card.querySelector('[data-variant-options]');
    const groups = [];

    for (const [key, label] of [['color', 'Màu sắc'], ['storage', 'Dung lượng'], ['ram', 'RAM']]) {
      const values = [...new Set(entries.map(v => v[key]).filter(Boolean))];
      if (!values.length || (key === 'ram' && values.length < 2)) continue;
      const isColor = key === 'color';
      const group = document.createElement('div');
      group.className = isColor ? 'card-color-dots' : 'variant-chips';
      group.setAttribute('role', 'group');
      group.setAttribute('aria-label', label);

      values.forEach(value => {
        const button = document.createElement('button');
        button.type = 'button';
        button.dataset.value = value;
        button.title = value;
        button.setAttribute('aria-label', `${label}: ${value}`);
        if (isColor) {
          button.className = 'color-dot-btn';
          const swatch = resolveColor(value);
          const dot = document.createElement('span');
          dot.className = 'color-dot' + (swatch.light ? ' is-light' : '');
          dot.style.background = swatch.bg;
          button.append(dot);
        } else {
          button.textContent = value;
        }
        button.addEventListener('click', () => {
          const candidates = entries.filter(v => v[key] === value);
          selected = candidates.find(v => v.color === selected.color && v.storage === selected.storage && v.ram === selected.ram)
            || candidates.find(v => isColor ? v.storage === selected.storage : v.color === selected.color) || candidates[0];
          card.dataset.variantPicked = 'true';
          refresh();
        });
        group.append(button);
      });
      controls.append(group);
      groups.push([key, group]);
    }

    function refresh() {
      groups.forEach(([key, group]) => group.querySelectorAll('button').forEach(button => {
        const pressed = button.dataset.value === selected[key];
        button.setAttribute('aria-pressed', String(pressed));
        button.classList.toggle('is-active', pressed);
      }));
      card.querySelector('[data-selected-caption]').textContent = [selected.storage, selected.color, selected.ram].filter(Boolean).join(' · ');
      const img = card.querySelector('.product-card-image img.img-primary') || card.querySelector('.product-card-image img');
      if (img) {
        img.src = selected.image;
        img.alt = `${card.querySelector('h3').textContent.trim()} — ${selected.color}`;
      }
      card.querySelector('[data-card-price]').textContent = money(Number(selected.price));
      const original = Number(card.dataset.originalPrice);
      const discounted = original > Number(selected.price);
      const pct = discounted ? Math.round((1 - Number(selected.price) / original) * 100) : 0;
      const origEl = card.querySelector('[data-card-original]');
      if (origEl) origEl.hidden = !discounted;
      const discEl = card.querySelector('[data-card-discount]');
      if (discEl) discEl.textContent = discounted ? `−${pct}%` : '';
      const badgeSale = card.querySelector('[data-card-badge-sale]');
      if (badgeSale) {
        badgeSale.hidden = !discounted;
        badgeSale.textContent = discounted ? `−${pct}%` : '';
      }
      const stockEl = card.querySelector('[data-card-stock]');
      stockEl.textContent = Number(selected.stock) > 0 ? 'Còn hàng' : 'Tạm hết hàng';
      stockEl.classList.toggle('text-danger', Number(selected.stock) === 0);
      card.querySelectorAll('[data-product-link]').forEach(link => link.href = `${card.dataset.productUrl}?variantId=${selected.id}`);
      const comparison = card.querySelector('[data-card-comparison]');
      if (comparison) { comparison.value = selected.id; comparison.disabled = false; }
    }
    refresh();
  });
});
