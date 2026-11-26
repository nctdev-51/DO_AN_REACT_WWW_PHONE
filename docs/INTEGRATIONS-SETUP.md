# Kết nối VNPay, email và giao hàng

Cập nhật 23/10/2026. Không commit mật khẩu/token và không gửi khóa vào chat. Cấu hình qua biến môi trường hoặc `application-local.properties` (đã được Git bỏ qua). Thay thông tin ví dụ bằng dữ liệu của chính bạn; không dùng khóa sandbox ở production.

## Chi phí

| Dịch vụ | Thử nghiệm | Vận hành thật |
|---|---|---|
| VNPay | Sandbox mô phỏng thanh toán, không thu tiền thật | Phí theo hợp đồng. [Biểu phí công bố](https://vnpay.vn/bieu-phi-dich-vu-0f61zmr3hwgs) ghi miễn phí hoặc thu phí, tối đa 2,5% giá trị giao dịch, có thể thay đổi |
| Email | Local ghi file `.eml`, không cần dịch vụ gửi mail | Gmail có giới hạn gửi và điều kiện tài khoản; dịch vụ email giao dịch có gói/giá riêng, cần kiểm tra nhà cung cấp trước đăng ký |
| GHN | [Tài khoản miễn phí, staging không tạo đơn thật](https://developer.ghn.dev/vi) | Cước theo tuyến, khối lượng, dịch vụ và điều khoản COD/bảo hiểm/hoàn hàng; xác nhận báo giá trước tạo đơn thật |

Chạy đồ án local không cần mua domain/hosting. Webhook/IPN từ dịch vụ ngoài cần URL HTTPS công khai; domain, hosting hoặc tunnel có thể có giới hạn/phí riêng.

## 1. VNPay sandbox

1. Truy cập [đăng ký merchant thử nghiệm](https://sandbox.vnpayment.vn/devreg), tự nhập tên website, URL, email và xác nhận theo hướng dẫn.
2. Lưu TmnCode và HashSecret nhận được vào cấu hình riêng. Xem [tài liệu chính thức](https://sandbox.vnpayment.vn/apis/docs/gioi-thieu/).
3. Đặt biến môi trường trước khi chạy ứng dụng:

```powershell
$env:VNPAY_ENABLED='true'
$env:VNPAY_TMN_CODE='<TmnCode sandbox>'
$env:VNPAY_HASH_SECRET='<HashSecret sandbox>'
$env:PUBLIC_BASE_URL='https://<ten-mien-thu-nghiem>'
$env:VNPAY_RETURN_URL='https://<ten-mien-thu-nghiem>/payments/vnpay/return'
```

4. Đăng ký IPN URL `https://<ten-mien-thu-nghiem>/payments/vnpay/ipn` với VNPay. Chỉ chuyển hướng trình duyệt về return URL không đủ xác nhận giao dịch; server phải nhận và xác minh chữ ký IPN.
5. Dùng dữ liệu thẻ thử theo tài liệu sandbox. Kiểm tra thành công, thất bại, đóng trang, callback lặp và quá hạn. Ứng dụng giữ đơn VNPay chưa trả tiền tối đa 15 phút; tiền đến sau khi hủy cần đối soát hoàn tiền.
6. Khi kinh doanh thật: làm hợp đồng merchant, xác nhận phí/hồ sơ với VNPay, đổi sang cấu hình production và kiểm thử lại. Mặc định dùng URL sandbox; cấu hình `VNPAY_PAYMENT_URL` theo endpoint production do VNPay cung cấp khi chuyển môi trường, không chỉ thay khóa thật vào URL thử.

## 2. Gmail SMTP để thử email

1. Dùng tài khoản thử riêng. Bật xác minh hai bước; tạo App Password theo [hướng dẫn Google](https://support.google.com/mail/answer/185833). Một số tài khoản tổ chức/chính sách bảo vệ không cho dùng App Password.
2. Cấu hình:

```powershell
$env:MAIL_ENABLED='true'
$env:MAIL_USERNAME='<email-gmail>'
$env:MAIL_PASSWORD='<app-password>'
$env:MAIL_FROM='<email-gmail>'
$env:PUBLIC_BASE_URL='http://localhost:8080'
```

3. Khởi động lại, tự đăng ký tài khoản thử, kiểm tra thư xác minh và đặt lại mật khẩu. Link localhost chỉ mở được trên máy đang chạy ứng dụng. Nếu người khác cần mở, cấu hình URL công khai phù hợp.
4. SMTP mặc định `smtp.gmail.com:587`, STARTTLS. Không dùng mật khẩu đăng nhập thông thường. Nếu chưa cấu hình, giữ `MAIL_ENABLED=false`, mở thư ở `target/mail-outbox` để kiểm thử luồng local.
5. Với cửa hàng thật, dùng domain/email đã xác minh và nhà cung cấp email giao dịch; cấu hình SPF/DKIM/DMARC theo nhà cung cấp. Không coi Gmail là dịch vụ gửi không giới hạn.

## 3. GHN

1. Tạo tài khoản staging tại `5sao.ghn.dev`; đọc [GHN Developer](https://developer.ghn.dev/vi). Tài khoản thật ở `khachhang.ghn.vn` là môi trường khác.
2. Lấy API Token và ShopId trong cài đặt; thiết lập địa chỉ lấy hàng thực tế. Không dùng địa chỉ/hotline giả.
3. Thử API địa chỉ, phí, thời gian giao dự kiến và tra cứu bằng Postman với token staging. Dùng dữ liệu tỉnh/phường từ API hiện hành, không hardcode danh sách hành chính cũ.
4. Tạo vận đơn thử trên staging, so sánh phí và dữ liệu nhận hàng. Webhook cần HTTPS và kiểm tra nguồn/xác thực theo tài liệu của môi trường.
5. Chỉ bật tạo vận đơn thật sau khi xác nhận báo giá, tài khoản production, kho lấy hàng, trọng lượng/đóng gói và quy trình đối soát COD/hoàn hàng.

Bản cập nhật có adapter xem trước, tạo và lấy trạng thái GHN. Sau khi tự lấy token staging và ShopId, đặt:

```powershell
$env:GHN_ENABLED='true'
$env:GHN_PRODUCTION='false'
$env:GHN_TOKEN='<token staging>'
$env:GHN_SHOP_ID='<shop id staging>'
```

Khởi động lại ứng dụng. Checkout có nút kiểm tra GHN sau khi nhập đủ địa chỉ; kết quả không tạo vận đơn. Mở Quản trị → Đơn hàng → Chi tiết → Giao hàng qua GHN, nhập khối lượng/kích thước kiện thực và giá trị khai bảo hiểm, xác nhận tạo. Đơn VNPay phải thanh toán trước khi tạo vận đơn. COD của luồng này tối đa 50 triệu; kiện dưới 20kg. Giá trị khai bảo hiểm phải phù hợp thỏa thuận với hãng, không tự mặc định điện thoại được bảo hiểm toàn bộ.

Nút Đồng bộ lấy trạng thái trực tiếp từ GHN; chưa tự chạy webhook hoặc đồng bộ nền. Trạng thái hãng và trạng thái đơn cửa hàng tách riêng: nhân viên kiểm tra rồi chốt đang giao/đã giao trong shop. Tạo thất bại chưa rõ kết quả phải gửi lại cùng mã yêu cầu và payload; không tự tạo mã mới. Khi cần hủy, hủy trên GHN, đồng bộ xác nhận `cancel`, rồi hủy đơn shop. Thao tác hủy/hoàn hàng trên hệ thống hãng không tự nhập tồn.

Chỉ chuyển `GHN_PRODUCTION=true` cùng token/ShopId production sau khi thử staging và xác nhận điều kiện vận hành. Khi production, nút tạo phát sinh vận đơn thật. Không bật production chỉ để kiểm thử giao diện. Nếu không cấu hình, vận đơn thủ công và phí shop vẫn dùng được.

Nguồn hợp đồng API: [Địa chỉ hai cấp](https://developer.ghn.dev/vi/docs/master-data/get-province-new), [xem trước không tạo đơn](https://developer.ghn.dev/vi/docs/order/preview), [tạo đơn và mã yêu cầu chống trùng](https://developer.ghn.dev/vi/docs/order/create), [tra cứu vận đơn](https://developer.ghn.dev/vi/docs/order/info). Code dùng hai host cố định của GHN, không theo redirect và không gửi khóa ra trình duyệt.

## Khi gặp lỗi

- Thanh toán không đổi trạng thái: kiểm tra IPN truy cập được từ internet, chữ ký, TmnCode và log; không sửa tay thành đã trả chỉ vì return URL báo thành công.
- Email không gửi: kiểm tra App Password, giới hạn tài khoản, STARTTLS và thư spam; không đăng log chứa mật khẩu/token.
- Phí giao hàng sai: kiểm tra môi trường, ShopId, địa chỉ và khối lượng; báo giá dự kiến phải phân biệt với phí đã được cửa hàng xác nhận.
- Đổi khóa nếu từng bị lộ; xóa biến môi trường sau phiên thử trên máy dùng chung.
