package iuh.fit.www_phone_ver1.service.shipping;

import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GhnShippingService {
  private final GhnClient ghn;
  private final OrderRepository orders;
  private final AddressDirectory addresses;
  private final OrderHistoryService history;

  public GhnShippingService(
      GhnClient ghn,
      OrderRepository orders,
      AddressDirectory addresses,
      OrderHistoryService history) {
    this.ghn = ghn;
    this.orders = orders;
    this.addresses = addresses;
    this.history = history;
  }

  public record Estimate(BigDecimal fee, String expected, String environment) {
    public BigDecimal getFee() {
      return fee;
    }

    public String getEnvironment() {
      return environment;
    }

    public String getExpectedLabel() {
      try {
        return java.time.Instant.parse(expected)
            .atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
      } catch (java.time.DateTimeException ex) {
        return "Cần cửa hàng xác nhận";
      }
    }
  }

  public record Dispatch(boolean confirmed, String message) {}

  public Estimate preview(CheckoutForm form, int quantity, BigDecimal payable) {
    var a =
        addresses.resolve(
            form.getProvinceCode(), form.getWardCode(), form.getProvince(), form.getWard(), true);
    var parcel = new ParcelForm();
    int weight = Math.multiplyExact(Math.max(1, quantity), 500);
    if (weight >= 20000)
      throw new IllegalArgumentException(
          "Đơn nhiều sản phẩm cần cửa hàng kiểm tra đóng gói trước khi báo giao hàng.");
    parcel.setWeight(weight);
    var result =
        ghn.preview(
            payload(
                form.getRecipientName(),
                form.getPhone(),
                form.getShippingAddress(),
                a.province(),
                a.ward(),
                payable,
                form.getPaymentMethod(),
                parcel,
                ""));
    BigDecimal fee = new BigDecimal(result.path("total_fee").asString("-1"));
    if (fee.signum() < 0) throw new IllegalArgumentException("GHN chưa báo phí hợp lệ.");
    return new Estimate(fee, result.path("expected_delivery_time").asString(""), ghn.environment());
  }

  private Map<String, Object> payload(
      String name,
      String phone,
      String street,
      String province,
      String ward,
      BigDecimal amount,
      String method,
      ParcelForm parcel,
      String reference) {
    if (name == null
        || name.isBlank()
        || phone == null
        || !phone.matches("0[0-9]{9}")
        || street == null
        || street.isBlank())
      throw new IllegalArgumentException(
          "Nhập đủ người nhận, điện thoại và địa chỉ trước khi kiểm tra GHN.");
    if (parcel.getWeight() < 1
        || parcel.getWeight() >= 20000
        || parcel.getLength() < 1
        || parcel.getLength() > 200
        || parcel.getWidth() < 1
        || parcel.getWidth() > 200
        || parcel.getHeight() < 1
        || parcel.getHeight() > 200
        || parcel.getInsuranceValue() == null
        || parcel.getInsuranceValue().signum() < 0
        || parcel.getInsuranceValue().compareTo(BigDecimal.valueOf(5000000)) > 0)
      throw new IllegalArgumentException("Thông tin kiện hàng không hợp lệ.");
    var cod = "COD".equals(method) ? amount : BigDecimal.ZERO;
    if (cod == null || cod.signum() < 0 || cod.compareTo(BigDecimal.valueOf(50000000)) > 0)
      throw new IllegalArgumentException("Khoản COD vượt giới hạn 50 triệu của luồng GHN này.");
    var destination = ghn.destination(province, ward);
    province = destination.province();
    ward = destination.ward();
    var p = new LinkedHashMap<String, Object>();
    p.put("to_name", name);
    p.put("to_phone", phone);
    p.put("to_address", street);
    p.put("to_province_name", province);
    p.put("to_ward_name", ward);
    p.put("is_new_to_address", true);
    p.put("payment_type_id", 1);
    p.put("required_note", "CHOXEMHANGKHONGTHU");
    p.put("weight", parcel.getWeight());
    p.put("length", parcel.getLength());
    p.put("width", parcel.getWidth());
    p.put("height", parcel.getHeight());
    p.put("service_type_id", 2);
    p.put("cod_amount", cod.longValueExact());
    p.put("insurance_value", parcel.getInsuranceValue().longValueExact());
    p.put("content", "Điện thoại / phụ kiện PTV Mobile");
    if (!reference.isBlank()) p.put("client_order_code", reference);
    return p;
  }

  @Transactional
  public Dispatch create(Long id, ParcelForm parcel) {
    if (!ghn.isAvailable()) throw new IllegalArgumentException("GHN chưa được cấu hình.");
    var o =
        orders
            .findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn."));
    if (!OrderStatuses.PENDING.equals(o.getStatus())
        || !Set.of("UNPAID", "PAID").contains(o.getPaymentStatus()))
      throw new IllegalArgumentException(
          "Chỉ tạo vận đơn khi đơn còn chờ xử lý và chưa hoàn tiền.");
    if ("VNPAY".equals(o.getPaymentMethod()) && !"PAID".equals(o.getPaymentStatus()))
      throw new IllegalArgumentException("Đơn VNPay phải thanh toán trước khi giao vận.");
    if (o.getGhnEnvironment() != null && !ghn.environment().equals(o.getGhnEnvironment()))
      throw new IllegalArgumentException("Môi trường GHN của đơn khác cấu hình hiện tại.");
    if ("GHN".equals(o.getCarrier()) && o.getTrackingCode() != null)
      return new Dispatch(true, "Đơn đã có vận đơn GHN. Hãy đồng bộ để kiểm tra.");
    if (o.getTrackingCode() != null)
      throw new IllegalArgumentException("Đơn đã có vận đơn khác; hãy xử lý vận đơn cũ trước.");
    var a =
        addresses.resolve(o.getProvinceCode(), o.getWardCode(), o.getProvince(), o.getWard(), true);
    if (o.getGhnRequestJson() == null) {
      var code = "PTV-" + (o.getPaymentReference() == null ? o.getId() : o.getPaymentReference());
      var p =
          payload(
              o.getRecipientName(),
              o.getPhone(),
              o.getShippingAddress(),
              a.province(),
              a.ward(),
              o.getTotalAmount(),
              o.getPaymentMethod(),
              parcel,
              code);
      o.setGhnRequestJson(new JsonMapper().writeValueAsString(p));
      o.setGhnEnvironment(ghn.environment());
    }
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> p = new JsonMapper().readValue(o.getGhnRequestJson(), Map.class);
      var result = ghn.create(p);
      String tracking = result.path("order_code").asString("");
      if (!tracking.matches("[A-Za-z0-9_-]{3,80}"))
        throw new IllegalArgumentException("GHN chưa trả mã vận đơn hợp lệ.");
      o.setCarrier("GHN");
      o.setTrackingCode(tracking);
      o.setCarrierStatus("ready_to_pick");
      o.setCarrierUpdatedAt(LocalDateTime.now());
      history.append(o, "GHN", "Đã tạo vận đơn " + tracking + " (" + ghn.environment() + ").");
      return new Dispatch(true, "Đã tạo vận đơn GHN " + tracking + " (" + ghn.environment() + ").");
    } catch (IllegalArgumentException ex) {
      o.setCarrierStatus("UNCONFIRMED");
      history.append(
          o,
          "GHN",
          "Chưa xác nhận kết quả tạo vận đơn. Gửi lại sẽ dùng cùng mã yêu cầu để tránh trùng đơn.");
      return new Dispatch(false, ex.getMessage());
    }
  }

  @Transactional
  public void sync(Long id) {
    var o =
        orders
            .findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn."));
    if (!"GHN".equals(o.getCarrier())
        || o.getTrackingCode() == null
        || !ghn.environment().equals(o.getGhnEnvironment()))
      throw new IllegalArgumentException("Đơn chưa có vận đơn GHN cùng môi trường.");
    var data = ghn.detail(o.getTrackingCode());
    if (!o.getTrackingCode().equals(data.path("order_code").asString())
        || !ghn.ownsShop(data.path("shop_id").asLong()))
      throw new IllegalArgumentException("GHN trả thông tin vận đơn không khớp.");
    String next = data.path("status").asString("");
    if (!next.matches("[a-z_]{3,40}"))
      throw new IllegalArgumentException("GHN chưa trả trạng thái hợp lệ.");
    if (!next.equals(o.getCarrierStatus()))
      history.append(
          o,
          "GHN",
          "Trạng thái vận chuyển: "
              + next
              + ". Nhân viên kiểm tra giao nhận trước khi chốt trạng thái đơn.");
    o.setCarrierStatus(next);
    o.setCarrierUpdatedAt(LocalDateTime.now());
  }
}
