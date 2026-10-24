<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="product" required="true" type="iuh.fit.www_phone_ver1.entity.Product" %>
<%@ attribute name="primary" required="false" type="java.lang.Boolean" %>
<%@ attribute name="subtitle" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<section class="showcase ${primary ? 'showcase-primary' : 'showcase-secondary'}" aria-label="<c:out value='${product.name}'/>">
  <div class="showcase-copy">
    <p class="eyebrow" data-reveal><c:out value="${product.brand}"/> · PTV MOBILE</p>
    <c:choose><c:when test="${primary}"><h1 data-reveal><c:out value="${product.name}"/></h1></c:when><c:otherwise><h2 data-reveal><c:out value="${product.name}"/></h2></c:otherwise></c:choose>
    <p class="showcase-subtitle" data-reveal><c:out value="${subtitle}"/></p>
    <p class="showcase-price" data-reveal>Từ <ui:money value="${product.price}"/></p>
    <div class="showcase-actions" data-reveal>
      <a class="btn btn-brand btn-lg" href="${pageContext.request.contextPath}/products/${product.id}">Khám phá sản phẩm</a>
      <c:url var="brandProducts" value="/products"><c:param name="brand" value="${product.brand}"/></c:url>
      <a class="btn btn-outline-dark btn-lg" href="${brandProducts}">Xem <c:out value="${product.brand}"/></a>
    </div>
  </div>
  <a class="showcase-visual" data-reveal href="${pageContext.request.contextPath}/products/${product.id}" aria-label="Xem chi tiết <c:out value='${product.name}'/>">
    <img src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" ${primary ? 'fetchpriority="high"' : 'loading="lazy"'}>
  </a>
</section>
