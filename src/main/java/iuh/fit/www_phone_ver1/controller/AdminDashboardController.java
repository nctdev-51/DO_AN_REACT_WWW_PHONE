package iuh.fit.www_phone_ver1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");

        // --- Thống kê tổng quan (data giả - sẽ thay bằng AdminService ở Task 7) ---
        model.addAttribute("totalOrders", 120);
        model.addAttribute("totalRevenue", "1.500.000.000");
        model.addAttribute("totalUsers", 85);
        model.addAttribute("pendingOrders", 12);

        // --- Đơn hàng gần đây (data giả - sẽ thay bằng orderRepository ở Task 7) ---
        // Mỗi phần tử: [id, tenKhach, ngayDat, tongTien, trangThai]
        List<Object[]> recentOrders = List.of(
            new Object[]{1L, "Nguyễn Văn An",  "25/09/2026", "32.990.000₫", "Chờ xử lý"},
            new Object[]{2L, "Trần Thị Bình",  "24/09/2026", "18.490.000₫", "Đang giao"},
            new Object[]{3L, "Lê Minh Châu",   "23/09/2026", "24.990.000₫", "Đã giao"},
            new Object[]{4L, "Phạm Thị Dung",  "22/09/2026", "31.490.000₫", "Đã hủy"},
            new Object[]{5L, "Hoàng Văn Em",   "21/09/2026", "15.990.000₫", "Chờ xử lý"}
        );
        model.addAttribute("recentOrders", recentOrders);

        // --- Dữ liệu biểu đồ doanh thu 6 tháng gần nhất (data giả - Task 7 sẽ query DB) ---
        model.addAttribute("chartLabels", "Tháng 4,Tháng 5,Tháng 6,Tháng 7,Tháng 8,Tháng 9");
        model.addAttribute("chartData",   "185000000,230000000,198000000,310000000,275000000,420000000");

        return "admin/dashboard"; // Trỏ tới /WEB-INF/views/admin/dashboard.jsp
    }
}
