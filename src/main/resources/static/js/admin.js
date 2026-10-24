'use strict';

document.addEventListener('DOMContentLoaded', () => {
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
