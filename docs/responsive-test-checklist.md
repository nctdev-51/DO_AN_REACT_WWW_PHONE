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

Đã chạy Chrome DevTools Protocol trên bản demo. Chrome nhận CSS của nhánh qua request intercept và class bảng banner được áp dụng trong DOM của trang. Mọi trang trong ma trận đều có viewport meta.

| Chiều rộng | Trang chủ | Danh sách | Chi tiết | Đăng nhập | Banner admin | Bảng banner |
|---|---:|---:|---:|---:|---:|---|
| 375px | 0px | 0px | 0px | 0px | 0px | Cuộn trong khung |
| 412px | 0px | 0px | 0px | 0px | 0px | Cuộn trong khung |
| 768px | 0px | 0px | 0px | 0px | 0px | Cuộn trong khung |
| 1280px | 0px | 0px | 0px | 0px | 0px | Vừa khung |

Các ô số là mức tràn ngang của toàn trang. Cần thực hiện thêm checklist thao tác với bản build của nhánh khi nghiệm thu.
