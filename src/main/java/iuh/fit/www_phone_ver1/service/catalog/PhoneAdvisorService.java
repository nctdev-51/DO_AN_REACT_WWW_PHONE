package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.dto.catalog.AdvisorForm;
import iuh.fit.www_phone_ver1.dto.catalog.AdvisorRecommendation;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule-based phone advisor matching real catalog specs, stock, and budget without generative AI.
 */
@Service
public class PhoneAdvisorService {
  public static final BigDecimal MIN_BUDGET = BigDecimal.valueOf(1_000_000);
  public static final BigDecimal MAX_BUDGET = BigDecimal.valueOf(150_000_000);
  public static final Set<String> VALID_NEEDS =
      Set.of("EVERYDAY", "STUDY_WORK", "CAMERA", "GAMING", "CREATOR");
  public static final Set<String> VALID_PRIORITIES =
      Set.of("CAMERA", "PERFORMANCE", "BATTERY", "DISPLAY", "STORAGE");
  public static final Set<String> VALID_SIZES = Set.of("ANY", "COMPACT", "LARGE");

  private static final Pattern BATTERY_MAH = Pattern.compile("(\\d{4,5})\\s*mAh", Pattern.CASE_INSENSITIVE);
  private static final Pattern MP_VALUE = Pattern.compile("(\\d{2,3})\\s*MP", Pattern.CASE_INSENSITIVE);
  private static final Pattern SCREEN_INCHES = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:\"|inch)", Pattern.CASE_INSENSITIVE);
  private static final Pattern RAM_GB = Pattern.compile("(\\d+)\\s*GB", Pattern.CASE_INSENSITIVE);

  private final ProductRepository products;

  public PhoneAdvisorService(ProductRepository products) {
    this.products = products;
  }

  public AdvisorForm normalize(
      BigDecimal budget,
      String need,
      String priority,
      String sizePreference,
      String brand,
      String legacyPurpose,
      Integer step) {
    AdvisorForm form = new AdvisorForm();
    if (budget != null) {
      if (budget.compareTo(MIN_BUDGET) < 0 || budget.compareTo(MAX_BUDGET) > 0) {
        throw new IllegalArgumentException("Ngân sách cần nằm trong khoảng 1.000.000đ đến 150.000.000đ.");
      }
      form.setBudget(budget);
    }
    String resolvedNeed = clean(need);
    String legacy = clean(legacyPurpose);
    if (resolvedNeed == null && legacy != null) {
      resolvedNeed =
          switch (legacy) {
            case "CAMERA" -> "CAMERA";
            case "GAMING" -> "GAMING";
            case "EVERYDAY" -> "EVERYDAY";
            default -> null;
          };
    }
    form.setNeed(resolvedNeed != null && VALID_NEEDS.contains(resolvedNeed) ? resolvedNeed : null);
    String resolvedPriority = clean(priority);
    if (resolvedPriority == null && "BATTERY".equals(clean(legacyPurpose))) {
      resolvedPriority = "BATTERY";
    }
    form.setPriority(
        resolvedPriority != null && VALID_PRIORITIES.contains(resolvedPriority)
            ? resolvedPriority
            : null);
    String resolvedSize = clean(sizePreference);
    if (resolvedSize == null && "COMPACT".equals(clean(legacyPurpose))) {
      resolvedSize = "COMPACT";
    }
    form.setSizePreference(
        resolvedSize != null && VALID_SIZES.contains(resolvedSize) ? resolvedSize : "ANY");
    form.setBrand(brand == null || brand.isBlank() ? null : brand.trim());
    form.setStep(step == null ? 1 : Math.min(4, Math.max(1, step)));
    return form;
  }

  @Transactional(readOnly = true)
  public List<AdvisorRecommendation> recommend(AdvisorForm form) {
    if (form == null || form.getBudget() == null) return List.of();
    BigDecimal budget = form.getBudget();
    List<Product> catalog = products.findByStatus(1);
    List<AdvisorRecommendation> matches = new ArrayList<>();
    for (Product product : catalog) {
      if (!"PHONE".equalsIgnoreCase(product.getKind())) continue;
      if (form.getBrand() != null
          && (product.getBrand() == null
              || !product.getBrand().equalsIgnoreCase(form.getBrand()))) {
        continue;
      }
      List<ProductVariant> inStock =
          product.getVariants().stream()
              .filter(ProductVariant::isActive)
              .filter(v -> v.getStock() > 0)
              .toList();
      if (inStock.isEmpty()) continue;

      List<ProductVariant> affordable =
          inStock.stream().filter(v -> v.getPrice().compareTo(budget) <= 0).toList();
      if (affordable.isEmpty()) continue;

      ProductVariant chosen = pickBestVariant(affordable, form);
      AdvisorRecommendation rec = score(product, chosen, form, budget);
      matches.add(rec);
    }

    matches.sort(
        Comparator.comparingInt(AdvisorRecommendation::getScore)
            .reversed()
            .thenComparing(
                r -> r.getRecommendedVariant().getPrice(), Comparator.reverseOrder())
            .thenComparing(r -> r.getProduct().getId(), Comparator.reverseOrder()));
    return matches.stream().limit(6).toList();
  }

  private ProductVariant pickBestVariant(List<ProductVariant> candidates, AdvisorForm form) {
    boolean preferLargeStorage =
        "STORAGE".equals(form.getPriority())
            || "CREATOR".equals(form.getNeed())
            || "GAMING".equals(form.getNeed());
    return candidates.stream()
        .max(
            Comparator.comparingInt((ProductVariant v) -> storageGb(v.getStorage()) * (preferLargeStorage ? 3 : 1))
                .thenComparingInt(v -> ramGb(v.getRam()) * 2)
                .thenComparing(ProductVariant::getPrice))
        .orElse(candidates.get(0));
  }

  private AdvisorRecommendation score(
      Product product, ProductVariant variant, AdvisorForm form, BigDecimal budget) {
    int score = 40;
    List<String> reasons = new ArrayList<>();
    BigDecimal ratio =
        BigDecimal.ONE.subtract(
            budget.subtract(variant.getPrice()).divide(budget, 4, java.math.RoundingMode.HALF_UP));
    if (ratio.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
      score += 14;
      reasons.add("Giá sát ngân sách, tối ưu cấu hình trong tầm giá");
    } else {
      score += 8;
      reasons.add("Giá tốt trong tầm giá, tiết kiệm ngân sách");
    }

    Set<String> tags = parseTags(product.getUsageTags());
    String need = form.getNeed();
    String priority = form.getPriority();
    int battery = extractInt(BATTERY_MAH, product.getBattery());
    int mp = extractMaxInt(MP_VALUE, product.getCamera());
    int ram = ramGb(variant.getRam());
    int storage = storageGb(variant.getStorage());
    double screen = extractDouble(SCREEN_INCHES, product.getDisplay());
    boolean highRefresh = containsAny(product.getDisplay(), "120hz", "144hz", "promotion");
    boolean oledDisplay = containsAny(product.getDisplay(), "oled", "amoled");
    boolean strongChip =
        containsAny(
            product.getChipset(),
            "snapdragon 8",
            "a17",
            "a18",
            "a19",
            "dimensity 9",
            "tensor g4",
            "exynos 2");

    // Step 2: Need matching
    if ("CAMERA".equals(need) || "CREATOR".equals(need)) {
      if (tags.contains("CAMERA") || mp >= 48 || containsAny(product.getCamera(), "tele", "leica", "zeiss", "ois")) {
        score += 22;
        if (product.getCamera() != null && !product.getCamera().isBlank()) {
          reasons.add("Camera nổi bật: " + product.getCamera().trim());
        } else {
          reasons.add("Phù hợp chụp ảnh & quay video");
        }
      }
      if ("CREATOR".equals(need) && storage >= 256) {
        score += 10;
        reasons.add("Bộ nhớ " + variant.getStorage() + " thoải mái lưu ảnh, video");
      }
    } else if ("GAMING".equals(need)) {
      if (tags.contains("GAMING") || strongChip || ram >= 12) {
        score += 22;
        reasons.add(
            product.getChipset() != null && !product.getChipset().isBlank()
                ? "Hiệu năng mạnh với " + product.getChipset().trim()
                : "Phù hợp chơi game & tác vụ nặng");
      } else if (ram >= 8) {
        score += 10;
      }
    } else if ("STUDY_WORK".equals(need)) {
      if (ram >= 8 || storage >= 256 || tags.contains("EVERYDAY")) {
        score += 16;
        reasons.add("Đa nhiệm ổn định cho học tập và công việc");
      }
      if (battery >= 4500) {
        score += 8;
      }
    } else if ("EVERYDAY".equals(need)) {
      if (tags.contains("EVERYDAY")) score += 14;
      reasons.add("Cân bằng tốt cho nhu cầu liên lạc & giải trí hằng ngày");
    }

    // Step 3: Primary priority
    if ("CAMERA".equals(priority)) {
      if (mp >= 48 || tags.contains("CAMERA")) {
        score += 18;
        if (reasons.stream().noneMatch(r -> r.contains("Camera") || r.contains("chụp ảnh"))) {
          reasons.add("Ưu tiên camera sắc nét (" + (mp > 0 ? mp + "MP" : "đa ống kính") + ")");
        }
      }
    } else if ("PERFORMANCE".equals(priority)) {
      if (strongChip || ram >= 12 || tags.contains("GAMING")) {
        score += 18;
        if (reasons.stream().noneMatch(r -> r.contains("Hiệu năng"))) {
          reasons.add(
              "Hiệu năng xử lý cao"
                  + (ram > 0 ? " · RAM " + ram + "GB" : ""));
        }
      }
    } else if ("BATTERY".equals(priority)) {
      if (battery >= 5000 || tags.contains("BATTERY")) {
        score += 20;
        reasons.add(battery > 0 ? "Pin lớn " + battery + "mAh dùng bền bỉ" : "Nhóm ưu tiên thời lượng pin tốt");
      } else if (battery >= 4400) {
        score += 10;
        reasons.add("Dung lượng pin " + battery + "mAh ổn định cả ngày");
      }
    } else if ("DISPLAY".equals(priority)) {
      if (oledDisplay || highRefresh) {
        score += 18;
        reasons.add(
            "Màn hình đẹp"
                + (highRefresh ? ", tần số quét mượt 120Hz" : "")
                + (oledDisplay ? " (OLED/AMOLED)" : ""));
      }
    } else if ("STORAGE".equals(priority)) {
      if (storage >= 512) {
        score += 20;
        reasons.add("Dung lượng rất lớn " + variant.getStorage());
      } else if (storage >= 256) {
        score += 14;
        reasons.add("Bộ nhớ thoải mái " + variant.getStorage());
      }
    }

    // Step 4: Screen / form factor preference
    if ("COMPACT".equals(form.getSizePreference())) {
      if (tags.contains("COMPACT") || (screen > 0 && screen <= 6.35)) {
        score += 15;
        reasons.add("Thiết kế gọn nhẹ dễ cầm một tay");
      } else if (screen >= 6.7) {
        score -= 8;
      }
    } else if ("LARGE".equals(form.getSizePreference())) {
      if (screen >= 6.6) {
        score += 14;
        reasons.add("Màn hình lớn " + product.getDisplay());
      } else if (tags.contains("COMPACT")) {
        score -= 6;
      }
    }

    if (reasons.size() < 2 && product.getDisplay() != null && !product.getDisplay().isBlank()) {
      reasons.add("Màn hình " + product.getDisplay().trim());
    }
    List<String> distinctReasons = reasons.stream().distinct().limit(4).toList();
    String headline = distinctReasons.isEmpty() ? "Còn hàng sẵn giao trong tầm giá" : distinctReasons.get(0);
    return new AdvisorRecommendation(product, variant, score, headline, distinctReasons, true);
  }

  private static String clean(String v) {
    return v == null || v.isBlank() ? null : v.trim().toUpperCase(Locale.ROOT);
  }

  private static Set<String> parseTags(String raw) {
    if (raw == null || raw.isBlank()) return Set.of();
    Set<String> set = new HashSet<>();
    for (String part : raw.split(",")) {
      if (!part.isBlank()) set.add(part.trim().toUpperCase(Locale.ROOT));
    }
    return set;
  }

  private static boolean containsAny(String source, String... needles) {
    if (source == null || source.isBlank()) return false;
    String lower = source.toLowerCase(Locale.ROOT);
    for (String needle : needles) {
      if (lower.contains(needle)) return true;
    }
    return false;
  }

  private static int extractInt(Pattern pattern, String text) {
    if (text == null || text.isBlank()) return 0;
    Matcher m = pattern.matcher(text);
    return m.find() ? Integer.parseInt(m.group(1)) : 0;
  }

  private static int extractMaxInt(Pattern pattern, String text) {
    if (text == null || text.isBlank()) return 0;
    Matcher m = pattern.matcher(text);
    int max = 0;
    while (m.find()) {
      max = Math.max(max, Integer.parseInt(m.group(1)));
    }
    return max;
  }

  private static double extractDouble(Pattern pattern, String text) {
    if (text == null || text.isBlank()) return 0;
    Matcher m = pattern.matcher(text);
    if (!m.find()) return 0;
    return Double.parseDouble(m.group(1).replace(',', '.'));
  }

  private static int storageGb(String storage) {
    if (storage == null || storage.isBlank()) return 0;
    String upper = storage.trim().toUpperCase(Locale.ROOT);
    if (upper.contains("1TB")) return 1024;
    if (upper.contains("2TB")) return 2048;
    return extractInt(RAM_GB, upper);
  }

  private static int ramGb(String ram) {
    return extractInt(RAM_GB, ram);
  }
}
