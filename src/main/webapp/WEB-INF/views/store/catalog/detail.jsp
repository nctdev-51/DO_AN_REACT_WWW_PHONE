<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<nav class="breadcrumb-line">
  <a href="${pageContext.request.contextPath}/products">Sản phẩm</a>
  <span>/</span>
  <c:out value="${product.name}"/>
</nav>
<section class="product-detail">
  <jsp:include page="/WEB-INF/views/store/catalog/gallery.jsp" />
  <div class="product-info">
    <span class="eyebrow">
      <c:out value="${product.brand}"/>
    </span>
    <h1 data-reveal>
      <c:out value="${product.name}"/>
    </h1>
    <p class="muted">SKU: <c:out value="${product.sku}"/> · Bảo hành ${product.warrantyMonths} tháng</p>
    <p class="rating-line"><c:choose><c:when test="${empty reviews}"><a href="#reviews">Chưa có đánh giá</a></c:when><c:otherwise>★ <fmt:formatNumber value="${averageRating}" maxFractionDigits="1"/> / 5 · <a href="#reviews">${reviews.size()} đánh giá</a></c:otherwise></c:choose>
    </p>
    <div class="price" id="product-price" data-reveal>
      <ui:money value="${product.price}"/>
    </div>
    <c:if test="${product.originalPrice > product.price}">
      <del id="product-original-price" class="muted" data-original-price="${product.originalPrice}">
        <ui:money value="${product.originalPrice}"/>
      </del>
    </c:if>
    <form id="product-purchase" class="panel mt-4" action="${pageContext.request.contextPath}/cart/add" method="post">
      <ui:csrf/>
      <label for="variant-select">Chọn phiên bản</label>
      <select id="variant-select" name="variantId" class="form-select mb-3" required>
        <c:forEach var="variant" items="${variants}">
          <option value="${variant.id}" data-price="${variant.price}" data-stock="${variant.stock}" data-color="<c:out value='${variant.color}'/>" data-storage="<c:out value='${variant.storage}'/>" data-ram="<c:out value='${variant.ram}'/>" data-image="${pageContext.request.contextPath}${variant.image}" ${variant.id == selectedVariantId ? 'selected' : ''}>
            <c:out value="${variant.label}"/> — ${variant.stock > 0 ? 'Còn hàng' : 'Hết hàng'}</option>
            </c:forEach>
          </select>
          <p id="variant-stock" class="stock-text">Còn ${product.stock} sản phẩm</p>
          <div class="d-flex gap-3">
            <input id="buy-quantity" class="form-control quantity-input" type="number" name="quantity" value="1" min="1" max="99" aria-label="Số lượng">
            <button id="buy-button" class="btn btn-brand flex-grow-1" ${product.stock == 0 ? 'disabled' : ''}>Thêm vào giỏ hàng</button>
          </div>
          <button class="btn btn-outline-dark w-100 mt-3" name="buyNow" value="true" data-buy-now>Mua ngay phiên bản này</button>
        </form>
        <div class="quick-links"><a href="${pageContext.request.contextPath}/account/consultations?productId=${product.id}&amp;type=INSTALLMENT">Tư vấn trả góp →</a><a href="${pageContext.request.contextPath}/account/consultations?productId=${product.id}&amp;type=TRADE_IN">Thu cũ đổi mới →</a></div>
        <form action="${pageContext.request.contextPath}/compare/add" method="post" class="mt-3">
          <ui:csrf/><input type="hidden" name="productId" value="${product.id}">
          <c:if test="${not empty variants}"><input type="hidden" name="variantId" value="${selectedVariantId}" data-comparison-variant></c:if>
          <button class="btn btn-outline-dark">+ Thêm vào so sánh</button>
        </form>
        <c:choose><c:when test="${pageContext.request.userPrincipal != null}"><form action="${pageContext.request.contextPath}/account/wishlist/add" method="post" class="mt-2"><ui:csrf/><input type="hidden" name="productId" value="${product.id}"><button class="btn btn-outline-dark">♡ Lưu yêu thích</button></form></c:when><c:otherwise><p class="mt-2"><a href="${pageContext.request.contextPath}/login">Đăng nhập để lưu sản phẩm yêu thích</a></p></c:otherwise></c:choose>
        <div class="policy-grid">
          <span>Thanh toán khi nhận hàng</span>
          <span>Theo dõi đơn trực tuyến</span>
          <span>Bảo hành ${product.warrantyMonths} tháng</span>
        </div>
      </div>
    </section>
    <section class="detail-sections">
      <article class="panel">
        <h2>Thông tin sản phẩm</h2>
        <p class="description">
          <c:out value="${product.description}"/>
        </p>
      </article>
      <article class="panel">
        <h2>Thông số kỹ thuật</h2>
        <dl class="spec-list">
          <dt>Màn hình</dt>
          <dd>
            <c:out value="${empty product.display ? 'Đang cập nhật' : product.display}"/>
          </dd>
          <dt>Chip xử lý</dt>
          <dd>
            <c:out value="${empty product.chipset ? 'Đang cập nhật' : product.chipset}"/>
          </dd>
          <dt>Camera sau</dt>
          <dd>
            <c:out value="${empty product.camera ? 'Đang cập nhật' : product.camera}"/>
          </dd>
          <dt>Pin</dt>
          <dd>
            <c:out value="${empty product.battery ? 'Đang cập nhật' : product.battery}"/>
          </dd>
          <dt>Hệ điều hành</dt>
          <dd>
            <c:out value="${empty product.operatingSystem ? 'Đang cập nhật' : product.operatingSystem}"/>
          </dd>
        </dl>
      </article>
    </section>
    <section class="panel" id="reviews">
      <h2>Đánh giá từ khách đã mua</h2>
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
          <textarea id="review-content" name="content" class="form-control" required maxlength="2000" rows="3">
          </textarea>
          <button class="btn btn-brand mt-3">Gửi / cập nhật đánh giá</button>
        </form>
      </c:if>
      <c:if test="${not canReview}">
        <p class="muted">Bạn có thể đánh giá sau khi mua và nhận sản phẩm này.</p>
      </c:if>
      <c:forEach var="review" items="${reviews}">
        <article class="review">
          <strong>
            <c:out value="${review.user.fullName}"/>
          </strong>
          <span class="rating-line">${review.rating} / 5 ★ · Đã mua hàng</span>
          <p>
            <c:out value="${review.content}"/>
          </p>
        </article>
      </c:forEach>
      <c:if test="${empty reviews}">
        <p class="muted">Sản phẩm chưa có đánh giá.</p>
      </c:if>
    </section>
    <c:if test="${not empty accessories}"><section class="collection-section"><div class="section-heading"><div><span class="eyebrow">DÙNG CÙNG THIẾT BỊ</span><h2>Thêm lựa chọn cho máy của bạn.</h2></div><a href="${pageContext.request.contextPath}/products?kind=ACCESSORY">Xem phụ kiện →</a></div><p class="muted">Kiểm tra chuẩn kết nối và điều kiện sử dụng trong mô tả. Giá bán kèm là tổng giá từng sản phẩm; mã combo chỉ giảm khi đủ điều kiện.</p><div class="product-grid"><c:forEach var="a" items="${accessories}"><ui:productCard product="${a}"/></c:forEach></div></section></c:if>
    <div class="section-heading">
      <h2>Có thể bạn cũng thích</h2>
    </div>
    <div class="product-grid">
      <c:forEach var="related" items="${relatedProducts}">
        <ui:productCard product="${related}"/>
      </c:forEach>
    </div>
    <aside class="sticky-purchase" aria-label="Mua sản phẩm đang xem"><div><strong><c:out value="${product.name}"/></strong><span data-sticky-variant></span></div><strong data-sticky-price><ui:money value="${product.price}"/></strong><button class="btn btn-brand" type="submit" form="product-purchase" name="buyNow" value="true" data-buy-now>Mua ngay</button></aside>
    <jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
