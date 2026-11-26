# Kiểm chứng đợt cập nhật hành trình mua sắm

Ngày: 23/10/2026. Môi trường: Java 17, Spring Boot, H2 kiểm thử và MariaDB local; trình duyệt Chromium.

## Kết quả

- `mvnw.cmd package`: đạt, 76 kiểm thử, không lỗi/không bỏ qua. Gồm cạnh tranh tồn kho, lượt mã và số dư điểm ở hai đơn đồng thời; kiểm tra giá trên cùng biến thể, tìm kiếm, phạm vi combo, tích/đổi/trả điểm, hoàn tiền, tư vấn, kiểm tra hàng trả và phân quyền.
- Kiểm tra trình duyệt tại 1440/768/390/320px: các trang mới, catalog, chi tiết, ưu đãi, checkout, tài khoản và quản trị trả 200; không tràn ngang toàn trang, không có ảnh nguồn bị lỗi hoặc lỗi JavaScript. Ảnh gallery ẩn chưa có `src` không phải lỗi tải ảnh.
- Bàn phím: gợi ý tìm kiếm chọn bằng mũi tên/Escape; xóa từng bộ lọc giữ điều kiện còn lại. Tắt JavaScript vẫn chọn đúng dung lượng và giá của thẻ lọc 512 GB. Giảm chuyển động được bật trong kiểm tra layout.
- Luồng thao tác thật qua trình duyệt và MariaDB: mua iPhone 15 kèm EarPods, áp mã combo; chuyển giao hàng/giao thành công và cộng điểm; nhận/kiểm tra hàng trả trước nhập tồn; tạo và xác nhận khoản hoàn một phần; đơn tiếp theo đổi điểm rồi hủy để trả điểm; gửi yêu cầu thu cũ và quản trị tiếp nhận/báo phương án/duyệt; khách xem được phản hồi.
- Hai file SQL được nhập vào database kiểm chứng riêng; nhập dữ liệu mẫu hai lần vẫn có 48 sản phẩm, 180 biến thể, 4 mã và 406 mục media. Database kiểm chứng đã xóa.
- Dữ liệu QA được xóa theo mã riêng, chỉ hoàn lại phần tồn đã dùng chưa được trả. Sau kiểm tra: giữ 2 tài khoản, 1 đơn gốc; tổng tồn 3.263; không còn yêu cầu hỗ trợ/tư vấn, khoản hoàn hoặc giao dịch điểm QA. Không đặt lại toàn bộ kho.
- `git diff --check`, định dạng Java và Python, biên dịch script nhập catalog/media: đạt.

## Giới hạn kiểm chứng

Chưa có tài khoản dịch vụ để kiểm tra thu tiền/gửi SMTP thật hoặc tạo vận đơn GHN thật. VNPay có kiểm thử chữ ký/IPN/idempotency trong bộ kiểm thử; môi trường mặc định vẫn tắt tích hợp. Vận đơn và đối soát hoàn tiền hiện do nhân viên cập nhật; không tự chuyển tiền hoặc giả trạng thái từ đối tác. Trả góp/thu cũ là yêu cầu tư vấn được duyệt thủ công theo lựa chọn của người dùng.

Kiểm tra trên Chromium và các tiêu chí tự động không thay thế thử trên thiết bị thực, trình đọc màn hình hay chứng nhận đầy đủ WCAG 2.2 AA. Nghiên cứu yêu cầu là thứ cấp, chưa có thử nghiệm với khách PTV Mobile ngoài nhóm phát triển. Báo cáo Word/PDF môn học chưa nằm trong đợt hiện thực này.


## Đợt hoàn thiện tiếp theo

- 95 kiểm thử đạt, gồm 14 trường hợp báo cáo sau hoàn tiền/email điểm/địa chỉ chuẩn/lọc màu/xếp hạng/lịch banner và 5 trường hợp hợp đồng GHN bằng HTTP server local. Không gọi cổng vận chuyển thật.
- Thêm 40 lượt kiểm tra trang × viewport tại 1440/768/390/320px: banner có lịch, bảng điều khiển, bộ lọc, chọn nhu cầu, địa chỉ và trang vận đơn. Không lỗi ảnh có nguồn, JavaScript hoặc tràn ngang. Đã thao tác tạo/sửa lịch để xác minh banner tự ẩn/hiện, lưu/xóa địa chỉ và kiểm tra fallback không JS.
- Hành trình mua combo → giao/tích điểm → trả hàng/kiểm tra tồn → hoàn một phần → đổi điểm/hủy → duyệt thu cũ đã chạy lại với mã tỉnh/phường mới và đạt. Dọn QA giữ 2 tài khoản, 1 đơn gốc và tổng tồn 3.263.
- Schema mới và sample được nhập hai lần vào database riêng: 48 sản phẩm, 180 biến thể, 46 sản phẩm có phân loại nhu cầu, không seed banner hay giao dịch giả. Database kiểm chứng đã xóa.
- GHN đã có code xem trước/tạo/đồng bộ theo nút quản trị nhưng chưa có token để nghiệm thu staging thật; mặc định tắt. Chưa có webhook nền, hoàn tiền tự động VNPay, thử tải lớn hoặc xác nhận UX với khách thật. Các giới hạn thiết bị/trình duyệt và báo cáo môn học phía trên vẫn còn.
