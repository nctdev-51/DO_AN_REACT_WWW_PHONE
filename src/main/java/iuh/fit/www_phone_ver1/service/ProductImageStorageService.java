package iuh.fit.www_phone_ver1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ProductImageStorageService {
    private final Path uploadDirectory;

    public ProductImageStorageService(@Value("${app.upload.dir:uploads/products}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) throws IOException {
        if (image.isEmpty() || image.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh phải có dung lượng không quá 5 MB");
        }

        byte[] signature;
        try (var input = image.getInputStream()) {
            signature = input.readNBytes(8);
        }
        String extension;
        if (signature.length >= 8
                && (signature[0] & 0xff) == 0x89 && signature[1] == 0x50
                && signature[2] == 0x4e && signature[3] == 0x47
                && signature[4] == 0x0d && signature[5] == 0x0a
                && signature[6] == 0x1a && signature[7] == 0x0a) {
            extension = ".png";
        } else if (signature.length >= 3
                && (signature[0] & 0xff) == 0xff
                && (signature[1] & 0xff) == 0xd8
                && (signature[2] & 0xff) == 0xff) {
            extension = ".jpg";
        } else {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh PNG hoặc JPEG");
        }

        Files.createDirectories(uploadDirectory);
        String filename = UUID.randomUUID() + extension;
        try (var input = image.getInputStream()) {
            Files.copy(input, uploadDirectory.resolve(filename));
        }
        return "/uploads/products/" + filename;
    }

    public void delete(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/products/")) {
            return;
        }
        String filename = imageUrl.substring("/uploads/products/".length());
        if (filename.contains("/") || filename.contains("\\") || filename.isBlank()) {
            return;
        }
        Path imagePath = uploadDirectory.resolve(filename).normalize();
        if (uploadDirectory.equals(imagePath.getParent())) {
            Files.deleteIfExists(imagePath);
        }
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}
