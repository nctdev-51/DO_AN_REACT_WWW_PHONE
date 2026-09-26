package iuh.fit.www_phone_ver1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    // =====================================================================
    // Data giả dùng chung
    // Mỗi phần tử: [id, hoTen, email, sdt, diaChi, trangThai(1/0), soLuongDon]
    // trangThai: 1 = Hoạt động, 0 = Đã khóa
    // soLuongDon: > 0 thì không được xóa
    // Sẽ thay bằng userRepository.findAll() ở Task 7
    // =====================================================================
    private List<Object[]> getMockUsers() {
        return List.of(
            new Object[]{1L, "Nguyễn Văn An",  "an@gmail.com",     "0901234567", "12 Lý Thường Kiệt, Q.1, TP.HCM",   1, 3},
            new Object[]{2L, "Trần Thị Bình",  "binh@gmail.com",   "0912345678", "45 Nguyễn Huệ, Q.1, TP.HCM",       1, 1},
            new Object[]{3L, "Lê Minh Châu",   "chau@gmail.com",   "0923456789", "78 Hai Bà Trưng, Q.3, TP.HCM",     0, 2},
            new Object[]{4L, "Phạm Thị Dung",  "dung@gmail.com",   "0934567890", "99 Điện Biên Phủ, Q. Bình Thạnh",  1, 0},
            new Object[]{5L, "Hoàng Văn Em",   "em@gmail.com",     "0945678901", "33 Cộng Hòa, Q. Tân Bình, TP.HCM", 1, 0}
        );
    }

    // =====================================================================
    // GET /admin/users — Danh sách người dùng
    // =====================================================================
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "Quản lý người dùng");
        model.addAttribute("users", getMockUsers()); // Task 7: thay bằng userRepository.findAll()
        return "admin/users"; // Trỏ tới /WEB-INF/views/admin/users.jsp
    }

    // =====================================================================
    // GET /admin/users/{id}/edit — Form sửa thông tin người dùng
    // =====================================================================
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        // Task 7: thay bằng userRepository.findById(id)
        Object[] user = getMockUsers().stream()
                .filter(u -> u[0].equals(id))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return "redirect:/admin/users";
        }

        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "Sửa người dùng");
        model.addAttribute("user", user);
        return "admin/user-edit"; // Trỏ tới /WEB-INF/views/admin/user-edit.jsp
    }

    // =====================================================================
    // POST /admin/users/{id}/edit — Lưu thông tin người dùng đã sửa
    // =====================================================================
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             @RequestParam("status") Integer status,
                             RedirectAttributes redirectAttributes) {
        // Task 7: thay bằng userRepository.findById(id) -> set fields -> save()
        redirectAttributes.addFlashAttribute("successMsg",
                "Cập nhật thông tin người dùng \"" + fullName + "\" thành công!");
        return "redirect:/admin/users";
    }

    // =====================================================================
    // GET /admin/users/{id}/delete — Xóa người dùng (chỉ khi chưa có đơn hàng)
    // =====================================================================
    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        // Task 7: thay bằng userRepository.findById(id) -> kiểm tra orders -> deleteById()
        Object[] user = getMockUsers().stream()
                .filter(u -> u[0].equals(id))
                .findFirst()
                .orElse(null);

        if (user != null && (int) user[6] > 0) {
            // Có đơn hàng → không cho xóa
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Không thể xóa \"" + user[1] + "\" vì đã có " + user[6] + " đơn hàng!");
        } else if (user != null) {
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đã xóa người dùng \"" + user[1] + "\" thành công!");
        }
        return "redirect:/admin/users";
    }
}
