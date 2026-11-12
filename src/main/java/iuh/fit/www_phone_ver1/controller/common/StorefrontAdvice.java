package iuh.fit.www_phone_ver1.controller.common;

import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.service.catalog.ComparisonService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(
    basePackages = {
      "iuh.fit.www_phone_ver1.controller.store",
      "iuh.fit.www_phone_ver1.controller.account"
    })
public class StorefrontAdvice {
  private final ComparisonService comparison;

  public StorefrontAdvice(ComparisonService comparison) {
    this.comparison = comparison;
  }

  @ModelAttribute("comparisonTrayItems")
  public List<ProductVariant> comparisonTrayItems(HttpSession session) {
    if (session == null) return List.of();
    Object raw = session.getAttribute("comparisonIds");
    if (!(raw instanceof List<?> list) || list.isEmpty()) return List.of();
    List<Long> ids = list.stream().filter(Long.class::isInstance).map(Long.class::cast).toList();
    return ids.isEmpty() ? List.of() : comparison.available(ids);
  }
}

