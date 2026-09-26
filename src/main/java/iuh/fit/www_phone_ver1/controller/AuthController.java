package iuh.fit.www_phone_ver1.controller;

import iuh.fit.www_phone_ver1.dto.UserRegistrationDto;
import iuh.fit.www_phone_ver1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "user.email", e.getMessage());
            return "register";
        }

        return "redirect:/register?success";
    }
}
