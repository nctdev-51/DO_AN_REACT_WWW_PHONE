package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.entity.*;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;

/** A single predicate shared by product filtering, price ordering and card selection. */
public record CatalogFilter(
    String keyword,
    String brand,
    Long categoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    String storage,
    String ram,
    boolean inStock,
    String kind,
    String color,
    String purpose) {
  public CatalogFilter(
      String keyword,
      String brand,
      Long categoryId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String storage,
      String ram,
      boolean inStock,
      String kind) {
    this(keyword, brand, categoryId, minPrice, maxPrice, storage, ram, inStock, kind, null, null);
  }

  public CatalogFilter(
      String keyword,
      String brand,
      Long categoryId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String storage,
      String ram,
      boolean inStock) {
    this(keyword, brand, categoryId, minPrice, maxPrice, storage, ram, inStock, null);
  }

  public boolean matches(ProductVariant v) {
    return v.isActive()
        && (blank(color) || color.equals(v.getColor()))
        && (blank(storage) || storage.equals(v.getStorage()))
        && (blank(ram) || ram.equals(v.getRam()))
        && (!inStock || v.getStock() > 0)
        && (minPrice == null || v.getPrice().compareTo(minPrice) >= 0)
        && (maxPrice == null || v.getPrice().compareTo(maxPrice) <= 0);
  }

  public ProductVariant selected(Product p) {
    return p.getVariants().stream()
        .filter(this::matches)
        .min(Comparator.comparing(ProductVariant::getPrice).thenComparing(ProductVariant::getId))
        .orElse(null);
  }

  private static boolean blank(String s) {
    return s == null || s.isBlank();
  }

  public Specification<Product> specification(String sort) {
    return (root, query, cb) -> {
      List<Predicate> filters = new ArrayList<>();
      filters.add(cb.equal(root.get("status"), 1));
      if (!blank(kind)) filters.add(cb.equal(root.get("kind"), kind));
      if (!blank(purpose))
        filters.add(
            cb.like(
                cb.concat(cb.concat(",", cb.coalesce(root.<String>get("usageTags"), "")), ","),
                "%," + purpose + ",%"));
      if (!blank(brand)) filters.add(cb.equal(root.get("brand"), brand));
      if (categoryId != null) filters.add(cb.equal(root.get("category").get("id"), categoryId));
      if (!blank(keyword)) {
        // Every token must match; wildcards are escaped rather than interpreted as patterns.
        for (String token : keyword.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
          String escaped = token.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
          String pattern = "%" + escaped + "%";
          filters.add(
              cb.or(
                  cb.like(cb.lower(root.get("name")), pattern, '\\'),
                  cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                  cb.like(cb.lower(root.get("sku")), pattern, '\\'),
                  cb.like(cb.lower(root.get("chipset")), pattern, '\\')));
        }
      }
      Subquery<BigDecimal> price = query.subquery(BigDecimal.class);
      Root<ProductVariant> v = price.from(ProductVariant.class);
      List<Predicate> choices = new ArrayList<>();
      choices.add(cb.equal(v.get("product"), root));
      choices.add(cb.isTrue(v.get("active")));
      if (!blank(color)) choices.add(cb.equal(v.get("color"), color));
      if (!blank(storage)) choices.add(cb.equal(v.get("storage"), storage));
      if (!blank(ram)) choices.add(cb.equal(v.get("ram"), ram));
      if (inStock) choices.add(cb.gt(v.get("stock"), 0));
      if (minPrice != null) choices.add(cb.ge(v.get("price"), minPrice));
      if (maxPrice != null) choices.add(cb.le(v.get("price"), maxPrice));
      price.select(cb.min(v.<BigDecimal>get("price"))).where(choices.toArray(Predicate[]::new));
      filters.add(cb.isNotNull(price));
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        Expression<Integer> relevance =
            cb.<Integer>selectCase()
                .when(
                    cb.equal(
                        cb.lower(root.get("name")),
                        Objects.toString(keyword, "").toLowerCase(Locale.ROOT)),
                    0)
                .when(
                    cb.like(
                        cb.lower(root.get("name")),
                        "%"
                            + Objects.toString(keyword, "")
                                .toLowerCase(Locale.ROOT)
                                .replace("\\", "\\\\")
                                .replace("%", "\\%")
                                .replace("_", "\\_")
                            + "%",
                        '\\'),
                    1)
                .otherwise(2);
        if ("rating".equals(sort)) {
          var average = query.subquery(Double.class);
          var review = average.from(Review.class);
          average
              .select(cb.avg(review.<Integer>get("rating")))
              .where(cb.equal(review.get("product"), root));
          var count = query.subquery(Long.class);
          var rating = count.from(Review.class);
          count.select(cb.count(rating)).where(cb.equal(rating.get("product"), root));
          query.orderBy(cb.desc(cb.coalesce(average, 0d)), cb.desc(count), cb.desc(root.get("id")));
        } else if ("bestselling".equals(sort)) {
          var sold = query.subquery(Long.class);
          var detail = sold.from(OrderDetail.class);
          sold.select(cb.sumAsLong(detail.<Integer>get("quantity")))
              .where(
                  cb.equal(detail.get("product"), root),
                  cb.equal(detail.get("order").get("status"), "Đã giao"),
                  detail.get("order").get("paymentStatus").in("PAID", "PARTIALLY_REFUNDED"));
          query.orderBy(cb.desc(cb.coalesce(sold, 0L)), cb.desc(root.get("id")));
        } else
          query.orderBy(
              "relevance".equals(sort) && !blank(keyword)
                  ? List.of(cb.asc(relevance), cb.desc(root.get("id")))
                  : "price-asc".equals(sort)
                      ? List.of(cb.asc(price), cb.desc(root.get("id")))
                      : "price-desc".equals(sort)
                          ? List.of(cb.desc(price), cb.desc(root.get("id")))
                          : List.of(cb.desc(root.get("id"))));
      }
      return cb.and(filters.toArray(Predicate[]::new));
    };
  }
}
