package iuh.fit.www_phone_ver1.controller;

import iuh.fit.www_phone_ver1.repository.CategoryRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final int PAGE_SIZE = 12;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String brand,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedBrand = brand == null || brand.isBlank() ? null : brand.trim();
        int pageNumber = Math.max(page, 0);

        model.addAttribute("products", productRepository.search(
                normalizedKeyword, normalizedBrand, categoryId, minPrice, maxPrice,
                PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"))));
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        model.addAttribute("brands", productRepository.findAvailableBrands());
        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("brand", normalizedBrand);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "products";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        return productRepository.findById(id)
                .filter(product -> product.getStatus() == 1)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product-detail";
                })
                .orElse("redirect:/products");
    }
}
