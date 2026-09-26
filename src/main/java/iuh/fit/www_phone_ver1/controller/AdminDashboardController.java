package iuh.fit.www_phone_ver1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard"; // Trỏ tới /WEB-INF/views/admin/dashboard.jsp
    }
}
