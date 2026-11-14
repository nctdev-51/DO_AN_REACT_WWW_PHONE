<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="product" required="true" type="iuh.fit.www_phone_ver1.entity.Product" %>
<%@ attribute name="selectedVariant" required="false" type="iuh.fit.www_phone_ver1.entity.ProductVariant" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:set var="initial" value="${empty selectedVariant ? product.defaultVariant : selectedVariant}"/>
<c:set var="primaryImg" value="${empty initial ? product.image : initial.image}"/>
<c:set var="displayPrice" value="${empty initial ? product.price : initial.price}"/>
<c:set var="hoverImg" value=""/>
<c:forEach var="m" items="${product.media}">
  <c:if test="${empty hoverImg and m.kind == 'PHOTO' and m.path != primaryImg}">
    <c:set var="hoverImg" value="${m.path}"/>
  </c:if>
</c:forEach>
<c:if test="${empty hoverImg}">
  <c:forEach var="v" items="${product.variants}">
    <c:if test="${empty hoverImg and v.active and v.image != primaryImg}">
      <c:set var="hoverImg" value="${v.image}"/>
    </c:if>
  </c:forEach>
</c:if>
<c:set var="isNewModel" value="${fn:contains(product.name, '17') or fn:contains(product.name, 'iPhone Air') or fn:contains(product.name, 'S25') or fn:contains(product.name, 'Fold7') or fn:contains(product.name, 'Flip7') or fn:contains(product.name, 'Find X8') or fn:contains(product.name, 'Xiaomi 15') or fn:contains(product.name, 'HONOR 400')}"/>
<c:set var="hasDiscount" value="${not empty product.originalPrice and product.originalPrice > displayPrice}"/>
<article class="product-card" data-selected-variant="${initial.id}" data-product-card data-product-url="${pageContext.request.contextPath}/products/${product.id}" data-original-price="${product.originalPrice}">
  <a data-product-link class="product-card-image ${not empty hoverImg ? 'has-hover-image' : ''}" href="${pageContext.request.contextPath}/products/${product.id}?variantId=${initial.id}">
    <div class="card-badges">
      <c:if test="${isNewModel}">
        <span class="card-badge card-badge-new">MỚI</span>
      </c:if>
      <c:if test="${not empty soldQuantities and soldQuantities[product.id] >= 50}">
        <span class="card-badge card-badge-popular">BÁN CHẠY</span>
      </c:if>
      <c:if test="${not empty initial and initial.stock > 0 and initial.stock <= 3}">
        <span class="card-badge card-badge-low" data-card-badge-low>SẮP HẾT</span>
      </c:if>
      <span class="card-badge card-badge-sale" data-card-badge-sale ${hasDiscount ? '' : 'hidden'}>
        <c:if test="${hasDiscount}">
          −<fmt:formatNumber value="${(1 - (displayPrice / product.originalPrice)) * 100}" maxFractionDigits="0"/>%
        </c:if>
      </span>
    </div>
    <img class="img-primary" src="${pageContext.request.contextPath}${primaryImg}" alt="<c:out value='${product.name}'/>" loading="lazy">
    <c:if test="${not empty hoverImg}">
      <img class="img-hover" src="${pageContext.request.contextPath}${hoverImg}" alt="" loading="lazy" aria-hidden="true">
    </c:if>
  </a>
  <div class="product-card-body">
    <span class="eyebrow">
      <c:out value="${product.brand}"/>
    </span>
    <h3>
      <a data-product-link href="${pageContext.request.contextPath}/products/${product.id}?variantId=${initial.id}">
        <c:out value="${product.name}"/>
      </a>
    </h3>
    <p class="compact-spec">
      <c:out value="${product.chipset}"/>
    </p>
    <div class="card-variant-options" data-variant-options aria-label="Chọn phiên bản">
      <c:forEach var="variant" items="${product.variants}"><c:if test="${variant.active}">
        <span hidden data-variant data-id="${variant.id}" data-color="<c:out value='${variant.color}'/>" data-storage="<c:out value='${variant.storage}'/>" data-ram="<c:out value='${variant.ram}'/>" data-price="${variant.price}" data-stock="${variant.stock}" data-image="${pageContext.request.contextPath}${variant.image}"></span>
      </c:if></c:forEach>
    </div>
    <p class="muted small" data-selected-caption><c:out value="${initial.storage}"/><c:if test="${not empty initial.storage and not empty initial.color}"> · </c:if><c:out value="${initial.color}"/></p>
    <div class="price" data-card-price>
      <ui:money value="${displayPrice}"/>
    </div>
    <div class="card-old-price">
      <del class="muted small" data-card-original ${hasDiscount ? '' : 'hidden'}>
        <ui:money value="${product.originalPrice}"/>
      </del>
    <span data-card-discount></span></div>
    <div class="card-bottom">
      <span data-card-stock class="${initial.stock > 0 ? 'stock-text' : 'text-danger'}">${initial.stock > 0 ? 'Còn hàng' : 'Tạm hết hàng'}</span>
      <a data-product-link href="${pageContext.request.contextPath}/products/${product.id}?variantId=${initial.id}" aria-label="Xem chi tiết">→</a>
    </div>
    <form class="mt-3" action="${pageContext.request.contextPath}/compare/add" method="post" data-compare-form data-api-action="${pageContext.request.contextPath}/compare/api/add">
      <ui:csrf/><input type="hidden" name="productId" value="${product.id}"><input type="hidden" name="variantId" data-card-comparison value="${initial.id}" ${empty initial ? 'disabled' : ''}>
      <button class="btn btn-sm btn-outline-dark w-100"><ui:icon name="compare"/> So sánh</button>
    </form>
  </div>
</article>
