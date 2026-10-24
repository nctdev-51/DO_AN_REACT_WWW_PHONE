package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.catalog.ComparisonService;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/compare")
public class ComparisonController {
  private static final String SELECTION = "comparisonIds";
  private final ComparisonService comparison;
  private final ProductRepository products;

  public ComparisonController(ComparisonService comparison, ProductRepository products) {
    this.comparison = comparison;
    this.products = products;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String ids,
      @RequestParam(defaultValue = "") String keyword,
      HttpSession session,
      Model model) {
    List<Long> selected = selection(session);
    if (ids != null) {
      try {
        if (ids.length() > 80) throw new IllegalArgumentException();
        selected =
            ids.isBlank()
                ? List.of()
                : Arrays.stream(ids.split(",", -1)).map(Long::valueOf).toList();
        if (selected.size() > ComparisonService.LIMIT || selected.stream().anyMatch(id -> id < 1))
          throw new IllegalArgumentException();
      } catch (IllegalArgumentException e) {
        selected = List.of();
        model.addAttribute("errorMsg", "Liên kết so sánh không hợp lệ. Chọn tối đa 3 sản phẩm.");
      }
    }
    var view = comparison.view(selected);
    model.addAttribute("comparison", view);
    model.addAttribute("sharedSelection", ids != null);
    model.addAttribute(
        "selectionIds",
        view.getVariants().stream()
            .map(v -> v.getId().toString())
            .collect(java.util.stream.Collectors.joining(",")));
    keyword = keyword.trim();
    model.addAttribute("keyword", keyword);
    model.addAttribute(
        "suggestions",
        products
            .search(
                keyword.isBlank() ? null : keyword,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 8, Sort.by("id").descending()))
            .getContent());
    return "store/compare/index";
  }

  @PostMapping("/add")
  public String add(
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long variantId,
      @RequestParam(required = false) String baseIds,
      HttpSession session,
      RedirectAttributes flash) {
    try {
      synchronized (session) {
        List<Long> current = selection(session);
        // A shared comparison can be adopted explicitly when adding/changing a product.
        if (baseIds != null) {
          if (baseIds.length() > 80)
            throw new IllegalArgumentException("Danh sách so sánh không hợp lệ.");
          current =
              baseIds.isBlank()
                  ? List.of()
                  : Arrays.stream(baseIds.split(",", -1)).map(Long::valueOf).toList();
          if (current.size() > ComparisonService.LIMIT)
            throw new IllegalArgumentException("Chỉ so sánh tối đa 3 sản phẩm.");
        }
        session.setAttribute(SELECTION, comparison.select(current, productId, variantId));
      }
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(
          "errorMsg",
          e instanceof NumberFormatException ? "Danh sách so sánh không hợp lệ." : e.getMessage());
    }
    return "redirect:/compare";
  }

  @PostMapping("/remove")
  public String remove(@RequestParam Long variantId, HttpSession session) {
    synchronized (session) {
      session.setAttribute(
          SELECTION, selection(session).stream().filter(id -> !id.equals(variantId)).toList());
    }
    return "redirect:/compare";
  }

  @PostMapping("/clear")
  public String clear(HttpSession session) {
    synchronized (session) {
      session.removeAttribute(SELECTION);
    }
    return "redirect:/compare";
  }

  @SuppressWarnings("unchecked")
  private List<Long> selection(HttpSession session) {
    Object value = session.getAttribute(SELECTION);
    return value instanceof List<?> ? List.copyOf((List<Long>) value) : List.of();
  }
}
