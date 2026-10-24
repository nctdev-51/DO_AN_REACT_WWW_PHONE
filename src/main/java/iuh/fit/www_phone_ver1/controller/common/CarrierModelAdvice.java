package iuh.fit.www_phone_ver1.controller.common;

import iuh.fit.www_phone_ver1.service.shipping.GhnClient;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(
    assignableTypes = iuh.fit.www_phone_ver1.controller.admin.AdminOrderController.class)
public class CarrierModelAdvice {
  private final GhnClient ghn;

  public CarrierModelAdvice(GhnClient ghn) {
    this.ghn = ghn;
  }

  @ModelAttribute("ghnAvailable")
  public boolean available() {
    return ghn.isAvailable();
  }

  @ModelAttribute("ghnEnvironment")
  public String environment() {
    return ghn.environment();
  }
}
