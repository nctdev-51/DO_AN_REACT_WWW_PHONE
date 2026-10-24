package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.user.UserSummary;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.repository.projection.UserOrderCount;
import iuh.fit.www_phone_ver1.service.admin.AdminUserService;
import java.security.Principal;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
  private final UserRepository users;
  private final OrderRepository orders;
  private final AdminUserService admin;

  public AdminUserController(UserRepository users, OrderRepository orders, AdminUserService admin) {
    this.users = users;
    this.orders = orders;
    this.admin = admin;
  }

  @GetMapping
  public String listUsers(Model model) {
    var counts =
        orders.userOrderCounts().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    UserOrderCount::getUserId, UserOrderCount::getOrderCount));
    model.addAttribute("currentPage", "users");
    model.addAttribute("pageTitle", "Quản lý người dùng");
    model.addAttribute(
        "users",
        users.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(u -> new UserSummary(u, counts.getOrDefault(u.getId(), 0L)))
            .toList());
    return "admin/users/list";
  }

  @GetMapping("/{id}/edit")
  public String editUserForm(@PathVariable Long id, Model model) {
    var user =
        users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    model.addAttribute("currentPage", "users");
    model.addAttribute("pageTitle", "Sửa người dùng");
    model.addAttribute("user", user);
    return "admin/users/form";
  }

  @PostMapping("/{id}/edit")
  public String updateUser(
      @PathVariable Long id,
      @RequestParam String fullName,
      @RequestParam String phone,
      @RequestParam String address,
      @RequestParam Integer status,
      Principal actor,
      RedirectAttributes flash) {
    try {
      admin.updateUser(id, fullName, phone, address, status, actor.getName());
      flash.addFlashAttribute("successMsg", "Đã cập nhật người dùng.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
      return "redirect:/admin/users/" + id + "/edit";
    }
    return "redirect:/admin/users";
  }

  @PostMapping("/{id}/delete")
  public String deleteUser(@PathVariable Long id, Principal actor, RedirectAttributes flash) {
    try {
      admin.deleteUser(id, actor.getName());
      flash.addFlashAttribute("successMsg", "Đã xóa người dùng.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/users";
  }
}
