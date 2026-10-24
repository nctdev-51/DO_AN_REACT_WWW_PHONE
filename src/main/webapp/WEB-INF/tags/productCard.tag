<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="product" required="true" type="iuh.fit.www_phone_ver1.entity.Product" %>
<%@ attribute name="selectedVariant" required="false" type="iuh.fit.www_phone_ver1.entity.ProductVariant" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:set var="initial" value="${empty selectedVariant ? product.defaultVariant : selectedVariant}"/>
<article class="product-card" data-selected-variant="${initial.id}" data-product-card data-product-url="${pageContext.request.contextPath}/products/${product.id}" data-original-price="${product.originalPrice}">
  <a data-product-link class="product-card-image" href="${pageContext.request.contextPath}/products/${product.id}?variantId=${initial.id}">
    <img src="${pageContext.request.contextPath}${empty initial ? product.image : initial.image}" alt="<c:out value='${product.name}'/>" loading="lazy">
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
    <p class="muted small" data-selected-caption><c:out value="${initial.storage}"/> · <c:out value="${initial.color}"/></p>
    <div class="price" data-card-price>
      <ui:money value="${empty initial ? product.price : initial.price}"/>
    </div>
    <div class="card-old-price">
      <del class="muted small" data-card-original ${product.originalPrice > product.price ? '' : 'hidden'}>
        <ui:money value="${product.originalPrice}"/>
      </del>
    <span data-card-discount></span></div>
    <div class="card-bottom">
      <span data-card-stock class="${initial.stock > 0 ? 'stock-text' : 'text-danger'}">${initial.stock > 0 ? 'Còn hàng' : 'Tạm hết hàng'}</span>
      <a data-product-link href="${pageContext.request.contextPath}/products/${product.id}?variantId=${initial.id}" aria-label="Xem chi tiết">→</a>
    </div>
    <form class="mt-3" action="${pageContext.request.contextPath}/compare/add" method="post">
      <ui:csrf/><input type="hidden" name="productId" value="${product.id}"><input type="hidden" name="variantId" data-card-comparison value="${initial.id}" ${empty initial ? 'disabled' : ''}>
      <button class="btn btn-sm btn-outline-dark w-100"><ui:icon name="compare"/> So sánh</button>
    </form>
  </div>
</article>
