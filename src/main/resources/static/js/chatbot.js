/**
 * chatbot.js — Module AI (Tài)
 * Widget chatbot tư vấn nổi (FAB) cho PTV Mobile.
 */
(function () {
    'use strict';

<<<<<<< HEAD
<<<<<<< HEAD
    document.addEventListener('DOMContentLoaded', function () {
=======
    function initChatbot() {
>>>>>>> origin/taidev
=======
    function initChatbot() {
>>>>>>> origin/taidev
        const fabBtn      = document.getElementById('chatbot-fab-btn');
        const chatPanel   = document.getElementById('chatbot-panel');
        const closeBtn    = document.getElementById('chatbot-close-btn');
        const messagesEl  = document.getElementById('chatbot-messages');
        const inputEl     = document.getElementById('chatbot-input');
        const sendBtn     = document.getElementById('chatbot-send-btn');

        if (!fabBtn || !chatPanel || !messagesEl || !inputEl || !sendBtn) return;

        // ── Context path ──────────────────────────────────────────
        const ctx = document.querySelector('meta[name="ctx"]')?.content
            || (document.querySelector('[data-context]')?.dataset.context)
            || '';

        // ── Toggle Panel ──────────────────────────────────────────
<<<<<<< HEAD
<<<<<<< HEAD
        fabBtn.addEventListener('click', function () {
            const isHidden = chatPanel.hidden;
            chatPanel.hidden = !isHidden;
            fabBtn.setAttribute('aria-expanded', String(isHidden));
            if (isHidden) {
                setTimeout(function () { inputEl.focus(); }, 100);
                scrollMessages();
            }
        });

        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                chatPanel.hidden = true;
=======
=======
>>>>>>> origin/taidev
        function toggleChatPanel() {
            const isHidden = chatPanel.hasAttribute('hidden') || chatPanel.hidden || chatPanel.style.display === 'none';
            if (isHidden) {
                chatPanel.removeAttribute('hidden');
                chatPanel.hidden = false;
                chatPanel.style.display = 'flex';
                fabBtn.setAttribute('aria-expanded', 'true');
                setTimeout(function () { inputEl.focus(); }, 100);
                scrollMessages();
            } else {
                chatPanel.setAttribute('hidden', '');
                chatPanel.hidden = true;
                chatPanel.style.display = 'none';
                fabBtn.setAttribute('aria-expanded', 'false');
            }
        }

        fabBtn.addEventListener('click', toggleChatPanel);

        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                chatPanel.setAttribute('hidden', '');
                chatPanel.hidden = true;
                chatPanel.style.display = 'none';
<<<<<<< HEAD
>>>>>>> origin/taidev
=======
>>>>>>> origin/taidev
                fabBtn.setAttribute('aria-expanded', 'false');
            });
        }

        // ── Send Message ──────────────────────────────────────────
        function sendMessage() {
            const text = inputEl.value.trim();
            if (!text || sendBtn.disabled) return;

            appendBubble(text, 'user');
            inputEl.value = '';
            inputEl.disabled = true;
            sendBtn.disabled = true;

            const typingEl = showTyping();

            fetch(ctx + '/api/ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ message: text })
            })
                .then(function (res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function (data) {
                    removeTyping(typingEl);
                    appendBubble(data.reply || 'Xin lỗi, không có phản hồi.', 'bot');

                    // Render danh sách thẻ sản phẩm gợi ý (kèm nút "Xem chi tiết")
                    if (data.productCards && data.productCards.length > 0) {
                        appendRichProductCards(data.productCards);
                    } else if (data.suggestedProducts && data.suggestedProducts.length > 0) {
                        appendLegacyProductSuggestions(data.suggestedProducts);
                    }
                })
                .catch(function (err) {
                    console.warn('[Chatbot] Error:', err);
                    removeTyping(typingEl);
                    appendBubble(
                        'Xin lỗi, đã xảy ra lỗi kết nối. Vui lòng thử lại sau. 🙏',
                        'bot'
                    );
                })
                .finally(function () {
                    inputEl.disabled = false;
                    sendBtn.disabled = false;
                    inputEl.focus();
                });
        }

        sendBtn.addEventListener('click', sendMessage);
        inputEl.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // ── Markdown & Table Formatter ────────────────────────────
        function formatMessageHtml(raw) {
            if (!raw) return '';
<<<<<<< HEAD
<<<<<<< HEAD
            // 1. Escape HTML an toàn
            let s = raw
=======
=======
>>>>>>> origin/taidev
            // 0. Loại bỏ triệt để các mã SUGGEST (kể cả chứa khoảng trắng unicode)
            let s = raw.replace(/\[[\s\S]*?(?:SUGGEST|GỢI Ý|PRODUCT|ID)\s*:?\s*\d+[\s\S]*?\]/gi, '');

            // 1. Escape HTML an toàn
            s = s
<<<<<<< HEAD
>>>>>>> origin/taidev
=======
>>>>>>> origin/taidev
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');

            // 2. Chuyển đổi định dạng bold **chữ đậm**
            s = s.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

            // 3. Xử lý triệt để trường hợp AI xuất bảng markdown (|...|)
            const lines = s.split(/\r?\n/);
            const cleaned = [];
            for (let line of lines) {
                const tr = line.trim();
                // Bỏ dòng gạch ngang ngăn cách bảng (|---|---|)
                if (/^\|[-|\s:]+\|$/.test(tr)) {
                    continue;
                }
                // Nếu là dòng bảng (| col1 | col2 | ...)
                if (tr.startsWith('|') && tr.endsWith('|')) {
                    const cols = tr.split('|').map(function (c) { return c.trim(); }).filter(function (c) { return c.length > 0; });
                    if (cols.length >= 2) {
                        const c0 = cols[0].toLowerCase();
                        const c1 = cols[1].toLowerCase();
                        // Bỏ qua dòng tiêu đề bảng bất kể AI dùng từ gì (id, mẫu, tên, sản phẩm, model, stt,...)
                        if (c0 === 'id' || c0 === 'stt' || c0.includes('mẫu') || c0.includes('tên') || c1.includes('mẫu') || c1.includes('tên') || c1.includes('sản phẩm') || c1.includes('giá')) {
                            continue;
                        }
                        // Format lại thành dòng đọc đẹp mắt kèm icon điện thoại
                        line = '📱 <strong>' + cols.slice(0, 3).join('</strong> — ') + (cols.length > 3 ? (' (' + cols.slice(3).join(', ') + ')') : '');
                    }
                }
                cleaned.push(line);
            }
            s = cleaned.join('\n');

            // 4. Chuyển đổi xuống dòng
            s = s.replace(/\n\n+/g, '<br><br>').replace(/\n/g, '<br>');

            return s;
        }

        // ── Helper: Append Bubble ─────────────────────────────────
        function appendBubble(text, who) {
            const div = document.createElement('div');
            div.className = who === 'user' ? 'chatbot-bubble-user' : 'chatbot-bubble-bot';

            if (who === 'bot') {
                div.innerHTML = formatMessageHtml(text);
            } else {
                div.textContent = text;
            }

            messagesEl.appendChild(div);
            scrollMessages();
        }

        // ── Helper: Safe Product Card Builder (anti-XSS) ──────────
<<<<<<< HEAD
<<<<<<< HEAD
        function createSuggestionCardElement(productId, titleText, priceText) {
=======
        function createSuggestionCardElement(productId, titleText, priceText, imageUrl) {
>>>>>>> origin/taidev
=======
        function createSuggestionCardElement(productId, titleText, priceText, imageUrl) {
>>>>>>> origin/taidev
            const safeId = Number(productId);
            if (!Number.isInteger(safeId) || safeId <= 0) {
                return null;
            }
            const a = document.createElement('a');
            a.className = 'chatbot-suggestion-card';
            a.href = ctx + '/products/' + safeId;
            a.target = '_blank';
            a.rel = 'noopener noreferrer';
            a.title = 'Xem chi tiết ' + titleText;

<<<<<<< HEAD
<<<<<<< HEAD
            const icon = document.createElement('div');
            icon.className = 'chatbot-card-icon';
            icon.textContent = '📱';
=======
=======
>>>>>>> origin/taidev
            // Media: Ảnh sản phẩm thật từ hệ thống kèm fallback
            const mediaWrap = document.createElement('div');
            mediaWrap.className = 'chatbot-card-media';

            const defaultImg = ctx + '/images/products/placeholder.svg';
            const img = document.createElement('img');
            img.className = 'chatbot-card-img';
            img.alt = titleText || 'Sản phẩm';
            img.loading = 'lazy';

            let src = imageUrl;
            if (src && typeof src === 'string' && src.trim().length > 0) {
                src = src.trim();
                if (!src.startsWith('http://') && !src.startsWith('https://') && !src.startsWith('//')) {
                    src = ctx + (src.startsWith('/') ? src : '/' + src);
                }
            } else {
                src = defaultImg;
            }

            img.src = src;
            img.onerror = function () {
                if (this.src !== defaultImg) {
                    this.src = defaultImg;
                }
            };
            mediaWrap.appendChild(img);
<<<<<<< HEAD
>>>>>>> origin/taidev
=======
>>>>>>> origin/taidev

            const info = document.createElement('div');
            info.className = 'chatbot-card-info';

            const nameEl = document.createElement('div');
            nameEl.className = 'chatbot-card-name';
            nameEl.textContent = titleText;

            const metaEl = document.createElement('div');
            metaEl.className = 'chatbot-card-meta';

            if (priceText) {
                const priceEl = document.createElement('span');
                priceEl.className = 'chatbot-card-price';
                priceEl.textContent = priceText;
                metaEl.appendChild(priceEl);
            }

            const btnEl = document.createElement('span');
            btnEl.className = 'chatbot-card-btn';
            btnEl.textContent = 'Xem chi tiết ➔';
            metaEl.appendChild(btnEl);

            info.appendChild(nameEl);
            info.appendChild(metaEl);
<<<<<<< HEAD
<<<<<<< HEAD
            a.appendChild(icon);
=======
            a.appendChild(mediaWrap);
>>>>>>> origin/taidev
=======
            a.appendChild(mediaWrap);
>>>>>>> origin/taidev
            a.appendChild(info);
            return a;
        }

        function createSuggestionRowHeader(titleText) {
            const label = document.createElement('div');
            label.className = 'chatbot-suggestion-title';
            const icon = document.createElement('span');
            icon.textContent = '📌';
            const strong = document.createElement('strong');
            strong.textContent = ' ' + titleText;
            label.appendChild(icon);
            label.appendChild(strong);
            return label;
        }

        // ── Helper: Rich Product Suggestion Cards ──────────────────
        function appendRichProductCards(cards) {
            const wrap = document.createElement('div');
            wrap.className = 'chatbot-suggestion-row';
            wrap.appendChild(createSuggestionRowHeader('Gợi ý xem chi tiết sản phẩm:'));

            cards.slice(0, 3).forEach(function (p) {
                const name = p && p.name != null ? String(p.name) : ('Sản phẩm #' + (p && p.id ? p.id : ''));
                const price = p && p.price != null ? String(p.price) : '';
<<<<<<< HEAD
<<<<<<< HEAD
                const card = createSuggestionCardElement(p && p.id, name, price);
=======
                const imageUrl = p && p.imageUrl != null ? String(p.imageUrl) : '';
                const card = createSuggestionCardElement(p && p.id, name, price, imageUrl);
>>>>>>> origin/taidev
=======
                const imageUrl = p && p.imageUrl != null ? String(p.imageUrl) : '';
                const card = createSuggestionCardElement(p && p.id, name, price, imageUrl);
>>>>>>> origin/taidev
                if (card) {
                    wrap.appendChild(card);
                }
            });

            messagesEl.appendChild(wrap);
            scrollMessages();
        }

        // ── Helper: Legacy Product Suggestions ─────────────────────
        function appendLegacyProductSuggestions(ids) {
            const wrap = document.createElement('div');
            wrap.className = 'chatbot-suggestion-row';
            wrap.appendChild(createSuggestionRowHeader('Gợi ý xem chi tiết:'));

            ids.slice(0, 3).forEach(function (id) {
<<<<<<< HEAD
<<<<<<< HEAD
                const card = createSuggestionCardElement(id, 'Sản phẩm #' + Number(id), '');
=======
                const card = createSuggestionCardElement(id, 'Sản phẩm #' + Number(id), '', null);
>>>>>>> origin/taidev
=======
                const card = createSuggestionCardElement(id, 'Sản phẩm #' + Number(id), '', null);
>>>>>>> origin/taidev
                if (card) {
                    wrap.appendChild(card);
                }
            });

            messagesEl.appendChild(wrap);
            scrollMessages();
        }

        // ── Helper: Typing indicator ──────────────────────────────
        function showTyping() {
            const div = document.createElement('div');
            div.className = 'chatbot-typing';
            div.innerHTML = '<span></span><span></span><span></span>';
            messagesEl.appendChild(div);
            scrollMessages();
            return div;
        }

        function removeTyping(el) {
            if (el && el.parentNode) el.parentNode.removeChild(el);
        }

        function scrollMessages() {
            setTimeout(function () {
                messagesEl.scrollTop = messagesEl.scrollHeight;
            }, 30);
        }
<<<<<<< HEAD
<<<<<<< HEAD
    });
=======
=======
>>>>>>> origin/taidev
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initChatbot);
    } else {
        initChatbot();
    }
<<<<<<< HEAD
>>>>>>> origin/taidev
=======
>>>>>>> origin/taidev
})();
