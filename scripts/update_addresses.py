"""Refresh the public two-level address snapshot; checkout never sends addresses to this API."""

import json
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SOURCE = "https://provinces.open-api.vn/api/v2/?depth=2"
ROOT = Path(__file__).resolve().parents[1]
request = urllib.request.Request(
    SOURCE, headers={"User-Agent": "PTVMobile-address-catalog/1.0"}
)
with urllib.request.urlopen(request, timeout=45) as response:
    data = json.load(response)
provinces = []
codes = set()
for province in data:
    wards = []
    for ward in province.get("wards") or []:
        code = str(ward["code"]).zfill(5)
        if code in codes:
            raise ValueError("Duplicate ward code")
        codes.add(code)
        wards.append({"code": code, "name": ward["name"]})
    provinces.append(
        {
            "code": str(province["code"]).zfill(2),
            "name": province["name"],
            "wards": wards,
        }
    )
if len(provinces) < 30 or len(codes) < 3000:
    raise ValueError("Incomplete address response; previous snapshot retained")
target = ROOT / "src/main/resources/data/vietnam-addresses.json"
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(
    json.dumps(
        {
            "source": SOURCE,
            "fetchedAt": datetime.now(timezone.utc).isoformat(),
            "provinces": provinces,
        },
        ensure_ascii=False,
        indent=2,
    )
    + "\n",
    encoding="utf-8",
)
print(f"Saved {len(provinces)} provinces and {len(codes)} wards")
