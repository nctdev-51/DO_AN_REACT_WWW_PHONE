package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
class InventoryConcurrencyTests {
  @Autowired UserRepository users;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired OrderRepository orders;
  @Autowired OrderService checkout;
  @Autowired PlatformTransactionManager transactions;
  @Autowired PromotionRepository promotions;

  @Test
  void twoBuyersCannotReserveTheSameLastCoupon() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User buyer =
        users.save(
            User.builder()
                .fullName("Coupon concurrency")
                .email(suffix + "@example.com")
                .password("unused")
                .build());
    Promotion coupon = new Promotion();
    coupon.setCode("LAST" + suffix.substring(0, 8).toUpperCase(java.util.Locale.ROOT));
    coupon.setDescription("Last coupon fixture");
    coupon.setDiscountType("FIXED");
    coupon.setAmount(BigDecimal.valueOf(10000));
    coupon.setMinimumSubtotal(BigDecimal.ZERO);
    coupon.setStartsAt(java.time.LocalDateTime.now().minusDays(1));
    coupon.setEndsAt(java.time.LocalDateTime.now().plusDays(1));
    coupon.setUsageLimit(1);
    promotions.save(coupon);
    var items = new java.util.ArrayList<ProductVariant>();
    var fixtures = new java.util.ArrayList<Product>();
    for (int i = 0; i < 2; i++) {
      Product product =
          products.save(
              Product.builder()
                  .name("Coupon fixture " + i)
                  .sku(suffix + i)
                  .price(BigDecimal.valueOf(100000))
                  .build());
      fixtures.add(product);
      ProductVariant variant = new ProductVariant();
      variant.setProduct(product);
      variant.setSku(suffix + i);
      variant.setColor("Đen");
      variant.setStorage("128GB");
      variant.setPrice(product.getPrice());
      variant.setStock(5);
      items.add(variants.save(variant));
    }
    var pool = Executors.newFixedThreadPool(2);
    var gate = new CountDownLatch(1);
    try {
      var futures = new java.util.ArrayList<Future<Boolean>>();
      for (ProductVariant item : items)
        futures.add(
            pool.submit(
                () -> {
                  Cart cart = new Cart();
                  cart.addItem(item, 1);
                  CheckoutForm form = new CheckoutForm();
                  form.setRecipientName("Buyer");
                  form.setPhone("0901234567");
                  form.setShippingAddress("Test address");
                  form.setPromotionCode(coupon.getCode());
                  gate.await();
                  try {
                    checkout.placeOrder(cart, buyer.getEmail(), form);
                    return true;
                  } catch (IllegalArgumentException exhausted) {
                    return false;
                  }
                }));
      gate.countDown();
      int won = 0;
      for (var future : futures) if (future.get(15, TimeUnit.SECONDS)) won++;
      assertEquals(1, won);
      assertEquals(1, orders.countByPromotion_IdAndStatusNot(coupon.getId(), "Đã hủy"));
      assertEquals(
          9,
          items.stream()
              .mapToInt(v -> variants.findById(v.getId()).orElseThrow().getStock())
              .sum());
    } finally {
      pool.shutdownNow();
      new TransactionTemplate(transactions)
          .executeWithoutResult(
              s -> {
                orders.deleteAll(orders.findByUser_EmailOrderByOrderDateDesc(buyer.getEmail()));
                orders.flush();
                variants.deleteAllById(items.stream().map(ProductVariant::getId).toList());
                variants.flush();
                products.deleteAllById(fixtures.stream().map(Product::getId).toList());
                products.flush();
                promotions.deleteById(coupon.getId());
                users.deleteById(buyer.getId());
              });
    }
  }

  @Test
  void twoBuyersCannotPurchaseTheSameLastUnit() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User buyer =
        users.save(
            User.builder()
                .fullName("Concurrency Test")
                .email(suffix + "@example.com")
                .password("unused")
                .status(1)
                .build());
    Product product =
        products.save(
            Product.builder()
                .name("Concurrency fixture")
                .sku(suffix)
                .price(BigDecimal.valueOf(100000))
                .status(1)
                .build());
    ProductVariant variant = new ProductVariant();
    variant.setProduct(product);
    variant.setSku(suffix);
    variant.setColor("Đen");
    variant.setStorage("128GB");
    variant.setPrice(product.getPrice());
    variant.setStock(1);
    variants.save(variant);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch gate = new CountDownLatch(1);
    try {
      Callable<Boolean> buy =
          () -> {
            Cart cart = new Cart();
            cart.addItem(variant, 1);
            CheckoutForm form = new CheckoutForm();
            form.setRecipientName("Test");
            form.setPhone("0901234567");
            form.setShippingAddress("Test address");
            gate.await();
            try {
              checkout.placeOrder(cart, buyer.getEmail(), form);
              return true;
            } catch (IllegalArgumentException soldOut) {
              return false;
            }
          };
      Future<Boolean> first = pool.submit(buy);
      Future<Boolean> second = pool.submit(buy);
      gate.countDown();
      int successful =
          (first.get(15, TimeUnit.SECONDS) ? 1 : 0) + (second.get(15, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, successful);
      assertEquals(0, variants.findById(variant.getId()).orElseThrow().getStock());
      assertEquals(1, orders.countByUser_Id(buyer.getId()));
    } finally {
      pool.shutdownNow();
      new TransactionTemplate(transactions)
          .executeWithoutResult(
              status -> {
                orders.deleteAll(orders.findByUser_EmailOrderByOrderDateDesc(buyer.getEmail()));
                orders.flush();
                variants.deleteById(variant.getId());
                variants.flush();
                products.deleteById(product.getId());
                products.flush();
                users.deleteById(buyer.getId());
              });
    }
  }
}
