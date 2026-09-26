package iuh.fit.www_phone_ver1.controller;

import iuh.fit.www_phone_ver1.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ProductRepository productRepository;

    public HomeController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("chuoi", "Chào mừng đến với Cửa hàng Điện thoại");
        model.addAttribute("featuredProducts", productRepository.findByStatus(
                1, PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        return "home";
    }
}
