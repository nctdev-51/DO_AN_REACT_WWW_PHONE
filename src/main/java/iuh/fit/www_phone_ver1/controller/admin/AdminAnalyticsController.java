package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.service.tracking.AnalyticsService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {
  private final AnalyticsService analytics;

  public AdminAnalyticsController(AnalyticsService analytics) {
    this.analytics = analytics;
  }

  @GetMapping
  public String dashboard(Model model) {
    var summary = analytics.summary();
    model.addAttribute("currentPage", "analytics");
    model.addAttribute("pageTitle", "Theo dõi & thống kê");
    model.addAttribute("summary", summary);
    model.addAttribute("dailyViews", analytics.dailyViews(30));
    model.addAttribute("newUsers", analytics.countNewUsersInLast(LocalDateTime.now().minusDays(30)));
    return "admin/analytics/dashboard";
  }

  @GetMapping("/export")
  public void exportCsv(HttpServletResponse response) throws Exception {
    response.setContentType("text/csv;charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=ptv-analytics.csv");
    var summary = analytics.summary();
    try (PrintWriter writer =
        new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8)) {
      writer.println("metric,value");
      writer.println("views_7_days," + summary.views7Days());
      writer.println("views_30_days," + summary.views30Days());
      writer.println("conversion_percent," + summary.conversionRate());
      writer.println("chat_messages_30_days," + summary.chatbotMessages());
      writer.println("keyword,count");
      for (var keyword : summary.topKeywords()) {
        writer.println("\"" + keyword.keyword().replace("\"", "\"\"") + "\"," + keyword.count());
      }
    }
  }
}
