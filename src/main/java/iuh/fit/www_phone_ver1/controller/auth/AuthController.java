package iuh.fit.www_phone_ver1.controller.auth;

import iuh.fit.www_phone_ver1.dto.user.UserRegistrationDto;
import iuh.fit.www_phone_ver1.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  public static String safeRedirect(String candidate) {
    if (candidate == null || candidate.isBlank()) return null;
    String trimmed = candidate.trim();
    if (!trimmed.startsWith("/") || trimmed.startsWith("//") || trimmed.startsWith("/\\"))
      return null;
    if (trimmed.contains("\r") || trimmed.contains("\n") || trimmed.contains("://")) return null;
    if (trimmed.equals("/login")
        || trimmed.startsWith("/login?")
        || trimmed.equals("/register")
        || trimmed.startsWith("/register?")) return null;
    return trimmed;
  }

  @GetMapping("/register")
  public String showRegistrationForm(Model model) {
    model.addAttribute("user", new UserRegistrationDto());
    return "auth/register";
  }

  @GetMapping("/login")
  public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
    String safe = safeRedirect(redirect);
    if (safe != null) model.addAttribute("redirectTarget", safe);
    return "auth/login";
  }

  @PostMapping("/register")
  public String registerUserAccount(
      @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
      BindingResult result,
      Model model) {
    if (result.hasErrors()) {
      return "auth/register";
    }

    try {
      userService.registerUser(registrationDto);
    } catch (IllegalArgumentException e) {
      result.rejectValue("email", "user.email", e.getMessage());
      return "auth/register";
    }

    return "redirect:/register?success";
  }
}
