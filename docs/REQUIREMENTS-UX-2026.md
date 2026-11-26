# PTV Mobile — yêu cầu và phân tích UX

Ngày đối chiếu: 23/10/2026. Phạm vi: cửa hàng điện thoại chạy local, có thể cấu hình dịch vụ ngoài sau. Đây là nghiên cứu thứ cấp và kiểm tra sản phẩm hiện có, **chưa phải kết quả phỏng vấn khách hàng PTV Mobile**. Các nhóm nhu cầu dưới đây là giả thuyết cần kiểm chứng bằng thử nghiệm người dùng.

## Bằng chứng và giới hạn

- [Baymard: danh sách và bộ lọc sản phẩm](https://baymard.com/research-articles/current-state-product-list-and-filtering): hỗ trợ lựa chọn bằng bộ lọc rõ ràng, biến thể gom trong cùng sản phẩm và sắp xếp phù hợp. Kiểm tra local phát hiện lọc 512 GB vẫn chọn thẻ 256 GB; lọc giá dựa giá gốc thay vì biến thể phù hợp.
- [Baymard: trường biểu mẫu checkout](https://baymard.com/research-articles/checkout-flow-average-form-fields): giảm thao tác và trường không cần thiết. Checkout phải cho thấy chi phí, lỗi và dữ liệu đã nhập trước khi đặt hàng.
- [DHL E-commerce Trends 2025](https://group.dhl.com/en/media-relations/press-releases/2025/dhl-e-commerce-trends-report-2025.html): giao nhận và quy trình trả hàng ảnh hưởng quyết định mua. Mẫu 24.000 người tại 24 thị trường là số liệu toàn cầu, không được diễn giải thành tỷ lệ riêng Việt Nam.
- [TGM: điện tử tại Việt Nam 2025](https://tgmresearch.com/vietnam-e-commerce-electronics-appliances-insights-2025.html): đặt bối cảnh hành trình tìm hiểu và mua điện tử kết hợp online/offline. Chỉ sử dụng mô tả công khai, không suy diễn số liệu trong báo cáo trả phí.
- [Apple Store Việt Nam](https://www.apple.com/vn/store): phân cấp tiêu đề, khoảng trắng, bộ sưu tập và khám phá sản phẩm. [Samsung ưu đãi](https://www.samsung.com/vn/offer/): ưu đãi gắn nhu cầu mua và hỗ trợ lựa chọn. Học nguyên tắc tương tác, giữ thương hiệu PTV Mobile; không sao chép tuyên bố bán hàng/chính sách của hãng.

## Nhóm nhu cầu và hành trình

1. Người mua theo ngân sách: chọn hãng, dung lượng, giá; cần giá đúng cấu hình, tồn kho và tổng chi phí rõ ràng.
2. Người nâng cấp: xem ảnh đúng màu, thông số, so sánh, phụ kiện và gửi yêu cầu thu cũ/trả góp để nhân viên tư vấn.
3. Người quay lại: tìm gần đây, yêu thích, địa chỉ, điểm thưởng và kiểm tra đơn hàng.
4. Người cần hỗ trợ: biết chính sách trước mua, theo dõi giao nhận, gửi đổi trả/bảo hành và xem kết quả hoàn tiền.
5. Nhân viên cửa hàng: cập nhật nội dung, ưu đãi, yêu cầu tư vấn, vận đơn, hoàn tiền và cấu hình điểm có kiểm soát.

Hành trình mục tiêu: khám phá → lọc/so sánh → cấu hình sản phẩm → giỏ hàng → kiểm tra địa chỉ và chi phí → đặt hàng → theo dõi → hỗ trợ/mua lại.

## Yêu cầu và tiêu chí chấp nhận

| ID | Yêu cầu | Điều kiện đạt |
|---|---|---|
| CAT-01 | Lọc theo cùng một biến thể | Dung lượng/RAM/tồn kho/giá cùng khớp một biến thể đang bán; giá và cấu hình đầu tiên trên thẻ phải khớp bộ lọc |
| CAT-02 | Sắp xếp giá thực tế | Giá tăng/giảm dựa biến thể phù hợp, phân trang ổn định; bộ lọc được giữ khi chuyển trang |
| FIND-01 | Tìm kiếm và khám phá | Gợi ý có giới hạn, thao tác bàn phím được; hỗ trợ tên/hãng và nhiều từ khóa; xem gần đây không lộ dữ liệu tài khoản khác |
| HOME-01 | Bộ sưu tập rõ ràng | Sản phẩm mới, theo ngân sách/nhu cầu và ưu đãi có đích đến thật; quản trị chọn sản phẩm nổi bật |
| PDP-01 | Quyết định mua | Ảnh/giá/tồn kho thay đổi đúng biến thể, mua nhanh giữ đúng cấu hình; không hiển thị 0/5 khi chưa có đánh giá |
| OFFER-01 | Ưu đãi có điều kiện | Ngày hiển thị dễ đọc, sao chép/áp dụng mã được; phạm vi hãng/sản phẩm được kiểm tra lại phía server |
| CHECK-01 | Minh bạch checkout | Giữ dữ liệu khi lỗi, cho biết phí và giảm giá trước đặt; chi phí cuối được tính lại, không tin giá từ trình duyệt |
| DELIVERY-01 | Giao nhận | Địa chỉ rõ tỉnh/phường/đường; thời gian là dự kiến; mã vận đơn do quản trị xác nhận, không tự giả lập trạng thái hãng |
| POLICY-01 | Tin cậy | Trang thanh toán/giao hàng/đổi trả/bảo hành/FAQ truy cập từ footer; không tạo thông tin liên hệ/pháp nhân giả |
| CARE-01 | Tư vấn trả góp/thu cũ | Khách gửi yêu cầu, xem trạng thái của mình; quản trị tiếp nhận/báo phương án/duyệt; không tự thông báo được cấp tín dụng |
| POINT-01 | Điểm thưởng | Mặc định 100.000đ = 1 điểm, 1 điểm = 1.000đ, đổi tối đa 10% tiền hàng; quản trị chỉnh; chỉ cộng khi giao thành công, hủy trả điểm đúng một lần |
| REFUND-01 | Đối soát hoàn tiền | Ghi nhận yêu cầu và xác nhận thủ công có tham chiếu, không vượt số đã thanh toán; hàng trả phải kiểm tra trước nhập tồn |
| CMS-01 | Quản trị nội dung | Nội dung/hero và chính sách chỉnh được, kiểm tra dữ liệu và xung đột cập nhật |
| A11Y-01 | Tiếp cận | Bàn phím dùng được mọi thao tác; focus nhìn thấy; nhãn/lỗi đọc được; reduced-motion/no-JS vẫn thấy nội dung; 320px không tràn ngang |

## Quy tắc UX và dữ liệu

- Must dùng semantic tokens trong `tokens.css`; should dùng thành phần chung thay vì CSS riêng lẻ cho từng trang.
- Must có default, hover, focus-visible, active, disabled, loading và error cho điều khiển tương tác. Nút đang gửi must chặn gửi lặp; lỗi must giữ dữ liệu hợp lệ.
- Must escape dữ liệu người dùng/nội dung quản trị ở HTML, không render HTML tùy ý. Mọi thay đổi nghiệp vụ must kiểm tra quyền, CSRF và dữ liệu phía server.
- Must phân biệt giá/tồn demo với dữ liệu thương mại đã xác minh; không dựng số lượng đã bán, đánh giá, ưu đãi hoặc đếm ngược giả.
- Must giữ lịch sử giá/địa chỉ/chính sách điểm ở đơn; thay cấu hình sau không được làm đổi số tiền đơn cũ.
- Must không yêu cầu CCCD/thu nhập để gửi yêu cầu tư vấn; nhân viên xử lý hồ sơ thực tế qua quy trình phù hợp sau.
- Must không tự gọi dịch vụ thu tiền/tạo vận đơn thật khi chưa có cấu hình. Luồng local vẫn hoạt động, chức năng ngoài hiển thị trạng thái khả dụng trung thực.

## Kiểm chứng sau hiện thực

Thử các nhiệm vụ: tìm điện thoại 512 GB trong khoảng giá; đổi màu và so sánh; áp mã đúng/sai phạm vi; đổi điểm rồi hủy; xác nhận giao thành công hai lần; gửi/xử lý tư vấn; ghi nhận hoàn tiền một phần; truy cập đơn/yêu cầu của tài khoản khác. Kiểm tra 1440/768/390/320px, bàn phím, reduced motion, không JS, lỗi mạng/dữ liệu trống. Mời 3–5 người thực hiện hành trình và ghi thời gian/lỗi/điểm vướng để xác nhận giả thuyết, không thay số liệu này bằng nhận xét chủ quan.

Tài liệu này là đặc tả triển khai, chưa thay thế báo cáo môn học. Tiến độ và các phần phụ thuộc dịch vụ thật phải được báo rõ khi bàn giao.
