package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm;
import iuh.fit.www_phone_ver1.entity.CampaignBanner;
import iuh.fit.www_phone_ver1.repository.*;
import java.io.IOException;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CampaignBannerService {
  private final CampaignBannerRepository banners;
  private final ProductRepository products;
  private final ProductImageStorageService images;

  public CampaignBannerService(
      CampaignBannerRepository banners,
      ProductRepository products,
      ProductImageStorageService images) {
    this.banners = banners;
    this.products = products;
    this.images = images;
  }

  @Transactional
  public void save(Long id, CampaignBannerForm form, MultipartFile upload) throws IOException {
    if (form.getStartsAt() == null
        || form.getEndsAt() == null
        || !form.getEndsAt().isAfter(form.getStartsAt()))
      throw new IllegalArgumentException("Lịch kết thúc phải sau lịch bắt đầu.");
    var banner =
        id == null
            ? new CampaignBanner()
            : banners
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner."));
    if (id != null && (form.getVersion() == null || form.getVersion() != banner.getVersion()))
      throw new IllegalArgumentException("Banner vừa thay đổi. Hãy tải lại.");
    var product =
        form.getProductId() == null
            ? null
            : products
                .findById(form.getProductId())
                .filter(p -> p.getStatus() == 1)
                .orElseThrow(
                    () -> new IllegalArgumentException("Sản phẩm chưa tồn tại hoặc đã ngừng bán."));
    if ("PRODUCT".equals(form.getTarget()) && product == null)
      throw new IllegalArgumentException("Chọn sản phẩm cho đích đến của banner.");
    String previous = banner.getImage(),
        next = upload == null || upload.isEmpty() ? previous : images.store(upload);
    cleanup(next, previous);
    BeanUtils.copyProperties(form, banner, "version", "productId");
    banner.setProduct(product);
    banner.setImage(next);
    banners.saveAndFlush(banner);
  }

  @Transactional
  public void delete(Long id, long version) {
    var b =
        banners
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner."));
    if (b.getVersion() != version)
      throw new IllegalArgumentException("Banner vừa thay đổi. Hãy tải lại.");
    cleanup(null, b.getImage());
    banners.delete(b);
    banners.flush();
  }

  private void cleanup(String next, String previous) {
    if (Objects.equals(next, previous)) return;
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCompletion(int status) {
            String path = status == STATUS_COMMITTED ? previous : next;
            try {
              images.delete(path);
            } catch (IOException ex) {
              org.slf4j.LoggerFactory.getLogger(CampaignBannerService.class)
                  .warn("Không dọn được ảnh banner");
            }
          }
        });
  }
}
