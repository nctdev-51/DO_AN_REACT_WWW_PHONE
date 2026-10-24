package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.entity.Review;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
  private final ReviewRepository reviews;
  private final OrderDetailRepository details;
  private final UserRepository users;
  private final ProductRepository products;

  public ReviewService(
      ReviewRepository reviews,
      OrderDetailRepository details,
      UserRepository users,
      ProductRepository products) {
    this.reviews = reviews;
    this.details = details;
    this.users = users;
    this.products = products;
  }

  public boolean canReview(String email, Long productId) {
    return details.existsByProduct_IdAndOrder_User_EmailAndOrder_Status(
        productId, email, OrderStatuses.DELIVERED);
  }

  @Transactional
  public void save(String email, Long productId, int rating, String content) {
    if (!canReview(email, productId))
      throw new IllegalArgumentException(
          "Chỉ khách đã mua và nhận hàng mới được đánh giá sản phẩm.");
    if (rating < 1 || rating > 5 || content == null || content.isBlank() || content.length() > 2000)
      throw new IllegalArgumentException("Đánh giá cần 1–5 sao và nội dung từ 1–2000 ký tự.");
    users.lockByEmail(email).orElseThrow();
    Review review = reviews.findByUser_EmailAndProduct_Id(email, productId).orElseGet(Review::new);
    review.setUser(users.findByEmail(email).orElseThrow());
    review.setProduct(products.findById(productId).orElseThrow());
    review.setRating(rating);
    review.setContent(content.trim());
    reviews.save(review);
  }
}
