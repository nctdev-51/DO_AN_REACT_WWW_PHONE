package iuh.fit.www_phone_ver1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    // Danh sách trạng thái hợp lệ dùng chung
    private static final List<String> ORDER_STATUSES = List.of(
            "Chờ xử lý", "Đang giao", "Đã giao", "Đã hủy"
    );

    // =====================================================================
    // Data giả dùng chung cho cả danh sách và chi tiết
    // Mỗi phần tử: [id, tenKhach, email, sdt, ngayDat, diaChiGiao, tongTien, trangThai]
    // Sẽ thay bằng orderRepository.findAll() ở Task 7
    // =====================================================================
    private List<Object[]> getMockOrders() {
        return List.of(
            new Object[]{1L, "Nguyễn Văn An",  "an@gmail.com",    "0901234567", "25/09/2026", "12 Lý Thường Kiệt, Q.1, TP.HCM",  32990000.0, "Chờ xử lý"},
            new Object[]{2L, "Trần Thị Bình",  "binh@gmail.com",  "0912345678", "24/09/2026", "45 Nguyễn Huệ, Q.1, TP.HCM",     18490000.0, "Đang giao"},
            new Object[]{3L, "Lê Minh Châu",   "chau@gmail.com",  "0923456789", "23/09/2026", "78 Hai Bà Trưng, Q.3, TP.HCM",   24990000.0, "Đã giao"},
            new Object[]{4L, "Phạm Thị Dung",  "dung@gmail.com",  "0934567890", "22/09/2026", "99 Điện Biên Phủ, Q. Bình Thạnh", 31490000.0, "Đã hủy"},
            new Object[]{5L, "Hoàng Văn Em",   "em@gmail.com",    "0945678901", "21/09/2026", "33 Cộng Hòa, Q. Tân Bình, TP.HCM",15990000.0, "Chờ xử lý"},
            new Object[]{6L, "Vũ Thị Phương",  "phuong@gmail.com","0956789012", "20/09/2026", "10 Phan Xích Long, Q. Phú Nhuận", 27490000.0, "Đang giao"},
            new Object[]{7L, "Đỗ Quang Huy",   "huy@gmail.com",   "0967890123", "19/09/2026", "55 Lê Văn Sỹ, Q.3, TP.HCM",      19990000.0, "Đã giao"}
        );
    }

    // Chi tiết sản phẩm trong đơn (giả)
    // Mỗi phần tử: [tenSanPham, soLuong, donGia, thanhTien]
    private List<Object[]> getMockOrderItems() {
        return List.of(
            new Object[]{"iPhone 16 Pro Max 256GB", 1, 32990000.0, 32990000.0},
            new Object[]{"Ốp lưng iPhone 16 Pro Max", 2, 250000.0,  500000.0}
        );
    }

    // =====================================================================
    // GET /admin/orders — Danh sách tất cả đơn hàng
    // =====================================================================
    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        model.addAttribute("orders", getMockOrders()); // Task 7: thay bằng orderRepository.findAll()
        model.addAttribute("statuses", ORDER_STATUSES);
        return "admin/orders"; // Trỏ tới /WEB-INF/views/admin/orders.jsp
    }

    // =====================================================================
    // GET /admin/orders/{id} — Chi tiết đơn hàng
    // =====================================================================
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        // Task 7: thay bằng orderRepository.findById(id)
        Object[] order = getMockOrders().stream()
                .filter(o -> o[0].equals(id))
                .findFirst()
                .orElse(null);

        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        model.addAttribute("order", order);
        model.addAttribute("orderItems", getMockOrderItems()); // Task 7: thay bằng order.getOrderDetails()
        model.addAttribute("statuses", ORDER_STATUSES);
        return "admin/order-detail"; // Trỏ tới /WEB-INF/views/admin/order-detail.jsp
    }

    // =====================================================================
    // POST /admin/orders/{id}/status — Cập nhật trạng thái đơn hàng
    // =====================================================================
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {
        // Task 7: thay bằng orderRepository.findById(id) -> setStatus -> save()
        redirectAttributes.addFlashAttribute("successMsg",
                "Cập nhật trạng thái đơn #" + id + " thành \"" + status + "\" thành công!");
        return "redirect:/admin/orders";
    }
}
