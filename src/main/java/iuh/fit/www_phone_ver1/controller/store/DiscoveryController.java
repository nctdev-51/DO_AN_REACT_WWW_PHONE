package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.dto.catalog.AdvisorForm;
import iuh.fit.www_phone_ver1.dto.catalog.AdvisorRecommendation;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DiscoveryController {
  private final ProductRepository products;
  private final StoreExperienceService settings;
  private final StorePolicyRepository policies;
  private final PhoneAdvisorService advisor;

  @Autowired
  public DiscoveryController(
      ProductRepository products,
      StoreExperienceService settings,
      StorePolicyRepository policies,
      PhoneAdvisorService advisor) {
    this.products = products;
    this.settings = settings;
    this.policies = policies;
    this.advisor = advisor;
  }

  public DiscoveryController(
      ProductRepository products, StoreExperienceService settings, StorePolicyRepository policies) {
    this(products, settings, policies, new PhoneAdvisorService(products));
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
      @RequestParam(required = false) String need,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String sizePreference,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String purpose,
      @RequestParam(required = false) Integer step,
      Model model) {
    model.addAttribute("brands", products.findAvailableBrands());
    AdvisorForm form;
    try {
      form = advisor.normalize(budget, need, priority, sizePreference, brand, purpose, step);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("errorMsg", ex.getMessage());
      form = advisor.normalize(null, need, priority, sizePreference, brand, purpose, 1);
    }
    List<AdvisorRecommendation> recommendations =
        form.getBudget() != null ? advisor.recommend(form) : List.of();
    model.addAttribute("advisorForm", form);
    model.addAttribute("budget", form.getBudget());
    model.addAttribute("brand", form.getBrand());
    model.addAttribute("purpose", purpose);
    model.addAttribute("need", form.getNeed());
    model.addAttribute("priority", form.getPriority());
    model.addAttribute("sizePreference", form.getSizePreference());
    model.addAttribute("recommendations", recommendations);
    if (form.getBudget() != null) {
      model.addAttribute(
          "matches",
          recommendations.stream().map(AdvisorRecommendation::getProduct).toList());
    }
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
