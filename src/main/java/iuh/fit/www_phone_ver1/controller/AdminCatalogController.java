package iuh.fit.www_phone_ver1.controller;

import iuh.fit.www_phone_ver1.dto.CategoryForm;
import iuh.fit.www_phone_ver1.dto.ProductForm;
import iuh.fit.www_phone_ver1.entity.Category;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.CategoryRepository;
import iuh.fit.www_phone_ver1.repository.OrderDetailRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.ProductImageStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminCatalogController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductImageStorageService imageStorageService;

    public AdminCatalogController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  OrderDetailRepository orderDetailRepository,
                                  ProductImageStorageService imageStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public String dashboard() {
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        model.addAttribute("image", null);
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElseThrow();
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setBrand(product.getBrand());
        form.setPrice(product.getPrice());
        form.setDescription(product.getDescription());
        form.setCategoryId(product.getCategory() == null ? null : product.getCategory().getId());
        form.setStatus(product.getStatus());
        model.addAttribute("productId", id);
        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        model.addAttribute("image", product.getImage());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @Valid @ModelAttribute("productForm") ProductForm form,
                              BindingResult bindingResult,
                              @RequestParam(required = false) MultipartFile image,
                              Model model,
                              RedirectAttributes redirectAttributes) throws IOException {
        Product product = id == null ? new Product() : productRepository.findById(id).orElseThrow();
        Category category = form.getCategoryId() == null ? null
                : categoryRepository.findById(form.getCategoryId()).orElse(null);
        if (form.getCategoryId() != null && category == null) {
            bindingResult.rejectValue("categoryId", "category.invalid", "Danh mục không tồn tại");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
            model.addAttribute("productId", id);
            model.addAttribute("image", product.getImage());
            return "admin/product-form";
        }

        product.setName(form.getName().trim());
        product.setBrand(form.getBrand().trim());
        product.setPrice(form.getPrice());
        product.setDescription(form.getDescription());
        product.setCategory(category);
        product.setStatus(form.getStatus());
        String previousImage = product.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                product.setImage(imageStorageService.store(image));
            } catch (IllegalArgumentException exception) {
                bindingResult.reject("image.invalid", exception.getMessage());
                model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
                model.addAttribute("productId", id);
                model.addAttribute("image", previousImage);
                return "admin/product-form";
            }
        }

        productRepository.save(product);
        if (!java.util.Objects.equals(previousImage, product.getImage())) {
            imageStorageService.delete(previousImage);
        }
        redirectAttributes.addFlashAttribute("message", "Đã lưu sản phẩm.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        Product product = productRepository.findById(id).orElseThrow();
        if (orderDetailRepository.existsByProduct_Id(id)) {
            product.setStatus(0);
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("message",
                    "Sản phẩm đã có trong đơn hàng nên được chuyển sang ngừng kinh doanh.");
        } else {
            productRepository.delete(product);
            imageStorageService.delete(product.getImage());
            redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm.");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/category-form";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElseThrow();
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        model.addAttribute("categoryId", id);
        model.addAttribute("categoryForm", form);
        return "admin/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @Valid @ModelAttribute("categoryForm") CategoryForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/category-form";
        }
        Category category = id == null ? new Category() : categoryRepository.findById(id).orElseThrow();
        category.setName(form.getName().trim());
        category.setDescription(form.getDescription());
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("message", "Đã lưu danh mục.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productRepository.countByCategoryId(id) > 0) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục đang có sản phẩm.");
        } else {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa danh mục.");
        }
        return "redirect:/admin/categories";
    }
}
