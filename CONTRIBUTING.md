# Quy ước phát triển PTV Mobile

## Cách làm việc

- Không push trực tiếp vào `main` hoặc `develop`. Mỗi tính năng bắt đầu bằng nhánh `feature/<module>-<muc-tieu>` và kết thúc bằng pull request.
- Một PR chỉ thuộc một module, không gộp refactor hàng loạt với tính năng mới.
- Trước khi mở PR, chạy `./mvnw test`. Nếu thay đổi DB, chạy thêm ứng dụng với MariaDB và kiểm tra Flyway history.
- Không commit `.env`, mật khẩu, token thanh toán, mail credential, dữ liệu khách hàng hay thư mục `uploads/`.

## Cấu trúc module

Mã hiện tại là modular monolith. Giữ package hiện hữu trong lúc phát triển; mã mới phải nằm trong feature sở hữu nó.

| Module | Chủ sở hữu | Phạm vi chính | File chia sẻ cần phối hợp |
| --- | --- | --- | --- |
| Catalog | TV1 | `catalog`, `ProductController`, catalog JSP/JS/CSS | `ProductRepository`, `productCard.tag` |
| AI | TV2 | `service.ai`, `controller.api.ai` | `detail.jsp`, `footer.jsp`, profile config |
| Recommendation | TV3 | `service.vector`, `controller.api.recommendations` | `detail.jsp`, dependency/vector config |
| Tracking | TV4 | `service.tracking`, `controller.api.tracking`, analytics | `footer.jsp`, admin navigation |
| Shell and infrastructure | TV5 | Docker, dependency, common tags/styles | `pom.xml`, shared header/footer, profiles |

Không sửa file chia sẻ của module khác trong PR nếu chưa thống nhất API và người chịu trách nhiệm merge.

## Quy ước code

- Controller chỉ nhận HTTP request, validate đầu vào và gọi service; không đặt nghiệp vụ hay transaction trong controller mới.
- Service sở hữu use case và transaction. Repository chỉ chứa truy vấn/persistence.
- API JSON nằm dưới `/api/<feature>/...`; trả DTO, không trả JPA entity trực tiếp.
- JSP chỉ đọc model/view model. Không thêm truy vấn repository, script inline hoặc logic nghiệp vụ vào JSP.
- Mọi POST giữ CSRF. Dữ liệu người dùng hiển thị trong JSP dùng `<c:out>`.
- Mỗi API mới phải ghi vào `docs/API-CONTRACTS.md` trước khi frontend khác dùng nó.

## Kiểm thử và chất lượng

- Viết test cho use case hoặc endpoint mới, gồm trường hợp sai/biên.
- Không sửa một test đang fail để “qua build” nếu chưa xác định nguyên nhân.
- Dùng `.editorconfig`; Java 17, UTF-8, hai spaces theo format hiện tại.
- `demo` là profile H2 cho test/demo; `dev` dùng MariaDB + Flyway; `prod` chỉ chạy khi đủ biến môi trường.
