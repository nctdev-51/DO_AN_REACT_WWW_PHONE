/**
 * ai-description.js — Module AI (Tài)
 * Tải mô tả AI thông minh cho trang chi tiết sản phẩm.
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        const panel = document.getElementById('ai-desc-panel');
        if (!panel) return; // Chỉ chạy trên trang detail

        const productId = panel.dataset.productId;
        if (!productId) return;

        const ctx = document.querySelector('meta[name="ctx"]')?.content
            || (document.querySelector('[data-context]')?.dataset.context)
            || '';

        const descEl     = document.getElementById('ai-description-text');
        const featuresWrap = document.getElementById('ai-key-features-wrap');
        const featuresList = document.getElementById('ai-key-features');
        const refreshBtn = document.getElementById('btn-refresh-ai');

        // ── Load AI Description ───────────────────────────────────
        function loadDescription(refresh) {
            // Show skeleton
            showSkeleton();
            if (featuresWrap) featuresWrap.hidden = true;

            const url = refresh
                ? ctx + '/api/ai/description/' + productId + '/refresh'
                : ctx + '/api/ai/description/' + productId;

            const headers = { Accept: 'application/json' };
            if (refresh && refreshBtn) {
                const csrfHeader = refreshBtn.dataset.csrfHeader;
                const csrfToken = refreshBtn.dataset.csrfToken;
                if (csrfHeader && csrfToken) {
                    headers[csrfHeader] = csrfToken;
                }
            }

            fetch(url, {
                method: refresh ? 'POST' : 'GET',
                headers: headers,
                credentials: 'same-origin'
            })
                .then(function (res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function (data) {
                    if (!refresh && typeof window.trackEvent === 'function') {
                        window.trackEvent('AI_DESCRIPTION_VIEW', {productId: Number(productId)});
                    }
                    // Render description
                    if (descEl) {
                        const p = document.createElement('p');
                        p.textContent = data.description || '';
                        descEl.innerHTML = '';
                        descEl.appendChild(p);
                    }

                    // Render key features
                    const features = data.keyFeatures || [];
                    if (featuresList && features.length > 0) {
                        featuresList.innerHTML = '';
                        features.forEach(function (feat) {
                            const li = document.createElement('li');
                            li.textContent = feat;
                            featuresList.appendChild(li);
                        });
                        if (featuresWrap) featuresWrap.hidden = false;
                    }
                })
                .catch(function (err) {
                    console.warn('[AI Panel] Fetch failed:', err);
                    showError();
                })
                .finally(function () {
                    if (refreshBtn) refreshBtn.disabled = false;
                });
        }

        // ── Skeleton ──────────────────────────────────────────────
        function showSkeleton() {
            if (!descEl) return;
            descEl.innerHTML =
                '<div class="ai-skeleton" aria-hidden="true"></div>' +
                '<div class="ai-skeleton ai-skeleton--medium" aria-hidden="true"></div>' +
                '<div class="ai-skeleton ai-skeleton--short" aria-hidden="true"></div>';
        }

        // ── Error state ───────────────────────────────────────────
        function showError() {
            if (descEl) {
                descEl.innerHTML =
                    '<span class="ai-error-state">' +
                    '⚠️ Không tải được mô tả AI. Hãy thử lại sau.' +
                    '</span>';
            }
        }

        // ── Refresh Button ────────────────────────────────────────
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function () {
                refreshBtn.disabled = true;
                loadDescription(true);
            });
        }

        // ── Initial load ──────────────────────────────────────────
        loadDescription(false);
    });

    // ── Admin Product Form Integration (Prompt 2 requirement) ───
    document.addEventListener('DOMContentLoaded', function () {
        const adminAiBtn = document.getElementById('btn-admin-ai-gen');
        const descTextarea = document.getElementById('description');
        const statusEl = document.getElementById('admin-ai-status');

        if (!adminAiBtn || !descTextarea) return;

        adminAiBtn.addEventListener('click', function () {
            const pId = adminAiBtn.dataset.productId;
            if (!pId) return;

            adminAiBtn.disabled = true;
            adminAiBtn.innerHTML = '⏳ Đang tạo mô tả AI...';
            if (statusEl) {
                statusEl.style.display = 'block';
                statusEl.textContent = 'Đang kết nối AI sinh nội dung chuẩn SEO...';
            }

            const ctx = document.querySelector('meta[name="ctx"]')?.content
                || (document.querySelector('[data-context]')?.dataset.context)
                || '';

            fetch(ctx + '/api/ai/description/' + encodeURIComponent(pId))
                .then(function (res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function (data) {
                    let fullText = (data.description || '').trim();
                    if (data.keyFeatures && data.keyFeatures.length > 0) {
                        fullText += '\n\nĐặc điểm nổi bật:\n' + data.keyFeatures.map(function (f) {
                            return '• ' + f;
                        }).join('\n');
                    }
                    descTextarea.value = fullText;
                    if (statusEl) {
                        statusEl.className = 'small mt-1 text-success';
                        statusEl.textContent = '✅ Đã điền mô tả AI vào khung thành công!';
                        setTimeout(function () { statusEl.style.display = 'none'; }, 4000);
                    }
                })
                .catch(function (err) {
                    console.warn('[Admin AI Desc] Failed:', err);
                    if (statusEl) {
                        statusEl.className = 'small mt-1 text-danger';
                        statusEl.textContent = '⚠️ Không thể tạo mô tả lúc này. Hãy kiểm tra kết nối mạng.';
                    }
                })
                .finally(function () {
                    adminAiBtn.disabled = false;
                    adminAiBtn.innerHTML = '✨ Tạo mô tả bằng AI';
                });
        });
    });
})();
