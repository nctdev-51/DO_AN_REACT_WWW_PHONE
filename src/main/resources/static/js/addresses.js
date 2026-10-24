'use strict';
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-address-picker]').forEach(root => {
    const province = root.querySelector('[data-province-select]'), ward = root.querySelector('[data-ward-select]');
    const enhanced = root.querySelector('[data-address-enhanced]'), fallback = root.querySelector('[data-address-fallback]'), status = root.querySelector('[data-address-status]'), retry = root.querySelector('[data-address-retry]');
    let request, generation = 0;
    const mode = enabled => {enhanced.hidden = !enabled; fallback.hidden = enabled; province.disabled = !enabled; ward.disabled = !enabled || !province.value; fallback.querySelectorAll('input').forEach(input => input.disabled = enabled);};
    const option = (value, text) => {const node = document.createElement('option'); node.value = value; node.textContent = text; return node;};
    async function load(selected = '') {
      const current = ++generation; request?.abort(); request = new AbortController(); retry.hidden = true; mode(true);
      ward.replaceChildren(option('', province.value ? 'Đang tải phường / xã…' : 'Chọn tỉnh trước')); ward.disabled = true;
      if (!province.value) {root.removeAttribute('aria-busy'); status.textContent = 'Chọn tỉnh / thành phố để xem phường / xã.'; return;}
      status.textContent = 'Đang tải danh sách phường / xã…'; root.setAttribute('aria-busy', 'true');
      try {
        const response = await fetch(`${root.dataset.context}/locations/wards?provinceCode=${encodeURIComponent(province.value)}`, {signal: request.signal});
        if (!response.ok) throw new Error('locations'); const wards = await response.json(); if (current !== generation) return;
        if (!wards.length) throw new Error('empty'); ward.replaceChildren(option('', 'Chọn phường / xã'));
        wards.forEach(item => ward.append(option(item.code, item.name))); ward.disabled = false; ward.value = selected;
        status.textContent = 'Địa chỉ gồm tỉnh / thành phố và phường / xã.';
      } catch (error) {
        if (error.name === 'AbortError' || current !== generation) return;
        fallback.querySelector('[name="province"]').value = province.selectedOptions[0]?.textContent || '';
        const hadFocus = root.contains(document.activeElement);
        mode(false); retry.hidden = false; status.textContent = 'Chưa tải được danh sách. Bạn có thể nhập tên tỉnh và phường/xã đầy đủ hoặc thử lại.';
        if (hadFocus) fallback.querySelector('input').focus();
      } finally {if (current === generation) root.removeAttribute('aria-busy');}
    }
    province.addEventListener('change', () => load()); retry.addEventListener('click', () => load());
    root.addEventListener('ptv:set-address', event => {province.value = event.detail.provinceCode || ''; load(event.detail.wardCode || '');});
    load(root.dataset.ward || '');
  });
});
