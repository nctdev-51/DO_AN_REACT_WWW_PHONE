'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.getElementById('compare-differences');
  const table = document.getElementById('comparison-table');
  if (toggle && table) {
    toggle.addEventListener('change', () => {
      table.querySelectorAll('tbody tr').forEach(row => {
        row.hidden = toggle.checked && row.dataset.different !== 'true';
      });
    });
  }
});
