package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.dto.catalog.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CatalogService {
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ProductVariantRepository variants;
  private final OrderDetailRepository details;
  private final ReviewRepository reviews;
  private final ProductImageStorageService images;
  private final WishlistRepository wishlist;
  private final ProductMediaRepository media;
  private final ConsultationRepository consultations;
  private final CampaignBannerRepository banners;

  public CatalogService(
      ProductRepository products,
      CategoryRepository categories,
      ProductVariantRepository variants,
      OrderDetailRepository details,
      ReviewRepository reviews,
      ProductImageStorageService images,
      WishlistRepository wishlist,
      ProductMediaRepository media,
      ConsultationRepository consultations,
      CampaignBannerRepository banners) {
    this.products = products;
    this.categories = categories;
    this.variants = variants;
    this.details = details;
    this.reviews = reviews;
    this.images = images;
    this.wishlist = wishlist;
    this.media = media;
    this.consultations = consultations;
    this.banners = banners;
  }

  public Product product(Long id) {
    return products
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."));
  }

  @Transactional
  public void saveProduct(Long id, ProductForm form, MultipartFile image) throws IOException {
    Product product = id == null ? new Product() : product(id);
    Category category =
        categories
            .findById(form.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));
    String nextImage = image == null || image.isEmpty() ? product.getImage() : images.store(image);
    String previous = product.getImage();
    BeanUtils.copyProperties(form, product, "categoryId", "sku");
    product.setName(form.getName().trim());
    product.setBrand(form.getBrand().trim());
    product.setSku(
        form.getSku() == null || form.getSku().isBlank()
            ? (product.getSku() == null
                ? "PS-" + UUID.randomUUID().toString().substring(0, 8)
                : product.getSku())
            : form.getSku().trim());
    product.setCategory(category);
    product.setImage(nextImage);
    try {
      products.saveAndFlush(product);
      updateMinimumPrice(product);
    } catch (RuntimeException error) {
      if (!Objects.equals(previous, nextImage)) images.delete(nextImage);
      throw error;
    }
    if (!Objects.equals(previous, nextImage)) images.delete(previous);
  }

  @Transactional
  public boolean deleteProduct(Long id) throws IOException {
    Product product = product(id);
    if (details.existsByProduct_Id(id)
        || consultations.countByProduct_Id(id) > 0
        || banners.countByProduct_Id(id) > 0) {
      product.setStatus(0);
      return false;
    }
    reviews.deleteByProduct_Id(id);
    wishlist.deleteByProduct_Id(id);
    var mediaPaths =
        media.findByProduct_IdOrderBySortOrderAscIdAsc(id).stream()
            .map(ProductMedia::getPath)
            .toList();
    media.deleteByProduct_Id(id);
    products.delete(product);
    products.flush();
    images.delete(product.getImage());
    for (String path : mediaPaths) images.delete(path);
    return true;
  }

  @Transactional
  public void saveCategory(Long id, CategoryForm form) {
    Category category =
        id == null
            ? new Category()
            : categories
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục."));
    category.setName(form.getName().trim());
    category.setDescription(form.getDescription());
    categories.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    if (products.countByCategoryId(id) > 0)
      throw new IllegalArgumentException("Không thể xóa danh mục đang có sản phẩm.");
    categories.deleteById(id);
  }

  @Transactional
  public void saveVariant(Long productId, Long id, VariantForm form) {
    Product product = product(productId);
    ProductVariant variant =
        id == null
            ? new ProductVariant()
            : variants.lockAll(List.of(id)).stream()
                .filter(v -> v.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên bản."));
    if (id != null && (form.getVersion() == null || form.getVersion() != variant.getVersion()))
      throw new IllegalArgumentException(
          "Tồn kho vừa thay đổi. Vui lòng tải lại trang trước khi sửa.");
    BeanUtils.copyProperties(form, variant, "version");
    variant.setProduct(product);
    variants.saveAndFlush(variant);
    updateMinimumPrice(product);
  }

  @Transactional
  public void disableVariant(Long productId, Long id) {
    ProductVariant variant =
        variants
            .findById(id)
            .filter(v -> v.getProduct().getId().equals(productId))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên bản."));
    variant.setActive(false);
    variants.flush();
    updateMinimumPrice(variant.getProduct());
  }

  private void updateMinimumPrice(Product product) {
    variants.findByProduct_IdOrderByPriceAscIdAsc(product.getId()).stream()
        .filter(ProductVariant::isActive)
        .map(ProductVariant::getPrice)
        .min(BigDecimal::compareTo)
        .ifPresent(product::setPrice);
  }
}
