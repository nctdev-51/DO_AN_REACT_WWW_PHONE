package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.dto.catalog.ProductGallery;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductMediaService {
  private final ProductRepository products;
  private final ProductMediaRepository media;
  private final ProductImageStorageService storage;

  public ProductMediaService(
      ProductRepository products,
      ProductMediaRepository media,
      ProductImageStorageService storage) {
    this.products = products;
    this.media = media;
    this.storage = storage;
  }

  @Transactional(readOnly = true)
  public ProductGallery gallery(Long productId, String color) {
    Product product =
        products
            .findById(productId)
            .filter(p -> p.getStatus() == 1)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."));
    var entries = media.findByProduct_IdOrderBySortOrderAscIdAsc(productId);
    var photos = new ArrayList<ProductGallery.Photo>();
    entries.stream()
        .filter(m -> "PHOTO".equals(m.getKind()) && color != null && color.equals(m.getColor()))
        .forEach(
            m ->
                photos.add(
                    new ProductGallery.Photo(
                        m.getPath(), m.getCaption() == null ? color : m.getCaption(), true)));
    entries.stream()
        .filter(m -> "PHOTO".equals(m.getKind()) && m.getColor() == null)
        .forEach(
            m ->
                photos.add(
                    new ProductGallery.Photo(m.getPath(), "Ảnh giới thiệu sản phẩm", false)));
    if (photos.isEmpty() && product.getImage() != null)
      photos.add(new ProductGallery.Photo(product.getImage(), "Ảnh giới thiệu sản phẩm", false));
    // Never use another color's spin sequence as a fallback.
    var frames =
        entries.stream()
            .filter(
                m ->
                    "SPIN".equals(m.getKind())
                        && (Objects.equals(color, m.getColor()) || m.getColor() == null))
            .toList();
    boolean exact = frames.stream().anyMatch(m -> Objects.equals(color, m.getColor()));
    List<String> sequence =
        frames.stream()
            .filter(m -> !exact || Objects.equals(color, m.getColor()))
            .map(ProductMedia::getPath)
            .toList();
    if (sequence.size() < 12) sequence = List.of();
    var model =
        entries.stream()
            .filter(
                m ->
                    "MODEL".equals(m.getKind())
                        && m.getPath().matches("https://sketchfab\\.com/models/[a-f0-9]{32}/embed"))
            .findFirst()
            .orElse(null);
    return new ProductGallery(
        photos,
        sequence,
        model == null ? null : model.getPath(),
        model == null ? null : model.getCaption());
  }

  @Transactional(rollbackFor = IOException.class)
  public void upload(Long productId, String color, String kind, List<MultipartFile> files)
      throws IOException {
    Product product = products.findById(productId).orElseThrow();
    if (!Set.of("PHOTO", "SPIN").contains(kind))
      throw new IllegalArgumentException("Loại ảnh không hợp lệ.");
    if (color != null && color.isBlank()) color = null;
    final String selectedColor = color;
    if (color != null
        && product.getVariants().stream().noneMatch(v -> selectedColor.equals(v.getColor())))
      throw new IllegalArgumentException("Màu không thuộc sản phẩm.");
    files =
        files.stream()
            .filter(f -> !f.isEmpty())
            .sorted(Comparator.comparing(f -> Objects.toString(f.getOriginalFilename(), "")))
            .toList();
    if (files.isEmpty()
        || files.size() > ("SPIN".equals(kind) ? 120 : 20)
        || ("SPIN".equals(kind) && files.size() < 12))
      throw new IllegalArgumentException(
          "Ảnh thường: 1–20 tệp. Bộ xoay: 12–120 ảnh đánh số thứ tự.");
    List<String> uploaded = new ArrayList<>();
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCompletion(int status) {
            if (status != STATUS_COMMITTED) for (String path : uploaded) deleteQuietly(path);
          }
        });
    try {
      var existing = media.findByProduct_IdOrderBySortOrderAscIdAsc(productId);
      int order = existing.stream().mapToInt(ProductMedia::getSortOrder).max().orElse(0) + 1;
      for (var file : files) {
        String path = storage.store(file);
        uploaded.add(path);
        ProductMedia entry = new ProductMedia();
        entry.setProduct(product);
        entry.setColor(color);
        entry.setKind(kind);
        entry.setPath(path);
        entry.setSortOrder(order++);
        entry.setCaption(color);
        media.save(entry);
      }
      media.flush();
      // A new spin upload replaces, rather than concatenates, the previous revolution.
      if ("SPIN".equals(kind))
        for (var entry : existing)
          if (kind.equals(entry.getKind()) && Objects.equals(color, entry.getColor())) {
            media.delete(entry);
            deleteAfterCommit(entry.getPath());
          }
    } catch (IOException | RuntimeException e) {
      for (String path : uploaded) deleteQuietly(path);
      throw e;
    }
  }

  @Transactional
  public void delete(Long productId, Long id) throws IOException {
    ProductMedia entry =
        media
            .findById(id)
            .filter(m -> m.getProduct().getId().equals(productId))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh."));
    media.delete(entry);
    media.flush();
    deleteAfterCommit(entry.getPath());
  }

  private void deleteAfterCommit(String path) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            deleteQuietly(path);
          }
        });
  }

  private void deleteQuietly(String path) {
    try {
      storage.delete(path);
    } catch (IOException error) {
      org.slf4j.LoggerFactory.getLogger(ProductMediaService.class)
          .warn("Could not remove unused product media", error);
    }
  }
}
