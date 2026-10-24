package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.dto.catalog.ComparisonView;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ComparisonService {
  public static final int LIMIT = 3;
  private final ProductVariantRepository variants;
  private final ProductRepository products;

  public ComparisonService(ProductVariantRepository variants, ProductRepository products) {
    this.variants = variants;
    this.products = products;
  }

  public List<ProductVariant> available(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    List<Long> unique = ids.stream().filter(Objects::nonNull).distinct().limit(LIMIT).toList();
    Map<Long, ProductVariant> byId =
        variants.findAllById(unique).stream()
            .filter(v -> v.isActive() && v.getProduct().getStatus() == 1)
            .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
    List<ProductVariant> result = new ArrayList<>();
    Set<Long> productIds = new HashSet<>();
    for (Long id : unique) {
      ProductVariant v = byId.get(id);
      if (v != null && productIds.add(v.getProduct().getId())) {
        v.getProduct().getVariants().size();
        result.add(v);
      }
    }
    return result;
  }

  public List<Long> select(List<Long> current, Long productId, Long variantId) {
    ProductVariant next;
    if (variantId != null) {
      next =
          available(List.of(variantId)).stream()
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Phiên bản không còn được kinh doanh."));
      if (productId != null && !productId.equals(next.getProduct().getId()))
        throw new IllegalArgumentException("Phiên bản không thuộc sản phẩm đã chọn.");
    } else {
      Product product =
          products
              .findById(productId == null ? -1L : productId)
              .filter(p -> p.getStatus() == 1)
              .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm đang bán."));
      next =
          product.getVariants().stream()
              .filter(ProductVariant::isActive)
              .min(
                  Comparator.comparing((ProductVariant v) -> v.getStock() == 0)
                      .thenComparing(ProductVariant::getPrice)
                      .thenComparing(ProductVariant::getId))
              .orElseThrow(
                  () -> new IllegalArgumentException("Sản phẩm chưa có phiên bản để so sánh."));
    }
    List<ProductVariant> selected = new ArrayList<>(available(current));
    for (int i = 0; i < selected.size(); i++) {
      if (selected.get(i).getProduct().getId().equals(next.getProduct().getId())) {
        selected.set(i, next);
        return selected.stream().map(ProductVariant::getId).toList();
      }
    }
    if (selected.size() >= LIMIT)
      throw new IllegalArgumentException(
          "Chỉ so sánh tối đa 3 sản phẩm. Hãy bỏ một sản phẩm trước khi thêm.");
    selected.add(next);
    return selected.stream().map(ProductVariant::getId).toList();
  }

  public ComparisonView view(List<Long> ids) {
    List<ProductVariant> selected = available(ids);
    var money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    List<ComparisonView.Row> rows = new ArrayList<>();
    row(rows, selected, "Giá bán của phiên bản", v -> money.format(v.getPrice()));
    row(rows, selected, "Thương hiệu", v -> v.getProduct().getBrand());
    row(rows, selected, "Màu sắc", ProductVariant::getColor);
    row(rows, selected, "Bộ nhớ trong", ProductVariant::getStorage);
    row(rows, selected, "RAM", ProductVariant::getRam);
    row(rows, selected, "Màn hình", v -> v.getProduct().getDisplay());
    row(rows, selected, "Chip xử lý", v -> v.getProduct().getChipset());
    row(rows, selected, "Camera sau", v -> v.getProduct().getCamera());
    row(rows, selected, "Pin", v -> v.getProduct().getBattery());
    row(rows, selected, "Hệ điều hành", v -> v.getProduct().getOperatingSystem());
    row(
        rows,
        selected,
        "Bảo hành",
        v ->
            v.getProduct().getWarrantyMonths() == null
                ? null
                : v.getProduct().getWarrantyMonths() + " tháng");
    row(
        rows,
        selected,
        "Tình trạng phiên bản",
        v -> v.getStock() > 0 ? "Còn hàng" : "Tạm hết hàng");
    String share =
        "/compare?ids="
            + selected.stream().map(v -> v.getId().toString()).collect(Collectors.joining(","));
    return new ComparisonView(selected, rows, share);
  }

  private void row(
      List<ComparisonView.Row> rows,
      List<ProductVariant> selected,
      String label,
      Function<ProductVariant, String> value) {
    List<String> cells =
        selected.stream()
            .map(value)
            .map(s -> s == null || s.isBlank() ? "Chưa cập nhật" : s.trim())
            .toList();
    boolean different =
        cells.stream()
                .map(s -> s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " "))
                .distinct()
                .count()
            > 1;
    rows.add(new ComparisonView.Row(label, cells, different));
  }
}
