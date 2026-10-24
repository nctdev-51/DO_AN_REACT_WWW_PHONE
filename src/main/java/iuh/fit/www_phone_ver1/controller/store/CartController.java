package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {
  private final ProductVariantRepository variants;

  public CartController(ProductVariantRepository variants) {
    this.variants = variants;
  }

  private Cart cart(HttpSession session) {
    synchronized (session) {
      Cart cart = (Cart) session.getAttribute("cart");
      if (cart == null) {
        cart = new Cart();
        session.setAttribute("cart", cart);
      }
      return cart;
    }
  }

  @GetMapping
  public String index(HttpSession session, Model model) {
    model.addAttribute("cart", cart(session));
    return "store/cart/index";
  }

  @PostMapping("/add")
  public String add(
      @RequestParam Long variantId,
      @RequestParam(defaultValue = "1") int quantity,
      @RequestParam(defaultValue = "false") boolean buyNow,
      HttpSession session,
      RedirectAttributes flash) {
    try {
      cart(session)
          .addItem(
              variants
                  .findById(variantId)
                  .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên bản.")),
              quantity);
      if (buyNow) return "redirect:/checkout";
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/cart";
  }

  @PostMapping("/update")
  public String update(
      @RequestParam Long variantId,
      @RequestParam int quantity,
      HttpSession session,
      RedirectAttributes flash) {
    try {
      if (quantity > 0) {
        var variant =
            variants
                .findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Phiên bản không còn tồn tại."));
        if (quantity > variant.getStock())
          throw new IllegalArgumentException("Số lượng vượt quá tồn kho.");
      }
      cart(session).updateQuantity(variantId, quantity);
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/cart";
  }

  @PostMapping("/remove/{id}")
  public String remove(@PathVariable Long id, HttpSession session) {
    cart(session).removeItem(id);
    return "redirect:/cart";
  }

  @PostMapping("/clear")
  public String clear(HttpSession session) {
    cart(session).clear();
    return "redirect:/cart";
  }
}
