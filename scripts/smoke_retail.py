"""Exercise retail workflows on the running local store. Uses the local .eml outbox.
Creates two fixture accounts, one coupon and an order; records exact fixture IDs in target/.
"""

import json
import os
import re
import urllib.parse
import uuid
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timedelta
from threading import Barrier

from smoke_test import ROOT, account_link, client, first_variant, login, post, request


def save(state):
    (ROOT / "target/retail-smoke-state.json").write_text(
        json.dumps(state), encoding="utf-8"
    )


def register(browser, email):
    password = "Retail-" + uuid.uuid4().hex
    _, page = request(browser, "/register")
    path, page = post(
        browser,
        "/register",
        page,
        fullName="Retail HTTP Test",
        email=email,
        password=password,
        confirmPassword=password,
        phone="0901234567",
    )
    assert path == "/register?success", path
    link = account_link(email, "/verify-email")
    _, page = request(browser, link)
    raw = urllib.parse.parse_qs(urllib.parse.urlsplit(link).query)["token"][0]
    path, page = post(browser, "/verify-email", page, token=raw)
    assert path == "/login?verified", path
    login(browser, email, password)


if __name__ == "__main__":
    suffix = uuid.uuid4().hex[:10]
    state = {
        "emails": [f"retail-smoke-{suffix}-{n}@example.com" for n in range(2)],
        "promotionCodes": ["RETAIL" + suffix.upper()],
        "orders": [],
    }
    save(state)
    guest = client()
    _, page = request(guest, "/products")
    for product_id in [1, 2, 3]:
        _, page = post(guest, "/compare/add", page, productId=product_id)
    assert page.count('class="compare-image"') == 3
    _, page = post(guest, "/compare/add", page, productId=4)
    assert "tối đa 3" in page
    shared = re.search(r'href="([^"]*/compare\?ids=[^"]+)"', page).group(1)
    _, shared_page = request(client(), shared)
    assert shared_page.count('class="compare-image"') == 3
    assert "Chỉ hiện điểm khác nhau" in shared_page
    ids = urllib.parse.parse_qs(urllib.parse.urlsplit(shared).query)["ids"][0].split(
        ","
    )
    _, page = post(guest, "/compare/remove", page, variantId=ids[0])
    assert page.count('class="compare-image"') == 2
    _, page = post(guest, "/compare/clear", page)
    assert "Bạn chưa chọn sản phẩm" in page
    for path in [
        "/css/comparison.css",
        "/js/comparison.js",
        "/products?storage=256GB&ram=8GB&inStock=true",
        "/offers",
    ]:
        request(guest, path)
    print("COMPARE, SHARE LINK, LIMIT, REMOVE, FILTERS AND ASSETS OK")

    buyers = [client(), client()]
    for browser, email in zip(buyers, state["emails"]):
        register(browser, email)
    _, page = request(buyers[0], "/products/1")
    _, page = post(buyers[0], "/account/wishlist/add", page, productId=1)
    _, page = post(buyers[0], "/account/wishlist/add", page, productId=1)
    assert page.count('class="product-card"') == 1
    _, other = request(buyers[1], "/account/wishlist")
    assert 'class="product-card"' not in other
    print("WISHLIST PERSISTENCE, DUPLICATE PROTECTION AND PRIVACY OK")

    admin = client()
    login(
        admin,
        os.environ.get("ADMIN_USERNAME", "admin"),
        os.environ.get("ADMIN_PASSWORD", "Admin123!"),
    )
    for path in ["/admin/promotions", "/admin/promotions/new", "/admin/support"]:
        request(admin, path)
    _, page = request(admin, "/admin/promotions/new")
    now = datetime.now()
    fields = dict(
        code=state["promotionCodes"][0],
        description="Retail HTTP coupon",
        discountType="FIXED",
        amount=100000,
        minimumSubtotal=0,
        maximumDiscount="",
        usageLimit=1,
        active="true",
        startsAt=(now - timedelta(days=1)).strftime("%Y-%m-%dT%H:%M"),
        endsAt=(now + timedelta(days=1)).strftime("%Y-%m-%dT%H:%M"),
    )
    path, page = post(admin, "/admin/promotions/save", page, **fields)
    assert path == "/admin/promotions", path
    code = state["promotionCodes"][0]
    assert code in page
    promo_id = int(
        re.search(r"/admin/promotions/(\d+)/edit", page[page.find(code) :]).group(1)
    )
    request(admin, f"/admin/promotions/{promo_id}/edit")
    _, page = request(guest, "/offers")
    assert code in page
    print("ADMIN PROMOTION CREATE/EDIT, POLICY FORM AND PUBLIC OFFERS OK")

    forms = []
    for n, browser in enumerate(buyers):
        _, page = request(browser, f"/products/{n+1}")
        variant_id, _ = first_variant(page)
        _, page = post(browser, "/cart/add", page, variantId=variant_id, quantity=1)
        _, page = request(browser, "/checkout")
        fields = dict(
            recipientName="Retail HTTP Test",
            phone="0901234567",
            shippingAddress="12 Test Street",
            paymentMethod="COD",
            promotionCode=code,
        )
        _, page = post(browser, "/checkout/quote", page, **fields)
        assert code in page
        forms.append((browser, page, fields))
    barrier = Barrier(2)

    def buy(args):
        browser, page, fields = args
        barrier.wait(timeout=10)
        return post(browser, "/checkout/place-order", page, **fields)

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(buy, forms))
    winners = [
        i
        for i, (path, _) in enumerate(results)
        if path.startswith("/checkout/success/")
    ]
    assert len(winners) == 1, [path for path, _ in results]
    winner = winners[0]
    assert "hết lượt" in results[1 - winner][1]
    order_id = int(results[winner][0].rsplit("/", 1)[1])
    state["orders"].append(order_id)
    save(state)
    _, page = request(buyers[winner], f"/orders/{order_id}")
    assert "Lịch sử xử lý" in page and code in page
    print("CONCURRENT COUPON QUOTA, CHECKOUT QUOTE AND ORDER SNAPSHOTS OK")

    _, page = request(admin, "/admin/orders")
    for status in ["Đang giao", "Đã giao"]:
        _, page = post(admin, f"/admin/orders/{order_id}/status", page, status=status)
    customer = buyers[winner]
    _, page = request(customer, f"/orders/{order_id}")
    detail = int(
        re.search(r'id="support-detail"[^>]*>\s*<option value="(\d+)"', page).group(1)
    )
    reason = "Retail HTTP test: màn hình bị lỗi hiển thị."
    _, page = post(
        customer,
        f"/orders/{order_id}/support",
        page,
        detailId=detail,
        type="WARRANTY",
        quantity=1,
        reason=reason,
    )
    _, page = request(customer, "/account/support")
    assert reason in page
    ticket_id = int(re.search(r"/account/support/(\d+)/cancel", page).group(1))
    _, other = request(buyers[1 - winner], "/account/support")
    assert reason not in other
    _, page = request(admin, "/admin/support")
    for status in ["APPROVED", "RECEIVED", "COMPLETED"]:
        _, page = post(
            admin,
            f"/admin/support/{ticket_id}/status",
            page,
            status=status,
            note="Retail HTTP test: đã kiểm tra và bàn giao thiết bị cho khách.",
        )
    _, page = request(customer, "/account/support")
    assert "Đã xử lý xong" in page
    _, page = request(admin, f"/admin/orders/{order_id}")
    assert "Lịch sử xử lý" in page
    print("DELIVERY TIMELINE, WARRANTY REQUEST, OWNERSHIP AND ADMIN LIFECYCLE OK")
    print("ALL RETAIL HTTP CHECKS PASSED")
