package iuh.fit.www_phone_ver1.controller.account;

import iuh.fit.www_phone_ver1.service.catalog.WishlistService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/wishlist")
public class WishlistController {
  private final WishlistService wishlist;

  public WishlistController(WishlistService wishlist) {
    this.wishlist = wishlist;
  }

  @GetMapping
  public String list(Principal user, Model model) {
    model.addAttribute("entries", wishlist.list(user.getName()));
    return "account/wishlist";
  }

  @PostMapping("/add")
  public String add(@RequestParam Long productId, Principal user, RedirectAttributes flash) {
    try {
      wishlist.add(user.getName(), productId);
      flash.addFlashAttribute("successMsg", "Đã lưu sản phẩm yêu thích.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/account/wishlist";
  }

  @PostMapping("/remove")
  public String remove(@RequestParam Long productId, Principal user) {
    wishlist.remove(user.getName(), productId);
    return "redirect:/account/wishlist";
  }
}
