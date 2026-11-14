# Kiến trúc PTV Mobile

PTV Mobile là modular monolith Spring Boot MVC. Một ứng dụng deployable duy nhất quản lý storefront, admin và các API nội bộ. MariaDB là nguồn dữ liệu giao dịch; các dịch vụ AI, vector và analytics được thêm như capability độc lập, không làm thay đổi ownership của order hoặc catalog.

## Ranh giới module

```text
web MVC and REST controllers
        |
application services by feature
        |
domain entities and repositories
        |
MariaDB transaction boundary
```

Mỗi feature có thể chứa `controller`, `service`, `dto` và `repository` riêng. Chỉ service được phép gọi service của feature khác. Controller và JSP không gọi chéo repository của feature khác.

## Quy tắc dữ liệu

- MariaDB là system of record cho user, product, inventory, order, payment và support.
- Số lượng đã bán, báo cáo và badge bán chạy là dữ liệu dẫn xuất từ order line hợp lệ, không lưu counter trùng trong `products`.
- MongoDB/Milvus, khi được thêm, chỉ là read model/cache cho analytics và recommendation. Nếu unavailable, storefront phải có fallback MariaDB.
- Tất cả thay đổi MariaDB mới đi qua Flyway tại `src/main/resources/db/migration/mariadb`.

## Profiles và triển khai

| Profile | Mục đích | Database | Hibernate | Flyway |
| --- | --- | --- | --- | --- |
| `demo` | Test/demo lặp lại | H2 in-memory | `create-drop` | Tắt |
| `dev` | Phát triển tích hợp | MariaDB local | `validate` | Bật |
| `prod` | Docker/production | MariaDB | `validate` | Bật |

`prod` yêu cầu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `PUBLIC_BASE_URL`. Ứng dụng dừng trước khi khởi tạo nếu biến nào thiếu.

Open Session in View được khai báo rõ là tương thích tạm thời vì JSP hiện còn đọc quan hệ lazy. Mỗi feature mới phải chuyển sang DTO/view model; chỉ tắt `spring.jpa.open-in-view` sau khi tất cả trang đã được chuyển đổi và có smoke test render trang.
