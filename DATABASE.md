# Database local

Thư mục `database` chỉ gồm:

1. `schema.sql`: database, các bảng, khóa ngoại và chỉ mục.
2. `sample-data.sql`: 5 danh mục/thương hiệu, 46 model, 178 biến thể, hai mã giảm giá mẫu, chính sách cửa hàng và hai vai trò ADMIN/USER.

Trên MariaDB, nhập `schema.sql` trước rồi `sample-data.sql`. Script mẫu không xóa tài khoản, đơn hàng hoặc reset tồn kho. Tài khoản Admin được ứng dụng mã hóa mật khẩu khi khởi tạo.

Ứng dụng mặc định dùng MariaDB với profile `local`. Trên máy mới chưa cấu hình:

```powershell
.\scripts\setup-local.ps1
.\mvnw.cmd spring-boot:run
```

File `application-local.properties` ở thư mục gốc chứa cấu hình riêng và được Git bỏ qua. Trên máy khác có thể chạy profile `mariadb` với `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Chạy H2 trong bộ nhớ, không cần MariaDB:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

H2 mất dữ liệu khi dừng ứng dụng. MariaDB giữ dữ liệu sau khi khởi động lại.

`users.email_verified` lưu trạng thái xác minh email. `account_tokens` lưu hash token xác minh/đặt lại mật khẩu, mục đích, hạn dùng và thời điểm đã sử dụng. Dữ liệu mẫu không chứa token hoặc tài khoản khách hàng giả.

`wishlist_entries` lưu yêu thích; `promotions`, `store_policy` lưu ưu đãi và quy tắc vận hành; `order_events` ghi lịch sử; `support_requests` ghi hồ sơ đổi/trả/bảo hành. Đơn hàng lưu riêng tiền sản phẩm, giảm giá, phí giao hàng và mã đã dùng. Chi tiết đơn lưu thời hạn bảo hành tại thời điểm mua.

Ảnh catalog nằm trong `src/main/resources/static/images/products`. Ảnh Admin tải lên nằm trong `uploads/products`, ngoài source và được Git bỏ qua.

Tạo lại SQL từ catalog hiện có, không cần Internet:

```powershell
python -X utf8 scripts/import_catalog.py
```

Thêm `--download` để lấy lại ảnh/thông số từ các URL công khai. Giá, màu và tồn kho là dữ liệu mẫu local/đồ án.
Thêm `--download-missing` để chỉ tải sản phẩm chưa có ảnh local. `catalog/promotions.json` là nguồn sinh mã giảm giá mẫu cho cả H2 và SQL.

`product_media` lưu thư viện ảnh theo sản phẩm/màu (`PHOTO`), bộ ảnh xoay có thứ tự (`SPIN`) và liên kết mô hình được phép nhúng (`MODEL`). Có 402 ảnh thư viện local và 4 mô hình nhúng trong catalog mẫu. `catalog/media.json` là nguồn sinh dữ liệu media cho cả H2 và SQL. Ảnh chung dùng `color=NULL`; không dùng ảnh của màu khác để thay cho ảnh đúng màu.

Hai file SQL vẫn là `database/schema.sql` và `database/sample-data.sql`. Chạy lại dữ liệu mẫu không đặt lại tồn kho, tài khoản, đơn hàng hoặc ảnh do quản trị viên tải lên. Ảnh/mô hình đã xóa không tự xuất hiện lại mỗi lần khởi động khi bảng media còn dữ liệu.


`store_experience` giữ nội dung trang chủ, sản phẩm trình bày, liên hệ và quy tắc điểm/giao dự kiến. `loyalty_entries` là sổ giao dịch có tham chiếu duy nhất; số dư được tính từ sổ, không tin số điểm gửi từ trình duyệt. Đơn lưu điểm đã dùng/đã tích/đã đảo/đã trả và quy tắc tích tại lúc mua.
`consultations` lưu tư vấn trả góp/thu cũ với version chống ghi đè; `refunds` lưu khoản hoàn chờ/đã đối soát/từ chối, số tiền, lý do và tham chiếu. `support_requests.inspected/restocked` ngăn nhập lại hàng trả hai lần. Đơn lưu hãng/mã vận đơn và khoảng ngày giao dự kiến, tách khỏi thông báo của hãng vận chuyển.
`products.kind` phân biệt điện thoại/phụ kiện; `compatible_skus` chỉ gắn các SKU đã đối chiếu. `promotions.scope_brand/scope_skus/required_skus` giới hạn sản phẩm nhận giảm và các sản phẩm bắt buộc của combo. Mã mẫu không thay thế báo giá thương mại.
Dữ liệu mẫu hiện có 48 sản phẩm, 180 biến thể, 4 mã ưu đãi và 406 mục media (402 ảnh + 4 mô hình). Hai phụ kiện dùng ảnh chính trong catalog. Không seed giao dịch điểm, yêu cầu tư vấn, khoản hoàn hoặc đơn giả.


`campaign_banners` lưu nội dung, lịch, vị trí/thứ tự và ảnh chiến dịch; không seed banner quảng cáo giả. `products.usage_tags` lưu nhóm nhu cầu do cửa hàng phân loại; dữ liệu mẫu chỉ bổ sung khi NULL, không phục hồi tag đã chủ động xóa. `addresses` và `orders` lưu mã tỉnh/phường riêng cùng tên chuẩn; địa chỉ lịch sử không bị chuyển đổi tự động. Danh mục địa phương là JSON tĩnh trong resources, hai file database vẫn giữ nguyên.
Đơn có snapshot môi trường/payload GHN, trạng thái hãng và thời điểm lấy dữ liệu. Payload có thông tin người nhận, chỉ dùng phía server/quản trị; không xuất công khai. Không lưu Token GHN trong database. Báo cáo trừ các `refunds.status=COMPLETED` của đơn đã giao.
