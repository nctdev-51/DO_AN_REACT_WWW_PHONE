package iuh.fit.www_phone_ver1.controller;

import iuh.fit.www_phone_ver1.dto.Cart;
import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String viewCheckout(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItemList().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);

        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("user", currentUser);

        return "checkout";
    }

    @PostMapping("/place-order")
    public String processOrder(@RequestParam("shippingAddress") String address,
                               @RequestParam("phone") String phone,
                               @RequestParam(value = "email", required = false) String email,
                               HttpSession session,
                               Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItemList().isEmpty()) {
            return "redirect:/cart";
        }

        User currentUser = (User) session.getAttribute("currentUser");

        Order order = orderService.placeOrder(cart, currentUser, address, phone, email);

        // Xóa giỏ hàng khỏi session sau khi đặt hàng thành công
        session.removeAttribute("cart");

        model.addAttribute("order", order);
        return "order-success";
    }
}