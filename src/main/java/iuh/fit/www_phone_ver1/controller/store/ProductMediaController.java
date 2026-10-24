package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.dto.catalog.ProductGallery;
import iuh.fit.www_phone_ver1.service.catalog.ProductMediaService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductMediaController {
  private final ProductMediaService service;

  public ProductMediaController(ProductMediaService service) {
    this.service = service;
  }

  @GetMapping("/products/{id}/media")
  public ResponseEntity<ProductGallery> media(
      @PathVariable Long id, @RequestParam(required = false) String color) {
    try {
      return ResponseEntity.ok()
          .cacheControl(CacheControl.noCache())
          .body(service.gallery(id, color));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
