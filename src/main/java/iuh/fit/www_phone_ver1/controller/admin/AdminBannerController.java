package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.CampaignBannerService;
import iuh.fit.www_phone_ver1.service.catalog.CatalogFilter;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {
  private static final ZoneId SHOP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  private final CampaignBannerRepository banners;
  private final ProductRepository products;
  private final CampaignBannerService service;

  public AdminBannerController(
      CampaignBannerRepository banners, ProductRepository products, CampaignBannerService service) {
    this.banners = banners;
    this.products = products;
    this.service = service;
  }

  private void heading(Model m) {
    m.addAttribute("pageTitle", "Banner & lịch chiến dịch");
    m.addAttribute("currentPage", "banners");
  }

  private void options(Model m) {
    heading(m);
    m.addAttribute("bannerProducts", products.findByStatus(1));
  }

  @GetMapping
  public String list(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "") String state,
      Model m) {
    heading(m);
    String keyword = q.trim();
    m.addAttribute("q", keyword);
    m.addAttribute("from", from);
    m.addAttribute("to", to);
    LocalDateTime now = LocalDateTime.now(SHOP_ZONE);
    m.addAttribute("bannerNow", now);
    m.addAttribute("state", state);
    m.addAttribute("hasFilters", !keyword.isEmpty() || from != null || to != null || !state.isEmpty());
    if (!List.of("", "off", "scheduled", "live", "expired").contains(state)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái banner không hợp lệ.");
    }
    if (from != null && to != null && from.isAfter(to)) {
      m.addAttribute("filterError", "Đến ngày phải bằng hoặc sau Từ ngày.");
      m.addAttribute("banners", List.of());
      return "admin/banners/list";
    }
    String literal = keyword.toLowerCase(Locale.ROOT)
        .replace("!", "!!").replace("%", "!%").replace("_", "!_");
    m.addAttribute("banners", banners.searchSchedule("%" + literal + "%",
        from == null ? null : from.atStartOfDay(), to == null ? null : to.plusDays(1).atStartOfDay(),
        state, now));
    return "admin/banners/list";
  }

  @GetMapping("/new")
  public String create(
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) String keyword,
      Model m) {
    options(m);
    var form = new CampaignBannerForm();
    if (keyword != null && !keyword.isBlank()) {
      String term = limit(keyword.trim(), 120);
      var filter = new CatalogFilter(term, null, null, null, null, null, null, false);
      var match = products.findAll(filter.specification("relevance"), PageRequest.of(0, 1))
          .stream().findFirst();
      form.setTitle(limit("Khám phá " + term, 120));
      form.setSubtitle("Tìm sản phẩm phù hợp với nhu cầu của bạn.");
      form.setActionLabel("Xem sản phẩm phù hợp");
      form.setSearchKeyword(term);
      form.setTarget(match.isPresent() ? "SEARCH" : "CHOOSE");
      match.ifPresent(product -> {
        form.setProductId(product.getId());
        m.addAttribute("bannerImage", product.getImage());
      });
      m.addAttribute("draftNote", match.isPresent()
          ? "Đề xuất từ từ khóa tìm kiếm. Banner sẽ mở danh sách sản phẩm khớp từ khóa."
          : "Từ khóa chưa có sản phẩm phù hợp. Bản nháp đang dẫn tới trang chọn theo nhu cầu.");
    } else if (productId != null) {
      var product = products.findById(productId).filter(p -> p.getStatus() == 1)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Sản phẩm đã ngừng bán hoặc không còn tồn tại."));
      form.setProductId(productId);
      form.setTitle(limit("Khám phá " + product.getName(), 120));
      form.setSubtitle("Xem thông số, phiên bản và giá hiện tại.");
      form.setActionLabel("Xem sản phẩm");
      m.addAttribute("bannerImage", product.getImage());
      m.addAttribute("draftNote", "Đề xuất từ bảng tương tác sản phẩm.");
    }
    if (m.containsAttribute("draftNote")) {
      LocalDateTime start = LocalDateTime.now(SHOP_ZONE)
          .withSecond(0).withNano(0);
      form.setStartsAt(start);
      form.setEndsAt(start.plusDays(7));
      form.setActive(false);
    }
    m.addAttribute("bannerForm", form);
    return "admin/banners/form";
  }

  private static String limit(String text, int length) {
    if (text.length() <= length) return text;
    int end = Character.isHighSurrogate(text.charAt(length - 1)) ? length - 1 : length;
    return text.substring(0, end);
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model m) {
    var b =
        banners
            .findById(id)
            .orElseThrow(
                () ->
                    new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
    var f = new CampaignBannerForm();
    BeanUtils.copyProperties(b, f);
    f.setProductId(b.getProduct() == null ? null : b.getProduct().getId());
    options(m);
    m.addAttribute("bannerId", id);
    m.addAttribute("bannerForm", f);
    m.addAttribute("bannerImage", b.getImagePath());
    return "admin/banners/form";
  }

  @PostMapping("/save")
  public String save(
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute("bannerForm") CampaignBannerForm f,
      BindingResult errors,
      @RequestParam(required = false) MultipartFile image,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        service.save(id, f, image);
        flash.addFlashAttribute("successMsg", "Đã lưu banner và lịch hiển thị.");
        return "redirect:/admin/banners";
      } catch (IllegalArgumentException
          | java.io.IOException
          | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        errors.reject(
            "banner",
            ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : "Chưa lưu được ảnh hoặc dữ liệu vừa thay đổi. Hãy thử lại.");
      }
    options(m);
    m.addAttribute("bannerId", id);
    return "admin/banners/form";
  }

  @PostMapping("/{id}/delete")
  public String delete(
      @PathVariable Long id, @RequestParam long version, RedirectAttributes flash) {
    try {
      service.delete(id, version);
      flash.addFlashAttribute("successMsg", "Đã xóa banner.");
    } catch (IllegalArgumentException
        | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
      flash.addFlashAttribute(
          "errorMsg", "Banner vừa thay đổi hoặc không còn tồn tại. Hãy tải lại.");
    }
    return "redirect:/admin/banners";
  }

  @PostMapping("/batch")
  public String batch(@RequestParam(required = false) java.util.List<Long> ids,
      @RequestParam String action, @RequestParam java.util.Map<String, String> parameters,
      RedirectAttributes flash) {
    if (!java.util.Set.of("enable", "disable", "delete").contains(action)) {
      flash.addFlashAttribute("errorMsg", "Chọn thao tác hợp lệ.");
    } else {
      AdminBatch.run(ids, id -> {
        String version = parameters.get("version_" + id);
        if (version == null || !version.matches("[0-9]{1,18}")) {
          throw new IllegalArgumentException("Thiếu phiên bản banner. Hãy tải lại.");
        }
        if ("delete".equals(action)) service.delete(id, Long.parseLong(version));
        else service.setBannerActive(id, Long.parseLong(version), "enable".equals(action));
        return null;
      }, flash);
    }
    return "redirect:/admin/banners";
  }

  @PostMapping("/{id}/enable")
  public String enable(
      @PathVariable Long id, @RequestParam long version,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String from,
      @RequestParam(defaultValue = "") String to,
      @RequestParam(defaultValue = "") String state,
      RedirectAttributes flash) {
    try {
      service.setBannerActive(id, version, true);
      flash.addFlashAttribute("successMsg", "Đã bật banner theo lịch đã lưu.");
    } catch (IllegalArgumentException | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
      flash.addFlashAttribute("errorMsg", "Banner vừa thay đổi hoặc không còn tồn tại. Hãy tải lại.");
    }
    if (!q.isBlank()) flash.addAttribute("q", q);
    if (!from.isBlank()) flash.addAttribute("from", from);
    if (!to.isBlank()) flash.addAttribute("to", to);
    if (!state.isBlank()) flash.addAttribute("state", state);
    return "redirect:/admin/banners";
  }
}
