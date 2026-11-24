package iuh.fit.www_phone_ver1.service.catalog;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  @Value("${cloudinary.cloud-name:}")
  private String cloudName;

  @Value("${cloudinary.api-key:}")
  private String apiKey;

  @Value("${cloudinary.api-secret:}")
  private String apiSecret;

  /**
   * Kiểm tra xem hệ thống đã cấu hình key Cloudinary thật hay chưa
   */
  public boolean isConfigured() {
    return cloudName != null
        && !cloudName.isBlank()
        && !"your_cloud_name".equalsIgnoreCase(cloudName)
        && apiKey != null
        && !apiKey.isBlank()
        && !"your_api_key".equalsIgnoreCase(apiKey)
        && apiSecret != null
        && !apiSecret.isBlank()
        && !"your_api_secret".equalsIgnoreCase(apiSecret);
  }

  /**
   * Upload ảnh mới từ form MultipartFile lên Cloudinary
   *
   * @param file File ảnh người dùng chọn từ form
   * @param folder Thư mục lưu trên Cloudinary (vd: "ptv_mobile/banners", "ptv_mobile/products")
   * @return Đường dẫn HTTPS an toàn (secure_url)
   */
  public String uploadImage(MultipartFile file, String folder) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    if (!isConfigured()) {
      throw new IllegalStateException(
          "Chưa cấu hình CLOUDINARY_CLOUD_NAME / API_KEY / API_SECRET trong file .env!");
    }
    try {
      Map<?, ?> uploadResult =
          cloudinary
              .uploader()
              .upload(
                  file.getBytes(),
                  ObjectUtils.asMap(
                      "folder", folder,
                      "resource_type", "image"));
      return uploadResult.get("secure_url").toString();
    } catch (IOException e) {
      log.error("Lỗi upload ảnh lên Cloudinary: {}", e.getMessage());
      throw new RuntimeException("Không thể tải ảnh lên Cloudinary. Vui lòng kiểm tra kết nối mạng!");
    }
  }

  /**
   * Upload file ảnh có sẵn trên ổ đĩa local lên Cloudinary (dùng cho Migration ảnh cũ)
   */
  public String uploadLocalFile(File localFile, String folder) {
    if (localFile == null || !localFile.exists() || !isConfigured()) {
      return null;
    }
    try {
      Map<?, ?> uploadResult =
          cloudinary
              .uploader()
              .upload(
                  localFile,
                  ObjectUtils.asMap(
                      "folder", folder,
                      "resource_type", "image"));
      return uploadResult.get("secure_url").toString();
    } catch (Exception e) {
      log.warn("Không thể migrate file ({}): {}", localFile.getName(), e.getMessage());
      return null;
    }
  }

  /**
   * Xóa ảnh trên Cloudinary theo URL đầy đủ (tùy chọn khi Admin xóa bản ghi)
   */
  public void deleteImageByUrl(String imageUrl) {
    if (imageUrl == null || !imageUrl.contains("res.cloudinary.com") || !isConfigured()) {
      return;
    }
    try {
      // Trích xuất public_id từ URL (ví dụ: ptv_mobile/banners/abc123)
      int uploadIndex = imageUrl.indexOf("/upload/");
      if (uploadIndex == -1) return;
      String pathAfterUpload = imageUrl.substring(uploadIndex + 8);
      // Bỏ qua phần version v123456789/
      if (pathAfterUpload.startsWith("v") && pathAfterUpload.contains("/")) {
        pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf("/") + 1);
      }
      // Bỏ đuôi mở rộng (.jpg, .png, .webp...)
      int dotIndex = pathAfterUpload.lastIndexOf(".");
      String publicId = (dotIndex != -1) ? pathAfterUpload.substring(0, dotIndex) : pathAfterUpload;

      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    } catch (Exception e) {
      log.warn("Không thể xóa ảnh cũ trên Cloudinary ({}): {}", imageUrl, e.getMessage());
    }
  }
}
