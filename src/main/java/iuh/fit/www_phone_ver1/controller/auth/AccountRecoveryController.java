package iuh.fit.www_phone_ver1.controller.auth;

import iuh.fit.www_phone_ver1.entity.AccountToken.Purpose;
import iuh.fit.www_phone_ver1.service.user.AccountTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountRecoveryController {
  private final AccountTokenService tokens;

  public AccountRecoveryController(AccountTokenService tokens) {
    this.tokens = tokens;
  }

  @GetMapping({"/forgot-password", "/resend-verification"})
  public String requestForm(jakarta.servlet.http.HttpServletRequest request, Model model) {
    model.addAttribute("verification", request.getServletPath().equals("/resend-verification"));
    return "auth/request-link";
  }

  @PostMapping("/forgot-password")
  public String forgot(@RequestParam(defaultValue = "") String email) {
    tokens.request(email, Purpose.RESET_PASSWORD);
    return "redirect:/forgot-password?sent";
  }

  @PostMapping("/resend-verification")
  public String resend(@RequestParam(defaultValue = "") String email) {
    tokens.request(email, Purpose.VERIFY_EMAIL);
    return "redirect:/resend-verification?sent";
  }

  @GetMapping("/verify-email")
  public String verifyForm(
      @RequestParam(defaultValue = "") String token, Model model, HttpServletResponse response) {
    return tokenPage(token, Purpose.VERIFY_EMAIL, model, response);
  }

  @PostMapping("/verify-email")
  public String verify(
      @RequestParam(defaultValue = "") String token, Model model, HttpServletResponse response) {
    protect(response);
    try {
      tokens.verify(token);
      return "redirect:/login?verified";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      return "auth/token";
    }
  }

  @GetMapping("/reset-password")
  public String resetForm(
      @RequestParam(defaultValue = "") String token, Model model, HttpServletResponse response) {
    return tokenPage(token, Purpose.RESET_PASSWORD, model, response);
  }

  @PostMapping("/reset-password")
  public String reset(
      @RequestParam(defaultValue = "") String token,
      @RequestParam(defaultValue = "") String password,
      @RequestParam(defaultValue = "") String confirmation,
      Model model,
      HttpServletResponse response) {
    protect(response);
    try {
      tokens.resetPassword(token, password, confirmation);
      return "redirect:/login?reset";
    } catch (IllegalArgumentException e) {
      tokenPage(token, Purpose.RESET_PASSWORD, model, response);
      model.addAttribute("error", e.getMessage());
      return "auth/token";
    }
  }

  private String tokenPage(
      String token, Purpose purpose, Model model, HttpServletResponse response) {
    protect(response);
    boolean valid = tokens.isValid(token, purpose);
    model.addAttribute("valid", valid);
    model.addAttribute("verification", purpose == Purpose.VERIFY_EMAIL);
    if (valid) model.addAttribute("token", token);
    else
      model.addAttribute(
          "error",
          "Liên kết không hợp lệ, đã được sử dụng hoặc đã hết hạn. Vui lòng yêu cầu liên kết mới.");
    return "auth/token";
  }

  private void protect(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Referrer-Policy", "no-referrer");
  }
}
