"""End-to-end checks against the running local store.
Creates one uniquely named customer and test orders; IDs are stored under ignored target/.
Only use against a local/test database.
"""

import html
import http.cookiejar
import json
import os
import re
import urllib.error
import urllib.parse
import urllib.request
import uuid
from email import policy
from email.parser import BytesParser
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = os.environ.get("STORE_URL", "http://127.0.0.1:8080")


def client():
    return urllib.request.build_opener(
        urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar())
    )


def request(browser, path, data=None):
    payload = None if data is None else urllib.parse.urlencode(data).encode()
    with browser.open(BASE + path, data=payload, timeout=30) as response:
        return response.geturl().removeprefix(BASE), response.read().decode("utf-8")


def token(page):
    match = re.search(r'name="_csrf"[^>]*value="([^"]+)"', page)
    assert match, "Missing CSRF input"
    return html.unescape(match.group(1))


def post(browser, path, page, **data):
    return request(browser, path, dict(data, _csrf=token(page)))


def login(browser, email, password):
    _, page = request(browser, "/login")
    path, page = post(browser, "/login", page, username=email, password=password)
    assert "error" not in path, path
    return page


def account_link(email, path):
    """Read a locally delivered message; always exercise links against STORE_URL."""
    messages = sorted(
        (ROOT / "target/mail-outbox").glob("*.eml"),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    for file in messages:
        message = BytesParser(policy=policy.default).parsebytes(file.read_bytes())
        if str(message["To"]) != email:
            continue
        body = message.get_body(preferencelist=("html",))
        content = body.get_content() if body else message.get_content()
        match = re.search(
            r'href="([^"]*' + re.escape(path) + r'\?token=[A-Za-z0-9_-]+)"', content
        )
        if match:
            parsed = urllib.parse.urlsplit(html.unescape(match.group(1)))
            return parsed.path + "?" + parsed.query
    raise AssertionError("No local account email for " + path)


def first_variant(page):
    match = re.search(
        r'<option\s+value="(\d+)"\s+data-price="([^"]+)"\s+data-stock="(\d+)"', page
    )
    assert match, "Missing variant options"
    return int(match.group(1)), int(match.group(3))


def stock(browser, variant_id):
    _, page = request(browser, "/products/1")
    match = re.search(
        rf'<option\s+value="{variant_id}"\s+data-price="[^"]+"\s+data-stock="(\d+)"',
        page,
    )
    return int(match.group(1))


def multipart(browser, path, page, fields, filename, content):
    boundary = "----StoreTest" + uuid.uuid4().hex
    payload = bytearray()
    for key, value in dict(fields, _csrf=token(page)).items():
        payload.extend(
            f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{value}\r\n'.encode()
        )
    payload.extend(
        f'--{boundary}\r\nContent-Disposition: form-data; name="image"; filename="{filename}"\r\nContent-Type: image/png\r\n\r\n'.encode()
    )
    payload.extend(content)
    payload.extend(f"\r\n--{boundary}--\r\n".encode())
    req = urllib.request.Request(
        BASE + path,
        data=bytes(payload),
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
    )
    with browser.open(req, timeout=30) as response:
        return response.geturl().removeprefix(BASE), response.read().decode("utf-8")


def forbidden(browser, path):
    try:
        request(browser, path)
        raise AssertionError("Unexpected access: " + path)
    except urllib.error.HTTPError as error:
        assert error.code == 403


if __name__ == "__main__":
    guest = client()
    for path in [
        "/",
        "/products",
        "/products?keyword=iPhone",
        "/products?brand=Samsung&sort=price-asc",
        "/products/1",
        "/cart",
        "/register",
        "/login",
        "/css/store.css",
        "/js/store.js",
        "/vendor/bootstrap/bootstrap.min.css",
    ]:
        request(guest, path)
        print("GET OK", path)
    catalog = json.loads(
        (ROOT / "src/main/resources/catalog/products.json").read_text(encoding="utf-8")
    )
    for product in catalog:
        with guest.open(BASE + product["image"]) as response:
            assert (
                response.headers["Content-Type"].startswith("image/")
                and len(response.read()) > 2000
            )
    print("LOCAL IMAGES OK", len(catalog))
    email = "smoke-" + uuid.uuid4().hex[:10] + "@example.com"
    password = uuid.uuid4().hex + "Aa9!"
    state = {"email": email, "orders": []}
    state_path = ROOT / "target/smoke-state.json"
    state_path.write_text(json.dumps(state), encoding="utf-8")
    _, page = request(guest, "/register")
    path, page = post(
        guest,
        "/register",
        page,
        fullName="Kiểm thử local",
        email=email,
        password=password,
        confirmPassword=password,
        phone="0901234567",
    )
    assert path == "/register?success", path
    verification = account_link(email, "/verify-email")
    _, page = request(guest, verification)
    path, page = post(
        guest,
        "/verify-email",
        page,
        token=urllib.parse.parse_qs(urllib.parse.urlsplit(verification).query)["token"][
            0
        ],
    )
    assert path == "/login?verified", path
    login(guest, email, password)
    for path in ["/account", "/account/password", "/account/addresses", "/orders"]:
        request(guest, path)
    _, page = request(guest, "/account")
    _, page = post(
        guest, "/account", page, fullName="Kiểm thử tài khoản", phone="0912345678"
    )
    assert "Kiểm thử tài khoản" in page
    _, page = request(guest, "/account/addresses")
    _, page = post(
        guest,
        "/account/addresses",
        page,
        recipientName="Người nhận thử",
        phone="0901234567",
        line="12 Test Street",
        defaultAddress="true",
    )
    assert "12 Test Street" in page
    address_id = int(re.search(r'name="id"\s+value="(\d+)"', page).group(1))
    _, page = post(
        guest,
        "/account/addresses",
        page,
        id=address_id,
        recipientName="Người nhận thử",
        phone="0901234567",
        line="Updated address",
        defaultAddress="true",
    )
    assert "Updated address" in page
    _, page = request(guest, "/products/1")
    variant_id, initial_stock = first_variant(page)
    _, page = post(
        guest, "/products/1/reviews", page, rating=5, content="Should be rejected"
    )
    assert "Chỉ khách đã mua" in page
    _, page = request(guest, "/products/1")
    _, page = post(guest, "/cart/add", page, variantId=variant_id, quantity=2)
    _, page = request(guest, "/checkout")
    assert "Updated address" in page
    path, invalid = post(
        guest,
        "/checkout/place-order",
        page,
        recipientName="",
        shippingAddress="",
        phone="abc",
        paymentMethod="COD",
    )
    assert path == "/checkout/place-order" and "alert-danger" in invalid
    path, page = post(
        guest,
        "/checkout/place-order",
        invalid,
        recipientName="Người nhận thử",
        shippingAddress="Updated address",
        phone="0901234567",
        paymentMethod="COD",
    )
    assert path.startswith("/checkout/success/"), path
    cancelled_id = int(path.rsplit("/", 1)[1])
    state["orders"].append(cancelled_id)
    state_path.write_text(json.dumps(state), encoding="utf-8")
    assert stock(guest, variant_id) == initial_stock - 2
    _, page = request(guest, f"/orders/{cancelled_id}")
    _, page = post(
        guest, f"/orders/{cancelled_id}/cancel", page, reason="Đổi ý khi kiểm thử"
    )
    assert "Đã hủy" in page and stock(guest, variant_id) == initial_stock
    print("ACCOUNT, ADDRESS, STOCK RESERVATION AND CANCELLATION OK")
    _, page = request(guest, "/products/1")
    _, page = post(guest, "/cart/add", page, variantId=variant_id, quantity=2)
    _, page = request(guest, "/checkout")
    path, page = post(
        guest,
        "/checkout/place-order",
        page,
        recipientName="Người nhận thử",
        shippingAddress="Updated address",
        phone="0901234567",
        paymentMethod="COD",
    )
    order_id = int(path.rsplit("/", 1)[1])
    state["orders"].append(order_id)
    state_path.write_text(json.dumps(state), encoding="utf-8")
    forbidden(guest, "/admin/orders")
    admin = client()
    login(
        admin,
        os.environ.get("ADMIN_USERNAME", "admin"),
        os.environ.get("ADMIN_PASSWORD", "Admin123!"),
    )
    for path in [
        "/admin",
        "/admin/orders",
        f"/admin/orders/{order_id}",
        "/admin/users",
        "/admin/products",
        "/admin/products/new",
        "/admin/products/1/edit",
        "/admin/products/1/variants",
        "/admin/categories",
        "/admin/categories/new",
        "/admin/categories/1/edit",
    ]:
        request(admin, path)
        print("ADMIN GET OK", path)
    _, page = request(admin, f"/admin/orders/{order_id}")
    _, page = post(admin, f"/admin/orders/{order_id}/status", page, status="Đã giao")
    assert "Không thể chuyển" in page
    for status in ["Đang giao", "Đã giao"]:
        _, page = request(admin, f"/admin/orders/{order_id}")
        _, page = post(admin, f"/admin/orders/{order_id}/status", page, status=status)
    _, page = request(guest, f"/orders/{order_id}")
    assert "Đã giao" in page and "Đã thanh toán" in page
    _, page = request(guest, "/products/1")
    _, page = post(
        guest, "/products/1/reviews", page, rating=4, content="Đánh giá từ đơn đã giao"
    )
    assert "Đánh giá từ đơn đã giao" in page
    _, page = request(guest, "/account/password")
    new_password = uuid.uuid4().hex + "Aa9!"
    _, page = post(
        guest,
        "/account/password",
        page,
        currentPassword=password,
        newPassword=new_password,
        confirmPassword=new_password,
    )
    _, page = post(guest, "/logout", page)
    login(guest, email, new_password)
    print("DELIVERY, VERIFIED REVIEW AND PASSWORD CHANGE OK")
    _, page = request(admin, "/admin/users")
    user_id = int(
        re.search(r"/admin/users/(\d+)/edit", page[page.find(email) :]).group(1)
    )
    state["userId"] = user_id
    state_path.write_text(json.dumps(state), encoding="utf-8")
    _, page = request(admin, f"/admin/users/{user_id}/edit")
    _, page = post(
        admin,
        f"/admin/users/{user_id}/edit",
        page,
        fullName="Kiểm thử cập nhật",
        phone="0912345678",
        address="Updated street",
        status=1,
    )
    _, page = request(admin, f"/admin/users/{user_id}/edit")
    assert "Kiểm thử cập nhật" in page
    _, page = post(admin, f"/admin/users/{user_id}/delete", page)
    assert "Không thể xóa người dùng đã có đơn hàng" in page
    category_name = "Smoke Category " + uuid.uuid4().hex[:8]
    _, page = request(admin, "/admin/categories/new")
    _, page = post(
        admin,
        "/admin/categories/save",
        page,
        name=category_name,
        description="Temporary test category",
    )
    category_id = int(
        re.search(
            r"/admin/categories/(\d+)/edit", page[page.find(category_name) :]
        ).group(1)
    )
    product_name = "Smoke Product " + uuid.uuid4().hex[:8]
    fields = dict(
        name=product_name,
        brand="Test",
        sku="TEST-" + uuid.uuid4().hex[:8],
        price=123456,
        description="Temporary test product",
        categoryId=category_id,
        status=1,
        warrantyMonths=12,
    )
    _, page = request(admin, "/admin/products/new")
    _, invalid = multipart(
        admin, "/admin/products/save", page, fields, "invalid.png", b"not a PNG"
    )
    assert "Chỉ chấp nhận ảnh PNG hoặc JPEG" in invalid
    picture = (
        ROOT / "src/main/resources/static/images/products/iphone-16.png"
    ).read_bytes()
    _, page = multipart(
        admin, "/admin/products/save", invalid, fields, "test.png", picture
    )
    product_id = int(
        re.search(r"/admin/products/(\d+)/edit", page[page.find(product_name) :]).group(
            1
        )
    )
    _, page = request(admin, f"/admin/products/{product_id}/variants")
    _, page = post(
        admin,
        f"/admin/products/{product_id}/variants",
        page,
        sku=fields["sku"] + "-BLACK",
        color="Đen",
        storage="128GB",
        ram="8GB",
        price=234567,
        stock=5,
        active="true",
    )
    assert fields["sku"] + "-BLACK" in page
    variant_form_id = int(re.search(r'name="id"\s+value="(\d+)"', page).group(1))
    version = int(re.search(r'name="version"\s+value="(\d+)"', page).group(1))
    variant_edit = dict(
        id=variant_form_id,
        version=version,
        sku=fields["sku"] + "-BLACK",
        color="Đen",
        storage="128GB",
        ram="8GB",
        price=345678,
        stock=6,
        active="true",
    )
    _, page = post(
        admin, f"/admin/products/{product_id}/variants", page, **variant_edit
    )
    assert "Đã lưu phiên bản" in page
    _, page = post(
        admin, f"/admin/products/{product_id}/variants", page, **variant_edit
    )
    assert "Tồn kho vừa thay đổi" in page
    _, page = request(admin, f"/products/{product_id}")
    uploaded = re.search(r'src="(/uploads/products/[^"]+)"', page).group(1)
    with guest.open(BASE + uploaded) as response:
        assert response.read() == picture
    _, page = request(admin, "/admin/categories")
    _, page = post(admin, f"/admin/categories/{category_id}/delete", page)
    assert "Không thể xóa danh mục đang có sản phẩm" in page
    _, page = request(admin, "/admin/products")
    _, page = post(admin, f"/admin/products/{product_id}/delete", page)
    assert product_name not in page
    _, page = request(admin, "/admin/categories")
    _, page = post(admin, f"/admin/categories/{category_id}/delete", page)
    assert category_name not in page
    _, result = request(
        guest, "/payments/vnpay/ipn?vnp_TxnRef=fake&vnp_ResponseCode=00"
    )
    assert json.loads(result)["RspCode"] == "97"
    print(
        "ADMIN CRUD, VARIANTS, MULTIPART UPLOAD, DATA CONSTRAINTS AND IPN REJECTION OK"
    )
    print("ALL HTTP CHECKS PASSED")
