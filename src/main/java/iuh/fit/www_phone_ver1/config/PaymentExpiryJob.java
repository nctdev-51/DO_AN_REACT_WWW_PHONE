package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.service.order.OrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(
    name = "app.payments.expiry-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PaymentExpiryJob {
  private final OrderService orders;

  public PaymentExpiryJob(OrderService orders) {
    this.orders = orders;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void expire() {
    orders.expireUnpaidOrders();
  }
}
