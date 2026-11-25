package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.Consultation;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.entity.Refund;
import iuh.fit.www_phone_ver1.entity.SupportRequest;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.repository.ConsultationRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.RefundRepository;
import iuh.fit.www_phone_ver1.repository.SupportRequestRepository;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.service.ai.ChatTopic;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo & dashboard-data")
@org.springframework.core.annotation.Order(40)
public class DashboardDemoData implements CommandLineRunner {
  private static final String DATABASE = "ptv_dashboard_preview";
  private static final List<String> PRODUCT_SKUS = List.of("PS-IPHONE-16", "PS-SAMSUNG-GALAXY-S25",
      "PS-XIAOMI-15", "PS-OPPO-RENO13", "PS-XIAOMI-POCO-X7-PRO", "PS-HONOR-400");
  private static final List<String> KEYWORDS = List.of("iPhone 16", "Samsung Galaxy S25",
      "điện thoại chụp ảnh", "Xiaomi", "POCO X7 Pro", "HONOR 400", "điện thoại chơi game");
  private static final List<String> QUESTIONS = List.of(
      "Điện thoại này bảo hành bao lâu?", "Cửa hàng có trả góp 0% không?",
      "Trong hộp có củ sạc đi kèm không?", "Giao hàng mất bao lâu?",
      "Mẫu này có ưu đãi gì?", "Máy nào chụp ảnh đẹp?");

  private final DataSource dataSource;
  private final ProductRepository products;
  private final UserRepository users;
  private final OrderRepository orders;
  private final RefundRepository refunds;
  private final SupportRequestRepository support;
  private final ConsultationRepository consultations;
  private final PasswordEncoder passwords;
  private final JdbcTemplate jdbc;
  private final MongoTemplate mongo;

  public DashboardDemoData(DataSource dataSource, ProductRepository products, UserRepository users,
      OrderRepository orders, RefundRepository refunds, SupportRequestRepository support,
      ConsultationRepository consultations, PasswordEncoder passwords,
      JdbcTemplate jdbc, MongoTemplate mongo) {
    this.dataSource = dataSource;
    this.products = products;
    this.users = users;
    this.orders = orders;
    this.refunds = refunds;
    this.support = support;
    this.consultations = consultations;
    this.passwords = passwords;
    this.jdbc = jdbc;
    this.mongo = mongo;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    try (var connection = dataSource.getConnection()) {
      if (!connection.getMetaData().getURL().equals("jdbc:h2:mem:" + DATABASE)
          || !DATABASE.equals(mongo.getDb().getName())) {
        throw new IllegalStateException("Dashboard demo data requires its dedicated H2 and MongoDB databases.");
      }
    }

    var featured = PRODUCT_SKUS.stream().map(sku -> products.findBySku(sku).orElseThrow()).toList();
    LocalDateTime now = LocalDateTime.now();
    var random = new Random(712);
    if (orders.count() == 0) {
      seedOrders(featured, now, random);
    }
    seedInteractions(featured, now, new Random(214));
  }

  private void seedOrders(List<Product> featured, LocalDateTime now, Random random) {
    String password = passwords.encode(UUID.randomUUID().toString());
    var customers = new ArrayList<User>();
    for (int i = 1; i <= 24; i++) {
      customers.add(users.save(User.builder().fullName("Khách demo " + i)
          .email("khach-demo-" + i + "@example.test")
          .phone(String.format(java.util.Locale.ROOT, "090000%04d", i))
          .password(password).address("Địa chỉ thử nghiệm, TP. HCM").build()));
    }

    int[] completedByMonth = {8, 12, 10, 16, 14, 19};
    var completed = new ArrayList<Order>();
    YearMonth currentMonth = YearMonth.from(now);
    for (int month = 0; month < completedByMonth.length; month++) {
      var period = currentMonth.minusMonths(5 - month);
      int lastDay = period.equals(currentMonth) ? now.getDayOfMonth() : period.lengthOfMonth();
      for (int i = 0; i < completedByMonth[month]; i++) {
        LocalDateTime delivered = period.atDay(1 + random.nextInt(lastDay)).atTime(12, 0);
        if (delivered.isAfter(now)) delivered = now.minusMinutes(15);
        Order order = saveOrder(featured, customers, random, delivered.minusDays(2), delivered,
            OrderStatuses.DELIVERED);
        completed.add(order);
        if (i == 0 && month % 2 == 0) {
          var refund = new Refund();
          refund.setOrder(order);
          refund.setAmount(BigDecimal.valueOf(250000L + month * 75000L));
          refund.setStatus("COMPLETED");
          refund.setReason("Hoàn tiền thử nghiệm");
          refund.setCreatedAt(delivered);
          refund.setCompletedAt(delivered);
          refunds.save(refund);
        }
      }
    }
    for (int i = 0; i < 13; i++) {
      String status = i < 7 ? OrderStatuses.PENDING
          : i < 11 ? OrderStatuses.SHIPPING : OrderStatuses.CANCELLED;
      saveOrder(featured, customers, random, now.minusHours(2L + i * 5L), null, status);
    }
    var variants = featured.get(5).getVariants().stream().filter(ProductVariant::isActive).toList();
    variants.forEach(variant -> variant.setStock(0));
    if (!variants.isEmpty()) variants.get(0).setStock(8);
    completed.sort(Comparator.comparing(Order::getDeliveredAt).reversed());
    seedSupport(completed, now);
    seedConsultations(featured, customers, now);
  }

  private void seedSupport(List<Order> completed, LocalDateTime now) {
    var examples = List.of(
        new SupportExample("RETURN", "REQUESTED", "Khách nhận máy khác màu đã đặt.", null),
        new SupportExample("WARRANTY", "REQUESTED", "Máy không nhận sạc sau vài ngày sử dụng.", null),
        new SupportExample("RETURN", "APPROVED", "Màn hình có điểm sáng khi vừa nhận hàng.",
            "Đã duyệt tiếp nhận. Mang máy và phụ kiện tới cửa hàng để kiểm tra."),
        new SupportExample("WARRANTY", "RECEIVED", "Loa thoại có tiếng rè khi gọi.",
            "Cửa hàng đã nhận thiết bị và chuyển cho kỹ thuật."),
        new SupportExample("WARRANTY", "COMPLETED", "Cổng sạc chập chờn.",
            "Đã kiểm tra và thay linh kiện trong thời hạn bảo hành."),
        new SupportExample("RETURN", "REJECTED", "Khách muốn đổi khi máy bị nứt do rơi.",
            "Tình trạng thiết bị không đáp ứng điều kiện đổi trả."));
    for (int i = 0; i < examples.size(); i++) {
      var example = examples.get(i);
      Order order = completed.get(i);
      var request = new SupportRequest();
      request.setOrderDetail(order.getOrderDetails().get(0));
      request.setType(example.type());
      request.setStatus(example.status());
      request.setQuantity(1);
      request.setReason(example.reason());
      request.setResponse(example.response());
      request.setCreatedAt(order.getDeliveredAt().plusMinutes(5));
      request.setUpdatedAt(example.response() == null ? request.getCreatedAt() : now);
      support.save(request);
    }
  }

  private void seedConsultations(List<Product> products, List<User> customers, LocalDateTime now) {
    var examples = List.of(
        new ConsultationExample("INSTALLMENT", "REQUESTED", "Muốn trả trước 30%, góp trong 6 tháng.", null, null),
        new ConsultationExample("TRADE_IN", "REQUESTED", "Có iPhone 13 128 GB, pin 83%, muốn lên đời.",
            null, null),
        new ConsultationExample("INSTALLMENT", "CONTACTED", "Cần tư vấn khoản trả trước và hồ sơ.",
            "Đã tiếp nhận, sẽ trao đổi các kỳ hạn với khách.", null),
        new ConsultationExample("TRADE_IN", "QUOTED", "Có Galaxy S22, máy hoạt động bình thường.",
            "Giá dự kiến, xác nhận sau khi kiểm tra máy thực tế.", 4200000L),
        new ConsultationExample("INSTALLMENT", "ACCEPTED", "Muốn mua góp 12 tháng.",
            "Khách đã đồng ý phương án trả trước.", 3500000L),
        new ConsultationExample("TRADE_IN", "REJECTED", "Máy cũ không bật nguồn.",
            "Thiết bị chưa đáp ứng điều kiện thu cũ.", null));
    for (int i = 0; i < examples.size(); i++) {
      var example = examples.get(i);
      User customer = customers.get(i);
      var request = new Consultation();
      request.setUser(customer);
      request.setProduct(products.get(i));
      request.setType(example.type());
      request.setStatus(example.status());
      request.setContactName(customer.getFullName());
      request.setPhone(customer.getPhone());
      request.setMessage(example.message());
      request.setResponse(example.response());
      if (example.amount() != null) request.setQuotedAmount(BigDecimal.valueOf(example.amount()));
      request.setCreatedAt(now.minusDays(i + 1L));
      if (example.response() != null) request.setUpdatedAt(now.minusDays(i));
      consultations.save(request);
    }
  }

  private record SupportExample(String type, String status, String reason, String response) {}

  private record ConsultationExample(String type, String status, String message, String response, Long amount) {}

  private Order saveOrder(List<Product> featured, List<User> customers, Random random,
      LocalDateTime placed, LocalDateTime delivered, String status) {
    User buyer = customers.get(random.nextInt(customers.size()));
    Product product = featured.get(random.nextInt(featured.size() - 1));
    ProductVariant variant = product.getDefaultVariant();
    int quantity = random.nextInt(5) == 0 ? 2 : 1;
    BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
    Order order = Order.builder().user(buyer).recipientName(buyer.getFullName()).phone(buyer.getPhone())
        .shippingAddress(buyer.getAddress()).orderDate(placed).deliveredAt(delivered).status(status)
        .subtotal(subtotal).totalAmount(subtotal)
        .paymentStatus(delivered == null ? "UNPAID" : "PAID").build();
    order.getOrderDetails().add(OrderDetail.builder().order(order).product(product).variant(variant)
        .productName(product.getName()).variantLabel(variant.getLabel()).sku(variant.getSku())
        .warrantyMonths(product.getWarrantyMonths())
        .quantity(quantity).price(variant.getPrice()).build());
    return orders.save(order);
  }

  private void seedInteractions(List<Product> featured, LocalDateTime now, Random random) {
    var batch = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, UserEvent.class);
    for (int day = 0; day < 30; day++) {
      int views = 36 + (29 - day) * 2 + random.nextInt(27) + (day % 7 < 2 ? 24 : 0);
      for (int i = 0; i < views; i++) {
        String id = "dashboard-demo-" + day + "-" + i;
        String session = "dashboard-demo-visitor-" + day + "-" + (i / 3);
        LocalDateTime timestamp = now.minusDays(day).minusSeconds(i * 11L);
        int productIndex = day < 2 && i % 2 == 0 ? 5 : i % 19 == 0 ? 5 : i % 5;
        Long productId = featured.get(productIndex).getId();
        addEvent(batch, id, session, "PAGE_VIEW", productId, null, Map.of(), timestamp);
        if (i % 3 == 0) {
          addEvent(batch, id + "-click", session, "PRODUCT_CLICK", productId, null, Map.of(), timestamp);
        }
        if (i % 9 == 0 && productIndex != 4) {
          addEvent(batch, id + "-cart", session, "ADD_TO_CART", productId, null, Map.of(), timestamp);
        }
        if (i % 7 == 0) {
          String keyword = KEYWORDS.get(Math.min(random.nextInt(7), random.nextInt(7)));
          addEvent(batch, id + "-search", session, "SEARCH", null, keyword, Map.of(), timestamp);
        }
        if (i % 13 == 0) {
          String question = QUESTIONS.get(Math.min(random.nextInt(6), random.nextInt(6)));
          addEvent(batch, id + "-chat", session, "CHAT_MESSAGE", null, null,
              Map.of("topic", ChatTopic.classify(question).label(), "durationMs", 350 + random.nextInt(900)),
              timestamp);
        }
      }
    }
    batch.execute();
    for (int i = QUESTIONS.size() - 1; i >= 0; i--) {
      jdbc.update("INSERT INTO chat_memory_messages (conversation_id, message_type, message_text, created_at)"
          + " VALUES (?, ?, ?, ?)", "dashboard-demo-chat", "user", QUESTIONS.get(i), now.minusHours(i));
    }
  }

  private void addEvent(BulkOperations batch, String id, String session, String type, Long productId,
      String keyword, Map<String, Object> meta, LocalDateTime timestamp) {
    var event = new UserEvent(id + "-" + type, session, null, type, productId, keyword,
        productId == null ? "/products" : "/products/" + productId, meta, timestamp, null, null);
    batch.replaceOne(Query.query(Criteria.where("_id").is(event.getId())), event,
        FindAndReplaceOptions.options().upsert());
  }
}
