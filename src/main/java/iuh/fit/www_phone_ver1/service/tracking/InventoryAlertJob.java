package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.service.mail.MailService;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryAlertJob {
  private final AnalyticsService analytics;
  private final MailService mail;
  private final String recipient;
  private final Map<Long, Instant> notified = new HashMap<>();

  public InventoryAlertJob(AnalyticsService analytics, MailService mail,
      @Value("${app.inventory.alert-email:}") String recipient) {
    this.analytics = analytics;
    this.mail = mail;
    this.recipient = recipient.trim();
  }

  @Scheduled(fixedDelayString = "${app.inventory.alert-interval-ms:3600000}", initialDelay = 60_000)
  public void alertStockTeam() {
    if (recipient.isEmpty()) return;
    Instant now = Instant.now();
    notified.values().removeIf(sent -> !sent.plus(Duration.ofHours(24)).isAfter(now));
    var alerts = analytics.forecastStockoutRisk().stream()
        .filter(row -> "HIGH".equals(row.riskLevel()) && !notified.containsKey(row.productId()))
        .toList();
    if (alerts.isEmpty()) return;
    String summary = alerts.stream()
        .map(row -> row.productName() + " — Còn " + row.stock() + " máy. " + row.recommendation())
        .collect(Collectors.joining("\n"));
    if (mail.sendInventoryAlert(recipient, summary)) {
      alerts.forEach(row -> notified.put(row.productId(), now));
    }
  }
}
