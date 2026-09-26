# Cài đặt database và chạy chức năng sản phẩm

Ứng dụng mặc định chạy ở profile `demo`, dùng H2 trong bộ nhớ và tự tạo dữ liệu mẫu. Không cần cài hoặc chạy MariaDB để xem giao diện. Dữ liệu demo sẽ mất khi dừng ứng dụng.

Chạy ứng dụng từ IntelliJ như bình thường, sau đó mở:

- `http://localhost:8080/` — trang chủ
- `http://localhost:8080/products` — danh sách, tìm kiếm, lọc và phân trang (15 sản phẩm mẫu)
- `http://localhost:8080/admin` — đăng nhập rồi quản lý sản phẩm/danh mục

Tại màn hình Admin đăng nhập bằng `admin` / `Admin123!`. Sau đó mở trang quản lý; dùng nút **Đăng xuất** để kết thúc phiên.

## Chuyển sang MariaDB sau này

Khi đã cài MariaDB, tạo database và user trong MariaDB client hoặc HeidiSQL:

```sql
CREATE USER IF NOT EXISTS 'webshop_app'@'localhost'
  IDENTIFIED BY 'DoiThanhMatKhauManh';
GRANT ALL PRIVILEGES ON webshop_db.* TO 'webshop_app'@'localhost';
```

Tài khoản riêng với mật khẩu dùng password authentication; không nên dùng `root` nếu tài khoản đó xác thực qua socket/GSSAPI. Chọn profile `mariadb` và cấu hình thông tin kết nối.

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mariadb"
$env:DB_USERNAME = "webshop_app"
$env:DB_PASSWORD = "DoiThanhMatKhauManh"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "mat_khau_admin_manh"
.\mvnw.cmd spring-boot:run
```

Trong IntelliJ, đặt các biến trên ở **Run → Edit Configurations → Environment variables**. Có thể thay URL bằng `DB_URL`. Hibernate tự tạo/cập nhật các bảng khi khởi động.

Khi dùng profile MariaDB, dữ liệu mẫu không tự chèn. Sau khi đã tạo các bảng (khởi động ứng dụng một lần), mở file `database/sample-data.sql` trong HeidiSQL, chọn database `webshop_db` rồi chạy. Script thêm 4 danh mục và 15 sản phẩm, đồng thời bỏ qua tên đã tồn tại nên có thể chạy lại an toàn. Có thể thêm dữ liệu khác qua trang Admin.

Ảnh tải lên chỉ nhận PNG/JPEG tối đa 5 MB, được lưu ngoài source code trong `uploads/products` (có thể đổi bằng biến `UPLOAD_DIR`). Xóa sản phẩm đã xuất hiện trong đơn hàng sẽ chuyển sản phẩm sang trạng thái ngừng kinh doanh để giữ lịch sử đơn.
