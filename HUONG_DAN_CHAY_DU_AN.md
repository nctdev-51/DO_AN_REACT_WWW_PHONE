# HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY DỰ ÁN PTV MOBILE
> **Đề tài:** Website Kinh Doanh Điện Thoại & Thiết Bị Di Động Thông Minh  
> **Công nghệ:** Java 17, Spring Boot 3, Spring MVC, JSP, Spring Security, MariaDB, MongoDB.

---

## 📋 MỤC LỤC
1. [Yêu cầu hệ thống (Prerequisites)](#1-yêu-cầu-hệ-thống-prerequisites)
2. [Cách 1: Chạy bằng Docker Compose (Khuyên dùng - Nhanh & Đầy đủ nhất)](#2-cách-1-chạy-bằng-docker-compose-khuyên-dùng)
3. [Cách 2: Chạy với MariaDB cài cục bộ (Local)](#3-cách-2-chạy-với-mariadb-cài-cục-bộ-local)
4. [Cách 3: Chạy chế độ Demo (H2 Database - Không cần cài DB)](#4-cách-3-chạy-chế-độ-demo-h2-database)
5. [Thông tin tài khoản & Đường dẫn kiểm thử](#5-thông-tin-tài-khoản--đường-dẫn-kiểm-thử)
6. [Xử lý các lỗi thường gặp (Troubleshooting)](#6-xử-lý-các-lỗi-thường-gặp-troubleshooting)

---

## 1. Yêu cầu hệ thống (Prerequisites)

Trước khi khởi chạy dự án, hãy đảm bảo máy tính đã cài đặt:
- **Java Development Kit (JDK):** Phiên bản **Java 17** trở lên.
- **Docker Desktop** (khuyên dùng để bật database nhanh chóng).
- Nếu không dùng Docker: Cần cài đặt **MariaDB** (phiên bản 10.6+) và **MongoDB** (phiên bản 6.0+).
- Công cụ dòng lệnh: **PowerShell** (Windows) hoặc **Terminal** (macOS/Linux).

---

## 2. Cách 1: Chạy bằng Docker Compose (Khuyên dùng)

Đây là cách đơn giản và đồng bộ nhất giữa các thành viên, chỉ cần Docker khởi chạy toàn bộ Database (MariaDB + MongoDB).

### Bước 1: Chuẩn bị file cấu hình môi trường `.env`
Mở PowerShell tại thư mục gốc của dự án và chạy lệnh sau để tạo file `.env`:

```powershell
Copy-Item .env.example .env
```

*(Nếu dùng macOS/Linux, dùng lệnh: `cp .env.example .env`)*

### Bước 2: Khởi động cơ sở dữ liệu qua Docker
Chạy lệnh sau để bật đồng thời **MariaDB** (cổng `3306`) và **MongoDB** (cổng `27017`):

```powershell
docker compose up -d mariadb mongodb
```

> **Ghi chú:** 
> - MariaDB dùng để lưu trữ dữ liệu sản phẩm, đơn hàng, tài khoản.
> - MongoDB dùng để lưu log sự kiện người dùng (`PAGE_VIEW`, `PRODUCT_CLICK`), giúp website chuyển trang tức thì không bị nghẽn luồng.

### Bước 3: Khởi động ứng dụng Spring Boot
Chạy lệnh khởi chạy dự án:

- **Trên Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **Trên macOS / Linux:**
  ```bash
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

### Bước 4: Truy cập ứng dụng
Khi terminal xuất hiện dòng chữ:
```
Tomcat started on port 8080 (http) with context path ''
```
Mở trình duyệt web và truy cập vào: **[http://localhost:8080](http://localhost:8080)**

---

## 3. Cách 2: Chạy với MariaDB cài cục bộ (Local)

Nếu máy tính của bạn đã cài sẵn MariaDB cục bộ (không muốn dùng Docker):

### Bước 1: Khởi động dịch vụ MariaDB
- Mở **Services** (`services.msc`), tìm dịch vụ **MariaDB** và bấm **Start**.
- Hoặc mở CMD/PowerShell quyền Admin chạy: `net start MariaDB`.

### Bước 2: Thiết lập Database và Tài khoản
Dự án có sẵn script thiết lập nhanh:
```powershell
.\scripts\setup-local.ps1
```
*(Script sẽ hỏi mật khẩu root của MariaDB, tự động tạo cơ sở dữ liệu `webshop_db`, user `webshop_app` và tạo file cấu hình kết nối).*

Hoặc cấu hình mật khẩu MariaDB của bạn trực tiếp trong file:
`src/main/resources/application-dev.properties`
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/webshop_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=mat_khau_cua_ban
```

### Bước 3: Khởi chạy dự án
```powershell
.\mvnw.cmd spring-boot:run
```
Flyway sẽ tự động chạy migration tạo toàn bộ bảng trong database.

---

## 4. Cách 3: Chạy chế độ Demo (H2 Database)

Phù hợp cho việc **chấm thi, demo nhanh** hoặc khi chạy trên máy lạ chưa cài đặt cơ sở dữ liệu. Toàn bộ dữ liệu sẽ chạy trên RAM (In-Memory H2 Database) kèm sẵn dữ liệu mẫu.

Chạy lệnh duy nhất sau:
```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```
*Lưu ý: Dữ liệu ở chế độ này sẽ được tự động làm mới mỗi khi khởi động lại ứng dụng.*

---

## 5. Thông tin tài khoản & Đường dẫn kiểm thử

### 🔗 Các trang chính:
| Chức năng | Đường dẫn URL | Mô tả |
| :--- | :--- | :--- |
| **Trang chủ** | `http://localhost:8080/` | Banner chiến dịch, danh mục nổi bật, sản phẩm mới |
| **Danh mục sản phẩm** | `http://localhost:8080/products` | Bộ lọc nâng cao, thanh trượt giá **noUiSlider**, sắp xếp |
| **So sánh điện thoại** | `http://localhost:8080/compare` | Khay so sánh nhanh 2–3 mẫu máy |
| **Ưu đãi & Voucher** | `http://localhost:8080/offers` | Danh sách mã giảm giá, chính sách freeship |
| **Đăng nhập** | `http://localhost:8080/login` | Cổng xác thực khách hàng & quản trị viên |
| **Đăng ký** | `http://localhost:8080/register` | Đăng ký thành viên mới |
| **Trang Quản trị (Admin)**| `http://localhost:8080/admin` | Quản lý sản phẩm, đơn hàng, khuyến mãi, người dùng |

### 🔑 Thông tin tài khoản mặc định:
- **Tài khoản Quản trị viên (ADMIN):**
  - Tên đăng nhập: `admin` (hoặc `admin@example.com`)
  - Mật khẩu: `ChangeMe123!`
- **Tài khoản Khách hàng (CUSTOMER):**
  - Bạn có thể đăng ký tài khoản mới trực tiếp tại trang `/register` hoặc sử dụng tài khoản có sẵn trong cơ sở dữ liệu.

---

## 6. Xử lý các lỗi thường gặp (Troubleshooting)

### ❓ 1. Lỗi: `required variable DB_PASSWORD is missing a value: Set DB_PASSWORD in .env`
- **Nguyên nhân:** Chưa tạo file `.env` trước khi chạy lệnh `docker compose`.
- **Cách sửa:** Chạy lệnh `Copy-Item .env.example .env` trong PowerShell rồi mới chạy Docker.

### ❓ 2. Chuyển trang từ trang này sang trang khác bị lâu (chờ từ 5 - 30 giây)
- **Nguyên nhân:** Container MongoDB chưa được bật, dẫn đến tính năng ghi log tracking `PAGE_VIEW` bị timeout kết nối.
- **Cách sửa:** Chạy lệnh:
  ```powershell
  docker compose up -d mongodb
  ```

### ❓ 3. Lỗi: `Port 8080 is already in use`
- **Nguyên nhân:** Có tiến trình khác hoặc một cửa sổ terminal trước đó đang chiếm dụng cổng 8080.
- **Cách sửa:**
  - Tìm và tắt tiến trình:
    ```powershell
    Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force
    ```
  - Hoặc đổi sang cổng khác bằng cách thêm tham số khi chạy:
    ```powershell
    .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
    ```

### ❓ 4. Lỗi: `Access Denied` hoặc kết nối MariaDB bị từ chối
- **Cách sửa:** Kiểm tra lại xem container MariaDB trong Docker đã hiển thị trạng thái `Up` (hoặc Service MariaDB trong Windows đã `Running`) hay chưa.
  ```powershell
  docker ps
  ```

---
*Tài liệu được cập nhật bởi nhóm dự án PTV Mobile.*
