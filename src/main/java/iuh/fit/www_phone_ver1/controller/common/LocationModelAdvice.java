package iuh.fit.www_phone_ver1.controller.common;

import iuh.fit.www_phone_ver1.service.order.AddressDirectory;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(
    assignableTypes = {
      iuh.fit.www_phone_ver1.controller.account.AccountController.class,
      iuh.fit.www_phone_ver1.controller.store.CheckoutController.class
    })
public class LocationModelAdvice {
  private final AddressDirectory directory;

  public LocationModelAdvice(AddressDirectory directory) {
    this.directory = directory;
  }

  @ModelAttribute("locationProvinces")
  public List<AddressDirectory.Province> provinces() {
    return directory.provinces();
  }
}
