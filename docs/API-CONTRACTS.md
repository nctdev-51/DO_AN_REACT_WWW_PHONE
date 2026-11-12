# API contracts giữa các module

Mỗi endpoint JSON mới phải có contract ở đây trước khi module frontend phụ thuộc vào nó. Đường dẫn dùng context path của ứng dụng và response luôn là JSON UTF-8.

## Catalog

| Endpoint | Owner | Response | Ghi chú |
| --- | --- | --- | --- |
| `GET /products/search/suggestions?q={text}` | TV1 | `string[]` | Tối đa 8 tên; truy vấn dưới 2 ký tự trả `[]`. |
| `GET /products/popular` | TV1 | redirect | Mở catalog sắp xếp bán chạy. |
| `GET /products/{id}/delivery-estimate?provinceCode={code}&variantId={id}` | Storefront | `{provinceName, fee, freeThreshold, minDays, maxDays, windowLabel, note}` | Trả `400` nếu mã tỉnh không hợp lệ. |
| `POST /compare/api/add`, `/compare/api/remove`, `/compare/api/clear` | Storefront | `{count, limit, items, error?}` | Cập nhật khay so sánh theo phiên kèm CSRF. |
| `POST /checkout/addresses` | Checkout | `{id, fullLine, recipientName, phone, line, provinceCode, wardCode, province, ward, defaultAddress}` | Validate `AddressForm` + `AddressDirectory`, trả `400 {error}` khi sai. |

## Quy tắc API cho module mới

- AI: `/api/ai/...`; Recommendation: `/api/recommendations/...`; Tracking: `/api/tracking/...`.
- API mới cần mô tả status code, request JSON, response JSON, validation và fallback trước khi merge frontend.
- Không thay đổi field đã public. Nếu buộc phải thay đổi, thêm field mới hoặc version endpoint.
- Không đưa secret, raw payment payload, địa chỉ đầy đủ hoặc PII không cần thiết vào response.
