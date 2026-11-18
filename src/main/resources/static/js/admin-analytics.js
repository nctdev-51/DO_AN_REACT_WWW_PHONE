'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const canvas = document.getElementById('analytics-views-chart');
  if (!canvas || !window.Chart) return;

  const labels = canvas.dataset.labels ? canvas.dataset.labels.split(',') : [];
  const values = canvas.dataset.values ? canvas.dataset.values.split(',').map(Number) : [];
  if (!labels.length || !values.length) {
    canvas.hidden = true;
    const empty = document.createElement('p');
    empty.className = 'chart-empty';
    empty.textContent = 'Chưa có lượt xem trong khoảng thời gian này.';
    canvas.parentElement.append(empty);
    return;
  }

  new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Lượt xem',
        data: values,
        borderColor: '#3f7064',
        backgroundColor: 'rgba(63, 112, 100, .14)',
        fill: true,
        tension: .25,
        pointRadius: 3,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0,
            callback: value => new Intl.NumberFormat('vi-VN').format(value),
          },
        },
      },
      plugins: {
        legend: {display: false},
        tooltip: {
          callbacks: {
            label: context => `${new Intl.NumberFormat('vi-VN').format(context.parsed.y)} lượt xem`,
          },
        },
      },
    },
  });
});
