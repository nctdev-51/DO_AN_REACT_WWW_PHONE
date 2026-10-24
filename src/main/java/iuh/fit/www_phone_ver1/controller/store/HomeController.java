package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final ProductRepository productRepository;
  private final iuh.fit.www_phone_ver1.repository.CampaignBannerRepository banners;
  private final iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience;

  public HomeController(
      ProductRepository productRepository,
      iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience,
      iuh.fit.www_phone_ver1.repository.CampaignBannerRepository banners) {
    this.productRepository = productRepository;
    this.experience = experience;
    this.banners = banners;
  }

  @GetMapping("/")
  public String home(Model model) {
    var live = banners.liveAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
    model.addAttribute(
        "heroBanners",
        live.stream().filter(v -> "HERO".equals(v.getPlacement())).limit(4).toList());
    model.addAttribute(
        "tileBanners",
        live.stream().filter(v -> "TILE".equals(v.getPlacement())).limit(12).toList());
    var settings = experience.get();
    model.addAttribute("experience", settings);
    model.addAttribute("brands", productRepository.findAvailableBrands());
    model.addAttribute(
        "heroProduct",
        productRepository
            .findBySku(java.util.Objects.toString(settings.getHeroSku(), ""))
            .filter(p -> p.getStatus() == 1)
            .orElse(null));
    model.addAttribute(
        "showcaseProduct",
        productRepository
            .findBySku(java.util.Objects.toString(settings.getSecondHeroSku(), ""))
            .filter(p -> p.getStatus() == 1)
            .orElse(null));
    model.addAttribute(
        "featuredProducts",
        productRepository
            .findAll(
                new iuh.fit.www_phone_ver1.service.catalog.CatalogFilter(
                        null, null, null, null, null, null, null, false, "PHONE")
                    .specification("newest"),
                PageRequest.of(0, 8))
            .getContent());
    if (settings.getFeaturedSkus() != null && !settings.getFeaturedSkus().isBlank())
      model.addAttribute(
          "featuredProducts",
          java.util.Arrays.stream(settings.getFeaturedSkus().split(","))
              .map(String::trim)
              .distinct()
              .map(productRepository::findBySku)
              .flatMap(java.util.Optional::stream)
              .filter(p -> p.getStatus() == 1)
              .limit(12)
              .toList());
    var budget =
        new iuh.fit.www_phone_ver1.service.catalog.CatalogFilter(
            null,
            null,
            null,
            null,
            java.math.BigDecimal.valueOf(10000000),
            null,
            null,
            true,
            "PHONE");
    var budgetProducts =
        productRepository
            .findAll(budget.specification("newest"), PageRequest.of(0, 6))
            .getContent();
    model.addAttribute("budgetProducts", budgetProducts);
    var selected = new java.util.HashMap<Long, iuh.fit.www_phone_ver1.entity.ProductVariant>();
    budgetProducts.forEach(p -> selected.put(p.getId(), budget.selected(p)));
    model.addAttribute("budgetVariants", selected);
    return "store/home";
  }
}
