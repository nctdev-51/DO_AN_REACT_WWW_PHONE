'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const dashboard = document.querySelector('.insights');
  if (!dashboard) return;

  const from = dashboard.querySelector('#analytics-from');
  const to = dashboard.querySelector('#analytics-to');
  const updateDateLimit = () => { to.min = from.value; };
  updateDateLimit();
  from.addEventListener('input', updateDateLimit);

  if (!window.Chart) return;
  const formatNumber = new Intl.NumberFormat('vi-VN');
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const colors = ['#368979', '#5575c8', '#c87a3b', '#855cb0', '#4385a1', '#aa5d78',
    '#768b3f', '#687d98', '#ad6470', '#637865', '#96704d'];

  function chartOptions() {
    return {
      responsive: true,
      maintainAspectRatio: false,
      animation: reduceMotion ? false : { duration: 220 },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0b2035',
          padding: 10,
          callbacks: {
            label: item => {
              const value = item.raw;
              const name = item.dataset.label || item.label;
              return name + ': ' + formatNumber.format(value);
            }
          }
        }
      }
    };
  }

  function valueScale(gridColor, textColor) {
    return {
      beginAtZero: true,
      grid: { color: gridColor },
      border: { display: false },
      ticks: { color: textColor, precision: 0, callback: value => formatNumber.format(value) }
    };
  }

  const trend = dashboard.querySelector('#analytics-views-chart');
  if (trend) {
    const dates = trend.dataset.labels.split(',');
    const labels = dates.map(date => date.slice(8, 10) + '/' + date.slice(5, 7));
    const values = trend.dataset.values.split(',').map(Number);
    const switcher = dashboard.querySelector('.insights-chart-switch');
    const buttons = [...switcher.querySelectorAll('button')];
    let chart;

    function renderTrend(type) {
      if (chart) chart.destroy();
      const lineStyle = {
        borderColor: '#73e4c9',
        borderWidth: 2.5,
        backgroundColor: ({ chart: instance }) => {
          if (!instance.chartArea) return 'transparent';
          const fade = instance.ctx.createLinearGradient(0, instance.chartArea.top, 0, instance.chartArea.bottom);
          fade.addColorStop(0, '#73e4c96b');
          fade.addColorStop(1, '#73e4c900');
          return fade;
        },
        fill: true,
        pointRadius: values.length === 1 ? 4 : 0,
        pointHoverRadius: 5,
        pointBackgroundColor: '#73e4c9'
      };
      const options = chartOptions();
      options.interaction = { mode: 'index', intersect: false };
      options.plugins.tooltip.callbacks.title = items => dates[items[0].dataIndex];
      options.scales = {
        x: {
          grid: { display: false },
          border: { display: false },
          ticks: { color: '#b6c9db', maxRotation: 0, maxTicksLimit: 9 }
        },
        y: valueScale('#ffffff19', '#b6c9db')
      };
      chart = new window.Chart(trend, {
        type,
        data: {
          labels,
          datasets: [{
            label: 'Lượt xem', data: values,
            ...(type === 'line' ? lineStyle : { backgroundColor: '#73e4c9', borderRadius: 3, maxBarThickness: 32 })
          }]
        },
        options
      });
      buttons.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.chartType === type)));
    }

    renderTrend('line');
    switcher.hidden = false;
    switcher.addEventListener('click', event => {
      const button = event.target.closest('button[data-chart-type]');
      if (button && button.getAttribute('aria-pressed') === 'false') renderTrend(button.dataset.chartType);
    });
  }

  const topics = dashboard.querySelector('#analytics-topics-chart');
  if (topics) {
    const rows = [...dashboard.querySelectorAll('[data-topic-count]')];
    const labels = rows.map(row => row.querySelector('[data-topic-label]').textContent.trim());
    const values = rows.map((row, index) => {
      row.style.setProperty('--topic-color', colors[index % colors.length]);
      return Number(row.dataset.topicCount);
    });
    topics.parentElement.hidden = false;
    new window.Chart(topics, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values, backgroundColor: labels.map((_, index) => colors[index % colors.length]),
          borderColor: '#fff', borderWidth: 2, hoverOffset: 4
        }]
      },
      options: { ...chartOptions(), cutout: '73%', layout: { padding: 6 } }
    });
  }

  const products = dashboard.querySelector('#analytics-products-chart');
  if (products) {
    const rows = [...dashboard.querySelectorAll('[data-product-views]')].slice(0, 5);
    const labels = rows.map(row => row.querySelector('th').textContent.trim());
    const fields = [
      { key: 'productViews', label: 'Lượt xem', color: colors[0] },
      { key: 'productClicks', label: 'Lượt nhấp', color: colors[1] },
      { key: 'productCarts', label: 'Thêm giỏ', color: colors[2] }
    ];
    products.closest('.insights-product-visual').hidden = false;
    products.parentElement.style.setProperty('--product-plot-height', (rows.length * 46 + 34) + 'px');
    const options = chartOptions();
    options.indexAxis = 'y';
    options.interaction = { mode: 'index', intersect: false, axis: 'y' };
    options.scales = {
      x: valueScale('#e5ecf2', '#53657c'),
      y: {
        grid: { display: false },
        border: { display: false },
        ticks: {
          color: '#384c62',
          callback(value) {
            const label = this.getLabelForValue(value);
            const limit = this.chart.width < 440 ? 17 : 26;
            return label.length > limit ? label.slice(0, limit - 1) + '…' : label;
          }
        }
      }
    };
    new window.Chart(products, {
      type: 'bar',
      data: {
        labels,
        datasets: fields.map(field => ({
          label: field.label, backgroundColor: field.color, borderRadius: 3, maxBarThickness: 9,
          data: rows.map(row => Number(row.dataset[field.key]))
        }))
      },
      options
    });
    dashboard.querySelector('.insights-details').open = false;
  }
});
