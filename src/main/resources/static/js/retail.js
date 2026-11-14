'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const moneyFmt = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
  const RECENT_KEY = 'ptv_recent_searches';
  const CATALOG_URL_KEY = 'ptv_last_catalog_url';
  const CATALOG_SCROLL_KEY = 'ptv_last_catalog_scroll';

  // 1. Unified Search Autocomplete + Recent Searches
  const input = document.querySelector('[data-search-input]');
  if (input && input.form) {
    const box = document.querySelector('[data-search-suggestions]');
    const base = new URL(input.form.action).pathname.replace(/\/products$/, '');
    let timer, request, options = [], active = -1;
    box.id = 'search-suggestions';
    box.setAttribute('role', 'listbox');
    box.setAttribute('aria-label', 'Gợi ý sản phẩm');
    input.setAttribute('role', 'combobox');
    input.setAttribute('aria-autocomplete', 'list');
    input.setAttribute('aria-controls', box.id);
    input.setAttribute('aria-expanded', 'false');

    const close = () => {
      box.hidden = true;
      input.setAttribute('aria-expanded', 'false');
      input.removeAttribute('aria-activedescendant');
      active = -1;
    };

    const loadRecent = () => {
      try {
        const parsed = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]');
        return Array.isArray(parsed) ? parsed.filter(v => typeof v === 'string' && v.trim()).slice(0, 5) : [];
      } catch {
        return [];
      }
    };

    const saveRecent = term => {
      const clean = (term || '').trim();
      if (clean.length < 2) return;
      try {
        const updated = [clean, ...loadRecent().filter(v => v.toLowerCase() !== clean.toLowerCase())].slice(0, 5);
        localStorage.setItem(RECENT_KEY, JSON.stringify(updated));
      } catch {
        // Ignore storage quota errors
      }
    };

    const showRecent = () => {
      const recent = loadRecent();
      if (!recent.length || input.value.trim().length >= 2) return;
      box.replaceChildren();
      options = [];
      const title = document.createElement('div');
      title.className = 'search-recent-header';
      title.textContent = 'Tìm kiếm gần đây';
      box.append(title);
      recent.forEach((term, i) => {
        const link = document.createElement('a');
        link.id = `search-option-${i}`;
        link.href = `${base}/products?keyword=${encodeURIComponent(term)}`;
        link.role = 'option';
        link.tabIndex = -1;
        link.setAttribute('aria-selected', 'false');
        const label = document.createElement('span');
        label.textContent = term;
        link.append(label);
        box.append(link);
        options.push(link);
      });
      box.hidden = false;
      input.setAttribute('aria-expanded', 'true');
    };

    const select = index => {
      active = index;
      options.forEach((option, i) => option.setAttribute('aria-selected', String(i === active)));
      if (options[active]) {
        input.setAttribute('aria-activedescendant', options[active].id);
        options[active].scrollIntoView({block: 'nearest'});
      }
    };

    input.form.addEventListener('submit', () => saveRecent(input.value));
    input.addEventListener('focus', showRecent);
    input.addEventListener('input', () => {
      clearTimeout(timer);
      request?.abort();
      const query = input.value.trim();
      if (query.length < 2) {
        close();
        if (!query.length && document.activeElement === input) showRecent();
        return;
      }
      if (query.length > 100) return;
      timer = setTimeout(async () => {
        const current = new AbortController();
        request = current;
        try {
          const response = await fetch(`${base}/search/suggestions?q=${encodeURIComponent(query)}`, {signal: current.signal});
          if (!response.ok) return;
          const items = await response.json();
          if (input.value.trim() !== query || document.activeElement !== input) return;
          box.replaceChildren();
          options = [];
          items.forEach((item, i) => {
            const link = document.createElement('a');
            link.id = `search-option-${i}`;
            link.href = `${base}/products/${Number(item.id)}`;
            link.role = 'option';
            link.tabIndex = -1;
            link.setAttribute('aria-selected', 'false');
            const image = document.createElement('img');
            image.src = `${base}${item.image}`;
            image.alt = '';
            const text = document.createElement('span');
            text.textContent = item.name;
            const price = document.createElement('small');
            price.textContent = `Từ ${moneyFmt.format(item.price)}`;
            text.append(price);
            link.append(image, text);
            box.append(link);
            options.push(link);
          });
          if (!items.length) {
            const p = document.createElement('p');
            p.textContent = 'Chưa có gợi ý. Nhấn Enter để tìm tất cả.';
            box.append(p);
          }
          box.hidden = false;
          input.setAttribute('aria-expanded', 'true');
        } catch (error) {
          if (error.name !== 'AbortError') close();
        }
      }, 180);
    });
    input.addEventListener('keydown', event => {
      if (event.key === 'Escape') {
        close();
        return;
      }
      if (box.hidden) return;
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        event.preventDefault();
        if (options.length) {
          select(active < 0 ? (event.key === 'ArrowDown' ? 0 : options.length - 1) : (active + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length);
        }
      }
      if (event.key === 'Enter' && active >= 0) {
        event.preventDefault();
        window.location.assign(options[active].href);
      }
      if (event.key === 'Tab') close();
    });
    input.addEventListener('blur', () => setTimeout(close, 160));
    document.addEventListener('pointerdown', event => {
      if (!input.form.contains(event.target)) close();
    });

    document.querySelectorAll('[data-mobile-search-trigger]').forEach(btn => {
      btn.addEventListener('click', () => {
        window.scrollTo({top: 0, behavior: 'smooth'});
        setTimeout(() => {
          input.focus();
          showRecent();
        }, 180);
      });
    });
  }

  // 2. Copy Coupon & Tracking Codes
  document.querySelectorAll('[data-copy-code]').forEach(button => {
    button.addEventListener('click', async () => {
      const container = button.closest('article, .promo-offer-box') || button.parentElement;
      const status = container?.querySelector('[data-copy-status]');
      try {
        await navigator.clipboard.writeText(button.dataset.copyCode);
        if (status) status.textContent = `Đã sao chép mã ${button.dataset.copyCode}.`;
      } catch {
        if (status) status.textContent = `Mã: ${button.dataset.copyCode}. Bạn có thể chọn và sao chép mã ở trên.`;
      }
    });
  });

  document.querySelectorAll('[data-copy-tracking]').forEach(button => {
    button.addEventListener('click', async () => {
      const status = button.closest('section')?.querySelector('[data-copy-tracking-status]');
      try {
        await navigator.clipboard.writeText(button.dataset.copyTracking);
        if (status) status.textContent = `Đã sao chép mã vận đơn ${button.dataset.copyTracking}.`;
      } catch {
        if (status) status.textContent = `Mã vận đơn: ${button.dataset.copyTracking}.`;
      }
    });
  });

  // 3. Double-submit Guard (non-AJAX POST forms)
  document.querySelectorAll('form[method="post"]').forEach(form => {
    if (form.hasAttribute('data-compare-form') || form.hasAttribute('data-checkout-address-form')) return;
    form.addEventListener('submit', event => {
      if (event.defaultPrevented) return;
      if (form.dataset.submitting === 'true') {
        event.preventDefault();
        return;
      }
      form.dataset.submitting = 'true';
      form.setAttribute('aria-busy', 'true');
      const submitter = event.submitter;
      setTimeout(() => {
        if (submitter) {
          submitter.disabled = true;
          submitter.dataset.submitLabel = submitter.textContent;
          submitter.textContent = 'Đang xử lý…';
        }
      }, 0);
    });
  });

  // 4. Product Detail Sticky Bar Sync + Delivery Estimator
  const variants = document.getElementById('variant-select');
  const deliveryEstimator = document.querySelector('[data-delivery-estimator]');
  const deliveryProvince = document.querySelector('[data-delivery-province]');
  const deliveryOutput = document.querySelector('[data-delivery-output]');

  async function refreshDeliveryEstimate() {
    if (!deliveryEstimator || !deliveryProvince || !deliveryOutput) return;
    const pCode = deliveryProvince.value;
    if (!pCode) return;
    const variantId = variants?.value || '';
    deliveryOutput.textContent = 'Đang tính thời gian giao dự kiến…';
    try {
      const url = `${deliveryEstimator.dataset.endpoint}?provinceCode=${encodeURIComponent(pCode)}&variantId=${encodeURIComponent(variantId)}`;
      const res = await fetch(url);
      const data = await res.json();
      if (!res.ok) {
        deliveryOutput.textContent = data.error || 'Chưa tính được ước tính giao hàng.';
        return;
      }
      const feeLabel = Number(data.fee) === 0 ? 'Miễn phí vận chuyển tiêu chuẩn' : `Phí chuẩn dự kiến: ${moneyFmt.format(Number(data.fee))}`;
      deliveryOutput.textContent = `Giao tới ${data.provinceName}: dự kiến ${data.windowLabel}. ${feeLabel}.`;
    } catch {
      deliveryOutput.textContent = 'Không thể tải ước tính giao hàng lúc này.';
    }
  }

  if (deliveryProvince) {
    deliveryProvince.addEventListener('change', refreshDeliveryEstimate);
  }

  if (variants) {
    const refresh = () => {
      const selected = variants.selectedOptions[0];
      document.querySelectorAll('[data-buy-now]').forEach(button => {
        button.disabled = !selected || Number(selected.dataset.stock) <= 0;
      });
      const label = document.querySelector('[data-sticky-variant]');
      const price = document.querySelector('[data-sticky-price]');
      const thumb = document.querySelector('[data-sticky-thumb]');
      if (selected && label) label.textContent = [selected.dataset.storage, selected.dataset.color, selected.dataset.ram].filter(Boolean).join(' · ');
      if (selected && price) price.textContent = moneyFmt.format(selected.dataset.price);
      if (selected && thumb && selected.dataset.image) {
        thumb.src = selected.dataset.image;
        thumb.alt = selected.dataset.color || '';
      }
      if (deliveryProvince && deliveryProvince.value) refreshDeliveryEstimate();
    };
    variants.addEventListener('change', refresh);
    refresh();
  }

  // 5. Catalog Filter & Scroll Restoration
  const catalogView = document.querySelector('[data-catalog-view]');
  if (catalogView) {
    try {
      const currentUrl = window.location.pathname + window.location.search;
      const savedUrl = sessionStorage.getItem(CATALOG_URL_KEY);
      const savedScroll = Number(sessionStorage.getItem(CATALOG_SCROLL_KEY) || '0');
      sessionStorage.setItem(CATALOG_URL_KEY, currentUrl);
      if (savedUrl === currentUrl && savedScroll > 0) {
        requestAnimationFrame(() => window.scrollTo(0, savedScroll));
        sessionStorage.removeItem(CATALOG_SCROLL_KEY);
      }
      document.querySelectorAll('[data-product-link]').forEach(link => {
        link.addEventListener('click', () => {
          sessionStorage.setItem(CATALOG_URL_KEY, window.location.pathname + window.location.search);
          sessionStorage.setItem(CATALOG_SCROLL_KEY, String(window.scrollY));
        });
      });
    } catch {
      // Ignore sessionStorage restrictions
    }
  }

  const catalogBack = document.querySelector('[data-catalog-back]');
  if (catalogBack) {
    try {
      const savedUrl = sessionStorage.getItem(CATALOG_URL_KEY);
      if (savedUrl && savedUrl.startsWith('/products')) {
        const ctx = catalogBack.getAttribute('href')?.replace(/\/products.*$/, '') || '';
        catalogBack.href = `${ctx}${savedUrl}`;
      }
    } catch {
      // Fallback to /products
    }
  }

  // 6. Sticky Compare Tray (Non-disruptive AJAX add/remove/clear)
  const tray = document.querySelector('[data-compare-tray]');
  const renderCompareTray = data => {
    if (!tray || !data) return;
    const count = Number(data.count || 0);
    document.querySelectorAll('[data-header-compare-count]').forEach(el => {
      el.textContent = String(count);
    });
    const countNode = tray.querySelector('[data-compare-tray-count]');
    if (countNode) countNode.textContent = String(count);
    const itemsBox = tray.querySelector('[data-compare-tray-items]');
    if (itemsBox) {
      itemsBox.replaceChildren();
      const ctx = tray.dataset.context || '';
      (data.items || []).forEach(item => {
        const card = document.createElement('div');
        card.className = 'compare-tray-item';
        card.dataset.trayVariantId = item.variantId;
        const img = document.createElement('img');
        img.src = `${ctx}${item.image}`;
        img.alt = '';
        const meta = document.createElement('div');
        meta.className = 'compare-tray-meta';
        const title = document.createElement('span');
        title.textContent = item.name;
        const sub = document.createElement('small');
        sub.className = 'muted';
        sub.textContent = item.variantLabel || '';
        meta.append(title, sub);
        const rm = document.createElement('button');
        rm.type = 'button';
        rm.className = 'compare-tray-remove';
        rm.dataset.compareTrayRemove = String(item.variantId);
        rm.setAttribute('aria-label', `Bỏ ${item.name} khỏi so sánh`);
        rm.textContent = '×';
        card.append(img, meta, rm);
        itemsBox.append(card);
      });
    }
    tray.hidden = count === 0;
  };

  document.querySelectorAll('[data-compare-form]').forEach(form => {
    form.addEventListener('submit', async event => {
      const apiAction = form.dataset.apiAction;
      if (!apiAction) return;
      event.preventDefault();
      const btn = form.querySelector('button[type="submit"], button:not([type])');
      const prevText = btn ? btn.textContent : '';
      try {
        const res = await fetch(apiAction, {method: 'POST', body: new FormData(form)});
        const data = await res.json();
        renderCompareTray(data);
        if (!res.ok) {
          window.alert(data.error || 'Không thể thêm vào danh sách so sánh.');
        } else if (btn) {
          btn.textContent = '✓ Đã thêm vào so sánh';
          setTimeout(() => {
            btn.textContent = prevText;
          }, 1400);
        }
      } catch {
        form.submit();
      }
    });
  });

  if (tray) {
    tray.addEventListener('click', async event => {
      const removeBtn = event.target.closest('[data-compare-tray-remove]');
      const clearBtn = event.target.closest('[data-compare-tray-clear]');
      const ctx = tray.dataset.context || '';
      const csrfName = tray.dataset.csrfName;
      const csrfVal = tray.dataset.csrfValue;
      if (!removeBtn && !clearBtn) return;
      const fd = new FormData();
      if (csrfName && csrfVal) fd.append(csrfName, csrfVal);
      if (removeBtn) {
        fd.append('variantId', removeBtn.dataset.compareTrayRemove);
        const res = await fetch(`${ctx}/compare/api/remove`, {method: 'POST', body: fd});
        if (res.ok) renderCompareTray(await res.json());
      } else if (clearBtn) {
        const res = await fetch(`${ctx}/compare/api/clear`, {method: 'POST', body: fd});
        if (res.ok) renderCompareTray(await res.json());
      }
    });
  }

  // 7. Interactive 4-Step Phone Advisor Wizard
  const wizard = document.querySelector('[data-advisor-wizard]');
  if (wizard) {
    const panels = [...wizard.querySelectorAll('[data-advisor-panel]')];
    const tabs = [...wizard.querySelectorAll('[data-advisor-tab]')];
    const showStep = step => {
      panels.forEach(p => {
        p.hidden = p.dataset.advisorPanel !== String(step);
      });
      tabs.forEach(t => {
        const activeTab = t.dataset.advisorTab === String(step);
        t.classList.toggle('is-active', activeTab);
        t.setAttribute('aria-selected', String(activeTab));
      });
    };
    const hasResults = Boolean(new URLSearchParams(window.location.search).get('budget'));
    showStep(hasResults ? '4' : '1');
    tabs.forEach(t => t.addEventListener('click', () => showStep(t.dataset.advisorTab)));
    wizard.querySelectorAll('[data-advisor-next]').forEach(btn => {
      btn.addEventListener('click', () => showStep(btn.dataset.advisorNext));
    });
    wizard.querySelectorAll('[data-advisor-budget]').forEach(btn => {
      btn.addEventListener('click', () => {
        const budgetInput = wizard.querySelector('#budget');
        if (budgetInput) budgetInput.value = btn.dataset.advisorBudget;
        wizard.querySelectorAll('[data-advisor-budget]').forEach(b => b.classList.remove('btn-brand'));
        btn.classList.add('btn-brand');
      });
    });
  }

  // 8. Checkout Inline Address Modal
  const addressModalForm = document.querySelector('[data-checkout-address-form]');
  if (addressModalForm) {
    addressModalForm.addEventListener('submit', async event => {
      event.preventDefault();
      const errBox = addressModalForm.querySelector('[data-modal-address-error]');
      if (errBox) errBox.hidden = true;
      try {
        const res = await fetch(addressModalForm.action, {
          method: 'POST',
          body: new FormData(addressModalForm)
        });
        const data = await res.json();
        if (!res.ok) {
          if (errBox) {
            errBox.textContent = data.error || 'Không thể lưu địa chỉ.';
            errBox.hidden = false;
          }
          return;
        }
        let select = document.getElementById('saved-address');
        if (!select) {
          const heading = document.querySelector('#checkout-form');
          select = document.createElement('select');
          select.id = 'saved-address';
          select.className = 'form-select mb-4';
          heading?.before(select);
        }
        const opt = document.createElement('option');
        opt.value = data.id;
        opt.textContent = data.fullLine + (data.defaultAddress ? ' (Mặc định)' : '');
        opt.dataset.recipient = data.recipientName;
        opt.dataset.phone = data.phone;
        opt.dataset.address = data.line;
        opt.dataset.provinceCode = data.provinceCode || '';
        opt.dataset.wardCode = data.wardCode || '';
        select.append(opt);
        select.value = String(data.id);
        document.getElementById('recipientName').value = data.recipientName;
        document.getElementById('phone').value = data.phone;
        document.getElementById('shippingAddress').value = data.line;
        document.querySelector('[data-address-picker]')?.dispatchEvent(
          new CustomEvent('ptv:set-address', {
            detail: {provinceCode: data.provinceCode || '', wardCode: data.wardCode || ''}
          })
        );
        const modalEl = document.getElementById('newAddressModal');
        if (modalEl && window.bootstrap?.Modal) {
          window.bootstrap.Modal.getInstance(modalEl)?.hide();
        }
        addressModalForm.reset();
      } catch {
        if (errBox) {
          errBox.textContent = 'Lỗi kết nối khi lưu địa chỉ.';
          errBox.hidden = false;
        }
      }
    });
  }

  // 9. Live 15-Minute VNPay Payment Window Countdown
  document.querySelectorAll('[data-countdown-deadline]').forEach(el => {
    const deadlineMs = Date.parse(el.dataset.countdownDeadline);
    if (Number.isNaN(deadlineMs)) return;
    const tick = () => {
      const diff = Math.max(0, Math.floor((deadlineMs - Date.now()) / 1000));
      if (diff <= 0) {
        el.textContent = 'Đã hết hạn thanh toán (15 phút)';
        return false;
      }
      const mins = String(Math.floor(diff / 60)).padStart(2, '0');
      const secs = String(diff % 60).padStart(2, '0');
      el.textContent = `${mins}:${secs}`;
      return true;
    };
    if (tick()) {
      const timerId = setInterval(() => {
        if (!tick()) clearInterval(timerId);
      }, 1000);
    }
  });
});

window.addEventListener('pageshow', () => {
  document.querySelectorAll('form[data-submitting]').forEach(form => {
    delete form.dataset.submitting;
    form.removeAttribute('aria-busy');
  });
  document.querySelectorAll('[data-submit-label]').forEach(button => {
    button.disabled = false;
    button.textContent = button.dataset.submitLabel;
    delete button.dataset.submitLabel;
  });
});
