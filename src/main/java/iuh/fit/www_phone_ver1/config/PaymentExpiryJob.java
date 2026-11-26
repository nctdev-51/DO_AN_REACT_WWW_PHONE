package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.service.order.OrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@org.springframework.context.annotation.Lazy(false)
@ConditionalOnProperty(
    name = "app.payments.expiry-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PaymentExpiryJob {
  private final OrderService orders;

  public PaymentExpiryJob(OrderService orders) {
    this.orders = orders;
  }

  @io.micrometer.core.annotation.Timed("ptv.payments.expiry")
<<<<<<< HEAD
  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
=======
  @Scheduled(fixedDelay = 15000, initialDelay = 15000)
>>>>>>> origin/taidev
  public void expire() {
    orders.expireUnpaidOrders();
  }
}
