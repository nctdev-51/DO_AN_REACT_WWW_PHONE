package iuh.fit.www_phone_ver1.service.order;

import java.util.List;

public final class OrderStatuses {
  public static final String PENDING = "Chờ xử lý";
  public static final String SHIPPING = "Đang giao";
  public static final String DELIVERED = "Đã giao";
  public static final String CANCELLED = "Đã hủy";
  public static final List<String> ALL = List.of(PENDING, SHIPPING, DELIVERED, CANCELLED);

  private OrderStatuses() {}
}
