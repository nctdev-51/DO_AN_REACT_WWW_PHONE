package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.service.order.AddressDirectory;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
public class AddressDirectoryController {
  private final AddressDirectory directory;

  public AddressDirectoryController(AddressDirectory directory) {
    this.directory = directory;
  }

  @GetMapping("/provinces")
  public List<Map<String, String>> provinces() {
    return directory.provinces().stream()
        .map(p -> Map.of("code", p.code(), "name", p.name()))
        .toList();
  }

  @GetMapping("/wards")
  public List<AddressDirectory.Ward> wards(@RequestParam String provinceCode) {
    return directory.wards(provinceCode);
  }
}
