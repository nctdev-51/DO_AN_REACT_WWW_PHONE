"""Real HTTP verification/reset flow using the local .eml outbox. Creates one fixture customer."""

import json
import urllib.parse
import uuid
from concurrent.futures import ThreadPoolExecutor
from threading import Barrier
from smoke_test import ROOT, account_link, client, login, post, request


def raw(link):
    return urllib.parse.parse_qs(urllib.parse.urlsplit(link).query)["token"][0]


if __name__ == "__main__":
    browser = client()
    email = "auth-smoke-" + uuid.uuid4().hex[:10] + "@example.com"
    password = "Old-" + uuid.uuid4().hex
    new_password = "New-" + uuid.uuid4().hex
    (ROOT / "target/auth-smoke-state.json").write_text(
        json.dumps({"email": email}), encoding="utf-8"
    )
    _, page = request(browser, "/register")
    path, page = post(
        browser,
        "/register",
        page,
        fullName="Auth HTTP Test",
        email=email,
        password=password,
        confirmPassword=password,
    )
    assert path == "/register?success", path
    _, page = request(browser, "/login")
    path, page = post(browser, "/login", page, username=email, password=password)
    assert path == "/login?error", path
    verification = account_link(email, "/verify-email")
    _, page = request(browser, verification)
    path, page = post(browser, "/verify-email", page, token=raw(verification))
    assert path == "/login?verified", path
    _, page = request(browser, verification)
    assert 'name="token"' not in page
    login(browser, email, password)
    recovery = client()
    _, page = request(recovery, "/forgot-password")
    path, page = post(recovery, "/forgot-password", page, email=email)
    assert path == "/forgot-password?sent", path
    reset = account_link(email, "/reset-password")
    _, page = request(recovery, reset)
    _, page = post(
        recovery,
        "/reset-password",
        page,
        token=raw(reset),
        password=new_password,
        confirmation="mismatch",
    )
    assert 'name="token"' in page
    competitor = client()
    _, other_page = request(competitor, reset)
    barrier = Barrier(2)

    def reset_once(args):
        browser_client, form = args
        barrier.wait(timeout=10)
        return post(
            browser_client,
            "/reset-password",
            form,
            token=raw(reset),
            password=new_password,
            confirmation=new_password,
        )[0]

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(
            pool.map(reset_once, [(recovery, page), (competitor, other_page)])
        )
    assert results.count("/login?reset") == 1, results
    assert results.count("/reset-password") == 1, results
    path, page = request(browser, "/account")
    assert path == "/login?expired", path
    _, page = request(recovery, "/login")
    path, page = post(recovery, "/login", page, username=email, password=password)
    assert path == "/login?error", path
    login(recovery, email, new_password)
    _, page = request(recovery, reset)
    assert 'name="token"' not in page
    print(
        "AUTH HTTP PASS: registration, verification, concurrent one-use reset, replay rejection, old password rejected, sessions expired"
    )
