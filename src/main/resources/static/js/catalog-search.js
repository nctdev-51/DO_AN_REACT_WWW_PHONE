'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const input = document.querySelector('[data-search-input]');
  const panel = document.querySelector('[data-search-suggestions]');
  if (!input || !panel) return;

  let timer;
  let activeRequest;
  const contextPath = new URL(input.form.action, window.location.origin).pathname.replace(/\/products$/, '');

  const hide = () => {
    panel.hidden = true;
    panel.replaceChildren();
  };

  const show = suggestions => {
    panel.replaceChildren();
    if (!suggestions.length) return hide();
    suggestions.forEach(name => {
      const option = document.createElement('button');
      option.type = 'button';
      option.className = 'search-suggestion';
      option.textContent = name;
      option.addEventListener('click', () => {
        input.value = name;
        hide();
        input.form.requestSubmit();
      });
      panel.append(option);
    });
    panel.hidden = false;
  };

  input.addEventListener('input', () => {
    window.clearTimeout(timer);
    const query = input.value.trim();
    if (query.length < 2) return hide();
    timer = window.setTimeout(async () => {
      activeRequest?.abort();
      activeRequest = new AbortController();
      try {
        const response = await fetch(
            `${contextPath}/products/search/suggestions?q=${encodeURIComponent(query)}`,
            {signal: activeRequest.signal, headers: {Accept: 'application/json'}});
        show(response.ok ? await response.json() : []);
      } catch (error) {
        if (error.name !== 'AbortError') hide();
      }
    }, 250);
  });
  input.addEventListener('keydown', event => {
    if (event.key === 'Escape') hide();
  });
  document.addEventListener('click', event => {
    if (!panel.contains(event.target) && event.target !== input) hide();
  });
});
