package iuh.fit.www_phone_ver1.service;

import iuh.fit.www_phone_ver1.dto.Cart;
import iuh.fit.www_phone_ver1.dto.CartItem;
import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MailService mailService;

    @Transactional
    public Order placeOrder(Cart cart, User user, String shippingAddress, String phone, String customerEmail) {
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status("Chờ xử lý")
                .shippingAddress(shippingAddress)
                .phone(phone)
                .totalAmount(cart.getTotalAmount())
                .build();

        for (CartItem item : cart.getItemList()) {
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .price(item.getProduct().getPrice())
                    .build();

            order.getOrderDetails().add(detail);
        }

        // Nhờ CascadeType.ALL trên Order, Hibernate sẽ tự động lưu cả bảng orders và order_details
        Order savedOrder = orderRepository.save(order);

        String emailToSend = (user != null && user.getEmail() != null) ? user.getEmail() : customerEmail;
        if (emailToSend != null && !emailToSend.trim().isEmpty()) {
            mailService.sendOrderConfirmation(emailToSend, savedOrder);
        }

        return savedOrder;
    }
}