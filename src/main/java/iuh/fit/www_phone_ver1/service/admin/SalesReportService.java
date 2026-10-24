package iuh.fit.www_phone_ver1.service.admin;

import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesReportService {
  private final OrderRepository orders;
  private final RefundRepository refunds;

  public SalesReportService(OrderRepository orders, RefundRepository refunds) {
    this.orders = orders;
    this.refunds = refunds;
  }

  public record Report(
      BigDecimal gross,
      BigDecimal refunded,
      BigDecimal net,
      List<String> labels,
      List<BigDecimal> values) {}

  @Transactional(readOnly = true)
  public Report report(YearMonth end) {
    var gross = orders.deliveredRevenue();
    var returned = refunds.completedDeliveredTotal();
    var start = end.minusMonths(5);
    Map<Long, BigDecimal> adjustments = new HashMap<>();
    refunds
        .deliveredTotalsSince(start.atDay(1).atStartOfDay())
        .forEach(r -> adjustments.put(r.getOrderId(), r.getAmount()));
    Map<YearMonth, BigDecimal> months = new LinkedHashMap<>();
    for (int i = 0; i < 6; i++) months.put(start.plusMonths(i), BigDecimal.ZERO);
    for (var o : orders.deliveredSince(start.atDay(1).atStartOfDay())) {
      var month =
          YearMonth.from(o.getDeliveredAt() == null ? o.getOrderDate() : o.getDeliveredAt());
      if (months.containsKey(month))
        months.merge(
            month,
            o.getTotalAmount().subtract(adjustments.getOrDefault(o.getId(), BigDecimal.ZERO)),
            BigDecimal::add);
    }
    return new Report(
        gross,
        returned,
        gross.subtract(returned),
        months.keySet().stream().map(m -> m.getMonthValue() + "/" + m.getYear()).toList(),
        List.copyOf(months.values()));
  }
}
