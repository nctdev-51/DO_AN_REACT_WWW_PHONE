package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.entity.*;
import org.springframework.stereotype.Service;

@Service
public class OrderHistoryService {
  public void append(Order order, String actor, String note) {
    OrderEvent event = new OrderEvent();
    event.setOrder(order);
    event.setActor(actor);
    event.setNote(note.length() > 500 ? note.substring(0, 500) : note);
    event.setOrderStatus(order.getStatus());
    event.setPaymentStatus(order.getPaymentStatus());
    order.getEvents().add(event);
  }
}
