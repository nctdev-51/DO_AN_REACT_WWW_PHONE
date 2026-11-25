'use strict';

function showAdminImageFallback(image) {
  const fallback = image.dataset.imageFallback;
  if (!fallback) return;
  delete image.dataset.imageFallback;
  image.src = fallback;
}

document.addEventListener('error', event => {
  if (event.target instanceof HTMLImageElement) showAdminImageFallback(event.target);
}, true);

document.addEventListener('DOMContentLoaded', () => {
  const batch = document.querySelector('[data-admin-batch]');
  let syncBatchSelection = () => {};
  if (batch) {
    const rowSelector = 'input[type="checkbox"][form="admin-batch"][name="ids"]';
    const visibleRows = () => [...document.querySelectorAll(rowSelector)];
    const selectAll = document.querySelector('[data-batch-all]');
    const count = batch.querySelector('[data-batch-count]');
    const hiddenIds = batch.querySelector('[data-batch-hidden-ids]');
    const selection = document.querySelector('[data-batch-selection]');
    const items = selection.querySelector('[data-batch-items]');
    const clearSelection = selection.querySelector('[data-batch-clear]');
    const action = batch.elements.namedItem('action');
    const submit = batch.querySelector('button[type="submit"]');
    const selected = new Map();

    const select = (row, checked) => {
      if (checked) selected.set(row.value, row.dataset.batchLabel || 'Mục #' + row.value);
      else selected.delete(row.value);
    };
    visibleRows().filter(row => row.checked).forEach(row => select(row, true));

    function renderSelection() {
      const list = document.createDocumentFragment();
      for (const [id, label] of selected) {
        const item = document.createElement('li');
        const name = document.createElement('span');
        name.textContent = label;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.setAttribute('aria-label', 'Bỏ chọn ' + label);
        remove.dataset.removeSelection = id;
        remove.textContent = '×';
        item.append(name, remove);
        list.append(item);
      }
      items.replaceChildren(list);
    }

    syncBatchSelection = (renderNames = true) => {
      const rows = visibleRows();
      const visibleIds = new Set(rows.map(row => row.value));
      rows.forEach(row => { row.checked = selected.has(row.value); });
      const checkedCount = rows.filter(row => row.checked).length;
      selectAll.disabled = rows.length === 0;
      selectAll.checked = checkedCount > 0 && checkedCount === rows.length;
      selectAll.indeterminate = checkedCount > 0 && checkedCount < rows.length;
      count.textContent = 'Đã chọn ' + selected.size + ' mục';
      submit.disabled = selected.size === 0 || action.value === '';
      selection.classList.toggle('d-none', selected.size === 0);

      // Checked visible rows submit themselves. Submit only offscreen selections as hidden inputs.
      const hidden = document.createDocumentFragment();
      for (const id of selected.keys()) {
        if (visibleIds.has(id)) continue;
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'ids';
        input.value = id;
        hidden.append(input);
      }
      hiddenIds.replaceChildren(hidden);
      if (renderNames) renderSelection();
    };

    function updateAction() {
      batch.querySelectorAll('[data-batch-field]').forEach(field => {
        const visible = field.dataset.batchField === action.value;
        field.hidden = !visible;
        field.querySelectorAll('input, select, textarea').forEach(input => { input.disabled = !visible; });
      });
      syncBatchSelection(false);
    }

    selectAll.addEventListener('change', () => {
      visibleRows().forEach(row => select(row, selectAll.checked));
      syncBatchSelection();
    });
    document.addEventListener('change', event => {
      if (event.target.matches(rowSelector)) {
        select(event.target, event.target.checked);
        syncBatchSelection();
      }
    });
    items.addEventListener('click', event => {
      const button = event.target.closest('[data-remove-selection]');
      if (!button) return;
      selected.delete(button.dataset.removeSelection);
      syncBatchSelection();
      (selected.size ? clearSelection : action).focus();
    });
    clearSelection.addEventListener('click', () => {
      selected.clear();
      syncBatchSelection();
      action.focus();
    });
    action.addEventListener('change', updateAction);
    batch.addEventListener('submit', event => {
      if (!selected.size || (action.selectedOptions[0].hasAttribute('data-destructive')
          && !window.confirm('Xác nhận thực hiện thao tác trên ' + selected.size + ' mục đã chọn?'))) {
        event.preventDefault();
      }
    });
    count.hidden = false;
    renderSelection();
    updateAction();
  }

  const search = document.querySelector('[data-admin-name-search]');
  const results = document.getElementById('admin-name-results');
  if (search && results) {
    const input = search.elements.namedItem('q');
    const clear = search.querySelector('[data-search-clear]');
    const status = search.querySelector('[data-search-status]');
    let appliedQuery = input.value.trim();
    let request;
    let timer;

    function scheduleSearch(delay) {
      clearTimeout(timer);
      request?.abort();
      const query = input.value.trim();
      clear.classList.toggle('d-none', !query);
      if (query === appliedQuery) {
        status.textContent = '';
        results.removeAttribute('aria-busy');
        return;
      }
      results.setAttribute('aria-busy', 'true');
      timer = setTimeout(async () => {
        const pending = new AbortController();
        request = pending;
        const url = new URL(search.action);
        if (query) url.searchParams.set('q', query);
        try {
          const response = await fetch(url, { signal: pending.signal, cache: 'no-store' });
          if (!response.ok) throw new Error('Search failed');
          const page = new DOMParser().parseFromString(await response.text(), 'text/html');
          if (pending.signal.aborted) return;
          const updated = page.getElementById(results.id);
          if (!updated) throw new Error('Search results missing');
          results.replaceChildren(...updated.children);
          appliedQuery = query;
          history.replaceState(history.state, '', url);
          const matches = results.querySelectorAll('input[name="ids"]').length;
          status.textContent = query ? matches + ' kết quả' : '';
          syncBatchSelection(false);
        } catch (error) {
          if (!pending.signal.aborted) status.textContent = 'Chưa tải được kết quả. Bấm Tìm để thử lại.';
        } finally {
          if (!pending.signal.aborted) results.removeAttribute('aria-busy');
        }
      }, delay);
    }

    input.addEventListener('input', event => {
      if (!event.isComposing) scheduleSearch(input.value.trim() ? 200 : 0);
    });
    input.addEventListener('compositionend', () => scheduleSearch(200));
    search.addEventListener('submit', event => {
      event.preventDefault();
      scheduleSearch(0);
    });
    clear.addEventListener('click', event => {
      event.preventDefault();
      input.value = '';
      scheduleSearch(0);
      input.focus();
    });
  }
  document.querySelectorAll('img[data-image-fallback]').forEach(image => {
    if (image.complete && image.naturalWidth === 0) showAdminImageFallback(image);
  });
  const canvas = document.getElementById('revenue-chart');
  if (!canvas || !window.Chart) return;
  if (canvas.dataset.values.split(',').every(value => Number(value) === 0)) {
    canvas.hidden = true;
    const empty = document.createElement('div');
    empty.className = 'chart-empty';
    const title = document.createElement('strong');
    title.textContent = 'Chưa ghi nhận doanh thu trong kỳ';
    const hint = document.createElement('p');
    hint.textContent = 'Doanh thu được tính từ những đơn đã giao.';
    empty.append(title, hint);
    canvas.parentElement.append(empty);
    return;
  }
  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: canvas.dataset.labels.split(','),
      datasets: [{
        label: 'Doanh thu đơn đã giao',
        data: canvas.dataset.values.split(',').map(Number),
        backgroundColor: '#3f7064',
        borderRadius: 7,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: { y: { beginAtZero: true, ticks: { callback: value => new Intl.NumberFormat('vi-VN', { notation: 'compact' }).format(value) } } },
      plugins: { legend: { display: false } },
    },
  });
});
