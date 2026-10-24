'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const money = value => new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(value);
  document.querySelectorAll('[data-product-card]').forEach(card => {
    const entries = [...card.querySelectorAll('[data-variant]')].map(node => node.dataset);
    if (!entries.length) return;
    let selected = entries.find(v => v.id === card.dataset.selectedVariant) || entries.find(v => Number(v.stock) > 0) || entries[0];
    const controls = card.querySelector('[data-variant-options]');
    const groups = [];
    for (const [key, label] of [['storage', 'Dung lượng'], ['color', 'Màu sắc'], ['ram', 'RAM']]) {
      const values = [...new Set(entries.map(v => v[key]).filter(Boolean))];
      if (!values.length || (key === 'ram' && values.length < 2)) continue;
      const group = document.createElement('div');
      group.className = 'variant-chips'; group.setAttribute('role', 'group'); group.setAttribute('aria-label', label);
      values.forEach(value => {
        const button = document.createElement('button');
        button.type = 'button'; button.textContent = value; button.dataset.value = value;
        button.addEventListener('click', () => {
          const candidates = entries.filter(v => v[key] === value);
          selected = candidates.find(v => v.color === selected.color && v.storage === selected.storage && v.ram === selected.ram)
            || candidates.find(v => key === 'color' ? v.storage === selected.storage : v.color === selected.color) || candidates[0];
          refresh();
        });
        group.append(button);
      });
      controls.append(group); groups.push([key, group]);
    }
    function refresh() {
      groups.forEach(([key, group]) => group.querySelectorAll('button').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.value === selected[key]))));
      card.querySelector('[data-selected-caption]').textContent = `${selected.storage} · ${selected.color}${selected.ram ? " · " + selected.ram : ""}`;
      const img = card.querySelector('.product-card-image img');
      img.src = selected.image;
      img.alt = `${card.querySelector('h3').textContent.trim()} — ${selected.color}`;
      card.querySelector('[data-card-price]').textContent = money(Number(selected.price));
      const original = Number(card.dataset.originalPrice);
      const discounted = original > Number(selected.price);
      card.querySelector('[data-card-original]').hidden = !discounted;
      card.querySelector('[data-card-discount]').textContent = discounted ? `−${Math.round((1 - Number(selected.price) / original) * 100)}%` : '';
      card.querySelector('[data-card-stock]').textContent = Number(selected.stock) > 0 ? 'Còn hàng' : 'Tạm hết hàng';
      card.querySelector('[data-card-stock]').classList.toggle('text-danger', Number(selected.stock) === 0);
      card.querySelectorAll('[data-product-link]').forEach(link => link.href = `${card.dataset.productUrl}?variantId=${selected.id}`);
      const comparison = card.querySelector('[data-card-comparison]');
      comparison.value = selected.id; comparison.disabled = false;
    }
    refresh();
  });
});
