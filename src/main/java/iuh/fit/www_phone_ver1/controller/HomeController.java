package iuh.fit.www_phone_ver1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("chuoi", "Chào mừng đến với Cửa hàng Điện thoại");
        return "home"; // Trỏ tới /WEB-INF/views/home.jsp
    }
}
