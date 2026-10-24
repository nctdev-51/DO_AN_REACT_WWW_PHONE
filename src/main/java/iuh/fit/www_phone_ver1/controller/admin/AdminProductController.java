package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.catalog.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.CatalogService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
  private final CatalogService catalog;
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ProductVariantRepository variants;

  public AdminProductController(
      CatalogService catalog,
      ProductRepository products,
      CategoryRepository categories,
      ProductVariantRepository variants) {
    this.catalog = catalog;
    this.products = products;
    this.categories = categories;
    this.variants = variants;
  }

  @ModelAttribute
  public void navigation(Model model) {
    model.addAttribute("currentPage", "products");
    model.addAttribute("pageTitle", "Quản lý sản phẩm");
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("products", products.findAll(Sort.by(Sort.Direction.DESC, "id")));
    return "admin/products/list";
  }

  @GetMapping("/new")
  public String create(Model model) {
    model.addAttribute("productForm", new ProductForm());
    options(model, null);
    return "admin/products/form";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    var product = catalog.product(id);
    ProductForm form = new ProductForm();
    BeanUtils.copyProperties(product, form);
    form.setCategoryId(product.getCategory() == null ? null : product.getCategory().getId());
    model.addAttribute("productForm", form);
    options(model, id);
    return "admin/products/form";
  }

  private void options(Model model, Long id) {
    model.addAttribute("productId", id);
    model.addAttribute("categories", categories.findAll(Sort.by("name")));
    if (id != null) model.addAttribute("image", catalog.product(id).getImage());
  }

  @PostMapping("/save")
  public String save(
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute("productForm") ProductForm form,
      BindingResult errors,
      @RequestParam(required = false) MultipartFile image,
      Model model,
      RedirectAttributes flash)
      throws IOException {
    if (!errors.hasErrors())
      try {
        catalog.saveProduct(id, form, image);
        flash.addFlashAttribute("successMsg", "Đã lưu sản phẩm.");
        return "redirect:/admin/products";
      } catch (IllegalArgumentException error) {
        errors.reject("product", error.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException error) {
        errors.reject("sku", "Mã SKU đã được sử dụng.");
      }
    options(model, id);
    return "admin/products/form";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes flash) throws IOException {
    flash.addFlashAttribute(
        "successMsg",
        catalog.deleteProduct(id)
            ? "Đã xóa sản phẩm."
            : "Sản phẩm đã có đơn hàng, được chuyển sang ngừng kinh doanh.");
    return "redirect:/admin/products";
  }

  @GetMapping("/{productId}/variants")
  public String variants(@PathVariable Long productId, Model model) {
    model.addAttribute("product", catalog.product(productId));
    model.addAttribute("variants", variants.findByProduct_IdOrderByPriceAscIdAsc(productId));
    return "admin/products/variants";
  }

  @PostMapping("/{productId}/variants")
  public String saveVariant(
      @PathVariable Long productId,
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute VariantForm form,
      BindingResult errors,
      RedirectAttributes flash) {
    if (errors.hasErrors())
      flash.addFlashAttribute("errorMsg", "Kiểm tra SKU, màu sắc, dung lượng, giá và tồn kho.");
    else
      try {
        catalog.saveVariant(productId, id, form);
        flash.addFlashAttribute("successMsg", "Đã lưu phiên bản.");
      } catch (IllegalArgumentException error) {
        flash.addFlashAttribute("errorMsg", error.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException error) {
        flash.addFlashAttribute("errorMsg", "SKU phiên bản đã tồn tại.");
      }
    return "redirect:/admin/products/" + productId + "/variants";
  }

  @PostMapping("/{productId}/variants/{id}/disable")
  public String disable(@PathVariable Long productId, @PathVariable Long id) {
    catalog.disableVariant(productId, id);
    return "redirect:/admin/products/" + productId + "/variants";
  }
}
