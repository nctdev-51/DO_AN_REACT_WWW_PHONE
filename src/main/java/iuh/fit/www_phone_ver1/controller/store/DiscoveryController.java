package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DiscoveryController {
  private final ProductRepository products;
  private final StoreExperienceService settings;
  private final StorePolicyRepository policies;

  public DiscoveryController(
      ProductRepository products, StoreExperienceService settings, StorePolicyRepository policies) {
    this.products = products;
    this.settings = settings;
    this.policies = policies;
  }

  @GetMapping("/search/suggestions")
  @ResponseBody
  public List<Map<String, Object>> suggestions(@RequestParam(defaultValue = "") String q) {
    if (q.trim().length() < 2 || q.length() > 100) return List.of();
    var filter = new CatalogFilter(q.trim(), null, null, null, null, null, null, false);
    return products.findAll(filter.specification("relevance"), PageRequest.of(0, 6)).stream()
        .map(
            p ->
                Map.<String, Object>of(
                    "id",
                    p.getId(),
                    "name",
                    p.getName(),
                    "image",
                    Objects.toString(p.getImage(), ""),
                    "price",
                    filter.selected(p).getPrice()))
        .toList();
  }

  @GetMapping("/choose")
  public String choose(
      @RequestParam(required = false) BigDecimal budget,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String purpose,
      Model model) {
    model.addAttribute("brands", products.findAvailableBrands());
    purpose =
        Set.of("EVERYDAY", "CAMERA", "GAMING", "BATTERY", "COMPACT")
                .contains(Objects.toString(purpose, ""))
            ? purpose
            : null;
    model.addAttribute("purpose", purpose);
    model.addAttribute("budget", budget);
    model.addAttribute("brand", brand);
    if (budget != null && budget.signum() > 0)
      model.addAttribute(
          "matches",
          products
              .findAll(
                  new CatalogFilter(
                          null,
                          brand == null || brand.isBlank() ? null : brand,
                          null,
                          null,
                          budget,
                          null,
                          null,
                          true,
                          "PHONE",
                          null,
                          purpose)
                      .specification("price-desc"),
                  PageRequest.of(0, 6))
              .getContent());
    return "store/discovery/choose";
  }

  @GetMapping("/recently-viewed")
  public String recent(HttpSession session, Model model) {
    @SuppressWarnings("unchecked")
    var ids = (List<Long>) session.getAttribute("recentProducts");
    var found =
        ids == null ? List.<iuh.fit.www_phone_ver1.entity.Product>of() : products.findAllById(ids);
    model.addAttribute(
        "recentProducts",
        ids == null
            ? found
            : ids.stream()
                .flatMap(
                    id -> found.stream().filter(p -> p.getId().equals(id) && p.getStatus() == 1))
                .toList());
    return "store/discovery/recent";
  }

  @GetMapping("/policies/{page}")
  public String policy(@PathVariable String page, Model model) {
    if (!Set.of("shipping", "returns", "warranty", "payment", "faq", "contact").contains(page))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND);
    model.addAttribute("policyPage", page);
    model.addAttribute("experience", settings.get());
    model.addAttribute("policy", policies.findById(1L).orElseThrow());
    return "store/discovery/policy";
  }
}
