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

            const url = ctx + '/api/ai/description/' + productId
                + (refresh ? '?refresh=true' : '');

            fetch(url, { credentials: 'same-origin' })
                .then(function (res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function (data) {
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
})();
