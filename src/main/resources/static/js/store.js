'use strict';

window.ptvResolveColor = function ptvResolveColor(name) {
  const raw = (name || '').trim();
  const key = raw.toLowerCase();
  const exact = {
    'đen tuyền': {bg: '#111113', light: false},
    'đen không gian': {bg: '#1d1e22', light: false},
    'đen - chỉ có tại cps': {bg: '#1c1c1e', light: false},
    'đen': {bg: '#222226', light: false},
    'đen tím': {bg: 'linear-gradient(135deg, #272233 0%, #4a3d63 100%)', light: false},
    'titan đen': {bg: 'linear-gradient(135deg, #2b2b2f 0%, #48484f 100%)', light: false},
    'titan trắng': {bg: 'linear-gradient(135deg, #f7f6f2 0%, #e4e2db 100%)', light: true},
    'titan tự nhiên': {bg: 'linear-gradient(135deg, #d0ccc4 0%, #a8a399 100%)', light: true},
    'titan sa mạc': {bg: 'linear-gradient(135deg, #d7bfa9 0%, #b59479 100%)', light: false},
    'trắng': {bg: '#f7f7f5', light: true},
    'trắng mây': {bg: 'linear-gradient(135deg, #f3f6fa 0%, #dfe7f0 100%)', light: true},
    'bạc': {bg: 'linear-gradient(135deg, #e5e7eb 0%, #c6c9cf 100%)', light: true},
    'xám bạc': {bg: 'linear-gradient(135deg, #c9cdd3 0%, #9fa5ad 100%)', light: false},
    'xám': {bg: '#757980', light: false},
    'cam vũ trụ': {bg: 'linear-gradient(135deg, #ea7845 0%, #c85224 100%)', light: false},
    'hồng': {bg: '#f1c4d3', light: true},
    'tím': {bg: '#8c73b8', light: false},
    'vàng': {bg: 'linear-gradient(135deg, #efe09b 0%, #d4ba5b 100%)', light: true},
    'nâu': {bg: '#6e5243', light: false},
    'nâu đồng': {bg: 'linear-gradient(135deg, #9b7356 0%, #745139 100%)', light: false},
    'xanh navy': {bg: '#1d2f4e', light: false},
    'xanh đậm': {bg: '#1b3d46', light: false},
    'xanh dương': {bg: '#2b63b0', light: false},
    'xanh da trời': {bg: '#7ab6e8', light: false},
    'xanh bóng': {bg: 'linear-gradient(135deg, #4b8c9a 0%, #2b6470 100%)', light: false},
    'xanh lá': {bg: '#4c8266', light: false},
    'xanh': {bg: '#467b9b', light: false}
  };
  if (exact[key]) return exact[key];
  if (key.includes('trắng')) return {bg: '#f5f5f3', light: true};
  if (key.includes('bạc')) return {bg: '#d2d5da', light: true};
  if (key.includes('đen')) return {bg: '#222226', light: false};
  if (key.includes('xám') || key.includes('titan')) return {bg: '#7c8087', light: false};
  if (key.includes('xanh lá') || key.includes('rêu')) return {bg: '#4c8266', light: false};
  if (key.includes('xanh')) return {bg: '#346fa8', light: false};
  if (key.includes('tím')) return {bg: '#8c73b8', light: false};
  if (key.includes('hồng')) return {bg: '#f1c4d3', light: true};
  if (key.includes('đỏ')) return {bg: '#b92d2d', light: false};
  if (key.includes('cam')) return {bg: '#d96b38', light: false};
  if (key.includes('vàng')) return {bg: '#e0c66c', light: true};
  if (key.includes('nâu') || key.includes('đồng')) return {bg: '#7f5e48', light: false};
  return {bg: '#8e949b', light: false};
};

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.account-nav a, .store-nav > a, .mobile-bottom-nav a').forEach(link => {
    const pathname = new URL(link.href, window.location.origin).pathname;
    if (pathname === window.location.pathname) link.setAttribute('aria-current', 'page');
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

  const filterDetails = document.querySelector('.catalog-sidebar .catalog-filters');
  if (filterDetails) {
    const syncFilterSidebar = () => {
      if (window.matchMedia('(min-width: 992px)').matches) filterDetails.setAttribute('open', '');
    };
    syncFilterSidebar();
    window.addEventListener('resize', syncFilterSidebar);
  }
  document.querySelectorAll('[data-price-preset]').forEach(button => {
    button.addEventListener('click', () => {
      const form = button.closest('form');
      if (!form) return;
      const minInput = form.querySelector('#minPrice');
      const maxInput = form.querySelector('#maxPrice');
      if (minInput) minInput.value = button.dataset.min || '';
      if (maxInput) maxInput.value = button.dataset.max || '';
      form.submit();
    });
  });
});
