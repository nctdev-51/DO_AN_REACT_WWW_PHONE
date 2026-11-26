package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.CampaignBannerService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {
  private final CampaignBannerRepository banners;
  private final ProductRepository products;
  private final CampaignBannerService service;

  public AdminBannerController(
      CampaignBannerRepository banners, ProductRepository products, CampaignBannerService service) {
    this.banners = banners;
    this.products = products;
    this.service = service;
  }

  private void options(Model m) {
    m.addAttribute("pageTitle", "Banner & lịch chiến dịch");
    m.addAttribute("currentPage", "banners");
    m.addAttribute("bannerProducts", products.findByStatus(1));
  }

  @GetMapping
  public String list(Model m) {
    options(m);
    m.addAttribute("banners", banners.findAll(Sort.by("position", "id")));
    return "admin/banners/list";
  }

  @GetMapping("/new")
  public String create(Model m) {
    options(m);
    m.addAttribute("bannerForm", new CampaignBannerForm());
    return "admin/banners/form";
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
}
