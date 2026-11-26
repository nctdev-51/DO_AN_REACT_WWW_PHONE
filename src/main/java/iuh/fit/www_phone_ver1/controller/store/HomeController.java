package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.catalog.HomePersonalization;
import iuh.fit.www_phone_ver1.service.catalog.CatalogFilter;
import iuh.fit.www_phone_ver1.service.tracking.TrackingSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final HomePersonalization personalization;
  private final ProductRepository productRepository;
  private final iuh.fit.www_phone_ver1.repository.CampaignBannerRepository banners;
  private final iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience;
  private final iuh.fit.www_phone_ver1.service.catalog.StorefrontCatalogService catalogService;
  private final iuh.fit.www_phone_ver1.service.vector.RecommendationService recommendationService;

  public HomeController(
      ProductRepository productRepository,
      HomePersonalization personalization,
      iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience,
      iuh.fit.www_phone_ver1.repository.CampaignBannerRepository banners,
      ObjectProvider<iuh.fit.www_phone_ver1.service.catalog.StorefrontCatalogService> catalogServiceProvider,
      ObjectProvider<iuh.fit.www_phone_ver1.service.vector.RecommendationService> recommendationServiceProvider) {
    this.productRepository = productRepository;
    this.personalization = personalization;
    this.experience = experience;
    this.banners = banners;
    this.catalogService = catalogServiceProvider.getIfAvailable();
    this.recommendationService = recommendationServiceProvider.getIfAvailable();
  }

  @GetMapping("/")
  public String home(Model model, HttpSession session, HttpServletRequest request,
      HttpServletResponse response) {
    String visitorId = TrackingSession.existingId(request);
    if (visitorId == null) TrackingSession.getOrCreateId(request, response);
    var interest = personalization.interest(visitorId);
    model.addAttribute("homeInterest", interest.name());
    if (interest == HomePersonalization.Interest.CAMERA
        || interest == HomePersonalization.Interest.GAMING) {
      var collection = new CatalogFilter(null, null, null, null, null, null, null,
          true, "PHONE", null, interest.name());
      model.addAttribute("interestProducts",
          productRepository.findAll(collection.specification("newest"), PageRequest.of(0, 4))
              .getContent());
    }
    var live = banners.liveAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")))
        .stream()
        .sorted(java.util.Comparator.comparing(b -> !interest.matches(b.getProduct())))
        .toList();
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

    if (catalogService != null && recommendationService != null && session != null) {
      var recent = catalogService.recentProducts(session, null);
      if (!recent.isEmpty()) {
        var anchor = recent.get(0);
        var recsOpt = recommendationService.recommend(anchor.getId(), 4);
        if (recsOpt.isPresent()) {
          var recIds =
              recsOpt.get().recommendations().stream()
                  .map(iuh.fit.www_phone_ver1.dto.catalog.RecommendedProductDTO::id)
                  .toList();
          var personalizedList =
              productRepository.findAllById(recIds).stream()
                  .filter(p -> p.getStatus() == 1)
                  .toList();
          if (!personalizedList.isEmpty()) {
            model.addAttribute("personalizedProducts", personalizedList);
            model.addAttribute("personalizedAnchorName", anchor.getName());
          }
        }
      }
    }

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
