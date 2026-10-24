'use strict';

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.account-nav a, .store-nav > a').forEach(link => {
    if (new URL(link.href).pathname === window.location.pathname) link.setAttribute('aria-current', 'page');
  });
  document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });

  const variants = document.getElementById('variant-select');
  if (variants) {
    const price = document.getElementById('product-price');
    const stock = document.getElementById('variant-stock');
    const quantity = document.getElementById('buy-quantity');
    const button = document.getElementById('buy-button');
    const refreshVariant = () => {
      const selected = variants.selectedOptions[0];
      const comparison = document.querySelector('[data-comparison-variant]');
      if (comparison && selected) comparison.value = selected.value;
      const available = selected ? Number(selected.dataset.stock) : 0;
      if (selected) price.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(selected.dataset.price));
      const original = document.getElementById('product-original-price');
      if (original && selected) original.hidden = Number(original.dataset.originalPrice) <= Number(selected.dataset.price);
      stock.textContent = available ? `Còn ${available} sản phẩm` : 'Phiên bản này tạm hết hàng';
      quantity.max = Math.min(99, available);
      quantity.value = available ? Math.max(1, Math.min(Number(quantity.value), available)) : 1;
      button.disabled = available === 0;
    };
    variants.addEventListener('change', refreshVariant);
    refreshVariant();
  }

  const savedAddress = document.getElementById('saved-address');
  if (savedAddress) {
    savedAddress.addEventListener('change', () => {
      const address = savedAddress.selectedOptions[0];
      if (!address.value) return;
      document.getElementById('recipientName').value = address.dataset.recipient;
      document.getElementById('phone').value = address.dataset.phone;
      document.getElementById('shippingAddress').value = address.dataset.address;
      document.querySelector('[data-address-picker]')?.dispatchEvent(new CustomEvent('ptv:set-address',{detail:{provinceCode:address.dataset.provinceCode,wardCode:address.dataset.wardCode}}));
    });
  }
});
