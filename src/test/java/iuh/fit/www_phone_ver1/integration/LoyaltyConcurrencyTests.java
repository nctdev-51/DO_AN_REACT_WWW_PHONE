package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.order.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
class LoyaltyConcurrencyTests {
  @Autowired UserRepository users;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired OrderRepository orders;
  @Autowired OrderService checkout;
  @Autowired LoyaltyRepository ledger;
  @Autowired PlatformTransactionManager transactions;

  CheckoutForm form() {
    var f = new CheckoutForm();
    f.setRecipientName("Test");
    f.setPhone("0901234567");
    f.setShippingAddress("Test address");
    return f;
  }

  @Test
  void parallelOrdersCannotSpendTheSamePointsTwice() throws Exception {
    String key = UUID.randomUUID().toString();
    var buyer =
        users.save(
            User.builder()
                .fullName("Points concurrency")
                .email(key + "@example.test")
                .password("unused")
                .status(1)
                .build());
    var fixtures = new ArrayList<Product>();
    var items = new ArrayList<ProductVariant>();
    for (int i = 0; i < 2; i++) {
      var p =
          products.save(
              Product.builder()
                  .name("Points fixture")
                  .sku(key + i)
                  .price(BigDecimal.valueOf(1000000))
                  .build());
      fixtures.add(p);
      var v = new ProductVariant();
      v.setProduct(p);
      v.setSku(key + i);
      v.setColor("Đen");
      v.setStorage("128GB");
      v.setPrice(p.getPrice());
      v.setStock(5);
      items.add(variants.save(v));
    }
    var pool = Executors.newFixedThreadPool(2);
    var gate = new CountDownLatch(1);
    try {
      var earned = new Cart();
      earned.addItem(items.get(0), 1);
      var o = checkout.placeOrder(earned, buyer.getEmail(), form());
      checkout.updateStatus(o.getId(), OrderStatuses.SHIPPING);
      checkout.updateStatus(o.getId(), OrderStatuses.DELIVERED);
      assertEquals(10, ledger.balance(buyer.getId()));
      var futures = new ArrayList<Future<Boolean>>();
      for (var v : items)
        futures.add(
            pool.submit(
                () -> {
                  var c = new Cart();
                  c.addItem(v, 1);
                  var f = form();
                  f.setRedeemPoints(10);
                  gate.await();
                  try {
                    checkout.placeOrder(c, buyer.getEmail(), f);
                    return true;
                  } catch (IllegalArgumentException insufficient) {
                    return false;
                  }
                }));
      gate.countDown();
      int won = 0;
      for (var f : futures) if (f.get(15, TimeUnit.SECONDS)) won++;
      assertEquals(1, won);
      assertEquals(0, ledger.balance(buyer.getId()));
      assertEquals(2, orders.countByUser_Id(buyer.getId()));
    } finally {
      pool.shutdownNow();
      new TransactionTemplate(transactions)
          .executeWithoutResult(
              tx -> {
                ledger.deleteAll(ledger.findTop100ByUser_EmailOrderByIdDesc(buyer.getEmail()));
                ledger.flush();
                orders.deleteAll(orders.findByUser_EmailOrderByOrderDateDesc(buyer.getEmail()));
                orders.flush();
                variants.deleteAllById(items.stream().map(ProductVariant::getId).toList());
                variants.flush();
                products.deleteAllById(fixtures.stream().map(Product::getId).toList());
                products.flush();
                users.deleteById(buyer.getId());
              });
    }
  }
}
