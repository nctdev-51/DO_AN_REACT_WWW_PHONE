package iuh.fit.www_phone_ver1.controller.account;

import iuh.fit.www_phone_ver1.dto.user.*;
import iuh.fit.www_phone_ver1.repository.AddressRepository;
import iuh.fit.www_phone_ver1.service.user.AccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {
  private final AccountService accounts;
  private final AddressRepository addresses;

  public AccountController(AccountService accounts, AddressRepository addresses) {
    this.accounts = accounts;
    this.addresses = addresses;
  }

  @GetMapping
  public String profile(Principal principal, Model model) {
    var user = accounts.user(principal.getName());
    ProfileForm form = new ProfileForm();
    form.setFullName(user.getFullName());
    form.setPhone(user.getPhone());
    model.addAttribute("profileForm", form);
    model.addAttribute("email", user.getEmail());
    return "account/profile";
  }

  @PostMapping
  public String profile(
      @Valid @ModelAttribute("profileForm") ProfileForm form,
      BindingResult errors,
      Principal principal,
      Model model,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      model.addAttribute("email", principal.getName());
      return "account/profile";
    }
    accounts.updateProfile(principal.getName(), form);
    flash.addFlashAttribute("successMsg", "Đã cập nhật tài khoản.");
    return "redirect:/account";
  }

  @GetMapping("/password")
  public String password(Model model) {
    model.addAttribute("passwordForm", new PasswordForm());
    return "account/password";
  }

  @PostMapping("/password")
  public String password(
      @Valid @ModelAttribute("passwordForm") PasswordForm form,
      BindingResult errors,
      Principal principal,
      RedirectAttributes flash) {
    if (errors.hasErrors()) return "account/password";
    try {
      accounts.changePassword(principal.getName(), form);
      flash.addFlashAttribute("successMsg", "Đã đổi mật khẩu.");
    } catch (IllegalArgumentException error) {
      errors.reject("password", error.getMessage());
      return "account/password";
    }
    return "redirect:/account";
  }

  @GetMapping("/addresses")
  public String addresses(Principal principal, Model model) {
    model.addAttribute(
        "addresses",
        addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(principal.getName()));
    return "account/addresses";
  }

  @PostMapping("/addresses")
  public String saveAddress(
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute AddressForm form,
      BindingResult errors,
      Principal principal,
      RedirectAttributes flash) {
    if (errors.hasErrors())
      flash.addFlashAttribute(
          "errorMsg", "Kiểm tra tên người nhận, địa chỉ và số điện thoại 10 chữ số.");
    else
      try {
        accounts.saveAddress(principal.getName(), id, form);
        flash.addFlashAttribute("successMsg", "Đã lưu địa chỉ.");
      } catch (IllegalArgumentException error) {
        flash.addFlashAttribute("errorMsg", error.getMessage());
      }
    if (flash.getFlashAttributes().containsKey("errorMsg")) {
      flash.addFlashAttribute("failedAddressForm", form);
      flash.addFlashAttribute("failedAddressId", id);
    }
    return "redirect:/account/addresses";
  }

  @PostMapping("/addresses/{id}/delete")
  public String deleteAddress(
      @PathVariable Long id, Principal principal, RedirectAttributes flash) {
    try {
      accounts.deleteAddress(principal.getName(), id);
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/account/addresses";
  }
}
