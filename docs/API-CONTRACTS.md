# API contracts giữa các module

Mỗi endpoint JSON mới phải có contract ở đây trước khi module frontend phụ thuộc vào nó. Đường dẫn dùng context path của ứng dụng và response luôn là JSON UTF-8.

## Catalog

| Endpoint | Owner | Response | Ghi chú |
| --- | --- | --- | --- |
| `GET /products/search/suggestions?q={text}` | TV1 | `string[]` | Tối đa 8 tên; truy vấn dưới 2 ký tự trả `[]`. |
| `GET /products/popular` | TV1 | redirect | Mở catalog sắp xếp bán chạy. |

## Quy tắc API cho module mới

- AI: `/api/ai/...`; Recommendation: `/api/recommendations/...`; Tracking: `/api/tracking/...`.
- API mới cần mô tả status code, request JSON, response JSON, validation và fallback trước khi merge frontend.
- Không thay đổi field đã public. Nếu buộc phải thay đổi, thêm field mới hoặc version endpoint.
- Không đưa secret, raw payment payload, địa chỉ đầy đủ hoặc PII không cần thiết vào response.
