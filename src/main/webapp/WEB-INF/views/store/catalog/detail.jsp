<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="detail-top-bar">
  <ui:breadcrumb sectionLabel="Sản phẩm" sectionHref="/products" currentLabel="${product.name}"/>
  <a class="small" href="${pageContext.request.contextPath}/products" data-catalog-back>← Quay lại danh sách sản phẩm</a>
</div>
<section class="product-detail">
  <jsp:include page="/WEB-INF/views/store/catalog/gallery.jsp" />
  <div class="product-info">
    <span class="eyebrow">
      <c:out value="${product.brand}"/>
    </span>
    <h1 data-reveal>
      <c:out value="${product.name}"/>
    </h1>
    <p class="muted">SKU: <c:out value="${product.sku}"/> · Bảo hành ${product.warrantyMonths} tháng chính hãng</p>
    <p class="rating-line">
      <c:choose>
        <c:when test="${empty reviews}">
          <a href="#reviews">Chưa có đánh giá · Mua hàng để đánh giá đầu tiên</a>
        </c:when>
        <c:otherwise>
          ★ <fmt:formatNumber value="${averageRating}" maxFractionDigits="1"/> / 5 · <a href="#reviews">${reviews.size()} đánh giá đã xác thực</a>
        </c:otherwise>
      </c:choose>
    </p>
    <div class="price-transparency-box">
      <div class="price" id="product-price" data-reveal>
        <ui:money value="${product.price}"/>
      </div>
      <c:if test="${product.originalPrice > product.price}">
        <del id="product-original-price" class="muted" data-original-price="${product.originalPrice}">
          <ui:money value="${product.originalPrice}"/>
        </del>
      </c:if>
      <p class="installment-hint" data-installment-summary>
        Trả góp 0% tham khảo từ <strong><ui:money value="${installment12Monthly}"/>/tháng</strong> (12 kỳ) hoặc <strong><ui:money value="${installment6Monthly}"/>/tháng</strong> (6 kỳ).
        <span class="muted d-block small">Tạm tính theo giá phiên bản; điều kiện duyệt hồ sơ xác nhận khi tư vấn.</span>
      </p>
    </div>

    <c:if test="${not empty applicablePromotions}">
      <div class="panel promo-offer-box mt-3">
        <strong class="d-block mb-2">Ưu đãi &amp; mã giảm giá áp dụng</strong>
        <c:forEach var="promo" items="${applicablePromotions}">
          <div class="promo-offer-row">
            <div>
              <span class="coupon-code"><c:out value="${promo.code}"/></span>
              <span class="small d-block"><c:out value="${promo.description}"/> · <span class="muted">HSD: <c:out value="${promo.endsAtLabel}"/></span></span>
            </div>
            <button type="button" class="btn btn-light btn-sm" data-copy-code="<c:out value='${promo.code}'/>">Sao chép mã</button>
          </div>
          <span class="small muted d-block mb-1" data-copy-status></span>
        </c:forEach>
        <small class="muted d-block">Mỗi đơn áp dụng 1 mã giảm giá tại bước Thanh toán.</small>
      </div>
    </c:if>

    <form id="product-purchase" class="panel mt-4" action="${pageContext.request.contextPath}/cart/add" method="post">
      <ui:csrf/>
      <label for="variant-select">Chọn phiên bản</label>
      <select id="variant-select" name="variantId" class="form-select mb-3" required>
        <c:forEach var="variant" items="${variants}">
          <option value="${variant.id}" data-price="${variant.price}" data-stock="${variant.stock}" data-color="<c:out value='${variant.color}'/>" data-storage="<c:out value='${variant.storage}'/>" data-ram="<c:out value='${variant.ram}'/>" data-image="${pageContext.request.contextPath}${variant.image}" ${variant.id == selectedVariantId ? 'selected' : ''}>
            <c:out value="${variant.label}"/> — ${variant.stock > 0 ? 'Còn hàng' : 'Hết hàng'}
          </option>
        </c:forEach>
      </select>
      <p id="variant-stock" class="stock-text">Còn ${product.stock} sản phẩm</p>
      <div class="d-flex gap-3">
        <input id="buy-quantity" class="form-control quantity-input" type="number" name="quantity" value="1" min="1" max="99" aria-label="Số lượng">
        <button id="buy-button" class="btn btn-brand flex-grow-1" ${product.stock == 0 ? 'disabled' : ''}>Thêm vào giỏ hàng</button>
      </div>
      <button class="btn btn-outline-dark w-100 mt-3" name="buyNow" value="true" data-buy-now>Mua ngay phiên bản này</button>
    </form>

    <div class="detail-secondary-actions mt-3">
      <form action="${pageContext.request.contextPath}/compare/add" method="post" data-compare-form data-api-action="${pageContext.request.contextPath}/compare/api/add">
        <ui:csrf/><input type="hidden" name="productId" value="${product.id}">
        <c:if test="${not empty variants}"><input type="hidden" name="variantId" value="${selectedVariantId}" data-comparison-variant></c:if>
        <button class="btn btn-outline-dark w-100"><ui:icon name="compare"/> + Thêm vào so sánh</button>
      </form>
      <a class="btn btn-light" href="${pageContext.request.contextPath}/account/consultations?productId=${product.id}&amp;type=INSTALLMENT">Tư vấn trả góp 0%</a>
      <a class="btn btn-light" href="${pageContext.request.contextPath}/account/consultations?productId=${product.id}&amp;type=TRADE_IN">Thu cũ đổi mới</a>
    </div>
    <c:choose>
      <c:when test="${pageContext.request.userPrincipal != null}">
        <form action="${pageContext.request.contextPath}/account/wishlist/add" method="post" class="mt-2">
          <ui:csrf/><input type="hidden" name="productId" value="${product.id}">
          <button class="btn btn-outline-dark">♡ Lưu yêu thích</button>
        </form>
      </c:when>
      <c:otherwise>
        <p class="mt-2"><a href="${pageContext.request.contextPath}/login?redirect=/products/${product.id}">Đăng nhập để lưu sản phẩm yêu thích</a></p>
      </c:otherwise>
    </c:choose>

    <div class="panel delivery-estimator mt-3" data-delivery-estimator data-endpoint="${pageContext.request.contextPath}/products/${product.id}/delivery-estimate">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <strong>Ước tính giao hàng theo khu vực</strong>
        <a class="small" href="${pageContext.request.contextPath}/policies/shipping">Chính sách giao hàng</a>
      </div>
      <div class="mt-2">
        <label for="delivery-province" class="small muted">Chọn tỉnh / thành phố nhận hàng</label>
        <select id="delivery-province" class="form-select" data-delivery-province>
          <option value="">Chọn tỉnh / thành phố</option>
          <c:forEach var="p" items="${locationProvinces}">
            <option value="${p.code}"><c:out value="${p.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <p class="small mt-2 mb-0" data-delivery-output aria-live="polite">
        Giao tiêu chuẩn dự kiến ${experience.deliveryMinDays}–${experience.deliveryMaxDays} ngày. Miễn phí vận chuyển cho đơn từ <ui:money value="${storePolicy.freeShippingThreshold}"/>.
      </p>
    </div>

    <div class="policy-grid">
      <span>COD hoặc VNPay bảo mật</span>
      <span>Đổi trả theo chính sách ${storePolicy.returnWindowDays} ngày</span>
      <span>Bảo hành ${product.warrantyMonths} tháng</span>
    </div>
  </div>
</section>

<section class="detail-sections">
  <article class="panel">
    <h2>Thông tin sản phẩm &amp; Phù hợp với ai</h2>
    <c:if test="${not empty usageFitReasons}">
      <ul class="usage-fit-list mb-3">
        <c:forEach var="fit" items="${usageFitReasons}">
          <li><c:out value="${fit}"/></li>
        </c:forEach>
      </ul>
    </c:if>
    <p class="description">
      <c:out value="${product.description}"/>
    </p>
  </article>
  <article class="panel">
    <h2>Thông số kỹ thuật</h2>
    <dl class="spec-list">
      <dt>Màn hình</dt>
      <dd><c:out value="${empty product.display ? 'Đang cập nhật' : product.display}"/></dd>
      <dt>Chip xử lý</dt>
      <dd><c:out value="${empty product.chipset ? 'Đang cập nhật' : product.chipset}"/></dd>
      <dt>Camera sau</dt>
      <dd><c:out value="${empty product.camera ? 'Đang cập nhật' : product.camera}"/></dd>
      <dt>Pin</dt>
      <dd><c:out value="${empty product.battery ? 'Đang cập nhật' : product.battery}"/></dd>
      <dt>Hệ điều hành</dt>
      <dd><c:out value="${empty product.operatingSystem ? 'Đang cập nhật' : product.operatingSystem}"/></dd>
    </dl>
  </article>
</section>

<section class="panel" id="reviews">
  <h2>Đánh giá từ khách đã mua</h2>
  <c:if test="${not empty reviews}">
    <div class="rating-summary">
      <div class="rating-score-box">
        <div class="rating-big-number"><fmt:formatNumber value="${averageRating}" maxFractionDigits="1"/></div>
        <div class="rating-stars-display" aria-label="Điểm trung bình ${averageRating} trên 5">
          <c:forEach begin="1" end="5" var="s">
            <span class="${averageRating >= s - 0.3 ? 'is-filled' : ''}">★</span>
          </c:forEach>
        </div>
        <span class="muted small">${reviews.size()} đánh giá đã xác thực</span>
      </div>
      <div class="rating-bars">
        <c:forEach begin="1" end="5" var="step">
          <c:set var="star" value="${6 - step}"/>
          <c:set var="starCount" value="${ratingCounts[star]}"/>
          <c:set var="starPct" value="${(starCount * 100) / reviews.size()}"/>
          <div class="rating-bar-row">
            <span class="rating-bar-label">${star} ★</span>
            <div class="rating-bar-track" role="progressbar" aria-valuenow="${starPct}" aria-valuemin="0" aria-valuemax="100">
              <div class="rating-bar-fill" style="width: ${starPct}%"></div>
            </div>
            <span class="rating-bar-count">${starCount}</span>
          </div>
        </c:forEach>
      </div>
    </div>
  </c:if>
  <c:if test="${canReview}">
    <form action="${pageContext.request.contextPath}/products/${product.id}/reviews" method="post" class="review-form">
      <ui:csrf/>
      <label for="rating">Số sao</label>
      <select id="rating" name="rating" class="form-select">
        <c:forEach begin="1" end="5" var="star">
          <option value="${star}" ${star == 5 ? 'selected' : ''}>${star} sao</option>
        </c:forEach>
      </select>
      <label for="review-content">Trải nghiệm của bạn</label>
      <textarea id="review-content" name="content" class="form-control" required maxlength="2000" rows="3"></textarea>
      <button class="btn btn-brand mt-3">Gửi / cập nhật đánh giá</button>
    </form>
  </c:if>
  <c:if test="${not canReview}">
    <p class="muted">Bạn có thể đánh giá sau khi mua và nhận sản phẩm này.</p>
  </c:if>
  <c:forEach var="review" items="${reviews}">
    <article class="review">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <strong><c:out value="${review.user.fullName}"/></strong>
        <span class="status-badge">Đã mua sản phẩm tại PTV Mobile</span>
      </div>
      <span class="rating-line">${review.rating} / 5 ★</span>
      <p><c:out value="${review.content}"/></p>
    </article>
  </c:forEach>
  <c:if test="${empty reviews}">
    <p class="muted">Sản phẩm chưa có đánh giá.</p>
  </c:if>
</section>

<c:if test="${not empty accessories}">
  <section class="collection-section">
    <div class="section-heading">
      <div>
        <span class="eyebrow">DÙNG CÙNG THIẾT BỊ</span>
        <h2>Phụ kiện tương thích cho máy của bạn</h2>
      </div>
      <a href="${pageContext.request.contextPath}/products?kind=ACCESSORY">Xem phụ kiện →</a>
    </div>
    <p class="muted">Kiểm tra chuẩn kết nối và điều kiện sử dụng trong mô tả. Giá bán kèm là tổng giá từng sản phẩm; mã combo chỉ giảm khi đủ điều kiện.</p>
    <div class="product-grid">
      <c:forEach var="a" items="${accessories}"><ui:productCard product="${a}"/></c:forEach>
    </div>
  </section>
</c:if>

<div class="section-heading">
  <h2>Có thể bạn cũng thích</h2>
</div>
<div class="product-grid">
  <c:forEach var="related" items="${relatedProducts}">
    <ui:productCard product="${related}"/>
  </c:forEach>
</div>

<c:if test="${not empty recentProducts}">
  <section class="collection-section">
    <div class="section-heading">
      <div>
        <span class="eyebrow">ĐÃ XEM GẦN ĐÂY</span>
        <h2>Các máy bạn vừa xem</h2>
      </div>
      <a href="${pageContext.request.contextPath}/recently-viewed">Xem tất cả →</a>
    </div>
    <div class="product-grid">
      <c:forEach var="recent" items="${recentProducts}">
        <ui:productCard product="${recent}"/>
      </c:forEach>
    </div>
  </section>
</c:if>

<aside class="sticky-purchase" aria-label="Mua sản phẩm đang xem">
  <img class="sticky-thumb" data-sticky-thumb src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>">
  <div><strong><c:out value="${product.name}"/></strong><span data-sticky-variant></span></div>
  <strong data-sticky-price><ui:money value="${product.price}"/></strong>
  <button class="btn btn-brand" type="submit" form="product-purchase" name="buyNow" value="true" data-buy-now>Mua ngay</button>
</aside>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
