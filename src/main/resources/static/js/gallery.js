'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const root = document.querySelector('[data-gallery]');
  const select = document.getElementById('variant-select');
  if (!root || !select) return;
  const find = name => root.querySelector(`[data-${name}]`);
  const image = find('gallery-image');
  const large = find('gallery-large');
  const dialog = find('gallery-dialog');
  const spinStage = find('spin-stage');
  const spinImage = find('spin-image');
  const slider = find('spin-range');
  const photoTab = find('gallery-photos');
  const spinTab = find('gallery-spin');
  const playButton = find('spin-play');
  const path = url => root.dataset.context + url;
  let gallery = {photos: [], frames: []}, index = 0, frame = 0, timer, request, loadedColor;
  let generation = 0, framesPreloaded = false;

  function showPhoto(next) {
    if (!gallery.photos.length) return;
    index = (next + gallery.photos.length) % gallery.photos.length;
    const photo = gallery.photos[index];
    image.src = path(photo.url); image.alt = photo.caption;
    large.src = image.src; large.alt = photo.caption;
    find('gallery-caption').textContent = photo.caption;
    find('gallery-counter').textContent = `${index + 1} / ${gallery.photos.length}`;
    find('gallery-large-caption').textContent = `${index + 1} / ${gallery.photos.length} · ${photo.caption}`;
    find('gallery-thumbs').querySelectorAll('button').forEach((button, n) => button.setAttribute('aria-pressed', String(n === index)));
  }
  function stop() {
    clearInterval(timer); timer = undefined;
    playButton.textContent = 'Tự xoay'; playButton.setAttribute('aria-pressed', 'false');
  }
  function showFrame(next) {
    if (!gallery.frames.length) return;
    frame = (next + gallery.frames.length) % gallery.frames.length;
    spinImage.src = path(gallery.frames[frame]); slider.value = frame;
  }
  function showMode(spin) {
    stop();
    find('photo-panel').hidden = spin; find('spin-panel').hidden = !spin;
    photoTab.setAttribute('aria-pressed', String(!spin)); spinTab.setAttribute('aria-pressed', String(spin));
    if (!spin) {spinStage.querySelector('iframe')?.remove(); return;}
    if (gallery.frames.length) {
      if (!framesPreloaded) gallery.frames.forEach(url => {const preload = new Image(); preload.src = path(url);});
      framesPreloaded = true; return;
    }
    if (spinStage.querySelector('iframe')) return;
    // The external 3D viewer is contacted only after the visitor chooses this mode.
    if (gallery.modelUrl && /^https:\/\/sketchfab\.com\/models\/[a-f0-9]{32}\/embed$/.test(gallery.modelUrl)) {
      const iframe = document.createElement('iframe');
      iframe.title = 'Xem và xoay mô hình 3D sản phẩm'; iframe.src = gallery.modelUrl + '?autostart=1';
      iframe.allow = 'autoplay; fullscreen; xr-spatial-tracking'; iframe.allowFullscreen = true;
      spinStage.append(iframe);
    }
  }
  root.querySelectorAll('[data-gallery-prev]').forEach(button => button.addEventListener('click', () => showPhoto(index - 1)));
  root.querySelectorAll('[data-gallery-next]').forEach(button => button.addEventListener('click', () => showPhoto(index + 1)));
  find('gallery-zoom').addEventListener('click', () => { if (gallery.photos.length) dialog.showModal(); });
  find('gallery-close').addEventListener('click', () => dialog.close());
  dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); });
  dialog.addEventListener('keydown', event => {
    if (event.key === 'ArrowLeft') {event.preventDefault(); showPhoto(index - 1);}
    if (event.key === 'ArrowRight') {event.preventDefault(); showPhoto(index + 1);}
  });
  photoTab.addEventListener('click', () => showMode(false)); spinTab.addEventListener('click', () => showMode(true));
  slider.addEventListener('input', () => {stop(); showFrame(Number(slider.value));});
  spinStage.addEventListener('keydown', event => {
    if (gallery.frames.length && ['ArrowLeft', 'ArrowRight'].includes(event.key)) {
      event.preventDefault(); stop(); showFrame(frame + (event.key === 'ArrowRight' ? 1 : -1));
    }
  });
  let dragX;
  spinStage.addEventListener('pointerdown', event => {
    if (!gallery.frames.length) return;
    stop(); dragX = event.clientX; spinStage.setPointerCapture(event.pointerId);
  });
  spinStage.addEventListener('pointermove', event => {
    if (dragX === undefined) return;
    const delta = Math.trunc((event.clientX - dragX) / 7);
    if (delta) {showFrame(frame - delta); dragX = event.clientX;}
  });
  for (const type of ['pointerup', 'pointercancel', 'lostpointercapture']) spinStage.addEventListener(type, () => dragX = undefined);
  playButton.addEventListener('click', () => {
    if (timer) return stop();
    if (!gallery.frames.length) return;
    timer = setInterval(() => showFrame(frame + 1), 120);
    playButton.textContent = 'Dừng xoay'; playButton.setAttribute('aria-pressed', 'true');
  });
  window.addEventListener('pagehide', stop);
  document.addEventListener('visibilitychange', () => {if (document.hidden) stop();});

  async function load() {
    const option = select.selectedOptions[0];
    if (!option) return;
    const color = option.dataset.color;
    if (color === loadedColor) return;
    loadedColor = color;
    const current = ++generation;
    request?.abort(); request = new AbortController(); stop(); framesPreloaded = false;
    showMode(false); spinTab.hidden = true;
    spinStage.querySelector('iframe')?.remove();
    find('gallery-thumbs').replaceChildren();
    // Clear the previous color immediately, including the zoom image.
    gallery = {photos: [], frames: []}; image.src = option.dataset.image; large.removeAttribute('src');
    image.alt = 'Ảnh giới thiệu sản phẩm'; find('gallery-caption').textContent = 'Đang tải hình ảnh…';
    root.setAttribute('aria-busy', 'true');
    try {
      const response = await fetch(`${root.dataset.endpoint}?color=${encodeURIComponent(color)}`, {signal: request.signal});
      if (!response.ok) throw new Error('media');
      const data = await response.json();
      if (current !== generation) return;
      gallery = data;
      find('gallery-notice').textContent = data.photos.some(photo => photo.colorSpecific) ? 'Chọn ảnh thu nhỏ để xem thêm góc nhìn. Bấm ảnh lớn để phóng to.' : 'Chưa có ảnh riêng cho màu này. Các ảnh bên trên dùng để giới thiệu sản phẩm.';
      data.photos.forEach((photo, n) => {
        const button = document.createElement('button'); button.type = 'button'; button.setAttribute('aria-label', `Ảnh ${n + 1}: ${photo.caption}`);
        const thumbnail = document.createElement('img'); thumbnail.src = path(photo.url); thumbnail.alt = ''; thumbnail.loading = 'lazy';
        button.append(thumbnail); button.addEventListener('click', () => showPhoto(n)); find('gallery-thumbs').append(button);
      });
      showPhoto(0);
      const hasFrames = data.frames.length >= 12;
      spinTab.hidden = !hasFrames && !data.modelUrl;
      spinImage.hidden = !hasFrames; find('spin-controls').hidden = !hasFrames;
      find('model-credit').replaceChildren();
      if (hasFrames) {
        slider.max = data.frames.length - 1; showFrame(0);
        find('spin-help').textContent = 'Kéo ngang để xoay, dùng thanh trượt hoặc phím ← →.';
      } else if (data.modelUrl) {
        find('spin-help').textContent = 'Kéo để xoay, cuộn để thu phóng. Mô hình tham khảo hình dáng, màu có thể khác phiên bản đang chọn. Cần kết nối Internet.';
        const credit = find('model-credit');
        const link = document.createElement('a'); link.href = data.modelUrl.replace('/embed', ''); link.target = '_blank'; link.rel = 'noopener noreferrer'; link.textContent = data.modelCredit || 'Mô hình trên Sketchfab';
        const license = document.createElement('a'); license.href = 'https://creativecommons.org/licenses/by/4.0/'; license.target = '_blank'; license.rel = 'noopener noreferrer'; license.textContent = 'CC BY 4.0';
        credit.append(link, ' · ', license);
      }
    } catch (error) {
      if (error.name !== 'AbortError' && current === generation) {
        loadedColor = undefined; find('gallery-caption').textContent = 'Chưa tải được thư viện ảnh. Hãy chọn lại màu để thử lại.';
      }
    } finally {if (current === generation) root.removeAttribute('aria-busy');}
  }

  // Keep the native select as the no-JavaScript and assistive-technology fallback.
  const choices = document.createElement('div'); choices.className = 'detail-variant-options';
  select.before(choices);
  select.hidden = true;
  document.querySelector('label[for="variant-select"]').hidden = true;
  const resolveColor = window.ptvResolveColor || (() => ({bg: '#8e949b', light: false}));
  const options = [...select.options];
  const groups = [];
  for (const [key, label] of [['color', 'Màu sắc'], ['storage', 'Dung lượng'], ['ram', 'RAM']]) {
    const values = [...new Set(options.map(o => o.dataset[key]).filter(Boolean))];
    if (!values.length || (key === 'ram' && values.length < 2)) continue;
    const isColor = key === 'color';
    const group = document.createElement('div');
    group.className = isColor ? 'variant-picker-group swatch-picker-group' : 'variant-picker-group pill-picker-group';
    group.setAttribute('role', 'radiogroup');
    group.setAttribute('aria-label', label);

    const heading = document.createElement('span');
    heading.className = 'variant-heading';
    const headingLabel = document.createTextNode(`${label}: `);
    const selectedText = document.createElement('strong');
    selectedText.className = 'variant-selected-label';
    heading.append(headingLabel, selectedText);
    group.append(heading);

    const track = document.createElement('div');
    track.className = isColor ? 'color-swatch-grid' : 'storage-pill-grid';

    values.forEach(value => {
      const button = document.createElement('button');
      button.type = 'button';
      button.dataset.value = value;
      button.setAttribute('aria-label', `${label}: ${value}`);
      button.title = value;
      if (isColor) {
        button.className = 'swatch-btn';
        const swatch = resolveColor(value);
        const circle = document.createElement('span');
        circle.className = 'swatch-circle' + (swatch.light ? ' is-light' : '');
        circle.style.background = swatch.bg;
        button.append(circle);
      } else {
        button.className = 'pill-btn';
        button.textContent = value;
      }
      button.addEventListener('click', () => {
        const old = select.selectedOptions[0];
        const candidates = options.filter(o => o.dataset[key] === value);
        const matchSame = (o, requireStock) => {
          const sameOthers = (!isColor || o.dataset.storage === old.dataset.storage)
            && (key !== 'color' ? o.dataset.color === old.dataset.color : true)
            && (key !== 'storage' ? (o.dataset.storage === old.dataset.storage || !old.dataset.storage) : true)
            && (key !== 'ram' ? (o.dataset.ram === old.dataset.ram || !old.dataset.ram) : true);
          return sameOthers && (!requireStock || Number(o.dataset.stock) > 0);
        };
        const selected = candidates.find(o => matchSame(o, true))
          || candidates.find(o => matchSame(o, false))
          || candidates.find(o => isColor ? (o.dataset.storage === old.dataset.storage && Number(o.dataset.stock) > 0) : (o.dataset.color === old.dataset.color && Number(o.dataset.stock) > 0))
          || candidates.find(o => isColor ? o.dataset.storage === old.dataset.storage : o.dataset.color === old.dataset.color)
          || candidates.find(o => Number(o.dataset.stock) > 0)
          || candidates[0];
        select.value = selected.value;
        select.dispatchEvent(new Event('change', {bubbles: true}));
      });
      track.append(button);
    });
    group.append(track);
    choices.append(group);
    groups.push([key, group, selectedText]);
  }
  function refresh() {
    const active = select.selectedOptions[0];
    groups.forEach(([key, group, selectedText]) => {
      if (active) selectedText.textContent = active.dataset[key] || '';
      group.querySelectorAll('button').forEach(button => {
        const val = button.dataset.value;
        const pressed = active && val === active.dataset[key];
        button.setAttribute('aria-pressed', String(Boolean(pressed)));
        button.classList.toggle('is-active', Boolean(pressed));
        if (key !== 'color' && active) {
          const matchingInColor = options.filter(o => o.dataset.color === active.dataset.color && o.dataset[key] === val);
          const anyInStock = matchingInColor.length ? matchingInColor.some(o => Number(o.dataset.stock) > 0) : options.some(o => o.dataset[key] === val && Number(o.dataset.stock) > 0);
          button.classList.toggle('is-out-of-stock', !anyInStock);
        } else if (key === 'color') {
          const anyInStock = options.some(o => o.dataset.color === val && Number(o.dataset.stock) > 0);
          button.classList.toggle('is-out-of-stock', !anyInStock);
        }
      });
    });
    load();
  }
  select.addEventListener('change', refresh); refresh();
});
