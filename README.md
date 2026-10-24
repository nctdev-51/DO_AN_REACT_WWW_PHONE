# PTV Mobile

Java 17, Spring Boot, Spring MVC/JSP, Spring Security và MariaDB.

## Chạy local

```powershell
# Trên máy mới chưa cấu hình MariaDB:
.\scripts\setup-local.ps1

.\mvnw.cmd spring-boot:run
```

Mở `http://localhost:8080`. Profile mặc định `local` dùng MariaDB `webshop_db`.
Thông tin kết nối riêng nằm trong `application-local.properties` ở thư mục gốc; file này được Git bỏ qua và không đóng gói vào WAR.

```powershell
.\mvnw.cmd clean package
java -jar target/WWW_PHONE_VER1-0.0.1-SNAPSHOT.war
```

## Bố trí source

| Thư mục | Nội dung |
| --- | --- |
| `src/main/java/iuh/fit/www_phone_ver1/controller/admin` | Quản trị sản phẩm, phiên bản, đơn hàng, người dùng, danh mục |
| `controller/store`, `controller/account`, `controller/auth` | Mua hàng, thanh toán, tài khoản, đăng nhập/đăng ký |
| `controller/common` | Trang lỗi và xử lý upload |
| `service/catalog`, `service/order`, `service/user` | Nghiệp vụ catalog, tồn kho/đơn hàng, tài khoản |
| `service/admin`, `service/payment`, `service/mail` | Quản lý người dùng, VNPay, gửi/lưu email |
| `service/promotion` | Quản lý mã giảm giá và chính sách cửa hàng |
| `entity`, `repository`, `dto` | Mô hình dữ liệu, truy vấn, form và dữ liệu hiển thị |
| `src/main/webapp/WEB-INF/views` | JSP theo `store`, `account`, `auth`, `admin`, `error` |
| `views/shared`, `WEB-INF/tags` | Layout, thông báo và component dùng chung |
| `src/main/resources/static/css`, `static/js` | CSS và JavaScript ứng dụng |
| `static/vendor`, `static/images/products` | Thư viện giao diện và ảnh local |
| `src/main/resources/catalog/products.json` | Nguồn catalog mẫu và các URL tham khảo |
| `src/main/resources/catalog/promotions.json` | Nguồn mã giảm giá mẫu dùng chung cho ứng dụng và script SQL |
| `database/schema.sql`, `database/sample-data.sql` | Cấu trúc bảng và dữ liệu mẫu |
| `scripts` | Thiết lập local, cập nhật catalog, kiểm tra HTTP |
| `src/test/java/iuh/fit/www_phone_ver1/integration` | Kiểm thử nghiệp vụ, bảo mật, thanh toán và mua đồng thời |

Catalog gồm 46 model điện thoại và 2 phụ kiện, tổng 180 phiên bản, 5 thương hiệu. Giá, màu và tồn kho là dữ liệu mẫu; ảnh/thông số có URL nguồn trong JSON.
Catalog chỉ tự khởi tạo khi chưa có sản phẩm. Khởi động lại không reset tồn kho hoặc phục hồi sản phẩm đã xóa.

## Kiểm thử

```powershell
.\mvnw.cmd test
# Chỉ dùng với ứng dụng/database local hoặc test; script tạo fixture riêng:
python -X utf8 scripts/smoke_test.py
python -X utf8 scripts/smoke_auth.py
python -X utf8 scripts/smoke_retail.py
```

## Thanh toán và email

COD hoạt động mặc định. Để bật VNPay, đặt `VNPAY_ENABLED=true`, `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_RETURN_URL` trong môi trường chạy.
Đăng ký endpoint HTTPS nhận IPN với VNPay tại `/payments/vnpay/ipn`. Return URL chỉ hiển thị kết quả; IPN hợp lệ mới xác nhận thanh toán.
Đơn VNPay chưa thanh toán sau 15 phút được hủy và hoàn kho. Khoản tiền đến sau khi đơn bị hủy được đánh dấu `REFUND_REQUIRED`, cần xử lý hoàn tiền riêng.

Email mặc định lưu `.eml` tại `target/mail-outbox`. Gửi SMTP cần `MAIL_ENABLED`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`.
Đặt `PUBLIC_BASE_URL` theo địa chỉ website (mặc định `http://localhost:8080`, gồm context path nếu có); liên kết email không lấy từ Host header của request.

Tài khoản mới phải xác minh email trước khi đăng nhập. Liên kết xác minh có hạn 24 giờ, đặt lại mật khẩu có hạn 15 phút; chỉ sử dụng một lần và chỉ tiêu thụ khi xác nhận bằng POST có CSRF.
Các trang `/resend-verification` và `/forgot-password` trả thông báo chung, giới hạn gửi lại mỗi 60 giây cho từng tài khoản và loại liên kết. Gửi lại làm vô hiệu liên kết trước đó.
Database chỉ lưu SHA-256 của token. Đặt lại mật khẩu kết thúc các phiên đăng nhập hiện tại; đổi mật khẩu trong tài khoản cũng hủy các liên kết đặt lại còn hiệu lực.
Tài khoản tồn tại trước khi bổ sung chức năng được giữ trạng thái đã xác minh để tiếp tục dùng local.

Xem [DATABASE.md](DATABASE.md) để nhập SQL hoặc chạy H2 demo.

## Nghiệp vụ bán hàng

- `/compare`: so sánh tối đa 3 mẫu máy theo phiên bản, đổi màu/dung lượng, xem điểm khác nhau và chia sẻ URL. Thông số chưa có được ghi rõ; không tự suy diễn điểm hiệu năng.
- `/products`: tìm kiếm, lọc thương hiệu/danh mục/giá/RAM/dung lượng/còn hàng. Các điều kiện biến thể phải khớp trên cùng một phiên bản.
- `/account/wishlist`: lưu sản phẩm yêu thích riêng cho từng tài khoản, tối đa 100 sản phẩm.
- `/offers`, `/admin/promotions`: mã giảm cố định hoặc phần trăm, trần giảm, đơn tối thiểu, thời hạn, tổng lượt dùng và mỗi khách một lần cho đơn chưa hủy. Hủy đơn trả lại lượt sử dụng. Lượt mã được khóa khi đặt hàng để tránh vượt giới hạn.
- Phí giao hàng mặc định 30.000đ; miễn phí từ 2.000.000đ tiền sản phẩm trước giảm giá. Admin thay đổi phí/ngưỡng tại Khuyến mãi & giao hàng; đơn cũ giữ giá và phí đã chốt.
- Chi tiết đơn hiển thị tiền hàng, giảm giá, phí giao hàng và lịch sử xử lý. Phản hồi VNPay hợp lệ cũng được ghi vào lịch sử.
- `/account/support`, `/admin/support`: tiếp nhận đổi/trả và bảo hành theo sản phẩm đã mua. Kiểm tra quyền sở hữu, số lượng, ngày giao, thời hạn; trạng thái chờ → duyệt → nhận thiết bị → hoàn tất hoặc từ chối. Khách được hủy trước khi duyệt.

Thời hạn tiếp nhận đổi/trả mẫu là 7 ngày, admin có thể chỉnh 1–30 ngày; thời hạn bảo hành được chốt theo sản phẩm lúc đặt hàng. Hồ sơ hỗ trợ ghi nhận xử lý thủ công. Hàng trả đã nhận có bước kiểm tra riêng: chỉ nhập lại tồn khi nhân viên xác nhận đủ điều kiện, tối đa một lần cho mỗi hồ sơ. Hoàn tiền được đối soát thủ công tại chi tiết đơn; chưa tự gọi API chuyển tiền của cổng thanh toán.

## Hình ảnh và chế độ 360°

Thanh chọn hãng nằm phía trên danh sách. Thẻ sản phẩm cho phép chọn dung lượng/màu, đồng bộ ảnh, giá và phiên bản dùng khi mở chi tiết hoặc so sánh.

Catalog có 402 ảnh thư viện lưu local, nguồn công khai được ghi trong `catalog/media.json`. Ảnh riêng của màu đang chọn được ưu tiên; ảnh giới thiệu chung có chú thích riêng. Giá và tồn kho vẫn là dữ liệu mẫu.

Trang chi tiết hỗ trợ ảnh thu nhỏ, chuyển ảnh, phóng to và điều khiển bàn phím. Bốn mẫu iPhone 16 Pro Max, iPhone 15, Galaxy S25 và S25 Ultra có mô hình Sketchfab tương tác, tải khi bấm **Xem 360°**, cần Internet và WebGL. Màu mô hình có thể khác phiên bản chọn; tác giả và giấy phép CC BY được hiển thị ngay dưới mô hình.

Admin mở **Sản phẩm → Sửa → Quản lý ảnh & 360°** để tải ảnh PNG/JPEG theo màu. Bộ xoay local cần 12–120 ảnh chụp quanh máy, đặt tên 001, 002…; mỗi tệp tối đa 5 MB, cả yêu cầu tối đa 64 MB. Có thể kéo ngang, dùng phím mũi tên/thanh trượt hoặc tự xoay. Bộ xoay mới thay bộ cũ cùng màu. Chưa có bộ ảnh xoay local trong dữ liệu mẫu; các mẫu thiếu dữ liệu 360° không hiển thị nút xem.

Làm mới ảnh công khai: `python -X utf8 scripts/import_media.py`; tạo lại SQL bằng `python -X utf8 scripts/import_catalog.py`. Công cụ nhập ảnh chỉ sửa màu của SKU mẫu sẵn có, không sửa bản chụp thông tin trong đơn hàng cũ.

## Giao diện và chuyển động

PTV Mobile dùng bộ token màu, chữ, khoảng cách và trạng thái chung tại `static/css/tokens.css`, áp dụng cho cửa hàng, tài khoản và quản trị qua `theme.css`. Trang chủ có khối giới thiệu sản phẩm dùng chung `showcase.tag`; kiểu hiển thị nằm trong `showcase.css`.

`motion.js` và `motion.css` tạo hiệu ứng chữ/sản phẩm hiện dần từ dưới lên khi vào màn hình, chạy một lần bằng IntersectionObserver. Nội dung vẫn hiện khi tắt JavaScript, thiếu API, bật giảm chuyển động hoặc in trang. Focus bàn phím mở ngay phần nội dung đang chờ; tương tác chuột giữ nguyên vị trí nút trong lúc bấm.

Xem [UI-DESIGN-SYSTEM.md](docs/UI-DESIGN-SYSTEM.md) để dùng token, các trạng thái component, yêu cầu bàn phím/touch, tiêu chí WCAG và checklist QA. Font dùng stack hệ thống với các fallback, không tải font từ bên ngoài.

## Hành trình mua sắm và vận hành bổ sung

- Bộ lọc giá và sắp xếp dùng giá của biến thể đáp ứng đồng thời dung lượng/RAM/tồn kho. Thẻ được chọn đúng biến thể ngay từ HTML; có thể xóa từng điều kiện mà giữ bộ lọc khác.
- Tìm kiếm nhiều từ khóa, gợi ý tối đa 6 sản phẩm, điều khiển mũi tên/Enter/Escape. `/choose` chọn theo ngân sách và hãng; `/recently-viewed` giữ tối đa 10 sản phẩm trong phiên, không lưu lịch sử công khai theo tài khoản.
- `/admin/experience`: sửa tiêu đề, nội dung giới thiệu, hai sản phẩm showcase, SKU bộ sưu tập theo thứ tự, liên hệ, thời gian giao dự kiến và quy tắc điểm. Thông tin liên hệ chưa có được giữ trống.
- `/offers`: mã gắn sản phẩm/hãng, sản phẩm bắt buộc trong combo, ngày đọc được, sao chép và chọn mã cho checkout. Sản phẩm quà tặng cần có trong giỏ để quản lý tồn thật; giới hạn giảm được kiểm tra phía server. Giá và chương trình seed là minh họa cho đồ án.
- Hai phụ kiện có ảnh tải local từ nguồn Apple, tương thích chỉ gắn SKU đã đối chiếu. Trang điện thoại hiển thị phụ kiện dùng kèm; không giả định mọi phụ kiện hợp với mọi máy.
- `/account/consultations`, `/admin/consultations`: nhận tư vấn trả góp/thu cũ → tiếp nhận → báo phương án → duyệt/từ chối. Khách có thể hủy yêu cầu chưa kết thúc, tối đa 5 yêu cầu mở. Đây là quy trình tư vấn thủ công, không tự cấp tín dụng hoặc cam kết định giá thiết bị.
- `/account/points`: sổ giao dịch điểm, mặc định 100.000đ tiền hàng thực trả nhận 1 điểm khi giao thành công; mỗi điểm giảm 1.000đ, tối đa 10% tiền hàng sau mã. Đơn lưu quy tắc tích và số tiền giảm tại lúc mua. Khóa tài khoản khi đổi điểm ngăn hai đơn tiêu cùng số dư; hủy đơn trả điểm đúng một lần.
- Hoàn tiền một phần điều chỉnh điểm theo tỷ lệ tổng tiền đã hoàn trên tổng tiền đơn, làm tròn xuống ở số điểm cộng dồn. Hoàn hết đảo hết điểm đã tích và trả điểm đã đổi; điểm đã trả khi hủy không trả lần hai. Nếu điểm đã tích bị dùng ở đơn khác trước khi hoàn tiền, số dư có thể âm và phải tích bù trước khi đổi tiếp.
- Quản trị có thể hủy đơn chờ đã thanh toán để trả tồn/điểm, sau đó tạo khoản hoàn và xác nhận giao dịch thực tế bằng tham chiếu. Không thể ghi vượt số tiền còn được hoàn; khoản chờ cũng giữ hạn mức. VNPay IPN lặp không ghi đè trạng thái đã đối soát.
- Checkout và đơn lưu tỉnh/phường bổ sung, khoảng ngày giao dự kiến. Quản trị nhập hãng, mã vận đơn và ghi chú; lịch sử hiển thị cho chủ đơn. Có adapter GHN để xem trước khả năng giao/cước, tạo vận đơn và lấy trạng thái theo thao tác quản trị khi đã bật cấu hình.
- `/policies/shipping`, `/returns`, `/warranty`, `/payment`, `/faq`, `/contact`: nội dung hỗ trợ nhất quán với cấu hình đang dùng.

Nghiên cứu và tiêu chí nghiệm thu: [REQUIREMENTS-UX-2026.md](docs/REQUIREMENTS-UX-2026.md). Hướng dẫn đăng ký, chi phí và cấu hình dịch vụ: [INTEGRATIONS-SETUP.md](docs/INTEGRATIONS-SETUP.md).
Chạy `./mvnw.cmd test` để kiểm tra các bất biến kho, mã, điểm, hoàn tiền và phân quyền. Dịch vụ SMTP/VNPay thật cần khóa riêng; GHN đã có code tích hợp nhưng chưa kiểm thử bằng tài khoản dịch vụ thật; hoàn tiền qua cổng vẫn đối soát thủ công. Không đưa khóa vào source.


## Cập nhật báo cáo, địa chỉ, bộ lọc và chiến dịch

- Dashboard tách giá trị đơn giao, số tiền đã hoàn và giá trị còn lại. Chỉ khoản hoàn `COMPLETED` của đơn đã giao được trừ; khoản chờ/từ chối không trừ. Biểu đồ dùng tháng giao, đơn cũ dùng tháng đặt; khoản hoàn về sau điều chỉnh lại tháng của đơn. Số liệu gồm phí giao, không phải báo cáo dòng tiền theo ngày hoàn.
- Email xác nhận tách mã giảm giá, điểm đã đổi, phí và tổng; địa chỉ gồm đường/phường/tỉnh được escape trước hiển thị.
- Bộ lọc thêm màu trên cùng biến thể; sắp xếp đánh giá dùng review thật. Bán chạy dùng số lượng trong đơn đã giao, đã thanh toán; loại đơn hủy/hoàn toàn bộ. Đây là số lượng giao trước khi điều chỉnh từng sản phẩm trả một phần, không phải số bán ròng sau mọi đổi trả.
- `/choose` thêm nhóm nhu cầu. `products.usage_tags` do cửa hàng phân loại trong quản trị (hằng ngày/camera/game/pin/gọn), không phải điểm benchmark hoặc cam kết hiệu năng. Seed có phân loại minh họa; chỉnh lại theo đánh giá thực tế khi vận hành.
- Danh mục địa chỉ hai cấp lưu tại `data/vietnam-addresses.json`: 34 tỉnh/thành và 3.321 phường/xã, tải từ [Province Open API v2](https://provinces.open-api.vn/). Đây là nguồn dữ liệu cộng đồng, có nguồn/ngày tải trong JSON; không được coi là dữ liệu trực tiếp từ cơ quan nhà nước hay mã riêng GHN. Chạy `python scripts/update_addresses.py` để làm mới có kiểm tra số lượng; không gọi dịch vụ ngoài mỗi khi khách nhập địa chỉ.
- Sổ địa chỉ và checkout xác minh quan hệ tỉnh/phường phía server, giữ mã và tên chuẩn ở đơn. Địa chỉ cũ giữ nguyên; khi sửa qua giao diện phải chọn lại địa phương. Không JS hoặc lỗi tải danh sách vẫn có trường nhập tên để server đối chiếu, không tự đoán tên sai. Form `legacy` còn được hỗ trợ cho dữ liệu/luồng nội bộ cũ; GHN luôn yêu cầu địa chỉ chuẩn.
- `/admin/banners`: thêm/sửa/xóa ảnh PNG/JPEG, đích đến nội bộ, nền sáng/tối, thứ tự, vị trí HERO/TILE và lịch giờ Việt Nam. Đầu lịch được tính, cuối lịch hết hiệu lực; banner sản phẩm ngừng bán tự ẩn. Khi không có hero theo lịch, trang chủ dùng showcase trong cấu hình nội dung. Ảnh được dọn theo kết quả transaction; cập nhật version chống ghi đè.
- GHN mặc định tắt và dùng staging. `/checkout/delivery-quote` xem trước không tạo vận đơn, với khối lượng ước tính 500g mỗi sản phẩm dưới 20kg. Cước này tham khảo, không tự thay phí khách thanh toán theo chính sách shop. Nhân viên nhập số đo thực khi tạo vận đơn trong chi tiết đơn; tên tỉnh/phường được đối chiếu lại với danh mục GHN, không tái sử dụng mã hành chính làm mã hãng.
- Tạo GHN giữ mã `client_order_code` và payload ban đầu khi chưa rõ kết quả, để retry không sinh vận đơn trùng. Thông tin tracking phải đúng mã đơn/ShopId/môi trường mới cập nhật. Trạng thái hãng hiển thị riêng; nhân viên xác nhận giao nhận trước khi chốt trạng thái đơn và thu tiền/điểm. Chưa có webhook/polling tự động.
- Khi đã gửi yêu cầu GHN, hủy vận đơn trên GHN và đồng bộ xác nhận `cancel` trước khi hủy đơn trong shop. Hàng đã vận chuyển trả về cần kiểm tra tồn riêng, không tự nhập kho qua trạng thái hãng.

Hướng dẫn biến môi trường, môi trường thử và giới hạn tại [INTEGRATIONS-SETUP.md](docs/INTEGRATIONS-SETUP.md). Các kiểm thử API GHN dùng HTTP server local, không tạo vận đơn thật.
