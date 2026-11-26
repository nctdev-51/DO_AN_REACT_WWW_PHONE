'use strict';

/**
 * Catalog Price Slider with noUiSlider (TV5 - Tú)
 * Đồng bộ hai chiều giữa noUiSlider và 2 ô input minPrice, maxPrice.
 */
(function () {
  function formatVnd(value) {
    if (value === null || value === undefined || isNaN(value)) return '0 ₫';
    return Number(value).toLocaleString('vi-VN') + ' ₫';
  }

  document.addEventListener('DOMContentLoaded', function () {
    const sliderContainer = document.getElementById('priceSlider');
    const minInput = document.getElementById('minPrice');
    const maxInput = document.getElementById('maxPrice');
    const minLabel = document.getElementById('priceSliderMinLabel');
    const maxLabel = document.getElementById('priceSliderMaxLabel');

    if (!sliderContainer || !minInput || !maxInput) {
      return;
    }

    if (typeof noUiSlider === 'undefined') {
      console.warn('noUiSlider library is not loaded.');
      return;
    }

    const SLIDER_MIN = 0;
    const SLIDER_MAX = 50000000;
    const SLIDER_STEP = 500000;

    const rawMin = minInput.value ? Number(minInput.value) : SLIDER_MIN;
    const rawMax = maxInput.value ? Number(maxInput.value) : SLIDER_MAX;

    let initMin = isNaN(rawMin) ? SLIDER_MIN : Math.max(SLIDER_MIN, Math.min(rawMin, SLIDER_MAX));
    let initMax = isNaN(rawMax) ? SLIDER_MAX : Math.max(SLIDER_MIN, Math.min(rawMax, SLIDER_MAX));
    if (initMin > initMax) {
      initMin = SLIDER_MIN;
    }

    noUiSlider.create(sliderContainer, {
      start: [initMin, initMax],
      connect: true,
      step: SLIDER_STEP,
      range: {
        'min': SLIDER_MIN,
        'max': SLIDER_MAX
      },
      format: {
        to: function (val) {
          return Math.round(val);
        },
        from: function (val) {
          return Number(val);
        }
      }
    });

    let isInternalUpdate = false;

    // Cập nhật label và input khi kéo thanh trượt
    sliderContainer.noUiSlider.on('update', function (values, handle) {
      const minVal = Math.round(values[0]);
      const maxVal = Math.round(values[1]);

      if (minLabel) minLabel.textContent = formatVnd(minVal);
      if (maxLabel) maxLabel.textContent = formatVnd(maxVal);

      if (!isInternalUpdate) {
        if (handle === 0) {
          minInput.value = minVal > SLIDER_MIN ? minVal : '';
        } else {
          maxInput.value = maxVal < SLIDER_MAX ? maxVal : '';
        }
      }
    });

    // Đồng bộ khi người dùng tự gõ vào ô input minPrice hoặc maxPrice
    function syncFromInputs() {
      const valMin = minInput.value ? Number(minInput.value) : SLIDER_MIN;
      const valMax = maxInput.value ? Number(maxInput.value) : SLIDER_MAX;

      let safeMin = isNaN(valMin) ? SLIDER_MIN : Math.max(SLIDER_MIN, Math.min(valMin, SLIDER_MAX));
      let safeMax = isNaN(valMax) ? SLIDER_MAX : Math.max(SLIDER_MIN, Math.min(valMax, SLIDER_MAX));

      if (safeMin > safeMax) {
        safeMin = safeMax;
      }

      isInternalUpdate = true;
      sliderContainer.noUiSlider.set([safeMin, safeMax]);
      isInternalUpdate = false;
    }

    minInput.addEventListener('change', syncFromInputs);
    maxInput.addEventListener('change', syncFromInputs);
  });
})();
