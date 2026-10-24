'use strict';
document.addEventListener('DOMContentLoaded', () => {
  const input = document.querySelector('[data-search-input]');
  if (input) {
    const box = document.querySelector('[data-search-suggestions]');
    const base = new URL(input.form.action).pathname.replace(/\/products$/, '');
    let timer, request, options = [], active = -1;
    box.id = 'search-suggestions'; box.setAttribute('role', 'listbox'); box.setAttribute('aria-label', 'Gợi ý sản phẩm');
    input.setAttribute('role', 'combobox'); input.setAttribute('aria-autocomplete', 'list'); input.setAttribute('aria-controls', box.id); input.setAttribute('aria-expanded', 'false');
    const close = () => {box.hidden = true; input.setAttribute('aria-expanded', 'false'); input.removeAttribute('aria-activedescendant'); active = -1;};
    const select = index => {
      active = index; options.forEach((option, i) => option.setAttribute('aria-selected', String(i === active)));
      if (options[active]) {input.setAttribute('aria-activedescendant', options[active].id); options[active].scrollIntoView({block: 'nearest'});}
    };
    input.addEventListener('input', () => {
      clearTimeout(timer); request?.abort(); close();
      const query = input.value.trim(); if (query.length < 2 || query.length > 100) return;
      timer = setTimeout(async () => {
        const current = new AbortController(); request = current;
        try {
          const response = await fetch(`${base}/search/suggestions?q=${encodeURIComponent(query)}`, {signal: current.signal});
          if (!response.ok) return;
          const items = await response.json(); if (input.value.trim() !== query || document.activeElement !== input) return;
          box.replaceChildren(); options = [];
          items.forEach((item, i) => {
            const link = document.createElement('a'); link.id = `search-option-${i}`; link.href = `${base}/products/${Number(item.id)}`; link.role = 'option'; link.tabIndex = -1; link.setAttribute('aria-selected', 'false');
            const image = document.createElement('img'); image.src = `${base}${item.image}`; image.alt = '';
            const text = document.createElement('span'); text.textContent = item.name;
            const price = document.createElement('small'); price.textContent = `Từ ${new Intl.NumberFormat('vi-VN', {style:'currency',currency:'VND'}).format(item.price)}`;
            text.append(price); link.append(image, text); box.append(link); options.push(link);
          });
          if (!items.length) {const p = document.createElement('p'); p.textContent = 'Chưa có gợi ý. Nhấn Enter để tìm tất cả.'; box.append(p);}
          box.hidden = false; input.setAttribute('aria-expanded', 'true');
        } catch (error) {if (error.name !== 'AbortError') close();}
      }, 180);
    });
    input.addEventListener('keydown', event => {
      if (event.key === 'Escape') {close(); return;}
      if (box.hidden) return;
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {event.preventDefault(); if (options.length) select(active < 0 ? (event.key === 'ArrowDown' ? 0 : options.length - 1) : (active + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length);}
      if (event.key === 'Enter' && active >= 0) {event.preventDefault(); window.location.assign(options[active].href);}
      if (event.key === 'Tab') close();
    });
    input.addEventListener('blur', () => setTimeout(close, 160));
    document.addEventListener('pointerdown', event => {if (!input.form.contains(event.target)) close();});
  }
  document.querySelectorAll('[data-copy-code]').forEach(button => button.addEventListener('click', async () => {
    const status = button.closest('article').querySelector('[data-copy-status]');
    try {await navigator.clipboard.writeText(button.dataset.copyCode); status.textContent = 'Đã sao chép mã.';}
    catch {status.textContent = `Mã: ${button.dataset.copyCode}. Bạn có thể chọn và sao chép mã ở trên.`;}
  }));
  document.querySelectorAll('form[method="post"]').forEach(form => form.addEventListener('submit', event => {
    if (event.defaultPrevented) return;
    if (form.dataset.submitting === 'true') {event.preventDefault(); return;}
    form.dataset.submitting = 'true'; form.setAttribute('aria-busy', 'true');
    const submitter = event.submitter;
    // Keep the clicked button's name/value in the native form payload.
    setTimeout(() => {if (submitter) {submitter.disabled = true; submitter.dataset.submitLabel = submitter.textContent; submitter.textContent = 'Đang xử lý…';}}, 0);
  }));
  const variants = document.getElementById('variant-select');
  if (variants) {
    const refresh = () => {
      const selected = variants.selectedOptions[0];
      document.querySelectorAll('[data-buy-now]').forEach(button => button.disabled = !selected || Number(selected.dataset.stock) <= 0);
      const label = document.querySelector('[data-sticky-variant]'), price = document.querySelector('[data-sticky-price]');
      if (selected && label) label.textContent = `${selected.dataset.storage} · ${selected.dataset.color} · ${selected.dataset.ram}`;
      if (selected && price) price.textContent = new Intl.NumberFormat('vi-VN', {style:'currency',currency:'VND'}).format(selected.dataset.price);
    };
    variants.addEventListener('change', refresh); refresh();
  }
});
window.addEventListener('pageshow', () => {
  document.querySelectorAll('form[data-submitting]').forEach(form => {delete form.dataset.submitting; form.removeAttribute('aria-busy');});
  document.querySelectorAll('[data-submit-label]').forEach(button => {button.disabled = false; button.textContent = button.dataset.submitLabel; delete button.dataset.submitLabel;});
});
