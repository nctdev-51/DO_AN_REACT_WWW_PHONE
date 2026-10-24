# SƠ ĐỒ SEQUENCE (PlantUML) - PHIÊN BẢN CUỐI CÙNG

> **Cấu trúc:**
> - `actor` - Admin (hình người)
> - `boundary` - View JSP (hình tròn với đường nét)
> - `control` - Controller (hình tròn với mũi tên)
> - `entity` - Các entity nghiệp vụ: Order, User, OrderDetail, Product (hình tròn)

---

## 1. Sequence Diagram - Xem danh sách đơn hàng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện danh sách đơn hàng" as View #7ACFF5
control "AdminOrderController" as Controller #7ACFF5
entity "Order" as Order #7ACFF5
entity "User" as User #7ACFF5

Admin -> View: 1. Truy cập trang "Quản lý đơn hàng"
activate View

View -> Controller: 2. Gửi yêu cầu xem danh sách đơn hàng
activate Controller

Controller -> Order: 3. Truy vấn tất cả đơn hàng\n(sắp xếp theo ngày đặt hàng)
activate Order

Order -> User: 4. Lấy thông tin khách hàng
activate User
User --> Order: 5. Trả về thông tin khách hàng\n(họ tên, email, SĐT)
deactivate User

Order --> Controller: 6. Trả về danh sách đơn hàng
deactivate Order

alt Danh sách có dữ liệu
    Controller -> Controller: 7. Chuẩn bị dữ liệu hiển thị
    Controller -> View: 8. Truyền danh sách đơn hàng
    View -> View: 9. Xây dựng giao diện danh sách
    View --> Admin: 10. Hiển thị danh sách đơn hàng\n(mã, khách hàng, ngày, tổng tiền, trạng thái)
else Danh sách trống
    Controller -> Controller: 11. Chuẩn bị thông báo
    Controller -> View: 12. Truyền danh sách rỗng
    View -> View: 13. Kiểm tra không có dữ liệu
    View --> Admin: 14. Hiển thị thông báo\n"Chưa có đơn hàng nào"
end

deactivate View
deactivate Controller
@enduml
```

---

## 2. Sequence Diagram - Xem chi tiết đơn hàng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện danh sách đơn hàng" as ListView #7ACFF5
control "AdminOrderController" as Controller #7ACFF5
entity "Order" as Order #7ACFF5
entity "OrderDetail" as OrderDetail #7ACFF5
entity "Product" as Product #7ACFF5
boundary "Giao diện chi tiết đơn hàng" as DetailView #7ACFF5

Admin -> ListView: 1. Click vào mã đơn hàng\nhoặc nút "Xem chi tiết"
activate ListView

ListView -> Controller: 2. Gửi yêu cầu xem chi tiết đơn hàng
activate Controller

Controller -> Order: 3. Truy vấn đơn hàng theo mã
activate Order

alt Tìm thấy đơn hàng
    Order --> Controller: 4. Trả về thông tin đơn hàng
    
    Controller -> OrderDetail: 5. Truy vấn chi tiết đơn hàng
    activate OrderDetail
    
    OrderDetail -> Product: 6. Lấy thông tin sản phẩm
    activate Product
    Product --> OrderDetail: 7. Trả về thông tin sản phẩm\n(tên, hình ảnh, giá)
    deactivate Product
    
    OrderDetail --> Controller: 8. Trả về danh sách chi tiết
    deactivate OrderDetail
    deactivate Order
    
    Controller -> Controller: 9. Chuẩn bị dữ liệu hiển thị
    Controller -> DetailView: 10. Truyền thông tin đơn hàng\nvà chi tiết sản phẩm
    activate DetailView
    
    DetailView -> DetailView: 12. Xây dựng giao diện chi tiết
    DetailView --> Admin: 13. Hiển thị chi tiết đơn hàng\n(thông tin khách hàng, danh sách sản phẩm,\ngiá, số lượng, tổng tiền)
    deactivate DetailView
    
else Không tìm thấy đơn hàng
    Order --> Controller: 14. Thông báo không tìm thấy
    deactivate Order
    
    Controller --> ListView: 15. Chuyển về trang danh sách
    activate ListView
    ListView --> Admin: 16. Hiển thị thông báo\n"Không tìm thấy đơn hàng"
    deactivate ListView
end

deactivate Controller
deactivate ListView
@enduml
```

---

## 3. Sequence Diagram - Cập nhật trạng thái đơn hàng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện quản lý đơn hàng" as View #7ACFF5
control "AdminOrderController" as Controller #7ACFF5
entity "Order" as Order #7ACFF5

Admin -> View: 1. Chọn trạng thái mới\nvà click "Cập nhật"
activate View

View -> Controller: 2. Gửi yêu cầu cập nhật trạng thái\n(mã đơn hàng, trạng thái mới)
activate Controller

Controller -> Order: 3. Truy vấn đơn hàng theo mã
activate Order

alt Tìm thấy đơn hàng
    Order --> Controller: 4. Trả về thông tin đơn hàng\nvà trạng thái hiện tại
    
    Controller -> Controller: 5. Kiểm tra tính hợp lệ\ncủa chuyển đổi trạng thái
    Controller -> Controller: 6. Áp dụng quy tắc State Machine
    
    alt Chuyển đổi hợp lệ
        Controller -> Order: 7. Cập nhật trạng thái mới
        Order -> Order: 8. Lưu trạng thái
        Order --> Controller: 9. Xác nhận thành công
        deactivate Order
        
        Controller -> View: 10. Thông báo cập nhật thành công
        View --> Admin: 11. Hiển thị thông báo\n"Đã cập nhật trạng thái đơn hàng"
        
    else Chuyển đổi không hợp lệ
        Order --> Controller: 12. Từ chối cập nhật
        deactivate Order
        
        Controller -> View: 13. Thông báo lỗi
        View --> Admin: 14. Hiển thị thông báo lỗi\n"Không thể chuyển từ [X] sang [Y]"
    end
    
else Không tìm thấy đơn hàng
    Order --> Controller: 15. Thông báo không tìm thấy
    deactivate Order
    
    Controller -> View: 16. Thông báo lỗi
    View --> Admin: 17. Hiển thị thông báo lỗi\n"Không tìm thấy đơn hàng"
end

deactivate View
deactivate Controller
@enduml
```

---

## 4. Sequence Diagram - Xem danh sách người dùng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện danh sách người dùng" as View #7ACFF5
control "AdminUserController" as Controller #7ACFF5
entity "User" as User #7ACFF5
entity "Order" as Order #7ACFF5

Admin -> View: 1. Truy cập trang "Quản lý người dùng"
activate View

View -> Controller: 2. Gửi yêu cầu xem danh sách người dùng
activate Controller

Controller -> User: 3. Truy vấn tất cả người dùng
activate User

User --> Controller: 4. Trả về danh sách người dùng

loop Với mỗi người dùng
    Controller -> Order: 5. Đếm số đơn hàng của người dùng
    activate Order
    Order --> Controller: 6. Trả về số lượng đơn hàng
    deactivate Order
    
    Controller -> Controller: 7. Gắn thông tin số đơn hàng
end

deactivate User

alt Danh sách có dữ liệu
    Controller -> Controller: 8. Chuẩn bị dữ liệu hiển thị
    Controller -> View: 9. Truyền danh sách người dùng
    View -> View: 10. Xây dựng giao diện danh sách
    View --> Admin: 11. Hiển thị danh sách người dùng\n(họ tên, email, SĐT, trạng thái, số đơn hàng)\nvới nút "Sửa" và "Xóa"
else Danh sách trống
    Controller -> Controller: 12. Chuẩn bị thông báo
    Controller -> View: 13. Truyền danh sách rỗng
    View -> View: 14. Kiểm tra không có dữ liệu
    View --> Admin: 15. Hiển thị thông báo\n"Chưa có người dùng nào"
end

deactivate View
deactivate Controller
@enduml
```

---

## 5. Sequence Diagram - Cập nhật thông tin người dùng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện danh sách người dùng" as ListView #7ACFF5
control "AdminUserController" as Controller #7ACFF5
entity "User" as User #7ACFF5
boundary "Giao diện chỉnh sửa người dùng" as EditView #7ACFF5

== Hiển thị form chỉnh sửa ==

Admin -> ListView: 1. Click nút "Sửa" tại người dùng
activate ListView

ListView -> Controller: 2. Gửi yêu cầu chỉnh sửa người dùng
activate Controller

Controller -> User: 3. Truy vấn người dùng theo mã
activate User

alt Tìm thấy người dùng
    User --> Controller: 4. Trả về thông tin người dùng
    deactivate User
    
    Controller -> EditView: 5. Truyền thông tin người dùng
    activate EditView
    
    EditView -> EditView: 6. Xây dựng form chỉnh sửa
    EditView --> Admin: 7. Hiển thị form chỉnh sửa\n(họ tên, SĐT, địa chỉ, trạng thái)
    deactivate EditView
    deactivate Controller
    deactivate ListView
    
    == Lưu thay đổi ==
    
    Admin -> EditView: 8. Chỉnh sửa thông tin và click "Lưu"
    activate EditView
    
    EditView -> Controller: 9. Gửi dữ liệu cập nhật\n(họ tên, SĐT, địa chỉ, trạng thái)
    activate Controller
    
    Controller -> Controller: 10. Kiểm tra tính hợp lệ dữ liệu
    Controller -> Controller: 11. Kiểm tra họ tên không trống
    Controller -> Controller: 12. Kiểm tra định dạng SĐT\n(bắt đầu bằng 0, có 10 chữ số)
    
    alt Họ tên trống
        Controller -> EditView: 13. Thông báo lỗi validation
        EditView --> Admin: 14. Hiển thị lỗi\n"Họ tên không được để trống"
        deactivate EditView
        deactivate Controller
        
    else SĐT sai định dạng
        Controller -> EditView: 15. Thông báo lỗi validation
        EditView --> Admin: 16. Hiển thị lỗi\n"Số điện thoại không đúng định dạng"
        deactivate EditView
        deactivate Controller
        
    else Dữ liệu hợp lệ
        Controller -> User: 17. Truy vấn người dùng để cập nhật
        activate User
        User --> Controller: 18. Trả về thông tin người dùng
        
        Controller -> User: 19. Cập nhật thông tin\n(họ tên, SĐT, địa chỉ, trạng thái)
        User -> User: 20. Lưu thông tin mới
        User --> Controller: 21. Xác nhận thành công
        deactivate User
        
        Controller -> ListView: 22. Chuyển về trang danh sách
        activate ListView
        ListView --> Admin: 23. Hiển thị thông báo thành công\n"Đã cập nhật thông tin người dùng"
        deactivate ListView
        deactivate Controller
    end
    
else Không tìm thấy người dùng
    User --> Controller: 24. Thông báo không tìm thấy
    deactivate User
    
    Controller -> ListView: 25. Chuyển về trang danh sách
    activate ListView
    ListView --> Admin: 26. Hiển thị thông báo lỗi\n"Không tìm thấy người dùng"
    deactivate ListView
    deactivate Controller
end
@enduml
```

---

## 6. Sequence Diagram - Xóa người dùng

```plantuml
@startuml
skinparam sequence {
    LifeLineBackgroundColor #7ACFF5
    ActorBackgroundColor #7ACFF5
}

actor Admin
boundary "Giao diện quản lý người dùng" as View #7ACFF5
boundary "Hộp thoại xác nhận" as Dialog #7ACFF5
control "AdminUserController" as Controller #7ACFF5
entity "User" as User #7ACFF5
entity "Role" as Role #7ACFF5
entity "Order" as Order #7ACFF5

Admin -> View: 1. Click nút "Xóa" tại người dùng
activate View

View -> Dialog: 2. Hiển thị hộp thoại xác nhận
activate Dialog

Dialog --> Admin: 3. "Bạn có chắc chắn muốn xóa\nngười dùng này?"

alt Admin chọn "Hủy"
    Admin -> Dialog: 4. Click "Hủy"
    Dialog -> Dialog: 5. Đóng hộp thoại
    Dialog --> Admin: 6. Hủy thao tác xóa
    deactivate Dialog
    deactivate View
    
else Admin chọn "Đồng ý"
    Admin -> Dialog: 7. Click "Đồng ý"
    deactivate Dialog
    
    View -> Controller: 8. Gửi yêu cầu xóa người dùng
    activate Controller
    
    Controller -> User: 9. Truy vấn người dùng theo mã
    activate User
    
    alt Tìm thấy người dùng
        User --> Controller: 10. Trả về thông tin người dùng
        
        Controller -> Role: 11. Kiểm tra vai trò của người dùng
        activate Role
        Role --> Controller: 12. Trả về thông tin vai trò
        deactivate Role
        
        alt Người dùng có vai trò ADMIN
            Controller -> Controller: 13. Kiểm tra ràng buộc bảo mật
            deactivate User
            
            Controller -> View: 14. Từ chối xóa
            View --> Admin: 15. Hiển thị lỗi bảo mật\n"Không thể xóa tài khoản Admin"
            
        else Người dùng đang đăng nhập
            Controller -> Controller: 16. Kiểm tra người dùng hiện tại
            deactivate User
            
            Controller -> View: 17. Từ chối xóa
            View --> Admin: 18. Hiển thị lỗi\n"Không thể xóa chính mình"
            
        else Người dùng thường
            Controller -> Order: 19. Kiểm tra đơn hàng của người dùng
            activate Order
            Order --> Controller: 20. Trả về số lượng đơn hàng
            deactivate Order
            deactivate User
            
            alt Chưa có đơn hàng
                Controller -> User: 21. Xóa người dùng
                activate User
                User -> User: 22. Xóa dữ liệu liên quan
                User --> Controller: 23. Xác nhận đã xóa
                deactivate User
                
                Controller -> View: 24. Thông báo thành công
                View --> Admin: 25. Hiển thị thông báo\n"Đã xóa người dùng"
                
            else Đã có đơn hàng
                Controller -> View: 26. Từ chối xóa
                View --> Admin: 27. Hiển thị lỗi ràng buộc\n"Không thể xóa người dùng\nđã có đơn hàng"
            end
        end
        
    else Không tìm thấy người dùng
        User --> Controller: 28. Thông báo không tìm thấy
        deactivate User
        
        Controller -> View: 29. Thông báo lỗi
        View --> Admin: 30. Hiển thị thông báo lỗi\n"Không tìm thấy người dùng"
    end
    
    deactivate View
    deactivate Controller
end
@enduml
```

---

## Hướng dẫn sử dụng

### Hình dạng participants:
- `actor` - Hình người (Admin)
- `boundary` - Hình tròn với đường nét (Giao diện người dùng, Hộp thoại)
- `control` - Hình tròn với mũi tên (Controller xử lý nghiệp vụ)
- `entity` - Hình tròn (Order, User, OrderDetail, Product, Role)

### Màu sắc:
- Background màu #7ACFF5 cho tất cả participants (View, Controller, Entity)
- LifeLineBackgroundColor #7ACFF5
- ActorBackgroundColor #7ACFF5

### Cấu trúc đơn giản:
- Chỉ 4 loại: actor, boundary, control, entity
- Entity là các đối tượng nghiệp vụ cụ thể
- Boundary là các giao diện người dùng
- Control là các thành phần xử lý logic
- Không có stereotype (<<View>>, <<Controller>>, <<Entity>>)
- Không có tầng Service, Repository, Database riêng
- Không có chi tiết kỹ thuật implementation (JSP, SQL, etc.)

**Test tại:** https://www.plantuml.com/plantuml/uml/
