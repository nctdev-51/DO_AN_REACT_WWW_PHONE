# SƠ ĐỒ SEQUENCE (PlantUML) - CẬP NHẬT

> **Lưu ý:** Tất cả sơ đồ đã được cập nhật với:
> - ✅ Thêm tầng Service đầy đủ
> - ✅ Sử dụng `entity` để có hình tròn cho participants
> - ✅ Đánh số bước rõ ràng
> - ✅ SQL queries chi tiết

---

## 1. Sequence Diagram - Xem danh sách đơn hàng

```plantuml
@startuml
skinparam participantPadding 20
skinparam boxPadding 10
skinparam sequence {
    ArrowColor DeepSkyBlue
    ActorBorderColor DeepSkyBlue
    LifeLineBorderColor blue
    LifeLineBackgroundColor #A9DCDF
    
    ParticipantBorderColor DeepSkyBlue
    ParticipantBackgroundColor DodgerBlue
    ParticipantFontColor #FFFFFF
    
    ActorBackgroundColor aqua
    ActorFontColor DeepSkyBlue
    ActorFontSize 17
    ActorFontName Aapex
}

actor Admin
boundary "<<View>>\nadmin/orders.jsp" as View
control "<<Controller>>\nAdminOrderController" as Controller
entity "<<Service>>\nOrderService" as Service
entity "<<Repository>>\nOrderRepository" as Repository
database "<<Database>>" as DB

Admin -> View: 1. Truy cập trang "Quản lý đơn hàng"
activate View

View -> Controller: 2. HTTP GET Request /admin/orders
activate Controller

Controller -> Service: 3. getAllOrders()
activate Service

Service -> Repository: 4. findAll(Sort.by("orderDate").descending())
activate Repository

Repository -> DB: 5. SQL Query\nSELECT o.*, u.full_name, u.email, u.phone\nFROM orders o LEFT JOIN users u\nORDER BY o.order_date DESC
activate DB
DB --> Repository: 6. ResultSet (raw records)
deactivate DB

Repository --> Service: 7. List<Order> (mapped entities)
deactivate Repository

Service --> Controller: 8. List<Order>
deactivate Service

alt Danh sách không rỗng (size > 0)
    Controller -> Controller: 9. Chuẩn bị Model với danh sách đơn hàng
    Controller -> View: 10. model.addAttribute("orders", orders)\n11. Trả về view "admin/orders"
    View -> View: 12. Render danh sách đơn hàng
    View --> Admin: 13. Hiển thị bảng danh sách\n(mã, khách hàng, ngày, tổng tiền, trạng thái)
else Danh sách rỗng (size == 0)
    Controller -> Controller: 14. Chuẩn bị Model với danh sách trống
    Controller -> View: 15. model.addAttribute("orders", emptyList)\n16. Trả về view "admin/orders"
    View -> View: 17. Kiểm tra danh sách rỗng
    View --> Admin: 18. Hiển thị thông báo\n"Chưa có đơn hàng nào"
end

deactivate View
deactivate Controller
@enduml
```

---

## 2. Sequence Diagram - Xem chi tiết đơn hàng

```plantuml
@startuml
skinparam participantPadding 20
skinparam boxPadding 10

actor "Admin" as Admin
entity "<<View>>\nadmin/orders.jsp" as ListView
entity "<<Controller>>\nAdminOrderController" as Controller
entity "<<Service>>\nOrderService" as Service
entity "<<Repository>>\nOrderRepository" as Repository
database "<<Database>>\nMySQL" as DB
entity "<<View>>\nadmin/order-detail.jsp" as DetailView

Admin -> ListView: 1. Click vào mã đơn hàng\nhoặc nút "Xem chi tiết"
activate ListView

ListView -> Controller: 2. HTTP GET Request /admin/orders/{id}
activate Controller

Controller -> Service: 3. getOrderById(id)
activate Service

Service -> Repository: 4. findById(id)
activate Repository

Repository -> DB: 5. SQL Query
activate DB
note right of DB
  SELECT * FROM orders
  WHERE id = ?
end note
DB --> Repository: 6. ResultSet (order record)
deactivate DB

Repository --> Service: 7. Optional<Order>
deactivate Repository

alt Tìm thấy đơn hàng
    Service -> Repository: 8. Eager load OrderDetails với Product
    activate Repository
    
    Repository -> DB: 9. SQL Query
    activate DB
    note right of DB
      SELECT od.*, p.name, p.image, p.price
      FROM order_details od
      JOIN products p ON od.product_id = p.id
      WHERE od.order_id = ?
    end note
    DB --> Repository: 10. ResultSet (order details)
    deactivate DB
    
    Repository --> Service: 11. List<OrderDetail>
    deactivate Repository
    
    Service --> Controller: 12. Order với OrderDetails
    deactivate Service
    
    Controller -> Controller: 13. Chuẩn bị Model với đơn hàng và chi tiết
    Controller -> DetailView: 14. model.addAttribute("order", order)\n15. model.addAttribute("orderItems", orderDetails)\n16. Trả về view "admin/order-detail"
    activate DetailView
    
    DetailView -> DetailView: 17. Render trang chi tiết
    DetailView --> Admin: 18. Hiển thị chi tiết đơn hàng:\n- Thông tin đơn hàng (mã, ngày, trạng thái)\n- Thông tin khách hàng\n- Danh sách sản phẩm\n- Tổng tiền
    deactivate DetailView
    
else Không tìm thấy đơn hàng
    Service --> Controller: 19. Optional.empty()
    deactivate Service
    
    Controller --> ListView: 20. redirect:/admin/orders
    activate ListView
    ListView --> Admin: 21. Hiển thị thông báo\n"Không tìm thấy đơn hàng"
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
skinparam participantPadding 20
skinparam boxPadding 10

actor "Admin" as Admin
entity "<<View>>\nadmin/orders.jsp" as View
entity "<<Controller>>\nAdminOrderController" as Controller
entity "<<Service>>\nOrderService" as Service
entity "<<Repository>>\nOrderRepository" as Repository
database "<<Database>>\nMySQL" as DB

Admin -> View: 1. Chọn trạng thái mới từ dropdown\nvà click "Cập nhật trạng thái"
activate View

View -> Controller: 2. HTTP POST Request\n/admin/orders/{id}/status\nstatus="Đang giao"
activate Controller

Controller -> Service: 3. updateOrderStatus(id, newStatus)
activate Service

Service -> Repository: 4. findById(id)
activate Repository

Repository -> DB: 5. SQL Query
activate DB
note right of DB
  SELECT * FROM orders
  WHERE id = ?
end note
DB --> Repository: 6. ResultSet
deactivate DB

Repository --> Service: 7. Optional<Order>
deactivate Repository

alt Tìm thấy đơn hàng
    Service -> Service: 8. Lấy trạng thái hiện tại\ncurrentStatus = order.getStatus()
    
    Service -> Service: 9. validateStateTransition(\ncurrentStatus, newStatus)
    
    Service -> Service: 10. Kiểm tra State Machine Rules
    note right of Service
      State Machine Rules:
      - Chờ xử lý → Đang giao, Đã hủy
      - Đang giao → Đã giao, Đã hủy
      - Đã giao: không đổi được
      - Đã hủy: không đổi được
    end note
    
    alt Chuyển đổi hợp lệ
        Service -> Service: 11. order.setStatus("Đang giao")
        
        Service -> Repository: 12. save(order)
        activate Repository
        
        Repository -> DB: 13. SQL Update
        activate DB
        note right of DB
          UPDATE orders
          SET status = 'Đang giao'
          WHERE id = ?
        end note
        DB --> Repository: 14. Success
        deactivate DB
        
        Repository --> Service: 15. Order đã cập nhật
        deactivate Repository
        
        Service --> Controller: 16. Order đã cập nhật
        deactivate Service
        
        Controller -> View: 17. redirect:/admin/orders\nFlash: "Cập nhật trạng thái đơn #[id]\nthành 'Đang giao' thành công"
        View --> Admin: 18. Hiển thị thông báo thành công\nvà làm mới danh sách
        
    else Chuyển đổi không hợp lệ (vi phạm State Machine)
        Service --> Controller: 19. throw InvalidStateTransitionException
        deactivate Service
        
        Controller -> View: 20. redirect:/admin/orders\nFlash: "Không thể chuyển từ\n'[Trạng thái cũ]' sang '[Trạng thái mới]'"
        View --> Admin: 21. Hiển thị thông báo lỗi
    end
    
else Không tìm thấy đơn hàng
    Service --> Controller: 22. throw OrderNotFoundException
    deactivate Service
    
    Controller -> View: 23. redirect:/admin/orders\nFlash: "Không tìm thấy đơn hàng"
    View --> Admin: 24. Hiển thị thông báo lỗi
end

deactivate View
deactivate Controller
@enduml
```

---

## 4. Sequence Diagram - Xem danh sách người dùng

```plantuml
@startuml
skinparam participantPadding 20
skinparam boxPadding 10

actor "Admin" as Admin
entity "<<View>>\nadmin/users.jsp" as View
entity "<<Controller>>\nAdminUserController" as Controller
entity "<<Service>>\nUserService" as Service
entity "<<Repository>>\nUserRepository" as Repository
entity "<<Repository>>\nOrderRepository" as OrderRepo
database "<<Database>>\nMySQL" as DB

Admin -> View: 1. Truy cập trang "Quản lý người dùng"
activate View

View -> Controller: 2. HTTP GET Request /admin/users
activate Controller

Controller -> Service: 3. getAllUsersWithOrderCount()
activate Service

Service -> Repository: 4. findAll()
activate Repository

Repository -> DB: 5. SQL Query
activate DB
note right of DB
  SELECT * FROM users
end note
DB --> Repository: 6. ResultSet (user records)
deactivate DB

Repository --> Service: 7. List<User>
deactivate Repository

loop Cho mỗi User trong List
    Service -> OrderRepo: 8. countByUserId(user.id)
    activate OrderRepo
    
    OrderRepo -> DB: 9. SQL Query
    activate DB
    note right of DB
      SELECT COUNT(*) as order_count
      FROM orders
      WHERE user_id = ?
    end note
    DB --> OrderRepo: 10. orderCount
    deactivate DB
    
    OrderRepo --> Service: 11. orderCount
    deactivate OrderRepo
    
    Service -> Service: 12. Gắn orderCount vào User
end

Service --> Controller: 13. List<User> (with orderCount)
deactivate Service

alt Danh sách không rỗng (size > 0)
    Controller -> Controller: 14. Chuẩn bị Model với danh sách người dùng
    Controller -> View: 15. model.addAttribute("users", users)\n16. Trả về view "admin/users"
    View -> View: 17. Render danh sách người dùng
    View --> Admin: 18. Hiển thị bảng danh sách người dùng\n(ID, họ tên, email, SĐT, địa chỉ,\ntrạng thái, số đơn hàng)\nvới nút "Sửa" và "Xóa"
else Danh sách rỗng (size == 0)
    Controller -> Controller: 19. Chuẩn bị Model với danh sách trống
    Controller -> View: 20. model.addAttribute("users", emptyList)\n21. Trả về view "admin/users"
    View -> View: 22. Kiểm tra danh sách rỗng
    View --> Admin: 23. Hiển thị thông báo\n"Chưa có người dùng nào"
end

deactivate View
deactivate Controller
@enduml
```

---

## 5. Sequence Diagram - Cập nhật thông tin người dùng

```plantuml
@startuml
skinparam participantPadding 20
skinparam boxPadding 10

actor "Admin" as Admin
entity "<<View>>\nadmin/users.jsp" as ListView
entity "<<Controller>>\nAdminUserController" as Controller
entity "<<Service>>\nUserService" as Service
entity "<<Validator>>\nUserValidator" as Validator
entity "<<Repository>>\nUserRepository" as Repository
database "<<Database>>\nMySQL" as DB
entity "<<View>>\nadmin/user-edit.jsp" as EditView

== Hiển thị form chỉnh sửa ==

Admin -> ListView: 1. Click nút "Sửa"\ntại người dùng
activate ListView

ListView -> Controller: 2. HTTP GET Request\n/admin/users/{id}/edit
activate Controller

Controller -> Service: 3. getUserById(id)
activate Service

Service -> Repository: 4. findById(id)
activate Repository

Repository -> DB: 5. SQL Query
activate DB
note right of DB
  SELECT * FROM users
  WHERE id = ?
end note
DB --> Repository: 6. ResultSet
deactivate DB

Repository --> Service: 7. Optional<User>
deactivate Repository

alt Tìm thấy người dùng
    Service --> Controller: 8. User
    deactivate Service
    
    Controller -> EditView: 9. model.addAttribute("user", user)\n10. Trả về view "admin/user-edit"
    activate EditView
    
    EditView -> EditView: 11. Render form chỉnh sửa
    EditView --> Admin: 12. Hiển thị form chỉnh sửa\n(họ tên, SĐT, địa chỉ, trạng thái)\nEmail: read-only
    deactivate EditView
    deactivate Controller
    deactivate ListView
    
    == Lưu thay đổi ==
    
    Admin -> EditView: 13. Chỉnh sửa thông tin\nvà click "Lưu thay đổi"
    activate EditView
    
    EditView -> Controller: 14. HTTP POST Request\n/admin/users/{id}/edit\nfullName, phone, address, status
    activate Controller
    
    Controller -> Validator: 15. validate(fullName, phone)
    activate Validator
    
    Validator -> Validator: 16. Kiểm tra họ tên không trống
    Validator -> Validator: 17. Kiểm tra SĐT đúng regex ^0[0-9]{9}$
    
    alt Họ tên trống
        Validator --> Controller: 18. ValidationError:\n"Họ tên không được để trống"
        deactivate Validator
        
        Controller -> EditView: 19. model.addAttribute("errors", errors)\n20. Trả về view "admin/user-edit"
        EditView --> Admin: 21. Hiển thị form với lỗi\ntại trường họ tên
        deactivate EditView
        deactivate Controller
        
    else SĐT sai định dạng (không khớp ^0[0-9]{9}$)
        Validator --> Controller: 22. ValidationError:\n"SĐT phải gồm 10 chữ số\nvà bắt đầu bằng 0"
        deactivate Validator
        
        Controller -> EditView: 23. model.addAttribute("errors", errors)\n24. Trả về view "admin/user-edit"
        EditView --> Admin: 25. Hiển thị form với lỗi\ntại trường SĐT
        deactivate EditView
        deactivate Controller
        
    else Dữ liệu hợp lệ
        Validator --> Controller: 26. Validation passed
        deactivate Validator
        
        Controller -> Service: 27. updateUser(id, fullName, phone, address, status)
        activate Service
        
        Service -> Repository: 28. findById(id)
        activate Repository
        Repository -> DB: 29. SQL Query
        activate DB
        note right of DB
          SELECT * FROM users WHERE id = ?
        end note
        DB --> Repository: 30. ResultSet
        deactivate DB
        Repository --> Service: 31. Optional<User>
        deactivate Repository
        
        Service -> Service: 32. user.setFullName(fullName)\nuser.setPhone(phone)\nuser.setAddress(address)\nuser.setStatus(status)
        
        Service -> Repository: 33. save(user)
        activate Repository
        
        Repository -> DB: 34. SQL Update
        activate DB
        note right of DB
          UPDATE users SET
            full_name = ?,
            phone = ?,
            address = ?,
            status = ?
          WHERE id = ?
        end note
        DB --> Repository: 35. Success
        deactivate DB
        
        Repository --> Service: 36. User đã cập nhật
        deactivate Repository
        
        Service --> Controller: 37. User đã cập nhật
        deactivate Service
        
        Controller -> ListView: 38. redirect:/admin/users\nFlash: "Cập nhật thông tin\nngười dùng '[Họ tên]' thành công"
        activate ListView
        ListView --> Admin: 39. Hiển thị thông báo thành công\nvà danh sách người dùng đã cập nhật
        deactivate ListView
        deactivate Controller
    end
    
else Không tìm thấy người dùng
    Service --> Controller: 40. throw UserNotFoundException
    deactivate Service
    
    Controller -> ListView: 41. redirect:/admin/users\nFlash: "Không tìm thấy người dùng"
    activate ListView
    ListView --> Admin: 42. Hiển thị thông báo lỗi
    deactivate ListView
    deactivate Controller
end
@enduml
```

---

## 6. Sequence Diagram - Xóa người dùng

```plantuml
@startuml
skinparam participantPadding 20
skinparam boxPadding 10

actor "Admin" as Admin
entity "<<View>>\nadmin/users.jsp" as View
entity "<<JavaScript>>\nConfirmDialog" as Dialog
entity "<<Controller>>\nAdminUserController" as Controller
entity "<<Service>>\nUserService" as Service
entity "<<Repository>>\nUserRepository" as Repository
entity "<<Repository>>\nOrderRepository" as OrderRepo
entity "<<Repository>>\nRoleRepository" as RoleRepo
database "<<Database>>\nMySQL" as DB

Admin -> View: 1. Click nút "Xóa"\ntại người dùng
activate View

View -> Dialog: 2. Hiển thị confirm dialog
activate Dialog

Dialog --> Admin: 3. Hiển thị hộp thoại:\n"Bạn có chắc chắn muốn xóa\nngười dùng '[Họ tên]' không?\nHành động này không thể hoàn tác."

alt Admin chọn "Hủy"
    Admin -> Dialog: 4. Click "Hủy"
    Dialog -> Dialog: 5. Đóng dialog
    Dialog --> Admin: 6. Không thực hiện xóa
    deactivate Dialog
    deactivate View
    
else Admin chọn "Đồng ý"
    Admin -> Dialog: 7. Click "Đồng ý"
    deactivate Dialog
    
    View -> Controller: 8. HTTP GET Request\n/admin/users/{id}/delete
    activate Controller
    
    Controller -> Service: 9. deleteUser(id, currentAdminId)
    activate Service
    
    Service -> Repository: 10. findById(id)
    activate Repository
    
    Repository -> DB: 11. SQL Query
    activate DB
    note right of DB
      SELECT * FROM users
      WHERE id = ?
    end note
    DB --> Repository: 12. ResultSet
    deactivate DB
    
    Repository --> Service: 13. Optional<User>
    deactivate Repository
    
    alt Tìm thấy người dùng
        Service -> RoleRepo: 14. hasRole(user, "ADMIN")
        activate RoleRepo
        
        RoleRepo -> DB: 15. SQL Query
        activate DB
        note right of DB
          SELECT COUNT(*) FROM user_roles ur
          JOIN roles r ON ur.role_id = r.id
          WHERE ur.user_id = ?
          AND r.name = 'ADMIN'
        end note
        DB --> RoleRepo: 16. count
        deactivate DB
        
        RoleRepo --> Service: 17. isAdmin (boolean)
        deactivate RoleRepo
        
        alt Người dùng có role ADMIN
            Service --> Controller: 18. throw CannotDeleteAdminException
            deactivate Service
            
            Controller -> View: 19. redirect:/admin/users\nFlash: "Không thể xóa tài khoản Admin\n'[Họ tên]' do ràng buộc bảo mật"
            View --> Admin: 20. Hiển thị thông báo lỗi
            note right of Service
              Ràng buộc bảo mật:
              - Không xóa user có role ADMIN
              - Không tự xóa chính mình
            end note
            
        else Người dùng là chính Admin đang đăng nhập
            Service -> Service: 21. Kiểm tra\nif (user.id == currentAdminId)
            Service --> Controller: 22. throw CannotDeleteSelfException
            deactivate Service
            
            Controller -> View: 23. redirect:/admin/users\nFlash: "Không thể tự xóa\ntài khoản của chính mình"
            View --> Admin: 24. Hiển thị thông báo lỗi
            
        else Người dùng thường (không phải Admin)
            Service -> OrderRepo: 25. countByUserId(id)
            activate OrderRepo
            
            OrderRepo -> DB: 26. SQL Query
            activate DB
            note right of DB
              SELECT COUNT(*) FROM orders
              WHERE user_id = ?
            end note
            DB --> OrderRepo: 27. orderCount
            deactivate DB
            
            OrderRepo --> Service: 28. orderCount
            deactivate OrderRepo
            
            alt Chưa có đơn hàng (orderCount = 0)
                Service -> Repository: 29. deleteById(id)
                activate Repository
                
                Repository -> DB: 30. SQL Delete (user_roles)
                activate DB
                note right of DB
                  DELETE FROM user_roles
                  WHERE user_id = ?
                end note
                deactivate DB
                
                Repository -> DB: 31. SQL Delete (users)
                activate DB
                note right of DB
                  DELETE FROM users
                  WHERE id = ?
                end note
                DB --> Repository: 32. Success
                deactivate DB
                
                Repository --> Service: 33. void
                deactivate Repository
                
                Service --> Controller: 34. Success
                deactivate Service
                
                Controller -> View: 35. redirect:/admin/users\nFlash: "Đã xóa người dùng\n'[Họ tên]' thành công"
                View --> Admin: 36. Hiển thị thông báo thành công\nvà làm mới danh sách
                
            else Đã có đơn hàng (orderCount > 0)
                Service --> Controller: 37. throw UserHasOrdersException
                deactivate Service
                
                Controller -> View: 38. redirect:/admin/users\nFlash: "Không thể xóa '[Họ tên]'\nvì đã có [orderCount] đơn hàng"
                View --> Admin: 39. Hiển thị thông báo lỗi
                note right of Service
                  Ràng buộc dữ liệu:
                  Chỉ xóa được người dùng
                  chưa từng đặt hàng
                end note
            end
        end
        
    else Không tìm thấy người dùng
        Service --> Controller: 40. throw UserNotFoundException
        deactivate Service
        
        Controller -> View: 41. redirect:/admin/users\nFlash: "Không tìm thấy người dùng"
        View --> Admin: 42. Hiển thị thông báo lỗi
    end
    
    deactivate View
    deactivate Controller
end
@enduml
```

---

## Hướng dẫn sử dụng

1. Copy từng đoạn code PlantUML vào file `.md` gốc của bạn
2. Hoặc test trực tiếp tại: https://www.plantuml.com/plantuml/uml/
3. Các sơ đồ sẽ hiển thị với:
   - **Hình tròn** cho tất cả participants (nhờ dùng `entity`)
   - **Tầng Service** đầy đủ trong mọi luồng xử lý
   - **SQL queries** chi tiết trong notes
   - **Đánh số bước** rõ ràng

## Lưu ý kỹ thuật

- `skinparam participantPadding 20` - Tăng khoảng cách giữa các participants
- `skinparam boxPadding 10` - Tăng padding cho các box
- `entity` thay vì `participant` - Tạo hình tròn
- `actor` - Giữ nguyên cho Admin (hình người)
- `database` - Giữ nguyên cho MySQL (hình cylinder)
