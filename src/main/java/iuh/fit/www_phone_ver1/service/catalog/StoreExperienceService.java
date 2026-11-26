package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.entity.StoreExperience;
import iuh.fit.www_phone_ver1.repository.*;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreExperienceService {
  private final StoreExperienceRepository settings;
  private final ProductRepository products;

  public StoreExperienceService(StoreExperienceRepository settings, ProductRepository products) {
    this.settings = settings;
    this.products = products;
  }

  @Transactional(readOnly = true)
  public StoreExperience get() {
    return settings.findById(1L).orElseGet(StoreExperience::new);
  }

  @Transactional
  public void save(StoreExperience form) {
    if (form.getDeliveryMaxDays() < form.getDeliveryMinDays())
      throw new IllegalArgumentException("Thời gian giao tối đa phải từ thời gian tối thiểu.");
    for (String sku :
        List.of(
                Objects.toString(form.getHeroSku(), ""),
                Objects.toString(form.getSecondHeroSku(), ""),
                Objects.toString(form.getFeaturedSkus(), ""))
            .stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList()) {
      if (products.findBySku(sku).filter(p -> p.getStatus() == 1).isEmpty())
        throw new IllegalArgumentException("SKU chưa tồn tại hoặc đã ngừng bán: " + sku);
    }
    var current = settings.findById(1L).orElseGet(StoreExperience::new);
    if (current.getVersion() != form.getVersion())
      throw new IllegalArgumentException("Nội dung vừa thay đổi. Hãy tải lại trang.");
    BeanUtils.copyProperties(form, current, "id", "version");
    settings.saveAndFlush(current);
  }
}
