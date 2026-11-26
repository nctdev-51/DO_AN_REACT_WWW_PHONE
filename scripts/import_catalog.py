"""Refresh local images/specifications and build database/sample-data.sql from the catalog.
Prices, colors and stock are illustrative seed data; specifications retain their source URLs.
Run with --download to fetch public source pages, or without it to regenerate SQL offline.
"""

import argparse
import html
import json
import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
from html.parser import HTMLParser
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "src/main/resources/catalog/products.json"
EXTRA = [
    ("iPhone 17 256GB", "Apple", "iphone-17", 24990000),
    ("iPhone 17 Pro 256GB", "Apple", "iphone-17-pro", 33990000),
    ("iPhone 17 Pro Max 256GB", "Apple", "iphone-17-pro-max", 36990000),
    ("iPhone Air 256GB", "Apple", "iphone-air", 28990000),
    ("Galaxy Z Fold7 256GB", "Samsung", "samsung-galaxy-z-fold-7", 39990000),
    ("Galaxy Z Flip7 256GB", "Samsung", "samsung-galaxy-z-flip-7", 25990000),
    ("Redmi Note 14 Pro 256GB", "Xiaomi", "xiaomi-redmi-note-14-pro", 7990000),
    ("POCO X7 Pro 256GB", "Xiaomi", "xiaomi-poco-x7-pro", 8990000),
    ("HONOR 400 256GB", "HONOR", "honor-400", 10990000),
    ("iPhone 13 128GB", "Apple", "iphone-13", 10990000),
    ("iPhone 14 128GB", "Apple", "iphone-14", 13990000),
    ("iPhone 14 Plus 128GB", "Apple", "iphone-14-plus", 15990000),
    ("iPhone 15 Plus 128GB", "Apple", "iphone-15-plus", 19990000),
    ("iPhone 15 Pro 128GB", "Apple", "iphone-15-pro", 22990000),
    ("iPhone 15 Pro Max 256GB", "Apple", "iphone-15-pro-max", 26990000),
    ("iPhone 16 Plus 128GB", "Apple", "iphone-16-plus", 25990000),
    ("Galaxy S24 Ultra 256GB", "Samsung", "samsung-galaxy-s24-ultra", 24990000),
    ("Galaxy S24 FE 128GB", "Samsung", "samsung-galaxy-s24-fe", 12990000),
    ("Galaxy A26 5G 128GB", "Samsung", "samsung-galaxy-a26", 6990000),
    ("Galaxy A36 5G 128GB", "Samsung", "samsung-galaxy-a36", 8990000),
    ("Galaxy Z Flip6 256GB", "Samsung", "samsung-galaxy-z-flip-6", 22990000),
    ("Galaxy Z Fold6 256GB", "Samsung", "samsung-galaxy-z-fold-6", 35990000),
    ("Xiaomi 14T 256GB", "Xiaomi", "xiaomi-14t", 11990000),
    ("Xiaomi 14T Pro 512GB", "Xiaomi", "xiaomi-14t-pro", 15990000),
    ("Redmi Note 13 128GB", "Xiaomi", "xiaomi-redmi-note-13", 3990000),
    ("Redmi Note 13 Pro 256GB", "Xiaomi", "xiaomi-redmi-note-13-pro", 6490000),
    ("OPPO Reno12 5G 256GB", "OPPO", "oppo-reno12", 9990000),
    ("OPPO Reno13 5G 256GB", "OPPO", "oppo-reno13", 12990000),
    ("OPPO A3 128GB", "OPPO", "oppo-a3", 3990000),
    ("OPPO A60 128GB", "OPPO", "oppo-a60", 4990000),
    ("OPPO Find X8 256GB", "OPPO", "oppo-find-x8", 22990000),
]

SOURCE_OVERRIDES = {
    "iphone-17": "https://cellphones.com.vn/iphone-17-256gb.html",
    "xiaomi-poco-x7-pro": "https://cellphones.com.vn/dien-thoai-poco-x7-pro-5g.html",
    "honor-400": "https://cellphones.com.vn/dien-thoai-honor-400-5g.html",
}


class Page(HTMLParser):
    def __init__(self):
        super().__init__()
        self.meta = {}
        self.scripts = []
        self.current = None

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "meta":
            self.meta[attrs.get("property", attrs.get("name"))] = attrs.get(
                "content", ""
            )
        if tag == "script" and attrs.get("type") == "application/ld+json":
            self.current = []

    def handle_data(self, data):
        if self.current is not None:
            self.current.append(data)

    def handle_endtag(self, tag):
        if tag == "script" and self.current is not None:
            try:
                self.scripts.append(json.loads("".join(self.current)))
            except json.JSONDecodeError:
                pass
            self.current = None


def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(request, timeout=35) as response:
        return response.read(), response.headers.get_content_type()


def find_product(node):
    if isinstance(node, list):
        for item in node:
            result = find_product(item)
            if result:
                return result
    if isinstance(node, dict):
        if node.get("@type") == "Product":
            return node
        for item in node.values():
            result = find_product(item)
            if result:
                return result
    return None


def refresh(product):
    candidates = [product["source"], product["source"].replace("/dien-thoai-", "/")]
    if "oppo-reno12" in product["slug"]:
        candidates += [
            "https://cellphones.com.vn/oppo-reno12-5g.html",
            "https://cellphones.com.vn/dien-thoai-oppo-reno12-5g.html",
        ]
    if "oppo-reno13-f" in product["slug"]:
        candidates += ["https://cellphones.com.vn/dien-thoai-oppo-reno13-f.html"]
    for candidate in dict.fromkeys(candidates):
        try:
            body, _ = fetch(candidate)
            product["source"] = candidate
            break
        except urllib.error.HTTPError as error:
            if error.code != 404:
                raise
    else:
        raise ValueError("No source page found")
    page = Page()
    page.feed(body.decode("utf-8"))
    structured = find_product(page.scripts) or {}
    specs = {
        p["name"]: html.unescape(str(p["value"])).strip()
        for p in structured.get("additionalProperty", [])
        if "name" in p and "value" in p
    }
    image_url = page.meta.get("og:image", "")
    if not image_url.startswith("https://") or "logo" in image_url.lower():
        raise ValueError("Missing product image")
    image, mime = fetch(image_url)
    ext = {"image/png": ".png", "image/jpeg": ".jpg", "image/webp": ".webp"}[mime]
    if len(image) < 2000:
        raise ValueError("Invalid image")
    slug = product["slug"]
    path = ROOT / "src/main/resources/static/images/products" / (slug + ext)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(image)
    product.update(
        image="/images/products/" + path.name,
        imageSource=image_url,
        sourceSpecifications=specs,
        description=html.unescape(
            structured.get("description") or page.meta.get("description", "")
        )[:2000],
        fetchedAt=datetime.now(timezone.utc).isoformat(),
    )
    return product


def normalize(product, index):
    if product.get("kind") == "ACCESSORY":
        return product
    product.setdefault("legacyName", product["name"])
    product["name"] = re.sub(r"\s+\d+GB$", "", product["legacyName"])
    specs = product.get("sourceSpecifications", {})

    def spec(*keys):
        return next((specs[k] for k in keys if specs.get(k)), "")

    product["sku"] = product.get("sku") or "PS-" + product["slug"].upper()
    product["chipset"] = spec("Chipset")
    product["camera"] = spec("Camera sau")[:200].strip()
    product["display"] = " · ".join(
        filter(None, [spec("Kích thước màn hình"), spec("Công nghệ màn hình")])
    )[:200].strip()
    product["battery"] = spec("Pin", "Dung lượng pin")[:120].strip()
    product["operatingSystem"] = spec("Hệ điều hành khi ra mắt", "Hệ điều hành")[
        :100
    ].strip()
    product["warrantyMonths"] = 12
    product.setdefault("sourceDescription", product.get("description", ""))
    highlights = [
        (label, product.get(key))
        for label, key in [
            ("Màn hình", "display"),
            ("Chip xử lý", "chipset"),
            ("Camera sau", "camera"),
            ("Pin", "battery"),
            ("Hệ điều hành", "operatingSystem"),
        ]
        if product.get(key)
    ]
    product["description"] = (
        product["name"]
        + " thuộc dòng điện thoại "
        + product["brand"]
        + ".\n\n"
        + "\n".join(label + ": " + value for label, value in highlights)
    )
    product["originalPrice"] = int(product["price"] * 1.12) // 10000 * 10000
    product["priceType"] = (
        "Illustrative VND prices, colors and inventory for local coursework; not current retail offers"
    )
    capacity = re.search(r"(\d+)\s*GB", product["legacyName"])
    storage = (
        (capacity.group(1) + "GB")
        if capacity
        else (spec("Bộ nhớ trong").replace(" ", "") or "256GB")
    )
    ram = spec("Dung lượng RAM").replace(" ", "")
    colors = ["Đen", "Xanh"] if product["brand"] != "Apple" else ["Đen", "Trắng"]
    capacities = [storage]
    size = re.search(r"^(\d+)GB$", storage)
    if size and int(size.group(1)) < 512:
        capacities.append(str(int(size.group(1)) * 2) + "GB")
    product["variants"] = product.get("variants") or [
        dict(
            sku=product["sku"] + "-" + capacity + "-" + str(i + 1),
            color=color,
            storage=capacity,
            ram=ram,
            price=product["price"] + (i * 200000) + (j * 2000000),
            stock=(index * 7 + i * 3 + j * 5) % 27 + 5,
        )
        for j, capacity in enumerate(capacities)
        for i, color in enumerate(colors)
    ]
    # Existing catalog entries are retained by name so existing order references remain valid.
    return product


def sql(value):
    if value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def generate(products):
    lines = [
        "-- Sample prices and inventory. Safe to rerun: never reset existing stock or customer data.",
        "SET NAMES utf8mb4;",
        "USE webshop_db;",
        "START TRANSACTION;",
        "UPDATE order_details d JOIN products p ON d.product_id=p.id SET d.product_name=p.name WHERE d.product_name IS NULL;",
    ]
    for role in ["ADMIN", "USER"]:
        lines.append(
            f"INSERT INTO roles (name) SELECT {sql(role)} WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name={sql(role)});"
        )
    lines.append(
        "INSERT INTO store_policy (id,version,shipping_fee,free_shipping_threshold,return_window_days) SELECT 1,0,30000,2000000,7 WHERE NOT EXISTS (SELECT 1 FROM store_policy WHERE id=1);"
    )
    for promotion in json.loads(
        (ROOT / "src/main/resources/catalog/promotions.json").read_text(
            encoding="utf-8"
        )
    ):
        code = promotion["code"]
        lines.append(
            "INSERT INTO promotions (version,code,description,discount_type,amount,minimum_subtotal,maximum_discount,scope_brand,scope_skus,required_skus,starts_at,ends_at,usage_limit,active) SELECT 0,"
            + ",".join(
                sql(promotion[key])
                for key in [
                    "code",
                    "description",
                    "discountType",
                    "amount",
                    "minimumSubtotal",
                    "maximumDiscount",
                ]
            )
            + ","
            + ",".join(
                sql(promotion.get(k))
                for k in ["scopeBrand", "scopeSkus", "requiredSkus"]
            )
            + f",CURRENT_TIMESTAMP - INTERVAL 1 DAY,CURRENT_TIMESTAMP + INTERVAL {int(promotion['validDays'])} DAY,{int(promotion['usageLimit'])},1 WHERE NOT EXISTS (SELECT 1 FROM promotions WHERE code="
            + sql(code)
            + ");"
        )
    lines.append(
        "INSERT INTO store_experience (id,version,headline,introduction,hero_sku,second_hero_sku,featured_skus,support_email,hotline,store_address,loyalty_enabled,earn_every,point_value,redeem_percent,delivery_min_days,delivery_max_days) SELECT 1,0,'Chọn công nghệ. Theo cách của bạn.','Khám phá điện thoại, so sánh cấu hình và tìm lựa chọn vừa ngân sách.','PS-IPHONE-17-PRO-MAX','PS-SAMSUNG-GALAXY-S25-ULTRA','','','','',1,100000,1000,10,2,5 WHERE NOT EXISTS (SELECT 1 FROM store_experience WHERE id=1);"
    )
    for brand in dict.fromkeys(p.get("category", p["brand"]) for p in products):
        lines.append(
            f"INSERT INTO categories (name,description) SELECT {sql(brand)},{sql('Điện thoại '+brand+' chính hãng')} WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name={sql(brand)});"
        )
    columns = [
        "usage_tags",
        "kind",
        "compatible_skus",
        "name",
        "brand",
        "sku",
        "image",
        "price",
        "original_price",
        "description",
        "chipset",
        "display",
        "camera",
        "battery",
        "operating_system",
        "warranty_months",
        "status",
        "category_id",
    ]
    for product in products:
        category = f"(SELECT id FROM categories WHERE name={sql(product.get('category', product['brand']))} ORDER BY id LIMIT 1)"
        values = [
            sql(product.get(k))
            for k in [
                "name",
                "brand",
                "sku",
                "image",
                "price",
                "originalPrice",
                "description",
                "chipset",
                "display",
                "camera",
                "battery",
                "operatingSystem",
                "warrantyMonths",
            ]
        ] + ["1", category]
        values = [
            sql(product.get("usageTags", "")),
            sql(product.get("kind", "PHONE")),
            sql(product.get("compatibleSkus")),
        ] + values
        match = f"(sku={sql(product['sku'])} OR name IN ({sql(product['name'])},{sql(product['legacyName'])}))"
        lines.append(
            "INSERT INTO products ("
            + ",".join(columns)
            + ") SELECT "
            + ",".join(values)
            + f" WHERE NOT EXISTS (SELECT 1 FROM products WHERE {match});"
        )
        updates = ",".join(
            (
                f"{column}=COALESCE({column},{sql(product.get(key))})"
                if column == "usage_tags"
                else f"{column}=COALESCE(NULLIF({column},''),{sql(product.get(key))})"
            )
            for column, key in [
                ("usage_tags", "usageTags"),
                ("sku", "sku"),
                ("image", "image"),
                ("chipset", "chipset"),
                ("display", "display"),
                ("camera", "camera"),
                ("battery", "battery"),
                ("operating_system", "operatingSystem"),
            ]
        )
        description = f"description=CASE WHEN sku IS NULL OR description IS NULL OR description='' OR description LIKE {sql(product['legacyName']+' thuộc dòng điện thoại%')} THEN {sql(product['description'])} ELSE description END,"
        name = f"name=CASE WHEN name={sql(product['legacyName'])} THEN {sql(product['name'])} ELSE name END,"
        lines.append(
            f'UPDATE products SET {name}{description}{updates},warranty_months=COALESCE(warranty_months,12),original_price=COALESCE(original_price,{sql(product["originalPrice"])}) WHERE {match};'
        )
        for variant in product["variants"]:
            values = (
                [f"(SELECT id FROM products WHERE {match} ORDER BY id LIMIT 1)"]
                + [
                    sql(variant[k])
                    for k in ["sku", "color", "storage", "ram", "price", "stock"]
                ]
                + ["1"]
            )
            lines.append(
                "INSERT INTO product_variants (product_id,sku,color,storage_capacity,ram,price,stock,active,version) SELECT "
                + ",".join(values)
                + f',0 WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE sku={sql(variant["sku"])});'
            )
    media_file = ROOT / "src/main/resources/catalog/media.json"
    if media_file.exists():
        for group in json.loads(media_file.read_text(encoding="utf-8")):
            product_id = f"(SELECT id FROM products WHERE sku={sql(group['productSku'])} LIMIT 1)"
            for position, image in enumerate(group["images"]):
                values = [
                    product_id,
                    sql(group.get("color")),
                    sql(group["kind"]),
                    sql(image["path"]),
                    sql(group.get("credit") or group.get("color")),
                    sql(image.get("source")),
                    str(position),
                ]
                lines.append(
                    "INSERT INTO product_media (product_id,color,kind,path,caption,source,sort_order) SELECT "
                    + ",".join(values)
                    + f" WHERE {product_id} IS NOT NULL AND NOT EXISTS (SELECT 1 FROM product_media WHERE product_id={product_id} AND path={sql(image['path'])});"
                )
    lines.append("COMMIT;")
    (ROOT / "database/sample-data.sql").write_text(
        "\n".join(lines) + "\n", encoding="utf-8"
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--download", action="store_true")
    parser.add_argument(
        "--download-missing",
        action="store_true",
        help="Only fetch products without an existing local image",
    )
    args = parser.parse_args()
    products = json.loads(CATALOG.read_text(encoding="utf-8"))
    for p in products:
        p.setdefault("slug", Path(p["image"]).stem)
    known = {p.get("legacyName", p["name"]) for p in products}
    for name, brand, slug, price in EXTRA:
        if name not in known:
            products.append(
                dict(
                    name=name,
                    brand=brand,
                    slug=slug,
                    price=price,
                    source=SOURCE_OVERRIDES.get(slug)
                    or "https://cellphones.com.vn/"
                    + ("" if slug.startswith("iphone") else "dien-thoai-")
                    + slug
                    + ".html",
                )
            )
    if args.download or args.download_missing:
        refreshed = []
        failed = []
        with ThreadPoolExecutor(max_workers=4) as pool:
            futures = [
                (p, pool.submit(refresh, p))
                for p in products
                if not (
                    args.download_missing
                    and p.get("image")
                    and (
                        ROOT / "src/main/resources/static" / p["image"].lstrip("/")
                    ).is_file()
                )
            ]
            fetched_slugs = {p["slug"] for p, _ in futures}
            refreshed.extend(p for p in products if p["slug"] not in fetched_slugs)
            for p, future in futures:
                try:
                    refreshed.append(future.result())
                    print("OK", p["slug"])
                except Exception as error:
                    failed.append(p["slug"])
                    print("FAILED", p["slug"], error)
                    if p.get("image"):
                        refreshed.append(p)
        products = refreshed
        if failed:
            print("Source pages needing review:", ", ".join(failed))
    products = [normalize(p, i) for i, p in enumerate(products)]
    CATALOG.write_text(
        json.dumps(products, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    generate(products)
    print(
        "Catalog:",
        len(products),
        "products;",
        sum(len(p["variants"]) for p in products),
        "variants",
    )
