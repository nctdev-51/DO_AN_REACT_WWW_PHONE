package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.repository.*;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
  private final OrderRepository orders;
  private final UserRepository users;
  private final iuh.fit.www_phone_ver1.service.admin.SalesReportService sales;

  public AdminDashboardController(
      OrderRepository orders,
      UserRepository users,
      iuh.fit.www_phone_ver1.service.admin.SalesReportService sales) {
    this.orders = orders;
    this.users = users;
    this.sales = sales;
  }

  @GetMapping({"", "/"})
  public String dashboard(Model model) {
    model.addAttribute("currentPage", "dashboard");
    model.addAttribute("pageTitle", "Tổng quan");
    var currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    model.addAttribute("totalOrders", orders.count());
    model.addAttribute("totalUsers", users.count());
    var report = sales.report(YearMonth.now());
    model.addAttribute("totalRevenue", currency.format(report.net()));
    model.addAttribute("grossRevenue", currency.format(report.gross()));
    model.addAttribute("refundedRevenue", currency.format(report.refunded()));
    model.addAttribute("pendingOrders", orders.countByStatus("Chờ xử lý"));
    model.addAttribute("recentOrders", orders.findTop5ByOrderByOrderDateDesc());
    model.addAttribute("chartLabels", String.join(",", report.labels()));
    model.addAttribute(
        "chartData",
        report.values().stream()
            .map(java.math.BigDecimal::toPlainString)
            .collect(java.util.stream.Collectors.joining(",")));
    return "admin/dashboard/index";
  }
}
