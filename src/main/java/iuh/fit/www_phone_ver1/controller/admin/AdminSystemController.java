package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.entity.CampaignBanner;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductMedia;
import iuh.fit.www_phone_ver1.repository.CampaignBannerRepository;
import iuh.fit.www_phone_ver1.repository.ProductMediaRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.catalog.CloudinaryService;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

  private final CloudinaryService cloudinaryService;
  private final CampaignBannerRepository bannerRepository;
  private final ProductRepository productRepository;
  private final ProductMediaRepository productMediaRepository;

  @PostMapping("/migrate-cloudinary")
  public String migrateOldImagesToCloudinary(
      @org.springframework.web.bind.annotation.RequestParam(value = "redirect", defaultValue = "/admin/banners") String redirectUrl,
      RedirectAttributes redirectAttributes) {
    if (!cloudinaryService.isConfigured()) {
      redirectAttributes.addFlashAttribute(
          "errorMsg", "Vui lòng cấu hình key Cloudinary trong file .env trước khi thực hiện đồng bộ!");
      return "redirect:" + redirectUrl;
    }

    int bannerMigrated = 0;
    int productMigrated = 0;
    int mediaMigrated = 0;

    // 1. Migrate ảnh bảng Banner
    for (CampaignBanner banner : bannerRepository.findAll()) {
      String url = banner.getImage();
      if (url != null && !url.isBlank() && !url.startsWith("http")) {
        File localFile = resolveLocalFile(url);
        if (localFile != null && localFile.exists()) {
          String cloudUrl = cloudinaryService.uploadLocalFile(localFile, "ptv_mobile/banners");
          if (cloudUrl != null) {
            banner.setImage(cloudUrl);
            bannerRepository.save(banner);
            bannerMigrated++;
          }
        }
      }
    }

    // 2. Migrate ảnh bảng Product
    for (Product product : productRepository.findAll()) {
      String url = product.getImage();
      if (url != null && !url.isBlank() && !url.startsWith("http")) {
        File localFile = resolveLocalFile(url);
        if (localFile != null && localFile.exists()) {
          String cloudUrl = cloudinaryService.uploadLocalFile(localFile, "ptv_mobile/products");
          if (cloudUrl != null) {
            product.setImage(cloudUrl);
            productRepository.save(product);
            productMigrated++;
          }
        }
      }
    }

    // 3. Migrate ảnh bảng ProductMedia
    for (ProductMedia media : productMediaRepository.findAll()) {
      String url = media.getPath();
      if (url != null && !url.isBlank() && !url.startsWith("http")) {
        File localFile = resolveLocalFile(url);
        if (localFile != null && localFile.exists()) {
          String cloudUrl = cloudinaryService.uploadLocalFile(localFile, "ptv_mobile/products");
          if (cloudUrl != null) {
            media.setPath(cloudUrl);
            productMediaRepository.save(media);
            mediaMigrated++;
          }
        }
      }
    }

    int total = bannerMigrated + productMigrated + mediaMigrated;
    redirectAttributes.addFlashAttribute(
        "successMsg",
        "Đã đồng bộ thành công " + total + " ảnh lên Cloudinary ("
            + bannerMigrated + " banner, "
            + productMigrated + " ảnh sản phẩm chính, "
            + mediaMigrated + " ảnh chi tiết sản phẩm)!");
    return "redirect:" + redirectUrl;
  }

  private File resolveLocalFile(String relativeUrl) {
    if (relativeUrl == null || relativeUrl.isBlank()) return null;
    String cleanPath = relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl;

    // 1. Thư mục uploads gốc (ví dụ uploads/products/xyz.jpg)
    File uploadFile = new File(cleanPath);
    if (uploadFile.exists()) return uploadFile;

    // 2. Thư mục static resources (ví dụ images/products/xyz.png -> src/main/resources/static/images/products/xyz.png)
    File staticFile = new File("src/main/resources/static/" + cleanPath);
    if (staticFile.exists()) return staticFile;

    // 3. Thư mục webapp
    File webappFile = new File("src/main/webapp/" + cleanPath);
    if (webappFile.exists()) return webappFile;

    return null;
  }
}
