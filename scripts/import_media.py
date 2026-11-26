"""Download original gallery/color photos. Never synthesize missing product views.
Updates catalog colors only for existing seed variant SKUs; order snapshots stay intact.
"""

import json, re, hashlib
from html.parser import HTMLParser
from concurrent.futures import ThreadPoolExecutor
from urllib.parse import urljoin
from import_catalog import ROOT, CATALOG, fetch, sql

TGDD_SOURCES = {
    "oppo-find-x8-pro": ("oppo-find-x8-pro", "oppo-find-x8-pro"),
    "oppo-reno13-pro": ("oppo-reno13-pro-5g", "oppo-reno13-pro-5g"),
    "oppo-reno13": ("oppo-reno13-5g", "oppo-reno13"),
    "oppo-a5-pro-5g": ("oppo-a5-pro-5g", "oppo-a5-pro-5g"),
    "oppo-a60": ("oppo-a60-128gb", "oppo-a60"),
    "xiaomi-redmi-note-14-pro": (
        "xiaomi-redmi-note-14-pro",
        "xiaomi-redmi-note-14-pro",
    ),
    "honor-400": ("honor-400", "honor-400"),
}


def supplement(product):
    """Read exact color/angle photographs from a second public product gallery."""
    slug, prefix = TGDD_SOURCES[product["slug"]]
    page = fetch("https://www.thegioididong.com/dtdd/" + slug)[0].decode("utf-8")
    urls = list(dict.fromkeys(re.findall(r'https[^\s"\'<>]*\.(?:png|jpg|webp)', page)))
    color_names = {
        "black": "Đen",
        "den": "Đen",
        "white": "Trắng",
        "trang": "Trắng",
        "purple": "Tím",
        "tim": "Tím",
        "blue": "Xanh",
        "xanh": "Xanh",
        "gray": "Xám",
        "grey": "Xám",
        "xam": "Xám",
        "brown": "Nâu",
        "nau": "Nâu",
        "pink": "Hồng",
        "hong": "Hồng",
        "vang": "Vàng",
        "den-tim": "Đen tím",
    }
    by_color = {}
    for url in urls:
        name = url.rsplit("/", 1)[-1]
        match = re.match(
            re.escape(prefix)
            + r"-(den-tim|black|den|white|trang|purple|tim|blue|xanh|gray|grey|xam|brown|nau|pink|hong|vang)-(\d+)(?:-|\.)",
            name,
        )
        if match:
            by_color.setdefault(color_names[match[1]], []).append((int(match[2]), url))
    groups = []
    changes = []
    assigned = {}
    if product["slug"] == "oppo-a5-pro-5g" and "Nâu" not in by_color:
        landing = fetch("https://www.oppo.com/vn/smartphones/series-a/a5-pro-5g/")[
            0
        ].decode("utf-8")
        brown = list(
            dict.fromkeys(
                re.findall(
                    r'[^\s"\'<>]*images-color-switch-l5-2-[123]-pc-1\.jpg', landing
                )
            )
        )
        by_color["Nâu"] = [
            (n, urljoin("https://www.oppo.com", url)) for n, url in enumerate(brown)
        ]
    for old in dict.fromkeys(v["color"] for v in product["variants"]):
        color = next((name for name in by_color if name == old), None)
        if not color:
            color = next(
                (name for name in by_color if name not in assigned.values()), None
            )
        if not color:
            continue
        assigned[old] = color
        images = [
            download(url, product["slug"], color, index)
            for index, (_, url) in enumerate(sorted(by_color[color])[:5])
        ]
        groups.append(
            dict(productSku=product["sku"], color=color, kind="PHOTO", images=images)
        )
    for variant in product["variants"]:
        old = variant["color"]
        new = assigned.get(old, old)
        if old != new:
            changes.append(dict(sku=variant["sku"], old=old, new=new))
            variant["color"] = new
    return product, groups, changes


class GalleryPage(HTMLParser):
    def __init__(self):
        super().__init__()
        self.photos = []
        self.colors = []
        self.anchor = None

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "a":
            self.anchor = a
            if "spotlight" in a.get("class", "") and "/media/catalog/product/" in a.get(
                "href", ""
            ):
                self.photos.append(a["href"])
        if (
            tag == "img"
            and self.anchor
            and "button__change-color" in self.anchor.get("class", "")
        ):
            source = a.get("src", "")
            name = self.anchor.get("title", "").strip()
            if name and "/media/catalog/product/" in source:
                source = (
                    "https://cdn2.cellphones.com.vn/700x/media/catalog/product/"
                    + source.split("/media/catalog/product/", 1)[1]
                )
                self.colors.append(
                    {
                        "name": name,
                        "image": source,
                        "page": urljoin(
                            "https://cellphones.com.vn", self.anchor.get("href", "")
                        ),
                    }
                )

    def handle_endtag(self, tag):
        if tag == "a":
            self.anchor = None


def download(source, slug, label, index):
    source = source.replace("/x/media/", "/700x/media/")
    body, mime = fetch(source)
    ext = {"image/png": ".png", "image/jpeg": ".jpg", "image/webp": ".webp"}.get(mime)
    if not ext or len(body) < 2000:
        raise ValueError("Invalid gallery image")
    key = hashlib.sha256(label.encode()).hexdigest()[:8]
    path = (
        ROOT
        / "src/main/resources/static/images/products/gallery"
        / slug
        / f"{key}-{index}{ext}"
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(body)
    return {
        "path": "/images/products/gallery/" + slug + "/" + path.name,
        "source": source,
    }


def collect(product):
    parser = GalleryPage()
    parser.feed(fetch(product["source"])[0].decode("utf-8"))
    groups = []
    changes = []
    colors = list({c["name"]: c for c in parser.colors}.values())
    # Keep two seed color choices, using exact names and images from the source.
    original = list(dict.fromkeys(v["color"] for v in product["variants"]))
    assigned = {}
    for n, old in enumerate(original):
        match = next(
            (
                c
                for c in colors
                if old.casefold() in c["name"].casefold()
                and c["name"] not in assigned.values()
            ),
            None,
        )
        if not match:
            match = next(
                (c for c in colors if c["name"] not in assigned.values()), None
            )
        if not match:
            continue
        assigned[old] = match["name"]
        image = download(match["image"], product["slug"], match["name"], 0)
        groups.append(
            {
                "productSku": product["sku"],
                "color": match["name"],
                "kind": "PHOTO",
                "images": [image],
            }
        )
    general = []
    for n, source in enumerate(list(dict.fromkeys(parser.photos))[:6]):
        try:
            general.append(download(source, product["slug"], "general", n))
        except Exception:
            pass
    if general:
        groups.append(
            {
                "productSku": product["sku"],
                "color": None,
                "kind": "PHOTO",
                "images": general,
            }
        )
    for variant in product["variants"]:
        old = variant["color"]
        new = assigned.get(old, old)
        if new != old:
            changes.append({"sku": variant["sku"], "old": old, "new": new})
            variant["color"] = new
    if product["slug"] in TGDD_SOURCES and any(
        not any(g["color"] == v["color"] for g in groups) for v in product["variants"]
    ):
        product, extra, updated = supplement(product)
        colors = {g["color"] for g in extra}
        groups = [g for g in groups if g["color"] not in colors] + extra
        changes.extend(updated)
    return product, groups, changes


if __name__ == "__main__":
    data = json.loads(CATALOG.read_text(encoding="utf-8"))
    media = []
    changes = []
    media_file = ROOT / "src/main/resources/catalog/media.json"
    previous = (
        json.loads(media_file.read_text(encoding="utf-8"))
        if media_file.exists()
        else []
    )
    phone_skus = {p["sku"] for p in data if p.get("kind", "PHONE") == "PHONE"}
    # This importer understands phone galleries. Keep other product media intact.
    media.extend(
        g
        for g in previous
        if g["productSku"] not in phone_skus and g["kind"] == "PHOTO"
    )
    with ThreadPoolExecutor(max_workers=4) as pool:
        futures = [(p, pool.submit(collect, p)) for p in data if p["sku"] in phone_skus]
        for product, future in futures:
            try:
                _, groups, updated = future.result()
                media.extend(groups)
                changes.extend(updated)
                print("OK", product["slug"], len(groups), flush=True)
            except Exception as error:
                media.extend(
                    g
                    for g in previous
                    if g["productSku"] == product["sku"] and g["kind"] == "PHOTO"
                )
                print("RETAINED", product["slug"], str(error), flush=True)
    CATALOG.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    if media_file.exists():
        media.extend(
            g
            for g in json.loads(media_file.read_text(encoding="utf-8"))
            if g.get("kind") != "PHOTO"
        )
    media_file.write_text(
        json.dumps(media, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    statements = ["USE webshop_db;", "SET NAMES utf8mb4;", "START TRANSACTION;"]
    for change in changes:
        statements.append(
            f"UPDATE product_variants SET color={sql(change['new'])},version=version+1 WHERE sku={sql(change['sku'])} AND color={sql(change['old'])};"
        )
    statements.append("COMMIT;")
    (ROOT / ".local-backups").mkdir(exist_ok=True)
    (ROOT / ".local-backups/update-media-colors.sql").write_text(
        "\n".join(statements), encoding="utf-8"
    )
    print(
        "Media:",
        sum(len(g["images"]) for g in media),
        "photos;",
        len(changes),
        "color names updated",
    )
