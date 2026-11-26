package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.repository.ProductMediaRepository;
import iuh.fit.www_phone_ver1.service.catalog.*;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products/{productId}/media")
public class AdminProductMediaController {
  private final CatalogService catalog;
  private final ProductMediaRepository media;
  private final ProductMediaService service;

  public AdminProductMediaController(
      CatalogService catalog, ProductMediaRepository media, ProductMediaService service) {
    this.catalog = catalog;
    this.media = media;
    this.service = service;
  }

  @GetMapping
  public String index(@PathVariable Long productId, Model model) {
    var product = catalog.product(productId);
    model.addAttribute("product", product);
    model.addAttribute("media", media.findByProduct_IdOrderBySortOrderAscIdAsc(productId));
    model.addAttribute(
        "colors", product.getVariants().stream().map(v -> v.getColor()).distinct().toList());
    model.addAttribute("pageTitle", "Ảnh & góc nhìn sản phẩm");
    model.addAttribute("currentPage", "products");
    return "admin/products/media";
  }

  @PostMapping
  public String upload(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "") String color,
      @RequestParam String kind,
      @RequestParam("files") List<MultipartFile> files,
      RedirectAttributes flash) {
    try {
      service.upload(productId, color, kind, files);
      flash.addFlashAttribute("successMsg", "Đã cập nhật bộ ảnh sản phẩm.");
    } catch (IllegalArgumentException | IOException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/products/" + productId + "/media";
  }

  @PostMapping("/{id}/delete")
  public String delete(
      @PathVariable Long productId, @PathVariable Long id, RedirectAttributes flash)
      throws IOException {
    try {
      service.delete(productId, id);
      flash.addFlashAttribute("successMsg", "Đã xóa ảnh.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/products/" + productId + "/media";
  }
}
