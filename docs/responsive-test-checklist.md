# Nghiệm thu responsive PTV Mobile

Mở Chrome DevTools → Toggle device toolbar. Đặt zoom 100%, dùng chiều rộng CSS bên dưới, đăng nhập admin bằng tài khoản demo và kiểm tra mỗi trang sau khi tải xong. Khi đo tràn ngang, so sánh `document.documentElement.scrollWidth` với `document.documentElement.clientWidth`. Bảng rộng được phép cuộn trong khung của nó.

| Thiết bị | Khung CSS | `--header-offset` | Trang cần kiểm tra |
|---|---:|---:|---|
| iPhone SE | 375 × 667 | 192px | Trang chủ, danh sách và chi tiết sản phẩm, đăng nhập, banner admin |
| Pixel 7 | 412 × 915 | 192px | Trang chủ, danh sách và chi tiết sản phẩm, đăng nhập, banner admin |
| iPad Mini | 768 × 1024 | 164px | Trang chủ, danh sách và chi tiết sản phẩm, đăng nhập, banner admin |
| Desktop | 1280 × 800 | 164px | Trang chủ, danh sách và chi tiết sản phẩm, đăng nhập, banner admin |

## Checklist thao tác

- [ ] Trang có đúng một viewport meta. Tiêu đề thay đổi kích thước theo viewport, tên dài vẫn đọc được.
- [ ] Header chứa đủ logo, ô tìm kiếm và mục điều hướng cho từng kích thước. Cuộn trang và mở menu để kiểm tra phần nội dung phía dưới.
- [ ] Danh sách sản phẩm mở được bộ lọc, trang chi tiết chuyển được ảnh và phiên bản, các nút mua hàng sử dụng được.
- [ ] Biểu mẫu đăng nhập giữ nhãn và nút gửi trong khung ở cả chiều dọc và ngang.
- [ ] Tại admin, hàng nút banner xuống dòng khi cần. Dùng bàn phím và thao tác vuốt để cuộn tới cột Thao tác trên màn hình nhỏ.
- [ ] Kiểm tra thêm bảng sản phẩm, đơn hàng, so sánh và thống kê với dữ liệu thực tế hoặc dữ liệu demo.

## Rà soát JSP

Các tài liệu HTML độc lập trong JSP đã có viewport. Các trang cửa hàng và quản trị dùng chung header tương ứng. Bảng dùng vùng cuộn riêng qua `table-responsive`, `comparison-scroll` hoặc `insights-table-scroll`. Độ rộng cột và thumbnail banner chuyển từ inline style sang CSS.

## Kết quả đo trên Chrome

Đã chạy Chrome DevTools Protocol trên năm trang trong ma trận, nạp CSS sửa đổi vào bản demo và áp dụng class bảng banner trong DOM. Ở 412, 768 và 1280px, cả năm trang đo được 0px tràn ngang toàn trang. Bảng banner cuộn bên trong ở 375, 412 và 768px. Ở 375px, trước khi giới hạn chiều rộng ô tìm kiếm, ba trang cửa hàng còn tràn 5px. Cần đo lại trên bản build mới và nghiệm thu các thao tác trong checklist.
