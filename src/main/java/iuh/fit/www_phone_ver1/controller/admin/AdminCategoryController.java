package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.catalog.CategoryForm;
import iuh.fit.www_phone_ver1.repository.CategoryRepository;
import iuh.fit.www_phone_ver1.service.catalog.CatalogService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
  private final CategoryRepository categories;
  private final CatalogService catalog;

  public AdminCategoryController(CategoryRepository categories, CatalogService catalog) {
    this.categories = categories;
    this.catalog = catalog;
  }

  @ModelAttribute
  public void navigation(Model model) {
    model.addAttribute("currentPage", "categories");
    model.addAttribute("pageTitle", "Quản lý danh mục");
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("categories", categories.findAll(Sort.by("name")));
    return "admin/categories/list";
  }

  @GetMapping("/new")
  public String create(Model model) {
    model.addAttribute("categoryForm", new CategoryForm());
    return "admin/categories/form";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    CategoryForm form = new CategoryForm();
    BeanUtils.copyProperties(categories.findById(id).orElseThrow(), form);
    model.addAttribute("categoryId", id);
    model.addAttribute("categoryForm", form);
    return "admin/categories/form";
  }

  @PostMapping("/save")
  public String save(
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute("categoryForm") CategoryForm form,
      BindingResult errors,
      Model model,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      model.addAttribute("categoryId", id);
      return "admin/categories/form";
    }
    catalog.saveCategory(id, form);
    flash.addFlashAttribute("successMsg", "Đã lưu danh mục.");
    return "redirect:/admin/categories";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes flash) {
    try {
      catalog.deleteCategory(id);
      flash.addFlashAttribute("successMsg", "Đã xóa danh mục.");
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/admin/categories";
  }
}
