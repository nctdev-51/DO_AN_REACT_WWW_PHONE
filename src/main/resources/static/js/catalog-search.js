/**
 * catalog-search.js — Live search autocomplete for PTV Mobile catalog.
 *
 * Calls GET /products/search/suggestions?q=<keyword> (ProductController, line 141)
 * and renders a dropdown below the keyword input on list.jsp.
 *
 * Features:
 *  - 250 ms debounce to avoid flooding the server
 *  - AbortController to cancel in-flight requests on each new keystroke
 *  - textContent (never innerHTML) to prevent XSS
 *  - encodeURIComponent for safe URL encoding
 *  - Keyboard navigation (↑ ↓ Enter Escape)
 *  - Clicking a suggestion submits the filter form immediately
 *  - Closes on outside click
 */
'use strict';

(function () {
  const DEBOUNCE_MS = 250;
  const MIN_LENGTH = 2;

  document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('keyword');
    if (!input) return; // not on catalog page

    const form = input.closest('form');
    if (!form) return;

    // ── Build dropdown container ──────────────────────────────────────────────
    const dropdown = document.createElement('ul');
    dropdown.className = 'search-suggestions-dropdown';
    dropdown.setAttribute('role', 'listbox');
    dropdown.setAttribute('aria-label', 'Gợi ý tìm kiếm');
    dropdown.hidden = true;

    // Insert right after input so CSS can position it absolutely
    input.parentNode.style.position = 'relative';
    input.insertAdjacentElement('afterend', dropdown);

    // ── State ─────────────────────────────────────────────────────────────────
    let controller = null;   // AbortController for the pending fetch
    let debounce = null;     // setTimeout handle
    let activeIndex = -1;    // keyboard-highlighted item index

    // ── Helpers ───────────────────────────────────────────────────────────────
    const contextPath = document.documentElement.dataset.contextPath || '';

    function closeDrop() {
      dropdown.hidden = true;
      dropdown.innerHTML = '';
      activeIndex = -1;
      input.removeAttribute('aria-activedescendant');
    }

    function highlight(index) {
      const items = dropdown.querySelectorAll('li');
      items.forEach((li, i) => li.classList.toggle('is-highlighted', i === index));
      if (index >= 0 && index < items.length) {
        input.setAttribute('aria-activedescendant', items[index].id);
      } else {
        input.removeAttribute('aria-activedescendant');
      }
    }

    function pickSuggestion(name) {
      input.value = name;
      closeDrop();
      form.submit();
    }

    // ── Fetch suggestions ─────────────────────────────────────────────────────
    async function fetchSuggestions(q) {
      if (controller) controller.abort();
      controller = new AbortController();

      try {
        const url = contextPath + '/products/search/suggestions?q=' + encodeURIComponent(q);
        const res = await fetch(url, { signal: controller.signal });
        if (!res.ok) return;
        const names = await res.json();

        dropdown.innerHTML = '';
        activeIndex = -1;

        if (!names.length) { dropdown.hidden = true; return; }

        names.forEach((name, i) => {
          const li = document.createElement('li');
          li.id = 'suggestion-' + i;
          li.className = 'search-suggestion-item';
          li.setAttribute('role', 'option');
          li.textContent = name; // ← textContent, NOT innerHTML — XSS-safe

          li.addEventListener('mousedown', (e) => {
            e.preventDefault(); // prevent blur before click registers
            pickSuggestion(name);
          });
          li.addEventListener('mousemove', () => {
            activeIndex = i;
            highlight(activeIndex);
          });

          dropdown.append(li);
        });

        dropdown.hidden = false;
      } catch (err) {
        if (err.name !== 'AbortError') console.warn('[catalog-search] fetch error:', err);
      }
    }

    // ── Input events ──────────────────────────────────────────────────────────
    input.setAttribute('autocomplete', 'off');
    input.setAttribute('aria-autocomplete', 'list');
    input.setAttribute('aria-haspopup', 'listbox');

    input.addEventListener('input', () => {
      clearTimeout(debounce);
      const q = input.value.trim();
      if (q.length < MIN_LENGTH) { closeDrop(); return; }
      debounce = setTimeout(() => fetchSuggestions(q), DEBOUNCE_MS);
    });

    input.addEventListener('keydown', (e) => {
      const items = dropdown.querySelectorAll('li');
      if (dropdown.hidden || !items.length) return;

      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault();
          activeIndex = Math.min(activeIndex + 1, items.length - 1);
          highlight(activeIndex);
          break;
        case 'ArrowUp':
          e.preventDefault();
          activeIndex = Math.max(activeIndex - 1, -1);
          highlight(activeIndex);
          if (activeIndex === -1) input.removeAttribute('aria-activedescendant');
          break;
        case 'Enter':
          if (activeIndex >= 0 && activeIndex < items.length) {
            e.preventDefault();
            pickSuggestion(items[activeIndex].textContent);
          }
          break;
        case 'Escape':
          closeDrop();
          break;
      }
    });

    input.addEventListener('blur', () => {
      // Delay so mousedown on suggestion fires first
      setTimeout(closeDrop, 150);
    });

    // ── Close on outside click ────────────────────────────────────────────────
    document.addEventListener('click', (e) => {
      if (!input.contains(e.target) && !dropdown.contains(e.target)) {
        closeDrop();
      }
    });
  });
})();
