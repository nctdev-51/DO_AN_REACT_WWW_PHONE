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
  let syncBatchSelection = () => { };
  if (batch) {
    const rowSelector = 'input[type="checkbox"][form="admin-batch"][name="ids"]';
    const rowsOnPage = () => [...document.querySelectorAll(rowSelector)];
    const selectAll = document.querySelector('[data-batch-all]');
    const count = batch.querySelector('[data-batch-count]');
    const eligibility = batch.querySelector('[data-batch-eligibility]');
    const hiddenIds = batch.querySelector('[data-batch-hidden-ids]');
    const selection = document.querySelector('[data-batch-selection]');
    const items = selection.querySelector('[data-batch-items]');
    const clearSelection = selection.querySelector('[data-batch-clear]');
    const action = batch.elements.namedItem('action');
    const submit = batch.querySelector('button[type="submit"]');
    const destination = batch.elements.namedItem('categoryId');
    const selected = new Map();
    const rowDetails = row => ({
      label: row.dataset.batchLabel || 'Mục #' + row.value,
      version: row.dataset.batchVersion,
      actions: row.dataset.batchActions?.split('|'),
      category: row.dataset.batchCategory
    });

    function isEligible(details) {
      if (!action.value) return true;
      if (details.actions && !details.actions.includes(action.value)) return false;
      return action.value !== 'category' || !destination?.value || details.category !== destination.value;
    }

    const select = (row, checked) => {
      if (!checked) selected.delete(row.value);
      else if (!selected.has(row.value)) {
        const details = rowDetails(row);
        if (isEligible(details)) selected.set(row.value, details);
      }
    };
    rowsOnPage().filter(row => row.checked).forEach(row => select(row, true));

    if (destination) {
      const picker = batch.querySelector('[data-category-picker]');
      const categorySearch = picker.querySelector('input');
      const suggestions = picker.querySelector('datalist');
      const normalize = text => text.trim().toLocaleLowerCase('vi')
        .normalize('NFD').replace(/\p{M}/gu, '').replace(/đ/g, 'd');
      const categories = [...destination.options].filter(option => option.value);
      const occurrences = new Map();
      categories.forEach(option => {
        const key = normalize(option.text);
        occurrences.set(key, (occurrences.get(key) || 0) + 1);
      });
      const choices = categories.map(option => ({
        id: option.value,
        name: occurrences.get(normalize(option.text)) === 1
          ? option.text : option.text + ' (#' + option.value + ')'
      }));
      suggestions.replaceChildren(...choices.map(choice => new Option('', choice.name)));
      categorySearch.value = choices.find(choice => choice.id === destination.value)?.name || '';
      categorySearch.addEventListener('input', () => {
        const typed = normalize(categorySearch.value);
        destination.value = choices.find(choice => normalize(choice.name) === typed)?.id || '';
        syncBatchSelection();
      });
      batch.querySelector('[data-category-fallback]').classList.add('d-none');
      destination.classList.add('d-none');
      picker.classList.remove('d-none');
    }

    function renderSelection() {
      const list = document.createDocumentFragment();
      for (const [id, { label }] of selected) {
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
      const rows = rowsOnPage();
      const current = new Map(rows.map(row => [row.value, rowDetails(row)]));
      const initialCount = selected.size;
      for (const [id, details] of selected) {
        if (!isEligible(current.get(id) || details)) selected.delete(id);
      }
      const visible = rows.filter(row => isEligible(current.get(row.value)));
      const visibleIds = new Set(visible.map(row => row.value));
      rows.forEach(row => {
        row.closest('tr, [data-batch-row]').classList.toggle('d-none', !visibleIds.has(row.value));
        row.disabled = !visibleIds.has(row.value);
        row.checked = selected.has(row.value);
        const version = row.parentElement.querySelector('[data-batch-version-field]');
        if (version) version.value = selected.get(row.value)?.version ?? row.dataset.batchVersion;
      });
      const checkedCount = visible.filter(row => row.checked).length;
      selectAll.disabled = visible.length === 0;
      selectAll.checked = checkedCount > 0 && checkedCount === visible.length;
      selectAll.indeterminate = checkedCount > 0 && checkedCount < visible.length;
      count.textContent = 'Đã chọn ' + selected.size + ' mục';
      const destinationMissing = action.value === 'category' && !destination?.value;
      submit.disabled = selected.size === 0 || action.value === '' || destinationMissing;
      selection.classList.toggle('d-none', selected.size === 0);
      eligibility.hidden = !action.value;
      const excluded = initialCount - selected.size;
      eligibility.textContent = (visible.length
        ? 'Có ' + visible.length + ' mục phù hợp đang hiển thị.'
        : 'Không có mục phù hợp với thao tác này trong kết quả hiện tại.')
        + (destinationMissing ? ' Chọn danh mục đích để tiếp tục.' : '')
        + (excluded ? ' Đã bỏ ' + excluded + ' mục không còn phù hợp.' : '');

      // Checked visible rows submit themselves. Submit only offscreen selections as hidden inputs.
      const hidden = document.createDocumentFragment();
      function appendHidden(name, value) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        input.value = value;
        hidden.append(input);
      }
      for (const [id, { version }] of selected) {
        if (visibleIds.has(id)) continue;
        appendHidden('ids', id);
        if (version !== undefined) appendHidden('version_' + id, version);
      }
      hiddenIds.replaceChildren(hidden);
      if (renderNames || excluded) renderSelection();
      return visible.length;
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
      rowsOnPage().filter(row => !row.disabled).forEach(row => select(row, selectAll.checked));
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
      if (!selected.size || (action.value === 'category' && !destination?.value)
        || (action.selectedOptions[0].hasAttribute('data-destructive')
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
    function searchUrl() {
      const url = new URL(search.action);
      for (const [name, raw] of new FormData(search)) {
        const value = raw.trim();
        if (value) url.searchParams.set(name, value);
      }
      return url;
    }
    let appliedUrl = searchUrl().href;
    let request;
    let timer;

    function scheduleSearch(delay) {
      clearTimeout(timer);
      request?.abort();
      const url = searchUrl();
      clear.classList.toggle('d-none', !url.search);
      if (url.href === appliedUrl) {
        status.textContent = '';
        results.removeAttribute('aria-busy');
        return;
      }
      results.setAttribute('aria-busy', 'true');
      timer = setTimeout(async () => {
        const pending = new AbortController();
        request = pending;
        try {
          const response = await fetch(url, { signal: pending.signal, cache: 'no-store' });
          if (!response.ok) throw new Error('Search failed');
          const page = new DOMParser().parseFromString(await response.text(), 'text/html');
          if (pending.signal.aborted) return;
          const updated = page.getElementById(results.id);
          if (!updated) throw new Error('Search results missing');
          results.replaceChildren(...updated.children);
          appliedUrl = url.href;
          history.replaceState(history.state, '', url);
          const matches = syncBatchSelection(false)
            ?? results.querySelectorAll('input[name="ids"]').length;
          status.textContent = url.search ? matches + ' kết quả' : '';
        } catch (error) {
          if (!pending.signal.aborted) status.textContent = 'Chưa tải được kết quả. Bấm nút lọc để thử lại.';
        } finally {
          if (!pending.signal.aborted) results.removeAttribute('aria-busy');
        }
      }, delay);
    }

    input.addEventListener('input', event => {
      if (!event.isComposing) scheduleSearch(input.value.trim() ? 200 : 0);
    });
    input.addEventListener('compositionend', () => scheduleSearch(200));
    search.addEventListener('change', event => {
      if (event.target !== input) scheduleSearch(0);
    });
    search.addEventListener('submit', event => {
      event.preventDefault();
      scheduleSearch(0);
    });
    clear.addEventListener('click', event => {
      event.preventDefault();
      for (const name of new FormData(search).keys()) search.elements.namedItem(name).value = '';
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
