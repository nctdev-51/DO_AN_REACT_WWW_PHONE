package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.service.tracking.AnalyticsService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {
  private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private final AnalyticsService analytics;

  public AdminAnalyticsController(AnalyticsService analytics) {
    this.analytics = analytics;
  }

  @GetMapping
  public String dashboard(
      @RequestParam(required = false) Integer days,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      Model model) {
    DateRange range = resolveRange(days, from, to);
    var report = analytics.report(range.from(), range.to());
    model.addAttribute("currentPage", "analytics");
    model.addAttribute("pageTitle", "Theo dõi & thống kê");
    model.addAttribute("report", report);
    model.addAttribute("from", range.from());
    model.addAttribute("to", range.to());
    model.addAttribute("selectedPreset", range.preset());
    model.addAttribute(
        "dailyLabels",
        report.dailyViews().stream()
            .map(view -> view.date().toString())
            .collect(java.util.stream.Collectors.joining(",")));
    model.addAttribute(
        "dailyValues",
        report.dailyViews().stream()
            .map(view -> Long.toString(view.count()))
            .collect(java.util.stream.Collectors.joining(",")));

    // Bổ sung phân tích dự báo tồn kho (Chức năng 5) & tương tác AI (Chức năng 6)
    model.addAttribute("stockoutForecasts", analytics.forecastStockoutRisk().stream().limit(20).toList());
    model.addAttribute("recentAiQuestions", analytics.recentAiChatInsights(6));
    model.addAttribute("aiTopicStats", analytics.aiTopicDistribution(range.from(), range.to()));

    return "admin/analytics/dashboard";
  }

  @GetMapping("/export")
  public void exportCsv(
      @RequestParam(required = false) Integer days,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      HttpServletResponse response)
      throws IOException {
    DateRange range = resolveRange(days, from, to);
    var report = analytics.report(range.from(), range.to());
    response.setContentType("text/csv;charset=UTF-8");
    response.setHeader(
        "Content-Disposition",
        "attachment; filename=analytics-"
            + range.from().format(FILE_DATE)
            + "-to-"
            + range.to().format(FILE_DATE)
            + ".csv");

    try (PrintWriter writer =
        new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8)) {
      writer.write('\ufeff');
      csvRow(writer, "Chỉ số", "Giá trị");
      csvRow(writer, "Từ ngày", range.from().toString());
      csvRow(writer, "Đến ngày", range.to().toString());
      csvRow(writer, "Lượt xem", Long.toString(report.views()));
      csvRow(writer, "Tỷ lệ chuyển đổi (%)", Double.toString(report.conversionRate()));
      csvRow(writer, "Đơn hàng", Long.toString(report.orderCount()));
      csvRow(writer, "Khách truy cập mới", Long.toString(report.newVisitors()));
      csvRow(writer, "Lượt hỏi chatbot", Long.toString(report.chatbotMessages()));
      writer.println();
      csvRow(writer, "Sản phẩm", "Lượt xem", "Lượt nhấp", "Thêm giỏ", "Chuyển đổi (%)");
      for (var product : report.topProducts()) {
        csvRow(
            writer,
            product.productName(),
            Long.toString(product.views()),
            Long.toString(product.clicks()),
            Long.toString(product.cartAdds()),
            Double.toString(product.conversionRate()));
      }
      writer.println();
      csvRow(writer, "Từ khóa", "Số lượt tìm kiếm");
      for (var keyword : report.topKeywords()) {
        csvRow(writer, keyword.keyword(), Long.toString(keyword.count()));
      }
      writer.println();
      csvRow(writer, "Ngày", "Lượt xem");
      for (var view : report.dailyViews()) {
        csvRow(writer, view.date().toString(), Long.toString(view.count()));
      }
    }
  }

  private DateRange resolveRange(Integer days, LocalDate from, LocalDate to) {
    if (from != null || to != null) {
      if (from == null || to == null || from.isAfter(to)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Khoảng ngày không hợp lệ.");
      }
      return new DateRange(from, to, "custom");
    }

    int preset = days == null ? 30 : days;
    if (preset != 7 && preset != 30) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ bộ lọc 7 ngày hoặc 30 ngày.");
    }
    LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    return new DateRange(today.minusDays(preset - 1L), today, Integer.toString(preset));
  }

  private void csvRow(PrintWriter writer, String... values) {
    writer.println(
        java.util.Arrays.stream(values)
            .map(AdminAnalyticsController::safeCsvCell)
            .collect(java.util.stream.Collectors.joining(",")));
  }

  public static String safeCsvCell(String raw) {
    String value = raw == null ? "" : raw;
    if (!value.isEmpty()) {
      char first = value.charAt(0);
      if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
        value = "'" + value;
      }
    }
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private record DateRange(LocalDate from, LocalDate to, String preset) {}
}
